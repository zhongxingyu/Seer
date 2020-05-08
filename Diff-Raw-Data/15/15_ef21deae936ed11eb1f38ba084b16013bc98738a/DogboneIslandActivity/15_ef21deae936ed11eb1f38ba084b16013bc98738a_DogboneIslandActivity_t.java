 // DogboneIslandActivity.java --- 
 // 
 // Filename: DogboneIslandActivity.java
 // Description: 
 // Author: Sevki Hasirci
 // Maintainer: Sevki Hasirci
 // Created: Wed Apr  4 13:50:40 2012 (+0100)
 // Version: 
 // Last-Updated: 
 //           By: 
 //     Update #: 0
 // URL: http://sevki.com
 // Keywords: 
 // Compatibility: 
 // 
 // 
 
 // Commentary: 
 // 
 // 
 // 
 // 
 
 // Change Log:
 // 
 // 
 // 
 // 
 package team18.cs.ncl.ac.uk;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import com.facebook.android.*;
 import com.facebook.android.Facebook.*;
 
 import android.util.Log;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import team18.cs.ncl.ac.uk.SessionEvents.AuthListener;
 
 
 
 public class DogboneIslandActivity extends Activity {
     /** Called when the activity is first created. */
     
     
     public  AsyncFacebookRunner mAsyncRunner=new AsyncFacebookRunner(FbRelatedStuff.facebook);
     String FILENAME = "AndroidSSO_data";
     private static TextView firstText;
     private static ImageView userImage;
     private SharedPreferences mPrefs;
     @Override
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 	
     	this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
     	//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
     	
         mPrefs = getSharedPreferences(FILENAME,MODE_PRIVATE);
         String access_token = mPrefs.getString("access_token", null);
         long expires = mPrefs.getLong("access_expires", 0);
   
         if(access_token != null) {
             FbRelatedStuff.facebook.setAccessToken(access_token);
             
         }
         if(expires != 0) {
         	FbRelatedStuff.facebook.setAccessExpires(expires);
         }
         firstText =  (TextView) DogboneIslandActivity.this.findViewById(R.id.textView1);
         userImage= (ImageView) DogboneIslandActivity.this.findViewById(R.id.UserImage);
	firstText.setText("Arrr! Welcome to t' Dogbone Island!!!");
         //buttons and shit
         final Button hangyManButton = (Button) findViewById(R.id.hangManbutton);
         final Button flButton = (Button) findViewById(R.id.flashCardButton);
         final Button anagramButton = (Button) findViewById(R.id.anagramButton);
         final Button storyModeButton = (Button) findViewById(R.id.storyModebutton);
          
         System.out.print("create dictionary and download");
         DictionaryTools t = new DictionaryTools();
         t.DownloadDictionaryToLocal();
         System.out.print("create story and download");
         StoryTools st = new StoryTools();
         st.DownloadStoryToLocal();
         
     	
         hangyManButton.setOnClickListener(new View.OnClickListener() {
      		@Override
 		    public void onClick(View v) {
 		    // TODO Auto-generated method stub
 		    firstText.setText("Yer booty awaits!");
 		    Intent intent =
 			new Intent(team18.cs.ncl.ac.uk.DogboneIslandActivity.this, team18.cs.ncl.ac.uk.HangManActivity.class);
 		    startActivity(intent);
 		    
 		}
 	    });
         //Jamie this is your bitch
         flButton.setOnClickListener(new View.OnClickListener() {
      		@Override
 		    public void onClick(View v) {
 		    // TODO Auto-generated method stub
 		    firstText.setText("Yer booty awaits!");
 		    Intent intent =
 			new Intent(team18.cs.ncl.ac.uk.DogboneIslandActivity.this, team18.cs.ncl.ac.uk.FlashCards.class);
 		    startActivity(intent);
 		}
 	    });
         
 	//Andy this is your bitch
         anagramButton.setOnClickListener(new View.OnClickListener() {
      		@Override
 		    public void onClick(View v) {
 		    // TODO Auto-generated method stub
 		    firstText.setText("Yer booty awaits!");
 		    Intent intent =
 			new Intent(team18.cs.ncl.ac.uk.DogboneIslandActivity.this, team18.cs.ncl.ac.uk.Anagram.class);
 		    startActivity(intent);
 		}
 	    });   
         storyModeButton.setOnClickListener(new View.OnClickListener() {
      		@Override
 		    public void onClick(View v) {
 		    // TODO Auto-generated method stub
 		    firstText.setText("Avast, me hearties!");
 		    StoryTools.ReadStory();
 		    Intent intent =
 			new Intent(team18.cs.ncl.ac.uk.DogboneIslandActivity.this, team18.cs.ncl.ac.uk.StoryActivity.class);
 		    startActivity(intent);
 		    
 		}
 	    });   
     
 	//Facebook Stuff
         FbRelatedStuff.facebook.authorize(this, new String[] { 
 		"user_photos",
 		"user_about_me",
 		"publish_actions",
 		"offline_access",
 		"user_games_activity",
 		"friends_games_activity",
 		"user_actions:newcaslteproject",
 		"friends_actions:newcaslteproject" },
 	    new DialogListener() {
 		@Override
 		    public void onComplete(Bundle values) {
 		    SessionEvents.addAuthListener(new dogBoneAuthListener());
 		    DogBoneServer.sendNewUserJson(FbRelatedStuff.facebook.getAccessToken());
 		    SharedPreferences.Editor editor = mPrefs.edit();
 		    editor.putString("access_token", FbRelatedStuff.facebook.getAccessToken());
 		    editor.putLong("access_expires", FbRelatedStuff.facebook.getAccessExpires());
             
 		    editor.commit();
 		    mAsyncRunner.request("me", new meRequestListener());
 		    Bundle params = new Bundle();
 		    params.putString("method", "fql.query");
 		    params.putString("query", "SELECT pic FROM user WHERE uid = me()");
 		    mAsyncRunner.request(null, params, new FQLRequestListener());
 		}
 		
 		@Override
 		    public void onFacebookError(FacebookError error) {}
 		
 		@Override
 		    public void onError(DialogError e) {}
 		
 		@Override
 		    public void onCancel() {}
 	    });
 	
         
     }
     @Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         FbRelatedStuff.facebook.authorizeCallback(requestCode, resultCode, data);
 	
     }
    
     void getImage(final String S)
     {
     	DogboneIslandActivity.this.runOnUiThread(new Runnable() {
 		public void run() {
 		    new GetProfilePicAsyncTask().execute(S);
 		}
 	    });
 
 	
     }
     public class FQLRequestListener extends BaseRequestListener {
 	
         @Override
 	    public void onComplete(final String response, final Object state) {
 	    
             /*
              * Output can be a JSONArray or a JSONObject.
              * Try JSONArray and if there's a JSONException, parse to JSONObject
              */
       
             try {
                 JSONArray json = new JSONArray(response);
                 getImage(json.getJSONObject(0).get("pic").toString());
             } catch (JSONException e) {
                 try {
                     /*
                      * JSONObject probably indicates there was some error
                      * Display that error, but for end user you should parse the
                      * error and show appropriate message
                      */
                     JSONObject json = new JSONObject(response);
                     getImage(json.getString("pic"));
                 } catch (JSONException e1) {
                     Log.d("JSON ERROR:", e1.getMessage());
                 }
             }
         }
 	
         public void onFacebookError(FacebookError error) {
             Log.d("FACEBOOK ERROR:", error.getMessage());
         }
     }
     public class meRequestListener extends BaseRequestListener {
         public void onComplete(final String response, final Object state) {
 	    
             try {
                 // process the response here: executed in background thread
                 Log.d("Facebook-Example", "Response: " + response.toString());
                 JSONObject json = Util.parseJson(response);
                 
                 //get users info and save them to fbrelatedstuff.
                 final String firstName = json.getString("first_name");
                 final String lastName = json.getString("last_name");
                 final String name = json.getString("name");
                 final String gender = json.getString("gender");
                 
                 
                 FbRelatedStuff.FirstName= firstName;
                 FbRelatedStuff.LastName = lastName;
                 FbRelatedStuff.Name = name;	
                 
                 FbRelatedStuff.Gender = team18.cs.ncl.ac.uk.FbRelatedStuff.gender.female;
                 if(gender =="male")
                 	FbRelatedStuff.Gender = team18.cs.ncl.ac.uk.FbRelatedStuff.gender.male;
                 
                 
                 final long uid = json.getLong("id");
                 // then post the processed result back to the UI thread
                 // if we do not do this, an runtime exception will be generated
                 // e.g. "CalledFromWrongThreadException: Only the original
                 // thread that created a view hierarchy can touch its views."
 		FbRelatedStuff.uid=uid;
 		DogboneIslandActivity.this.runOnUiThread(new Runnable() {
 			public void run() {
 			    firstText.setText("Ahoy there " + firstName + "!");
 			}
 		    });
             } catch (JSONException e) {
                 Log.w("Facebook-Example", "JSON Error in response");
                 DogboneIslandActivity.this.runOnUiThread(new Runnable() {
 			public void run() {
 			    firstText.setText("FacebookError");
 			}
 		    });
                 
             } catch (FacebookError e) {
                 Log.w("Facebook-Example", "Facebook Error: " + e.getMessage());
                 DogboneIslandActivity.this.runOnUiThread(new Runnable() {
 			public void run() {
 			    firstText.setText("JSON");
 			    
 			}
 		    });
             }
         }
     }
     
     private class GetProfilePicAsyncTask extends AsyncTask<Object, Void, Bitmap>  
     {
 	String picturePath;
 	
         @Override
 	    protected Bitmap doInBackground(Object... params) {
 	    picturePath =  params[0].toString();
 	    return  Utility.getBitmap(picturePath);
 	    
             
         }
 	
         @Override
 	    protected void onPostExecute(final Bitmap result) {
             
 	    DogboneIslandActivity.this.runOnUiThread(new Runnable() {
         	    public void run() {
 			userImage.setImageBitmap(result);
 			
         	    }
 		});
         }
     }
 
     public class dogBoneAuthListener implements AuthListener {
 	
         public void onAuthSucceed() {
 	    firstText.setText("You have logged in! ");
 	    
         }
 	
         public void onAuthFail(String error) {
 	    
             firstText.setText("Login Failed: " + error);
         }
     }
     
}
