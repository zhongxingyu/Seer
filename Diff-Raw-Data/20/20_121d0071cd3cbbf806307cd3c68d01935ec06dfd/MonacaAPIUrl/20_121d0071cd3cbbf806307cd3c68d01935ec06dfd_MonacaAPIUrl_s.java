 package mobi.monaca.utils;
 
 public class MonacaAPIUrl {
     public static String API_DOMAIN = "api.monaca.mobi"; //TODO  change this in test
 
     public static final String API_HOST = "https://" + API_DOMAIN;
 
     public static String getPushRegistrationAPIUrl(String pushProjectId) {
    	return API_HOST + "/v1/push/register/:";
     }
 }
