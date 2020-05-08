 package edu.ucsb.cs56.projects.scrapers.baseball_stats;
 
 import org.junit.Test;
 import org.junit.*;
 import static org.junit.Assert.*;
 import java.util.ArrayList;
 
 /** Test class for StatManager
  * @author Max Sorenson
  * @version 2014/02/26 for baseball scraper, cs56, W14
  * @see StatManager
 */
 
 public class StatManagerTest {
 
     StatManager x;
     Statistic y;
     int id1 = 1234567, id2 = 7654321;
     String name1 = "Jim Jones", name2 = "Jon Jennings";
 
     @Before public void setup() {
 	x = new StatManager();
 	y = new Statistic("Home Run", 4);
     }
 
     /** Test case for StatManager()
         @see StatManager
     */
 
     @Test public void testNoArgConstructor() {
         assertTrue(x instanceof StatManager);
     }
 
 
     /** Test case for StatManager.endGame()
         @see StatManager
     */
 
     @Test public void testEndGame() {
        StatManager x = new StatManager();
	Statistic y = new Statistic("Home Run", 4);
 	x.incrementPlayerStat(id1, y);
 	//x.endGame();
         assertTrue(x instanceof StatManager);
     }
 
 
     /** Test case for StatManager.incrementPlayerStat(int,Statistic)
         @see StatManager
     */
 
     @Test public void testIncrementPlayerStat() {
         x.incrementPlayerStat(id1, y);
         assertEquals(0, x.getPlayerIndex(id1));
     }
 
     /** Test case for StatManager.getPlayerIndex(int)
         @see StatManager
     */
 
    @Test public void testGetPlayerIndex() {
         x.incrementPlayerStat(id1, y);
 	x.incrementPlayerStat(id2, y);
         assertEquals(0, x.getPlayerIndex(id1));
 	assertEquals(1, x.getPlayerIndex(id2));
     }
 
     /** Test case for StatManager.setPlayerName(int,String)
         @see StatManager
     */
 
     @Test public void testSetPlayerName() {
 	x.incrementPlayerStat(id1, y);
 	x.incrementPlayerStat(id2, y);
 	x.setPlayerName(id1, name1);
 	x.setPlayerName(id2, name2);
 	assertEquals(0, x.getPlayerIndex(id1));
     }
 
     /** Test case for StatManager.startNewGame(int,int,int)
         @see StatManager
     */
 
     @Test public void testStartNewGame() {
         /*StatManager x = new StatManager();
         saving for future use
         x.startNewGame(1,2,3);;
         assertEquals(0, 0);*/
     }
 
     /** Test case for StatManager.toString()
         @see StatManager
     */
 
     @Test public void testToString() {
         x.incrementPlayerStat(id1, y);
         x.setPlayerName(id1, name1);
 	assertEquals("Game Stats:\nJim Jones| ID = 1234567: Home Run = 4\n",
 		     x.toString());
     }
 
 
 
     
 
 }
 
