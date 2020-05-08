 package test.unit;
 
 import static org.junit.Assert.*;
 import io.reader.IntervalParser;
 
 import org.junit.Test;
 
 public class TestIntervalParser {
 	private IntervalParser p;
 
 	@Test
 	public void testSingle() {
 		p = new IntervalParser("1");
 
 		assertEquals(new IntervalParser.Interval(1), p.getIntervals().get(0));
 	}
 
 	@Test
 	public void testMultipleSingles() {
 		p = new IntervalParser("1,2");
 
 		assertArrayEquals(
 				new IntervalParser.Interval[] { new IntervalParser.Interval(1),
 						new IntervalParser.Interval(2) }, p.getIntervals()
 						.toArray());
 	}
 
 	@Test
 	public void testRange() {
 		p = new IntervalParser("1-4");
 
 		assertArrayEquals(
 				new IntervalParser.Interval[] { new IntervalParser.Interval(1,
 						4) }, p.getIntervals().toArray());
 	}
 
 	@Test
 	public void testMultipleRanges() {
 		p = new IntervalParser("1-4,6-18");
 
 		assertArrayEquals(new IntervalParser.Interval[] {
 				new IntervalParser.Interval(1, 4),
 				new IntervalParser.Interval(6, 18) }, p.getIntervals()
 				.toArray());
 	}
 
 	@Test
 	public void testmixed() {
 		p = new IntervalParser("1-4,6-18,22,55,112-166");
 
 		assertArrayEquals(new IntervalParser.Interval[] {
 				new IntervalParser.Interval(1, 4),
 				new IntervalParser.Interval(6, 18),
 				new IntervalParser.Interval(22),
 				new IntervalParser.Interval(55),
 				new IntervalParser.Interval(112, 166) }, p.getIntervals()
 				.toArray());
 	}
 	
 	@Test
 	public void testWeirdRange() {
 		p = new IntervalParser("4-1");
 
 		assertArrayEquals(
 				new IntervalParser.Interval[] { new IntervalParser.Interval(1,
						4) }, p.getIntervals().toArray());
 	}
 
 	@Test
 	public void testdoublecomma() {
 		p = new IntervalParser("1-4,");
 		assertTrue(!p.isValid());
 	}
 	
 	@Test
 	public void testDoubleRange() {
 		p = new IntervalParser("1-4-");
 		assertTrue(!p.isValid());
 	}
 	
 	@Test
 	public void testDoubleRange2() {
 		p = new IntervalParser("1--4");
 		assertTrue(!p.isValid());
 	}
 }
