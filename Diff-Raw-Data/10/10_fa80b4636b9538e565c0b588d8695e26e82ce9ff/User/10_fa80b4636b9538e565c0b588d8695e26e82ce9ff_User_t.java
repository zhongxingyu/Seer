 package com.CC.General;
 
 import java.util.ArrayList;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 public class User
 {
 	String player;
 	int PlayerPoints;
 	int Reputation;
 	int Kills;
 	int Deaths;
 	int timeOnRed;
 	int timeOnBlue;
 	String latestGame;
 	ArrayList<String> friends;
 	ArrayList<String> enemies;
 	
        private User(String name)
         {
            friends = new ArrayList<String>();
            enemies = new ArrayList<String>();
            player = name;
         }
         
 	public User(Player p)
         {
 		friends = new ArrayList<String>();
                 enemies = new ArrayList<String>();
 		player = p.getName();	
 	}
 	
 	public int getPoints()
         {
 		return PlayerPoints;
 	}
         
 	//For loading and Normal Use
 	public void changePoints(int points)
         {
 		PlayerPoints = points;
 	}
 	
 	public int getReputation()
         {
 		return Reputation;
 	}
 	
 	//For loading and normal use
 	public void changeReputation(int changedReputation)
         {
 		Reputation = changedReputation;
 	}
 	
 	public int getKills()
         {
 		return Kills;
 	}
         
 	//For loading
 	public void setKills(int amount)
         {
 		Kills = amount;
 	}
 	
 	public void addKill()
         {
 		Kills++;
 	}
 	
 	public int getDeaths()
         {
 		return Deaths;
 	}
         
 	//For loading 
 	public void setDeaths(int amount)
         {
 		Deaths = amount;
 	}
 	
 	public void addDeath()
         {
 		Deaths++;
 	}
 	
 	public int getTimesPlayedOnRedTeam()
         {
 		return timeOnRed;
 	}
 	
 	public void addTimeOnRed(){
 		timeOnRed++;
 	}
 	//For loading 
 	public void setTimesOnRed(int times)
         {
 		timeOnRed = times;
 	}
 	
 	public int getTimesPlayedOnBlueTeam()
         {
 		return timeOnBlue;
 	}
 	
 	public void addTimeOnBlue()
         {
 		timeOnBlue++;
 	}
 	//For loading 
 	public void setTimeOnBlue(int times){
 		timeOnBlue = times;
 	}
 	
 	public String getLatestGame()
         {
 		return latestGame;
 	}
 	//For loading normal use 
 	public void changeLatestGame(String game)
         {
 		latestGame = game;
 	}
 	
 	
 	public ArrayList<String> getFriends()
         {
 		return friends;
 	}
 	//for Loading 
 	public void setFriendsList(ArrayList<String> friendslist)
         {
 		friends = friendslist;
 	}
 	
 	public void addFriend(String friendName)
         {
 		friends.add(friendName);
 	}
 	
 	
 	public ArrayList<String> getEnemies()
         {
 		return enemies;
 	}
 	//for Loading 
 	public void setEnemiesList(ArrayList<String> enemyList)
         {
 		enemies = enemyList;
 	}
 	
 	public void addEnemy(String enemyName)
         {
 		enemies.add(enemyName);
 	}
         
 	public Player getPlayer()
         {
 		return Bukkit.getServer().getPlayer(player);
 	}
         
         @Override
         public User clone()
         {
            User clone = new User(this.player);
             clone.changePoints(this.getPoints());
             clone.changeReputation(this.getReputation());
             clone.setKills(this.getKills());
             clone.setDeaths(this.getDeaths());
             clone.setTimesOnRed(this.getTimesPlayedOnRedTeam());
             clone.setTimeOnBlue(this.getTimesPlayedOnBlueTeam());
             clone.changeLatestGame(this.getLatestGame());
             clone.setFriendsList(this.getFriends());
             clone.setEnemiesList(this.getEnemies());
             return clone;
         }
 }
