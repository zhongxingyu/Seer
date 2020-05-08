 package ru.ratadubna.dubnabus;
 
 import android.os.Bundle;
 import android.view.View;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 public class MenuActivity extends SherlockFragmentActivity {
 	private static final String MENU = "menu";
 	private MenuFragment menu;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setTheme(R.style.Theme_Sherlock_Light);
 		if ((menu = (MenuFragment) getSupportFragmentManager()
 				.findFragmentByTag(MENU)) == null) {
 			menu = new MenuFragment();
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, menu, MENU).commit();
 		}
 	}
 
 	public void onChooseClick(View v) {
 		menu.saveSelection();
 		Bus.clearList();
 		BusLocationReceiver.loadingPermission = false;
 		DubnaBusActivity.reloadOverlays = true;
 		finish();
 	}
 
 	public void onCancelClick(View v) {
 		finish();
 	}
 }
