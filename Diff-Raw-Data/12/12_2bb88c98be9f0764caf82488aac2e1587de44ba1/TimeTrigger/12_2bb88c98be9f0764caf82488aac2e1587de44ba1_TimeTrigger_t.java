 package com.wolvencraft.prison.mines.triggers;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.configuration.serialization.SerializableAs;
 
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.AutomaticResetRoutine;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.ResetTrigger;
 import com.wolvencraft.prison.mines.util.Util;
 
 @SerializableAs("TimeTrigger")
 public class TimeTrigger implements BaseTrigger {
 	
 	private long period;
 	private long next;
 	private Mine mine;
 	
 	private boolean canceled;
 	
 	/**
 	 * Default constructor for the TimeTrigger
 	 * @param mine Mine object associated with the trigger
 	 * @param period Reset period, in seconds
 	 */
 	public TimeTrigger(Mine mine, int period) {
 		this.mine = mine;
 		this.period = this.next = period * 20L;
 		
 		canceled = false;
 		
 		PrisonSuite.addTask(this);
 	}
 	
 	/**
 	 * Deserializing constructor for TimeTrigger
 	 * @param map Map of trigger data
 	 */
 	public TimeTrigger(Map<String, Object> map) {
 		mine = Mine.get((String) map.get("mine"));
 		period = Long.parseLong((String)map.get("period"));
 		next = Long.parseLong((String)map.get("next"));
 		
 		canceled = false;
 		
 		PrisonSuite.addTask(this);
 	}
 	
 	/**
 	 * Serialization method for the trigger
 	 */
 	public Map<String, Object> serialize() {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("mine", mine.getId());
 		map.put("period", Long.toString(period));
 		map.put("next", Long.toString(next));
 		return map;
 	}
 	
 	/**
 	 * Main trigger method. Run every TICKRATE, defined in the config
 	 */
 	public void run() {
 		if(mine == null) {
 			Message.log(Level.SEVERE, "Mine " + mine + " was not found, but its TimeTrigger still exists");
 			return;
 		}
 		
 		if(mine.getCooldown() && mine.getCooldownEndsIn() > 0) {
 			mine.updateCooldown(PrisonMine.getSettings().TICKRATE);
 		}
 		
 		if(mine.hasParent()) return;
 
 		next -= PrisonMine.getSettings().TICKRATE;
 		
 		if(next <= 0L) {
 			Message.debug("+---------------------------------------------");
			Message.debug("| Mine " + mine.getId() + " is resetting. Reset report:");
 			Message.debug("| Reset cause: timer has expired (" + next +" / " + period + ")");
 			AutomaticResetRoutine.run(mine);
 			Message.debug("| Updated the timer (" + next +" / " + period + ")");
			Message.debug("| Reached the end of the report for " + mine.getId());
 			Message.debug("+---------------------------------------------");
 		}
 		
 		List<Integer> warnTimes = mine.getWarningTimes();
 		if(!mine.getSilent() && mine.getWarned() && warnTimes.indexOf((int)(next / 20)) != -1)
 			Message.broadcast(Util.parseVars(PrisonMine.getLanguage().RESET_WARNING, mine));
 	}
 	
 	/**
 	 * Tags the task to expire during the next run
 	 */
 	public void cancel() { canceled = true; }
 	
 	public String getName() 	{ return "PrisonMine:TimeTrigger:" + mine; }
 	public ResetTrigger getId() { return ResetTrigger.TIME; }
 	public boolean getExpired() { return canceled; }
 	
 	public int getPeriod() 		{ return (int)(period / 20L); }
 	public int getNext() 		{ return (int)(next / 20L); }
 	public void resetTimer()	{ next = period; }
 	
 	public void setPeriod(int period) {
 		this.period = period * 20L;
 		if(this.next > period) this.next = period * 20L;
 	}
 }
