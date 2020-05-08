 /**
  * ---------------------------------------------------------------------------
  * File name: PlayerTest.java<br/>
  * Project name: quasiZork<br/>
  * ---------------------------------------------------------------------------
  * Creator's name and email: Matthew Paul, paulmr@goldmail.etsu.edu<br/>
  * Course:  CSCI 1260-088<br/>
  * Creation Date: Mar 15, 2012<br/>
  * Date of Last Modification: Mar 15, 2012
  * ---------------------------------------------------------------------------
  */
 
 package test;
 
 import weapons.Stick;
 import weapons.Sword;
 import weapons.Weapon;
 import core.Player;
 
 
 /**
  * Test driver for the person class.<br>
  *
  * <hr>
  * Date created: Mar 15, 2012<br>
  * Date last modified: Mar 15, 2012<br>
  * <hr>
  * @author Matthew Paul
  */
 public class PlayerTest
 {
 
 	/**
 	 * Main method for the driver. <br>        
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
 //		create a new player and display him
 		Player player = new Player("Gilgamesh", 100);
 		System.out.println("Displaying player: " + player);
 		
 //		create a stick, give it to the player, and display him
 		Weapon stick = new Stick();
 		System.out.println("Giving " + player.getName( ) + " a " + stick.getName( ));
 		player.setWeapon(stick);
 		System.out.println("Displaying player: " + player + "\n\n");
 		System.out.println(player.getWeapon( ));
 		
 //		create a copy of the player, change his name, and display him
 		Player shadowPlayer = new Player(player);
 		System.out.println("Oh Noes! " + player.getName( ) + "'s shadow has rebelled!");
 		shadowPlayer.setName("shadow " + player.getName( ));
 		System.out.println(shadowPlayer.getName( ) + " was born!");
 		System.out.println(shadowPlayer + "\n\n");
 		System.out.println(shadowPlayer.getWeapon( ));
 		
 //		create a sword, give it to the player, and display both players
 		Weapon sword = new Sword();
 		System.out.println("Giving " + player.getName( ) + " a " + sword.getName( ) + ".");
 		player.setWeapon(sword);
 		System.out.println(player);
 		System.out.println(shadowPlayer + "\n\n");
 		
 //		simulate the player wailing on his shadow
 		while (shadowPlayer.getHealth( ) > 0)
 		{
 			int damage = player.attack( );
 			System.out.println(player.getName( ) +
 							" attacks " +
 							shadowPlayer.getName( ) +
 							(damage == 0 ? " but misses." : " and hits for " +
 											damage + " damage!"));
 			shadowPlayer.takeDamage(damage);
 			System.out.println(player);
 			System.out.println(shadowPlayer + "\n\n");
 			
 			if (shadowPlayer.getHealth( ) < 1)
 			{
 				System.out.println(shadowPlayer.getName( ) + " is dead.");
 			}
 		}
 		
 //		check the average hit rate of the player for off-by-1 errors
 		int attackCount = 0;
 		for (int c = 0; c < 10000; c++)
 		{
 			if (player.attack( ) != 0)
 				attackCount++;
 		}
 		
 		System.out.println("The average hit percent over 10,000 attacks is " + ((double)attackCount / 10000) + ".");
 	}
 
 }
