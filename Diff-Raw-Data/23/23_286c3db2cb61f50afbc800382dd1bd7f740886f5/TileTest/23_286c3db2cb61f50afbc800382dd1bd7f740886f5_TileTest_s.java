 package com.WordFinder.Tests;
 
 import com.WordFinder.Tile;
 import student.TestCase;
 
 /**
  *  Tests the Tile class
  *
 *  @author cmbuck
  *  @version Dec 3, 2011
  */
 public class TileTest extends TestCase
 {
 
     private Tile t1;
     private Tile t2;
     private Tile t3;
 
 
     /**
      * Sets up the testing environment
      */
     public void setUp()
     {
         t1 = new Tile(0, 0, 'a');
         t2 = new Tile(0, 1, 'b');
         t3 = new Tile(1, 1, 'c');
     }
 
     /**
      * tests the getState() and setState() methods
      */
     public void testGetAndSetState()
     {
         assertEquals(Tile.State.UP, t1.getState());
         t1.setState(Tile.State.DOWN);
         assertEquals(Tile.State.DOWN, t1.getState());
     }
 
     /**
      * tests the getLetter() method
      */
     public void testGetLetter()
     {
         assertEquals('a', t1.getLetter());
         assertEquals('b', t2.getLetter());
         assertEquals('c', t3.getLetter());
     }
 
     /**
      * tests the getX() and getY()
      */
     public void testGetXGetY()
     {
         assertEquals(0, t1.getX());
         assertEquals(0, t1.getY());
         assertEquals(0, t2.getX());
         assertEquals(1, t2.getY());
         assertEquals(1, t3.getX());
         assertEquals(1, t3.getY());
     }
 
     /**
      * Tests all the adjacent methods
      */
     public void testAdjacent()
     {
         assertEquals(0, t1.getAdjascent().size());
         assertFalse(t1.hasAdjascent('b'));
         t1.addAdjascent(t2);
         assertTrue(t1.hasAdjascent('b'));
         assertEquals(1, t1.getAdjascent().size());
 
         //add twice
         t1.addAdjascent(t2);
         assertEquals(2, t1.getAdjascent().size());
 
     }
 }
