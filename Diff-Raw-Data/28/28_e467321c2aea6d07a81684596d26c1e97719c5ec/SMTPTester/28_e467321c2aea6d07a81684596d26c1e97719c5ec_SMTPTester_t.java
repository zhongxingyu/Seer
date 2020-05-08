 package SMTPClient;
 
import java.io.IOException;
import java.net.UnknownHostException;

 import Email.Email;
 
 
 public class SMTPTester {
 
 	private static Email email=null;
 	private static int delay=10;
 	/**
 	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
 	 */
	public static void main(String[] args) throws UnknownHostException, IOException {
 	SMTPClient smtp = new SMTPClient();
 	Email email = new Email();
 	email.set_smtpServer(" gmani.mail.ik2213.lab");
 	email.set_from(" <gmani@mail.ik2213.lab>");
 	email.set_to(" <gmani@mail.ik2213.lab>");
	email.set_subject("=E4=E5");
	email.set_message("Hai this is swedish message =E4=E5");
 	String returnmessage =smtp.sendEmail(email);
 	System.out.println("From STMPTESTER:"+returnmessage);
 	}
 
 }
