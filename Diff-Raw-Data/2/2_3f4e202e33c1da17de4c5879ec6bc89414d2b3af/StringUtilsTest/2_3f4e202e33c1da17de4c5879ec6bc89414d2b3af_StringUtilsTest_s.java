 package com.max.algs.string;
 
 import org.testng.AssertJUnit;
 import org.testng.annotations.Test;
 
import com.max.algs.util.StringUtils;

 public class StringUtilsTest {
 
 
     @Test
     public void longestStringWithoutRepNullRandom(){
         for( int i =0; i < 10; i++){
             String str = StringUtils.createASCIIString(100);
             String longest1 = StringUtils.longestStringWithoutRepBruteforce(str);
             String longest2 = StringUtils.longestStringWithoutRep(str);    
             AssertJUnit.assertEquals( "longest1 != longest2, longest1 = " + longest1 + ", longest2 = " + longest2, longest1, longest2 );
         }
     }
 
 
     @Test(expectedExceptions  = IllegalArgumentException.class)
     public void longestStringWithoutRepNullStr(){
         StringUtils.longestStringWithoutRep( null );
     }
 
 
     @Test
     public void longestStringWithoutRep(){
     	AssertJUnit.assertEquals( "bcade", StringUtils.longestStringWithoutRep("dabcade") );
     	AssertJUnit.assertEquals( "bcade", StringUtils.longestStringWithoutRep("dbcade") );
     	AssertJUnit.assertEquals( "fbcade", StringUtils.longestStringWithoutRep("fbcade") );
     	AssertJUnit.assertEquals( "bcade", StringUtils.longestStringWithoutRep("dabcaded") );
     	AssertJUnit.assertEquals( "a", StringUtils.longestStringWithoutRep("aaaaa") );
     	AssertJUnit.assertEquals( "", StringUtils.longestStringWithoutRep("") );
     }
 
 	
 	@Test
 	public void firstNonRepeatedChar(){
 		AssertJUnit.assertEquals( 's', StringUtils.firstNonRepeatedChar("tslmtmal") );
 	}
 	
 	
 	@Test
 	public void isAscciString(){
 		AssertJUnit.assertTrue( StringUtils.isAscciString("ascii string") );
 		AssertJUnit.assertFalse( StringUtils.isAscciString("ascii \u0100 string") );
 	}
 
 }
