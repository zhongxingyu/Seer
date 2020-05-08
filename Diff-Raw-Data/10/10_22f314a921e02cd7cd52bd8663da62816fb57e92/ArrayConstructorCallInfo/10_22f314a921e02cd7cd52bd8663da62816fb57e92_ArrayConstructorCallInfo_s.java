 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 
 /**
  * zRXgN^Ăяo\NX
  * 
  * @author higo
  *
  */
 @SuppressWarnings("serial")
 public final class ArrayConstructorCallInfo extends ConstructorCallInfo<ArrayTypeInfo> {
 
     /**
      * ^^ĔzRXgN^Ăяo
      * 
      * @param arrayType Ăяǒ^
      * @param indexExpressions CfbNX̎
      * @param ownerMethod I[i[\bh 
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I 
      */
     public ArrayConstructorCallInfo(final ArrayTypeInfo arrayType,
             final List<ExpressionInfo> indexExpressions, final CallableUnitInfo ownerMethod,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn) {
 
         super(arrayType, null, ownerMethod, fromLine, fromColumn, toLine, toColumn);
 
         if (null == indexExpressions) {
             throw new IllegalArgumentException();
         }
         this.indexExpressions = Collections.unmodifiableList(indexExpressions);
 
         for (final ExpressionInfo element : this.indexExpressions) {
             element.setOwnerExecutableElement(this);
         }
     }
 
     /**
      * CfbNX̎擾
      * @param dimention CfbNX̎擾z̎
      * @return w肵̃CfbNX̎
      */
     public ExpressionInfo getIndexExpression(final int dimention) {
         return this.indexExpressions.get(dimention - 1);
     }
 
     /**
      * CfbNX̎̃Xg擾
      * 
      * @return CfbNX̎̃Xg 
      */
     public List<ExpressionInfo> getIndexExpressions() {
         return this.indexExpressions;
     }
 
     /**
      * z̏̃eLXg\Ԃ
      * 
      * @return z̏̃eLXg\
      * 
      */
     @Override
     public String getText() {
 
         final StringBuilder text = new StringBuilder();
         text.append("new ");
 
         final ArrayTypeInfo arrayType = this.getType();
         final TypeInfo elementType = arrayType.getElementType();
         text.append(elementType.getTypeName());
 
         final int dimension = arrayType.getDimension();
         for (int i = 1; i <= dimension; i++) {
             final ExpressionInfo indexExpression = this.getIndexExpression(i);
             text.append("[");
             text.append(indexExpression.getText());
             text.append("]");
         }
 
         return text.toString();
     }
 
     /**
      * ̎œ\OSetԂ
      * 
      * @return@̎œ\OSet
      */
     @Override
     public Set<ReferenceTypeInfo> getThrownExceptions() {
         final Set<ReferenceTypeInfo> thrownExceptions = new HashSet<ReferenceTypeInfo>();
         for (final ExpressionInfo indexExpression : this.getIndexExpressions()) {
             thrownExceptions.addAll(indexExpression.getThrownExceptions());
         }
         return Collections.unmodifiableSet(thrownExceptions);
     }
 
     private final List<ExpressionInfo> indexExpressions;
 }
