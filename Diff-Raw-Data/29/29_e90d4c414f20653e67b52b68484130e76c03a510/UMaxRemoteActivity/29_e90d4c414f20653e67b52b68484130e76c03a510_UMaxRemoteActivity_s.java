 package edu.uidaho.engr.seniordesign.umax;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import edu.uidaho.engr.seniordesign.umax.DefaultVariables.OnControlStatusListener;
 import edu.uidaho.engr.seniordesign.umax.NetConnectionFragment.OnNetListClickListener;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.DisplayMetrics;
 import android.view.*;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.widget.*;
 
 public class UMaxRemoteActivity extends Activity implements OnNetListClickListener {
     /** Called when the activity is first created. */
 	
 	public static final String PREFS_NAME = "UMaxPrefs";
 	
 	SharedPreferences settings;
 	
 	MenuItem menu_start, menu_stop, menu_reset, menu_connect, menu_disconnect;
 	
 	//NetworkConnection nc;
 	
 	FragmentManager fragmentManager;
 	
 	FrameLayout rightFrame;
 	
 	MainMenu mainMenu;
 	
 	private Intent serviceIntent;
 	private CommService commService;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ad_action_bar_gradient_bak));
                 
         rightFrame = (FrameLayout) findViewById(R.id.extras);
        if (!isTablet()) {
         	rightFrame.setVisibility(View.GONE);
         }
         
         fragmentManager = getFragmentManager();
         
         settings = getSharedPreferences(PREFS_NAME, 0);
         
         commService = CommService.getInstance();
         
         if (commService == null) {
         	serviceIntent = new Intent(getApplicationContext(), CommService.class);
         	startService(serviceIntent);
         }
         
         commService = CommService.getInstance();
         if (commService == null) {
         	Timer t = new Timer();
         	t.schedule(getCommService, 50);
         } else {
         	fragmentHandler.sendMessage(fragmentHandler.obtainMessage(0));
         }
         
         DefaultVariables.addOnControlStatusListener(new OnControlStatusListener() {
 
 			public void updateView(int what, int arg1, int arg2, Object obj) {
 				viewUpdateHandler.sendMessage(viewUpdateHandler.obtainMessage(what, arg1, arg2, obj));
 			}
         	
         });
     }
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
     
     private TimerTask getCommService = new TimerTask() {
 		@Override
 		public void run() {
 			commService = CommService.getInstance();
 	        if (commService == null) {
 	        	Timer t = new Timer();
 	        	getCommService.cancel();
 	        	try {
 	        		t.schedule(getCommService, 50);
 	        	} catch (Exception e) { }
 	        } else {
 	        	fragmentHandler.sendMessage(fragmentHandler.obtainMessage(0));
 	        }
 		}
     };
     
     final Handler fragmentHandler = new Handler() {
     	public void handleMessage(Message msg) {
     		if (msg.what == 0) {
     			// LOAD FRAGMENTS
     	    	mainMenu = new MainMenu();
     	    	FragmentTransaction ft = getFragmentManager().beginTransaction();
     	    	ft.replace(R.id.mainFragment, mainMenu);
     	    	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
     	    	ft.commit();
     		}
     	}
     };
     
     private Handler viewUpdateHandler = new Handler() {
     	public void handleMessage(Message msg) {
     		if (msg.what == 0) {
     			// set enabled
     			if (msg.obj == null)
     				return;
     			setViewEnabled((View) msg.obj, (msg.arg1 == 1 ? true : false));
     		} else if (msg.what == 1) {
     			// set enabled
     			if (msg.obj == null)
     				return;
     			setMenuEnabled((MenuItem) msg.obj, (msg.arg1 == 1 ? true : false));
     		}
     	}
     	public void setViewEnabled(View view, boolean enabled) {
     		try {
     			view.setEnabled(enabled);
     		}
     		catch (Exception e) {
     			// we can't touch this view, so we do nothing
     		}
     	}
     	public void setMenuEnabled(MenuItem view, boolean enabled) {
     		try {
     			view.setEnabled(enabled);
     		}
     		catch (Exception e) {
     			// we can't touch this view, so we do nothing
     		}
     	}
     };
     
     private OnMenuItemClickListener MenuItemClick = new OnMenuItemClickListener() {
     	public boolean onMenuItemClick(MenuItem item) {
     		switch (item.getItemId()) {
     		case R.id.menu_start:
     			commService.sendJsonAction("buttonRun");
     			break;
     		case R.id.menu_stop:
     			commService.sendJsonAction("buttonStop");
     			break;
     		case R.id.menu_reset:
     			commService.sendJsonAction("buttonReset");
     			break;
     		}
     		return false;
     	}
     };
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main_menu, menu);
         
 
         menu_start = menu.findItem(R.id.menu_start);
         menu_stop = menu.findItem(R.id.menu_stop);
         menu_reset = menu.findItem(R.id.menu_reset);
         
         menu_start.setOnMenuItemClickListener(MenuItemClick);
         menu_stop.setOnMenuItemClickListener(MenuItemClick);
         menu_reset.setOnMenuItemClickListener(MenuItemClick);
         
         if (commService == null)
         	commService = CommService.getInstance();
         
         DefaultVariables.setStart(menu_start);
         DefaultVariables.setStop(menu_stop);
         DefaultVariables.setReset(menu_reset);
         
         return super.onCreateOptionsMenu(menu);
     }
 
 	public void connect() {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle("Enter address");
 		alert.setMessage("Enter address");
 		final EditText input = new EditText(this);
 		settings = getSharedPreferences(PREFS_NAME, 0);
 		String lastConnectedAddress = settings.getString("lastConnectedAddress", "");
 		input.setText(lastConnectedAddress);
 		input.setInputType(0x00000003);
 		alert.setView(input);
 		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				SharedPreferences.Editor editor = settings.edit();
 				editor.putString("lastConnectedAddress", input.getText().toString());
 				editor.commit();
 				DefaultVariables.setConnecting();
 				commService.connect(input.getText().toString());
 			}
 		});
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				// canceled.
 			}
 		});
 		alert.show();
 		
 	}
 
 	public void disconnect() {
 		commService.disconnect();
 	}
     
 
 	public boolean isTablet()
 	{
 	    Display display = getWindowManager().getDefaultDisplay();
 	    DisplayMetrics displayMetrics = new DisplayMetrics();
 	    display.getMetrics(displayMetrics);
 
 	    int width = displayMetrics.widthPixels / displayMetrics.densityDpi;
 	    int height = displayMetrics.heightPixels / displayMetrics.densityDpi;
 
 	    double screenDiagonal = Math.sqrt( width * width + height * height );
 	    return (screenDiagonal >= 7.0 );
 	}
 }
