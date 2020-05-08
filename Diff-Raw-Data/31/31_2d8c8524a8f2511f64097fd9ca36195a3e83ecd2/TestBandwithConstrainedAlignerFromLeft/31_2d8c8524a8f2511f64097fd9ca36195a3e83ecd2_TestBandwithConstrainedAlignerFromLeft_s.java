 /** 
  * Author:  anthony.fodor@gmail.com
  * 
  * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version,
 * provided that any use properly credits the author.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details at http://www.gnu.org * * */
 
 package test.testReduceOTU;
 
 import java.util.List;
 
 import dynamicProgramming.PairedAlignment;
 
 import reduceOTU.DP_Expand;
 import reduceOTU.IndividualEdit;
 import reduceOTU.Reduce;
 import reduceOTU.ReducedTools;
 import junit.framework.TestCase;
 
 public class TestBandwithConstrainedAlignerFromLeft extends TestCase
 {
 	/*
 	public void testSingleMisMatch() throws Exception
 	{
 		StringBuffer buff = new StringBuffer();
 		
 		for( int x=0; x < 32; x++)
 			buff.append("X");
 		
 		String common = buff.toString();
 		String s1 = "ACTGACG" + common;
 		String s2 = "ACTGACT" + common;
 		
 		
 		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
 						32, 3);
 		
 		List<IndividualEdit> list = dp.getEditList();
 		assertEquals(list.size(),1);
 		assertEquals(dp.getNumErrors(),1);
 		//System.out.println(list);
 		assertEquals(list.get(0).toString(),"S6T" );
 		assertTrue(dp.alignmentWasSuccesful());
 	}
 	
 	public void testFailedAlignment()  throws Exception
 	{
 		StringBuffer buff = new StringBuffer();
 		
 		for( int x=0; x < 32; x++)
 			buff.append("X");
 		
 		String common = buff.toString();
 		String s1 = "AAAA" + common;
 		String s2 = "CCCC" + common;
 		
 		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
 				32, 3);
 		//System.out.println(dp.getEditList());
 		assertFalse(dp.alignmentWasSuccesful());
 		List<IndividualEdit> list = dp.getEditList();
 		
 		assertEquals(list.size(),4);
 		assertEquals(dp.getNumErrors(),4);
 		assertEquals(list.get(0).toString(), "S3C");
 		assertEquals(list.get(1).toString(), "S2C");
 		assertEquals(list.get(2).toString(), "S1C");
 		assertEquals(list.get(3).toString(), "S0C");
 		
 		dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
 				32, 4);
 		//System.out.println(dp.getEditList());
 		assertTrue(dp.alignmentWasSuccesful());
 		list = dp.getEditList();
 		
 		assertEquals(list.size(),4);
 		assertEquals(dp.getNumErrors(),4);
 		assertEquals(list.get(0).toString(), "S3C");
 		assertEquals(list.get(1).toString(), "S2C");
 		assertEquals(list.get(2).toString(), "S1C");
 		assertEquals(list.get(3).toString(), "S0C");
 	}
 
 	public void testSingleLeftAlignmentInsertionInString2() throws Exception
 	{
 		StringBuffer buff = new StringBuffer();
 		
 		for( int x=0; x < 32; x++)
 			buff.append("X");
 		
 		String common = buff.toString();
 		String s1 = "ACTGACTG" + common;
 		String s2 = "ACTGACT" + common;
 		
 		
 		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
 						32, 3);
 		
 		List<IndividualEdit> list = dp.getEditList();
 		assertEquals(list.size(),1);
 		assertEquals(dp.getNumErrors(),1);
 		//System.out.println(list);
 		assertEquals(list.get(0).toString(),"I6G" );
 		assertTrue(dp.alignmentWasSuccesful());
 	}
 	
 	public void testDoubleLeftAlignmentDeltionInString2() throws Exception
 	{
 		StringBuffer buff = new StringBuffer();
 		
 		for( int x=0; x < 32; x++)
 			buff.append("X");
 		
 		String common = buff.toString();
 		String s1 = "AAATTT" + common;
 		String s2 = "AAATTTTT" + common;
 		
 		
 		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
 				32, 3);
 
 		List<IndividualEdit> list = dp.getEditList();
 		//System.out.println(list);
 		assertEquals(list.size(),2);
 		assertEquals(dp.getNumErrors(),2);
 		assertEquals( list.get(0).toString(), "D4T" );
 		assertEquals( list.get(1).toString(), "D3T" );
 		assertTrue( dp.alignmentWasSuccesful());
 	}
 	
 	public void testSingleLeftAlignmentDeletionInString2() throws Exception
 	{
 		StringBuffer buff = new StringBuffer();
 		
 		for( int x=0; x < 32; x++)
 			buff.append("X");
 		
 		String common = buff.toString();
 		String s1 = "ACTGACT" + common;
 		String s2 = "ACTGACTG" + common;
 		
 		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
 						32, 3);
 		
 		List<IndividualEdit> list = dp.getEditList();
 		assertEquals(list.size(),1);
 		assertEquals(dp.getNumErrors(),1);
 		//System.out.println(list);
 		assertEquals(list.get(0).toString(),"D7G" );
 		assertTrue(dp.alignmentWasSuccesful());
 	}
 
 	public void testDoubleLeftAlignmentInsertionInString2() throws Exception
 	{
 		StringBuffer buff = new StringBuffer();
 		
 		for( int x=0; x < 32; x++)
 			buff.append("X");
 		
 		String common = buff.toString();
 		String s1 = "AAATTTTT" + common;
 		String s2 = "AAATTT" + common;
 		
 		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
 				32, 3);
 
 		List<IndividualEdit> list = dp.getEditList();
 		//System.out.println(list);
 		assertEquals(list.size(),2);
 		assertEquals(dp.getNumErrors(),2);
 		assertEquals( list.get(0).toString(), "I2T" );
 		assertEquals( list.get(1).toString(), "I2T" );
 		assertTrue( dp.alignmentWasSuccesful());
 	}
 	
 	public void testTrailingNotCountingAsErrors() throws Exception
 	{
 		StringBuffer buff = new StringBuffer();
 		
 		for( int x=0; x < 32; x++)
 			buff.append("X");
 		
 		String common = buff.toString();
 		String s1 = "AAAAAATTTTTTTG" + common;
 		String s2 = "AAAAAAAAAATTTTTTTC" + common;
 		
 		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
 				32, 3);
 
 		List<IndividualEdit> list = dp.getEditList();
 		//System.out.println(list);
 		assertEquals(list.size(),5);
 		assertEquals(dp.getNumErrors(),2);
 		assertTrue( dp.alignmentWasSuccesful());
 	}
 	*/
 	
 	public void testSomeSequence() throws Exception
 	{
 		StringBuffer buff = new StringBuffer();
 		
 		for( int x=0; x < 32; x++)
 			buff.append("X");
 		
 		String common = buff.toString();
 		String s1 =  "AAACCGGTTAAGGGGTTA" + common;
 		String s2 =  "AAGGGTTGGGGCTTG" + common;
 		
 		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
 				32, 20);
 
 		List<IndividualEdit> list = dp.getEditList();
 		System.out.println(list);
 		PairedAlignment pa = ReducedTools.getAlignment(s1, list);
 		System.out.println(pa.toString());
 		
 		assertEquals( pa.getFirstSequence().replaceAll("-",""), s1);
 	}
 }
