 package notifiers;
 
 import models.User;
 import play.exceptions.MailException;
 import play.mvc.Mailer;
  
 public class Mails extends Mailer {
 
 	/**
 	 * Sends a registration e-mail to the user. That e-mail will contain a
 	 * confirmation key the user will have to activate within the next hour so
 	 * that the requested user account is enabled (otherwise it's deleted by
 	 * {@link CleanUpJobs})
 	 * 
 	 * @param user
 	 *            the user to send the e-mail to
 	 * @return true, if sending the e-mail was successful (false indicates that
 	 *         e.g. the mail server wasn't properly configured)
 	 */
 	public static boolean welcome(User user) throws MailException {
 		setSubject("Welcome %s", user.getName());
 		addRecipient(user.getEmail());
		setFrom("ajopi <ese3-noreply@iam.unibe.ch>");
 		String key = user.getConfirmKey();
 		return sendAndWait(user, key);
 	}
 }
