 package my.b1701.SB.Activities;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import my.b1701.SB.R;
 import my.b1701.SB.HelperClasses.ThisUserConfig;
 import my.b1701.SB.HttpClient.AddUserRequest;
 import my.b1701.SB.HttpClient.SBHttpClient;
 import my.b1701.SB.HttpClient.SBHttpRequest;
 import my.b1701.SB.Util.StringUtils;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class Tutorial extends Activity{
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.username_dialog);
         
         final EditText userNameView = (EditText) findViewById(R.id.listviewusername);
         userNameView.requestFocus();
         Intent i = getIntent();
         Bundle b = i.getExtras();
         final String uuid = b.getString("uuid");
         Button enterButton = (Button) findViewById(R.id.enterusernamebutton);
 		// if button is clicked, close the custom dialog
 		enterButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				
                 String userNameText = userNameView.getText().toString();
                 if (StringUtils.isBlank(userNameText)) {
                     return;
                 }
 
                ThisUserConfig.getInstance().putString(ThisUserConfig.USERNAME, userNameText);
                SBHttpRequest request = new AddUserRequest(uuid);		
        			SBHttpClient.getInstance().executeRequest(request);
        			finish();
 				
 			}
 		});
 	}
 
     @Override
     public void onStart(){
         super.onStart();
         EasyTracker.getInstance().activityStart(this);
     }
 
     @Override
     public void onStop(){
         super.onStop();
         EasyTracker.getInstance().activityStop(this);
     }
 
 }
