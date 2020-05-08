 package de.kumpelblase2.dragonslair.api;
 
 import java.util.*;
 import org.bukkit.entity.Player;
 import de.kumpelblase2.dragonslair.DragonsLairMain;
 
 public class PlayerQueue
 {
	private Set<QueuedPlayer> players = new HashSet<QueuedPlayer>();
 	
 	public ActiveDungeon start(Dungeon dungeon)
	{	
 		if(!this.hasEnoughPeople(dungeon))
 			return null;
 		
 		List<String> startingPlayers = new ArrayList<String>();
 		Iterator<QueuedPlayer> it = players.iterator();
 		while(it.hasNext())
 		{
 			QueuedPlayer player = it.next();
 			if(player.getDungeon().equals(dungeon.getName()))
 			{
				if(dungeon.getMaxPlayers() != 0 && startingPlayers.size() <= dungeon.getMaxPlayers())
 					break;
 				
 				startingPlayers.add(player.getPlayer().getName());
 				it.remove();
 			}
 		}
		
 		return DragonsLairMain.getDungeonManager().startDungeon(dungeon.getID(), startingPlayers.toArray(new String[0]));
 	}
 	
 	public boolean hasEnoughPeople(Dungeon dungeon)
 	{
 		return this.getQueueForDungeon(dungeon).size() >= dungeon.getMinPlayers();
 	}
 	
 	public Set<QueuedPlayer> getQueueForDungeon(Dungeon dungeon)
 	{
 		Set<QueuedPlayer> tempList = new HashSet<QueuedPlayer>();
 		for(QueuedPlayer player : this.players)
 		{
 			if(player.getDungeon().equals(dungeon.getName()))
 				tempList.add(player);
 		}
 		return tempList;
 	}
 	
 	public void queuePlayer(String dungeon, Player p)
 	{
 		if(this.isInQueue(p))
 			return;
 		
 		this.players.add(new QueuedPlayer(dungeon, p));
 	}
 	
 	public boolean isInQueue(Player p)
 	{
 		for(QueuedPlayer qp : this.players)
 		{
 			if(qp.equals(p))
 				return true;
 		}
 		return false;
 	}
 }
