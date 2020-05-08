 package optimization;
 
 import java.util.ListIterator;
 
 import relationenalgebra.AndExpression;
 import relationenalgebra.ITreeNode;
 import relationenalgebra.OrExpression;
 import relationenalgebra.Selection;
 
 public class CascadeSelects implements IOptimization {
 
     @Override
     public ITreeNode optimize(ITreeNode plan) {
         if (!(plan instanceof Selection)) {
             return plan;
         }
         Selection s = (Selection)plan;
 
         // create a cascade
         ListIterator<OrExpression> iter = s.getExpr().getExprs().listIterator();
        ITreeNode result = s.getChild();
         while (iter.hasNext()) {
             Selection nextSelection = new Selection(
                     new AndExpression(iter.next()));
             nextSelection.setChild(result);
             result = nextSelection;
         }
 
         return result;
     }
 
 }
