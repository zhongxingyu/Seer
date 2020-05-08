 package com.stationmillenium.rdsmanager.services.alertmails;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.mail.MailException;
 import org.springframework.mail.MailSender;
 import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
 import org.springframework.stereotype.Service;
 
 /**
  * Service to send alert mails
  * @author vincent
  *
  */
 @Service
 public class AlertMailService {
 
 	//logger
 	private static final Logger LOGGER = LoggerFactory.getLogger(AlertMailService.class);
 	
 	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
 
 	//mail sender
 	@Autowired
 	private MailSender mailSender;
 	
 	//mail templates
 	@Autowired @Qualifier("comPortErrorAlertTemplate") private SimpleMailMessage comPortErrorAlertTemplate;
 	@Autowired @Qualifier("rdsCoderErrorAlertTemplate") private SimpleMailMessage rdsCoderErrorAlertTemplate;
 	
 	/**
 	 * Send the alert
 	 * @param mailToSend the mail template to send
 	 * @param e the exception
 	 */
 	private void sendAlert(SimpleMailMessage mailToSend, Exception e) {
 		SimpleMailMessage mail = new SimpleMailMessage(mailToSend); //copy template message
 		
 		computeMessageText(e, mail);
 		
 		//send mail
 		sendMail(mail);
 	}
 
 	/**
 	 * Compute the message text
 	 * @param e the exception
 	 * @param mail the mail template
 	 */
 	private void computeMessageText(Exception e, SimpleMailMessage mail) {
 		//get stack trace as string 
 		Writer result = new StringWriter();
 	    PrintWriter printWriter = new PrintWriter(result);
 	    e.printStackTrace(printWriter);
 	    String stackTrace = result.toString();
 	    
 	    //format message 
 	    Calendar calendar = Calendar.getInstance();
 		String dateText = "[" + sdf.format(calendar.getTime()) + "]\n";		
 		String originalMessageText = mail.getText();
 		String finalText = dateText + originalMessageText + " " + e.getMessage() + "\n";
 		finalText += stackTrace;
 		mail.setText(finalText);
 	}
 
 	/**
 	 * Send alert mail
 	 * @param message the message template
 	 */
 	private void sendMail(SimpleMailMessage message) {
 		try {
 			mailSender.send(message); //send message
 			LOGGER.info("Sent alert mail : " + message);
 		} catch (MailException e) {
 			LOGGER.warn("Error while send alert mail", e);
 		}		
 	}
 	
 	/**
 	 * Send a COM port error alert
 	 * @param e the exception
 	 */
	@Async
 	public void sendCOMPortErrorAlert(Exception e) {
 		LOGGER.warn("Send COM port error alert");
 		sendAlert(comPortErrorAlertTemplate, e);
 	}
 	
 	/**
 	 * Send a RDS coder error alert
 	 */
	 @Async
 	public void sendRDSCoderErrorAlert(Exception e) {
 		LOGGER.warn("Send RDS coder error alert");
 		sendAlert(rdsCoderErrorAlertTemplate, e);
 	}
 	
 }
