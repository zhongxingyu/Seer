 package SMTPClient;
 
 import Email.Email;
 
 
 public class SMTPTester {
 
 	private static Email email=null;
 	private static int delay=10;
 	/**
 	 * @param args
 	 */
	public static void main(String[] args) {
 	SMTPClient smtp = new SMTPClient();
 	Email email = new Email();
 	email.set_smtpServer(" gmani.mail.ik2213.lab");
 	email.set_from(" <gmani@mail.ik2213.lab>");
 	email.set_to(" <gmani@mail.ik2213.lab>");
	email.set_subject("hello");
	email.set_message("Swedish characters ÅÄÖåäö");
 	String returnmessage =smtp.sendEmail(email);
 	System.out.println("From STMPTESTER:"+returnmessage);
 	}
 
 }
