 package com.simpleminds.popbell;
 
 import wei.mark.standout.StandOutWindow;
 import wei.mark.standout.StandOutWindow.StandOutLayoutParams;
 import wei.mark.standout.constants.StandOutFlags;
 import wei.mark.standout.ui.Window;
 import android.app.Instrumentation;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 
 public class SimpleWindow extends StandOutWindow {
 	
 	String[] array;
 	
 	
 	@Override
 	public String getAppName() {
 		return "SimpleWindow";
 	}
 
 	@Override
 	public int getAppIcon() {
 		return android.R.drawable.ic_menu_close_clear_cancel;
 	}
 
 	@Override
 	public void createAndAttachView(int id, FrameLayout frame) {
 		
 		// create a new layout from body.xml
 		LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
 		View view = inflater.inflate(R.layout.simple, frame, true);
 		
 	
 		/*Button back = (Button)view.findViewById(R.id.button1);
 		back.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				new Thread(new Runnable() {         
 					public void run() {                 
 						new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_UP);
 					}   
 				}).start();
 			}
 		});*/
 		
 		
 	}
 	
 	
 	// the window will be centered
 	@Override
 	public StandOutLayoutParams getParams(int id, Window window) {
 		/*return new StandOutLayoutParams(id, 200, 200,
 				StandOutLayoutParams.CENTER, StandOutLayoutParams.CENTER);*/
 		SharedPreferences pref = getSharedPreferences("display", 0);
         array = pref.getString("xy", "0/0").split("/");
 		return new StandOutLayoutParams(id, Integer.parseInt(array[0])*7/8, Integer.parseInt(array[1])/8,
 				  StandOutLayoutParams.CENTER, StandOutLayoutParams.TOP + 20);
 	}
 
 	// move the window by dragging the view
 	@Override
 	public int getFlags(int id) {
 		return super.getFlags(id) /*| StandOutFlags.FLAG_BODY_MOVE_ENABLE*/ | StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE;
 	}
 
 	@Override
 	public String getPersistentNotificationMessage(int id) {
 		return "Click to close the SimpleWindow";
 	}
 
 	@Override
 	public Intent getPersistentNotificationIntent(int id) {
 		return StandOutWindow.getCloseIntent(this, SimpleWindow.class, id);
 	}
 	
 	
 	@Override
 	public void onReceiveData(int id, int requestCode, Bundle data,
 			Class<? extends StandOutWindow> fromCls, int fromId) 
 	{
 		Window window = getWindow(id);
		
 			String changedText = data.getString("sysnotidata");
 			TextView status = (TextView) window.findViewById(R.id.textView2);
 			status.setTextSize(20);
 			status.setText(changedText);
		
 	}
 }
