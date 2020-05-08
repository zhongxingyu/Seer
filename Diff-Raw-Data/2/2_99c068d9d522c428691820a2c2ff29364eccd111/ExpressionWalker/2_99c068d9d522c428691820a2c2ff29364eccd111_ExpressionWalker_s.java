 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.modelinglab.ocl.evaluator.walker;
 
 import org.modelinglab.ocl.core.ast.expressions.*;
 import org.modelinglab.ocl.core.ast.utils.CollectionLiteralPartVisitor;
 import org.modelinglab.ocl.core.ast.utils.OclExpressionsVisitor;
 import org.modelinglab.ocl.core.exceptions.IllegalOclExpression;
 import org.modelinglab.ocl.core.values.OclValue;
 import org.modelinglab.ocl.core.vartables.VariableTableException;
 import org.modelinglab.ocl.evaluator.EvaluatorVisitorArg;
 import org.modelinglab.ocl.evaluator.evaluators.AssociationEndCallExpEval;
 import org.modelinglab.ocl.evaluator.evaluators.AttributeCallExpEval;
 import org.modelinglab.ocl.evaluator.evaluators.IfExpEval;
 import org.modelinglab.ocl.evaluator.evaluators.IteratorExpEval;
 import org.modelinglab.ocl.evaluator.evaluators.LetExpEval;
 import org.modelinglab.ocl.evaluator.evaluators.LiteralExpEval;
 import org.modelinglab.ocl.evaluator.evaluators.OperationCallExpEval;
 import org.modelinglab.ocl.evaluator.evaluators.TupleAttributeCallExpEval;
 import org.modelinglab.ocl.evaluator.evaluators.VariableExpEval;
 
 /**
  *
  * @author Gonzalo Ortiz Jaureguizar (gortiz at software.imdea.org)
  */
 public final class ExpressionWalker implements OclExpressionsVisitor<OclValue<?>, EvaluatorVisitorArg>, CollectionLiteralPartVisitor<OclValue<?>, EvaluatorVisitorArg> {
 
     private ExpressionWalker() {
     }
 
     public static ExpressionWalker getInstance() {
         return ExpressionWalker.ExpressionWalkerHolder.INSTANCE;
     }
 
     public OclValue<?> evaluate(OclExpression exp, EvaluatorVisitorArg runtimeEnv) {
         return exp.accept(this, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(AssociationEndCallExp exp, EvaluatorVisitorArg runtimeEnv) {
         return AssociationEndCallExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(AttributeCallExp exp, EvaluatorVisitorArg runtimeEnv) {
         return AttributeCallExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(TupleAttributeCallExp exp, EvaluatorVisitorArg runtimeEnv) {
         return TupleAttributeCallExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(BooleanLiteralExp exp, EvaluatorVisitorArg runtimeEnv) {
         return LiteralExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(CollectionLiteralExp exp, EvaluatorVisitorArg runtimeEnv) {
         return LiteralExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(EnumLiteralExp exp, EvaluatorVisitorArg runtimeEnv) {
         return LiteralExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(IfExp exp, EvaluatorVisitorArg runtimeEnv) {
         return IfExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(IntegerLiteralExp exp, EvaluatorVisitorArg runtimeEnv) {
         return LiteralExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(InvalidLiteralExp exp, EvaluatorVisitorArg runtimeEnv) {
         return LiteralExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(IterateExp exp, EvaluatorVisitorArg runtimeEnv) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public OclValue<?> visit(IteratorExp exp, EvaluatorVisitorArg runtimeEnv) {
         return IteratorExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(LetExp exp, EvaluatorVisitorArg runtimeEnv) {
         try {
             return LetExpEval.getInstance().evaluate(exp, runtimeEnv);
         } catch (VariableTableException ex) {
             throw new IllegalOclExpression(exp, ex);
         }
     }
 
     @Override
     public OclValue<?> visit(NullLiteralExp exp, EvaluatorVisitorArg runtimeEnv) {
         return LiteralExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(OperationCallExp exp, EvaluatorVisitorArg runtimeEnv) {
         return OperationCallExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(RealLiteralExp exp, EvaluatorVisitorArg runtimeEnv) {
         return LiteralExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(StringLiteralExp exp, EvaluatorVisitorArg runtimeEnv) {
         return LiteralExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(TupleLiteralExp exp, EvaluatorVisitorArg runtimeEnv) {
         return LiteralExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(TypeExp exp, EvaluatorVisitorArg runtimeEnv) {
         return exp.getStaticEvaluation();
     }
 
     @Override
     public OclValue<?> visit(UnlimitedNaturalExp exp, EvaluatorVisitorArg runtimeEnv) {
         return LiteralExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(VariableExp exp, EvaluatorVisitorArg runtimeEnv) {
         return VariableExpEval.getInstance().evaluate(exp, runtimeEnv);
     }
 
     @Override
     public OclValue<?> visit(CollectionRange obj, EvaluatorVisitorArg argument) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public OclValue<?> visit(CollectionItem obj, EvaluatorVisitorArg argument) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /*
     * This methos should not be used.
      */
     @Override
     public OclValue<?> visit(MessageExp exp, EvaluatorVisitorArg argument) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public OclValue<?> visit(StateExp exp, EvaluatorVisitorArg argument) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public OclValue<?> visit(Variable var, EvaluatorVisitorArg runtimeEnv) {
         throw new UnsupportedOperationException("Not supported.");
     }
 
     // This method is called immediately after an object of this class is deserialized.
     // This method returns the singleton instance.
     Object readResolve() {
         return ExpressionWalker.getInstance();
     }
 
     private static class ExpressionWalkerHolder {
 
         private static final ExpressionWalker INSTANCE = new ExpressionWalker();
 
         private ExpressionWalkerHolder() {
         }
     }
 }
