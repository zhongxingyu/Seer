 package bteam.capstone.risk;
 
 import java.util.ArrayList;
import java.util.Scanner;
 
 /**
  * 
  * @author Nick
  * The player class will have a constructor that will 
  * create a player given a name and faction.
  * The class will hold data that includes missles, 
  * redstars, coins, and resources.
  * 
  */
 public class player {
 	
 	//private variables storing data class wide
 	private int 	missles, redstar, coin, resource;
 	private String 	name;
 	private Faction faction;
 	private ArrayList<Integer> countrys = new ArrayList<Integer>(); 
 	//private Faction faction;
 	
 	//Constructor for creating a new player given a name
 	// and a faction.
	public player(String data){
		Scanner scan = new Scanner(data);
		this.name = scan.next();
		this.missles = scan.nextInt();
		this.redstar = scan.nextInt();
		this.coin = scan.nextInt();
		this.resource = scan.nextInt();
 		
 	}
 	
 	
 	
 	public String getName(){
 		return name;
 	}
 	public void setMissles(int missles) {
 		this.missles = missles;
 	}
 	public int getMissles() {
 		return missles;
 	}
 	public void setRedstar(int redstar) {
 		this.redstar = redstar;
 	}
 	public int getRedstar() {
 		return redstar;
 	}
 	public void setCoin(int coin) {
 		this.coin = coin;
 	}
 	public int getCoin() {
 		return coin;
 	}
 	public void setResource(int resource) {
 		this.resource = resource;
 	}
 	public int getResource() {
 		return resource;
 	}
 
 	public void setFaction(StandardFaction faction) {
 		this.faction = faction;
 	}
 
 	public Faction getFaction() {
 		return faction;
 	}
 	
 }
