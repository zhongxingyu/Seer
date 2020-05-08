 package com.infoit.main;
 
 import java.util.Arrays;
 import java.util.Set;
 
 import org.codehaus.jackson.JsonNode;
 
 import android.app.Activity;
 import android.content.pm.ActivityInfo;
 import android.content.res.ColorStateList;
 import android.graphics.drawable.Drawable;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.FrameLayout.LayoutParams;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.infoit.adapters.MenuCategoryAdapter;
 import com.infoit.adapters.MenuItemListAdapter;
 import com.infoit.adapters.SeparatedListAdapter;
 import com.infoit.adapters.WebServiceAdapter;
 import com.infoit.async.LoadMenuTask;
 import com.infoit.constants.Constants;
 import com.infoit.record.MenuInformation;
 import com.infoit.util.ShellUtil;
 import com.infoit.widgets.UiMenuHorizontalScrollView;
 
 public class DisplayMenu extends Activity {
 	private UiMenuHorizontalScrollView mApplicationContainer;
 	private ListView mMenuItemList;
 	private MenuInformation mMenuInformation;
 	private String mCurrentMenuType;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		
 		
 		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
 			Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(
 					NfcAdapter.EXTRA_NDEF_MESSAGES);
 			NdefMessage rawUrl = (NdefMessage) rawMsgs[0];
 			NdefRecord rawUrlRecord = rawUrl.getRecords()[0];
 			byte[] payload = rawUrlRecord.getPayload();
 			String uri = new String(Arrays.copyOfRange(payload, 1,
 					payload.length));
 
 			int identifier = Integer.parseInt(uri.split("/menus/")[1]);
 			new LoadMenuTask(this, identifier).execute();
 			setSplashScreen();
 		} else if (Constants.QRCODE.equals(getIntent().getAction())) {
 			int identifier = getIntent().getExtras().getInt("identifier");
 			new LoadMenuTask(this, identifier).execute();
 		} else {
 			String jsonAsString = getIntent().getExtras().getString("menu");
 			JsonNode json = WebServiceAdapter.createJsonFromString(jsonAsString);
			mMenuInformation = new MenuInformation(json.path("entity").path("place_details"));
 		    mCurrentMenuType = (String) ((Set<String>) mMenuInformation.getMenuTypes()).iterator().next();
 		}

	    // Lock to Portrait Mode
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}


	@Override
	protected void onResume() {
		super.onResume();
 	    
 	    //Adapters intialized in async for NFC
 	    if (Constants.DISPLAY_INFO.equals(getIntent().getAction())) {
 		    mApplicationContainer = ShellUtil.initializeApplicationContainer(this,
 		            R.layout.ui_navigation_menu, R.layout.ui_empty_action_menu,
 		            R.layout.menu);
 		    ShellUtil.clearActionMenuButton(mApplicationContainer);
 		    
 		    //50 should work, but not displaying correctly, so nudging to 70
 		    int menuBarHeight = (int) (75 * getResources().getDisplayMetrics().density);
 		    Display display = getWindowManager().getDefaultDisplay();
 
 		    LinearLayout container = (LinearLayout) findViewById(R.id.container);
 		    container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, display.getHeight() - menuBarHeight));
 	    	
 		    initializeMenuTypeSelector();
 		    initializeAdapters();
 		    
 		    mApplicationContainer.scrollToApplicationView();
 	    }
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 	    unbindDrawables(mApplicationContainer);
 	    mApplicationContainer = null;
 	}
 	
 	@Override
 	public void onBackPressed() {
 		if (mApplicationContainer.isApplicationView()) {
 			finish();
 		} else {
 			mApplicationContainer.scrollToApplicationView();
 		}
 		return;
 	}
 
 	private void unbindDrawables(View view) { 
 		if (view.getBackground() != null) {
 			view.getBackground().setCallback(null);
 		}
 		if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
 			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
 				unbindDrawables(((ViewGroup) view).getChildAt(i));
 			}
 			((ViewGroup) view).removeAllViews();
 		}
 	}
 	
 	public void initializeAdapters() {
 		mMenuItemList = (ListView) findViewById(R.id.menu_item_list);
 		
 		Set<String> currentMenuCategories = mMenuInformation.getCategoriesForMenu(mCurrentMenuType);
 		
 		MenuCategoryAdapter headerAdapter = new MenuCategoryAdapter(this, R.layout.menu_list_header, R.id.list_header_title, null);
 		SeparatedListAdapter menuAdapter = new SeparatedListAdapter(this, headerAdapter);
 		
 		for (String category : currentMenuCategories) {
 			MenuItemListAdapter menuItemListAdapter = 
 					new MenuItemListAdapter(this, R.layout.menu_list_item, R.id.menu_item_name, null);
 			menuItemListAdapter.setMenuItems(mMenuInformation.getMenuItemsForCategory(mCurrentMenuType, category));
 			
 			menuAdapter.addSection(category, menuItemListAdapter);
 		}
 		
 		mMenuItemList.setAdapter(menuAdapter);
 	}
 	
 	public void initializeMenuTypeSelector() {
 		LinearLayout menuTypesContainer = (LinearLayout) findViewById(R.id.menu_types);
 		
 		Set<String> menuTypes = mMenuInformation.getMenuTypes();
 		
 		
 		int menuTypeWidth = (int) (150 * getResources().getDisplayMetrics().density);
 	    int menuTypeHeight = (int) (50 * getResources().getDisplayMetrics().density);
 	    
 		for (String menuType : menuTypes) {
 			TextView menuTypeTextView = new TextView(this);
 			Drawable menuTypeDrawable = getResources().getDrawable(R.drawable.menu_types_container);
 			ColorStateList menuTypeTextColor = getResources().getColorStateList(R.color.menu_type_button_text);
 			
 			menuTypeTextView.setText(menuType);
 			menuTypeTextView.setBackgroundDrawable(menuTypeDrawable);
 			menuTypeTextView.setTextColor(menuTypeTextColor);
 			
 			menuTypeTextView.setLayoutParams(new LinearLayout.LayoutParams(menuTypeWidth, menuTypeHeight));
 			menuTypeTextView.setGravity(Gravity.CENTER);
 			
 			menuTypeTextView.setOnClickListener(new MenuTypeOnClickListener());
 			
 			menuTypesContainer.addView(menuTypeTextView);
 		}
 		
 		RelativeLayout blank = new RelativeLayout(this);
 		blank.setLayoutParams(new LinearLayout.LayoutParams(menuTypeWidth, menuTypeHeight));
 		Drawable menuTypeDrawable = getResources().getDrawable(R.drawable.menu_types_container);
 		blank.setBackgroundDrawable(menuTypeDrawable);
 		menuTypesContainer.addView(blank);
 	}
 	
 	private class MenuTypeOnClickListener implements OnClickListener {
 
 		@Override
 		public void onClick(View view) {
 			mCurrentMenuType = (String) ((TextView) view).getText();
 			initializeAdapters();
 		}
 		
 	}
 
 	public void setMenuInformation(MenuInformation menuInformation) {
 		mMenuInformation = menuInformation;
 	}
 	
 	public void setCurrentMenuType(String currentMenuType) {
 		mCurrentMenuType = currentMenuType;
 	}
 
 	public UiMenuHorizontalScrollView getApplicationContainer() {
 		return mApplicationContainer;
 	}
 	
 	public void setApplicationContainer(UiMenuHorizontalScrollView applicationContainer) {
 		mApplicationContainer = applicationContainer;
 	}
 	
 	private void setSplashScreen() {
 		UiMenuHorizontalScrollView splashContainer = 
 				ShellUtil.initializeApplicationContainer(this,
 						R.layout.ui_navigation_menu,
 						R.layout.ui_empty_action_menu,
 						R.layout.ui_splash_screen);
 		TextView splashText = (TextView) splashContainer.findViewById(R.id.splash_text);
 		splashText.setText("Downloading information...");
 		setContentView(splashContainer);
 	}
 }
