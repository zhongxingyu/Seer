 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 

 import java.util.Set;
 
 
 /**
  * ^p[^̎gp\NX
  * 
  * @author higo
  *
  */
 public final class TypeParameterUsageInfo extends EntityUsageInfo {
 
     /**
      * Kvȏ^āCIuWFNg
      * 
     * @param expression pĂ鎮
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
    public TypeParameterUsageInfo(final ExpressionInfo expression, final int fromLine,
             final int fromColumn, final int toLine, final int toColumn) {
 
         super(fromLine, fromColumn, toLine, toColumn);
 
        if (null == expression) {
             throw new NullPointerException();
         }
 
        this.expression = expression;
     }
 
     @Override
     public TypeInfo getType() {
        return this.expression.getType();
     }
 
     /**
     * ̌^p[^gp̎Ԃ
      * 
     * @return ̌^p[^gp̎
      */
    public ExpressionInfo getExpression() {
        return this.expression;
     }
 
     /**
      * ^p[^̎gpɕϐgp܂܂邱Ƃ͂Ȃ̂ŋ̃ZbgԂ
      * 
      * @return ̃Zbg
      */
     @Override
     public final Set<VariableUsageInfo<?>> getVariableUsages() {
         return VariableUsageInfo.EmptySet;
     }

    private final ExpressionInfo expression;
 }
