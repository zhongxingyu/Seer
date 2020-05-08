 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.Settings;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ArrayTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.Members;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.NullTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.OPERATOR;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.PrimitiveTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.SuperTypeParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetFieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetInnerClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnknownTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.VoidTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalFieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.DefaultMessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessageSource;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter.MESSAGE_TYPE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.LANGUAGE;
 
 
 /**
  * ^邽߂̃[eBeBNX
  * 
  * @author y-higo
  * 
  */
 public final class NameResolver {
 
     /**
      * ^iUnresolvedTypeInfojς݌^iTypeInfojԂD Ής݌^񂪂Ȃꍇ UnknownTypeInfo ԂD
      * 
      * @param unresolvedTypeInfo O^
      * @param usingClass ̖^݂ĂNX
      * @param usingMethod ̖^݂Ă郁\bhC\bhOłꍇ null ^
      * @param classInfoManager ^ɗpNXf[^x[X
      * @param fieldInfoManager ^ɗptB[hf[^x[X
      * @param methodInfoManager ^ɗp郁\bhf[^x[X
      * @param resolvedCache ςUnresolvedTypeInfõLbV
      * @return Oꂽ^
      */
     public static TypeInfo resolveTypeInfo(final UnresolvedTypeInfo unresolvedTypeInfo,
             final TargetClassInfo usingClass, final TargetMethodInfo usingMethod,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager,
             final Map<UnresolvedTypeInfo, TypeInfo> resolvedCache) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == unresolvedTypeInfo) {
             throw new NullPointerException();
         }
 
         // ɉς݂ł΁C^擾
         if ((null != resolvedCache) && resolvedCache.containsKey(unresolvedTypeInfo)) {
             final TypeInfo type = resolvedCache.get(unresolvedTypeInfo);
             return type;
         }
 
         // v~eBu^̏ꍇ
         if (unresolvedTypeInfo instanceof PrimitiveTypeInfo) {
             return (PrimitiveTypeInfo) unresolvedTypeInfo;
 
             // void^̏ꍇ
         } else if (unresolvedTypeInfo instanceof VoidTypeInfo) {
             return (VoidTypeInfo) unresolvedTypeInfo;
 
         } else if (unresolvedTypeInfo instanceof NullTypeInfo) {
             return (NullTypeInfo) unresolvedTypeInfo;
 
             // Qƌ^̏ꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedReferenceTypeInfo) {
 
             final TypeInfo classInfo = NameResolver.resolveClassReference(
                     (UnresolvedReferenceTypeInfo) unresolvedTypeInfo, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
             return classInfo;
 
             // ^p[^̏ꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedTypeParameterInfo) {
 
             final TypeInfo typeParameterInfo = NameResolver.resolveTypeParameter(
                     (UnresolvedTypeParameterInfo) unresolvedTypeInfo, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
             return typeParameterInfo;
 
             // z^̏ꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedArrayTypeInfo) {
 
             final UnresolvedTypeInfo unresolvedElementType = ((UnresolvedArrayTypeInfo) unresolvedTypeInfo)
                     .getElementType();
             final int dimension = ((UnresolvedArrayTypeInfo) unresolvedTypeInfo).getDimension();
 
             final TypeInfo elementType = NameResolver.resolveTypeInfo(unresolvedElementType,
                     usingClass, usingMethod, classInfoManager, fieldInfoManager, methodInfoManager,
                     resolvedCache);
             assert elementType != null : "resolveTypeInfo returned null!";
 
             // vf̌^ŝƂ UnnownTypeInfo Ԃ
             if (elementType instanceof UnknownTypeInfo) {
                 return UnknownTypeInfo.getInstance();
 
                 // vf̌^łꍇ͂̔z^쐬Ԃ
             } else {
                 final ArrayTypeInfo arrayType = ArrayTypeInfo.getType(elementType, dimension);
                 return arrayType;
             }
 
             // NX̏ꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedClassInfo) {
 
             final TypeInfo classInfo = (ClassInfo) ((UnresolvedClassInfo) unresolvedTypeInfo)
                     .getResolvedInfo();
             return classInfo;
 
             // tB[hgp̏ꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedFieldUsage) {
 
             final TypeInfo classInfo = NameResolver.resolveFieldReference(
                     (UnresolvedFieldUsage) unresolvedTypeInfo, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
             return classInfo;
 
             // \bhĂяȍꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedMethodCall) {
 
             // NX`擾
             final TypeInfo classInfo = NameResolver.resolveMethodCall(
                     (UnresolvedMethodCall) unresolvedTypeInfo, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
             return classInfo;
 
             // ɍZq̏ꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedBinominalOperation) {
 
             // 񍀉Ž^
             final TypeInfo operationResultType = NameResolver.resolveBinomialOperation(
                     (UnresolvedBinominalOperation) unresolvedTypeInfo, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
             return operationResultType;
 
             // GeBeBgp̏ꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedEntityUsage) {
 
             // GeBeB̃NX`擾
             final TypeInfo classInfo = NameResolver.resolveEntityUsage(
                     (UnresolvedEntityUsage) unresolvedTypeInfo, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
             return classInfo;
 
             // zgp̏ꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedArrayElementUsage) {
 
             final TypeInfo classInfo = NameResolver.resolveArrayElementUsage(
                     (UnresolvedArrayElementUsage) unresolvedTypeInfo, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
             return classInfo;
 
             // ȊǑ^̏ꍇ̓G[
         } else {
             throw new IllegalArgumentException(unresolvedTypeInfo.toString()
                     + " is a wrong object!");
         }
     }
 
     /**
      * NXQƂCQƌ^ԂD
      * 
      * @param reference NXQ
      * @param usingClass NXQƂsĂNX
      * @param usingMethod NXQƂsĂ郁\bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @param resolvedCache ςUnresolvedTypeInfõLbV
      * @return ςݎQƌ^
      */
     public static TypeInfo resolveClassReference(final UnresolvedReferenceTypeInfo reference,
             final TargetClassInfo usingClass, final TargetMethodInfo usingMethod,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager,
             final Map<UnresolvedTypeInfo, TypeInfo> resolvedCache) {
 
         if ((null == reference) || (null == classInfoManager)) {
             throw new NullPointerException();
         }
 
         final String[] referenceName = reference.getReferenceName();
 
         // Qƌ^ UnresolvedFullQualifiedNameReferenceTypeInfo Ȃ΁CS薼QƂłƔfł
         if (reference instanceof UnresolvedFullQualifiedNameReferenceTypeInfo) {
 
             ClassInfo classInfo = classInfoManager.getClassInfo(referenceName);
             if (null == classInfo) {
                 classInfo = new ExternalClassInfo(referenceName);
                 classInfoManager.add((ExternalClassInfo) classInfo);
             }
 
             // LbVpnbVe[uꍇ̓LbVǉ
             if (null != resolvedCache) {
                 resolvedCache.put(reference, classInfo);
             }
             return classInfo;
         }
 
         // QƖS薼łƂČ
         {
             final ClassInfo classInfo = classInfoManager.getClassInfo(referenceName);
             if (null != classInfo) {
                 return classInfo;
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
                     TypeInfo ownerTypeInfo = innerClassInfo;
                     NEXT_NAME: for (int i = 1; i < referenceName.length; i++) {
 
                         // e UnknownTypeInfo Cǂ悤Ȃ
                         if (ownerTypeInfo instanceof UnknownTypeInfo) {
 
                             return UnknownTypeInfo.getInstance();
 
                             // eΏۃNX(TargetClassInfo)̏ꍇ
                         } else if (ownerTypeInfo instanceof TargetClassInfo) {
 
                             // Ci[NXT̂ňꗗ擾
                             final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                     .getAvailableDirectInnerClasses((TargetClassInfo) ownerTypeInfo);
                             for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                 // vNXꍇ
                                 if (referenceName[i].equals(innerClass.getClassName())) {
                                     // TODO p֌W\zR[hKvH
 
                                     ownerTypeInfo = innerClass;
                                     continue NEXT_NAME;
                                 }
                             }
 
                             assert false : "Here should be reached!";
 
                             // eONX(ExternalClassInfo)̏ꍇ
                         } else if (ownerTypeInfo instanceof ExternalClassInfo) {
 
                             ownerTypeInfo = UnknownTypeInfo.getInstance();
                             continue NEXT_NAME;
                         }
 
                         assert false : "Here should be reached!";
                     }
 
                     // LbVpnbVe[uꍇ̓LbVǉ
                     if (null != resolvedCache) {
                         resolvedCache.put(reference, ownerTypeInfo);
                     }
 
                     return ownerTypeInfo;
                 }
             }
         }
 
         // p\ȖOԂ^T
         {
             for (final AvailableNamespaceInfo availableNamespace : reference
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
                             TypeInfo ownerTypeInfo = classInfo;
                             NEXT_NAME: for (int i = 1; i < referenceName.length; i++) {
 
                                 // e UnknownTypeInfo Cǂ悤Ȃ
                                 if (ownerTypeInfo instanceof UnknownTypeInfo) {
 
                                     return UnknownTypeInfo.getInstance();
 
                                     // eΏۃNX(TargetClassInfo)̏ꍇ
                                 } else if (ownerTypeInfo instanceof TargetClassInfo) {
 
                                     // Ci[NXT̂ňꗗ擾
                                     final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                             .getAvailableDirectInnerClasses((TargetClassInfo) ownerTypeInfo);
                                     for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                         // vNXꍇ
                                         if (referenceName[i].equals(innerClass.getClassName())) {
                                             // TODO p֌W\zR[hKvH
 
                                             ownerTypeInfo = innerClass;
                                             continue NEXT_NAME;
                                         }
                                     }
 
                                     assert false : "Here should be reached!";
 
                                     // eONX(ExternalClassInfo)̏ꍇ
                                 } else if (ownerTypeInfo instanceof ExternalClassInfo) {
 
                                     ownerTypeInfo = UnknownTypeInfo.getInstance();
                                     continue NEXT_NAME;
                                 }
 
                                 assert false : "Here should be reached!";
                             }
 
                             // LbVpnbVe[uꍇ̓LbVǉ
                             if (null != resolvedCache) {
                                 resolvedCache.put(reference, ownerTypeInfo);
                             }
 
                             return ownerTypeInfo;
                         }
                     }
 
                     // O.NX ƂȂĂꍇ
                 } else {
 
                     final String[] importName = availableNamespace.getImportName();
 
                     // NXƎQƖ̐擪ꍇ́C̃NXQƐłƌ肷
                     if (importName[importName.length - 1].equals(referenceName[0])) {
 
                         ClassInfo specifiedClassInfo = classInfoManager.getClassInfo(importName);
                         if (null == specifiedClassInfo) {
                             specifiedClassInfo = new ExternalClassInfo(importName);
                             classInfoManager.add((ExternalClassInfo) specifiedClassInfo);
                         }
 
                         TypeInfo ownerTypeInfo = specifiedClassInfo;
                         NEXT_NAME: for (int i = 1; i < referenceName.length; i++) {
 
                             // e UnknownTypeInfo Cǂ悤Ȃ
                             if (ownerTypeInfo instanceof UnknownTypeInfo) {
 
                                 return UnknownTypeInfo.getInstance();
 
                                 // eΏۃNX(TargetClassInfo)̏ꍇ
                             } else if (ownerTypeInfo instanceof TargetClassInfo) {
 
                                 // Ci[NXꗗ擾
                                 final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                         .getAvailableDirectInnerClasses((TargetClassInfo) ownerTypeInfo);
                                 for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                     // vNXꍇ
                                     if (referenceName[i].equals(innerClass.getClassName())) {
                                         // TODO p֌W\zR[hKvH
 
                                         ownerTypeInfo = innerClass;
                                         continue NEXT_NAME;
                                     }
                                 }
 
                                 // eONX(ExternalClassInfo)̏ꍇ
                             } else if (ownerTypeInfo instanceof ExternalClassInfo) {
 
                                 ownerTypeInfo = UnknownTypeInfo.getInstance();
                                 continue NEXT_NAME;
                             }
 
                             assert false : "Here shouldn't be reached!";
                         }
 
                         // ς݃LbVɓo^
                         if (null != resolvedCache) {
                             resolvedCache.put(reference, ownerTypeInfo);
                         }
 
                         return ownerTypeInfo;
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
         return UnknownTypeInfo.getInstance();
     }
 
     /**
      * ^p[^Cς݌^p[^Ԃ
      * 
      * @param unresolvedTypeParameter ^p[^
      * @param usingClass ^p[^錾ĂNX
      * @param usingMethod ^p[^錾Ă郁\bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @param resolvedCache ςUnresolvedTypeInfõLbV
      * @return ς݌^p[^
      */
     public static TypeInfo resolveTypeParameter(
             final UnresolvedTypeParameterInfo unresolvedTypeParameter,
             final TargetClassInfo usingClass, final TargetMethodInfo usingMethod,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager,
             final Map<UnresolvedTypeInfo, TypeInfo> resolvedCache) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == unresolvedTypeParameter) || (null == classInfoManager)) {
             throw new NullPointerException();
         }
 
         // hNX^p[^<T super B>̏ꍇ
         if (unresolvedTypeParameter instanceof UnresolvedSuperTypeParameterInfo) {
 
             final String name = unresolvedTypeParameter.getName();
             final UnresolvedTypeInfo unresolvedSuperType = ((UnresolvedSuperTypeParameterInfo) unresolvedTypeParameter)
                     .getSuperType();
             final TypeInfo superType = NameResolver.resolveTypeInfo(unresolvedSuperType,
                     usingClass, usingMethod, classInfoManager, fieldInfoManager, methodInfoManager,
                     resolvedCache);
 
             if (unresolvedTypeParameter.hasExtendsType()) {
 
                 final UnresolvedTypeInfo unresolvedExtendsType = unresolvedTypeParameter
                         .getExtendsType();
                 final TypeInfo extendsType = NameResolver.resolveTypeInfo(unresolvedExtendsType,
                         usingClass, usingMethod, classInfoManager, fieldInfoManager,
                         methodInfoManager, resolvedCache);
 
                 final SuperTypeParameterInfo superTypeParameter = new SuperTypeParameterInfo(name,
                         extendsType, superType);
                 if (null != resolvedCache) {
                     resolvedCache.put(unresolvedTypeParameter, superTypeParameter);
                 }
                 return superTypeParameter;
 
             } else {
 
                 final SuperTypeParameterInfo superTypeParameter = new SuperTypeParameterInfo(name,
                         null, superType);
                 if (null != resolvedCache) {
                     resolvedCache.put(unresolvedTypeParameter, superTypeParameter);
                 }
                 return superTypeParameter;
             }
 
             // ̑̏ꍇ
         } else {
 
             final String name = unresolvedTypeParameter.getName();
 
             if (unresolvedTypeParameter.hasExtendsType()) {
 
                 final UnresolvedTypeInfo unresolvedExtendsType = unresolvedTypeParameter
                         .getExtendsType();
                 final TypeInfo extendsType = NameResolver.resolveTypeInfo(unresolvedExtendsType,
                         usingClass, usingMethod, classInfoManager, fieldInfoManager,
                         methodInfoManager, resolvedCache);
 
                final TypeParameterInfo typeParameter = new TypeParameterInfo(name,
                        extendsType);
                 if (null != resolvedCache) {
                     resolvedCache.put(unresolvedTypeParameter, typeParameter);
                 }
                 return typeParameter;
 
             } else {
 
                 final TypeParameterInfo typeParameter = new TypeParameterInfo(name, null);
                 if (null != resolvedCache) {
                     resolvedCache.put(unresolvedTypeParameter, typeParameter);
                 }
                 return typeParameter;
             }
         }
     }
 
     /**
      * tB[hQƂCtB[hQƂsĂ郁\bhɓo^D܂CtB[ȟ^ԂD
      * 
      * @param fieldReference tB[hQ
      * @param usingClass tB[hQƂsĂNX
      * @param usingMethod tB[hQƂsĂ郁\bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @param resolvedCache ςUnresolvedTypeInfõLbV
      * @return ς݃tB[hQƂ̌^i܂CtB[ȟ^j
      */
     public static TypeInfo resolveFieldReference(final UnresolvedFieldUsage fieldReference,
             final TargetClassInfo usingClass, final TargetMethodInfo usingMethod,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager,
             final Map<UnresolvedTypeInfo, TypeInfo> resolvedCache) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == fieldReference) || (null == usingClass) || (null == usingMethod)
                 || (null == classInfoManager) || (null == fieldInfoManager)
                 || (null == methodInfoManager) || (null == resolvedCache)) {
             throw new NullPointerException();
         }
 
         // ɉς݂ł΁C^擾
         if (resolvedCache.containsKey(fieldReference)) {
             final TypeInfo type = resolvedCache.get(fieldReference);
             return type;
         }
 
         // tB[h擾
         final String fieldName = fieldReference.getFieldName();
 
         // ě^
         final UnresolvedTypeInfo unresolvedFieldOwnerClassType = fieldReference.getOwnerClassType();
         final TypeInfo fieldOwnerClassType = NameResolver.resolveTypeInfo(
                 unresolvedFieldOwnerClassType, usingClass, usingMethod, classInfoManager,
                 fieldInfoManager, methodInfoManager, resolvedCache);
         assert fieldOwnerClassType != null : "resolveTypeInfo returned null!";
 
         // -----eTypeInfo ɉď𕪊
         // ełȂꍇ͂ǂ悤Ȃ
         if (fieldOwnerClassType instanceof UnknownTypeInfo) {
 
             // Ȃs
             usingMethod.addUnresolvedUsage(fieldReference);
 
             // ς݃LbVɓo^
             resolvedCache.put(fieldReference, UnknownTypeInfo.getInstance());
 
             return UnknownTypeInfo.getInstance();
 
             // eΏۃNX(TargetClassInfo)ꍇ
         } else if (fieldOwnerClassType instanceof TargetClassInfo) {
 
             // ܂͗p\ȃtB[h猟
             {
                 // p\ȃtB[hꗗ擾
                 final List<TargetFieldInfo> availableFields = NameResolver.getAvailableFields(
                         (TargetClassInfo) fieldOwnerClassType, usingClass);
 
                 // p\ȃtB[hCtB[hŌ
                 for (TargetFieldInfo availableField : availableFields) {
 
                     // vtB[hꍇ
                     if (fieldName.equals(availableField.getName())) {
                         usingMethod.addReferencee(availableField);
                         availableField.addReferencer(usingMethod);
 
                         // ς݃LbVɓo^
                         resolvedCache.put(fieldReference, availableField.getType());
 
                         return availableField.getType();
 
                     }
                 }
             }
 
             // p\ȃtB[hȂꍇ́CONXłeNX͂
             // ̃NX̕ϐgpĂƂ݂Ȃ
             {
                 for (TargetClassInfo classInfo = (TargetClassInfo) fieldOwnerClassType; true; classInfo = ((TargetInnerClassInfo) classInfo)
                         .getOuterClass()) {
 
                     final ExternalClassInfo externalSuperClass = NameResolver
                             .getExternalSuperClass(classInfo);
                     if (null != externalSuperClass) {
 
                         final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(fieldName,
                                 externalSuperClass);
                         usingMethod.addReferencee(fieldInfo);
                         fieldInfo.addReferencer(usingMethod);
                         fieldInfoManager.add(fieldInfo);
 
                         // ς݃LbVɓo^
                         resolvedCache.put(fieldReference, fieldInfo.getType());
 
                         // ONXɐVKŊOϐ(ExternalFieldInfo)ǉ̂Ō^͕sD
                         return fieldInfo.getType();
                     }
 
                     if (!(classInfo instanceof TargetInnerClassInfo)) {
                         break;
                     }
                 }
             }
 
             // Ȃs
             {
                 err.println("Can't resolve field reference : " + fieldReference.getFieldName());
 
                 usingMethod.addUnresolvedUsage(fieldReference);
 
                 // ς݃LbVɓo^
                 resolvedCache.put(fieldReference, UnknownTypeInfo.getInstance());
 
                 return UnknownTypeInfo.getInstance();
             }
 
             // eONXiExternalClassInfojꍇ
         } else if (fieldOwnerClassType instanceof ExternalClassInfo) {
 
             final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(fieldName,
                     (ExternalClassInfo) fieldOwnerClassType);
             usingMethod.addReferencee(fieldInfo);
             fieldInfo.addReferencer(usingMethod);
             fieldInfoManager.add(fieldInfo);
 
             // ς݃LbVɓo^
             resolvedCache.put(fieldReference, fieldInfo.getType());
 
             // ONXɐVKŊOϐ(ExternalFieldInfo)ǉ̂Ō^͕sD
             return fieldInfo.getType();
 
         } else if (fieldOwnerClassType instanceof ArrayTypeInfo) {
 
             // TODO ͌ˑɂ邵Ȃ̂H z.length Ȃ
 
             // Java  tB[h length ꍇ int ^Ԃ
             if (Settings.getLanguage().equals(LANGUAGE.JAVA) && fieldName.equals("length")) {
 
                 resolvedCache.put(fieldReference, PrimitiveTypeInfo.INT);
                 return PrimitiveTypeInfo.INT;
             }
         }
 
         assert false : "Here shouldn't be reached!";
         return UnknownTypeInfo.getInstance();
     }
 
     /**
      * tB[hCtB[hsĂ郁\bhɓo^D܂CtB[ȟ^ԂD
      * 
      * @param fieldAssignment tB[h
      * @param usingClass tB[hsĂNX
      * @param usingMethod tB[hsĂ郁\bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @param resolvedCache ςUnresolvedTypeInfõLbV
      * @return ς݃tB[ȟ^i܂CtB[ȟ^j
      */
     public static TypeInfo resolveFieldAssignment(final UnresolvedFieldUsage fieldAssignment,
             final TargetClassInfo usingClass, final TargetMethodInfo usingMethod,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager,
             final Map<UnresolvedTypeInfo, TypeInfo> resolvedCache) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == fieldAssignment) || (null == usingClass) || (null == usingMethod)
                 || (null == classInfoManager) || (null == fieldInfoManager)
                 || (null == methodInfoManager) || (null == resolvedCache)) {
             throw new NullPointerException();
         }
 
         // ɉς݂ł΁C^擾
         if (resolvedCache.containsKey(fieldAssignment)) {
             final TypeInfo type = resolvedCache.get(fieldAssignment);
             return type;
         }
 
         // tB[h擾
         final String fieldName = fieldAssignment.getFieldName();
 
         // ě^
         final UnresolvedTypeInfo unresolvedFieldOwnerClassType = fieldAssignment
                 .getOwnerClassType();
         final TypeInfo fieldOwnerClassType = NameResolver.resolveTypeInfo(
                 unresolvedFieldOwnerClassType, usingClass, usingMethod, classInfoManager,
                 fieldInfoManager, methodInfoManager, resolvedCache);
         assert fieldOwnerClassType != null : "resolveTypeInfo returned null!";
 
         // -----eTypeInfo ɉď𕪊
         // ełȂꍇ͂ǂ悤Ȃ
         if (fieldOwnerClassType instanceof UnknownTypeInfo) {
 
             // Ȃs
             usingMethod.addUnresolvedUsage(fieldAssignment);
 
             // ς݃LbVɓo^
             resolvedCache.put(fieldAssignment, UnknownTypeInfo.getInstance());
 
             return UnknownTypeInfo.getInstance();
 
             // eΏۃNX(TargetClassInfo)ꍇ
         } else if (fieldOwnerClassType instanceof TargetClassInfo) {
 
             // ܂͗p\ȃtB[h猟
             {
                 // p\ȃtB[hꗗ擾
                 final List<TargetFieldInfo> availableFields = NameResolver.getAvailableFields(
                         (TargetClassInfo) fieldOwnerClassType, usingClass);
 
                 // p\ȃtB[hꗗCtB[hŌ
                 for (TargetFieldInfo availableField : availableFields) {
 
                     // vtB[hꍇ
                     if (fieldName.equals(availableField.getName())) {
                         usingMethod.addAssignmentee(availableField);
                         availableField.addAssignmenter(usingMethod);
 
                         // ς݃LbVɂɓo^
                         resolvedCache.put(fieldAssignment, availableField.getType());
 
                         return availableField.getType();
                     }
                 }
             }
 
             // p\ȃtB[hȂꍇ́CONXłeNX͂D
             // ̃NX̕ϐgpĂƂ݂Ȃ
             {
                 for (TargetClassInfo classInfo = (TargetClassInfo) fieldOwnerClassType; true; classInfo = ((TargetInnerClassInfo) classInfo)
                         .getOuterClass()) {
 
                     final ExternalClassInfo externalSuperClass = NameResolver
                             .getExternalSuperClass(classInfo);
                     if (null != externalSuperClass) {
 
                         final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(fieldName,
                                 externalSuperClass);
                         usingMethod.addAssignmentee(fieldInfo);
                         fieldInfo.addAssignmenter(usingMethod);
                         fieldInfoManager.add(fieldInfo);
 
                         // ς݃LbVɓo^
                         resolvedCache.put(fieldAssignment, fieldInfo.getType());
 
                         // ONXɐVKŊOϐiExternalFieldInfojǉ̂Ō^͕s
                         return fieldInfo.getType();
                     }
 
                     if (!(classInfo instanceof TargetInnerClassInfo)) {
                         break;
                     }
                 }
             }
 
             // Ȃs
             {
                 err.println("Can't resolve field assignment : " + fieldAssignment.getFieldName());
 
                 usingMethod.addUnresolvedUsage(fieldAssignment);
 
                 // ς݃LbVɓo^
                 resolvedCache.put(fieldAssignment, UnknownTypeInfo.getInstance());
 
                 return UnknownTypeInfo.getInstance();
             }
 
             // eONXiExternalClassInfojꍇ
         } else if (fieldOwnerClassType instanceof ExternalClassInfo) {
 
             final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(fieldName,
                     (ExternalClassInfo) fieldOwnerClassType);
             usingMethod.addAssignmentee(fieldInfo);
             fieldInfo.addAssignmenter(usingMethod);
             fieldInfoManager.add(fieldInfo);
 
             // ς݃LbVɓo^
             resolvedCache.put(fieldAssignment, fieldInfo.getType());
 
             // ONXɐVKŊOϐ(ExternalFieldInfo)ǉ̂Ō^͕sD
             return fieldInfo.getType();
         }
 
         assert false : "Here shouldn't be reached!";
         return UnknownTypeInfo.getInstance();
     }
 
     /**
      * \bhĂяoC\bhĂяosĂ郁\bhɓo^D܂C\bh̕Ԃľ^ԂD
      * 
      * @param methodCall \bhĂяo
      * @param usingClass \bhĂяosĂNX
      * @param usingMethod \bhĂяosĂ郁\bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @param resolvedCache ςUnresolvedTypeInfõLbV
      * @return \bhĂяoɑΉ MethodInfo
      */
     public static TypeInfo resolveMethodCall(final UnresolvedMethodCall methodCall,
             final TargetClassInfo usingClass, final TargetMethodInfo usingMethod,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager,
             final Map<UnresolvedTypeInfo, TypeInfo> resolvedCache) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == methodCall) || (null == usingClass) || (null == usingMethod)
                 || (null == classInfoManager) || (null == methodInfoManager)
                 || (null == resolvedCache)) {
             throw new NullPointerException();
         }
 
         // ɉς݂ł΁C^擾
         if (resolvedCache.containsKey(methodCall)) {
             final TypeInfo type = resolvedCache.get(methodCall);
             return type;
         }
 
         // \bh̃VOl`擾
         final String methodName = methodCall.getMethodName();
         final boolean constructor = methodCall.isConstructor();
         final List<UnresolvedTypeInfo> unresolvedParameterTypes = methodCall.getParameterTypes();
 
         // \bh̖
         final List<TypeInfo> parameterTypes = new LinkedList<TypeInfo>();
         for (UnresolvedTypeInfo unresolvedParameterType : unresolvedParameterTypes) {
             TypeInfo parameterType = NameResolver.resolveTypeInfo(unresolvedParameterType,
                     usingClass, usingMethod, classInfoManager, fieldInfoManager, methodInfoManager,
                     resolvedCache);
             assert parameterType != null : "resolveTypeInfo returned null!";
             if (parameterType instanceof UnknownTypeInfo) {
                 if (unresolvedParameterType instanceof UnresolvedReferenceTypeInfo) {
                     parameterType = NameResolver
                             .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedParameterType);
                     classInfoManager.add((ExternalClassInfo) parameterType);
                 } else if (unresolvedParameterType instanceof UnresolvedArrayTypeInfo) {
                     final UnresolvedTypeInfo unresolvedElementType = ((UnresolvedArrayTypeInfo) unresolvedParameterType)
                             .getElementType();
                     final int dimension = ((UnresolvedArrayTypeInfo) unresolvedParameterType)
                             .getDimension();
                     final TypeInfo elementType = NameResolver
                             .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedElementType);
                     classInfoManager.add((ExternalClassInfo) elementType);
                     parameterType = ArrayTypeInfo.getType(elementType, dimension);
                 }
             }
             parameterTypes.add(parameterType);
         }
 
         // ě^
         final UnresolvedTypeInfo unresolvedMethodOwnerClassType = methodCall.getOwnerClassType();
         TypeInfo methodOwnerClassType = NameResolver.resolveTypeInfo(
                 unresolvedMethodOwnerClassType, usingClass, usingMethod, classInfoManager,
                 fieldInfoManager, methodInfoManager, resolvedCache);
         assert methodOwnerClassType != null : "resolveTypeInfo returned null!";
         if (methodOwnerClassType instanceof UnknownTypeInfo) {
             if (unresolvedMethodOwnerClassType instanceof UnresolvedReferenceTypeInfo) {
                 methodOwnerClassType = NameResolver
                         .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedMethodOwnerClassType);
                 classInfoManager.add((ExternalClassInfo) methodOwnerClassType);
             }
         }
 
         // -----eTypeInfo ɉď𕪊
         // ełȂꍇ͂ǂ悤Ȃ
         if (methodOwnerClassType instanceof UnknownTypeInfo) {
 
             // Ȃs
             usingMethod.addUnresolvedUsage(methodCall);
 
             // ς݃LbVɓo^
             resolvedCache.put(methodCall, UnknownTypeInfo.getInstance());
 
             return UnknownTypeInfo.getInstance();
 
             // eΏۃNX(TargetClassInfo)ꍇ
         } else if (methodOwnerClassType instanceof TargetClassInfo) {
 
             // ܂͗p\ȃ\bh猟
             {
                 // p\ȃ\bhꗗ擾
                 final List<TargetMethodInfo> availableMethods = NameResolver.getAvailableMethods(
                         (TargetClassInfo) methodOwnerClassType, usingClass);
 
                 // p\ȃ\bhC\bhƈv̂
                 // \bhČ^̃XgpāC̃\bȟĂяoł邩ǂ𔻒
                 for (TargetMethodInfo availableMethod : availableMethods) {
 
                     // Ăяo\ȃ\bhꍇ
                     if (availableMethod.canCalledWith(methodName, parameterTypes)) {
                         usingMethod.addCallee(availableMethod);
                         availableMethod.addCaller(usingMethod);
 
                         // ς݃LbVɂɓo^
                         resolvedCache.put(methodCall, availableMethod.getReturnType());
 
                         return availableMethod.getReturnType();
                     }
                 }
             }
 
             // p\ȃ\bhȂꍇ́CONXłeNX͂D
             // ̃NX̃\bhgpĂƂ݂Ȃ
             {
                 final ExternalClassInfo externalSuperClass = NameResolver
                         .getExternalSuperClass((TargetClassInfo) methodOwnerClassType);
                 if (null != externalSuperClass) {
 
                     final ExternalMethodInfo methodInfo = new ExternalMethodInfo(methodName,
                             externalSuperClass, constructor);
                     final List<ParameterInfo> parameters = NameResolver
                             .createParameters(parameterTypes);
                     methodInfo.addParameters(parameters);
 
                     usingMethod.addCallee(methodInfo);
                     methodInfo.addCaller(usingMethod);
                     methodInfoManager.add(methodInfo);
 
                     // ς݃LbVɓo^
                     resolvedCache.put(methodCall, methodInfo.getReturnType());
 
                     // ONXɐVKŊOϐiExternalFieldInfojǉ̂Ō^͕s
                     return methodInfo.getReturnType();
                 }
 
                 assert false : "Here shouldn't be reached!";
             }
 
             // Ȃs
             {
                 err.println("Can't resolve method Call : " + methodCall.getMethodName());
 
                 usingMethod.addUnresolvedUsage(methodCall);
 
                 // ς݃LbVɓo^
                 resolvedCache.put(methodCall, UnknownTypeInfo.getInstance());
 
                 return UnknownTypeInfo.getInstance();
             }
 
             // eONXiExternalClassInfojꍇ
         } else if (methodOwnerClassType instanceof ExternalClassInfo) {
 
             final ExternalMethodInfo methodInfo = new ExternalMethodInfo(methodName,
                     (ExternalClassInfo) methodOwnerClassType, constructor);
             final List<ParameterInfo> parameters = NameResolver.createParameters(parameterTypes);
             methodInfo.addParameters(parameters);
 
             usingMethod.addCallee(methodInfo);
             methodInfo.addCaller(usingMethod);
             methodInfoManager.add(methodInfo);
 
             // ς݃LbVɓo^
             resolvedCache.put(methodCall, methodInfo.getReturnType());
 
             // ONXɐVKŊO\bh(ExternalMethodInfo)ǉ̂Ō^͕sD
             return methodInfo.getReturnType();
 
             // ez񂾂ꍇ
         } else if (methodOwnerClassType instanceof ArrayTypeInfo) {
 
             // Java ł΁C java.lang.Object ɑ΂Ăяo
             if (Settings.getLanguage().equals(LANGUAGE.JAVA)) {
                 final ClassInfo ownerClass = classInfoManager.getClassInfo(new String[] { "java",
                         "lang", "Object" });
                 final ExternalMethodInfo methodInfo = new ExternalMethodInfo(methodName,
                         (ExternalClassInfo) ownerClass, false);
                 final List<ParameterInfo> parameters = NameResolver
                         .createParameters(parameterTypes);
                 methodInfo.addParameters(parameters);
 
                 usingMethod.addCallee(methodInfo);
                 methodInfo.addCaller(usingMethod);
                 methodInfoManager.add(methodInfo);
 
                 // ς݃LbVɓo^
                 resolvedCache.put(methodCall, methodInfo.getReturnType());
 
                 // ONXɐVKŊO\bhǉ̂Ō^͕s
                 return methodInfo.getReturnType();
             }
 
             // ev~eBu^ꍇ
         } else if (methodOwnerClassType instanceof PrimitiveTypeInfo) {
 
             switch (Settings.getLanguage()) {
             // Java ̏ꍇ̓I[g{NVOł̃\bhĂяo\
             // TODO Iɂ͂ switch͂ƂDȂȂ TypeConverter.getTypeConverter(LANGUAGE)邩D
             case JAVA:
 
                 final ExternalClassInfo wrapperClass = TypeConverter.getTypeConverter(
                         Settings.getLanguage()).getWrapperClass(
                         (PrimitiveTypeInfo) methodOwnerClassType);
                 final ExternalMethodInfo methodInfo = new ExternalMethodInfo(methodName,
                         wrapperClass, constructor);
                 final List<ParameterInfo> parameters = NameResolver
                         .createParameters(parameterTypes);
                 methodInfo.addParameters(parameters);
 
                 usingMethod.addCallee(methodInfo);
                 methodInfo.addCaller(usingMethod);
                 methodInfoManager.add(methodInfo);
 
                 // ς݃LbVɓo^
                 resolvedCache.put(methodCall, methodInfo.getReturnType());
 
                 // ONXɐVKŊO\bh(ExternalMethodInfo)ǉ̂Ō^͕sD
                 return methodInfo.getReturnType();
 
             default:
                 assert false : "Here shouldn't be reached!";
                 return UnknownTypeInfo.getInstance();
             }
         }
 
         assert false : "Here shouldn't be reached!";
         return UnknownTypeInfo.getInstance();
     }
 
     /**
      * z^tB[h̗vfgpCz^tB[h̗vfgpsĂ郁\bhɓo^D܂CtB[ȟ^ԂD
      * 
      * @param arrayElement z^tB[h̗vfgp
      * @param usingClass tB[hsĂNX
      * @param usingMethod tB[hsĂ郁\bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @param resolvedCache ςUnresolvedTypeInfõLbV
      * @return ς݃tB[ȟ^i܂CtB[ȟ^j
      */
     public static TypeInfo resolveArrayElementUsage(final UnresolvedArrayElementUsage arrayElement,
             final TargetClassInfo usingClass, final TargetMethodInfo usingMethod,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager,
             final Map<UnresolvedTypeInfo, TypeInfo> resolvedCache) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == arrayElement) || (null == usingClass) || (null == usingMethod)
                 || (null == classInfoManager) || (null == fieldInfoManager)
                 || (null == methodInfoManager) || (null == resolvedCache)) {
             throw new NullPointerException();
         }
 
         // ɉς݂ł΁C^擾
         if (resolvedCache.containsKey(arrayElement)) {
             final TypeInfo type = resolvedCache.get(arrayElement);
             return type;
         }
 
         // vfgpĂ関`^擾
         final UnresolvedTypeInfo unresolvedOwnerType = arrayElement.getOwnerArrayType();
         TypeInfo ownerArrayType = NameResolver.resolveTypeInfo(unresolvedOwnerType, usingClass,
                 usingMethod, classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
         assert ownerArrayType != null : "resolveTypeInfo returned null!";
 
         // ^̖OłȂꍇ
         if (ownerArrayType instanceof UnknownTypeInfo) {
 
             // ^z^łꍇ́C^쐬
             if (unresolvedOwnerType instanceof UnresolvedArrayTypeInfo) {
                 final UnresolvedTypeInfo unresolvedElementType = ((UnresolvedArrayTypeInfo) unresolvedOwnerType)
                         .getElementType();
                 final int dimension = ((UnresolvedArrayTypeInfo) unresolvedOwnerType)
                         .getDimension();
                 final TypeInfo elementType = NameResolver
                         .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedElementType);
                 classInfoManager.add((ExternalClassInfo) elementType);
                 ownerArrayType = ArrayTypeInfo.getType(elementType, dimension);
 
                 // z^ȊȌꍇ͂ǂ悤Ȃ
             } else {
 
                 usingMethod.addUnresolvedUsage(arrayElement);
                 resolvedCache.put(arrayElement, UnknownTypeInfo.getInstance());
                 return UnknownTypeInfo.getInstance();
             }
         }
 
         // z̎ɉČ^𐶐
         final int ownerArrayDimension = ((ArrayTypeInfo) ownerArrayType).getDimension();
         final TypeInfo ownerElementType = ((ArrayTypeInfo) ownerArrayType).getElementType();
 
         // z񂪓񎟌ȏ̏ꍇ́CƂzԂ
         if (1 < ownerArrayDimension) {
 
             final TypeInfo type = ArrayTypeInfo.getType(ownerElementType, ownerArrayDimension - 1);
             resolvedCache.put(arrayElement, type);
             return type;
 
             // z񂪈ꎟ̏ꍇ́Cvf̌^Ԃ
         } else {
 
             resolvedCache.put(arrayElement, ownerElementType);
             return ownerElementType;
         }
     }
 
     /**
      * GeBeBgpCGeBeBgpsĂ郁\bhɓo^D܂CGeBeB̉ς݌^ԂD
      * 
      * @param entityUsage GeBeBgp
      * @param usingClass \bhĂяosĂNX
      * @param usingMethod \bhĂяosĂ郁\bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @param resolvedCache ςUnresolvedTypeInfõLbV
      * @return \bhĂяoɑΉ MethodInfo
      */
     public static TypeInfo resolveEntityUsage(final UnresolvedEntityUsage entityUsage,
             final TargetClassInfo usingClass, final TargetMethodInfo usingMethod,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager,
             final Map<UnresolvedTypeInfo, TypeInfo> resolvedCache) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == entityUsage) || (null == usingClass) || (null == usingMethod)
                 || (null == classInfoManager) || (null == methodInfoManager)
                 || (null == resolvedCache)) {
             throw new NullPointerException();
         }
 
         // ɉς݂ł΁C^擾
         if (resolvedCache.containsKey(entityUsage)) {
             final TypeInfo type = resolvedCache.get(entityUsage);
             assert null != type : "resolveEntityUsage returned null!";
             return type;
         }
 
         // GeBeBQƖ擾
         final String[] name = entityUsage.getName();
 
         // p\ȃCX^XtB[hGeBeB
         {
             // ̃NXŗp\ȃCX^XtB[hꗗ擾
             final List<TargetFieldInfo> availableFieldsOfThisClass = Members
                     .<TargetFieldInfo> getInstanceMembers(NameResolver
                             .getAvailableFields(usingClass));
 
             for (TargetFieldInfo availableFieldOfThisClass : availableFieldsOfThisClass) {
 
                 // vtB[hꍇ
                 if (name[0].equals(availableFieldOfThisClass.getName())) {
                     usingMethod.addReferencee(availableFieldOfThisClass);
                     availableFieldOfThisClass.addReferencer(usingMethod);
 
                     // availableField.getType() 玟word(name[i])𖼑O
                     TypeInfo ownerTypeInfo = availableFieldOfThisClass.getType();
                     for (int i = 1; i < name.length; i++) {
 
                         // e UnknownTypeInfo Cǂ悤Ȃ
                         if (ownerTypeInfo instanceof UnknownTypeInfo) {
 
                             // ς݃LbVɓo^
                             resolvedCache.put(entityUsage, UnknownTypeInfo.getInstance());
 
                             return UnknownTypeInfo.getInstance();
 
                             // eΏۃNX(TargetClassInfo)̏ꍇ
                         } else if (ownerTypeInfo instanceof TargetClassInfo) {
 
                             // ܂͗p\ȃtB[hꗗ擾
                             boolean found = false;
                             {
                                 // p\ȃCX^XtB[hꗗ擾
                                 final List<TargetFieldInfo> availableFields = Members
                                         .getInstanceMembers(NameResolver.getAvailableFields(
                                                 (TargetClassInfo) ownerTypeInfo, usingClass));
 
                                 for (TargetFieldInfo availableField : availableFields) {
 
                                     // vtB[hꍇ
                                     if (name[i].equals(availableField.getName())) {
                                         usingMethod.addReferencee(availableField);
                                         availableField.addReferencer(usingMethod);
 
                                         ownerTypeInfo = availableField.getType();
                                         found = true;
                                         break;
                                     }
                                 }
                             }
 
                             // p\ȃtB[hȂꍇ́CONXłeNX͂D
                             // ̃NX̃tB[hgpĂƂ݂Ȃ
                             {
                                 if (!found) {
 
                                     final ExternalClassInfo externalSuperClass = NameResolver
                                             .getExternalSuperClass((TargetClassInfo) ownerTypeInfo);
                                     if (!(ownerTypeInfo instanceof TargetInnerClassInfo)
                                             && (null != externalSuperClass)) {
 
                                         final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                 name[i], externalSuperClass);
 
                                         usingMethod.addReferencee(fieldInfo);
                                         fieldInfo.addReferencer(usingMethod);
                                         fieldInfoManager.add(fieldInfo);
 
                                         ownerTypeInfo = fieldInfo.getType();
 
                                     } else {
                                         err.println("Can't resolve entity usage1 : "
                                                 + entityUsage.getTypeName());
                                     }
                                 }
                             }
 
                             // eONX(ExternalClassInfo)̏ꍇ
                         } else if (ownerTypeInfo instanceof ExternalClassInfo) {
 
                             final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(name[i],
                                     (ExternalClassInfo) ownerTypeInfo);
 
                             usingMethod.addReferencee(fieldInfo);
                             fieldInfo.addReferencer(usingMethod);
                             fieldInfoManager.add(fieldInfo);
 
                             ownerTypeInfo = fieldInfo.getType();
 
                         } else {
                             err.println("here shouldn't be reached!");
                             assert false;
                         }
                     }
 
                     // ς݃LbVɓo^
                     resolvedCache.put(entityUsage, ownerTypeInfo);
                     assert null != ownerTypeInfo : "resolveEntityUsage returned null!";
                     return ownerTypeInfo;
                 }
             }
         }
 
         // p\ȃX^eBbNtB[hGeBeB
         {
             // ̃NXŗp\ȃX^eBbNtB[hꗗ擾
             final List<TargetFieldInfo> availableFieldsOfThisClass = Members
                     .<TargetFieldInfo> getStaticMembers(NameResolver.getAvailableFields(usingClass));
 
             for (TargetFieldInfo availableFieldOfThisClass : availableFieldsOfThisClass) {
 
                 // vtB[hꍇ
                 if (name[0].equals(availableFieldOfThisClass.getName())) {
                     usingMethod.addReferencee(availableFieldOfThisClass);
                     availableFieldOfThisClass.addReferencer(usingMethod);
 
                     // availableField.getType() 玟word(name[i])𖼑O
                     TypeInfo ownerTypeInfo = availableFieldOfThisClass.getType();
                     for (int i = 1; i < name.length; i++) {
 
                         // e UnknownTypeInfo Cǂ悤Ȃ
                         if (ownerTypeInfo instanceof UnknownTypeInfo) {
 
                             // ς݃LbVɓo^
                             resolvedCache.put(entityUsage, UnknownTypeInfo.getInstance());
 
                             return UnknownTypeInfo.getInstance();
 
                             // eΏۃNX(TargetClassInfo)̏ꍇ
                         } else if (ownerTypeInfo instanceof TargetClassInfo) {
 
                             // ܂͗p\ȃtB[hꗗ擾
                             boolean found = false;
                             {
                                 // p\ȃX^eBbNtB[hꗗ擾
                                 final List<TargetFieldInfo> availableFields = Members
                                         .getStaticMembers(NameResolver.getAvailableFields(
                                                 (TargetClassInfo) ownerTypeInfo, usingClass));
 
                                 for (TargetFieldInfo availableField : availableFields) {
 
                                     // vtB[hꍇ
                                     if (name[i].equals(availableField.getName())) {
                                         usingMethod.addReferencee(availableField);
                                         availableField.addReferencer(usingMethod);
 
                                         ownerTypeInfo = availableField.getType();
                                         found = true;
                                         break;
                                     }
                                 }
                             }
 
                             // X^eBbNtB[hŌȂꍇ́CCi[NXT
                             {
                                 if (!found) {
                                     // Ci[NXꗗ擾
                                     final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                             .getAvailableDirectInnerClasses((TargetClassInfo) ownerTypeInfo);
                                     for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                         // vNXꍇ
                                         if (name[i].equals(innerClass.getClassName())) {
                                             // TODO p֌W\zR[hKvH
 
                                             ownerTypeInfo = innerClass;
                                             found = true;
                                             break;
                                         }
                                     }
                                 }
                             }
 
                             // p\ȃtB[hȂꍇ́CONXłeNX͂D
                             // ̃NX̃tB[hgpĂƂ݂Ȃ
                             {
                                 if (!found) {
 
                                     final ExternalClassInfo externalSuperClass = NameResolver
                                             .getExternalSuperClass((TargetClassInfo) ownerTypeInfo);
                                     if (!(ownerTypeInfo instanceof TargetInnerClassInfo)
                                             && (null != externalSuperClass)) {
 
                                         final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                 name[i], externalSuperClass);
 
                                         usingMethod.addReferencee(fieldInfo);
                                         fieldInfo.addReferencer(usingMethod);
                                         fieldInfoManager.add(fieldInfo);
 
                                         ownerTypeInfo = fieldInfo.getType();
 
                                     } else {
                                         err.println("Can't resolve entity usage2 : "
                                                 + entityUsage.getTypeName());
                                     }
                                 }
                             }
 
                             // eONX(ExternalClassInfo)̏ꍇ
                         } else if (ownerTypeInfo instanceof ExternalClassInfo) {
 
                             final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(name[i],
                                     (ExternalClassInfo) ownerTypeInfo);
 
                             usingMethod.addReferencee(fieldInfo);
                             fieldInfo.addReferencer(usingMethod);
                             fieldInfoManager.add(fieldInfo);
 
                             ownerTypeInfo = fieldInfo.getType();
 
                         } else {
                             assert false : "Here shouldn't be reached!";
                         }
                     }
 
                     // ς݃LbVɓo^
                     resolvedCache.put(entityUsage, ownerTypeInfo);
                     assert null != ownerTypeInfo : "resolveEntityUsage returned null!";
                     return ownerTypeInfo;
                 }
             }
         }
 
         // GeBeBS薼łꍇ
         {
 
             for (int length = 1; length <= name.length; length++) {
 
                 // 閼O(String[])쐬
                 final String[] searchingName = new String[length];
                 System.arraycopy(name, 0, searchingName, 0, length);
 
                 final ClassInfo searchingClass = classInfoManager.getClassInfo(searchingName);
                 if (null != searchingClass) {
 
                     TypeInfo ownerTypeInfo = searchingClass;
                     for (int i = length; i < name.length; i++) {
 
                         // e UnknownTypeInfo Cǂ悤Ȃ
                         if (ownerTypeInfo instanceof UnknownTypeInfo) {
 
                             // ς݃LbVɓo^
                             resolvedCache.put(entityUsage, UnknownTypeInfo.getInstance());
 
                             return UnknownTypeInfo.getInstance();
 
                             // eΏۃNX(TargetClassInfo)̏ꍇ
                         } else if (ownerTypeInfo instanceof TargetClassInfo) {
 
                             // ܂͗p\ȃtB[hꗗ擾
                             boolean found = false;
                             {
                                 // p\ȃtB[hꗗ擾
                                 final List<TargetFieldInfo> availableFields = Members
                                         .getStaticMembers(NameResolver.getAvailableFields(
                                                 (TargetClassInfo) ownerTypeInfo, usingClass));
 
                                 for (TargetFieldInfo availableField : availableFields) {
 
                                     // vtB[hꍇ
                                     if (name[i].equals(availableField.getName())) {
                                         usingMethod.addReferencee(availableField);
                                         availableField.addReferencer(usingMethod);
 
                                         ownerTypeInfo = availableField.getType();
                                         found = true;
                                         break;
                                     }
                                 }
                             }
 
                             // X^eBbNtB[hŌȂꍇ́CCi[NXT
                             {
                                 if (!found) {
                                     // Ci[NXꗗ擾
                                     final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                             .getAvailableDirectInnerClasses((TargetClassInfo) ownerTypeInfo);
                                     for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                         // vNXꍇ
                                         if (name[i].equals(innerClass.getClassName())) {
                                             // TODO p֌W\zR[hKvH
 
                                             ownerTypeInfo = innerClass;
                                             found = true;
                                             break;
                                         }
                                     }
                                 }
                             }
 
                             // p\ȃtB[hȂꍇ́CONXłeNX͂D
                             // ̃NX̃tB[hgpĂƂ݂Ȃ
                             {
                                 if (!found) {
 
                                     final ExternalClassInfo externalSuperClass = NameResolver
                                             .getExternalSuperClass((TargetClassInfo) ownerTypeInfo);
                                     if (!(ownerTypeInfo instanceof TargetInnerClassInfo)
                                             && (null != externalSuperClass)) {
 
                                         final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                 name[i], externalSuperClass);
 
                                         usingMethod.addReferencee(fieldInfo);
                                         fieldInfo.addReferencer(usingMethod);
                                         fieldInfoManager.add(fieldInfo);
 
                                         ownerTypeInfo = fieldInfo.getType();
 
                                     } else {
                                         err.println("Can't resolve entity usage3 : "
                                                 + entityUsage.getTypeName());
                                     }
                                 }
                             }
 
                             // eONX(ExternalClassInfo)̏ꍇ
                         } else if (ownerTypeInfo instanceof ExternalClassInfo) {
 
                             final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(name[i],
                                     (ExternalClassInfo) ownerTypeInfo);
 
                             usingMethod.addReferencee(fieldInfo);
                             fieldInfo.addReferencer(usingMethod);
                             fieldInfoManager.add(fieldInfo);
 
                             ownerTypeInfo = fieldInfo.getType();
 
                         } else {
                             assert false : "Here shouldn't be reached!";
                         }
                     }
 
                     // ς݃LbVɓo^
                     resolvedCache.put(entityUsage, ownerTypeInfo);
                     assert null != ownerTypeInfo : "resolveEntityUsage returned null!";
                     return ownerTypeInfo;
                 }
             }
         }
 
         // p\ȃNXGeBeB
         {
 
             // NX猟
             {
                 final TargetClassInfo outestClass;
                 if (usingClass instanceof TargetInnerClassInfo) {
                     outestClass = NameResolver.getOuterstClass((TargetInnerClassInfo) usingClass);
                 } else {
                     outestClass = usingClass;
                 }
 
                 for (final TargetInnerClassInfo innerClassInfo : NameResolver
                         .getAvailableInnerClasses(outestClass)) {
 
                     // NXƎQƖ̐擪ꍇ́C̃NXQƐłƌ肷
                     final String innerClassName = innerClassInfo.getClassName();
                     if (innerClassName.equals(name[0])) {
 
                         TypeInfo ownerTypeInfo = innerClassInfo;
                         for (int i = 1; i < name.length; i++) {
 
                             // e UnknownTypeInfo Cǂ悤Ȃ
                             if (ownerTypeInfo instanceof UnknownTypeInfo) {
 
                                 // ς݃LbVɓo^
                                 resolvedCache.put(entityUsage, UnknownTypeInfo.getInstance());
 
                                 return UnknownTypeInfo.getInstance();
 
                                 // eΏۃNX(TargetClassInfo)̏ꍇ
                             } else if (ownerTypeInfo instanceof TargetClassInfo) {
 
                                 // ܂͗p\ȃtB[hꗗ擾
                                 boolean found = false;
                                 {
                                     // p\ȃtB[hꗗ擾
                                     final List<TargetFieldInfo> availableFields = NameResolver
                                             .getAvailableFields((TargetClassInfo) ownerTypeInfo,
                                                     usingClass);
 
                                     for (TargetFieldInfo availableField : availableFields) {
 
                                         // vtB[hꍇ
                                         if (name[i].equals(availableField.getName())) {
                                             usingMethod.addReferencee(availableField);
                                             availableField.addReferencer(usingMethod);
 
                                             ownerTypeInfo = availableField.getType();
                                             found = true;
                                             break;
                                         }
                                     }
                                 }
 
                                 // X^eBbNtB[hŌȂꍇ́CCi[NXT
                                 {
                                     if (!found) {
                                         // Ci[NXꗗ擾
                                         final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                                 .getAvailableDirectInnerClasses((TargetClassInfo) ownerTypeInfo);
                                         for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                             // vNXꍇ
                                             if (name[i].equals(innerClass.getClassName())) {
                                                 // TODO p֌W\zR[hKvH
 
                                                 ownerTypeInfo = innerClassInfo;
                                                 found = true;
                                                 break;
                                             }
                                         }
                                     }
                                 }
 
                                 // p\ȃtB[hȂꍇ́CONXłeNX͂D
                                 // ̃NX̃tB[hgpĂƂ݂Ȃ
                                 {
                                     if (!found) {
 
                                         final ExternalClassInfo externalSuperClass = NameResolver
                                                 .getExternalSuperClass((TargetClassInfo) ownerTypeInfo);
                                         if (!(ownerTypeInfo instanceof TargetInnerClassInfo)
                                                 && (null != externalSuperClass)) {
 
                                             final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                     name[i], externalSuperClass);
 
                                             usingMethod.addReferencee(fieldInfo);
                                             fieldInfo.addReferencer(usingMethod);
                                             fieldInfoManager.add(fieldInfo);
 
                                             ownerTypeInfo = fieldInfo.getType();
 
                                         } else {
                                             err.println("Can't resolve entity usage3.5 : "
                                                     + entityUsage.getTypeName());
                                         }
                                     }
                                 }
 
                                 // eONX(ExternalClassInfo)̏ꍇ
                             } else if (ownerTypeInfo instanceof ExternalClassInfo) {
 
                                 final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(name[i],
                                         (ExternalClassInfo) ownerTypeInfo);
 
                                 usingMethod.addReferencee(fieldInfo);
                                 fieldInfo.addReferencer(usingMethod);
                                 fieldInfoManager.add(fieldInfo);
 
                                 ownerTypeInfo = fieldInfo.getType();
 
                             } else {
                                 assert false : "Here should be reached!";
                             }
                         }
 
                         // ς݃LbVɓo^
                         resolvedCache.put(entityUsage, ownerTypeInfo);
                         assert null != ownerTypeInfo : "resolveEntityUsage returned null!";
                         return ownerTypeInfo;
                     }
                 }
             }
 
             // p\ȖOԂ猟
             {
                 for (AvailableNamespaceInfo availableNamespace : entityUsage
                         .getAvailableNamespaces()) {
 
                     // OԖ.* ƂȂĂꍇ
                     if (availableNamespace.isAllClasses()) {
                         final String[] namespace = availableNamespace.getNamespace();
 
                         // OԂ̉ɂeNXɑ΂
                         for (ClassInfo classInfo : classInfoManager.getClassInfos(namespace)) {
                             final String className = classInfo.getClassName();
 
                             // NXƎQƖ̐擪ꍇ́C̃NXQƐłƌ肷
                             if (className.equals(name[0])) {
 
                                 TypeInfo ownerTypeInfo = classInfo;
                                 for (int i = 1; i < name.length; i++) {
 
                                     // e UnknownTypeInfo Cǂ悤Ȃ
                                     if (ownerTypeInfo instanceof UnknownTypeInfo) {
 
                                         // ς݃LbVɓo^
                                         resolvedCache.put(entityUsage, UnknownTypeInfo
                                                 .getInstance());
 
                                         return UnknownTypeInfo.getInstance();
 
                                         // eΏۃNX(TargetClassInfo)̏ꍇ
                                     } else if (ownerTypeInfo instanceof TargetClassInfo) {
 
                                         // ܂͗p\ȃtB[hꗗ擾
                                         boolean found = false;
                                         {
                                             // p\ȃtB[hꗗ擾
                                             final List<TargetFieldInfo> availableFields = NameResolver
                                                     .getAvailableFields(
                                                             (TargetClassInfo) ownerTypeInfo,
                                                             usingClass);
 
                                             for (TargetFieldInfo availableField : availableFields) {
 
                                                 // vtB[hꍇ
                                                 if (name[i].equals(availableField.getName())) {
                                                     usingMethod.addReferencee(availableField);
                                                     availableField.addReferencer(usingMethod);
 
                                                     ownerTypeInfo = availableField.getType();
                                                     found = true;
                                                     break;
                                                 }
                                             }
                                         }
 
                                         // X^eBbNtB[hŌȂꍇ́CCi[NXT
                                         {
                                             if (!found) {
                                                 // Ci[NXꗗ擾
                                                 final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                                         .getAvailableDirectInnerClasses((TargetClassInfo) ownerTypeInfo);
                                                 for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                                     // vNXꍇ
                                                     if (name[i].equals(innerClass.getClassName())) {
                                                         // TODO p֌W\zR[hKvH
 
                                                         ownerTypeInfo = innerClass;
                                                         found = true;
                                                         break;
                                                     }
                                                 }
                                             }
                                         }
 
                                         // p\ȃtB[hȂꍇ́CONXłeNX͂D
                                         // ̃NX̃tB[hgpĂƂ݂Ȃ
                                         {
                                             if (!found) {
 
                                                 final ExternalClassInfo externalSuperClass = NameResolver
                                                         .getExternalSuperClass((TargetClassInfo) ownerTypeInfo);
                                                 if (!(ownerTypeInfo instanceof TargetInnerClassInfo)
                                                         && (null != externalSuperClass)) {
 
                                                     final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                             name[i], externalSuperClass);
 
                                                     usingMethod.addReferencee(fieldInfo);
                                                     fieldInfo.addReferencer(usingMethod);
                                                     fieldInfoManager.add(fieldInfo);
 
                                                     ownerTypeInfo = fieldInfo.getType();
 
                                                 } else {
                                                     err.println("Can't resolve entity usage4 : "
                                                             + entityUsage.getTypeName());
                                                 }
                                             }
                                         }
 
                                         // eONX(ExternalClassInfo)̏ꍇ
                                     } else if (ownerTypeInfo instanceof ExternalClassInfo) {
 
                                         final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                 name[i], (ExternalClassInfo) ownerTypeInfo);
 
                                         usingMethod.addReferencee(fieldInfo);
                                         fieldInfo.addReferencer(usingMethod);
                                         fieldInfoManager.add(fieldInfo);
 
                                         ownerTypeInfo = fieldInfo.getType();
 
                                     } else {
                                         assert false : "Here should be reached!";
                                     }
                                 }
 
                                 // ς݃LbVɓo^
                                 resolvedCache.put(entityUsage, ownerTypeInfo);
                                 assert null != ownerTypeInfo : "resolveEntityUsage returned null!";
                                 return ownerTypeInfo;
                             }
                         }
 
                         // O.NX ƂȂĂꍇ
                     } else {
 
                         final String[] importName = availableNamespace.getImportName();
 
                         // NXƎQƖ̐擪ꍇ́C̃NXQƐłƌ肷
                         if (importName[importName.length - 1].equals(name[0])) {
 
                             ClassInfo specifiedClassInfo = classInfoManager
                                     .getClassInfo(importName);
                             if (null == specifiedClassInfo) {
                                 specifiedClassInfo = new ExternalClassInfo(importName);
                                 classInfoManager.add((ExternalClassInfo) specifiedClassInfo);
                             }
 
                             TypeInfo ownerTypeInfo = specifiedClassInfo;
                             for (int i = 1; i < name.length; i++) {
 
                                 // e UnknownTypeInfo Cǂ悤Ȃ
                                 if (ownerTypeInfo instanceof UnknownTypeInfo) {
 
                                     // ς݃LbVɓo^
                                     resolvedCache.put(entityUsage, UnknownTypeInfo.getInstance());
 
                                     return UnknownTypeInfo.getInstance();
 
                                     // eΏۃNX(TargetClassInfo)̏ꍇ
                                 } else if (ownerTypeInfo instanceof TargetClassInfo) {
 
                                     // ܂͗p\ȃtB[hꗗ擾
                                     boolean found = false;
                                     {
                                         // p\ȃtB[hꗗ擾
                                         final List<TargetFieldInfo> availableFields = NameResolver
                                                 .getAvailableFields(
                                                         (TargetClassInfo) ownerTypeInfo, usingClass);
 
                                         for (TargetFieldInfo availableField : availableFields) {
 
                                             // vtB[hꍇ
                                             if (name[i].equals(availableField.getName())) {
                                                 usingMethod.addReferencee(availableField);
                                                 availableField.addReferencer(usingMethod);
 
                                                 ownerTypeInfo = availableField.getType();
                                                 found = true;
                                                 break;
                                             }
                                         }
                                     }
 
                                     // X^eBbNtB[hŌȂꍇ́CCi[NXT
                                     {
                                         if (!found) {
                                             // Ci[NXꗗ擾
                                             final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                                     .getAvailableDirectInnerClasses((TargetClassInfo) ownerTypeInfo);
                                             for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                                 // vNXꍇ
                                                 if (name[i].equals(innerClass.getClassName())) {
                                                     // TODO p֌W\zR[hKvH
 
                                                     ownerTypeInfo = innerClass;
                                                     found = true;
                                                     break;
                                                 }
                                             }
                                         }
                                     }
 
                                     // p\ȃtB[hȂꍇ́CONXłeNX͂D
                                     // ̃NX̃tB[hgpĂƂ݂Ȃ
                                     {
                                         if (!found) {
 
                                             final ExternalClassInfo externalSuperClass = NameResolver
                                                     .getExternalSuperClass((TargetClassInfo) ownerTypeInfo);
                                             if (!(ownerTypeInfo instanceof TargetInnerClassInfo)
                                                     && (null != externalSuperClass)) {
 
                                                 final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                         name[i], externalSuperClass);
 
                                                 usingMethod.addReferencee(fieldInfo);
                                                 fieldInfo.addReferencer(usingMethod);
                                                 fieldInfoManager.add(fieldInfo);
 
                                                 ownerTypeInfo = fieldInfo.getType();
 
                                             } else {
                                                 err.println("Can't resolve entity usage5 : "
                                                         + entityUsage.getTypeName());
                                             }
                                         }
                                     }
 
                                     // eONX(ExternalClassInfo)̏ꍇ
                                 } else if (ownerTypeInfo instanceof ExternalClassInfo) {
 
                                     final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                             name[i], (ExternalClassInfo) ownerTypeInfo);
 
                                     usingMethod.addReferencee(fieldInfo);
                                     fieldInfo.addReferencer(usingMethod);
                                     fieldInfoManager.add(fieldInfo);
 
                                     ownerTypeInfo = fieldInfo.getType();
 
                                 } else {
                                     assert false : "Here shouldn't be reached!";
                                 }
                             }
 
                             // ς݃LbVɓo^
                             resolvedCache.put(entityUsage, ownerTypeInfo);
                             assert null != ownerTypeInfo : "resolveEntityUsage returned null!";
                             return ownerTypeInfo;
                         }
                     }
                 }
             }
         }
 
         err.println("Remain unresolved \"" + entityUsage.getTypeName() + "\"" + " on \""
                 + usingClass.getFullQualifiedName(LANGUAGE.JAVA.getNamespaceDelimiter()) + "#"
                 + usingMethod.getMethodName() + "\".");
 
         // Ȃs
         usingMethod.addUnresolvedUsage(entityUsage);
 
         // ς݃LbVɓo^
         resolvedCache.put(entityUsage, UnknownTypeInfo.getInstance());
 
         return UnknownTypeInfo.getInstance();
     }
 
     /**
      * 񍀉ZČ^ԂD
      * 
      * @param binominalOperation 񍀉Z
      * @param usingClass 񍀉ZsĂNX
      * @param usingMethod 񍀉ZsĂ郁\bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @param resolvedCache ςUnresolvedTypeInfõLbV
      * @return ςݓ񍀉Ž^i܂CZʂ̌^j
      */
     public static TypeInfo resolveBinomialOperation(
             final UnresolvedBinominalOperation binominalOperation,
             final TargetClassInfo usingClass, final TargetMethodInfo usingMethod,
             final ClassInfoManager classInfoManager, final FieldInfoManager fieldInfoManager,
             final MethodInfoManager methodInfoManager,
             final Map<UnresolvedTypeInfo, TypeInfo> resolvedCache) {
 
         final OPERATOR operator = binominalOperation.getOperator();
         final UnresolvedTypeInfo unresolvedFirstOperandType = binominalOperation.getFirstOperand();
         final UnresolvedTypeInfo unresolvedSecondOperandType = binominalOperation
                 .getSecondOperand();
         final TypeInfo firstOperandType = NameResolver.resolveTypeInfo(unresolvedFirstOperandType,
                 usingClass, usingMethod, classInfoManager, fieldInfoManager, methodInfoManager,
                 resolvedCache);
         final TypeInfo secondOperandType = NameResolver.resolveTypeInfo(
                 unresolvedSecondOperandType, usingClass, usingMethod, classInfoManager,
                 fieldInfoManager, methodInfoManager, resolvedCache);
 
         final ExternalClassInfo DOUBLE = TypeConverter.getTypeConverter(Settings.getLanguage())
                 .getWrapperClass(PrimitiveTypeInfo.DOUBLE);
         final ExternalClassInfo FLOAT = TypeConverter.getTypeConverter(Settings.getLanguage())
                 .getWrapperClass(PrimitiveTypeInfo.FLOAT);
         final ExternalClassInfo LONG = TypeConverter.getTypeConverter(Settings.getLanguage())
                 .getWrapperClass(PrimitiveTypeInfo.LONG);
         final ExternalClassInfo INTEGER = TypeConverter.getTypeConverter(Settings.getLanguage())
                 .getWrapperClass(PrimitiveTypeInfo.INT);
         final ExternalClassInfo SHORT = TypeConverter.getTypeConverter(Settings.getLanguage())
                 .getWrapperClass(PrimitiveTypeInfo.SHORT);
         final ExternalClassInfo CHARACTER = TypeConverter.getTypeConverter(Settings.getLanguage())
                 .getWrapperClass(PrimitiveTypeInfo.CHAR);
         final ExternalClassInfo BYTE = TypeConverter.getTypeConverter(Settings.getLanguage())
                 .getWrapperClass(PrimitiveTypeInfo.BYTE);
         final ExternalClassInfo BOOLEAN = TypeConverter.getTypeConverter(Settings.getLanguage())
                 .getWrapperClass(PrimitiveTypeInfo.BOOLEAN);
 
         switch (Settings.getLanguage()) {
         case JAVA:
 
             final ExternalClassInfo STRING = (ExternalClassInfo) classInfoManager
                     .getClassInfo(new String[] { "java", "lang", "String" });
 
             switch (operator) {
             case ARITHMETIC:
 
                 if ((firstOperandType.equals(STRING) || (secondOperandType.equals(STRING)))) {
                     resolvedCache.put(binominalOperation, STRING);
                     return STRING;
 
                 } else if (firstOperandType.equals(DOUBLE)
                         || firstOperandType.equals(PrimitiveTypeInfo.DOUBLE)
                         || secondOperandType.equals(DOUBLE)
                         || secondOperandType.equals(PrimitiveTypeInfo.DOUBLE)) {
                     resolvedCache.put(binominalOperation, PrimitiveTypeInfo.DOUBLE);
                     return PrimitiveTypeInfo.DOUBLE;
 
                 } else if (firstOperandType.equals(FLOAT)
                         || firstOperandType.equals(PrimitiveTypeInfo.FLOAT)
                         || secondOperandType.equals(FLOAT)
                         || secondOperandType.equals(PrimitiveTypeInfo.FLOAT)) {
                     resolvedCache.put(binominalOperation, PrimitiveTypeInfo.FLOAT);
                     return PrimitiveTypeInfo.FLOAT;
 
                 } else if (firstOperandType.equals(LONG)
                         || firstOperandType.equals(PrimitiveTypeInfo.LONG)
                         || secondOperandType.equals(LONG)
                         || secondOperandType.equals(PrimitiveTypeInfo.LONG)) {
                     resolvedCache.put(binominalOperation, PrimitiveTypeInfo.LONG);
                     return PrimitiveTypeInfo.LONG;
 
                 } else if (firstOperandType.equals(INTEGER)
                         || firstOperandType.equals(PrimitiveTypeInfo.INT)
                         || secondOperandType.equals(INTEGER)
                         || secondOperandType.equals(PrimitiveTypeInfo.INT)) {
                     resolvedCache.put(binominalOperation, PrimitiveTypeInfo.INT);
                     return PrimitiveTypeInfo.INT;
 
                 } else if (firstOperandType.equals(SHORT)
                         || firstOperandType.equals(PrimitiveTypeInfo.SHORT)
                         || secondOperandType.equals(SHORT)
                         || secondOperandType.equals(PrimitiveTypeInfo.SHORT)) {
                     resolvedCache.put(binominalOperation, PrimitiveTypeInfo.SHORT);
                     return PrimitiveTypeInfo.SHORT;
 
                 } else if (firstOperandType.equals(CHARACTER)
                         || firstOperandType.equals(PrimitiveTypeInfo.CHAR)
                         || secondOperandType.equals(CHARACTER)
                         || secondOperandType.equals(PrimitiveTypeInfo.CHAR)) {
                     resolvedCache.put(binominalOperation, PrimitiveTypeInfo.CHAR);
                     return PrimitiveTypeInfo.CHAR;
 
                 } else if (firstOperandType.equals(BYTE)
                         || firstOperandType.equals(PrimitiveTypeInfo.BYTE)
                         || secondOperandType.equals(BYTE)
                         || secondOperandType.equals(PrimitiveTypeInfo.BYTE)) {
                     resolvedCache.put(binominalOperation, PrimitiveTypeInfo.BYTE);
                     return PrimitiveTypeInfo.BYTE;
 
                 } else if ((firstOperandType instanceof UnknownTypeInfo)
                         || (secondOperandType instanceof UnknownTypeInfo)) {
 
                     resolvedCache.put(binominalOperation, UnknownTypeInfo.getInstance());
                     return UnknownTypeInfo.getInstance();
 
                 } else {
                     assert false : "Here shouldn't be reached!";
                 }
 
                 break;
 
             case COMPARATIVE:
                 resolvedCache.put(binominalOperation, PrimitiveTypeInfo.BOOLEAN);
                 return PrimitiveTypeInfo.BOOLEAN;
             case LOGICAL:
                 resolvedCache.put(binominalOperation, PrimitiveTypeInfo.BOOLEAN);
                 return PrimitiveTypeInfo.BOOLEAN;
             case BITS:
 
                 if (firstOperandType.equals(LONG)
                         || firstOperandType.equals(PrimitiveTypeInfo.LONG)
                         || secondOperandType.equals(LONG)
                         || secondOperandType.equals(PrimitiveTypeInfo.LONG)) {
                     resolvedCache.put(binominalOperation, PrimitiveTypeInfo.LONG);
                     return PrimitiveTypeInfo.LONG;
 
                 } else if (firstOperandType.equals(INTEGER)
                         || firstOperandType.equals(PrimitiveTypeInfo.INT)
                         || secondOperandType.equals(INTEGER)
                         || secondOperandType.equals(PrimitiveTypeInfo.INT)) {
                     resolvedCache.put(binominalOperation, PrimitiveTypeInfo.INT);
                     return PrimitiveTypeInfo.INT;
 
                 } else if (firstOperandType.equals(SHORT)
                         || firstOperandType.equals(PrimitiveTypeInfo.SHORT)
                         || secondOperandType.equals(SHORT)
                         || secondOperandType.equals(PrimitiveTypeInfo.SHORT)) {
                     resolvedCache.put(binominalOperation, PrimitiveTypeInfo.SHORT);
                     return PrimitiveTypeInfo.SHORT;
 
                 } else if (firstOperandType.equals(BYTE)
                         || firstOperandType.equals(PrimitiveTypeInfo.BYTE)
                         || secondOperandType.equals(BYTE)
                         || secondOperandType.equals(PrimitiveTypeInfo.BYTE)) {
                     resolvedCache.put(binominalOperation, PrimitiveTypeInfo.BYTE);
                     return PrimitiveTypeInfo.BYTE;
 
                 } else if (firstOperandType.equals(BOOLEAN)
                         || firstOperandType.equals(PrimitiveTypeInfo.BOOLEAN)
                         || secondOperandType.equals(BOOLEAN)
                         || secondOperandType.equals(PrimitiveTypeInfo.BOOLEAN)) {
                     resolvedCache.put(binominalOperation, PrimitiveTypeInfo.BOOLEAN);
                     return PrimitiveTypeInfo.BOOLEAN;
 
                 } else if ((firstOperandType instanceof UnknownTypeInfo)
                         || (secondOperandType instanceof UnknownTypeInfo)) {
 
                     resolvedCache.put(binominalOperation, UnknownTypeInfo.getInstance());
                     return UnknownTypeInfo.getInstance();
 
                 } else {
                     assert false : "Here shouldn't be reached!";
                 }
 
             case SHIFT:
                 resolvedCache.put(binominalOperation, firstOperandType);
                 return firstOperandType;
             case ASSIGNMENT:
                 resolvedCache.put(binominalOperation, firstOperandType);
                 return firstOperandType;
             default:
                 assert false : "Here shouldn't be reached";
             }
 
             break;
 
         default:
             assert false : "Here shouldn't be reached";
         }
 
         return null;
     }
 
     /**
      * ŗ^ꂽ^\ς݌^NX𐶐D ňƂė^̂́C\[XR[hp[XĂȂ^ł̂ŁCς݌^NX
      * ExternalClassInfo ƂȂD
      * 
      * @param unresolvedReferenceType ^
      * @return ς݌^
      */
     public static ExternalClassInfo createExternalClassInfo(
             final UnresolvedReferenceTypeInfo unresolvedReferenceType) {
 
         if (null == unresolvedReferenceType) {
             throw new NullPointerException();
         }
 
         // NX̎QƖ擾
         final String[] referenceName = unresolvedReferenceType.getReferenceName();
 
         // p\ȖOԂCNX̊S薼
         for (AvailableNamespaceInfo availableNamespace : unresolvedReferenceType
                 .getAvailableNamespaces()) {
 
             // OԖ.* ƂȂĂꍇ́C邱ƂłȂ
             if (availableNamespace.isAllClasses()) {
                 continue;
 
                 // O.NX ƂȂĂꍇ
             } else {
 
                 final String[] importName = availableNamespace.getImportName();
 
                 // NXƎQƖ̐擪ꍇ́C̃NXQƐłƌ肷
                 if (importName[importName.length - 1].equals(referenceName[0])) {
 
                     final String[] namespace = availableNamespace.getNamespace();
                     final String[] fullQualifiedName = new String[namespace.length
                             + referenceName.length];
                     System.arraycopy(namespace, 0, fullQualifiedName, 0, namespace.length);
                     System.arraycopy(referenceName, 0, fullQualifiedName, namespace.length,
                             referenceName.length);
 
                     final ExternalClassInfo classInfo = new ExternalClassInfo(fullQualifiedName);
                     return classInfo;
                 }
             }
         }
 
         // Ȃꍇ́COԂ UNKNOWN  ONX쐬
         final ExternalClassInfo unknownClassInfo = new ExternalClassInfo(
                 referenceName[referenceName.length - 1]);
         return unknownClassInfo;
     }
 
     /**
      * ŗ^ꂽ^ List Op[^ List 쐬CԂ
      * 
      * @param types ^List
      * @return Op[^ List
      */
     public static List<ParameterInfo> createParameters(final List<TypeInfo> types) {
 
         if (null == types) {
             throw new NullPointerException();
         }
 
         final List<ParameterInfo> parameters = new LinkedList<ParameterInfo>();
         for (TypeInfo type : types) {
             final ExternalParameterInfo parameter = new ExternalParameterInfo(type);
             parameters.add(parameter);
         }
 
         return Collections.unmodifiableList(parameters);
     }
 
     /**
      * ŗ^ꂽNX̐eNXłCONX(ExternalClassInfo)ł̂ԂD NXKwIɍłʂɈʒuONXԂD
      * YNX݂Ȃꍇ́C null ԂD
      * 
      * @param classInfo ΏۃNX
      * @return ŗ^ꂽNX̐eNXłCNXKwIɍłʂɈʒuONX
      */
     private static ExternalClassInfo getExternalSuperClass(final TargetClassInfo classInfo) {
 
         if (null == classInfo) {
             throw new NullPointerException();
         }
 
         for (final ClassInfo superClassInfo : classInfo.getSuperClasses()) {
 
             if (superClassInfo instanceof ExternalClassInfo) {
                 return (ExternalClassInfo) superClassInfo;
             }
 
             final ExternalClassInfo superSuperClassInfo = NameResolver
                     .getExternalSuperClass((TargetClassInfo) superClassInfo);
             if (null != superSuperClassInfo) {
                 return superSuperClassInfo;
             }
         }
 
         return null;
     }
 
     /**
      * ŗ^ꂽNXNXƂĎCłÓiCi[NXłȂjNXԂ
      * 
      * @param innerClass Ci[NX
      * @return łÕNX
      */
     private static TargetClassInfo getOuterstClass(final TargetInnerClassInfo innerClass) {
 
         if (null == innerClass) {
             throw new NullPointerException();
         }
 
         final TargetClassInfo outerClass = innerClass.getOuterClass();
         if (outerClass instanceof TargetInnerClassInfo) {
             return NameResolver.getOuterstClass((TargetInnerClassInfo) outerClass);
         } else {
             return outerClass;
         }
     }
 
     /**
      * ŗ^ꂽNX̗p\ȓNX SortedSet Ԃ
      * 
      * @param classInfo NX
      * @return ŗ^ꂽNX̗p\ȓNX SortedSet
      */
     private static SortedSet<TargetInnerClassInfo> getAvailableInnerClasses(
             final TargetClassInfo classInfo) {
 
         if (null == classInfo) {
             throw new NullPointerException();
         }
 
         final SortedSet<TargetInnerClassInfo> innerClasses = new TreeSet<TargetInnerClassInfo>();
         for (final TargetInnerClassInfo innerClass : classInfo.getInnerClasses()) {
 
             innerClasses.add(innerClass);
             final SortedSet<TargetInnerClassInfo> innerClassesInInnerClass = NameResolver
                     .getAvailableInnerClasses(innerClass);
             innerClasses.addAll(innerClassesInInnerClass);
         }
 
         return Collections.unmodifiableSortedSet(innerClasses);
     }
 
     /**
      * ũ݂NXvŗp\ȃtB[hꗗԂD
      * ŁCup\ȃtB[hvƂ́Cũ݂NXvŒ`ĂtB[hCũ݂NXṽCi[NXŒ`ĂtB[hC
      * yт̐eNXŒ`ĂtB[ĥqNXANZX\ȃtB[hłD p\ȃtB[h List Ɋi[ĂD
      * Xg̐擪D揇ʂ̍tB[hi܂C NXKwɂĉʂ̃NXɒ`ĂtB[hji[ĂD
      * 
      * @param currentClass ݂̃NX
      * @return p\ȃtB[hꗗ
      */
     private static List<TargetFieldInfo> getAvailableFields(final TargetClassInfo currentClass) {
 
         if (null == currentClass) {
             throw new NullPointerException();
         }
 
         // `FbNNX邽߂̃LbVCLbVɂNX͓xڂ̓tB[h擾Ȃi[v\΍j
         final Set<TargetClassInfo> checkedClasses = new HashSet<TargetClassInfo>();
 
         // p\ȕϐ邽߂̃Xg
         final List<TargetFieldInfo> availableFields = new LinkedList<TargetFieldInfo>();
 
         // łÕNX擾
         final TargetClassInfo outestClass;
         if (currentClass instanceof TargetInnerClassInfo) {
             outestClass = NameResolver.getOuterstClass((TargetInnerClassInfo) currentClass);
 
            // NXŒ`ꂽ\bhǉ
            availableFields.addAll(currentClass.getDefinedFields());
            checkedClasses.add(currentClass);
 
             // NXŒ`ꂽtB[hǉ
             for (final TargetInnerClassInfo innerClass : currentClass.getInnerClasses()) {
                 final List<TargetFieldInfo> availableFieldsDefinedInInnerClasses = NameResolver
                         .getAvailableFieldsDefinedInInnerClasses(innerClass, checkedClasses);
                 availableFields.addAll(availableFieldsDefinedInInnerClasses);
             }
 
             // eNXŒ`ꂽtB[hǉ
             for (final ClassInfo superClass : currentClass.getSuperClasses()) {
                 if (superClass instanceof TargetClassInfo) {
                     final List<TargetFieldInfo> availableFieldsDefinedInSuperClasses = NameResolver
                             .getAvailableFieldsDefinedInSuperClasses((TargetClassInfo) superClass,
                                     checkedClasses);
                     availableFields.addAll(availableFieldsDefinedInSuperClasses);
                 }
             }
 
         } else {
             outestClass = currentClass;
         }
 
         // łÕNXŒ`ꂽtB[hǉ
         availableFields.addAll(outestClass.getDefinedFields());
         checkedClasses.add(outestClass);
 
         // NXŒ`ꂽtB[hǉ
         for (final TargetInnerClassInfo innerClass : outestClass.getInnerClasses()) {
             final List<TargetFieldInfo> availableFieldsDefinedInInnerClasses = NameResolver
                     .getAvailableFieldsDefinedInInnerClasses(innerClass, checkedClasses);
             availableFields.addAll(availableFieldsDefinedInInnerClasses);
         }
 
         // eNXŒ`ꂽtB[hǉ
         for (final ClassInfo superClass : outestClass.getSuperClasses()) {
             if (superClass instanceof TargetClassInfo) {
                 final List<TargetFieldInfo> availableFieldsDefinedInSuperClasses = NameResolver
                         .getAvailableFieldsDefinedInSuperClasses((TargetClassInfo) superClass,
                                 checkedClasses);
                 availableFields.addAll(availableFieldsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableFields);
     }
 
     /**
      * ŗ^ꂽNXƂ̓NXŒ`ꂽtB[ĥCÕNXŗp\ȃtB[h List Ԃ
      * 
      * @param classInfo NX
      * @param checkedClasses Ƀ`FbNNX̃LbV
      * @return ÕNXŗp\ȃtB[h List
      */
     private static List<TargetFieldInfo> getAvailableFieldsDefinedInInnerClasses(
             final TargetInnerClassInfo classInfo, final Set<TargetClassInfo> checkedClasses) {
 
         if ((null == classInfo) || (null == checkedClasses)) {
             throw new NullPointerException();
         }
 
         // Ƀ`FbNNXłꍇ͉ɏI
         if (checkedClasses.contains(classInfo)) {
             return new LinkedList<TargetFieldInfo>();
         }
 
         final List<TargetFieldInfo> availableFields = new LinkedList<TargetFieldInfo>();
 
         // NXŒ`ĂCOԉtB[hǉ
         // for (final TargetFieldInfo definedField : classInfo.getDefinedFields()) {
         // if (definedField.isNamespaceVisible()) {
         // availableFields.add(definedField);
         // }
         // }
         availableFields.addAll(classInfo.getDefinedFields());
         checkedClasses.add(classInfo);
 
         // NXŒ`ꂽtB[hǉ
         for (final TargetInnerClassInfo innerClass : classInfo.getInnerClasses()) {
             final List<TargetFieldInfo> availableFieldsDefinedInInnerClasses = NameResolver
                     .getAvailableFieldsDefinedInInnerClasses(innerClass, checkedClasses);
             availableFields.addAll(availableFieldsDefinedInInnerClasses);
         }
 
         // eNXŒ`ꂽtB[hǉ
         for (final ClassInfo superClass : classInfo.getSuperClasses()) {
             if (superClass instanceof TargetClassInfo) {
                 final List<TargetFieldInfo> availableFieldsDefinedInSuperClasses = NameResolver
                         .getAvailableFieldsDefinedInSuperClasses((TargetClassInfo) superClass,
                                 checkedClasses);
                 availableFields.addAll(availableFieldsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableFields);
     }
 
     /**
      * ŗ^ꂽNXƂ̐eNXŒ`ꂽtB[ĥCqNXŗp\ȃtB[h List Ԃ
      * 
      * @param classInfo NX
      * @param checkedClasses Ƀ`FbNNX̃LbV
      * @return qNXŗp\ȃtB[h List
      */
     private static List<TargetFieldInfo> getAvailableFieldsDefinedInSuperClasses(
             final TargetClassInfo classInfo, final Set<TargetClassInfo> checkedClasses) {
 
         if ((null == classInfo) || (null == checkedClasses)) {
             throw new NullPointerException();
         }
 
         // Ƀ`FbNNXłꍇ͉ɏI
         if (checkedClasses.contains(classInfo)) {
             return new LinkedList<TargetFieldInfo>();
         }
 
         final List<TargetFieldInfo> availableFields = new LinkedList<TargetFieldInfo>();
 
         // NXŒ`ĂCNXKwtB[hǉ
         for (final TargetFieldInfo definedField : classInfo.getDefinedFields()) {
             if (definedField.isInheritanceVisible()) {
                 availableFields.add(definedField);
             }
         }
         checkedClasses.add(classInfo);
 
         // NXŒ`ꂽtB[hǉ
         for (final TargetInnerClassInfo innerClass : classInfo.getInnerClasses()) {
             final List<TargetFieldInfo> availableFieldsDefinedInInnerClasses = NameResolver
                     .getAvailableFieldsDefinedInInnerClasses(innerClass, checkedClasses);
             for (final TargetFieldInfo field : availableFieldsDefinedInInnerClasses) {
                 if (field.isInheritanceVisible()) {
                     availableFields.add(field);
                 }
             }
         }
 
         // eNXŒ`ꂽtB[hǉ
         for (final ClassInfo superClass : classInfo.getSuperClasses()) {
             if (superClass instanceof TargetClassInfo) {
                 final List<TargetFieldInfo> availableFieldsDefinedInSuperClasses = NameResolver
                         .getAvailableFieldsDefinedInSuperClasses((TargetClassInfo) superClass,
                                 checkedClasses);
                 availableFields.addAll(availableFieldsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableFields);
     }
 
     /**
      * ũ݂NXvŗp\ȃ\bhꗗԂD
      * ŁCup\ȃ\bhvƂ́Cũ݂NXvŒ`Ă郁\bhCyт̐eNXŒ`Ă郁\bĥqNXANZX\ȃ\bhłD
      * p\ȃ\bh List Ɋi[ĂD Xg̐擪D揇ʂ̍\bhi܂CNXKwɂĉʂ̃NXɒ`Ă郁\bhji[ĂD
      * 
      * @param thisClass ݂̃NX
      * @return p\ȃ\bhꗗ
      */
     private static List<TargetMethodInfo> getAvailableMethods(final TargetClassInfo currentClass) {
 
         if (null == currentClass) {
             throw new NullPointerException();
         }
 
         // `FbNNX邽߂̃LbVCLbVɂNX͓xڂ̓tB[h擾Ȃi[v\΍j
         final Set<TargetClassInfo> checkedClasses = new HashSet<TargetClassInfo>();
 
         // p\ȕϐ邽߂̃Xg
         final List<TargetMethodInfo> availableMethods = new LinkedList<TargetMethodInfo>();
 
         // łÕNX擾
         final TargetClassInfo outestClass;
         if (currentClass instanceof TargetInnerClassInfo) {
             outestClass = NameResolver.getOuterstClass((TargetInnerClassInfo) currentClass);
 
             // NXŒ`ꂽ\bhǉ
             availableMethods.addAll(currentClass.getDefinedMethods());
             checkedClasses.add(currentClass);
 
             // NXŒ`ꂽ\bhǉ
             for (final TargetInnerClassInfo innerClass : currentClass.getInnerClasses()) {
                 final List<TargetMethodInfo> availableMethodsDefinedInInnerClasses = NameResolver
                         .getAvailableMethodsDefinedInInnerClasses(innerClass, checkedClasses);
                 availableMethods.addAll(availableMethodsDefinedInInnerClasses);
             }
 
             // eNXŒ`ꂽ\bhǉ
             for (final ClassInfo superClass : currentClass.getSuperClasses()) {
                 if (superClass instanceof TargetClassInfo) {
                     final List<TargetMethodInfo> availableMethodsDefinedInSuperClasses = NameResolver
                             .getAvailableMethodsDefinedInSuperClasses((TargetClassInfo) superClass,
                                     checkedClasses);
                     availableMethods.addAll(availableMethodsDefinedInSuperClasses);
                 }
             }
 
         } else {
             outestClass = currentClass;
         }
 
         // łÕNXŒ`ꂽ\bhǉ
         availableMethods.addAll(outestClass.getDefinedMethods());
         checkedClasses.add(outestClass);
 
         // NXŒ`ꂽ\bhǉ
         for (final TargetInnerClassInfo innerClass : outestClass.getInnerClasses()) {
             final List<TargetMethodInfo> availableMethodsDefinedInInnerClasses = NameResolver
                     .getAvailableMethodsDefinedInInnerClasses(innerClass, checkedClasses);
             availableMethods.addAll(availableMethodsDefinedInInnerClasses);
         }
 
         // eNXŒ`ꂽ\bhǉ
         for (final ClassInfo superClass : outestClass.getSuperClasses()) {
             if (superClass instanceof TargetClassInfo) {
                 final List<TargetMethodInfo> availableMethodsDefinedInSuperClasses = NameResolver
                         .getAvailableMethodsDefinedInSuperClasses((TargetClassInfo) superClass,
                                 checkedClasses);
                 availableMethods.addAll(availableMethodsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableMethods);
     }
 
     /**
      * ŗ^ꂽNXƂ̓NXŒ`ꂽ\bĥCÕNXŗp\ȃ\bh List Ԃ
      * 
      * @param classInfo NX
      * @param checkedClasses Ƀ`FbNNX̃LbV
      * @return ÕNXŗp\ȃ\bh List
      */
     private static List<TargetMethodInfo> getAvailableMethodsDefinedInInnerClasses(
             final TargetInnerClassInfo classInfo, final Set<TargetClassInfo> checkedClasses) {
 
         if ((null == classInfo) || (null == checkedClasses)) {
             throw new NullPointerException();
         }
 
         // Ƀ`FbNNXłꍇ͉ɏI
         if (checkedClasses.contains(classInfo)) {
             return new LinkedList<TargetMethodInfo>();
         }
 
         final List<TargetMethodInfo> availableMethods = new LinkedList<TargetMethodInfo>();
 
         // NXŒ`ĂCOԉ\bhǉ
         // for (final TargetFieldInfo definedField : classInfo.getDefinedFields()) {
         // if (definedField.isNamespaceVisible()) {
         // availableFields.add(definedField);
         // }
         // }
         availableMethods.addAll(classInfo.getDefinedMethods());
         checkedClasses.add(classInfo);
 
         // NXŒ`ꂽ\bhǉ
         for (final TargetInnerClassInfo innerClass : classInfo.getInnerClasses()) {
             final List<TargetMethodInfo> availableMethodsDefinedInInnerClasses = NameResolver
                     .getAvailableMethodsDefinedInInnerClasses(innerClass, checkedClasses);
             availableMethods.addAll(availableMethodsDefinedInInnerClasses);
         }
 
         // eNXŒ`ꂽ\bhǉ
         for (final ClassInfo superClass : classInfo.getSuperClasses()) {
             if (superClass instanceof TargetClassInfo) {
                 final List<TargetMethodInfo> availableMethodsDefinedInSuperClasses = NameResolver
                         .getAvailableMethodsDefinedInSuperClasses((TargetClassInfo) superClass,
                                 checkedClasses);
                 availableMethods.addAll(availableMethodsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableMethods);
     }
 
     /**
      * ŗ^ꂽNXƂ̐eNXŒ`ꂽ\bĥCqNXŗp\ȃ\bh List Ԃ
      * 
      * @param classInfo NX
      * @param checkedClasses Ƀ`FbNNX̃LbV
      * @return qNXŗp\ȃ\bh List
      */
     private static List<TargetMethodInfo> getAvailableMethodsDefinedInSuperClasses(
             final TargetClassInfo classInfo, final Set<TargetClassInfo> checkedClasses) {
 
         if ((null == classInfo) || (null == checkedClasses)) {
             throw new NullPointerException();
         }
 
         // Ƀ`FbNNXłꍇ͉ɏI
         if (checkedClasses.contains(classInfo)) {
             return new LinkedList<TargetMethodInfo>();
         }
 
         final List<TargetMethodInfo> availableMethods = new LinkedList<TargetMethodInfo>();
 
         // NXŒ`ĂCNXKw\bhǉ
         for (final TargetMethodInfo definedMethod : classInfo.getDefinedMethods()) {
             if (definedMethod.isInheritanceVisible()) {
                 availableMethods.add(definedMethod);
             }
         }
         checkedClasses.add(classInfo);
 
         // NXŒ`ꂽ\bhǉ
         for (final TargetInnerClassInfo innerClass : classInfo.getInnerClasses()) {
             final List<TargetMethodInfo> availableMethodsDefinedInInnerClasses = NameResolver
                     .getAvailableMethodsDefinedInInnerClasses(innerClass, checkedClasses);
             for (final TargetMethodInfo method : availableMethodsDefinedInInnerClasses) {
                 if (method.isInheritanceVisible()) {
                     availableMethods.add(method);
                 }
             }
         }
 
         // eNXŒ`ꂽ\bhǉ
         for (final ClassInfo superClass : classInfo.getSuperClasses()) {
             if (superClass instanceof TargetClassInfo) {
                 final List<TargetMethodInfo> availableMethodsDefinedInSuperClasses = NameResolver
                         .getAvailableMethodsDefinedInSuperClasses((TargetClassInfo) superClass,
                                 checkedClasses);
                 availableMethods.addAll(availableMethodsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableMethods);
     }
 
     /**
      * ugpNXvugpNXvɂĎgpꍇɁCp\ȃtB[hꗗԂD
      * ŁCup\ȃtB[hvƂ́CugpNXvŒ`ĂtB[hCyт̐eNXŒ`ĂtB[ĥqNXANZX\ȃtB[hłD
      * ܂CugpNXvƁugpNXv̖OԂrC萳mɗp\ȃtB[h擾D qNXŗp\ȃtB[hꗗ List Ɋi[ĂD
      * Xg̐擪D揇ʂ̍tB[hi܂CNXKwɂĉʂ̃NXɒ`ĂtB[hji[ĂD
      * 
      * @param usedClass gpNX
      * @param usingClass gpNX
      * @return p\ȃtB[hꗗ
      */
     private static List<TargetFieldInfo> getAvailableFields(final TargetClassInfo usedClass,
             final TargetClassInfo usingClass) {
 
         if ((null == usedClass) || (null == usingClass)) {
             throw new NullPointerException();
         }
 
         // gpNX̍łÕNX擾
         final TargetClassInfo usedOutestClass;
         if (usedClass instanceof TargetInnerClassInfo) {
             usedOutestClass = NameResolver.getOuterstClass((TargetInnerClassInfo) usedClass);
         } else {
             usedOutestClass = usedClass;
         }
 
         // gpNX̍łÕNX擾
         final TargetClassInfo usingOutestClass;
         if (usingClass instanceof TargetInnerClassInfo) {
             usingOutestClass = NameResolver.getOuterstClass((TargetInnerClassInfo) usingClass);
         } else {
             usingOutestClass = usingClass;
         }
 
         // ̃NXŒ`ĂtB[ĥCgpNXŗp\ȃtB[h擾
         // 2̃NXꍇCSẴtB[hp\
         if (usedOutestClass.equals(usingOutestClass)) {
 
             return NameResolver.getAvailableFields(usedClass);
 
             // 2̃NXOԂĂꍇ
         } else if (usedOutestClass.getNamespace().equals(usingOutestClass.getNamespace())) {
 
             final List<TargetFieldInfo> availableFields = new LinkedList<TargetFieldInfo>();
 
             // OԉtB[ĥ݂p\
             for (final TargetFieldInfo field : NameResolver.getAvailableFields(usedClass)) {
                 if (field.isNamespaceVisible()) {
                     availableFields.add(field);
                 }
             }
 
             return Collections.unmodifiableList(availableFields);
 
             // ႤOԂĂꍇ
         } else {
 
             final List<TargetFieldInfo> availableFields = new LinkedList<TargetFieldInfo>();
 
             // StB[ĥ݂p\
             for (final TargetFieldInfo field : NameResolver.getAvailableFields(usedClass)) {
                 if (field.isPublicVisible()) {
                     availableFields.add(field);
                 }
             }
 
             return Collections.unmodifiableList(availableFields);
         }
     }
 
     /**
      * ugpNXvugpNXvɂĎgpꍇɁCp\ȃ\bhꗗԂD
      * ŁCup\ȃ\bhvƂ́CugpNXvŒ`Ă郁\bhCyт̐eNXŒ`Ă郁\bĥqNXANZX\ȃ\bhłD
      * ܂CugpNXvƁugpNXv̖OԂrC萳mɗp\ȃ\bh擾D qNXŗp\ȃ\bhꗗ List Ɋi[ĂD
      * Xg̐擪D揇ʂ̍\bhi܂CNXKwɂĉʂ̃NXɒ`Ă郁\bhji[ĂD
      * 
      * @param usedClass gpNX
      * @param usingClass gpNX
      * @return p\ȃ\bhꗗ
      */
     private static List<TargetMethodInfo> getAvailableMethods(final TargetClassInfo usedClass,
             final TargetClassInfo usingClass) {
 
         if ((null == usedClass) || (null == usingClass)) {
             throw new NullPointerException();
         }
 
         // gpNX̍łÕNX擾
         final TargetClassInfo usedOutestClass;
         if (usedClass instanceof TargetInnerClassInfo) {
             usedOutestClass = NameResolver.getOuterstClass((TargetInnerClassInfo) usedClass);
         } else {
             usedOutestClass = usedClass;
         }
 
         // gpNX̍łÕNX擾
         final TargetClassInfo usingOutestClass;
         if (usingClass instanceof TargetInnerClassInfo) {
             usingOutestClass = NameResolver.getOuterstClass((TargetInnerClassInfo) usingClass);
         } else {
             usingOutestClass = usingClass;
         }
 
         // ̃NXŒ`Ă郁\bĥCgpNXŗp\ȃ\bh擾
         // 2̃NXꍇCSẴ\bhp\
         if (usedOutestClass.equals(usingOutestClass)) {
 
             return NameResolver.getAvailableMethods(usedClass);
 
             // 2̃NXOԂĂꍇ
         } else if (usedOutestClass.getNamespace().equals(usingOutestClass.getNamespace())) {
 
             final List<TargetMethodInfo> availableMethods = new LinkedList<TargetMethodInfo>();
 
             // Oԉ\bĥ݂p\
             for (final TargetMethodInfo method : NameResolver.getAvailableMethods(usedClass)) {
                 if (method.isNamespaceVisible()) {
                     availableMethods.add(method);
                 }
             }
 
             return Collections.unmodifiableList(availableMethods);
 
             // ႤOԂĂꍇ
         } else {
 
             final List<TargetMethodInfo> availableMethods = new LinkedList<TargetMethodInfo>();
 
             // S\bĥ݂p\
             for (final TargetMethodInfo method : NameResolver.getAvailableMethods(usedClass)) {
                 if (method.isPublicVisible()) {
                     availableMethods.add(method);
                 }
             }
 
             return Collections.unmodifiableList(availableMethods);
         }
     }
 
     /**
      * ŗ^ꂽNX̒ڂ̃Ci[NXԂDeNXŒ`ꂽCi[NX܂܂D
      * 
      * @param classInfo NX
      * @return ŗ^ꂽNX̒ڂ̃Ci[NXCeNXŒ`ꂽCi[NX܂܂D
      */
     private static final SortedSet<TargetInnerClassInfo> getAvailableDirectInnerClasses(
             final TargetClassInfo classInfo) {
 
         if (null == classInfo) {
             throw new NullPointerException();
         }
 
         final SortedSet<TargetInnerClassInfo> availableDirectInnerClasses = new TreeSet<TargetInnerClassInfo>();
 
         // ŗ^ꂽNX̒ڂ̃Ci[NXǉ
         availableDirectInnerClasses.addAll(classInfo.getInnerClasses());
 
         // eNXɑ΂čċAIɏ
         for (final ClassInfo superClassInfo : classInfo.getSuperClasses()) {
 
             if (superClassInfo instanceof TargetClassInfo) {
                 final SortedSet<TargetInnerClassInfo> availableDirectInnerClassesInSuperClass = NameResolver
                         .getAvailableDirectInnerClasses((TargetClassInfo) superClassInfo);
                 availableDirectInnerClasses.addAll(availableDirectInnerClassesInSuperClass);
             }
         }
 
         return Collections.unmodifiableSortedSet(availableDirectInnerClasses);
     }
 
     /**
      * G[bZ[Wo͗p̃v^
      */
     private static final MessagePrinter err = new DefaultMessagePrinter(new MessageSource() {
         public String getMessageSourceName() {
             return "NameResolver";
         }
     }, MESSAGE_TYPE.ERROR);
 }
