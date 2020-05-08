 package com.theminequest.MQCoreEvents.AreaEvent;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.BukkitEvents.CompleteStatus;
 import com.theminequest.MineQuest.EventsAPI.QEvent;
 import com.theminequest.MineQuest.Group.Group;
 import com.theminequest.MineQuest.Quest.Quest;
 
 public class AreaEvent extends QEvent {
 	
 	//private long delay;
 	private int taskid;
 	protected Location loc;
	protected double radiussq;
 	
 	private List<Player> player;
 	protected Group group;
 
 	public AreaEvent(long q, int e, String details) {
 		super(q, e, details);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.EventsAPI.QEvent#parseDetails(java.lang.String[])
 	 * [0] DELAY in MS (depreciated)
 	 * [1] Task #
 	 * [2] X
 	 * [3] Y
 	 * [4] Z
 	 * [5] Radius
 	 */
 	@Override
 	public void parseDetails(String[] details) {
 		//delay = Long.parseLong(details[0]);
 		taskid = Integer.parseInt(details[1]);
 		int X = Integer.parseInt(details[2]);
 		int Y = Integer.parseInt(details[3]);
 		int Z = Integer.parseInt(details[4]);
		radiussq = Math.pow(Double.parseDouble(details[5]),2);
 		Quest q = MineQuest.questManager.getQuest(getQuestId());
 		loc = new Location(Bukkit.getWorld(q.details.world),X,Y,Z);
 		group = MineQuest.groupManager.getGroup(MineQuest.groupManager.indexOfQuest(q));
 		player = new ArrayList<Player>();
 	}
 
 	@Override
 	public boolean conditions() {
 		List<Player> py = group.getPlayers();
 		for (Player p : py){
 			if (!p.getWorld().getName().equals(group.getQuest().getWorld()))
 				continue;
			if (p.getLocation().distanceSquared(loc)<=radiussq)
 				player.add(p);
 		}
 		if (player.size()>=group.getPlayers().size())
 			return true;
 		return false;
 	}
 
 	@Override
 	public CompleteStatus action() {
 		return CompleteStatus.SUCCESS;
 		
 	}
 
 	@Override
 	public Integer switchTask() {
 		return taskid;
 	}
 
 }
