 package edu.gatech.CS2340.GrandTheftPoke.backend.persons;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 import edu.gatech.CS2340.GrandTheftPoke.backend.GameMap;
 
 /**
  * 
  * @author Team Rocket
  */
 @XStreamAlias("Player")
 public class Player extends Person {
 	@XStreamOmitField
 	private static final int START_SKILLS = 16;
 	@XStreamOmitField
	private static final Float START_MONEY = 5000f;
 
 	public Player() {
 		super();
 	}
 
 	public Player(String name, int strength, int agility, int trade,
 			int stamina, int health, int range, int capacity, GameMap theMap) {
 		super(name, strength, agility, trade, stamina, health, range, capacity,
 				START_MONEY, theMap);
 	}
 }
