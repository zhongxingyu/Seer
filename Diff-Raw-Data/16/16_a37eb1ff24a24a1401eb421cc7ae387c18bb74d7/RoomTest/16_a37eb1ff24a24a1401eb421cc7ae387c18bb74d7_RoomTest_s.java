 /**
  * ---------------------------------------------------------------------------
  * File name: RoomTest.java<br/>
  * Project name: quasiZork<br/>
  * ---------------------------------------------------------------------------
  * Creator's name and email: Matthew Paul, paulmr@goldmail.etsu.edu<br/>
  * Course:  CSCI 1260-088<br/>
  * Creation Date: Mar 15, 2012<br/>
  * Date of Last Modification: Mar 15, 2012
  * ---------------------------------------------------------------------------
  */
 
 package test;
 
 import monsters.Monster;
 import weapons.Stick;
 import weapons.Sword;
import weapons.Weapon;
 import core.Room;
 
 
 /**
  * Tests the room class.<br>
  *
  * <hr>
  * Date created: Mar 15, 2012<br>
  * Date last modified: Mar 15, 2012<br>
  * <hr>
  * @author Matthew Paul
  */
 public class RoomTest
 {
 
 	/**
 	 * Driver method for the RoomTest class. <br>        
 	 *
 	 * <hr>
 	 * Date created: Mar 15, 2012 <br>
 	 * Date last modified: Mar 15, 2012 <br>
 	 *
 	 * <hr>
 	 * @param args
 	 */
 
 	public static void main(String [ ] args)
 	{
 //		create a default room and display it
 		Room room = new Room();
 		System.out.println(room);
 		
 //		add a stick to the room and display it
 		System.out.println("Adding a weapon to the room");
 		room.setWeapon(new Stick());
 		System.out.println(room);
 		
 //		add a monster to the room and display it
 		System.out.println("Adding a monster to the room");
 		room.setMonster(new Monster("goblin", 20, 4));
 		System.out.println(room);
 		
 //		copy the room and display the original and the copy
 		System.out.println("Copying the room");
 		Room copy = new Room(room);
 		System.out.println(room);
 		System.out.println(copy);
 		
 //		change the copy's attributes and display both
 		System.out.println("Changing the copy's attributes");
 		copy.setMonster(new Monster("gorgon", 25, 7));
 		copy.setWeapon(new Sword("slim blade", 4));
 		System.out.println(room);
 		System.out.println(copy);
 		
 //		remove the monster and weapon and display both
 		System.out.println("Removing the copy's weapon and monster");
 		copy.setWeapon(null);
 		copy.setMonster(null);
 		System.out.println(room);
 		System.out.println(copy);
 	}
 
 }
