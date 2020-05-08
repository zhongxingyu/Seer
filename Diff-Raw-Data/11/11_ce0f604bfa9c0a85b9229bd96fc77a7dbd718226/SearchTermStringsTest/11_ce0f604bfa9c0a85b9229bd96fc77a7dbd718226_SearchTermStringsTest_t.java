 package com.bananity.util;
 
 // Main Class
import com.bananity.util.SearchTermStrings;
 
 // Junit
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.Ignore;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 
 /**
  * @author Andreu Correa Casablanca
  */
 @RunWith(JUnit4.class)
 public class SearchTermStringsTest
 {
 	@Test
 	public void test_getMaxTokenLength () {
		SearchTermStrings searchSubstrings_A = new SearchTermStrings("");
 		Assert.assertEquals(0, searchSubstrings_A.getMaxTokenLength());
 
		SearchTermStrings searchSubstrings_B = new SearchTermStrings("Bananity");
 		Assert.assertEquals(8, searchSubstrings_B.getMaxTokenLength());
 
		SearchTermStrings searchSubstrings_C = new SearchTermStrings("The Banana Society");
 		Assert.assertEquals(14, searchSubstrings_C.getMaxTokenLength());
 	}
 
 	
 }
