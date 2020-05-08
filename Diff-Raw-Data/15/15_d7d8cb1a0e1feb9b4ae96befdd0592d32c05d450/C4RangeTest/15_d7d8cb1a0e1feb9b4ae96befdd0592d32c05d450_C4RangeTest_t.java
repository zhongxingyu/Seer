 package edu.berkeley.gamesman.hadoop.game.connect4;
 
 import java.util.Collection;
 
 import junit.framework.Assert;
 
 import org.apache.hadoop.conf.Configuration;
 import org.junit.Test;
 
 import edu.berkeley.gamesman.hadoop.ranges.MoveWritable;
 import edu.berkeley.gamesman.hadoop.ranges.Range;
 import edu.berkeley.gamesman.hasher.genhasher.GenHasher;
 import edu.berkeley.gamesman.hasher.genhasher.Move;
 import edu.berkeley.gamesman.propogater.writable.list.WritableList;
 
 public class C4RangeTest {
 	@Test
 	public void testRangeSize() {
 		Configuration conf = new Configuration();
 		Connect4 c4 = new Connect4();
 
 		// Test default = 10
 		c4.setConf(conf);
 		Collection<Range<C4State>> roots = c4.getRoots();
 		assert roots.size() == 1;
 		Range<C4State> root = roots.iterator().next();
 		Assert.assertEquals(root.length(), 11);
 
 		// Test for 15
 		conf.setInt("gamesman.game.variance.length", 15);
 		c4.setConf(conf);
 		roots = c4.getRoots();
 		assert roots.size() == 1;
 		root = roots.iterator().next();
 		Assert.assertEquals(root.length(), 6);
 	}
 
 	@Test
 	public void testRangeChildrenPlaces() {
 		Configuration conf = new Configuration();
 		Connect4 c4 = new Connect4();
 
 		c4.setConf(conf);
 		Collection<Range<C4State>> roots = c4.getRoots();
 		assert roots.size() == 1;
 		Range<C4State> root = roots.iterator().next();
 		Assert.assertEquals(c4.getDivision(root), 0);
 		WritableList<Range<C4State>> toFill = new WritableList<Range<C4State>>(
 				(Class<? extends Range<C4State>>) Range.class
 						.<Range> asSubclass(Range.class),
 				null);
 		c4.getChildren(root, toFill);
 		for (int i = 0; i < toFill.length(); i++) {
 			Assert.assertEquals(c4.getDivision(toFill.get(i)), 1);
 		}
 	}
 
 	@Test
 	public void testFirstPosition() {
 		Configuration conf = new Configuration();
 		conf.setInt("gamesman.game.width", 4);
 		Connect4 c4 = new Connect4();
 		c4.setConf(conf);
 		C4State state = c4.newState();
 		Range<C4State> range = new Range<C4State>();
 		range.setLength(7);
 		for (int i = 0; i < 5; i++)
 			range.set(i, 1);
 		range.set(5, 0);
 		range.set(6, 15);
 		GenHasher<C4State> hasher = c4.getHasher();
 		range.addMoves(hasher, c4.getMoves());
 		long diff = range.firstPosition(hasher, 13, state);
 		Assert.assertTrue(diff == -1 || range.matches(state));
 	}
 
 	@Test
 	public void testStepping() {
 		Configuration conf = new Configuration();
 		conf.setInt("gamesman.game.width", 4);
 		Connect4 c4 = new Connect4();
 		c4.setConf(conf);
 		C4Hasher hasher = c4.getHasher();
 		Move[] moves = c4.getMoves();
 
 		Range<C4State> parent = new Range<C4State>(1, 1, 2, 1, 1, 1, 15);
 		parent.addMoves(hasher, moves);
 		Range<C4State> child = new Range<C4State>(1, 1, 2, 1, 1, 1, 16);
 		child.addMoves(hasher, moves);
 
 		testCase(hasher, parent, child, 12);
 		testCase(hasher, parent, child, 6);
 
 		parent = new Range<C4State>(2, 1, 2, 1, 1, 1, 15);
 		parent.addMoves(hasher, moves);
 		child = new Range<C4State>(2, 1, 2, 1, 1, 1, 16);
 		child.addMoves(hasher, moves);
 
 		testCase(hasher, parent, child, 5);
 	}
 
 	private void testCase(C4Hasher hasher, Range<C4State> parent,
 			Range<C4State> child, int childNum) {
 		MoveWritable move = parent.getMove(childNum);
 		long lParentPositions = parent.numPositions(hasher);
 		assert lParentPositions <= Integer.MAX_VALUE;
 		C4State state = hasher.getPoolState();
 		C4State childState = hasher.getPoolState();
 		try {
 			long lChange = parent.firstPosition(hasher, childNum, state);
 			if (lChange != -1) {
 				assert parent.matches(state);
 			}
 			assert lChange <= Integer.MAX_VALUE;
 			int change = (int) lChange;
 			long childPositions = child.numPositions(hasher);
 			for (int i = change; change != -1; i += change) {
				if (i == 251)
					System.out.println("Here");
 				Assert.assertEquals(parent.subHash(hasher, state), i);
 				hasher.makeMove(state, move, childState);
 				long lIndex = child.indexOf(hasher, state, move);
 				Assert.assertEquals(child.subHash(hasher, childState), lIndex);
 				assert lIndex <= Integer.MAX_VALUE;
 				Assert.assertTrue(lIndex >= 0);
 				Assert.assertTrue(lIndex < childPositions);
 				lChange = parent.step(hasher, childNum, state);
 				assert i + lChange <= Integer.MAX_VALUE;
 				change = (int) lChange;
 			}
 		} finally {
 			hasher.release(childState);
 			hasher.release(state);
 		}
 	}
 }
