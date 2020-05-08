 package louis;
 
 import louis.driver.*;
 
 import robocode.AdvancedRobot;
 import robocode.DeathEvent;
 import robocode.HitByBulletEvent;
 import robocode.HitRobotEvent;
 import robocode.HitWallEvent;
 import robocode.RobocodeFileOutputStream;
 import robocode.ScannedRobotEvent;
 import robocode.WinEvent;
 import static robocode.util.Utils.normalRelativeAngleDegrees;
 
 import java.awt.*;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 
 
 /**
  * TrackFire - a sample robot by Mathew Nelson, and maintained by Flemming N. Larsen
  * <p/>
  * Sits still.  Tracks and fires at the nearest robot it sees
  */
 public class QRobot extends AdvancedRobot {
 
     private static final String QLearningDataFile = "count.dat";
 
     double gunTurnAmt; // How much to turn our gun when searching
     private String trackName = null;
     
     double bulletPower = 1;  /*Narchi*/
     boolean hitEnemy; /*Narchi*/
     
     private RobotState mCurrentState;
     int dist = 50; // distance to move when we're hit
     private RobotState mPreviousState;
 	Integer mPreviousAction;
 	long mPreviousActionStartedTurn;
     
     private ScannedRobotEvent lastseen = null;
     private DataInterface mDataInterface;
 	
     public void readTable() throws Exception{
         mDataInterface = new DataInterface();
         try {
             BufferedReader r = new BufferedReader(new FileReader(getDataFile("count.dat")));
             String data_row;
             while( (data_row = r.readLine()) !=  null){
 				mDataInterface.addDataRow(data_row);
             }
             r.close();
         } catch (IOException e) {
 			mDataInterface.initAllData();
             e.printStackTrace();
         } catch (NumberFormatException e) {
 			mDataInterface.initAllData();
             e.printStackTrace();
         }
         //mDataInterface.printAllData();
     }
 		
 	Driver driver = null;
 	
 	Driver chooseDriver() {
		stop();
		
 		mPreviousState = mCurrentState;
 		mCurrentState = getStateByCurrentEnvironment(DefVariable.STATE_START);		
 		
 		Integer actionId = mDataInterface.decideStratgyFromEnvironmentState(mCurrentState);
 		
 		Driver d = DriverManager.getDriver(actionId, this);
 		d.init();
 		
 		mPreviousAction = actionId;
 		mPreviousActionStartedTurn = getTime();
 
 		return d;
 	}
 	
 	public void checkDriverExpire() {
 		// Test if driver is expired
 		if (getTime() - mPreviousActionStartedTurn > 200) {
 			if(mPreviousAction != DefVariable.NOACTION && mPreviousState != null){
 				executeQLearningFunction(DefVariable.onDeathReward, 0, mPreviousState, mPreviousAction);
 			}
 			driver = chooseDriver();
 		}
 	}
     
 	public void run() {
         // Set colors
         setBodyColor(Color.pink);
         setGunColor(Color.pink);
         setRadarColor(Color.pink);
         setScanColor(Color.pink);
         setBulletColor(Color.pink);
 		
         try {
             readTable();
             //printRawTable();
             mCurrentState = new RobotState();
             mPreviousState = new RobotState();
 			mPreviousAction = DefVariable.NOACTION;
 			mPreviousActionStartedTurn = getTime();
         } catch (Exception e) {
             e.printStackTrace();
         }
 		
 		driver = chooseDriver();
 		
 		while (true) {
 			checkDriverExpire();			
 			driver.loop();
 		}
     }
 
     /**
      * onScannedRobot:  We have a target.  Go get it.
      */
     public void onScannedRobot(ScannedRobotEvent e) {
 		checkDriverExpire();			
     	driver.onScannedRobot(e);
     }
 	
     public void onHitRobot(HitRobotEvent e){
 		checkDriverExpire();			
         driver.onHitRobot(e);
     }
 	
     public void onHitByBullet(HitByBulletEvent e) {
 		checkDriverExpire();			
         driver.onHitByBullet(e);
     }
     
     public void onHitWall(HitWallEvent e) {
 		checkDriverExpire();			
 		driver.onHitWall(e);
     }
 	
     public void onWin(WinEvent e) {
 		if(mPreviousAction != DefVariable.NOACTION && mPreviousState != null){
             executeQLearningFunction(DefVariable.onWinReward, 0, mPreviousState, mPreviousAction);
         }
 		this.writeToFile(QLearningDataFile);
         turnRight(36000);
     }
 	
     public void onDeath(DeathEvent e){
 		if(mPreviousAction != DefVariable.NOACTION && mPreviousState != null){
             executeQLearningFunction(DefVariable.onDeathReward, 0, mPreviousState, mPreviousAction);
         }
 
 		this.writeToFile(QLearningDataFile);
     }
 	
 	private void writeToFile(String fileName) {
         //System.out.println("Writing File " + System.currentTimeMillis());
 		PrintStream w = null;
 		try {
 			w = new PrintStream(new RobocodeFileOutputStream(getDataFile(fileName)));
 			
             w.print(mDataInterface.DatatoString());
             w.close();
         } catch (IOException e1) {
             e1.printStackTrace(out);
         } finally {
 			if (w != null) {
 				w.close();
 			}
 		}
 	}
     
     private void executeQLearningFunction(int reward, double maxQvalue, RobotState previousState, Integer previousAction){
         double alpha = DefVariable.ALPHA;
         double gamma = DefVariable.GAMMA;
         double oldValue = mDataInterface.getQValueByStateAndAction(previousState, previousAction);
         double newValue = oldValue + alpha*( (double)reward + gamma*maxQvalue - oldValue);
         mDataInterface.updateQValueByStateAndAction(previousState, previousAction, newValue);
     }
     
     private RobotState getStateByCurrentEnvironment(int eventNumber){
         int numberEnemy = getOthers() > 1 ? 1 : 0;
         int zoneNumber = getAreaZone(getX(), getY());
         int powerLevel = getPowerLevel(getEnergy());
         int nearestDistance = getDistanceWithRobot(lastseen);
         int freshness = getTimeWithRobot(lastseen);
         int event = eventNumber;
 		
         RobotState state = new RobotState(zoneNumber, numberEnemy, powerLevel, nearestDistance, event, freshness);
         return state;
     }
     private int getAreaZone(double x, double y){
         return 0;
     }
     private int getDistanceWithRobot(ScannedRobotEvent e){
         
 /*        if(e == null){
             return 4;
         }
         double x = e.getDistance();
         if(x > 0 && x < 100){
             return 0;
         }
         else if(x >= 100 && x < 200){
             return 1;
         }
         else if(x>=200 && x<300){
             return 2;
         }
         else if(x>=300 && x<400){
             return 3;
         }
         else if(x>=400){
             return 4;
         }
         return -1;*/
 		
 		if (e == null) return 1;
 		double x = e.getDistance();
         if(x > 0 && x < 250){
             return 0;
         }
         else {
             return 1;
         }
 
     }
     
     private int getTimeWithRobot(ScannedRobotEvent e){
 /*        if (e == null) return 1;
         double diff = getTime() - e.getTime();
         if(diff > 0 && diff < 10){
             return 0;
         }
         else if(diff >= 10 && diff < 20){
             return 1;
         }
         else if(diff >= 20 && diff < 30){
             return 2;
         }
         else if(diff >= 30 && diff < 40){
             return 3;
         }
         else if(diff >= 40){
             return 4;
         }
         return -1;*/
 		if (e == null) return 1;
         double diff = getTime() - e.getTime();
         if(diff < 5){
             return 0;
         }
         else {
             return 1;
         }
     }
     private int getPowerLevel(double power){
         if (power < 50) return 0;
 		else return 1;
     }
 }               
 
