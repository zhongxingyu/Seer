 package directi.androidteam.training.chatclient.Authentication;
 
 import android.util.Log;
 import directi.androidteam.training.TagStore.*;
 import directi.androidteam.training.chatclient.Util.PacketReader;
 import directi.androidteam.training.chatclient.Util.PacketWriter;
 import directi.androidteam.training.chatclient.Util.ServiceThread;
 
 import javax.net.ssl.SSLSocketFactory;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vinayak
  * Date: 2/10/12
  * Time: 3:56 PM
  * To change this template use File | Settings | File Templates.
  */
 public  abstract class Account implements Publisher{
     public  String service;
     protected Socket socket;
     protected Thread readerThread;
 
     protected String accountUid;
     protected LoginStatus loginStatus;
     private static Vector<Subscriber> subscribers = new Vector<Subscriber>();
 
 
     private String status;
     private String show;
 
 
 
 
     public String getShow() {
         return this.show;
     }
 
     public void setShow(String show) {
         this.show = show;
         publish();
     }
 
     public String getStatus() {
         return this.status;
     }
 
     public void setStatus(String status) {
         this.status = status;
         publish();
     }
 
 
 
 
 
     public LoginStatus getPersistedLoginStatus() {
         return persistedLoginStatus;
     }
 
     public void setPersistedLoginStatus(LoginStatus persistedLoginStatus) {
         this.persistedLoginStatus = persistedLoginStatus;
     }
 
     protected LoginStatus persistedLoginStatus;
 
     protected XMPPLogin xmppLogin;
     protected  String serverURL;
     protected  int serverPort;
     protected String passwd;
     protected String fullJID;
 
     public String getFullJID() {
         return fullJID;
     }
 
     public void setFullJID(String fullJID) {
         this.fullJID = fullJID;
     }
 
     public String getPasswd() {
         return passwd;
     }
 
     public int getServiceIcon() {
         return serviceIcon;
     }
 
     protected int serviceIcon;
 
     public Thread getReaderThread() {
         return readerThread;
     }
 
     public void setReaderThread(Thread readerThread) {
         this.readerThread = readerThread;
     }
 
     public String getAccountUid() {
         return accountUid;
     }
 
     public void setAccountUid(String accountUid) {
         this.accountUid = accountUid;
     }
 
     public LoginStatus isLoginStatus() {
         return loginStatus;
     }
 
     public void setLoginStatus(LoginStatus loginStatus) {
         this.loginStatus = loginStatus;
         publish();
         if(loginStatus.equals(LoginStatus.ONLINE))
             postLogin();
     }
 
     public XMPPLogin getXmppLogin() {
         return xmppLogin;
     }
 
     public void setXmppLogin(XMPPLogin xmppLogin) {
         this.xmppLogin = xmppLogin;
     }
 
 
 
     public Account() {
 
     }
 
     public static Account createAccount(String username, String password , String service , String prevStatus){
         Account ret = null;
         LoginStatus status = null;
         if(prevStatus.equals("ONLINE"))
             status = LoginStatus.ONLINE;
         else
             status = LoginStatus.OFFLINE;
         if(service.equals("gtalk")){
             ret = new GtalkAccount(username,password,true);
             ret.setPersistedLoginStatus(status);
         }
         else if(service.equals("gtalkauth")){
             ret = new GtalkAccount(username,password,false);
             ret.setPersistedLoginStatus(status);
         }
         else if(service.equals("pingpong")){
             ret = new PingPongAccount(username,password);
             ret.setPersistedLoginStatus(status);
         }
         return ret;
     }
 
     protected Socket createSocket() throws IOException {
         SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
         Socket sock = sslSocketFactory.createSocket(this.serverURL,this.serverPort);
         sock.setSoTimeout(0);
         sock.setKeepAlive(true);
         return sock;
     }
 
     private Thread launchInNewThread(final ServiceThread serviceThread) {
         Thread t = new Thread() {
             public void run() {
                 serviceThread.execute();
             }
         };
         t.start();
         return t;
     }
 
     public Socket getSocket() {
         return socket;
     }
 
     public void setSocket(Socket socket) {
         this.socket = socket;
     }
 
     public void setupReaderWriter(Thread thread)throws IOException{
 
         readerThread = thread;
 
         PrintWriter writer = new PrintWriter(socket.getOutputStream());
         PacketWriter.addStream(writer, accountUid);
         Log.d("readerwriter","setup");
     }
 
     public void Login(){
         try {
             this.socket = createSocket();
             BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
             setupReaderWriter(launchInNewThread(new PacketReader(reader, accountUid)));
         } catch (IOException e) {
             e.printStackTrace();
         }
        xmppLogin.initiateLogin();
        setLoginStatus(LoginStatus.CONNECTING);
        setPersistedLoginStatus(LoginStatus.ONLINE);
     }
 
     public void postLogin(){
         /*IQTag selfVcard = new IQTag("self",fullJID,"get",new VCardTag("vcard-temp"));
         selfVcard.setRecipientAccount(accountUid);
         PacketWriter.addToWriteQueue(selfVcard);*/
        /* Presence presence = new Presence();
         //presence.addAttribute("id","selfpresence");
         presence.setRecipientAccount(accountUid);
         PacketWriter.addToWriteQueue(presence);*/
 
     }
 
     public void Logout(){
         StreamClose close = new StreamClose();
         close.setRecipientAccount(accountUid);
         PacketWriter.addToWriteQueue(close);
         setLoginStatus(LoginStatus.OFFLINE);
         setPersistedLoginStatus(LoginStatus.OFFLINE);
 
 
     }
 
     public void freeResources(){
         readerThread.interrupt();
         readerThread=null;
         PacketWriter.removeStream(this.accountUid);
         socket=null;
     }
 
 
     //Publisher Methods
     @Override
     public void addSubscriber(Subscriber subscriber){
         subscribers.add(subscriber);
 
     }
 
     @Override
     public void removeSubscriber(Subscriber subscriber){
         subscribers.remove(subscriber);
     }
 
     @Override
     public void publish(){
         Subscriber[] store = subscribers.toArray(new Subscriber[subscribers.size()]);
         for (Subscriber subscriber : store) {
             subscriber.receivedNotification(PublicationType.ACCOUNT_STATE_CHANGED,accountUid);
         }
 
     }
 
 
     public void initPresence(Presence presence) {
         if(!presence.getFrom().equals(fullJID))
             return;
         Log.d("presenceSet","ok");
         this.setShow(presence.getShow());
         this.setStatus(presence.getStatus());
     }
 
     public void sendStatus(String status) {
         Presence presence = new Presence();
         presence.setStatus(status);
         presence.setRecipientAccount(getAccountUid());
         //      presence.setShow("dnd");//account.getShow());
         setStatus(status);
         PacketWriter.addToWriteQueue(presence);
 
     }
     public void sendAvail(String avail) {
         Presence presence = new Presence();
         presence.setShow(avail);
         presence.setRecipientAccount(getAccountUid());
 //        presence.setStatus("stat2");//account.getStatus());
         setShow(avail);
         PacketWriter.addToWriteQueue(presence);
 
     }
 }
 
 
