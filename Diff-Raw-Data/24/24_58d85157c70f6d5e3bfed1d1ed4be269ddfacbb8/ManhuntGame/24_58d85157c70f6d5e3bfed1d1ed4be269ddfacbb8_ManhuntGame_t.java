 package com.deaboy.manhunt.game;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
import org.bukkit.World.Environment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 
 import com.deaboy.manhunt.ManhuntPlugin;
 import com.deaboy.manhunt.ManhuntUtil;
 import com.deaboy.manhunt.chat.ChatManager;
 import com.deaboy.manhunt.game.events.*;
 import com.deaboy.manhunt.lobby.Lobby;
 import com.deaboy.manhunt.lobby.Team;
 import com.deaboy.manhunt.map.SpawnType;
 
 public class ManhuntGame extends Game implements Listener
 {
 	//---------------- Properties ----------------//
 	Timeline timeline;
 	
 	
 	//---------------- Constructors ----------------//
 	public ManhuntGame(Lobby lobby)
 	{
 		super(lobby);
 		
 		
 	}
 	
 	
 	//---------------- Public methods ----------------//
 	@Override
 	public void startGame()
 	{
 		this.setMap(getLobby().getCurrentMap());
 		setStage(GameStage.PREGAME);
 		// Initiate timeline
 		
 		timeline = generateTimeline();
 		

		if (getWorld().getEnvironment() == Environment.NORMAL)
			ManhuntUtil.transitionWorldTime(getWorld(), 12000 - getLobby().getSettings().TIME_SETUP.getValue() * 1200 - 320, new Runnable(){ public void run(){ timeline.run(); }});
		else
			timeline.run();
 		
 		startListening();
 	}
 	@Override
 	public void stopGame()
 	{
 		ManhuntUtil.cancelWorldTimeTransition(getWorld());
 		
 		timeline.stop();
 		
 		for (Player p : getOnlinePlayers(Team.HUNTERS, Team.PREY, Team.SPECTATORS))
 		{
 			p.teleport(getLobby().getRandomSpawnLocation());
 		}
 		
 		stopListening();
 		
 		setStage(GameStage.INTERMISSION);
 	}
 	@Override
 	public void endGame()
 	{
 		int hunters = getPlayerNames(Team.HUNTERS).size();
 		int prey = getPlayerNames(Team.PREY).size();
 		
 		getLobby().broadcast(ChatManager.bracket1_ + "The Game is OVER!" + ChatManager.bracket2_);
 		// Neither team won.
 		if (hunters == 0 && prey == 0)
 		{
 			getLobby().broadcast(ChatManager.leftborder + "Both teams died!");
 			getLobby().broadcast(ChatManager.leftborder + ChatColor.RED + "You all FAIL!");
 		}
 		// The hunters won
 		else if (prey == 0)
 		{
 			getLobby().broadcast(ChatManager.leftborder + "The " + Team.PREY.getColor() + Team.PREY.getName(true) + ChatManager.color + " have all died!");
 			getLobby().broadcast(ChatManager.leftborder + Team.HUNTERS.getColor() + "the " + Team.HUNTERS.getName(true) + " have won the game!");
 		}
 		// The prey won
 		else if (hunters == 0)
 		{
 			getLobby().broadcast(ChatManager.leftborder + "The " + Team.HUNTERS.getColor() + Team.HUNTERS.getName(true) + ChatManager.color + " are all dead!");
 			getLobby().broadcast(ChatManager.leftborder + Team.PREY.getColor() + "The " + Team.PREY.getName(true) + " have won the game!");
 		}
 		// The prey won from time limit
 		else
 		{
 			getLobby().broadcast(ChatManager.leftborder + "The time limit has been reached!");
 			getLobby().broadcast(ChatManager.leftborder + Team.PREY.getColor() + "The " + Team.PREY.getName(true) + " have won the game!");
 		}
 		
 		getLobby().broadcast(ChatManager.divider);
 		
 		stopGame();
 	}
 	@Override
 	public void startListening()
 	{
 		Bukkit.getPluginManager().registerEvents(this, ManhuntPlugin.getInstance());
 	}
 	@Override
 	public void stopListening()
 	{
 		HandlerList.unregisterAll(this);
 	}
 	@Override
 	public void close()
 	{
 		super.close();
 	}
 	@Override
 	public boolean addPlayer(String name)
 	{
 		if (containsPlayer(name))
 			return false;
 		else
 			addPlayer(name, Team.SPECTATORS);
 		return true;
 	}
 	
 	
 	//---------------- Timeline ----------------//
 	private Timeline generateTimeline()
 	{
 		// Create the new timeline and placeholder event
 		ManhuntTimeline timeline = new ManhuntTimeline(getWorld());
 		Event event;
 		long time = 0L;
 		long lobby_id = getLobby().getId();
 		
 		
 		//////////////// LENGTH OF PREGAME ////////////////
 		//
 		//
 		//
 		time = 320L; // 16 seconds until start time
 		//
 		//
 		//
 		///////////////////////////////////////////////////
 		
 		event = new ManhuntWorldEvent(getWorld(), time - 320);
 		event.addAction(new BroadcastAction(lobby_id, "Prepare for Teleport!", Team.PREY, Team.HUNTERS));
 		timeline.registerEvent(event);
 		
 		event = new ManhuntWorldEvent(getWorld(), time - 260);
 		event.addAction(new TeleportTeamAction(lobby_id, Team.PREY, getPoints(SpawnType.PREY)));
 		event.addAction(new TeleportTeamAction(lobby_id, Team.HUNTERS, getPoints(getLobby().getSettings().TIME_SETUP.getValue() > 0 ? SpawnType.SETUP : SpawnType.HUNTER)));
 		timeline.registerEvent(event);
 		
 		
 		if (getLobby().getSettings().TIME_SETUP.getValue() > 0)
 		{
 			event = new ManhuntWorldEvent(getWorld(), time - 200);
 			event.addAction(new BroadcastAction(lobby_id, "Setup begins in 10 seconds."));
 			timeline.registerEvent(event);
 			
 			event = new ManhuntWorldEvent(getWorld(), time - 100);
 			event.addAction(new BroadcastAction(lobby_id, "5..."));
 			timeline.registerEvent(event);
 			
 			event = new ManhuntWorldEvent(getWorld(), time - 80);
 			event.addAction(new BroadcastAction(lobby_id, "4..."));
 			timeline.registerEvent(event);
 	
 			event = new ManhuntWorldEvent(getWorld(), time - 60);
 			event.addAction(new BroadcastAction(lobby_id, "3..."));
 			timeline.registerEvent(event);
 	
 			event = new ManhuntWorldEvent(getWorld(), time - 40);
 			event.addAction(new BroadcastAction(lobby_id, "2..."));
 			timeline.registerEvent(event);
 	
 			event = new ManhuntWorldEvent(getWorld(), time - 20);
 			event.addAction(new BroadcastAction(lobby_id, "1..."));
 			timeline.registerEvent(event);
 			
 			event = new ManhuntWorldEvent(getWorld(), time);
 			event.addAction(new RunnableAction(new Runnable(){ public void run(){ setStage(GameStage.SETUP); }}));
 			event.addAction(new BroadcastAction(lobby_id, "GO! Use this time to prepare for the hunt!", Team.PREY));
 			event.addAction(new BroadcastAction(lobby_id, "The prey are preparing for the hunt.", Team.HUNTERS));
			event.addAction(new BroadcastAction(lobby_id, (getWorld().getEnvironment() == Environment.NORMAL ? "The hunt will begin at sundown. (" + getLobby().getSettings().TIME_SETUP.getValue() + " minutes)" : "The hunt will begin in " + getLobby().getSettings().TIME_SETUP.getValue() + " minutes.")));
 			timeline.registerEvent(event);
 			
 			//////////////// LENGTH OF SETUP ////////////////
 			//
 			//
 			//
 			time += getLobby().getSettings().TIME_SETUP.getValue() * 1200;
 			//
 			//
 			//
 			/////////////////////////////////////////////////
 			
 			if (getLobby().getSettings().TIME_SETUP.getValue() > 1)
 			{
 				event = new ManhuntWorldEvent(getWorld(), time - 1200);
 				event.addAction(new BroadcastAction(lobby_id, "The hunt will begin in 1 minute!"));
 				timeline.registerEvent(event);
 			}
 			
 			event = new ManhuntWorldEvent(getWorld(), time - 320);
 			event.addAction(new BroadcastAction(lobby_id, "Prepare for teleport!", Team.HUNTERS));
 			timeline.registerEvent(event);
 			
 			event = new ManhuntWorldEvent(getWorld(), time - 260);
 			event.addAction(new TeleportTeamAction(lobby_id, Team.HUNTERS, getPoints(SpawnType.HUNTER)));
 			timeline.registerEvent(event);
 			
 		}
 		
 		event = new ManhuntWorldEvent(getWorld(), time - 200);
 		event.addAction(new BroadcastAction(lobby_id, "The hunt begins in 10 seconds."));
 		timeline.registerEvent(event);
 		
 		event = new ManhuntWorldEvent(getWorld(), time - 100);
 		event.addAction(new BroadcastAction(lobby_id, "5..."));
 		timeline.registerEvent(event);
 		
 		event = new ManhuntWorldEvent(getWorld(), time - 80);
 		event.addAction(new BroadcastAction(lobby_id, "4..."));
 		timeline.registerEvent(event);
 
 		event = new ManhuntWorldEvent(getWorld(), time - 60);
 		event.addAction(new BroadcastAction(lobby_id, "3..."));
 		timeline.registerEvent(event);
 
 		event = new ManhuntWorldEvent(getWorld(), time - 40);
 		event.addAction(new BroadcastAction(lobby_id, "2..."));
 		timeline.registerEvent(event);
 
 		event = new ManhuntWorldEvent(getWorld(), time - 20);
 		event.addAction(new BroadcastAction(lobby_id, "1..."));
 		timeline.registerEvent(event);
 		
 		event = new ManhuntWorldEvent(getWorld(), time);
 		event.addAction(new TeleportTeamAction(lobby_id, Team.HUNTERS, getPoints(SpawnType.HUNTER)));
 		event.addAction(new RunnableAction(new Runnable(){ public void run(){ setStage(GameStage.HUNT); }}));
 		event.addAction(new BroadcastAction(lobby_id, "The hunt has begun! The game will end in " + getLobby().getSettings().TIME_LIMIT.getValue() + " minutes."));
 		event.addAction(new BroadcastAction(lobby_id, "Beware! The hunters have been released!", Team.PREY));
 		event.addAction(new BroadcastAction(lobby_id, "Now's you're chance! Hunt the prey!", Team.HUNTERS));
 		timeline.registerEvent(event);
 		
 		//////////////// LENGTH OF THE HUNT ////////////////
 		//
 		//
 		//
 		time += getLobby().getSettings().TIME_LIMIT.getValue() * 1200;
 		//
 		//
 		//
 		////////////////////////////////////////////////////
 		
 		if (getLobby().getSettings().TIME_LIMIT.getValue() > 20)
 		{
 			event = new ManhuntWorldEvent(getWorld(),  time - 20 * 1200);
 			event.addAction(new BroadcastAction(lobby_id, "Only one day left in the hunt!"));
 			timeline.registerEvent(event);
 		}
 		
 		if (getLobby().getSettings().TIME_LIMIT.getValue() > 5)
 		{
 			event = new ManhuntWorldEvent(getWorld(), time - 5 * 1200);
 			event.addAction(new BroadcastAction(lobby_id, "5 minutes left in the hunt!"));
 			timeline.registerEvent(event);
 		}
 		
 		if (getLobby().getSettings().TIME_LIMIT.getValue() > 1)
 		{
 			event = new ManhuntWorldEvent(getWorld(), time - 1200);
 			event.addAction(new BroadcastAction(lobby_id, "1 minute left in the hunt!"));
 			timeline.registerEvent(event);
 		}
 		
 		event = new ManhuntWorldEvent(getWorld(), time - 600);
 		event.addAction(new BroadcastAction(lobby_id, "30 seconds left in the hunt!"));
 		timeline.registerEvent(event);
 		
 		event = new ManhuntWorldEvent(getWorld(), time - 300);
 		event.addAction(new BroadcastAction(lobby_id, "15 remaining!"));
 		
 		event = new ManhuntWorldEvent(getWorld(), time - 200);
 		event.addAction(new BroadcastAction(lobby_id, "10..."));
 		timeline.registerEvent(event);
 		
 		event = new ManhuntWorldEvent(getWorld(), time - 180);
 		event.addAction(new BroadcastAction(lobby_id, "9..."));
 		timeline.registerEvent(event);
 
 		event = new ManhuntWorldEvent(getWorld(), time - 160);
 		event.addAction(new BroadcastAction(lobby_id, "8..."));
 		timeline.registerEvent(event);
 
 		event = new ManhuntWorldEvent(getWorld(), time - 140);
 		event.addAction(new BroadcastAction(lobby_id, "7..."));
 		timeline.registerEvent(event);
 
 		event = new ManhuntWorldEvent(getWorld(), time - 120);
 		event.addAction(new BroadcastAction(lobby_id, "6..."));
 		timeline.registerEvent(event);
 
 		event = new ManhuntWorldEvent(getWorld(), time - 100);
 		event.addAction(new BroadcastAction(lobby_id, "5..."));
 		timeline.registerEvent(event);
 
 		event = new ManhuntWorldEvent(getWorld(), time - 80);
 		event.addAction(new BroadcastAction(lobby_id, "4..."));
 		timeline.registerEvent(event);
 
 		event = new ManhuntWorldEvent(getWorld(), time - 60);
 		event.addAction(new BroadcastAction(lobby_id, "3..."));
 		timeline.registerEvent(event);
 
 		event = new ManhuntWorldEvent(getWorld(), time - 40);
 		event.addAction(new BroadcastAction(lobby_id, "2..."));
 		timeline.registerEvent(event);
 
 		event = new ManhuntWorldEvent(getWorld(), time - 20);
 		event.addAction(new BroadcastAction(lobby_id, "1..."));
 		timeline.registerEvent(event);
 		
 		
 		//////////////// END GAME ////////////////
 		event = new ManhuntWorldEvent(getWorld(), time);
 		event.addAction(new RunnableAction(new Runnable()
 		{
 			public void run()
 			{
 				endGame();
 			}
 		}));
 		timeline.registerEvent(event);
 		
 		
 		return timeline;
 	}
 	
 	
 	
 }
 
 
 
