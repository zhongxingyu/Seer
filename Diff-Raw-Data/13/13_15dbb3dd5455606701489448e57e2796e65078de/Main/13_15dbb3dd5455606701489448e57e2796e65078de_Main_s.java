 package pt.up.fe.dceg.accu;
 
 import java.io.FileInputStream;
 
 import pt.up.fe.dceg.accu.console.ConsoleConfig;
 import pt.up.fe.dceg.accu.panel.AccuPanelContainer;
 import pt.up.fe.dceg.accu.state.Accu;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.google.android.maps.MapActivity;
 
 /**
  * Main Activity for ACCU application
  * @author jqcorreia
  *
  */
 public class Main extends MapActivity 
 {
 	private static final String TAG = "Main";
 	
 	/**
 	 * The Panel container (and respective panel selector)
 	 */
 	AccuPanelContainer container;
 	
 	/**
 	 * Console Configuration object
 	 */
 	ConsoleConfig config;
 	FileInputStream configFile;
 	private PowerManager.WakeLock wl;
 	
     public void onCreate(Bundle savedInstanceState) 
     {	
         String s = Accu.getInstance().getPrefs().getString("colorMode", "1");
 		if(s.equals("1"))
 			setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);
 		else
 			setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
 		
         super.onCreate(savedInstanceState);
         PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
         wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
         
         System.out.println("Main onCreate");
         
         if(!Accu.getInstance().isStarted())
         	Accu.getInstance().start();
         
         setContentView(R.layout.main2);
        
         container = (AccuPanelContainer)findViewById(R.id.container);
         
         // Load/Process configuration panel
         config = new ConsoleConfig(this, "config.xml", container);
         config.initPanels();
 
         container.getSelector().setOpenerVisible(false); // Hide the selector opener button
         
         // Replace the active system state (if one was selected previously)
         if(savedInstanceState != null)
         {
 			if (savedInstanceState.getString("sys") != null) {
 				Accu.getInstance().setActiveSys(
 						Accu.getInstance().mSysList
 								.findSysByName(savedInstanceState
 										.getString("sys")));
 			}
         }
     } 
 
 	@Override
     public void onStart()
     {
     	super.onStart();
     	if(!Accu.getInstance().isStarted())
     		Accu.getInstance().start();
     	container.startPanelWithId(container.currentPanelId);
     	wl.acquire();
     }
     
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
		outState.putString("sys", Accu.getInstance().getActiveSys().getName());
 	}
     
     @Override
 	protected void onDestroy() {
     	Log.i(TAG,"Destroying Main Activity");
     	super.onDestroy();
 	}
 
 	@Override
     public void onPause()
     {
     	super.onPause();
     	// Panel must be stopped before Accu instance because of pausing/stopping messages
     	// like TeleOperationDone on TeleOp Panel
     	Log.i(TAG,"Pausing main activity");
     	container.stopCurrentPanel();
     	Accu.getInstance().pause();
     	if(wl.isHeld())
     		wl.release();
     }
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	public ConsoleConfig getConsoleConfig()
 	{
 		return config;
 	}
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) 
 	{
 		if(event.getAction() == KeyEvent.ACTION_DOWN)
 		{
 			if(event.getKeyCode()==KeyEvent.KEYCODE_BACK)
 			{
 				if(event.isLongPress())
 					this.finish();
 				else
 					container.getSelector().toggle();
 			}
 		}
 		return false;	
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu)
 	{
 		container.getCurrentPanel().prepareMenu(menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		container.getCurrentPanel().menuHandler(item);
 		return true;
 	}
 }
