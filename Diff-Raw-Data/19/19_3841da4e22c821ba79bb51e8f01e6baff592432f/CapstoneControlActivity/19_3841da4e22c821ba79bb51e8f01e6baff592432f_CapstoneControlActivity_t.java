 package com.capstonecontrol;
 
 import java.util.Collection;
 import java.util.List;
 
 import com.capstonecontrol.client.ModulesRequestFactory;
 import com.capstonecontrol.client.ModulesRequestFactory.ModuleFetchRequest;
 import com.capstonecontrol.client.MyRequestFactory;
 import com.capstonecontrol.client.MyRequestFactory.HelloWorldRequest;
import com.capstonecontrol.shared.ModuleInfoProxy;
 import com.google.web.bindery.requestfactory.shared.EntityProxy;
 import com.google.web.bindery.requestfactory.shared.Receiver;
 import com.google.web.bindery.requestfactory.shared.ServerFailure;
 
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class CapstoneControlActivity extends BarActivity {
 
 
 	private Button securityButton;
 	private Button lightsButton;
 	private Button appliancesButton;
 	private Button settingsButton;
 	private Button doorButton;
 	
 	 private static final String TAG = "CapstoneControlActivity";
 
 	    /**
 	     * The current context.
 	     */
 	    private Context mContext = this;
 
 	    /**
 	     * A {@link BroadcastReceiver} to receive the response from a register or
 	     * unregister request, and to update the UI.
 	     */
 	    private final BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
 	        @Override
 	        public void onReceive(Context context, Intent intent) {
 	            String accountName = intent.getStringExtra(DeviceRegistrar.ACCOUNT_NAME_EXTRA);
 	            int status = intent.getIntExtra(DeviceRegistrar.STATUS_EXTRA,
 	                    DeviceRegistrar.ERROR_STATUS);
 	            String message = null;
 	            String connectionStatus = Util.DISCONNECTED;
 	            if (status == DeviceRegistrar.REGISTERED_STATUS) {
 	                message = getResources().getString(R.string.registration_succeeded);
 	                connectionStatus = Util.CONNECTED;
 	            } else if (status == DeviceRegistrar.UNREGISTERED_STATUS) {
 	                message = getResources().getString(R.string.unregistration_succeeded);
 	            } else {
 	                message = getResources().getString(R.string.registration_error);
 	            }
 
 	            // Set connection status
 	            SharedPreferences prefs = Util.getSharedPreferences(mContext);
 	            prefs.edit().putString(Util.CONNECTION_STATUS, connectionStatus).commit();
 
 	            // Display a notification
 	            //@TODO find out why below is causing null pointer and crash
 	           // Util.generateNotification(mContext, String.format(message, accountName));
 	        }
 	    };
 
 	    /**
 	     * Begins the activity.
 	     */
 	    @Override
 	    public void onCreate(Bundle savedInstanceState) {
 	        Log.i(TAG, "onCreate");
 	        requestWindowFeature(Window.FEATURE_NO_TITLE);
 	        super.onCreate(savedInstanceState);
 	        
 	        //set policy to ignore network on main thread
 	        //@TODO fix this for real, this is a temp fix
 	        StrictMode.setThreadPolicy(
 	        		new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork()   
 	        		// or .detectAll() for all detectable 
 	        		.build());
 
 	        // Register a receiver to provide register/unregister notifications
 	        registerReceiver(mUpdateUIReceiver, new IntentFilter(Util.UPDATE_UI_INTENT));
 	        
 	        //set up to get modules from gae datastore
 	    }
 
 	    @Override
 	    public void onResume() {
 	        super.onResume();
 
 	        SharedPreferences prefs = Util.getSharedPreferences(mContext);
 	        String connectionStatus = prefs.getString(Util.CONNECTION_STATUS, Util.DISCONNECTED);
 	        if (Util.DISCONNECTED.equals(connectionStatus)) {
 	            startActivity(new Intent(this, AccountsActivity.class));
 	        }
 	        setScreenContent(R.layout.main);
 	    }
 
 	    /**
 	     * Shuts down the activity.
 	     */
 	    @Override
 	    public void onDestroy() {
 	        unregisterReceiver(mUpdateUIReceiver);
 	        super.onDestroy();
 	    }
 
 	    @Override
 	    public boolean onCreateOptionsMenu(Menu menu) {
 	        MenuInflater inflater = getMenuInflater();
 	        inflater.inflate(R.menu.main_menu, menu);
 	        // Invoke the Register activity
 	        menu.getItem(0).setIntent(new Intent(this, AccountsActivity.class));
 	        return true;
 	    }
 
 	    // Manage UI Screens
 
 	    private void setMainScreenContent() {
 	        setContentView(R.layout.main);
 	    	//enable the main button listeners
 	    	enableMainButtonListeners();
 	    	//enable bar
 	    	enableBar();
 	    	//TESTING CONNECTION TO GAE - USING SECURITY BUTTON
 	        final TextView aeMessage = (TextView) findViewById(R.id.aeMessage);
 	        securityButton = (Button) findViewById(R.id.securityButton);
 	        securityButton.setOnClickListener(new OnClickListener() {
 	            public void onClick(View v) {
 	            	securityButton.setEnabled(false);
 	                aeMessage.setText(R.string.contacting_server);
 
 	                // Use an AsyncTask to avoid blocking the UI thread
 	                new AsyncTask<Void, Void, String>() {
 	                    private String message;
 
 	                    @Override
 	                    protected String doInBackground(Void... arg0) {
 	                        MyRequestFactory requestFactory = Util.getRequestFactory(mContext,
 	                                MyRequestFactory.class);
 	                        final HelloWorldRequest request = requestFactory.helloWorldRequest();
 	                        Log.i(TAG, "Sending request to server");
 	                        request.getMessage().fire(new Receiver<String>() {
 	                            @Override
 	                            public void onFailure(ServerFailure error) {
 	                                message = "Failure: " + error.getMessage();
 	                            }
 
 	                            @Override
 	                            public void onSuccess(String result) {
 	                                message = result;
 	                            }
 	                        });
 	                        return message;
 	                    }
 
 	                    @Override
 	                    protected void onPostExecute(String result) {
 	                        aeMessage.setText(result);
 	                        securityButton.setEnabled(true);
 	                    }
 	                }.execute();
 	            }
 	        });
 	    }
 
 	    /**
 	     * Sets the screen content based on the screen id.
 	     */
 	    private void setScreenContent(int screenId) {
 	        setContentView(screenId);
 	        switch (screenId) {
 	            case R.layout.main:
 	                setMainScreenContent();
 	                break;
 	        }
 	    }
 	    
 	    
 	    public void enableMainButtonListeners(){
 	    	//create listeners for the buttons
 	    	//lights button
 	    	  this.lightsButton = (Button)this.findViewById(R.id.lightsButton);
 	    	  this.lightsButton.setOnClickListener(new OnClickListener() {
 	    	    @Override
 	    	    public void onClick(View view) {
 	    	    	//featureNotEnabledMsg();
 	    	    	//change layout to lights layout
 	    	    	//setContentView(R.layout.lights);
 	    	    	Intent myIntent = new Intent(view.getContext(), LightsActivity.class);
 	    	    	startActivity(myIntent);
 	    	    }
 	    	  });
 	    	  //doors button
 	    	  this.doorButton = (Button)this.findViewById(R.id.doorButton);
 	    	  this.doorButton.setOnClickListener(new OnClickListener() {
 	    	    @Override
 	    	    public void onClick(View view) {
 	    	    	//featureNotEnabledMsg();
 	    	    	Intent myIntent = new Intent(view.getContext(), DoorActivity.class);
 	    	    	startActivity(myIntent);
 	    	    }
 	    	  });
 	      	//appliances button
	    	  final TextView aeMessage = (TextView) findViewById(R.id.aeMessage);
 	      	  this.appliancesButton = (Button)this.findViewById(R.id.appliancesButton);
 	      	  this.appliancesButton.setOnClickListener(new OnClickListener() {
 	      	    @Override
 	      	    public void onClick(View view) {
 	      	    	//featureNotEnabledMsg();
 	                // Use an AsyncTask to avoid blocking the UI thread
 	                new AsyncTask<Void, Void, List<ModuleInfo>>() {
 	                    //private String message;
 	                    private List<ModuleInfo> modules;
 	                    @Override
 	                    protected List<ModuleInfo> doInBackground(Void... arg0) {
 	                        ModulesRequestFactory requestFactory = Util.getRequestFactory(mContext,
 	                        		ModulesRequestFactory.class);
 	                        final ModuleFetchRequest request = requestFactory.moduleFetchRequest();
 	                        Log.i(TAG, "Fetching modules");
	                        request.getModules().fire(new Receiver<List<ModuleInfoProxy>>() {
 	                            @Override
 	                            public void onFailure(ServerFailure error) {
	                            	aeMessage.setText("Modules fetch failed!");
 	                            }
 
 								@Override
								public void onSuccess(List<ModuleInfoProxy> arg0) {
									aeMessage.setText("Modules fetch success!");
									modules.equals(arg0);
 								}
 	                        });
	                        
 	                        return modules;
 	                    }
 
 	                    @Override
 	                    protected void onPostExecute(List<ModuleInfo> modules) {
 	                        securityButton.setEnabled(true);
 	                        
 	                    }
 	                }.execute();
 	      	    }
 	    	  });
 	      	//settings button
 	      	  this.settingsButton = (Button)this.findViewById(R.id.settingsButton);
 	      	  this.settingsButton.setOnClickListener(new OnClickListener() {
 	      	    @Override
 	      	    public void onClick(View view) {
 	      	    	//featureNotEnabledMsg();
 	      	    	Intent myIntent = new Intent(view.getContext(), SettingsActivity.class);
 	    	    	startActivity(myIntent);
 	      	    }
 	    	  });
 	    }
 	    
 	    public void featureNotEnabledMsg(){
 	    	new AlertDialog.Builder(this)
 	        .setTitle("Function not yet implemented.")
 	        //.setMessage("")
 	        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 	            public void onClick(DialogInterface dialog, int which) { 
 	                // do nothing, returns to the main menu
 	            }
 	         })
 	        /*
 	        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 	            public void onClick(DialogInterface dialog, int which) { 
 	                // continue with delete
 	            }
 	         })
 	        .setNegativeButton("No", new DialogInterface.OnClickListener() {
 	            public void onClick(DialogInterface dialog, int which) { 
 	                // do nothing
 	            }        
 	         })
 	         */
 	         .show();
 	    }
 	 
 }
