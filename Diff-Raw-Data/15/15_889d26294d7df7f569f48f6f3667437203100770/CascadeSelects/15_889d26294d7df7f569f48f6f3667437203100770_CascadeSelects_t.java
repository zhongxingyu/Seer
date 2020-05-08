 package optimization;
 
 import java.util.ListIterator;
 
 import relationenalgebra.AndExpression;
import relationenalgebra.IOneChildNode;
 import relationenalgebra.ITreeNode;
import relationenalgebra.ITwoChildNode;
 import relationenalgebra.OrExpression;
 import relationenalgebra.Selection;
 
 public class CascadeSelects implements IOptimization {
 
     @Override
     public ITreeNode optimize(ITreeNode plan) {
         if (!(plan instanceof Selection)) {
            if (plan instanceof ITwoChildNode) {
                ((ITwoChildNode) plan).setSecondChild(
                        optimize(((ITwoChildNode) plan).getSecondChild()));
            }
            if (plan instanceof IOneChildNode) {
                ((IOneChildNode) plan).setChild(
                        optimize(((IOneChildNode) plan).getChild()));
            }
             return plan;
         }
         Selection s = (Selection)plan;
 
         // create a cascade
         ListIterator<OrExpression> iter = s.getExpr().getExprs().listIterator();
        ITreeNode result = optimize(s.getChild());
         while (iter.hasNext()) {
             Selection nextSelection = new Selection(
                     new AndExpression(iter.next()));
             nextSelection.setChild(result);
             result = nextSelection;
         }
 
         return result;
     }
 
 }
