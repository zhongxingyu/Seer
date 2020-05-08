 package model.validator.loginvalidator;
 
 import model.configuration.Config;
 import model.configuration.KeyValueConfiguration;
 import model.validator.LoginValidationException;
 import model.validator.LoginValidator;
 
 import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Interval;
 import org.joda.time.LocalTime;
 
 public class TimeValidator implements LoginValidator {
 
 	private static Logger logger = Logger.getLogger(TimeValidator.class);
 	private static KeyValueConfiguration accessTimeConfig = Config.getInstance().getKeyValueConfig("access_time");
 
 	private String mailAddress;
 	
 	public TimeValidator(String mailAddress) {
 		this.mailAddress = mailAddress;
 	}
 	
 	@Override
 	public void validate() throws LoginValidationException {
 		logger.info("Checking time access for user: " + mailAddress);
 		String timeRestriction = accessTimeConfig.get(mailAddress);
 		if (timeRestriction == null) {
 			return;
 		}
 		try {
 			timeRestriction = timeRestriction.trim();
 			final String[] times = timeRestriction.split("-");
 			final String[] startTimeParts = times[0].split(":");
 			final String[] endTimeParts = times[1].split(":");
 			final LocalTime startTime = new LocalTime(Integer.valueOf(startTimeParts[0].trim()), Integer.valueOf(startTimeParts[1].trim()));
 			LocalTime endTime = new LocalTime(Integer.valueOf(endTimeParts[0].trim()), Integer.valueOf(endTimeParts[1].trim()));
 			LocalTime now = LocalTime.now();
 			if(startTime.isAfter(endTime)){
 				if (now.isBefore(startTime) && now.isAfter(endTime)) {
 					throw new LoginValidationException(mailAddress + " does not have access during this time");
 				}
 			}else{
 				if (now.isBefore(startTime) || now.isAfter(endTime)) {
 					throw new LoginValidationException(mailAddress + " does not have access during this time");
 				}
 			}
 		} catch (final Exception e) {
 			if (e instanceof LoginValidationException) {
 				throw (LoginValidationException) e;
 			}
 			logger.error("Could not parse schedule for " + mailAddress + " - read: " + timeRestriction);
 		}
 	}
 }
