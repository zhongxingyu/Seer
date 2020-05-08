 package com.data;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import com.activities.Activity_Chat;
 import com.activities.Activity_ContactList;
 import com.security.AndroidKeyPairManager;
 import com.services.MessageService;
 import crypto.Cryptography;
 import crypto.Envelope;
 import data.Message;
 import data.User;
 import data.contents.ChatContent;
 import data.contents.PublicKeyResponse;
 import networking.SChatClient;
 import networking.SChatServer;
 
 import javax.crypto.SecretKey;
 import java.io.IOException;
 import java.security.KeyPair;
 import java.security.PublicKey;
 
 /**
  * @author Gary Ye, Elias Frantar, Wolfram Soyka
  * @version 12/16/13
  *          One has to specify the following data:
  *          - host name (default: server name)
  *          - port ( default: port number )
  */
 public class ApplicationUser extends User {
     private static ApplicationUser instance = null;
 
     private final String USER_ID = "id";
 
     private SChatClient client;
     private AndroidSQLManager dbMangager;
 
    // private final static String hostName = "85.10.240.108";
    private final static String hostName = "62.178.242.13";
     // private final static String hostName = "192.168.1.4";
     private final static int portNumber = SChatServer.PORT_ADDRESS;
 
     private Activity_ContactList activity_contactList;
     private Activity_Chat activity_chat;
     private MessageService messageService;
 
     private ApplicationUser() throws IOException {
         this(hostName, portNumber);
     }
 
     private ApplicationUser(String hostName, int portNumber) throws IOException {
         dbMangager = new AndroidSQLManager();
         dbMangager.connect();
 
         // pass this object as a reference so the chat listener is able to call the method 'receiveMessage'
         // client = new SChatClient(this, hostName, portNumber, dbMangager);
     }
 
     public static ApplicationUser getInstance() throws IOException {
         if (instance == null) {
             synchronized (ApplicationUser.class) {
                 if (instance == null) {
                     instance = new ApplicationUser();
                 }
             }
         }
         return instance;
     }
 
     public void setActivity_contactList(Activity_ContactList activity_contactList) {
         this.activity_contactList = activity_contactList;
     }
     public void setActivity_chat(Activity_Chat activity_chat) {
         this.activity_chat = activity_chat;
     }
     public void setMessageService(MessageService messageService) {
         this.messageService = messageService;
     }
 
     public void connect() {
         try {
             client = new SChatClient(this, hostName, portNumber, dbMangager);
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     public boolean registerToServer() {
         try {
             client.registerToServer();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return true;
     }
 
     public boolean loginToServer() {
         try {
             client.loginToServer();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return true;
     }
 
     public void sendMessage(ChatContent content, String receiver_id) {
         client.sendMessage(content, receiver_id);
     }
 
     public void requestPublicKey(String id) {
         client.sendPublicKeyRequest(id);
     }
 
     @Override
     public void registerUser(Envelope envelope) {
         SecretKey secretKey1 = envelope.getUnwrappedKey(keyPair.getPrivate());
         PublicKeyResponse publicKeyResponse = envelope.<PublicKeyResponse>decryptMessage(secretKey1).getContent();
         dbMangager.insertUser(new User(publicKeyResponse.getRequestId(),
                 new KeyPair(publicKeyResponse.getPublicKey(), null), null));
         // contactList.addUser(publicKeyResponse.getRequestId());
         if(activity_contactList != null)
             activity_contactList.addContact(publicKeyResponse.getRequestId());
     }
     @Override
     public void receiveMessage(Envelope e) {
         User sender = dbMangager.getUserFromGivenId(e.getSender());
         PublicKey senderPublicKey = sender.getPublicKey();
         SecretKey secretKey1 = e.getUnwrappedKey(keyPair.getPrivate());
         Message<ChatContent> message = e.<ChatContent>decryptMessage(secretKey1, senderPublicKey);
         if(message != null) {
             dbMangager.insertMessage(message);
             if(messageService != null && activity_chat != null)
                 activity_chat.receiveMessage(message, messageService);
             else
                 if(messageService != null)
                     messageService.receiveMessage(message);
         }
     }
 
     public void initialize(Activity activity) {
         if (!dbMangager.userExists(SChatServer.SERVER_ID)) { // save the server-key in the database
             User server = new User(SChatServer.SERVER_ID, new KeyPair(AndroidKeyPairManager.getServerPK(activity), null), Cryptography.gen_symm_key());
             dbMangager.insertUser(server);
         }
 
         if (!AndroidKeyPairManager.isKeyPairAlreadySaved(activity)) { // generate your own keypair
             KeyPair keyPair1 = Cryptography.gen_asymm_key();
             AndroidKeyPairManager.saveKeyPair(activity, keyPair1);
         }
 
         SharedPreferences shre = PreferenceManager.getDefaultSharedPreferences(activity); // set you own username
         if(!shre.contains(USER_ID)) {
             SharedPreferences.Editor editor = shre.edit();
             editor.putString(USER_ID, "Elias");
             editor.commit();
         }
 
         keyPair = AndroidKeyPairManager.getKeyPairFormSharedPref(activity);
         id = shre.getString(USER_ID, "");
     }
 }
