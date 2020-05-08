 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.LinkedList;
 import java.util.List;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.Settings;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.DataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ArrayTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassReferenceInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExpressionInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExternalMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExternalParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodCallInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.PrimitiveTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ReferenceTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnknownEntityUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnknownTypeInfo;
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
      * @param qualifierUsage \bhĂяosϐ̌^
      * @param methodName \bh
      */
     public UnresolvedMethodCallInfo(final UnresolvedExpressionInfo<?> qualifierUsage,
             final String methodName) {
 
         if ((null == qualifierUsage) || (null == methodName)) {
             throw new NullPointerException();
         }
 
         this.qualifierUsage = qualifierUsage;
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
         final List<ExpressionInfo> actualParameters = super.resolveArguments(usingClass,
                 usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
         final List<ReferenceTypeInfo> typeArguments = super.resolveTypeArguments(usingClass,
                 usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
 
         // \bhĂяoĂ^("."̑Ô)
         final UnresolvedExpressionInfo<?> unresolvedQualifierUsage = this.getQualifierType();
         ExpressionInfo qualifierUsage = unresolvedQualifierUsage.resolve(usingClass, usingMethod,
                 classInfoManager, fieldInfoManager, methodInfoManager);
         assert qualifierUsage != null : "resolveEntityUsage returned null!";
 
         /*// vfgp̃I[i[vfԂ
         final UnresolvedExecutableElementInfo<?> unresolvedOwnerExecutableElement = this
                 .getOwnerExecutableElement();
         final ExecutableElementInfo ownerExecutableElement = unresolvedOwnerExecutableElement
                 .resolve(usingClass, usingMethod, classInfoManager, fieldInfoManager,
                         methodInfoManager);*/
 
         if (qualifierUsage instanceof UnknownEntityUsageInfo) {
             if (unresolvedQualifierUsage instanceof UnresolvedClassReferenceInfo) {
 
                 final ExternalClassInfo externalClassInfo = UnresolvedClassReferenceInfo
                         .createExternalClassInfo((UnresolvedClassReferenceInfo) unresolvedQualifierUsage);
                 classInfoManager.add(externalClassInfo);
                 final ClassTypeInfo referenceType = new ClassTypeInfo(externalClassInfo);
                 for (final UnresolvedTypeInfo<?> unresolvedTypeArgument : ((UnresolvedClassReferenceInfo) unresolvedQualifierUsage)
                         .getTypeArguments()) {
                     final TypeInfo typeArgument = unresolvedTypeArgument.resolve(usingClass,
                             usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
                     referenceType.addTypeArgument(typeArgument);
                 }
                 qualifierUsage = new ClassReferenceInfo(referenceType, usingMethod, fromLine,
                         fromColumn, toLine, toColumn);
                 /*qualifierUsage.setOwnerExecutableElement(ownerExecutableElement);*/
             }
         }
 
         // -----ě^ɉď𕪊
         TypeInfo ownerType = qualifierUsage.getType();
 
         // ^p[^̏ꍇ͂̌p^߂
         if (ownerType instanceof TypeParameterTypeInfo) {
             final TypeInfo extendsType = ((TypeParameterTypeInfo) ownerType)
                     .getReferncedTypeParameter().getExtendsType();
 
             // ȂɂpĂȂꍇObject^pĂ邱Ƃɂ
             if (null != extendsType) {
                 ownerType = extendsType;
             } else {
 
                 final ClassInfo objectClass = DataManager.getInstance().getClassInfoManager()
                        .getClassInfo(new String[0]);
                 ownerType = new ClassTypeInfo(objectClass);
             }
         }
 
         // ełȂꍇ͂ǂ悤Ȃ
         if (ownerType instanceof UnknownTypeInfo) {
 
             final ExternalMethodInfo unknownMethod = new ExternalMethodInfo(name);
             this.resolvedInfo = new MethodCallInfo(ownerType, qualifierUsage, unknownMethod,
                     usingMethod, fromLine, fromColumn, toLine, toColumn);
             /*this.resolvedInfo.setOwnerExecutableElement(ownerExecutableElement);*/
             this.resolvedInfo.addArguments(actualParameters);
             this.resolvedInfo.addTypeArguments(typeArguments);
             return this.resolvedInfo;
 
             // eNX^ꍇ
         } else if (ownerType instanceof ClassTypeInfo || ownerType instanceof PrimitiveTypeInfo) {
 
             final ClassInfo ownerClass;
             if (ownerType instanceof PrimitiveTypeInfo) {
                 final Settings settings = Settings.getInstance();
                 ownerClass = TypeConverter.getTypeConverter(settings.getLanguage())
                         .getWrapperClass((PrimitiveTypeInfo) ownerType);
             } else {
                 ownerClass = ((ClassTypeInfo) ownerType).getReferencedClass();
             }
 
             if (ownerClass instanceof TargetClassInfo) {
 
                 // ܂͗p\ȃ\bh猟
                 {
                     // p\ȃ\bhꗗ擾
                     final List<MethodInfo> availableMethods = NameResolver.getAvailableMethods(
                             (TargetClassInfo) ownerClass, usingClass);
 
                     // p\ȃ\bhC\bhƈv̂
                     // \bhČ^̃XgpāC̃\bȟĂяoł邩ǂ𔻒
                     for (final MethodInfo availableMethod : availableMethods) {
 
                         // Ăяo\ȃ\bhꍇ
                         if (availableMethod.canCalledWith(name, actualParameters)) {
                             this.resolvedInfo = new MethodCallInfo(ownerType, qualifierUsage,
                                     availableMethod, usingMethod, fromLine, fromColumn, toLine,
                                     toColumn);
                             /*this.resolvedInfo.setOwnerExecutableElement(ownerExecutableElement);*/
                             this.resolvedInfo.addArguments(actualParameters);
                             this.resolvedInfo.addTypeArguments(typeArguments);
                             return this.resolvedInfo;
                         }
                     }
                 }
 
                 // p\ȃ\bhȂꍇ́CONXłeNX͂D
                 // ̃NX̃\bhgpĂƂ݂Ȃ
                 {
                     final ExternalClassInfo externalSuperClass = NameResolver
                             .getExternalSuperClass(ownerClass);
                     if (null != externalSuperClass) {
 
                         final ExternalMethodInfo methodInfo = new ExternalMethodInfo(
                                 this.getName(), externalSuperClass);
                         final List<ParameterInfo> dummyParameters = ExternalParameterInfo
                                 .createParameters(actualParameters, methodInfo);
                         methodInfo.addParameters(dummyParameters);
                         methodInfoManager.add(methodInfo);
 
                         // ONXɐVKŊO\bhϐiExternalMethodInfojǉ̂Ō^͕s
                         this.resolvedInfo = new MethodCallInfo(ownerType, qualifierUsage,
                                 methodInfo, usingMethod, fromLine, fromColumn, toLine, toColumn);
                         /*this.resolvedInfo.setOwnerExecutableElement(ownerExecutableElement);*/
                         this.resolvedInfo.addArguments(actualParameters);
                         this.resolvedInfo.addTypeArguments(typeArguments);
                         return this.resolvedInfo;
                     }
 
                     //assert false : "Here shouldn't be reached!";
                 }
 
                 // Ȃs
                 {
                     err.println("Remain unresolved \"" + this.getName() + "\"" + " line:"
                             + this.getFromLine() + " column:" + this.getFromColumn() + " on \""
                             + usingClass.getOwnerFile().getName());
 
                     final ExternalMethodInfo unknownMethod = new ExternalMethodInfo(name);
                     this.resolvedInfo = new MethodCallInfo(ownerType, qualifierUsage,
                             unknownMethod, usingMethod, fromLine, fromColumn, toLine, toColumn);
                     /*this.resolvedInfo.setOwnerExecutableElement(ownerExecutableElement);*/
                     return this.resolvedInfo;
                 }
 
                 // eONXiExternalClassInfojꍇ
             } else if (ownerClass instanceof ExternalClassInfo) {
 
                 final ExternalMethodInfo methodInfo = new ExternalMethodInfo(this.getName(),
                         (ExternalClassInfo) ownerClass);
                 final List<ParameterInfo> parameters = ExternalParameterInfo.createParameters(
                         actualParameters, methodInfo);
                 methodInfo.addParameters(parameters);
                 methodInfoManager.add(methodInfo);
 
                 // ONXɐVKŊO\bh(ExternalMethodInfo)ǉ̂Ō^͕sD
                 this.resolvedInfo = new MethodCallInfo(ownerType, qualifierUsage, methodInfo,
                         usingMethod, fromLine, fromColumn, toLine, toColumn);
                 /*this.resolvedInfo.setOwnerExecutableElement(ownerExecutableElement);*/
                 this.resolvedInfo.addArguments(actualParameters);
                 this.resolvedInfo.addTypeArguments(typeArguments);
                 return this.resolvedInfo;
             }
 
             // ez񂾂ꍇ
         } else if (ownerType instanceof ArrayTypeInfo) {
 
             // XXX Javał΁C java.lang.Object ɑ΂Ăяo
             final Settings settings = Settings.getInstance();
             if (settings.getLanguage().equals(LANGUAGE.JAVA15)
                     || settings.getLanguage().equals(LANGUAGE.JAVA14)
                     || settings.getLanguage().equals(LANGUAGE.JAVA13)) {
                 final ClassInfo ownerClass = classInfoManager.getClassInfo(new String[] { "java",
                         "lang", "Object" });
 
                 if (ownerClass instanceof ExternalClassInfo) {
                     final ExternalMethodInfo methodInfo = new ExternalMethodInfo(this.getName(),
                             (ExternalClassInfo) ownerClass);
                     final List<ParameterInfo> parameters = ExternalParameterInfo.createParameters(
                             actualParameters, methodInfo);
                     methodInfo.addParameters(parameters);
                     methodInfoManager.add(methodInfo);
 
                     // ONXɐVKŊO\bhǉ̂Ō^͕s
                     this.resolvedInfo = new MethodCallInfo(ownerType, qualifierUsage, methodInfo,
                             usingMethod, fromLine, fromColumn, toLine, toColumn);
                     /*this.resolvedInfo.setOwnerExecutableElement(ownerExecutableElement);*/
                     this.resolvedInfo.addArguments(actualParameters);
                     this.resolvedInfo.addTypeArguments(typeArguments);
                     return this.resolvedInfo;
                 }
 
                 else if (ownerClass instanceof TargetClassInfo) {
 
                     // p\ȃ\bhꗗ擾, NameResolver.getAvailableMethod͂Ă͂߁D
                     //@ȂȂC̃ReLXgł͉CqɊ֌WȂCׂẴ\bhp\
                     final List<MethodInfo> availableMethods = new LinkedList<MethodInfo>();
                     availableMethods.addAll(((TargetClassInfo) ownerClass).getDefinedMethods());
 
                     // p\ȃ\bhC\bhƈv̂
                     // \bhČ^̃XgpāC̃\bȟĂяoł邩ǂ𔻒
                     for (final MethodInfo availableMethod : availableMethods) {
 
                         // Ăяo\ȃ\bhꍇ
                         if (availableMethod.canCalledWith(name, actualParameters)) {
                             this.resolvedInfo = new MethodCallInfo(ownerType, qualifierUsage,
                                     availableMethod, usingMethod, fromLine, fromColumn, toLine,
                                     toColumn);
                             this.resolvedInfo.addArguments(actualParameters);
                             this.resolvedInfo.addTypeArguments(typeArguments);
                             return this.resolvedInfo;
                         }
                     }
                 }
             }
         }
 
         assert false : "Here shouldn't be reached!";
         final ExternalMethodInfo unknownMethod = new ExternalMethodInfo(name);
         this.resolvedInfo = new MethodCallInfo(ownerType, qualifierUsage, unknownMethod,
                 usingMethod, fromLine, fromColumn, toLine, toColumn);
         /*this.resolvedInfo.setOwnerExecutableElement(ownerExecutableElement);*/
         return this.resolvedInfo;
     }
 
     /**
      * \bhĂяosϐ̌^Ԃ
      * 
      * @return \bhĂяosϐ̌^
      */
     public UnresolvedExpressionInfo<?> getQualifierType() {
         return this.qualifierUsage;
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
     private final UnresolvedExpressionInfo<?> qualifierUsage;
 
 }
