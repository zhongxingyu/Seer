 package alex.algorithms.graphs.trees.binary;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Random;
 import java.util.Stack;
 
 public class TreeAlgorithms {
     static final Random rand = new Random(System.currentTimeMillis());
 
     public static <T extends Comparable<T>> int height(final Node<T> root) {
         if (root == null)
             return 0;
         return 1 + Math.max(height(root.getLeft()), height(root.getRight()));
     }
 
     public static Node<Integer> createRandomTree(final int height) {
         if (height == 0)
             return null;
         Node<Integer> root = new Node<>(rand.nextInt());
         root.setLeft(createRandomTree(height - 1));
         root.setRight(createRandomTree(height - 1));
         return root;
     }
 
     public static Node<Integer> insertNodeBST(final Node<Integer> root, final int value) {
         if (root == null) {
             Node<Integer> node = new Node<Integer>(value);
             return node;
         }
         if (value > root.getValue()) {
             root.setRight(insertNodeBST(root.getRight(), value));
         } else if (value < root.getValue()) {
             root.setLeft(insertNodeBST(root.getLeft(), value));
         }
         return root;
     }
 
     public static void printInOrder(final Node<Integer> root) {
         if (root == null)
             return;
         printInOrder(root.getLeft());
         System.out.printf("%d ", root.getValue());
         printInOrder(root.getRight());
     }
 
     public static void printPreOrder(final Node<Integer> root) {
         if (root == null)
             return;
         System.out.printf("%d ", root.getValue());
         printPreOrder(root.getLeft());
         printPreOrder(root.getRight());
     }
 
     public static void printPreOrderIterative(final Node<Integer> root) {
         Stack<Node<Integer>> stack = new Stack<>();
         stack.push(root);
         while (!stack.isEmpty()) {
             Node<Integer> node = stack.pop();
             System.out.printf("%d ", node.getValue());
             if (node.getRight() != null) {
                 stack.push(node.getRight());
             }
             if (node.getLeft() != null) {
                 stack.push(node.getLeft());
             }
         }
     }
 
     public static void lowestCommonAntecessor(Node<Integer> root, final int i, final int j) {
         int max = Math.max(i, j);
         int min = Math.min(i, j);
         while (root != null) {
             if (root.getValue() > min && root.getValue() > max) {
                 root = root.getLeft();
                 continue;
             }
             if (root.getValue() < min && root.getValue() < max) {
                 root = root.getRight();
                 continue;
             }
             if (root.getValue() <= max && root.getValue() >= min) {
                 break;
             }
         }
         if (root != null) {
             System.out.printf("Common Ancestor de %d e %d = %d", min, max, root.getValue());
         }
     }
 
     public static Node<Integer> leastCommonAntecessor(final Node<Integer> root, final int n1, final int n2) {
         if (root == null)
             return null;
 
         int max = Math.max(n1, n2);
         int min = Math.min(n1, n2);
 
         if (root.getValue() >= min && root.getValue() <= max) {
             return root;
         } else if (root.getValue() < min && root.getValue() < max) {
             return leastCommonAntecessor(root.getRight(), n1, n2);
         } else if (root.getValue() > min && root.getValue() > max) {
             return leastCommonAntecessor(root.getLeft(), n1, n2);
         }
         return null;
     }
 
     public static Node<Integer> rightRotation(final Node<Integer> oldRoot) {
         Node<Integer> root = oldRoot.getLeft();
         oldRoot.setLeft(root.getRight());
         root.setRight(oldRoot);
         return root;
     }
 
     public static void BFS(final Node<Integer> root) {
         Queue<Node<Integer>> queue = new LinkedList<>();
         queue.add(root);
         root.setBaconNumber(0);
         while (!queue.isEmpty()) {
             Node<Integer> current = queue.poll();
             for (Iterator<Node<Integer>> iterator = current.getAdj().iterator(); iterator.hasNext();) {
                 Node<Integer> c = iterator.next();
                 if (c.getBaconNumber() != -1) {
                     c.setBaconNumber(current.getBaconNumber() + 1);
                     queue.add(c);
                 }
             }
         }
     }
 
     public static Node<Integer> bstToDoubleLinky(final Node<Integer> root) {
         if (root == null)
             return null;
         Node<Integer> head = bstToDoubleLinky(root.getLeft());
         Node<Integer> tail = bstToDoubleLinky(root.getRight());
         if (tail != null) {
             while (tail.getLeft() != null) {
                 tail = tail.getLeft();
             }
         }
         if (head != null) {
             while (head.getRight() != null) {
                 head = head.getRight();
             }
         }
         if (head != null) {
             head.setRight(root);
             root.setLeft(head);
             root.setRight(tail);
             if (tail != null) {
                 tail.setLeft(root);
             }
         } else {
             root.setLeft(null);
             root.setRight(tail);
             if (tail != null) {
                 tail.setLeft(root);
             }
             head = root;
         }
         while (head.getLeft() != null) {
             head = head.getLeft();
         }
         return head;
     }
 
     public static void printVerticalSumOfTree(final Node<Integer> tree, final int axisVal, final int[] sums) {
         if (tree == null)
             return;
         sums[axisVal] += tree.getValue();
         printVerticalSumOfTree(tree.getLeft(), axisVal - 1, sums);
         printVerticalSumOfTree(tree.getRight(), axisVal + 1, sums);
     }
 
     public static void printHorizontalSumOfTree(final Node<Integer> tree, final int axisVal, final int[] sums) {
         if (tree == null)
             return;
         sums[axisVal] += tree.getValue();
         printHorizontalSumOfTree(tree.getLeft(), axisVal + 1, sums);
         printHorizontalSumOfTree(tree.getRight(), axisVal + 1, sums);
     }
 
     public static int getLeftWidth(final Node<Integer> root) {
         if (root == null)
             return 0;
         int right = 0;
         int left = 0;
         if (root.getRight() != null) {
             right = getLeftWidth(root.getRight()) - 1;
         }
         if (root.getLeft() != null) {
             right = getLeftWidth(root.getLeft()) + 1;
         }
         return Math.max(right, left);
     }
 
     public static int countElements(final Node<Integer> root) {
         if (root == null)
             return 0;
         return 1 + countElements(root.getLeft()) + countElements(root.getRight());
     }
 
     public static ArrayList<LinkedList<Node<Integer>>> getTreeLevels(final Node<Integer> root) {
         ArrayList<LinkedList<Node<Integer>>> r = new ArrayList<>();
         LinkedList<Node<Integer>> l = new LinkedList<Node<Integer>>();
         l.add(root);
         int level = 0;
         r.add(level, l);
         while (true) {
             LinkedList<Node<Integer>> nextLevel = new LinkedList<Node<Integer>>();
             for (int i = 0; i < r.get(level).size(); i++) {
                 Node<Integer> nodeLevel = r.get(level).get(i);
                 if (nodeLevel.getLeft() != null) {
                     nextLevel.add(nodeLevel.getLeft());
                 }
                 if (nodeLevel.getRight() != null) {
                     nextLevel.add(nodeLevel.getRight());
                 }
 
             }
             if (nextLevel.isEmpty()) {
                 break;
             } else {
                 level++;
                 r.add(level, nextLevel);
             }
         }
         return r;
     }
 
     public static void printTreeLevels(final Node<Integer> root, final int lvl) {
         LinkedList<Node<Integer>> l = new LinkedList<Node<Integer>>();
         l.add(root);
         int level = 0;
         while (level != lvl) {
             LinkedList<Node<Integer>> nextLevel = new LinkedList<Node<Integer>>();
             for (int i = 0; i < l.size(); i++) {
                 Node<Integer> nodeLevel = l.get(i);
                 if (nodeLevel.getLeft() != null) {
                     nextLevel.add(nodeLevel.getLeft());
                 }
                 if (nodeLevel.getRight() != null) {
                     nextLevel.add(nodeLevel.getRight());
                 }
 
             }
             if (nextLevel.isEmpty()) {
                 break;
             } else {
                 l = nextLevel;
                 level++;
                 System.out.println(level);
             }
         }
         for (Node<Integer> node : l) {
             System.out.printf("%d ", node.getValue());
         }
         System.out.println();
     }
 
     public static void find(final Node<Integer> node, final int num) {
         Stack<Node<Integer>> stack = new Stack<Node<Integer>>();
 
         Node<Integer> current = node;
         int tmp = num;
 
         while (stack.size() > 0 || current != null) {
             if (current != null) {
                 stack.add(current);
                 current = current.getLeft();
             } else {
                 current = stack.pop();
                 tmp--;
 
                 if (tmp == 0) {
                     System.out.println(current.getValue());
                     return;
                 }
 
                 current = current.getRight();
             }
         }
     }
 
     public static void printZigZag(final Node<Integer> root) {
         if (root == null)
             return;
         Stack<Node<Integer>>[] levels = new Stack[2];
         int current = 0;
         int next = 1;
         levels[current].push(root);
         while (!levels[0].isEmpty() || !levels[1].isEmpty()) {
             Node<Integer> node = levels[current].pop();
             System.out.printf("%d ", node.getValue());
             if (current == 0) {
                 if (node.getLeft() != null) {
                     levels[next].push(node.getLeft());
                 }
                 if (node.getRight() != null) {
                     levels[next].push(node.getRight());
                 }
             } else {
                 if (node.getRight() != null) {
                     levels[next].push(node.getRight());
                 }
                 if (node.getLeft() != null) {
                     levels[next].push(node.getLeft());
                 }
             }
             if (levels[current].isEmpty()) {
                 System.out.println();
             }
             current = (current + 1) % 2;
             next = (next + 1) % 2;
         }
     }
 
     public static void findPath(final Node<Integer> root, final int expectedSum) {
         if (root == null)
             return;
         ArrayList<Integer> path = new ArrayList<Integer>();
         int currentSum = 0;
         findPath(root, expectedSum, currentSum, path);
     }
 
     private static void findPath(final Node<Integer> root, final int expectedSum, int currentSum,
             final ArrayList<Integer> path) {
         currentSum += root.getValue();
         path.add(root.getValue());
         boolean isLeaf = (root.getLeft() == null && root.getRight() == null);
         if (isLeaf && currentSum == expectedSum) {
             System.out.printf("Path found\n");
             for (Iterator<Integer> iterator = path.iterator(); iterator.hasNext();) {
                 Integer integer = iterator.next();
                 System.out.printf("%d ", integer);
             }
             System.out.println();
         }
         if (root.getLeft() != null) {
             findPath(root.getLeft(), expectedSum, currentSum, path);
         }
         if (root.getRight() != null) {
             findPath(root.getRight(), expectedSum, currentSum, path);
         }
         path.remove(path.size() - 1);
     }
 
     public static int findKth(final Node<Integer> root, int num) {
         Stack<Node<Integer>> stack = new Stack<>();
         Node<Integer> current = root;
         while (current != null || !stack.isEmpty()) {
             if (current != null) {
                 stack.push(current);
                 current = current.getLeft();
             } else {
                 current = stack.pop();
                 num--;
                 if (num == 0) {
                     return current.getValue();
                 }
                 current = current.getRight();
             }
         }
         return -1;
     }
 
     public static int findKth2(final Node<Integer> root, int num) {
         Stack<Node<Integer>> stack = new Stack<>();
         Node<Integer> current = root;
         while (true) {
             if (current != null) {
                 stack.push(current);
                 current = current.getLeft();
             } else {
                 if (stack.isEmpty()) {
                     break;
                 } else {
                     current = stack.pop();
                     num--;
                     if (num == 0) {
                         return current.getValue();
                     }
                     current = current.getRight();
                 }
             }
         }
 
         return -1;
     }
 
     public static void iterativeInOrder(final Node<Integer> root, final int num) {
         Stack<Node<Integer>> stack = new Stack<>();
         Node<Integer> current = root;
         while (true) {
             if (current != null) {
                 stack.push(current);
                 current = current.getLeft();
             } else {
                 if (stack.isEmpty()) {
                     break;
                 } else {
                     current = stack.pop();
                     System.out.printf("%d\n", current.getValue());
                     current = current.getRight();
                 }
             }
         }
 
     }
 
     // Inorder
     public static void findKthSmaller(final Node<Integer> root, final int[] k) {
         if (k[0] < 0 || root == null)
             return;
         findKthSmaller(root.getLeft(), k);
         k[0]--;
         if (k[0] == 0) {
             System.out.printf("%d\n", root.getValue());
             return;
         } else {
             findKthSmaller(root.getRight(), k);
         }
 
     }
 
     public static void zigzagTraversal(final Node<Integer> root) {
         LinkedList<Node<Integer>> level = new LinkedList<>();
         boolean leftToRight = true;
         level.add(root);
         while (true) {
             LinkedList<Node<Integer>> nextLevel = new LinkedList<>();
             for (Node<Integer> node : level) {
                 if (node.getLeft() != null) {
                     nextLevel.add(node.getLeft());
                 }
                 if (node.getRight() != null) {
                     nextLevel.add(node.getRight());
                 }
             }
             if (!leftToRight) {
                 Collections.reverse(level);
             }
             leftToRight = !leftToRight;
             for (Node<Integer> node : level) {
                 System.out.printf("%d ", node.getValue());
             }
             System.out.println();
             if (nextLevel.isEmpty()) {
                 return;
             } else {
                 level = nextLevel;
             }
         }
     }
 
     public static void mirrorTree(final Node<Integer> root) {
         if (root == null)
             return;
         mirrorTree(root.getLeft());
         mirrorTree(root.getRight());
         Node<Integer> temp = root.getRight();
         root.setRight(root.getLeft());
         root.setLeft(temp);
     }
 
     public static void morrisTraversal(final Node<Integer> root) {
         Node<Integer> current, pre;
         if (root == null)
             return;
         current = root;
         while (current != null) {
             if (current.getLeft() == null) {
                 System.out.printf(" %d ", current.getValue());
                 current = current.getRight();
             } else {
                 pre = current.getLeft();
                 while (pre.getRight() != null && pre.getRight() != current) {
                     pre = pre.getRight();
                 }
                 if (pre.getRight() == null) {
                     pre.setRight(current);
                     current = current.getLeft();
                 } else {
                     pre.setRight(null);
                     System.out.printf(" %d ", current.getValue());
                     current = current.getRight();
                 }
             }
         }
     }
 
     public static boolean isLeaf(final Node<Integer> n) {
         if (n.getLeft() == n.getRight() && n.getLeft() == null) {
             return true;
         }
         return false;
     }
 
     public static void printPaths(final Node<Integer> root, final LinkedList<Integer> path) {
         if (isLeaf(root)) {
             path.add(root.getValue());
             for (Iterator<Integer> iterator = path.iterator(); iterator.hasNext();) {
                 Integer v = iterator.next();
                 System.out.printf("%d ", v);
             }
             path.removeLast();
             System.out.println();
         } else {
             path.add(root.getValue());
             if (root.getLeft() != null)
                 printPaths(root.getLeft(), path);
             if (root.getRight() != null)
                 printPaths(root.getRight(), path);
             path.removeLast();
         }
     }
 
     public static boolean isSymmetrical(final Node<Integer> root) {
         return isSymmetrical(root, root);
     }
 
     private static boolean isSymmetrical(final Node<Integer> root, final Node<Integer> root2) {
         if (root == null && root2 == null) {
             return true;
         }
 
         if (root == null || root2 == null) {
             return false;
         }
         if (root.getValue() != root2.getValue())
             return false;
 
         return isSymmetrical(root.getLeft(), root2.getRight()) && isSymmetrical(root.getRight(), root2.getLeft());
     }
 
     public static int distance(final Node<Integer> a, final Node<Integer> b) {
         int depthA = 0;
         int depthB = 0;
         Node<Integer> n = a;
         while (n.getParent() != null) {
             n = n.getParent();
             depthA++;
         }
         n = a;
        while (n.getParent() != null) {
             n = n.getParent();
             depthB++;
         }
         Node<Integer> n1 = a;
         Node<Integer> n2 = b;
         int count = 0;
         while (depthA > depthB) {
             n1 = n1.getParent();
             depthA--;
             count++;
         }
         while (depthB > depthA) {
             n2 = n2.getParent();
             depthB--;
             count++;
         }
         while (n1 != n2) {
             n1 = n1.getParent();
             n2 = n2.getParent();
             count += 2;
         }
         return count;
     }
 
     /**
      * @param args
      */
     public static void main(final String[] args) {
         // int height = Math.abs(rand.nextInt()) % 20;
         // if (height < 10)
         // height += 10;
         // Node<Integer> root = createRandomTree(height);
         // System.out.printf("Height of tree=%d = %d\n", height(root), height);
         // root = null;
         // for (int i = 0; i < 200; i++) {
         // root = insertNodeBST(root, Math.abs(rand.nextInt()) % 200);
         // }
         // int i = Math.abs(rand.nextInt()) % 200;
         // int j = Math.abs(rand.nextInt()) % 200;
         // insertNodeBST(root, i);
         // insertNodeBST(root, j);
         // System.out.printf("Height of tree=%d = %d\n", height(root), height);
         // printPreOrder(root);
         // System.out.println();
         // printPreOrderIterative(root);
         // System.out.println();
         // printInOrder(root);
         // System.out.println();
         // lowestCommonAntecessor(root, i, j);
         // System.out.println();
         // Node<Integer> head = bstToDoubleLinky(root);
         // while(head!= null){
         // System.out.printf("%d ", head.getValue());
         // head = head.getRight();
         // }
         //
         // root = null;
         // for ( i = 0; i < 200; i++) {
         // root = insertNodeBST(root, Math.abs(rand.nextInt()) % 200);
         // }
         // printInOrder(root);
         // System.out.println();
         // System.out.println(getLeftWidth(root));
         // int sum[] = new int[100];
         // printVerticalSumOfTree(root, 10, sum);
         // for (int k = 0; k < sum.length; k++) {
         // System.out.printf("%d ", sum[k]);
         // }
         // System.out.println();
 
         Node<Integer> root = null;
         for (int i = 0; i < 20; i++) {
             root = insertNodeBST(root, Math.abs(rand.nextInt()) % 2000);
         }
         // findKthSmaller(root, new int[] { 199 });
         // find(root, 199);
         // System.out.println(findKth(root, 199));
         // System.out.println(findKth2(root, 199));
         // printTreeLevels(root, 3);
         // System.out.println();
         // ArrayList<LinkedList<Node<Integer>>> levels = getTreeLevels(root);
         // for (LinkedList<Node<Integer>> linkedList : levels) {
         // for (Node<Integer> node : linkedList) {
         // System.out.printf("%d ", node.getValue());
         // }
         // System.out.println();
         // }
         // zigzagTraversal(root);
         printPaths(root, new LinkedList<Integer>());
         findPath(root, 20000);
         // printInOrder(root);
 
     }
 
 }
