 /**
  * 
  */
 package de.htwg.se.battleship.model.impl;
 
 import static org.junit.Assert.*;
 
 import java.util.HashMap;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import de.htwg.se.battleship.model.ICell;
 import de.htwg.se.battleship.model.IPlayer;
 import de.htwg.se.battleship.model.impl.Cell;
 import de.htwg.se.battleship.model.impl.Grid;
 import de.htwg.se.battleship.model.impl.Player;
 import de.htwg.se.battleship.model.impl.Ship;
 
 /**
  * @author Philipp Daniels<philipp.daniels@gmail.com>
  * 
  */
 public class CellTest {
 
     private Cell c1;
     private Cell c2;
     private Grid g1;
     private Grid g2;
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
         g1 = new Grid(1, new Player(""));
         g2 = new Grid(1, new Player(""));
         c1 = new Cell(1, 2, g1);
         c2 = new Cell(12, 8, g2);
     }
 
     @Test
     public void testX() {
         assertEquals(1, c1.getX());
         assertEquals(12, c2.getX());
     }
 
     @Test
     public void testY() {
         assertEquals(2, c1.getY());
         assertEquals(8, c2.getY());
     }
 
     @Test
     public void testKey() {
         assertEquals("1.2", c1.getKey());
         assertEquals("12.8", c2.getKey());
     }
 
     @Test
     public void testGrid() {
         assertEquals(g1, c1.getGrid());
         assertEquals(g2, c2.getGrid());
     }
 
     @Test
     public void testShip() {
         IPlayer p = new Player("test");
 
         HashMap<String, ICell> map1 = new HashMap<String, ICell>();
         map1.put(Cell.createKey(c1.getX(), c1.getY()), c1);
         assertNull(c1.getShip());
        Ship s1 = new Ship(p, map1);
         c1.setShip(s1);
         assertEquals(c1.getShip(), s1);
 
         HashMap<String, ICell> map2 = new HashMap<String, ICell>();
         map1.put(Cell.createKey(c2.getX(), c2.getY()), c2);
         Ship s2 = new Ship(p, map2);
         assertNull(c2.getShip());
         c2.setShip(s2);
         assertEquals(c2.getShip(), s2);
 
         assertEquals(c1.getShip(), s1);
     }
 
     @Test
     public void testStatus() {
         assertTrue(c1.isNormal());
         assertFalse(c1.isHit());
         assertFalse(c1.isShot());
 
         c1.setToHit();
         assertFalse(c1.isNormal());
         assertTrue(c1.isHit());
         assertTrue(c1.isShot());
 
         c1.setToShot();
         assertFalse(c1.isNormal());
         assertFalse(c1.isHit());
         assertTrue(c1.isShot());
     }
 }
