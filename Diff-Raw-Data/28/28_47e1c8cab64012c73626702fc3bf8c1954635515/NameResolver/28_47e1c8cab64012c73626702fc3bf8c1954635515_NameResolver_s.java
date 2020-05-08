 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ArrayTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.NullTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.PrimitiveTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetFieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetInnerClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeInfo;
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
 
 
 /**
  * Unresolved * Info  * Info 𓾂邽߂̖O[eBeBNX
  * 
  * @author y-higo
  * 
  */
 public final class NameResolver {
 
     /**
      * ^iUnresolvedTypeInfojς݌^iTypeInfojԂD Ής݌^񂪂Ȃꍇ null ԂD
      * 
      * @param unresolvedTypeInfo O^
      * @param classInfoManager Qƌ^̉ɗpf[^x[X
      * @return Oꂽ^
      */
     public static TypeInfo resolveTypeInfo(final UnresolvedTypeInfo unresolvedTypeInfo,
             final ClassInfoManager classInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == unresolvedTypeInfo) || (null == classInfoManager)) {
             throw new NullPointerException();
         }
 
         // v~eBu^̏ꍇ
         if (unresolvedTypeInfo instanceof PrimitiveTypeInfo) {
             return (PrimitiveTypeInfo) unresolvedTypeInfo;
 
             // void^̏ꍇ
         } else if (unresolvedTypeInfo instanceof VoidTypeInfo) {
             return (VoidTypeInfo) unresolvedTypeInfo;
 
             // Qƌ^̏ꍇ
         } else if (unresolvedTypeInfo instanceof UnresolvedReferenceTypeInfo) {
 
             // p\ȖOԂC^T
             final String[] referenceName = ((UnresolvedReferenceTypeInfo) unresolvedTypeInfo)
                     .getReferenceName();
             for (AvailableNamespaceInfo availableNamespace : ((UnresolvedReferenceTypeInfo) unresolvedTypeInfo)
                     .getAvailableNamespaces()) {
 
                 // OԖ.* ƂȂĂꍇ
                 if (availableNamespace.isAllClasses()) {
                     final String[] namespace = availableNamespace.getNamespace();
 
                     // OԂ̉ɂeNXɑ΂
                     for (ClassInfo classInfo : classInfoManager.getClassInfos(namespace)) {
                         final String className = classInfo.getClassName();
 
                         // NXƎQƖ̐擪ꍇ́C̃NXQƐłƌ肷
                         if (className.equals(referenceName[0])) {
                             return classInfo;
                         }
                     }
 
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
                         final ClassInfo specifiedClassInfo = classInfoManager
                                 .getClassInfo(fullQualifiedName);
                         // NXȂꍇ null Ԃ
                         return specifiedClassInfo;
                     }
                 }
             }
 
             // Ȃꍇ null Ԃ
             return null;
 
         } else if (unresolvedTypeInfo instanceof UnresolvedArrayTypeInfo) {
 
             final UnresolvedTypeInfo unresolvedElementType = ((UnresolvedArrayTypeInfo) unresolvedTypeInfo)
                     .getElementType();
             final int dimension = ((UnresolvedArrayTypeInfo) unresolvedTypeInfo).getDimension();
 
             final TypeInfo elementType = NameResolver.resolveTypeInfo(unresolvedElementType,
                     classInfoManager);
             if (elementType != null) {
 
                 final ArrayTypeInfo arrayType = ArrayTypeInfo.getType(elementType, dimension);
                 return arrayType;
             }
 
             // vf̌^sȂƂ null Ԃ
             return null;
 
             // ȊǑ^̏ꍇ̓G[
         } else {
             throw new IllegalArgumentException(unresolvedTypeInfo.toString()
                     + " is a wrong object!");
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
 
         // tB[hCyуtB[hQƂĂ関`^擾
         final String fieldName = fieldReference.getFieldName();
         final UnresolvedTypeInfo unresolvedFieldOwnerClassType = fieldReference.getOwnerClassType();
 
         // -----eTypeInfo 擾R[h
         TypeInfo fieldOwnerClassType = null;
 
         // tB[hQ(a)tB[hQ(b)ɂĂꍇ (b.a)
         if (unresolvedFieldOwnerClassType instanceof UnresolvedFieldUsage) {
 
             // (b)̃NX`擾
             fieldOwnerClassType = NameResolver.resolveFieldReference(
                     (UnresolvedFieldUsage) unresolvedFieldOwnerClassType, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
 
             // tB[hQ(a)\bhĂяo(c())ɂĂꍇ(c().a)
         } else if (unresolvedFieldOwnerClassType instanceof UnresolvedMethodCall) {
 
             // (c)̃NX`擾
             fieldOwnerClassType = NameResolver.resolveMethodCall(
                     (UnresolvedMethodCall) unresolvedFieldOwnerClassType, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
 
             // tB[hQ(a)GeBeBgpɂĂꍇ
         } else if (unresolvedFieldOwnerClassType instanceof UnresolvedEntityUsage) {
 
             // GeBeB̃NX`擾
             fieldOwnerClassType = NameResolver.resolveEntityUsage(
                     (UnresolvedEntityUsage) unresolvedFieldOwnerClassType, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
 
             // tB[hQ(a)z̗vfɂĂꍇ(d[i].a)
         } else if (unresolvedFieldOwnerClassType instanceof UnresolvedArrayElementUsage) {
 
             fieldOwnerClassType = NameResolver.resolveArrayElementUsage(
                     (UnresolvedArrayElementUsage) unresolvedFieldOwnerClassType, usingClass,
                     usingMethod, classInfoManager, fieldInfoManager, methodInfoManager,
                     resolvedCache);
 
             // tB[hgp(a)IuWFNgɂĂꍇ(a or this.a or super.a )
         } else if (unresolvedFieldOwnerClassType instanceof UnresolvedClassInfo) {
 
             // NX̃NX`擾
             fieldOwnerClassType = usingClass;
 
         } else {
             err.println("Here shouldn't be reached!");
             return null;
         }
 
         // -----eTypeInfo ɉď𕪊
         // ełȂꍇ͂ǂ悤Ȃ
         if (null == fieldOwnerClassType) {
 
             // Ȃs
             usingMethod.addUnresolvedUsage(fieldReference);
 
             // ς݃LbVɓo^
             resolvedCache.put(fieldReference, null);
 
             return null;
 
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
                 final ExternalClassInfo externalSuperClass = NameResolver
                         .getExternalSuperClass((TargetClassInfo) fieldOwnerClassType);
                 if (!(fieldOwnerClassType instanceof TargetInnerClassInfo)
                         && (null != externalSuperClass)) {
 
                     final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(fieldName,
                             externalSuperClass);
                     usingMethod.addReferencee(fieldInfo);
                     fieldInfo.addReferencer(usingMethod);
                     fieldInfoManager.add(fieldInfo);
 
                     // ς݃LbVɓo^
                     resolvedCache.put(fieldReference, null);
 
                     // ONXɐVKŊOϐ(ExternalFieldInfo)ǉ̂Ō^͕sD
                     return null;
                 }
             }
 
             // Ȃs
             {
                 usingMethod.addUnresolvedUsage(fieldReference);
 
                 // ς݃LbVɓo^
                 resolvedCache.put(fieldReference, null);
 
                 return null;
             }
 
             // eONXiExternalClassInfojꍇ
         } else if (fieldOwnerClassType instanceof ExternalClassInfo) {
 
             final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(fieldName,
                     (ExternalClassInfo) fieldOwnerClassType);
             usingMethod.addReferencee(fieldInfo);
             fieldInfo.addReferencer(usingMethod);
             fieldInfoManager.add(fieldInfo);
 
             // ς݃LbVɓo^
             resolvedCache.put(fieldReference, null);
 
             // ONXɐVKŊOϐ(ExternalFieldInfo)ǉ̂Ō^͕sD
             return null;
         }
 
         err.println("Here shouldn't be reached!");
         return null;
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
 
         // tB[hCyуtB[hĂ関`^擾
         final String fieldName = fieldAssignment.getFieldName();
         final UnresolvedTypeInfo unresolvedFieldOwnerClassType = fieldAssignment
                 .getOwnerClassType();
 
         // -----eTypeInfo 擾R[h
         TypeInfo fieldOwnerClassType = null;
 
         // tB[h(a)tB[hQ(b)ɂĂꍇ (b.a)
         if (unresolvedFieldOwnerClassType instanceof UnresolvedFieldUsage) {
 
             // (b)̃NX`擾
             fieldOwnerClassType = NameResolver.resolveFieldReference(
                     (UnresolvedFieldUsage) unresolvedFieldOwnerClassType, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
 
             // tB[h(a)\bhĂяo(c())ɂĂꍇ(c().a)
         } else if (unresolvedFieldOwnerClassType instanceof UnresolvedMethodCall) {
 
             // (c)̃NX`擾
             fieldOwnerClassType = NameResolver.resolveMethodCall(
                     (UnresolvedMethodCall) unresolvedFieldOwnerClassType, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
 
             // tB[h(a)GeBeBgpɂĂꍇ
         } else if (unresolvedFieldOwnerClassType instanceof UnresolvedEntityUsage) {
 
             // GeBeB̃NX`擾
             fieldOwnerClassType = NameResolver.resolveEntityUsage(
                     (UnresolvedEntityUsage) unresolvedFieldOwnerClassType, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
 
             // tB[h(a)z̗vfɂĂꍇ(d[i].a)
         } else if (unresolvedFieldOwnerClassType instanceof UnresolvedArrayElementUsage) {
 
             fieldOwnerClassType = NameResolver.resolveArrayElementUsage(
                     (UnresolvedArrayElementUsage) unresolvedFieldOwnerClassType, usingClass,
                     usingMethod, classInfoManager, fieldInfoManager, methodInfoManager,
                     resolvedCache);
 
             // tB[h(a)IuWFNgɂĂꍇ(a or this.a or super.a )
         } else if (unresolvedFieldOwnerClassType instanceof UnresolvedClassInfo) {
 
             fieldOwnerClassType = usingClass;
 
         } else {
 
             err.println("Here shouldn't be reached!");
             return null;
         }
 
         // -----eTypeInfo ɉď𕪊
         // ełȂꍇ͂ǂ悤Ȃ
         if (null == fieldOwnerClassType) {
 
             // Ȃs
             usingMethod.addUnresolvedUsage(fieldAssignment);
 
             // ς݃LbVɓo^
             resolvedCache.put(fieldAssignment, null);
 
             return null;
 
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
                 final ExternalClassInfo externalSuperClass = NameResolver
                         .getExternalSuperClass((TargetClassInfo) fieldOwnerClassType);
                 if (!(fieldOwnerClassType instanceof TargetInnerClassInfo)
                         && (null != externalSuperClass)) {
 
                     final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(fieldName,
                             externalSuperClass);
                     usingMethod.addAssignmentee(fieldInfo);
                     fieldInfo.addAssignmenter(usingMethod);
                     fieldInfoManager.add(fieldInfo);
 
                     // ς݃LbVɓo^
                     resolvedCache.put(fieldAssignment, null);
 
                     // ONXɐVKŊOϐiExternalFieldInfojǉ̂Ō^͕s
                     return null;
                 }
             }
 
             // Ȃs
             {
                 usingMethod.addUnresolvedUsage(fieldAssignment);
 
                 // ς݃LbVɓo^
                 resolvedCache.put(fieldAssignment, null);
 
                 return null;
             }
 
             // eONXiExternalClassInfojꍇ
         } else if (fieldOwnerClassType instanceof ExternalClassInfo) {
 
             final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(fieldName,
                     (ExternalClassInfo) fieldOwnerClassType);
             usingMethod.addAssignmentee(fieldInfo);
             fieldInfo.addAssignmenter(usingMethod);
             fieldInfoManager.add(fieldInfo);
 
             // ς݃LbVɓo^
             resolvedCache.put(fieldAssignment, null);
 
             // ONXɐVKŊOϐ(ExternalFieldInfo)ǉ̂Ō^͕sD
             return null;
         }
 
         err.println("Here shouldn't be reached!");
         return null;
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
 
         // \bh̃VOl`Cyу\bhgpĂ関`Qƌ^擾
         final String methodName = methodCall.getMethodName();
         final boolean constructor = methodCall.isConstructor();
         final List<UnresolvedTypeInfo> unresolvedParameterTypes = methodCall.getParameterTypes();
         final UnresolvedTypeInfo unresolvedMethodOwnerClassType = methodCall.getOwnerClassType();
 
         // \bh̖
         final List<TypeInfo> parameterTypes = new LinkedList<TypeInfo>();
         for (UnresolvedTypeInfo unresolvedParameterType : unresolvedParameterTypes) {
 
             // tB[hQƂ̏ꍇ
             if (unresolvedParameterType instanceof UnresolvedFieldUsage) {
                 final TypeInfo parameterType = NameResolver.resolveFieldReference(
                         (UnresolvedFieldUsage) unresolvedParameterType, usingClass, usingMethod,
                         classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
                 parameterTypes.add(parameterType);
 
                 // \bhĂяȍꍇ
             } else if (unresolvedParameterType instanceof UnresolvedMethodCall) {
                 final TypeInfo parameterType = NameResolver.resolveMethodCall(
                         (UnresolvedMethodCall) unresolvedParameterType, usingClass, usingMethod,
                         classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
                 parameterTypes.add(parameterType);
 
                 //  null ̏ꍇ
             } else if (unresolvedParameterType instanceof NullTypeInfo) {
                 parameterTypes.add((TypeInfo) unresolvedParameterType);
 
                 // ȊǑ^̓G[
             } else {
                 err.println("Here shouldn't be reached!");
                 return null;
             }
         }
 
         // -----eTypeInfo 擾R[h
         TypeInfo methodOwnerClassType = null;
 
         // \bhĂяo(a())tB[hgp(b)ɂĂꍇ (b.a())
         if (unresolvedMethodOwnerClassType instanceof UnresolvedFieldUsage) {
 
             // (b)̃NX`擾
             methodOwnerClassType = NameResolver.resolveFieldReference(
                     (UnresolvedFieldUsage) unresolvedMethodOwnerClassType, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
 
             // \bhĂяo(a())\bhĂяo(c())ɂĂꍇ(c().a())
         } else if (unresolvedMethodOwnerClassType instanceof UnresolvedMethodCall) {
 
             // (c)̃NX`擾
             methodOwnerClassType = NameResolver.resolveMethodCall(
                     (UnresolvedMethodCall) unresolvedMethodOwnerClassType, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
 
             // \bhĂяo(a())GeBeBgpɂĂꍇ
         } else if (unresolvedMethodOwnerClassType instanceof UnresolvedEntityUsage) {
 
             // GeBeB̃NX`擾
             methodOwnerClassType = NameResolver.resolveEntityUsage(
                     (UnresolvedEntityUsage) unresolvedMethodOwnerClassType, usingClass,
                     usingMethod, classInfoManager, fieldInfoManager, methodInfoManager,
                     resolvedCache);
 
             // \bhĂяo(a())z̗vfɂĂꍇ(d[i].a())
         } else if (unresolvedMethodOwnerClassType instanceof UnresolvedArrayElementUsage) {
 
             methodOwnerClassType = NameResolver.resolveArrayElementUsage(
                     (UnresolvedArrayElementUsage) unresolvedMethodOwnerClassType, usingClass,
                     usingMethod, classInfoManager, fieldInfoManager, methodInfoManager,
                     resolvedCache);
 
             // \bhĂяo(a())IuWFNgɂĂꍇ(a or this.a or super.a )
         } else if (unresolvedMethodOwnerClassType instanceof UnresolvedClassInfo) {
 
             methodOwnerClassType = usingClass;
 
         } else {
 
             err.println("Here shouldn't be reached!");
             return null;
         }
 
         // -----eTypeInfo ɉď𕪊
         // ełȂꍇ͂ǂ悤Ȃ
         if (null == methodOwnerClassType) {
 
             // Ȃs
             usingMethod.addUnresolvedUsage(methodCall);
 
             // ς݃LbVɓo^
             resolvedCache.put(methodCall, null);
 
             return null;
 
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
                 if (!(methodOwnerClassType instanceof TargetInnerClassInfo)
                         && (null != externalSuperClass)) {
 
                     final ExternalMethodInfo methodInfo = new ExternalMethodInfo(methodName,
                             externalSuperClass, constructor);
                     final List<ParameterInfo> parameters = NameResolver
                             .createParameters(parameterTypes);
                     methodInfo.addParameters(parameters);
 
                     usingMethod.addCallee(methodInfo);
                     methodInfo.addCaller(usingMethod);
                     methodInfoManager.add(methodInfo);
 
                     // ς݃LbVɓo^
                     resolvedCache.put(methodCall, null);
 
                     // ONXɐVKŊOϐiExternalFieldInfojǉ̂Ō^͕s
                     return null;
                 }
             }
 
             // Ȃs
             {
                 usingMethod.addUnresolvedUsage(methodCall);
 
                 // ς݃LbVɓo^
                 resolvedCache.put(methodCall, null);
 
                 return null;
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
             resolvedCache.put(methodCall, null);
 
             // ONXɐVKŊO\bh(ExternalMethodInfo)ǉ̂Ō^͕sD
             return null;
         }
 
         err.println("Here shouldn't be reached!");
         return null;
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
         final UnresolvedTypeInfo unresolvedOwnerArrayType = arrayElement.getOwnerArrayType();
 
         // vfgp([i])tB[hQ(b)ɂĂꍇ (b[i])
         if (unresolvedOwnerArrayType instanceof UnresolvedFieldUsage) {
 
             // (b)̃NX`擾
             final TypeInfo ownerArrayType = NameResolver.resolveFieldReference(
                     (UnresolvedFieldUsage) unresolvedOwnerArrayType, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
 
             // ς݃LbVɓo^
             resolvedCache.put(arrayElement, ownerArrayType);
 
             return ownerArrayType;
 
             // vfgp([i])\bhĂяo(c())ɂĂꍇ(c()[i])
         } else if (unresolvedOwnerArrayType instanceof UnresolvedMethodCall) {
 
             // (c)̃NX`擾
             final TypeInfo ownerArrayType = NameResolver.resolveMethodCall(
                     (UnresolvedMethodCall) unresolvedOwnerArrayType, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
 
             // ς݃LbVɓo^
             resolvedCache.put(arrayElement, ownerArrayType);
 
             return ownerArrayType;
 
             // vfgp([i])GeBeBgpɂĂꍇ
         } else if (unresolvedOwnerArrayType instanceof UnresolvedEntityUsage) {
 
             // GeBeB̃NX`擾
             final TypeInfo ownerArrayType = NameResolver.resolveEntityUsage(
                     (UnresolvedEntityUsage) unresolvedOwnerArrayType, usingClass, usingMethod,
                     classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
 
             // ς݃LbVɓo^
             resolvedCache.put(arrayElement, ownerArrayType);
 
             return ownerArrayType;
 
             // vfgp([i])z̗vfɂĂꍇ(d[j][i])
         } else if (unresolvedOwnerArrayType instanceof UnresolvedArrayElementUsage) {
 
             final TypeInfo ownerArrayType = NameResolver.resolveArrayElementUsage(
                    (UnresolvedArrayElementUsage) unresolvedOwnerArrayType, usingClass, usingMethod,
                    classInfoManager, fieldInfoManager, methodInfoManager, resolvedCache);
 
             // ς݃LbVɓo^
             resolvedCache.put(arrayElement, ownerArrayType);
 
             return ownerArrayType;
            
         } else {
 
             err.println("Here shouldn't be reached!");
             return null;
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
             return type;
         }
 
         // GeBeBQƖ擾
         final String[] name = entityUsage.getName();
 
         // p\ȃtB[hGeBeB
         {
             // ̃NXŗp\ȃtB[hꗗ擾
             final List<TargetFieldInfo> availableFieldsOfThisClass = NameResolver
                     .getAvailableFields(usingClass);
 
             for (TargetFieldInfo availableFieldOfThisClass : availableFieldsOfThisClass) {
 
                 // vtB[hꍇ
                 if (name[0].equals(availableFieldOfThisClass.getName())) {
                     usingMethod.addReferencee(availableFieldOfThisClass);
                     availableFieldOfThisClass.addReferencer(usingMethod);
 
                     // availableField.getType() 玟word(name[i])𖼑O
                     TypeInfo ownerTypeInfo = availableFieldOfThisClass.getType();
                     for (int i = 1; i < name.length; i++) {
 
                         // e null Cǂ悤Ȃ
                         if (null == ownerTypeInfo) {
 
                             // ς݃LbVɓo^
                             resolvedCache.put(entityUsage, null);
 
                             return ownerTypeInfo;
 
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
 
                                         ownerTypeInfo = null;
                                     }
 
                                     // Ȃs
                                     usingMethod.addUnresolvedUsage(entityUsage);
 
                                     // ς݃LbVɓo^
                                     resolvedCache.put(entityUsage, null);
 
                                     return null;
                                 }
                             }
 
                             // eONX(ExternalClassInfo)̏ꍇ
                         } else if (ownerTypeInfo instanceof ExternalClassInfo) {
 
                             final ExternalClassInfo externalSuperClass = NameResolver
                                     .getExternalSuperClass((TargetClassInfo) ownerTypeInfo);
                             if (!(ownerTypeInfo instanceof TargetInnerClassInfo)
                                     && (null != externalSuperClass)) {
 
                                 final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(name[i],
                                         externalSuperClass);
 
                                 usingMethod.addReferencee(fieldInfo);
                                 fieldInfo.addReferencer(usingMethod);
                                 fieldInfoManager.add(fieldInfo);
 
                                 ownerTypeInfo = null;
                             }
                         }
                     }
 
                     // ς݃LbVɓo^
                     resolvedCache.put(entityUsage, ownerTypeInfo);
 
                     return ownerTypeInfo;
                 }
             }
         }
 
         // p\ȃNXGeBeB
         {
 
             for (int length = 1; length <= name.length; length++) {
 
                 // 閼O(String[])쐬
                 final String[] searchingName = new String[length];
                 System.arraycopy(name, 0, searchingName, 0, length);
 
                 final ClassInfo searchingClass = classInfoManager.getClassInfo(searchingName);
                 if (null != searchingClass) {
 
                     TypeInfo ownerTypeInfo = searchingClass;
                     for (int i = length; i < name.length; i++) {
 
                         // e null Cǂ悤Ȃ
                         if (null == ownerTypeInfo) {
 
                             // ς݃LbVɓo^
                             resolvedCache.put(entityUsage, null);
 
                             return ownerTypeInfo;
 
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
 
                                         ownerTypeInfo = null;
                                     }
 
                                     // Ȃs
                                     usingMethod.addUnresolvedUsage(entityUsage);
 
                                     // ς݃LbVɓo^
                                     resolvedCache.put(entityUsage, null);
 
                                     return null;
                                 }
                             }
 
                             // eONX(ExternalClassInfo)̏ꍇ
                         } else if (ownerTypeInfo instanceof ExternalClassInfo) {
 
                             final ExternalClassInfo externalSuperClass = NameResolver
                                     .getExternalSuperClass((TargetClassInfo) ownerTypeInfo);
                             if (!(ownerTypeInfo instanceof TargetInnerClassInfo)
                                     && (null != externalSuperClass)) {
 
                                 final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(name[i],
                                         externalSuperClass);
 
                                 usingMethod.addReferencee(fieldInfo);
                                 fieldInfo.addReferencer(usingMethod);
                                 fieldInfoManager.add(fieldInfo);
 
                                 ownerTypeInfo = null;
                             }
                         }
                     }
 
                     // ς݃LbVɓo^
                     resolvedCache.put(entityUsage, ownerTypeInfo);
 
                     return ownerTypeInfo;
                 }
             }
         }
 
         // Ȃs
         usingMethod.addUnresolvedUsage(entityUsage);
 
         // ς݃LbVɓo^
         resolvedCache.put(entityUsage, null);
 
         return null;
     }
 
     /**
      * tB[h񂩂CΉFieldInfo ԂDY郁\bhȂꍇ́C IllegalArgumentException 
      * 
      * @param unresolvedFieldInfo tB[h
      * @param classInfoManager pNX}l[W
      * @return Ή FieldInfo
      */
     public static TargetFieldInfo resolveFieldInfo(final UnresolvedFieldInfo unresolvedFieldInfo,
             final ClassInfoManager classInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == unresolvedFieldInfo) || (null == classInfoManager)) {
             throw new NullPointerException();
         }
 
         // LNX擾C擾NXONXłꍇ́C̏񂪂
         final UnresolvedClassInfo unresolvedOwnerClass = unresolvedFieldInfo.getOwnerClass();
         final ClassInfo ownerClass = NameResolver.resolveClassInfo(unresolvedOwnerClass,
                 classInfoManager);
         if (!(ownerClass instanceof TargetClassInfo)) {
             throw new IllegalArgumentException(unresolvedFieldInfo.toString() + " is wrong!");
         }
 
         // UnresolvedFieldInfo tB[hC^擾
         final String fieldName = unresolvedFieldInfo.getName();
         final UnresolvedTypeInfo unresolvedFieldType = unresolvedFieldInfo.getType();
         TypeInfo fieldType = NameResolver.resolveTypeInfo(unresolvedFieldType, classInfoManager);
         if (null == fieldType) {
             fieldType = NameResolver
                     .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedFieldType);
             classInfoManager.add((ExternalClassInfo) fieldType);
         }
 
         for (TargetFieldInfo fieldInfo : ((TargetClassInfo) ownerClass).getDefinedFields()) {
 
             // tB[hႤꍇ́CYtB[hł͂Ȃ
             if (!fieldName.equals(fieldInfo.getName())) {
                 continue;
             }
 
             // tB[ȟ^Ⴄꍇ́CYtB[hł͂Ȃ
             if (!fieldType.equals(fieldInfo.getType())) {
                 continue;
             }
 
             return fieldInfo;
         }
 
         throw new IllegalArgumentException(unresolvedFieldInfo.toString() + " is wrong!");
     }
 
     /**
      * \bh񂩂CΉMethodInfo ԂDY郁\bhȂꍇ IllegalArgumentException 
      * 
      * @param unresolvedMethodInfo \bh
      * @param classInfoManager pNX}l[W
      * @return Ή MethodInfo
      */
     public static TargetMethodInfo resolveMethodInfo(
             final UnresolvedMethodInfo unresolvedMethodInfo, final ClassInfoManager classInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == unresolvedMethodInfo) || (null == classInfoManager)) {
             throw new NullPointerException();
         }
 
         // UnresolvedMethodInfo 珊LNX擾C擾NXONXłꍇ́C̏񂪂
         final UnresolvedClassInfo unresolvedOwnerClass = unresolvedMethodInfo.getOwnerClass();
         final ClassInfo ownerClass = NameResolver.resolveClassInfo(unresolvedOwnerClass,
                 classInfoManager);
         if (!(ownerClass instanceof TargetClassInfo)) {
             throw new IllegalArgumentException(unresolvedMethodInfo.toString() + " is wrong!");
         }
 
         // Unresolved \bhC擾
         final String methodName = unresolvedMethodInfo.getMethodName();
         final List<UnresolvedParameterInfo> unresolvedParameterInfos = unresolvedMethodInfo
                 .getParameterInfos();
 
         for (TargetMethodInfo methodInfo : ((TargetClassInfo) ownerClass).getDefinedMethods()) {
 
             // \bhႤꍇ́CY\bhł͂Ȃ
             if (!methodName.equals(methodInfo.getMethodName())) {
                 continue;
             }
 
             // ̐Ⴄꍇ́CY\bhł͂Ȃ
             final List<ParameterInfo> typeInfos = methodInfo.getParameters();
             if (unresolvedParameterInfos.size() != typeInfos.size()) {
                 continue;
             }
 
             // SĂ̈̌^`FbNC1łقȂꍇ́CY\bhł͂Ȃ
             final Iterator<UnresolvedParameterInfo> unresolvedParameterIterator = unresolvedParameterInfos
                     .iterator();
             final Iterator<ParameterInfo> parameterInfoIterator = typeInfos.iterator();
             boolean same = true;
             while (unresolvedParameterIterator.hasNext() && parameterInfoIterator.hasNext()) {
                 final UnresolvedParameterInfo unresolvedParameterInfo = unresolvedParameterIterator
                         .next();
                 final UnresolvedTypeInfo unresolvedTypeInfo = unresolvedParameterInfo.getType();
                 TypeInfo typeInfo = NameResolver.resolveTypeInfo(unresolvedTypeInfo,
                         classInfoManager);
                 if (null == typeInfo) {
                    typeInfo = NameResolver
                            .createExternalClassInfo((UnresolvedReferenceTypeInfo) unresolvedTypeInfo);
                    classInfoManager.add((ExternalClassInfo) typeInfo);
                 }
                 final ParameterInfo parameterInfo = parameterInfoIterator.next();
                 if (!typeInfo.equals(parameterInfo.getType())) {
                     same = false;
                     break;
                 }
             }
             if (same) {
                 return methodInfo;
             }
         }
 
         throw new IllegalArgumentException(unresolvedMethodInfo.toString() + " is wrong!");
     }
 
     /**
      * NX񂩂CY ClassInfo ԂDYNXȂꍇ IllegalArgumentException 
      * 
      * @param unresolvedClassInfo NX
      * @param classInfoManager pNX}l[W
      * @return Y ClassInfo
      */
     public static TargetClassInfo resolveClassInfo(final UnresolvedClassInfo unresolvedClassInfo,
             final ClassInfoManager classInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == unresolvedClassInfo) || (null == classInfoManager)) {
             throw new NullPointerException();
         }
 
         // S薼擾CClassInfo 擾
         final String[] fullQualifiedName = unresolvedClassInfo.getFullQualifiedName();
         final ClassInfo classInfo = classInfoManager.getClassInfo(fullQualifiedName);
 
         // UnresolvedClassInfo ̃IuWFNg registClassInfo \bhɂCSēo^ς݂̂͂Ȃ̂ŁC
         // null ԂĂꍇ́Cs
         if (null == classInfo) {
             throw new IllegalArgumentException(unresolvedClassInfo.toString() + " is wrong!");
         }
         // registClassInfoɂo^ꂽNX TargetClassInfo łׂ
         if (!(classInfo instanceof TargetClassInfo)) {
             throw new IllegalArgumentException(unresolvedClassInfo.toString() + " is wrong!");
         }
 
         return (TargetClassInfo) classInfo;
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
 
         for (ClassInfo superClassInfo : classInfo.getSuperClasses()) {
 
             if (superClassInfo instanceof ExternalClassInfo) {
                 return (ExternalClassInfo) superClassInfo;
             }
 
             NameResolver.getExternalSuperClass((TargetClassInfo) superClassInfo);
         }
 
         return null;
     }
 
     /**
      * ũ݂NXvŗp\ȃtB[hꗗԂD
      * ŁCup\ȃtB[hvƂ́Cũ݂NXvŒ`ĂtB[hCyт̐eNXŒ`ĂtB[ĥqNXANZX\ȃtB[hłD
      * p\ȃtB[h List Ɋi[ĂD Xg̐擪D揇ʂ̍tB[hi܂CNXKwɂĉʂ̃NXɒ`ĂtB[hji[ĂD
      * 
      * @param thisClass ݂̃NX
      * @return p\ȃtB[hꗗ
      */
     private static List<TargetFieldInfo> getAvailableFields(final TargetClassInfo thisClass) {
 
         if (null == thisClass) {
             throw new NullPointerException();
         }
 
         final List<TargetFieldInfo> availableFields = new LinkedList<TargetFieldInfo>();
 
         // ̃NXŒ`ĂtB[hꗗ擾
         availableFields.addAll(thisClass.getDefinedFields());
 
         // eNXŒ`ĂC̃NXANZX\ȃtB[h擾
         for (ClassInfo superClass : thisClass.getSuperClasses()) {
 
             if (superClass instanceof TargetClassInfo) {
                 final List<TargetFieldInfo> availableFieldsDefinedInSuperClasses = NameResolver
                         .getAvailableFieldsInSubClasses((TargetClassInfo) superClass);
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
     private static List<TargetMethodInfo> getAvailableMethods(final TargetClassInfo thisClass) {
 
         if (null == thisClass) {
             throw new NullPointerException();
         }
 
         final List<TargetMethodInfo> availableMethods = new LinkedList<TargetMethodInfo>();
 
         // ̃NXŒ`Ă郁\bhꗗ擾
         availableMethods.addAll(thisClass.getDefinedMethods());
 
         // eNXŒ`ĂC̃NXANZX\ȃ\bh擾
         for (ClassInfo superClass : thisClass.getSuperClasses()) {
 
             if (superClass instanceof TargetClassInfo) {
                 final List<TargetMethodInfo> availableMethodsDefinedInSuperClasses = NameResolver
                         .getAvailableMethodsInSubClasses((TargetClassInfo) superClass);
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
 
         final List<TargetFieldInfo> availableFields = new LinkedList<TargetFieldInfo>();
 
         // ̃NXŒ`ĂtB[ĥCgpNXŗp\ȃtB[h擾
         // 2̃NXOԂĂꍇ
         if (usedClass.getNamespace().equals(usingClass.getNamespace())) {
 
             for (TargetFieldInfo field : usedClass.getDefinedFields()) {
                 if (field.isNamespaceVisible()) {
                     availableFields.add(field);
                 }
             }
 
             // ႤOԂĂꍇ
         } else {
             for (TargetFieldInfo field : usedClass.getDefinedFields()) {
                 if (field.isPublicVisible()) {
                     availableFields.add(field);
                 }
             }
         }
 
         // eNXŒ`ĂCqNXANZX\ȃtB[h擾
         // List ɓ̂ŁCeNX̃tB[ȟ add Ȃ΂ȂȂ
         for (ClassInfo superClassInfo : usedClass.getSuperClasses()) {
 
             if (superClassInfo instanceof TargetClassInfo) {
                 final List<TargetFieldInfo> availableFieldsDefinedInSuperClasses = NameResolver
                         .getAvailableFieldsInSubClasses((TargetClassInfo) superClassInfo);
                 availableFields.addAll(availableFieldsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableFields);
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
 
         final List<TargetMethodInfo> availableMethods = new LinkedList<TargetMethodInfo>();
 
         // ̃NXŒ`Ă郁\bĥCgpNXŗp\ȃ\bh擾
         // 2̃NXOԂĂꍇ
         if (usedClass.getNamespace().equals(usingClass.getNamespace())) {
 
             for (TargetMethodInfo method : usedClass.getDefinedMethods()) {
                 if (method.isNamespaceVisible()) {
                     availableMethods.add(method);
                 }
             }
 
             // ႤOԂĂꍇ
         } else {
             for (TargetMethodInfo method : usedClass.getDefinedMethods()) {
                 if (method.isPublicVisible()) {
                     availableMethods.add(method);
                 }
             }
         }
 
         // eNXŒ`ĂCqNXANZX\ȃ\bh擾
         // List ɓ̂ŁCeNX̃\bȟ add Ȃ΂ȂȂ
         for (ClassInfo superClassInfo : usedClass.getSuperClasses()) {
 
             if (superClassInfo instanceof TargetClassInfo) {
                 final List<TargetMethodInfo> availableMethodsDefinedInSuperClasses = NameResolver
                         .getAvailableMethodsInSubClasses((TargetClassInfo) superClassInfo);
                 availableMethods.addAll(availableMethodsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableMethods);
     }
 
     /**
      * ugpNXv̎qNXgpꍇɁCp\ȃtB[hꗗԂD
      * ŁCup\ȃtB[hvƂ́CugpNXv͂̐eNXŒ`ĂtB[ĥCqNXANZX\ȃtB[hłD
      * qNXŗp\ȃtB[hꗗ List Ɋi[ĂD
      * Xg̐擪D揇ʂ̍tB[hi܂CNXKwɂĉʂ̃NXɒ`ĂtB[hji[ĂD
      * 
      * @param usedClass gpNX
      * @return p\ȃtB[hꗗ
      */
     private static List<TargetFieldInfo> getAvailableFieldsInSubClasses(
             final TargetClassInfo usedClass) {
 
         if (null == usedClass) {
             throw new NullPointerException();
         }
 
         final List<TargetFieldInfo> availableFields = new LinkedList<TargetFieldInfo>();
 
         // ̃NXŒ`ĂCqNXANZX\ȃtB[h擾
         for (TargetFieldInfo field : usedClass.getDefinedFields()) {
             if (field.isInheritanceVisible()) {
                 availableFields.add(field);
             }
         }
 
         // eNXŒ`ĂCqNXANZX\ȃtB[h擾
         // List ɓ̂ŁCeNX̃tB[ȟ add Ȃ΂ȂȂ
         for (ClassInfo superClassInfo : usedClass.getSuperClasses()) {
 
             if (superClassInfo instanceof TargetClassInfo) {
                 final List<TargetFieldInfo> availableFieldsDefinedInSuperClasses = NameResolver
                         .getAvailableFieldsInSubClasses((TargetClassInfo) superClassInfo);
                 availableFields.addAll(availableFieldsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableFields);
     }
 
     /**
      * ugpNXv̎qNXgpꍇɁCp\ȃ\bhꗗԂD
      * ŁCup\ȃ\bhvƂ́Cugp郁\bhv͂̐eNXŒ`Ă郁\bĥCqNXANZX\ȃ\bhłD
      * qNXŗp\ȃ\bhꗗ List Ɋi[ĂD
      * Xg̐擪D揇ʂ̍\bhi܂CNXKwɂĉʂ̃NXɒ`Ă郁\bhji[ĂD
      * 
      * @param usedClass gpNX
      * @return p\ȃ\bhꗗ
      */
     private static List<TargetMethodInfo> getAvailableMethodsInSubClasses(
             final TargetClassInfo usedClass) {
 
         if (null == usedClass) {
             throw new NullPointerException();
         }
 
         final List<TargetMethodInfo> availableMethods = new LinkedList<TargetMethodInfo>();
 
         // ̃NXŒ`ĂCqNXANZX\ȃ\bh擾
         for (TargetMethodInfo method : usedClass.getDefinedMethods()) {
             if (method.isInheritanceVisible()) {
                 availableMethods.add(method);
             }
         }
 
         // eNXŒ`ĂCqNXANZX\ȃ\bh擾
         // List ɓ̂ŁCeNX̃\bȟ add Ȃ΂ȂȂ
         for (ClassInfo superClassInfo : usedClass.getSuperClasses()) {
 
             if (superClassInfo instanceof TargetClassInfo) {
                 final List<TargetMethodInfo> availableMethodsDefinedInSuperClasses = NameResolver
                         .getAvailableMethodsInSubClasses((TargetClassInfo) superClassInfo);
                 availableMethods.addAll(availableMethodsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableMethods);
     }
 
     /**
      * o̓bZ[Wo͗p̃v^
      */
     private static final MessagePrinter out = new DefaultMessagePrinter(new MessageSource() {
         public String getMessageSourceName() {
             return "NameResolver";
         }
     }, MESSAGE_TYPE.OUT);
 
     /**
      * G[bZ[Wo͗p̃v^
      */
     private static final MessagePrinter err = new DefaultMessagePrinter(new MessageSource() {
         public String getMessageSourceName() {
             return "NameResolver";
         }
     }, MESSAGE_TYPE.ERROR);
 }
