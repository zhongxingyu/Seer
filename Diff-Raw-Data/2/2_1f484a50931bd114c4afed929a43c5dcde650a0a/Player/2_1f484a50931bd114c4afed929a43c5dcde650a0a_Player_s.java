 package gameObjects;
 
 import com.badlogic.gdx.graphics.Color;
 
 /**
  * Represents a single player in the MULE game. It will keep track of its own data and allow
  * for the modification of its data through methods.
  * 
  * @author antonio
  * @version 1.0
  *
  */
 public class Player {
 	
 	private static enum Race {
 		HUMAN, FLAPPER
 	};
 	
 	private Race race;
 	private String name;
 	private Color color;
 	
 	public Player(){
 		super();
 		name = "";
 		color = null;
 		race = Race.HUMAN;
 	}
 	
 	public Player(String n, Color c){
 		this();
 		name = n;
 		color = c;
 	}
 	
 	public Player(String name, String race, Color c){
 		this(name, c);
 		this.setRace(race);
 	}
 	
 	public boolean setRace(String s){
 		if(s.toLowerCase().equals("human")){
 			race = Race.HUMAN;
 			return true;
		} else if(s.toLowerCase().equals("FLAPPER")){
 			race = Race.FLAPPER;
 			return true;
 		}
 		return false;
 	}
 	
 	public void setName(String s){
 		name = s;
 	}
 	
 	public String getName(){
 		return name;
 	}
 	
 	public String toString(){
 		String s = "";
 		s += "NAME : " + name;
 		s += "\t RACE : " + race;
 		s += "\t COLOR : " + color;
 		s += "\n";
 		return s;
 	}
 	
 	public Color getColor(){
 		return color;
 	}
 }
