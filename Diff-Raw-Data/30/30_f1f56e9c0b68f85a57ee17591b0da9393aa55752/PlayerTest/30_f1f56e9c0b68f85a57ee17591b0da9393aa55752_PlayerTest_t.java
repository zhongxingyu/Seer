 package edu.ucsb.cs56.projects.scrapers.baseball_stats;
 
 import org.junit.Test;
 import org.junit.*;
 import static org.junit.Assert.*;
 
 /** Test class for Player
  * @author Max Sorenson
  * @version 2014/02/26 for baseball scraper, cs56, W14
  * @see Player
 */
 
 public class PlayerTest {
 
     Player x;
     int id = 1234567;
     String firstName = "Jim";
     String lastName = "Jones";
     String fullName = "Jim Jones";
 
     Statistic y;
     String statType = "Home Run";
     int statNum = 4;
     
 
     @Before public void setup() {
 	x = new Player(id,firstName,lastName);
         y = new Statistic(statType,statNum);
     }
 
     /** Test case for Player(int)
         @see Player
     */
 
     @Test public void testOneArgConstructor() {
 	Player x2 = new Player(1234567);
         assertEquals(id, x2.getID());
     }
 
 
     /** Test case for Player(int,String)
         @see Player
     */
 
    @Test public void testTwoArgConstructor_id() {
         assertEquals(id, x.getID());
    }

     /** Test case for Player(int,String)
        @see Player
    */

    @Test public void testTwoArgConstructor_name() {
 	assertEquals(fullName, x.getFullName());
     }
 
     /** Test case for Player(int,String,String)
         @see Player
     */
 
     @Test public void testThreeArgConstructor() {
         Player x3 = new Player(1234567, "Jim", "Jones");
         assertEquals(id, x3.getID());
     }
 
     
     
     /** Test case for Player.addStatistic(Statistic)
         @see Player
     */
 
     @Test public void testAddStatistic() {
         x.addStatistic(y);
 	assertEquals(statNum, x.getStatValue(statType));
     }
 
     
     /** Test case for Player.equals(Object)
         @see Player
     */
 
     @Test public void testEquals() {
         assertTrue(x.equals(x));
     }
 
     
 
 
     /** Test case for false Player.equals(Object)
         @see Player
     */
 
     @Test public void testEquals_false() {
         Player y = new Player(7654321, "Jon", "Jennings");
         assertFalse(x.equals(y));
     }
 
     
 
     /** Test case for Player.getFirstName()
 	@see Player
     */
     
     @Test public void testGetFirstName() {
 	assertEquals(firstName, x.getFirstName());
     }
 
     
     /** Test case for Player.getLastName()
         @see Player
     */
 
     @Test public void testGetLastName() {
         assertEquals(lastName, x.getLastName());
     }
 
 
     /** Test case for Player.getFullName()
         @see Player
     */
 
     @Test public void testGetFullName() {
 	assertEquals(fullName, x.getFullName());
     }
  
 
     /** Test case for Player.getID()
         @see Player
     */
 
     @Test public void testGetID() {
 	assertEquals(id, x.getID());
     }
    
     /** Test case for Player.getStat()
         @see Player
     */
 
     @Test public void testGetStat() {
 	x.addStatistic(y);
 	assertEquals(y, x.getStat(0));
     }
 
     /** Test case for Player.getStatisticIndex(Statistic)
         @see Player
     */
 
     @Test public void testGetStatisticIndex_oneStat() {
         x.addStatistic(y);
         assertEquals(0, x.getStatisticIndex(y));
     }
 
     /** Test case 2 for Player.getStatisticIndex(Statistic)
         @see Player
     */
 
     @Test public void testGetStatisticIndex_twoStats() {
 	Statistic z = new Statistic("Single", 5);
         x.addStatistic(y);
 	x.addStatistic(z);
         assertEquals(1, x.getStatisticIndex(z));
     }
 
     
     /** Test case for Player.getStatValue(String)
         @see Player
     */
 
     @Test public void testGetStatValue() {
         x.addStatistic(y);
         assertEquals(statNum, x.getStatValue(statType));
     }
 
     
     /** Test case for Player.setFirstName(String)
         @see Player
     */
 
     @Test public void testSetFirstName() {
         x.setFirstName("Jon");
         assertEquals("Jon", x.getFirstName());
     }
 
     /** Test case for Player.setLastName(String)
         @see Player
     */
 
     @Test public void testSetLastName() {
         x.setLastName("Jennings");
         assertEquals("Jennings", x.getLastName());
     }
     
     /** Test case for Player.setFullName(String)
         @see Player
     */
 
     @Test public void testSetFullName() {
         x.setFullName("Jon Jennings");
         assertEquals("Jon Jennings", x.getFullName());
     }
 
     /** Test case for Player.toString()
         @see Player
     */
 
     @Test public void testToString() {
         assertEquals(fullName + "| ID = " + id + ": ", x.toString());
     }
 
 
     
 
 
 }
 
