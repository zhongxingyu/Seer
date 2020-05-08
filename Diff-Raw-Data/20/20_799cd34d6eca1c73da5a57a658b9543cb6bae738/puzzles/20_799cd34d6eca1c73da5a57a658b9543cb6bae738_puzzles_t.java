 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertTrue;
 import static junit.framework.Assert.fail;
 
 import java.math.BigInteger;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Queue;
 import java.util.Random;
 import java.util.Stack;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.junit.Test;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 
 // Heapsort competes with quicksort, another very efficient nearly-in-place sort algorithm
 // Quicksort is typically somewhat faster, due to better cache behavior and other factors.
 // The worst-case running time for quicksort is makes it unacceptable for large data sets.
 // Heapsort relies strongly on random access, and its poor locality of reference requires
 // efficient random access to be practical.
 // Mergesort is a stable sort, unlike quicksort and heapsort, and can be easily adapted
 // to operate on linked lists and very large set on slow media with long access time.
 // Heapsort requires only @(1) auxiliary space instead of Mergesort @(n) auxiliary space,
 // quicksort with in-place partitioning and tail recursion uses only O(log n) space.
 
 public class puzzles {
     private static final Random random = new Random();
 
     public interface Action<V> {
         void apply(V v);
     }
 
     public interface Action2<V, W> {
         void apply(V v, W w);
     }
 
     public static class Edge {
         public int y;
         public int weight;
         public static Edge of(int y, int weight) { Edge e = new Edge(); e.y = y; e.weight = weight; return e; }
     }
 
     public static class Graph {
        public Stack<Integer> topologicalSort;
         public List<Edge>[] edges;
         int verticeCount; 
         int edgeCount;
         boolean directed;
 
         boolean[] discovered = new boolean[edges.length];
         boolean[] processed = new boolean[edges.length];
         int[] parents = new int[edges.length];
         int[] colors = new int[edges.length];
 
         public void addEdge(int x, int y, int weight) {
             edges[x].add(Edge.of(y, weight));
             if (!directed) {
                 edges[y].add(Edge.of(x, weight));
             }
         }
 
         public void dfs(int v, Action<Integer> enter, Action2<Integer, Integer> cross, Action<Integer> leave) {
             discovered[v] = true;
             enter.apply(v);

             for (Edge e : edges[v]) {
                 if (!discovered[e.y]) {
                     parents[e.y] = v;
                     cross.apply(v, e.y);
                     dfs(e.y, enter, cross, leave);
                 } else if (!processed[e.y] || directed) {
                     cross.apply(v, e.y);
                 }
             }

            leave.apply(v); // leave might be an action for topological sort.
            processed[v] = true;
         }
 
        private Action<Integer> sort = new Action<Integer>() {
             public void apply(Integer v) {
                topologicalSort.add(v);
             }
         };
 
         public void bfs(int start, Action<Integer> enter, Action2<Integer, Integer> cross, Action<Integer> leave) {
             Queue<Integer> queue = new LinkedList<Integer>();
             queue.add(start);
             discovered[start] = true;
 
             while (!queue.isEmpty()) {
                 int v = queue.remove();
                 enter.apply(v);
                 processed[v] = true;

                 for (Edge e : edges[v]) {
                     if (!processed[e.y] || directed) {
                         cross.apply(v, e.y);
                     }
 
                     if (!discovered[e.y]) {
                         queue.add(e.y);
                         discovered[e.y] = true;
                         parents[e.y] = v;
                     }
                 }
 
                 leave.apply(v);
             }
         }
 
         public int twoColorComponents() {
             int count = 0;
             for (int v = 0; v < edges.length; v++) {
                 if (!discovered[v]) {
                     count++;
                     colors[v] = 0;
                     bfs(v, null, complementColor, null);
                 }
             }
 
             return count;
         }
 
         private Action2<Integer, Integer> complementColor = new Action2<Integer, Integer>() {
             public void apply(Integer v, Integer y) {
                 if (colors[v] == colors[y]) {
                     throw new IllegalStateException(String.format("This is not a bipartitle due to (%d, %d).", v, y));
                 }
 
                 colors[y] = ~colors[v];
             }
         };
 
         private Action2<Integer, Integer> throwIfCyclic = new Action2<Integer, Integer>() {
             public void apply(Integer v, Integer y) {
                 if (v == y) {
                     throw new IllegalStateException();
                 }
             }
         };
 
         public void prim(int start) {
             boolean[] isInTrees = new boolean [this.edges.length];
             int[] distances = new int[this.edges.length];
             for (int v = 0; v < this.edges.length; v++) {
                 isInTrees[v] = false;
                 distances[v] = Integer.MAX_VALUE;
                 parents[v] = -1;
             }
 
             distances[start] = 0;
             for (int v = start; !isInTrees[v]; ) {
                 isInTrees[v] = true;
                 for (Edge e : this.edges[v]) {
                     if (!isInTrees[e.y] && distances[e.y] > e.weight) {
                         distances[e.y] = e.weight;
                         parents[e.y] = v;
                     }
                 }
 
                 v = 0;
                 int distance = Integer.MAX_VALUE;
                 for (int x = 0; x < this.edges.length; x++) {
                     if (!isInTrees[x] && distance > distances[x]) {
                         v = x;
                         distance = distances[x];
                     }
                 }
             }
         }
 
         public void dijkstra(int start) {
             boolean[] isInTrees = new boolean[this.edges.length];
             int[] distances = new int[this.edges.length];
 
             for (int v = 0; v < this.edges.length; v++) {
                 isInTrees[v] = false;
                 distances[v] = Integer.MAX_VALUE;
                 parents[v] = -1;
             }
 
             distances[start] = 0;
             for (int v = start; !isInTrees[v]; ) {
                 isInTrees[v] = true;
                 for (Edge e : this.edges[v]) {
                     if (distances[e.y] > distances[v] + e.weight) {
                         distances[e.y] = distances[v] + e.weight;
                         parents[e.y] = v;
                     }
                 }
 
                 v = 0;
                 int distance = Integer.MAX_VALUE;
                 for (int x = 0; x < this.edges.length; x++) {
                     if (!isInTrees[x] && distance > distances[x]) {
                         v = x;
                         distance = distances[x];
                     }
                 }
             }
         }
 
         public void floyd() {
             int[][] matrix = new int[3][3];
             int throughK;
             for (int k = 0; k < matrix.length; k++) {
                 for (int i = 0; i < matrix.length; i++) {
                     for (int j = 0; j < matrix.length; j++) {
                         throughK = matrix[i][k] + matrix[k][j];
                         if (throughK < matrix[i][j]) {
                             matrix[i][j] = throughK;
                         }
                     }
                 }
             }
         }
 
         public void printPath(int start, int end) {
             if (start == end || -1 == end) {
                 System.out.print(String.format("%d ", start));
             } else {
                 printPath(start, parents[end]);
                 System.out.print(String.format("%d", end));
             }
         }
     }
 
     public static class LinkedDictionary<K, V> {
         private final LinkedList<SimpleEntry<K, V>> list = new LinkedList<SimpleEntry<K, V>>();
         private final Map<K, LinkedList.Node<SimpleEntry<K, V>>> map = new HashMap<K, LinkedList.Node<SimpleEntry<K, V>>>();
         private final int capacity = 1024;
 
         public void trimEldest() {
             SimpleEntry<K, V> eldest = this.list.getFirst();
             this.map.remove(eldest.getKey());
             this.list.removeFirst();
         }
 
         private void access(LinkedList.Node<SimpleEntry<K, V>> node) {
             this.list.remove(node);
             this.list.addLast(node);
         }
 
         public void put(K key, V value) {
             this.remove(key);
             this.list.addLast(new SimpleEntry<K, V>(key, value));
             this.map.put(key, this.list.getLastNode());
 
             while (this.map.size() > this.capacity) {
                 this.trimEldest();
             }
         }
 
         public V get(K key) {
             if (!map.containsKey(key)) {
                 return null;
             }
 
             access(map.get(key));
             return map.get(key).getValue().getValue();
         }
 
         public boolean remove(K key) {
             if (!map.containsKey(key)) {
                 return false;
             }
 
             list.remove(map.get(key));
             map.remove(key);
             return true;
         }
 
         static class LinkedList<T> {
             public static class Node<T> {
                 public T getValue() { throw new UnsupportedOperationException(); }
                 public Node<T> getNext() { throw new UnsupportedOperationException(); }
                 public Node<T> getPrevious() { throw new UnsupportedOperationException(); }
             }
 
             public boolean isEmpty() { throw new UnsupportedOperationException(); }
             public T getFirst() { throw new UnsupportedOperationException(); }
             public Node<T> getLastNode() { throw new UnsupportedOperationException(); }
             public void remove(Node<T> t) { throw new UnsupportedOperationException(); }
             public void removeFirst() { throw new UnsupportedOperationException(); }
             public void addLast(Node<T> t) { throw new UnsupportedOperationException(); }
             public void addLast(T t) { throw new UnsupportedOperationException(); }
         }
     }
 
     @Test
     public void testPrime() {
         assertEquals(919, prime(912));
         assertEquals(2, prime(1));
         assertEquals(2, prime(2));
         assertEquals(3, prime(3));
         assertEquals(5, prime(4));
         assertEquals(17, prime(15));
     }
 
     /*
      * This function returns a number when the probability that the number is prime exceeds (1 - 0.5^certainty).
      */
     public static long prime(long n) {
         return prime(n, 5); // the is 96.875% prime.
     }
 
     public static long prime(long n, int certainty) {
 
         if (n <= 2) return 2;
         if (n <= 3) return 3;
 
         long lnN = (int)Math.ceil(Math.log(n));
         for (long prime = (0 == n % 2 ? 1 + n : n); prime < n + lnN * 3/2; prime += 2) {
             boolean found = true;
             for (int i = 0; i < certainty; i++) { // an iteration of rabin miller tests.
                 BigInteger a = BigInteger.valueOf(1 + random.nextInt((int)n - 1));
                 BigInteger a2theNminus1 = a; // a uniform random on (1, n - 1)
                 for (int e = 1; e < prime - 1; e++) {
                     a2theNminus1 = a2theNminus1.multiply(a);
                 }
 
                 if (!BigInteger.ONE.equals(a2theNminus1.mod(BigInteger.valueOf(prime)))) {
                     found = false;
                     break;
                 }
             }
 
             if (found) {
                 return prime;
             }
         }
 
         return -1;
     }
 
     public int rabinKarp(String t, String p) {
         int m = p.length();
         int hashP = hash(p.toCharArray(), 0, m);
         int hashT = hash(t.toCharArray(), 0, m);
         if (hashP == hashT) { return 0; }
 
         int a_to_m_minus_1 = 1; // 31 ^ (m - 1)
         for (int i = 0; i < m - 1; i++) {
             a_to_m_minus_1 = (a_to_m_minus_1 << 5) - a_to_m_minus_1;
         }
 
         for (int i = 1; i < t.length() - m + 1; i++) {
             hashT = hashT - a_to_m_minus_1 * t.toCharArray()[i - 1];
             hashT = (hashT << 5) - hashT;
             hashT = hashT + t.toCharArray()[i + m - 1];
             if (hashP == hashT) { return i; }
         }
 
         return -1;
     }
 
     public static int hash(char[] chars, int offset, int length) {
         int hash = 0;
         for (int i = 0; i < length; i++) {
             hash = (hash << 5) - hash + chars[offset + i];
         }
 
         return hash;
     }
 
     public static class MinStack<T extends Comparable<T>> {
         private T mininum;
         private Stack<T> stack = new Stack<T>();
 
         public T getMin() {
             return mininum;
         }
 
         public MinStack<T> push(T element) {
             if (null == mininum || element.compareTo(mininum) <= 0) {
                 stack.push(mininum);
                 mininum = element;
             }
 
             stack.push(element);
             return this;
         }
 
         public T pop() {
             T element = stack.pop();
             if (element.compareTo(mininum) == 0) {
                 mininum = stack.pop();
             }
 
             return element;
         }
     }
 
     public static int[] products(int... numbers) {
         int[] prefixProducts = new int[numbers.length];
         prefixProducts[0] = 1;
         for (int i = 1; i < numbers.length; i++) {
             prefixProducts[i] = prefixProducts[i-1] * numbers[i-1];
         }
 
         int[] postfixProducts = new int[numbers.length];
         postfixProducts[numbers.length - 1] = 1;
         for (int i = numbers.length - 2; i >= 0; i--) {
             postfixProducts[i] = postfixProducts[i+1] * numbers[i+1];
         }
 
         int[] products = new int[numbers.length];
         for (int i = 0; i < numbers.length; i++) {
             products[i] = prefixProducts[i] * postfixProducts[i];
         }
 
         return products;
     }
 
     // http://n1b-algo.blogspot.com/2009/01/string-permutations.html
     public static Iterable<String> permutate(char[] chars, int k) {
         if (chars.length == k) {
             return ImmutableList.of(String.valueOf(chars));
         }
 
         List<String> permutations = new ArrayList<String>();
         BitSet bits = new BitSet(128);
         for (int i = k; i < chars.length; i++) {
             if (bits.get(chars[i])) {
                 continue;
             } else {
                 bits.set(chars[i]);
                 swap(chars, k, i);
                 Iterables.addAll(permutations, permutate(chars, k + 1));
                 swap(chars, k, i);
             }
         }
 
         return permutations;
     }
 
     public static int divide(int dividend, int divisor) {
         int bit = 1;
         while (divisor <= dividend) {
             divisor <<= 1;
             bit <<= 1;
         }
 
         int quotient = 0;
         while (divisor > 0) {
             divisor >>= 1;
             bit >>= 1;
             if (dividend >= divisor) {
                 dividend -= divisor;
                 quotient |= bit;
             }
         }
 
         return quotient;
     }
 
     public static int[] kadane(int[][] m) {
         int[][] prefixSums = new int[m.length][m[0].length];
         for (int i = 0; i < m[0].length; i++) {
             prefixSums[0][i] = m[0][i];
             for (int j = 1; j < m.length; j++) {
                 prefixSums[j][i] = prefixSums[j - 1][i] + m[j][i];
             }
         }
 
         int maxTop = 0, maxLeft = 0, maxBottom = 0, maxRight = 0, maxSum = m[0][0];
         for (int top = 0; top < m[0].length; top++) {
             for (int bottom = top; bottom < m[0].length; bottom++) {
                 int minimum = 0, maximum = 0;
                 for (int left = 0, right = 0; right < m.length; right++) {
                     if (top == 0) {
                         maximum += prefixSums[bottom][right];
                     } else {
                         maximum += prefixSums[bottom][right] - prefixSums[top - 1][right];
                     }
 
                     if (maximum <= minimum) {
                         minimum = maximum;
                         left = right;
                     }
 
                     if (maximum - minimum > maxSum) {
                         maxTop = top;
                         maxBottom = bottom;
                         maxLeft = left;
                         maxRight = right;
                         maxSum = maximum - minimum;
                     }
                 }
             }
         }
 
         return new int[] { maxSum, maxTop, maxLeft, maxBottom, maxRight };
     }
 
     // Kadane's algorithm http://en.wikipedia.org/wiki/Maximum_subarray_problem
     public static int[] kadane(int... a) {
         int maxHead = 0, maxTail = 0, maxSum = a[0];
         for (int head = 0, tail = 1, sum = a[0]; tail < a.length; tail++) {
             sum = sum + a[tail];
             if (sum > maxSum) {
                 maxHead = head; maxTail = tail; maxSum = sum;
             }
 
             if (0 > sum) {
                 head = tail + 1; sum = 0;
             }
         }
 
         return new int[] { maxSum, maxHead, maxTail };
     }
 
     public static String toExcelColumn(int n) {
         //   26 cases :   A -   Z
         // 26^2 cases :  AA -  ZZ
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
             sb.insert(0, (char)(n % 26 + 'A'));
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
             columns += cases * (int)(s.charAt(k) - 'A');
         }
 
         return columns + 1;
     }
 
     public static void subsets(int n) {
         Boolean a[] = new Boolean[n];
         backtrack(a, 0, n - 1);
     }
 
     public static boolean isSolution(Boolean a[], int k, int level) {
         return k == level;
     }
 
     public static void backtrack(Boolean a[], int k, int level) {
         if (isSolution(a, k, level)) {
             System.out.print(Joiner.on(" ,").join(a));
         } else {
             boolean candidates[] = {true, false}; // constructCandidates(a, k, n);
             for (int i = 0; i < candidates.length; i++) {
                 a[k] = candidates[i];
                 // move
                 backtrack(a, k, level);
                 // back
             }
         }
     }
 
     public static int median3(int[] elements, int left, int right) {
         int center = (left + right) / 2;
         if (elements[left] > elements[center]) swap(elements, left, center);
         if (elements[left] > elements[right]) swap(elements, left, right);
         if (elements[center] > elements[right]) swap(elements, center, right);
         swap(elements, center, right - 1); // place pivot at position right - 1;
         return elements[right - 1];
     }
 
     public static void quicksort(int[] elements, int left, int right) {
         if (left >= right) return;
 
         int pivot = elements[right];
         int i = left;
         int j = right - 1;
 
         while (true) {
             while (elements[i] < pivot) i++;
             while (j > 0 && elements[j] > pivot) j--;
             if (i >= j) break;
             swap(elements, i, j);
         }
 
         // to print the k-th smallest, if (i == k) System.out.print(elements[i]);
         swap(elements, i, right);
         quicksort(elements, left, i - 1);
         quicksort(elements, i + 1, right);
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
             temp[position++] = input[right++];
         }
 
         while (right <= rightEnd) {
             temp[position++] = input[right++];
         }
 
         System.arraycopy(temp, 0, input, leftBegin, rightEnd - leftBegin + 1);
     }
 
     public static class SNode<T> implements Comparable<SNode<T>> {
         public T item;
         public SNode<T> next;
 
         public SNode<T> mergeSort(SNode<T> p) {
             if (null == p || null == p.next) return p;
             SNode<T> q = partition(p);
             p = mergeSort(p);
             q = mergeSort(q);
             p = merge(p, q);
             return p;
         }
 
         public SNode<T> partition(SNode<T> p) {
             SNode<T> p1 = p;
             SNode<T> p2 = p.next;
             while (null != p2 && null != p2.next) {
                 p2 = p2.next.next;
                 p2 = p1.next;
             }
 
             SNode<T> q = p1.next;
             p1.next = null;
             return q;
         }
 
         public SNode<T> merge(SNode<T> p, SNode<T> q) {
             SNode<T> head = null, r = null;
             while (null != p && null != q) {
                 SNode<T> next;
                 if (p.compareTo(q) < 0) {
                     next = p; p = p.next;
                 } else {
                     next = q; q = q.next;
                 }
 
                 if (null == head) {
                     head = r = next;
                 } else {
                     r.next = next; r = r.next;
                 }
             }
 
             if (null == p) r.next = q;
             if (null == q) r.next = p;
             return head;
         }
 
         @Override
         public int compareTo(SNode<T> o) {
             return 0;
         }
     }
 
     public static class DNode<T> {
         public T item;
         public DNode<T> next;
         public DNode<T> prev;
 
         public static <T> DNode<T> reverse(DNode<T> current) {
             if (null == current) return current;
 
             while (null != current.next) {
                 DNode<T> save = current.next;
                 current.next = current.prev;
                 current.prev = save;
                 current = save;
             }
 
             return current;
         }
     }
 
     public static class BNode<T extends Comparable<T>> implements Comparable<BNode<T>> {
         public T item;
         public BNode<T> left;
         public BNode<T> right;
         public BNode<T> parent;
 
         public static <T extends Comparable<T>> BNode<T> of(T item, BNode<T> left, BNode<T> right) {
             BNode<T> node = new BNode<T>();
             node.item = item;
             node.left = left;
             node.right = right;
             return node;
         }
 
         public static <T extends Comparable<T>> BNode<T> reverseInorder(BNode<T> current, int n, AtomicInteger k) {
             if (null == current) return current;
 
             reverseInorder(current.right, n, k);
             if (k.incrementAndGet() == n) {
                 System.out.print(current.item);
                 return current;
             }
 
             return reverseInorder(current.left, n, k);
         }
 
         // http://crackinterviewtoday.wordpress.com/2010/03/12/check-whether-given-binary-tree-is-a-bst-or-not/
         public static <T extends Comparable<T>> boolean isBST(BNode<T> current) {
             if (null == current) return true;
             if (null != current.left && maximum(current.left).compareTo(current) > 0) { return false; }
             if (null != current.right && minimum(current.right).compareTo(current) < 0) { return false; }
             if (!isBST(current.left) || !isBST(current.right)) return false;
 
             return true;
         }
 
         public static <T extends Comparable<T>> BNode<T> maximum(BNode<T> node) { while (null != node.right) { node = node.right; } return node; }
         public static <T extends Comparable<T>> BNode<T> minimum(BNode<T> node) { while (null != node.left) { node = node.left; } return node; }
 
         public static <T extends Comparable<T>> List<T> yieldInorder(BNode<T> current, BNode<T> left, BNode<T> right) {
             // BNode<T> ancestor = findLowestCommonAncestor(current, left, right);
             if (null ==  current) return ImmutableList.of();
 
             List<T> output = new ArrayList<T>();
             if (current.compareTo(left) >= 0 && current.compareTo(right) <= 0)
                 output.add(current.item);
             if (current.compareTo(left) > 0)
                 output.addAll(yieldInorder(current.left, left, right));
             if (current.compareTo(right) < 0)
                 output.addAll(yieldInorder(current.right, left, right));
             return output;
         }
 
         public static <T extends Comparable<T>> BNode<T> findLowestCommonAncestor(BNode<T> current, BNode<T> left, BNode<T> right) {
             if (null == current) {
                 return current;
             }
 
             if (current.compareTo(left) > 0 && current.compareTo(right) > 0) {
                 return findLowestCommonAncestor(current.left, left, right);
             } else if (current.compareTo(left) < 0 && current.compareTo(right) < 0) {
                 return findLowestCommonAncestor(current.right, left, right);
             }
 
             return current;
         }
 
         public static <T extends Comparable<T>> List<BNode<T>> yieldInorder(BNode<T> current) {
             if (null ==  current) return ImmutableList.of();
 
             List<BNode<T>> output = new ArrayList<BNode<T>>();
             if (null != current.left) output.addAll(yieldInorder(current.left));
             output.add(current);
             if (null != current.right) output.addAll(yieldInorder(current.right));
             return output;
         }
 
         public static <T extends Comparable<T>> List<BNode<T>> yieldInorderFast(BNode<T> current) {
             List<BNode<T>> output = new ArrayList<BNode<T>>();
             Stack<BNode<T>> stack = new Stack<BNode<T>>();
 
             while (null != current || !stack.isEmpty()) {
                 if (null == current) {
                     current = stack.pop();
                     output.add(current);
                     current = current.right;
                 } else {
                     stack.push(current);
                     current = current.left;
                 }
             }
 
             return output;
         }
 
         public static <T extends Comparable<T>> List<BNode<T>> yieldPreorder(BNode<T> current) {
             List<BNode<T>> output = new ArrayList<BNode<T>>();
             Stack<BNode<T>> stack = new Stack<BNode<T>>();
 
             while (null != current || !stack.isEmpty()) {
                 if (null == current) {
                     current = stack.pop();
                 } else {
                     output.add(current);
                     stack.push(current.right);
                     current = current.left;
                 }
             }
 
             return output;
         }
 
         public static <T extends Comparable<T>> List<BNode<T>> yieldPostorder(BNode<T> current) {
             List<BNode<T>> output = new ArrayList<BNode<T>>();
             Stack<BNode<T>> stack = new Stack<BNode<T>>();
 
             while (null != current || !stack.isEmpty()) {
                 if (null == current) {
                     while (!stack.isEmpty() && null == stack.peek().right) {
                         current = stack.pop();
                         output.add(current);
                     }
 
                     current = stack.isEmpty() ? null : stack.peek().right;
                 } else {
                     stack.push(current);
                     current = current.left;
                 }
             }
 
             return output;
         }
 
         public static <T extends Comparable<T>> int findHeight(BNode<T> current) {
             return null == current ? -1 : 1 + Math.max(findHeight(current.left), findHeight(current.right));
         }
 
         public static <T extends Comparable<T>> int findDistance(BNode<T> lhs, BNode<T> rhs) {
             Map<BNode<T>, Integer> map = new HashMap<BNode<T>, Integer>();
             for (int i = 0; null != lhs; lhs = lhs.parent) {
                 map.put(lhs, i++);
             }
 
             for (int i = 0; null != rhs; rhs = rhs.parent) {
                 if (map.containsKey(rhs)) return i + map.get(rhs);
                 i++;
             }
 
             return -1; // disjoint
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
             if (random < weights[i]) return i;
         }
 
         // http://download.oracle.com/javase/1.4.2/docs/guide/lang/assert.html
         throw new RuntimeException("This bug should go unhandled.");
     }
 
     public static List<Integer> reservoirSamples(Iterator<Integer> iterator, int k) {
         List<Integer> samples = new ArrayList<Integer>();
         int count = 0;
         Random random = new Random();
         while (iterator.hasNext()) {
             count++;
             if (samples.size() < k) {
                 samples.add(iterator.next());
             } else {
                 int s = random.nextInt(count);
                 if (s < k) {
                     samples.set(s, iterator.next());
                 }
             }
         }
 
         return samples;
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
 
     public boolean matchesWildcard(String pattern, String input) {
         if (pattern.equals(input)) return true;
 
         int i = 0, j = 0;
         for (; i < pattern.length() && j < input.length(); i++, j++) {
             // recursive cases
             if ('*' == pattern.charAt(i)) {
                 return matchesWildcard(pattern.substring(i), input.substring(j + 1))
                        || matchesWildcard(pattern.substring(i + 1), input.substring(j + 1))
                        || matchesWildcard(pattern.substring(i + 1), input.substring(j));
             }
 
             if ('?' != pattern.charAt(i) && pattern.charAt(i) != input.charAt(j)) return false;
         }
 
         if (i == pattern.length() && j == input.length()) return true;
         if ('*' == pattern.charAt(i)) return true;
 
         return false;
     }
 
     public boolean bracketsMatching(String s) {
         Stack<Character> brackets = new Stack<Character>();
         for (int i = 0; i < s.length(); i++) {
             Character c = s.charAt(i);
             if ('(' == c) brackets.push(')');
             else if ('[' == c) brackets.push(']');
             else if ('{' == c) brackets.push('}');
             else {
                 if (')' != c && ']' != c && '}' != c) continue;
                 if (brackets.isEmpty() || brackets.peek() != c) return false;
                 brackets.pop();
             }
         }
 
         return brackets.empty();
     }
 
     public static boolean isPowerOf2(int x) {
         return (0 == (x & x - 1)) && (x > 0);
     }
 
     // Design a card game http://math.hws.edu/javanotes/c5/s4.html
     interface Deck {
         void suffle();
         int cardsLeft();
         Card dealCard() throws IllegalStateException; // if the deck is empty.
     }
 
     interface Card {
         int getSuit();
         int getValue();
     }
 
     interface Hand {
         void addCard(Card c);
         boolean removeCard(Card card) throws NoSuchElementException;
         Card removeCard(int position) throws IndexOutOfBoundsException;
         int getCardCount();
         void clear();
         Card getCard(int position);
         void sortBySuit();
         void sortByValue();
     }
 
     interface Game {
         int getGamesPlayed();
         int getScoresAchived();
         void setUp(); // new Deck() and shuffle();
         void tearDown(); // 
         Card getCurrentCard();
         Card getNextCard();
     }
 
     public static class TicTacToeEngine {
         public enum TicTacToePiece { None, X, O; }
         public enum TicTacToeState { Uninitialized, Initialized, InProgress, Over; }
         public enum TicTacToeEngineResponse { IllegalPiece, IllegalPosition, GameOver, GameInProgress }
 
         private TicTacToeState gameState = TicTacToeState.InProgress;
         private TicTacToePiece winningPiece = TicTacToePiece.None;
         private TicTacToePiece currentPiece = TicTacToePiece.None;
         private TicTacToePiece[] boardArray = new TicTacToePiece[9];
         private final static int[][] boardLines = new int[][] {
             {0, 1}, {3, 1}, {6, 1}, // horizontal lines
             {0, 3}, {1, 3}, {2, 3}, // vertical lines
             {0, 4}, {2, 2} }; // diagonal lines
 
         public TicTacToeEngine(TicTacToePiece firstPiece) { initialize(firstPiece); }
         public TicTacToeEngineResponse reset(TicTacToePiece firstPiece) { initialize(firstPiece); return null; }
         private void initialize(TicTacToePiece firstPiece) {
             this.boardArray = new TicTacToePiece[9];
             this.currentPiece = firstPiece;
             this.gameState = TicTacToeState.Initialized;
             this.winningPiece = TicTacToePiece.None;
         }
 
         public TicTacToeEngineResponse movePiece(TicTacToePiece piece, int offset) {
             if (gameState == TicTacToeState.Over) {
                 return TicTacToeEngineResponse.GameOver;
             }
 
             if (piece != currentPiece) {
                 return TicTacToeEngineResponse.IllegalPiece;
             }
 
             if (offset < 0 || offset > 9 || boardArray[offset] != TicTacToePiece.None) {
                 return TicTacToeEngineResponse.IllegalPosition;
             }
 
             boardArray[offset] = currentPiece;
             updateGameState();
 
             if (gameState == TicTacToeState.Over) {
                 return TicTacToeEngineResponse.GameOver;
             } else {
                 return TicTacToeEngineResponse.GameInProgress;
             }
         }
 
         public void updateGameState() {
             for (int i = 0; i < boardLines.length; i++) {
                 if (3 == countPiecesUsingSteps(boardLines[i][0], boardLines[i][1], this.currentPiece)) {
                     winningPiece = currentPiece;
                 }
             }
 
             gameState = TicTacToeState.Over;
             if (winningPiece == TicTacToePiece.None) {
                 for (int i = 0; i < 9; i++) {
                     if (boardArray[i] == TicTacToePiece.None) {
                         gameState = TicTacToeState.InProgress;
                         break;
                     }
                 }
             }
 
             if (gameState == TicTacToeState.InProgress) {
                 currentPiece = (currentPiece == TicTacToePiece.X) ? TicTacToePiece.O : TicTacToePiece.X;
             }
         }
 
         private int countPiecesUsingSteps(int offset, int step, TicTacToePiece current) {
             for (int count = 0; count < 3; count++, offset += step) {
                 if (current != boardArray[offset]) {
                     return count;
                 }
             }
 
             return 3;
         }
     }
 
     public static int findSmallestOutOfCycle(int... numbers) {
         if (null == numbers) throw new IllegalArgumentException("'numbers' must be non-null.");
         if (0 == numbers.length) throw new IllegalArgumentException("'numbers' must not be empty.");
 
         return findSmallestOutOfCycle(0, numbers.length - 1, numbers);
     }
 
     private static int findSmallestOutOfCycle(int left, int right, int... numbers) {
         if (right == left) {
             return numbers[left];
         } else {
             int middle = (left + right) / 2;
             if (numbers[right] < numbers[middle]) {
                 return findSmallestOutOfCycle(middle + 1, right, numbers);
             } else {
                 return findSmallestOutOfCycle(left, middle, numbers);
             }
         }
     }
 
     @Test
     public void testPartitionBookshelf() {
         partition(3, 1, 2, 3, 4, 5, 6, 7, 8, 9);
     }
 
     public static void partition(int sections, int... books) {
         int[][] pivots = new int[books.length + 1][sections + 1];
         int[][] pages = new int[books.length + 1][sections + 1];
         int[] ps = new int[books.length+1]; // prefix sums
 
         ps[0] = 0;
         for (int i = 1; i <= books.length; i++) {
             ps[i] = ps[i-1] + books[i-1];
         }
 
         for (int i = 1; i <= books.length; i++) {
             pages[i][1] = ps[i];
         }
 
         for (int k = 1; k <= sections; k++) {
             pages[1][k] = books[0];
         }
 
         for (int n = 2; n <= books.length; n++) {
             for (int k = 2; k <= sections; k++) {
                 pages[n][k] = Integer.MAX_VALUE;
                 for (int book = 1; book <= n-1; book++) {
                     int value = Math.max(pages[book][k-1], ps[n] - ps[book]);
                     if (pages[n][k] > value) {
                         pages[n][k] = value;
                         pivots[n][k] = book;
                     }
                 }
             }
         }
 
         pivots[0][0] = 0;
     }
 
     public static List<Integer> knapsackUnbounded(int[] values, int[] costs, int capacity) {
         int[] sums = new int[capacity + 1];
         int[] bests = new int[capacity + 1];
         for (int c = 0; c < costs.length; c++) {
             for (int k = 0; k <= capacity; k++) {
                 if (costs[c] > k) continue;
                 if (sums[k] > sums[k - costs[c]] + values[c]) continue;
 
                 sums[k] = sums[k - costs[c]] + values[c];
                 bests[k] = c;
             }
         }
 
         List<Integer> knapsack = new ArrayList<Integer>();
         while (capacity > 0) {
             knapsack.add(bests[capacity]);
             capacity -= costs[bests[capacity]];
         }
 
         return knapsack;
     }
 
     // http://www.animal.ahrgr.de/showAnimationDetails.php3?lang=en&anim=18
     public static List<Integer> knapsack01(int[] values, int[] costs, int capacity) {
         int[][] sums = new int[costs.length + 1][capacity + 1];
         for (int n = 0; n < capacity + 1; n++) { sums[0][n] = 0; }
 
         for (int c = 0, k = 1; k <= values.length; k++, c++) {
             sums[k][0] = 0;
             for (int n = 1; n <= capacity; n++) {
                 if (costs[c] > n || sums[k - 1][n] > values[c] + sums[k - 1][n - costs[c]]) {
                     sums[k][n] = sums[k - 1][n];
                     continue;
                 }
 
                 sums[k][n] = values[c] + sums[k - 1][n - costs[c]];
             }
         }
 
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < sums.length; i++) {
             for (int j = 0; j < sums[0].length; j++)
                 sb.append(String.format("%d, ", sums[i][j]));
             sb.append("\r\n");
         }
         System.out.print(sb);
 
         List<Integer> knapsack = new ArrayList<Integer>();
         for (int k = values.length, c = k - 1; k > 0; k--, c--) {
             if (sums[k][capacity] != sums[k - 1][capacity]) {
                 knapsack.add(c);
                 capacity -= costs[c];
             }
         }
 
         return knapsack;
     }
 
     public static List<String> findLongestCommonSubstring(char[] lhs, char[] rhs) {
         int[][] spans = new int[lhs.length][rhs.length];
         int longest = 0;
         List<String> lcs = new ArrayList<String>();
 
         for (int i = 0; i < lhs.length; i++) {
             for (int j = 0; j < rhs.length; j++) {
                 if (lhs[i] == rhs[j]) {
                     spans[i][j] = (i == 0 || j == 0) ? 1 : spans[i - 1][j - 1] + 1;
                     if (spans[i][j] > longest) {
                         longest = spans[i][j];
                         lcs.clear();
                     }
 
                     if (spans[i][j] == longest) {
                         lcs.add(new String(lhs, i - longest + 1, longest));
                     }
                 }
             }
         }
 
         return lcs;
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
             public List<Integer> apply(int[] input) { return findModesUsingMap(input); };
         });
     }
 
     @Test
     public void testFindModesUsingArray() {
         testFindModes(new Function<int[], List<Integer>>() {
             public List<Integer> apply(int[] input) { return findModesUsingArray(input); };
         });
     }
 
     public void testFindModes(Function<int[], List<Integer>> findModes) {
         try {
             findModes.apply(null);
             fail("findsModes should have thrown NPE.");
         } catch (AssertionError ae) {
         } catch (NullPointerException npe) {
         } catch (Exception e) {
             fail("findsModes should have thrown NPE.");
         }
 
         assertEquals(ImmutableList.of(), findModes.apply(new int[0]));
 
         assertEquals(ImmutableList.of(1), findModes.apply(new int[] {1}));
         assertEquals(ImmutableList.of(2), findModes.apply(new int[] {1, 2, 2}));
         assertEquals(ImmutableList.of(3), findModes.apply(new int[] {1, 2, 2, 3, 3, 3}));
 
         assertEquals(ImmutableList.of(2), findModes.apply(new int[] {2, 2}));
         assertEquals(ImmutableList.of(1, 2), findModes.apply(new int[] {1, 1, 2, 2}));
     }
 
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
     public void testMatchPattern() {
         assertEquals(2, rabinKarp("aaabc", "abc"));
         assertEquals(2, rabinKarp("aaabcc", "abc"));
         assertEquals(0, rabinKarp("abc", "abc"));
         assertEquals(0, rabinKarp("abcc", "abc"));
         assertEquals(-1, rabinKarp("abc", "xyz"));
         assertEquals(-1, rabinKarp("abcc", "xyz"));
     }
 
     @Test
     public void testQuicksort() {
         int[] input = new int[] {44, 75, 23, 43, 55, 12, 64, 77, 33};
         quicksort(input, 0, 8);
         assertTrue(Arrays.equals(new int[]{12, 23, 33, 43, 44, 55, 64, 75, 77}, input));
     }
 
     @Test
     public void testKadane() {
         int[] a = { -2, 1, -3, 4, -1, 2, 1, -5, 4};
         assertTrue(Arrays.equals(new int[]{6, 3, 6}, kadane(a)));
         assertTrue(Arrays.equals(new int[]{3, 0, 0, 2, 2}, kadane(new int[][] {{1, 0, 1}, {0, -1, 0}, {1, 0, 1}})));
         assertTrue(Arrays.equals(new int[]{3, 0, 0, 0, 1}, kadane(new int[][] {{1, 2, -1}, {-3, -1, -4}, {1, -5, 2}})));
     }
 
     @Test
     public void testProducts() {
         Iterables.elementsEqual(ImmutableList.of(120, 60, 40, 30, 24), ImmutableList.of(products(1, 2, 3, 4, 5)));
     }
 
     @Test
     public void testDivide() {
         assertEquals(1024768/7, divide(1024768, 7));
     }
 
     @Test
     public void testPermutate() {
         assertTrue(Iterables.elementsEqual(
             ImmutableList.of("abc", "acb", "bac", "bca", "cba", "cab"),
             permutate("abc".toCharArray(), 0)));
         assertTrue(Iterables.elementsEqual(
             ImmutableList.of("abab", "abba", "aabb", "baab", "baba", "bbaa"),
             permutate("abab".toCharArray(), 0)));
     }
 
     @Test
     public void testFindSmallestOutOfCycle() {
         try {
             findSmallestOutOfCycle(null);
             fail("'numbers' must be non-null.");
         } catch (IllegalArgumentException e) {
         }
 
         try {
             findSmallestOutOfCycle(new int[0]);
             fail("'numbers' must not be empty.");
         } catch (IllegalArgumentException e) {
         }
 
         assertEquals(6, findSmallestOutOfCycle(new int[] {6}));
         assertEquals(6, findSmallestOutOfCycle(new int[] {6, 7}));
         assertEquals(6, findSmallestOutOfCycle(new int[] {7, 6}));
         assertEquals(6, findSmallestOutOfCycle(new int[] {38, 40, 55, 89, 6, 13, 20, 23, 36}));
         assertEquals(6, findSmallestOutOfCycle(new int[] {6, 13, 20, 23, 36, 38, 40, 55, 89}));
         assertEquals(6, findSmallestOutOfCycle(new int[] {13, 20, 23, 36, 38, 40, 55, 89, 6}));
     }
 
     @Test
     public void testToAndFromExcelColumn() {
         assertEquals("AB", toExcelColumn(28));
         assertEquals("ABC", toExcelColumn(731));
         assertEquals(28, fromExcelColumn(toExcelColumn(28)));
         assertEquals(731, fromExcelColumn(toExcelColumn(731)));
     }
 
     @Test
     public void testYieldInorderLeftToRight() {
         BNode<Integer> i12 = BNode.of(12, null, null);
         BNode<Integer> i17 = BNode.of(17, null, null);
         BNode<Integer> i15 = BNode.of(15, i12, i17);
         BNode<Integer> i8 = BNode.of(8, null, null);
         BNode<Integer> i10 = BNode.of(10, i8, i15);
         BNode<Integer> i2 = BNode.of(2, null, null);
         BNode<Integer> i5 = BNode.of(5, i2, i10);
         BNode<Integer> i20 = BNode.of(20, null, null);
         BNode<Integer> i24 = BNode.of(24, null, null);
         BNode<Integer> i22 = BNode.of(22, i20, i24);
         BNode<Integer> i27 = BNode.of(27, null, null);
         BNode<Integer> i25 = BNode.of(25, i22, i27);
         BNode<Integer> i19 = BNode.of(19, i5, i25);
         List<Integer> i13_21 = BNode.yieldInorder(i19, BNode.of(13, null, null), BNode.of(21, null, null));
         List<Integer> i12_22 = BNode.yieldInorder(i19, BNode.of(12, null, null), BNode.of(22, null, null));
         assertTrue(Iterables.elementsEqual(ImmutableList.of(19, 15, 17, 20), i13_21));
         assertTrue(Iterables.elementsEqual(ImmutableList.of(19, 15, 12, 17, 22, 20), i12_22));
     }
 
 //  public static int CountOnes(int value) {
 //      unchecked {
 //          uint x = (uint)value;
 //          x = ((0xaaaaaaaa & x) >> 1) + (0x55555555 & x);
 //          x = ((0xcccccccc & x) >> 2) + (0x33333333 & x);
 //          x = ((0xf0f0f0f0 & x) >> 4) + (0x0f0f0f0f & x);
 //          x = ((0xff00ff00 & x) >> 8) + (0x00ff00ff & x);
 //          x = (x >> 16) + (0x0000ffff & x);
 //          return (int)x;
 //      }
 //  }
 //
 //  public static int Reverse(int value) {
 //      unchecked {
 //          uint x = (uint)value;
 //          x = x >> 16 | (0x0000ffff & x) << 16;
 //          x = (0xff00ff00 & x) >> 8 | (0x00ff00ff & x) << 8;
 //          x = (0xf0f0f0f0 & x) >> 4 | (0x0f0f0f0f & x) << 4;
 //          x = (0xcccccccc & x) >> 2 | (0x33333333 & x) << 2;
 //          x = (0xaaaaaaaa & x) >> 1 | (0x55555555 & x) << 1;
 //          return (int)x;
 //      };
 //  }
 //
 //  public static int CountTrailingZeros(int value) {
 //      return CountOnes((value & -value) - 1);
 //  }
 //
 //  public static int CountLeadingZeros(int value) {
 //      unchecked {
 //          uint x = (uint)value;
 //          x |= x >>= 1;
 //          x |= x >>= 2;
 //          x |= x >>= 4;
 //          x |= x >>= 8;
 //          x |= x >>= 16;
 //          return CountOnes((int)~x);
 //      }
 //  }
 }
