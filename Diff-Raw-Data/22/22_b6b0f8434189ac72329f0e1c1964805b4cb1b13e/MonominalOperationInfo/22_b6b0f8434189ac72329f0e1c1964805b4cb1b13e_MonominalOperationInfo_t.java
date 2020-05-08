 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 
 /**
  * ꍀZۑ邽߂̃NX
  * 
  * @author t-miyake
  *
  */
 public final class MonominalOperationInfo extends EntityUsageInfo {
 
     /**
      * IyhAꍀŽʂ̌^Aʒu^ď
      * @param operand Iyh
      * @param type ꍀŽʂ̌^
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public MonominalOperationInfo(final EntityUsageInfo operand, final OPERATOR operator,
             final boolean isPreposed, final PrimitiveTypeInfo type, final int fromLine, final int fromColumn,
             final int toLine, final int toColumn) {
 
         super(fromLine, fromColumn, toLine, toColumn);
 
         if(null == operand || null == operator || null == type) {
             throw new IllegalArgumentException();
         }
         
         this.operand = operand;
         this.operator = operator;
         this.isPreposed = isPreposed;
         this.type = type;
     }
 
     @Override
     public TypeInfo getType() {
         return this.type;
     }
 
     /**
      * IyhԂ
      * @return Iyh
      */
     public final EntityUsageInfo getOperand() {
         return this.operand;
     }
 
     /**
      * ꍀZ̉ZqԂD
      * @return Zq
      */
     public final OPERATOR getOperator() {
         return this.operator;
     }
 
     /**
      * ZqOuĂ邩ǂԂ
      * @return ZqOuĂȂtrue
      */
     public final boolean isPreposed() {
         return this.isPreposed;
     }
     
     @Override
     public Set<VariableUsageInfo<?>> getVariableUsages() {
         final SortedSet<VariableUsageInfo<?>> variableUsages = new TreeSet<VariableUsageInfo<?>>();
         variableUsages.addAll(this.getOperand().getVariableUsages());
        return variableUsages;
     }
 
     /**
      * Iyhۑ邽߂̕ϐ
      */
     private final EntityUsageInfo operand;
 
     /**
      * ꍀZ̉Zqۑ邽߂̕ϐ
      */
     private final OPERATOR operator;
 
     /**
      * ꍀŽʂ̌^ۑ邽߂̕ϐ
      */
     private final PrimitiveTypeInfo type;
     
     /**
      * ZqOuĂ邩ǂϐ
      */
     private final boolean isPreposed;
 }
