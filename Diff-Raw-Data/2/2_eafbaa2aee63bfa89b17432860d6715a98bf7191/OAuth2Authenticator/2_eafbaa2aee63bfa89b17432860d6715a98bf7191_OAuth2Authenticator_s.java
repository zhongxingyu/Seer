 /**
  * Brif 
  */
 
 package com.brif.nix.oauth2;
 
 import java.io.IOException;
 import java.security.Provider;
 import java.security.Security;
 import java.util.Properties;
 
 import javax.mail.AuthenticationFailedException;
 import javax.mail.Folder;
 import javax.mail.FolderClosedException;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 
 import com.brif.nix.gdrive.DriveManager;
 import com.brif.nix.listeners.NixMessageCountListener;
 import com.brif.nix.model.DataAccess;
 import com.brif.nix.model.User;
 import com.brif.nix.notifications.SapiNotificationsHandler;
 import com.brif.nix.parser.MessageParser;
 import com.google.api.client.auth.oauth2.TokenResponse;
 import com.google.api.client.auth.oauth2.TokenResponseException;
 import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.json.jackson2.JacksonFactory;
 import com.sun.mail.gimap.GmailFolder;
 import com.sun.mail.gimap.GmailSSLStore;
 import com.sun.mail.iap.ProtocolException;
 import com.sun.mail.imap.IMAPFolder;
 import com.sun.mail.imap.protocol.IMAPProtocol;
 
 /**
  * Performs OAuth2 authentication.
  * 
  * <p>
  * Before using this class, you must call {@code initialize} to install the
  * OAuth2 SASL provider.
  */
 public class OAuth2Authenticator {
 
 	public static final class OAuth2Provider extends Provider {
 		private static final long serialVersionUID = 1L;
 
 		public OAuth2Provider() {
 			super("Google OAuth2 Provider", 1.0,
 					"Provides the XOAUTH2 SASL Mechanism");
 			put("SaslClientFactory.XOAUTH2",
 					"com.brif.nix.oauth2.OAuth2SaslClientFactory");
 		}
 	}
 
 	/**
 	 * Authenticates to IMAP with parameters passed in on the command-line.
 	 */
 	public static void main(String args[]) throws Exception {
 
 		// command-line handling
 		if (args.length == 0 || !isValidEmailAddress(args[0])) {
 			System.out.println("Usage: java -jar nix.jar <user's email>");
 			System.out.println("\t\tError loading user's email");
 			return;
 		}
 
 		String email = args[0];
 
 		// initialize provider
 		initialize();
 
 		// keeping this loop forever with different access_token
 		while (true) {
 
 			logStatus();
 
 			// user info
 			DataAccess dataAccess = new DataAccess();
 			final User currentUser = dataAccess.findByEmail(email);
 			if (currentUser == null) {
 				System.out.println("user " + email + " couldn't be found");
 				return;
 			}
 
 			// init google drive
 			DriveManager drive = DriveManager.getSingelton();
 			drive.setUser(currentUser);
 
 			// IMAP connection
 			GmailSSLStore imapStore = connect(currentUser);
 			if (imapStore == null) {
 				// TODO Auto-generated catch block
 				// internal error
 				return;
 			}
 
 			// update with latest access_token
 			String originalAccessToken = currentUser.access_token;
 			if (currentUser.access_token != originalAccessToken) {
 				dataAccess.updateUserToken(currentUser);
 			}
 
 			GmailFolder inbox = (GmailFolder) imapStore.getFolder("[Gmail]")
 					.getFolder("All Mail");
 			inbox.open(Folder.READ_ONLY);
 
 			// TODO map reduce ?
 			final long uidNext = inbox.getUIDNext();
 			long min = Math.max(currentUser.next_uid + 1, uidNext - 1000);
 
 			final Message[] messages = inbox.getMessagesByUID(min, uidNext);
 			for (int i = messages.length - 1; i >= 0; i--) {
 				Message message = messages[i];
 				MessageParser mp = new MessageParser(message, currentUser);
 				if (!mp.isDraft()) {
 					System.out.println("Adding message: " + mp.getMessageId());
 					dataAccess.addMessage(currentUser, mp);
 				}
 			}
 
 			dataAccess = new DataAccess(new SapiNotificationsHandler(
					"http://api.brif.us"));
 			// https://bugzilla.mozilla.org/show_bug.cgi?id=518581
 			inbox.addMessageCountListener(new NixMessageCountListener(
 					currentUser, dataAccess));
 
 			try {
 				startKeepAliveListener((IMAPFolder) inbox, currentUser);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	private static void logStatus() {
 		/* This will return Long.MAX_VALUE if there is no preset limit */
 		long maxMemory = Runtime.getRuntime().maxMemory();
 		/* Maximum amount of memory the JVM will attempt to use */
 		System.out.println("Maximum memory (bytes): "
 				+ (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));
 
 		/* Total memory currently in use by the JVM */
 		System.out.println("Total memory (bytes): "
 				+ Runtime.getRuntime().totalMemory());
 	}
 
 	/**
 	 * Installs the OAuth2 SASL provider. This must be called exactly once
 	 * before calling other methods on this class.
 	 */
 	public static void initialize() {
 		Security.addProvider(new OAuth2Provider());
 	}
 
 	/**
 	 * Connects and authenticates to an IMAP server with OAuth2. You must have
 	 * called {@code initialize}.
 	 * 
 	 * @param host
 	 *            Hostname of the imap server, for example
 	 *            {@code imap.googlemail.com}.
 	 * @param port
 	 *            Port of the imap server, for example 993.
 	 * @param userEmail
 	 *            Email address of the user to authenticate, for example
 	 *            {@code oauth@gmail.com}.
 	 * @param oauthToken
 	 *            The user's OAuth token.
 	 * @param debug
 	 *            Whether to enable debug logging on the IMAP connection.
 	 * 
 	 * @return An authenticated IMAPStore that can be used for IMAP operations.
 	 */
 	public static GmailSSLStore connectToImap(String host, int port,
 			String userEmail, String oauthToken, boolean debug)
 			throws Exception {
 		Properties props = new Properties();
 		props.put("mail.store.protocol", "gimaps");
 		props.put("mail.gimaps.sasl.enable", "true");
 		props.put("mail.gimaps.sasl.mechanisms", "XOAUTH2");
 		props.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, oauthToken);
 
 		Session session = Session.getDefaultInstance(props, null);
 		session.setDebug(debug);
 		session.getProperties().setProperty(
 				OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, oauthToken);
 		GmailSSLStore store = (GmailSSLStore) session.getStore("gimaps");
 		store.connect(host, port, userEmail, "");
 
 		return store;
 	}
 
 	/**
 	 * @param refreshToken
 	 * @param clientId
 	 * @param clientSecret
 	 * @return
 	 * @throws IOException
 	 */
 	public static String refreshAccessToken(String refreshToken,
 			String clientId, String clientSecret) throws IOException {
 		try {
 
 			TokenResponse response = new GoogleRefreshTokenRequest(
 					new NetHttpTransport(), new JacksonFactory(), refreshToken,
 					clientId, clientSecret).execute();
 			String accessToken = response.getAccessToken();
 			System.out.println("Access token: " + accessToken);
 			return accessToken;
 		} catch (TokenResponseException e) {
 			// TODO Auto-generated catch block
 			if (e.getDetails() != null) {
 				System.err.println("Error: " + e.getDetails().getError());
 				if (e.getDetails().getErrorDescription() != null) {
 					System.err.println(e.getDetails().getErrorDescription());
 				}
 				if (e.getDetails().getErrorUri() != null) {
 					System.err.println(e.getDetails().getErrorUri());
 				}
 			} else {
 				// TODO Auto-generated catch block
 				System.err.println(e.getMessage());
 			}
 		}
 		return null;
 	}
 
 	private static GmailSSLStore connect(User currentUser) throws Exception,
 			IOException {
 		GmailSSLStore imapStore = null;
 		final boolean debug = false;
 		try {
 			imapStore = connectToImap("imap.gmail.com", 993, currentUser.email,
 					currentUser.access_token, debug);
 		} catch (AuthenticationFailedException e) {
 
 			// try again... first invalidate access token
 			invalidateAccessToken(currentUser);
 
 			try {
 				imapStore = connectToImap("imap.gmail.com", 993,
 						currentUser.email, currentUser.access_token, debug);
 			} catch (Exception e1) {
 				// TODO: invalid grant - application revoked???
 				// send a message
 			}
 		}
 		return imapStore;
 	}
 
 	protected static void invalidateAccessToken(User currentUser)
 			throws IOException {
 
 		OAuth2Configuration conf = OAuth2Configuration
 				.getConfiguration(currentUser.origin);
 
 		final String access_token = refreshAccessToken(
 				currentUser.refresh_token, conf.get("client_id"),
 				conf.get("client_secret"));
 
 		currentUser.access_token = access_token;
 	}
 
 	public static void startKeepAliveListener(IMAPFolder imapFolder,
 			User currentUser) throws MessagingException {
 		// We need to create a new thread to keep alive the connection
 		Thread t = new Thread(new KeepAliveRunnable(imapFolder, currentUser),
 				"IdleConnectionKeepAlive");
 
 		t.start();
 
 		while (!Thread.interrupted()) {
 			System.out.println("Starting IDLE");
 			try {
 				imapFolder.idle();
 			} catch (MessagingException e) {
 				System.out.println("Messaging exception during IDLE");
 				throw e;
 			}
 		}
 
 		// Shutdown keep alive thread
 		if (t.isAlive()) {
 			t.interrupt();
 		}
 	}
 
 	private static boolean isValidEmailAddress(String email) {
 		boolean result = true;
 		try {
 			InternetAddress emailAddr = new InternetAddress(email);
 			emailAddr.validate();
 		} catch (AddressException ex) {
 			result = false;
 		}
 		return result;
 	}
 
 	/**
 	 * Runnable used to keep alive the connection to the IMAP server
 	 * 
 	 * @author Juan Martin Sotuyo Dodero <jmsotuyo@monits.com>
 	 */
 	private static class KeepAliveRunnable implements Runnable {
 
 		private static final long KEEP_ALIVE_FREQ = 300000; // 5 minutes
 
 		private IMAPFolder folder;
 
 		private User currentUser;
 
 		public KeepAliveRunnable(IMAPFolder folder, User currentUser) {
 			this.folder = folder;
 			this.currentUser = currentUser;
 		}
 
 		@Override
 		public void run() {
 			while (!Thread.interrupted()) {
 				try {
 					Thread.sleep(KEEP_ALIVE_FREQ);
 
 					// Perform a NOOP just to keep alive the connection
 					System.out
 							.println("Performing a NOOP to keep alvie the connection");
 					folder.doCommand(new IMAPFolder.ProtocolCommand() {
 						public Object doCommand(IMAPProtocol p)
 								throws ProtocolException {
 							p.simpleCommand("NOOP", null);
 							return null;
 						}
 					});
 				} catch (InterruptedException e) {
 					// Ignore, just aborting the thread...
 				} catch (FolderClosedException ex) {
 					ex.printStackTrace();
 					try {
 						connect(currentUser);
 						if (!folder.isOpen()) {
 							folder.open(Folder.READ_ONLY);
 						}
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 				} catch (MessagingException e) {
 					// Shouldn't really happen...
 					System.out
 							.println("Unexpected exception while keeping alive the IDLE connection");
 				}
 			}
 		}
 	}
 }
