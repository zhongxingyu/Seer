 package com.igordaoconsulting.training;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.junit.Assert;
 import org.junit.Test;
 
import com.igordaoconsulting.training.StringUtils;
 
 public class StringPermutationTest {
 
 	@Test (expected=IllegalArgumentException.class)
 	public void testInvalidArgument() {
 		StringUtils.allPermutations(null);
 		StringUtils.allPermutations("");
 	}
 
 	@Test
 	public void testAllPermutations() {
 		assertEquals(Arrays.asList("abc", "acb", "bac", "bca", "cab", "cba"), StringUtils.allPermutations("abc"));
 
 		assertEquals(
 			Arrays.asList(
 				"abcd", "abdc", "acbd", "acdb", "adbc", "adcb",
 				"bacd", "badc", "bcad", "bcda", "bdac", "bdca",
 				"cabd", "cadb", "cbad", "cbda", "cdab", "cdba",
 				"dabc", "dacb", "dbac", "dbca", "dcab", "dcba"
 			), StringUtils.allPermutations("abcd")
 		);
 	}
 
 	private void assertEquals(Collection<String> expected, Collection<String> actual) {
 		Assert.assertTrue(CollectionUtils.isEqualCollection(expected, actual));
 	}
 
 }
