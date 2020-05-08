 /*
  * Copyright (C) 2011, Valentin Lorentz
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package org.openihs.seendroid;
 
 import org.openihs.seendroid.lib.Connection;
 import org.openihs.seendroid.lib.MessageEmitter;
 import org.openihs.seendroid.lib.Query.ParserException;
 import org.w3c.dom.Document;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager.LayoutParams;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	private static SharedPreferences settings;
 	private Connection connection;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.main);
         MainActivity.settings = PreferenceManager.getDefaultSharedPreferences(this);
 
         this.bindUi();
 
         this.safeConnect();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return true;
     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
 	        case R.id.main_menu_settings:
 				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
 				startActivity(intent);
 	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
         }
     }
     
     public void safeConnect() {
         // Check if username and password are available.
         String username = settings.getString("login.username", "");
         String password = settings.getString("login.password", "");
         if (username == "" || password == "") {
         	// Ask login credentials if not available.
         	final Dialog dialog = new Dialog(this);
 
         	dialog.setContentView(R.layout.loginprompt);
         	dialog.setTitle(R.string.loginprompt_title);
         	
         	Button button = (Button) dialog.findViewById(R.id.loginprompt_button_connect);
         	button.setOnClickListener(
 	        			new Button.OnClickListener() {
 							@Override
 							public void onClick(View button) {
 								// User clicked the "Connect" button.
 								EditText username = (EditText) dialog.findViewById(R.id.loginprompt_edittext_username);
 								EditText password = (EditText) dialog.findViewById(R.id.loginprompt_edittext_password);
 								SharedPreferences.Editor editor = MainActivity.settings.edit();
 								editor.putString("login.username", username.getText().toString());
 								editor.putString("login.password", password.getText().toString());
 								editor.commit();
 								dialog.dismiss(); // It will run safeConnect().
 							}
 	        			}
         			);
         	dialog.setOnDismissListener(
 	        			new Dialog.OnDismissListener() {
 							@Override
 							public void onDismiss(DialogInterface dialog) {
 								MainActivity.this.safeConnect();
 							}
 	        			}
         			);
         	dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
         	dialog.show();
         }
         else {
         	// Connect immediatly if credentials are available.
         	this.connect();
         }
     }
     private void connect() {
         String username = settings.getString("login.username", "");
         String password = settings.getString("login.password", "");
         Log.d("SeenDroid", String.format("Login as %s", username));
         this.connection = new Connection(username, password);
     }
     
     private class OnUnavailableFeatureClickListener implements Button.OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 			Toast.makeText(MainActivity.this, R.string.main_unavailablefeature, Toast.LENGTH_LONG).show();
 		}
     	
     }
 
     private void bindUi() {
         // Profile
     	((Button) this.findViewById(R.id.main_button_profile)).setOnClickListener(
     			new Button.OnClickListener() {
 
 					@Override
 					public void onClick(View button) {
 						Bundle bundle = new Bundle();
 						bundle.putString("username", MainActivity.this.connection.getUsername());
 						Intent intent = new Intent(MainActivity.this, ShowUserActivity.class);
 						intent.putExtras(bundle);
 						startActivity(intent);
 					}
     			}
 			);
 
     	// Show user
     	((Button) this.findViewById(R.id.main_button_show_user)).setOnClickListener(
 	    			new Button.OnClickListener() {
 
 						@Override
 						public void onClick(View button) {
 							EditText editText = (EditText) MainActivity.this.findViewById(R.id.main_edittext_goto);
 							Bundle bundle = new Bundle();
 							bundle.putString("username", editText.getText().toString());
 							Intent intent = new Intent(MainActivity.this, ShowUserActivity.class);
 							intent.putExtras(bundle);
 							startActivity(intent);
 						}
 	    				
 	    			}
     			);
     	
     	
     	// Post message
    	((Button) this.findViewById(R.id.main_button_newpost)).setOnClickListener(
 	    			new Button.OnClickListener() {
 
 						@Override
 						public void onClick(View button) {
 							Intent intent = new Intent(MainActivity.this, PostMessageActivity.class);
 							startActivity(intent);
 
 						}
 	    				
 	    			}
     			);
     	
     	// Home feed
     	((Button) this.findViewById(R.id.main_button_home_feed)).setOnClickListener(new OnUnavailableFeatureClickListener());
     	
     	// Search
     	((Button) this.findViewById(R.id.main_button_search)).setOnClickListener(new OnUnavailableFeatureClickListener());
 
     	// Settings
     	((Button) this.findViewById(R.id.main_button_settings)).setOnClickListener(new OnUnavailableFeatureClickListener());
     }
 }
