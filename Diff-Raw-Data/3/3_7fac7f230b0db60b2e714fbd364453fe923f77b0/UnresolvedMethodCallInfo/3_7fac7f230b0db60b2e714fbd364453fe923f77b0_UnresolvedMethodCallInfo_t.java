 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.List;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.Settings;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ArrayTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassReferenceInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.EntityUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodCallInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.PrimitiveTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnknownEntityUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnknownTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.LANGUAGE;
 
 
 /**
  * \bhĂяoۑ邽߂̃NX
  * 
  * @author higo
  * 
  */
 public final class UnresolvedMethodCallInfo extends UnresolvedCallInfo<MethodCallInfo> {
 
     /**
      * \bhĂяosϐ̌^C\bh^ăIuWFNg
      * 
      * @param ownerUsage \bhĂяosϐ̌^
      * @param methodName \bh
      */
     public UnresolvedMethodCallInfo(final UnresolvedEntityUsageInfo<?> ownerUsage,
             final String methodName) {
 
         if ((null == ownerUsage) || (null == methodName)) {
             throw new NullPointerException();
         }
 
         this.ownerUsage = ownerUsage;
         this.methodName = methodName;
     }
 
     @Override
     public MethodCallInfo resolve(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == usingClass) || (null == usingMethod) || (null == classInfoManager)
                 || (null == methodInfoManager)) {
             throw new NullPointerException();
         }
 
         // ɉς݂łꍇ́CLbVԂ
         if (this.alreadyResolved()) {
             return this.getResolved();
         }
 
         // gpʒu擾
         final int fromLine = this.getFromLine();
         final int fromColumn = this.getFromColumn();
         final int toLine = this.getToLine();
         final int toColumn = this.getToColumn();
 
         // \bh̃VOl`擾
         final String name = this.getName();
         final List<EntityUsageInfo> actualParameters = super.resolveParameters(usingClass,
                 usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
 
         // ě^
         final UnresolvedEntityUsageInfo<?> unresolvedOwnerUsage = this.getOwnerClassType();
         EntityUsageInfo ownerUsage = unresolvedOwnerUsage.resolve(usingClass, usingMethod,
                 classInfoManager, fieldInfoManager, methodInfoManager);
         assert ownerUsage != null : "resolveEntityUsage returned null!";
         if (ownerUsage instanceof UnknownEntityUsageInfo) {
             if (unresolvedOwnerUsage instanceof UnresolvedClassReferenceInfo) {
 
                 final ExternalClassInfo externalClassInfo = NameResolver
                         .createExternalClassInfo((UnresolvedClassReferenceInfo) unresolvedOwnerUsage);
                 classInfoManager.add(externalClassInfo);
                 final ClassTypeInfo referenceType = new ClassTypeInfo(externalClassInfo);
                 for (final UnresolvedTypeInfo unresolvedTypeArgument : ((UnresolvedClassReferenceInfo) unresolvedOwnerUsage)
                         .getTypeArguments()) {
                     final TypeInfo typeArgument = unresolvedTypeArgument.resolve(usingClass,
                             usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
                     referenceType.addTypeArgument(typeArgument);
                 }
                 ownerUsage = new ClassReferenceInfo(referenceType, fromLine, fromColumn, toLine,
                         toColumn);
             }
         }
 
         // -----ě^ɉď𕪊
         TypeInfo ownerType = ownerUsage.getType();
 
         // ^p[^̏ꍇ͂̌p^߂
         if (ownerType instanceof TypeParameterInfo) {
             final TypeInfo extendsType = ((TypeParameterInfo) ownerType).getExtendsType();
             if (null != extendsType) {
                 ownerType = extendsType;
             } else {
                 assert false : "Here should not be reached";
                 final ExternalMethodInfo unknownMethod = new ExternalMethodInfo(name);
                 this.resolvedInfo = new MethodCallInfo(ownerType, unknownMethod, fromLine, fromColumn,
                         toLine, toColumn);
                this.resolvedInfo.addParameters(actualParameters);
                 return this.resolvedInfo;
             }
         }
 
         // ełȂꍇ͂ǂ悤Ȃ
         if (ownerType instanceof UnknownTypeInfo) {
 
             final ExternalMethodInfo unknownMethod = new ExternalMethodInfo(name);
             this.resolvedInfo = new MethodCallInfo(ownerType, unknownMethod, fromLine, fromColumn,
                     toLine, toColumn);
            this.resolvedInfo.addParameters(actualParameters);
             return this.resolvedInfo;
 
             // eNX^ꍇ
         } else if (ownerType instanceof ClassTypeInfo) {
 
             final ClassInfo ownerClass = ((ClassTypeInfo) ownerType).getReferencedClass();
             if (ownerClass instanceof TargetClassInfo) {
 
                 // ܂͗p\ȃ\bh猟
                 {
                     // p\ȃ\bhꗗ擾
                     final List<TargetMethodInfo> availableMethods = NameResolver
                             .getAvailableMethods((TargetClassInfo) ownerClass, usingClass);
 
                     // p\ȃ\bhC\bhƈv̂
                     // \bhČ^̃XgpāC̃\bȟĂяoł邩ǂ𔻒
                     for (final TargetMethodInfo availableMethod : availableMethods) {
 
                         // Ăяo\ȃ\bhꍇ
                         if (availableMethod.canCalledWith(name, actualParameters)) {
                             this.resolvedInfo = new MethodCallInfo(ownerType, availableMethod,
                                     fromLine, fromColumn, toLine, toColumn);
                             this.resolvedInfo.addParameters(actualParameters);
                             return this.resolvedInfo;
                         }
                     }
                 }
 
                 // p\ȃ\bhȂꍇ́CONXłeNX͂D
                 // ̃NX̃\bhgpĂƂ݂Ȃ
                 {
                     final ExternalClassInfo externalSuperClass = NameResolver
                             .getExternalSuperClass((TargetClassInfo) ownerClass);
                     if (null != externalSuperClass) {
 
                         final ExternalMethodInfo methodInfo = new ExternalMethodInfo(
                                 this.getName(), externalSuperClass);
                         final List<ParameterInfo> dummyParameters = NameResolver.createParameters(
                                 actualParameters, methodInfo);
                         methodInfo.addParameters(dummyParameters);
                         methodInfoManager.add(methodInfo);
 
                         // ONXɐVKŊO\bhϐiExternalMethodInfojǉ̂Ō^͕s
                         this.resolvedInfo = new MethodCallInfo(ownerType, methodInfo, fromLine,
                                 fromColumn, toLine, toColumn);
                         this.resolvedInfo.addParameters(actualParameters);
                         return this.resolvedInfo;
                     }
 
                     assert false : "Here shouldn't be reached!";
                 }
 
                 // Ȃs
                 {
                     err.println("Can't resolve method Call : " + this.getName());
 
                     final ExternalMethodInfo unknownMethod = new ExternalMethodInfo(name);
                     this.resolvedInfo = new MethodCallInfo(ownerType, unknownMethod, fromLine,
                             fromColumn, toLine, toColumn);
                     return this.resolvedInfo;
                 }
 
                 // eONXiExternalClassInfojꍇ
             } else if (ownerClass instanceof ExternalClassInfo) {
 
                 final ExternalMethodInfo methodInfo = new ExternalMethodInfo(this.getName(),
                         ownerClass);
                 final List<ParameterInfo> parameters = NameResolver.createParameters(
                         actualParameters, methodInfo);
                 methodInfo.addParameters(parameters);
                 methodInfoManager.add(methodInfo);
 
                 // ONXɐVKŊO\bh(ExternalMethodInfo)ǉ̂Ō^͕sD
                 this.resolvedInfo = new MethodCallInfo(ownerType, methodInfo, fromLine, fromColumn,
                         toLine, toColumn);
                 this.resolvedInfo.addParameters(actualParameters);
                 return this.resolvedInfo;
             }
 
             // ez񂾂ꍇ
         } else if (ownerType instanceof ArrayTypeInfo) {
 
             // XXX Javał΁C java.lang.Object ɑ΂Ăяo
             if (Settings.getLanguage().equals(LANGUAGE.JAVA)) {
                 final ClassInfo ownerClass = classInfoManager.getClassInfo(new String[] { "java",
                         "lang", "Object" });
                 final ExternalMethodInfo methodInfo = new ExternalMethodInfo(this.getName(),
                         ownerClass);
                 final List<ParameterInfo> parameters = NameResolver.createParameters(
                         actualParameters, methodInfo);
                 methodInfo.addParameters(parameters);
                 methodInfoManager.add(methodInfo);
 
                 // ONXɐVKŊO\bhǉ̂Ō^͕s
                 this.resolvedInfo = new MethodCallInfo(ownerType, methodInfo, fromLine, fromColumn,
                         toLine, toColumn);
                 this.resolvedInfo.addParameters(actualParameters);
                 return this.resolvedInfo;
             }
 
             // ev~eBu^ꍇ
         } else if (ownerType instanceof PrimitiveTypeInfo) {
 
             switch (Settings.getLanguage()) {
             // Java ̏ꍇ̓I[g{NVOł̃\bhĂяo\
             // TODO Iɂ͂ switch͂ƂDȂȂ TypeConverter.getTypeConverter(LANGUAGE)邩D
             case JAVA:
                 final ExternalClassInfo wrapperClass = TypeConverter.getTypeConverter(
                         Settings.getLanguage()).getWrapperClass((PrimitiveTypeInfo) ownerType);
                 final ExternalMethodInfo methodInfo = new ExternalMethodInfo(this.getName(),
                         wrapperClass);
                 final List<ParameterInfo> parameters = NameResolver.createParameters(
                         actualParameters, methodInfo);
                 methodInfo.addParameters(parameters);
                 methodInfoManager.add(methodInfo);
 
                 // ONXɐVKŊO\bh(ExternalMethodInfo)ǉ̂Ō^͕sD
                 this.resolvedInfo = new MethodCallInfo(ownerType, methodInfo, fromLine, fromColumn,
                         toLine, toColumn);
                 this.resolvedInfo.addParameters(actualParameters);
                 return this.resolvedInfo;
 
             default:
                 assert false : "Here shouldn't be reached!";
                 final ExternalMethodInfo unknownMethod = new ExternalMethodInfo(name);
                 this.resolvedInfo = new MethodCallInfo(ownerType, unknownMethod, fromLine, fromColumn,
                         toLine, toColumn);
                 return this.resolvedInfo;
             }
         }
 
         assert false : "Here shouldn't be reached!";
         final ExternalMethodInfo unknownMethod = new ExternalMethodInfo(name);
         this.resolvedInfo = new MethodCallInfo(ownerType, unknownMethod, fromLine, fromColumn, toLine,
                 toColumn);
         return this.resolvedInfo;
     }
 
     /**
      * \bhĂяosϐ̌^Ԃ
      * 
      * @return \bhĂяosϐ̌^
      */
     public UnresolvedEntityUsageInfo<?> getOwnerClassType() {
         return this.ownerUsage;
     }
 
     /**
      * \bhԂ
      * 
      * @return \bh
      */
     public final String getName() {
         return this.methodName;
     }
 
     /**
      * \bhۑ邽߂̕ϐ
      */
     protected String methodName;
 
     /**
      * \bhĂяosϐ̎QƂۑ邽߂̕ϐ
      */
     private final UnresolvedEntityUsageInfo<?> ownerUsage;
 
 }
