 /* Copyright 2012 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.brif.nix.oauth2;
 
 import java.io.IOException;
 import java.security.Provider;
 import java.security.Security;
 import java.util.Properties;
 
 import javax.mail.AuthenticationFailedException;
 import javax.mail.Folder;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.URLName;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 
 import com.brif.nix.listeners.NixListener;
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
 import com.sun.mail.smtp.SMTPTransport;
 
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
 
 			// user info
 			DataAccess dataAccess = new DataAccess();
 			final User currentUser = dataAccess.findByEmail(email);
 
 			String originalAccessToken = currentUser.access_token;
 
 			// IMAP connection
 			GmailSSLStore imapStore = connect(currentUser);
 			if (imapStore == null) {
 				// TODO Auto-generated catch block
 				// internal error
 				return;
 			}
 
 			// update with latest access_token
 			if (currentUser.access_token != originalAccessToken) {
 				dataAccess.updateUserToken(currentUser);
 			}
 
 			GmailFolder inbox = (GmailFolder) imapStore.getFolder("[Gmail]")
 					.getFolder("All Mail");
 			inbox.open(Folder.READ_ONLY);
 						
 			// TODO map reduce ?
 			final long uidNext = inbox.getUIDNext();
 			long min = Math.max(currentUser.next_uid, uidNext - 500);
 			final Message[] messages = inbox.getMessagesByUID(min, uidNext);
 			
 			for (Message message : messages) {
 				MessageParser mp = new MessageParser(message);
 				if (!mp.isDraft()) { 
 					System.out.println("Adding message: " + mp.getMessageId());
 					dataAccess.addMessage(currentUser, mp);	
 				}
 			}
 
 			// update user with latest fetch
 			currentUser.next_uid = uidNext;
 			dataAccess.updateUserNextUID(currentUser);
 
 			dataAccess = new DataAccess(new SapiNotificationsHandler(
 					"http://api.brif.us"));
 			inbox.addMessageCountListener(new NixListener(currentUser,
 					dataAccess));
 
 			try {
 				startKeepAliveListener((IMAPFolder) inbox);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		// SMTP connection
 		// SMTPTransport smtpTransport = connectToSmtp("smtp.gmail.com", 587,
 		// email, oauthToken, true);
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
 	 * Connects and authenticates to an SMTP server with OAuth2. You must have
 	 * called {@code initialize}.
 	 * 
 	 * @param host
 	 *            Hostname of the smtp server, for example
 	 *            {@code smtp.googlemail.com}.
 	 * @param port
 	 *            Port of the smtp server, for example 587.
 	 * @param userEmail
 	 *            Email address of the user to authenticate, for example
 	 *            {@code oauth@gmail.com}.
 	 * @param oauthToken
 	 *            The user's OAuth token.
 	 * @param debug
 	 *            Whether to enable debug logging on the connection.
 	 * 
 	 * @return An authenticated SMTPTransport that can be used for SMTP
 	 *         operations.
 	 */
 	public static SMTPTransport connectToSmtp(String host, int port,
 			String userEmail, String oauthToken, boolean debug)
 			throws Exception {
 		Properties props = new Properties();
 		props.put("mail.smtp.starttls.enable", "true");
 		props.put("mail.smtp.starttls.required", "true");
 		props.put("mail.smtp.sasl.enable", "true");
 		props.put("mail.smtp.sasl.mechanisms", "XOAUTH2");
 		props.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, oauthToken);
 		Session session = Session.getInstance(props);
 		session.setDebug(debug);
 
 		final URLName unusedUrlName = null;
 		SMTPTransport transport = new SMTPTransport(session, unusedUrlName);
 		// If the password is non-null, SMTP tries to do AUTH LOGIN.
 		final String emptyPassword = "";
 		transport.connect(host, port, userEmail, emptyPassword);
 
 		return transport;
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
 		try {
 			imapStore = connectToImap("imap.gmail.com", 993, currentUser.email,
 					currentUser.access_token, false);
 		} catch (AuthenticationFailedException e) {
 			OAuth2Configuration conf = OAuth2Configuration
 					.getConfiguration(currentUser.origin);
 
 			final String access_token = refreshAccessToken(
 					currentUser.refresh_token, conf.get("client_id"),
 					conf.get("client_secret"));
 			currentUser.access_token = access_token;
 
 			try {
 				imapStore = connectToImap("imap.gmail.com", 993,
						currentUser.email, currentUser.access_token, true);
 			} catch (Exception e1) {
 				// TODO: invalid grant - application revoked???
 				// send a message
 			}
 		}
 		return imapStore;
 	}
 
 	public static void startKeepAliveListener(IMAPFolder imapFolder)
 			throws MessagingException {
 		// We need to create a new thread to keep alive the connection
 		Thread t = new Thread(new KeepAliveRunnable(imapFolder),
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
 
 		public KeepAliveRunnable(IMAPFolder folder) {
 			this.folder = folder;
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
 				} catch (MessagingException e) {
 					// Shouldn't really happen...
 					System.out
 							.println("Unexpected exception while keeping alive the IDLE connection");
 				}
 			}
 		}
 	}
 }
