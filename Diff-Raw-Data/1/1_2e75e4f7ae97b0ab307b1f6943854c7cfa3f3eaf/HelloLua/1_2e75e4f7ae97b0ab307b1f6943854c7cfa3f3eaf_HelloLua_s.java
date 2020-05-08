 /****************************************************************************
 Copyright (c) 2010-2012 cocos2d-x.org
 
 http://www.cocos2d-x.org
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/
 package org.cocos2dx.hellolua;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.cocos2dx.lib.Cocos2dxActivity;
 import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.LinearLayout;
 
 public class HelloLua extends Cocos2dxActivity{
 	private MyHandler myHandler;
 	LinearLayout layout;
 	AdView view;
 	AdRequest request;
 	
 	private class MyHandler extends Handler{
 		public HelloLua h;
 		
 		public void handleMessage(Message msg) {
 			//h.view.stopLoading();
 			//h.layout.setVisibility(View.INVISIBLE);
 			request = new AdRequest();
 			view.loadAd(request);
 		}
 		public void setHelloLua(HelloLua hh) {
 			h = hh;
 		}
 	}
 	
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		myHandler = new MyHandler();
 		myHandler.setHelloLua(this);
 		setUpAds();
 		showBanner();
 	}
 	private void showBanner(){
 		layout.setVisibility(View.VISIBLE);
 		request = new AdRequest();
 		LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
 		Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		LocationListener ll = new LocationListener() {
 
 			@Override
 			public void onLocationChanged(Location arg0) {
 				// TODO Auto-generated method stub
 				request.setLocation(arg0);
 			}
 
 			@Override
 			public void onProviderDisabled(String arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onProviderEnabled(String arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		};
 		//lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
 		request.setLocation(location);
 		view.loadAd(request);
 		
 		
 		Timer timer = new Timer();
 		
 		timer.schedule(new TimerTask() {
 
 			@Override
 			public void run() {
 				//Log.d("helloLua", "sendMessage");
 				// TODO Auto-generated method stub
 				Boolean ret = myHandler.sendMessage(new Message());
 				Log.d("sendSuc?", ""+ret);
 			}
 			
 		}
 		, 15000, 20000);
 		
 	}
 	private void setUpAds() {
 		layout = new LinearLayout(this);
 		layout.setOrientation(LinearLayout.HORIZONTAL);
 		addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 		view = new AdView(this, AdSize.BANNER, "a151d75e797b3d8");
 		layout.addView(view);
 	}
 	
 	public Cocos2dxGLSurfaceView onCreateGLSurfaceView() {
 		return new LuaGLSurfaceView(this);
 	}
 	
 	static {
         System.loadLibrary("hellolua");
    }
 }
 
 class LuaGLSurfaceView extends Cocos2dxGLSurfaceView{
 	
 	public LuaGLSurfaceView(Context context){
 		super(context);
 	}
 	
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
     	// exit program when key back is entered
     	if (keyCode == KeyEvent.KEYCODE_BACK) {
     		android.os.Process.killProcess(android.os.Process.myPid());
     	}
         return super.onKeyDown(keyCode, event);
     }
 }
