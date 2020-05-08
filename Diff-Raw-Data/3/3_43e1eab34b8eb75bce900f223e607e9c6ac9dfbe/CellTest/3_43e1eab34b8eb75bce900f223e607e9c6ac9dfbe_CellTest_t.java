 package edu.ncsu.csc216.fire;
 
 import junit.framework.TestCase;
 
 /**
  * 
  * @author Andrew Kofink and James Bruening
  *
  */
 public class CellTest extends TestCase {
 	
 	/**
 	 *A test Cell
 	 */
 	public static Cell c;
 
 	/**
 	 *Initial state
 	 */
 	private static final int TESTSTATE = Cell.EMPTY;
 
 	/**
 	 *New state
 	 */
 	private static final int NEWSTATE = Cell.TREE;
 
 	/**
 	 *Number of times to test probability
 	 */
 	private static final int TIMESTOTEST = 1000;
 
 	/**
 	 *Probability of adjacent trees burning
 	 */
 	private static final double PROBTEST = .55;
 
 	/**
 	 *Margin of error for test
 	 */
 	private static final double MARGIN = .1;
 
 	/**
 	 *Number of adjacent trees (N, S, E, W)
 	 */
 	private static final int NUMOFADJACENTTREES = 4;
 	
 	/**
 	 *Builds a cell, c.
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		c = new Cell(TESTSTATE);
 	}
 	
 	/**
 	 *Tests whether the proper state is returned
 	 */
 	public void testGetState() {
 		assertEquals (c.getState(), TESTSTATE);
 	}
 	
 	/**
 	 *Tests whether the state is successfully set
 	 */
 	public void testSetState() {
 		c.setState(NEWSTATE);
 		
 		assertEquals (c.getState(), NEWSTATE);
 	}
 	
 	/**
 	 *Tests the copy method of a cell
 	 */
 	public void testCopy() {
 		assertEquals (c, c.copy());
 	}
 	
 	/**
 	 *Tests the spreading of fire
 	 */
 	public void testSpread() {
 		int counter = 0;
 		for (int i = 0; i < TIMESTOTEST; i++) {
 			Cell[] cells = new Cell[NUMOFADJACENTTREES];
 			for (int j = 0; j < cells.length; j++) {
 				cells[j] = new Cell(NEWSTATE);
 			}
 			c.spread(PROBTEST, cells[0], cells[1], cells[2], cells[NUMOFADJACENTTREES-1]);
 			for (int j = 0; j < cells.length; j++) {
 				if (cells[j].getState() == Cell.BURNING) {
 					counter++;
 				}
 			}
 		}
		double percentage = (double) counter / (TIMESTOTEST * NUMOFADJACENTTREES);
		assertTrue (percentage > PROBTEST - MARGIN && percentage < PROBTEST + MARGIN);
 	}
 
 }
