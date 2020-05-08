 package com.max.algs.regexp_engine;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 public class RegexpEngineTest {
 	
 	
 	@Test
 	public void regexpMatchingWithPlus(){
 		
 		RegexpEngine regexp = new RegexpEngine("a+b");	
 		
		assertTrue( regexp.match("ab") );
//		assertTrue( regexp.match("aaab") );
		
 	}
 	
 	
 	@Test
 	public void regexpMatchingWithAsterix(){
 		
 		RegexpEngine regexp = new RegexpEngine("a*b");		
 		
 //		assertTrue( regexp.match("ab") );
 //		assertTrue( regexp.match("aaaab") );
 //		assertTrue( regexp.match("b") );
 		
 	
 //		assertFalse( regexp.match("abb") );
 //		assertFalse( regexp.match("a") );
 //		assertFalse( regexp.match("") );		
 //		assertFalse( regexp.match("acbb") );
 //		assertFalse( regexp.match("aacbb") );
 
 	}
 	
 	
 	@Test
 	public void regexpMatchingWithDot(){
 		
 		RegexpEngine regexp = new RegexpEngine("a.b");		
 		
 		assertTrue( regexp.match("acb") );
 		assertTrue( regexp.match("adb") );
 		
 		assertFalse( regexp.match("ab") );
 		assertFalse( regexp.match("a") );
 		assertFalse( regexp.match("b") );
 		assertFalse( regexp.match("") );		
 		assertFalse( regexp.match("acbb") );
 		assertFalse( regexp.match("aacbb") );
 
 	}
 	
 	@Test
 	public void regexpMatchingWithQuestionMarkAtEnd(){
 		
 		RegexpEngine regexp = new RegexpEngine("ab?");		
 		
 		assertTrue( regexp.match("ab") );
 		assertTrue( regexp.match("a") );		
 		assertFalse( regexp.match("b") );
 		assertFalse( regexp.match("") );		
 		assertFalse( regexp.match("aab") );
 
 	}
 	
 	
 	@Test
 	public void regexpMatchingWithTwoQuestionMarks(){
 		
 		RegexpEngine regexp = new RegexpEngine("a?b?");
 		
 		assertTrue( regexp.match("ab") );
 		assertTrue( regexp.match("a") );
 		assertTrue( regexp.match("b") );
 		assertTrue( regexp.match("") );
 		
 		assertFalse( regexp.match("aab") );
 
 	}
 
 	
 	@Test
 	public void regexpMatchingWithQuestionMark(){
 		
 		RegexpEngine regexp = new RegexpEngine("a?b");
 		
 		assertTrue( regexp.match("ab") );
 		assertTrue( regexp.match("b") );
 		
 		assertFalse( regexp.match("aab") );
 
 	}
 	
 		@Test
 	public void simpleRegexpMatchingWithNumbers(){
 		
 		RegexpEngine regexp = new RegexpEngine("a23b");
 		
 		assertTrue( regexp.match("a23b") );		
 		assertFalse( regexp.match("ab") );
 		assertFalse( regexp.match("a23bb") );
 	
 	}
 	
 	
 	@Test
 	public void simpleRegexpMatching(){
 		
 		RegexpEngine regexp = new RegexpEngine("abba");
 		
 		assertTrue( regexp.match("abba") );		
 		assertFalse( regexp.match("abbaa") );
 		assertFalse( regexp.match("babba") );
 		assertFalse( regexp.match("abbc") );		
 	}
 	
 	@Test
 	public void simpleRegexpMatchingWithSingleChar(){
 		
 		RegexpEngine regexp = new RegexpEngine("a");
 		
 		assertTrue( regexp.match("a") );		
 		assertFalse( regexp.match("b") );
 		assertFalse( regexp.match("aa") );	
 	}
 	
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void incorrectRegexpWithTwoAsterix(){		
 		new RegexpEngine("a**b");
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void incorrectRegexpWithTwoPluses(){		
 		new RegexpEngine("a++b");
 	}
 	
 
 }
