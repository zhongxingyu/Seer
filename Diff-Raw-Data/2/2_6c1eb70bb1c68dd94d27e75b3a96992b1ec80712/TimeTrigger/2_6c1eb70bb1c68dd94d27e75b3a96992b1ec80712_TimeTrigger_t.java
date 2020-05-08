 package com.wolvencraft.prison.mines.triggers;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.configuration.serialization.SerializableAs;
 
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.ResetTrigger;
 import com.wolvencraft.prison.mines.util.Util;
 
 @SerializableAs("TimeTrigger")
 public class TimeTrigger implements BaseTrigger {
 	
 	private long period;
 	private long next;
 	private String mine;
 	
 	private boolean canceled;
 	
 	public TimeTrigger(Mine mineObj, long period) {
 		mine = mineObj.getId();
 		this.period = period * 20L;
 		this.next = this.period;
 		
 		canceled = false;
 		
 		PrisonSuite.addTask(this);
 	}
 	
 	public TimeTrigger(Map<String, Object> map) {
 		mine = (String)map.get("mine");
 		period = Long.parseLong((String)map.get("period"));
 		next = Long.parseLong((String)map.get("next"));
 		
 		canceled = false;
 		
 		PrisonSuite.addTask(this);
 	}
 	
 	public Map<String, Object> serialize() {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("mine", mine);
 		map.put("period", Long.toString(period));
 		map.put("next", Long.toString(next));
 		return map;
 	}
 	
 	public void run() {
 		Mine mineObj = Mine.get(mine);
 		if(mineObj == null) {
 			Message.log(Level.SEVERE, "Mine " + mine + " was not found, but its TimeTrigger still exists");
 			return;
 		}
 		
 		if(!mineObj.hasParent()) {
 			List<Integer> warnTimes = mineObj.getWarningTimes();
 			
			if(!mineObj.getSilent() && mineObj.getWarned() && warnTimes.indexOf((int)(next / 20)) != -1)
 				Message.broadcast(Util.parseVars(PrisonMine.getLanguage().RESET_WARNING, mineObj));
 
 			next -= PrisonMine.getSettings().TICKRATE;
 			
 			if(next <= 0L) {
 				Message.debug("+---------------------------------------------");
 				Message.debug("| Mine " + mine + " is resetting. Reset report:");
 				Message.debug("| Reset cause: timer has expired (" + next +" / " + period + ")");
 				CommandManager.RESET.run(mineObj.getId());
 				resetTimer();
 				Message.debug("| Updated the timer (" + next +" / " + period + ")");
 				Message.debug("| Reached the end of the report for " + mine);
 				Message.debug("+---------------------------------------------");
 			}
 		} else if(next <= 0L) Message.debug("Mine " + mine + " has a parent, ignoring it.");
 	
 		if(mineObj.getCooldown() && mineObj.getCooldownEndsIn() > 0)
 			mineObj.updateCooldown(PrisonMine.getSettings().TICKRATE);
 	}
 	
 	public void cancel() { Message.debug("Cancelling task: " + getName()); canceled = true; }
 	
 	public String getName() 	{ return "PrisonMine:TimeTrigger:" + mine; }
 	public ResetTrigger getId() { return ResetTrigger.TIME; }
 	public boolean getExpired() { return canceled; }
 	
 	public int getPeriod() 		{ return (int)(period / 20); }
 	public int getNext() 		{ return (int)(next / 20); }
 	public void resetTimer()	{ next = period; }
 	public void setPeriod(int period) {
 		this.period = period * 20L;
 		if(this.next > period) this.next = this.period;
 	}
 }
