 package org.irmacard.personalisation;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.URI;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.Properties;
 import java.util.Random;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.smartcardio.CardTerminal;
 import javax.smartcardio.TerminalFactory;
 
 import net.sourceforge.scuba.smartcards.TerminalCardService;
 
 import org.irmacard.credentials.Attributes;
 import org.irmacard.credentials.idemix.IdemixCredentials;
 import org.irmacard.credentials.idemix.IdemixPrivateKey;
 import org.irmacard.credentials.idemix.spec.IdemixIssueSpecification;
 import org.irmacard.credentials.idemix.test.TestSetup;
 import org.irmacard.credentials.idemix.util.CredentialInformation;
 import org.irmacard.credentials.idemix.util.IssueCredentialInformation;
 import org.irmacard.credentials.info.DescriptionStore;
 import org.irmacard.idemix.IdemixService;
 
 public class CredentialLoader {
 	public static void main(String[] args) {
 		try {			
 			Logger.log("main() called");
 
 			URI coreURI= new File(System.getProperty("user.dir")).toURI()
 					.resolve("irma_configuration/");
 
 			CredentialInformation.setCoreLocation(coreURI);
 			DescriptionStore.setCoreLocation(coreURI);
 			Properties config = readProperties(coreURI);
 			Connection con = getDatabaseConnection(config);
 			Card card = getCard(con, args[0]);
 			
 			byte[] credentialPin = getRandomPin(4);
 			byte[] cardPin = getRandomPin(6);
 						
 			Attributes attributes = new Attributes();
 			attributes.add("userID", card.getUserID().getBytes());
 			attributes.add("securityHash", hash(card.getUserID(), card.getCardId()));
 
 			IssueCredentialInformation ici = new IssueCredentialInformation("Surfnet", "root");
 			IdemixIssueSpecification spec = ici.getIdemixIssueSpecification();
 			IdemixPrivateKey isk = ici.getIdemixPrivateKey();
 			
 			CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);
 			IdemixService is = new IdemixService(new TerminalCardService(terminal));
 			IdemixCredentials ic = new IdemixCredentials(is);
 			ic.connect();
 			
 			is.sendCardPin(TestSetup.DEFAULT_CARD_PIN);
 			is.updateCardPin(TestSetup.DEFAULT_CARD_PIN, cardPin);
 			is.updateCredentialPin(credentialPin);
 			
 			is.sendCredentialPin(credentialPin);
 			ic.issue(spec, isk, attributes, null);
 
 			sendMail(cardPin, credentialPin, card, config);
 			setCardStatusPersonalized(con, card.getCardId());
 			
 			System.exit(0);
 		}
 		catch(Exception e) {
 			System.err.println("Exception in Credential Loader: " + e.toString());
 			Logger.log("Exception in Credential Loader: " + e.toString());
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	private static Connection getDatabaseConnection(Properties config) throws SQLException {
 		Connection con = DriverManager.getConnection(config.getProperty("database_url"), config.getProperty("database_username"), config.getProperty("database_password"));
 		con.setAutoCommit(false);
 		return con;
 	}
 
 	private static byte[] getRandomPin(int length) {
 		Random rand = new Random();
 		byte[] pin = new byte[length];
 		for(int i = 0; i < length; i++) {
 			pin[i] = (byte) (rand.nextInt(10) + 0x30);
 		}
 		return pin;
 	}
 	
 	private static void sendMail(byte[] cardPin, byte[] credentialPin, Card card, final Properties config) throws MessagingException {
 		String cardPinString = new String(cardPin);
 		String credentialPinString = new String(credentialPin);
 		
 		Properties props = System.getProperties();
 		props.put("mail.smtp.host", config.getProperty("smtpServer"));
 		Session session = Session.getDefaultInstance(props);
 		Message msg = new MimeMessage(session);/*{
 			@Override
 			protected void updateMessageID() throws MessagingException {
 				super.updateMessageID();
 				String messageId = getMessageID();
 				String fromAdres = config.getProperty("fromAdres");
 				setHeader("Message-ID", messageId.substring(0, messageId.indexOf('@')) + fromAdres.substring(fromAdres.indexOf('@')));
 			}
 		};*/
 		try {
 			msg.setFrom(new InternetAddress(config.getProperty("fromAdres")));
 		} catch (AddressException e) {
 			e.printStackTrace();
 		}
 		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(card.getEmail()));
 		msg.setSubject(config.getProperty("mailSubject"));
 		msg.setText(config.getProperty("mailBody").replace("$NAME$", card.getName()).replace("$CARD_PIN$", cardPinString).replace("$CREDENTIAL_PIN$", credentialPinString));
 		msg.setSentDate(new Date());
 		Transport.send(msg);
 	}
 
 	private static Properties readProperties(URI coreURI) throws IOException {
 		URI config = coreURI.resolve("config.properties.txt");
 		
 		BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(config)));
 		StringBuilder sb = new StringBuilder();
 		String line;
 		while((line = bufferedReader.readLine()) != null) {
 			sb.append(line);
 			sb.append(String.format("%n"));
 		}
 		bufferedReader.close();
 		Properties props = new Properties();
 		props.load(new StringReader(sb.toString()));
 		return props;
 	}
 	
 	private static byte[] hash(String... strings) throws NoSuchAlgorithmException {
 		MessageDigest digest = MessageDigest.getInstance("SHA-256");
 		for(String string : strings) {
 			digest.update(string.getBytes());
 		}
 		return digest.digest();
 	}
 	
 	private static Card getCard(Connection con, String cardId) throws SQLException {
 		PreparedStatement stmt = null;
 		String sql = "SELECT eduPersonPrincipalName, givenName, surname, email " +
 				"FROM PilotParticipants "+
 				"WHERE cardid = ?";
 		try {
 			stmt = con.prepareStatement(sql);
 			stmt.setString(1, cardId);
 			ResultSet result = stmt.executeQuery();
 		
 			if(result.next()) {
 				String userID = result.getString("eduPersonPrincipalName");
 				String name = result.getString("givenName") + " " + result.getString("surname");
 				String email = result.getString("email");
 				return new Card(userID, name, email, null, cardId);
 			} else {
 				return null;		
 			}
 		} finally {
 			if (stmt != null) {
 				stmt.close();
 			}
 		}
 	}
 	
 	private static void setCardStatusPersonalized(Connection con, String cardId) throws SQLException {
 		PreparedStatement stmt = null;
		String sql = "UPDATE PilotParticipants " +
				"SET cardStatus = 'personalised' " +
				"WHERE cardId = ?"; 
 		
 		try {
 			stmt = con.prepareStatement(sql);
 			stmt.setString(1, cardId);
			stmt.execute();
 		} finally {
 			if (stmt != null) {
 				stmt.close();
 			}
 		}
 	}
 }
