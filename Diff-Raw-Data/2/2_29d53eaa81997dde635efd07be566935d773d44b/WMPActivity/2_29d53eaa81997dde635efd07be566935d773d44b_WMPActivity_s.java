 package edu.bonn.cs.wmp;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.Toast;
 import edu.bonn.cs.wmp.WMPConnectionListener;
 import edu.bonn.cs.wmp.application.WMPApplication;
 
 /**
  * This class should be used as a super class for all collaborative Activities.
 * It automatically registrates at the {@link WMPApplication} object and sports
  * some necessary connection related menu entries.
  * 
  * @author Sven Bendel
  *
  */
 public class WMPActivity extends Activity implements WMPConnectionListener {
 	private WMPApplication app;
 
 	boolean DEBUG = false;
 	private final String TAG = this.getClass().getSimpleName();
 
 	/**
 	 * Convenience method for presenting a toast to the user.
 	 * @param text
 	 * 		The text to be displayed within the toast. 
 	 * @param length
 	 * 		The amount of time this toast will be shown. May be {@link Toast#LENGTH_LONG} or
 	 * 		{@link Toast#LENGTH_SHORT}.
 	 */
 	protected void makeAndShowToast(final String text, final int length) {
 		runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				Toast.makeText(getApplicationContext(), text, length).show();
 			}
 		});
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		app = (WMPApplication) getApplication();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menu_pref) {
 			app.startXMPPPrefs();
 		} else if (item.getItemId() == R.id.menu_join) {
 			app.connectToServiceAndServer(this);
 		} else if (item.getItemId() == R.id.menu_disconnect) {
 			app.disconnectFromMXA();
 		}
 		return true;
 	}
 
 	@Override
 	protected void onDestroy() {
 		if (app.isCollabEditingServiceConnected()) {
 			app.disconnectFromMXA();
 		}
 		super.onDestroy();
 	}
 
 	@Override
 	public void onConnected() {
 		app.joinSession("edit_text_test");
 	}
 
 	@Override
 	public void onDisconnected() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
