 package com.mattdrees.dropboxcalories;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 
 import java.util.Set;
 
 import org.testng.annotations.Test;
 
 import com.google.common.collect.ImmutableSet;
 
 public class BitSetSortedIntegerSetTest
 {
     
     @Test
     public void testEqualitySmall()
     {
         BitSetSortedIntegerSet set1 = new BitSetSortedIntegerSet(100);
         set1.add(6);
         set1.add(8);
         BitSetSortedIntegerSet set2 = new BitSetSortedIntegerSet(100);
         set2.add(8);
         set2.add(6);
         
         assertThat(set1, equalTo(set2));
     }
     
     @Test
     public void testEqualityAgainstOtherSetImpl()
     {
         Set<Integer> bitSet = new BitSetSortedIntegerSet(100);
         bitSet.add(6);
         bitSet.add(8);
         Set<Integer> immutableSet = ImmutableSet.of(6, 8);
         
         assertThat(bitSet, equalTo(immutableSet));
         assertThat(immutableSet, equalTo(bitSet));
     }
     
     @Test
     public void testAdd()
     {
         BitSetSortedIntegerSet set1 = new BitSetSortedIntegerSet(100);
         set1.add(6);
         assertThat(set1.size(), equalTo(1));
         assertThat(set1.contains(6), equalTo(true));
     }
     
     @Test
    public void testAddTwice()
    {
        BitSetSortedIntegerSet set1 = new BitSetSortedIntegerSet(100);
        set1.add(6);
        set1.add(6);
        assertThat(set1.size(), equalTo(1));
        assertThat(set1.contains(6), equalTo(true));
    }
    
    @Test
     public void testRemove()
     {
         BitSetSortedIntegerSet set1 = new BitSetSortedIntegerSet(100);
         set1.add(6);
         set1.remove(6);
         assertThat(set1.contains(6), equalTo(false));
         assertThat(set1.isEmpty(), equalTo(true));
     }
     
     @Test
     public void testToString()
     {
         BitSetSortedIntegerSet set1 = new BitSetSortedIntegerSet(100);
         set1.add(6);
         set1.add(9);
         assertThat(set1.toString(), equalTo("[6, 9]"));
     }
 
 }
