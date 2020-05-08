 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.utils;
 
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.SortedMap;
 
 /**
  * An implementation of interval tree.
  * 
  * @author Martin Pecka
  */
 public class IntervalTree<K extends Comparable<? super K>, V> extends RedBlackTree<Interval<K>, V>
 {
 
     /** */
     private static final long serialVersionUID = -2870778379455176949L;
 
     /**
      * Construct an empty interval tree.
      */
     public IntervalTree()
     {
         super(new IntervalByMinComparator<K>());
         entryFactory = new IntervalEntryFactory();
     }
 
     /**
      * Construct interval tree with mappings from the given map.
      * 
      * @param m The map to read mappings from.
      */
     public IntervalTree(Map<? extends Interval<K>, ? extends V> m)
     {
         super(new IntervalByMinComparator<K>());
         entryFactory = new IntervalEntryFactory();
         putAll(m);
     }
 
     /**
      * Construct interval tree with mappings from the given map.
      * 
      * If the given map's comparator is instance of {@link IntervalByMinComparator}&lt;K&gt;, this method will run in
      * linear time, otherwise it will run in n*log n time.
      * 
      * @param m The map to read mappings from.
      */
     public IntervalTree(SortedMap<Interval<K>, ? extends V> m)
     {
         super(new IntervalByMinComparator<K>());
         entryFactory = new IntervalEntryFactory();
 
         if (!(m.comparator() instanceof IntervalByMinComparator<?>))
             putAll(m);
         else
             buildFromSorted(m.size(), m.entrySet().iterator());
     }
 
     @Override
     protected void repairTreeAfterInsert(TreePath path)
     {
         // the inserted value is on the end of the path
         K max = path.getLast().getKey().getMax();
 
         // we update the maximum values for all nodes on the path (setting it also for the last one is ok)
         for (Entry e : path) {
             @SuppressWarnings("unchecked")
             IntervalEntry ie = (IntervalEntry) e;
             if (ie.getMax() == null || ie.getMax().compareTo(max) < 0)
                 ie.setMax(max);
         }
 
         super.repairTreeAfterInsert(path);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     protected void deleteLastPathEntry(TreePath path)
     {
         ((IntervalEntry) path.getLast()).setMax(null);
 
         Iterator<Entry> it = path.descendingIterator();
         it.next(); // skip the last node which we've already checked
 
         // walk up the path and repair the subtree maximum caches
         while (it.hasNext()) {
             IntervalEntry e = (IntervalEntry) it.next();
             K maxK = e.getKey().getMax();
             K maxL = (e.left == null ? null : ((IntervalEntry) e.left).getMax());
             K maxR = (e.right == null ? null : ((IntervalEntry) e.right).getMax());
             e.setMax(max(maxK, max(maxL, maxR)));
         }
 
         super.deleteLastPathEntry(path);
     }
 
     /**
      * Return the value of a node that has its key overlapping with the given interval.
      * 
      * @param key The interval to find overlap for.
      * @return The value of a node that has its key overlapping with the given interval.
      */
     public V intervalGet(Interval<K> key)
     {
         IntervalEntry e = getIntervalEntry(key);
         return (e == null ? null : e.value);
     }
 
     /**
      * Return a node that has its key overlapping with the given interval.
      * 
      * @param key The interval to find overlap for.
      * @return A node that has its key overlapping with the given interval.
      */
     @SuppressWarnings("unchecked")
     protected IntervalEntry getIntervalEntry(Interval<K> key)
     {
         IntervalEntry p = (IntervalEntry) root;
         while (p != null && !p.getKey().overlapsWith(key)) {
            if (p.left != null && ((IntervalEntry) p.left).getMax() != null
                    && ((IntervalEntry) p.left).getMax().compareTo(key.getMin()) >= 0)
                 p = (IntervalEntry) p.left;
             else
                 p = (IntervalEntry) p.right;
         }
         return p;
     }
 
     /**
      * Comparator of intervals that compares them according to their lower bounds.
      * 
      * <code>null</code> is less than everything else, two <code>null</code>s are equal.
      * 
      * @author Martin Pecka
      */
     protected static class IntervalByMinComparator<T extends Comparable<? super T>> implements Comparator<Interval<T>>
     {
 
         @Override
         public int compare(Interval<T> o1, Interval<T> o2)
         {
             if (o1 == null)
                 return (o2 == null ? 0 : -1);
             if (o2 == null)
                 return 1;
 
             return o1.getMax().compareTo(o2.getMax());
         }
 
     }
 
     /**
      * Entry factory that returns {@link IntervalEntry} entries and {@link IntervalTreePath} paths.
      * 
      * @author Martin Pecka
      */
     protected class IntervalEntryFactory extends EntryFactory
     {
         @Override
         public IntervalEntry createEntry(Interval<K> key, V value)
         {
             return new IntervalEntry(key, value);
         }
 
         @Override
         public IntervalTreePath createTreePath()
         {
             return new IntervalTreePath();
         }
     }
 
     /**
      * Entry of the interval tree - it has to store the maximum value contained in the subtree defined by this entry.
      * 
      * @author Martin Pecka
      */
     protected class IntervalEntry extends Entry
     {
 
         /** The maximum upper bound contained in the subtree defined by this entry. */
         protected K max;
 
         /**
          * @param key
          * @param value
          */
         IntervalEntry(Interval<K> key, V value)
         {
             super(key, value);
             max = key.getMax();
         }
 
         /**
          * @return the max
          */
         public K getMax()
         {
             return max;
         }
 
         /**
          * @param max the max to set
          */
         public void setMax(K max)
         {
             this.max = max;
         }
 
         @Override
         public String toString()
         {
             return key + "=" + value + "(max=" + max + ")";
         }
 
     }
 
     /**
      * A tree path in the interval tree which handles maximum subtree caches correctly.
      * 
      * @author Martin Pecka
      */
     protected class IntervalTreePath extends TreePath
     {
 
         /** */
         private static final long serialVersionUID = -7383073835756793866L;
 
         @SuppressWarnings("unchecked")
         @Override
         public void rotateLeft()
         {
             IntervalEntry x = (IntervalEntry) getLast();
             IntervalEntry y = (IntervalEntry) x.right;
 
             super.rotateLeft();
 
             y.setMax(x.getMax());
 
             K maxK = x.getKey().getMax();
             K maxL = (x.left == null ? null : ((IntervalEntry) x.left).getMax());
             K maxR = (x.right == null ? null : ((IntervalEntry) x.right).getMax());
             x.setMax(max(maxK, max(maxL, maxR)));
         }
 
         @SuppressWarnings("unchecked")
         @Override
         public void rotateRight()
         {
             IntervalEntry x = (IntervalEntry) getLast();
             IntervalEntry y = (IntervalEntry) x.left;
 
             super.rotateRight();
 
             y.setMax(x.getMax());
 
             K maxK = x.getKey().getMax();
             K maxL = (x.left == null ? null : ((IntervalEntry) x.left).getMax());
             K maxR = (x.right == null ? null : ((IntervalEntry) x.right).getMax());
             x.setMax(max(maxK, max(maxL, maxR)));
         }
     }
 
     /**
      * Return the maximum of the two comparables. <code>null</code> is less than everything.
      * 
      * @param <K>
      * @param val1 The first value to compare.
      * @param val2 The second value to compare.
      * @return The maximum.
      */
     protected static <K extends Comparable<? super K>> K max(K val1, K val2)
     {
         if (val1 == null)
             return val2;
 
         if (val2 == null)
             return val1;
 
         return (val1.compareTo(val2) >= 0 ? val1 : val2);
     }
 }
