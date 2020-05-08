 package com.theminequest.events.area;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.theminequest.api.CompleteStatus;
 import com.theminequest.api.Managers;
 import com.theminequest.api.group.Group;
 import com.theminequest.api.platform.MQLocation;
import com.theminequest.api.platform.MQPlayer;
 import com.theminequest.api.quest.Quest;
 import com.theminequest.api.quest.QuestDetails;
 import com.theminequest.api.quest.event.DelayedQuestEvent;
 import com.theminequest.api.quest.event.UserQuestEvent;
 
 public class AreaEvent extends DelayedQuestEvent implements UserQuestEvent {
 	
 	private long delay;
 	private int taskid;
 	private MQLocation loc;
 	protected double radius;
 	
 	private List<MQPlayer> player;
 	protected Group group;
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Events.QEvent#parseDetails(java.lang.String[])
 	 * [0] DELAY in MS
 	 * [1] Task #
 	 * [2] X
 	 * [3] Y
 	 * [4] Z
 	 * [5] Radius
 	 */
 	@Override
 	public void setupArguments(String[] details) {
 		delay = Long.parseLong(details[0]);
 		taskid = Integer.parseInt(details[1]);
 		int X = Integer.parseInt(details[2]);
 		int Y = Integer.parseInt(details[3]);
 		int Z = Integer.parseInt(details[4]);
 		radius = Double.parseDouble(details[5]);
 		Quest q = getQuest();
 		loc = new MQLocation((String) q.getDetails().getProperty(QuestDetails.QUEST_WORLD),X,Y,Z);
 		group = Managers.getGroupManager().get(getQuest());
 		player = new ArrayList<MQPlayer>();
 	}
 
 	@Override
 	public CompleteStatus action() {
 		return CompleteStatus.SUCCESS;
 		
 	}
 
 	@Override
 	public Integer switchTask() {
 		return taskid;
 	}
 
 	@Override
 	public String getDescription() {
 		return "Head to " + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "! (" + radius + " block leeway)";
 	}
 	
 	public MQLocation getLocation() {
 		return loc;
 	}
 
 	@Override
 	public long getDelay() {
 		return delay;
 	}
 
 	@Override
 	public boolean delayedConditions() {
 		List<MQPlayer> py = group.getMembers();
		String worldname = getQuest().getDetails().getProperty(QuestDetails.QUEST_WORLD);
 		for (MQPlayer p : py){
			if (!p.getLocation().getWorld().equals(worldname))
 				continue;
 			if (p.getLocation().distance(loc)<=radius)
 				player.add(p);
 		}
 		if (player.size()>=group.getMembers().size())
 			return true;
 		return false;
 	}
 
 }
