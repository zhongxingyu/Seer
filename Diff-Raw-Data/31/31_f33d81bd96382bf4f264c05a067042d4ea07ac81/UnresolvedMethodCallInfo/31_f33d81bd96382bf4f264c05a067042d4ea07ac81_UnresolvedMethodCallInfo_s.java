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
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.Member;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MemberImportStatementInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodCallInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.PrimitiveTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ReferenceTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterInfo;
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
      * @param memberImportStatements ̃\bhĂяô߂ɗpłC|[g
      * @param qualifierUsage \bhĂяosϐ̌^
      * @param methodName \bh
      */
     public UnresolvedMethodCallInfo(
             final List<UnresolvedMemberImportStatementInfo> memberImportStatements,
             final UnresolvedExpressionInfo<?> qualifierUsage, final String methodName) {
 
         if ((null == memberImportStatements) && (null == qualifierUsage) || (null == methodName)) {
             throw new NullPointerException();
         }
 
         this.memberImportStatements = memberImportStatements;
         this.qualifierUsage = qualifierUsage;
         this.methodName = methodName;
     }
 
     @Override
     public MethodCallInfo resolve(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == classInfoManager) {
             throw new IllegalArgumentException();
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
             }
         }
 
         final TypeInfo qualifierType = qualifierUsage.getType();
         this.resolvedInfo = this.resolve(usingClass, usingMethod, qualifierUsage, qualifierType,
                 name, actualParameters, typeArguments, fromLine, fromColumn, toLine, toColumn,
                 classInfoManager, fieldInfoManager, methodInfoManager);
         assert null != this.resolvedInfo : "resolvedInfo must not be null!";
         return this.resolvedInfo;
     }
 
     private MethodCallInfo resolve(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ExpressionInfo qualifierUsage,
             final TypeInfo qualifierType, final String methodName,
             final List<ExpressionInfo> actualParameters,
             final List<ReferenceTypeInfo> typeArguments, final int fromLine, final int fromColumn,
             final int toLine, final int toColumn, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // ^p[^̏ꍇ͂̌p^߂
         if (qualifierType instanceof TypeParameterTypeInfo) {
 
             final TypeParameterInfo qualifierParameterType = ((TypeParameterTypeInfo) qualifierType)
                     .getReferncedTypeParameter();
 
             // extends ꍇ
             if (qualifierParameterType.hasExtendsType()) {
                 for (final TypeInfo extendsType : qualifierParameterType.getExtendsTypes()) {
                     final MethodCallInfo resolve = this.resolve(usingClass, usingMethod,
                             qualifierUsage, extendsType, methodName, actualParameters,
                             typeArguments, fromLine, fromColumn, toLine, toColumn,
                             classInfoManager, fieldInfoManager, methodInfoManager);
                     if (null != resolve) {
                         return resolve;
                     }
                 }
             }
 
             // extends Ȃꍇ
             else {
                 final ClassInfo objectClass = DataManager.getInstance().getClassInfoManager()
                         .getClassInfo(new String[] { "java", "lang", "Object" });
                 final MethodCallInfo resolve = this.resolve(usingClass, usingMethod,
                         qualifierUsage, new ClassTypeInfo(objectClass), methodName,
                         actualParameters, typeArguments, fromLine, fromColumn, toLine, toColumn,
                         classInfoManager, fieldInfoManager, methodInfoManager);
                 return resolve;
             }
         }
 
         // ełȂꍇ͂ǂ悤Ȃ
         else if (qualifierType instanceof UnknownTypeInfo) {
 
             final ExternalMethodInfo unknownMethod = new ExternalMethodInfo(methodName);
             final MethodCallInfo resolved = new MethodCallInfo(qualifierType, qualifierUsage,
                     unknownMethod, usingMethod, fromLine, fromColumn, toLine, toColumn);
             resolved.addArguments(actualParameters);
             resolved.addTypeArguments(typeArguments);
             return resolved;
 
             // eNX^ꍇ
         } else if (qualifierType instanceof ClassTypeInfo
                 || qualifierType instanceof PrimitiveTypeInfo) {
 
             final ClassInfo ownerClass;
             if (qualifierType instanceof PrimitiveTypeInfo) {
                 final Settings settings = Settings.getInstance();
                 ownerClass = TypeConverter.getTypeConverter(settings.getLanguage())
                         .getWrapperClass((PrimitiveTypeInfo) qualifierType);
             } else {
                 ownerClass = ((ClassTypeInfo) qualifierType).getReferencedClass();
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
                         if (availableMethod.canCalledWith(methodName, actualParameters)) {
                             final MethodCallInfo resolved = new MethodCallInfo(qualifierType,
                                     qualifierUsage, availableMethod, usingMethod, fromLine,
                                     fromColumn, toLine, toColumn);
                             resolved.addArguments(actualParameters);
                             resolved.addTypeArguments(typeArguments);
                             return resolved;
                         }
                     }
                 }
 
                 // X^eBbNC|[gĂ郁\bhT
                 {
                     for (final UnresolvedMemberImportStatementInfo unresolvedMemberImportStatement : this
                             .getImportStatements()) {
                         final MemberImportStatementInfo memberImportStatement = unresolvedMemberImportStatement
                                 .resolve(usingClass, usingMethod, classInfoManager,
                                         fieldInfoManager, methodInfoManager);
                         for (final Member importedMember : memberImportStatement
                                 .getImportedMembers()) {
                             if (importedMember instanceof MethodInfo) {
                                 final MethodInfo importedMethod = (MethodInfo) importedMember;
 
                                 // Ăяo\ȃ\bhꍇ
                                 if (importedMethod.canCalledWith(methodName, actualParameters)) {
                                     final ClassInfo classInfo = importedMethod.getOwnerClass();
                                     final ClassTypeInfo classType = new ClassTypeInfo(classInfo);
                                     final ClassReferenceInfo classReference = new ClassReferenceInfo(
                                             classType, usingMethod, fromLine, fromColumn, fromLine,
                                             fromColumn);
                                     final MethodCallInfo resolved = new MethodCallInfo(classType,
                                             classReference, importedMethod, usingMethod, fromLine,
                                             fromColumn, toLine, toColumn);
                                     resolved.addArguments(actualParameters);
                                     resolved.addTypeArguments(typeArguments);
                                     return resolved;
                                 }
                             }
                         }
                     }
                 }
 
                 // p\ȃ\bhȂꍇ́CONXłeNX͂D
                 // ̃NX̃\bhgpĂƂ݂Ȃ
                 {
                     final ExternalClassInfo externalSuperClass = NameResolver
                             .getExternalSuperClass(ownerClass);
                     if (null != externalSuperClass) {
 
                         final ExternalMethodInfo methodInfo = new ExternalMethodInfo(this.getName());
                         methodInfo.setOuterUnit(externalSuperClass);
                         final List<ParameterInfo> dummyParameters = ExternalParameterInfo
                                 .createParameters(actualParameters, methodInfo);
                         methodInfo.addParameters(dummyParameters);
                         methodInfoManager.add(methodInfo);
 
                         // ONXɐVKŊO\bhϐiExternalMethodInfojǉ̂Ō^͕s
                         final MethodCallInfo resolved = new MethodCallInfo(qualifierType,
                                 qualifierUsage, methodInfo, usingMethod, fromLine, fromColumn,
                                 toLine, toColumn);
                         resolved.addArguments(actualParameters);
                         resolved.addTypeArguments(typeArguments);
                         return resolved;
                     }
                 }
 
                 // Ȃs
                 {
                     err.println("Resolved as an external element, \"" + this.getName() + "\""
                             + " line:" + this.getFromLine() + " column:" + this.getFromColumn()
                             + " on \"" + usingClass.getOwnerFile().getName() + "\"");
 
                     final ExternalMethodInfo unknownMethod = new ExternalMethodInfo(methodName);
                     final MethodCallInfo resolved = new MethodCallInfo(qualifierType,
                             qualifierUsage, unknownMethod, usingMethod, fromLine, fromColumn,
                             toLine, toColumn);
                     resolved.addArguments(actualParameters);
                     resolved.addTypeArguments(typeArguments);
                     return resolved;
                 }
 
                 // eONXiExternalClassInfojꍇ
             } else if (ownerClass instanceof ExternalClassInfo) {
 
                 final ExternalMethodInfo methodInfo = new ExternalMethodInfo(this.getName());
                 methodInfo.setOuterUnit(ownerClass);
                 final List<ParameterInfo> parameters = ExternalParameterInfo.createParameters(
                         actualParameters, methodInfo);
                 methodInfo.addParameters(parameters);
                 methodInfoManager.add(methodInfo);
 
                 // ONXɐVKŊO\bh(ExternalMethodInfo)ǉ̂Ō^͕sD
                 final MethodCallInfo resolved = new MethodCallInfo(qualifierType, qualifierUsage,
                         methodInfo, usingMethod, fromLine, fromColumn, toLine, toColumn);
                 resolved.addArguments(actualParameters);
                 resolved.addTypeArguments(typeArguments);
                 return resolved;
             }
 
             // ez񂾂ꍇ
         } else if (qualifierType instanceof ArrayTypeInfo) {
 
             // XXX Javał΁C java.lang.Object ɑ΂Ăяo
             final Settings settings = Settings.getInstance();
             if (settings.getLanguage().equals(LANGUAGE.JAVA15)
                     || settings.getLanguage().equals(LANGUAGE.JAVA14)
                     || settings.getLanguage().equals(LANGUAGE.JAVA13)) {
                 final ClassInfo ownerClass = classInfoManager.getClassInfo(new String[] { "java",
                         "lang", "Object" });
 
                 if (ownerClass instanceof ExternalClassInfo) {
                     final ExternalMethodInfo methodInfo = new ExternalMethodInfo(this.getName());
                     methodInfo.setOuterUnit(ownerClass);
                     final List<ParameterInfo> parameters = ExternalParameterInfo.createParameters(
                             actualParameters, methodInfo);
                     methodInfo.addParameters(parameters);
                     methodInfoManager.add(methodInfo);
 
                     // ONXɐVKŊO\bhǉ̂Ō^͕s
                     final MethodCallInfo resolved = new MethodCallInfo(qualifierType,
                             qualifierUsage, methodInfo, usingMethod, fromLine, fromColumn, toLine,
                             toColumn);
                     resolved.addArguments(actualParameters);
                     resolved.addTypeArguments(typeArguments);
                     return resolved;
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
                         if (availableMethod.canCalledWith(methodName, actualParameters)) {
                             final MethodCallInfo resolved = new MethodCallInfo(qualifierType,
                                     qualifierUsage, availableMethod, usingMethod, fromLine,
                                     fromColumn, toLine, toColumn);
                             resolved.addArguments(actualParameters);
                             resolved.addTypeArguments(typeArguments);
                             return resolved;
                         }
                     }
                 }
             }
         }
 
         return null;
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
 
     public List<UnresolvedMemberImportStatementInfo> getImportStatements() {
         return this.memberImportStatements;
     }
 
     /**
      * \bhĂяosϐ̎QƂۑ邽߂̕ϐ
      */
     private final UnresolvedExpressionInfo<?> qualifierUsage;
 
     private final List<UnresolvedMemberImportStatementInfo> memberImportStatements;
 }
