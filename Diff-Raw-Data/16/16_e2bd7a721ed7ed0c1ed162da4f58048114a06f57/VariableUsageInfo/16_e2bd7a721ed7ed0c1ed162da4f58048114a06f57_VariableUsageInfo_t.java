 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
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
      * ϐgpCollectiongpĂϐSetԂ
      * 
      * @param variableUsages ϐgpCollection
      * @return gpĂϐSet
      */
     public static Set<VariableInfo<? extends UnitInfo>> getUsedVariables(
             Collection<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> variableUsages) {
 
         final Set<VariableInfo<?>> usedVariables = new HashSet<VariableInfo<?>>();
         for (final VariableUsageInfo<?> variableUsage : variableUsages) {
             final VariableInfo<?> variable = variableUsage.getUsedVariable();
             usedVariables.add(variable);
         }
         return Collections.unmodifiableSet(usedVariables);
     }
 
     /**
      * ŗ^ĂϐgpɊ܂܂ϐQƂSetԂ
      * 
      * @param variableUsages ϐgpSet
      * @return ŗ^ĂϐgpɊ܂܂ϐQƂSet
      */
     public static Set<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> getReferencees(
             Collection<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> variableUsages) {
 
         final Set<VariableUsageInfo<?>> references = new HashSet<VariableUsageInfo<?>>();
         for (final VariableUsageInfo<?> variableUsage : variableUsages) {
             if (variableUsage.isReference()) {
                 references.add(variableUsage);
             }
         }
 
         return Collections.unmodifiableSet(references);
     }
 
     /**
      * ŗ^ĂϐgpɊ܂܂ϐSetԂ
      * 
      * @param variableUsages ϐgpSet
      * @return ŗ^ĂϐgpɊ܂܂ϐSet
      */
     public static Set<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> getAssignments(
             Collection<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> variableUsages) {
 
         final Set<VariableUsageInfo<?>> assignments = new HashSet<VariableUsageInfo<?>>();
         for (final VariableUsageInfo<?> variableUsage : variableUsages) {
             if (variableUsage.isAssignment()) {
                 assignments.add(variableUsage);
             }
         }
 
         return Collections.unmodifiableSet(assignments);
     }
 
     /**
      * 
      * @param usedVariable gpĂϐ
      * @param reference QƂǂ
      * @param assignment ǂ
      * @param ownerMethod I[i[\bh
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     VariableUsageInfo(final T usedVariable, final boolean reference, final boolean assignment,
             final CallableUnitInfo ownerMethod, final int fromLine, final int fromColumn,
             final int toLine, final int toColumn) {
 
         super(ownerMethod, fromLine, fromColumn, toLine, toColumn);
 
         this.usedVariable = usedVariable;
         this.reference = reference;
         this.assignment = assignment;
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
         return this.assignment;
     }
 
     @Override
     public Set<VariableUsageInfo<?>> getVariableUsages() {
         final SortedSet<VariableUsageInfo<?>> variableUsage = new TreeSet<VariableUsageInfo<?>>();
         variableUsage.add(this);
         return Collections.unmodifiableSortedSet(variableUsage);
     }
 
     /**
      * ĂяoSetԂ
      * 
      * @return ĂяoSet
      */
     @Override
     public Set<CallInfo<?>> getCalls() {
         return CallInfo.EmptySet;
     }
 
     /**
      * ̕ϐgp̃eLXg\i^jԂ
      * 
      * @return ̕ϐgp̃eLXg\i^j
      */
     @Override
     public String getText() {
         final T variable = this.getUsedVariable();
         return variable.getName();
     }
 
     /**
      * ϐgp̌^Ԃ
      * 
      * @return ϐgp̌^
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
 
         // NX̌^p[^ǂ
         for (ClassInfo ownerClass = ownerMethod.getOwnerClass(); true; ownerClass = ((TargetInnerClassInfo) ownerClass)
                 .getOuterClass()) {
 
             //@^p[^̂܂܂
             for (final TypeParameterInfo typeParameter : ownerClass.getTypeParameters()) {
                 if (typeParameter.equals(definitionType)) {
                     return ((TypeParameterInfo) definitionType).getExtendsType();
                 }
             }
 
             //@eNXŒ`ꂽ^p[^
             final Map<TypeParameterInfo, TypeInfo> typeParameterUsages = ownerClass
                     .getTypeParameterUsages();
             for (final Map.Entry<TypeParameterInfo, TypeInfo> entry : typeParameterUsages
                     .entrySet()) {
                 final TypeParameterInfo typeParameter = entry.getKey();
                 if (typeParameter.equals(definitionType)) {
                     return entry.getValue();
                 }
             }
 
             if (!(ownerClass instanceof TargetInnerClassInfo)) {
                 break;
             }
         }
 
         throw new IllegalStateException();
     }

     /**
      * ̎œ\OSetԂ
      * 
      * @return@̎œ\OSet
      */
     @Override
     public final Set<ClassTypeInfo> getThrownExceptions() {
         return Collections.unmodifiableSet(new HashSet<ClassTypeInfo>());
     }
 
     private final T usedVariable;
 
     private final boolean reference;
 
     private final boolean assignment;
 
     /**
      * ̕ϐpSet\
      */
    public static final SortedSet<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> EmptySet = Collections
            .unmodifiableSortedSet(new TreeSet<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>>());
 }
