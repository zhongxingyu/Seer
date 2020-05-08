 
 package mx.ferreyra.dogapp;
 
 public class AppData {
 	public static String USER_ID= "";
 
 	public static enum USER_LOGIN_TYPE { NOT_LOGGED, APPLICATION, FACEBOOK}
 
 	public static USER_LOGIN_TYPE userLogin;
 	public static void assignType(USER_LOGIN_TYPE type) {
 		userLogin = type;
 	}
 	public static USER_LOGIN_TYPE getLoginType() {
 		return userLogin;
 	}
 
 	public static boolean IS_FIRST = true;
	public static final String URL = "http://marketing7veinte.net/dc_app_perroton/appWSDog/wsDog.asmx?WSDL";
 	//public static final String URL = "http://64.211.201.54/dev/APP_PERRO/appWSDog/wsDog.asmx";
 	//public static final String URL = "http://64.211.201.54/dev/APP_PERRO/appWSDog/wsDog.asmx?";
 	//public static final String URL = "http://dogapp.dci.in/dog_services.php";
 
 	//public static final String USER_URL = "http://64.211.201.54/dev/APP_PERRO/repo/users.aspx";
 	//public static final String LOGIN_URL = "http://64.211.201.54/dev/APP_PERRO/repo/login.aspx";	
 	//public static final String ROUTES_URL = "http://64.211.201.54/dev/APP_PERRO/repo/routes.aspx";
 
 	public static final String NAMESPACE = "http://tempuri.org/";
 	public static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
 	//public static final String SOAP_ACTION = "\"\"";
 	public static final String SOAP_ACTION = "http://tempuri.org/"; 
 
 	public static final String METHOD_NAME_USER_LOGIN = "userLogin";	
 
 	public static final String METHOD_NAME_USER_REGSTRATION = "insertUsers"; 
 
 	public static final String METHOD_NAME_GET_TRAININGSPOT = "getTrainingSpot"; 
 
 	public static final String METHOD_NAME_GET_USER_TRAININGSPOT = "getUserTrainingSpot"; 
 
 	public static final String METHOD_NAME_INSERT_RATINGS = "insertRating";	
 
 	public static final String METHOD_NAME_INSERT_ROUTE = "insertRoute";
 	
 	public static final String METHOD_NAME_USER_RECOVERY_PWD = "userRecoveryPWD";
 	
 	public static final String METHOD_NAME_USER_STATS = "getStats";	
 
 }
