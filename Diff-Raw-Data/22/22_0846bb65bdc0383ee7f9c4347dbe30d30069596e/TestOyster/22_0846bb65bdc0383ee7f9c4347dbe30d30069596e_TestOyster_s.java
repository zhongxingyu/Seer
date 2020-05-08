 package devo;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.fail;
 import static devo.Oyster.LinkedLists.SNode.of;
 
 import devo.Oyster.LinkedLists.SNode;
 import java.util.List;
 
 import org.junit.Test;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 
 public class TestOyster {
     // Shift-Command-B, or Run | Toggle Breakpoint
     // Command-F11, or Run | Debug
     // F5 (step into), F6 (step over), F7 (step out), F8 (resume)
     // Window | Open Perspective | Other ... | Debug
     // Window | Show View | Other ... | Problem
     // Control-Q to go to the last edit; Command-[, Command-]
     @Test
     public void testSNodeInsert() {
         Oyster.LinkedLists.SNode head = Oyster.LinkedLists.SNode.insert(null, 2); // to be the one and the only node.
         assertEquals(2, head.data);
         assertEquals(null, head.next);
 
         Oyster.LinkedLists.SNode head2 = Oyster.LinkedLists.SNode.insert(head, 4); // to be a tail.
         assertEquals(2, head2.data);
         assertEquals(4, head2.next.data);
         assertEquals(null, head2.next.next);
 
         Oyster.LinkedLists.SNode head3 = Oyster.LinkedLists.SNode.insert(head2, 3); // to be a middle man.
         assertEquals(2, head3.data);
         assertEquals(3, head3.next.data);
         assertEquals(4, head3.next.next.data);
         assertEquals(null, head3.next.next.next);
 
         Oyster.LinkedLists.SNode head4 = Oyster.LinkedLists.SNode.insert(head2, 1); // to be a head.
         assertEquals(1, head4.data);
         assertEquals(2, head4.next.data);
         assertEquals(3, head4.next.next.data);
         assertEquals(4, head4.next.next.next.data);
         assertEquals(null, head4.next.next.next.next);
     }
 
     @Test
     public void testRemoveDupsInConstantSpace() {
         SNode head1 = Oyster.LinkedLists.SNode.removeDupsInConstantSpace(of(1, of(2, of(2, null))));
         assertEquals(1, head1.data);
         assertEquals(2, head1.next.data);
         assertEquals(null, head1.next.next);
     }
 
     /*
      * Positive test cases: 
      * - {1} yields {1}.
      * - {1, 2, 2} yields {2}.
      * - {1, 2, 2, 3, 3, 3} yields {3}.
      * - {2, 2} yields {2}. 
      * - {1, 1, 2, 2} yields {1, 2}.
      * Negative test cases: 
      * - null throws NPE.
      * - empty yields empty.
      */
     @Test
     public void testFindModesUsingMap() {
         testFindModes(new Function<int[], List<Integer>>() {
             public List<Integer> apply(int[] input) {
                 return Oyster.Arrays.findModesUsingMap(input);
             };
         });
     }
 
     @Test
     public void testFindModesUsingArray() {
         testFindModes(new Function<int[], List<Integer>>() {
             public List<Integer> apply(int[] input) {
                 return Oyster.Arrays.findModesUsingArray(input);
             };
         });
     }
 
     private void testFindModes(Function<int[], List<Integer>> findModes) {
         try {
             findModes.apply(null);
             fail("findsModes should have thrown NPE.");
         } catch (AssertionError ae) {
         } catch (NullPointerException npe) {
         } catch (Exception e) {
             fail("findsModes should have thrown NPE.");
         }
 
         assertEquals(ImmutableList.of(), findModes.apply(new int[0]));
 
         assertEquals(ImmutableList.of(1), findModes.apply(new int[] { 1 }));
         assertEquals(ImmutableList.of(2), findModes.apply(new int[] { 1, 2, 2 }));
         assertEquals(ImmutableList.of(3), findModes.apply(new int[] { 1, 2, 2, 3, 3, 3 }));
 
         assertEquals(ImmutableList.of(2), findModes.apply(new int[] { 2, 2 }));
         assertEquals(ImmutableList.of(1, 2), findModes.apply(new int[] { 1, 1, 2, 2 }));
     }
 
     @Test
     public void testIndexOutOfCycle() {
         assertEquals(4, Oyster.Sorting.indexOutOfCycle(30, new int[] { 90, 100, 10, 20, 30, 40, 50, 60, 70, 80 }));
         assertEquals(3, Oyster.Sorting.indexOutOfCycle(20, new int[] { 90, 100, 10, 20, 30, 40, 50, 60, 70, 80 }));
         assertEquals(0, Oyster.Sorting.indexOutOfCycle(90, new int[] { 90, 100, 10, 20, 30, 40, 50, 60, 70, 80 }));
         assertEquals(-1, Oyster.Sorting.indexOutOfCycle(95, new int[] { 90, 100, 10, 20, 30, 40, 50, 60, 70, 80 }));
         assertEquals(5, Oyster.Sorting.indexOutOfCycle(40, new int[] { 90, 100, 10, 20, 30, 40, 50, 60, 70, 80 }));
         assertEquals(9, Oyster.Sorting.indexOutOfCycle(80, new int[] { 90, 100, 10, 20, 30, 40, 50, 60, 70, 80 }));
         assertEquals(8, Oyster.Sorting.indexOutOfCycle(70, new int[] { 90, 100, 10, 20, 30, 40, 50, 60, 70, 80 }));
         assertEquals(-1, Oyster.Sorting.indexOutOfCycle(75, new int[] { 90, 100, 10, 20, 30, 40, 50, 60, 70, 80 }));
 
         assertEquals(4, Oyster.Sorting.indexOutOfCycle(70, new int[] { 30, 40, 50, 60, 70, 80, 90, 100, 10, 20 }));
         assertEquals(0, Oyster.Sorting.indexOutOfCycle(30, new int[] { 30, 40, 50, 60, 70, 80, 90, 100, 10, 20 }));
         assertEquals(3, Oyster.Sorting.indexOutOfCycle(60, new int[] { 30, 40, 50, 60, 70, 80, 90, 100, 10, 20 }));
         assertEquals(-1, Oyster.Sorting.indexOutOfCycle(55, new int[] { 30, 40, 50, 60, 70, 80, 90, 100, 10, 20 }));
         assertEquals(5, Oyster.Sorting.indexOutOfCycle(80, new int[] { 30, 40, 50, 60, 70, 80, 90, 100, 10, 20 }));
         assertEquals(9, Oyster.Sorting.indexOutOfCycle(20, new int[] { 30, 40, 50, 60, 70, 80, 90, 100, 10, 20 }));
         assertEquals(8, Oyster.Sorting.indexOutOfCycle(10, new int[] { 30, 40, 50, 60, 70, 80, 90, 100, 10, 20 }));
         assertEquals(-1, Oyster.Sorting.indexOutOfCycle(15, new int[] { 30, 40, 50, 60, 70, 80, 90, 100, 10, 20 }));
     }
 
     @Test
     public void testFindSmallestOutOfCycle() {
         try {
             Oyster.Sorting.smallestOutOfCycle(null);
             fail("'numbers' must be non-null.");
         } catch (IllegalArgumentException e) {
         }
 
         try {
             Oyster.Sorting.smallestOutOfCycle(new int[0]);
             fail("'numbers' must not be empty.");
         } catch (IllegalArgumentException e) {
         }
 
         assertEquals(6, Oyster.Sorting.smallestOutOfCycle(new int[] { 6 }));
         assertEquals(6, Oyster.Sorting.smallestOutOfCycle(new int[] { 6, 7 }));
         assertEquals(6, Oyster.Sorting.smallestOutOfCycle(new int[] { 7, 6 }));
         assertEquals(6, Oyster.Sorting.smallestOutOfCycle(new int[] { 38, 40, 55, 89, 6, 13, 20, 23, 36 }));
         assertEquals(6, Oyster.Sorting.smallestOutOfCycle(new int[] { 6, 13, 20, 23, 36, 38, 40, 55, 89 }));
         assertEquals(6, Oyster.Sorting.smallestOutOfCycle(new int[] { 13, 20, 23, 36, 38, 40, 55, 89, 6 }));
     }
 
     @Test
     public void testToAndFromExcelColumn() {
         assertEquals("AB", Oyster.Recursions.toExcelColumn(28));
         assertEquals("ABC", Oyster.Recursions.toExcelColumn(731));
         assertEquals(28, Oyster.Recursions.fromExcelColumn(Oyster.Recursions.toExcelColumn(28)));
         assertEquals(731, Oyster.Recursions.fromExcelColumn(Oyster.Recursions.toExcelColumn(731)));
     }
 }
