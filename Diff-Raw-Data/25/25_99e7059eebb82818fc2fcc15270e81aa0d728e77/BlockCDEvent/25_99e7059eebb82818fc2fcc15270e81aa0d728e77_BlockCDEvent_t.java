 package com.theminequest.MQCoreEvents.BlockEvent;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 
 import com.theminequest.MineQuest.API.CompleteStatus;
 import com.theminequest.MineQuest.API.Events.QuestEvent;
 import com.theminequest.MineQuest.API.Quest.QuestDetails;
 
 public class BlockCDEvent extends QuestEvent {
 
 	private long firstdelay;
 	private long seconddelay;
 	private Location loc;
 	private int typeid;
 	private long starttime;
 	private int step;
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#parseDetails(java.lang.String[])
 	 * Destroy, then create
 	 * [0]: first delay in MS before create
 	 * [1]: second delay is MS after create before destroy
 	 * [2]: X
 	 * [3]: Y
 	 * [4]: Z
 	 * [5]: Type ID 
 	 */
 	@Override
 	public void parseDetails(String[] details) {
 		firstdelay = Long.parseLong(details[0]);
 		seconddelay = Long.parseLong(details[1]);
 		String worldname = getQuest().getDetails().getProperty(QuestDetails.QUEST_WORLD);
		World w = Bukkit.getWorld(worldname);
		int x = Integer.parseInt(details[2]);
		int y = Integer.parseInt(details[3]);
		int z = Integer.parseInt(details[4]);
		loc = new Location(w,x,y,z);
 		typeid = Integer.parseInt(details[5]);
 		step = 0;
 		starttime = System.currentTimeMillis();
 	}
 
 	@Override
 	public boolean conditions() {
 		if (step==0){
 			if (starttime+firstdelay<=System.currentTimeMillis()){
 				loc.getBlock().setTypeId(typeid);
 				step++;
 				starttime = System.currentTimeMillis();
 			}
 			return false;
 		}
 		if (starttime+seconddelay<=System.currentTimeMillis()){
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public CompleteStatus action() {
 		boolean result = loc.getBlock().setTypeId(0);
 		if (result)
 			return CompleteStatus.SUCCESS;
 		else
 			return CompleteStatus.FAILURE;
 	}
 
 	@Override
 	public Integer switchTask() {
 		return null;
 	}
 }
