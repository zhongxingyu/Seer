 /**
  * ---------------------------------------------------------------------------
  * File name: MonsterTest.java<br/>
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
 import monsters.Monster;
 
 
 /**
  * Enter type purpose here<br>
  *
  * <hr>
  * Date created: Mar 15, 2012<br>
  * Date last modified: Mar 15, 2012<br>
  * <hr>
  * @author Matthew Paul
  */
 public class MonsterTest
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
 //		create a new monster and display him
 		Monster monster = new Monster("batty rodent", 100, 4);
 		System.out.println("Displaying monster: " + monster);
 		
 //		create a copy of the monster, change his name, and display him
 		Monster shadowMonster = new Monster(monster);
 		System.out.println("Oh Noes! " + monster.getName( ) + "'s shadow has rebelled!");
 		shadowMonster.setName("shadow " + monster.getName( ));
 		System.out.println(shadowMonster.getName( ) + " was born!");
 		System.out.println(shadowMonster + "\n\n");
 		
 		
 //		simulate the monster wailing on his shadow
 		while (shadowMonster.getHealth( ) > 0)
 		{
 			int damage = monster.attack( );
 			System.out.println(monster.getName( ) +
 							" attacks " +
 							shadowMonster.getName( ) +
 							(damage == 0 ? " but misses." : " and hits for " +
 											damage + " damage!"));
 			shadowMonster.takeDamage(damage);
 			System.out.println(monster);
 			System.out.println(shadowMonster + "\n\n");
 			
 			if (shadowMonster.getHealth( ) < 1)
 			{
 				System.out.println(shadowMonster.getName( ) + " is dead.");
 			}
 		}
 		
 //		check the average hit rate of the monster for off-by-1 errors
 		int attackCount = 0;
 		for (int c = 0; c < 10000; c++)
 		{
 			if (monster.attack( ) != 0)
 				attackCount++;
 		}
 		
 		System.out.println("The average hit percent over 10,000 attacks is " + ((double)attackCount / 10000) + ".");
 	}
 
 }
