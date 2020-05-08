 package SMTPClient;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import Email.Email;
 import Email.EmailStatus;
 import Interfaces.ISMTPClient;
 
 public class SMTPClient{
 	 String m = null;
 	
 	public String sendEmail(Email email) throws UnknownHostException, IOException {
 		
 		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 		      
 		PrintStream out = System.out;
 		      
 		     
 		         Socket c = new Socket("mail.ik2213.lab",25);
 		         
 		         BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
 		            c.getOutputStream()));
 		         BufferedReader r = new BufferedReader(new InputStreamReader(
 		            c.getInputStream()));
 		        
 		         
 		         //for the delay
 		         /*for (int i = 0; i < delay; i++)
 		         {
 		          try
 		              {
 		              Thread.sleep(1000);        
 		              }
 		           catch (InterruptedException ie)
 		              {
 		            System.out.println(ie.getMessage());
 		              }
 		         }*/
 		         
 		         //For Sending the HELO Message
 		         String hostName = email.get_smtpServer();
 		         //String hostName = " gmani.mail.ik2213.lab";
 		         
 		              
 		         w.write("EHLO "+hostName);
 		         w.newLine();
 		         int i =8;
 		         do
 		         {
 		         w.flush();
 		         m=r.readLine(); out.println(m);
 		         i--;
 		         }while(i!=0);
 		         ;
 		         //For Sending The From Address
 		         String from = email.get_from();
 		         //String from = "<gmani@mail.ik2213.lab>";
 		         out.println(from);
 		         String mailfrom = "MAIL FROM: " +"<"+ from +">";
 		         w.write(mailfrom);
 		         w.newLine();
 		         w.flush();
 		        m=r.readLine();out.println(m);
 		       
 		        //for checking the correctness of from address
 		        if(!m.equals("250 2.1.0 Ok"))
 		       {
 		        	out.println("Error in From Part");
 		        	return m;
 		       }
 		         //For Sending The TO Address
 		         
 		        String to = email.get_to();
 		        //String to = " <gmani@mail.ik2213.lab>";
 		         w.write("RCPT TO: "+"<"+to+">");
 		         w.newLine();
 		         w.flush();
 		         m=r.readLine();out.println(m);
 		       //for checking the correctness of from address
 			        if(!m.equals("250 2.1.5 Ok"))
 			       {
 			        	out.println("Error in To Part");
 			        	return m;
 			       }
 		         
 			     String message = email.get_message();
 			     //String message = "hai how are you.";
 			     String subject= email.get_subject();	
 			     //String subject= "hello";
 		         
 		         w.write("DATA");
 		         w.newLine();
 		         w.flush();
 		      // send header As a part of Message
 		         /*w.write("From: " + from + "\r\n");
 		         w.write("To: " + to + "\r\n");
 		         w.write("Subject: " + subject + "\r\n");
 		         w.write("\r\n"); // end header*/
 
 		         w.write("MIME-Version: 1.1"+ "\r\n");
		         w.write("Content-Type: text/RFC822"+ "\r\n");
 		         w.write("Content-Transfer-Encoding: quoted-printable"+"\r\n");
 		         //w.write("\r\n"); // end header
		         //w.flush();
 		         String m1=r.readLine();out.println(m1);
 		     
 		         // send header As a part of Message
 		         w.write("From: " + from + "\r\n");
 		         w.write("To: " + to + "\r\n");
		         w.write("Subject: " + subject + "\r\n");
 		         w.write("\r\n"); // end header
 		        
 		         // The Body of the mail		         
 		         w.write(message + "\r\n");
 
 		         // end data with <CR/LF>.<CR/LF>
 		         w.write("\r\n.\r\n.....");
 		         w.flush();
 		         
 		         m=r.readLine(); out.println(m);
 		         if(!m.regionMatches(0, "2.0.0 Ok: queued as",0,10))
 			       {
 			        	out.println("Error in Body");
 			        	return m;
 			       }
 		         // To close the SMTP connection
 		         String quit = "QUIT";
 		         w.write(quit);
 		         w.newLine();
 		         w.flush();
 		         
 		         //m=r.readLine();out.println(m);
 		         
 		        
 		         w.close();
 		         r.close();
 		         c.close();
 		         
 		     
 			return m;
 
 	}
 
 	
 
 }
