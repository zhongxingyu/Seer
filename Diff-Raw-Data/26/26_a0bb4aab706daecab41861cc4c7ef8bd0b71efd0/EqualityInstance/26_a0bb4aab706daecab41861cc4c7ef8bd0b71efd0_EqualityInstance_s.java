 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.co.mtford.alp.abduction.asystem;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import uk.co.mtford.alp.abduction.AbductiveFramework;
 import uk.co.mtford.alp.abduction.logic.instance.*;
 import uk.co.mtford.alp.unification.Unifier;
 
 /**
  *
  * @author mtford
  */
 public class EqualityInstance implements IEqualityInstance  {
     
     private static Logger LOGGER = Logger.getLogger(EqualityInstance.class);
 
     private IAtomInstance left;
     private IAtomInstance right;
 
     public EqualityInstance(IAtomInstance left, IAtomInstance right) {
         this.left=left;
         this.right=right;
     }
 
     public IAtomInstance getLeft() {
         return left;
     }
 
     public void setLeft(IAtomInstance left) {
         this.left = left;
     }
 
     public IAtomInstance getRight() {
         return right;
     }
 
     public void setRight(IAtomInstance right) {
         this.right = right;
     }
 
     
     @Override
     public Object clone() {
         return new EqualityInstance((IAtomInstance)left.clone(),(IAtomInstance)right.clone());
     }
     
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final EqualityInstance other = (EqualityInstance) obj;
         if (this.left != other.left && (this.left == null || !this.left.equals(other.left))) {
             return false;
         }
         if (this.right != other.right && (this.right == null || !this.right.equals(other.right))) {
             return false;
         }
         return true;
     }
     
      public boolean deepEquals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final EqualityInstance other = (EqualityInstance) obj;
         if (this.left != other.left && (this.left == null || !this.left.deepEquals(other.left))) {
             return false;
         }
         if (this.right != other.right && (this.right == null || !this.right.deepEquals(other.right))) {
             return false;
         }
         return true;
     }
  
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 89 * hash + (this.left != null ? this.left.hashCode() : 0);
         hash = 89 * hash + (this.right != null ? this.right.hashCode() : 0);
         return hash;
     }
 
     /** Implements inference rule E1.
      * 
      * @param framework
      * @param s
      * @return 
      */
     public List<ASystemState> applyInferenceRule(AbductiveFramework framework, ASystemState s) {
         LinkedList<ASystemState> possibleStates = new LinkedList<ASystemState>();
         if (LOGGER.isDebugEnabled()) LOGGER.debug("Applying inference rule E1 to "+this);
         EqualityInstance clonedThis = (EqualityInstance) s.popGoal();
         LinkedList<IASystemInferable> newInferables = 
                 (LinkedList<IASystemInferable>) clonedThis.getLeft().positiveEqualitySolve(clonedThis.getRight());
         for (IASystemInferable inferable:newInferables) s.putGoal(inferable);
         possibleStates.add(s);
         return possibleStates;
     }
 
     // TODO Better implementation
     public List<ASystemState> applyDenialInferenceRule(AbductiveFramework framework, ASystemState s) {
         if (LOGGER.isDebugEnabled()) LOGGER.debug("Applying equality denial inference rules to "+this);
         LinkedList<ASystemState> possibleStates = new LinkedList<ASystemState>();
         ASystemState clonedState = (ASystemState) s.clone();
         DenialInstance denialClone = (DenialInstance) clonedState.popGoal();
         EqualityInstance thisClone = (EqualityInstance) denialClone.popLiteral();
         if (thisClone.left instanceof VariableInstance) thisClone.left=((VariableInstance) thisClone.left).getDeepValue();
         if (thisClone.right instanceof VariableInstance) thisClone.right=((VariableInstance) thisClone.right).getDeepValue();
         boolean varAndVar = 
                 thisClone.getLeft() instanceof VariableInstance &&
                 thisClone.getRight() instanceof VariableInstance;
         boolean constantAndVar =
                 thisClone.getLeft() instanceof VariableInstance &&
                 thisClone.getRight() instanceof ConstantInstance ||
                 thisClone.getLeft() instanceof ConstantInstance &&
                 thisClone.getRight() instanceof VariableInstance;
         boolean constantAndConstant =
                 thisClone.getLeft() instanceof ConstantInstance &&
                 thisClone.getRight() instanceof ConstantInstance;
 
         if (varAndVar) {
             if (denialClone.isUniversallyQuantified((VariableInstance)thisClone.getLeft())) {  // Solver
                 if (LOGGER.isDebugEnabled()) LOGGER.debug("Applying equality solver to "+this);
                 denialClone.addLiteral(0,thisClone.left.positiveEqualitySolve(thisClone.right));
                 clonedState.putGoal(denialClone);
                 possibleStates.add(clonedState);
             }
             else { // Existentially quantified hence 2c.
                 if (LOGGER.isDebugEnabled()) LOGGER.debug("Applying E2.C. inference rule to "+this);
                 ((VariableInstance)thisClone.left).setValue(thisClone.right); // TODO: Right way round?
                 clonedState.putGoal(denialClone);
                 possibleStates.add(clonedState);
             }
         }
         else if (constantAndVar) {
             ConstantInstance constant;
             VariableInstance variable;
             if (thisClone.getLeft() instanceof VariableInstance)  {
                 variable = (VariableInstance) thisClone.getLeft();
                 constant = (ConstantInstance) thisClone.getRight();
             }
             else {
                 variable = (VariableInstance) thisClone.getRight();
                 constant = (ConstantInstance) thisClone.getLeft();
             }
             if (denialClone.isUniversallyQuantified(variable)) { // Solver
                 if (LOGGER.isDebugEnabled()) LOGGER.debug("Applying equality solver to "+this);
                 denialClone.addLiteral(0,variable.positiveEqualitySolve(constant));
                 clonedState.putGoal(denialClone);
                 possibleStates.add(clonedState);
             }
             else { // 2b
                 if (LOGGER.isDebugEnabled()) LOGGER.debug("Applying E2.B inference rule to "+this);
                 clonedState = (ASystemState) s.clone();
                 denialClone = (DenialInstance) clonedState.popGoal();
                 thisClone = (EqualityInstance) denialClone.popLiteral();
                 clonedState.getStore().getEqualities().add(new InequalityInstance(thisClone.left,thisClone.right));
                 possibleStates.add(clonedState);
                 // OR
                 clonedState = (ASystemState) s.clone();
                 denialClone = (DenialInstance) clonedState.popGoal();
                 thisClone = (EqualityInstance) denialClone.popLiteral();
                 variable.setValue(constant);
                 clonedState.putGoal(denialClone);
                 possibleStates.add(clonedState);
             }
         }
         else if (constantAndConstant) {  // Solver
             if (LOGGER.isDebugEnabled()) LOGGER.debug("Applying equality solver to "+this);
             denialClone.addLiteral(0,thisClone.left.positiveEqualitySolve(thisClone.right));
             clonedState.putGoal(denialClone);
             possibleStates.add(clonedState);
         }
         else {
             LOGGER.error("Error applying denial inference rules for equalities.");
             throw new UnsupportedOperationException();
         }
         return possibleStates;
 
     }
 
     public Object clone(Map<String, VariableInstance> variablesSoFar) {
         return new EqualityInstance((IAtomInstance)left.clone(variablesSoFar),(IAtomInstance)right.clone(variablesSoFar));
     }
 
     @Override
     public String toString() {
         String leftString = left.toString();
         String rightString = right.toString();
         
         return leftString + "==" + rightString;
     }
 
     @Override
     public List<VariableInstance> getVariables() {
         LinkedList<VariableInstance> vars = new LinkedList<VariableInstance>();
         vars.addAll(left.getVariables());
         vars.addAll(right.getVariables());
         return vars;
     }
 }
