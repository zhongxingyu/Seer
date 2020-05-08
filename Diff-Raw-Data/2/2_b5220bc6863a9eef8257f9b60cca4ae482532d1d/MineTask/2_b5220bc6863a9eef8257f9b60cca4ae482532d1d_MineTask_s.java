 package com.wolvencraft.prison.mines;
 
 import java.util.List;
 
 import com.wolvencraft.prison.hooks.TimedTask;
 import com.wolvencraft.prison.mines.mine.DisplaySign;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.Util;
 
 public class MineTask extends TimedTask {
 	
 	long period;
 	
 	public MineTask(long ticks) {
 		super(ticks);
 		period = ticks;
 	}
 	
 	public void run() {
 		for(Mine curMine : PrisonMine.getMines()) {
 			if(curMine.getAutomatic() && Mine.get(curMine.getParent()) == null) {
 				int nextReset = curMine.getResetsInSafe();
 				List<Integer> warnTimes = curMine.getWarningTimes();
 				
 				if(!curMine.getSilent() && curMine.getWarned() && warnTimes.indexOf(new Integer(nextReset)) != -1)
					Message.broadcast(Util.parseVars(PrisonMine.getLanguage().RESET_AUTOMATIC, curMine));
 				
 				if(nextReset <= 0) {
 					MineCommand.RESET.run(curMine.getName());
 				}
 				curMine.updateTimer(PrisonMine.getSettings().TICKRATE);
 			}
 		
 			if(curMine.getCooldown() && curMine.getCooldownEndsIn() > 0)
 				curMine.updateCooldown(PrisonMine.getSettings().TICKRATE);
 		}
 		DisplaySign.updateAll();
 	}
 	
 	public String getName() { return "MineTask"; }
 }
