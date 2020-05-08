 package com.android.cb;
 
 import com.android.cb.source.CBSettings;
 import com.android.cb.view.CBDialogButton;
 
 import android.app.Activity;
 import android.content.Intent;
 
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 
 public class CBActivity extends Activity {
 
 	private CBDialogButton mButtonNewOrder;
 	private CBDialogButton mButtonOrdersList;
 	private CBDialogButton mButtonQuit;
 
 	public CBActivity() {
 		super();
 
 		CBSettings.load();
 	}
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_manage);
 
 		mButtonNewOrder = (CBDialogButton) this.findViewById(R.id.button_newOrder);
 		mButtonNewOrder.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				openGridViewActivity();
 			}
 		});
 
 		mButtonOrdersList = (CBDialogButton) this.findViewById(R.id.button_ordersList);
 		mButtonOrdersList.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				openGridViewActivity(null);
 			}
 		});
 
 		mButtonQuit = (CBDialogButton) this.findViewById(R.id.button_quit);
 		mButtonQuit.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				finish();
 			}
 		});
     }
 
 	private void openGridViewActivity() {
 		Intent intent = new Intent();
 		intent.setClass(CBActivity.this, GridViewActivity.class);
 		CBActivity.this.startActivity(intent);
     }
 
 	private void openGridViewActivity(String orderRecordPath) {
 		if (orderRecordPath == null) {
 			openGridViewActivity();
 			return;
 		}
 
 		Intent intent = new Intent();
 		intent.setClass(CBActivity.this, GridViewActivity.class);
 		intent.putExtra(GridViewActivity.INTENT_ORDER_RECORD_PATH, orderRecordPath);
 
 		CBActivity.this.startActivity(intent);
     }
 
 	@Override
 	protected void onDestroy() {
		CBSettings.save();

 		super.onDestroy();
 	}
 
 //	private void testDB() {
 //		File dbDir = getDir("db", Context.MODE_PRIVATE);
 //		dbDir.mkdirs();
 //		File dbFile = new File(dbDir, "cbdb.sqllite");
 //		Log.d("-DB-", "DB created on " + dbFile.getAbsolutePath());
 //		CBDB db = new CBDB(dbFile.getAbsolutePath());
 //
 ////		db.addMenuItemsToDB(mMenuEngine.getMenuSet());
 //
 //		CBMenuItemsSet set = db.loadMenuItemsSet();
 //		for (int i = 0; i < set.count(); ++i) {
 //			Log.d("-DB-", "item loaded: " + set.get(i).getDish().getPicture());
 //		}
 //
 //		db.close();
 //	}
 
 }
