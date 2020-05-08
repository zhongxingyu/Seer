 package cz.cuni.mff.peckam.java.origamist.utils;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.AbstractCollection;
 import java.util.AbstractMap;
 import java.util.AbstractSet;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.SortedSet;
 
 /**
  * A red-black tree implementation.
  * 
  * Inspired by the Sun JRE's implementation.
  * 
  * @author Martin Pecka
  */
 public class RedBlackTree<K, V> extends AbstractMap<K, V> implements SortedMap<K, V>, Cloneable, Serializable
 {
 
     /** */
     private static final long             serialVersionUID = 919286545866124006L;
 
     /** The comparator to be used for comparing the keys. */
     protected final Comparator<? super K> comparator;
 
     /** The root of the tree. */
     protected transient Entry             root             = null;
 
     /** Number of elements of the tree. */
     protected transient int               size             = 0;
 
     /** Number of modifications, used for detecting concurrent modifications while using iterators. */
     protected transient int               modCount         = 0;
 
     /**
      * Colors of the tree's nodes.
      * 
      * @author Martin Pecka
      */
     protected enum Color
     {
         RED, BLACK
     };
 
     /**
      * Construct a new red-black tree with the default comparator. The default comparator needs all values inserted into
      * or deleted from the tree to implement {@link Comparable}&lt;K&gt;. If an element doesn't implement it, an insert
      * or delete method will end up throwing a {@link ClassCastException}.
      */
     public RedBlackTree()
     {
         comparator = getDefaultComparator();
     }
 
     /**
      * Construct a new red-black tree using the given comparator. The comparator must be able to compare every two
      * "valid" keys, and must throw a {@link ClassCastException} if an "invalid" key is to be compared.
      * 
      * @param comparator The comparator to use for comparing this tree's element's keys.
      */
     public RedBlackTree(Comparator<? super K> comparator)
     {
         this.comparator = comparator;
     }
 
     /**
      * Create a new red-black tree with entries from the given map. Use the default comparator to compare the keys. The
      * keys must implement {@link Comparable}&lt;K&gt;.
      * 
      * @param m The map to take entries from.
      * 
      * @throws ClassCastException If a key from the given map doesn't implement the {@link Comparable}&lt;K&gt;
      *             interface.
      * @throws NullPointerException If <code>m == null</code> or if the map contains a <code>null</code> key and the
      *             comparator doesn't support it.
      */
     public RedBlackTree(Map<? extends K, ? extends V> m)
     {
         this();
         putAll(m);
     }
 
     /**
      * Create a new red-black tree with entries from the given sorted map. The map's comparator is used. This is an
      * effective (linear time) algorithm.
      * 
      * @param m The sorted map to take entries from.
      */
     public RedBlackTree(SortedMap<K, ? extends V> m)
     {
         this(m.comparator());
         buildFromSorted(m.size(), m.entrySet().iterator());
     }
 
     /**
      * @return The number of entries in this tree.
      */
     @Override
     public int size()
     {
         return size;
     }
 
     /**
      * Test if the tree contains an entry with the given key.
      * 
      * Runs in logarithmic time (or linear to the tree's height).
      * 
      * @throws ClassCastException If the specified key cannot be compared with the keys currently in the map
      * @throws NullPointerException If the specified key is <code>null</code> and this tree's comparator does not permit
      *             <code>null</code> keys.
      */
     @SuppressWarnings("unchecked")
     @Override
     public boolean containsKey(Object key)
     {
         return getEntry((K) key) != null;
     }
 
     /**
      * Test if the tree contains an entry with the given value.
      * 
      * Runs in time linear to the size of the tree (ineffective).
      */
     @SuppressWarnings("unchecked")
     @Override
     public boolean containsValue(Object value)
     {
         if (root == null)
             return false;
         return getPathForValue((V) value).size() > 0;
     }
 
     /**
      * Return the value associated to the given key.
      * 
      * Runs in logarithmic time (or linear to the tree's height).
      * 
      * @throws ClassCastException if the specified key cannot be compared with the keys currently in the map.
      * @throws NullPointerException If the specified key is <code>null</code> and this tree's comparator does not permit
      *             <code>null</code> keys.
      */
     @Override
     public V get(Object key)
     {
         @SuppressWarnings("unchecked")
         Entry e = getEntry((K) key);
         return (e == null ? null : e.value);
     }
 
     /**
      * @return The comparator used for comparing keys.
      */
     @Override
     public Comparator<? super K> comparator()
     {
         return comparator;
     }
 
     /**
      * Return the lowest key in the tree.
      * 
      * Runs in logarithmic time (or linear to the tree's height).
      * 
      * @throws NoSuchElementException {@inheritDoc}
      */
     @Override
     public K firstKey()
     {
         Entry e = getFirstEntry();
         if (e == null)
             throw new NoSuchElementException("No first key was found.");
         return e.getKey();
     }
 
     /**
      * Return the highest key in the tree.
      * 
      * Runs in logarithmic time (or linear to the tree's height).
      * 
      * @throws NoSuchElementException {@inheritDoc}
      */
     public K lastKey()
     {
         Entry e = getLastEntry();
         if (e == null)
             throw new NoSuchElementException("No last key was found.");
         return e.getKey();
     }
 
     /**
      * Puts all entries from the given map into this tree.
      * 
      * If the provided map is a {@link SortedMap} and the tree is empty, a linear-time algorithm is used, othwerwise
      * this operations takes O(n*log n) time.
      * 
      * @throws ClassCastException If a key or value has invalid class.
      * @throws NullPointerException If the specified map is <code>null</code> or the specified map contains a
      *             <code>null</code> key and this tree does not permit <code>null</code> keys.
      */
     public void putAll(Map<? extends K, ? extends V> map)
     {
         if (size == 0 && map.size() != 0 && map instanceof SortedMap<?, ?>) {
             Comparator<?> c = ((SortedMap<? extends K, ? extends V>) map).comparator();
             if (comparator.equals(c)) {
                 ++modCount;
                 buildFromSorted(map.size(), map.entrySet().iterator());
                 return;
             }
         }
         super.putAll(map);
     }
 
     /**
      * Return the entry for the given key. If the key is not found, return <code>null</code>.
      * 
      * Runs in logarithmic time (or linear to the tree's height).
      * 
      * @param key The key to find the entry for.
      * @return The entry for the given key. If the key is not found, return <code>null</code>.
      * 
      * @throws NullPointerException If key is <code>null</code> and this tree does not permit <code>null</code> keys.
      */
     protected Entry getEntry(K key)
     {
         Entry p = root;
         while (p != null) {
             int cmp = comparator.compare(key, p.key);
             if (cmp < 0)
                 p = p.left;
             else if (cmp > 0)
                 p = p.right;
             else
                 return p;
         }
         return null;
     }
 
     /**
      * Returns the first entry in the tree (according to the comparator).
      * 
      * @return The entry with the lowest key. <code>null</code> if the tree is empty.
      */
     protected Entry getFirstEntry()
     {
         Entry p = root;
         if (p != null)
             while (p.left != null)
                 p = p.left;
         return p;
     }
 
     /**
      * Returns the last entry in the tree (according to the comparator).
      * 
      * @return The entry with the highest key. <code>null</code> if the tree is empty.
      */
     protected Entry getLastEntry()
     {
         Entry p = root;
         if (p != null)
             while (p.right != null)
                 p = p.right;
         return p;
     }
 
     /**
      * Return the path to the given key. If this tree doesn't contain the given key, the path will end with the entry
      * the key would be a child of.
      * 
      * Runs in logarithmic time (or linear to the tree's height).
      * 
      * @param key The key to find path for.
      * @return Return the path to the given key. If this tree doesn't contain the given key, the path will end with the
      *         entry the key would be a child of.
      */
     protected TreePath getPath(K key)
     {
         TreePath path = new TreePath();
         Entry p = root;
         while (p != null) {
             path.addLast(p);
             int cmp = comparator.compare(key, p.key);
             if (cmp < 0)
                 p = p.left;
             else if (cmp > 0)
                 p = p.right;
             else
                 break;
         }
         return path;
     }
 
     /**
      * Return the path to the given value. If this tree doesn't contain the given value, empty path will be returned.
      * 
      * Runs in time linear to the size of this tree (ineffective).
      * 
      * @param value The value to find path for.
      * @return The path to the given value. If this tree doesn't contain the given value, empty path will be returned.
      */
     protected TreePath getPathForValue(V value)
     {
         TreePath path = new TreePath();
         path.add(root);
 
         if (searchPathForValue(value, path)) {
             return path;
         } else {
             return new TreePath();
         }
     }
 
     /**
      * Return whether the subtree defined by the last entry of the given path contains the given value. If it contains,
      * <code>path</code> will contain the path to the value's entry, otherwise it will be left unchanged.
      * 
      * @param value The value to find path for.
      * @param path The path that defines the subtree to search within.
      * @return Return whether the subtree defined by the last entry of the given path contains the given value.
      */
     protected boolean searchPathForValue(V value, TreePath path)
     {
         Entry p = path.getLast();
         if (p.value.equals(value)) {
             return true;
         } else {
             if (p.left != null) {
                 path.addLast(p.left);
                 if (searchPathForValue(value, path)) {
                     return true;
                 }
                 path.removeLast();
             }
             if (p.right != null) {
                 path.addLast(p.right);
                 if (searchPathForValue(value, path)) {
                     return true;
                 }
                 path.removeLast();
             }
         }
         return false;
     }
 
     /**
      * Return an unmodifiable view of the entry for the given key.
      * 
      * @param key The key to find the entry for.
      * @return The entry for the given key. If the key is not found, return <code>null</code>.
      * 
      * @throws NullPointerException If key is <code>null</code> and this tree does not permit <code>null</code> keys.
      */
     public Map.Entry<K, V> getEntryForKey(K key)
     {
         return exportEntry(getEntry(key));
     }
 
     /**
      * Put the key and value in the tree.
      * 
      * Runs in logarithmic time (or linear to the tree's height).
      * 
      * @throws ClassCastException If the specified key cannot be compared with the keys currently in the map.
      * @throws NullPointerException If the specified key is <code>null</code> and this tree's comparator does not permit
      *             <code>null</code> keys.
      */
     @Override
     public V put(K key, V value)
     {
         if (root == null) {
             comparator.compare(key, key); // throws NPE if the comparator doesn't support null
             root = new Entry(key, value);
             size = 1;
             modCount++;
             return null;
         }
 
         TreePath path = getPath(key);
         // if we found the key, just change its associated value
         if (path.endsWithKey(key))
             return path.getLast().setValue(value);
 
         int cmp = comparator.compare(key, path.getLast().getKey());
         // now we have the new entry's parent as the last item on the path
         Entry e = new Entry(key, value);
         if (cmp < 0)
             path.getLast().left = e;
         else
             path.getLast().right = e;
 
         path.addLast(e);
 
         repairTreeAfterInsert(path);
 
         size++;
         modCount++;
         return null;
     }
 
     /**
      * Removes the entry with the given key, if it exists in the tree.
      * 
      * Runs in logarithmic time (or linear to the tree's height).
      * 
      * @throws ClassCastException if the specified key cannot be compared with the keys currently in the map.
      * @throws NullPointerException If the specified key is <code>null</code> and this tree's comparator does not permit
      *             <code>null</code> keys.
      */
     @Override
     public V remove(Object key)
     {
         @SuppressWarnings("unchecked")
         K k = (K) key;
         TreePath path = getPath(k);
         if (!path.endsWithKey(k))
             return null;
 
         V oldValue = path.getLast().value;
         deleteLastPathEntry(path);
         return oldValue;
     }
 
     @Override
     public void clear()
     {
         modCount++;
         size = 0;
         root = null;
     }
 
     /**
      * Repair the tree structure after insert.
      * 
      * @param path The path to the newly added entry.
      */
     protected void repairTreeAfterInsert(RedBlackTree<K, V>.TreePath path)
     {
         path.getLast().color = Color.RED;
 
         while (path.size() > 1 && path.getLast(1).color == Color.RED) {
             if (path.getLast(1) == leftOf(path.getLast(2))) {
                 Entry y = rightOf(path.getLast(2));
                 if (colorOf(y) == Color.RED) {
                     setColor(path.getLast(2), Color.BLACK);
                     setColor(y, Color.BLACK);
                     setColor(path.getLast(2), Color.RED);
                     path.removeLast();
                     path.removeLast();
                 } else {
                     if (path.getLast() == rightOf(path.getLast(1))) {
                         path.removeLast();
                         path.rotateLeft();
                     }
                     setColor(path.getLast(1), Color.BLACK);
                     setColor(path.getLast(2), Color.RED);
                     path.removeLast();
                     path.removeLast();
                     path.rotateRight();
                 }
             } else {
                 Entry y = leftOf(path.getLast(2));
                 if (colorOf(y) == Color.RED) {
                     setColor(path.getLast(1), Color.BLACK);
                     setColor(y, Color.BLACK);
                     setColor(path.getLast(2), Color.RED);
                     path.removeLast();
                     path.removeLast();
                 } else {
                     if (path.getLast() == leftOf(path.getLast(1))) {
                         path.removeLast();
                         path.rotateRight();
                     }
                     setColor(path.getLast(1), Color.BLACK);
                     setColor(path.getLast(2), Color.RED);
                     path.removeLast();
                     path.removeLast();
                     path.rotateLeft();
                 }
             }
         }
         root.color = Color.BLACK;
     }
 
     /**
      * Delete the path's last entry and repair the tree. The path will be returned in an undetermined state.
      * 
      * @param path The path to the entry to be deleted.
      */
     protected void deleteLastPathEntry(RedBlackTree<K, V>.TreePath path)
     {
         modCount++;
         size--;
 
         Entry p = path.getLast();
         // If p has 2 children, copy successor's element to p and then make p point to successor.
         if (p.left != null && p.right != null) {
             path.moveToSuccesor();
             Entry s = path.getLast();
             p.key = s.key;
             p.value = s.value;
             p = s;
         }
 
         // now we can be sure that p has at most one non-null child
 
         // Start fixup at replacement node, if it exists.
         Entry replacement = (p.left != null ? p.left : p.right);
 
         if (replacement != null) {
             if (path.size() == 1)
                 root = replacement;
             else if (p == path.getLast(1).left)
                 path.getLast(1).left = replacement;
             else
                 path.getLast(1).right = replacement;
 
             path.removeLast();
             path.addLast(replacement);
 
             // Fix replacement
             if (p.color == Color.BLACK)
                 repairTreeAfterDeletion(path);
         } else if (path.size() == 1) { // return if we are the only node.
             root = null;
         } else { // No children. Use self as phantom replacement and unlink.
             if (p.color == Color.BLACK)
                 repairTreeAfterDeletion(path);
 
             if (path.size() > 1) {
                 if (p == path.getLast(1).left)
                     path.getLast(1).left = null;
                 else if (p == path.getLast(1).right)
                     path.getLast(1).right = null;
                 path.removeLast();
             }
         }
     }
 
     /**
      * Repair the tree structure after deletion.
      * 
      * @param path Path points to the entry used as replacement (or the deleted entry itself if it has no children).
      *            After the repair is complete, path leads to the entry with the same key as it led before calling this
      *            procedure.
      */
     protected void repairTreeAfterDeletion(RedBlackTree<K, V>.TreePath path)
     {
         boolean setColor = false;
 
         Entry toDelete = path.getLast();
 
         while (path.size() > 1 && colorOf(path.getLast()) == Color.BLACK) {
             if (path.getLast() == leftOf(path.getLast(1))) {
                 Entry sib = rightOf(path.getLast(1));
 
                 if (colorOf(sib) == Color.RED) {
                     setColor(sib, Color.BLACK);
                     setColor(path.getLast(1), Color.RED);
                     Entry p = path.removeLast();
                     path.rotateLeft(); // p still remains the left child of its original parent
                     path.addLast(p);
                     sib = rightOf(path.getLast(1));
                 }
 
                 if (colorOf(leftOf(sib)) == Color.BLACK && colorOf(rightOf(sib)) == Color.BLACK) {
                     setColor(sib, Color.RED);
                     path.removeLast();
                     setColor = true;
                 } else {
                     if (colorOf(rightOf(sib)) == Color.BLACK) {
                         setColor(leftOf(sib), Color.BLACK);
                         setColor(sib, Color.RED);
                         Entry p = path.removeLast();
                         path.addLast(sib);
                         path.rotateRight(); // rotate p's sibling
                         path.removeLast(); // and let path lead to p again
                         path.removeLast();
                         path.addLast(p);
                         sib = rightOf(path.getLast(1));
                     }
                     setColor(sib, colorOf(path.getLast(1)));
                     setColor(path.getLast(1), Color.BLACK);
                     setColor(rightOf(sib), Color.BLACK);
                     path.removeLast();
                     path.rotateLeft();
                     setColor(root, Color.BLACK);
                     setColor = false;
                     break;
                 }
             } else { // symmetric
                 Entry sib = leftOf(path.getLast(1));
 
                 if (colorOf(sib) == Color.RED) {
                     setColor(sib, Color.BLACK);
                     setColor(path.getLast(1), Color.RED);
                     Entry p = path.removeLast();
                     path.rotateRight();
                     path.addLast(p); // p still remains the left child of its original parent
                     sib = leftOf(path.getLast(1));
                 }
 
                 if (colorOf(rightOf(sib)) == Color.BLACK && colorOf(leftOf(sib)) == Color.BLACK) {
                     setColor(sib, Color.RED);
                     path.removeLast();
                     setColor = true;
                 } else {
                     if (colorOf(leftOf(sib)) == Color.BLACK) {
                         setColor(rightOf(sib), Color.BLACK);
                         setColor(sib, Color.RED);
                         Entry p = path.removeLast();
                         path.addLast(sib);
                         path.rotateLeft(); // rotate p's sibling
                         path.removeLast(); // and let path lead to p again
                         path.removeLast();
                         path.addLast(p);
                         sib = leftOf(path.getLast(1));
                     }
                     setColor(sib, colorOf(path.getLast(1)));
                     setColor(path.getLast(1), Color.BLACK);
                     setColor(leftOf(sib), Color.BLACK);
                     path.removeLast();
                     path.rotateRight();
                     setColor(root, Color.BLACK);
                     setColor = false;
                     break;
                 }
             }
         }
 
         if (setColor)
             setColor(path.getLast(), Color.BLACK);
 
         // return to the initial endpoint
 
         boolean goHigher = comparator.compare(toDelete.getKey(), path.getLast().getKey()) >= 0;
        while (path.size() > 0 && !path.endsWithKey(toDelete.getKey())) {
             if (goHigher)
                 path.moveToSuccesor();
             else
                 path.moveToPredecessor();
         }
     }
 
     /**
      * Linear time tree building algorithm from sorted data.
      * 
      * It is assumed that the comparator of the tree is already set prior to calling this method.
      * 
      * @param size The size of the built tree.
      * @param it New entries are created from entries from this iterator.
      */
     protected void buildFromSorted(int size, Iterator<?> it)
     {
         this.size = size;
         root = buildFromSorted(0, 0, size - 1, computeRedLevel(size), it);
     }
 
     /**
      * Find the level down to which to assign all nodes BLACK. This is the last `full' level of the complete binary tree
      * produced by buildTree. The remaining nodes are colored RED. (This makes a `nice' set of color assignments wrt
      * future insertions.) This level number is computed by finding the number of splits needed to reach the zeroeth
      * node. (The answer is ~lg(N), but in any case must be computed by same quick O(lg(N)) loop.)
      */
     protected int computeRedLevel(int sz)
     {
         int level = 0;
         for (int m = sz - 1; m >= 0; m = m / 2 - 1)
             level++;
         return level;
     }
 
     /**
      * Recursive "helper method" that does the real work of the previous method.
      * 
      * It is assumed that the comparator and size fields of the RedBlackTree are already set prior to calling this
      * method. (It ignores both fields.)
      * 
      * @param level The current level of tree. Initial call should be 0.
      * @param lo The first element index of this subtree. Initial should be 0.
      * @param hi The last element index of this subtree. Initial should be size-1.
      * @param redLevel The level at which nodes should be red. Must be equal to computeRedLevel for tree of this size.
      * @param it New entries are created from entries from this iterator.
      */
     private final Entry buildFromSorted(int level, int lo, int hi, int redLevel, Iterator<?> it)
     {
         /*
          * Strategy: The root is the middlemost element. To get to it, we
          * have to first recursively construct the entire left subtree,
          * so as to grab all of its elements. We can then proceed with right
          * subtree.
          * 
          * The lo and hi arguments are the minimum and maximum
          * indices to pull out of the iterator or stream for current subtree.
          * They are not actually indexed, we just proceed sequentially,
          * ensuring that items are extracted in corresponding order.
          */
 
         if (hi < lo)
             return null;
 
         int mid = (lo + hi) / 2;
 
         Entry left = null;
         if (lo < mid)
             left = buildFromSorted(level + 1, lo, mid - 1, redLevel, it);
 
         K key;
         V value;
         @SuppressWarnings("unchecked")
         Map.Entry<K, V> entry = (java.util.Map.Entry<K, V>) it.next();
         key = entry.getKey();
         value = entry.getValue();
 
         Entry middle = new Entry(key, value);
 
         // color nodes in non-full bottommost level red
         if (level == redLevel)
             middle.color = Color.RED;
 
         if (left != null) {
             middle.left = left;
         }
 
         if (mid < hi) {
             Entry right = buildFromSorted(level + 1, mid + 1, hi, redLevel, it);
             middle.right = right;
         }
 
         return middle;
     }
 
     /**
      * Returns a shallow copy of this <code>RedBlackTree</code> instance. (The keys and
      * values themselves are not cloned.)
      * 
      * @return A shallow copy of this tree.
      */
     @SuppressWarnings("unchecked")
     @Override
     public RedBlackTree<K, V> clone()
     {
         RedBlackTree<K, V> clone = null;
         try {
             clone = (RedBlackTree<K, V>) super.clone();
         } catch (CloneNotSupportedException e) {
             throw new InternalError();
         }
 
         clone.root = null;
         clone.size = 0;
         clone.modCount = 0;
         clone.entrySet = null;
         clone.keySet = null;
 
         clone.buildFromSorted(size, entrySet().iterator());
 
         return clone;
     }
 
     /**
      * A view on a part of this tree.
      * 
      * @author Martin Pecka
      */
     protected class SubMap extends AbstractMap<K, V> implements SortedMap<K, V>
     {
         /** Bounds. */
         protected K from, to;
         /** Are the bounds inclusive? */
         protected boolean fromIncl, toIncl;
         /** Ignore bounds? */
         protected boolean fromStart, toEnd;
 
         /**
          * Create a submap defined by the arguments.
          * 
          * @param fromStart If true, ignore the lower bound, the submap will be down-unbounded.
          * @param from The lower bound.
          * @param fromIncl If true, the lower bound is treated as inclusive.
          * @param toEnd If true, ignore the upper bound, the submap will be up-unbounded.
          * @param to The upper bound.
          * @param toIncl If true, the upper bound is treated as inclusive.
          * 
          * @throws IllegalArgumentException If from > to and the bounds aren't ignored.
          */
         public SubMap(boolean fromStart, K from, boolean fromIncl, boolean toEnd, K to, boolean toIncl)
         {
             if (!fromStart && !toEnd) {
                 if (comparator.compare(from, to) > 0)
                     throw new IllegalArgumentException("Cannot construct submap with from > to.");
             } else {
                 if (!fromStart)
                     comparator.compare(from, from); // type check
                 if (!toEnd)
                     comparator.compare(to, to); // type check
             }
 
             this.from = from;
             this.to = to;
             this.fromIncl = fromIncl;
             this.toIncl = toIncl;
             this.fromStart = fromStart;
             this.toEnd = toEnd;
         }
 
         @Override
         public int size()
         {
             return (fromStart && toEnd) ? RedBlackTree.this.size() : entrySet().size();
         }
 
         @Override
         public boolean isEmpty()
         {
             return (fromStart && toEnd) ? RedBlackTree.this.isEmpty() : entrySet().isEmpty();
         }
 
         /**
          * Returns true if the given key is in the range this submap works on.
          * 
          * @param key The key to compare.
          * @return true if the given key is in the range this submap works on.
          * 
          * @throws NullPointerException If the key is <code>null</code> and the iterator doesn't handle
          *             <code>null</code> values.
          * @throws ClassCastException If key cannot be cast to the type of keys in the tree.
          */
         protected boolean keyValid(Object key)
         {
             @SuppressWarnings("unchecked")
             K k = (K) key;
             comparator.compare(k, k); // check type
             return (fromStart || comparator.compare(from, k) < (fromIncl ? 1 : 0))
                     && (toEnd || comparator.compare(k, to) < (toIncl ? 1 : 0));
         }
 
         @Override
         public boolean containsKey(Object key)
         {
             return keyValid(key) && RedBlackTree.this.containsKey(key);
         }
 
         @Override
         public boolean containsValue(Object value)
         {
             @SuppressWarnings("unchecked")
             TreePath path = getPathForValue((V) value);
             if (path.size() == 0)
                 return false;
             return containsKey(path.getLast().getKey());
         }
 
         @Override
         public V get(Object key)
         {
             if (!keyValid(key))
                 return null;
             return RedBlackTree.this.get(key);
         }
 
         @Override
         public V put(K key, V value)
         {
             if (!keyValid(key))
                 throw new IllegalArgumentException("The given key is out of range.");
             return null;
         }
 
         @Override
         public V remove(Object key)
         {
             return keyValid(key) ? RedBlackTree.this.remove(key) : null;
         }
 
         @Override
         public void putAll(Map<? extends K, ? extends V> m)
         {
             if (fromStart && toEnd) {
                 RedBlackTree.this.putAll(m);
             } else {
                 for (Entry<? extends K, ? extends V> e : m.entrySet()) {
                     put(e.getKey(), e.getValue());
                 }
             }
         }
 
         @Override
         public void clear()
         {
             if (fromStart && toEnd) {
                 RedBlackTree.this.clear();
             } else {
                 entrySet().clear();
             }
         }
 
         @Override
         public Comparator<? super K> comparator()
         {
             return comparator;
         }
 
         @Override
         public SortedMap<K, V> subMap(K fromKey, K toKey)
         {
             if (!keyValid(fromKey))
                 throw new IllegalArgumentException(
                         "Cannot construct submap with lower bound outside the containing submap.");
             if (!keyValid(toKey))
                 throw new IllegalArgumentException(
                         "Cannot construct submap with upper bound outside the containing submap.");
             return RedBlackTree.this.subMap(fromKey, toKey);
         }
 
         @Override
         public SortedMap<K, V> headMap(K toKey)
         {
             if (!keyValid(toKey))
                 throw new IllegalArgumentException(
                         "Cannot construct submap with upper bound outside the containing submap.");
 
             if (fromStart) {
                 return RedBlackTree.this.headMap(toKey);
             } else {
                 return subMap(from, toKey);
             }
         }
 
         @Override
         public SortedMap<K, V> tailMap(K fromKey)
         {
             if (!keyValid(fromKey))
                 throw new IllegalArgumentException(
                         "Cannot construct submap with lower bound outside the containing submap.");
 
             if (toEnd) {
                 return RedBlackTree.this.tailMap(fromKey);
             } else {
                 return subMap(fromKey, to);
             }
         }
 
         @Override
         public K firstKey()
         {
             if (fromStart) {
                 return RedBlackTree.this.firstKey();
             } else {
                 TreePath path = getPath(from);
                 while (path.size() > 0 && comparator.compare(path.getLast().getKey(), from) < (fromIncl ? 0 : 1))
                     path.moveToSuccesor();
 
                 if (path.size() > 0) {
                     return path.getLast().getKey();
                 } else {
                     return null;
                 }
             }
         }
 
         @Override
         public K lastKey()
         {
             if (toEnd) {
                 return RedBlackTree.this.lastKey();
             } else {
                 TreePath path = getPath(to);
                 while (path.size() > 0 && comparator.compare(to, path.getLast().getKey()) < (toIncl ? 0 : 1))
                     path.moveToPredecessor();
 
                 if (path.size() > 0) {
                     return path.getLast().getKey();
                 } else {
                     return null;
                 }
             }
         }
 
         protected SortedSet<K> keySet = null;
 
         protected class SubMapKeySet extends AbstractSet<K> implements SortedSet<K>
         {
             @Override
             public Iterator<K> iterator()
             {
                 RedBlackTree<K, V>.Entry first = fromStart ? getFirstEntry() : getEntry(firstKey());
                 return new KeyIterator(first) {
                     @Override
                     public boolean hasNext()
                     {
                         return super.hasNext() && keyValid(pathToNext.getLast().getKey());
                     }
                 };
             }
 
             @Override
             public int size()
             {
                 return SubMap.this.size();
             }
 
             @Override
             public boolean remove(Object o)
             {
                 int oldSize = SubMap.this.size();
                 SubMap.this.remove(o);
                 return oldSize != SubMap.this.size();
             }
 
             @Override
             public boolean contains(Object o)
             {
                 return SubMap.this.containsKey(o);
             }
 
             @Override
             public Comparator<? super K> comparator()
             {
                 return comparator;
             }
 
             @Override
             public SortedSet<K> subSet(K fromElement, K toElement)
             {
                 return new SubMap(false, fromElement, true, false, toElement, false).keySet();
             }
 
             @Override
             public SortedSet<K> headSet(K toElement)
             {
                 return new SubMap(true, null, true, false, toElement, false).keySet();
             }
 
             @Override
             public SortedSet<K> tailSet(K fromElement)
             {
                 return new SubMap(false, fromElement, true, true, null, false).keySet();
             }
 
             @Override
             public K first()
             {
                 return SubMap.this.firstKey();
             }
 
             @Override
             public K last()
             {
                 return SubMap.this.lastKey();
             }
         }
 
         @Override
         public SortedSet<K> keySet()
         {
             if (keySet == null)
                 keySet = new SubMapKeySet();
             return keySet;
         }
 
         protected class SubMapValues extends AbstractCollection<V>
         {
             @Override
             public Iterator<V> iterator()
             {
                 RedBlackTree<K, V>.Entry first = fromStart ? getFirstEntry() : getEntry(firstKey());
                 return new ValueIterator(first) {
                     @Override
                     public boolean hasNext()
                     {
                         return super.hasNext() && keyValid(pathToNext.getLast().getKey());
                     }
                 };
             }
 
             @Override
             public int size()
             {
                 return SubMap.this.size();
             }
         }
 
         @Override
         public Collection<V> values()
         {
             if (values == null)
                 values = new SubMapValues();
             return values;
         }
 
         protected Set<Map.Entry<K, V>> entrySet = null;
 
         protected class SubMapEntrySet extends AbstractSet<Map.Entry<K, V>>
         {
             @Override
             public Iterator<Map.Entry<K, V>> iterator()
             {
                 RedBlackTree<K, V>.Entry first = fromStart ? getFirstEntry() : getEntry(firstKey());
                 return new EntryIterator(first) {
 
                     @Override
                     public boolean hasNext()
                     {
                         return super.hasNext() && keyValid(pathToNext.getLast().getKey());
                     }
                 };
             }
 
             /** Cache modcount in the times we last computed submap size. */
             private int sizeModCount = -1;
 
             /** */
             private int cachedSize   = 0;
 
             @Override
             public int size()
             {
                 if (sizeModCount == modCount) {
                     return cachedSize;
                 }
                 sizeModCount = modCount;
 
                 TreePath path = getPath(SubMap.this.firstKey());
                 K last = SubMap.this.lastKey();
 
                 cachedSize = 0;
                 while (path.size() > 0 && comparator.compare(path.getLast().getKey(), last) <= 0) {
                     cachedSize++;
                     path.moveToSuccesor();
                 }
 
                 return cachedSize;
             }
 
             @Override
             public boolean remove(Object o)
             {
                 @SuppressWarnings("unchecked")
                 Map.Entry<K, V> e = (Map.Entry<K, V>) o;
                 TreePath path = getPath(e.getKey());
                 if (!path.endsWithKey(e.getKey()))
                     return false;
                 if (!valEquals(path.getLast().getValue(), e.getValue()))
                     return false;
                 int oldSize = SubMap.this.size();
                 SubMap.this.remove(e.getKey());
                 return oldSize != SubMap.this.size();
             }
 
             @Override
             public boolean contains(Object o)
             {
                 @SuppressWarnings("unchecked")
                 Map.Entry<K, V> e = (Map.Entry<K, V>) o;
                 if (!keyValid(e.getKey()))
                     return false;
                 Map.Entry<K, V> treeE = getEntryForKey(e.getKey());
                 return treeE != null && valEquals(treeE.getValue(), e.getValue());
             }
 
         }
 
         @Override
         public Set<Map.Entry<K, V>> entrySet()
         {
             if (entrySet == null)
                 entrySet = new SubMapEntrySet();
             return entrySet;
         }
 
     }
 
     /**
      * @throws ClassCastException If the key cannot be compared to the keys in the tree.
      * @throws NullPointerException If the specified key is <code>null</code> and this tree's comparator does not permit
      *             <code>null</code> keys.
      * @throws IllegalArgumentException If <code>fromKey &gt; toKey</code>.
      */
     @Override
     public SortedMap<K, V> subMap(K fromKey, K toKey)
     {
         return new SubMap(false, fromKey, true, false, toKey, false);
     }
 
     /**
      * @throws ClassCastException If the key cannot be compared to the keys in the tree.
      * @throws NullPointerException If the specified key is <code>null</code> and this tree's comparator does not permit
      *             <code>null</code> keys.
      */
     @Override
     public SortedMap<K, V> headMap(K toKey)
     {
         return new SubMap(true, null, true, false, toKey, false);
     }
 
     /**
      * @throws ClassCastException If the key cannot be compared to the keys in the tree.
      * @throws NullPointerException If the specified key is <code>null</code> and this tree's comparator does not permit
      *             <code>null</code> keys.
      */
     @Override
     public SortedMap<K, V> tailMap(K fromKey)
     {
         return new SubMap(false, fromKey, true, true, null, false);
     }
 
     /**
      * The view of the values of this tree.
      * 
      * @author Martin Pecka
      */
     class Values extends AbstractCollection<V>
     {
         @Override
         public Iterator<V> iterator()
         {
             return new ValueIterator(getFirstEntry());
         }
 
         @Override
         public int size()
         {
             return RedBlackTree.this.size();
         }
 
         @Override
         public boolean contains(Object o)
         {
             return RedBlackTree.this.containsValue(o);
         }
 
         @Override
         public boolean remove(Object o)
         {
             @SuppressWarnings("unchecked")
             TreePath path = getPathForValue((V) o);
             if (path.size() > 0) {
                 deleteLastPathEntry(path);
                 return true;
             }
             return false;
         }
 
         @Override
         public void clear()
         {
             RedBlackTree.this.clear();
         }
     }
 
     /**
      * A set of views on the entries of the tree.
      * 
      * @author Martin Pecka
      */
     class EntrySet extends AbstractSet<Map.Entry<K, V>>
     {
         @Override
         public Iterator<Map.Entry<K, V>> iterator()
         {
             return new EntryIterator(getFirstEntry());
         }
 
         @Override
         public boolean contains(Object o)
         {
             if (!(o instanceof Map.Entry<?, ?>))
                 return false;
             @SuppressWarnings("unchecked")
             Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
             Entry p = getEntry(entry.getKey());
             return p != null && valEquals(p.getValue(), entry.getValue());
         }
 
         @Override
         public boolean remove(Object o)
         {
             if (!(o instanceof Map.Entry))
                 return false;
             @SuppressWarnings("unchecked")
             Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
             TreePath path = getPath(entry.getKey());
             if (path.endsWithKey(entry.getKey()) && valEquals(path.getLast().getValue(), entry.getValue())) {
                 deleteLastPathEntry(path);
                 return true;
             }
             return false;
         }
 
         @Override
         public int size()
         {
             return RedBlackTree.this.size();
         }
 
         @Override
         public void clear()
         {
             RedBlackTree.this.clear();
         }
     }
 
     /**
      * The view of keys in this tree.
      * 
      * @author Martin Pecka
      */
     protected class KeySet extends AbstractSet<K> implements SortedSet<K>
     {
         @Override
         public Iterator<K> iterator()
         {
             return keyIterator();
         }
 
         public Iterator<K> descendingIterator()
         {
             return descendingKeyIterator();
         }
 
         @Override
         public int size()
         {
             return RedBlackTree.this.size();
         }
 
         @Override
         public K first()
         {
             return firstKey();
         }
 
         @Override
         public K last()
         {
             return lastKey();
         }
 
         @Override
         public Comparator<? super K> comparator()
         {
             return RedBlackTree.this.comparator();
         }
 
         @Override
         public boolean remove(Object o)
         {
             int oldSize = size();
             RedBlackTree.this.remove(o);
             return size() != oldSize;
         }
 
         @Override
         public SortedSet<K> subSet(K fromElement, K toElement)
         {
             return new SubMap(false, fromElement, true, false, toElement, false).keySet();
         }
 
         @Override
         public SortedSet<K> headSet(K toElement)
         {
             return new SubMap(true, null, true, false, toElement, false).keySet();
         }
 
         @Override
         public SortedSet<K> tailSet(K fromElement)
         {
             return new SubMap(false, fromElement, true, true, null, false).keySet();
         }
     }
 
     private Set<K> keySet = null;
 
     @Override
     public Set<K> keySet()
     {
         if (keySet == null)
             keySet = new KeySet();
         return keySet;
     }
 
     private Collection<V> values = null;
 
     @Override
     public Collection<V> values()
     {
         if (values == null)
             values = new Values();
         return values;
     }
 
     /** A view of the tree's entries. */
     private transient EntrySet entrySet = null;
 
     @Override
     public Set<Map.Entry<K, V>> entrySet()
     {
         if (entrySet == null)
             entrySet = new EntrySet();
         return entrySet;
     }
 
     /**
      * The base for all iterators.
      */
     protected abstract class BaseEntryIterator<T> implements Iterator<T>
     {
         /** The path to the last returned entry. */
         protected TreePath pathToLastReturned;
         /** The path to "next" entry (if prevEntry() was called, this contains the path to the previous entry). */
         protected TreePath pathToNext;
         /**
          * Modification counter state in the time of this iterator's creation - serves for revealing concurrent
          * modification.
          */
         protected int      expectedModCount;
 
         /** Set to true in each remove() call and to false in each nextEntry() and prevEntry(). */
         protected boolean  usedRemove = false;
 
         /**
          * Create the iterator with <code>first</code> as the first entry to be returned by next().
          * 
          * @param first The first entry to return. Can be <code>null</code> (in that case the iterator will be empty).
          */
         public BaseEntryIterator(Entry first)
         {
             expectedModCount = modCount;
             pathToLastReturned = null;
             if (first != null)
                 pathToNext = getPath(first.getKey());
             else
                 pathToNext = new TreePath();
         }
 
         @Override
         public boolean hasNext()
         {
             return pathToNext.size() > 0;
         }
 
         /**
          * @return The next entry.
          */
         protected Entry nextEntry()
         {
             if (pathToNext.size() == 0)
                 throw new NoSuchElementException();
             if (modCount != expectedModCount)
                 throw new ConcurrentModificationException();
 
             usedRemove = false;
 
             pathToLastReturned = pathToNext;
             pathToNext = new TreePath();
             pathToNext.addAll(pathToLastReturned);
 
             pathToNext.moveToSuccesor();
 
             return pathToLastReturned.getLast();
         }
 
         /**
          * @return The previous entry.
          */
         protected Entry prevEntry()
         {
             if (pathToNext.size() == 0)
                 throw new NoSuchElementException();
             if (modCount != expectedModCount)
                 throw new ConcurrentModificationException();
 
             usedRemove = false;
 
             pathToLastReturned = pathToNext;
             pathToNext = new TreePath();
             pathToNext.addAll(pathToLastReturned);
 
             pathToNext.moveToPredecessor();
 
             return pathToLastReturned.getLast();
         }
 
         @Override
         public void remove()
         {
             if (pathToLastReturned == null || usedRemove)
                 throw new IllegalStateException();
             if (modCount != expectedModCount)
                 throw new ConcurrentModificationException();
 
             deleteLastPathEntry(pathToLastReturned);
             // the path to the next node may have changed
             pathToNext = getPath(pathToNext.getLast().getKey());
 
             expectedModCount = modCount;
             usedRemove = true;
         }
     }
 
     /**
      * Iterator over entries.
      * 
      * @author Martin Pecka
      */
     protected class EntryIterator extends BaseEntryIterator<Map.Entry<K, V>>
     {
         /**
          * Create the iterator over entries beginning with first.
          * 
          * @param first The first entry to start with.
          */
         public EntryIterator(Entry first)
         {
             super(first);
         }
 
         @Override
         public Map.Entry<K, V> next()
         {
             return nextEntry();
         }
     }
 
     /**
      * Iterator over values (but sorted by keys).
      * 
      * @author Martin Pecka
      */
     protected class ValueIterator extends BaseEntryIterator<V>
     {
         /**
          * Create the iterator over values beginning with <code>first</code> as the first entry.
          * 
          * @param first The first entry to start with.
          */
         public ValueIterator(Entry first)
         {
             super(first);
         }
 
         @Override
         public V next()
         {
             return nextEntry().value;
         }
     }
 
     /**
      * Iterator over keys.
      * 
      * @author Martin Pecka
      */
     protected class KeyIterator extends BaseEntryIterator<K>
     {
         /**
          * Create the iterator over keys beginning with <code>first</code> as the first entry.
          * 
          * @param first The first entry to start with.
          */
         public KeyIterator(Entry first)
         {
             super(first);
         }
 
         @Override
         public K next()
         {
             return nextEntry().key;
         }
     }
 
     /**
      * Iterator over keys in reverse order.
      * 
      * @author Martin Pecka
      */
     protected class DescendingKeyIterator extends BaseEntryIterator<K>
     {
         /**
          * Create the iterator over keys beginning with <code>first</code> as the first entry.
          * 
          * @param first The first entry to start with.
          */
         public DescendingKeyIterator(Entry first)
         {
             super(first);
         }
 
         @Override
         public K next()
         {
             return prevEntry().key;
         }
     }
 
     /**
      * @return The iterator over keys in ascending order.
      */
     public Iterator<K> keyIterator()
     {
         return new KeyIterator(getFirstEntry());
     }
 
     /**
      * @return The iterator over keys in descending order.
      */
     public Iterator<K> descendingKeyIterator()
     {
         return new DescendingKeyIterator(getLastEntry());
     }
 
     /**
      * Test two values for equality. Differs from o1.equals(o2) only in that it copes with <tt>null</tt> o1 properly.
      */
     protected final static boolean valEquals(Object o1, Object o2)
     {
         return (o1 == null ? o2 == null : o1.equals(o2));
     }
 
     /**
      * Return SimpleImmutableEntry for entry, or <code>null</code> if <code>null</code>.
      */
     protected static <K, V> Map.Entry<K, V> exportEntry(RedBlackTree<K, V>.Entry e)
     {
         return e == null ? null : new AbstractMap.SimpleImmutableEntry<K, V>(e);
     }
 
     /**
      * Return the color of the given entry, or Black, if it is <code>null</code>.
      * 
      * @param p The entry to get color of.
      * @return The color of the given entry, or Black, if it is <code>null</code>.
      */
     protected static <K, V> Color colorOf(RedBlackTree<K, V>.Entry p)
     {
         return (p == null ? Color.BLACK : p.color);
     }
 
     /**
      * Set p's color to c, if p isn't <code>null</code>.
      * 
      * @param p The entry to set color for.
      * @param c The color to set.
      */
     protected static <K, V> void setColor(RedBlackTree<K, V>.Entry p, Color c)
     {
         if (p != null)
             p.color = c;
     }
 
     /**
      * Return the left child of p, or <code>null</code> if p is <code>null</code>.
      * 
      * @param p The entry to get left child of.
      * @return The left child of p, or <code>null</code> if p is <code>null</code>.
      */
     protected static <K, V> RedBlackTree<K, V>.Entry leftOf(RedBlackTree<K, V>.Entry p)
     {
         return (p == null) ? null : p.left;
     }
 
     /**
      * Return the right child of p, or <code>null</code> if p is <code>null</code>.
      * 
      * @param p The entry to get right child of.
      * @return The right child of p, or <code>null</code> if p is <code>null</code>.
      */
     protected static <K, V> RedBlackTree<K, V>.Entry rightOf(RedBlackTree<K, V>.Entry p)
     {
         return (p == null) ? null : p.right;
     }
 
     /**
      * Save the state of the tree's instance to a stream.
      * 
      * @param s The stream to write in.
      * 
      * @serialData The <i>size</i> of the RedBlackTree (the number of key-value mappings) is emitted (int), followed by
      *             the key (Object) and value (Object) for each key-value mapping represented by the RedBlackTree. The
      *             key-value mappings are emitted in key-order.
      */
     private void writeObject(ObjectOutputStream s) throws IOException
     {
         // Write out the Comparator and any hidden stuff
         s.defaultWriteObject();
 
         // Write out size (number of Mappings)
         s.writeInt(size);
 
         // Write out keys and values (alternating)
         for (Iterator<Map.Entry<K, V>> i = entrySet().iterator(); i.hasNext();) {
             Map.Entry<K, V> e = i.next();
             s.writeObject(e.getKey());
             s.writeObject(e.getValue());
         }
     }
 
     /**
      * Reconstitute the tree's instance from a stream.
      * 
      * @param s The stream to read from.
      */
     @SuppressWarnings("unchecked")
     private void readObject(final ObjectInputStream s) throws IOException, ClassNotFoundException
     {
         // Read in the Comparator and any hidden stuff
         s.defaultReadObject();
 
         // Read in size
         int size = s.readInt();
 
         List<RedBlackTree<K, V>.Entry> data = new LinkedList<RedBlackTree<K, V>.Entry>();
         for (int i = 0; i < size; i++) {
             K key = (K) s.readObject();
             V value = (V) s.readObject();
             data.add(new Entry(key, value));
         }
         buildFromSorted(size, data.iterator());
     }
 
     /**
      * @return The default comparator to be used if no other comparator is provided.
      */
     protected Comparator<? super K> getDefaultComparator()
     {
         return new Comparator<K>() {
             @SuppressWarnings("unchecked")
             @Override
             public int compare(K o1, K o2) throws ClassCastException
             {
                 Comparable<K> o1c = (Comparable<K>) o1;
                 return o1c.compareTo(o2);
             }
         };
     }
 
     /**
      * A path to a tree's entry, the very first element is the tree's root.
      * 
      * @author Martin Pecka
      */
     protected class TreePath extends LinkedList<Entry>
     {
         /** */
         private static final long serialVersionUID = -5735382496269541155L;
 
         /**
          * Returns true if this path ends with an entry with the given key.
          * 
          * @param key The key to check.
          * @return true if this path ends with an entry with the given key.
          */
         public boolean endsWithKey(K key)
         {
             if (size() == 0)
                 return false;
             K k = getLast().key;
             return (key == null ? k == null : key.equals(k));
         }
 
         /**
          * Return the index-th last entry from the path. Indexed from 0. If it doesn't exist, return <code>null</code>.
          * 
          * @param index The index from the end of the path.
          * @return The index-th last entry from the path. Indexed from 0. If it doesn't exist, return <code>null</code>.
          */
         public Entry getLast(int index)
         {
             int i = size() - index - 1;
             if (i < 0)
                 return null;
             return get(i);
         }
 
         /**
          * Make this path lead to it's current last entry's successor. Empty the path if the current last entry is the
          * last entry in the tree.
          * 
          * @return The successor, or <code>null</code> if it doesn't exist.
          */
         protected Entry moveToSuccesor()
         {
             if (size() == 0)
                 return null;
 
             if (getLast().right != null) {
                 addLast(getLast().right);
                 while (getLast().left != null) {
                     addLast(getLast().left);
                 }
             } else {
                 while (size() > 0 && getLast(1) != null && getLast() == getLast(1).right) {
                     removeLast();
                 }
                 removeLast();
             }
 
             return getLast(0);
         }
 
         /**
          * Make this path lead to it's current last entry's predecessor. Empty the path if the current last entry is the
          * first entry in the tree.
          * 
          * @return The predecessor, or <code>null</code> if it doesn't exist.
          */
         protected Entry moveToPredecessor()
         {
             if (size() == 0)
                 return null;
 
             if (getLast().left != null) {
                 addLast(getLast().left);
                 while (getLast().right != null) {
                     addLast(getLast().right);
                 }
             } else {
                 while (size() > 0 && getLast(1) != null && getLast() == getLast(1).left) {
                     removeLast();
                 }
                 removeLast();
             }
 
             return getLast(0);
         }
 
         /**
          * Rotate left the last path's entry and repair the path so that it is the new path to its previous last entry.
          */
         public void rotateLeft()
         {
             if (size() > 0) {
                 Entry p = getLast(), q = p.right;
                 p.right = q.left;
 
                 // substitute p with q in the path
                 removeLast();
                 addLast(q);
 
                 if (size() == 1)
                     root = q;
                 else if (getLast(1).left == p)
                     getLast(1).left = q;
                 else
                     getLast(1).right = q;
 
                 q.left = p;
                 addLast(p);
             }
         }
 
         /**
          * Rotate right the last path's entry and repair the path so that it is the new path to its previous last entry.
          */
         public void rotateRight()
         {
             if (size() > 0) {
                 Entry p = getLast(), q = p.left;
                 p.left = q.right;
 
                 // substitute p with q in the path
                 removeLast();
                 addLast(q);
 
                 if (size() == 1)
                     root = q;
                 else if (getLast(1).left == p)
                     getLast(1).left = q;
                 else
                     getLast(1).right = q;
 
                 q.right = p;
                 addLast(p);
             }
         }
     }
 
     /**
      * Entry in the Tree.
      */
     protected class Entry implements Map.Entry<K, V>
     {
         K     key;
         V     value;
         Entry left  = null;
         Entry right = null;
         Color color = Color.BLACK;
 
         /**
          * Make a new entry with given key and value, and with <tt>null</tt> child links, and BLACK color.
          */
         Entry(K key, V value)
         {
             this.key = key;
             this.value = value;
         }
 
         @Override
         public K getKey()
         {
             return key;
         }
 
         @Override
         public V getValue()
         {
             return value;
         }
 
         @Override
         public V setValue(V value)
         {
             V oldValue = this.value;
             this.value = value;
             return oldValue;
         }
 
         @Override
         public boolean equals(Object o)
         {
             if (!(o instanceof Map.Entry))
                 return false;
             Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
 
             return valEquals(key, e.getKey()) && valEquals(value, e.getValue());
         }
 
         @Override
         public int hashCode()
         {
             int keyHash = (key == null ? 0 : key.hashCode());
             int valueHash = (value == null ? 0 : value.hashCode());
             return keyHash ^ valueHash;
         }
 
         @Override
         public String toString()
         {
             return key + "=" + value;
         }
     }
 }
