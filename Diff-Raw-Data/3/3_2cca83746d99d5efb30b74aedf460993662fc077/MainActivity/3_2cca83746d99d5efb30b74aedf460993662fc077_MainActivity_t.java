 package com.pixeldoctrine.hussh;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 
 import com.pixeldoctrine.hussh.shell.SshConnectParams;
 
 public class MainActivity extends Activity implements OnClickListener {
 
 	public final static String HOSTNAME = "com.pixeldoctrine.hussh.HOSTNAME";
 	public final static String PORT		= "com.pixeldoctrine.hussh.PORT";
 	public final static String USERNAME	= "com.pixeldoctrine.hussh.USERNAME";
 	private static String hostname;
 	private static String port;
 	private static String username;
 	private static List<SshConnectParams> recents;
 
     @SuppressWarnings("unchecked")
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         if (savedInstanceState != null) {
 			hostname = savedInstanceState.getString("host");
 			port = savedInstanceState.getString("port");
 			username = savedInstanceState.getString("user");
 			recents = (List<SshConnectParams>) savedInstanceState.getSerializable("recent");
         }
         if (recents == null) {
         	recents = new ArrayList<SshConnectParams>();
         }
         updateInputFromShadow();
         updateRecentsView();
     }
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		shadowInput();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		savedInstanceState.putString("host", hostname);
 		savedInstanceState.putString("port", port);
 		savedInstanceState.putString("user", username);
 		super.onSaveInstanceState(savedInstanceState);
 	}
 
     public void onConnect(View view) {
     	shadowInput();
 
     	int portInt = 22;
     	try {
     		portInt = Integer.valueOf(port);
     	} catch (Exception e) {
     	}
     	updateRecents(hostname, portInt, username);
 
     	Intent intent = new Intent(this, ShellActivity.class);
     	intent.putExtra(HOSTNAME, hostname);
    		intent.putExtra(PORT, portInt);
     	intent.putExtra(USERNAME, username);
     	startActivity(intent);
     }
 
     /**
      * Click on "recent" button.
      */
 	@Override
 	public void onClick(View view) {
 		hostname = "";
 		port = "";
 		username = "";
 		Button recentButton = (Button) view;
 		String recent = recentButton.getText().toString();
 		if (recent.indexOf("@") > 0) {
 			hostname = recent.split("@")[1];
 			username = recent.split("@")[0];
 		} else {
 			hostname = recent;
 		}
 		if (hostname.indexOf(":") > 0) {
 			port = hostname.split(":")[1];
 			hostname = hostname.split(":")[0];
 		}
 		updateInputFromShadow();
 		onConnect(view);
 	}
 
 	private void updateRecents(String hostname, int port, String username) {
 		SshConnectParams param = new SshConnectParams(hostname, port, username);
 		int cnt = recents .size();
 		for (int i = 0; i < cnt; ++i) {
 			if (recents.get(i).equals(param)) {
 				if (i == 0) {	// No need to update if we're using the same as last time.
 					return;
 				}
 				recents.remove(i);
 				break;
 			}
 		}
 		recents.add(0, param);
 		updateRecentsView();
 	}
 
 	private void updateRecentsView() {
 		LinearLayout recentList = (LinearLayout) findViewById(R.id.recentList);
 		recentList.removeAllViews();
 		for (SshConnectParams param : recents) {
 			Button recentButton = new Button(this);
 			recentButton.setText(param.toString());
 			recentButton.setBackgroundColor(Color.WHITE);
			recentButton.setTextColor(Color.rgb(46,6,17));
			recentButton.setTextSize(14.0f);
 			recentButton.setOnClickListener(this);
 			recentList.addView(recentButton);
 		}
 	}
 
 	private void shadowInput() {
     	EditText hostnameText = (EditText) findViewById(R.id.hostname);
     	EditText portText = (EditText) findViewById(R.id.port);
     	EditText usernameText = (EditText) findViewById(R.id.username);
     	hostname = hostnameText.getText().toString().trim();
     	port = portText.getText().toString().trim();
     	username = usernameText.getText().toString().trim();
 	}
 
 	private void updateInputFromShadow() {
     	EditText hostnameText = (EditText) findViewById(R.id.hostname);
     	EditText portText = (EditText) findViewById(R.id.port);
     	EditText usernameText = (EditText) findViewById(R.id.username);
 		hostnameText.setText(hostname);
 		portText.setText(port);
 		usernameText.setText(username);
 	}
 }
