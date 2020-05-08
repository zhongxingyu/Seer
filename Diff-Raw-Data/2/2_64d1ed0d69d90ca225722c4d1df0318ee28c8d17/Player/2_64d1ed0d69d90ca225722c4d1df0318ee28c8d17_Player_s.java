 import java.awt.Color;
 import java.util.ArrayList;
 
 /*
 * This is a player class that holds the player information, such as race, color, money amount, and name.
 * @author Andy Fang
 * Version 1.0 10/7/2013
 */
 public class Player{
 	private String name;
 	private String race;
 	private Color color;
 	private int money,food,energy,ore,score;
 	private boolean done;
 	private ArrayList<Tile> propertyOwned;
 	private boolean hasVisitedTown;
 	
 	/*
 	* This is the constructor that instantiates the Player
 	*@name - the name of the player
 	*@race - the race of the player
 	*@color - the color repesenting that player
 	*/
 
 	public Player(String name, String race, Color color){
 		int i = 0;
 		
 		this.name = name;
 		this.race = race;
 		this.color = color;
 		done=false;
 		hasVisitedTown=false;
 		propertyOwned = new ArrayList<Tile>();
 		
 		if (race == "Flapper"){
 			money = 1600;
 		}
 		else if (race == "Human"){
 			money = 600;
 		}
 		else
 			money = 1000;
 		
 		food = 8;
 		energy = 4;
 		ore = 0;
 	}
 	
 	public String getName(){
 		return name;
 	}
 	/*
 	 * This method is to calculate player's time of turn based on resource
 	 */
 	public int getTurnTime(int round){
 		int time = 50;
		int foodRequire = (int)(Math.floor(round/4) +3);
 		if (food == 0){
 			time = 5;
 		}
 		else if (food < foodRequire){
 			time = 30;
 		}
 		return time;
 	}
 	
 	public int getScore(){
 		score = money/200+food+energy+ore;
 		return score;
 	}
 	
 	public void setScore(int score){
 		this.score = score;
 	}
 	
 	public void setDone(boolean done){
 		this.done = done;
 	}
 	
 	public Color getColor(){
 		return color;
 	}
 	
 	public boolean isDone(){
 		return done;
 	}
 	
 	public boolean buyProperty(int cost, Tile tile){
 		System.out.println("Is owned? " + tile.isOwned());
 		if(cost<=money && tile.isOwned()==false){
 			money-=cost;
 			propertyOwned.add(tile);
 			tile.isBought(this);
 			return true;
 		}
 		else{
 			return false;
 		}
 			
 	}
 	
 	public String toString(){
 		return "Player name: " + name  + " Player race: " + race + " Player color: " + color.toString();
 	}
 	
 	public int getMoney(){
 		return money;
 	}
 
 	public void gamble(int gambleGame) {
 		money+=gambleGame;
 		
 	}
 	
 	public boolean hasVisited(){
 		return hasVisitedTown;
 	}
 	
 	public void setVisited(boolean visit){
 		hasVisitedTown=visit;
 	}
 }
