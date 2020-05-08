 package nl.ru.cs.irma.irmawriter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.Observable;
 import java.util.Properties;
 import java.util.Random;
 import java.util.Scanner;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import credentials.Attributes;
 
 public class CardWriter extends Observable {
 	private static final String MUTIL_LOG_LOCATION = "mutil_log.txt";
 
 	private static final String LOAD_SCRIPT_LOCATION = "load_applet.txt";
 	private static final String DELETE_SCRIPT_LOCATION = "delete_applet.txt";
 
 	private static final String MUTIL_LOCATION = "MUtil/MUtil.exe";
 	
 	private static Random rand;
 	
 	private int progress = 0;
 	private Properties config;
 	
 	static {
 		rand = new Random();
 	}
 	
 	public CardWriter(Properties config) {
 		this.config = config;
 	}
 	
 	public void Write(Card card) throws Exception {
 		setProgress(5);
 	
 		byte[] pin = new byte[4];
 		for(int i = 0; i < 4; i++) {
 			pin[i] = (byte) (rand.nextInt(10) + 0x30);
 		}
 		
 		try{
 			DatabaseConnection.setCardStatusPersonalized(card.getCardId(), config); //Start with db, so we won't have unsaved personalized cards. Will commit when card is personalized.
 			setProgress(15); 
 			
 			loadApplet();
 			setProgress(40); 
 			
 			IrmaIssuer issuer = new IrmaIssuer();
 			Attributes attributes = new Attributes();
 			attributes.add("userID", card.getUserID().getBytes());
 			attributes.add("securityHash", hash(card.getUserID(), Integer.toString(card.getCardId())));
 			issuer.issue(attributes);
 			issuer.setPin(pin);
 			setProgress(80);
 				
 			card.setPersonalised();
 			
 			setProgress(90);
 			sendMail(pin, card);
 			setProgress(100);
 		}
 		catch(Exception e) {
 			try {
 				DatabaseConnection.rollback();
 			}
 			catch (Exception ex) {
 				ex.printStackTrace();
 				Logger.log("Error while rolling back", ex);
 			}; //Throw original exception
 			throw e;
 		}
 		DatabaseConnection.commit(); //Only save the changed status if personalization actually worked
 	}
 
 	private byte[] hash(String... strings) throws NoSuchAlgorithmException {
 		MessageDigest digest = MessageDigest.getInstance("SHA-256");
 		for(String string : strings) {
 			digest.update(string.getBytes());
 		}
 		return digest.digest();
 	}
 
 	private void loadApplet() throws IOException, InterruptedException, MUtilException {
 		Runtime rt = Runtime.getRuntime();
 		Process proc = rt.exec(new String[]{MUTIL_LOCATION, LOAD_SCRIPT_LOCATION, MUTIL_LOG_LOCATION});
 		proc.waitFor();
 		
 		Scanner scan = new Scanner(new File(MUTIL_LOG_LOCATION));
 		String line = scan.nextLine();
 		while(scan.hasNextLine()) {
 			line = scan.nextLine();
 		}
 		
 		//It is not only OK if the applet is succesfully loaded, but also if it was already on the card
 		if(!line.equals("Line 3:;Loaded") && !line.endsWith("Duplicate AID")) {
 			throw new MUtilException(line);
 		}
 	}
 	
 	public static void clearCard() throws IOException, InterruptedException, MUtilException {
 		Runtime rt = Runtime.getRuntime();
 		Process proc = rt.exec(new String[]{MUTIL_LOCATION, DELETE_SCRIPT_LOCATION, MUTIL_LOG_LOCATION});
 		proc.waitFor();
 		
 		Scanner scan = new Scanner(new File(MUTIL_LOG_LOCATION));
 		String line = scan.nextLine();
 		while(scan.hasNextLine()) {
 			line = scan.nextLine();
 		}
 		
 		//It is not only OK if the applet is succesfully deleted, but also if it was never on the card
 		if(!line.equals("Line 3:;Deleted") && !line.endsWith("Application Not Loaded")) {
 			throw new MUtilException(line);
 		}
 	}
 
 	private void sendMail(byte[] pin, Card card) throws MessagingException, FileNotFoundException, IOException {
 		
 		String pinString = new String(pin);
 		
 		Properties props = System.getProperties();
 		props.put("mail.smtp.host", config.getProperty("smtpServer"));
 		Session session = Session.getDefaultInstance(props);
		Message msg = new MimeMessage(session){
			@Override
			protected void updateMessageID() throws MessagingException {
				super.updateMessageID();
				String messageId = getMessageID();
				String fromAdres = config.getProperty("fromAdres");
				setHeader("Message-ID", messageId.substring(0, messageId.indexOf('@')) + fromAdres.substring(fromAdres.indexOf('@')));
			}
		};
 		try {
 			msg.setFrom(new InternetAddress(config.getProperty("fromAdres")));
 		} catch (AddressException e) {
 			e.printStackTrace();
 		}
 		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(card.getEmail()));
 		msg.setSubject(config.getProperty("mailSubject"));
 		msg.setText(config.getProperty("mailBody").replace("$NAME$", card.getName()).replace("$PIN$", pinString));
 		msg.setSentDate(new Date());
 		Transport.send(msg);
 	}
 	
 	private void setProgress(int progress) {
 		this.progress = progress;
 		setChanged();
 		notifyObservers();
 	}
 	
 	public int getProgress() {
 		return progress;
 	}
 	
 	public static class MUtilException extends Exception {
 		private static final long serialVersionUID = 0x9288f90e2cfb7723L;
 
 		public MUtilException(String message) {
 			super(message);
 		}
 	}
 }
