 package gov.nih.nci.caintegrator.application.mail;
 
 import gov.nih.nci.caintegrator.exceptions.ValidationException;
 
 import java.io.File;
 import java.text.MessageFormat;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 /**
  * 
  *  Manages sending of emails 
  *
  */
 public class MailManager {
 	
 	private static Logger logger = Logger.getLogger(MailManager.class);
 
 	/**
 	 * Sends an email notifying user that their files are ready for download
 	 * via FTP
 	 * 
 	 * @param mailTo - email address
 	 * @param fileName - filename
 	 * @throws Exception
 	 */
 	public static void sendFTPMail(String mailTo, List<String> fileNames)
 	{
 		try
 		{				
 		    // Part 1 is always included  
 		    String message = new MessageFormat(MailConfig.getFtpUnformattedBody1()).format(new String[] {MailConfig.getFileRetentionPeriodInDays(),MailConfig.getProject(),MailConfig.getAcronym()});		  
 			
 		    // Part 2 is only included if there are multiple fies
 		    if(fileNames.size() > 1) {
 		    	message += MailConfig.getFtpUnformattedBody2();	
 		    }
 		    	
 		    // Part 3 appears once for each file
 		    for(String fileName : fileNames)
 		    {    	
 		    	message += new MessageFormat(MailConfig.getFtpUnformattedBody3()).format(new String[] {cleanFileName(fileName), MailConfig.getFtpHostnameAndPort()});	
 		    }
 		    
 		    // Part 4 always appears  
 		    message += new MessageFormat(MailConfig.getFtpUnformattedBody4()).format(new String[] {MailConfig.getAppSupportNumber(), MailConfig.getTechSupportStartTime(), MailConfig.getTechSupportEndTime(),MailConfig.getAcronym(),MailConfig.getTechSupportURL()});
 		    
 	        // Send the message
 	        new SendMail().sendMail(mailTo, null, message,formatFTPSubject());		
 		} catch (Exception e) {
 			logger.error("Send FTP mail error", e);
 		} catch (ValidationException e) {
 			logger.error("Send FTP mail error", e);
 		}
 	 }
 	
	/**
	 * sendFTPErrorMail is used to send a message to the user and copy the help desk when
	 * a query was not able to successfully complete.
	 * <P>
	 * @param mailTo The email address of the user to be notified
	 */
 	public static void sendFTPErrorMail(String mailTo)
 	{
 		try
 		{				
 		    // Part 1 is always included  
 		    String message = new MessageFormat(MailConfig.getFtpUnformattedErrorBody1()).format(new String[] {MailConfig.getFileRetentionPeriodInDays(),MailConfig.getProject(),MailConfig.getAcronym()});		  
 		    
 		    // Part 2 always appears  
 		    message += new MessageFormat(MailConfig.getFtpUnformattedErrorBody2()).format(new String[] {MailConfig.getAppSupportNumber(), MailConfig.getTechSupportStartTime(), MailConfig.getTechSupportEndTime(),MailConfig.getAcronym(),MailConfig.getTechSupportURL()});
 		    
 	        // Send the message
 		    String mailCC = MailConfig.getTechSupportMail();
 		    
	        new SendMail().sendMail(mailTo, mailCC, message,formatFTPErrorSubject());		
		}
		catch (Exception e)
		{
 			logger.error("Send FTP mail error", e);
		}
		catch (ValidationException e)
		{
 			logger.error("Send FTP mail error", e);
 		}
 	 }
 
 
 	/**
 	 * Sends an email notifying user that their registration has been accepted
 	 * 
 	 * @param mailTo - email address
 	 * @param userId - user's login ID
 	 * @throws Exception
 	 */
 	public static void sendConfirmationMail(String mailTo, String userId) throws Exception {
 	
 		try {
 		    String[] params = {userId, MailConfig.getAppSupportNumber(), MailConfig.getTechSupportStartTime(), MailConfig.getTechSupportEndTime(),};
 			
 	        MessageFormat formatter = new MessageFormat(MailConfig.getRegisterUnformattedBody());
 	        String formattedBody = formatter.format(params);		  
 				   
 	        new SendMail().sendMail(mailTo, null, formattedBody, formatFTPSubject());
 		} catch (Exception e) {
 			logger.error("Send confirmation mail error", e);
 		} catch (ValidationException e) {
 			logger.error("Send confirmation mail error", e);
 		}			
      }
 	
 
 	/**
 	 * The filename is sometimes sent in with a leading slash.  Clean it off.
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	private static String cleanFileName(String fileName)
 	{
 		String nameWithoutPath;
 		if(fileName.lastIndexOf(File.separator)>0){
 			// Strip off path to just get the filename
 			nameWithoutPath = fileName.substring(fileName.lastIndexOf(File.separator)); 		
 		}
 		else{
 			nameWithoutPath = fileName; 		
 		}
 		return (nameWithoutPath.startsWith(File.separator)) ? nameWithoutPath.substring(1) : nameWithoutPath;
 	}
 	public static String formatFromAddress(){
 		 String from = new MessageFormat(MailConfig.getFrom()).format(new String[] {MailConfig.getAcronym()});
 		 return from;
 	}
 	public static String formatFTPSubject(){
 		 String ftpSubject = new MessageFormat(MailConfig.getFtpSubject()).format(new String[] {MailConfig.getProject(),MailConfig.getAcronym()});
 		 return ftpSubject;
 	}
	public static String formatFTPErrorSubject(){
		 String ftpSubject = new MessageFormat(MailConfig.getFtpErrorSubject()).format(new String[] {MailConfig.getProject(),MailConfig.getAcronym()});
		 return ftpSubject;
	}
 
 }
