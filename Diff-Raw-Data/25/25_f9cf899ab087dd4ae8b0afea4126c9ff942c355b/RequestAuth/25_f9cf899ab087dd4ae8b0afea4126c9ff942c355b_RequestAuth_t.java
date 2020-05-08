 package helpertools;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 //import javax.mail.PasswordAuthentication;
 import javax.mail.Transport;
 import javax.mail.Session;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 
 @SuppressWarnings("serial")
 public class RequestAuth implements /*ActionListener,*/ Serializable/*, Runnable */{
 	private String file = null;
 	private String username;
 //	public boolean authenticated;
 	private Session session;
 //	private char[] password;
 	private String newLine = System.getProperty("line.separator");
	private boolean emailToAdmin = true; // default option when using the requestAuth obj
 	private NewUser newUser = null;
 	
	public RequestAuth(String username, boolean sendEmail) {
		this(username);
		this.emailToAdmin = sendEmail;
	}
	
 	public RequestAuth(String username) {
 		this.username = username;
 	}
 	
 	@Deprecated
 	public RequestAuth(String username,char[] password, String file) {
 		this.file = file;
 		this.username = username;
 //		this.password = password;
 	}
 
 //	@Override
 //	public void actionPerformed(ActionEvent ae) {
 //		if (ae.getSource() == submitBtn) {
 //			System.out.println("About to wake up thread...");
 //			synchronized (this) {
 //				t.notify(); // wake the thread up
 //			}
 //			System.out.println("Thread is alive?: " + t.isAlive());
 //			final String user = emailField.getText();
 //			char[] pass = passwordField.getPassword();
 //			final String epassword = new String(pass);
 //			System.out.println("About to send msg...");
 //			try {
 //				System.out.println("About to authenticate...");
 //				statusMsgs.setText("Sending message...");
 //				Authenticate(user, epassword);
 //				System.out.println("Authenticated!");
 ////				sendMessageToUser(this.session, user, file);
 //				emailField.setText("");
 //				passwordField.setText("");
 //				statusMsgs.setText("Message sent!");
 //				emailFrame.dispose();
 //			}
 //			catch (Exception me){
 //				me.printStackTrace();
 //				JOptionPane.showMessageDialog(null, "The password and username are not correct.");
 //			}
 //			statusMsgs.setText("Click submit to login...");
 //		}
 //	}
 	
 	public void Authenticate () {
 		Properties props = new Properties();
 		props.put("mail.smtp.auth", "false");
 		props.put("mail.smtp.starttls.enable", "true");
 		props.put("mail.smtp.host", "mail.rtp.local");
 		props.put("mail.smtp.port", "25");
 		/* Use an authenticator to connect to gmail */
 //		final String pass = new String(password);
 //		final String user = this.username;
 		
 		Session session = Session.getInstance(props, null);
 		// clear out password when done
 //		for (int i = 0; i < password.length; i++) {
 //			password[i] = '0';
 //		}
 		this.session = session;
 	}
 	
 	public void sendMessageToUser() throws AddressException, MessagingException, IOException {
		if (!emailToAdmin) return; // if we didn't specify an address don't do anything.
 		System.out.println("Setting up message to send.");
 		/* Set up a new message */
 		Message message = new MimeMessage(this.session);
 		message.setFrom(new InternetAddress(this.username));
 		message.setRecipients(Message.RecipientType.TO,
 			InternetAddress.parse(this.username));
 		message.setSubject("Request for New User Account Sent");
 		if (file != null) {
 			// Attach the specified file, so there needs to be
 			// a multipart message created.
 			MimeBodyPart mbp1 = new MimeBodyPart();
 			mbp1.setText("This is the form that was generated for this user " + this.file);
 			MimeBodyPart mbp2 = new MimeBodyPart();
 			mbp2.attachFile(this.file);
 			MimeMultipart mp = new MimeMultipart();
 			mp.addBodyPart(mbp1);
 			mp.addBodyPart(mbp2);
 			message.setContent(mp);
 		}
 		else if (newUser != null) {
 			String msgToSend = "Request sent to Network Administrator for the following user: " + newLine;
 			
 			String firstName = newUser.getF_name();
 			String lastName = newUser.getL_name(); 
 			String prefName = newUser.getPref_name(); 
 			String midName = newUser.getM_initials(); 
 			String title = newUser.getTitle(); 
 			String manager = newUser.getManager(); 
 			String dept = newUser.getDept(); 
 			String employeeID = newUser.getDiosynth_empID(); 
 			String location = newUser.getEmp_location(); 
 			String startDate = newUser.getEmp_startDate(); 
 			String empRehire = newUser.isEmp_rehire(); 
 			String empStatus = newUser.isEmp_status();
 			String retVal = null;
 			
 			if (firstName.length() != 0) {
 				retVal = firstName;
 				msgToSend += "First Name: " + retVal + newLine;
 			}
 			
 			if (midName.length() != 0) {
 				retVal = midName;
 				msgToSend += "Middle Initials: " + retVal + newLine;
 			}
 			
 			if (lastName.length() != 0) {
 				retVal = lastName;
 				msgToSend += "Last Name: " + retVal + newLine;
 			}
 			
 			if (prefName.length() != 0) {
 				retVal = prefName;
 				msgToSend += "Preferred Name: " + retVal + newLine;
 			}
 			
 			if (title.length() != 0) {
 				retVal = title;
 				msgToSend += "Title: " + retVal + newLine;
 			}
 			
 			if (manager.length() != 0) {
 				retVal = manager;
 				msgToSend += "Manager: " + retVal + newLine;
 			}
 			
 			if (dept.length() != 0) {
 				retVal = dept;
 				msgToSend += "Department: " + retVal + newLine;
 			}
 			
 			if (employeeID.length() != 0) {
 				retVal = employeeID;
 				msgToSend += "Employee ID:" + retVal + newLine;
 			}
 			
 			if (location.length() != 0) {
 				retVal = location;
 				msgToSend +="Location: " + retVal + newLine;
 			}
 			
 			if (startDate.length() != 0) {
 				retVal = startDate;
 				msgToSend += "Start Date: " + retVal + newLine;
 			}
 			
 			if (empRehire.length() != 0) {
 				retVal = empRehire;
 				msgToSend += "Is the employee is a rehire? " + retVal + newLine;
 			}
 			
 			if (empStatus.length() != 0) {
 				retVal = empStatus;
 				msgToSend += "Employee Status: " + retVal + newLine;
 			}
 			
 			message.setText(msgToSend);
 		}
 		else {
 			message.setText("Request sent to Network Administrator.");
 		}
		Transport.send(message); 
 		System.out.println("Actually sent message.");
 	}
 	
 	public void addUser(NewUser newUser) {
 		this.newUser = newUser;
 	}
 	
 	public void updateFilename(String filename) {
 		this.file = filename;
 	}
 	public String getFilename() {
 		return this.file;
 	}
 //	public boolean isAuthenticated() {
 //		return this.authenticated;
 //	}
 
 //	@Override
 //	public void run() {
 //		// TODO Auto-generated method stub
 //		System.out.println("In the run method...");
 //		try {
 //			synchronized(this) {
 //				if (!isAuthenticated()) {
 //					while (authenticated == false) {
 //						System.out.println("waiting...");
 //						t.wait(5);
 //					}
 //				}
 //				else {
 //					System.out.println("Out of waiting!");
 //					sendMessageToUser();
 //				}
 //			}
 //		} catch (InterruptedException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		} catch (AddressException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		} catch (MessagingException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		} catch (IOException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 //	}
 }
