 package org.logparser.stats;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 import org.apache.commons.math.stat.descriptive.SummaryStatistics;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Tests for {@link StandardDeviationPredicate}.
  * 
  * @author jorge.decastro
  * 
  */
 public class StandardDeviationPredicateTest {
 	private StandardDeviationPredicate underTest;
 
 	@Before
 	public void setUp() {
 		underTest = new StandardDeviationPredicate();
 	}
 
 	@After
 	public void tearDown() {
 		underTest = null;
 	}
 
 	@Test(expected = NullPointerException.class)
 	public void testNullArguments() {
 		assertThat(underTest.apply(null), is(false));
 	}
 
 	@Test
 	public void testNumberOfStandardDeviationsHasDefault() {
		assertThat(underTest.getNumberOfStandardDeviations(), is(1));
 	}
 
 	@Test
 	public void testNumberOfStandardDeviationsGivenIsNumberObtained() {
 		underTest = new StandardDeviationPredicate(3);
		assertThat(underTest.getNumberOfStandardDeviations(), is(3));
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testStandardDeviationsNumberGivenMustBePositiveNumber() {
 		underTest = new StandardDeviationPredicate(-10);
 	}
 
 	@Test
 	public void testDeltaOfAveragesBelowOrEqualToStandardDeviationGivenYieldsFalse() {
 		SummaryStatistics stats = new SummaryStatistics();
 		stats.addValue(70);
 		stats.addValue(90);
 		double current = 85; // sd = 14.142135623730951, delta = 5
 		PredicateArguments arguments = new PredicateArguments(stats, current);
 		assertThat(underTest.apply(arguments), is(false));
 	}
 
 	@Test
 	public void testDeltaOfAveragesAboveStandardDeviationGivenYieldsTrue() {
 		SummaryStatistics stats = new SummaryStatistics();
 		stats.addValue(70);
 		stats.addValue(90);
 		double current = 100; // sd = 14.142135623730951, delta = 20
 		PredicateArguments arguments = new PredicateArguments(stats, current);
 		assertThat(underTest.apply(arguments), is(true));
 	}
 }
