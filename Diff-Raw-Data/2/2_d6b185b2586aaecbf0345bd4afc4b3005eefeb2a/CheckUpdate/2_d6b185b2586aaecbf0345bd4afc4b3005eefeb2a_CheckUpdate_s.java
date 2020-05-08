 package com.android.gallery3d.update;
 
 import com.android.gallery3d.update.DownThread;
 import com.android.gallery3d.app.Gallery;
 
 	
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Calendar;
 
 import android.app.PendingIntent;
 import android.app.AlarmManager;
 import android.app.Service;
 import android.app.ActivityManagerNative;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.HandlerThread;
 import android.os.RemoteException;
 import android.widget.Toast;
 import android.util.Log;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.Context;
 import android.os.Build;
 
 
 
 public class CheckUpdate extends Service {
 	private String TAG	="CheckUpdate";
 	private Context mContext;
     private String[] OdPath=new String[]{"http://www.allwinnertech.com/update/Gallery2-update-Zh.txt",
 		"http://www.x-powers.com/update/Gallery2-update-Zh.txt",
 		"http://www.softwinners.com/update/Gallery2-update-Zh.txt"};
     private String path;
     private String target="/mnt/sdcard/apk-info-update.txt";
 	private String apkName;
 	
     private MyBinder binder;
     private Handler handler;
     private List<String> newURL=new ArrayList<String>();
 	private String randomURL;
 	private String newVerName;
 	private String describe;
 	private String packageName;
 
     public  int status=0;
     private Thread thread;
     private int ThreadNum=0;
 	private String[] UrlTeam ;
 	private int UrlNum=0;
     private boolean  readFinish=false;
 	private boolean discoverNew=false;
 	private SharedPreferences mShare;
 	private int randomDay=4;
 	private int testFlag=0;
     private String language;
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		Log.i(TAG,"CheckUpdate_service is Binded");
 		return binder;
 	}
 	@Override
 	public void onCreate()
 	{
 		super.onCreate();
 		mContext=getBaseContext();
 		binder=new MyBinder();
 		Log.i(TAG,"CheckUpdate_service is Created");
 		try{
             language= ActivityManagerNative.getDefault().getConfiguration().locale.getCountry();
 		    
 		}catch (RemoteException e)
 			{}
 		Log.v(TAG,"language="+language);
 		
 		mShare=getSharedPreferences("NUM",MODE_PRIVATE);
 		int random=(int)(Math.random()*100); 
 		path=OdPath[random%OdPath.length];
 		if(language.equals("CN")||language.equals("TW")){
 			 Log.v(TAG,"now ,The language is CH orTM");
 			}else{
 			    path=path.replace("-Zh.txt","-Eg.txt");
 			}
 		readPreference();
 	    handler=new Handler()
 	    {   @Override
 	    	public  void handleMessage(Message msg)
 	    	{ 
 			   if(msg.what==100){
 		           Log.i(TAG,"download finished");
 	    	       readTextInfo();
 	    	       status=100;
 	    	     }
 	    	     else if(msg.what<100){
 	    		    Log.i(TAG,"finish percent:"+msg.what+"%");
 	    		    status=msg.what;
 	    		  }  else if (msg.what==111){ 
 	    			          if(ThreadNum<=1) { 
 						 	     thread=new Thread(new DownThread(path,target,handler) );
 						         Log.i(TAG,"start new DownThread:"+ThreadNum);
 	    			             thread.start();
 	    			             ThreadNum++;    			      
 	    			         }
 	    		    }
 	    	}
 	    };
 
 		
 	   thread=new Thread(new DownThread(path,target,handler));
 	   thread.start();
 	    
 	}
 		
 	public class MyBinder extends Binder
 	{
 		public boolean isDiscoverableNew()
 		{ 
 			 return discoverNew;
 		}
 		public String getNewURL()
 		{
 			if(readFinish)
 			 return randomURL;
 				else 
 					return null;
 		}
 		public boolean isReadFinish()
 		{
 			return readFinish;
 		}
 		public boolean getIsFail()
 		{
 			 if(ThreadNum>=2)
 				 return true;
 			 else 
 				 return false;
 		}
 		public String getNewVerName()
 		{
                return newVerName;
 		
 		}
 		public String getApkName()
 		{
 		    return apkName;
 
 		}
 		public String getDescribe()
 		{
 		    return describe;
 
 		}
 		public String getPackageName()
 		{
 		    return packageName;
 
 		}
 	}
 	
 	private void readTextInfo()
 	{   
 		TextInfo textInfo=new TextInfo(target,mContext);
 		String scope;
 		String release;
 		String FIRMWARE;					
 		int num=textInfo.discoverNew();
 		if(num!=-1){
           Log.v(TAG,"find new apk to download");
 		  scope=textInfo.getScope(num);
 		  release=textInfo.getRelease(num);
 		  if(scope==null||release==null){
             Log.v(TAG,"scope==null or release==null ");
 			return;
 		  }
 		  Log.v(TAG,"release="+release);
 		  Log.v(TAG,"scope="+scope);
 		  String mScope[]=scope.split("-");
 		  int min=Integer.parseInt(mScope[0]);
 		  int max=Integer.parseInt(mScope[1]);
 		  if(min<=readPreference()&&max>=readPreference()){
		  	  FIRMWARE=Build.FIRMWARE;
 			  Log.v(TAG,"Build.FIRMWARE="+FIRMWARE);
 			  FIRMWARE=getNumber(FIRMWARE);
 			  release=getNumber(release);
 		      if(release.toString().equals("")||FIRMWARE.toString().equals("")) {
                  Log.v(TAG,"Can know the release or FIRMWARE");
 			     return;
 				 }
 		  	  if(Integer.parseInt(FIRMWARE)<Integer.parseInt(release)){
                   Log.v(TAG,"The FIRMWARE is to low");
 			      return;
 		        } 
 		  	  apkName=textInfo.getApkName(num);
 			  SharedPreferences.Editor mEditor=Gallery.mableUpdate.edit();						
 		      mEditor.putString("apkName",apkName);
 		      mEditor.commit();
 			  Log.v(TAG,"apkName="+apkName);
 		  	  discoverNew=true;
 			  describe=textInfo.getDescribe(num);
 			  packageName=textInfo.getPackageName(num);
 			  Log.v(TAG,"describe="+describe);
               Log.v(TAG,"This pad Can download apk package");
 			  newURL=textInfo.getNewURL(num);
 			  Iterator<String> getLen=newURL.iterator();
 		      while(getLen.hasNext()){ 
 			  	 UrlNum++;
 		         getLen.next();
 		      } 
 			  UrlTeam=new String[UrlNum];
 		      Iterator<String> getUrl=newURL.iterator();
 			  int i=0;
 		      while(getUrl.hasNext()){   
 			      UrlTeam[i++]=getUrl.next(); 
 		      }
 			  int random=(int)(Math.random()*(UrlNum-1)); 
 			  randomURL=UrlTeam[random];
 			  readFinish=true;
 		  }else {
               Log.v(TAG,"This pad Can't download apk package");
 		  }
 		}
 
 	}	
 	public int readPreference()
 	{		
     	SharedPreferences.Editor mEditor=mShare.edit();
     	int random=(int)(Math.random()*999); 
     	int shareNum;
 		if(testFlag==1){
             mEditor.putInt("shareNum", 1000);
     		mEditor.commit();
 			Log.v(TAG,"put test number to shareNUm");
 		}
     	if(mShare.getInt("shareNum", 1001)==1001)
     	{
     		mEditor.putInt("shareNum", random);
     		mEditor.commit();	
     	}
     	 shareNum=mShare.getInt("shareNum", 1001);
     	 Log.v(TAG,"shareNum="+shareNum);     	
 		return shareNum;    	
 	}
 	public String getNumber(String str){
 		StringBuilder sb = new StringBuilder();
     	for (int i = 0; i < str.length(); i++) {
     		char c = str.charAt(i);
     		if (c <= 57 && c >= 48) {
     		sb.append(c);
     		}
     		}
     	return sb.toString();
     	
 		
 	}
 	@Override
 	public void onDestroy()
 	{
 		Log.i(TAG,"---CheckUpdae service Destroy  finish---");
 	}	
 	
 }
