 package model.validator.loginvalidator;
 
 import model.configuration.Config;
 import model.configuration.KeyValueConfiguration;
 import model.validator.LoginValidationException;
 import model.validator.LoginValidator;
 
 import org.apache.log4j.Logger;
 import org.joda.time.Interval;
 import org.joda.time.LocalTime;
 
 public class TimeValidator implements LoginValidator {
 
 	private static Logger logger = Logger.getLogger(TimeValidator.class);
 	private static KeyValueConfiguration accessTimeConfig = Config.getInstance().getKeyValueConfig("access_time");
 
 	private String mailAddress;
 	
 	public TimeValidator(String mailAddress) {
 		this.mailAddress = mailAddress;
 	}
 	
 	public void validate() throws Exception {
 		String timeRestriction = accessTimeConfig.get(mailAddress);
 		if (timeRestriction == null) {
 			return;
 		}
 		try {
 			timeRestriction = timeRestriction.trim();
 			final String[] times = timeRestriction.split("-");
 			final String[] startTimeParts = times[0].split(":");
 			final String[] endTimeParts = times[1].split(":");
 			final LocalTime startTime = new LocalTime(Integer.valueOf(startTimeParts[0]), Integer.valueOf(startTimeParts[1]));
 			final LocalTime endTime = new LocalTime(Integer.valueOf(endTimeParts[0]), Integer.valueOf(endTimeParts[1]));
 			final Interval timeRestrictionInterval = new Interval(startTime.toDateTimeToday(), endTime.toDateTimeToday());
 			if (timeRestrictionInterval.containsNow()) {
 				throw new LoginValidationException();
 			}
 		} catch (final Exception e) {
			if(e.getClass().equals(LoginValidationException.class)){
 				throw e;
 			}
 			logger.error("Could not parse schedule for " + mailAddress + " - read: " + timeRestriction);
 		}
 	}
 }
