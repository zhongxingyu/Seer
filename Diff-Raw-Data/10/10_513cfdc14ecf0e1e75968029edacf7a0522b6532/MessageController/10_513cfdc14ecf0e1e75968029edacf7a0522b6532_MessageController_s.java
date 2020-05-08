 package Email;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Observable;
 import java.util.UUID;
 import util.Util;
 
 /**
  * Message controller class
  */
 public class MessageController extends Observable {
 
     private static MessageController instance = null;
     Mailbox store;
     // What should I be doing here? this is dumb
     HashMap<String, Message> messageLookup = Util.newHashMap();
     HashMap<String, Folder> folderLookup = Util.newHashMap();
 
     /**
      * MessageController Constructor
      *
      * @param messagestore
      */
     private MessageController(Mailbox messagestore) {
         store = messagestore;
     }
 
     /**
      * Get the instance ofMessageController
      *
      * @param messagestore
      * @return instance
      */
     static public MessageController getInstance(Mailbox messagestore) {
         if (instance == null) {
             instance = new MessageController(messagestore);
         }
         return instance;
 
     }
 
     /**
      * Get the instance ofMessageController
      *
      * @return instance
      */
     static public MessageController getInstance() {
         return instance;
     }
 
     private Message getMessageFromId(String messageId) {
         return messageLookup.get(messageId);
     }
 
     private Folder getFolderFromId(String folderId) {
         return folderLookup.get(folderId);
     }
 
     private String getIdfromMessage(Message message) {
         String id = message.getId();
         messageLookup.put(id, message);
         return id;
     }
 
     private String getIdfromFolder(Folder folder) {
         String id = folder.getId();
         folderLookup.put(id, folder);
         return id;
     }
 
     /**
      * Get sub folders from a folder
      *
      * @param folderId
      * @return Array of string id's of all the sub folders inside our folder
      */
     public String[] getFolderList(String folderId) {
         Folder fldr = getFolderFromId(folderId);
         ArrayList<Folder> set = fldr.getSubfolders();
         String[] ids = new String[set.size()];
 
         int index = 0;
         for (Folder subfolder : set) {
             ids[index++] = getIdfromFolder(subfolder);
         }
 
         return ids;
     }
 
     /**
      * Get email list from a folder
      *
      * @param folderId Id of folder
      * @return Array of string id's of all the messages inside our folder
      */
     public String[] getEmailList(String folderId) {
         String[] ids;
 
         try {
             Folder fldr = getFolderFromId(folderId);
             ArrayList<Message> set;
             set = fldr.getMessages();
 
             ids = new String[set.size()];
 
             int index = 0;
             for (Message message : set) {
                 ids[index++] = getIdfromMessage(message);
             }
         } catch (Exception ex) {
             ids = new String[0];
         }
         return ids;
     }
 
     /**
      * Get email content
      *
      * @param id
      * @return content
      */
     public String getEmailContent(String id) {
         try {
             Message message = getMessageFromId(id);
             return message.getContent();
         } catch (NullPointerException e) {
             return new String();
         }
     }
 
     /**
      * Set email content
      *
      * @param id
      * @param content
      */
     public void setEmailContent(String id, String content) {
         Message message = getMessageFromId(id);
         message.setContent(content);
 
         // update anyone waiting on updates
         this.setChanged();
         this.notifyObservers();
 
     }
 
     /**
      * Get email summary
      *
      * @param messageId Id of Message
      * @return Summary of the Message
      */
     public Summary getEmailSummary(String messageId) {
         Message message = getMessageFromId(messageId);
         Summary summary = new Summary(message);
 
         return summary;
     }
 
     /**
      * Set email header
      *
      * @param messageId Message Id
      * @param key Key
      * @param value Value
      */
     public void setEmailHeader(String messageId, String key, String value) {
         Message msg = getMessageFromId(messageId);
         msg.setHeader(key, value);
 
         // update anyone waiting on updates
         this.setChanged();
         this.notifyObservers();
 
     }
 
     /**
      * Get email header
      *
      * @param messageId Message Id
      * @param key Key for which we want a value
      * @return Value of particular key within message header
      */
     public String getEmailHeader(String messageId, String key) {
         Message msg = getMessageFromId(messageId);
         return msg.getHeaderValue(key);
     }
 
     /**
      * Mark message as read
      *
      * @param messageId Message Id
      */
     public void markRead(String messageId) {
         setEmailHeader(messageId, "X-Read", "FIXME: Set to NOW()");
 
         // update anyone waiting on updates
         this.setChanged();
         this.notifyObservers();
     }
 
     /**
      * Mark message as unread
      *
      * @param messageId Message Id
      */
     public void markUnread(String messageId) {
         setEmailHeader(messageId, "X-Read", null);
 
         // update anyone waiting on updates
         this.setChanged();
         this.notifyObservers();
     }
 
     /**
      * Delete message for given messageId
      *
      * @param messageId Message Id
      */
     public void delete(String messageId) {
         // FIXME;
 
         // update anyone waiting on updates
         this.setChanged();
         this.notifyObservers();
 
     }
 
     /**
      * Move message to specified destination folder
      *
      * @param messageId Message Id
      * @param destinationFolderId Destination Folder Id
      */
     public void moveMessageToFolder(String messageId, String destinationFolderId) {
         Message msg = getMessageFromId(messageId);
         Folder destination = getFolderFromId(destinationFolderId);
         destination.addMessage(msg);
     }
 
     //FIXME Not sure what this does
     /**
      * Compose a new message
      *
      * @return message
      */
     public String compose() {
         Message newMsg = new PlainTextMessage();
         UUID messageId = UUID.randomUUID();
         newMsg.setId(messageId.toString());
         Folder drafts = store.getDrafts();
        drafts.addMessage(newMsg);
         return getIdfromMessage(newMsg);
     }
 
     /**
      * Update the date
      *
      * @param messageid
      */
     public void updateDate(String messageid) {
         Message msg = this.getMessageFromId(messageid);
         DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         Date date = new Date();
         msg.setHeader("Date", dateFormat.format(date));
     }
 
     /**
      * Create Reply content from the original message
      *
      * @param originalMessage
      * @return reply id
      */
     public String reply(String originalMessage) {
         // create a new message
         String replyid = compose();
         updateDate(replyid);
 
         Message replymsg = getMessageFromId(replyid);
 
         // get the original message and use it to create the reply content
         Message original = getMessageFromId(originalMessage);
 
         String replyContent = original.getContent();
         replyContent = "\r\n\r\n" + replyContent;
         replyContent = replyContent.replaceAll("\n", "\n> ");
 
         // get the headers based on the original message
         String to = original.getHeaderValue("From");
         String subject = original.getHeaderValue("Subject");
 
         // add RE to the subject if it is not already there
         if (subject.length() < 3 || !"RE:".equals(subject.substring(0, 3).toUpperCase())) {
             subject = "RE: " + subject;
         }
 
         // set the headers and content of the reply
         replymsg.setContent(replyContent);
         replymsg.setHeader("To", to);
         replymsg.setHeader("Subject", subject);
 
         // update anyone waiting on updates
         this.setChanged();
         this.notifyObservers();
 
         return replyid;
     }
 
     /**
      *
      * @param currentMessage
      * @return
      */
     public String forward(String currentMessage) {
         // create a new message
         String forwardid = compose();
         updateDate(forwardid);
 
         Message forwardmsg = getMessageFromId(forwardid);
 
         // get the original message and use it to create the reply content
         Message original = getMessageFromId(currentMessage);
 
         String forwardContent = original.getContent();
         forwardContent = "\r\n\r\n" + forwardContent;
         forwardContent = forwardContent.replaceAll("\n", "\n> ");
 
         // get the headers based on the original message
         String subject = original.getHeaderValue("Subject");
 
         // add RE to the subject if it is not already there
         if (subject.length() < 4 || !"FWD:".equals(subject.substring(0, 4).toUpperCase())) {
             subject = "Fwd: " + subject;
         }
 
         // set the headers and content of the reply
         forwardmsg.setContent(forwardContent);
         forwardmsg.setHeader("Subject", subject);
 
         // update anyone waiting on updates
         this.setChanged();
         this.notifyObservers();
 
         return forwardid;
     }
 
     // get particular folder ids
     /**
      * Get root folder id
      *
      * @return id
      */
     public String getRootFolderId() {
         return getIdfromFolder(store);
     }
 
     /**
      * Get drafts folder id
      *
      * @return id
      */
     public String getDraftsFolderId() {
         return getIdfromFolder(store.getDrafts());
     }
 
     /**
      * Get inbox folder id
      *
      * @return id
      */
     public String getInboxFolderId() {
         return getIdfromFolder(store.getInbox());
     }
 
     /**
      * Get outbox folder id
      *
      * @return id
      */
     public String getOutboxFolderId() {
         return getIdfromFolder(store.getOutbox());
     }
 
     /**
      * Get sent messages folder id
      *
      * @return id
      */
     public String getSentMessagesFolderId() {
         return getIdfromFolder(store.getSentMessages());
     }
 
     /**
      * Get trash folder id
      *
      * @return id
      */
     public String getTrashFolderId() {
         return getIdfromFolder(store.getTrash());
     }
 
     /**
      * Get the name of a folder
      *
      * @param folder
      * @return folderName
      */
     public String getFolderName(String folder) {
         return getFolderFromId(folder).getName();
     }
 
     /**
      * Create a new folder
      *
      * @param in
      * @param name
      */
     public void newFolder(String in, String name) {
         Folder folder = getFolderFromId(in);
         folder.createFolder(name);
 
         // update anyone waiting on updates
         this.setChanged();
         this.notifyObservers();
     }
 
     /**
      * Delete a folder selected
      *
      * @param selected
      */
     public void deletefolder(String selected) {
         Folder folder;
         folder = getFolderFromId(selected);
         store.deleteFolder(folder);
 
         this.setChanged();
         this.notifyObservers();
     }
 
     /**
      * Move folder form source to destination
      *
      * @param sourcePath
      * @param destinationPath
      */
     public void moveFolder(String sourcePath, String destinationPath) {
         Folder source = this.getFolderFromId(sourcePath);
         Folder dest = this.getFolderFromId(destinationPath);
         source.moveFolder(dest);
 
         this.setChanged();
         this.notifyObservers();
     }
 
     /**
      *
      */
     public void doSendRecieve() {
         Folder outbox = store.getOutbox();
         Folder sent = store.getSentMessages();
 
         ArrayList<Message> outbound = store.getOutbox().getMessages();
         for (Message out : outbound) {
             
             sent.addMessage(out);
             this.getIdfromMessage(out);
         }
     }
 }
