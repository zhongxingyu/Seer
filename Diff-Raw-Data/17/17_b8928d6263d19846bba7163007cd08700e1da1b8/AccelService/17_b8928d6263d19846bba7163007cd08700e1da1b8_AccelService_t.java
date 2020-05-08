 package edu.ucla.cens.accelservice;
 
 import edu.ucla.cens.systemlog.ISystemLog;
 import edu.ucla.cens.systemlog.Log;
 
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.SystemClock;
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
 	
 	/** Timer message types */
 	private static final int SLEEP_TIMER_MSG = 1;
 	private static final int READ_TIMER_MSG = 2;
     private static final int WARMUP_TIMER_MSG = 3;
 	
 	/** Constant values used for easy time specification */
 	private static final long ONE_SECOND = 1000;
 	private static final long ONE_MINUTE = 60 * ONE_SECOND;
 
     private static final long DEFAULT_WARMUP_INTERVAL = ONE_SECOND;
 
 
 	
 	/** Operation power consumption regimes */
 	public static final int REGIME_RELAXED = 0;
 	public static final int REGIME_CONTROLLED = 1;
 
 	
 	/** Sensor reading rate. Default rate is set to GAME */
 	private int mRate = SensorManager.SENSOR_DELAY_GAME;
 	
 	/** Sleep interval value. By default set to one minutes */
 	private long mSleepInterval = ONE_MINUTE;
 
     /** Sensor warmup interval */
     private long mWarmupInterval = DEFAULT_WARMUP_INTERVAL;
 	
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
 
 	
 	 
 	/** The SensorManager object */
 	private SensorManager mSensorManager;
 	
 	private static int mRegime = REGIME_RELAXED;
 	
 	
 	
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
 	
 	private final IAccelServiceControl.Stub mControlBinder = 
         new IAccelServiceControl.Stub()
 	{
 		
 		/**
 		 * Sets the current operational regime.
 		 * REGIME_RELAXED (0x00000000) is the default regime 
          * where the service can
 		 * take suggestions from its clients.
 		 * Other integer values indicate next levels of power 
          * consumption limitations
 		 *
 		 * @param		regime		new power consumption regime
 		 */ 
 		public void setOperationRegime(int regime)
 		{
             if (mRegime != regime)
             {
                 String regStr = "controlled";
                 if (regime == REGIME_RELAXED)
                     regStr = "relaxed";
 
                 Log.i(TAG, "Operating regime changed to " + regStr);
             }
 
 			mRegime = regime;
 			
 			if (regime == REGIME_RELAXED)
 			{
 				resetToDefault();
 			}
 		}
 		
 		
 		/**
 		 * Increases the sleep interval between accelerometer 
          * sampling events.
 		 *
 		 * @return 					the new interval after the increase
 		 *
 		 */
 		public long increaseInterval()
 		{
             setOperationRegime(REGIME_CONTROLLED);
 			return changeSleepInterval(mSleepInterval * 2);
 			
 		}
 		
 		/**
 		 * Decreases the sleep interval between accelerometer 
          * sampling events.
 		 * 
 		 * @return 					the new interval after the decrease
 		 */	
 		public long decreaseInterval()
 		{
             setOperationRegime(REGIME_CONTROLLED);
 			return changeSleepInterval(mSleepInterval / 2);
 		}
 
 		/**
 		 * Sets the sleep interval
 		 * 
 		 * @param		interval	the value to be set	
 		 * @return 					the new interval after the change
 		 */	
 		public long setInterval(long interval)
 		{
             setOperationRegime(REGIME_CONTROLLED);
 			return changeSleepInterval(interval);
 		}
 
 		/**
 		 * Returns the current sleep interval.
 		 * 
 		 * @return 					the new interval after the change
 		 */	
 		public long getInterval()
 		{
 			
 			return mSleepInterval;
 		}
 		
 		/**
 		 * Increases the accelerometer sampling rate.
 		 * Possible values are:
 		 * SENSOR_DELAY_FASTEST, SENSOR_DELAY_GAME, 
          * SENSOR_DELAY_NORMA, SENSOR_DELAY_UI
 		 *
 		 * @return 					the actual rate after the increase
 		 *
 		 */	
 		public int increaseRate()
 		{
 			mRegime = REGIME_CONTROLLED;
 			int newRate;
 			
 			switch (mRate)
 			{
 				case SensorManager.SENSOR_DELAY_NORMAL:
 					newRate = SensorManager.SENSOR_DELAY_UI; break;
 				case SensorManager.SENSOR_DELAY_UI:
 					newRate = SensorManager.SENSOR_DELAY_GAME; break;
 				case SensorManager.SENSOR_DELAY_GAME:
 					newRate = SensorManager.SENSOR_DELAY_FASTEST; break;
 				default:
 					newRate = SensorManager.SENSOR_DELAY_FASTEST; break;
 			}
 			
 			return changeRate(newRate);
 			
 		}
 		
 		/**
 		 * Decrease the accelerometer sampling rate.
 		 * Possible values are:
 		 * SENSOR_DELAY_FASTEST, SENSOR_DELAY_GAME, 
          * SENSOR_DELAY_NORMA, SENSOR_DELAY_UI
 		 *
 		 * @return 					the actual rate after the decrease
 		 *
 		 */	
 		public int decreaseRate()
 		{
 			mRegime = REGIME_CONTROLLED;
 			int newRate;
 			
 			switch (mRate)
 			{
 				case SensorManager.SENSOR_DELAY_UI:
 					newRate = SensorManager.SENSOR_DELAY_NORMAL; break;
 				case SensorManager.SENSOR_DELAY_GAME:
 					newRate = SensorManager.SENSOR_DELAY_UI; break;
 				case SensorManager.SENSOR_DELAY_FASTEST:
 					newRate = SensorManager.SENSOR_DELAY_GAME; break;
 				default:
 					newRate = SensorManager.SENSOR_DELAY_NORMAL; break;
 			}
 			
 			return changeRate(newRate);			
 		}
 		
 		/**
 		 * Set the rate of accelerometer sampling. 
 		 * Possible values are:
 		 * SENSOR_DELAY_FASTEST, SENSOR_DELAY_GAME, 
          * SENSOR_DELAY_NORMA, SENSOR_DELAY_UI
 		 * 
 		 * @param 		rate		rate of sensor reading
 		 * @return 					the actual rate that was set 
 		 * 
 		 */
 		public int setRate(int rate)
 		{
             setOperationRegime(REGIME_CONTROLLED);
 			return changeRate(rate);
 		}
 		
 		/**
 		 * Returns the current accelerometer sampling rate.
 		 * 
 		 * @param 		rate		rate of sensor reading
 		 */
 		public int getRate()
 		{
 			return mRate;
 		}
 
 		
 		/**
 		 * Sets the power consumption.
 		 * 
 		 * @param		power		the power consumption level
 		 */
 		public void setPower(int power)
 		{
             setOperationRegime(REGIME_CONTROLLED);
 			//TODO: Not implemented yet.
 		}
 		
 		/**
 		 * Returns the current power consumption level.
 		 * 
 		 * @return					the new power consumption level
 		 */	
 		public int getPower()
 		{
 			//TODO: Not implemented yet.
 			return 0;
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
 			if (mRegime == REGIME_RELAXED)
 				return changeRate(rate);
 			else
 				return mRate;
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
 			if (mRegime == REGIME_RELAXED)
 				return changeSleepInterval(interval);
 			else
 				return mSleepInterval;
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
 
               if (mClientCount == 1)
               {
                   Log.i(TAG, "Starting the service");
                   mHandler.sendMessageAtTime(
                          mHandler.obtainMessage(WARMUP_TIMER_MSG),
                          SystemClock.uptimeMillis() +
                          mSleepInterval);				 
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
 
               if (mClientCount <= 0)
               {
                   Log.i(TAG, "Stopping the service");
                   mHandler.removeMessages(SLEEP_TIMER_MSG);
                   mHandler.removeMessages(READ_TIMER_MSG);
                   
                   mSensorManager.unregisterListener(mSensorListener, 
                      mSensorManager.getDefaultSensor(
                          Sensor.TYPE_ACCELEROMETER));
                  
                   
                  mSensorRunning = false;
                  mClientCount = 0;
               }
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
         if (IAccelServiceControl.class.getName().equals(
                     intent.getAction())) 
         {
             return mControlBinder;
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
             if (msg.what == SLEEP_TIMER_MSG)
             {
             	if (mSensorRunning)
             	{
             		// Debug
             		Log.v(TAG, "Turning off the sensor");
                     Log.v(TAG, "Recorded " 
                             + mTempForceList.size()
                             + " samples.");
 
                     Log.v(TAG, "Last force value: " 
                             + mTempForceList.get(mTempForceList.size() -
                             1));
             		
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
 
             	}
 
                 mHandler.sendMessageAtTime(
                 		mHandler.obtainMessage(WARMUP_TIMER_MSG),
                 		SystemClock.uptimeMillis() + mSleepInterval);
             }
             else if (msg.what == WARMUP_TIMER_MSG)
             {
             	if (!mSensorRunning)
             	{
             		// Debug
             		Log.v(TAG, "Starting to warm up the sensor for "
                             + mWarmupInterval
                             + " milliseconds");
 
             		mSensorManager.registerListener(mSensorListener, 
                     		mSensorManager.getDefaultSensor(
                                 Sensor.TYPE_ACCELEROMETER), 
                     		mRate);
 
                     mHandler.sendMessageAtTime(
                             mHandler.obtainMessage(READ_TIMER_MSG),
                             SystemClock.uptimeMillis() + mWarmupInterval);
 
 
             	}
 
                 mSensorRunning = true;
                 mRecordSensor = false;
 
             }
             else if (msg.what == READ_TIMER_MSG)
             {             
                 // Debug
                 Log.v(TAG, "Recording the sensor");
             		
                 mJustStarted = true; 
                 mRecordSensor = true;
 
             }
 
         }
 
     };
 
 
     @Override
     public void onStart(Intent intent, int startId)
     {
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
         bindService(new Intent(ISystemLog.class.getName()),
                 Log.SystemLogConnection, Context.BIND_AUTO_CREATE);
      
         Log.i(TAG, "onCreate");
      
         resetToDefault();
         mSensorManager = (SensorManager) getSystemService(
                 Context.SENSOR_SERVICE);
         
 		
         
 
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
     	if (interval < 10 * ONE_SECOND)
     	{
     		mSleepInterval = ONE_MINUTE;
     	}
     	else
     	{
     		mSleepInterval = interval;
     	}
     	
    	Log.i(TAG, "Sleeping interval changed to " + mSleepInterval);
     	
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
     
 }
