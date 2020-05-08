 package com.mofirouz.javapushmail;
 
 import com.sun.mail.imap.IMAPFolder;
 import com.sun.mail.imap.IMAPStore;
 import com.sun.mail.imap.SortTerm;
 
 import java.util.ArrayList;
 import java.util.EventListener;
 import java.util.Properties;
 
 import java.util.Vector;
 import javax.mail.AuthenticationFailedException;
 import javax.mail.Flags;
 import javax.mail.Folder;
 import javax.mail.FolderClosedException;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.event.MessageChangedEvent;
 import javax.mail.event.MessageChangedListener;
 import javax.mail.event.MessageCountEvent;
 import javax.mail.event.MessageCountListener;
 
 /**
  *
  * @author Mo Firouz
  * @since 2/10/11
  */
 public abstract class JavaPushMailAccount implements Runnable {
 
     public final static int READ_ONLY_FOLDER = Folder.READ_ONLY;
     public final static int READ_WRITE_FOLDER = Folder.READ_WRITE;
     private boolean connected = false;
     private boolean usePush = true;
     private String accountName;
     private String serverAddress;
     private String username;
     private String password;
     private int serverPort;
     private boolean useSSL;
     private IMAPStore server;
     private Session session;
     private IMAPFolder folder;
     private MessageCountListener messageCountListener, externalCountListener;
     private MessageChangedListener messageChangedListener, externalChangedListener;
     private NetworkProber prober;
     private MailPoller poller;
     private Thread pushThread;
 
     public JavaPushMailAccount(String accountName, String serverAddress, int serverPort, boolean useSSL) {
         this.accountName = accountName;
         this.serverAddress = serverAddress;
         this.serverPort = serverPort;
         this.useSSL = useSSL;
     }
 
     public void setCredentials(String username, String password) {
         this.username = username;
         this.password = password;
     }
 
     public void run() {
         this.initConnection();
     }
 
     public void connect() {
         try {
             server.connect(serverAddress, serverPort, username, password);
             selectFolder("");
             prober.start();
             connected = true;
             JavaPushMailLogger.info(accountName + " connected!");
             onConnect();
         } catch (AuthenticationFailedException ex) {
             connected = false;
             JavaPushMailLogger.warn(accountName, ex);
             onError(ex);
         } catch (MessagingException ex) {
             connected = false;
             folder = null;
             messageChangedListener = null;
             messageCountListener = null;
             JavaPushMailLogger.warn(accountName, ex);
             onError(ex);
         } catch (IllegalStateException ex) {
             JavaPushMailLogger.warn(accountName, ex);
             connected = true;
             onConnect();
         }
     }
 
     public void disconnect() {
         if (!connected && server == null && !server.isConnected())
             return;
 
         Thread t = new Thread(new Runnable() {
 
             public void run() {
                 try {
                     closeFolder();
                     server.close();
                     prober.stop();
                     poller.stop();
                     connected = false;
                     JavaPushMailLogger.info(accountName + " disconnected!");
                     onDisconnect();
                 } catch (Exception e) {
                     JavaPushMailLogger.debug(e);
                     onError(e);
                 }
             }
         });
         t.start();
     }
 
     public void setMessageChangedListerer(MessageChangedListener listener) {
         removeListener(externalChangedListener);
         externalChangedListener = listener;
         addListener(externalChangedListener);
     }
 
     public void setMessageCounterListerer(MessageCountListener listener) {
         removeListener(externalCountListener);
         externalCountListener = listener;
         addListener(externalCountListener);
     }
 
     private void initConnection() {
         prober = new NetworkProber(this) {
 
             @Override
             public void onNetworkChange(boolean status) {
                 if (status != connected) { // if two states do not match, something has truly changed!
                     if (status && !connected) { // if connection up, but not connected...
                         connect();
                     } else if (!status && connected) { //if previously connected, but link down... then just disconnect...
                         if (getSessionFailureCount() >= 2 || getPingFailureCount() >= 2) {
                             connected = false;
                             if (!usePush)
                                 poller.stop();
 
                             onDisconnect();
                             //connect();
                         }
                     }
                 }
             }
 
             @Override
             public void missedBeat() { // missed beat, because of going to sleep, probably?!
                 connected = false;
             }
         };
 
         poller = new MailPoller(folder) {
 
             @Override
             public void onNewMessage() {
                 try {
                     if (externalCountListener != null) {
                         externalCountListener.messagesAdded(new MessageCountEvent(folder, MessageCountEvent.ADDED, false, getNewMessages()));
                         messageCountListener.messagesAdded(new MessageCountEvent(folder, MessageCountEvent.ADDED, false, getNewMessages()));
                     }
                 } catch (Exception e) {
                     onError(e);
                 }
 
             }
         };
 
         Properties props = System.getProperties();
 
         // enable to throw out everything...
         //props.put("mail.debug", "true");
 
         String imapProtocol = "imap";
         if (useSSL) {
             imapProtocol = "imaps";
             props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
             props.setProperty("mail.imap.socketFactory.fallback", "false");
         }
         props.setProperty("mail.store.protocol", imapProtocol);
         session = Session.getDefaultInstance(props, null);
         try {
             server = (IMAPStore) session.getStore(imapProtocol);
             connect();
         } catch (MessagingException ex) {
             JavaPushMailLogger.debug(ex);
             onError(ex);
         }
     }
 
     private void selectFolder(String folderName) {
         try {
             closeFolder();
             if (folderName.equalsIgnoreCase("")) {
                 folder = (IMAPFolder) server.getFolder("INBOX");
             } else {
                 folder = (IMAPFolder) server.getFolder(folderName);
             }
             openFolder();
         } catch (MessagingException ex) {
             JavaPushMailLogger.debug(ex);
             onError(ex);
         } catch (IllegalStateException ex) {
             JavaPushMailLogger.debug(ex);
         }
     }
 
     private void openFolder() throws MessagingException {
         if (folder == null)
             return;
 
         folder.open(Folder.READ_ONLY);
         folder.setSubscribed(true);
         removeAllListenersFromFolder();
         addAllListenersFromFolder();
         poller.setFolder(folder);
 
         if (usePush)
             usePush();
         else
             poller.start(accountName);
     }
 
     private void closeFolder() throws MessagingException {
         if (folder == null || !folder.isOpen())
             return;
 
         removeAllListenersFromFolder();
         folder.setSubscribed(false);
         folder.close(false);
         folder = null;
     }
 
     private void usePush() {
         if (folder == null || !usePush)
             return;
 
         Runnable r = new Runnable() {
 
             public void run() {
                 try {
                     folder.idle(false);
                 } catch (FolderClosedException e) {
                     JavaPushMailLogger.warn("Push Error: [DISCONNECT] " + accountName, e);
                     usePush = true;
                     selectFolder("");
                 } catch (MessagingException e) {
                     JavaPushMailLogger.warn("Push Error: [NO IDLE] " + accountName, e);
                     usePush = false;
                     selectFolder("");
                 } catch (Exception e) {
                     JavaPushMailLogger.warn("Push Error: [UNKNOWN] " + accountName, e);
                     usePush = false;
                     selectFolder("");
                 }
             }
         };
         pushThread = new Thread(r, "Push-" + accountName);
         pushThread.setDaemon(true);
         pushThread.start();
     }
 
     private void removeAllListenersFromFolder() {
         removeListener(externalChangedListener);
         removeListener(externalCountListener);
     }
 
     private void removeListener(EventListener listener) {
         if (listener == null || folder == null) {
             return;
         }
 
         if (listener instanceof MessageChangedListener) {
             folder.removeMessageChangedListener((MessageChangedListener) listener);
         } else {
             if (listener instanceof MessageCountListener) {
                 folder.removeMessageCountListener((MessageCountListener) listener);
             }
         }
     }
 
     private void addAllListenersFromFolder() {
         addListener(externalCountListener);
         addListener(externalChangedListener);
     }
 
     private void addListener(EventListener listener) {
         if (listener == null || folder == null) {
             return;
         }
 
         if (listener instanceof MessageChangedListener) {
             folder.addMessageChangedListener((MessageChangedListener) listener);
         } else {
             if (listener instanceof MessageCountListener) {
                 folder.addMessageCountListener((MessageCountListener) listener);
             }
         }
 
         addInternalListeners(listener);
 
     }
 
     private void addInternalListeners(EventListener listener) {
         if (listener == null || folder == null) {
             return;
         }
 
         if (listener instanceof MessageChangedListener && messageChangedListener == null) {
             messageChangedListener = new MessageChangedListener() {
 
                 public void messageChanged(MessageChangedEvent mce) {
                     usePush();
                 }
             };
             folder.addMessageChangedListener(messageChangedListener);
         } else {
             if (listener instanceof MessageCountListener && messageCountListener == null) {
                 messageCountListener = new MessageCountListener() {
 
                     public void messagesAdded(MessageCountEvent mce) {
                         usePush();
                     }
 
                     public void messagesRemoved(MessageCountEvent mce) {
                         usePush();
                     }
                 };
                 folder.addMessageCountListener(messageCountListener);
             }
         }
     }
 
     public Message[] getNewMessages() throws MessagingException {
         ArrayList<Message> mess = new ArrayList<Message>();
 
         Message[] allmess = folder.getSortedMessages(new SortTerm[]{SortTerm.ARRIVAL, SortTerm.DATE});
 
         for (int i = 0; i < poller.getDiffCount(); i++) {
             if (allmess[i].isSet(Flags.Flag.SEEN) == false) {
                 mess.add(allmess[i]);
             }
         }
 
         Message[] messages = new Message[mess.size()];
         for (int i = 0; i < mess.size(); i++) {
             messages[i] = mess.get(i);
         }
 
         return messages;
     }
 
     public Message[] getMessages() throws MessagingException {
         return folder.getMessages();
     }
 
     public String getAccountName() {
         return accountName;
     }
 
     public String getPassword() {
         return password;
     }
 
     public String getServerAddress() {
         return serverAddress;
     }
 
     public int getServerPort() {
         return serverPort;
     }
 
     public boolean isSSL() {
         return useSSL;
     }
 
     public String getUsername() {
         return username;
     }
 
     public boolean isConnected() {
         return connected;
     }
 
     public boolean isSessionValid() {
         return server.isConnected();
     }
 
     @Override
     public String toString() {
         return accountName;
     }
 
     public Vector getVectorData() {
         Vector data = new Vector();
         data.add(accountName);
         data.add(serverAddress);
         data.add(serverPort);
         data.add(useSSL);
         data.add(username);
         data.add(password);
         return data;
     }
 
     public void setAccountName(String accountName) {
         this.accountName = accountName;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public void setServerAddress(String serverAddress) {
         this.serverAddress = serverAddress;
     }
 
     public void setServerPort(int serverPort) {
         this.serverPort = serverPort;
     }
 
     public void setUseSSL(boolean useSSL) {
         this.useSSL = useSSL;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public abstract void onError(Exception e);
 
     public abstract void onDisconnect();
 
     public abstract void onConnect();
 }
