 package com.CC.Game;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 
 public class Game
 {
 	public enum Team {
 		 RED, BLUE;  
 		}
			//Player  Arena   Team
 	HashMap<Player, HashMap<String, Team>>  gameData =  new HashMap<Player, HashMap<String, Team>>();
 	
     public Game()
     {
     }
     
     
 }
