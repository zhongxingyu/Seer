 package com.ezgliding.igc;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 
 public class CandidateTest {
 
 	private RectangleSet set;
 
 	@Before
 	public void setUp() {
 		ArrayList<Fix> fixes = new ArrayList<Fix>();
 		fixes.add(new Fix(new Date(), 45.888, 108.999, 0, 0, 'V'));
 		fixes.add(new Fix(new Date(), 44.223, 109.112, 0, 0, 'V'));
 		fixes.add(new Fix(new Date(), 43.123, 109.998, 0, 0, 'V'));
 
 		set = new RectangleSet(fixes);
 	}
 
 	@Test
 	public void testCreationEmpty() {
 		Candidate candate = new Candidate();
 		assertEquals(0, candate.getRectangles().size());
 	}
 
 	@Test
 	public void testCreationNull() {
 		Candidate candate = new Candidate(null);
 	}
 
 	@Test
 	public void testCreation() {
 		ArrayList<RectangleSet> rects = new ArrayList<RectangleSet>();
 		rects.add(set);
 		Candidate candate = new Candidate(rects);
 		assertEquals(1, candate.getRectangles().size());
 		assertEquals(set, candate.getRectangles().get(0));
 	}
 
 	@Test
 	public void testMax() {
 		ArrayList<RectangleSet> sets = new ArrayList<RectangleSet>();
 
 		ArrayList<Fix> fixes1 = new ArrayList<Fix>();
 		fixes1.add(new Fix(new Date(), 47.900, 106.700, 0, 0, 'V'));
 		fixes1.add(new Fix(new Date(), 45.900, 108.700, 0, 0, 'V'));
 		sets.add(new RectangleSet(fixes1));
 
 		ArrayList<Fix> fixes2 = new ArrayList<Fix>();
 		fixes2.add(new Fix(new Date(), 43.900, 110.700, 0, 0, 'V'));
 		fixes2.add(new Fix(new Date(), 41.900, 112.700, 0, 0, 'V'));
 		sets.add(new RectangleSet(fixes2));
 
 		ArrayList<Fix> fixes3 = new ArrayList<Fix>();
 		fixes3.add(new Fix(new Date(), 40.700, 114.800, 0, 0, 'V'));
 		fixes3.add(new Fix(new Date(), 38.120, 115.330, 0, 0, 'V'));
 		sets.add(new RectangleSet(fixes3));
 
 		Candidate candate = new Candidate(sets);
 		double expected = Util.distance(sets.get(0).nw, sets.get(1).se) 
 			+ Util.distance(sets.get(1).nw, sets.get(2).se);
 		assertEquals(expected, candate.max(), 0.0);
 	}
 
 	@Test
 	public void testMin() {
 		ArrayList<RectangleSet> sets = new ArrayList<RectangleSet>();
 
 		ArrayList<Fix> fixes1 = new ArrayList<Fix>();
 		fixes1.add(new Fix(new Date(), 47.900, 106.700, 0, 0, 'V'));
 		fixes1.add(new Fix(new Date(), 45.900, 108.700, 0, 0, 'V'));
 		sets.add(new RectangleSet(fixes1));
 
 		ArrayList<Fix> fixes2 = new ArrayList<Fix>();
 		fixes2.add(new Fix(new Date(), 43.900, 110.700, 0, 0, 'V'));
 		fixes2.add(new Fix(new Date(), 41.900, 112.700, 0, 0, 'V'));
 		sets.add(new RectangleSet(fixes2));
 
 		ArrayList<Fix> fixes3 = new ArrayList<Fix>();
 		fixes3.add(new Fix(new Date(), 40.700, 114.800, 0, 0, 'V'));
 		fixes3.add(new Fix(new Date(), 38.120, 115.330, 0, 0, 'V'));
 		sets.add(new RectangleSet(fixes3));
 
 		Candidate candate = new Candidate(sets);
 		double expected = Util.distance(sets.get(0).se, sets.get(1).nw) 
 			+ Util.distance(sets.get(1).se, sets.get(2).nw);
		assertEquals(expected, candate.min(), 0.0);
 	}
 
 	@Test
 	public void testAdd() {
 		Candidate candate = new Candidate();
 		candate.add(set);
 		assertEquals(1, candate.getRectangles().size());
 
 		ArrayList<Fix> fixes = new ArrayList<Fix>();
 		fixes.add(new Fix(new Date(), 42.111, 107.333, 0, 0, 'V'));
 		fixes.add(new Fix(new Date(), 44.411, 103.333, 0, 0, 'A'));
 		RectangleSet newSet = new RectangleSet(fixes);
 		candate.add(newSet);
 		
 		assertEquals(2, candate.getRectangles().size());
 		assertEquals(newSet, candate.getRectangles().get(1));
 	}
 
 	@Test(expected=IllegalArgumentException.class)
 	public void testAddNull() {
 		Candidate candate = new Candidate();
 		candate.add(null);
 	}
 
 	@Test
 	public void testGetRectangles() {
 		Candidate candate = new Candidate();
 		candate.add(set);
 		assertNotNull(candate.getRectangles());
 		assertEquals(1, candate.getRectangles().size());
 		assertEquals(set, candate.getRectangles().get(0));
 	}
 
 	@Test
 	public void testReplaceSingle() {
 		ArrayList<RectangleSet> sets = new ArrayList<RectangleSet>();
 
 		ArrayList<Fix> fixes1 = new ArrayList<Fix>();
 		fixes1.add(new Fix(new Date(), 47.900, 106.700, 0, 0, 'V'));
 		fixes1.add(new Fix(new Date(), 45.900, 108.700, 0, 0, 'V'));
 		sets.add(new RectangleSet(fixes1));
 
 		ArrayList<Fix> fixes2 = new ArrayList<Fix>();
 		fixes2.add(new Fix(new Date(), 43.900, 110.700, 0, 0, 'V'));
 		fixes2.add(new Fix(new Date(), 41.900, 112.700, 0, 0, 'V'));
 		sets.add(new RectangleSet(fixes2));
 	
 		Candidate candate = new Candidate(sets);
 
 		ArrayList<Fix> fixes3 = new ArrayList<Fix>();
 		fixes3.add(new Fix(new Date(), 45.900, 112.700, 0, 0, 'V'));
 		fixes3.add(new Fix(new Date(), 43.900, 115.700, 0, 0, 'V'));
 		RectangleSet replacement = new RectangleSet(fixes3);
 		
 		assertEquals(2, candate.getRectangles().size());
 		assertFalse(replacement.equals(candate.getRectangles().get(1)));
 		candate.replace(1, replacement);
 		assertEquals(2, candate.getRectangles().size());
 		assertEquals(replacement, candate.getRectangles().get(1));
 	}
 
 	@Test
 	public void testReplaceMultiple() {
 		ArrayList<RectangleSet> sets = new ArrayList<RectangleSet>();
 
 		ArrayList<Fix> fixes1 = new ArrayList<Fix>();
 		fixes1.add(new Fix(new Date(), 47.900, 106.700, 0, 0, 'V'));
 		fixes1.add(new Fix(new Date(), 45.900, 108.700, 0, 0, 'V'));
 		sets.add(new RectangleSet(fixes1));
 
 		ArrayList<Fix> fixes2 = new ArrayList<Fix>();
 		fixes2.add(new Fix(new Date(), 43.900, 110.700, 0, 0, 'V'));
 		fixes2.add(new Fix(new Date(), 41.900, 112.700, 0, 0, 'V'));
 		sets.add(new RectangleSet(fixes2));
 	
 		Candidate candate = new Candidate(sets);
 
 		ArrayList<Fix> fixes3 = new ArrayList<Fix>();
 		fixes3.add(new Fix(new Date(), 45.900, 112.700, 0, 0, 'V'));
 		fixes3.add(new Fix(new Date(), 43.900, 115.700, 0, 0, 'V'));
 		RectangleSet replacement1 = new RectangleSet(fixes3);
 		ArrayList<Fix> fixes4 = new ArrayList<Fix>();
 		fixes4.add(new Fix(new Date(), 47.900, 122.700, 0, 0, 'V'));
 		fixes4.add(new Fix(new Date(), 49.900, 125.700, 0, 0, 'V'));
 		RectangleSet replacement2 = new RectangleSet(fixes3);
 		
 		assertEquals(2, candate.getRectangles().size());
 		assertFalse(replacement1.equals(candate.getRectangles().get(1)));
 		assertFalse(replacement2.equals(candate.getRectangles().get(1)));
 		candate.replace(1, new RectangleSet[] { replacement1, replacement2 });
 		assertEquals(3, candate.getRectangles().size());
 		assertEquals(replacement1, candate.getRectangles().get(1));
 		assertEquals(replacement2, candate.getRectangles().get(2));
 	}
 
 	@Test
 	public void testLargestDiagonal() {
 		ArrayList<RectangleSet> sets = new ArrayList<RectangleSet>();
 
 		ArrayList<Fix> fixes1 = new ArrayList<Fix>();
 		fixes1.add(new Fix(new Date(), 47.900, 106.700, 0, 0, 'V'));
 		fixes1.add(new Fix(new Date(), 45.900, 108.700, 0, 0, 'V'));
 		sets.add(new RectangleSet(fixes1));
 
 		ArrayList<Fix> fixes2 = new ArrayList<Fix>();
 		fixes2.add(new Fix(new Date(), 40.900, 110.700, 0, 0, 'V'));
 		fixes2.add(new Fix(new Date(), 35.900, 103.700, 0, 0, 'V'));
 		sets.add(new RectangleSet(fixes2));
 	
 		ArrayList<Fix> fixes3 = new ArrayList<Fix>();
 		fixes3.add(new Fix(new Date(), 48.900, 107.700, 0, 0, 'V'));
 		fixes3.add(new Fix(new Date(), 49.900, 108.700, 0, 0, 'V'));
 		sets.add(new RectangleSet(fixes3));
 
 		Candidate candate = new Candidate(sets);
 		assertEquals(1, candate.largestDiagonal());
 	}
 
 	@Test
 	public void testClone() {
 		Candidate initial = new Candidate();
 		initial.add(set);
 
 		Candidate clone = initial.clone();
 		assertFalse(initial == clone);
 		assertTrue(initial.equals(clone));
 		assertEquals(initial.getRectangles().size(), clone.getRectangles().size());
 	}
 }
