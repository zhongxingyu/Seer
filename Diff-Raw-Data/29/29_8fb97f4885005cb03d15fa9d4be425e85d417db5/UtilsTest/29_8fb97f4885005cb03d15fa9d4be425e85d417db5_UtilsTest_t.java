 package com.datascience.gal.decision;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.datascience.gal.AbstractDawidSkene;
 import com.datascience.utils.CostMatrix;
 
 /**
  * The class <code>UtilsTest</code> contains tests for the class {@link <code>Utils</code>}
  *
  * @pattern JUnit Test Case
  *
  * @generatedBy CodePro at 10/2/12 11:02 AM
  *
  * @author Konrad Kurdej
  *
  * @version $Revision$
  */
 public class UtilsTest {
 
 	/**
 	 * Run the Double calculateLabelCost(String, Map<String,Double>,
 	 * CostMatrix<String>) method test
 	 */
 	@Test
 	public void testCalculateLabelCost() {
 		String calcLabel = "OK";
 		Map<String, Double> labelProbabilities = new HashMap<String, Double>();
 		labelProbabilities.put("OK", 0.5);
 		labelProbabilities.put("LOK", 0.0);
 		labelProbabilities.put("HOK", 1.);
 
 		CostMatrix<String> costMatrix = new CostMatrix<String>();
 		costMatrix.add("OK", "OK", 1.);
		costMatrix.add("LOK", "OK", 10.);
		costMatrix.add("HOK", "OK", 20.);
 
 		Double result = Utils.calculateLabelCost(calcLabel, labelProbabilities,
 						costMatrix);
 		assertEquals(20.5, result, 0.0);
 	}
 
 	/**
 	 * Run the Map<T,Double> generateConstantDistribution(Collection<T>, double)
 	 * method test
 	 */
 	@Test
 	public void testGenerateConstantDistribution() {
 		Collection<String> objects = new LinkedList<String>();
 		for (int i = 0; i < 10; i++) {
 			objects.add("" + i);
 		}
 		double value = Math.PI;
 		Map<String, Double> result = Utils.generateConstantDistribution(
 										 objects, value);
 		for (String s : objects) {
 			assertEquals(Math.PI, result.get(s), 0.0);
 		}
 	}
 
 	/**
 	 * Run the Map<T,Double> generateUniformDistribution(Collection<T>) method
 	 * test
 	 */
 	@Test
 	public void testGenerateUniformDistribution() {
 		Collection<String> objects = new LinkedList<String>();
 		for (int i = 0; i < 8; i++) {
 			objects.add("" + i);
 		}
 		Map<String, Double> result = Utils.generateUniformDistribution(objects);
 		for (String s : objects) {
 			assertEquals(1. / 8, result.get(s), 0.0);
 		}
 	}
 
 	/**
 	 * Run the CostMatrix<String> getCategoriesCostMatrix(AbstractDawidSkene)
 	 * method test
 	 */
 	@Ignore("This might require more work ...")
 	@Test
 	public void testGetCategoriesCostMatrix() {
 		fail("Madnes ... I would have to create whole AbstractDawidSkene");
 		// add test code here
 		AbstractDawidSkene ads = null;
 		CostMatrix<String> result = Utils.getCategoriesCostMatrix(ads);
 		assertTrue(false);
 	}
 }
 
 /*
  * $CPS$ This comment was generated by CodePro. Do not edit it. patternId =
  * com.instantiations.assist.eclipse.pattern.testCasePattern strategyId =
  * com.instantiations.assist.eclipse.pattern.testCasePattern.junitTestCase
  * additionalTestNames = assertTrue = false callTestMethod = true createMain =
  * false createSetUp = false createTearDown = false createTestFixture = false
  * createTestStubs = true methods = package = com.datascience.gal.decision
  * package.sourceFolder = DSaS/src/test/java superclassType =
  * junit.framework.TestCase testCase = UtilsTest testClassType = Utils
  */
