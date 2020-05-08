 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.List;
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.Settings;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ArrayLengthUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ArrayTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.EntityUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetFieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetInnerClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnknownEntityUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnknownTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalFieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.LANGUAGE;
 
 
 /**
  * tB[hgpۑ邽߂̃NX
  * 
  * @author higo
  * 
  */
 public final class UnresolvedFieldUsageInfo extends UnresolvedVariableUsageInfo {
 
     /**
      * tB[hgpsϐ̌^ƕϐCp\ȖOԂ^ăIuWFNg
      * 
      * @param availableNamespaces p\ȖO
      * @param ownerClassType tB[hgpsϐ̌^
      * @param fieldName ϐ
      * @param reference tB[hgpQƂłꍇ trueCłꍇ false w
      */
     public UnresolvedFieldUsageInfo(final Set<AvailableNamespaceInfo> availableNamespaces,
             final UnresolvedEntityUsageInfo ownerClassType, final String fieldName,
             final boolean reference) {
 
         super(fieldName, reference);
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == availableNamespaces) || (null == ownerClassType) || (null == fieldName)) {
             throw new NullPointerException();
         }
 
         this.availableNamespaces = availableNamespaces;
         this.ownerClassType = ownerClassType;
         this.fieldName = fieldName;
         this.reference = reference;
     }
 
     /**
      * tB[hgpČ^ԂD
      * 
      * @param usingClass tB[hgpsĂNX
      * @param usingMethod tB[hgpsĂ郁\bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @return ς݃tB[hgp
      */
     @Override
     public EntityUsageInfo resolveEntityUsage(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == usingClass) || (null == usingMethod) || (null == classInfoManager)
                 || (null == fieldInfoManager) || (null == methodInfoManager)) {
             throw new NullPointerException();
         }
 
         // ɉς݂łꍇ́CLbVԂ
         if (this.alreadyResolved()) {
             return this.getResolvedEntityUsage();
         }
 
         // tB[hCQƁE擾
         final String fieldName = this.getFieldName();
         final boolean reference = this.isReference();
 
         // gpʒu擾
         final int fromLine = this.getFromLine();
         final int fromColumn = this.getFromColumn();
         final int toLine = this.getToLine();
         final int toColumn = this.getToColumn();
 
         // ě^
         final UnresolvedEntityUsageInfo unresolvedOwnerUsage = this.getOwnerClassType();
         final EntityUsageInfo ownerUsage = unresolvedOwnerUsage.resolveEntityUsage(usingClass,
                 usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
         assert ownerUsage != null : "resolveEntityUsage returned null!";
 
         // -----ě^ ɉď𕪊
         // ełȂꍇ͂ǂ悤Ȃ
         if (ownerUsage.getType() instanceof UnknownTypeInfo) {
 
             // Ȃs
             usingMethod.addUnresolvedUsage(this);
 
             this.resolvedInfo = new UnknownEntityUsageInfo(fromLine, fromColumn, toLine, toColumn);
             return this.resolvedInfo;
 
             //eNX^̏ꍇ
         } else if (ownerUsage.getType() instanceof ClassTypeInfo) {
 
             final ClassInfo ownerClass = ((ClassTypeInfo) ownerUsage.getType())
                     .getReferencedClass();
             // eΏۃNX(TargetClassInfo)ꍇ
             if (ownerClass instanceof TargetClassInfo) {
 
                 // ܂͗p\ȃtB[h猟
                 {
                     // p\ȃtB[hꗗ擾
                     final List<TargetFieldInfo> availableFields = NameResolver.getAvailableFields(
                            (TargetClassInfo) ownerUsage.getType(), usingClass);
 
                     // p\ȃtB[hCtB[hŌ
                     for (final TargetFieldInfo availableField : availableFields) {
 
                         // vtB[hꍇ
                         if (fieldName.equals(availableField.getName())) {
 
                             this.resolvedInfo = new FieldUsageInfo(availableField, reference,
                                     fromLine, fromColumn, toLine, toColumn);
                             return this.resolvedInfo;
                         }
                     }
                 }
 
                 // p\ȃtB[hȂꍇ́CONXłeNX͂
                 // ̃NX̕ϐgpĂƂ݂Ȃ
                 {
                    for (TargetClassInfo classInfo = (TargetClassInfo) ownerUsage.getType(); true; classInfo = ((TargetInnerClassInfo) classInfo)
                             .getOuterClass()) {
 
                         final ExternalClassInfo externalSuperClass = NameResolver
                                 .getExternalSuperClass(classInfo);
                         if (null != externalSuperClass) {
 
                             final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(fieldName,
                                     externalSuperClass);
                             fieldInfoManager.add(fieldInfo);
 
                             // ONXɐVKŊOϐ(ExternalFieldInfo)ǉ̂Ō^͕sD
                             this.resolvedInfo = new FieldUsageInfo(fieldInfo, reference, fromLine,
                                     fromColumn, toLine, toColumn);
                             return this.resolvedInfo;
                         }
 
                         if (!(classInfo instanceof TargetInnerClassInfo)) {
                             break;
                         }
                     }
                 }
 
                 // Ȃs
                 {
                     assert false : "Can't resolve field reference : " + this.getFieldName();
 
                     usingMethod.addUnresolvedUsage(this);
 
                     this.resolvedInfo = new UnknownEntityUsageInfo(fromLine, fromColumn, toLine,
                             toColumn);
                     return this.resolvedInfo;
                 }
 
                 // eONXiExternalClassInfojꍇ
             } else if (ownerClass instanceof ExternalClassInfo) {
 
                 final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(fieldName, ownerClass);
                 fieldInfoManager.add(fieldInfo);
 
                 // ONXɐVKŊOϐ(ExternalFieldInfo)ǉ̂Ō^͕sD
                 this.resolvedInfo = new FieldUsageInfo(fieldInfo, reference, fromLine, fromColumn,
                         toLine, toColumn);
                 return this.resolvedInfo;
             }
 
         } else if (ownerUsage.getType() instanceof ArrayTypeInfo) {
 
             // TODO ͌ˑɂ邵Ȃ̂H z.length Ȃ
 
             // Java  tB[h length ꍇ int ^Ԃ
             if (Settings.getLanguage().equals(LANGUAGE.JAVA) && fieldName.equals("length")) {
                 this.resolvedInfo = new ArrayLengthUsageInfo(ownerUsage, fromLine, fromColumn,
                         toLine, toColumn);
                 return this.resolvedInfo;
             }
         }
 
         assert false : "Here shouldn't be reached!";
         this.resolvedInfo = new UnknownEntityUsageInfo(fromLine, fromColumn, toLine, toColumn);
         return this.resolvedInfo;
     }
 
     /**
      * gp\ȖOԂԂ
      * 
      * @return gp\ȖOԂԂ
      */
     public Set<AvailableNamespaceInfo> getAvailableNamespaces() {
         return this.availableNamespaces;
     }
 
     /**
      * tB[hgpsϐ̖^Ԃ
      * 
      * @return tB[hgpsϐ̖^
      */
     public UnresolvedEntityUsageInfo getOwnerClassType() {
         return this.ownerClassType;
     }
 
     /**
      * tB[hԂ
      * 
      * @return tB[h
      */
     public String getFieldName() {
         return this.fieldName;
     }
 
     /**
      * gp\ȖOԂۑ邽߂̕ϐ
      */
     private final Set<AvailableNamespaceInfo> availableNamespaces;
 
     /**
      * tB[hgpsϐ̖^ۑ邽߂̕ϐ
      */
     private final UnresolvedEntityUsageInfo ownerClassType;
 
     /**
      * tB[hۑ邽߂̕ϐ
      */
     private final String fieldName;
 }
