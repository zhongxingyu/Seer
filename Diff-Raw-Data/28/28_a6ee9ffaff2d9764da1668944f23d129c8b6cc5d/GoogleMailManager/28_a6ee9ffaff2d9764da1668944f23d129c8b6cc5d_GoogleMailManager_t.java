 package de.fhb.autobday.manager.mail;
 
 import java.io.IOException;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.ejb.LocalBean;
 import javax.ejb.Singleton;
 import javax.ejb.Startup;
 import javax.interceptor.Interceptors;
 import javax.mail.AuthenticationFailedException;
 import javax.mail.Message.RecipientType;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import de.fhb.autobday.commons.PropertyLoader;
 import de.fhb.autobday.data.AbdAccount;
 import de.fhb.autobday.exception.mail.FailedToLoadPropertiesException;
 import de.fhb.autobday.exception.mail.FailedToSendMailException;
 import de.fhb.autobday.manager.LoggerInterceptor;
 
 /**
  * This is the mailmanager, which is responsible for sending mails
  *
  * @author
  * Andy Klay <klay@fh-brandenburg.de>
  * Michael Koppen <koppen@fh-brandenburg.de>
  */
 @Singleton
 @Startup
 @LocalBean
 @Interceptors(LoggerInterceptor.class)
 public class GoogleMailManager implements GoogleMailManagerLocal{
 	
 	private final static Logger LOGGER = Logger.getLogger(GoogleMailManager.class.getName());
 	private PropertyLoader propLoader;
 	
 	public GoogleMailManager() {
 		propLoader = new PropertyLoader();
 	}
 	
	@Override
 	public synchronized void sendSystemMail(String subject, String message, String to) throws Exception {
 
 		Properties accountProps = null;
 		
 		try {
 			
 			//DONT CHANGE THIS PATH
 			accountProps = propLoader.loadSystemMailAccountProperty("SystemMailAccount.properties");	
 
 			String user = accountProps.getProperty("mail.smtp.user");
 			String password = accountProps.getProperty("mail.smtp.password");
			
			this.sendUserMailInternal(user, password, subject, message, to);
 			
         } catch (IOException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			throw new FailedToLoadPropertiesException("Failed to load mail properties.");
 		} catch (Exception ex) {
             LOGGER.log(Level.SEVERE, null, ex);
 			throw new FailedToSendMailException("Failed to send mail.");
         }
 		
		
 
 	}
	@Override
 	public synchronized void sendUserMail(AbdAccount account, String subject, String message, String to) throws Exception{
		sendUserMailInternal(account.getUsername(), account.getPasswort(), subject, message, to);
	}
	
	private synchronized void sendUserMailInternal(String username, String password, String subject, String message, String to) throws Exception{
 		Properties systemProps = null;
 		try {
 			
 			//DONT CHANGE THIS PATH
 			systemProps = propLoader.loadSystemMailProperty("SystemMail.properties");
 		
 			//systemProps
 			String host = systemProps.getProperty("mail.smtp.host");
 			
             //Obtain the default mail session
             Session session = Session.getDefaultInstance(systemProps, null);
             session.setDebug(true);
 			
             //Construct the mail message
             MimeMessage mail = new MimeMessage(session);
 			
             mail.setText(message);
             mail.setSubject(subject);
            mail.setFrom(new InternetAddress(username));
             mail.addRecipient(RecipientType.TO, new InternetAddress(to));
             mail.saveChanges();
 			
             //Use Transport to deliver the message
             Transport transport = session.getTransport("smtp");
            transport.connect(host, username, password);
             transport.sendMessage(mail, mail.getAllRecipients());
             transport.close();
 			
         } catch (AuthenticationFailedException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			throw new FailedToSendMailException("Authentication-Error. Please check your credentials.");
 		} catch (IOException ex) {
 			LOGGER.log(Level.SEVERE, null, ex);
 			throw new FailedToLoadPropertiesException("Failed to load mail properties.");
 		} catch (Exception ex) {
             LOGGER.log(Level.SEVERE, null, ex);
 			throw new FailedToSendMailException("Failed to send mail.");
         }
 
 	}
 
	@Override
 	public PropertyLoader getPropLoader() {
 		return propLoader;
 	}
 
	@Override
 	public void setPropLoader(PropertyLoader propLoader) {
 		this.propLoader = propLoader;
 	}
 }
 
