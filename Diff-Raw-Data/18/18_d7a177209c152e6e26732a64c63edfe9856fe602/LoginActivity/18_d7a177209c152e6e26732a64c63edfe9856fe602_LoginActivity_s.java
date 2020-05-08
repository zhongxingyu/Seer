 package com.ericsson.ecom.ui;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.content.Intent;
 import com.ericsson.ecom.R;
 
 import com.ericsson.ecom.app.EcomApplication;
 import com.ericsson.ecom.cli.CliSession;
 import com.ericsson.ecom.nc.NetconfSession;
 import com.ericsson.ecom.ssh.SshSession;
 
 /**
  * DESCRIPTION
  *
  * @author eranibl
  * @date 2013-04-19, 09:17
  */
 public class LoginActivity extends Activity {
     private static final String LOG = "eCOM - LoginActivity";
     private TextView server, message;
     private EditText username, password;
 
     public void onCreate(Bundle bundle) {
         super.onCreate(bundle);
         setContentView(R.layout.login);
 
         ActionBar actionBar = getActionBar();
         actionBar.setDisplayShowHomeEnabled(true);
         actionBar.setDisplayUseLogoEnabled(true);
         actionBar.setDisplayShowTitleEnabled(true);
         actionBar.setTitle("eCOM");
 
         server = (TextView) findViewById(R.id.server);
         message = (TextView) findViewById(R.id.message);
         username = (EditText) findViewById(R.id.username);
         password = (EditText) findViewById(R.id.password);
 
 
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         EcomApplication app = (EcomApplication) getApplication();
         server.setText(app.getServer()+":" + app.getPort());
         username.setText(app.getUsername());
         password.setText(app.getPassword());
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.menu_main, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.menu_settings) {
             startActivity(new Intent(this, Preferences.class));
             return true;
         } else if (item.getItemId() == R.id.menu_about) {
             startActivity(new Intent(this, AboutActivity.class));
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
 
     public void login(View view) {
         String user = username.getText().toString();
         String pwd = password.getText().toString();
         Log.d(LOG, "LOGIN: " + user + " / " + pwd);

         EcomApplication app = (EcomApplication) getApplication();
         String server = app.getServer();
         int port = app.getPort();
 
         CliSession cli = new CliSession(server, port, user, pwd);
         NetconfSession nc = new NetconfSession(server, port, user, pwd);
     }
 }
