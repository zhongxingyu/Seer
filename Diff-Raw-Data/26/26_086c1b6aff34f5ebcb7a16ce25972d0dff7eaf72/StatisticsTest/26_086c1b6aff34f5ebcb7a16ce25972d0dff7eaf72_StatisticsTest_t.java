 package galapagos.tests;
 
 import junit.framework.TestCase;
import galapagos.biotope.Statistics;
 
 public class StatisticsTest extends TestCase {
 
 	private Statistics statistics;
 
 	public void setUp () {
 		statistics = new Statistics();
 	}
 	
 	/**
 	 * Checks the initial values.
 	 */
 	public void testConstructor () {
 		assertEquals(0, statistics.getPopulation());
 		assertEquals(0, statistics.getBorn());
 		assertEquals(0, statistics.getDeadByAge());
 		assertEquals(0, statistics.getDeadByTicks());
 		assertEquals(0, statistics.getBornThisRound());
 		assertEquals(0, statistics.getDeadByAgeThisRound());
 		assertEquals(0, statistics.getDeadByTicksThisRound());
 		elementTest(statistics);
 	}
 	
 	/**
 	 * Tests the incPopulation() and decPopulation() of Statistics.
 	 */
 	public void testIncDecPopulation () {
 		statistics.incPopulation();
 		assertEquals(1, statistics.getPopulation());
 		assertEquals(0, statistics.getBorn());
 		assertEquals(0, statistics.getDeadByAge());
 		assertEquals(0, statistics.getDeadByTicks());
 		assertEquals(0, statistics.getBornThisRound());
 		assertEquals(0, statistics.getDeadByAgeThisRound());
 		assertEquals(0, statistics.getDeadByTicksThisRound());
 		elementTest(statistics);
 		
 		statistics.decPopulation();
 		assertEquals(0, statistics.getPopulation());
 		assertEquals(0, statistics.getBorn());
 		assertEquals(0, statistics.getDeadByAge());
 		assertEquals(0, statistics.getDeadByTicks());
 		assertEquals(0, statistics.getBornThisRound());
 		assertEquals(0, statistics.getDeadByAgeThisRound());
 		assertEquals(0, statistics.getDeadByTicksThisRound());
 		elementTest(statistics);
 	}
 	
 	/**
 	 * Tests the incBorn() of Statistics.
 	 */
 	public void testIncrementBorn () {
 		statistics.incBorn();
 		assertEquals(0, statistics.getPopulation());
 		assertEquals(1, statistics.getBorn());
 		assertEquals(0, statistics.getDeadByAge());
 		assertEquals(0, statistics.getDeadByTicks());
 		assertEquals(1, statistics.getBornThisRound());
 		assertEquals(0, statistics.getDeadByAgeThisRound());
 		assertEquals(0, statistics.getDeadByTicksThisRound());
 		elementTest(statistics);
 	}
 	
 	/**
 	 * Tests the incDeadByAge() of Statistics.
 	 */
 	public void testIncrementDeadByAge () {
 		statistics.incDeadByAge();
 		assertEquals(0, statistics.getPopulation());
 		assertEquals(0, statistics.getBorn());
 		assertEquals(1, statistics.getDeadByAge());
 		assertEquals(0, statistics.getDeadByTicks());
 		assertEquals(0, statistics.getBornThisRound());
 		assertEquals(1, statistics.getDeadByAgeThisRound());
 		assertEquals(0, statistics.getDeadByTicksThisRound());
 		elementTest(statistics);
 	}
 	
 	/**
 	 * Tests the incDeadByTicks() of Statistics.
 	 */
 	public void testIncrementDeadByTicks () {
 		statistics.incDeadByTicks();
 		assertEquals(0, statistics.getPopulation());
 		assertEquals(0, statistics.getBorn());
 		assertEquals(0, statistics.getDeadByAge());
 		assertEquals(1, statistics.getDeadByTicks());
 		assertEquals(0, statistics.getBornThisRound());
 		assertEquals(0, statistics.getDeadByAgeThisRound());
 		assertEquals(1, statistics.getDeadByTicksThisRound());
 		elementTest(statistics);
 	}
 	
 	/**
 	 * Tests that newRound resets the round-specific values.
 	 */
 	public void testNewRound () {
 		statistics.incBorn();
 		statistics.incDeadByAge();
 		statistics.incDeadByTicks();
 		statistics.incPopulation();
 		assertEquals(1, statistics.getPopulation());
 		assertEquals(1, statistics.getBorn());
 		assertEquals(1, statistics.getDeadByAge());
 		assertEquals(1, statistics.getDeadByTicks());
 		assertEquals(1, statistics.getBornThisRound());
 		assertEquals(1, statistics.getDeadByAgeThisRound());
 		assertEquals(1, statistics.getDeadByTicksThisRound());
 		elementTest(statistics);
 		
 		statistics.newRound();
 		assertEquals(1, statistics.getPopulation());
 		assertEquals(1, statistics.getBorn());
 		assertEquals(1, statistics.getDeadByAge());
 		assertEquals(1, statistics.getDeadByTicks());
 		assertEquals(0, statistics.getBornThisRound());
 		assertEquals(0, statistics.getDeadByAgeThisRound());
 		assertEquals(0, statistics.getDeadByTicksThisRound());
 		elementTest(statistics);
 	}
 	
 	/**
 	 * Test helper testing that the values returned by getStatByElement()
 	 * gives the same as the corresponding method on Statistics.
 	 * @param statistics The statistics that should be tested.
 	 */
 	public void elementTest(Statistics statistics) 
 	{
 		assertEquals(statistics.getPopulation(),
 				statistics.getStatByElement(Statistics.StatisticsElement.POPULATION));
 		assertEquals(statistics.getBornThisRound(),
 				statistics.getStatByElement(Statistics.StatisticsElement.BORN_THIS_ROUND));
 		assertEquals(statistics.getDeadByTicksThisRound(),
 				statistics.getStatByElement(Statistics.StatisticsElement.DEAD_TICKS_THIS_ROUND));
 		assertEquals(statistics.getDeadByAgeThisRound(),
 				statistics.getStatByElement(Statistics.StatisticsElement.DEAD_AGE_THIS_ROUND));
 		assertEquals(statistics.getBorn(),
 				statistics.getStatByElement(Statistics.StatisticsElement.BORN_TOTAL));
 		assertEquals(statistics.getDeadByTicks(),
 				statistics.getStatByElement(Statistics.StatisticsElement.DEAD_TICKS_TOTAL));
 		assertEquals(statistics.getDeadByAge(),
 				statistics.getStatByElement(Statistics.StatisticsElement.DEAD_AGE_TOTAL));
 	}
 }
