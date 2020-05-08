 package com.ehb.questiontime;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.ActivityGroup;
 import android.app.LocalActivityManager;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Window;
 
 public class TabGroupActivity extends ActivityGroup {
 	private ArrayList<String> mIdList;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 
 		if (mIdList == null)
 			mIdList = new ArrayList<String>();
 	}
 
 	@Override
 	public void finishFromChild(Activity child) {
 		LocalActivityManager manager = getLocalActivityManager();
 		int index = mIdList.size() - 1;
 		if (index < 1) {
 			finish();
 			return;
 		}
 		manager.destroyActivity(mIdList.get(index), true);
 		mIdList.remove(index);
 		index--;
 		String lastId = mIdList.get(index);
 		Intent lastIntent = manager.getActivity(lastId).getIntent();
 		Window newWindow = manager.startActivity(lastId, lastIntent);
 		setContentView(newWindow.getDecorView());
 
 	}
 
 	public void startChildActivity(String Id, Intent intent) {
 		Window window = getLocalActivityManager().startActivity(Id,
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
 		if (window != null) {
 			mIdList.add(Id);
 			setContentView(window.getDecorView());
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
		TabGroupActivity parentActivity = (TabGroupActivity) getParent();
		parentActivity.onBackPressed();
 
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			onBackPressed();
 			return true;
 		}
 		return super.onKeyUp(keyCode, event);
 	}
 
 }
