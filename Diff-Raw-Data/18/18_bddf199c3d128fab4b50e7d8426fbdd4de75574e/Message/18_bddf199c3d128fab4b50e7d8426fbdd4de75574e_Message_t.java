 import java.util.*;
 import java.text.*;
 
 /* $Id: Message.java,v 1.5 1999/07/22 12:10:57 kangasha Exp $ */
 
 /**
  * Mail message.
  *
  * @author Jussi Kangasharju
  * @author Julien Antony
  */
 public class Message {
     /* The headers and the body of the message. */
     public String Headers;
     public String Body;
 
     /* Sender and recipient. With these, we don't need to extract them
        from the headers. */
     private String From;
     private ArrayList<String> To;
     private ArrayList<String> Cc;
     
     /* To make it look nicer */
     private static final String CRLF = "\r\n";
 
     /* Create the message object by inserting the required headers from
        RFC 822 (From, To, Date). */
     public Message(String from, String to,String cc, String subject, String text) {
     	To = new ArrayList<String>();
     	Cc = new ArrayList<String>();
     	
     	//split to and cc fields
     	String[] toList = to.split(";");
     	String[] ccList = cc.split(";");
     	
     	/* Remove whitespace */
 		From = from.trim();
 		for(int i=0 ;i<toList.length; i++){
 			String trimmedTo = toList[i].trim();
			if(trimmedTo.indexOf(";")!= -1)
				trimmedTo = trimmedTo.substring(0,trimmedTo.indexOf(";"));
 			To.add(trimmedTo);
 		}
 		for(int i=0 ;i<ccList.length; i++){
 			String trimmedCc = ccList[i].trim();
			if(trimmedCc.indexOf(";")!= -1)
				trimmedCc = trimmedCc.substring(0,trimmedCc.indexOf(";"));
			if(!trimmedCc.equals(""))
				Cc.add(trimmedCc);
 		}
 		
 		Headers = "From: " + From + CRLF;
 		for(String email: To){
 			Headers += "To: " + email + CRLF;
 		}
 		
 		if(!cc.equals("")){
 			for(String email: Cc){
 				Headers += "Cc: " + email + CRLF;
 			}
 		}
 		
 		
 		Headers += "Subject: " + subject.trim() + CRLF;
 
 		/* A close approximation of the required format. Unfortunately
 		   only GMT. */
 		SimpleDateFormat format = 
 		    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'");
 		String dateString = format.format(new Date());
 		Headers += "Date: " + dateString + CRLF;
 		Body = text;
     }
 
     /* Two functions to access the sender and recipient. */
     public String getFrom() {
 		return From;
     }
 
     public ArrayList<String> getTo() {
 		return To;
     }
     
     public ArrayList<String> getCc() {
 		return Cc;
     }
 
     /* Check whether the message is valid. In other words, check that
        both sender and recipient contain only one @-sign. */
     public boolean isValid() {
 		int fromat = From.indexOf('@');
 		
 		
 
 		if(fromat < 1 || (From.length() - fromat) <= 1) {
 		    System.out.println("Sender address is invalid");
 		    return false;
 		}
 		
 		for(String email: To){
 			int toat = email.indexOf('@');
 			if(toat < 1 || (email.length() - toat) <= 1) {
 			    System.out.println("Recipient address is invalid");
 			    return false;
 			}
 		}
 		
 		if(Cc.size()!= 0){
 			for(String email: Cc){
				int ccat = email.indexOf('@');
 				if(ccat < 1 || (email.length() - ccat) <= 1) {
 				    System.out.println("Cc Recipient address is invalid");
 				    return false;
 				}
 			}
 		}
 		
 		if(fromat != From.lastIndexOf('@')) {
 		    System.out.println("Sender address is invalid");
 		    return false;
 		}
 		for(String email: To){
 			int toat = email.indexOf('@');
 			if(toat != email.lastIndexOf('@')) {
 			    System.out.println("Recipient address is invalid");
 			    return false;
 			}	
 		}
 		
 		for(String email: Cc){
 			int ccat = email.indexOf('@');
 			if(ccat != email.lastIndexOf('@')) {
 			    System.out.println("Recipient address is invalid");
 			    return false;
 			}	
 		}
 		
		return true;
     }
     
     /* For printing the message. */
     public String toString() {
 		String res;
 
 		res = Headers + CRLF;
 		res += Body;
 		return res;
     }
 }
