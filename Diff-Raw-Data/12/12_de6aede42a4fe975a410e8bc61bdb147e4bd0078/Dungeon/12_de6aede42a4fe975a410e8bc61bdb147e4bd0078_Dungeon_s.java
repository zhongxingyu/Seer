 /**
  * ---------------------------------------------------------------------------
  * File name: Dungeon.java<br/>
  * Project name: 1260-088-PROJECT4-MiddaughStephen<br/>
  * ---------------------------------------------------------------------------
  * Creator's name and email: Stephen Middaugh, middaughs@goldmail.etsu.edu<br/>
  * Course:  CSCI 1260<br/>
  * Creation Date: Mar 13, 2012<br/>
  * Date of Last Modification: Mar 13, 2012
  * ---------------------------------------------------------------------------
  */
 
 import java.util.Random;
 
 /**
  * Represents a collection of rooms that make up a dungeon.<br>
  *
  * <hr>
  * Date created: Mar 13, 2012<br>
  * Date last modified: Mar 13, 2012<br>
  * <hr>
  * @author Stephen Middaugh
  */
 
 public class Dungeon
 {
 	private int playerLocation;	// The players location in the dungeon. 0 = start.
 	private Room[] rooms;	// The collection of rooms for the player to travel through.
 	
 	
 	
 	/**
 	 * Constructor <br>        
 	 *
 	 * <hr>
 	 * Date created: Mar 13, 2012 <br>
 	 * Date last modified: Mar 14, 2012 <br>
 	 *
 	 * <hr>
 	 */
 	public Dungeon()
 	{
 		Random rand = new Random();
 		rooms = new Room[rand.nextInt(6) + 5];
 	}
 	
 	/**
 	 * Populates the dungeon with both monsters and a weapon. <br>        
 	 *
 	 * <hr>
 	 * Date created: Mar 14, 2012 <br>
 	 * Date last modified: Mar 14, 2012 <br>
 	 *
 	 * <hr>
 	 */
 	public void populateDungeon()
 	{
 //		create the random number generator to be used for this method
 		Random rand = new Random();
 		
 //		for each room other than the starting room, randomly decide if there is a monster
 		for (int i = 1; i < rooms.length; i++ )
 		{
 //			there is an approximately 50% chance for a monster to be there
 			if (rand.nextBoolean( ))
 			{	
 				rooms[i].setMonster(new Monster());
 			}
 		}
 		
		rooms[rand.nextInt(9)].setWeapon(null);
 		
 	}
 }
