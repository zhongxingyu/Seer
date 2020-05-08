 package com.bananity.util;
 
 // Main Class
import com.bananity.util.SearchSubstrings2;
 
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
		SearchSubstrings2 searchSubstrings_A = new SearchSubstrings2("");
 		Assert.assertEquals(0, searchSubstrings_A.getMaxTokenLength());
 
		SearchSubstrings2 searchSubstrings_B = new SearchSubstrings2("Bananity");
 		Assert.assertEquals(8, searchSubstrings_B.getMaxTokenLength());
 
		SearchSubstrings2 searchSubstrings_C = new SearchSubstrings2("The Banana Society");
 		Assert.assertEquals(14, searchSubstrings_C.getMaxTokenLength());
 	}
 
 	
 }
