 package cloudprofilingmonitor;
 
 /**
  *
  * @author christoph
  */
 public class SimulationDefaults {
     /** Settings for the simulation itself **/
     public static final double STEP_DELAY = 1;
 
     /** Default thresholds for VM creation and deletion **/
     public static final int MAX_THRESHOLD_CPU = 70;
     public static final int MIN_THRESHOLD_CPU = 30;
     public static final int MAX_THRESHOLD_MEM = 70;
     public static final int MIN_THRESHOLD_MEM = 30;
    public static final int MAX_THRESHOLD_BW = 70;
    public static final int MIN_THRESHOLD_BW = 30;
     public static final int MIN_THRESHOLD_HDD = 0;
     public static final int MAX_THRESHOLD_HDD = 0;
 }
