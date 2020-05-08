 package pt.lsts.accu.state;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import pt.lsts.accu.components.Heart;
 import pt.lsts.accu.components.HeartbeatVibrator;
 import pt.lsts.accu.msg.IMCManager;
 import pt.lsts.accu.types.Sys;
 import pt.lsts.accu.util.MUtil;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 /**
  * Global Singleton that actually acts as the workhorse of ACCU.
  * Contains all the structures that need to be global and persistent across all activities/panels
  * 
  * @author jqcorreia
  *
  */
 public class Accu {
 
 	private static Context mContext;
 	private static Accu instance;
 	private static Sys activeSys;
 	
 	private static IMCManager imcManager;
 	public SystemList mSysList;
 	public static Announcer mAnnouncer;
 	public static AccuSmsHandler mSmsHandler;
 	public static GPSManager mGpsManager;
 	public static HeartbeatVibrator mHBVibrator;
 	public static Heart mHeart;
 	public static LblBeaconList mBeaconList;
 	public static CallOut callOut;
 	
 	private static ArrayList<MainSysChangeListener> mMainSysChangeListeners;
 	public String broadcastAddress;
 	public boolean started=false;
 	SharedPreferences mPrefs;
 
 	private static Integer requestId = 0xFFFF; // Request ID for quick plan sending
 	
 	private Accu(Context context)
 	{
 		System.out.println("Initializing Global ACCU Object");
 		mContext = context;
 		imcManager = new IMCManager();
 		imcManager.startComms(); // Start comms here upfront
 		
 		mSysList = new SystemList(imcManager); 
 		
 		try {
 			broadcastAddress = MUtil.getBroadcastAddress(mContext);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		mGpsManager = new GPSManager(mContext);
 		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
 		mMainSysChangeListeners=new ArrayList<MainSysChangeListener>();
 		mAnnouncer = new Announcer(imcManager,broadcastAddress,"224.0.75.69");
		mSmsHandler = new AccuSmsHandler(mContext, imcManager);
 		mHBVibrator = new HeartbeatVibrator(mContext, imcManager);
 		callOut = new CallOut(mContext);
 	}
 
 	public void load()
 	{
 		mHeart = new Heart();
 		mBeaconList = new LblBeaconList();
 	}
 	
 	public void start()
 	{
 		if(!started)
 		{
 			imcManager.startComms();
 			mAnnouncer.start();
 			mSysList.start();
 			mHeart.start();
 			started = true;
 		}
 		else
 			Log.i("ACCU ERROR","Already Started ACCU Global");
 	}
 	public void pause()
 	{
 		if(started)
 		{
 			imcManager.killComms();
 			mAnnouncer.stop();
 			mSysList.stop();
 			mHeart.stop();
 			mSmsHandler.stop();
 			started = false;
 		}
 		else
 			Log.i("ACCU ERROR","ACCU Global already stopped");
 	}
 	
 	public static Accu getInstance(Context context)
 	{
 		if(instance == null)
 		{
 			instance = new Accu(context);
 		}
 		return instance;
 	}
 	
 	public static Accu getInstance()
 	{
 		return instance;
 	}
 	
 //	public static void killInstance()
 //	{
 //		instance = null;
 //		mSysList.timer.cancel(); //FIXME For now the timer cancelling goes here.. 
 //		mAnnouncer.timer.cancel(); //FIXME same as above
 //	}
 	public Sys getActiveSys() {
 		return activeSys;
 	}
 	public void setActiveSys(Sys activeS) {
 		activeSys = activeS;
 		notifyMainSysChange();
 	}
 	public IMCManager getIMCManager()
 	{
 		return imcManager;
 	}
 	public SystemList getSystemList()
 	{
 		return mSysList;
 	}
 	public GPSManager getGpsManager()
 	{
 		return mGpsManager;
 	}
 	public LblBeaconList getLblBeaconList()
 	{
 		return mBeaconList;
 	}
 	public CallOut getCallOut()
 	{
 		return callOut;
 	}
 	// Main System listeners list related code
 	public void addMainSysChangeListener(MainSysChangeListener listener)
 	{
 		mMainSysChangeListeners.add(listener);
 	}
 	public void removeMainSysChangeListener(MainSysChangeListener listener)
 	{
 		mMainSysChangeListeners.remove(listener);
 	}
 	private static void notifyMainSysChange()
 	{
 		for(MainSysChangeListener l: mMainSysChangeListeners)
 		{
 			l.onMainSysChange(activeSys);
 		}
 	}
 	public SharedPreferences getPrefs() {
 		return mPrefs;
 	}
 	
 	public boolean isStarted()
 	{
 		return started;
 	}
 	
     /**
      * @return the next requestId 
      */
     public int getNextRequestId() {
         synchronized (requestId) {
             ++requestId;
             if (requestId > 0xFFFF)
                 requestId = 0;
             if (requestId < 0)
                 requestId = 0;
             return requestId;
         }
     }
 }
