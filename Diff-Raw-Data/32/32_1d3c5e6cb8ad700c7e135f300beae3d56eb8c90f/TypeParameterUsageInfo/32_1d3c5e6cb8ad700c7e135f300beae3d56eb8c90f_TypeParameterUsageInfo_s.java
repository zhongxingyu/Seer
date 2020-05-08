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
     * @param entityUsage pĂGeBeB
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
    public TypeParameterUsageInfo(final EntityUsageInfo entityUsage, final int fromLine,
             final int fromColumn, final int toLine, final int toColumn) {
 
         super(fromLine, fromColumn, toLine, toColumn);
 
        if (null == entityUsage) {
             throw new NullPointerException();
         }
 
        this.entityUsage = entityUsage;
     }
 
     @Override
     public TypeInfo getType() {
        return this.entityUsage.getType();
     }
 
     /**
     * GeBeBԂ
      * 
     * @return GeBeB
      */
    public EntityUsageInfo getEntityUsage() {
        return this.entityUsage;
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
    
    private final EntityUsageInfo entityUsage;
 }
