 package org.archboy.clobaframe.mail.impl;
 
 import org.apache.commons.mail.DefaultAuthenticator;
 import org.apache.commons.mail.Email;
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.HtmlEmail;
 import org.apache.commons.mail.SimpleEmail;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 import javax.inject.Named;
 import org.archboy.clobaframe.mail.SenderAgent;
 import org.archboy.clobaframe.mail.SendMailException;
 
 /**
  *
  * @author arch
  */
 @Named
 public class SmtpSenderAgent implements SenderAgent {
 
 	private static final String AGENT_NAME = "smtp";
 	private static final int DEFAULT_PORT = 25; // smtp standard port, the port with TLS usually is 587.
 	private static final boolean DEFAULT_TLS = false; // do not use TLS by default.
 	
 	@Value("${mail.smtp.host}")
 	private String host;
 	
 	@Value("${mail.smtp.port}")
 	private int port = DEFAULT_PORT;
 	
 	@Value("${mail.smtp.tls}")
 	private boolean tls = DEFAULT_TLS;
 	
 	@Value("${mail.smtp.loginName}")
 	private String loginName;
 
 	@Value("${mail.smtp.loginPassword}")
 	private String loginPassword;
 
 	@Value("${mail.smtp.fromAddress}")
 	private String fromAddress;
 
	private final Logger logger = LoggerFactory.getLogger(SmtpSenderAgent.class);
 
 	@Override
 	public String getName() {
 		return AGENT_NAME;
 	}
 
 	@Override
 	public void send(String recipient, String subject, String content) throws SendMailException {
 		Email email = new SimpleEmail();
 		email.setHostName("smtp.gmail.com");
 		email.setAuthenticator(new DefaultAuthenticator(loginName, loginPassword));
 		email.setSmtpPort(587);
 		email.setTLS(true);
 
 		try {
 			email.setCharset("UTF-8"); // specify the charset.
 			email.setFrom(fromAddress);
 			email.setSubject(subject);
 			email.setMsg(content);
 			email.addTo(recipient);
 			email.send();
 		} catch (EmailException e) {
 			logger.error("Send mail from Gmail agent error.", e);
 			throw new SendMailException(
 					String.format("Failed to send mail to %s.", recipient));
 
 		}
 	}
 
 	@Override
 	public void sendWithHtml(String recipient, String subject, String content) throws SendMailException {
 		HtmlEmail email = new HtmlEmail();
 		email.setHostName("smtp.gmail.com");
 		email.setAuthenticator(new DefaultAuthenticator(loginName, loginPassword));
 		email.setSmtpPort(587);
 		email.setTLS(true);
 
 		try {
 			email.setFrom(fromAddress);
 			email.setSubject(subject);
 			email.setHtmlMsg(content);
 			//email.setMsg(""); // can set plain text either
 			email.addTo(recipient);
 			email.send();
 		} catch (EmailException e) {
 			logger.error("Send mail from Gmail agent error.", e);
 			throw new SendMailException(
 					String.format("Failed to send mail to %s.", recipient));
 		}
 	}
 }
