 package uk.co.mtford.jalp.abduction;
 
 import uk.co.mtford.jalp.abduction.logic.instance.DenialInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.IInferableInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.IUnifiableAtomInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.ChocoConstraintSolverFacade;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.IConstraintInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.EqualityInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.InEqualityInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.VariableInstance;
 import uk.co.mtford.jalp.abduction.rules.RuleNode;
 
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 27/05/2012
  * Time: 09:30
  * To change this template use File | Settings | File Templates.
  */
 public class Result {
     private List<IInferableInstance> query;
     private RuleNode root;
     private Store store;
     private Map<VariableInstance, IUnifiableAtomInstance> assignments;  // Theta
 
     public Result(Store store,Map<VariableInstance, IUnifiableAtomInstance> assignments, List<IInferableInstance> query, RuleNode root) {
         this.assignments = assignments;
         this.store = store;
         this.query = query;
         this.root = root;
     }
 
     public List<IInferableInstance> getQuery() {
         return query;
     }
 
     public RuleNode getRoot() {
         return root;
     }
 
     public Store getStore() {
         return store;
     }
 
     public Map<VariableInstance, IUnifiableAtomInstance> getAssignments() {
         return assignments;
     }
 
     public void setAssignments(Map<VariableInstance, IUnifiableAtomInstance> assignments) {
         this.assignments = assignments;
     }
 
     public void setQuery(List<IInferableInstance> query) {
         this.query = query;
     }
 
     public void setRoot(RuleNode root) {
         this.root = root;
     }
 
     public void setStore(Store store) {
         this.store = store;
     }
 
     public void reduce(Collection<VariableInstance> relevantVariables) {
         // Remove irrelevant assignments.
         Map<VariableInstance, IUnifiableAtomInstance> newAssignments = new HashMap<VariableInstance, IUnifiableAtomInstance>();
        for (VariableInstance v:assignments.keySet()) {
             IUnifiableAtomInstance newValue = v;
             while (assignments.containsKey(newValue)) {
                 newValue = assignments.get(newValue);
             }
             if (v!=newValue) {
                 newAssignments.put(v,newValue);
             }
         }
         assignments = newAssignments;
         // Remove irrelevant denials
         List<DenialInstance> newDenials = new LinkedList<DenialInstance>();
         for (DenialInstance d:store.denials) {
          //   for (VariableInstance v:d.getVariables()) {
                // if (relevantVariables.contains(v)) {
                     if (!newDenials.contains(d)) { // Remove repeats.
                         newDenials.add(d);
                     }
              //   }
            // }
         }
 
         store.denials=newDenials;
         // Remove irrelevant equalities + repeat equalities.
         List<EqualityInstance> newEqualities = new LinkedList<EqualityInstance>();
         for (EqualityInstance e:store.equalities) {
             for (VariableInstance v:e.getVariables()) {
                 if (relevantVariables.contains(v)) {
                     if (!newEqualities.contains(e)) {
                         newEqualities.add(e);
                     }
                     break;
                 }
             }
         }
         store.equalities=newEqualities;
         // Remove irrelevant inEqualities + repeat inequalities.
         List<InEqualityInstance> newInEqualities = new LinkedList<InEqualityInstance>();
         for (InEqualityInstance e:store.inequalities) {
             for (VariableInstance v:e.getVariables()) {
                 if (relevantVariables.contains(v)) {
                     if (!newInEqualities.contains(e)) {
                         newInEqualities.add(e);
                     }
                     break;
                 }
             }
         }
         store.inequalities=newInEqualities;
         // Remove irrelevant constraints + repeat constraints
         List<IConstraintInstance> newConstraints = new LinkedList<IConstraintInstance>();
         for (IConstraintInstance e:store.constraints) {
             for (VariableInstance v:e.getVariables()) {
                 if (relevantVariables.contains(v)) {
                     if (!newConstraints.contains(e)) {
                         newConstraints.add(e);
                     }
                     break;
                 }
             }
         }
         store.constraints=newConstraints;
     }
 
     public String toString() {
 
         List epsilon = new LinkedList();
 
         for (InEqualityInstance e:store.inequalities) {
             epsilon.add(e);
         }
 
         for (EqualityInstance e:store.equalities) {
             epsilon.add(e);
         }
 
         boolean assignmentsEmpty = assignments.isEmpty();
         boolean abduciblesEmpty = store.abducibles.isEmpty();
         boolean constraintsEmpty = store.denials.isEmpty();
         boolean equalitiesEmpty = epsilon.isEmpty();
         boolean finiteDomainConstraintsEmpty = store.constraints.isEmpty();
         boolean allEmpty = assignmentsEmpty && constraintsEmpty && abduciblesEmpty && equalitiesEmpty &&
                            finiteDomainConstraintsEmpty;
 
         if (allEmpty) {
             return "Yes.";
         }
         else {
             String message = "";
             if (!assignmentsEmpty) message+="Subst:" + assignments + "\n";
             if (!abduciblesEmpty) message+="Delta:" + store.abducibles + "\n";
             if (!constraintsEmpty) message+="Delta*:" + store.denials + "\n";
             if (!equalitiesEmpty) message+="Epsilon:" + epsilon + "\n";
             if (!finiteDomainConstraintsEmpty) message+="FD:" + store.constraints +"\n";
 
             return message.substring(0,message.length()-1);
         }
 
     }
 
 }
