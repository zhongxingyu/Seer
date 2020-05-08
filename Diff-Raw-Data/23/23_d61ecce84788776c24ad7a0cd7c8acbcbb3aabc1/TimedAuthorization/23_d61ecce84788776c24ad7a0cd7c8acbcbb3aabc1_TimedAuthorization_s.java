 package authorization.impl.timedauth;
 
 import java.util.Calendar;
 
 import aQute.bnd.annotation.component.Component;
 import authorization.api.AuthorizationToken;
 import authorization.api.IAuthorization;
 
 
 /**
  * This class is the implementation of the enum type TIMED in {@link IAuthorization}.
  *
  */
 @Component
 public class TimedAuthorization implements IAuthorization {
 	
 	private int minHour = 8;
 	private int maxHour = 16;
 
 	@Override
 	public Type getType() {
 		return IAuthorization.Type.TIMED;
 	}
 
 	@Override
 	public boolean authorize(AuthorizationToken token) {
		if (token.getType().equals(getType().toString())) {
 			Calendar cal = Calendar.getInstance();
 			if (cal.get(Calendar.HOUR_OF_DAY) > minHour && cal.get(Calendar.HOUR_OF_DAY) < maxHour) {
 				return true;
 			}
 			System.out.println("Access denied.");
 		}
 		else {
 			System.out.println("Invalid AuthorizationToken.");
 		}
 		return false;
 	}
 
 }
