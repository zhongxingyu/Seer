 package com.playphone.sdk.example;
 
 import com.playphone.multinet.MNDirect;
 import com.playphone.multinet.MNDirectUIHelper;
 import com.playphone.multinet.MNErrorInfo;
 import com.playphone.multinet.MNGameParams;
 import com.playphone.multinet.MNUserInfo;
 import com.playphone.multinet.core.IMNSessionEventHandler;
 import com.playphone.multinet.core.MNAppHostCallInfo;
 import com.playphone.multinet.core.MNChatMessage;
 import com.playphone.multinet.core.MNCurrGameResults;
 import com.playphone.multinet.core.MNGameResult;
 import com.playphone.multinet.core.MNJoinRoomInvitationParams;
 
 import android.content.Intent;
 import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
public class LoginUserActivity extends CustomTitleActivity implements Callback {
 	
 	private Button btnLogIn;
 	private Button btnLogOut;
 	private TextView txtPlayerStatus;
 	private TextView txtPlayerName;
	Handler handler = new Handler(this);
 	
 	
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.login_user);
 	
 		// set the breadcrumbs text
 		TextView txtBreadCrumbs = (TextView) findViewById(R.id.txtBreadCrumbs);
 		txtBreadCrumbs.setText("Home > Login User");
 		
 		
 		txtPlayerName = (TextView) findViewById(R.id.txtPlayerName);
 		txtPlayerStatus = (TextView) findViewById(R.id.txtPlayerStatus);
 		
 		
 		btnLogIn = (Button) findViewById(R.id.btnLogIn);
 		btnLogIn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				MNDirect.execAppCommand("jumpToUserHome",null);
 				MNDirectUIHelper.showDashboard();
 			}
 		});
 		
 		btnLogOut = (Button) findViewById(R.id.btnLogOut);
 		btnLogOut.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				MNDirect.execAppCommand("jumpToUserProfile",null);
 				MNDirectUIHelper.showDashboard();
 			}
 		});
 		
 		
 		// set user status and hide the correct button
 		updatePlayerStatus(-1);
 		
 		//MNDirect.getSession().addEventHandler(this);
		// add the callback to the mainactivity
		MainActivity.getMNEventHandler().setHandler(handler);
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 		Log.d("playphone","Login status: " + String.valueOf(intent.getExtras().getInt("status")));
 		Log.d("playphone", "Logged in: " + String.valueOf(MNDirect.getSession().isUserLoggedIn()));
 		if(intent.getExtras().containsKey("loginStatus"))
 			updatePlayerStatus(intent.getExtras().getInt("loginStatus"));
 	}
 	
 	private void updatePlayerStatus(int status)
 	{
 		boolean loggedIn;
 		if(status == -1) loggedIn = MNDirect.getSession().isUserLoggedIn();
 		else if(status >= 50) loggedIn = true;
 		else loggedIn = false;
 			
 		
 		// set user name
 		txtPlayerName.setText(String.valueOf(MNDirect.getSession().getMyUserId()) + "   " +  MNDirect.getSession().getMyUserName());
 		MNDirectUIHelper.hideDashboard();
 		// set user status and hide the correct button
 		if(loggedIn)
 		{
 			txtPlayerStatus.setText("User is logged in");
 			btnLogOut.setEnabled(true);
 			btnLogIn.setEnabled(false);
 			txtPlayerName.setText(String.valueOf(MNDirect.getSession().getMyUserId()) + "   " +  MNDirect.getSession().getMyUserName());
 		}
 		else
 		{
 			txtPlayerStatus.setText("User is not logged in");
 			btnLogOut.setEnabled(false);
 			btnLogIn.setEnabled(true);
 			txtPlayerName.setText("0    null");
 		}
 	}

	@Override
	public boolean handleMessage(Message msg) {
		if(msg.getData().containsKey("statusChange"))
			updatePlayerStatus(msg.getData().getInt("statusChange"));
		return false;
	}
 	
 	
 }
