 package ch9k.core;
 
 import ch9k.chat.Contact;
 import ch9k.chat.ContactList;
 import ch9k.chat.ConversationManager;
 import ch9k.chat.events.ContactOnlineEvent;
 import ch9k.configuration.Configuration;
 import ch9k.core.gui.ApplicationWindow;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 /**
  * The main application, OMG!
  *
  * @author Bruno
  * @author Pieter De baets
  */
 public class ChatApplication {
     public static ChatApplication getInstance() {
         return SingletonHolder.INSTANCE;
     }
 
     /* Helper-class for singleton, aka Bill Pugh's method */
     private static class SingletonHolder {
          private static final ChatApplication INSTANCE = new ChatApplication();
     }
 
     private ApplicationWindow appWindow;
     private Configuration configuration;
     private ConversationManager conversationManager;
 
     public static void main(String[] args) {
         ChatApplication app = ChatApplication.getInstance();
         app.start(args);
     }
 
     private ChatApplication() {
         conversationManager = new ConversationManager();
         appWindow = new ApplicationWindow();
 
         appWindow.addWindowListener(new WindowAdapter() {
             public void windowClosed(WindowEvent e) {
                 logout();
             }
         });
     }
 
     private void start(String[] args) {
         // perform auto login?
         if(args!= null && args.length == 2) {
             Configuration configuration = new Configuration(args[0]);
             Account account = configuration.getAccount();
             if(account != null && account.authenticate(args[1])) {
                 this.configuration = configuration;
                 performTestLogin();
             } else {
                 System.err.println("Failed to login with provided credentials");
             }
         }
 
         if(configuration == null) {
             // show login dialog
             LoginController loginController = new LoginController();
             configuration = loginController.run(appWindow);
             if(configuration == null) {
                 // user closed window
                 System.exit(0);
             }
         }
         configuration.save();
 
         appWindow.initApplicationView();
         appWindow.setStatus(I18n.get("ch9k.core", "booting"));
     }
 
     public void logout() {
         new Thread(new Runnable() {
             public void run() {
                 configuration.save();
                 configuration = null;
 
                 // TODO: clear more stuff
 
                 start(null);
             }
         }).start();
     }
 
     /**
      * Get the currently logged in account, may return null if authentication
      * is not performed yet
      * @return account
      */
     public Account getAccount() {
         if(configuration != null) {
             return configuration.getAccount();
         } else {
             return null;
         }
     }
 
     /**
      * Get the conversation manager
      * @return conversationManager
      */
     public ConversationManager getConversationManager() {
         return conversationManager;
     }
 
     /**
      * Perform a fake login
      * (to be used for testing purposes only!)
      */
     public void performTestLogin() {
         if(configuration == null || configuration.getAccount() == null) {
             configuration = new Configuration("CH9K");
             configuration.setAccount(new Account("CH9K", "password"));
            configuration.save();
         }
 
         final ContactList contactList = ChatApplication.getInstance().getAccount().getContactList();
 
         new Thread(new Runnable() {
             public void run() {
                 try {
                     Thread.sleep(5000);
 
                     Contact contact = contactList.getContact(
                             InetAddress.getByName("cartman"),
                             "Zeus WPI");
                     ContactOnlineEvent event = new ContactOnlineEvent(contact);
                     event.setSource(InetAddress.getByName("cartman"));
                 }
                 catch(InterruptedException ex) {}
                 catch(UnknownHostException ex) {}
             }
         }).start();
     }
 }
