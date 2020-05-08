 package com.facebook.android.theSocial;
 
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.FacebookError;
import com.facebook.android.R;
 import com.facebook.android.Facebook.DialogListener;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 
 public class MainActivity extends Activity {
 	
 	private SharedPreferences pref;
 	private Button login;
 	Facebook facebook;
 	private AlertDialog unsavedChangesDialog;
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		facebook = getSocialApplication().getFacebook();
 		login = (Button)findViewById(R.id.login);
 		setContentView(R.layout.activity_main);
 		pref = getPreferences(MODE_PRIVATE);
         String access_token = pref.getString("access_token",null);
         long expires = pref.getLong("access_expires", 0);
 		if(access_token != null)
         	facebook.setAccessToken(access_token);
         if(expires != 0)
         	facebook.setAccessExpires(expires);
         if(facebook.isSessionValid()){
         	getSocialApplication().setFacebook(facebook);
         	Intent intent = new Intent(getContext(),mainMenu.class);
         	startActivity(intent);
         }
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void login(View view){
 		facebook.authorize(this,new String[]{"email","user_status","user_photos",
 				"user_about_me","user_birthday","user_location","friends_about_me",
 				"friends_location","friends_status","friends_birthday"
 		}, new DialogListener() {
             @Override
             public void onComplete(Bundle values) {
             	SharedPreferences.Editor editor = pref.edit();
             	editor.putString("access_token", facebook.getAccessToken());
             	editor.putLong("access_expires", facebook.getAccessExpires());
             	editor.commit();
             	getSocialApplication().setFacebook(facebook);
             	Intent intent = new Intent(getContext(),mainMenu.class);
             	startActivity(intent);
             }
 			@Override
             public void onFacebookError(FacebookError error) {
 				getSocialApplication().onTerminate();
 			}
 				
             @Override
             public void onError(DialogError e) {
             	unsavedChangesDialog = new AlertDialog.Builder(getContext())
     			.setTitle(R.string.error)
     			.setMessage(R.string.dialogMessage)
     			.setPositiveButton(R.string.tryAgain,new AlertDialog.OnClickListener() {
     				
     				public void onClick(DialogInterface dialog, int which) {
     					
     				}
     			}).setNegativeButton(android.R.string.cancel, new AlertDialog.OnClickListener() {
     				
     				public void onClick(DialogInterface dialog, int which) {
     					// TODO Auto-generated method stub
     					getSocialApplication().onTerminate();
     				}
     			}).create();
     			unsavedChangesDialog.show();
             }
             	
             @Override
             public void onCancel() {
             	getSocialApplication().onTerminate();
             }
         });
 	}
 	
 	private theSocialApplication getSocialApplication(){
 		theSocialApplication app = (theSocialApplication)getApplication();
 		return app;
 	}
 	private Context getContext(){
 		return this;
 	}
 		
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onResume() {
 		super.onResume();
 		facebook.extendAccessTokenIfNeeded(this, null);
 	}
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 }
