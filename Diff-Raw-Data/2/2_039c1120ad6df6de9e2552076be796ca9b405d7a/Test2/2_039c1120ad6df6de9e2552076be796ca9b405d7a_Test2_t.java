 package com.biogenidec.datasciences.codetest;
 
 import static org.junit.Assert.*;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Test;
 
 public class Test2 {
 
 	private static final Log log = LogFactory.getLog(Test2.class);
 
 	/**
 	 * Calculate all the possible permutations of the letters in the input
 	 * string and sort the results to match the desired output string.
 	 */
 	@Test
 	public void test() {
 
 		/*
 		 * Your code - begin **********************************************
 		 */
 
 		/*
 		 * Replace PermuationMock with your own implementation of the
 		 * Permutation interface
 		 */
 		Permutation permutation = new PermutationMock();
 
 		/*
 		 * Your code - end *************************************************
 		 */
 
 		String input1 = "ABC";
 		String desiredoutput1 = "ABC ACB BAC BCA CAB CBA";
 		
 		String computedoutput1 = permutation.calculate(input1);
 
 		log.debug("Input 1:           " + input1);
 		log.debug("Desired output 1:  " + desiredoutput1);
 		log.debug("Computed output 1: " + computedoutput1);
 
 		assertEquals("Computed output is not correct", desiredoutput1,
 				computedoutput1);
 
 
 		String input2 = "ABCD";
 		String desiredoutput2 = "ABCD ABDC ACBD ACDB ADBC ADCB BACD BADC BCAD BCDA BDAC BDCA CABD CADB CBAD CBDA CDAB CDBA DABC DACB DBAC DBCA DCAB DCBA";
 		
		String computedoutput2 = permutation.calculate(input2);
 
 		log.debug("Input 2:           " + input2);
 		log.debug("Desired output 2:  " + desiredoutput2);
 		log.debug("Computed output 2: " + computedoutput2);
 
 		assertEquals("Computed output is not correct", desiredoutput2,
 				computedoutput2);
 		
 		
 	}
 
 }
