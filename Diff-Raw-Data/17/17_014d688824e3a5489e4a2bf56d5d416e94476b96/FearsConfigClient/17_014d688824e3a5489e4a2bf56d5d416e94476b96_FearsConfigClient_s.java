 package eu.ist.fears.common;
 
import com.google.gwt.user.client.Window;
 
 public class FearsConfigClient {
 
     public static String getCasUrl() {
 	return "redirectLogin";
     }
 
    public static String getFearsUrl() {
	return Window.Location.getProtocol() + "//" + Window.Location.getHost() + "/" ;
    }
 }
