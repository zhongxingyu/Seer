 /**
  * ---------------------------------------------------------------------------
  * File name: LaserWordTest.java<br/>
  * Project name: TypingTutor<br/>
  * ---------------------------------------------------------------------------
  * Creator's name and email: Matthew Paul, paulmr@goldmail.etsu.edu<br/>
  * Course:  CSCI 1260-088<br/>
  * Creation Date: Apr 19, 2012<br/>
  * Date of Last Modification: Apr 19, 2012
  * ---------------------------------------------------------------------------
  */
 
 package test;
 
import laserDefense.LaserWord;
 import core.Word;
 import core.WordPool;
 
 
 /**
  * Enter type purpose here<br>
  *
  * <hr>
  * Date created: Apr 19, 2012<br>
  * Date last modified: Apr 19, 2012<br>
  * <hr>
  * @author Matthew Paul
  */
 public class LaserWordTest
 {
 
 	/**
 	 * Enter method description here <br>        
 	 *
 	 * <hr>
 	 * Date created: Apr 19, 2012 <br>
 	 * Date last modified: Apr 19, 2012 <br>
 	 *
 	 * <hr>
 	 * @param args
 	 */
 
 	public static void main(String [ ] args)
 	{
 		Word laser = new LaserWord("Hello", 13, 37);
 		
 		System.out.println(laser);
 		
 		Word copy = laser.copy( );
 		
 		Word copy2 = laser.copy( );
 		
 		System.out.println(copy);
 		System.out.println("laser: " + laser.getClass( ) + " copy: " + copy.getClass( ));
 		System.out.println(copy2);
 		System.out.println("laser: " + laser.getClass( ) + " copy2: " + copy2.getClass( ));
 		
 		Word[] array = new Word[3];
 		
 		array[0] = laser;
 		array[1] = copy;
 		array[2] = copy2;
 		
 		WordPool pool = new WordPool(array);
 		
 		while(pool.hasMoreWords( ))
 		{
 			Word word = pool.getNextWord( );
 			System.out.println(word + "|" + word.getClass( ));
 		}
 	}
 
 }
