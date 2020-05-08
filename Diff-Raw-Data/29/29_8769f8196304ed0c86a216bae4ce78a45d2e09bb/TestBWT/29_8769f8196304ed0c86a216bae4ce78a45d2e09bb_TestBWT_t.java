 package org.biotoolkit.algorithms;
 
 import static org.junit.Assert.*;
 import junit.framework.Assert;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.biotoolkit.algorithms.bwtx.BWTCompressor;
 import org.junit.Test;
 
 /**
 *  PROJECT: Burrows Wheeler transform of a DNA sequence
 */
 
 /**
  * JAYS EXAMPLE JUNIT TEST
 * Marty: Write your unit tests like this:
 *
 * 1) Call the function in the OTHER class. (i.e. bwt.method.java)
 * 2) Store the return value in a variable (i.e. aString)
 * 3) Assert something about that value (i.e. assertTrue, assertFalse).
 
 
 @Test
 public void testBWTExample()
 {
 String aString = bwt.aSimpleMethod();
 Assert.assertEquals(aString,"Look how easy junit is");
 Assert.assertTrue(aString.equals("Look how easy junit is"));
 Assert.assertFalse(1==0+2);
 }
 */
 
 /**
  * Creates a test suite containing all Unit Tests
  * for this component.
  *
  * @return A test suite containing all unit tests.
  */
 public class TestBWT 
 {
 /**
 	public static void main(String[] args) // use main for testing then depricat
 	{
 		
 		
 	}
 */	
 	// test to check sequence length
 	@Test	
 	public void testCalcSequenceLength()
 		{		
 			Assert.assertTrue(1==1); //always passes 
 			Assert.assertTrue("GGATCCTTT".length()==9); //compare string length 
 			Assert.assertTrue("aGGATCCTTTz".length()==11); //compare string alength
 			Assert.assertFalse(1==0); //always passes 	
 		}
 		
 		// test to check accuracy for append sequence
 		@Test
 		public void testSequenceAppend()
 		{
 			String appendSeq = BWTApplication.sequenceAppend("GGATCCTTT");
 			Assert.assertEquals(appendSeq,"aGGATCCTTTz");
 			Assert.assertTrue(appendSeq.equals("aGGATCCTTTz"));
 			Assert.assertFalse(1==0+2);
 		}
 		
 		// test to check for sequence rotation of first array element
 		@Test
 		public void testSequenceRotation()
 		{
 			String [] rotateSeq = BWTApplication.sequenceRotateArray("aGGATCCTTTz", 11);
 			Assert.assertEquals(rotateSeq[1],"zaGGATCCTTT");
 			Assert.assertTrue(rotateSeq[1].equals("zaGGATCCTTT"));
 			Assert.assertEquals(rotateSeq[3],"TTzaGGATCCT");
 			Assert.assertTrue(rotateSeq[3].equals("TTzaGGATCCT"));
 			Assert.assertEquals(rotateSeq[10],"GGATCCTTTza");
 			Assert.assertTrue(rotateSeq[10].equals("GGATCCTTTza"));
 			Assert.assertFalse(1==0+2);
 		}
 		
 		// test to check for sequence sort of first  and last array element
 		public void testSequenceSort(String[] rotateSeq)
		{ 
		    
 		}
 		
 		// test to check for bwt transform
 		public void testBWTTransform(String[] sortSeqTest)
 			{		
 				Assert.assertTrue(1==1); 
 				String bwtSeqTest = BWTApplication.sequenceBWT(sortSeqTest, 11);// JAY NOT CLEAR WHY THIS DOES NOT WORK - same as above
 				Assert.assertEquals(bwtSeqTest,"zGTCGaACTTT");
 				Assert.assertTrue(bwtSeqTest.equals("zGTCGaACTTT"));
 				Assert.assertFalse(1==0+2); 	
 			}
 			
 		// test to check for BWTCompress 
 		@Test	
 		public void testBWTComp()
 			{		
 				Assert.assertTrue(1==1); 
 				String bwtCompress = BWTCompressor.compress("zGTCGaACTTT", 11);
 				Assert.assertEquals(bwtCompress,"zGTCGaACT3");
 				Assert.assertTrue(bwtCompress.equals("zGTCGaACT3"));
 				Assert.assertFalse(1==0+2); 	
 			}		
 }
