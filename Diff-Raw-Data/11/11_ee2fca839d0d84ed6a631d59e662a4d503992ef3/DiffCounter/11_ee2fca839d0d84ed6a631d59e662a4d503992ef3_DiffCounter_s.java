 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 package org.geogit.api.plumbing.diff;
 
 import static com.google.common.base.Preconditions.checkState;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.SortedSet;
 
 import javax.annotation.Nonnull;
 
 import org.geogit.api.Bucket;
 import org.geogit.api.Node;
 import org.geogit.api.ObjectId;
 import org.geogit.api.RevObject;
 import org.geogit.api.RevObject.TYPE;
 import org.geogit.api.RevTree;
 import org.geogit.storage.NodeStorageOrder;
 import org.geogit.storage.ObjectDatabase;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Supplier;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSortedMap;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.PeekingIterator;
 import com.google.common.collect.Sets;
 import com.google.common.collect.SortedSetMultimap;
 import com.google.common.collect.TreeMultimap;
 
 /**
  * A faster alternative to count the number of diffs between two trees than walking a
  * {@link DiffTreeWalk} iterator; doesn't support filtering, counts the total number of differences
  * between the two trees
  * <p>
  * TODO: add support for path filtering
  */
 public class DiffCounter implements Supplier<Long> {
 
     @Nonnull
     private final RevTree fromRootTree;
 
     @Nonnull
     private final RevTree toRootTree;
 
     @Nonnull
     private ObjectDatabase objectDb;
 
     public DiffCounter(final ObjectDatabase db, final RevTree fromRootTree, final RevTree toRootTree) {
         Preconditions.checkNotNull(db);
         Preconditions.checkNotNull(fromRootTree);
         Preconditions.checkNotNull(toRootTree);
         this.objectDb = db;
         this.fromRootTree = fromRootTree;
         this.toRootTree = toRootTree;
     }
 
     @Override
     public Long get() {
 
         RevTree oldTree = this.fromRootTree;
         RevTree newTree = this.toRootTree;
 
         return countDiffs(oldTree, newTree);
     }
 
     private Long countDiffs(ObjectId oldTreeId, ObjectId newTreeId) {
         RevTree leftTree = getTree(oldTreeId);
         RevTree rightTree = getTree(newTreeId);
         return countDiffs(leftTree, rightTree);
     }
 
     private long countDiffs(RevTree oldTree, RevTree newTree) {
         if (oldTree.getId().equals(newTree.getId())) {
             return 0L;
         } else if (newTree.isEmpty()) {
             return countOf(oldTree);
         } else if (oldTree.isEmpty()) {
             return countOf(newTree);
         }
 
         long count = 0L;
 
         final boolean childrenVsChildren = !oldTree.buckets().isPresent()
                 && !newTree.buckets().isPresent();
         final boolean bucketsVsBuckets = oldTree.buckets().isPresent()
                 && newTree.buckets().isPresent();
 
         if (childrenVsChildren) {
             count = countChildrenDiffs(oldTree, newTree);
         } else if (bucketsVsBuckets) {
             ImmutableSortedMap<Integer, Bucket> leftBuckets = oldTree.buckets().get();
             ImmutableSortedMap<Integer, Bucket> rightBuckets = newTree.buckets().get();
             count = countBucketDiffs(leftBuckets, rightBuckets);
         } else {
             // get the children and buckets from the respective trees, order doesn't matter as we're
             // counting diffs
             ImmutableSortedMap<Integer, Bucket> buckets;
             Iterator<Node> children;
 
             buckets = oldTree.buckets().isPresent() ? oldTree.buckets().get() : newTree.buckets()
                     .get();
 
             children = oldTree.buckets().isPresent() ? newTree.children() : oldTree.children();
             count = countBucketsChildren(buckets, children);
         }
 
         return count;
     }
 
     /**
      * Handles the case where one version of a tree has so few nodes that they all fit in its
      * {@link RevTree#children() children}, but the other version of the tree has more nodes so its
      * split into {@link RevTree#buckets()}.
      */
     private long countBucketsChildren(ImmutableSortedMap<Integer, Bucket> buckets,
             Iterator<Node> children) {
 
         final NodeStorageOrder refOrder = new NodeStorageOrder();
         final int bucketDepth = 0; // start at depth 0
         return countBucketsChildren(buckets, children, refOrder, bucketDepth);
     }
 
     private long countBucketsChildren(ImmutableSortedMap<Integer, Bucket> buckets,
             Iterator<Node> children, final NodeStorageOrder refOrder, final int depth) {
 
         final SortedSetMultimap<Integer, Node> treesByBucket;
         final SortedSetMultimap<Integer, Node> featuresByBucket;
         {
             treesByBucket = TreeMultimap.create(Ordering.natural(), refOrder); // make sure values
                                                                                // are sorted
                                                                                // according to
                                                                                // refOrder
             featuresByBucket = TreeMultimap.create(Ordering.natural(), refOrder);// make sure values
                                                                                  // are sorted
                                                                                  // according to
                                                                                  // refOrder
             while (children.hasNext()) {
                 Node ref = children.next();
                 Integer bucket = refOrder.bucket(ref, depth);
                 if (ref.getType().equals(TYPE.TREE)) {
                     treesByBucket.put(bucket, ref);
                 } else {
                     featuresByBucket.put(bucket, ref);
                 }
             }
         }
 
         long count = 0;
 
         {// count full size of all buckets for which no children falls into
             final Set<Integer> loneleyBuckets = Sets.difference(buckets.keySet(),
                     Sets.union(featuresByBucket.keySet(), treesByBucket.keySet()));
 
             for (Integer bucket : loneleyBuckets) {
                 ObjectId bucketId = buckets.get(bucket).id();
                 count += sizeOfTree(bucketId);
             }
         }
         {// count the full size of all children whose buckets don't exist on the buckets tree
             for (Integer bucket : Sets.difference(featuresByBucket.keySet(), buckets.keySet())) {
                 SortedSet<Node> refs = featuresByBucket.get(bucket);
                 count += refs.size();
             }
 
             for (Integer bucket : Sets.difference(treesByBucket.keySet(), buckets.keySet())) {
                 SortedSet<Node> refs = treesByBucket.get(bucket);
                 count += aggregateSize(refs);
             }
         }
 
         // find the number of diffs of the intersection
         final Set<Integer> commonBuckets = Sets.intersection(buckets.keySet(),
                 Sets.union(featuresByBucket.keySet(), treesByBucket.keySet()));
         for (Integer bucket : commonBuckets) {
 
             Iterator<Node> refs = Iterators.concat(treesByBucket.get(bucket).iterator(),
                     featuresByBucket.get(bucket).iterator());
 
             final ObjectId bucketId = buckets.get(bucket).id();
             final RevTree bucketTree = getTree(bucketId);
 
             if (bucketTree.isEmpty()) {
                 // unlikely
                 count += aggregateSize(refs);
             } else if (!bucketTree.buckets().isPresent()) {
                 count += countChildrenDiffs(bucketTree.children(), refs);
             } else {
                 final int deeperBucketsDepth = depth + 1;
                 final ImmutableSortedMap<Integer, Bucket> deeperBuckets;
                 deeperBuckets = bucketTree.buckets().get();
                 count += countBucketsChildren(deeperBuckets, refs, refOrder, deeperBucketsDepth);
             }
         }
 
         return count;
     }
 
     /**
      * Counts the number of differences between two trees that contain {@link RevTree#buckets()
      * buckets} instead of direct {@link RevTree#children() children}
      */
     private long countBucketDiffs(ImmutableSortedMap<Integer, Bucket> leftBuckets,
             ImmutableSortedMap<Integer, Bucket> rightBuckets) {
 
         long count = 0;
         final Set<Integer> bucketIds = Sets.union(leftBuckets.keySet(), rightBuckets.keySet());
 
         ObjectId leftTreeId;
         ObjectId rightTreeId;
 
         for (Integer bucketId : bucketIds) {
            leftTreeId = leftBuckets.get(bucketId).id();
            rightTreeId = rightBuckets.get(bucketId).id();
 
             if (leftTreeId == null || rightTreeId == null) {
                 count += sizeOfTree(leftTreeId == null ? rightTreeId : leftTreeId);
             } else {
                 count += countDiffs(leftTreeId, rightTreeId);
             }
         }
         return count;
     }
 
     private long countChildrenDiffs(RevTree leftTree, RevTree rightTree) {
         // ImmutableList<Node> empty = ImmutableList.of();
         //
         // Set<Node> leftFeatures = ImmutableSet.copyOf(leftTree.features().or(empty));
         // Set<Node> rightFeatures = ImmutableSet.copyOf(rightTree.features().or(empty));
         // SetView<Node> featureDiff = Sets.difference(leftFeatures, rightFeatures);
         // long count = featureDiff.size();
 
         return countChildrenDiffs(leftTree.children(), rightTree.children());
     }
 
     private long countChildrenDiffs(Iterator<Node> leftTree, Iterator<Node> rightTree) {
 
         final Ordering<Node> storageOrder = new NodeStorageOrder();
 
         long count = 0;
 
         PeekingIterator<Node> left = Iterators.peekingIterator(leftTree);
         PeekingIterator<Node> right = Iterators.peekingIterator(rightTree);
 
         while (left.hasNext() && right.hasNext()) {
             Node peekLeft = left.peek();
             Node peekRight = right.peek();
 
             if (0 == storageOrder.compare(peekLeft, peekRight)) {
                 // same path, consume both
                 peekLeft = left.next();
                 peekRight = right.next();
                 if (!peekLeft.getObjectId().equals(peekRight.getObjectId())) {
                     // find the diffs between these two specific refs
                     if (RevObject.TYPE.FEATURE.equals(peekLeft.getType())) {
                         checkState(RevObject.TYPE.FEATURE.equals(peekRight.getType()));
                         count++;
                     } else {
                         checkState(RevObject.TYPE.TREE.equals(peekLeft.getType()));
                         checkState(RevObject.TYPE.TREE.equals(peekRight.getType()));
                         ObjectId leftTreeId = peekLeft.getObjectId();
                         ObjectId rightTreeId = peekRight.getObjectId();
                         count += countDiffs(leftTreeId, rightTreeId);
                     }
                 }
             } else if (peekLeft == storageOrder.min(peekLeft, peekRight)) {
                 peekLeft = left.next();// consume only the left value
                 count += aggregateSize(ImmutableList.of(peekLeft));
             } else {
                 peekRight = right.next();// consume only the right value
                 count += aggregateSize(ImmutableList.of(peekRight));
             }
         }
 
         if (left.hasNext()) {
             count += countRemaining(left);
         } else if (right.hasNext()) {
             count += countRemaining(right);
         }
         Preconditions.checkState(!left.hasNext());
         Preconditions.checkState(!right.hasNext());
         return count;
     }
 
     private long countRemaining(Iterator<Node> remaining) {
         ArrayList<Node> iterable = Lists.newArrayList(remaining);
         return aggregateSize(iterable);
     }
 
     private long sizeOfTree(ObjectId treeId) {
         RevTree tree = getTree(treeId);
         return countOf(tree);
     }
 
     private RevTree getTree(ObjectId treeId) {
         if (treeId.isNull()) {
             return RevTree.EMPTY;
         }
         RevTree tree = objectDb.get(treeId, RevTree.class);
         return tree;
     }
 
     /**
      * @return the total size of {@code tree}
      */
     private long countOf(RevTree tree) {
         return tree.size();
     }
 
     private long aggregateSize(Iterable<Node> children) {
         return aggregateSize(children.iterator());
     }
 
     private long aggregateSize(Iterator<Node> children) {
         long size = 0;
         while (children.hasNext()) {
             Node ref = children.next();
             if (RevObject.TYPE.FEATURE.equals(ref.getType())) {
                 size++;
             } else if (RevObject.TYPE.TREE.equals(ref.getType())) {
                 ObjectId treeId = ref.getObjectId();
                 size += sizeOfTree(treeId);
             }
         }
         return size;
     }
 
 }
