 /**
  * @Title: GridViewActivity.java
  * @Package: com.android.cb
  * @Author: Raiden Awkward<raiden.ht@gmail.com>
  * @Date: 2012-3-29
  */
 package com.android.cb;
 
 import com.android.cb.source.CBDishesScanner;
 import com.android.cb.source.CBOrderFactory;
 import com.android.cb.source.CBSettings;
 import com.android.cb.support.CBDish;
 import com.android.cb.support.CBIFOrderHandler;
 import com.android.cb.support.CBId;
 import com.android.cb.support.CBMenuEngine;
 import com.android.cb.support.CBMenuItem;
 import com.android.cb.support.CBMenuItemsSet;
 import com.android.cb.support.CBOrder;
 import com.android.cb.support.CBTagsSet;
 import com.android.cb.view.CBButton;
 import com.android.cb.view.CBButtonsGroup;
 import com.android.cb.view.CBDialogButton;
 import com.android.cb.view.GridMenuView;
 import com.android.cb.view.LaunchingDialog;
 import com.android.cb.view.OrderedDialog;
 import com.android.cb.view.OrderingDialog;
 import com.android.cb.view.PreviewDialog;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.Toast;
 
 /**
  * @author raiden
  *
  * @Description grid style activity
  */
 public class GridViewActivity extends Activity implements CBButtonsGroup.Callback,
 	LaunchingDialog.Callback,
 	OrderingDialog.Callback,
 	OrderedDialog.Callback,
 	CBIFOrderHandler,
 	GridMenuView.Callback {
 
 	public static final String INTENT_ORDER_RECORD_PATH = "intent.order.record.path";
 	public static final String INTENT_ORDER_LOCATION = "intent.order.location";
 
 	private GridMenuView mGridView;
 	private CBButtonsGroup mButtonsGruop;
 	private CBDialogButton mButtonOrdered;
 
 	private CBMenuEngine mMenuEngine;
 	private CBTagsSet mTagButtonTags = null;
 	private LaunchingDialog mLaunchingDialog = null;
 	private boolean mIsInitDone = false;
 
 
 	public GridViewActivity() {
 		super();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.main_grid_menu_view);
 		super.onCreate(savedInstanceState);
 
 		InitAsyncTask initTask = new InitAsyncTask();
 		initTask.execute((Object)null);
 	}
 
 	private void initMenuEngine() {
 		CBDishesScanner scanner = new CBDishesScanner(CBSettings.getStringValue(CBSettings.CB_SETTINGS_SOURCE_DIR_DISHES));
 		CBMenuItemsSet set = scanner.scan();
 
 		mMenuEngine = new CBMenuEngine();
 		mMenuEngine.setMenuSet(set);
 
 		Bundle bundle = this.getIntent().getExtras();
 		String orderRecordPath = (bundle == null ? null : bundle.getString(INTENT_ORDER_RECORD_PATH));
 		if (orderRecordPath != null) {
 			if (this.loadOrderRecord(orderRecordPath) == false) {
 				Toast.makeText(this, R.string.gridview_activity_load_order_failed, 0).show();
 				finish();
 			}
 		} else {
 			this.createOrder();
 			String orderLocation = (bundle == null ? null : bundle.getString(INTENT_ORDER_LOCATION));
 			mMenuEngine.getOrder().setLocation(orderLocation);
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private void initTestingMenuEngine() {
 		CBMenuItemsSet set = new CBMenuItemsSet();
 		for (int i = 1; i <= 242; ++i) {
 			CBId id = new CBId();
 			id.setId(String.valueOf(i));
 
 			CBDish dish = new CBDish();
 			String image = "/sdcard/image/img" + String.valueOf(i) + ".jpg";
 			dish.setPicture(image);
 			dish.setThumb(image);
 			dish.setId(id);
 			dish.setName("img" + String.valueOf(i));
 			if (i % 2 == 0) {
 				dish.addTag("even");
 			} else {
 				dish.addTag("odd");
 			}
 			dish.addTag("dish");
 
 			CBMenuItem item = new CBMenuItem();
 			item.setDish(dish);
 			set.add(item);
 		}
 
 		CBOrder order = new CBOrder();
 		mMenuEngine = new CBMenuEngine();
 		mMenuEngine.setMenuSet(set);
 		mMenuEngine.setOrder(order);
 	}
 
 	private void initButtons() {
 		mButtonsGruop = (CBButtonsGroup) this.findViewById(R.id.buttonsGroup);
 		mButtonsGruop.setCallback(this);
 		mButtonsGruop.setButtonsMargins(5);
 
 		mTagButtonTags = CBSettings.getLeftButtonTagsSet();
 		if (mTagButtonTags == null)
 			mTagButtonTags = mMenuEngine.getContainedTags();
 
 		for (int i = 0; i < mTagButtonTags.count(); ++i) {
 			CBButton button = new CBButton(mButtonsGruop.getContext(), R.drawable.button_orange, R.drawable.button_gray);
 			button.setText(mTagButtonTags.get(i));
 			button.setTextSize(CBSettings.getIntValue(CBSettings.CB_SETTINGS_LEFT_BUTTON_TEXT_SIZE));
 			mButtonsGruop.addButton(button);
 		}
 
 		mButtonOrdered = (CBDialogButton) this.findViewById(R.id.botton_OrderList);
 		mButtonOrdered.setTextSize(CBSettings.getIntValue(CBSettings.CB_SETTINGS_LEFT_BUTTON_TEXT_SIZE));
 		mButtonOrdered.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View arg0) {
 				showOrderedDialog();
 			}
 		});
 		updateOrderingButtonStatus();
 	}
 
 	private void initGridMenuView() {
 		mGridView = (GridMenuView) this.findViewById(R.id.gridMenuView);
 		mGridView.setCallback(this);
 
 		if (mTagButtonTags.count() > 0) {
 			mGridView.setMenuItemSet(mMenuEngine.getMenuItemsSetWithTag(mTagButtonTags.get(0)));
 		} else {
 			mGridView.setMenuItemSet(mMenuEngine.getMenuSet());
 		}
 	}
 
 	public void onButtonInGroupClicked(int index) {
 		String tagSelected = mTagButtonTags.get(index);
 		mGridView.setMenuItemSet(mMenuEngine.getMenuItemsSetWithTag(tagSelected));
 	}
 
 	private void showLaunchingDialog() {
 		if (mLaunchingDialog == null)
 			mLaunchingDialog = new LaunchingDialog(this);
 		mLaunchingDialog.setCallback(this);
 		mLaunchingDialog.show();
 		mLaunchingDialog.start();
 	}
 
 	public boolean onLaunchingTick() {
 		return !mIsInitDone;
 	}
 
 	private static String sLaunchingText = "";
 	public String getCurrentLaunchingText() {
 		String text = this.getResources().getString(R.string.launching_text);
 
 		sLaunchingText += ".";
 		if (sLaunchingText.length() > 3)
 			sLaunchingText = ".";
 
 		return text + sLaunchingText;
 	}
 
 	public void onItemClicked(CBMenuItem item) {
 		PreviewDialog dialog = new PreviewDialog(this);
 		dialog.setMenuItem(item);
 		dialog.setOrderHandler(this);
 		dialog.show();
 	}
 
 	public boolean onItemLongPressed(CBMenuItem item) {
 		OrderingDialog dialog = new OrderingDialog(this);
 		dialog.setCallback(this);
 		dialog.setOrderHandler(this);
 		dialog.setMenuItem(item);
 		dialog.show();
 		return false;
 	}
 
 	private class InitAsyncTask extends AsyncTask<Object, Integer, Boolean> {
 
 		@Override
 		protected Boolean doInBackground(Object... params) {
 			// fake delay for engine initializing
 //			try {
 //				Thread.sleep(5000);
 //			} catch (InterruptedException e) {
 //				e.printStackTrace();
 //			}
 
 			initMenuEngine();
 			return Boolean.TRUE;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			initButtons();
 			initGridMenuView();
 
 			mIsInitDone = true;
 			mLaunchingDialog.launchingDone();
 
 			super.onPostExecute(result);
 		}
 
 		@Override
 		protected void onPreExecute() {
 			showLaunchingDialog();
 			super.onPreExecute();
 		}
 
 	} // InitAsyncTask
 
 	public boolean createOrder() {
 		CBOrder order = CBOrderFactory.newOrder();
 		mMenuEngine.setOrder(order);
 		return true;
 	}
 
 	public boolean loadOrderRecord(CBOrder order) {
 		if (order == null)
 			return false;
 
 		mMenuEngine.setOrder(order);
 		return true;
 	}
 
 	public boolean loadOrderRecord(String recordPath) {
 		CBOrder order = CBOrderFactory.loadOrder(recordPath, mMenuEngine);
 		return loadOrderRecord(order);
 	}
 
 	public boolean saveOrderRecord() {
 		CBOrder order = mMenuEngine.getOrder();
 		return CBOrderFactory.saveOrder(order);
 	}
 
 	public boolean addItemToOrder(CBMenuItem item, int count) {
 		if (item == null || count <= 0)
 			return false;
 
 		int index = mMenuEngine.getMenuItemIndex(item);
 		if (index < 0)
 			return false;
 
 		boolean res = mMenuEngine.orderIndexedItem(index, count);
 		if (res == true)
 			updateOrderingButtonStatus();
 		return res;
 	}
 
 	public boolean removeItemFromOrder(CBMenuItem item) {
 		int index = mMenuEngine.getMenuItemIndex(item);
 		boolean res = mMenuEngine.disOrderIndexedItem(index);
 		if (res == true)
 			updateOrderingButtonStatus();
 		return res;
 	}
 
 	public boolean removeItemFromOrder(int index) {
 		boolean res = mMenuEngine.disOrderIndexedItem(index);
 		if (res == true)
 			updateOrderingButtonStatus();
 		return res;
 	}
 
 	public int getItemOrederedCount(CBMenuItem item) {
 		int index = mMenuEngine.getMenuItemIndex(item);
 		return mMenuEngine.getIndexedItemCheckedCount(index);
 	}
 
 	public void onItemAddedToOrder(boolean succeed) {
 		int stringId = (succeed ? R.string.ordering_adding_succeed : R.string.ordering_adding_failed);
 		String str = this.getResources().getString(stringId);
 		Toast.makeText(this, str, 0).show();
 	}
 
 	public void onItemDeletedFromOrder(boolean succeed) {
 		int stringId = (succeed ? R.string.ordering_deleting_succeed : R.string.ordering_deleting_falied);
 		String str = this.getResources().getString(stringId);
 		Toast.makeText(this, str, 0).show();
 	}
 
 	public boolean onItemDeletingFromOrder(CBMenuItem item) {
 		return true;
 	}
 
 	public void updateOrderingButtonStatus() {
 		String buttonText = this.getResources().getString(R.string.ordered_myOrder_button_text);
 		int orderingCount = mMenuEngine.getTotalItemCheckedCount();
 		if (orderingCount > 0)
 			buttonText += " (" + orderingCount + ")";
 
 		mButtonOrdered.setText(buttonText);
 	}
 
 	public void showOrderedDialog() {
 		OrderedDialog dialog = new OrderedDialog(this);
 		dialog.setOrder(mMenuEngine.getOrder());
 		dialog.setOrderHandler(this);
 		dialog.setCallback(this);
 		dialog.show();
 	}
 
 	public void onOrderSubmitted() {
 		this.finish();
 	}
 
 }
