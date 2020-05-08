 package info.ohgita.bincalc_android;
 
 /**
  * Bin.Calc - MainActivity
  * @author Masanori Ohgita
  */
 
 import android.annotation.SuppressLint;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.view.WindowManager;
 
 import com.actionbarsherlock.R;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.SubMenu;
 
 public class MainActivity extends SherlockFragmentActivity {
 	
 	private static final int MENU_ID_LOG = 100;
 	private static final int MENU_ID_ABOUT = 200;
 	private static final int MENU_ID_ALLCLEAR = 300;
 	private static final int MENU_ID_PREF = 400;
 	
 	FragmentManager fragmentManager;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		setTheme(R.style.Theme_Sherlock_Light);
 		setContentView(R.layout.activity_main);
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
 		sub_menu.add(Menu.NONE, MENU_ID_ABOUT, Menu.NONE , R.string.menu_about);
 		
 		/* All-Clear button*/
 		menu.add(Menu.NONE, MENU_ID_ALLCLEAR, Menu.NONE, "AllClear")
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
 			MainFragment f = (MainFragment) fragmentManager.findFragmentById(R.id.fragment_Main);
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
