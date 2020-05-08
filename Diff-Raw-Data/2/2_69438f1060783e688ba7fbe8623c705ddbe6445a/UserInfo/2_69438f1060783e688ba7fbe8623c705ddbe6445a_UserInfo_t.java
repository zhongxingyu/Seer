 package com.example.sea_game_testing;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.android_usb_test.testUSB;
 import com.example.gameData.Stage;
 import com.example.sea_game_testing.util.DeviceUtil;
 import com.example.sea_game_testing.util.Util;
 import com.stage.data.StageItem;
 
 public class UserInfo extends Activity {
 	TextView info = null;
 	ImageView img = null;
 	GridView list = null;
 	Button device = null;
 	public static ArrayList<StageItem> item = null; 
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.user_info);
 		init();
 		USBInit();
 		if (getIntent().getExtras() != null) {
 			if (
 					getIntent().getExtras().getString("user") != null && 
 					!getIntent().getExtras().getString("user").equals("")) 
 			{
 				Util.user = getIntent().getExtras().getString("user");
 				
 			} 
 			
 		}
 		checkDeviceStatus();
 	}
 	
 	private void checkDeviceStatus() {
 		if ( DeviceUtil.USB.getDeviceList() != null && DeviceUtil.USB.getDeviceList().size() > 0 ) {
 //			String str = String.format(
 //					getResources().getString(R.string.device_number).toString() , 
 //					""+DeviceUtil.USB.getDeviceList().size()
 //					);
 			Toast.makeText( this,  "finish!", Toast.LENGTH_SHORT).show();
 //			DeviceUtil.USB.deviceStart();
 			device.setBackgroundResource(R.drawable.connection_ok);
 		}else {
 			device.setBackgroundResource(R.drawable.connection_no);
 			Toast.makeText( this,  "No Devices" + (DeviceUtil.USB.getDeviceList() != null), Toast.LENGTH_SHORT).show();
 		}
 	}
 	public void checkZB( View v ) {
 		checkDeviceStatus();
 	}
 	
 	private void init() {
 		Util.AddId(android.os.Process.myPid());
 		info = (TextView) findViewById(R.id.info);
 		img = (ImageView) findViewById(R.id.img);
 		list = (GridView) findViewById(R.id.list);
 		device = (Button) findViewById(R.id.device);
 		String str = String.format(getResources().getString(R.string.info),
 				Util.user, Util.sex, "20", "0");
 		info.setText(str);
 		if ( item == null ) {
 			item = new ArrayList<StageItem>();
 			item.add(new StageItem(0 ,20));
 			item.add(new StageItem(1,30));
 			item.add(new StageItem(2,80));
 			item.add(new StageItem(3,0));
 			item.add(new StageItem(4,0));
 			item.add(new StageItem(5,0));
 		}
 		list.setAdapter(new ListData(this, item));
 		list.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// playGame();
 				Intent i = new Intent();
 				i.setClass(UserInfo.this, Game.class);
 				i.putExtra("stage", arg2);
 
 				startActivity( i );
 				finish();
 			}
 		});
 	}
 
 	private void USBInit() {
 		if (DeviceUtil.USB == null) {
 			DeviceUtil.USB = new testUSB(this, Util.VID, Util.PID);
 			DeviceUtil.USB.connect();
 		}
 		
 	}
 	private void playGame() {
 		Intent i = new Intent();
 		i.setClass(this, Game.class);
 		
 		// touch play when check a stage lose		
 		int stage = 0;
 		if ( item != null ) {
 			for ( int x = stage; x < item.size(); x++ ) {
 				if ( item.get(x).score > 60) {
 					stage = item.get(x).stage;
 				}
 			}
 		}
 		
 		i.putExtra("stage", stage);
 		// i.setFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP );
 		// //如果這Activity是開啟的就不再重複開啟
 		i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 		startActivity(i);
 		finish();
 	}
 
 	public void play(View v) {
 		playGame();
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			this.finish();
 			Util.closeGame();
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 
 
 	class ListData extends BaseAdapter {
 		ArrayList<StageItem> item = null;
 //		TextView text = null;
 		Context c = null;
 
 		ListData(Context c, ArrayList<StageItem> item) {
 			this.c = c;
 			this.item = item;
 		}
 
 		@Override
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return this.item.size();
 		}
 
 		@Override
 		public Stage getItem(int arg0) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = new TextView(c);
 			}
 
			if (item.get( position ).score <= 0)
 				((TextView) convertView).setBackgroundResource(R.drawable.clam_2);
 			else {
 				((TextView) convertView).setBackgroundResource(R.drawable.clam_1);
 //				((ImageView) convertView).setImageResource(R.drawable.pearl_1);
 			}
 
 			 ((TextView) convertView).setGravity( Gravity.CENTER);
 			// ((TextView) convertView).setLayoutParams( new
 			// AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT,
 			// LayoutParams.WRAP_CONTENT));
 			// ((TextView) convertView).setWidth(80);
 
 			 ((TextView) convertView).setText( ""+ item.get(position).score);
 			 ((TextView) convertView).setTextSize(35);
 			 ((TextView) convertView).setTextColor(Color.CYAN);
 			return convertView;
 		}
 
 	}
 }
