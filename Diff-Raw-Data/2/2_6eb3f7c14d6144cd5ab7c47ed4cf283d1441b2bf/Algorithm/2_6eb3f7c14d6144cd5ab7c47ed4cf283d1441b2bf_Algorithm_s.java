 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package blocksworld;
 
 import java.util.ArrayList;
 import operators.Operator;
 import predicates.Predicate;
 
 /**
  *
  * @author gaspercat
  */
 public class Algorithm {
     private ArrayList<State> states;
     private ArrayList<Object> stack;
     
     public Algorithm(){
         this.states = new ArrayList<State>();
         this.stack = new ArrayList<Object>();
     }
             
     public void run(State initial, State goal){
         State state = initial;
         ArrayList<Operator> plan = new ArrayList<Operator>();
         
         stack.add(goal);
         stack.add(goal.getPredicates());
         
         while(stack.size() > 0){
             Object c = stack.remove(stack.size()-1);
             
             // If c is an operator
             if(c instanceof Operator){
                 plan.add((Operator)c);
                 state = new State(state, (Operator)c);
                 
             // If c is a condition not fully instanced
             }else if((c instanceof Predicate) && !((Predicate)c).isInstanced()){
                 instanceCondition(state, (Predicate)c);
                 stack.add(c);
                 
             // If c is a condition fully instanced
             }else if((c instanceof Predicate) && ((Predicate)c).isInstanced()){
                 Predicate pred = (Predicate)c;
                 if(!state.hasPredicate(pred)){
                     // TODO
                 }
                 
             // If c is a list of conditions
             }else if(c instanceof Preconditions){
                 ArrayList<Predicate> unmet = state.getUnmetConditions((Preconditions)c);
                 if(unmet.size() > 0){
                     stack.add(c);
                    stack.add(unmet);
                 }
             }
         }
     }
     
     private void instanceCondition(State state, Predicate pred){
         // Get related operator
         Operator op = null;
         for(int i=stack.size()-1;;i--){
             if(stack.get(i) instanceof Operator){
                 op = (Operator)stack.get(i);
                 break;
             }
         }
         
         // Define value at operator
         op.instanceValues(pred, state);
     }
     
     private void clear(){
         this.states.clear();
         this.stack.clear();
     }
 }
