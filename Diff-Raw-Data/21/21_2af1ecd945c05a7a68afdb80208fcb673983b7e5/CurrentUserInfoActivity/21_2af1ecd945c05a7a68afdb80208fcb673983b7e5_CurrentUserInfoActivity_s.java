 package com.playphone.sdk.example;
 
 import com.playphone.multinet.MNDirect;
 import com.playphone.multinet.MNDirectUIHelper;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class CurrentUserInfoActivity extends CustomTitleActivity{
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.post_userinfo);
 		
 		
 		// set the breadcrumbs text
      	TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
      	txtBreadCrumbs.setText("Home > Current User Info");
      	
		TextView txtResult = (TextView) findViewById(R.id.txtResult);
		StringBuffer str = new StringBuffer().
				append("Username: ").append(MNDirect.getSession().getMyUserName()).append("\n").
				append("User id: ").append(MNDirect.getSession().getMyUserId()).append("\n").
				append("Current room: ").append(MNDirect.getSession().getCurrentRoomId());
 		txtResult.setText(str.toString());
 		
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		MNDirectUIHelper.setHostActivity(null);
 	}
  
 	@Override
 	protected void onResume() {
 		super.onResume();
 		MNDirectUIHelper.setHostActivity(this);
 	}
 
 }
