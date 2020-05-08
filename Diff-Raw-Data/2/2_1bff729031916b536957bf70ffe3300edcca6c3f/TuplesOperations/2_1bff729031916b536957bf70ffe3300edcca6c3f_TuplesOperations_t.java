 /*
  * The contents of this file are subject to the Mozilla Public License
  * Version 1.1 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
  * the License for the specific language governing rights and limitations
  * under the License.
  *
  * The Original Code is the Kowari Metadata Store.
  *
  * The Initial Developer of the Original Code is Plugged In Software Pty
  * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
  * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
  * Plugged In Software Pty Ltd. All Rights Reserved.
  *
  * Contributor(s): N/A.
  *
  * [NOTE: The text of this Exhibit A may differ slightly from the text
  * of the notices in the Source Code files of the Original Code. You
  * should use the text of this Exhibit A rather than the text found in the
  * Original Code Source Code for Your Modifications.]
  *
  */
 
 package org.mulgara.store.tuples;
 
 // Java 2 standard packages
 import java.util.*;
 
 // Log4j
 import org.apache.log4j.*;
 
 // Local packages
 import org.mulgara.query.*;
 import org.mulgara.resolver.spi.*;
 
 /**
  * TQL answer. An answer is a set of solutions, where a solution is a mapping of
  * {@link Variable}s to {@link Value}s.
  *
  * @created 2003-01-30
  *
  * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
  *
  * @version $Revision: 1.12 $
  *
  * @modified $Date: 2005/05/16 11:07:10 $
  *
  * @maintenanceAuthor: $Author: amuys $
  *
  * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
  *
  * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In
  *      Software Pty Ltd</A>
  *
  * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
  */
 public abstract class TuplesOperations {
 
   /**
    * Logger. This is named after the class.
    */
   private final static Logger logger =
       Logger.getLogger(TuplesOperations.class.getName());
 
   /**
    * The factory used to generate new {@link Tuples} instances.
    */
   private static TuplesFactory tuplesFactory = TuplesFactory.newInstance();
 
   /**
    * Create a proposition which is always false. This is the additive identity
    * of the relational algebra: appending the empty value to a tuples leaves it
    * unchanged. By duality, it's also the multiplicative zero: joining the empty
    * value to a tuples generates an empty result.
    *
    * @return the expression which is never satisfied, no matter what value any
    *      variable takes
    */
   public static StoreTuples empty() {
 
     return EmptyTuples.getInstance();
   }
 
   /**
    * Create a proposition which is always true.
    *
    * This is the multiplicative
    * identity of the relational algebra: joining the unconstrained value to a
    * tuples leaves it unchanged. By duality, it's also the additive zero:
    * appending the unconstrained value to a tuples generates an unconstrained
    * result.
    *
    * @return the expression which is always true, for any value of any variables
    */
   public static StoreTuples unconstrained() {
 
     return UnconstrainedTuples.getInstance();
   }
 
   /**
    * Assign a value to a variable, representing the binding as a tuples with one
    * row and one column.
    *
    * @param variable PARAMETER TO DO
    * @param value PARAMETER TO DO
    * @return RETURNED VALUE TO DO
    */
   public static Tuples assign(Variable variable, long value) {
 
     return (value == Tuples.UNBOUND) ? (Tuples)unconstrained()
                                      : new Assignment(variable, value);
   }
 
   /**
    * This is approximately a disjunction.
    *
    * @param lhs PARAMETER TO DO
    * @param rhs PARAMETER TO DO
    * @return RETURNED VALUE TO DO
    * @throws TuplesException if the append fails
    */
   public static Tuples append(Tuples lhs, Tuples rhs) throws TuplesException {
     return append(Arrays.asList(new Tuples[] { lhs, rhs }));
   }
 
 
   public static Tuples append(List args) throws TuplesException {
     if (logger.isDebugEnabled()) {
       logger.debug("Appending " + args);
     }
 
     HashSet variableSet = new HashSet();
     List variables = new ArrayList();
     boolean unionCompat = true;
     Variable[] leftVars = null;
     List operands = new ArrayList();
     Iterator i = args.iterator();
     while (i.hasNext()) {
       Tuples operand = (Tuples)i.next();
       if (operand.isUnconstrained()) {
         closeOperands(operands);
         if (logger.isDebugEnabled()) {
           logger.debug("Returning unconstrained from append.");
         }
         return unconstrained();
       } else if (operand.getRowCardinality() == Cursor.ZERO) {
         if (logger.isDebugEnabled()) {
           logger.debug("Ignoring append operand " + operand + " with rowcount = " + operand.getRowCount());
         }
         continue;
       }
 
       operands.add(operand.clone());
 
       Variable[] vars = operand.getVariables();
       if (leftVars == null) {
         leftVars = vars;
       } else {
         unionCompat = unionCompat && Arrays.equals(leftVars, vars);
       }
       for (int j = 0; j < vars.length; j++) {
         if (!variableSet.contains(vars[j])) {
           variableSet.add(vars[j]);
           variables.add(vars[j]);
         }
       }
     }
 
     if (logger.isDebugEnabled()) {
       logger.debug("Operands after append-unification: " + operands);
     }
 
     if (operands.isEmpty()) {
       if (logger.isDebugEnabled()) {
         logger.debug("Returning empty from append.");
       }
       return empty();
     }
 
     if (operands.size() == 1) {
       if (logger.isDebugEnabled()) {
         logger.debug("Returning singleton from append.");
       }
       return (Tuples)operands.get(0);
     }
 
     if (unionCompat) {
       if (logger.isDebugEnabled()) {
         logger.debug("Columns are union-compatible");
         logger.debug("Returning OrderedAppend from Union compatible append.");
       }
       Tuples result = new OrderedAppend((Tuples[])operands.toArray(new Tuples[0]));
       closeOperands(operands);
       return result;
     } else {
       List projected = new ArrayList();
       i = operands.iterator();
       while (i.hasNext()) {
         Tuples operand = (Tuples)i.next();
         Tuples proj = project(operand, variables);
         projected.add(proj);
         operand.close();
       }
 
       if (logger.isDebugEnabled()) {
         logger.debug("Returning OrderedAppend from Non-Union compatible append.");
       }
       Tuples result = new OrderedAppend((Tuples[])projected.toArray(new Tuples[0]));
       closeOperands(projected);
       return result;
     }
   }
 
 
   private static String printArgs(String header, List args) {
     StringBuffer buff = new StringBuffer(header + "[");
     Iterator i = args.iterator();
     if (i.hasNext()) {
       buff.append(tuplesSummary((Tuples)i.next()));
     }
 
     while (i.hasNext()) {
       buff.append(", " + tuplesSummary((Tuples)i.next()));
     }
     buff.append("]");
     return buff.toString();
   }
 
 
   /**
    * This is approximately a conjunction.
    */
   public static Tuples join(Tuples lhs, Tuples rhs) throws TuplesException {
     return join(Arrays.asList(new Tuples[] { lhs, rhs }));
   }
 
   public static Tuples join(List args) throws TuplesException {
     try {
       if (logger.isDebugEnabled()) {
         logger.debug(printArgs("Flattening args:", args));
       }
       List operands = flattenOperands(args);
 
       if (logger.isDebugEnabled()) {
         logger.debug(printArgs("Unifying args: ", operands));
       }
       List unified = unifyOperands(operands);
 
       if (logger.isDebugEnabled()) {
         logger.debug(printArgs("Sorting args:", unified));
       }
       List sorted = sortOperands(unified);
 
       if (logger.isDebugEnabled()) {
         logger.debug(printArgs("Preparing result: ", sorted));
       }
       switch (sorted.size()) {
         case 0:
           if (logger.isDebugEnabled()) {
             logger.debug("Short-circuit empty");
           }
           return empty();
 
         case 1:
           if (logger.isDebugEnabled()) {
             logger.debug("Short-circuit singleton");
           }
           return (Tuples)sorted.get(0);
 
         default:
           if (logger.isDebugEnabled()) {
             logger.debug("return UnboundJoin");
           }
           Tuples result = new UnboundJoin((Tuples[]) sorted.toArray(new Tuples[0]));
           closeOperands(sorted);
           return result;
       }
     } catch (RuntimeException re) {
       logger.warn("RuntimeException thrown in join", re);
       throw re;
     } catch (TuplesException te) {
       logger.warn("TuplesException thrown in join", te);
       throw te;
     }
   }
 
 
   /**
    * This is approximately a subtraction.
    */
   public static Tuples subtract(Tuples minuend, Tuples subtrahend) throws TuplesException {
     try {
 
       if (logger.isDebugEnabled()) {
         logger.debug("subtracting " + subtrahend + " from " + minuend);
       }
       // get the matching columns
       Set matchingVars = getMatchingVars(minuend, subtrahend);
       if (matchingVars.isEmpty()) {
         // check to see if the subtrahend is empty
        if (subtrahend.getVariables().length == 0 || minuend.getVariables().length == 0) {
           return (Tuples)minuend.clone();
         }
         throw new TuplesException("Unable to subtract: no common variables.");
       }
       // reorder the subtrahend as necessary
       Tuples sortedSubtrahend;
       // check if there are variables which should not be considered when sorting
       if (checkForExtraVariables(subtrahend, matchingVars)) {
         // yes, there are extra variables
         logger.debug("removing extra variables not needed in subtraction");
         // project out the extra variables (sorting happens in projection)
         sortedSubtrahend = project(subtrahend, new ArrayList(matchingVars));
       } else {
         // there were no extra variables in the subtrahend
         logger.debug("All variables needed");
         // check if the data is already sorted
         sortedSubtrahend = (null == subtrahend.getComparator()) ? sort(subtrahend) : subtrahend;
       }
       // return the difference
       return new Difference(minuend, sortedSubtrahend);
 
     } catch (RuntimeException re) {
       logger.warn("RuntimeException thrown in subtraction", re);
       throw re;
     } catch (TuplesException te) {
       logger.warn("TuplesException thrown in substraction", te);
       throw te;
     }
   }
 
 
   /**
    * Flattens any nested joins to allow polyadic join operations.
    */
   private static List flattenOperands(List operands) throws TuplesException {
     List result = new ArrayList();
     Iterator i = operands.iterator();
     while (i.hasNext()) {
       result.addAll(flattenOperand((Tuples)i.next()));
     }
 
     return result;
   }
 
 
   private static List flattenOperand(Tuples operand) throws TuplesException {
     List operands = new ArrayList();
     if (operand instanceof UnboundJoin) {
       Iterator i = operand.getOperands().iterator();
       while (i.hasNext()) {
         operands.add(((Tuples)i.next()).clone());
       }
       operand.close();
     } else {
       operands.add(operand.clone());
     }
 
     return operands;
   }
 
 
   /**
    * Unifies bound variables in operands.
    * Prepends a LiteralTuples containing constrained variable bindings.
    * If any operand returns 0-rows returns EmptyTuples.
    *
    * @param operands List of Tuples to unify.  Consumed by this function.
    * @return List of operands remaining after full unification.
    */
   private static List unifyOperands(List operands) throws TuplesException {
     Map bindings = new HashMap();
 
     if (!bindSingleRowOperands(bindings, operands)) {
       closeOperands(operands);
       logger.debug("Returning empty due to shortcircuiting initial bindSingleRowOperands");
       return new ArrayList(Collections.singletonList(empty()));
     }
 
     List result = extractNonReresolvableTuples(operands);
 
     List reresolved;
     do {
       reresolved = resolveNewlyBoundFreeNames(operands, bindings);
       if (!bindSingleRowOperands(bindings, reresolved)) {
         closeOperands(operands);
         closeOperands(result);
         closeOperands(reresolved);
         logger.debug("Returning empty due to shortcircuiting progressive bindSingleRowOperands");
         return new ArrayList(Collections.singletonList(empty()));
       }
       operands.addAll(reresolved);
     } while (reresolved.size() != 0);
 
     result.addAll(operands);
     result.add(createTuplesFromBindings(bindings));
 
     return result;
   }
 
 
   /**
    * Extracts all bound names from workingSet into bindings.
    */
   private static boolean bindSingleRowOperands(Map bindings, List workingSet)
       throws TuplesException {
     Iterator iter = workingSet.iterator();
     while (iter.hasNext()) {
       Tuples tuples = (Tuples)iter.next();
 
       switch ((int)tuples.getRowCardinality()) {
         case Cursor.ZERO:
           return false;
 
         case Cursor.ONE:
           Variable[] vars = tuples.getVariables();
           tuples.beforeFirst();
           if (tuples.next()) {
             for (int i = 0; i < vars.length; i++) {
               Long value = new Long(tuples.getColumnValue(tuples.getColumnIndex(vars[i])));
               Long oldValue = (Long)bindings.put(vars[i], value);
               if (oldValue != null && !value.equals(oldValue)) {
                 return false;
               }
             }
           } else {
             // This should not happen.
             // If the call to getRowCardinality returns > 0 then beforeFirst,
             // and then next should return true too.
             logger.error(
                 "No rows but getRowCardinality returned Cursor.ONE: (class=" +
                 tuples.getClass().getName() + ") " + tuples.toString(),
                 new Throwable()
             );
             throw new AssertionError(
                 "No rows but getRowCardinality returned Cursor.ONE"
             );
           }
           iter.remove();
           tuples.close();
           break;
 
         case Cursor.MANY:
           continue;
 
         default:
           throw new TuplesException("getRowCardinality() returned other than ZERO, ONE, or MANY");
       }
     }
 
     return true;
   }
 
 
   private static List extractNonReresolvableTuples(List workingSet)
       throws TuplesException {
     List nonReresolvable = new ArrayList(workingSet.size());
 
     Iterator iter = workingSet.iterator();
     while (iter.hasNext()) {
       Tuples operand = (Tuples)iter.next();
       if (!(operand instanceof ReresolvableResolution)) {
         nonReresolvable.add(operand);
         iter.remove();
       }
     }
 
     return nonReresolvable;
   }
 
 
   /**
    * Compares the free names in the working-set against the current bindings
    * and resolves any constraints found with bindings.
    * @return List of ConstrainedTuples resulting from any resolutions required.
    */
   private static List resolveNewlyBoundFreeNames(List workingSet, Map bindings)
       throws TuplesException {
     List reresolved = new ArrayList();
     Iterator iter = workingSet.iterator();
     while (iter.hasNext()) {
       ReresolvableResolution tuples = (ReresolvableResolution) iter.next();
       ReresolvableResolution updated = tuples.reresolve(bindings);
       if (updated != null) {
         reresolved.add(updated);
         tuples.close();
         iter.remove();
       }
     }
 
     return reresolved;
   }
 
 
   private static Tuples createTuplesFromBindings(Map bindings)
       throws TuplesException {
     if (bindings.isEmpty()) {
       return unconstrained();
     }
 
     Variable[] vars = (Variable[])bindings.keySet().toArray(new Variable[0]);
 
     long[] values = new long[vars.length];
     for (int i = 0; i < values.length; i++) {
       values[i] = ((Long)bindings.get(vars[i])).longValue();
     }
 
     LiteralTuples tuples = new LiteralTuples(vars);
     tuples.appendTuple(values);
 
     return tuples;
   }
 
 
   /**
    * Calls close on all tuples in operands list.
    */
   private static void closeOperands(List operands) throws TuplesException {
     Iterator i = operands.iterator();
     while (i.hasNext()) {
       ((Tuples)i.next()).close();
     }
   }
 
 
   /**
    * Sorts operands by weighted row count in-place.
    * Each row count is discounted by the number of free-names bound to its left.
    * Weighted-row-count = row-count ^ (free-after-binding / free-before-binding)
    */
   private static List sortOperands(List operands) throws TuplesException {
     Set boundVars = new HashSet();
     List result = new ArrayList();
 
     while (!operands.isEmpty()) {
       Tuples bestTuples = removeBestTuples(operands, boundVars);
 
       if (bestTuples instanceof DefinableResolution) {
         defineIndex((DefinableResolution) bestTuples, boundVars);
       }
 
       // Add all variables that don't contain UNBOUND to boundVars set.
       // Note that the inefficiency this introduces for distributed results
       // can only be eliminated by propagating isColumnEverUnbound through Answer.
       Variable[] vars = bestTuples.getVariables();
       for (int i = 0; i < vars.length; i++) {
         if (!bestTuples.isColumnEverUnbound(i)) {
           boundVars.add(vars[i]);
         }
       }
 
       result.add(bestTuples);
     }
 
     return result;
   }
 
 
   // FIXME: Method too long.  Refactor.
   private static Tuples removeBestTuples(List operands, Set boundVars)
       throws TuplesException {
     ListIterator iter = operands.listIterator();
     Tuples minTuples = null;
     double minRowCount = Double.MAX_VALUE;
     int minIndex = -1;
 
     assert(iter.hasNext());
 
     logger.debug("removeBestTuples");
     while (iter.hasNext()) {
       Tuples tuples = (Tuples)iter.next();
       if (logger.isDebugEnabled()) {
         logger.debug("tuples: " + tuplesSummary(tuples));
       }
 
       // Check tuples meets any mandatory left bindings.
       MandatoryBindingAnnotation bindingRequirements =
           (MandatoryBindingAnnotation)tuples.getAnnotation(MandatoryBindingAnnotation.class);
       if (bindingRequirements != null && !bindingRequirements.meetsRequirement(boundVars)) {
         continue;
       }
 
       Variable[] vars = tuples.getVariables();
       int numLeftBindings = calculateNumberOfLeftBindings(tuples, boundVars);
       if (logger.isDebugEnabled()) {
         logger.debug("numLeftBindings: " + numLeftBindings);
       }
 
       // Basic formula assumes uniform distribution.  So number of rows is the
       // product of the length of each variable taken seperately, hence expected
       // row count for n from m bindings is expected(0 from m)**((m - n) / m).
       // This fails to consider the effect on performance of worst case so we
       // incorporate weighted terms to allow for possible skew on each column.
       // We assume a reducing probability of compounded failure so weight each
       // term by 10**term (0-indexed), this is a fudge factor that needs proper
       // analysis.
       double weightedRowCount = 0.0;
       for (int weight = 0; weight < numLeftBindings + 1; weight++) {
         double term = vars.length > 0
                         ? Math.pow(tuples.getRowUpperBound(), (double)(vars.length - (numLeftBindings - weight)) / vars.length)
                         : tuples.getRowUpperBound();
         weightedRowCount += term / Math.pow(10.0, weight);
       }
 
       if (logger.isDebugEnabled()) {
         logger.debug("weightedRowCount: " + weightedRowCount);
         logger.debug("minRowCount: " + minRowCount);
       }
 
       if (weightedRowCount < minRowCount) {
         minRowCount = weightedRowCount;
         minTuples = tuples;
         minIndex = iter.nextIndex() - 1;
       }
     }
 
     if (minTuples == null) {
       logger.info("Unable to meet ordering constraints with bindings: " + boundVars);
       Iterator i = operands.iterator();
       while (i.hasNext()) {
         logger.info("    Operand: " + tuplesSummary((Tuples)i.next()));
       }
       throw new TuplesException("Unable to meet ordering constraints");
     }
 
     if (logger.isDebugEnabled()) {
       logger.debug("Selected: " + tuplesSummary(minTuples) + " with weightedRowCount: " + minRowCount);
     }
     operands.remove(minIndex);
     return minTuples;
   }
 
 
   private static int calculateNumberOfLeftBindings(Tuples tuples,
       Set boundVars) throws TuplesException {
     int numLeftBindings = 0;
     Variable[] vars = tuples.getVariables();
     // Special case DefinableResolution until all tuples can propagate prefix-definitions.
     if (tuples instanceof DefinableResolution) {
       for (int i = 0; i < vars.length; i++) {
         if (boundVars.contains(vars[i])) {
           numLeftBindings++;
         }
       }
     } else {
       for (int i = 0; i < vars.length; i++) {
         if (boundVars.contains(vars[i])) {
           numLeftBindings++;
         } else {
           break;
         }
       }
     }
 
     return numLeftBindings;
   }
 
 
   private static void defineIndex(DefinableResolution tuples,
       Set boundVars) throws TuplesException {
     boolean[] bound = new boolean[4];
     Constraint constraint = tuples.getConstraint();
     for (int i = 0; i < 4; i++) {
       ConstraintElement elem = constraint.getElement(i);
       if (elem instanceof LocalNode) {
         bound[i] = true;
       } else if (boundVars.contains((Variable)elem)) {
         bound[i] = true;
       } else {
         bound[i] = false;
       }
     }
     if (logger.isDebugEnabled()) {
       logger.debug("Tuples: " + tuplesSummary(tuples));
       logger.debug("binding definition = " + AbstractTuples.toString(bound));
     }
     tuples.defineIndex(bound);
   }
 
 
   /**
    * Relational projection. This eliminates any columns not in the specified
    * list, and eliminates any duplicate rows that result.
    *
    * @param tuples PARAMETER TO DO
    * @param variableList the list of {@link Variable}s to project on
    * @return RETURNED VALUE TO DO
    * @throws TuplesException if the projection operation fails
    */
   public static Tuples project(Tuples tuples, List variableList)
       throws TuplesException {
     try {
 
       boolean noVariables = (variableList == null) || (variableList.size() == 0);
 
       if (tuples.isUnconstrained() ||
          (noVariables && tuples.getRowCardinality() != Cursor.ZERO)) {
 
         if (logger.isDebugEnabled()) {
           logger.debug("returning Unconstrained Tuples.");
         }
 
         return TuplesOperations.unconstrained();
       } else if (tuples.getRowCardinality() == Cursor.ZERO) {
         return empty();
       // If the tuples is not unconstrained, and there's no variables in the SELECT
       // and the tuples is a ConstrainedNegationTuples return empty/false.
       } else if ((noVariables) && (tuples instanceof ConstrainedNegationTuples)) {
         return empty();
       } else {
         if (logger.isDebugEnabled()) {
           logger.debug("Projecting to " + variableList);
         }
 
         // Perform the actual projection
         Tuples oldTuples = tuples;
         tuples = new UnorderedProjection(tuples, variableList);
         assert tuples != oldTuples;
 
         // Test whether creating an unordered projects has removed variables.
         if (tuples.isUnconstrained()) {
           tuples.close();
           return TuplesOperations.unconstrained();
         }
 
         // Eliminate any duplicates
         oldTuples = tuples;
         tuples = removeDuplicates(tuples);
         assert tuples != oldTuples;
         if (tuples == oldTuples) {
           logger.warn("removeDuplicates does not change the underlying tuples");
         } else {
           oldTuples.close();
         }
 
         assert tuples.hasNoDuplicates();
 
         return tuples;
       }
     } catch (TuplesException e) {
       throw new TuplesException("Couldn't perform projection", e);
     }
   }
 
 
   public static Tuples restrict(Tuples tuples, RestrictPredicate pred) throws TuplesException {
     return new RestrictionTuples(tuples, pred);
   }
 
 
   /**
    * Sort into default order, based on the columns and local node numbers.
    *
    * @param tuples the tuples to sort
    * @return RETURNED VALUE TO DO
    * @throws TuplesException if the sorting can't be accomplished
    */
   public static Tuples sort(Tuples tuples) throws TuplesException {
     if (tuples.getComparator() == null) {
       if (tuples.isUnconstrained()) {
         return TuplesOperations.unconstrained();
       } else if (tuples.getRowCardinality() == Cursor.ZERO) {
         tuples = empty();
       } else {
         if (logger.isDebugEnabled()) {
           logger.debug("Sorting " + tuples.getRowCount() + " rows");
         }
 
         tuples = tuplesFactory.newTuples(tuples);
         assert tuples.getComparator() != null;
       }
 
       if (logger.isDebugEnabled()) {
         logger.debug("Sorted " + tuples.getRowCount() + " rows");
       }
 
       return tuples;
     } else {
       return (Tuples) tuples.clone();
     }
   }
 
   /**
    * Sort into a specified order.
    *
    * @param tuples the tuples to sort
    * @param rowComparator the ordering
    * @return RETURNED VALUE TO DO
    * @throws TuplesException if the sorting can't be accomplished
    */
   public static Tuples sort(Tuples tuples,
       RowComparator rowComparator) throws TuplesException {
 
     if (!rowComparator.equals(tuples.getComparator())) {
 
       tuples = tuplesFactory.newTuples(tuples, rowComparator);
 
       if (logger.isDebugEnabled()) {
         logger.debug("Sorted: " + tuples + " (using supplied row comparator)");
       }
 
       return tuples;
     }
     else {
 
       return (Tuples) tuples.clone();
     }
   }
 
   /**
    * Truncate a tuples to have no more than a specified number of rows. This
    * method removes rows from the end of the tuples; to remove rows from the
    * start of the tuples, the {@link #offset} method can be used. If the limit
    * is larger than number of rows, the result is unchanged.
    *
    * @param tuples  the instance to limit
    * @param rowCount the number of leading rows to retain
    * @return the truncated tuples
    * @throws TuplesException EXCEPTION TO DO
    */
   public static Tuples limit(Tuples tuples, long rowCount)
     throws TuplesException {
     return new LimitedTuples((Tuples) tuples.clone(), rowCount);
   }
 
   /**
    * If a tuples is virtual, evaluate and store it.
    *
    * @param tuples the instance to materialize
    * @return RETURNED VALUE TO DO
    * @throws TuplesException EXCEPTION TO DO
    */
   public static Tuples materialize(Tuples tuples) throws TuplesException {
 
     if (tuples.isMaterialized()) {
 
       return (Tuples) tuples.clone();
     }
     else {
 
       return tuplesFactory.newTuples(tuples);
     }
   }
 
   /**
    * Skip a specified number of rows from the beginning of a tuples. This method
    * removes rows from the beginning of the tuples; to remove rows from the end
    * of the tuples, the {@link #limit} method can be used. If more rows are
    * removed than are present, an empty tuples is produced.
    *
    * @param tuples  the instance to offset
    * @param rowCount the number of leading rows to remove
    * @return the remaining rows, if any
    * @throws TuplesException EXCEPTION TO DO
    */
   public static Tuples offset(Tuples tuples, long rowCount)
       throws TuplesException {
     return new OffsetTuples((Tuples) tuples.clone(), rowCount);
   }
 
   /**
    * Filter out duplicate rows.
    *
    * @param tuples PARAMETER TO DO
    * @return RETURNED VALUE TO DO
    * @throws TuplesException EXCEPTION TO DO
    */
   public static Tuples removeDuplicates(Tuples tuples) throws TuplesException {
 
     if (tuples.hasNoDuplicates()) {
       if (logger.isDebugEnabled()) {
         logger.debug("Didn't need to remove duplicates");
       }
       return (Tuples) tuples.clone();
     }
 
     if (logger.isDebugEnabled()) {
       logger.debug("Removing duplicates");
     }
 
     if (tuples.getComparator() == null) {
       Tuples oldTuples = tuples;
       tuples = sort(tuples);
       assert tuples != oldTuples;
       // leave the original tuples.  We may not touch it.
 
       if (!tuples.hasNoDuplicates()) {
         oldTuples = tuples;
         tuples = new DistinctTuples(tuples);
         assert tuples != oldTuples;
         oldTuples.close();
       }
 
       return tuples;
     }
     else {
       if (logger.isDebugEnabled()) {
         logger.debug("Already sorted: " + tuples);
       }
 
       Tuples result = new DistinctTuples(tuples);
       return result;
     }
   }
 
 
   public static String formatTuplesTree(Tuples tuples) {
     return indentedTuplesTree(tuples, "").toString();
   }
 
 
   public static StringBuffer tuplesSummary(Tuples tuples) {
     StringBuffer buff = new StringBuffer();
 
     buff.append(tuples.getClass().toString());
 
     buff.append("<" + System.identityHashCode(tuples) + ">");
     buff.append("[");
     if (!tuples.isMaterialized()) {
       buff.append("~");
     } else {
       buff.append("=");
     }
     try {
       buff.append(tuples.getRowUpperBound()).append("]");
     } catch (TuplesException et) {
       buff.append(et.toString()).append("]");
     }
 
     buff.append(" {");
     Variable[] vars = tuples.getVariables();
     if (vars.length > 0) {
       buff.append(vars[0].toString());
       for (int i = 1; i < vars.length; i++) {
         buff.append(", " + vars[i].toString());
       }
     }
     buff.append("}");
 
     try {
       MandatoryBindingAnnotation mba = (MandatoryBindingAnnotation)tuples.getAnnotation(MandatoryBindingAnnotation.class);
       if (mba != null) {
         buff.append(" :: MBA{ " + mba.requiredVariables() + " }");
       }
     } catch (TuplesException et) {
       logger.error("Failed to obtain annotation", et);
     }
 
     return buff;
   }
 
 
   /**
    * Find the list of variables which appear in both the lhs and rhs tuples.
    *
    * @param lhs The first tuples to check the variables of.
    * @param rhs The second tuples to check the variables of.
    * @return A set containing all of the shared variables from lhs and rhs.
    */
   static Set getMatchingVars(Tuples lhs, Tuples rhs) {
     // get all the variables from the lhs
     Set commonVarSet = new HashSet(Arrays.asList(lhs.getVariables()));
     // get all the variables from the rhs
     Set rhsVars = new HashSet(Arrays.asList(rhs.getVariables()));
 
     // find the intersecting set of variables
     commonVarSet.retainAll(rhsVars);
     return commonVarSet;
   }
 
 
   /**
    * Compares a tuples' variables to a set of variables.
    *
    * @param tuples The tuples to check the variables of.
    * @param vars The variables to check for.
    * @return <code>true</code> when all of the tuples' variables are in <code>vars</code>.
    */
   private static boolean checkForExtraVariables(Tuples tuples, Collection vars) {
     // get the variable list
     Variable[] sv = tuples.getVariables();
     for (int i = 0; i < sv.length; i++) {
       if (!vars.contains(sv[i])) {
         // extra variable
         return true;
       }
     }
     return false;
   }
 
 
   private static StringBuffer indentedTuplesTree(Tuples tuples, String indent) {
 
     StringBuffer buff = new StringBuffer();
 
     buff.append("\n").append(indent).append("(").append(tuplesSummary(tuples));
 
     Iterator it = tuples.getOperands().iterator();
     while (it.hasNext()) {
       buff.append(" ").append(indentedTuplesTree((Tuples)it.next(), indent + ".   "));
     }
 
     buff.append(")");
 
     return buff;
   }
 
 
   /**
    * Count the number of leading columns and order has that are included in a
    * set variables.
    *
    * @param variableSet the set of variables
    * @param order the order
    * @return RETURNED VALUE TO DO
    */
   private static int leadingBindings(Tuples order, Set variableSet) {
 
     Variable[] variables = order.getVariables();
     int i = 0;
 
     while ( (i < variables.length) && variableSet.contains(variables[i])) {
 
       i++;
     }
 
     return i;
   }
 
   /**
    * Calculate projection orderings that allow indexed joins to function
    * optimally.
    *
    * @param self the tuples for which to generate a projection ordering
    * @param other the tuples the generated projection ordering should match
    * @return the projection ordering
    */
   private static List generateProjectionOrder(Tuples self, Tuples other) {
 
     // Calculate a new column ordering for self which moves all the columns
     // it has in common with the other into its prefix
     LinkedList selfColumns = new LinkedList(Arrays.asList(self.getVariables()));
     List otherColumns = Arrays.asList(other.getVariables());
 
     LinkedList suffix = new LinkedList(Arrays.asList(self.getVariables()));
     suffix.removeAll(otherColumns);
 
     selfColumns.retainAll(otherColumns);
     selfColumns.addAll(suffix);
 
     return selfColumns;
   }
 
 }
