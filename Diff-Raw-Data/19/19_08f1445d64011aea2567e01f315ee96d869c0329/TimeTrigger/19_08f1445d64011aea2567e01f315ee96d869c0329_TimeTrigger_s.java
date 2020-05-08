 package com.wolvencraft.prison.mines.triggers;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.configuration.serialization.SerializableAs;
 
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.mines.MineCommand;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.Util;
 
 @SerializableAs("TimeTrigger")
 public class TimeTrigger implements BaseTrigger {
 	
 	private long period;
 	private long next;
 	private String mine;
 	
 	private boolean canceled;
 	
 	public TimeTrigger(Mine mineObj, long period) {
 		mine = mineObj.getId();
 		this.period = period * 20;
		this.next = period;
 		
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
 	
 	/**
 	 * Bulk of the task. All timed actions go here.
 	 */
 	public void run() {
 		Mine mineObj = Mine.get(mine);
 		if(mineObj == null) return;
 		if(!mineObj.hasParent()) {
 			List<Integer> warnTimes = mineObj.getWarningTimes();
 			
 			if(!mineObj.getSilent() && mineObj.getWarned() && warnTimes.indexOf(next) != -1)
 				Message.broadcast(Util.parseVars(PrisonMine.getLanguage().RESET_WARNING, mineObj));
 			
 			if(next <= 0) {
 				MineCommand.RESET.run(mineObj.getName());
 				next = period;
 			} else {
 				next -= PrisonMine.getSettings().TICKRATE;
 			}
 		}
 	
 		if(mineObj.getCooldown() && mineObj.getCooldownEndsIn() > 0)
 			mineObj.updateCooldown(PrisonMine.getSettings().TICKRATE);
 	}
 	
 	public void cancel() {
 		Mine.get(mine).removeTrigger("time");
 		canceled = true;
 	}
 	
 	public String getName() 	{ return "PrisonMine:TimeTrigger:" + mine; }
 	public String getId() 		{ return "time"; }
 	public boolean getExpired() { return canceled; }
 	
 	public int getPeriod() 		{ return (int)(period / 20); }
 	public int getNext() 		{ return (int)(next / 20); }
 	
 	public void setPeriod(int period) { this.period = period * 20; }
 }
