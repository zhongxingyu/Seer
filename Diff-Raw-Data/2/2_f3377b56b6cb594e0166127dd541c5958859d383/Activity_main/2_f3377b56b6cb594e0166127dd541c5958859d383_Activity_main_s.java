 package info.ohgita.bincalc_android;
 
 /**
  * Bin.Calc - MainActivity
  * @author Masanori Ohgita
  */
 
 import android.annotation.SuppressLint;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.view.WindowManager;
 
 import com.actionbarsherlock.R;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.SubMenu;
 
 public class Activity_main extends SherlockFragmentActivity {
 
 	private static final int MENU_ID_LOG = 100;
 	private static final int MENU_ID_ABOUT = 200;
 	private static final int MENU_ID_ALLCLEAR = 300;
 	private static final int MENU_ID_PREF = 400;
 
 	private static final int REQUEST_CODE_PREFERENCE_DONE = 1000;
 
 	private static final String STATE_KEY_BASETYPE = "BASETYPE";
 	private static final String STATE_KEY_BASEINPUT_VALUE = "BASEINP_VAL";
 
 	FragmentManager fragmentManager;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getWindow().setSoftInputMode(
 				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		setTheme(R.style.Theme_Sherlock_Light);
 		setContentView(R.layout.activity_main);
 
 		fragmentManager = getSupportFragmentManager();
 		
 		Log.i("binCalc", "Activity - onCreated");
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		Log.d("binCalc","Activity - onSaveInstanceState()");
 
 		Fragment_main f = (Fragment_main) fragmentManager
 				.findFragmentById(R.id.fragment_Main);
 
 		outState.putInt(STATE_KEY_BASETYPE, f.selectedBasetypeId);
 		outState.putString(STATE_KEY_BASEINPUT_VALUE, f
 				.getCurrent_Baseinput_EditText().getText().toString());
 
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		Log.d("binCalc","Activity - onRestoreInstanceState()");
 		Fragment_main f = (Fragment_main) fragmentManager
 				.findFragmentById(R.id.fragment_Main);
 
 		int basetype = savedInstanceState.getInt(STATE_KEY_BASETYPE);
 		if (basetype != -1) {
 			f.selectedBasetypeId = basetype;
 		}
 		
 		String value = savedInstanceState.getString(STATE_KEY_BASEINPUT_VALUE); 
 		if(value != null){
 			// DEBUG!! 値のリストアは未実装
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == REQUEST_CODE_PREFERENCE_DONE) {
 			/* Return from Preference activity */
 			if (resultCode == RESULT_OK) {
 				/* Reload preference */
 				Fragment_main f = (Fragment_main) fragmentManager
 						.findFragmentById(R.id.fragment_Main);
 				f.loadPreferences();
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		/* SubMenu button */
 		SubMenu sub_menu = menu.addSubMenu("Overflow Item");
 		sub_menu.getItem().setIcon(R.drawable.ic_action_overflow);
 		sub_menu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 
 		/* SubMenu > Log button */
 		sub_menu.add(Menu.NONE, MENU_ID_LOG, Menu.NONE, R.string.menu_log);
 
 		/* SubMenu > Preference button */
 		sub_menu.add(Menu.NONE, MENU_ID_PREF, Menu.NONE, R.string.menu_pref);
 
 		/* SubMenu > About button */
 		sub_menu.add(Menu.NONE, MENU_ID_ABOUT, Menu.NONE, R.string.menu_about);
 
 		/* All-Clear button */
 		menu.add(Menu.NONE, MENU_ID_ALLCLEAR, Menu.NONE, "All-clear")
 				.setIcon(R.drawable.button_allclear)
 				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 
 		return true;
 	}
 
 	@SuppressLint("NewApi")
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		boolean ret = true;
 		switch (item.getItemId()) {
 		default:
 			ret = super.onOptionsItemSelected(item);
 			break;
 		case MENU_ID_LOG:
 			ret = true;
 			break;
 		case MENU_ID_ABOUT:
 			ret = false;
 			startActivity(new Intent(this, Activity_appInfo.class));
 			break;
 		case MENU_ID_ALLCLEAR:
 			ret = false;
 			Fragment_main f = (Fragment_main) fragmentManager
 					.findFragmentById(R.id.fragment_Main);
 			f.inputAllClear();
 			break;
 		case MENU_ID_PREF:
 			ret = false;
			startActivity(new Intent(this, Activity_preference.class));
 			break;
 		}
 		return ret;
 	}
 }
