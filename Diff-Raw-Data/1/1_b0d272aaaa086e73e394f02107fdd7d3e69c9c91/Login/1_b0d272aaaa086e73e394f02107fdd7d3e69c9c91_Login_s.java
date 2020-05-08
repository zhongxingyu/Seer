 package at.roadrunner.android.activity;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Resources.NotFoundException;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 import at.roadrunner.android.R;
 import at.roadrunner.android.controller.ContainerController;
 import at.roadrunner.android.couchdb.RequestWorker;
 
 public class Login extends Activity {
 	@SuppressWarnings("unused")
 	private static final String TAG = "Login";
 	
 	private Runnable _loadContainers;
 	private ProgressDialog _progressDialog = null;
 	private ArrayAdapter<String> _containerAdapter;
 	private ArrayList<JSONObject> _containers;
 	
 	private EditText _username; 
 	private EditText _password;
 	private Spinner _container;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		
 		_username = (EditText) findViewById(R.id.login_username);
 		_password = (EditText) findViewById(R.id.login_password);
 		_container = (Spinner) findViewById(R.id.login_container);
 		
 		// Runnable to fill the items in a Thread
 		_loadContainers = new Runnable() {
 			@Override
 			public void run() {
 				loadTransportations();
 			}
 		};
 
 		// start a new Thread to get the items
 		new Thread(null, _loadContainers, "getTransportations").start();
 
 		// show the Progressbar
 		_progressDialog = ProgressDialog.show(this, getString(R.string.app_progress_pleasewait), getString(R.string.app_progress_retdata), true);
 		
 		_containerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
 		_containerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		
 		final Spinner cbxTransportation = (Spinner) findViewById(R.id.login_container);
 		cbxTransportation.setAdapter(_containerAdapter);
 	}
 
 	private void loadTransportations() {
 		_containers = new ContainerController(this).getContainers();
 		runOnUiThread(updateActivity);
 	}
 	
 	private final Runnable updateActivity = new Runnable() {
 		@Override
 		public void run() {
 			if (_containers != null) {
 				for (JSONObject container : _containers) {
 					try {
 						_containerAdapter.add(container.getString("name"));
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				}
 				_containerAdapter.notifyDataSetChanged();
 			} 
 			_progressDialog.dismiss();
 		}
 	};
 	
 	public void onLoginClick(View view) {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
 		String username = _username.getText().toString();
 		String password = _password.getText().toString();
 		String container = null;
 		
 		// you can't login if there are no transportation loaded
 		if (_container.getCount() > 0) {
 			container = _container.getSelectedItem().toString();
 			String containerId = getIdForContainer(container);
 			if (new RequestWorker(this).isAuthenticatedAtServer(username, password))  {
 				MediaPlayer mp = MediaPlayer.create(this, R.raw.meepmeep);
 				mp.start();
 				
 				// save the used form values into the preferences
 				Editor edit = prefs.edit();
 				edit.putString("user", username);
 				edit.putString("password", password);
 				edit.putString("transportation", container);
 				edit.putString("transportationId", containerId);
 				edit.commit();
 				
 				startActivity(new Intent(this, Roadrunner.class));
 				finish();
 			}  else  {
 				Toast.makeText(this, "Not Authenticated", Toast.LENGTH_SHORT).show();
 			}
 		} else {
 			Toast.makeText(this, "There are no Transportations available. Select \"Refresh Transportation\" in the Menu.", Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	/*
 	 * inflate menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.login_menu, menu);
 		return true;
 	}
 
     /*
      * Event OptionsMenuItemSelected
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.login_menu_refresh:
         	new Thread(null, _loadContainers, "getTransportations").start();
             return true;
         case R.id.login_menu_exit:
         	finish();
         	return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
     
     private String getIdForContainer(String containerName) {
 		try {
 			for (JSONObject obj : _containers)  {
 				if (obj.get("name").equals(containerName))  {
 					return obj.getString("id");
 				}
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return null;
     }
 }
