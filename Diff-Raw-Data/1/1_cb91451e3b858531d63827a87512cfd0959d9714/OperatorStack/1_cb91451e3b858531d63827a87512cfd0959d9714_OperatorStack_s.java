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
 public class OperatorStack extends Operator {
     public OperatorStack(Block a, Block b){
         super(Operator.STACK);
 
         // Add preconditions
         pres.add(new PredicatePickedUp(a));
         pres.add(new PredicateFree(b));
         pres.add(new PredicateHeavier(b, a));
         
         // Add deletions
         rmvs.add(new PredicatePickedUp(a));
         rmvs.add(new PredicateFree(b));
         
         // Add additions
         adds.add(new PredicateOn(a, b));
         adds.add(new PredicateFreeArm());
     }
     
     public OperatorStack(OperatorStack op){
         super(op);
     }
     
     @Override
     public void instanceValues(Predicate pred, State state){
         if(pred instanceof PredicatePickedUp){
             instanceA(state);
         }else if(pred instanceof PredicateFree){
             instanceB(state);
         }else if(pred instanceof PredicateHeavier){
             PredicateHeavier p = (PredicateHeavier)pred;
             if(!p.isInstancedA()) instanceB(state);
             if(!p.isInstancedB()) instanceA(state);
         }
     }
     
     private void instanceA(State state){
         // Select value
         // *******************************
         
         ArrayList<Block> blocks = state.getAllBlocks();
         
         Block b = pres.get(1).getA();
         if(b != null){
             // Remove b from possible options
             blocks.remove(b);
             
             // Remove elements heavier than b
             ArrayList<Predicate> preds = state.matchPredicates(new PredicateHeavier(null, b));
             for(Predicate p: preds) blocks.remove(p.getA());
         }
         
         Block val = blocks.get(rnd.nextInt(blocks.size()));
         
         // Give value to predicates
         // *******************************
         
         pres.get(0).setA(val);
         pres.get(2).setB(val);
         rmvs.get(0).setA(val);
         adds.get(0).setA(val);
     }
     
     private void instanceB(State state){
         // Select value
         // *******************************
         
         ArrayList<Block> blocks = state.getAllBlocks();
         
         Block a = pres.get(0).getA();
         if(a != null){
             // Remove a from possible options
             blocks.remove(a);
             
             // Remove elements lighter than a
             ArrayList<Predicate> preds = state.matchPredicates(new PredicateHeavier(a, null));
             for(Predicate p: preds) blocks.remove(p.getB());
         }
         
         Block val = blocks.get(rnd.nextInt(blocks.size()));
         
         // Give value to predicates
         // *******************************
         
         pres.get(1).setA(val);
         pres.get(2).setA(val);
         rmvs.get(1).setA(val);
     }
     
     @Override
     public Operator clone(){
         OperatorStack ret = new OperatorStack(this); 
         return ret;
     }
     
     @Override
     public String toString(){
         Block a = this.pres.get(0).getA();
         Block b = this.pres.get(1).getA();
         return "stack(" + (a != null ? a.getName() : "undef") + ", " + (b != null ? b.getName() : "undef") + ")";
 
     }
 }
