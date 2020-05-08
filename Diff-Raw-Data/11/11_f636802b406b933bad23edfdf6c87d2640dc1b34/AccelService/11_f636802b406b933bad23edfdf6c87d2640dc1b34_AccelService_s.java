 package edu.ucla.cens.accelservice;
 
 import edu.ucla.cens.systemlog.ISystemLog;
 import edu.ucla.cens.systemlog.Log;
 import edu.ucla.cens.systemsens.IPowerMonitor;
 import edu.ucla.cens.systemsens.IAdaptiveApplication;
 
 import android.app.Service;
 import android.app.PendingIntent;
 import android.app.AlarmManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ComponentName;
 import android.content.ServiceConnection;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.SystemClock;
 import android.os.RemoteException;
 import android.os.PowerManager;
 import android.hardware.SensorManager;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorEvent;
 import android.hardware.Sensor;
 
 import java.util.List;
 import java.util.ArrayList;
 
 
 public class AccelService extends Service
 {
 	/** TAG string used for logging */
 	private static final String TAG = "AccelService";
 
     private static final String APP_NAME = "AccelService";
 
     private static final String ACCEL_UNIT_NAME = "Accel";
 	
 	/** Timer message types */
 	private static final int SLEEP_TIMER_MSG = 1;
 	private static final int READ_TIMER_MSG = 2;
     //private static final int WARMUP_TIMER_MSG = 3;
 
     /** Alarm intent action */
     private static final String ACCEL_ALARM_ACTION = "accel_alarm";
 	
 	/** Constant values used for easy time specification */
 	private static final int ONE_SECOND = 1000;
 	private static final int ONE_MINUTE = 60 * ONE_SECOND;
 
     private static final int DEFAULT_WARMUP_INTERVAL = ONE_SECOND;
 
 
 	
 	/** Operation power consumption regimes */
 	public static final int REGIME_RELAXED = 0;
 	public static final int REGIME_CONTROLLED = 1;
 
 	
 	/** Sensor reading rate. Default rate is set to GAME */
 	private int mRate = SensorManager.SENSOR_DELAY_GAME;
 	
 	/** Sleep interval value. By default set to one minutes */
 	private long mSleepInterval = ONE_MINUTE;
 
     /** Sensor warmup interval */
     private long mWarmupInterval = DEFAULT_WARMUP_INTERVAL;
 
     /** Default power cycle interval */
     private static final int DEFAULT_POWERCYCLE_HORIZON = 5 * ONE_MINUTE;
 	
 	/** Reading interval value. By default set to one second */
 	private long mReadInterval = ONE_SECOND;
 	
 	/** Boolean variable used to re-initialize the recorded Lists */
 	private boolean mJustStarted = true;
 	
 	/** Boolean variable used to duty-cycle the sensor */
 	private boolean mSensorRunning = false;
 
     /** Boolean variable set to read the sensor after warm-up */
     private boolean mRecordSensor = false;
 	
 	/** Latest recorded time-stamp */
 	private long mLastTS;
 
     /** Counter for the number of connected clients */
     private int mClientCount = 0;
 
     /** Set if the service is running */
     private boolean mIsRunning = false;
 	
 	
 	/** List of recorded force values */
 	private List<Double> mLastForceList;
 	private List<Double> mTempForceList;
 	
 	/** List of recorded sensor values */
 	private List<Double> mLastListX;
 	private List<Double> mTempListX;
 
 	private List<Double> mLastListY;
 	private List<Double> mTempListY;
 
 	private List<Double> mLastListZ;
 	private List<Double> mTempListZ;
 
     private AccelCounter mAccelCounter;
 	
 
     /** The alarm manager object */
     private AlarmManager mAlarmManager;
 
     private PendingIntent mAccelSender;
 
     /** The partial wakelock object */
     private PowerManager.WakeLock mCpuLock;
 	 
 	/** The SensorManager object */
 	private SensorManager mSensorManager;
 	
 	
     /** Power monitor stuff */
     private IPowerMonitor mPowerMonitor;
     private boolean mPowerMonitorConnected = false;
 
 
     private final IAdaptiveApplication mAdaptiveControl
         = new IAdaptiveApplication.Stub()
     {
 
         public String getName()
         {
             return APP_NAME;
         }
 
         public List<String> identifyList()
         {
             ArrayList<String> unitNames = new ArrayList<String>(1);
             unitNames.add(ACCEL_UNIT_NAME);
 
             return unitNames;
         }
 
         public List<Double> getWork()
         {
             ArrayList<Double> totalWork = new ArrayList<Double>(1);
 
             totalWork.add(mAccelCounter.getCount());
 
             return totalWork;
         }
 
         public void setWorkLimit(List workLimit)
         {
             double accelLimit = (Double) workLimit.get(0);
 
             mAccelCounter.setLimit(accelLimit);
         }
 
 
     };
 
     private ServiceConnection mPowerMonitorConnection
         = new ServiceConnection()
     {
         public void onServiceConnected(ComponentName className,
                 IBinder service)
         {
             mPowerMonitor = IPowerMonitor.Stub.asInterface(service);
             try
             {
                 mPowerMonitor.register(mAdaptiveControl,
                         DEFAULT_POWERCYCLE_HORIZON);
             }
             catch (RemoteException re)
             {
                 Log.e(TAG, "Could not register AdaptivePower object",
                         re);
             }
             mPowerMonitorConnected = true;
         }
 
         public void onServiceDisconnected(ComponentName className)
         {
             try
             {
                 mPowerMonitor.unregister(mAdaptiveControl);
             }
             catch (RemoteException re)
             {
                 Log.e(TAG, "Could not unregister AdaptivePower object",
                         re);
             }
             mPowerMonitor = null;
             mPowerMonitorConnected = false;
         }
 
     };
 
 
 
 	
 	
 	/**
 	 * SensorEventListener object is passed to the SensorManager instance.
 	 * Every time the onSensorChanged() method is called values 
      * are recorded 
 	 * in the lists.
 	 */
 	private final SensorEventListener mSensorListener = 
         new SensorEventListener() {
 	
 		/*
 		 * Called when a new sensor reading is available.
 		 * 
 		 * If this is the first value after a sleep interval 
          * (mJustStarted is true)
 		 * the list objects are re-initialized. 
 		 * 
 		 * @see android.hardware.SensorEventListener#onSensorChanged(
          * android.hardware.SensorEvent)
 		 */
 		public void onSensorChanged(SensorEvent se) 
 		{
             mAccelCounter.count();
 
             if (mRecordSensor)
             {
                 if (mJustStarted)
                 {
                     // Just started receiving sensor updates. 
                     // Start a new list
                     mTempForceList = new ArrayList<Double>();
                     mTempListX = new ArrayList<Double>();
                     mTempListY = new ArrayList<Double>();
                     mTempListZ = new ArrayList<Double>();
 
 
                     mHandler.sendMessageAtTime(
                             mHandler.obtainMessage(SLEEP_TIMER_MSG),
                             SystemClock.uptimeMillis() + mReadInterval);
 
 
                     mJustStarted = false;
                 }
                 double x = se.values[0];
                 double y = se.values[1];
                 double z = se.values[2];
                 double totalForce = 0.0;
                 double grav = SensorManager.GRAVITY_EARTH;
                 
                 totalForce += Math.pow(x/grav, 2.0);
                 totalForce += Math.pow(y/grav, 2.0);
                 totalForce += Math.pow(z/grav, 2.0);
                 totalForce = Math.sqrt(totalForce);
                 
                 mLastTS = System.currentTimeMillis();
                 
                 mTempForceList.add(totalForce);
                 mTempListX.add(x);
                 mTempListY.add(y);
                 mTempListZ.add(z);
 
                 /* Debug
                 Log.i(TAG, "Recorded the " + mTempForceList.size() 
                         + "th data point");
                 */
             }
 			
 		}
 		
 		/*
 		 * Called when the sensor accuracy changes.
 		 * I do not handle this event.
 		 * @see android.hardware.SensorEventListener#onAccuracyChanged(
          * android.hardware.Sensor, int)
 		 */
 		public void onAccuracyChanged(Sensor sensor, int accuracy) 
 		{
 			/*
 			String accuracyStr = "Unkown";
 			
 			switch (accuracy)
 			{
 			case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
 				accuracyStr = "high";
 				break;
 			case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
 				accuracyStr = "medim";
 				break;
 			case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
 				accuracyStr = "medim";
 				break;			
 			case 0:
 				accuracyStr = "Zero";
 				break;
 			}
 			Log.i(TAG, "Accuracy changed to " + 
                 accuracyStr + " (" + accuracy + ")");
 			*/
 		}
 	};
 	
 
 	
 	/*
 	 * Binder object for the service. 
 	 */
 	private final IAccelService.Stub mBinder = new IAccelService.Stub()
 	{
 
 		/**
 		 * Set the rate of accelerometer sampling. This is only a 
 		 * suggestion and the service may choose a lower rate to save power.
 		 * Possible values are:
 		 * SENSOR_DELAY_FASTEST, SENSOR_DELAY_GAME, 
          * SENSOR_DELAY_NORMA, SENSOR_DELAY_UI
 		 * 
 		 * @param 	rate	rate of sensor reading
 		 * @return 			the actual rate that was set
 		 * 
 		 */
 		public int suggestRate(int rate)
 		{
             Log.v(TAG, "Got rate suggestion of " + rate);
             return changeRate(rate);
 		}
 		
 		/**
 		 * Set the length of the interval that accelerometer is
          * recorded before it is turned of (for duty-cycling).
 		 *
 		 * @param 	length		length of the interval for sensor 
          *                      reading in milli-seconds
 		 */
 		public long setReadingLength(long length)
 		{
             Log.v(TAG, "Reading length set to " + length);
 			mReadInterval = length;
 
             return mReadInterval;
 		}
 
 
         /**
          * Set the length of the warm-up interval before the actual
          * reading interval begins.
          *
          * @param   length      length of the warm-up interval for
          *                      preparing the accelerometer
          */
         public long setWarmupLength(long length)
         {
             if (length > 0)
                 mWarmupInterval = length;
             else
                 mWarmupInterval = DEFAULT_WARMUP_INTERVAL;
 
 
             Log.v(TAG, "Warmup length set to " + mWarmupInterval);
 
             return mWarmupInterval;
         }
 		
 		/**
 		 * Suggest length of the duty-cycling interval. The
          * accelerometer sensor will be turned off for some time
          * between readings.  This is only a suggestion and the
          * service may choose a longer interval to save power
          *
 		 * 
 		 * @param	interval	suggested length of off interval 
                                 in milli-seconds
 		 */
 		public long suggestInterval(long interval)
 		{
             Log.v(TAG, "Got interval suggestion of " + interval );
             return changeSleepInterval(interval);
 		}
 		
 		/**
 		 * Returns the current sleeping interval.
 		 * 
 		 * @return				current sleep interval used by the service
 		 */
 		public long getInterval()
 		{
 		    return mSleepInterval;
 		}
 
 		/**
 		 * Returns the current rate.
 		 * 
 		 * @return				current rate
 		 */
 		public int getRate()
 		{
 		    return mRate;
 		}
 
 
 		/**
 		 * Returns the current reading length
 		 * 
 		 * @return				current reading length 
 		 */
 		public long getReadingLength()
 		{
 		    return mReadInterval;
 		}
 
 		/**
 		 * Returns the length of current warmup interval
 		 * 
 		 * @return				current warmup interval length 
 		 */
 		public long getWarmupLength()
 		{
             return mWarmupInterval;
 		}
 
 
 		
 		/**
 		 * Returns the latest recorded force vector.
 		 * 
 		 * @return				latest recorded force vector
 		 */
 		public synchronized List<Double> getLastForce()
 		{
 		    return mLastForceList; 
 		}
 		 
 		 /**
 		  * Returns the list of latest recorded X values.
 		  * Each element of the list contains an array of values.
 		  *
 		  * @return				latest recorded values
 		  */
 		 public synchronized List<Double> getLastXValues()
 		 {
 		     return mLastListX;
 		 }
 
 
 		 /**
 		  * Returns the list of latest recorded Y values.
 		  * Each element of the list contains an array of values.
 		  *
 		  * @return				latest recorded values
 		  */
 		 public synchronized List<Double> getLastYValues()
 		 {
 		     return mLastListY;
 		 }
 
 		 /**
 		  * Returns the list of latest recorded Z values.
 		  * Each element of the list contains an array of values.
 		  *
 		  * @return				latest recorded values
 		  */
 		 public synchronized List<Double> getLastZValues()
 		 {
 		     return mLastListZ;
 		 }
 
 
           /**
            * Returns true if the mean of last Force Values is greater
            * than the threshold.
            *
            * @param     threshold   threshold value
            * @return                true if Force mean is *
            *            greater than threshold
            */
           public boolean significantForce(double threshold)
           {
               double mean, sum = 0.0;
 
               if (mLastForceList == null)
                   return true;
 
               for (double force : mLastForceList)
               {
                   sum += force;
               }
 
               mean = sum/mLastForceList.size();
 
               if (mean > threshold)
                   return true;
               else 
                   return false;
 
           }
 
 		  
 		  /**
 		   * Returns the time-stamp of the last recorded value.
 		   * This method can be used to verify the freshness of the values.
 		   *
 		   * @return 			time-stamp of the latest recorded
            *                    sensor value
 		   */
 		  public long getLastTimeStamp()
 		  {
 		      return mLastTS;
 		  }
 		 
           /**
            * Starts the accelerometer service.
            */
           public void start()
           {
               
               mClientCount++;
 
               Log.i(TAG, "Client count is " + mClientCount);
 
               if ((mClientCount == 1) && (!mIsRunning))
               {
                   Log.i(TAG, "Starting the service");
 
                   mAlarmManager.setRepeating(
                           AlarmManager.ELAPSED_REALTIME_WAKEUP,
                           SystemClock.elapsedRealtime(),
                           mSleepInterval, 
                           mAccelSender);
                   /*
                   mHandler.sendMessageAtTime(
                          mHandler.obtainMessage(WARMUP_TIMER_MSG),
                          SystemClock.uptimeMillis() +
                          mSleepInterval);				 
                  */
                   mIsRunning = true;
               }
               else
               {
                   Log.i(TAG, "Already running");
               }
           }
           
           /**
           * Stops the accelerometer service to save maximum power.
           */
           public void stop()
           {
               mClientCount--;
 
               Log.i(TAG, "Client count is " + mClientCount);
 
               if ((mClientCount <= 0) && (mIsRunning))
               {
                   Log.i(TAG, "Stopping the service");
                   mIsRunning = false;
                   mAlarmManager.cancel(mAccelSender);
                   mHandler.removeMessages(SLEEP_TIMER_MSG);
                   mHandler.removeMessages(READ_TIMER_MSG);
                   //mHandler.removeMessages(WARMUP_TIMER_MSG);
 
                   mSensorManager.unregisterListener(mSensorListener, 
                      mSensorManager.getDefaultSensor(
                          Sensor.TYPE_ACCELEROMETER));
                  
                   
                  mSensorRunning = false;
                  mClientCount = 0;
                  mIsRunning = false;
               }
 
               if (mClientCount < 0)
                   mClientCount = 0;
           }
  
 	};
 	
 	/*
 	 * Returns the binder object.
 	 * 
 	 * @see android.app.Service#onBind(android.content.Intent)
 	 */
 	@Override
 	public IBinder onBind(Intent intent)
 	{
         if (IAccelService.class.getName().equals(intent.getAction())) 
         {
             return mBinder;
         }
         
 		return null;
 	}
 	
 	/*
 	 * Handler object to manage self messages.
 	 * There are only two types of messages: SLEEP_TIMER_MSG 
      * and READ_TIMER_MSG.
 	 */
     private final Handler mHandler = new Handler()
     {
         @Override
         public synchronized void handleMessage(Message msg)
         {
             // Discard the message if the service is not 
             // supposed to be running.
             if (!mIsRunning)
             {
                 Log.w(TAG, "Discarding internal message.");
                 mAlarmManager.cancel(mAccelSender);
                 return;
             }
 
             if (msg.what == SLEEP_TIMER_MSG)
             {
             	if (mSensorRunning)
             	{
             		// Debug
             		Log.v(TAG, "Turning off the sensor");
                     Log.v(TAG, "Recorded " 
                             + mTempForceList.size()
                             + " samples.");
 
            		
             		mSensorManager.unregisterListener(mSensorListener, 
             				mSensorManager.getDefaultSensor(
                                 Sensor.TYPE_ACCELEROMETER));
             		
             		mSensorRunning = false;
                     mRecordSensor = false;
             		
             		// Time to copy temp lists to last lists
         			mLastForceList = mTempForceList;
         			mLastListX = mTempListX;
         			mLastListY = mTempListY;
         			mLastListZ = mTempListZ;
 
 
                     Log.v(TAG, "Last force value: " 
                             + mLastForceList.get(
                                 mLastForceList.size() - 1));
  
 
             	}
 
                mCpuLock.release();
 
             }
             /* Replaced by the Alarm mechanism 
             else if (msg.what == WARMUP_TIMER_MSG)
             {
             	if (!mSensorRunning)
             	{
             		Log.v(TAG, "Starting to warm up the sensor for "
                             + mWarmupInterval
                             + " milliseconds");
 
                     if (mAccelCounter.hasBudget())
                     {
                         mSensorManager.registerListener(mSensorListener, 
                                 mSensorManager.getDefaultSensor(
                                     Sensor.TYPE_ACCELEROMETER), 
                                 mRate);
                     }
                     else
                     {
                         Log.i(TAG, "Ran out of budget. Did not turn" +
                                 "on the sensor");
                     }
 
                     mHandler.sendMessageAtTime(
                             mHandler.obtainMessage(READ_TIMER_MSG),
                             SystemClock.uptimeMillis() + mWarmupInterval);
 
 
             	}
 
                 mSensorRunning = true;
                 mRecordSensor = false;
 
             }
             */
             else if (msg.what == READ_TIMER_MSG)
             {             
                 // Debug
                 Log.v(TAG, "Recording the sensor");
             		
                 mJustStarted = true; 
                 mRecordSensor = true;
 
             }
 
         }
 
     };
 
 
     /**
       * Triggers the sensor reading cycle.
       * Starts the sensor and also sends a message for the
       * warmup interval.
       */
     private void sensorCycle()
     {
 
         if (!mSensorRunning)
         {
             Log.v(TAG, "Starting to warm up the sensor for "
                     + mWarmupInterval
                     + " milliseconds");
 
             if (mAccelCounter.hasBudget())
             {
                 mSensorManager.registerListener(mSensorListener, 
                         mSensorManager.getDefaultSensor(
                             Sensor.TYPE_ACCELEROMETER), 
                         mRate);
 
                 mHandler.sendMessageAtTime(
                         mHandler.obtainMessage(READ_TIMER_MSG),
                         SystemClock.uptimeMillis() + mWarmupInterval);
 
                 mSensorRunning = true;
                 mRecordSensor = false;
 
             }
             else
             {
                 Log.i(TAG, "Ran out of budget. Did not turn " +
                         "on the sensor.");
             }
 
         }
 
 
     }
 
 
     @Override
     public void onStart(Intent intent, int startId)
     {
         if (!mPowerMonitorConnected)
         {
             Log.i(TAG, "Rebinding to PowerMonitor");
             bindService(new Intent(IPowerMonitor.class.getName()),
                     mPowerMonitorConnection, Context.BIND_AUTO_CREATE);
      
         }
 
         if (intent != null)
         {
             String action = intent.getAction();
 
             if (action != null)
             {
                 Log.i(TAG, "Received action: " + action);
 
                 if (action.equals(ACCEL_ALARM_ACTION))
                 {
                    mCpuLock.acquire(); // Released after sensor
                                        // reading is over
 
                     sensorCycle();
                 }
             }
         }
         super.onStart(intent, startId);
         Log.i(TAG, "onStart");
     }
 	
     /*
      * Create and initialize the service object.
      * 
      * We first bind to SystemLog to send all logging messages through that.
      * After initializing the SensorManager object as self-message is sent
      * to get things started.
      * @see android.app.Service#onCreate()
      */
     @Override
     public void onCreate() {
         super.onCreate();
 
         Log.setAppName(APP_NAME);
         bindService(new Intent(ISystemLog.class.getName()),
                 Log.SystemLogConnection, Context.BIND_AUTO_CREATE);
      
         bindService(new Intent(IPowerMonitor.class.getName()),
                 mPowerMonitorConnection, Context.BIND_AUTO_CREATE);
  
 
         Log.i(TAG, "onCreate");
      
         resetToDefault();
         mSensorManager = (SensorManager) getSystemService(
                 Context.SENSOR_SERVICE);
         
 		
         mAccelCounter = new AccelCounter();
 
         PowerManager pm = (PowerManager) getSystemService(
                 Context.POWER_SERVICE);
         mCpuLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                 APP_NAME);
 
 
         mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 
         // Repeating alarm for Accel dutycycling
         Intent accelAlarmIntent = new Intent(AccelService.this,
                 AccelService.class);
         accelAlarmIntent.setAction(ACCEL_ALARM_ACTION);
         mAccelSender = PendingIntent.getService(AccelService.this, 0,
                 accelAlarmIntent, 0);
 
         
 
     }
     
     /*
      * Called to clean up.
      * If the sensor is running we stop it.
      * @see android.app.Service#onDestroy()
      */
     @Override
     public void onDestroy()
     {
     	Log.i(TAG, "onDestroy");
 
         Log.i(TAG, "Stopping the service");
         mAlarmManager.cancel(mAccelSender);
         mHandler.removeMessages(SLEEP_TIMER_MSG);
         mHandler.removeMessages(READ_TIMER_MSG);
         
         mSensorManager.unregisterListener(mSensorListener, 
            mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER));
         
         
         mSensorRunning = false;
         mClientCount = 0;
 
 
     	super.onDestroy();
 
         unbindService(Log.SystemLogConnection);
     	
     	if (mSensorRunning)
     		mSensorManager.unregisterListener(mSensorListener);
     	
     }
     
     /*
      * Sets all the running parameters to default values
      */
     private void resetToDefault()
     {
         Log.i(TAG, "Resetting variables to default");
     	/** Sensor reading rate. Default rate is set to GAME */
     	mRate = SensorManager.SENSOR_DELAY_GAME;
     	
     	/** Sleep interval value. By default set to one minutes */
     	mSleepInterval = ONE_MINUTE;
     	
     	/** Reading interval value. By default set to one second */
     	mReadInterval = ONE_SECOND;
     	
     }
     
     /*
      * Used internally to modify sleep interval.
      * For now does nothing but applying the change. But in future will 
      * implement the power adaptation logic.
      * 
      * @param 		interval 		suggested interval in miliseconds
      * @return						final applied value
      * 
      */
     private long changeSleepInterval(long interval)
     {
     	if (interval < 5 * ONE_SECOND)
     	{
     		mSleepInterval = 5 * ONE_SECOND;
     	}
     	else
     	{
     		mSleepInterval = interval;
     	}
     	
     	Log.i(TAG, "Sleeping interval changed to " + mSleepInterval);
 
         if (mIsRunning)
         {
             mAlarmManager.cancel(mAccelSender);
             mAlarmManager.setRepeating(
                 AlarmManager.ELAPSED_REALTIME_WAKEUP,
                 SystemClock.elapsedRealtime(),
                 mSleepInterval, 
                 mAccelSender);
         }
 
     	
     	return mSleepInterval;
     }
     
     /*
      * Used internally to modify the sampling rate.
      * For now does nothing but applying the change. But in future will 
      * implement the power adaptation logic.
      * 
      * @param 		interval 		suggested interval in miliseconds
      * @return						final applied value
      * 
      */
     private int changeRate(int rate)
     {
 		switch (rate)
 		{
 		case SensorManager.SENSOR_DELAY_FASTEST:
 			mRate = SensorManager.SENSOR_DELAY_FASTEST;
 			break;
 		case SensorManager.SENSOR_DELAY_GAME:
 			mRate = SensorManager.SENSOR_DELAY_GAME;
 			break;
 		case SensorManager.SENSOR_DELAY_NORMAL:
 			mRate = SensorManager.SENSOR_DELAY_NORMAL;
 			break;
 		case SensorManager.SENSOR_DELAY_UI:
 			mRate = SensorManager.SENSOR_DELAY_UI;
 			break;
 		default:
 			mRate = SensorManager.SENSOR_DELAY_GAME;
 			break;
 		}
 
         Log.i(TAG, "Changing rate to " + mRate);
 		
 		return mRate;
     }
 
 
     class AccelCounter
     {
         private double mTotal;
         private double mCurTotal;
         private double mLimit;
 
         public AccelCounter()
         {
             mTotal = mCurTotal = 0.0;
             mLimit = Double.NaN;
         }
 
         public boolean hasBudget()
         {
             if ( Double.isNaN(mLimit) || (mCurTotal < mLimit))
             {
                 return true;
             }
             else if (!Double.isNaN(mLimit) && (mCurTotal >= mLimit))
             {
                 return false;
             }
             return true;
         }
 
         public boolean count()
         {
             if (hasBudget())
             {
                 mTotal += 1;
                 mCurTotal += 1;
                 return true;
             }
             else
                 return false;
         }
 
         public void setLimit(double workLimit)
         {
             mLimit = workLimit;
             mCurTotal = 0.0;
         }
 
         public double getCount()
         {
             return mTotal;
         }
 
     }
 
     
 }
