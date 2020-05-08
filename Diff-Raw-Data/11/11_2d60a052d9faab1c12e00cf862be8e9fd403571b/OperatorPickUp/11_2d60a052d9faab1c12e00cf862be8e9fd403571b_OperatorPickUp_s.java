 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package operators;
 
 import java.util.ArrayList;
 import blocksworld.Block;
 import blocksworld.State;
 import predicates.PredicateUsedColsNum;
 import predicates.*;
 
 /**
  *
  * @author gaspercat
  */
 public class OperatorPickUp extends Operator {
     public OperatorPickUp(Block a){
         super(Operator.PICK_UP);
         
         // Add preconditions
         pres.add(new PredicateUsedColsNum(PredicateUsedColsNum.N_UNDEFINED));
         pres.add(new PredicateFreeArm());
         pres.add(new PredicateFree(a));
         pres.add(new PredicateOnTable(a));
         
         // Add deletions
         rmvs.add(new PredicateOnTable(a));
         rmvs.add(new PredicateFreeArm());
         rmvs.add(new PredicateUsedColsNum(PredicateUsedColsNum.N_UNDEFINED));
         
         // Add additions
         adds.add(new PredicatePickedUp(a));
         adds.add(new PredicateUsedColsNum(PredicateUsedColsNum.N_DECREASED));
     }
     
     public OperatorPickUp(OperatorPickUp op){
         super(op);
     }
     
     @Override
     public void instanceValues(Predicate pred, State state){
         if(pred instanceof PredicateOnTable){
             instanceA(state);
         }else if(pred instanceof PredicateFree){
             instanceA(state);
         }else if(pred instanceof PredicateUsedColsNum){
             instanceN(state);
         }
     }
     
     private void instanceA(State state){
         // Select value
         // *******************************
         
        ArrayList<Block> blocks = state.getAllBlocks();
        Block val = blocks.get(rnd.nextInt(blocks.size()));
         
         
         // Give value to predicates
         // *******************************
         
         pres.get(2).setA(val);
         pres.get(3).setA(val);
         rmvs.get(0).setA(val);
         adds.get(0).setA(val);
     }
     
     private void instanceN(State state){
         int val = state.getNumColumns();
         
         // Give value to predicates
         // *******************************
         
         pres.get(0).setN(val);
         rmvs.get(2).setN(val);
         adds.get(1).setN(val-1);
     }
     
     @Override
     public Operator clone(){
         OperatorPickUp ret = new OperatorPickUp(this); 
         return ret;
     }
     
     @Override
     public String toString(){
         Block a = this.pres.get(2).getA();
         return "pick-up(" + (a == null ? "undef" : a.getName()) + ")";
     }
 }
