 package de.unisiegen.tpml.core.smallstep;
 
 import java.util.Vector;
 
 import de.unisiegen.tpml.core.ProofNode;
 import de.unisiegen.tpml.core.ProofRule;
 import de.unisiegen.tpml.core.ProofRuleSet;
 import de.unisiegen.tpml.core.ProofStep;
 import de.unisiegen.tpml.core.expressions.Expression;
 import de.unisiegen.tpml.core.interpreters.DefaultStore;
 import de.unisiegen.tpml.core.interpreters.Store;
 
 /**
  * Default implementation of the <code>SmallStepProofContext</code> interface.
  *
  * @author Benedikt Meurer
  * @version $Rev$
  *
  * @see de.unisiegen.tpml.core.smallstep.SmallStepProofContext
  */
 final class DefaultSmallStepProofContext implements SmallStepProofContext {
   //
   // Attributes
   //
   
   /**
    * The resulting {@link Expression}.
    * 
    * @see #getExpression()
    */
   private Expression expression;
   
   /**
    * The steps in the proof.
    * 
    * @see #getSteps()
    */
   private Vector<ProofStep> steps = new Vector<ProofStep>();
   
   /**
    * The resulting {@link Store}.
    * 
    * @see #getStore()
    */
   private DefaultStore store;
   
   
   
   //
   // Constructor (package)
   //
   
   /**
   * Allocates a new <code>DefaultSmallStepProofContext</code> instance using the specified <code>node</code>
   * and the small step proof <code>ruleSet</code>.
    *
    * @param node the {@link ProofNode} for which to determine the next small steps. The expression
    *             and the store from the <code>node</code> are relevant here.
    * @param ruleSet the {@link AbstractSmallStepProofRuleSet} to use for the evaluation of the next
    *                small steps.
    * 
    * @throws NullPointerException if <code>node</code> or <code>ruleSet</code> is <code>null</code>.
    */
   DefaultSmallStepProofContext(ProofNode node, ProofRuleSet ruleSet) {
     if (node == null) {
       throw new NullPointerException("node is null");
     }
     if (ruleSet == null) {
       throw new NullPointerException("ruleSet is null");
     }
     
     // setup the initial expression and store
     this.expression = node.getExpression();
     this.store = new DefaultStore((DefaultStore)((SmallStepProofNode)node).getStore());
     
     // evaluate the next steps
     this.expression = ((AbstractSmallStepProofRuleSet)ruleSet).evaluate(this, this.expression);
   }
   
   
   
   //
   // Accessors
   //
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.smallstep.SmallStepProofContext#getExpression()
    */
   public Expression getExpression() {
     return this.expression;
   }
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.smallstep.SmallStepProofContext#getSteps()
    */
   public ProofStep[] getSteps() {
     // return the steps in reversed order
     ProofStep[] steps = new ProofStep[this.steps.size()];
     for (int n = 0; n < steps.length; ++n) {
       if (this.expression.isException()) {
         // translate meta rules to the associated EXN rules
         SmallStepProofRule rule = (SmallStepProofRule)this.steps.elementAt(n).getRule();
         steps[n] = new ProofStep(this.steps.elementAt(n).getExpression(), rule.toExnRule());
       }
       else {
         // just use the proof step
         steps[n] = this.steps.elementAt(n);
       }
     }
     return steps;
   }
 
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.smallstep.SmallStepProofContext#getStore()
    */
   public Store getStore() {
     return this.store;
   }
 
   
   
   //
   // Primitives
   //
   
   /**
    * {@inheritDoc}
    *
    * @see de.unisiegen.tpml.core.smallstep.SmallStepProofContext#addProofStep(de.unisiegen.tpml.core.ProofRule, de.unisiegen.tpml.core.expressions.Expression)
    */
   public void addProofStep(ProofRule rule, Expression expression) {
     this.steps.add(new ProofStep(expression, rule));
   }
 }
