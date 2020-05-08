 package edu.rit.cs.distrivia;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 import edu.rit.cs.distrivia.api.DistriviaAPI;
 import edu.rit.cs.distrivia.model.GameData;
 
 /**
  * Main activity to provide login functionality to the Application.
  */
 public class LoginActivity extends Activity {
 
     private EditText uname;
     private EditText pass;
     private Button loginBut;
     private Button registerBut;
     private ProgressDialog dialog;
 
     /** Enumeration to define which action a button is performing. */
     enum ButtonAction {
         REGISTER_EVENT, LOGIN_EVENT
     }
 
     /**
      * Handler to move onto the next Activity on successful login.
      */
     private final Handler loginHandler = new Handler() {
         @Override
         public void handleMessage(final Message msg) {
             GameData gd = (GameData) msg.getData().getSerializable("game_data");
             Intent joinIntent = new Intent(getBaseContext(), JoinActivity.class);
             // Make sure to pass session/game data to the next view
             joinIntent.putExtra("game_data", gd);
             startActivity(joinIntent);
         }
     };
 
     /**
      * Handler to make toast notifications from the Networking thread.
      */
     private final Handler toastHandler = new Handler() {
         @Override
         public void handleMessage(final Message msg) {
             // Cancel the loading dialog since something obviously happend.
             dialog.dismiss();
             String txtmsg = msg.getData().getString("msg");
             Toast.makeText(getApplicationContext(), txtmsg, 10).show();
         }
     };
     
     /**
      * Handler to toggle buttons from the Networking thread.
      */
     private final Handler buttonHandler = new Handler() {
     	@Override
     	public void handleMessage(final Message msg) {
     		toggleButtons();
     	}
     };
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.main);
 
         loginBut = (Button) findViewById(R.id.login_button);
         registerBut = (Button) findViewById(R.id.register_button);
         uname = (EditText) findViewById(R.id.login_input_username);
         pass = (EditText) findViewById(R.id.login_input_password);
 
         SharedPreferences settings = getPreferences(MODE_PRIVATE);
         String userName = settings.getString("uname", "");
         uname.setText(userName);
         if (!userName.equals("")) {
         	pass.requestFocus();
         }
 
         // Listener for login button
         loginBut.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(final View v) {
                 toggleButtons();
                 launchEvent(ButtonAction.LOGIN_EVENT);
             }
         });
 
         // Listener for register button
         registerBut.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(final View v) {
                 toggleButtons();
                 launchEvent(ButtonAction.REGISTER_EVENT);
             }
         });
 
         // Listener for enter key on password field
         pass.setOnKeyListener(new OnKeyListener() {
             @Override
             public boolean onKey(View v, int keyCode, KeyEvent event) {
                 if ((event.getAction() == KeyEvent.ACTION_DOWN)
                         && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                     toggleButtons();
                     launchEvent(ButtonAction.LOGIN_EVENT);
                     return true;
                 }
                 return false;
             }
         });
     }
 
     private void toggleButtons() {
         if (loginBut != null) {
             loginBut.setEnabled(!loginBut.isEnabled());
             registerBut.setEnabled(!registerBut.isEnabled());
             loginBut.invalidate();
             registerBut.invalidate();
         }
     }
 
     private void launchEvent(final ButtonAction action) {
         dialog = ProgressDialog.show(LoginActivity.this, "", "Logging In...",
                 true);
         new Thread() {
             @Override
             public void run() {
                 final String name = uname.getText().toString().trim();
                 final String passwd = pass.getText().toString().trim();
                 if (!name.equals("") && !passwd.equals("")) {
                     if (action == ButtonAction.LOGIN_EVENT) {
                         login(name, passwd);
                     } else {
                         register(name, passwd);
                     }
                 } else {
                     makeToast("Missing username or password");
                 }
             }
         }.start();
     }
 
     private void login(String name, String passwd) {
         String authToken = null;
         try {
             authToken = DistriviaAPI.login(name, passwd);
         } catch (Exception e) {
             Log.d("login error:", e.getMessage());
             makeToast("Service is down, please try again later");
             return;
         }
 
         boolean loginSuccessful = authToken != null;
         loginSuccessful &= (!authToken.equals(DistriviaAPI.API_ERROR));
 
         if (loginSuccessful) {
             SharedPreferences settings = getPreferences(MODE_PRIVATE);
             SharedPreferences.Editor editor = settings.edit();
             editor.putString("uname", name);
             editor.commit();
 
             makeToast("Welcome " + name);
             GameData gd = new GameData(authToken, name);
             relayGameData(gd);
         } else {
             makeToast("Login failure");
         }
     }
 
     private void register(String name, String passwd) {
         String authToken = null;
         try {
             authToken = DistriviaAPI.register(name, passwd);
         } catch (Exception e) {
             makeToast("Service is down, please try again later");
             return;
         }
 
         boolean registerSuccessful = authToken != null;
         registerSuccessful &= (!authToken.equals(DistriviaAPI.API_ERROR));
 
         if (registerSuccessful) {
        	login(name, passwd);
         } else {
             makeToast("Username already in use");
         }
     }
 
     private void relayGameData(GameData gd) {
         Bundle data = new Bundle();
         data.putSerializable("game_data", gd);
         Message msg = new Message();
         msg.setData(data);
         loginHandler.sendMessage(msg);
     }
 
     private void makeToast(String txtMsg) {
         Bundle data = new Bundle();
         data.putString("msg", txtMsg);
         Message msg = new Message();
         msg.setData(data);
         toastHandler.sendMessage(msg);
     }
 
     /**
      * @param bd
      */
     @Override
     public void onResume() {
         super.onResume();
         if (!loginBut.isEnabled()) {
             toggleButtons();
         }
     }
 
     @Override
     public void onBackPressed() {
         super.onBackPressed();
         toggleButtons();
     }
 }
