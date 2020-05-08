 /*
  * This file is part of experimaestro.
  * Copyright (c) 2013 B. Piwowarski <benjamin@bpiwowar.net>
  *
  * experimaestro is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * experimaestro is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with experimaestro.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package sf.net.experimaestro.manager.plans;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
 import sf.net.experimaestro.utils.CartesianProduct;
 import sf.net.experimaestro.utils.log.Logger;
 
 import javax.xml.xpath.XPathExpressionException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeSet;
 
 import static java.lang.StrictMath.max;
 
 /**
  * Join
  *
  * @author B. Piwowarski <benjamin@bpiwowar.net>
  * @date 3/3/13
  */
 public class Join extends Product {
     final static private Logger LOGGER = Logger.getLogger();
 
     ArrayList<JoinReference> joins = new ArrayList<>();
 
     @Override
     protected Iterator<ReturnValue> _iterator() {
         return new JoinIterator();
     }
 
     @Override
     protected void ensureConnections(HashMap<Operator, Operator> simplified) {
         for (JoinReference reference : joins)
             reference.operator = Operator.getSimplified(simplified, reference.operator);
     }
 
     @Override
     protected void addNeededStreams(Collection<Operator> streams) {
         for (JoinReference reference : joins)
             streams.add(reference.operator);
     }
 
     @Override
     protected String getName() {
         return "join";
     }
 
     @Override
     public boolean printDOT(PrintStream out, HashSet<Operator> planNodes) {
         if (super.printDOT(out, planNodes)) {
             for (JoinReference join : joins)
                 out.format("p%s -> p%s [style=\"dotted\"];%n", System.identityHashCode(join.operator), System.identityHashCode(this));
 
         }
         return false;
     }
 
     @Override
     protected void doPostInit(List<Map<Operator, Integer>> parentStreams) throws XPathExpressionException {
         super.doPostInit(parentStreams);
 
         // Order the joins in function of the orders of streams
         // We can pick any parent since they share the same order
         Order<Operator> order = ((OrderBy) parents.get(0)).order;
         order.flatten();
         int rank = 0;
         final Object2IntOpenHashMap<Operator> rankMap = new Object2IntOpenHashMap<>();
         for (Operator operator : order.items()) {
             rankMap.put(operator, rank++);
         }
 
         Collections.sort(joins, new Comparator<JoinReference>() {
             @Override
             public int compare(JoinReference o1, JoinReference o2) {
                return Integer.compare(rankMap.get(o1), rankMap.get(o2));
             }
         });
 
 
         // Get the context index for each join & stream
         int joinRank = 0;
         for (JoinReference reference : joins) {
             reference.rank = joinRank++;
             reference.contextIndices = new int[parents.size()];
             for (int i = 0; i < parentStreams.size(); i++) {
                 Map<Operator, Integer> map = parentStreams.get(i);
                 reference.contextIndices[i] = map.get(reference.operator).intValue();
             }
         }
 
 
     }
 
     static public class JoinReference {
         int rank;
         Operator operator;
         int[] contextIndices;
 
         public JoinReference(Operator operator) {
             this.operator = operator;
         }
     }
 
     /**
      * Add a new operator to joins
      *
      * @param operator
      */
     public void addJoin(Operator operator) {
         joins.add(new JoinReference(operator));
     }
 
     private class JoinIterator extends Product.AbstractProductIterator {
         Iterator<Value[]> productIterator = ImmutableList.<Value[]>of().iterator();
         long positions[];
         boolean last = false;
 
         /**
          * Used to store values when there is some context == -1
          */
         TreeSet<Value> stored[] = new TreeSet[parents.size()];
 
         {
             for (int i = 0; i < stored.length; i++) {
                 stored[i] = new TreeSet<>(new ContextComparator(i));
             }
         }
 
         private JoinIterator() {
         }
 
 
         @Override
         boolean next(int i) {
             if (!super.next(i))
                 return false;
 
 
             // Add the value to the set
             final Value value = current[i];
             for (long c : value.context) {
                 if (c == -1) {
                     stored[i].add(value);
                     break;
                 }
             }
 
             return true;
         }
 
         private Iterable<Value> jokers(final int streamIndex) {
             TreeSet<Value> set = stored[streamIndex];
 
             // Clean up unuseful values
             set.headSet(new Value(positions), true).clear();
 
             return Iterables.filter(set, new Predicate<Value>() {
                 @Override
                 public boolean apply(Value input) {
                     for (JoinReference reference : joins) {
                         long pos = input.context[reference.contextIndices[streamIndex]];
                         if (pos != -1 && pos != positions[reference.rank])
                             return false;
                     }
                     return true;
                 }
             });
         }
 
         @Override
         protected ReturnValue computeNext() {
             // First loop
 
             if (first) {
                 if (!computeFirst()) return endOfData();
                 positions = new long[joins.size()];
                 for (int i = positions.length; --i >= 0; ) {
                     positions[i] = -1;
                     for (int j = parents.size(); --j >= 0; )
                         positions[i] = max(positions[i], current[j].context[joins.get(i).contextIndices[j]]);
                 }
             }
 
             // Loop until we have a not empty cartesian product with joined values
             while (true) {
                 if (productIterator.hasNext()) {
                     return getReturnValue(productIterator.next());
                 }
 
                 // If it was the last product iterator, stop now
                 if (last)
                     return endOfData();
 
                 // Loop until joins are satisfied
                 joinLoop:
                 for (int joinIndex = 0; joinIndex < joins.size(); ) {
                     JoinReference join = joins.get(joinIndex);
 
                     for (int streamIndex = 0; streamIndex < parents.size(); streamIndex++) {
                         int contextIndex = join.contextIndices[streamIndex];
                         LOGGER.debug("Context %d of stream %d is %d (position = %d)", contextIndex, streamIndex, current[streamIndex].context[contextIndex], positions[joinIndex]);
                         while (current[streamIndex].context[contextIndex] < positions[joinIndex]) {
                             if (!next(streamIndex))
                                 return endOfData();
 
                             int minRank = checkChanges(streamIndex, positions, joinIndex);
 
                             // A join current index changed: go back to the main loop on joins
                             if (minRank != -1) {
                                 joinIndex = minRank;
                                 if (LOGGER.isTraceEnabled())
                                     LOGGER.trace("Restarting the join with context: ", Arrays.toString(positions));
                                 continue joinLoop;
                             }
 
                             LOGGER.debug("Context[a] %d of stream %d is %d (position = %d)", contextIndex, streamIndex, current[streamIndex].context[contextIndex], positions[joinIndex]);
                         }
 
 
                         assert current[streamIndex].context[contextIndex] == positions[joinIndex];
                     }
 
                     // A join is complete, now we can process next joinIndex
                     joinIndex++;
                 }
 
                 // Fill the cartesian product
                 List<Value> lists[] = new List[parents.size()];
                 long newPositions[] = new long[joins.size()];
 
                 last = true;
                 for (int streamIndex = 0; streamIndex < parents.size(); streamIndex++) {
                     lists[streamIndex] = new ArrayList<>();
                     lists[streamIndex].add(current[streamIndex]);
                     while (true) {
                         if (!next(streamIndex)) {
                             break;
                         }
                         if (checkChanges(streamIndex, newPositions, joins.size()) >= 0) {
                             last = false;
                             break;
                         }
                         lists[streamIndex].add(current[streamIndex]);
 
                         // Add all compatible joker
                         for (Value value : jokers(streamIndex)) {
                             lists[streamIndex].add(new Value(positions, value.nodes));
                         }
                     }
                 }
 
                 if (LOGGER.isDebugEnabled())
                     LOGGER.debug("Selected context: %s", Arrays.toString(positions));
                 if (LOGGER.isTraceEnabled()) {
                     for (int streamIndex = 0; streamIndex < parents.size(); streamIndex++) {
                         for (Value value : lists[streamIndex]) {
                             LOGGER.trace("[%d] %d: %s", streamIndex, value.id, Arrays.toString(value.context));
                         }
                     }
                 }
 
                 // Set the current position
                 positions = newPositions;
                 productIterator = CartesianProduct.of(Value.class, lists).iterator();
 
             }
 
         }
 
 
         /**
          * Check the changes in joins
          *
          * @param streamIndex
          * @param newPositions
          * @param maxRank      Check until this rank
          * @return
          */
         private int checkChanges(int streamIndex, long[] newPositions, int maxRank) {
             int minRank = -1;
             for (int i = maxRank; --i >= 0; ) {
                 JoinReference join = joins.get(i);
                 long resultId = current[streamIndex].context[join.contextIndices[streamIndex]];
                 if (this.positions[i] < resultId) {
                     newPositions[i] = resultId;
                     minRank = i;
                 } else {
                     // Fails if the input stream is not properly ordered
                     // TODO: check is not correct (fails when properly ordered)
 //                    assert this.positions[i] == resultId;
                 }
             }
             return minRank;
         }
 
 
         private class ContextComparator implements Comparator<Value> {
             private final int stream;
 
             public ContextComparator(int stream) {
                 this.stream = stream;
             }
 
             @Override
             public int compare(Value o1, Value o2) {
                 for (JoinReference reference : joins) {
                     final int ix = reference.contextIndices[stream];
                     int z = compare(o1.context[ix], o2.context[ix]);
                     if (z != 0)
                         return z;
                 }
                 return Long.compare(o1.id, o2.id);
             }
 
             /**
              * If the context is -1, it is greater than anything else
              */
             private int compare(long a, long b) {
                 if (a == -1)
                     return b == -1 ? 0 : 1;
                 if (b == -1)
                     return -1;
                 return Long.compare(a, b);
             }
         }
     }
 
 }
