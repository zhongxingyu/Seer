 package fi.helsinki.coderodde.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.RandomAccess;
 
 /**
  *
  * @author rodionefremov
  */
 public class MyTreeList<E> implements List<E>, RandomAccess {
 
     public static final int ORIGINAL = -1;
 
     /**
      * The maximum amount of elements per a node.
      */
     private final int degree;
 
     /**
      * The root node, never null;
      */
     protected Node<E> root;
 
     /**
      * The total amount of elements in this <code>TreeList</code>.
      */
     private int size;
 
     /**
      * The modification count.
      */
     private long modCount;
 
     /**
      * The last (rightmost) node of this <code>TreeList</code>.
      */
     private Node<E> last;
 
     /**
      * Maps an element to list of nodes containing the element.
      */
     private final Map<E, List<Node<E>>> map;
 
     /**
      * The list of all list views.
      */
     private final List<ListView> listViews;
 
     /**
      * The lowest list index in a parent list.
      */
     protected final int firstViewIndexInParentList;
 
     protected MyTreeList<E> parent;
 
     public MyTreeList(final int degree) {
         this(degree, ORIGINAL);
     }
 
     MyTreeList(final int degree, final int firstViewIndexInParentList) {
         checkDegree(degree);
         checkFirstIndex(firstViewIndexInParentList);
         this.degree = degree;
         this.listViews = new ArrayList<ListView>();
         this.map = new HashMap<E, List<Node<E>>>();
         this.root = new Node<E>();
         this.last = this.root;
         this.firstViewIndexInParentList = firstViewIndexInParentList;
     }
 
     /**
      * Return the size of this list.
      *
      * @return the size of this list.
      */
     @Override
     public int size() {
         return size;
     }
 
     /**
      * Returns a boolean indicating a possible emptyness.
      *
      * @return <code>true</code> is this <code>TreeList</code> is empty,
      * <code>false</code> otherwise.
      */
     @Override
     public boolean isEmpty() {
         return size == 0;
     }
 
     /**
      * Queries existence of an element in this tree.
      *
      * @param o the element to query.
      * @return <code>true</code> if this list contains <code>o</code>,
      * <code>false</code> otherwise.
      */
     @Override
     public boolean contains(Object o) {
         return indexOf(o) >= 0;
     }
 
     /**
      * Returns an iterator over this list.
      *
      * @return an iterator over this list.
      */
     public Iterator<E> iterator() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /**
      * Dumps this list to an array.
      *
      * @return list containing all the elements in the same order as they appear
      * in the list.
      */
     public Object[] toArray() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /**
      * Dumps this list to an array.
      *
      * @return list containing all the elements in the same order as they appear
      * in the list.
      */
     public <T> T[] toArray(T[] a) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /**
      * Appends an element to the end of this list.
      *
      * @param e the element to append.
      * @return always <code>true</code>.
      */
     public boolean add(E e) {
         size++;
         modCount++;
         if (firstViewIndexInParentList == ORIGINAL) {
             if (last.size() == MyTreeList.this.degree) {
                 // Complexity: O(log N).
                 Node<E> node = new Node<E>();
                 node.add(0, e);
                 last.right = node;
                 node.parent = last;
                 last = node;
                 fixTreeAfterAddition(node);
                 fixLeftCountersAfterInsertion(node);
             } else {
                 // Complexity: O(1).
                 last.add(last.size(), e);
             }
 
             for (int i = 0, end = listViews.size(); i < end; i++) {
                 ListView view = listViews.get(0);
                 if (view.getLowestActualIndex() <= size
                         && size < view.getLowestActualIndex() + view.size()) {
                     view.size++;
                 }
             }
             if (map.containsKey(e)) {
                 List<Node<E>> nodes = this.map.get(e);
                 if (nodes.get(nodes.size() - 1) != last) {
                     nodes.add(last);
                 }
             } else {
                 List<Node<E>> list = new ArrayList<Node<E>>(1);
                 list.add(last);
                 map.put(e, list);
             }
             return true;
         } else {
             parent.add(firstViewIndexInParentList + size() - 1, e);
             return true;
         }
     }
 
     /**
      * Removes the first occurrence of an element.
      * @param o
      * @return
      */
     public boolean remove(Object o) {
         List<Node<E>> nodes = map.get(o);
         if (nodes == null) {
             return false;
         }
 
         return true;
     }
 
     public boolean containsAll(Collection<?> c) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public boolean addAll(Collection<? extends E> c) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public boolean addAll(int index, Collection<? extends E> c) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public boolean removeAll(Collection<?> c) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public boolean retainAll(Collection<?> c) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void clear() {
         modCount++;
         size = 0;
         root.left = null;
         root.right = null;
         root.leftCount = 0;
         root.firstIndex = 0;
         root.lastIndex = 0;
         root.height = 0;
 
         for (int i = 0, end = listViews.size(); i < end; i++) {
             listViews.get(i).clear();
         }
     }
 
     public E get(int index) {
         if (this.firstViewIndexInParentList == ORIGINAL) {
             checkIndex(index);
             if (isEmpty()) {
                 return null;
             }
             Node<E> node = root;
             for (;;) {
                 if (index >= node.leftCount + node.size()) {
                     index -= node.leftCount + node.size();
                     node = node.right;
                 } else if (index < node.leftCount) {
                     node = node.left;
                 } else {
                     return node.get(index - node.leftCount);
                 }
             }
         } else {
             return parent.get(this.firstViewIndexInParentList + index);
         }
     }
 
     public E set(int index, E element) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void add(int index, E element) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public E remove(int index) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public int lastIndexOf(Object o) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public ListIterator<E> listIterator() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public ListIterator<E> listIterator(int index) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public List<E> subList(int fromIndex, int toIndex) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public String toString() {
         return "[TreeList2, size: " + size() + "]";
     }
 
     public boolean isHealthy() {
         return !hasCycles()
                 && heightFieldsOK()
                 && isBalanced()
                 && isWellIndexed();
     }
 
     protected int getLowestActualIndex() {
         return 0;
     }
 
     private void checkDegree(int degree) {
         if (degree < 1) {
             throw new IllegalArgumentException("Faulty degree passed: " + degree);
         }
     }
 
     @Override
     public int indexOf(final Object o) {
         List<Node<E>> list = map.get(o);
         if (list == null) {
             return -1;
         }
         Node<E> node = list.get(0);
         int index = node.indexOf(o);
 
         while (node != null) {
             if (node.parent != null && node.parent.right == node) {
                 index += node.size() + node.leftCount;
             }
             node = node.parent;
         }
         return index;
     }
 
     /**
      * Rebalances this tree if needed.
      *
      * @param node
      */
     private void fixTreeAfterAddition(Node<E> node) {
         Node<E> p = node;
         Node<E> pp;
 
         while (p != null) {
             if (h(p.left) == h(p.right) + 2) {
                 pp = p.parent;
                 Node<E> subroot =
                         h(p.left.left) >= h(p.left.right) ?
                         rightRotate(p) :
                         leftRightRotate(p);
 
                 if (pp == null) {
                     root = subroot;
                 } else if (pp.left == p) {
                     pp.left = subroot;
                 } else {
                     pp.right = subroot;
                 }
 
                 if (pp != null) {
                     pp.height = Math.max(h(pp.left), h(pp.right)) + 1;
                 }
             } else if (h(p.left) + 2 == h(p.right)) {
                 pp = p.parent;
                 Node<E> subroot =
                         h(p.right.right) >= h(p.right.left) ?
                         leftRotate(p) :
                         rightLeftRotate(p);
 
                 if (pp == null) {
                     root = subroot;
                 } else if (pp.left == p) {
                     pp.left = subroot;
                 } else {
                     pp.right = subroot;
                 }
 
                 if (pp != null) {
                     pp.height = Math.max(h(p.left), h(p.right)) + 1;
                 }
             }
 
             p.height = Math.max(h(p.left), h(p.right)) + 1; // set the height.
             p = p.parent; // move one edge upwards.
         }
     }
 
     private void fixTreeAfterRemoval(Node<E> node) {
 
     }
 
     /**
      * Fixes the left counters on the path to root.
      *
      * @param node the node in which an insertion was performed.
      */
     private void fixLeftCountersAfterInsertion(Node<E> node) {
         while (node.parent != null) {
             if (node.parent.left == node) {
                 node.parent.leftCount++;
             }
             node = node.parent;
         }
     }
 
     private int h(Node<E> node) {
         return node == null ? -1 : node.height;
     }
 
     private Node<E> leftRotate(Node<E> e) {
         Node<E> ee = e.right;
         ee.parent = e.parent;
         e.parent = ee;
         e.right = ee.left;
         ee.left = e;
 
         if (e.right != null) {
             e.right.parent = e;
         }
 
         e.height = Math.max(h(e.left), h(e.right)) + 1;
         ee.height = Math.max(h(ee.left), h(ee.right)) + 1;
 
         ee.leftCount += e.leftCount + e.size();
         return ee;
     }
 
     private Node<E> rightRotate(Node<E> e) {
         Node<E> ee = e.left;
         ee.parent = e.parent;
         e.parent = ee;
         e.left = ee.right;
         ee.right = e;
 
         if (e.left != null) {
             e.left.parent = e;
         }
 
         e.height = Math.max(h(e.left), h(e.right)) + 1;
         ee.height = Math.max(h(ee.left), h(ee.right)) + 1;
 
         e.leftCount -= ee.leftCount + ee.size();
         return ee;
     }
 
     private Node<E> leftRightRotate(Node<E> e) {
         Node<E> ee = e.left;
         e.left = leftRotate(ee);
         return rightRotate(e);
     }
 
     private Node<E> rightLeftRotate(Node<E> e) {
         Node<E> ee = e.right;
         e.right = rightRotate(ee);
         return leftRotate(e);
     }
 
     private boolean hasCycles(Node<E> e, java.util.HashSet<Node<E>> set) {
         if (e == null) {
             return false;
         }
         if (set.contains(e)) {
             return true;
         }
         set.add(e);
         if (hasCycles(e.left, set)) {
             return true;
         }
         if (hasCycles(e.right, set)) {
             return true;
         }
         set.remove(e);
         return false;
     }
 
     private int checkHeight(Node<E> e) {
         if (e == null) {
             return -1;
         }
 
         int l = checkHeight(e.left);
 
         if (l == Integer.MIN_VALUE) {
             return Integer.MIN_VALUE;
         }
 
         int r = checkHeight(e.right);
 
         if (r == Integer.MIN_VALUE) {
             return Integer.MIN_VALUE;
         }
 
         int h = Math.max(l, r) + 1;
 
         if (h != e.height) {
             return Integer.MIN_VALUE;
         } else {
             return h;
         }
     }
 
     private boolean isBalanced(Node<E> e) {
         if (e == null) {
             return true;
         }
 
         if (Math.abs(h(e.left) - h(e.right)) > 1) {
             return false;
         }
 
         if (isBalanced(e.left) == false) {
             return false;
         }
 
         if (isBalanced(e.right) == false) {
             return false;
         }
 
         return true;
     }
 
     private int countLeft(Node<E> e) {
         if (e == null) {
             return 0;
         }
 
         int l;
         int r;
 
         if ((l = countLeft(e.left)) != e.leftCount) {
             return Integer.MIN_VALUE;
         }
 
         if ((r = countLeft(e.right)) == Integer.MIN_VALUE) {
             return Integer.MIN_VALUE;
         }
 
         return l + r + e.size();
     }
 
     private boolean hasCycles() {
         return hasCycles(root, new java.util.HashSet<Node<E>>());
     }
 
     private boolean heightFieldsOK() {
         return checkHeight(root) == root.height;
     }
 
     private boolean isBalanced() {
         return isBalanced(root);
     }
 
     private boolean isWellIndexed() {
         return root.leftCount == countLeft(root.left)
                && root.right != null ? root.right.leftCount == countLeft(root.right) : true;
     }
 
     private void checkFirstIndex(int firstViewIndexInParentList) {
         if (firstViewIndexInParentList < ORIGINAL) {
             throw new IllegalArgumentException(
                     "Bad firstViewIndexInParentList: "
                     + firstViewIndexInParentList);
         }
     }
 
     private void checkIndex(int index) {
         if (index > 0) {
 
         }
     }
 
     private class Node<E> {
 
         /**
          * The internal storage arrays.
          */
         Object[] array;
 
         /**
          * The total amount of elements in the left subtree.
          */
         int leftCount;
 
         /**
          * The index of the first element in <code>array</code>.
          */
         int firstIndex;
 
         /**
          * The index of the last element in <code>array</code>.
          */
         int lastIndex;
 
         /**
          * The left subtree.
          */
         Node<E> left;
 
         /**
          * The right subtree.
          */
         Node<E> right;
 
         /**
          * The parent node.
          */
         Node<E> parent;
 
         /**
          * The height of this node.
          */
         int height;
 
         /**
          * Returns the amount of elements in this node.
          * @return the amount of elements in this node.
          */
         int size() {
             return lastIndex - firstIndex;
         }
 
         Node() {
             array = new Object[MyTreeList.this.degree];
         }
 
         /**
          * Assigns the element to the local node localIndex <code>localIndex</code>.
          * @param localIndex the local localIndex within this <code>Node</code>'s array.
          * @param element the element to set.
          */
         void add(int localIndex, E element) {
             final int elementsBefore = localIndex;
             final int elementsAfter = lastIndex - this.size() - localIndex;
 
             if (elementsBefore < elementsAfter && firstIndex > 0) {
                 // shift preceding elements to the left.
                 for (int i = firstIndex; i < firstIndex + elementsBefore; i++) {
                     array[i - 1] = array[i];
                 }
                 firstIndex--;
                 array[firstIndex + elementsBefore] = element;
             } else {
                 // shift consequent elements to the right.
                 for (int i = lastIndex; i > lastIndex - elementsAfter; i--) {
                     array[i] = array[i - 1];
                 }
                 array[firstIndex + elementsBefore] = element;
             }
 
             lastIndex++;
         }
 
         /**
          * Sets the new value at index <code>localIndex</code>.
          *
          * @param localIndex the index in this node to set at.
          * @param newValue the new value.
          * @return old value.
          */
         E set(int localIndex, E newValue) {
             E old = (E) array[firstIndex + localIndex];
             array[firstIndex + localIndex] = newValue;
             return old;
         }
 
         /**
          * Get an element at <code>localIndex</code>.
          *
          * @param localIndex
          * @return
          */
         E get(int localIndex) {
             return (E) array[firstIndex + localIndex];
         }
 
         /**
          * Removes an element from this node.
          *
          * @param localIndex
          */
         void removeAt(int localIndex) {
             final int elementsBefore = localIndex - firstIndex;
             final int elementsAfter = lastIndex - localIndex - 1;
 
             if (elementsBefore < elementsAfter) {
                 // shift preceding elements to the right.
                 for (int i = firstIndex + localIndex; i > firstIndex; i--) {
                     array[i] = array[i - 1];
                 }
                 array[firstIndex] = null;
                 firstIndex++;
             } else {
                 // shift subsequent elements to the left.
                 for (int i = lastIndex - 1; i > lastIndex - elementsAfter; i--) {
                     array[i - 1] = array[i];
                 }
                 array[lastIndex - 1] = null;
                 lastIndex--;
             }
         }
 
         int indexOf(final Object o) {
             if (size() > 0) {
                 for (int i = firstIndex; i < lastIndex; i++) {
                     if (o.equals(array[i])) {
                         return i - firstIndex;
                     }
                 }
             }
             return -1;
         }
     }
 
     private class ListView extends MyTreeList<E> {
 
         /**
          * The tree list this <code>ListView</code> follows.
          */
         private final MyTreeList<E> parent;
 
         /**
          * The size of this view.
          */
         private int size;
 
         /**
          * Creates a new list view.
          *
          * @param parent the parent list.
          * @param degree the degree of this view list.
          * @param firstIndex the first index in parent.
          */
         ListView(final MyTreeList<E> parent,
                  final int degree,
                  final int firstIndex,
                  final int size) {
             super(degree, firstIndex);
             this.parent = parent;
             this.size = size;
         }
 
         @Override
         public void clear() {
             this.size = 0;
         }
 
         @Override
         public int size() {
             return size;
         }
 
         @Override
         public boolean isEmpty() {
             return size == 0;
         }
 
         @Override
         protected int getLowestActualIndex() {
             return this.firstViewIndexInParentList
                    + parent.getLowestActualIndex();
         }
     }
 }
