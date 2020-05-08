 package uk.co.jarofgreen.cityoutdoors.API;
 
 
 import org.xml.sax.Attributes;
 
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.sax.Element;
 import android.sax.RootElement;
 import android.sax.StartElementListener;
 import android.util.Log;
 
 /**
  * 
  * @author James Baster  <james@jarofgreen.co.uk>
  * @copyright City of Edinburgh Council & James Baster
  * @license Open Source under the 3-clause BSD License
  * @url https://github.com/City-Outdoors/City-Outdoors-Android
  */
 public class LogInOrSignUpCall extends BaseCall {
 
 	public LogInOrSignUpCall(Context context) {
 		super(context);
 	}
 	
 	
 	Integer userID = null;
 	String token = null;
 	String name = null;
 	String email = null;
 	Integer score = null;
 	
     public boolean execute(String email, String password) {
     	this.email = email;
         RootElement root = new RootElement("data");
         Element user = root.getChild("user");
         user.setStartElementListener(new StartElementListener(){
 			public void start(Attributes attributes) {
 				if (attributes.getValue("id") != null) userID = Integer.parseInt(attributes.getValue("id"));
 				if (attributes.getValue("score") != null) score = Integer.parseInt(attributes.getValue("score"));
 				token = attributes.getValue("token");
 				name = attributes.getValue("name");
 				Log.d("LOGINCALL","userID="+Integer.toString(userID));
 			}
         });
 
         setUpCall("/api/v1/logInOrSignUp.php?showLinks=0&");
         addDataToCall("email", email);
         addDataToCall("password", password);
         makeCall(root);
         
         if (userID != null) {
 	        return true;
         } else {
         	return false;
         }
     }
     
     public boolean saveResults() {
         if (userID != null) {
 	        SharedPreferences settings=PreferenceManager.getDefaultSharedPreferences(context);
 	        SharedPreferences.Editor editor = settings.edit();
 	        editor.putInt("userID", userID);
	        editor.putInt("userScore", score);
	        editor.putString("userToken", token);
	        editor.putString("userDisplayName", name);
	        editor.putString("userEmail", email);
	        editor.putString("newFeatureReportName", name);
	        editor.putString("newFeatureReportEmail", email);	        
 	        editor.commit();
 	        return true;
         } else {
         	return false;
         }    	
     }
 }
