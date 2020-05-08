 package ch.parametrix.common.util.ui.swing.clap;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 import ch.bitwave.platform.codestyle.PreConditionException;
 import ch.parametrix.common.util.ui.swing.WorldCoordinate;
 import ch.parametrix.common.util.ui.swing.contracts.ICanvasFeature;
 import ch.parametrix.common.util.ui.swing.contracts.WorldDistance;
 
 /**
  * 
  */
 public class ArrangeYGoalTest {
 	@Test
 	public void shouldNotCreateWithZeroMargin() {
 		try {
 			new ArrangeYGoal(0);
 		} catch (PreConditionException e) {
			assertEquals("The margin must not be 0 because it is used to determine orientation.",
 					e.getMessage());
 		}
 	}
 
 	@Test
 	public void shouldArrangeSingleSourcePositive() {
 		ICanvasFeature s1 = new SampleFeature("S1", new WorldCoordinate(50, -100),
 				new WorldDistance(400, 100));
 		ICanvasFeature t1 = new SampleFeature("T1", new WorldCoordinate(300, 250),
 				new WorldDistance(100, 50));
 		ArrangeYGoal goal = new ArrangeYGoal(100);
 		goal.addSource(s1);
 		goal.addTarget(t1);
 		goal.apply(null);
 		assertEquals(new WorldCoordinate(300, 100), t1.getPlacement());
 	}
 
 	@Test
 	public void shouldArrangeSingleSourceNegative() {
 		ICanvasFeature s1 = new SampleFeature("S1", new WorldCoordinate(50, -100),
 				new WorldDistance(400, 100));
 		ICanvasFeature t1 = new SampleFeature("T1", new WorldCoordinate(300, 250),
 				new WorldDistance(100, 50));
 		ArrangeYGoal goal = new ArrangeYGoal(-100);
 		goal.addSource(s1);
 		goal.addTarget(t1);
 		goal.apply(null);
 		assertEquals(new WorldCoordinate(300, -250), t1.getPlacement());
 	}
 }
