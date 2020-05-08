 package TLTTC;
 
 
 public class TrainController{
   public int trainID;
   
   public boolean underground = false;
   public boolean inStation = false;
   public String nextStation = "";
   public boolean stationAnnounced = false;
   public boolean daytime = false; // True = day, False = night
   public boolean doorsOpen = false;
   public boolean lightsOn = false;
   public boolean engineFail = false;
   public boolean signalPickupFail = false;
   public boolean brakeFail = false;
   
   public double KP = 5000; // Proportional gain
   public double ek = 0; // Proportional error
   public double histEk = 0; // Proportional error one time step back
   
   public double T = 0.1; // Sample period of train model (0.1 seconds)
   public double KI = 1000; // Integral gain
   public double uk = 0; // Integral error
   public double histUk = 0; // Integral error one time step back
   
   public double power = 0; // Power of train
   public double histPower = 0; // Power command one step back
   public double trainMaxPower = 120000.0; // Maximum power of train (120 kW)
   
   public double trainOperatorVelocity = 0; // Velocity sent from train operator
   public double ctcOperatorVelocity = 0; // Velocity sent from CTC operator
   public double velocity = 0; // Current velocity of train
   public double trackLimit = 0; // Track's speed limit
   public double trainLimit = 19.4444; // Train's speed limit (70 km/hr = 19.44 m/s)
   
   public double fixedBlockAuth = 0; // Fixed block authority
   public double ctcFixedBlockAuth = 1400; // Fixed block authority sent from CTC operator
   public double movingBlockAuth = 1400; // Moving block authority sent from MBO
   public double ctcMovingBlockAuth = 1400; // Moving block authority sent from CTC operator
   public double authority = 0;  // Overall authority of train
   public double histAuthority = 0; // Overall authority of train one time step back
   
   public double trackLength = 1400; // Track length
   
   public TrainControllerGUI gui; // GUI
   public TrainControllerModule module;
   public TrainModel tm;
   
   public TrainController(int id, TrainControllerModule mod, TrainModel t)
   {
     //gui = new TrainControllerGUI(mod);
     //gui.openGUI();
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
   
   public double setPower() // this method is called whenever an authority or new speed limit is received
   {
     // get failure flags and update UI
     // get time for UI 
     if (engineFail || signalPickupFail || brakeFail){
       return 0.0;
     }
     
     System.out.println("Current velocity = " + velocity + " m/s.");
     System.out.println("Current setpoint = " + trainOperatorVelocity + " m/s.");
     trainOperatorVelocity = Math.min(trainOperatorVelocity, ctcOperatorVelocity); // Selects safer of two velocities.
     if ((trainOperatorVelocity > trackLimit) || (trainOperatorVelocity > trainLimit)) // If the operator sends a dangerous velocity
     {
       trainOperatorVelocity = Math.min(trackLimit, trainLimit); // Set to next highest allowable velocity
     }
     authority = Math.min(fixedBlockAuth, Math.min(ctcFixedBlockAuth, Math.min(movingBlockAuth, ctcMovingBlockAuth))); // Selects safest authority
     if (authority > trackLength){ // If authority is unsafe, set it to the last authority
       authority = histAuthority;
     }
     
     ek = trainOperatorVelocity - velocity; // kth sample of velocity error
     if (histPower < trainMaxPower)
     {
       uk = histUk + (T/2)*(ek + histEk);
     }
     else
     {
       uk = histUk;
     }
     power = ((KP*ek)+(KI*uk));
     
     histAuthority = authority;
     histPower = power;
     histEk = ek;
     histUk = uk;
     System.out.println("Power command of " + power + " Watts sent.");
     return power;
   }
   
   
   public void setDoors(){ // this method is called every time the train enters a new block
     velocity = tm.getVelocity();
     // get door status from train model
     
     if (velocity == 0 && inStation && !doorsOpen){
       // open doors
     }
     else if (velocity != 0 && doorsOpen){
       // close doors
     }
   }
   
   public void setLights(){ // this method is called every time the train enters a new block
     // get time from train model and set daytime variable
     
     if (!daytime || underground && !lightsOn){
       // turn on lights
       // change UI
     }
     else if (daytime && !underground && lightsOn){
       // turn off lights
       // change UI
     }
   }
   
   
   public void announceStation(){ // this method is called whenever a station name is sent to the train controller
     if (!stationAnnouced){
       // announce station on train model
       // update UI so that button cannot be pressed
       stationAnnounced = true;
     }
   }
 }
