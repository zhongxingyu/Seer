 package nl.sense_os.commonsense.client.common.constants;
 
 public class Urls {
 
     private static final String _STABLE_BASE = "http://common.sense-os.nl/api";
     private static final String _RC_BASE = "http://rc.sense-os.nl/api";
     private static final String _DEV_BASE = "http://api.dev.sense-os.nl";
     private static final String _INTERMED_BASE = Constants.RC_MODE ? _RC_BASE : _STABLE_BASE;
     private static final String _REG_BASE = Constants.DEV_MODE ? _DEV_BASE : _INTERMED_BASE;
     private static final String _TED_BASE = "http://217.77.159.221/restful/api";
 
     public static final String BASE_URL = Constants.TED_MODE ? _TED_BASE : _REG_BASE;
 
     public static final String ENVIRONMENTS = BASE_URL + "/environments";
     public static final String GROUPS = BASE_URL + "/groups";
     public static final String LOGIN = BASE_URL + "/login";
     public static final String LOGOUT = BASE_URL + "/logout";
     public static final String SENSORS = BASE_URL + "/sensors";
     public static final String STATES = BASE_URL + "/states";
     public static final String USERS = BASE_URL + "/users";
 
     private Urls() {
         // empty constructor to make sure this class is not instantiated
     }
 }
