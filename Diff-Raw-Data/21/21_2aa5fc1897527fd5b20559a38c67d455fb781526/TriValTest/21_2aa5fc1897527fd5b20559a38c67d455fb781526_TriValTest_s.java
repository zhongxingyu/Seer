 package test.com.wealdtech;
 
 import org.testng.annotations.Test;
 
 import com.google.common.collect.ComparisonChain;
 import com.google.common.collect.ImmutableSet;
 import com.wealdtech.TriVal;
 import com.wealdtech.utils.TriValOrdering;
 
import static org.testng.Assert.*;

 public class TriValTest
 {
   @Test
   public void testComparisonChain() throws Exception
   {
     // Tests with Guava's comparisonchain
     final TriVal<String> triAbsent = TriVal.<String>absent();
     final TriVal<String> triClear = TriVal.<String>clear();
     final TriVal<String> triTest1 = TriVal.<String>of("Test 1");
     final TriVal<String> triTest2 = TriVal.<String>of("Test 2");
     final TriVal<String> triTest1Again = TriVal.<String>of("Test 1");
 
     assertEquals(ComparisonChain.start().compare(triAbsent, triAbsent, TriValOrdering.INSTANCE).result(), 0);
     assertEquals(ComparisonChain.start().compare(triAbsent, triClear, TriValOrdering.INSTANCE).result(), -1);
     assertEquals(ComparisonChain.start().compare(triAbsent, triTest1, TriValOrdering.INSTANCE).result(), -1);
     assertEquals(ComparisonChain.start().compare(triClear, triAbsent, TriValOrdering.INSTANCE).result(), 1);
     assertEquals(ComparisonChain.start().compare(triClear, triClear, TriValOrdering.INSTANCE).result(), 0);
     assertEquals(ComparisonChain.start().compare(triClear, triTest1, TriValOrdering.INSTANCE).result(), -1);
     assertEquals(ComparisonChain.start().compare(triTest1, triAbsent, TriValOrdering.INSTANCE).result(), 1);
     assertEquals(ComparisonChain.start().compare(triTest1, triClear, TriValOrdering.INSTANCE).result(), 1);
     assertEquals(ComparisonChain.start().compare(triTest1, triTest1, TriValOrdering.INSTANCE).result(), 0);
     assertEquals(ComparisonChain.start().compare(triTest1, triTest1Again, TriValOrdering.INSTANCE).result(), 0);
    assertEquals(ComparisonChain.start().compare(triTest2, triTest1Again, TriValOrdering.INSTANCE).result(), 0);
   }
 
   @Test
   public void testComparisonChainComplex() throws Exception
   {
     final TriVal<ImmutableSet<String>> triAbsent = TriVal.absent();
     final TriVal<ImmutableSet<String>> triClear = TriVal.clear();
     final TriVal<ImmutableSet<String>> triTest1 = TriVal.of(ImmutableSet.of("T1", "T2", "T3"));
     final TriVal<ImmutableSet<String>> triTest2 = TriVal.of(ImmutableSet.of("T2", "T3", "T4"));
     final TriVal<ImmutableSet<String>> triTest1Again = TriVal.of(ImmutableSet.of("T1", "T2", "T3"));
 
     assertEquals(ComparisonChain.start().compare(triAbsent, triAbsent, TriValOrdering.INSTANCE).result(), 0);
     assertEquals(ComparisonChain.start().compare(triAbsent, triClear, TriValOrdering.INSTANCE).result(), -1);
     assertEquals(ComparisonChain.start().compare(triAbsent, triTest1, TriValOrdering.INSTANCE).result(), -1);
     assertEquals(ComparisonChain.start().compare(triClear, triAbsent, TriValOrdering.INSTANCE).result(), 1);
     assertEquals(ComparisonChain.start().compare(triClear, triClear, TriValOrdering.INSTANCE).result(), 0);
     assertEquals(ComparisonChain.start().compare(triClear, triTest1, TriValOrdering.INSTANCE).result(), -1);
     assertEquals(ComparisonChain.start().compare(triTest1, triAbsent, TriValOrdering.INSTANCE).result(), 1);
     assertEquals(ComparisonChain.start().compare(triTest1, triClear, TriValOrdering.INSTANCE).result(), 1);
     assertEquals(ComparisonChain.start().compare(triTest1, triTest1, TriValOrdering.INSTANCE).result(), 0);
     assertEquals(ComparisonChain.start().compare(triTest1, triTest1Again, TriValOrdering.INSTANCE).result(), 0);
    assertEquals(ComparisonChain.start().compare(triTest2, triTest1Again, TriValOrdering.INSTANCE).result(), 0);
   }
 }
