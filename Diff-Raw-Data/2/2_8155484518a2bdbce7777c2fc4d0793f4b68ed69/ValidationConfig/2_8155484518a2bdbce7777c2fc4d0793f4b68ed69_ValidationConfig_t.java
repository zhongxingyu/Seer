 package playground.pool;
 
 import javax.validation.constraints.Min;
 
 import playground.pool.util.PropertyValidator;
 import playground.pool.util.PropertyValidationException;
 
 /**
  * Configuration of {@link PoolEntry} validation.
  * */
 public class ValidationConfig {
 	
 	private static byte TEST_INTERVAL_NOT_TAKEN = 0;
 
 	private static byte MAX_AGE_UNLIMIT = 0;
 	
 	/**
 	 * Check validity of the object when borrow the object from {@link Pool} 
 	 * */
 	private boolean testOnBorrow = true;
 
 	/**
 	 * Check validity of the object when return the object to {@link Pool} 
 	 * */
 	private boolean testOnReturn = false;
 	
 	/**
 	 * Time interval for check validity of the object.
 	 * <p>
 	 * If this value is greater than zero, 
 	 * does not check the validity while not exceeded specified interval millis. 
 	 * If this value is zero, time interval for check is not taken.
 	 * </p>
 	 * */
 	@Min(0)
 	private long testIntervalMillis = 0;
 	
 	/**
 	 * Specify survival time of the object.
 	 * <p>
 	 * If survival time of the object exceeded specified value, 
 	 * the object is invalidated when check validity of the object.
 	 * If this value is zero, survival time of the object is unlimited.
 	 * </p> 
 	 * */
 	@Min(0)
 	private long maxAgeMillis = 0;
 	
 	/**
 	 * The number of threads to validation threads. 
 	 * These threads check validity of the object in the background while object is idling.  
 	 * */
 	@Min(0)
 	private int testThreads = 0;
 	
 	/** 
 	 * The time to delay first execution of validation threads.
 	 * */
 	@Min(0)
 	private long testThreadInitialDelayMillis = 1000 * 60 * 10;	// default 10min 
 
 	/**
 	 * The validation threads execution interval
 	 * */
 	@Min(1)
 	private long testThreadIntervalMillis = 1000 * 60 * 10;	// default 10min
 	
 	/**
 	 * Validate this configuration value.
 	 * @throws PropertyValidationException If this configuration value is invalid. 
 	 * */
 	public void validateConfig() throws PropertyValidationException {
 		validatePropValues();
 	}
 	
 	private void validatePropValues() throws PropertyValidationException {
 		PropertyValidator.INSTANCE.validate(this);
 	}
 	
 	public boolean isMaxAgeUnlimit() {
 		return maxAgeMillis == MAX_AGE_UNLIMIT;
 	}
 	
 	public boolean isTestWithInterval() {
		return testIntervalMillis != TEST_INTERVAL_NOT_TAKEN;
 	}
 	
 	public boolean isTestInBackground() {
 		return testThreads > 0;
 	}
 	
 	public boolean isTestOnBorrow() {
 		return testOnBorrow;
 	}
 	public void setTestOnBorrow(boolean testOnBorrow) {
 		this.testOnBorrow = testOnBorrow;
 	}
 
 	public boolean isTestOnReturn() {
 		return testOnReturn;
 	}
 	public void setTestOnReturn(boolean testOnReturn) {
 		this.testOnReturn = testOnReturn;
 	}
 	
 	public long getTestIntervalMillis() {
 		return testIntervalMillis;
 	}
 	public void setTestIntervalMillis(long testIntervalMillis) {
 		this.testIntervalMillis = testIntervalMillis;
 	}
 
 	public long getMaxAgeMillis() {
 		return maxAgeMillis;
 	}
 	public void setMaxAgeMillis(long maxAgeMillis) {
 		this.maxAgeMillis = maxAgeMillis;
 	}
 	
 	public int getTestThreads() {
 		return testThreads;
 	}
 	public void setTestThreads(int testThreads) {
 		this.testThreads = testThreads;
 	}
 	
 	public long getTestThreadInitialDelayMillis() {
 		return testThreadInitialDelayMillis;
 	}
 	public void setTestThreadInitialDelayMillis(long testThreadInitialDelayMillis) {
 		this.testThreadInitialDelayMillis = testThreadInitialDelayMillis;
 	}
 
 	public long getTestThreadIntervalMillis() {
 		return testThreadIntervalMillis;
 	}
 	public void setTestThreadIntervalMillis(long testThreadIntervalMillis) {
 		this.testThreadIntervalMillis = testThreadIntervalMillis;
 	}
 }
