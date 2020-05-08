 package de.skuzzle.polly.sdk.time;
 
 
 
 
 /**
  * <p>This class is used to modify the global system time used by all polly components
  * and plugins.</p>
  * 
  * <p>Using this class, you can modify the system time in order to debug certain 
 * components. You can use {@link Time#setProvider(TimeProvider)} to install
  * your time provider instance. Note that this method will have no effect if polly is
  * not running in debug mode.</p> 
  * 
  * <p>Note that this class will not change the system time of the machine that polly is
  * running on. Instead it modifies the return value of 
 * {@link Time#currentTimeMillis()}, so all components which use this method to get
  * the current time will use the modified time.</p>
  * 
  * @author Simon
  * @since 0.7
  */
 public interface TimeProvider {
 
     /**
      * This method should return a modified system time in milliseconds. All polly 
     * components which use {@link Time#currentTime()} to retrieve the current
      * time will get the time computed by this method. So it can be used to debug
      * time dependent behavior.
      * 
      * @return The current system time in milliseconds
      * @since 0.7
      */
     public long currentTimeMillis();
 }
