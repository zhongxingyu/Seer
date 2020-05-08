 package gameCommons.balanceSettings;
 
 import gameCommons.balanceSettings.DataLevel;
import gameCommons.balanceSettings.jdbcDB;
import gameCommons.system.Trace;
 
 
 /*
 Levels level = new Levels();
 level(5).xpNeeded
 level.5.xpNeeded 
 level.with(5).xpNeeded
 */
 public class Levels {
     protected DataLevel settings = null;
 
 	public Levels() {
         this.settings = create();
     }
 
     public DataItemLevel with(int key) {
        return settings.cached.get(key);
     }
 
     protected DataLevel create() {
        Trace t = new Trace();
        return new DataLevel(new jdbcDB(t), t);
     }
 }
