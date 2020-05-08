 package nl.sison.xmpp;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * NOTE: before 4.* (api level 15) this was FragmentActivity, using the Fragment
  * from the compatibility shizzle
  * 
  * @author Jasm Sison
  * 
  */
 public class SinglePanelActivity extends Activity implements FragmentLoader {
 	private static final String TAG = "SinglePanelActivity";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		/*
 		 * if (getResources().getConfiguration().orientation ==
 		 * Configuration.ORIENTATION_LANDSCAPE) { // If the screen is now in
 		 * landscape mode, we can show the // dialog in-line with the list so we
 		 * don't need this activity. finish(); return; }
 		 */
 		// setContentView(R.layout.main_layout); // NOTE: this does not add
 		// anything to the container
 		// object in onCreateView
 
 		startService(new Intent(this, XMPPService.class));
 
 		setContentView(R.layout.tabbed_single_fragment_layout);
 
		// During initial setup, plug in the details fragment.
		Fragment fragment = new ConnectionListFragment();
 		fragment.setArguments(getIntent().getExtras());
 		getFragmentManager().beginTransaction()
 				.add(R.id.single_panel_1, fragment).commit();
 	}
 
 	/**
 	 * 
 	 * After clicking a damn on an ListItem. Load a new fragment and put
 	 * previous fragment on the backstack
 	 * 
 	 * @param intent
 	 */
 	@Override
 	public void loadFragment(Intent intent) throws NullPointerException {
 		String className;
 		className = intent.getComponent().getClassName();
 		Fragment fragment;
 		try {
 			makeToast("loading fragment: " + className);
 			fragment = (Fragment) Class.forName(className).newInstance(); // Reflection
 			fragment.setArguments(intent.getExtras());
 			FragmentTransaction transaction = getFragmentManager()
 					.beginTransaction().replace(R.id.single_panel_1, fragment);
 			transaction.addToBackStack("identifier"); // TODO - use the same
 														// "identifier" for the
 														// tab?
 			transaction.commit();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} // new java 7: catch (Exception1 e1 | Exception2 e2 | Exception e3 ...
 
 	}
 
 	/**
 	 * TODO - use this to detect the screen size WindowManager wm =
 	 * (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE); Display
 	 * display = wm.getDefaultDisplay();
 	 */
 
 	/**
 	 * TODO - use this to detect the screen size Configuration conf =
 	 * getResources().getConfiguration();
 	 */
 
 	@Deprecated
 	private void makeToast(String message) {
 		if (!BuildConfig.DEBUG)
 			return;
 		Log.i(TAG, message);
 		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
 		toast.show();
 	}
 
 	@Override
 	public void swipeToFragment(Intent intent) throws NullPointerException {
 		// TODO Auto-generated method stub
 
 	}
 }
