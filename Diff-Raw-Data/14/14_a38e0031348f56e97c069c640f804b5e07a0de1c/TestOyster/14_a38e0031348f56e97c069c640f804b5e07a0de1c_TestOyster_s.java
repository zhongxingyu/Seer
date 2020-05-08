 package devo;
 
 import static devo.Oyster.LinkedLists.SNode.snodeOf;
 import static devo.Oyster.LinkedLists.DNode.dnodeOf;
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import static junit.framework.Assert.fail;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 
 import devo.Oyster.ARRAYS;
 import devo.Oyster.LinkedLists.DNode;
 import devo.Oyster.LinkedLists.SNode;
 import devo.Oyster.Recursions;
 import devo.Oyster.Sorting;
 import devo.Oyster.Stacks.MinStack;
 
 public class TestOyster {
     // to pull down other's changes from githut.com at terminal:
     //   cd /Users/henry/github/main; git pull; git status
     // to commit, and push up our local changes to github.com:
     //   git add .; git commit -m 'no comment'; git push; git status
     // See common git commands at https://github.com/0ishi/main
 
     // Shift-Command-B, or Run | Toggle Breakpoint
     // Command-F11, or Run | Debug
     // F5 (step into), F6 (step over), F7 (step out), F8 (resume)
     // Window | Open Perspective | Other ... | Debug
     // Window | Show View | Other ... | Problem
     // Control-Q to go to the last edit; Command-[, Command-]
     // Command-Shift-G to see usages of classes, fields, and methods.
     // Command-Shift-R to incremental-search sources, e.g. type 'oyster'
 
     @Test
     public void testRemoveDupeCharsUsingStringBuilder() {
        assertEquals(new char[] {'a'}, ARRAYS.removeDupCharsUsingStringBuilder(new char[] {'a'}));
        assertEquals(new char[] {'a', 'b', 'c', 'd'}, ARRAYS.removeDupCharsUsingStringBuilder(new char[] {'a', 'b', 'c', 'd'}));
        assertEquals(new char[] {'a' }, ARRAYS.removeDupCharsUsingStringBuilder(new char[] {'a', 'a', 'a', 'a'}));
        assertEquals(new char[] {'a', 'b'}, ARRAYS.removeDupCharsUsingStringBuilder(new char[] {'a', 'b', 'a', 'b', 'a', 'b'}));
        assertEquals(new char[] {'a', 'b'}, ARRAYS.removeDupCharsUsingStringBuilder(new char[] {'a', 'a', 'a', 'b', 'b', 'b'}));
        assertEquals(null, ARRAYS.removeDupCharsUsingStringBuilder(null));
        assertEquals(new char[] {}, ARRAYS.removeDupCharsUsingStringBuilder(new char[] {}));
     }
 
     @Test
     public void testFindSubArrayOfSumX() {
         // Input: x = 6, a = { 5, -1, 3, -2, 5, -3, 4, 2 }
         // Output: a[2] .. a[4] when you see 3 - 2 + 5 = 6.
         assertTrue(Arrays.equals(new int[] {0, 0}, ARRAYS.findSubArrayOfSumXInLinearTime(6, 6, -1, 3, -2, 5, -3, 4, 2)));
         assertTrue(Arrays.equals(new int[] {7, 7}, ARRAYS.findSubArrayOfSumXInLinearTime(6, 1, -1, 2, -2, 3, -3, 4, 6)));
         assertTrue(Arrays.equals(new int[] {2, 4}, ARRAYS.findSubArrayOfSumXInLinearTime(6, 5, -1, 3, -2, 5, -3, 4, 2)));
         assertTrue(Arrays.equals(new int[] {0, 7}, ARRAYS.findSubArrayOfSumXInLinearTime(8, 1, 1, 1, 1, 1, 1, 1, 1)));
         assertEquals(null, ARRAYS.findSubArrayOfSumXInLinearTime(6, 1, 11, 1, 11, 1, 11, 1, 1));
     }
 
     @Test
     public void testMinStack() {
         MinStack<Integer> stack = new MinStack<Integer>();
         stack.push(2).push(6).push(4).push(1).push(5).push(1);
         assertTrue(1 == stack.getMin());
         assertTrue(1 == stack.pop());
         assertTrue(1 == stack.getMin());
         assertTrue(5 == stack.pop());
         assertTrue(1 == stack.getMin());
         assertTrue(1 == stack.pop());
         assertTrue(2 == stack.getMin());
         assertTrue(4 == stack.pop());
         assertTrue(2 == stack.getMin());
         assertTrue(6 == stack.pop());
         assertTrue(2 == stack.getMin());
         assertTrue(2 == stack.pop());
         assertTrue(null == stack.getMin());
     }
 
     @Test
     public void testSumDigitNodes() {
         SNode lhs = snodeOf(3, snodeOf(4, snodeOf(5)));
         SNode rhs = snodeOf(6, snodeOf(7, snodeOf(8)));
         SNode sum = SNode.sumDigitNodes(lhs, rhs);
         assertEquals(9, sum.data);
         assertEquals(1, sum.next.data);
         assertEquals(4, sum.next.next.data);
         assertEquals(1, sum.next.next.next.data);
         assertEquals(null, sum.next.next.next.next);
     }
 
     @Test
     public void testAnagram() {
         assertTrue(Oyster.ARRAYS.anagrams("aab", "aba"));
     }
 
     @Test
     public void testDNodeDelete() {
         // Input: null -> 1 -> 1 -> 2 -> 1 -> 1 -> null
         // Output: null -> 2 -> null
 
         DNode input = dnodeOf(1);
         input.next = dnodeOf(1, input);
         input.next.next = dnodeOf(2, input.next);
         input.next.next.next = dnodeOf(1, input.next.next);
         input.next.next.next.next = dnodeOf(1, input.next.next.next);
 
         DNode output = DNode.deleteAll(input, 1);
         assertEquals(2, output.data);
         assertEquals(null, output.next);
         assertEquals(null, output.prev);
     }
 
     @Test
     public void testSNodeInsert() {
         SNode head = SNode.insert(null, 2); // to be the one and the only node.
         assertEquals(2, head.data);
         assertEquals(null, head.next);
 
         SNode head2 = SNode.insert(head, 4); // to be a tail.
         assertEquals(2, head2.data);
         assertEquals(4, head2.next.data);
         assertEquals(null, head2.next.next);
 
         SNode head3 = SNode.insert(head2, 3); // to be a middle man.
         assertEquals(2, head3.data);
         assertEquals(3, head3.next.data);
         assertEquals(4, head3.next.next.data);
         assertEquals(null, head3.next.next.next);
 
         SNode head4 = SNode.insert(head2, 1); // to be a head.
         assertEquals(1, head4.data);
         assertEquals(2, head4.next.data);
         assertEquals(3, head4.next.next.data);
         assertEquals(4, head4.next.next.next.data);
         assertEquals(null, head4.next.next.next.next);
     }
 
     @Test
     public void testLinkedListCRUD() { // CRUD: http://en.wikipedia.org/wiki/Create,_read,_update_and_delete
         SNode reverse = SNode.reverse(snodeOf(4, snodeOf(3, snodeOf(2, snodeOf(1, null)))));
         assertEquals(1, reverse.data);
         assertEquals(2, reverse.next.data);
         assertEquals(3, reverse.next.next.data);
         assertEquals(4, reverse.next.next.next.data);
         assertEquals(null, reverse.next.next.next.next);
 
         SNode head1 = SNode.deleteOne(reverse, 2);
         assertEquals(1, head1.data);
         assertEquals(3, head1.next.data);
         assertEquals(4, head1.next.next.data);
         assertEquals(null, head1.next.next.next);
 
         SNode head3 = SNode.deleteOne(reverse, 1);
         assertEquals(3, head3.data);
         assertEquals(4, head3.next.data);
         assertEquals(null, head3.next.next);
 
         SNode.deleteInConstantTime(head3);
         assertEquals(4, head3.data);
         assertEquals(null, head3.next);
     }
 
     @Test
     public void testRemoveDupsInConstantSpace() {
         SNode head2 = SNode.removeDupsInConstantSpace(
             snodeOf(1, snodeOf(2, snodeOf(2, snodeOf(3, snodeOf(3, snodeOf(3, null)))))));
 
         assertEquals(1, head2.data);
         assertEquals(2, head2.next.data);
         assertEquals(3, head2.next.next.data);
         assertEquals(null, head2.next.next.next);
     }
 
     @Test
     public void testRemoveDupsInLinearTime() {
         SNode head2 = SNode.removeDupsInLinearTime(
             snodeOf(1, snodeOf(2, snodeOf(2, snodeOf(3, snodeOf(3, snodeOf(3, null)))))));
 
         assertEquals(1, head2.data);
         assertEquals(2, head2.next.data);
         assertEquals(3, head2.next.next.data);
         assertEquals(null, head2.next.next.next);
     }
 
     @Test
     public void testDNodeInsert() {
 
         DNode head = DNode.insertIntoSorted(null, DNode.dnodeOf(2)); // to be the one and the only node.
         assertEquals(2, head.data);
         assertEquals(null, head.next);
 
         DNode head2 = DNode.insertIntoSorted(head, DNode.dnodeOf(4)); // to be a tail.
         assertEquals(2, head2.data);
         assertEquals(4, head2.next.data);
         assertEquals(2, head2.next.prev.data);
         assertEquals(null, head2.next.next);
 
         DNode head3 = DNode.insertIntoSorted(head2, DNode.dnodeOf(3)); // to be a middle man.
         assertEquals(2, head3.data);
         assertEquals(3, head3.next.data);
         assertEquals(4, head3.next.next.data);
         assertEquals(3, head3.next.next.prev.data);
         assertEquals(null, head3.next.next.next);
 
         DNode head4 = DNode.insertIntoSorted(head2, DNode.dnodeOf(1)); // to be a head.
         assertEquals(1, head4.data);
         assertEquals(2, head4.next.data);
         assertEquals(1, head4.next.prev.data);
         assertEquals(3, head4.next.next.data);
         assertEquals(4, head4.next.next.next.data);
         assertEquals(null, head4.next.next.next.next);
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
                 return Oyster.ARRAYS.findModesUsingMap(input);
             };
         });
     }
 
     @Test
     public void testFindModesUsingArray() {
         testFindModes(new Function<int[], List<Integer>>() {
             public List<Integer> apply(int[] input) {
                 return Oyster.ARRAYS.findModesUsingArray(input);
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
         assertEquals(4, Sorting.indexOutOfCycle(30, new int[] { 90, 100, 10, 20, 30, 40, 50, 60, 70, 80 }));
         assertEquals(3, Sorting.indexOutOfCycle(20, new int[] { 90, 100, 10, 20, 30, 40, 50, 60, 70, 80 }));
         assertEquals(0, Sorting.indexOutOfCycle(90, new int[] { 90, 100, 10, 20, 30, 40, 50, 60, 70, 80 }));
         assertEquals(-1, Sorting.indexOutOfCycle(95, new int[] { 90, 100, 10, 20, 30, 40, 50, 60, 70, 80 }));
         assertEquals(5, Sorting.indexOutOfCycle(40, new int[] { 90, 100, 10, 20, 30, 40, 50, 60, 70, 80 }));
         assertEquals(9, Sorting.indexOutOfCycle(80, new int[] { 90, 100, 10, 20, 30, 40, 50, 60, 70, 80 }));
         assertEquals(8, Sorting.indexOutOfCycle(70, new int[] { 90, 100, 10, 20, 30, 40, 50, 60, 70, 80 }));
         assertEquals(-1, Sorting.indexOutOfCycle(75, new int[] { 90, 100, 10, 20, 30, 40, 50, 60, 70, 80 }));
 
         assertEquals(4, Sorting.indexOutOfCycle(70, new int[] { 30, 40, 50, 60, 70, 80, 90, 100, 10, 20 }));
         assertEquals(0, Sorting.indexOutOfCycle(30, new int[] { 30, 40, 50, 60, 70, 80, 90, 100, 10, 20 }));
         assertEquals(3, Sorting.indexOutOfCycle(60, new int[] { 30, 40, 50, 60, 70, 80, 90, 100, 10, 20 }));
         assertEquals(-1, Sorting.indexOutOfCycle(55, new int[] { 30, 40, 50, 60, 70, 80, 90, 100, 10, 20 }));
         assertEquals(5, Sorting.indexOutOfCycle(80, new int[] { 30, 40, 50, 60, 70, 80, 90, 100, 10, 20 }));
         assertEquals(9, Sorting.indexOutOfCycle(20, new int[] { 30, 40, 50, 60, 70, 80, 90, 100, 10, 20 }));
         assertEquals(8, Sorting.indexOutOfCycle(10, new int[] { 30, 40, 50, 60, 70, 80, 90, 100, 10, 20 }));
         assertEquals(-1, Sorting.indexOutOfCycle(15, new int[] { 30, 40, 50, 60, 70, 80, 90, 100, 10, 20 }));
     }
 
     @Test
     public void testFindSmallestOutOfCycle() {
         try {
             Sorting.smallestOutOfCycle(null);
             fail("'numbers' must be non-null.");
         } catch (IllegalArgumentException e) {
         }
 
         try {
             Sorting.smallestOutOfCycle(new int[0]);
             fail("'numbers' must not be empty.");
         } catch (IllegalArgumentException e) {
         }
 
         assertEquals(6, Sorting.smallestOutOfCycle(new int[] { 6 }));
         assertEquals(6, Sorting.smallestOutOfCycle(new int[] { 6, 7 }));
         assertEquals(6, Sorting.smallestOutOfCycle(new int[] { 7, 6 }));
         assertEquals(6, Sorting.smallestOutOfCycle(new int[] { 38, 40, 55, 89, 6, 13, 20, 23, 36 }));
         assertEquals(6, Sorting.smallestOutOfCycle(new int[] { 6, 13, 20, 23, 36, 38, 40, 55, 89 }));
         assertEquals(6, Sorting.smallestOutOfCycle(new int[] { 13, 20, 23, 36, 38, 40, 55, 89, 6 }));
     }
 
     @Test
     public void testToAndFromExcelColumn() {
         assertEquals("AB", Recursions.toExcelColumn(28));
         assertEquals("ABC", Recursions.toExcelColumn(731));
         assertEquals(28, Recursions.fromExcelColumn(Recursions.toExcelColumn(28)));
         assertEquals(731, Recursions.fromExcelColumn(Recursions.toExcelColumn(731)));
     }
 }
