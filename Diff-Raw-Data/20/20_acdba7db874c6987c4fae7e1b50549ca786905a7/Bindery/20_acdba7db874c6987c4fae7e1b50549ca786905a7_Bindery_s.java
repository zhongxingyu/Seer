/* $Id: Bindery.java,v 1.7 2003/09/16 04:45:29 vpapad Exp $
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
 
 /*
     Bindery
     ========
    All rule based optimizers must bind patterns to expressions in the
    search space.
    For example, consider the LTOR join associative rule, which includes 
    two member data, Pattern and Substitute.  Here L(i) stands for the 
    LeafOp with index i:
 		Pattern: (L(1) join L(2)) join L(3)
 		Substitute: L(1) join (L(2) join L(3))
    Each time the optimizer applies this rule, it must bind the pattern to an
    expression in the search space.  A sample binding is to the expression
 		(G7 join G4) join G10
    where Gi is the group with GROUP_NO i.
 
    A Bindery object (a bindery) performs the nontrivial task of 
    identifying all bindings for a given pattern.  A Bindery object will, 
    over its lifetime, produce all such bindings.
 
    In order to produce a binding, a bindery must spawn one bindery for each
    input subgroup.  For example, consider a bindery for the LTOR 
    associativity rule.  It will spawn a bindery for the left input, which
    will seek all bindings to the pattern
 	L(1) join L(2)
    and a bindery for the right input, which will seek all bindings 
    for the pattern
 	L(3).
    The right bindery will find only one binding, to the entire right
    input group.  The left bindery will typically find many bindings, 
    one per join in the left input group.
     
    Bindery objects (binderys) are of two types.  Expression binderys
    bind the pattern to only one multi-expression in a group.  An
    expression bindery is used by a rule in the top group, to bind 
    a single expression (cf.  ApplyRule::perform).  Group binderys, 
    which are spawned for use in input groups, bind to all 
    expressions in a group.
 
    Because Columbia and its predecessors apply rules only to logical
    expressions, binderys bind logical operators only.
 
    An expression bindery (and even more, a group bindery) may bind
    several EXPRs in the search space.  For example, the pattern 
    (L1 join L2) join L3, applied to the multiexpression G1 join G2, will
    have (L1 join L2) bind any join in G2.  Thus a bindery
    will go through several stages: start, then loop over several valid 
    bindings, then finish.
 
 */
 
 class Node {
     public Bindery bindery;
     public Node next;
     Node() {
         bindery = null;
         next = null;
     }
 }
 
 public class Bindery {
 
     private Expr pattern; // bind with this pattern
 
     private MExpr cur_expr; // bind the pattern to subexpressions of
     //  this multi-expression
 
     private boolean one_expr; // Is this an expression bindery?
 
     private Group group; // group  of the cur_expr
 
     public static class BINDERY_STATE {
         private BINDERY_STATE() {
         }
         public static final BINDERY_STATE START = new BINDERY_STATE();
         // This is a new MExpression
         public static final BINDERY_STATE VALID_BINDING = new BINDERY_STATE();
         // A binding was found.
         public static final BINDERY_STATE FINISHED = new BINDERY_STATE();
         // Finished with this expression
     }
 
     private BINDERY_STATE state;
 
     private SSP ssp;
 
     private Bindery[] input; // binderys for input expr's
     // XXX These 3 vars were # ifdef _REUSE_SIB 
     private Node list, last, currentBind;
 
     //Get the current MExpr
     MExpr getMExpr() {
         return cur_expr;
     }
 
     Bindery(Bindery other) {
         this(other.group, other.pattern, other.ssp);
     }
 
     // Create a Group bindery
     Bindery(Group group, Expr pattern, SSP ssp) {
         state = BINDERY_STATE.START;
         this.group = group;
         this.pattern = pattern;
         input = null;
         one_expr = false; // try all expressions within this group
         assert pattern != null;
         this.ssp = ssp;
     }
 
     // Create an Expression bindery
     Bindery(MExpr expr, Expr pattern, SSP ssp) {
         state = BINDERY_STATE.START;
         cur_expr = expr;
         this.pattern = pattern;
         input = null;
         one_expr = true; // restricted to this log expr           
 
         group = expr.getGroup();
         assert pattern != null;
         this.ssp = ssp;
     }
 
     // If a valid binding has been found, then return the bound Expr.  
     Expr extract_expr() {
         Expr result;
 
         // If pattern is null something weird is happening.
         assert pattern != null;
 
         Op patt_op = pattern.getOp();
 
         // Ensure that there has been a binding, so there is an 
         // expression to extract.
         assert state == BINDERY_STATE.VALID_BINDING
             || state == BINDERY_STATE.FINISHED
             || (patt_op.isLeaf() && state == BINDERY_STATE.START);
 
         // create leaf marked with group index
         if (patt_op.isLeaf()) {
             result = new Expr(new LeafOp(((LeafOp) patt_op).getIndex(), group));
         } // create leaf marked with group index
         else // general invocation of new Expr
             {
             //Top operator in the new Expr will be top operator in cur_expr.  
             //Get it.  (Probably could use patt_op here.)
             Op op_arg = cur_expr.getOp().copy();
 
             //Need the arity of the top operator to construct inputs of new Expr
             int arity = op_arg.getArity();
 
             // Inputs of new Expr can be extracted from binderys stored in
             // input.  Put these in the array subexpr.
             if (arity > 0) {
                 Expr[] subexpr = new Expr[arity];
                 for (int input_no = 0; input_no < arity; input_no++)
                     subexpr[input_no] = input[input_no].extract_expr();
 
                 // Put everything together for the result.
                 result = new Expr(op_arg, subexpr);
             } else
                 result = new Expr(op_arg);
 
         } // general invocation of new Expr
 
         return result;
     } // extract_expr
 
     //advance() requests a bindery to produce its next binding, if one
     //exists.  This may cause the state of the bindery to change.
     //advance() returns true if a binding has been found.
     /*
         Function advance() walks the many trees embedded in the
         MEMO structure in order to find possible bindings.  It is called
         only by ApplyRule::perform.  The walking is done with a finite
         state machine, as follows.
     
         State start:
     	If the pattern is a leaf, we are done.  
     		State = finished
     		Return TRUE
     	Skip over non-logical, non-matching expressions.  
     		State = finished
     		break
     	Create a group bindery for each input and 
     	   try to create a binding for each input.
     	If successful
     		State = valid_binding
     		Return TRUE
     	else
     		delete input binderys
     		State = finished
     		break
     
     
         State valid_binding:
     	Increment input bindings in right-to-left order.
     	If we found a next binding, 
     		State = valid_binding
     		return TRUE
     	else
     		delete input binderys
     		state = finished
     		break
     
     
         State finished
     	If pattern is a leaf //second time through, so we are done
     	   OR 
     	   this is an expr bindery //we finished the first expression, so done
     	   OR
     	   there is no next expression
     		return FALSE
     	else
     		state = start
     		break
     	
     */
     boolean advance() {
         if (ssp.REUSE_SIB) {
             Op patt_op = pattern.getOp();
             // If the pattern is a leaf, we will get one binding, 
             //   to the entire group, then we will be done
             if (patt_op.isLeaf()) {
                 if (state == BINDERY_STATE.START) {
                     state = BINDERY_STATE.FINISHED; //failure next time, but 
                     return true; // success now
                 } else if (state == BINDERY_STATE.FINISHED) {
                     return false;
                 } else {
                     assert false;
                 }
             }
 
             if (!one_expr
                 && state == BINDERY_STATE.START) // begin the group binding
                 { //Search entire group for bindings
                 cur_expr = group.getFirstLogMExpr();
                 // get the first mexpr
             }
 
             // loop until either failure or success
             for (;;) {
                 //PTRACE ("advancing the cur_expr: %s", cur_expr.Dump() );
 
                 // cache some function results
                 Op op_arg = cur_expr.getOp();
                 int arity = op_arg.getArity();
                 int input_no;
 
                 assert op_arg.isLogical();
 
                 // state analysis and transitions
                 if (state == BINDERY_STATE.START) {
                     // is this expression unusable?
                     if (!patt_op.matches(op_arg)) {
                         state = BINDERY_STATE.FINISHED; // try next expression
                     } else if (arity == 0) { // only the Operator, matched
                         state = BINDERY_STATE.VALID_BINDING;
                         return true;
                     } else { // successful bindings for the Operator without inputs
                         // Create a group bindery for each input
                         input = new Bindery[arity];
                         for (input_no = 0; input_no < arity; input_no++) {
                             input[input_no] =
                                 new Bindery(
                                     cur_expr.getInput(input_no),
                                     pattern.getInput(input_no),
                                     ssp);
                             //						if ( ! input[input_no].advance() )
                             //							break; // terminate this loop
                         }
                         // Try to advance each (new) input bindery to a binding
                         // a failure is failure for the expr
                         for (input_no = 0; input_no < arity; input_no++)
                             if (!input[input_no].advance())
                                 break; // terminate this loop
 
                         // check whether all inputs found a binding
                         if (input_no == arity) // successful!
                             {
                             state = BINDERY_STATE.VALID_BINDING;
                             return true;
                         } // successful bindings for new expression
                         else { // otherwise, failure! -- dealloc inputs
                             input = null;
                             state = BINDERY_STATE.FINISHED;
                         }
                     } // if(arity)
                 } else if (state == BINDERY_STATE.VALID_BINDING) {
                     for (input_no = arity; --input_no >= 0;) {
                         if (currentBind == null)
                             // try existing inputs in right-to-left order
                             // first success is overall success
                             {
                             if (input[input_no].advance()) {
                                 for (int other_input_no = input_no;
                                     ++other_input_no < arity;
                                     ) {
                                     // input[other_input_no] = get_first_bindery_in_list;
                                     // currentBind.bindery = input[other_input_no];
                                     input[other_input_no] = list.bindery;
                                     currentBind = list;
                                 }
                                 state = BINDERY_STATE.VALID_BINDING;
                             }
                         } else {
                             currentBind = currentBind.next;
                             if (currentBind != null) {
                                 state = BINDERY_STATE.VALID_BINDING;
                                 input[input_no] = currentBind.bindery;
                                 // input[input_no] = get_next_bindery_in_list;
                                 return true;
                             } else
                                 return false;
                         }
                     }
                     if (arity != 0 && !one_expr) {
                         Node newNode = new Node();
                         Bindery dup = new Bindery(this);
                         //		 dup = this; // ??????
                         newNode.bindery = dup;
                         if (list == null)
                             list = last = newNode;
                         else {
                             last.next = newNode;
                             last = last.next;
                         }
                         // add_to_the_list (this bindery);
                         // ?? input
                     }
                     state = BINDERY_STATE.FINISHED;
                 }
                 /*
                 			// try existing inputs in right-to-left order
                 			// first success is overall success
                 
                 			for (input_no = arity;  -- input_no >= 0; )
                 			{
                 				if ( input[input_no].advance() )
                 				// found one more binding
                 				{
                 					// If we have a new binding in a non-rightmost location,
                 					// we must create new binderys for all inputs to the
                 					// right of input_no, else we will not get all bindings.
                 					//  This is inefficient code since the each input on the
                 					//  right has multiple binderys created for it, and each
                 					//  bindery produces the same bindings as the others.
                 					//  The simplest example of this is the exchange rule.
                 					for (int other_input_no = input_no;
                 							++ other_input_no < arity;  )
                 					{
                 						delete input[other_input_no];
                 						input[other_input_no] = (new Bindery (
                 									cur_expr.GetInput(other_input_no),
                 									pattern .GetInput(other_input_no)) );
                 	
                 						if (! input[other_input_no].advance() )
                 						// Impossible since we found these bindings earlier
                 							ASSERT(false) ; 
                 					}
                 
                 					// return overall success
                 					state = valid_binding;
                 					return true;
                 				} // found one more binding
                 			} // try existing inputs in right-to-left order
                 
                 			// There are no more bindings to this log expr; 
                 			//   dealloc input binderys.
                 			if(arity)
                 			{
                 				for (input_no = arity; -- input_no >= 0;  )
                 					delete input[input_no];
                 				delete [] input; input = null;
                 			}
                 			state = finished;
                 			break;
                 */
                 else if (state == BINDERY_STATE.FINISHED) {
                     if (one_expr
                         || ((cur_expr = cur_expr.getNextMExpr()) == null))
                         return false;
                     else {
                         state = BINDERY_STATE.START;
                     }
                 } else
                     assert false;
             } // loop until either failure or success
         } else { // ! REUSE_SIB
             Op patt_op = pattern.getOp();
             // If the pattern is a leaf, we will get one binding, 
             //   to the entire group, then we will be done
             if (patt_op.isLeaf()) {
                 if (state == BINDERY_STATE.START) {
                     state = BINDERY_STATE.FINISHED; //failure next time, but 
                     return true; // success now
                 } else if (state == BINDERY_STATE.FINISHED) {
                     return false;
                 } else
                     assert false;
             } // if (patt_op . is_leaf ())
 
             if (!one_expr
                 && state == BINDERY_STATE.START) // begin the group binding
                 { //Search entire group for bindings
                 cur_expr = group.getFirstLogMExpr();
                 // get the first mexpr
             }
 
             // loop until either failure or success
             for (;;) {
                 //PTRACE ("advancing the cur_expr: %s", cur_expr.Dump() );
 
                 // cache some function results
                 Op op_arg = cur_expr.getOp();
                 int arity = op_arg.getArity();
                 int input_no;
 
                 assert op_arg.isLogical();
 
                 if (state == BINDERY_STATE.START) {
                     // is this expression unusable?
                     if (!(patt_op.matches(op_arg))) {
                         state = BINDERY_STATE.FINISHED; // try next expression
                     } else if (arity == 0) // only the Operator, matched
                         {
                         state = BINDERY_STATE.VALID_BINDING;
                         return true;
                     } // successful bindings for the Operator without inputs
                     else {
                         // Create a group bindery for each input
                        input = new Bindery[arity];
                        for (input_no = 0; input_no < arity; input_no++)
                            input[input_no] =
                                new Bindery(
                                    cur_expr.getInput(input_no),
                                    pattern.getInput(input_no),
                                    ssp);
 
                         // Try to advance each (new) input bindery to a binding
                         // a failure is failure for the expr
                         for (input_no = 0; input_no < arity; input_no++)
                             if (!input[input_no].advance())
                                 break; // terminate this loop
 
                         // check whether all inputs found a binding
                         if (input_no == arity) // successful!
                             {
                             state = BINDERY_STATE.VALID_BINDING;
                             return true;
                         } // successful bindings for new expression
                         else { // otherwise, failure! -- dealloc inputs
                             input = null;
                             state = BINDERY_STATE.FINISHED;
                         }
                     } // if(arity)
                 } else if (state == BINDERY_STATE.VALID_BINDING) {
                     // try existing inputs in right-to-left order
                     // first success is overall success
                     for (input_no = arity; --input_no >= 0;) {
                         if (input[input_no].advance())
                             // found one more binding
                             {
                             // If we have a new binding in a non-rightmost location,
                             // we must create new binderys for all inputs to the
                             // right of input_no, else we will not get all bindings.
                             //  This is inefficient code since the each input on the
                             //  right has multiple binderys created for it, and each
                             //  bindery produces the same bindings as the others.
                             //  The simplest example of this is the exchange rule.
                             for (int other_input_no = input_no;
                                 ++other_input_no < arity;
                                 ) {
                                 input[other_input_no] =
                                     (new Bindery(cur_expr
                                         .getInput(other_input_no),
                                         pattern.getInput(other_input_no),
                                         ssp));
 
                                 if (!input[other_input_no].advance())
                                     // Impossible since we found these bindings earlier
                                     assert false;
                             }
 
                             // return overall success
                             state = BINDERY_STATE.VALID_BINDING;
                             return true;
                         } // found one more binding
                     } // try existing inputs in right-to-left order
 
                     // There are no more bindings to this log expr; 
                     //   dealloc input binderys.
                     if (arity > 0) {
                         input = null;
                     }
                     state = BINDERY_STATE.FINISHED;
                 } else if (state == BINDERY_STATE.FINISHED) {
                     if (one_expr
                         || ((cur_expr = cur_expr.getNextMExpr()) == null))
                         return false;
                     else {
                         state = BINDERY_STATE.START;
                     }
                 } else {
                     assert false;
                 } // loop until either failure or success
             }
         }
     } // Bindery::advance
 }
