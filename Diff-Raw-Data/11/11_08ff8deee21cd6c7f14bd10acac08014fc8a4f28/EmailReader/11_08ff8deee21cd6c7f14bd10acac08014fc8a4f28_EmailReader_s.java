 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.mail.Flags;
 import javax.mail.Folder;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 
 import AndrewCassidy.PluggableBot.PluggableBot;
 import AndrewCassidy.PluggableBot.Plugin;
 
 public class EmailReader implements Plugin {
 
 	public EmailReader() {
 		String username = "";
 		String password = "";
 		
 		final javax.mail.Store store;
 		try {
 			// configure the jvm to use the jsse security.
 			java.security.Security
 					.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
 
 			// create the properties for the Session
 			java.util.Properties props = new java.util.Properties();
 
 			// set this session up to use SSL for IMAP connections
 			props.setProperty("mail.imap.socketFactory.class",
 					"javax.net.ssl.SSLSocketFactory");
 			// don't fallback to normal IMAP connections on failure.
 			props.setProperty("mail.imap.socketFactory.fallback", "false");
 			// use the simap port for imap/ssl connections.
 			props.setProperty("mail.imap.socketFactory.port", "993");
 
 			// note that you can also use the defult imap port (including the
 			// port specified by mail.imap.port) for your SSL port
 			// configuration.
 			// however, specifying mail.imap.socketFactory.port means that,
 			// if you decide to use fallback, you can try your SSL connection
 			// on the SSL port, and if it fails, you can fallback to the normal
 			// IMAP port.
 
 			// create the Session
 			javax.mail.Session session = javax.mail.Session.getInstance(props);
 			// and create the store..
 			store = session.getStore(new javax.mail.URLName(
 					"imap://" + username + ":" + password + "@imap.gmail.com/"));
 			// and connect.
 			store.connect();
 			System.out.println("connected to store.");
 
 			// Get folder
 			
			TimerTask t = new TimerTask() {
 				
 				@Override
 				public void run() {
 					try {
 						System.out.println("Checking mailbox");
 						
 						Folder folder = store.getFolder("INBOX");
 						
 						folder.open(Folder.READ_WRITE);
 
 						// Get directory
 						Message message[] = folder.getMessages();
 
 						for (int i = 0, n = message.length; i < n; i++) {
 							System.out.println(i + ": " + message[i].getFrom()[0] + "\t"
 									+ message[i].getSubject());
 							 
 							for (String chan : PluggableBot.getChans()) {
								PluggableBot.Message(chan, message[i].getFrom()[0] + message[i].getSubject());
 							}
 							message[i].setFlag(Flags.Flag.DELETED, true);
 						}
 						
 						folder.close(true);
 					} catch (MessagingException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					
 				}
 			};
 			
 			Timer tim = new Timer(false);
 			
 			tim.schedule(t,0,1 * 60 * 1000);
 			
 		} catch (Exception e) {
 			System.out.println("caught exception: " + e);
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public String getHelp() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void onAction(String sender, String login, String hostname,
 			String target, String action) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onAdminMessage(String sender, String login, String hostname,
 			String message) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onJoin(String channel, String sender, String login,
 			String hostname) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onKick(String channel, String kickerNick, String kickerLogin,
 			String kickerHostname, String recipientNick, String reason) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onMessage(String channel, String sender, String login,
 			String hostname, String message) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onPart(String channel, String sender, String login,
 			String hostname) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onPrivateMessage(String sender, String login, String hostname,
 			String message) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onQuit(String sourceNick, String sourceLogin,
 			String sourceHostname, String reason) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void unload() {
		// TODO Auto-generated method stub
 
 	}
 
 	public static void main(String[] args) {
 		new EmailReader();
 	}
 
 }
