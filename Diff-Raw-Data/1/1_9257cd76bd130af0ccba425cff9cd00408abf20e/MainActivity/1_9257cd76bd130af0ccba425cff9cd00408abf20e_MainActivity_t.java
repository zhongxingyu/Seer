 package net.jsiq.marketing.activity;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import net.jsiq.marketing.R;
 import net.jsiq.marketing.fragment.CatalogFragment;
 import net.jsiq.marketing.model.MenuItem;
 import net.jsiq.marketing.util.MessageToast;
 import android.os.Bundle;
 import android.os.Handler;
 
 public class MainActivity extends BaseActivity {
 
 	private static Boolean isExit = false;
 	private MenuItem firstMenu;
 	private MenuItem currentMenu;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.content_frame);
 	}
 
 	public void initFirstDefaultFragment(MenuItem item) {
 		firstMenu = item;
		currentMenu = item;
 		initNewCatalogFragmentByMenu(item);
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (currentMenu.getMenuId() == firstMenu.getMenuId()) {
 			exitBy2Click();
 		} else {
 			currentMenu = firstMenu;
 			switchCatalogByMenu(firstMenu);
 		}
 	}
 
 	public void switchCatalogByMenu(MenuItem item) {
 		currentMenu = item;
 		initNewCatalogFragmentByMenu(item);
 		new Handler().post(new Runnable() {
 			public void run() {
 				getSlidingMenu().showContent();
 			}
 		});
 	}
 
 	private void exitBy2Click() {
 		Timer tExit = null;
 		if (isExit) {
 			finish();
 			System.exit(0);
 		} else {
 			isExit = true;
 			MessageToast.showText(this, R.string.clickAgain);
 			tExit = new Timer();
 			tExit.schedule(new TimerTask() {
 
 				@Override
 				public void run() {
 					isExit = false;
 				}
 			}, 2000);
 		}
 	}
 
 	private void initNewCatalogFragmentByMenu(MenuItem item) {
 		Bundle extra = new Bundle();
 		extra.putInt(CatalogFragment.MENU_ID, item.getMenuId());
 		extra.putString(CatalogFragment.CATALOG_TITLE, item.getMenuName());
 		CatalogFragment fragment = new CatalogFragment();
 		fragment.setArguments(extra);
 		getSupportFragmentManager().beginTransaction()
 				.replace(R.id.content_frame, fragment).commit();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 	}
 
 }
