 package TLTTC;
 
 public class TrainController
 {
   private int trainID;
   private TrainModel tm;
   
   private boolean underground = false;
   private boolean inStation = false;
   private String nextStation = "";
   private boolean daytime = false; // True = day, False = night
   private boolean doorsOpen = false;
   private boolean lightsOn = false;
   private boolean engineFail = false;
   private boolean signalPickupFail = false;
   private boolean brakeFail = false;
   
   private final double KP = 5000; // Proportional gain
   private double ek = 0; // Proportional error
   private final double T = 0.1; // Sample period of train model (0.1 seconds)
   private final double KI = 1000; // Integral gain
   private double uk = 0; // Integral error
   private double power = 0; // Power of train
   private final double trainMaxPower = 120000.0; // Maximum power of train (120 kW)
   
   private double trainOperatorVelocity = 0; // Velocity sent from train operator
   private double ctcOperatorVelocity = 0; // Velocity sent from CTC operator
   private double velocity = 0; // Current velocity of train
   private double trackLimit = 0; // Track's speed limit
   private final double trainLimit = 19.4444; // Train's speed limit (70 km/hr = 19.44 m/s)
   
   private double fixedBlockAuth = 0; // Fixed block authority
   private double ctcFixedBlockAuth = 0; // Fixed block authority sent from CTC operator
   private double movingBlockAuth = 0; // Moving block authority sent from MBO
   private double ctcMovingBlockAuth = 0; // Moving block authority sent from CTC operator
   
   
   public TrainController(int id, TrainModel t)
   {
     trainID = id;
     tm = t;
     // Todo: connect to GPS here
     
     // Test variables -- Remove later
     velocity = 5;
     trainOperatorVelocity = 10;
     ctcOperatorVelocity = 1000;
     trackLimit = 15;
     
     fixedBlockAuth = 10;
     ctcFixedBlockAuth = 1400;
     ctcMovingBlockAuth = 1400;
     movingBlockAuth = 1400;
   }
   
   
   public void setPower() // this method is called whenever an authority or new speed limit is received
   {
     // get failure flags and update UI
     // get time for UI
     
     if (engineFail || signalPickupFail || brakeFail) // If train failure, stop train
     {
       tm.setPower(0.0);
     }
     else
     {
       velocity = tm.getVelocity();
       double authority = Math.min(Math.min(fixedBlockAuth, ctcFixedBlockAuth), Math.min(movingBlockAuth, ctcMovingBlockAuth)); // Selects safest authority
       // Train's maximum non-emergency deceleration is -1.2098 m/s^2.
       // Using vf^2 = vi^2 + 2ad = 0 (final velocity cannot be > 0), vi = sqrt(-2ad) = sqrt(2*1.2098*authority)
       double authorityVelocityLimit = Math.sqrt(2.4196*authority);
       
       double velocitySetpoint = Math.max(trainOperatorVelocity, ctcOperatorVelocity); // Selects faster of two velocities.
       if (velocitySetpoint > Math.min(Math.min(trackLimit, trainLimit), authorityVelocityLimit)) // If the operator sends a dangerous velocity,
       {
         velocitySetpoint = Math.min(Math.min(trackLimit, trainLimit), authorityVelocityLimit); // set to next highest allowable velocity
       }
       
       if (power < trainMaxPower)
       {
         uk = uk + (T/2)*(ek + (velocitySetpoint - velocity));
       }
       ek = velocitySetpoint - velocity; // kth sample of velocity error
       power = ((KP*ek)+(KI*uk));
       tm.setPower(power);
     }
   }
   
   
   public void setDoors() // this method is called every time the train enters a new block
   {
     velocity = tm.getVelocity();
     // get door status from train model
     
     if (velocity == 0 && inStation && !doorsOpen)
     {
       // open doors
     }
     else if (velocity != 0 && doorsOpen)
     {
       // close doors
     }
   }
   
   public void setLights() // this method is called every time the train enters a new block
   {
     // get time from train model and set daytime variable
     
     if (!daytime || underground && !lightsOn)
     {
       // turn on lights
       // change UI
     }
     else if (daytime && !underground && lightsOn)
     {
       // turn off lights
       // change UI
     }
   }
   
   
   public void announceStation() // this method is called whenever a station name is sent to the train controller
   {
     // announce station on train model
     // update UI so that button cannot be pressed
   }
   
   
   public void setMovingBlockAuth(double m)
   {
     movingBlockAuth = m;
    //sendPower();
     setPower();
   }
   
   
   public void setCtcMovingBlockAuth(double m)
   {
     ctcMovingBlockAuth = m;
    //sendPower();
     setPower();
   }
   
   
   public void setFixedBlockAuth(double f)
   {
     fixedBlockAuth = f;
    //sendPower();
     setPower();
   }
   
   
   public void setCtcFixedBlockAuth(double f)
   {
     ctcFixedBlockAuth = f;
    //sendPower();
     setPower();
   }
   
   
   public void setCtcOperatorVelocity(double v)
   {
     ctcOperatorVelocity = v;
    //sendPower();
     setPower();
   }
   
   
   public void setTrainOperatorVelocity(double v)
   {
     trainOperatorVelocity = v;
    //sendPower();
     setPower();
   }
   
   
   public void setTrackLimit(double v)
   {
     trackLimit = v;
    //sendPower();
     setPower();
   }
   
   
   public void setUnderground(boolean u)
   {
     underground = u;
     setLights();
   }
   
   
   public void setInStation(boolean i){
     inStation = i;
     setDoors();
   }
   
   
   public void setNextStation(String s)
   {
     nextStation = s;
     announceStation();
   }
 }
