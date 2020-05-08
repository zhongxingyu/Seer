 package ch.parametrix.common.util.ui.swing.clap;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.awt.Graphics2D;
 
 import org.junit.Test;
 
 import ch.bitwave.platform.codestyle.PreConditionException;
 import ch.parametrix.common.util.ui.swing.WorldCoordinate;
 import ch.parametrix.common.util.ui.swing.contracts.ICanvasFeature;
 import ch.parametrix.common.util.ui.swing.contracts.WorldDistance;
 
 /**
  * 
  */
 public class ArrangeXGoalTest {
 	@Test
 	public void shouldNotCreateWithZeroMargin() {
 		try {
 			new ArrangeXGoal(0);
 		} catch (PreConditionException e) {
			assertEquals(
					"Precondition failed: The margin must not be 0 because it is used to determine orientation.",
 					e.getMessage());
 		}
 	}
 
 	@Test
 	public void shouldArrangeSingleSourcePositive() {
 		ICanvasFeature s1 = new SampleFeature("S1", new WorldCoordinate(50, -100),
 				new WorldDistance(400, 100));
 		ICanvasFeature t1 = new SampleFeature("T1", new WorldCoordinate(300, 250),
 				new WorldDistance(100, 50));
 		ArrangeXGoal goal = new ArrangeXGoal(100);
 		goal.addSource(s1);
 		goal.addTarget(t1);
 		Graphics2D g = null;
 		assertTrue(goal.apply(g));
 		assertEquals(new WorldCoordinate(550, 250), t1.getPlacement());
 		assertFalse(goal.apply(g));
 	}
 
 	@Test
 	public void shouldArrangeSingleSourceNegative() {
 		ICanvasFeature s1 = new SampleFeature("S1", new WorldCoordinate(50, -100),
 				new WorldDistance(400, 100));
 		ICanvasFeature t1 = new SampleFeature("T1", new WorldCoordinate(300, 250),
 				new WorldDistance(100, 50));
 		ArrangeXGoal goal = new ArrangeXGoal(-100);
 		goal.addSource(s1);
 		goal.addTarget(t1);
 		Graphics2D g = null;
 		goal.apply(g);
 		assertEquals(new WorldCoordinate(-150, 250), t1.getPlacement());
 	}
 }
