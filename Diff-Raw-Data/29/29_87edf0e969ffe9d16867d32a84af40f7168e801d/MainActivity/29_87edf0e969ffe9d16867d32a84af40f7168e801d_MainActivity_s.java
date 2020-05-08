 package net.jsiq.marketing.activity;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import net.jsiq.marketing.R;
 import net.jsiq.marketing.fragment.CatalogFragment;
 import net.jsiq.marketing.fragment.LeftMenuFragment;
 import net.jsiq.marketing.fragment.LeftMenuFragment.LOADSTATUS;
 import net.jsiq.marketing.model.MenuItem;
 import net.jsiq.marketing.util.MessageToast;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 
 public class MainActivity extends BaseActivity {
 
 	private static Boolean isExit = false;
 	private MenuItem firstMenu, currentMenu;
 	private View loadingHintView, loadingFailedHintView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.content_frame);
 		findViews();
 		setListeners();
 	}
 
 	public void refreshMainContent(LeftMenuFragment.LOADSTATUS loadStatus) {
 		loadingHintView
 				.setVisibility(loadStatus == LOADSTATUS.LOADING ? View.VISIBLE
 						: View.GONE);
 		loadingFailedHintView
 				.setVisibility(loadStatus == LOADSTATUS.FAILED ? View.VISIBLE
 						: View.GONE);
 	}
 
 	public void initFirstDefaultCatalog(MenuItem item) {
 		if (item != null) {
 			firstMenu = item;
 			currentMenu = item;
 		}
 		initNewCatalogFragmentByMenu(item);
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (currentMenu == null || firstMenu == null
 				|| currentMenu.getMenuId() == firstMenu.getMenuId()) {
 			exitBy2Click();
 		} else {
 			switchCatalogByMenu(firstMenu);
 		}
 	}
 
 	public void switchCatalogByMenu(MenuItem item) {
 		if (currentMenu.getMenuId() != item.getMenuId()) {
 			currentMenu = item;
 			initNewCatalogFragmentByMenu(item);
 		}
 		new Handler().post(new Runnable() {
 			public void run() {
 				getSlidingMenu().showContent();
 			}
 		});
 	}
 
 	private void setListeners() {
 		loadingFailedHintView.setOnClickListener(this);
 	}
 
 	private void findViews() {
 		loadingHintView = findViewById(R.id.loadingHint);
 		loadingFailedHintView = findViewById(R.id.loadingFailedHint);
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
 		CatalogFragment fragment = new CatalogFragment();
 		Bundle extra = new Bundle();
 		if (item != null) {
 			extra.putInt(CatalogFragment.MENU_ID, item.getMenuId());
 			extra.putString(CatalogFragment.CATALOG_TITLE, item.getMenuName());
 		}
 		fragment.setArguments(extra);
 		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment).commit();
 	}
 
 	@Override
 	public void onClick(View v) {
 		super.onClick(v);
 		if (v.getId() == R.id.loadingFailedHint) {
 			loadMenu();
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 	}
 
 }
