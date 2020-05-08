 package org.wwscc.storage;
 
import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 @SuppressWarnings("unchecked")
 public class ClassListTest {
 
 	static Set<ClassData.Index> allindexes;
 	
 	private static Set<ClassData.Index> buildSet(String[] codes)
 	{
 		Set<ClassData.Index> ret = new HashSet<ClassData.Index>();
 		for (String s : codes) {
 			ClassData.Index i = new ClassData.Index();
 			i.code = s;
 			ret.add(i);
 		}
 		return ret;
 	}
 	
 	@BeforeClass
 	public static void setup()
 	{
 		allindexes = buildSet(new String[] { "SA", "SB", "STU", "STX", "MA", "MB", "AS" });
 	}
 	
 	@Test
 	public void testRestrict() {
 		ClassData.Class cls = new ClassData.Class();
 		cls.caridxrestrict = "+(S*,A*)-(ST*)-[A*]";
 		Set<ClassData.Index>[] ret = cls.restrictedIndexes(allindexes);
 
 		Assert.assertEquals(buildSet(new String[] { "SA", "SB", "AS" }), ret[0]);
 		Assert.assertEquals(buildSet(new String[] { "SA", "SB", "STU", "STX", "MA", "MB" }), ret[1]);
 	}
 
 	
 	@Test
 	public void testRestrictBlank() {
 		ClassData.Class cls = new ClassData.Class();
 		cls.caridxrestrict = "";
 		Set<String>[] ret = cls.restrictedIndexes(allindexes);
 
 		Assert.assertEquals(allindexes, ret[0]);
 		Assert.assertEquals(allindexes, ret[1]);
 	}
 }
