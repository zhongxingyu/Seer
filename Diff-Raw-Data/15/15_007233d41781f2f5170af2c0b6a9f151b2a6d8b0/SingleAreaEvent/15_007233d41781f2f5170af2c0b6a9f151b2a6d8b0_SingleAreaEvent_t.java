 package com.theminequest.MQCoreEvents.AreaEvent;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import com.theminequest.MineQuest.BukkitEvents.CompleteStatus;
 import com.theminequest.MineQuest.EventsAPI.QEvent;
 
 public class SingleAreaEvent extends AreaEvent {
 
 	public SingleAreaEvent(long q, int e, String details) {
 		super(q, e, details);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public boolean conditions() {
 		List<Player> py = group.getPlayers();
 		for (Player p : py){
 			if (!p.getWorld().getName().equals(group.getQuest().getWorld()))
 				continue;
			if (p.getLocation().distance(loc)<=radius)
 				return true;
 		}
 		return false;
 	}
 
 }
