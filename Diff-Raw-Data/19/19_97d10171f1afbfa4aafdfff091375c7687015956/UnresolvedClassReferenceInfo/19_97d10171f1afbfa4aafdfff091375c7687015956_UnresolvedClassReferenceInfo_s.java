 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassReferenceInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.EntityUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetInnerClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnknownEntityUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnknownTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * NXQƂ\NX
  * 
  * @author higo
  * 
  */
 public class UnresolvedClassReferenceInfo extends UnresolvedEntityUsageInfo {
 
     /**
      * p\ȖOԖCQƖ^ď
      * 
      * @param availableNamespaces OԖ
      * @param referenceName QƖ
      */
     public UnresolvedClassReferenceInfo(final Set<AvailableNamespaceInfo> availableNamespaces,
             final String[] referenceName) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == availableNamespaces) || (null == referenceName)) {
             throw new NullPointerException();
         }
 
         this.availableNamespaces = availableNamespaces;
         this.referenceName = referenceName;
         this.fullReferenceName = referenceName;
         this.ownerUsage = null;
         this.typeArguments = new LinkedList<UnresolvedReferenceTypeInfo>();
     }
 
     /**
      * p\ȖOԖCQƖ^ď
      * 
      * @param availableNamespaces OԖ
      * @param referenceName QƖ
      * @param ownerUsage eQ
      */
     public UnresolvedClassReferenceInfo(final Set<AvailableNamespaceInfo> availableNamespaces,
             final String[] referenceName, final UnresolvedClassReferenceInfo ownerUsage) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == availableNamespaces) || (null == referenceName) || (null == ownerUsage)) {
             throw new NullPointerException();
         }
 
         this.availableNamespaces = availableNamespaces;
         String[] ownerReferenceName = ownerUsage.getFullReferenceName();
         String[] fullReferenceName = new String[referenceName.length + ownerReferenceName.length];
         System.arraycopy(ownerReferenceName, 0, fullReferenceName, 0, ownerReferenceName.length);
         System.arraycopy(referenceName, 0, fullReferenceName, ownerReferenceName.length,
                 referenceName.length);
         this.fullReferenceName = fullReferenceName;
         this.referenceName = referenceName;
         this.ownerUsage = ownerUsage;
         this.typeArguments = new LinkedList<UnresolvedReferenceTypeInfo>();
     }
 
     /**
      * ɉς݂ǂԂD
      * 
      * @return ς݂łꍇ trueCłȂꍇ false
      */
     @Override
     public boolean alreadyResolved() {
         return null != this.resolvedInfo;
     }
 
     /**
      * ς݃NXQƂԂ
      * 
      * @return ς݃NXQ
      * @throws NotResolvedException ĂȂꍇɃX[
      */
     @Override
     public EntityUsageInfo getResolvedEntityUsage() {
 
         if (!this.alreadyResolved()) {
             throw new NotResolvedException();
         }
 
         return this.resolvedInfo;
     }
 
     @Override
     public EntityUsageInfo resolveEntityUsage(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == usingClass) || (null == classInfoManager)) {
             throw new NullPointerException();
         }
 
         // ɉς݂łꍇ́CLbVԂ
         if (this.alreadyResolved()) {
             return this.getResolvedEntityUsage();
         }
 
         //@ʒu擾
         final int fromLine = this.getFromLine();
         final int fromColumn = this.getFromColumn();
         final int toLine = this.getToLine();
         final int toColumn = this.getToColumn();
 
         final String[] referenceName = this.getReferenceName();
 
         if (this.hasOwnerReference()) {
 
             final UnresolvedClassReferenceInfo unresolvedClassReference = this.getOwnerUsage();
             EntityUsageInfo classReference = unresolvedClassReference.resolveEntityUsage(
                     usingClass, usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
             assert null != classReference : "null is returned!";
 
             NEXT_NAME: for (int i = 0; i < referenceName.length; i++) {
 
                 // e UnknownTypeInfo Cǂ悤Ȃ
                 if (classReference.getType() instanceof UnknownTypeInfo) {
 
                     this.resolvedInfo = new UnknownEntityUsageInfo(fromLine, fromColumn, toLine,
                             toColumn);
                     return this.resolvedInfo;
 
                     // eΏۃNX(TargetClassInfo)̏ꍇ
                 } else if (classReference.getType() instanceof ClassTypeInfo) {
 
                     final ClassInfo ownerClass = ((ClassTypeInfo) classReference.getType())
                             .getReferencedClass();
                     if (ownerClass instanceof TargetClassInfo) {
 
                         // Ci[NXT̂ňꗗ擾
                         final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                .getAvailableDirectInnerClasses((TargetClassInfo) classReference
                                        .getType());
                         for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                             // vNXꍇ
                             if (referenceName[i].equals(innerClass.getClassName())) {
                                 // TODO p֌W\zR[hKvH
 
                                 // TODO ^p[^ǋL鏈Kv
                                 final ClassTypeInfo reference = new ClassTypeInfo(innerClass);
                                 classReference = new ClassReferenceInfo(reference, fromLine,
                                         fromColumn, toLine, toColumn);
                                 continue NEXT_NAME;
                             }
                         }
 
                         assert false : "Here shouldn't be reached!";
 
                         // eONX(ExternalClassInfo)̏ꍇ
                     } else if (ownerClass instanceof ExternalClassInfo) {
 
                         classReference = new UnknownEntityUsageInfo(fromLine, fromColumn, toLine,
                                 toColumn);
                         continue NEXT_NAME;
                     }
                 }
 
                 assert false : "Here shouldn't be reached!";
             }
 
             this.resolvedInfo = classReference;
             return this.resolvedInfo;
 
         } else {
 
             // Qƌ^ UnresolvedFullQualifiedNameReferenceTypeInfo Ȃ΁CS薼QƂłƔfł
             if (this instanceof UnresolvedFullQualifiedNameClassReferenceInfo) {
 
                 ClassInfo classInfo = classInfoManager.getClassInfo(referenceName);
                 if (null == classInfo) {
                     classInfo = new ExternalClassInfo(referenceName);
                     classInfoManager.add((ExternalClassInfo) classInfo);
                 }
 
                 // TODO ^p[^ǋL鏈Kv
                 final ClassTypeInfo reference = new ClassTypeInfo(classInfo);
                 this.resolvedInfo = new ClassReferenceInfo(reference, fromLine, fromColumn, toLine,
                         toColumn);
                 return this.resolvedInfo;
             }
 
             // QƖS薼łƂČ
             {
                 final ClassInfo classInfo = classInfoManager.getClassInfo(referenceName);
                 if (null != classInfo) {
 
                     // TODO@^p[^ǋL鏈Kv
                     final ClassTypeInfo reference = new ClassTypeInfo(classInfo);
                     this.resolvedInfo = new ClassReferenceInfo(reference, fromLine, fromColumn,
                             toLine, toColumn);
                     return this.resolvedInfo;
                 }
             }
 
             // p\ȃCi[NXT
             {
                 final TargetClassInfo outestClass;
                 if (usingClass instanceof TargetInnerClassInfo) {
                     outestClass = NameResolver.getOuterstClass((TargetInnerClassInfo) usingClass);
                 } else {
                     outestClass = usingClass;
                 }
 
                 for (final TargetInnerClassInfo innerClassInfo : NameResolver
                         .getAvailableInnerClasses(outestClass)) {
 
                     if (innerClassInfo.getClassName().equals(referenceName[0])) {
 
                         // availableField.getType() 玟word(name[i])𖼑O
                         // TODO ^p[^i[鏈Kv
                         ClassTypeInfo reference = new ClassTypeInfo(innerClassInfo);
                         EntityUsageInfo classReference = new ClassReferenceInfo(reference,
                                 fromLine, fromColumn, toLine, toColumn);
                         NEXT_NAME: for (int i = 1; i < referenceName.length; i++) {
 
                             // e UnknownTypeInfo Cǂ悤Ȃ
                             if (classReference.getType() instanceof UnknownTypeInfo) {
 
                                 this.resolvedInfo = new UnknownEntityUsageInfo(fromLine,
                                         fromColumn, toLine, toColumn);
                                 return this.resolvedInfo;
 
                                 // eNX^̏ꍇ
                             } else if (classReference.getType() instanceof ClassTypeInfo) {
 
                                 final ClassInfo ownerClass = ((ClassTypeInfo) classReference
                                         .getType()).getReferencedClass();
 
                                 // eΏۃNX(TargetClassInfo)̏ꍇ
                                 if (ownerClass instanceof TargetClassInfo) {
 
                                     // Ci[NXT̂ňꗗ擾
                                     final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                            .getAvailableDirectInnerClasses((TargetClassInfo) classReference
                                                    .getType());
                                     for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                         // vNXꍇ
                                         if (referenceName[i].equals(innerClass.getClassName())) {
                                             // TODO p֌W\zR[hKvH
 
                                             // TODO@^p[^i[鏈Kv
                                             reference = new ClassTypeInfo(innerClass);
                                             classReference = new ClassReferenceInfo(reference,
                                                     fromLine, fromColumn, toLine, toColumn);
                                             continue NEXT_NAME;
                                         }
                                     }
 
                                     assert false : "Here shouldn't be reached!";
 
                                     // eONX(ExternalClassInfo)̏ꍇ
                                 } else if (ownerClass instanceof ExternalClassInfo) {
 
                                     classReference = new UnknownEntityUsageInfo(fromLine,
                                             fromColumn, toLine, toColumn);
                                     continue NEXT_NAME;
                                 }
                             }
 
                             assert false : "Here shouldn't be reached!";
                         }
 
                         this.resolvedInfo = classReference;
                         return this.resolvedInfo;
                     }
                 }
             }
 
             // p\ȖOԂ^T
             {
                 for (final AvailableNamespaceInfo availableNamespace : this
                         .getAvailableNamespaces()) {
 
                     // OԖ.* ƂȂĂꍇ
                     if (availableNamespace.isAllClasses()) {
                         final String[] namespace = availableNamespace.getNamespace();
 
                         // OԂ̉ɂeNXɑ΂
                         for (final ClassInfo classInfo : classInfoManager.getClassInfos(namespace)) {
 
                             // NXƎQƖ̐擪ꍇ́C̃NXQƐłƌ肷
                             final String className = classInfo.getClassName();
                             if (className.equals(referenceName[0])) {
 
                                 // availableField.getType() 玟word(name[i])𖼑O
                                 // TODO ^p[^i[鏈Kv
                                 ClassTypeInfo reference = new ClassTypeInfo(classInfo);
                                 EntityUsageInfo classReference = new ClassReferenceInfo(reference,
                                         fromLine, fromColumn, toLine, toColumn);
                                 NEXT_NAME: for (int i = 1; i < referenceName.length; i++) {
 
                                     // e UnknownTypeInfo Cǂ悤Ȃ
                                     if (classReference.getType() instanceof UnknownTypeInfo) {
 
                                         this.resolvedInfo = new UnknownEntityUsageInfo(fromLine,
                                                 fromColumn, toLine, toColumn);
                                         return this.resolvedInfo;
 
                                         // eNX^̏ꍇ
                                     } else if (classReference.getType() instanceof ClassTypeInfo) {
 
                                         final ClassInfo ownerClass = ((ClassTypeInfo) classReference
                                                 .getType()).getReferencedClass();
 
                                         // eΏۃNX(TargetClassInfo)̏ꍇ                                         
                                         if (ownerClass instanceof TargetClassInfo) {
 
                                             // Ci[NXT̂ňꗗ擾
                                             final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                                    .getAvailableDirectInnerClasses((TargetClassInfo) classReference
                                                            .getType());
                                             for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                                 // vNXꍇ
                                                 if (referenceName[i].equals(innerClass
                                                         .getClassName())) {
                                                     // TODO p֌W\zR[hKvH
 
                                                     // TODO ^p[^i[鏈Kv
                                                     reference = new ClassTypeInfo(innerClass);
                                                     classReference = new ClassReferenceInfo(
                                                             reference, fromLine, fromColumn,
                                                             toLine, toColumn);
                                                     continue NEXT_NAME;
                                                 }
                                             }
 
                                             // Ȃ̂ null ԂD
                                             // ݂̑zł́C̕ɓ̂͌p֌W̖OSɏIĂȂiK݂̂̂͂D
                                             return null;
 
                                             // eONX(ExternalClassInfo)̏ꍇ
                                         } else if (ownerClass instanceof ExternalClassInfo) {
 
                                             classReference = new UnknownEntityUsageInfo(fromLine,
                                                     fromColumn, toLine, toColumn);
                                             continue NEXT_NAME;
                                         }
                                     }
 
                                     assert false : "Here shouldn't be reached!";
                                 }
 
                                 this.resolvedInfo = classReference;
                                 return this.resolvedInfo;
                             }
                         }
 
                         // O.NX ƂȂĂꍇ
                     } else {
 
                         final String[] importName = availableNamespace.getImportName();
 
                         // NXƎQƖ̐擪ꍇ́C̃NXQƐłƌ肷
                         if (importName[importName.length - 1].equals(referenceName[0])) {
 
                             ClassInfo specifiedClassInfo = classInfoManager
                                     .getClassInfo(importName);
                             if (null == specifiedClassInfo) {
                                 specifiedClassInfo = new ExternalClassInfo(importName);
                                 classInfoManager.add((ExternalClassInfo) specifiedClassInfo);
                             }
 
                             // TODO ^p[^i[鏈Kv
                             ClassTypeInfo reference = new ClassTypeInfo(specifiedClassInfo);
                             EntityUsageInfo classReference = new ClassReferenceInfo(reference,
                                     fromLine, fromColumn, toLine, toColumn);
                             NEXT_NAME: for (int i = 1; i < referenceName.length; i++) {
 
                                 // e UnknownTypeInfo Cǂ悤Ȃ
                                 if (classReference.getType() instanceof UnknownTypeInfo) {
 
                                     this.resolvedInfo = new UnknownEntityUsageInfo(fromLine,
                                             fromColumn, toLine, toColumn);
                                     return this.resolvedInfo;
 
                                     // eNX^̏ꍇ
                                 } else if (classReference.getType() instanceof ClassTypeInfo) {
 
                                     final ClassInfo ownerClass = ((ClassTypeInfo) classReference
                                             .getType()).getReferencedClass();
 
                                     // eΏۃNX(TargetClassInfo)̏ꍇ
                                     if (ownerClass instanceof TargetClassInfo) {
 
                                         // Ci[NXꗗ擾
                                         final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                                .getAvailableDirectInnerClasses((TargetClassInfo) classReference
                                                        .getType());
                                         for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                             // vNXꍇ
                                             if (referenceName[i].equals(innerClass.getClassName())) {
                                                 // TODO p֌W\zR[hKvH
 
                                                 // TODO ^p[^i[鏈Kv
                                                 reference = new ClassTypeInfo(innerClass);
                                                 classReference = new ClassReferenceInfo(reference,
                                                         fromLine, fromColumn, toLine, toColumn);
                                                 continue NEXT_NAME;
                                             }
                                         }
 
                                         // eONX(ExternalClassInfo)̏ꍇ
                                     } else if (ownerClass instanceof ExternalClassInfo) {
 
                                         classReference = new UnknownEntityUsageInfo(fromLine,
                                                 fromColumn, toLine, toColumn);
                                         continue NEXT_NAME;
                                     }
                                 }
 
                                 assert false : "Here shouldn't be reached!";
                             }
 
                             this.resolvedInfo = classReference;
                             return this.resolvedInfo;
                         }
                     }
                 }
             }
         }
 
         /*
          * if (null == usingMethod) { err.println("Remain unresolved \"" +
          * reference.getReferenceName(Settings.getLanguage().getNamespaceDelimiter()) + "\"" + " on
          * \"" + usingClass.getFullQualifiedtName(LANGUAGE.JAVA.getNamespaceDelimiter())); } else {
          * err.println("Remain unresolved \"" +
          * reference.getReferenceName(Settings.getLanguage().getNamespaceDelimiter()) + "\"" + " on
          * \"" + usingClass.getFullQualifiedtName(LANGUAGE.JAVA.getNamespaceDelimiter()) + "#" +
          * usingMethod.getMethodName() + "\"."); }
          */
 
         // Ȃꍇ́CUknownTypeInfo Ԃ
         this.resolvedInfo = new UnknownEntityUsageInfo(fromLine, fromColumn, toLine, toColumn);
         return this.resolvedInfo;
     }
 
     /**
      * ^p[^gpǉ
      * 
      * @param typeArgument ǉ^p[^gp
      */
     public final void addTypeArgument(final UnresolvedReferenceTypeInfo typeArgument) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == typeArgument) {
             throw new NullPointerException();
         }
 
         this.typeArguments.add(typeArgument);
     }
 
     /**
      * ̃NXQƂŎgpĂ^p[^ List Ԃ
      * 
      * @return ̃NXQƂŎgpĂ^p[^ List
      */
     public final List<UnresolvedReferenceTypeInfo> getTypeArguments() {
         return Collections.unmodifiableList(this.typeArguments);
     }
 
     /**
      * ̎Qƌ^owner܂߂QƖԂ
      * 
      * @return ̎Qƌ^owner܂߂QƖԂ
      */
     public final String[] getFullReferenceName() {
         return this.fullReferenceName;
     }
 
     /**
      * ̎Qƌ^̎QƖԂ
      * 
      * @return ̎Qƌ^̎QƖԂ
      */
     public final String[] getReferenceName() {
         return this.referenceName;
     }
 
     /**
      * ̎Qƌ^Ă関Qƌ^Ԃ
      * 
      * @return ̎Qƌ^Ă関Qƌ^
      */
     public final UnresolvedClassReferenceInfo getOwnerUsage() {
         return this.ownerUsage;
     }
 
     /**
      * ̎Qƌ^C̎Qƌ^ɂĂ邩ǂԂ
      * 
      * @return Ăꍇ trueCĂȂꍇ false
      */
     public final boolean hasOwnerReference() {
         return null != this.ownerUsage;
     }
 
     /**
      * ̎Qƌ^̎QƖŗ^ꂽŌĕԂ
      * 
      * @param delimiter ɗp镶
      * @return ̎Qƌ^̎QƖŗ^ꂽŌ
      */
     public final String getReferenceName(final String delimiter) {
 
         if (null == delimiter) {
             throw new NullPointerException();
         }
 
         final StringBuilder sb = new StringBuilder(this.referenceName[0]);
         for (int i = 1; i < this.referenceName.length; i++) {
             sb.append(delimiter);
             sb.append(this.referenceName[i]);
         }
 
         return sb.toString();
     }
 
     /**
      * ̎Qƌ^̊S薼Ƃĉ\̂閼OԖ̈ꗗԂ
      * 
      * @return ̎Qƌ^̊S薼Ƃĉ\̂閼OԖ̈ꗗ
      */
     public final Set<AvailableNamespaceInfo> getAvailableNamespaces() {
         return this.availableNamespaces;
     }
 
     public final static UnresolvedClassReferenceInfo createClassReference(
             UnresolvedClassTypeInfo referenceType) {
         return new UnresolvedClassReferenceInfo(referenceType.getAvailableNamespaces(),
                 referenceType.getReferenceName());
     }
 
     /**
      * p\ȖOԖۑ邽߂̕ϐCO̍ۂɗp
      */
     private final Set<AvailableNamespaceInfo> availableNamespaces;
 
     /**
      * QƖۑϐ
      */
     private final String[] referenceName;
 
     /**
      * owner܂߂QƖۑϐ
      */
     private final String[] fullReferenceName;
 
     /**
      * ̎QƂĂ関Qƌ^ۑϐ
      */
     private final UnresolvedClassReferenceInfo ownerUsage;
 
     /**
      * ^p[^gpۑ邽߂̕ϐ
      */
     private final List<UnresolvedReferenceTypeInfo> typeArguments;
 
     /**
      * ς݃NXQƂۑ邽߂̕ϐ
      */
     protected EntityUsageInfo resolvedInfo;
 }
