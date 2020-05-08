 package example;
 
 //Copyright 2011 Google Inc. All Rights Reserved.
 //Copyright 1996 John Maloney and Mario Wolczko
 //
 //This file is part of GNU Smalltalk.
 //
 //GNU Smalltalk is free software; you can redistribute it and/or modify it
 //under the terms of the GNU General Public License as published by the Free
 //Software Foundation; either version 2, or (at your option) any later version.
 //
 //GNU Smalltalk is distributed in the hope that it will be useful, but WITHOUT
 //ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 //FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 //details.
 //
 //You should have received a copy of the GNU General Public License along with
 //GNU Smalltalk; see the file COPYING.  If not, write to the Free Software
 //Foundation, 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 //
 //Translated first from Smalltalk to JavaScript, and finally to
 //Dart by Google 2008-2010.
 
 //Ported from Dart to Java by Nikolay Botev.
 //Dart Source: https://github.com/dart-lang/benchmark_harness/blob/9f596cda6b8300e69b22cce4004e59f8028f59c5/example/DeltaBlue.dart
 
 /**
 * A Java implementation of the DeltaBlue constraint-solving
 * algorithm, translated from Dart, as described in:
 *
 * "The DeltaBlue Algorithm: An Incremental Constraint Hierarchy Solver"
 *   Bjorn N. Freeman-Benson and John Maloney
 *   January 1990 Communications of the ACM,
 *   also available as University of Washington TR 89-08-06.
 *
 * Beware: this benchmark is written in a grotesque style where
 * the constraint model is built by side-effects from constructors.
 * I've kept it this way to avoid deviating too much from the original
 * implementation.
 */
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import benchmark_harness.BenchmarkBase;
 
 public class DeltaBlueExample {
 
     public static void main(String[] args) {
         new DeltaBlue().report();
     }
 
     /// Benchmark class required to report results.
     static class DeltaBlue extends BenchmarkBase {
 
         public DeltaBlue() {
             super("DeltaBlue");
         }
 
         protected void run() {
             chainTest(100);
             projectionTest(100);
         }
     }
 
 
     /**
      * Strengths are used to measure the relative importance of constraints.
      * New strengths may be inserted in the strength hierarchy without
      * disrupting current constraints.  Strengths cannot be created outside
      * this class, so == can be used for value comparison.
      */
     static class Strength {
 
         final int value;
         final String name;
 
         Strength(int value, String name) {
             this.value = value;
             this.name = name;
         }
 
         Strength nextWeaker() {
             // Note: extracting the array into a static field
             // does not improve performance
             return new Strength[] {WEAKEST, WEAK_DEFAULT, NORMAL, STRONG_DEFAULT,
                     PREFERRED, STRONG_REFERRED}[value];
         }
 
         static boolean stronger(Strength s1, Strength s2) {
             return s1.value < s2.value;
         }
 
         static boolean weaker(Strength s1, Strength s2) {
             return s1.value > s2.value;
         }
 
         static Strength weakest(Strength s1, Strength s2) {
             return weaker(s1, s2) ? s1 : s2;
         }
 
         static Strength strongest(Strength s1, Strength s2) {
             return stronger(s1, s2) ? s1 : s2;
         }
     }
 
 
     //Compile time computed constants.
     static final Strength REQUIRED        = new Strength(0, "required");
     static final Strength STRONG_REFERRED = new Strength(1, "strongPreferred");
     static final Strength PREFERRED       = new Strength(2, "preferred");
     static final Strength STRONG_DEFAULT  = new Strength(3, "strongDefault");
     static final Strength NORMAL          = new Strength(4, "normal");
     static final Strength WEAK_DEFAULT    = new Strength(5, "weakDefault");
     static final Strength WEAKEST         = new Strength(6, "weakest");
 
 
     static abstract class Constraint {
 
         final Strength strength;
 
         Constraint(Strength strength) {
             this.strength = strength;
         }
 
         abstract boolean isSatisfied();
         abstract void markUnsatisfied();
         abstract void addToGraph();
         abstract void removeFromGraph();
         abstract void chooseMethod(int mark);
         abstract void markInputs(int mark);
         abstract boolean inputsKnown(int mark);
         abstract Variable output();
         abstract void execute();
         abstract void recalculate();
 
         /// Activate this constraint and attempt to satisfy it.
         void addConstraint() {
             addToGraph();
             planner.incrementalAdd(this);
         }
 
         /**
          * Attempt to find a way to enforce this constraint. If successful,
          * record the solution, perhaps modifying the current dataflow
          * graph. Answer the constraint that this constraint overrides, if
          * there is one, or nil, if there isn't.
          * Assume: I am not already satisfied.
          */
         Constraint satisfy(int mark) {
             chooseMethod(mark);
             if (!isSatisfied()) {
                 if (strength == REQUIRED) {
                     System.out.println("Could not satisfy a required constraint!");
                 }
                 return null;
             }
             markInputs(mark);
             Variable out = output();
             Constraint overridden = out.determinedBy;
             if (overridden != null) overridden.markUnsatisfied();
             out.determinedBy = this;
             if (!planner.addPropagate(this, mark)) System.out.println("Cycle encountered");
             out.mark = mark;
             return overridden;
         }
 
         void destroyConstraint() {
             if (isSatisfied()) planner.incrementalRemove(this);
             removeFromGraph();
         }
 
         /**
          * Normal constraints are not input constraints.  An input constraint
          * is one that depends on external state, such as the mouse, the
          * keybord, a clock, or some arbitraty piece of imperative code.
          */
         boolean isInput() { return false; };
     }
 
     /**
      * Abstract superclass for constraints having a single possible output variable.
      */
     static abstract class UnaryConstraint extends Constraint {
 
         final Variable myOutput;
         boolean satisfied = false;
 
         UnaryConstraint(Variable myOutput, Strength strength) {
             super(strength);
             this.myOutput = myOutput;
             addConstraint();
         }
 
         /// Adds this constraint to the constraint graph
         void addToGraph() {
             myOutput.addConstraint(this);
             satisfied = false;
         }
 
         /// Decides if this constraint can be satisfied and records that decision.
         void chooseMethod(int mark) {
             satisfied = (myOutput.mark != mark)
                     && Strength.stronger(strength, myOutput.walkStrength);
         }
 
         /// Returns true if this constraint is satisfied in the current solution.
         boolean isSatisfied() { return satisfied; }
 
         void markInputs(int mark) {
             // has no inputs.
         }
 
         /// Returns the current output variable.
         Variable output() { return myOutput; }
 
         /**
          * Calculate the walkabout strength, the stay flag, and, if it is
          * 'stay', the value for the current output of this constraint. Assume
          * this constraint is satisfied.
          */
         void recalculate() {
             myOutput.walkStrength = strength;
             myOutput.stay = !isInput();
             if (myOutput.stay) execute(); // Stay optimization.
         }
 
         /// Records that this constraint is unsatisfied.
         void markUnsatisfied() {
             satisfied = false;
         }
 
         boolean inputsKnown(int mark) { return true; }
 
         void removeFromGraph() {
             if (myOutput != null) myOutput.removeConstraint(this);
             satisfied = false;
         }
     }
 
 
     /**
      * Variables that should, with some level of preference, stay the same.
      * Planners may exploit the fact that instances, if satisfied, will not
      * change their output during plan execution.  This is called "stay
      * optimization".
      */
     static class StayConstraint extends UnaryConstraint {
 
         StayConstraint(Variable v, Strength str) { super(v, str); }
 
         void execute() {
             // Stay constraints do nothing.
         }
     }
 
 
     /**
      * A unary input constraint used to mark a variable that the client
      * wishes to change.
      */
     static class EditConstraint extends UnaryConstraint {
 
         EditConstraint(Variable v, Strength str) { super(v, str); }
 
         /// Edits indicate that a variable is to be changed by imperative code.
         boolean isInput() { return true; }
 
         void execute() {
             // Edit constraints do nothing.
         }
     }
 
 
     //Directions.
     static final int NONE = 1;
     static final int FORWARD = 2;
     static final int BACKWARD = 0;
 
 
     /**
      * Abstract superclass for constraints having two possible output
      * variables.
      */
     static abstract class BinaryConstraint extends Constraint {
 
         Variable v1;
         Variable v2;
         int direction = NONE;
 
         BinaryConstraint(Variable v1, Variable v2, Strength strength) {
             super(strength);
             this.v1 = v1;
             this.v2 = v2;
             // Java: moved to sublcasses to fix init order
             //addConstraint();
         }
 
         /**
          * Decides if this constraint can be satisfied and which way it
          * should flow based on the relative strength of the variables related,
          * and record that decision.
          */
         void chooseMethod(int mark) {
             if (v1.mark == mark) {
                 direction = (v2.mark != mark &&
                         Strength.stronger(strength, v2.walkStrength))
                         ? FORWARD : NONE;
             }
             if (v2.mark == mark) {
                 direction = (v1.mark != mark &&
                         Strength.stronger(strength, v1.walkStrength))
                         ? BACKWARD : NONE;
             }
             if (Strength.weaker(v1.walkStrength, v2.walkStrength)) {
                 direction = Strength.stronger(strength, v1.walkStrength)
                         ? BACKWARD : NONE;
             } else {
                 direction = Strength.stronger(strength, v2.walkStrength)
                         ? FORWARD : BACKWARD;
             }
         }
 
         /// Add this constraint to the constraint graph.
         void addToGraph() {
             v1.addConstraint(this);
             v2.addConstraint(this);
             direction = NONE;
         }
 
         /// Answer true if this constraint is satisfied in the current solution.
         boolean isSatisfied() { return direction != NONE; }
 
         /// Mark the input variable with the given mark.
         void markInputs(int mark) {
             input().mark = mark;
         }
 
         /// Returns the current input variable
         Variable input() { return direction == FORWARD ? v1 : v2; }
 
         /// Returns the current output variable.
         Variable output() { return direction == FORWARD ? v2 : v1; }
 
         /**
          * Calculate the walkabout strength, the stay flag, and, if it is
          * 'stay', the value for the current output of this
          * constraint. Assume this constraint is satisfied.
          */
         void recalculate() {
             Variable ihn = input(), out = output();
             out.walkStrength = Strength.weakest(strength, ihn.walkStrength);
             out.stay = ihn.stay;
             if (out.stay) execute();
         }
 
         /// Record the fact that this constraint is unsatisfied.
         void markUnsatisfied() {
             direction = NONE;
         }
 
         boolean inputsKnown(int mark) {
             Variable i = input();
             return i.mark == mark || i.stay || i.determinedBy == null;
         }
 
         void removeFromGraph() {
             if (v1 != null) v1.removeConstraint(this);
             if (v2 != null) v2.removeConstraint(this);
             direction = NONE;
         }
     }
 
 
     /**
      * Relates two variables by the linear scaling relationship: "v2 =
      * (v1 * scale) + offset". Either v1 or v2 may be changed to maintain
      * this relationship but the scale factor and offset are considered
      * read-only.
      */
 
     static class ScaleConstraint extends BinaryConstraint {
 
         final Variable scale;
         final Variable offset;
 
         ScaleConstraint(Variable src, Variable scale, Variable offset,
                 Variable dest, Strength strength) {
             super(src, dest, strength);
             this.scale = scale;
             this.offset = offset;
             addConstraint();
         }
 
         /// Adds this constraint to the constraint graph.
         void addToGraph() {
             super.addToGraph();
             scale.addConstraint(this);
             offset.addConstraint(this);
         }
 
         void removeFromGraph() {
             super.removeFromGraph();
             if (scale != null) scale.removeConstraint(this);
             if (offset != null) offset.removeConstraint(this);
         }
 
         void markInputs(int mark) {
             super.markInputs(mark);
             scale.mark = offset.mark = mark;
         }
 
         /// Enforce this constraint. Assume that it is satisfied.
         void execute() {
             if (direction == FORWARD) {
                 v2.value = v1.value * scale.value + offset.value;
             } else {
                 v1.value = (v2.value - offset.value) / scale.value;
             }
         }
 
         /**
          * Calculate the walkabout strength, the stay flag, and, if it is
          * 'stay', the value for the current output of this constraint. Assume
          * this constraint is satisfied.
          */
         void recalculate() {
             Variable ihn = input(), out = output();
             out.walkStrength = Strength.weakest(strength, ihn.walkStrength);
             out.stay = ihn.stay && scale.stay && offset.stay;
             if (out.stay) execute();
         }
 
     }
 
 
     /**
      * Constrains two variables to have the same value.
      */
     static class EqualityConstraint extends BinaryConstraint {
 
         EqualityConstraint(Variable v1, Variable v2, Strength strength)
         { super(v1, v2, strength); addConstraint(); }
 
         /// Enforce this constraint. Assume that it is satisfied.
         void execute() {
             output().value = input().value;
         }
     }
 
 
     /**
      * A constrained variable. In addition to its value, it maintain the
      * structure of the constraint graph, the current dataflow graph, and
      * various parameters of interest to the DeltaBlue incremental
      * constraint solver.
      **/
     static class Variable {
 
         List<Constraint> constraints = new ArrayList<Constraint>();
         Constraint determinedBy;
         int mark = 0;
         Strength walkStrength = WEAKEST;
         boolean stay = true;
         int value;
         final String name;
 
         Variable(String name, int value) {
             this.name = name;
             this.value = value;
         }
 
         /**
          * Add the given constraint to the set of all constraints that refer
          * this variable.
          */
         void addConstraint(Constraint c) {
             constraints.add(c);
         }
 
         /// Removes all traces of c from this variable.
         void removeConstraint(Constraint c) {
             constraints.remove(c);
             if (determinedBy == c) determinedBy = null;
         }
     }
 
 
     static class Planner {
 
         int currentMark = 0;
 
         /**
          * Attempt to satisfy the given constraint and, if successful,
          * incrementally update the dataflow graph.  Details: If satifying
          * the constraint is successful, it may override a weaker constraint
          * on its output. The algorithm attempts to resatisfy that
          * constraint using some other method. This process is repeated
          * until either a) it reaches a variable that was not previously
          * determined by any constraint or b) it reaches a constraint that
          * is too weak to be satisfied using any of its methods. The
          * variables of constraints that have been processed are marked with
          * a unique mark value so that we know where we've been. This allows
          * the algorithm to avoid getting into an infinite loop even if the
          * constraint graph has an inadvertent cycle.
          */
         void incrementalAdd(Constraint c) {
             int mark = newMark();
             for(Constraint overridden = c.satisfy(mark);
                     overridden != null;
                     overridden = overridden.satisfy(mark));
         }
 
         /**
          * Entry point for retracting a constraint. Remove the given
          * constraint and incrementally update the dataflow graph.
          * Details: Retracting the given constraint may allow some currently
          * unsatisfiable downstream constraint to be satisfied. We therefore collect
          * a list of unsatisfied downstream constraints and attempt to
          * satisfy each one in turn. This list is traversed by constraint
          * strength, strongest first, as a heuristic for avoiding
          * unnecessarily adding and then overriding weak constraints.
          * Assume: [c] is satisfied.
          */
         void incrementalRemove(Constraint c) {
             Variable out = c.output();
             c.markUnsatisfied();
             c.removeFromGraph();
             List<Constraint> unsatisfied = removePropagateFrom(out);
             Strength strength = REQUIRED;
             do {
                 for (int i = 0; i < unsatisfied.size(); i++) {
                     Constraint u = unsatisfied.get(i);
                     if (u.strength == strength) incrementalAdd(u);
                 }
                 strength = strength.nextWeaker();
             } while (strength != WEAKEST);
         }
 
         /// Select a previously unused mark value.
         int newMark() { return ++currentMark; }
 
         /**
          * Extract a plan for resatisfaction starting from the given source
          * constraints, usually a set of input constraints. This method
          * assumes that stay optimization is desired; the plan will contain
          * only constraints whose output variables are not stay. Constraints
          * that do no computation, such as stay and edit constraints, are
          * not included in the plan.
          * Details: The outputs of a constraint are marked when it is added
          * to the plan under construction. A constraint may be appended to
          * the plan when all its input variables are known. A variable is
          * known if either a) the variable is marked (indicating that has
          * been computed by a constraint appearing earlier in the plan), b)
          * the variable is 'stay' (i.e. it is a constant at plan execution
          * time), or c) the variable is not determined by any
          * constraint. The last provision is for past states of history
          * variables, which are not stay but which are also not computed by
          * any constraint.
          * Assume: [sources] are all satisfied.
          */
         Plan makePlan(List<Constraint> sources) {
             int mark = newMark();
             Plan plan = new Plan();
             List<Constraint> todo = sources;
             while (todo.size() > 0) {
                 Constraint c = todo.remove(todo.size() - 1);//removeLast();
                 if (c.output().mark != mark && c.inputsKnown(mark)) {
                     plan.addConstraint(c);
                     c.output().mark = mark;
                     addConstraintsConsumingTo(c.output(), todo);
                 }
             }
             return plan;
         }
 
         /**
          * Extract a plan for resatisfying starting from the output of the
          * given [constraints], usually a set of input constraints.
          */
         Plan extractPlanFromConstraints(List<Constraint> constraints) {
             List<Constraint> sources = new ArrayList<Constraint>();
             for (int i = 0; i < constraints.size(); i++) {
                 Constraint c = constraints.get(i);
                 // if not in plan already and eligible for inclusion.
                 if (c.isInput() && c.isSatisfied()) sources.add(c);
             }
             return makePlan(sources);
         }
 
         /**
          * Recompute the walkabout strengths and stay flags of all variables
          * downstream of the given constraint and recompute the actual
          * values of all variables whose stay flag is true. If a cycle is
          * detected, remove the given constraint and answer
          * false. Otherwise, answer true.
          * Details: Cycles are detected when a marked variable is
          * encountered downstream of the given constraint. The sender is
          * assumed to have marked the inputs of the given constraint with
          * the given mark. Thus, encountering a marked node downstream of
          * the output constraint means that there is a path from the
          * constraint's output to one of its inputs.
          */
         boolean addPropagate(Constraint c, int mark) {
             List<Constraint> todo = new ArrayList<Constraint>();
             todo.add(c);
             while (todo.size() > 0) {
                 Constraint d = todo.remove(todo.size() - 1);//removeLast();
                 if (d.output().mark == mark) {
                     incrementalRemove(c);
                     return false;
                 }
                 d.recalculate();
                 addConstraintsConsumingTo(d.output(), todo);
             }
             return true;
         }
 
         /**
          * Update the walkabout strengths and stay flags of all variables
          * downstream of the given constraint. Answer a collection of
          * unsatisfied constraints sorted in order of decreasing strength.
          */
         List<Constraint> removePropagateFrom(Variable out) {
             out.determinedBy = null;
             out.walkStrength = WEAKEST;
             out.stay = true;
             List<Constraint> unsatisfied = new ArrayList<Constraint>();
             List<Variable> todo = new ArrayList<Variable>();
             todo.add(out);
             while (todo.size() > 0) {
                 Variable v = todo.remove(todo.size() - 1);//removeLast();
                 for (int i = 0; i < v.constraints.size(); i++) {
                     Constraint c = v.constraints.get(i);
                     if (!c.isSatisfied()) unsatisfied.add(c);
                 }
                 Constraint determining = v.determinedBy;
                 for (int i = 0; i < v.constraints.size(); i++) {
                     Constraint next = v.constraints.get(i);
                     if (next != determining && next.isSatisfied()) {
                         next.recalculate();
                         todo.add(next.output());
                     }
                 }
             }
             return unsatisfied;
         }
 
         void addConstraintsConsumingTo(Variable v, List<Constraint> coll) {
             Constraint determining = v.determinedBy;
             for (int i = 0; i < v.constraints.size(); i++) {
                 Constraint c = v.constraints.get(i);
                 if (c != determining && c.isSatisfied()) coll.add(c);
             }
         }
     }
 
 
     /**
      * A Plan is an ordered list of constraints to be executed in sequence
      * to resatisfy all currently satisfiable constraints in the face of
      * one or more changing inputs.
      */
     static class Plan {
         List<Constraint> list = new ArrayList<Constraint>();
 
         void addConstraint(Constraint c) {
             list.add(c);
         }
 
         int size() { return list.size(); }
 
         void execute() {
             for (int i = 0; i < list.size(); i++) {
                 list.get(i).execute();
             }
         }
     }
 
 
     /**
      * This is the standard DeltaBlue benchmark. A long chain of equality
      * constraints is constructed with a stay constraint on one end. An
      * edit constraint is then added to the opposite end and the time is
      * measured for adding and removing this constraint, and extracting
      * and executing a constraint satisfaction plan. There are two cases.
      * In case 1, the added constraint is stronger than the stay
      * constraint and values must propagate down the entire length of the
      * chain. In case 2, the added constraint is weaker than the stay
      * constraint so it cannot be accomodated. The cost in this case is,
      * of course, very low. Typical situations lie somewhere between these
      * two extremes.
      */
     static void chainTest(int n) {
         planner = new Planner();
         Variable prev = null, first = null, last = null;
         // Build chain of n equality constraints.
         for (int i = 0; i <= n; i++) {
             Variable v = new Variable("v", 0);
             if (prev != null) new EqualityConstraint(prev, v, REQUIRED);
             if (i == 0) first = v;
             if (i == n) last = v;
             prev = v;
         }
         new StayConstraint(last, STRONG_DEFAULT);
         EditConstraint edit = new EditConstraint(first, PREFERRED);
         Plan plan = planner.extractPlanFromConstraints(Collections.<Constraint>singletonList(edit));
         for (int i = 0; i < 100; i++) {
             first.value = i;
             plan.execute();
             if (last.value != i) {
                System.out.println("Chain test failed.\n{last.value)\n{i}");
             }
         }
     }
 
     /**
      * This test constructs a two sets of variables related to each
      * other by a simple linear transformation (scale and offset). The
      * time is measured to change a variable on either side of the
      * mapping and to change the scale and offset factors.
      */
     static void projectionTest(int n) {
         planner = new Planner();
         Variable scale = new Variable("scale", 10);
         Variable offset = new Variable("offset", 1000);
         Variable src = null, dst = null;
 
         List<Variable> dests = new ArrayList<Variable>();
         for (int i = 0; i < n; i++) {
             src = new Variable("src", i);
             dst = new Variable("dst", i);
             dests.add(dst);
             new StayConstraint(src, NORMAL);
             new ScaleConstraint(src, scale, offset, dst, REQUIRED);
         }
         change(src, 17);
         if (dst.value != 1170) System.out.println("Projection 1 failed");
         change(dst, 1050);
         if (src.value != 5) System.out.println("Projection 2 failed");
         change(scale, 5);
         for (int i = 0; i < n - 1; i++) {
             if (dests.get(i).value != i * 5 + 1000) System.out.println("Projection 3 failed");
         }
         change(offset, 2000);
         for (int i = 0; i < n - 1; i++) {
             if (dests.get(i).value != i * 5 + 2000) System.out.println("Projection 4 failed");
         }
     }
 
     static void change(Variable v, int newValue) {
         EditConstraint edit = new EditConstraint(v, PREFERRED);
         Plan plan = planner.extractPlanFromConstraints(Collections.<Constraint>singletonList(edit));
         for (int i = 0; i < 10; i++) {
             v.value = newValue;
             plan.execute();
         }
         edit.destroyConstraint();
     }
 
     static Planner planner;
 
 }
