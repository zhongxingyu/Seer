 package ai;
 
 import db.DatabaseHandler;
 import model.Sheep;
 import util.Vec2;
 
 import java.sql.SQLException;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Mayacat
  * Date: 9/12/13
  * Time: 9:37 AM
  * To change this template use File | Settings | File Templates.
  */
 public class SheepDaemon extends Thread {
 
     private DatabaseHandler mHandler;
 
     private Timer timer;
 
     private ArrayList<Integer> mSheeps;
 
     //Hasing the IDs are safer than hashing the objects
     private HashMap<Integer, Vec2> velocities;
     private HashMap<Integer, Vec2> accelerations;
 
     private float multiplier = 1e-5f;
     private boolean lockEverything;
 
     public SheepDaemon(DatabaseHandler mHandler) {
         this.mHandler = mHandler;
         this.mSheeps = new ArrayList<Integer>();
         this.timer = new Timer("SheepDaemon", true);
         this.velocities = new HashMap<Integer,Vec2>();
         this.accelerations = new HashMap<Integer,Vec2>();
         this.lockEverything = false;
     }
 
     /**
      * Runs the thread, preferably after a .start() call
      */
     @Override
     public void run() {
         Random ran = new Random();
         for(int i = 0; i < mSheeps.size(); ++i){
 
             //Velocities and accelerations stored in range [-0.5f, 0.5f]
             this.velocities.put(this.mSheeps.get(i), new Vec2(ran.nextFloat() - 0.5f, ran.nextFloat() - 0.5f));
             this.accelerations.put(this.mSheeps.get(i), new Vec2(ran.nextFloat() - 0.5f, ran.nextFloat() - 0.5f));
         }
         scheduleAndMove();
     }
 
     /**
      * Schedulerfunction
      */
     public void scheduleAndMove(){
         TimerTask task = new TimerTask() {
             @Override
             public void run() {
                 scheduleAndMove();
             }
         };
         timer.schedule(task, 60*60*8);
         moveSheeps();
     }
 
     /**
      * Randomize accelerations
      */
     private void randomizeAccelerations(){
         if(lockEverything)
             return;
 
         lockEverything = true;
         Random ran = new Random();
         for(int i = 0; i < this.accelerations.size(); i++){
             Collection<Vec2> accs = this.accelerations.values();
             for(Vec2 vec : accs){
                 vec.x = ran.nextFloat();
                 vec.y = ran.nextFloat();
             }
         }
         lockEverything = false;
     }
 
     /**
      * Moves all sheeps and increments velocities by accelerations. Finishes off by randomizing accelerations
      */
     public void moveSheeps(){
         lockEverything = true;
         ArrayList<Vec2> sheepPositions = new ArrayList<Vec2>();
 
         for(int i = 0; i < this.mSheeps.size(); ++i){
             try {
                 sheepPositions.add(this.mHandler.getSheepPosition(mSheeps.get(i)));
             } catch (SQLException e) {
                 continue;
             }
         }
 
         for(int i = 0; i < this.mSheeps.size(); ++i){
             int id = this.mSheeps.get(i);
             Vec2 pos = sheepPositions.get(i);
             this.velocities.get(id).add(this.accelerations.get(id));
             try {
                 this.mHandler.setSheepPosition(id, pos.x, pos.y);
             } catch (SQLException e) {
                 continue;
             }
         }
         lockEverything = false;
         randomizeAccelerations();
     }
 
     /**
      * Gets sheepupdates from database
      * @param sheeps
      */
     public void updateSheeps(ArrayList<Sheep> sheeps){
         if(lockEverything)
             return;
 
         lockEverything = true;
         Random ran = new Random();
         ArrayList<Integer> newSheeps = new ArrayList<Integer>();
 
         for(Sheep sheep : sheeps){
             newSheeps.add(sheep.getId());
             if(this.mSheeps.contains(sheep.getId()))
                 continue;
             this.mSheeps.add(sheep.getId());
             this.velocities.put(sheep.getId(), new Vec2(ran.nextFloat(), ran.nextFloat()));
             this.accelerations.put(sheep.getId(), new Vec2(ran.nextFloat(), ran.nextFloat()));
 
         }
 
         //Remove sheeps no longer in the system
         for(Integer in : this.mSheeps){
             if(!newSheeps.contains(in)){
                 mSheeps.remove(in);
                 velocities.remove(in);
                 accelerations.remove(in);
             }
         }
         lockEverything = false;
 
     }
 }
