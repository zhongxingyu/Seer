 package com.namarius.weathernews;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import com.namarius.weathernews.utils.ChatUtil;
 import com.namarius.weathernews.utils.MinecraftTime;
 import com.namarius.weathernews.utils.ChatUtil.Symbol;
 
 public class NewsRunner implements Runnable {
 	
 	private final World world;
 	private boolean oldstorm;
 	private boolean oldthunder;
 	private List<Player> players = null;
 	
 	public NewsRunner( World world, boolean storm, boolean thunder)
 	{
 		this.world = world;
 		this.oldstorm = storm;
 		this.oldthunder = thunder;
 	}
 	
 	public NewsRunner(World world, Player player)
 	{
 		this.world=world;
 		oldstorm=world.hasStorm();
 		oldthunder=world.isThundering();
 		players = new ArrayList<Player>();
 		players.add(player);		
 	}
 
 	@Override
 	public void run() {
 		players = players!=null?players:world.getPlayers();
 		ArrayList<Object> data = new ArrayList<Object>();
 		boolean storm = world.hasStorm();
 		boolean thunder = world.isThundering();
 		int state = (oldstorm?1:0) + (oldthunder?2:0) + (storm?4:0) + (thunder?8:0);
 		String from = null;
 		String to = null;
 		String next = null;
 		switch(state)
 		{
 			case 0:from=from==null?"sun":from;//sun>sun
 			case 1:from=from==null?"rain":from;//rain>sun
 			case 2:from=from==null?"sun":from;//sun(thunderstorm)>sun
 			case 3:from=from==null?"thunderstorm":from;//thunderstorm>sun
 				to="sun";
 				next=world.getWeatherDuration()<world.getThunderDuration()?"rain":"sun";					
 				break;
 				
 			case 4:from=from==null?"sun":from;//sun>sun
 			case 5:from=from==null?"rain":from;//rain>sun
 			case 6:from=from==null?"sun":from;//sun(thunderstorm)>sun
 			case 7:from=from==null?"thunderstorm":from;//thunderstorm>sun
 				to="rain";
 				next=world.getWeatherDuration()<world.getThunderDuration()?"sun":"thunderstorm";	
 				break;
 				
 			case 8:from=from==null?"sun":from;//sun>sun
 			case 9:from=from==null?"rain":from;//rain>sun
 			case 10:from=from==null?"sun":from;//sun(thunderstorm)>sun
 			case 11:from=from==null?"thunderstorm":from;//thunderstorm>sun
 				to="sun";
 				next=world.getWeatherDuration()<world.getThunderDuration()?"thunderstorm":"sun";	
 				break;
 				
 			case 12:from=from==null?"sun":from;//sun>sun
 			case 13:from=from==null?"rain":from;//rain>sun
 			case 14:from=from==null?"sun":from;//sun(thunderstorm)>sun
 			case 15:from=from==null?"thunderstorm":from;//thunderstorm>sun
 				to="thunderstorm";
 				next=world.getWeatherDuration()<world.getThunderDuration()?"sun":"rain";	
 				break;
 		}
 		MinecraftTime time = new MinecraftTime(world.getFullTime()+6000);//I want midnight
 		
 		data.add("[WeatherNews]");
 		data.add("Today the day:");
 		data.add(time.getDay());
 		data.add(Symbol.Newline);
		data.add("We have");
		data.add(to);
		data.add("for the next");
		data.add(new MinecraftTime(Math.min(world.getWeatherDuration(),world.getThunderDuration())).nicePrint());
		data.add("and then we get");
		data.add(next);
 		for(Player p : players)
 		{
 			ChatUtil.send(data.toArray(), p, ChatColor.GOLD);
 		}
 	}
 
 }
