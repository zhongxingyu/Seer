 package petrinet;
 
 import java.util.Map;
 
 import exceptions.ShowAsWarningException;
 
 public final class Renews {
 
 	public static IRenew fromString(String string) {
 		if (string.equalsIgnoreCase("id")) {
 			return IDENTITY;
 		} else if (string.equalsIgnoreCase("count")) {
 			return COUNT;
 		} else if (string.equalsIgnoreCase("toggle")) {
 			return TOGGLE;
 		} else {
 			throw new ShowAsWarningException("\"" + string
 					+ "\" ist kein gültiger Renew-Bezeichner. Gültige Bezeichner sind: \"id\", \"count\", \"toggle\"");
 		}
 	}
 
 	private Renews() {
 	}
 
 	public static final IRenew IDENTITY = new RenewId();
 
 	public static final IRenew COUNT = new RenewCount();
 
 	public static final IRenew TOGGLE = new RenewToggle();
 
 	public static final IRenew fromMap(Map<String, String> f) {

 		return new RenewMap(f);
 	}
 }
