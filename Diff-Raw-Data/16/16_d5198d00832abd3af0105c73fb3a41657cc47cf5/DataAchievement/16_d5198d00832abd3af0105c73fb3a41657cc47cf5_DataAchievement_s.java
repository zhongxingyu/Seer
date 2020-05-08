 package gameCommons.balanceSettings;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 
 import gameCommons.balanceSettings.DataItemAchievement;
 import gameCommons.balanceSettings.DataStore;
 import gameCommons.system.Trace;
 
 
 public class DataAchievement {
 	protected DataStore ds;
     protected Trace t;
 	public HashMap<String,DataItemAchievement> cached = new HashMap<String,DataItemAchievement>();
 	public HashMap<String,DataItemAchievement> cachedByAward = new HashMap<String,DataItemAchievement>();
 	
     public DataAchievement() {}
 	public DataAchievement(DataItemAchievement achievement, Trace t) {
         cache(achievement);
     }
 	public DataAchievement(DataStore ds, Trace t) {
         this.ds = ds;
         this.t = t;
         refresh();
     }
 
     public void refresh() {
 	    DataItemAchievement achievement = null;
         ResultSet db_res = retrieve(ds);
 	    try {
 		    while (db_res.next()) {
 		        achievement = new DataItemAchievement();
 		        achievement.name = db_res.getString("Name");
 		        achievement.awardName = db_res.getString("AwardName");
 		        achievement.threshold = db_res.getInt("Threshold");
 
                 cache(achievement);
 		    }
 	    } catch (SQLException e) {}
 	}
     
     protected void cache(DataItemAchievement achievement) {
         cached.put(achievement.name, achievement);
        cachedByAward.put(achievement.awardName"), achievement);
     }
 
    protected ResultSet retrieve(ds) {
 	    String db_sql_read = 
 	    	" SELECT Name, Threshold, AwardName "
 	        + " FROM Achievement ";
 	    return ds.query(db_sql_read, "read", null);
     }
 }
