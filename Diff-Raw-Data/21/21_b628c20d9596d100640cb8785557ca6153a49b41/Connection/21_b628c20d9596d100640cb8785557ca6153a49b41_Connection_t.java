 package com.eleventytwo.EmailReader;
 
 import java.security.Security;
 import java.util.Properties;
 
 import javax.mail.Flags;
 import javax.mail.Folder;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.NoSuchProviderException;
 import javax.mail.Session;
 import javax.mail.Store;
 
 import AndrewCassidy.PluggableBot.PluggableBot;
 
 public class Connection implements Runnable {
 
 	// private String username;
 	// private String password;
 	//
 	// private String mailbox;
 	//
 	// private List<String> channels = new ArrayList<String>();
 	//
 	// private String server;
 	//
 	// private int port;
 
 	Properties props;
 
 	private static final int MAX_RETRIES = 3;
 
 	public Connection(Properties props) {
 		super();
 		this.props = props;
 	}
 
 	public Connection() {
 
 	}
 
 	@Override
 	public void run() {
 		int retries = 0;
 		Store store = null;
 		while (retries < MAX_RETRIES) {
 			System.out.println("retries = " + retries);
 			if (store == null || !store.isConnected()) {
 				boolean ok = false;
 
 				try {
 					Security
 							.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
 					Properties ssl_props = new java.util.Properties();
 					ssl_props.setProperty("mail.imap.socketFactory.class",
 							"javax.net.ssl.SSLSocketFactory");
 					ssl_props.setProperty("mail.imap.socketFactory.fallback",
 							"false");
 					ssl_props.setProperty("mail.imap.socketFactory.port", props
 							.getProperty("port"));
 
 					Session session = javax.mail.Session.getInstance(props);
 					System.out.println("imap://"
							+ props.getProperty("username").trim() + ":"
							+ props.getProperty("password").trim() + "@"
							+ props.getProperty("server").trim() + "/");
 					store = session.getStore(new javax.mail.URLName("imap://"
							+ props.getProperty("username").trim() + ":"
							+ props.getProperty("password").trim() + "@"
							+ props.getProperty("server").trim() + "/"));
 					store.connect();
 					ok = true;
 
 				} catch (NoSuchProviderException e) {
 					e.printStackTrace();
 				} catch (MessagingException e) {
 					e.printStackTrace();
 				}
 
 				if (ok) {
 					retries = 0;
 				} else {
 					retries++;
 					try {
 						Thread.sleep(30 * 1000);
 					} catch (InterruptedException e) {
 					}
 				}
 			} else {
 				read(store,  props.getProperty("mailbox"));
 			}
 
 		}
 		if (retries >= MAX_RETRIES) {
 			System.out.println("MAX_RETRIES REACHED");
 		}
 
 	}
 
 	public Properties getProps() {
 		return props;
 	}
 
 	private void read(Store store, String mailbox) {
 		System.out.println("reading mailbox");
 		try {
 			Folder folder = store.getFolder(mailbox);
 
 			folder.open(Folder.READ_WRITE);
 
 			// Get directory
 			Message message[] = folder.getMessages();
 
 			for (int i = 0, n = message.length; i < n; i++) {
 				for (String chan :  props.getProperty("channels").split(",")) {
					System.out.println(chan.trim() + " : " + message[i].getSubject());
					PluggableBot.Message(chan.trim(), "email: "
 							+ message[i].getSubject());
 				}
				//message[i].setFlag(Flags.Flag.DELETED, true);
 			}
 
 			folder.close(true);
 		} catch (MessagingException e) {
 			e.printStackTrace();
 		}
 		try {
 			Thread.sleep(30 * 1000);
 		} catch (InterruptedException e) {
 		}
 	}
 
 	
 
 }
