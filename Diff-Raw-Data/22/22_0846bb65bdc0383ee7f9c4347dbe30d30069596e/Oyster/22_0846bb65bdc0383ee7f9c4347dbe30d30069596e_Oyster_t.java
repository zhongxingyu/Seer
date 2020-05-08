 package devo;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Random;
 
 import com.google.common.collect.ImmutableList;
 
 // Heapsort competes with quicksort, another very efficient nearly-in-place sort algorithm
 // Quicksort is typically somewhat faster, due to better cache behavior and other factors.
 // The worst-case running time for quicksort is makes it unacceptable for large data sets.
 // Heapsort relies strongly on random access, and its poor locality of reference requires
 // efficient random access to be practical.
 // Mergesort is a stable sort, unlike quicksort and heapsort, and can be easily adapted
 // to operate on linked lists and very large set on slow media with long access time.
 // Heapsort requires only @(1) auxiliary space instead of Mergesort @(n) auxiliary space,
 // quicksort with in-place partitioning and tail recursion uses only O(log n) space.
 
 public class Oyster {
     public static class Arrays {
         // 1.1. Implement an algorithm to determine if a string has all unique characters.
         //      What if you can not use additional data structures?
 
         // 1.2. Write code to reverse a C-Style String.
         //      (C-String means that abcd is represented as five characters, including the null character.)
 
         // 1.3. Design an algorithm and write code to remove the duplicate characters in a string without using any additional buffer.
         //      NOTE: One or two additional variables are fine. An extra copy of the array is not.
         //      FOLLOW UP: Write the test cases for this method.
 
         // 1.4. Write a method to decide if two strings are anagrams or not.
 
         // 1.5. Write a method to replace all spaces in a string with '%20'.
 
         // 1.6. Given an image represented by an NxN matrix, where each pixel in the image is 4 bytes, 
         //      write a method to rotate the image by 90 degrees. Can you do this in place?
 
         // 1.7. Write an algorithm such that if an element in an MxN matrix is 0, its entire row and column is set to 0.
 
         // 1.8. Assume you have a method isSubstring which checks if one word is a substring of another.
         //      Given two strings, s1 and s2, write code to check if s2 is a rotation of s1 using only one call to isSubstring
         //      (i.e., "waterbottle" is a rotation of "erbottlewat").
 
         // 1-A. Find the max sum sub array.
         //      Kadane's algorithm http://en.wikipedia.org/wiki/Maximum_subarray_problem
         public static int[] findMaxSumSubArrayUsingKadane(int... a) {
             int maxHead = 0, maxTail = 0, maxSum = a[0];
             for (int head = 0, tail = 1, sum = a[0]; tail < a.length; tail++) {
                 sum = sum + a[tail];
                 if (sum > maxSum) {
                     maxHead = head;
                     maxTail = tail;
                     maxSum = sum;
                 }
 
                 if (0 > sum) {
                     head = tail + 1;
                     sum = 0;
                 }
             }
 
             return new int[] { maxSum, maxHead, maxTail };
         }
 
         // 1-B. Find the modes using a map in Java (also called Dictionary in C#).
         public static List<Integer> findModesUsingMap(int... numbers) {
             // We assert preconditions; arguments according to assumptions (for debug builds).
             assert null != numbers;
 
             // We validate arguments if this is a public interface (for debug and retail builds).
             if (null == numbers) {
                 throw new NullPointerException("'numbers' must be non-null.");
             }
 
             // We short-circuit known cases such as an empty set of numbers.
             if (0 == numbers.length) {
                 return ImmutableList.of();
             }
 
             int maximumHits = 1;
             List<Integer> modes = new ArrayList<Integer>();
             Map<Integer, Integer> hitsByNumber = new HashMap<Integer, Integer>();
             for (int number : numbers) {
                 if (hitsByNumber.containsKey(number)) {
                     hitsByNumber.put(number, 1 + hitsByNumber.get(number));
                 } else {
                     hitsByNumber.put(number, 1);
                 }
 
                 if (hitsByNumber.get(number) >= maximumHits) {
                     if (hitsByNumber.get(number) > maximumHits) {
                         maximumHits = hitsByNumber.get(number);
                         modes.clear();
                     }
 
                     modes.add(number);
                 }
             }
 
             // We assert postconditions.
             assert !modes.isEmpty();
             return modes;
         }
 
         // 1-C. Find the modes using a map in Array (also called Dictionary in C#).
         public static List<Integer> findModesUsingArray(int... numbers) {
             // We assert preconditions; arguments according to assumptions (for debug builds).
             assert null != numbers;
 
             // We validate arguments if this is a public interface (for debug and retail builds).
             if (null == numbers) {
                 throw new NullPointerException("'numbers' must be non-null.");
             }
 
             // We short-circuit known cases such as an empty set of numbers.
             if (0 == numbers.length) {
                 return ImmutableList.of();
             }
 
             int min = numbers[0];
             int max = numbers[0];
             for (int i = 1; i < numbers.length; i++) {
                 min = Math.min(min, numbers[i]); // 1, 2, 2, 3, 3, 3
                 max = Math.max(max, numbers[i]);
             }
 
             int maximumHits = 1;
             List<Integer> modes = new ArrayList<Integer>();
 
             int[] hitsByNumber = new int[max - min + 1]; // 11, 10..20 -> 10 rooms
             for (int number : numbers) {
                 hitsByNumber[number - min]++; // {0, 0, ... } => {0, 1, ... }
                 if (hitsByNumber[number - min] >= maximumHits) {
                     if (hitsByNumber[number - min] > maximumHits) {
                         modes.clear();
                         maximumHits = hitsByNumber[number - min];
                     }
 
                     modes.add(number);
                 }
             }
 
             return modes;
         }
     }
 
     public static class LinkedLists {
         public static class SNode implements Comparable<SNode> {
             public int data;
             public SNode next;
 
             public SNode() {
             }
 
             public SNode(int data) {
                 this.data = data;
                 this.next = null; // this line seems unnecessary, as the uninitialized fields remain to be null.
             }
 
             public static SNode of(int data) {
                 SNode snode = new SNode();
                 snode.data = data;
                 return snode;
             }
 
             public static SNode of(int data, SNode next) {
                 SNode snode = new SNode();
                 snode.data = data;
                 snode.next = next;
                 return snode;
             }
 
             // This method inserts an data element into a sorted linked list, and returns the new head node.
             public static SNode insert(SNode head, int insert) {
                 return insert(head, SNode.of(insert));
             }
 
             private static SNode insert(SNode head, SNode insert) {
                 // how-to assert in Java http://download.oracle.com/javase/1.4.2/docs/guide/lang/assert.html
                 assert null != insert : insert;
 
                 if (head == null) {
                     return insert;
                 }
 
                 if (head.data > insert.data) {
                     insert.next = head;
                     return insert;
                 }
 
                 for (SNode previous = head; ; previous = previous.next) {
                     if (previous.next == null || previous.next.data >= insert.data) { // repeat until ...
                         insert.next = previous.next;
                         previous.next = insert;
                         break;
                     }
                 }
 
                 // Henry's proposal for fix as follows:
 //                SNode previous = head;
 //                for (; previous.next != null && previous.next.data < insert.data; previous = previous.next) {
 //                }
 //
 //                insert.next = previous.next;
 //                previous.next = insert;
 
                 return head;
             }
 
             // This method deletes an data element into a sorted linked list, and returns the new head node.
             public static SNode delete(SNode head, int delete){
                 if (head == null) return head;
                 if (head.data == delete) return head.next;
 
                 for (SNode current = head; current.next != null; current = current.next){
                     if (current.next.data == delete) {
                         current.next = current.next.next;
                         break;
                     }
                 }
 
                 return head;
             }
 
             // This method deletes a node in a constant time, i.e. time O(1), space: O(1)
             public static void delete(SNode delete) {
                 if (delete.next != null) {
                     delete.data = delete.next.data;
                     delete.next = delete.next.next;
                 } else {
                     delete.data = 0; // mark it deleted and collect garbage later on
                 }
             }
 
             // This method reverses a linked list in an iteration.
             public static <T extends Comparable<T>> SNode reverse(SNode current) {
                 SNode result = null;
                 while (current != null) {
                     SNode save = current.next;
                     current.next = result;
                     result = current;
                     current = save;
                 }
 
                 return result;
             }
 
             // 2.1. Write code to remove duplicates from an unsorted linked list. 
             //      FOLLOW UP: How would you solve this problem if a temporary buffer is not allowed?
             public static SNode removeDupsInConstantSpace(SNode head) {
                 for (SNode remaining = head; remaining != null; remaining = remaining.next) {
                     SNode previous = remaining;
                     SNode current = remaining.next;
 
                    for (; null != current; current = current.next) {
                         if (remaining.data == current.data) {
                            previous.next = current.next; // previous remains to be the same.
                        } else {
                            previous = current;
                         }
                     }
                 }
 
                 return head;
             }
 
             // This method removes duplicates using a hash map.
             public static void removeDupsUsingHashMap(SNode current) {
                 // HashSet may be a better fit, but Hashtable is an old school
                 Map<Integer, Object> map = new HashMap<Integer, Object>();
                 SNode previous = null;
                 while (current != null) {
                     if (map.containsKey(current.data))
                         previous.next = current.next;
                     else {
                         map.put(current.data, null);
                         previous = current;
                     }
 
                     current = current.next;
                 }
             }
 
             // 2.2.1 Implement an algorithm to find the nth to last element of a singly linked list.
             public static <T extends Comparable<T>> SNode nthToLast(SNode head, int n){
                 if (head == null) throw new IllegalArgumentException("'head' must be non-null.");
                 if (n <= 0) throw new IllegalArgumentException("'n' must be positive.");
 
                 SNode p1 = head;
                 SNode p2 = head;
 
                 for (int i = 0; i < n - 1; i++) {
                     if (p2 == null)
                         return null;
                     p2 = p2.next;
                 }
 
                 while (p2.next != null) {
                     p1 = p1.next;
                     p2 = p2.next;
                 }
 
                 return p1;
             }
 
             // 2.2.2 Find nth to last element using Queue
             public static <T extends Comparable<T>> SNode nthToLast2(SNode head, int n){
                 if (head == null || n < 1) {
                     return null;
                 }
 
                 Queue<SNode> q = new LinkedList<SNode>();
                 while (head != null) {
                     if (q.size() > n) {
                         q.remove(); // dequeue
                     } else {
                         q.add(head); // enqueue
                     }
 
                     head = head.next;
                 }
 
                 return q.size() < n ? null : q.peek();
             }
 
             // 2.3. Implement an algorithm to delete a node in the middle of a single linked list, given only access to that node.
             //      EXAMPLE: Input: the node c from the linked list a->b->c->d->e
             //               Result: nothing is returned, but the new linked list looks like a->b->d->e
 
             // 2.4. You have two numbers represented by a linked list, where each node contains a single digit.
             //      The digits are stored in reverse order, such that the 1's digit is at the head of the list.
             //      Write a function that adds the two numbers and returns the sum as a linked list.
             //      EXAMPLE: Input: (3 -> 1 -> 5), (5 -> 9 -> 2)
             //               Output: 8 -> 0 -> 8
 
             // 2.5. Given a circular linked list, implement an algorithm which returns node at the beginning of the loop.
             //      DEFINITION: Circular linked list: A (corrupt) linked list in which a nodes next pointer points to an earlier node, so as to make a loop in the linked list.
             //      EXAMPLE: Input: A -> B -> C -> D -> E -> C [the same C as earlier]
             //      Output: C
 
             // 2-A. Merge sort in linked list.
             public SNode mergeSort(SNode p) {
                 if (null == p || null == p.next)
                     return p;
                 SNode q = partition(p);
                 p = mergeSort(p);
                 q = mergeSort(q);
                 p = merge(p, q);
                 return p;
             }
 
             public SNode partition(SNode p) {
                 SNode p1 = p;
                 SNode p2 = p.next;
                 while (null != p2 && null != p2.next) {
                     p2 = p2.next.next;
                     p2 = p1.next;
                 }
 
                 SNode q = p1.next;
                 p1.next = null;
                 return q;
             }
 
             public SNode merge(SNode p, SNode q) {
                 SNode head = null, r = null;
                 while (null != p && null != q) {
                     SNode next;
                     if (p.compareTo(q) < 0) {
                         next = p;
                         p = p.next;
                     } else {
                         next = q;
                         q = q.next;
                     }
 
                     if (null == head) {
                         head = r = next;
                     } else {
                         r.next = next;
                         r = r.next;
                     }
                 }
 
                 if (null == p)
                     r.next = q;
                 if (null == q)
                     r.next = p;
                 return head;
             }
 
             @Override
             public String toString() {
                 StringBuilder sb = new StringBuilder();
                 for (SNode n = this; null != n; n = n.next) {
                     sb.append("{").append(n.data).append("}, ");
                 }
 
                 return sb.substring(0, sb.length() - 2);
             }
 
             @Override
             public int compareTo(SNode o) {
                 return this.data - o.data;
             }
         }
         // Doubly linked list
         public static class DNode implements Comparable<DNode>{
             public int data;
             public DNode next;
             public DNode prev;
 
             // Insert a node to a doubly linked list
             public static DNode insertDNode(DNode head, DNode insert){
                 if (head == null) return insert;
                 if (insert == null) return head;
 
                 if (head.compareTo(insert) >= 0) {
                     insert.next = head;
                     head.prev = insert;
                     return insert;
                 }
 
                 for (DNode current = head; current.next != null; current = current.next){
                     if (current.next.compareTo(insert) >=0) {
                         insert.next = current.next;
                         if (insert.next != null) insert.next.prev = insert;
                         current.next = insert;
                         insert.prev = current;
                     }
                 }
                 return head;
             }
 
             // Delete a node to a doubly linked list
             public static DNode deleteDNode(DNode head, int delete){
                 if (head == null) return null;
                 if (head.data == delete) return head.next;
 
                 for (DNode current = head; current.next != null; current = current.next){
                     if (current.next.data == delete){
                         current.next = current.next.next;
                         if(current.next != null) current.next.prev = current;
                     }
                 }
                 return head;
             }
             
             // Reverse
             public static DNode reverse(DNode current) {
                 if (null == current)
                     return current;
 
                 while (null != current.next) {
                     DNode save = current.next;
                     current.next = current.prev;
                     current.prev = save;
                     current = save;
                 }
 
                 return current;
             }
 
             @Override
             public String toString() {
                 StringBuilder sb = new StringBuilder();
                 for (DNode n = this; null != n; n = n.next) {
                     sb.append("{").append(n.data).append("}, ");
                 }
 
                 return sb.substring(0, sb.length() - 2);
             }
 
             @Override
             public int compareTo(DNode o) {
                 return this.data - o.data;
             }
         }
     }
 
     public static class Stacks {
 
     }
 
     public static class Recursions {
         public static String toExcelColumn(int n) {
             // 26 cases : A - Z
             // 26^2 cases : AA - ZZ
             // 26^3 cases : AAA - ZZZ
 
             int cases = 26;
             int k = 0;
             for (; n > cases; k++) { // k is the level of recursion.
                 n -= cases;
                 cases *= 26;
             }
 
             n -= 1; // n falls between 0 and (26^k - 1).
             StringBuilder sb = new StringBuilder();
             for (int i = 0; i <= k; i++) {
                 sb.insert(0, (char) (n % 26 + 'A'));
                 n /= 26;
             }
 
             return sb.toString();
         }
 
         public static int fromExcelColumn(String s) {
             int columns = 0;
             for (int k = 1, cases = 26; k < s.length(); k++, cases *= 26) {
                 columns += cases;
             }
 
             for (int k = s.length() - 1, cases = 1; k >= 0; k--, cases *= 26) {
                 columns += cases * (int) (s.charAt(k) - 'A');
             }
 
             return columns + 1;
         }
     }
 
     public static class Sorting {
         public static int indexOutOfCycle(int i, int... args) {
             if (null == args)
                 throw new IllegalArgumentException("'args' must be non-null.");
             if (0 == args.length)
                 throw new IllegalArgumentException("'args' must not be empty.");
 
             return indexOutOfCycle(0, args.length - 1, i, args);
         }
 
         private static int indexOutOfCycle(int left, int right, int i, int... args) {
             int pivot = (left + right) / 2;
             if (i == args[pivot]) {
                 return pivot;
             } else if (left == right) {
                 return -1;
             } else if (i == args[right]) {
                 return right;
             }
 
             if (args[right] < args[pivot]) {
                 if (args[right] < i && i < args[pivot]) {
                     return indexOutOfCycle(left, pivot - 1, i, args);
                 } else {
                     return indexOutOfCycle(pivot + 1, right - 1, i, args);
                 }
             } else {
                 if (args[pivot] < i && i < args[right]) {
                     return indexOutOfCycle(pivot + 1, right - 1, i, args);
                 } else {
                     return indexOutOfCycle(left, pivot - 1, i, args);
                 }
             }
         }
 
         public static int smallestOutOfCycle(int... args) {
             if (null == args)
                 throw new IllegalArgumentException("'args' must be non-null.");
             if (0 == args.length)
                 throw new IllegalArgumentException("'args' must not be empty.");
 
             return smallestOutOfCycle(0, args.length - 1, args);
         }
 
         private static int smallestOutOfCycle(int left, int right, int... args) {
             if (right == left) {
                 return args[left];
             } else {
                 int pivot = (left + right) / 2;
                 if (args[right] < args[pivot]) {
                     return smallestOutOfCycle(pivot + 1, right, args);
                 } else {
                     return smallestOutOfCycle(left, pivot, args);
                 }
             }
         }
 
         public static void mergeSort(int[] elements) {
             int[] temp = new int[elements.length];
             mergeSort(elements, temp, 0, elements.length - 1);
         }
 
         private static void mergeSort(int[] input, int[] temp, int left, int right) {
             if (left < right) {
                 int center = (left + right) / 2;
                 mergeSort(input, temp, left, center);
                 mergeSort(input, temp, center + 1, right);
                 merge(input, temp, left, center + 1, right);
             }
         }
 
         private static void merge(int[] input, int[] temp, int left, int right, int rightEnd) {
             int leftEnd = right - 1;
             int leftBegin = left;
             int position = 0;
             while (left <= leftEnd && right <= rightEnd) {
                 if (input[left] <= input[right]) {
                     temp[position++] = input[left++];
                 } else {
                     temp[position++] = input[right++];
                 }
             }
 
             while (left <= leftEnd) {
                 temp[position++] = input[left++];
             }
 
             while (right <= rightEnd) {
                 temp[position++] = input[right++];
             }
 
             System.arraycopy(temp, 0, input, leftBegin, rightEnd - leftBegin + 1);
         }
     }
 
     public static class BNode<T extends Comparable<T>> implements Comparable<BNode<T>> {
         public T item;
         public BNode<T> left;
         public BNode<T> right;
         public BNode<T> parent;
 
         public static <T extends Comparable<T>> BNode<T> of(BNode<T> parent) {
             BNode<T> node = new BNode<T>();
             node.parent = parent;
             return node;
         }
 
         public static <T extends Comparable<T>> BNode<T> of(T item, BNode<T> left, BNode<T> right) {
             BNode<T> node = new BNode<T>();
             node.item = item;
             node.left = left;
             node.right = right;
             return node;
         }
 
         @Override
         public int compareTo(BNode<T> o) {
             return this.item.compareTo(o.item);
         }
 
         @Override
         public String toString() {
             return item.toString();
         }
     }
 
     public static int weighedChoice(int... weights) {
         int sum = 0;
         for (int i = 0; i < weights.length; i++) {
             sum += weights[i];
         }
 
         int random = new Random().nextInt(sum);
         for (int i = 0; i < weights.length; i++, random -= weights[i]) {
             if (random < weights[i])
                 return i;
         }
 
         // http://download.oracle.com/javase/1.4.2/docs/guide/lang/assert.html
         throw new RuntimeException("This bug should go unhandled.");
     }
 
     public static void knuthShuffle(List<Integer> list) {
         Random random = new Random();
         for (int i = 0; i < list.size(); i++) {
             int j = i + random.nextInt(list.size() - i);
             Integer save = list.get(j);
             list.set(j, list.get(i));
             list.set(i, save);
         }
     }
 
     public static int rand7() {
         int rand21;
         do {
             rand21 = 5 * (rand5() - 1); // 0, 5, ..., 20.
             rand21 += rand5(); // 1, 2, ..., 5.
         } while (rand21 > 21);
 
         assert rand21 >= 1 && rand21 <= 21;
         return rand21 % 7 + 1; // 1, 2, ..., 7
     }
 
     public static int rand5() {
         return new Random().nextInt() % 5 + 1;
     }
 
     public static void swap(char[] chars, int i, int j) {
         if (chars[i] != chars[j]) {
             chars[i] ^= chars[j];
             chars[j] ^= chars[i];
             chars[i] ^= chars[j];
         }
     }
 
     public static void swap(int[] ints, int i, int j) {
         if (ints[i] != ints[j]) {
             ints[i] ^= ints[j];
             ints[j] ^= ints[i];
             ints[i] ^= ints[j];
         }
     }
 }
