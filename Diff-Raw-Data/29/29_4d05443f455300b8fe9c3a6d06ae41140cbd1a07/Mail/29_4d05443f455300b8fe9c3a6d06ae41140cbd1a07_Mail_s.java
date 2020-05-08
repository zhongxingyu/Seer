 package models;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 
 import org.apache.commons.mail.Email;
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 
 import play.Logger;
 import play.data.validation.EmailCheck;
 import plugins.AutoBean;
 import util.Utils;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 import exception.CommandException;
 
 
 @AutoBean
 @XStreamAlias("mail")
 public class Mail extends AbstractCommand {
 
 	@XStreamOmitField
 	EmailCheck check = new EmailCheck();
 
 	@XStreamOmitField
 	transient Email fEmail;
 	
 	@XStreamOmitField
 	transient boolean fSent;
 	
 	public Eval subject;
 	
 	/** Address of the sender */
 	public Eval from;
 	
 	/** Address to which reply */
 	public Eval reply;
 	
 	/** Address of the recipients */
 	public Eval to;
 	
 	/** Addresses on carbon copy */
 	public Eval cc;
 
 	/** Addresses on blind carbon copy */
 	public Eval bcc;
 	
 	/** The message content */
 	public Eval body;
 	
 	/** The default constructor */
 	public Mail() {}
 	
 	/** The copy constructor */
 
 	@Override
 	public boolean run() throws CommandException {
 		
 		String _from = from != null ? from.eval() : AppProps.instance().getWebmasterEmail();
 		String _reply = reply != null ? reply.eval() : Service.current().bundle.email;
 		String _to = to != null ? to.eval() : null;
 		String _cc = cc != null ? cc.eval() : null;
 		String _bcc = bcc != null ? bcc.eval() : null;
 		String _subject = subject != null ? subject.eval() : null;
 		String _body = body != null ? body.eval() : null;
 		
 		try {
     		
 			/* if 'reply-to' is still empty just use the 'from' field */
     		if( Utils.isEmpty(_reply)) _reply = _from;
     		
         	/*
         	 * create the email object
         	 */
         	fEmail = new SimpleEmail();
         	
         	
     		/* 
     		 * Validate and set From address 
     		 */ 
     		if( Utils.isEmpty(_from) ) {
     			Logger.error("Cannot send mail with missing sender");
     			return false;
     		}	
     		
         	fEmail.setFrom(_from);
 
     		/*
     		 * reply-to 
     		 */
     		List<InternetAddress> listReply;
     		if( Utils.isNotEmpty(_reply) && (listReply = asList(_reply))!=null) {
 				fEmail.setReplyTo(listReply);
     		}
     		
         	
     		List<InternetAddress> listTo = asList(_to);
     		List<InternetAddress> listCc = asList(_cc);
     		List<InternetAddress> listBcc= asList(_bcc);
 
     		/* 
     		 * check if at least one target recipient exists, otherwise just skip the email submit returning TRUE
     		 */
     		if( listTo == null && listCc == null && listBcc == null  ) {
     			return true;
     		}
     		
     		/* 
     		 * set TO address 
     		 */
     		if( listTo != null ) {
         		fEmail.setTo(listTo);
     		}
 
 
     		/* 
     		 * check CC addresses 
     		 */
     		if( listCc != null ) {
     			fEmail.setCc(listCc);
     		}
 
     		/* 
     		 * check BCC addresses 
     		 */
     		if( listBcc != null ) {
     			fEmail.setBcc(listBcc);
     		}  		
     		
     		
     		/*
     		 * check subject and body 
     		 */
     		if( Utils.isEmpty(_subject) && Utils.isEmpty(_body) ) {
     			return true;
     		}
     	
         	fEmail.setSubject(_subject);
         	fEmail.setMsg(_body);
 
     		Logger.debug("Sending e-mail From: '%s', Reply: '%s', To: '%s', Cc: '%s', Bcc: '%s', Subject: %s",
 					_from,
 					_reply,
 					_to,
 					_cc, 
 					_bcc,
 					_subject);
 	
         	
         	Future<Boolean> result = play.libs.Mail.send(fEmail);
             return fSent=result.get();
         } 
         catch (InterruptedException e) {
             Logger.error(e, "Error while waiting Mail.send result");
         } 
         catch (ExecutionException e) {
             Logger.error(e, "Error while waiting Mail.send result");
         } 
         catch( EmailException e ) { 
         	Logger.error(e, "Malformed email message From: '%'; To: '%s'", _from, _to );
 		}
         
         return false;
 
 	}
 
 	
 	/**
 	 * Convert a comma separated string of email address and converts to a list of {@link InternetAddress} instances 
 	 * 
 	 * @param recipients multiple email addresses separated bu a comma ',' or a semicolon ';' char
 	 * @return a list containing at least one {@link InternetAddress} or null otherwise (if empty)
 	 */
 	public static List<InternetAddress> asList( String recipients ) 
 	{ 
 		if( recipients == null ) { return null; }
 		
 		
 		recipients = recipients.replace(",", ";"); // <-- normalize to semicolon separetor
 		String[] items = recipients.split(";");
 		
 		List<InternetAddress> result = new ArrayList<InternetAddress>(items.length);
 		for( String item : items ) {
 			item = item != null ? item.trim() : null;
 			if( Utils.isNotEmpty(item) ) {
 				try {
 					result.add(new InternetAddress(item));
 				} catch (AddressException e) {
 					Logger.warn("Invalid email address format: '%s'", item);
 				}
 			}
 		}
 
 		return result.size() > 0 ? result : null;
 	}
 	
 	public static String toString( List<InternetAddress> addresses ) { 
 		if( addresses == null ) { 
 			return null;
 		}
 		
 		List<String> list = new ArrayList<String>(addresses.size());
 		for( InternetAddress addr : addresses ) { 
 			list.add(addr.getAddress());
 		}
 		
 		return Utils.asString(list);
 	}
 }
