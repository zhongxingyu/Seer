 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * tB[h̎gp\NX
  * 
  * @author higo
  * 
  */
 public class FieldUsageInfo extends VariableUsageInfo<FieldInfo> {
 
     /**
      * gpĂtB[h^ăIuWFNg
      * 
      * @param ownerUsage tB[hgpseGeBeB
      * @param usedField gpĂtB[h
      * @param reference QƂłꍇ true, łꍇ false
      */
     protected FieldUsageInfo(final EntityUsageInfo ownerUsage, final TypeInfo ownerType,
             final FieldInfo usedField, final boolean reference, final int fromLine,
             final int fromColumn, final int toLine, final int toColumn) {
 
         super(usedField, reference, fromLine, fromColumn, toLine, toColumn);
 
        this.ownerUsage = ownerUsage;
         this.ownerType = ownerType;
     }
 
     /**
      * ̃tB[hgp̌^Ԃ
      * 
      * @return ̃tB[hgp̌^
      */
     @Override
     public TypeInfo getType() {
 
         final FieldInfo usedVariable = this.getUsedVariable();
         final TypeInfo definitionType = usedVariable.getType();
 
         // `̕Ԃl^p[^łȂ΂̂܂ܕԂ
         if (!(definitionType instanceof TypeParameterInfo)) {
             return definitionType;
         }
 
         //@
         final int typeParameterIndex = ((TypeParameterInfo) definitionType).getIndex();
         final ClassTypeInfo callOwnerType = (ClassTypeInfo) this.getOwnerType();
         final TypeInfo typeArgument = callOwnerType.getTypeArgument(typeParameterIndex);
         return typeArgument;
     }
 
     /**
      * ̃tB[hgp̐eC܂肱̃tB[hgpĂvfԂ
      * 
      * @return ̃tB[hgp̐e
      */
     public final TypeInfo getOwnerType() {
         return this.ownerType;
     }
 
     /**
      * tB[hgpseGeBeBԂ
      * @return tB[hgpseGeBeB
      */
    public final EntityUsageInfo getOwnerUsage() {
        return this.ownerUsage;
     }
 
     /**
      * ̎itB[hgpjɂϐp̈ꗗԂ
      * 
      * @return ϐpSet
      */
     @Override
     public SortedSet<VariableUsageInfo<?>> getVariableUsages() {
 
         final SortedSet<VariableUsageInfo<?>> variableUsages = new TreeSet<VariableUsageInfo<?>>();
         variableUsages.addAll(super.getVariableUsages());
 
        final ExpressionInfo ownerExpression = this.getOwnerUsage();
         variableUsages.addAll(ownerExpression.getVariableUsages());
 
         return Collections.unmodifiableSortedSet(variableUsages);
     }
 
     private final TypeInfo ownerType;
 
     /**
      * tB[hQƂseGeBeBۑϐ
      */
    private final EntityUsageInfo ownerUsage;
 
     /**
      * Kvȏ^āCCX^X擾
      * 
      * @param ownerUsage eGeBeB
      * @param ownerType eGeBeB̌^
      * @param usedField gpĂtB[h
      * @param reference QƂłꍇtrueCłꍇfalse
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      * @return tB[hgp̃CX^X
      */
     public static FieldUsageInfo getInstance(final EntityUsageInfo ownerUsage,
             final TypeInfo ownerType, final FieldInfo usedField, final boolean reference,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn) {
         final FieldUsageInfo instance = new FieldUsageInfo(ownerUsage, ownerType, usedField,
                 reference, fromLine, fromColumn, toLine, toColumn);
         addFieldUsage(instance);
         return instance;
     }
 
     /**
      * tB[hgp̃CX^XtB[htB[hgpւ̃}bvɒǉ
      * @param fieldUsage tB[hgp
      */
     private static void addFieldUsage(final FieldUsageInfo fieldUsage) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
 
         if (null == fieldUsage) {
             throw new IllegalArgumentException("localVariableUsage is null");
         }
 
         final FieldInfo usedField = fieldUsage.getUsedVariable();
         if (USAGE_MAP.containsKey(usedField)) {
             USAGE_MAP.get(usedField).add(fieldUsage);
         } else {
             final TreeSet<FieldUsageInfo> usages = new TreeSet<FieldUsageInfo>();
             usages.add(fieldUsage);
             USAGE_MAP.put(usedField, usages);
         }
     }
 
     /**
      * ^ꂽtB[h̎gp̃Zbg擾
      * @param field gp擾tB[h
      * @return tB[hgp̃ZbgDŗ^ꂽtB[hgpĂȂꍇnull
      */
     public final static Set<FieldUsageInfo> getUsages(final FieldInfo field) {
         if (USAGE_MAP.containsKey(field)) {
             return USAGE_MAP.get(field);
         } else {
             return Collections.<FieldUsageInfo> emptySet();
         }
     }
 
     private static final Map<FieldInfo, TreeSet<FieldUsageInfo>> USAGE_MAP = new HashMap<FieldInfo, TreeSet<FieldUsageInfo>>();
 }
