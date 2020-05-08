 package io;
 
 import gui.Log;
 import gui.Stats;
 
 import java.net.URL;
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 import net.GetHtml;
 
 public class CachePrune {
	GetHtml getHtml = new GetHtml();
 	URL testAliveUrl;
 	int refreshInterMin = 1;
 	int startupDelayMin = 1;
 	int maximumAgeMin = 1;
 	Timer cachePruneTimer;
 	MySQLaid sql;
 
 	static final Logger logger = Logger.getLogger(CachePrune.class.getName());
 
	public CachePrune(MySQLaid sql, URL testAliveUrl, int refreshInterMin, int startupDelayMin, int maximumAgeMin) {
 		this.testAliveUrl = testAliveUrl;
 
 		this.refreshInterMin = refreshInterMin * 60 * 1000;
 		this.startupDelayMin = startupDelayMin * 60 * 1000;
 		this.maximumAgeMin = maximumAgeMin;
 	}
 
 	public boolean start(){
 		if(cachePruneTimer != null)
 			return false;
 
 		cachePruneTimer = new Timer("CachePrune Timer");
 		cachePruneTimer.scheduleAtFixedRate(new CachePruneWorker() , startupDelayMin, refreshInterMin);
 
 		return true;
 	}
 
 	public void stop(){
 		if(cachePruneTimer == null)
 			return;
 		cachePruneTimer.cancel();
 		cachePruneTimer = null;
 	}
 
 	class CachePruneWorker extends TimerTask{
 
 		@Override
 		public void run() {
 			try{
 				if(getHtml.getResponse(testAliveUrl) != 200){
 					String message = "Could not verify that client is online, skipping cache prune.";
 					logger.warning(message);
 					Log.add(message);
 					return;
 				}
 
 
 
 				sql.pruneCache(maxAge(maximumAgeMin)); // delete keys that are older than maximumAgeMin
 				Stats.setCacheSize(sql.size(MySQLtables.Cache)); // update GUI
 
 			}catch (Exception e){
 				String message = "Failed to prune cache: "+e.getMessage();
 				logger.warning(message);
 				Log.add(message);
 			}
 		}
 
 	}
 
 	private long maxAge(int timeInMin){
 		//TODO replace this with SQL
 		Calendar exp = Calendar.getInstance();
 		exp.add(Calendar.MINUTE, -timeInMin);
 
 		return exp.getTimeInMillis();
 	}
 }
