 package de.unisiegen.tpml.core.smallstep;
 
 import org.apache.log4j.Logger;
 
 import de.unisiegen.tpml.core.AbstractProofRuleSet;
 import de.unisiegen.tpml.core.ProofGuessException;
 import de.unisiegen.tpml.core.ProofNode;
 import de.unisiegen.tpml.core.ProofRule;
 import de.unisiegen.tpml.core.ProofRuleException;
 import de.unisiegen.tpml.core.ProofStep;
 import de.unisiegen.tpml.core.expressions.Expression;
 import de.unisiegen.tpml.core.interpreters.AbstractInterpreterProofModel;
 import de.unisiegen.tpml.core.interpreters.AbstractInterpreterProofNode;
 
 /**
  * The heart of the small step interpreter. Smallstep rules are supplied via an
  * {@link de.unisiegen.tpml.core.smallstep.AbstractSmallStepProofRuleSet} that is passed to the
  * constructor.
  *
  * @author Benedikt Meurer
  * @version $Rev$
  *
  * @see de.unisiegen.tpml.core.interpreters.AbstractInterpreterProofModel
  * @see de.unisiegen.tpml.core.smallstep.SmallStepProofNode
  */
 public final class SmallStepProofModel extends AbstractInterpreterProofModel {
   //
   // Constants
   //
   
   /**
    * The {@link Logger} for this class.
    * 
    * @see Logger
    */
   private static final Logger logger = Logger.getLogger(SmallStepProofModel.class);
   
   
   
   //
   // Constructor
   //
   
   /**
    * Allocates a new <code>SmallStepProofModel</code> with the specified <code>expression</code>
    * as its root node.
    * 
    * @param expression the {@link Expression} for the root node.
    * @param ruleSet the available small step proof rules for the model.
    * 
    * @throws NullPointerException if either <code>expression</code> or <code>ruleSet</code> is
    *                              <code>null</code>.
    *
    * @see AbstractInterpreterProofModel#AbstractInterpreterProofModel(AbstractInterpreterProofNode, AbstractProofRuleSet)
    */
   public SmallStepProofModel(Expression expression, AbstractSmallStepProofRuleSet ruleSet) {
     super(new DefaultSmallStepProofNode(expression), ruleSet);
   }
   
   
   
   //
   // Actions
   //
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.AbstractProofModel#guess(de.unisiegen.tpml.core.ProofNode)
    */
   @Override
   public void guess(ProofNode node) throws ProofGuessException {
     // guess the remaining steps for the node
     ProofStep[] remainingSteps = remaining(node);
     
     // check if the node is already completed
     if (remainingSteps.length == 0) {
       throw new IllegalStateException("Cannot prove an already proven node");
     }
     
     // try to prove using the guessed rule
     try {
       // apply the last rule of the remaining steps to the node
       apply(remainingSteps[remainingSteps.length - 1].getRule(), node);
     }
     catch (ProofRuleException e) {
       // failed to guess
       throw new ProofGuessException(node, e);
     }
   }
 
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.AbstractProofModel#prove(de.unisiegen.tpml.core.ProofRule, de.unisiegen.tpml.core.ProofNode)
    */
   @Override
   public void prove(ProofRule rule, ProofNode node) throws ProofRuleException {
     if (node == null) {
       throw new NullPointerException("node is null");
     }
     if (rule == null) {
       throw new NullPointerException("rule is null");
     }
     if (!this.root.isNodeRelated(node)) {
       throw new IllegalArgumentException("The node is invalid for the model");
     }
     if (!this.ruleSet.contains(rule)) {
       throw new IllegalArgumentException("The rule is invalid for the model");
     }
     
     // apply the rule to the specified node
     try {
       apply(rule, node);
     }
     catch (RuntimeException e) {
       logger.error("An internal error occurred while proving " + node + " using (" + rule + ")", e);
       throw e;
     }
   }
   
   /**
    * Returns the remaining {@link de.unisiegen.tpml.core.ProofStep}s required to prove the specified
    * <code>node</code>. This method is used to guess the next step, see the {@link #guess(ProofNode)}
    * method for further details, and in the user interface, to highlight the next expression.
    * 
    * @param node the {@link de.unisiegen.tpml.core.ProofNode} for which to return the remaining steps
    *             required to prove the <code>node</code>.
    *             
    * @return the remaining {@link ProofStep}s required to prove the <code>node</code>, or an empty
    *         array if the <code>node</code> is already proven or the evaluation is stuck.
    *         
    * @throws IllegalArgumentException if the <code>node</code> is invalid for the model.
    * @throws NullPointerException if <code>node</code> is <code>null</code>.
    */
   public ProofStep[] remaining(ProofNode node) {
     if (node == null) {
       throw new NullPointerException("node is null");
     }
     if (!this.root.isNodeRelated(node)) {
       throw new IllegalArgumentException("The node is invalid for the model");
     }
     
     // evaluate the next step for the node
     DefaultSmallStepProofContext context = new DefaultSmallStepProofContext(node, this.ruleSet);
     
     // determine the completed/evaluated steps
     ProofStep[] completedSteps = node.getSteps();
     ProofStep[] evaluatedSteps = context.getSteps();
 
     // generate the remaining steps
     ProofStep[] remainingSteps = new ProofStep[evaluatedSteps.length - completedSteps.length];
     System.arraycopy(evaluatedSteps, completedSteps.length, remainingSteps, 0, remainingSteps.length);
     return remainingSteps;
   }
   
   
   //
   // Undo/Redo
   //
 
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.AbstractProofModel#addUndoableTreeEdit(de.unisiegen.tpml.core.AbstractProofModel.UndoableTreeEdit)
    */
   @Override
   protected void addUndoableTreeEdit(UndoableTreeEdit edit) {
     // perform the redo of the edit
     edit.redo();
     
     // add to the undo history
     super.addUndoableTreeEdit(edit);
   }
   
   
   
   //
   // Internals
   //
   
   /**
    * Convenience wrapper for the {@link #apply(DefaultSmallStepProofRule, DefaultSmallStepProofNode)}
    * method, which automatically casts to the appropriate types.
    * 
    * @param rule the small step proof rule to apply to <code>node</code>.
    * @param node the node to which to apply the <code>rule</code>.
    * 
    * @throws ProofRuleException if the <code>rule</code> cannot be applied to the <code>node</code>.
    * 
    * @see #apply(DefaultSmallStepProofRule, DefaultSmallStepProofNode)
    */
   private void apply(ProofRule rule, ProofNode node) throws ProofRuleException {
     apply((DefaultSmallStepProofRule)rule, (DefaultSmallStepProofNode)node);
   }
   
   /**
    * Applies the <code>rule</code> to the <code>node</code>. The <code>rule</code> must be any of
    * the remaining rules for <code>node</code>.
    * 
    * @param rule the small step proof rule to apply to <code>node</code>.
    * @param node the node to which to apply the <code>rule</code>.
    * 
    * @throws ProofRuleException if the <code>rule</code> cannot be applied to the <code>node</code>.
    * 
    * @see #apply(ProofRule, ProofNode)
    * @see #remaining(ProofNode)
    */
   private void apply(DefaultSmallStepProofRule rule, final DefaultSmallStepProofNode node) throws ProofRuleException {
     // evaluate the expression and determine the proof steps
     DefaultSmallStepProofContext context = new DefaultSmallStepProofContext(node, this.ruleSet);
     final Expression expression = context.getExpression();
     ProofStep[] evaluatedSteps = context.getSteps();
     
     // determine the completed steps for the node
     final ProofStep[] completedSteps = node.getSteps();
     
     // check if the node is already completed
     if (completedSteps.length == evaluatedSteps.length) {
       // check if the interpreter is stuck
       SmallStepProofRule lastRule = (SmallStepProofRule)evaluatedSteps[evaluatedSteps.length - 1].getRule();
       if (!lastRule.isAxiom()) {
         // the proof is stuck
         throw new ProofRuleException(node, rule);
       }
       else {
         // an internal error in the upper layers
         throw new IllegalStateException("Cannot prove an already proven node");
       }
     }
     else if (completedSteps.length > evaluatedSteps.length) {
       // this is a bug then
       throw new IllegalStateException("completedSteps > evaluatedSteps");
     }
     
     // verify the completed steps
     int n;
     for (n = 0; n < completedSteps.length; ++n) {
       if (!completedSteps[n].getRule().equals(evaluatedSteps[n].getRule()))
         throw new IllegalStateException("Completed steps don't match evaluated steps");
     }
 
     // check if the rule is valid, accepting regular meta-rules for EXN rules
     int m;
     for (m = n; m < evaluatedSteps.length; ++m) {
       if (evaluatedSteps[m].getRule().equals(rule) || evaluatedSteps[m].getRule().equals(rule.toExnRule()))
         break;
     }
     
     // check if rule is invalid
     if (m >= evaluatedSteps.length) {
       throw new ProofRuleException(node, rule);
     }
     
     // determine the new step(s) for the node
     final ProofStep[] newSteps = new ProofStep[m + 1];
     System.arraycopy(evaluatedSteps, 0, newSteps, 0, m + 1);
 
     // check if the node is finished (the last
     // step is an application of an axiom rule)
     if (((SmallStepProofRule)newSteps[m].getRule()).isAxiom()) {
       // create the child node for the node
       final DefaultSmallStepProofNode child = new DefaultSmallStepProofNode(expression, context.getStore());
       
       // add the undoable edit
       addUndoableTreeEdit(new UndoableTreeEdit() {
         public void redo() {
           // update the "finished" state
           setFinished(expression.isException() || expression.isValue());
 
           // apply the new steps and add the child
           node.setSteps(newSteps);
           node.add(child);
           nodesWereInserted(node, new int[] { node.getIndex(child) });
           nodeChanged(node);
         }
         
         public void undo() {
           // update the "finished" state
           setFinished(false);
 
           // remove the child and revert the steps
           int[] indices = { node.getIndex(child) };
           node.remove(child);
           nodesWereRemoved(node, indices, new Object[] { child });
           node.setSteps(completedSteps);
           nodeChanged(node);
         }
       });
     }
     else {
       // add the undoable edit
       addUndoableTreeEdit(new UndoableTreeEdit() {
         public void redo() {
           // apply the new steps
           node.setSteps(newSteps);
           nodeChanged(node);
         }
         
         public void undo() {
           // revert to the previous steps
           node.setSteps(completedSteps);
           nodeChanged(node);
         }
       });
     }
   }
 }
