 package com.golddigger.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.golddigger.model.Game;
 import com.golddigger.model.Player;
 
 public class AppContext {
 	/**
 	 * All the "games" currently running in the server
 	 */
 	public static List<Game> games = new ArrayList<Game>();
 	public static List<GameTemplate> templates = new ArrayList<GameTemplate>();
 	public static List<Player> players = new ArrayList<Player>();
 
 	public static Game getGameByPlayer(String name){
 		return getGame(getPlayer(name));
 	}
 	
 	public static Game getGame(Player player){
 		for (Game game:games){
 			if (game.hasPlayer(player)){
 				return game;
 			}
 		}
 		return null;
 	}
 
 	public static void add(Game game) {
 		AppContext.games.add(game);
 	}
 	
 	public static void add(GameTemplate template){
 		System.out.println("Adding template["+templates.size()+"]: "+template.getClass().getSimpleName());
 		template.setID(templates.size());
 		AppContext.templates.add(template);
 	}
 	
 	public static void add(Player player){
 		System.out.println("Adding new Player "+player.getName());
 		if (exists(player)) throw new RuntimeException("The Player ("+player.getName()+") is already playing");
 		AppContext.players.add(player);
 		Game first = templates.get(0).build();
 		AppContext.add(first);
 		first.add(player);
 	}
 	
 	public static Player getPlayer(String name){
 		for (Player player : players){
 			if (player.getName().equalsIgnoreCase(name)){
 				return player;
 			}
 		}
 		return null;
 	}
 	
 	public static void progress(Player player){
 		Game old = getGame(player);
 		int id = old.getID();
 		games.remove(old);
 		Game next = templates.get(id+1).build();
 		add(next);
 		next.add(player);
 	}
 	
 	private static boolean exists(Player player){
 		return AppContext.getPlayer(player.getName()) != null;
 	}
 }
 		
