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
 public class CheckCurrentUserCall extends BaseCall {
 
 	public CheckCurrentUserCall(Context context) {
 		super(context);
 	}
 	
 	
 	Integer userID = null;
 	String name = null;
 	String email = null;
 	Integer score = null;
 	
     public boolean execute() {
         RootElement root = new RootElement("data");
         Element user = root.getChild("user");
         user.setStartElementListener(new StartElementListener(){
 			public void start(Attributes attributes) {
 				if (attributes.getValue("id") != null) userID = Integer.parseInt(attributes.getValue("id"));
 				if (attributes.getValue("score") != null) score = Integer.parseInt(attributes.getValue("score"));
 				name = attributes.getValue("name");
 				email = attributes.getValue("email");
 				Log.d("CHECKCALL","userID="+Integer.toString(userID));
 			}
         });
 
         setUpCall("/api/v1/currentUser.php?showLinks=0&");
         if (!isUserTokenAttached) {
         	return false;
         }
         makeCall(root);
         
         if (userID != null) {
 	        SharedPreferences settings=PreferenceManager.getDefaultSharedPreferences(context);
 	        SharedPreferences.Editor editor = settings.edit();
	        if (score != null) editor.putInt("userScore", score);
	        if (name != null) editor.putString("userDisplayName", name);
	        if (email != null) editor.putString("userEmail", email);     
 	        editor.commit();
 	        return true;
         } else {
         	return false;
         }    	
     }
 }
