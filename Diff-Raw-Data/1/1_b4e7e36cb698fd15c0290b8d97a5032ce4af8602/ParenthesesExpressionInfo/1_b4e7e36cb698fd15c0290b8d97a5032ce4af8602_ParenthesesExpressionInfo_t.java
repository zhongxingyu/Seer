 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.Set;
 
 
 /**
  * ʂŊꂽ\NX
  * 
  * @author higo
  *
  */
 public final class ParenthesesExpressionInfo extends ExpressionInfo {
 
     /**
      * 
      */
     private static final long serialVersionUID = -742042745531180181L;
 
     /**
      * IuWFNg@
      * 
      * @param parentheticExpression ʓ̎
      * @param ownerMethod L\bh
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public ParenthesesExpressionInfo(final ExpressionInfo parentheticExpression,
             final CallableUnitInfo ownerMethod, final int fromLine, final int fromColumn,
             final int toLine, final int toColumn) {
 
         super(ownerMethod, fromLine, fromColumn, toLine, toColumn);
 
         if (null == parentheticExpression) {
             throw new IllegalArgumentException();
         }
         this.parentheticExpression = parentheticExpression;
        this.parentheticExpression.setOwnerExecutableElement(this);
     }
 
     /**
      * ʂ̓̎Ԃ
      * 
      * @return ʂ̓̎
      */
     public ExpressionInfo getParnentheticExpression() {
         return this.parentheticExpression;
     }
 
     /**
      * ̌^Ԃ
      * 
      * @return ̌^
      */
     @Override
     public TypeInfo getType() {
         return this.getParnentheticExpression().getType();
     }
 
     /**
      * ̃eLXg\Ԃ
      * 
      * @return ̃eLXg\
      */
     @Override
     public String getText() {
 
         final StringBuilder text = new StringBuilder();
         text.append("(");
 
         final ExpressionInfo parentheticExpression = this.getParnentheticExpression();
         text.append(parentheticExpression.getText());
 
         text.append(")");
 
         return text.toString();
     }
 
     /**
      * ̃\bhĂяoꗗԂ
      * 
      * @return ̃\bhĂяoꗗ
      */
     @Override
     public Set<CallInfo<?>> getCalls() {
         return this.getParnentheticExpression().getCalls();
     }
 
     /**
      * ̕ϐgpꗗԂ
      * 
      * @return ̕ϐgpꗗ
      */
     @Override
     public Set<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> getVariableUsages() {
         return this.getParnentheticExpression().getVariableUsages();
     }
 
     /**
      * ̎œ\OSetԂ
      * 
      * @return@̎œ\OSet
      */
     @Override
     public Set<ClassTypeInfo> getThrownExceptions() {
         return Collections.unmodifiableSet(this.getParnentheticExpression().getThrownExceptions());
     }
 
     final ExpressionInfo parentheticExpression;
 }
