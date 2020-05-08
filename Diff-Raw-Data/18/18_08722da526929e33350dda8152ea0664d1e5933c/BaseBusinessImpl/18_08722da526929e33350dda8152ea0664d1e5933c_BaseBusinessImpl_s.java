 package edu.cmu.square.server.business.implementation;
 
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.mail.MessagingException;
 
 import org.apache.velocity.app.VelocityEngine;
 import org.springframework.mail.MailSender;
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.velocity.VelocityEngineUtils;
 
 import edu.cmu.square.client.exceptions.ExceptionType;
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.server.dao.model.User;
 
 @Transactional
 public abstract class BaseBusinessImpl {
 	private static final String EMAIL_VALIDATOR = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
 	
 	private String userName;
 
 	private String projectName;
 	
 	@Resource
 	private VelocityEngine velocityEngine;
 	
 	@Resource
 	private MailSender mailSender;
 	
 	public String getUserName() {
 		return userName;
 	}
 
 	public void setUserName(String userId) {
 		this.userName = userId;
 	}
 
 	public String getProjectName() {
 		return projectName;
 	}
 
 	public void setProjectName(String projectName) {
 		this.projectName = projectName;
 	}
 	
 	public void setSessionExpired(boolean sessionExpired)
 	{
 		this.sessionExpired = sessionExpired;
 	}
 
 	public boolean isSessionExpired()
 	{
 		return sessionExpired;
 	}
 
 	private boolean sessionExpired;
 	
 	/**
 	 * This sends an email to the user 
 	 * 
 	 * @param updateUser
 	 * @throws MessagingException
 	 * @throws SquareException
 	 */
 	protected void sendMail(User updateUser, SimpleMailMessage templateMessage, String velocityTemplate) throws SquareException
 	{
 		// Create a thread safe "copy" of the template message and customize it
 		try 
 		{
 			Map<String, Object> model = new Hashtable<String, Object>();
 			model.put("user", updateUser);
 			String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, velocityTemplate, model);
 
			
 			if (updateUser.getEmail() == null || !(updateUser.getEmail().matches(EMAIL_VALIDATOR)))
 			{
 				SquareException se = new SquareException("Invalid email address");
 				se.setType(ExceptionType.invalidEmail);
 				throw se;
 			}
		
 			templateMessage.setTo(updateUser.getEmail());
 			templateMessage.setText(text);
			mailSender.send(templateMessage);
 		}
 		catch (SquareException se)
 		{
 			throw se;
 		}
 		catch (Throwable m)
 		{
 			SquareException se = new SquareException("Error sending mail", m);
 			se.setType(ExceptionType.mailError);
 			throw se;
 		}
 	}
 	
 }
