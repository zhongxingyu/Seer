 /* Copyright (c) 2013 All Right Reserved Steven T. Ramzel
  *
  *	This file is part of Overplayed.
  *
  *	Overplayed is free software: you can redistribute it and/or modify
  *	it under the terms of the GNU Lesser General Public License as published by
  *	the Free Software Foundation, either version 3 of the License, or
  *	(at your option) any later version.
  *
  *	Overplayed is distributed in the hope that it will be useful,
  *	but WITHOUT ANY WARRANTY; without even the implied warranty of
  *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *	GNU Lesser General Public License for more details.
  *
  *	You should have received a copy of the GNU Lesser General Public License
  *	along with Overplayed.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.gphrost.Overplayed;
 
 import java.io.File;
 
 import org.gphrost.Overplayed.Controller.Controller;
 import org.gphrost.Overplayed.Menu.MenuActivity;
 import org.gphrost.Overplayed.Menu.MenuButtonLinearLayout;
 
 import com.gphrost.Overplayed.R;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.PixelFormat;
 import android.os.Binder;
 import android.os.IBinder;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.view.WindowManager.LayoutParams;
 
 /**
  * Service used to manage GameControllerViews and the NetworkThread.
  * 
  * @author Steven T. Ramzel
  */
 public class MainService extends Service {
 	public static MainService staticThis;
 	public static Controller controller;
 	static final String EXTRA_ADDRESS = "EXTRA_ADDRESS";
 	// GameControllerViews
 	public static final String EXTRA_PORT = "EXTRA_PORT";
 	static Intent intent; // Intent used to call this service
 	static NetworkThread thread; // Thread for network routine to run on
 	// Binder used to bind to main activity
 	private final IBinder mBinder = new LocalBinder();
 	public static MenuButtonLinearLayout menuButton;
 	public static LayoutParams menuParams;
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public IBinder onBind(Intent intent) {
 		MainService.intent = intent;
 
 		// Create notification to call startForegroud()
 		Notification note = new Notification(R.drawable.overplayed_logo,
 				"Overplayed is running ...", System.currentTimeMillis());
 
		Intent i = new Intent(this,Overplayed.class);
 		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);//.getService(this, 0, intent, 0);
 
 		note.setLatestEventInfo(this, "Overplayed", "Overplayed is running", pi);
 
 		// Notification stays until app is done
 		note.flags |= Notification.FLAG_NO_CLEAR;
 
 		// Start the service in the foreground so it stays until manually closed
 		startForeground(1, note);
 		return mBinder;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		staticThis = this;
 		File file = new File(MainService.staticThis.getExternalFilesDir(null), Preferences.Load.defaultController(this) + ".xml");
 		controller = new Controller(this, file);
 	}
 
 	@Override
 	public void onDestroy() {
 		// Stop the NetworkThread
 		thread.running = false;
 		try {
 			thread.join();
 			thread = null;
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 		//controller.wm.removeView(menuButton);
 		controller.detach();
 		controller.wm = null;
 		controller = null;
 		menuButton = null;
 		// This tells UDPlay whether or not this service is running
 		Overplayed.mService = null;
 		super.onDestroy();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		// Extract the network address from the intent
 		String address = intent.getStringExtra(EXTRA_ADDRESS);
 		int port = intent.getIntExtra(EXTRA_PORT, 30000);
 
 		// If the thread isn't running, RUN IT!!!
 		if (thread == null) {
 			thread = new NetworkThread(address, port, controller);
 			thread.running = true;
 			thread.start();
 			// Let the GameControllerViews know about it so they can interact
 			// with it
 			controller.thread = thread;
 		}
 
 		// This means the service was not already running, otherwise don't touch
 		// anything
 		if (controller.wm == null) {
 			controller.attach((WindowManager) getSystemService(WINDOW_SERVICE),
 					this);
 			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			menuParams = new WindowManager.LayoutParams(
 					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
 					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
 					// Go on top of everything, I would do TYPE_SYSTEM_ALERT but
 					// it
 					// messes with the notification bar.
 					WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
 					// Let touch events pass to other apps
 					WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
 					// Let the background of controls be transparent
 					PixelFormat.TRANSLUCENT);
 			menuParams.gravity = Gravity.TOP;
 
 			menuButton = ((MenuButtonLinearLayout) inflater.inflate(
 					R.layout.hidebutton, null));
 			menuParams.alpha = .5f;
 			menuParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
 
 			menuButton.findViewById(R.id.button1).setOnClickListener(
 					new OnClickListener() {
 						public void onClick(View arg0) {
 							// TODO Auto-generated method stub
 							if (controller.active) {
 								controller.setInactive();
 								menuButton.active = false;
 								controller.wm.updateViewLayout(menuButton,
 										menuParams);
 							} else {
 								controller.setActive(MainService.this);
 								menuButton.active = true;
 								controller.wm.updateViewLayout(menuButton,
 										menuParams);
 							}
 						}
 					});
 			menuButton.findViewById(R.id.button2).setOnClickListener(
 					new OnClickListener() {
 						public void onClick(View arg0) {
 							Intent i = new Intent(MainService.this,
 									MenuActivity.class);
 							i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 							startActivity(i);
 							controller.wm.removeView(MainService.menuButton);
 							controller.detach();
 						}
 					});
 			((WindowManager) getSystemService(WINDOW_SERVICE)).addView(menuButton, menuParams);
 		}
 		return Service.START_NOT_STICKY;// When we press quit, the service
 										// should die
 	}
 
 	public class LocalBinder extends Binder {
 	    MainService getService() {
 	        return MainService.this;
 	    }
 
 	}
 }
