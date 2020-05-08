 /*
 Copyright (c) 2012 Ronald Tsang, ronaldtsang@orochis-den.com
 
 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:
 
 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package orochi.nativeadapter;
 
 import java.util.Hashtable;
 
 import orochi.util.ActivityForResultObject;
 import orochi.util.FileUtils;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.Environment;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 public class NativeService extends Service {
 	
 	public static String serviceName = "Orochi";
 	public static String[] allowIPs = {"/127.0.0.1", "*"};
 	public static int portTryLimit = 20;
 	
 	private Class<RequestHandler>[] requestHandlers;
 	private NativeEngine nativeEngine = null;
 	private Hashtable<String, OrochiActivity> activities = new Hashtable<String, OrochiActivity>();
 	private NativeService me = this;
 	private boolean engineRunning = false;	
 	
 	
 	
 	public int port = 8181; //default port number to start the NativeService
 	//Application's default directory
 	public String appDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Orochi";		
 	public Class<OrochiActivity> mainActivityClass = null;
 	
 	private final NativeIBinder nativeIBinder = new NativeIBinder();
 	
 	public Hashtable<String, OrochiActivity> getActivities(){
 		return activities;
 	}
 	
 	public void setRequestHandlers(Class<RequestHandler>[] requestHandlers){
 		this.requestHandlers = requestHandlers;
 	}
 	
 	//validate ip address
 	public static boolean isValidIP(String _ip){
 		for (String ip : allowIPs)
 			if(_ip.equals(ip) || ip.equals("*"))
 				return true;
 		return false;
 	}
 	
 	@Override
 	public void onCreate(){
 		super.onCreate();
 		FileUtils.mkdirIfNeeded(appDirectory);
 		Log.d("Orochi", "NativeService: onCreate()");
 	}
 	
 	@Override
 	public void onStart(Intent intent, int startId){
 		super.onStart(intent, startId);
 		start();
 		Log.d("Orochi", "NativeService: onStart()");
 	}
 
 	@Override
     public void onDestroy() {  
 		super.onDestroy();		
 		stop();
 		Log.d("Orochi", "NativeService: onDestroy()");
 		
     }  	
 	
 	//start the NativeService's Engine
 	private void start(){
 		if(!engineRunning){
 			int createTry = 0;
 			while(nativeEngine==null && createTry<portTryLimit){
 				nativeEngine = NativeEngine.create(this, requestHandlers, port+(createTry++));
 			}
 			if(nativeEngine==null){
 		        Log.d("Orochi", "NativeService: port "+port+"-"+(port+createTry-1)+" already in use");
 				Toast.makeText(this, "Native Service start fail: port "+port+"-"+(port+createTry-1)+" already in use", Toast.LENGTH_SHORT).show();				
 			}
 	        engineRunning = true;
 	        Log.d("Orochi", "NativeService: Running");
 			//Toast.makeText(this, "Native Service On", Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	//stop the NativeService's Engine
 	public void stop(){
 		if(engineRunning){
 			nativeEngine.kill();
			nativeEngine = null;
 			engineRunning = false;
 			Log.d("Orochi", "NativeService: Stopped");
 			//Toast.makeText(this, "Native Service Off", Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	//weather the NativeService's Engine is running
 	public boolean isRunning(){
 		return engineRunning;
 	}
 	
 	//a mirror function of nativeEngine.addActForResult()
 	public void addActForResult(ActivityForResultObject actForResObj){
 		nativeEngine.addActForResult(actForResObj);
 	}
 	
 	//a mirror function of nativeEngine.handleOnActResult()
     public void handleOnActResult(int requestCode, int resultCode, Intent data) {
     	nativeEngine.handleOnActResult(requestCode, resultCode, data);
     }
     
 	@Override
 	public IBinder onBind(Intent intent) {		
 		return nativeIBinder;
 	}    
 	
     public class NativeIBinder extends Binder{
     	
     	public NativeService getService(){
     		return me;
     	}
     }    
     
 
 }
