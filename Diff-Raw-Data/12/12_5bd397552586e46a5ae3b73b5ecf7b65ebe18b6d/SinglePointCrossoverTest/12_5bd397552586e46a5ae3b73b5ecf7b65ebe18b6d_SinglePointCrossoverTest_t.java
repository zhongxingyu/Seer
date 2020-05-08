 /*
  *	Copyright (C) 2011 by Allamanis Miltiadis
  *
  *	Permission is hereby granted, free of charge, to any person obtaining a copy
  *	of this software and associated documentation files (the "Software"), to deal
  *	in the Software without restriction, including without limitation the rights
  *	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *	copies of the Software, and to permit persons to whom the Software is
  *	furnished to do so, subject to the following conditions:
  *
  *	The above copyright notice and this permission notice shall be included in
  *	all copies or substantial portions of the Software.
  *
  *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *	THE SOFTWARE.
  */
 /**
  * 
  */
 package gr.auth.ee.lcs.geneticalgorithm.operators;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import gr.auth.ee.lcs.AbstractLearningClassifierSystem;
 import static org.easymock.EasyMock.*;
 import gr.auth.ee.lcs.classifiers.Classifier;
 import gr.auth.ee.lcs.data.AbstractUpdateStrategy;
 import gr.auth.ee.lcs.data.ClassifierTransformBridge;
 import gr.auth.ee.lcs.utilities.ExtendedBitSet;
 import gr.auth.ee.lcs.utilities.SettingsLoader;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import org.junit.Test;
 
 /**
  * Test the single point crossover.
  * 
  * @author Miltos Allamanis
  * 
  */
 public class SinglePointCrossoverTest extends SinglePointCrossover {
 
 	private static final AbstractLearningClassifierSystem lcs = createMock(AbstractLearningClassifierSystem.class);
 
 	public SinglePointCrossoverTest() throws IOException {
 		super(lcs);
 		lcs.setElements(createMock(ClassifierTransformBridge.class),
 				createMock(AbstractUpdateStrategy.class));
 		SettingsLoader.loadSettings();
 	}
 
 	/**
 	 * Test method for
 	 * {@link gr.auth.ee.lcs.geneticalgorithm.operators.SinglePointCrossover#performCrossover(gr.auth.ee.lcs.utilities.ExtendedBitSet, gr.auth.ee.lcs.utilities.ExtendedBitSet, int)}
 	 * .
 	 */
 	@Test
 	public void testOperateCrossover() {
 		final ExtendedBitSet chromosome1 = new ExtendedBitSet("0000000");
 		final ExtendedBitSet chromosome2 = new ExtendedBitSet("1111111");
 
		expect(lcs.getUpdateStrategy().createStateClassifierObject()).andReturn(null).anyTimes();
		expect(lcs.getUpdateStrategy().getComparisonValue(anyObject(Classifier.class), anyInt())).andReturn(0.).anyTimes();
		lcs.getUpdateStrategy().setComparisonValue(anyObject(Classifier.class), anyInt(), anyDouble());
		expectLastCall().anyTimes();
		
		replay(lcs.getUpdateStrategy());
 		final Classifier cl1 = Classifier.createNewClassifier(lcs, chromosome1);
 		final Classifier cl2 = Classifier.createNewClassifier(lcs, chromosome2);
		
 		final boolean atLeastOnce[] = new boolean[8];
 		Arrays.fill(atLeastOnce, false);
 
 		for (int i = 0; i < 500; i++) {
 			Classifier child = operate(cl1, cl2);
 			final String result = child.getSubSet(0, 7).toString();
 
 			boolean isRight = false;
 
 			isRight |= (result.equals("0000000"));
 			isRight |= (result.equals("1000000"));
 			isRight |= (result.equals("1100000"));
 			isRight |= (result.equals("1110000"));
 			isRight |= (result.equals("1111000"));
 			isRight |= (result.equals("1111100"));
 			isRight |= (result.equals("1111110"));
 			isRight |= (result.equals("1111111"));
 
 			assertTrue(isRight);
 
 			if (result.equals("0000000"))
 				atLeastOnce[0] = true;
 			if (result.equals("1000000"))
 				atLeastOnce[1] = true;
 			if (result.equals("1100000"))
 				atLeastOnce[2] = true;
 			if (result.equals("1110000"))
 				atLeastOnce[3] = true;
 			if (result.equals("1111000"))
 				atLeastOnce[4] = true;
 			if (result.equals("1111100"))
 				atLeastOnce[5] = true;
 			if (result.equals("1111110"))
 				atLeastOnce[6] = true;
 			if (result.equals("1111111"))
 				atLeastOnce[7] = true;
 		}
 
 		// Check if at least once all possible crossovers have been made
 		for (int i = 0; i < atLeastOnce.length; i++)
 			assertTrue("This test might fail with probabilty (.125)^500",
 					atLeastOnce[i]);
		verify(lcs.getUpdateStrategy());
 	}
 
 	/**
 	 * Test method for
 	 * {@link gr.auth.ee.lcs.geneticalgorithm.operators.SinglePointCrossover#performCrossover(gr.auth.ee.lcs.utilities.ExtendedBitSet, gr.auth.ee.lcs.utilities.ExtendedBitSet, int)}
 	 * .
 	 */
 	@Test
 	public void testPerformCrossover() {
 		ExtendedBitSet chromosome1 = new ExtendedBitSet("0000000");
 		ExtendedBitSet chromosome2 = new ExtendedBitSet("1111111");
 
 		assertEquals("Chromosomes do not crossover correctly at position 3",
 				performCrossover(chromosome1, chromosome2, 3).toString(),
 				"1111000");
 		assertEquals("Chromosomes do not crossover correctly at position 0",
 				performCrossover(chromosome1, chromosome2, 0).toString(),
 				"1111111");
 		assertEquals("Chromosomes do not crossover correctly at position 7",
 				performCrossover(chromosome1, chromosome2, 7).toString(),
 				"0000000");
 
 		chromosome1 = new ExtendedBitSet("1001001");
 		chromosome2 = new ExtendedBitSet("0011001");
 		assertEquals("Chromosomes do not crossover correctly at position 1",
 				performCrossover(chromosome1, chromosome2, 1).toString(),
 				"0011001");
 		assertEquals("Chromosomes do not crossover correctly at position 5",
 				performCrossover(chromosome1, chromosome2, 5).toString(),
 				"0001001");
 	}
 
 }
