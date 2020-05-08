 package org.hitzemann.mms.solver.rule.knuth;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 
 import org.hitzemann.mms.model.SpielKombination;
 import org.hitzemann.mms.solver.rule.IRule;
 import org.junit.Test;
 
 /**
 * Tests für {@link KnuthRuleFactory}.
  * 
  * @author schusterc
  */
 public final class KnuthRuleFactoryTest {
 
 	/**
 	 * Die zu testende Instanz.
 	 */
 	private final IKnuthRuleFactory underTest = new KnuthRuleFactory();
 
 	/**
 	 * Test für die Methode zur Erzeugung einer {@link GuessFirstRule}.
 	 */
 	@Test
 	public void testCreateGuessFirstRule() {
 		final IRule rule = underTest.createGuessFirstRule(1, 2);
 		assertEquals(GuessFirstRule.class, rule.getClass());
 	}
 
 	/**
 	 * Test für die Methode zur Erzeugung einer {@link GuessFixedSimpleRule}.
 	 */
 	@Test
 	public void tetCreateGuessFixedSimpleRule() {
 		final IRule rule = underTest.createGuessFixedSimpleRule(1,
 				new SpielKombination(new int[0]), mock(IRule.class));
 		assertEquals(GuessFixedSimpleRule.class, rule.getClass());
 	}
 
 	/**
 	 * Test für die Methode zur Erzeugung einer {@link GuessFixedComplexRule}.
 	 */
 	@Test
 	public void testCreateGuessFixedComplexRule() {
 		final IRule rule = underTest.createGuessFixedComplexRule(1,
 				new SpielKombination(new int[0]), new IRule[1][0]);
 		assertEquals(GuessFixedComplexRule.class, rule.getClass());
 	}
 }
