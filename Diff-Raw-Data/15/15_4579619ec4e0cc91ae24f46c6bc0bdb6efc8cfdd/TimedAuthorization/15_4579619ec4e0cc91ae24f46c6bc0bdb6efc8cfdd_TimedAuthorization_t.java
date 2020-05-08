 package authorization.impl.timedauth;
 
 import java.util.Calendar;
 
 import componenttypes.api.ComponentTypes;
 
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
	private int maxHour = 22;
 
 	@Override
 	public ComponentTypes.Authorization getType() {
 		return ComponentTypes.Authorization.TIMED;
 	}
 
 	@Override
 	public boolean authorize(AuthorizationToken token) {
 		if (token.getType().equals(getType())) {
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
 
 	@Override
 	public void addAuthorizedValue(AuthorizationToken token) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void removeAuthorizedValue(AuthorizationToken token) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
