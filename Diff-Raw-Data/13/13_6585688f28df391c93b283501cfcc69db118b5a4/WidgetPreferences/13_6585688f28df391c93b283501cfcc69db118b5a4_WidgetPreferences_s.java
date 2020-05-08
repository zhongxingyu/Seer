 package org.kvj.bravo7.widget;
 
 import org.kvj.bravo7.widget.WidgetList.ClickListener;
 import org.kvj.bravo7.widget.WidgetList.WidgetInfo;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.FrameLayout;
 import android.widget.FrameLayout.LayoutParams;
 
 public abstract class WidgetPreferences extends FragmentActivity implements
 		ClickListener {
 
 	public WidgetPreferences(WidgetList widgetList) {
 		this.widgetList = widgetList;
 	}
 
 	WidgetList widgetList = null;
 	Fragment editorFragment = null;
 
 	@Override
 	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
 		super.onActivityResult(arg0, arg1, arg2);
 		widgetList.reloadData();
 	}
 
 	@Override
 	protected void onCreate(Bundle arg0) {
 		super.onCreate(arg0);
 		FrameLayout root = new FrameLayout(this);
 		LayoutParams params = new LayoutParams(
 				FrameLayout.LayoutParams.MATCH_PARENT,
 				FrameLayout.LayoutParams.MATCH_PARENT);
 		root.setId(1);
 		setContentView(root, params);
 		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 		ft.replace(1, widgetList);
 		widgetList.setClickListener(this);
 		ft.commit();
 	}
 
 	public void click(WidgetInfo info) {
 		if (null == editorFragment) {
 			Class<? extends WidgetPreferenceActivity> configActivity = getConfigActivity(info);
 			if (null == configActivity) {
 				return;
 			}
 			Intent intent = new Intent(this, configActivity);
 			intent.putExtra("id", info.id);
 			startActivity(intent);
 		}
 	}
 
 	abstract protected Class<? extends WidgetPreferenceActivity> getConfigActivity(
 			WidgetInfo info);
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.clear();
 		menu.add(Menu.NONE, 1, 0, "Update");
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case 1: // Update
 
 			widgetList.app.updateWidgets(-1);
 			widgetList.reloadData();
 			break;
 		}
 		return true;
 	}
 
 	// public void onCreateOptionsMenu(Menu menu) {
 	// }
 	//
 	// @Override
 	// public boolean onOptionsItemSelected(MenuItem item) {
 	// }
 }
