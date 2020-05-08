 package model.validator.emailvalidator;
 
 import model.User;
 import model.configuration.Config;
 import model.configuration.KeyValueConfiguration;
 import model.mail.Mail;
 import model.validator.MailValidator;
 import model.validator.MailValidationException;
 
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 public class DelMaxDateValidator implements MailValidator {
 
 	private static KeyValueConfiguration deleteDateConfig = Config.getInstance().getKeyValueConfig("notdelete_max_age");
 
 	@Override
 	public void validate(User user, Mail email) throws MailValidationException {
 		String maxDateString = deleteDateConfig.get(user.getMail());
 		if (maxDateString == null) {
 			// No restrictions for this user
 			return;
 		}
 		DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy");
 		LocalDate maxDate = dtf.parseLocalDate(maxDateString.trim());
 		if (!email.getDate().isAfter(maxDate)) {
 			String message = "Restricting message deletion because mail date: "
 				+ email.getDate()
				+ " is lower to configuration file declared" + " date "
				+ maxDate + " for this user (" + user.getMail() + ")";
 			throw new MailValidationException(message);
 		}
 	}
 
 }
