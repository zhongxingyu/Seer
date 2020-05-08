 package com.reflexer.ui;
 
 import android.content.ComponentName;
import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.reflexer.R;
 import com.reflexer.model.RXReflex;
 import com.reflexer.service.RXBinder;
import com.reflexer.service.RXService;
 import com.reflexer.ui.adapters.RXAdapter;
 
 import java.util.ArrayList;
 
 public class RXMenuActivity extends SherlockActivity {
 
 	/**
 	 * ListView containing all the reflexes.
 	 */
 	private ListView rxList;
 
 	/**
 	 * IBinder interface for accessing RXService methods.
 	 */
 	private RXBinder serviceBinder;
 
 	private final ServiceConnection mConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder binder) {
 			Log.d("RXMenuActivity", "onServiceConnected");
 			serviceBinder = ((RXBinder) binder);
 
 			ArrayList<RXReflex> reflexes = serviceBinder.getReflexes();
 
 			RXAdapter rxAdapter = new RXAdapter(RXMenuActivity.this, reflexes);
 			rxList.setAdapter(rxAdapter);
 			rxList.setOnItemClickListener(rxOnItemClickListener);
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName className) {
 			Log.d("RXMenuActivity", "onServiceDisconnected");
 			serviceBinder = null;
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_menu);
 
 		initUI();
 	}
 
 	private void initUI() {
 		rxList = (ListView) findViewById(R.id.rx_list);
 	}
 
 	private final OnItemClickListener rxOnItemClickListener = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
 
 			Intent intent = new Intent(RXMenuActivity.this, RXCreateActivity.class);
 			intent.putExtra(RXCreateActivity.REFLEX_INDEX, index);
 			startActivity(intent);
 
 		}
 	};
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
		bindService(new Intent(this, RXService.class), mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		super.onPause();

		unbindService(mConnection);
 	}
 
 	@Override
 	public void onPanelClosed(int featureId, android.view.Menu menu) {
 		// TODO Auto-generated method stub
 		super.onPanelClosed(featureId, menu);

 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add("Add new Stimuli").setIcon(android.R.drawable.ic_input_add)
 				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
 		Toast.makeText(this, "Add new Stimuli", Toast.LENGTH_SHORT).show();
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 }
