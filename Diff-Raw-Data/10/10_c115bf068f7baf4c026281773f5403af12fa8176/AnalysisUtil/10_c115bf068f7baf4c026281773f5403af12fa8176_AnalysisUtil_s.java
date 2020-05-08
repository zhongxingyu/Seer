 package org.clafer.ir.analysis;
 
 import org.clafer.collection.Pair;
 import org.clafer.ir.IrBoolExpr;
 import org.clafer.ir.IrCard;
 import org.clafer.ir.IrCompare;
 import org.clafer.ir.IrIfOnlyIf;
 import org.clafer.ir.IrIntExpr;
 import org.clafer.ir.IrSetVar;
 
 /**
  *
  * @author jimmy
  */
 public class AnalysisUtil {
 
     private AnalysisUtil() {
     }
 
     public static Pair<IrIntExpr, IrSetVar> getAssignCardinality(IrBoolExpr expr) {
         if (expr instanceof IrCompare) {
             IrCompare compare = (IrCompare) expr;
            return getAssignCardinality(compare.getLeft(), compare.getRight());
         } else if (expr instanceof IrIfOnlyIf) {
             IrIfOnlyIf ifOnlyIf = (IrIfOnlyIf) expr;
             return getAssignCardinality(ifOnlyIf.getLeft(), ifOnlyIf.getRight());
         }
         return null;
     }
 
     private static Pair<IrIntExpr, IrSetVar> getAssignCardinality(IrIntExpr left, IrIntExpr right) {
         Pair<IrIntExpr, IrSetVar> cardinality = getAssignCardinalityImpl(left, right);
         if (cardinality == null) {
             cardinality = getAssignCardinalityImpl(right, left);
         }
         return cardinality;
     }
 
     private static Pair<IrIntExpr, IrSetVar> getAssignCardinalityImpl(IrIntExpr left, IrIntExpr right) {
         if (right instanceof IrCard) {
             IrCard card = (IrCard) right;
             if (card.getSet() instanceof IrSetVar) {
                 IrSetVar set = (IrSetVar) card.getSet();
                 return new Pair<IrIntExpr, IrSetVar>(left, set);
             }
         }
         return null;
     }
 }
