/* $Id: Rule.java,v 1.9 2003/09/17 23:40:46 vpapad Exp $
    Colombia -- Java version of the Columbia Database Optimization Framework
 
    Copyright (c)    Dept. of Computer Science , Portland State
    University and Dept. of  Computer Science & Engineering,
    OGI School of Science & Engineering, OHSU. All Rights Reserved.
 
    Permission to use, copy, modify, and distribute this software and
    its documentation is hereby granted, provided that both the
    copyright notice and this permission notice appear in all copies
    of the software, derivative works or modified versions, and any
    portions thereof, and that both notices appear in supporting
    documentation.
 
    THE AUTHORS, THE DEPT. OF COMPUTER SCIENCE DEPT. OF PORTLAND STATE
    UNIVERSITY AND DEPT. OF COMPUTER SCIENCE & ENGINEERING AT OHSU ALLOW
    USE OF THIS SOFTWARE IN ITS "AS IS" CONDITION, AND THEY DISCLAIM ANY
    LIABILITY OF ANY KIND FOR ANY DAMAGES WHATSOEVER RESULTING FROM THE
    USE OF THIS SOFTWARE.
 
    This software was developed with support of NSF grants IRI-9118360,
    IRI-9119446, IRI-9509955, IRI-9610013, IRI-9619977, IIS 0086002,
    and DARPA (ARPA order #8230, CECOM contract DAAB07-91-C-Q518).
 */
 
 package niagara.optimizer.colombia;
 
 import java.util.BitSet;
 
 /*
     Rules
     =====
     A rule is defined primarily by its pattern and its substitute.
     For example, the LTOR join associative rule has these member data,
     in which L(i) stands for Leaf operator i:
            Pattern: (L(1) join L(2)) join L(3)
         Substitute: L(1) join (L(2) join L(3))
 
     The pattern and substitute describe how to produce new multi-expressions
     in the search space.  The production of these new multi-expressions
     is done by APPLY_perform(), in two parts: 
     First a Bindery object produces a binding of the pattern to an Expr 
     in the search space.  Then next_substitute() produces the new expression, 
     which is integrated into the search space by SSP::include().
 
     A rule is called an implementation rule if its substitute has a top
     physical operator, for example GET_TO_SCAN.
 
     Columbia and its ancestors use only rules for which the pattern has all
     logical operators, and for which the substitute has logical operators
     except perhaps at the top.
 
     In O_EXPR::perform(), the optimizer decides which rules to push onto
     the PTASK stack.  It uses top_match() to checks whether the top operator 
     of a rule matches the top operator of the curent expression.
 
     The method O_EXPR::perform() must decide the order in which rules
     are pushed onto the PTASK stack.  For this purpose it uses promise().
     A promise value of 0 or less means do not schedule this rule here.  
     Higher promise values mean schedule this rule earlier.
     By default, an implementation  rule has a promise of 2 and others
     a promise of 1.  Notice that a rule's promise is evaluated before 
     inputs of the top-most operator are expanded (searched, transformed) 
     and matched against the rule's pattern; the promise value is used 
     to decide whether or not to explore those inputs.
     promise() is used for both exploring and optimizing, though for 
     exploration it is currently irrelevant.
 
     When a rule is applied (in APPLY_perform), and after a binding
     is found, the method condition() is invoked to determine whether 
     the rule actually applies.  condition() has available to it the entire
     Expr matched to the rule's pattern.  For example, the rule which
     pushes a select below a join requires a condition about compatibitily
     of schemas.  This condition cannot be checked until after the binding,
     since schemas of input groups are only available from the binding.
 
     The check() method verifies that a rule seems internally consistent,
     i.e., that a rule's given cardinality is consistent with its given
     pattern, and that pattern and substitute satisfy other
     requirements.  These requirements are:
     - leaves are numbered 0, 1, 2, ..., (arity-1)
     - all leaf numbers up to (arity-1) are used in the pattern
     - each leaf number is used exactly once in the pattern
     - the substitute uses only leaf numbers in the pattern
     - (each leaf number may appear 0, 1, 2, or more times)
     - all operators in the pattern are logical operators
     - all operators except the root in the substitute are logical
 */
 public abstract class Rule {
     // XXX vpapad should be moved to their correspoding operators,
     // or (better?) configurable at rule loading
     protected static final int FILESCAN_PROMISE = 6;
     protected static final int SORT_PROMISE = 7;
     protected static final int MERGE_PROMISE = 5;
     protected static final int HASH_PROMISE = 5;
     protected static final int PHYS_PROMISE = 4; // physical rules
     protected static final int LOG_PROMISE = 2; // logical rules 
     protected static final int ASSOC_PROMISE = 3;
 
     // To set general logical rules including some unnesting rules higher 
     // than the unnesting rules that duplicate expressions. The purpose is 
     // to avoid explosion of the search space. Some rules such as djoin 
     // pushdown rule will mask off the rules generatign duplicate expression. 
     // These ruels are performed before the duplicating rules, as specified by 
     // the promises here.
     public static final int DJOIN_DUP_PROMISE = 1;
     // The promise of the rules that duplicate expressions
     public static final int UNNESTING_PROMISE = 2;
     // other unnesting rules           
 
     protected String name;
     protected Expr pattern; // pattern to match
     protected Expr substitute; // replacement for pattern
 
     protected RuleSet ruleSet;
     
     protected int arity; // number of leaf operators in pattern.
     //  Leaf ops must be numbered 0, 1, 2,..
 
     //Used for unique rule sets
     //Which rules to turn off in "after" expression
     protected BitSet mask;
 
     // Used for efficent unnesting
     // Which rules to turn off in "before" expression
     // Before-masks are assigned in the constructors of the rules
     // Used with promise, they realize that firing certain rules blocks other rules
     // espcially useful for unnesting.
     // It seems like the only place needed to be modified to enable and disable 
     // before-mask is a piece of code in APPLY_Perform()
     protected BitSet beforeMask;
     protected int index; // index in the rule set
 
     public Rule(String name, int arity, Expr pattern, Expr substitute) {
         this.name = name;
         this.arity = arity;
         this.pattern = pattern;
         this.substitute = substitute;
     }
 
     public String getName() {
         return name;
     }
     
     public Expr getPattern() {
         return pattern;
     }
     
     public Expr getSubstitute() {
         return substitute;
     }
 
     boolean canFire(MExpr mexpr) {
         if (ruleSet.isUnique()) return true;
         return mexpr.canFire(index);
     }
     
    boolean topMatch(Op top) {
         // The results of this check are cached in RuleSet
         // We should never apply rules to physical expressions
        assert top instanceof LogicalOp;
 
         Op op = pattern.getOp();
         
         // if pattern is a leaf, means it represents a group, always matches
         // otherwise, the pattern should be logical op
        return (op.isLeaf() || ((LogicalOp) op).opMatch(top.getClass()));
     }
 
     // default value is 1.0, resulting in exhaustive search
     public double promise(Op op_arg, Context context) {
         return (substitute.getOp().isPhysical() ? PHYS_PROMISE : LOG_PROMISE);
     }
 
     // Does before satisfy this rule's condition, if we are using
     // context for the search?  mexpr is the multi-expression bound to
     // before, probably mexpr is not needed.
     // Default value is TRUE, i.e., rule applies
     public boolean condition(Expr before, MExpr mexpr, PhysicalProperty ReqdProp) {
         return true;
     }
 
     //The term next is from Cascades, where rules which produced  many
     // expressions per binding were envisioned.  [For example, in Cesar's
     // unique rule set, one rule could produce both a LTOR and
     // a commute.]
     //Given an expression which is a binding (before), this
     // returns the substitute form (after) of the rule.
 
     // Argument "MExpr * mexpr" is added in by QUAN WANG in DEC/99
     // to let the substitute function access groupid of the current expression.
 
     // XXX Vassilis: It is often the case that determining whether a rule applies 
     // to an expression takes almost as much work as it does to actually 
     // apply the rule. This code and effort would have to be duplicated between 
     // condition and nextSubstitute (for example to check whether a join reordering 
     // produces a cartesian product or not). We now allow nextSubstitute to return 
     // null, to signify that we discovered within nextSubstitute that a rule does not 
     // actually apply.   
     public abstract Expr nextSubstitute(
         Expr before,
         MExpr mexpr,
         PhysicalProperty ReqdProp);
 
     int getIndex() {
         return index;
         
     } // get the rule's index in the rule set
     void setIndex(int index) {
         this.index = index;
     }
     
     /** Initialize masks */
     public void initMasks(int length) {
         mask = new BitSet(length);
         beforeMask = new BitSet(length);
     }
     
     // get the rule's mask
     BitSet getMask() {
         return mask;
     }
 
     // get the rule's before_mask
     BitSet getBeforeMask() {
         return beforeMask;
     }
 
     /** initialize the rule after it has been added to a ruleset */
     public void initialize() {
         ;
     }
     
     protected void maskRule(String ruleName) {
         int idx = ruleSet.getIndex(ruleName);
         if (idx < 0) return;
         mask.set(idx);
     }
 
     protected void maskRuleBefore(String ruleName) {
         int idx = ruleSet.getIndex(ruleName);
         if (idx < 0) return;
         beforeMask.set(idx);
     }
     
     // if not stop generating logical expression when epsilon pruning is applied
     // need these to identify the substitue
 
     boolean isLogicalToPhysical() {
         return (substitute.getOp().isPhysical());
     }
     boolean isLogicalToLogical() {
         return (substitute.getOp().isLogical());
     }
     
     public String toString() {
         return getName();
     }
 
     public void setRuleSet(RuleSet ruleSet) {
         this.ruleSet = ruleSet;
     }
 }
