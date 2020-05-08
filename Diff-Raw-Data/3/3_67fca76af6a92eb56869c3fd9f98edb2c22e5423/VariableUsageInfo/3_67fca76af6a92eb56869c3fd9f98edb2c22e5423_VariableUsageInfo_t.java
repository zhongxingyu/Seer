 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 
 /**
  * ϐgp\ۃNX
  * 
  * @author higo
  * @param <T> gpĂϐ
  */
 public abstract class VariableUsageInfo<T extends VariableInfo<? extends UnitInfo>> extends
         ExpressionInfo {
 
     /**
      * 
      * @param usedVariable gpĂϐ
      * @param reference QƂǂ
      * @param ownerMethod I[i[\bh
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     VariableUsageInfo(final T usedVariable, final boolean reference,
             final CallableUnitInfo ownerMethod, final int fromLine, final int fromColumn,
             final int toLine, final int toColumn) {
 
         super(ownerMethod, fromLine, fromColumn, toLine, toColumn);
 
         this.usedVariable = usedVariable;
         this.reference = reference;
 
     }
 
     /**
      * gpĂϐԂ
      * 
      * @return gpĂϐ
      */
     public final T getUsedVariable() {
         return this.usedVariable;
     }
 
     /**
      * QƂԂ
      * 
      * @return QƂłꍇ trueCłꍇ false
      */
     public final boolean isReference() {
         return this.reference;
     }
 
     /**
      * ̃tB[hgpł邩ǂԂ
      * 
      * @return łꍇ trueCQƂłꍇ false
      */
     public final boolean isAssignment() {
         return !this.reference;
     }
 
     @Override
     public SortedSet<VariableUsageInfo<?>> getVariableUsages() {
         final SortedSet<VariableUsageInfo<?>> variableUsage = new TreeSet<VariableUsageInfo<?>>();
         variableUsage.add(this);
         return Collections.unmodifiableSortedSet(variableUsage);
     }
 
     /**
      * ̕ϐgp̃eLXg\i^jԂ
      * 
      * @return ̕ϐgp̃eLXg\i^j
      */
     @Override
     public final String getText() {
         final T variable = this.getUsedVariable();
         return variable.getName();
     }
 
     /**
      * ̃tB[hgp̌^Ԃ
      * 
      * @return ̃tB[hgp̌^
      */
     @Override
     public TypeInfo getType() {
 
         final T usedVariable = this.getUsedVariable();
         final TypeInfo definitionType = usedVariable.getType();
 
         // `̕Ԃl^p[^łȂ΂̂܂ܕԂ
         if (!(definitionType instanceof TypeParameterInfo)) {
             return definitionType;
         }
 
         // ^p[^CۂɎgpĂ^擾Ԃ
         // \bȟ^p[^ǂ
        final CallableUnitInfo ownerMethod = this.getOwnerMethod();
         for (final TypeParameterInfo typeParameter : ownerMethod.getTypeParameters()) {
             if (typeParameter.equals(definitionType)) {
                 return ((TypeParameterInfo) definitionType).getExtendsType();
             }
         }
 
         //@NX̌^p[^ǂ
         final ClassInfo ownerClass = ownerMethod.getOwnerClass();
         for (final TypeParameterInfo typeParameter : ownerClass.getTypeParameters()) {
             if (typeParameter.equals(definitionType)) {
                 return ((TypeParameterInfo) definitionType).getExtendsType();
             }
         }
 
         //@eNXŒ`ꂽ^p[^
         final Map<TypeParameterInfo, TypeInfo> typeParameterUsages = ownerClass
                 .getTypeParameterUsages();
         for (final TypeParameterInfo typeParameter : typeParameterUsages.keySet()) {
             if (typeParameter.equals(definitionType)) {
                 return typeParameterUsages.get(typeParameter);
             }
         }
 
         throw new IllegalStateException();
     }
 
     private final T usedVariable;
 
     private final boolean reference;
 
     /**
      * ̕ϐpSet\
      */
     public static final SortedSet<VariableUsageInfo<?>> EmptySet = Collections
             .unmodifiableSortedSet(new TreeSet<VariableUsageInfo<?>>());
 }
