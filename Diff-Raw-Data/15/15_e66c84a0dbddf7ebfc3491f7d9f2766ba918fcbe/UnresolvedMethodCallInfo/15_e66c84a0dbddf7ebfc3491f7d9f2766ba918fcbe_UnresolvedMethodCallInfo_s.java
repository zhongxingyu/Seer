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
 public final class UnresolvedMethodCallInfo extends UnresolvedCallInfo {
 
     /**
      * \bhĂяosϐ̌^C\bh^ăIuWFNg
      * 
      * @param ownerUsage \bhĂяosϐ̌^
      * @param methodName \bh
      */
     public UnresolvedMethodCallInfo(final UnresolvedEntityUsageInfo ownerUsage,
             final String methodName) {
 
         super();
 
         if ((null == ownerUsage) || (null == methodName)) {
             throw new NullPointerException();
         }
 
         this.ownerUsage = ownerUsage;
         this.methodName = methodName;
     }
 
     @Override
     public EntityUsageInfo resolveEntityUsage(final TargetClassInfo usingClass,
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
             return this.getResolvedEntityUsage();
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
         final UnresolvedEntityUsageInfo unresolvedOwnerUsage = this.getOwnerClassType();
         EntityUsageInfo ownerUsage = unresolvedOwnerUsage.resolveEntityUsage(usingClass,
                 usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
         assert ownerUsage != null : "resolveEntityUsage returned null!";
         if (ownerUsage instanceof UnknownEntityUsageInfo) {
             if (unresolvedOwnerUsage instanceof UnresolvedClassReferenceInfo) {
 
                 // TODO ^p[^̏i[
                 final ExternalClassInfo externalClassInfo = NameResolver
                         .createExternalClassInfo((UnresolvedClassReferenceInfo) unresolvedOwnerUsage);
                 classInfoManager.add(externalClassInfo);
                 final ClassTypeInfo referenceType = new ClassTypeInfo(externalClassInfo);
                 ownerUsage = new ClassReferenceInfo(referenceType, fromLine, fromColumn, toLine,
                         toColumn);
             }
         }
 
         // -----ě^ɉď𕪊
         // ełȂꍇ͂ǂ悤Ȃ
         if (ownerUsage.getType() instanceof UnknownTypeInfo) {
 
             // Ȃs
             usingMethod.addUnresolvedUsage(this);
 
             this.resolvedInfo = new UnknownEntityUsageInfo(fromLine, fromColumn, toLine, toColumn);
             return this.resolvedInfo;
 
             // eNX^ꍇ
         } else if (ownerUsage.getType() instanceof ClassTypeInfo) {
 
             final ClassInfo ownerClass = ((ClassTypeInfo) ownerUsage.getType())
                     .getReferencedClass();
             if (ownerClass instanceof TargetClassInfo) {
 
                 // ܂͗p\ȃ\bh猟
                 {
                     // p\ȃ\bhꗗ擾
                     final List<TargetMethodInfo> availableMethods = NameResolver
                            .getAvailableMethods((TargetClassInfo) ownerUsage.getType(), usingClass);
 
                     // p\ȃ\bhC\bhƈv̂
                     // \bhČ^̃XgpāC̃\bȟĂяoł邩ǂ𔻒
                     for (final TargetMethodInfo availableMethod : availableMethods) {
 
                         // Ăяo\ȃ\bhꍇ
                         if (availableMethod.canCalledWith(name, actualParameters)) {
                             this.resolvedInfo = new MethodCallInfo(availableMethod, fromLine,
                                     fromColumn, toLine, toColumn);
                             ((MethodCallInfo) this.resolvedInfo).addParameters(actualParameters);
                             return this.resolvedInfo;
                         }
                     }
                 }
 
                 // p\ȃ\bhȂꍇ́CONXłeNX͂D
                 // ̃NX̃\bhgpĂƂ݂Ȃ
                 {
                     final ExternalClassInfo externalSuperClass = NameResolver
                            .getExternalSuperClass((TargetClassInfo) ownerUsage.getType());
                     if (null != externalSuperClass) {
 
                         final ExternalMethodInfo methodInfo = new ExternalMethodInfo(
                                 this.getName(), externalSuperClass);
                         final List<ParameterInfo> dummyParameters = NameResolver
                                 .createParameters(actualParameters);
                         methodInfo.addParameters(dummyParameters);
                         methodInfoManager.add(methodInfo);
 
                         // ONXɐVKŊO\bhϐiExternalMethodInfojǉ̂Ō^͕s
                         this.resolvedInfo = new MethodCallInfo(methodInfo, fromLine, fromColumn,
                                 toLine, toColumn);
                         ((MethodCallInfo) this.resolvedInfo).addParameters(actualParameters);
                         return this.resolvedInfo;
                     }
 
                     assert false : "Here shouldn't be reached!";
                 }
 
                 // Ȃs
                 {
                     err.println("Can't resolve method Call : " + this.getName());
 
                     usingMethod.addUnresolvedUsage(this);
 
                     this.resolvedInfo = new UnknownEntityUsageInfo(fromLine, fromColumn, toLine,
                             toColumn);
                     return this.resolvedInfo;
                 }
 
                 // eONXiExternalClassInfojꍇ
             } else if (ownerClass instanceof ExternalClassInfo) {
 
                 final ExternalMethodInfo methodInfo = new ExternalMethodInfo(this.getName(),
                         ownerClass);
                 final List<ParameterInfo> parameters = NameResolver
                         .createParameters(actualParameters);
                 methodInfo.addParameters(parameters);
                 methodInfoManager.add(methodInfo);
 
                 // ONXɐVKŊO\bh(ExternalMethodInfo)ǉ̂Ō^͕sD
                 this.resolvedInfo = new MethodCallInfo(methodInfo, fromLine, fromColumn, toLine,
                         toColumn);
                 ((MethodCallInfo) this.resolvedInfo).addParameters(actualParameters);
                 return this.resolvedInfo;
             }
 
             // ez񂾂ꍇ
         } else if (ownerUsage.getType() instanceof ArrayTypeInfo) {
 
             // XXX Javał΁C java.lang.Object ɑ΂Ăяo
             if (Settings.getLanguage().equals(LANGUAGE.JAVA)) {
                 final ClassInfo ownerClass = classInfoManager.getClassInfo(new String[] { "java",
                         "lang", "Object" });
                 final ExternalMethodInfo methodInfo = new ExternalMethodInfo(this.getName(),
                         ownerClass);
                 final List<ParameterInfo> parameters = NameResolver
                         .createParameters(actualParameters);
                 methodInfo.addParameters(parameters);
                 methodInfoManager.add(methodInfo);
 
                 // ONXɐVKŊO\bhǉ̂Ō^͕s
                 this.resolvedInfo = new MethodCallInfo(methodInfo, fromLine, fromColumn, toLine,
                         toColumn);
                 ((MethodCallInfo) this.resolvedInfo).addParameters(actualParameters);
                 return this.resolvedInfo;
             }
 
             // ev~eBu^ꍇ
         } else if (ownerUsage.getType() instanceof PrimitiveTypeInfo) {
 
             switch (Settings.getLanguage()) {
             // Java ̏ꍇ̓I[g{NVOł̃\bhĂяo\
             // TODO Iɂ͂ switch͂ƂDȂȂ TypeConverter.getTypeConverter(LANGUAGE)邩D
             case JAVA:
                 final ExternalClassInfo wrapperClass = TypeConverter.getTypeConverter(
                         Settings.getLanguage()).getWrapperClass(
                         (PrimitiveTypeInfo) ownerUsage.getType());
                 final ExternalMethodInfo methodInfo = new ExternalMethodInfo(this.getName(),
                         wrapperClass);
                 final List<ParameterInfo> parameters = NameResolver
                         .createParameters(actualParameters);
                 methodInfo.addParameters(parameters);
                 methodInfoManager.add(methodInfo);
 
                 // ONXɐVKŊO\bh(ExternalMethodInfo)ǉ̂Ō^͕sD
                 this.resolvedInfo = new MethodCallInfo(methodInfo, fromLine, fromColumn, toLine,
                         toColumn);
                 ((MethodCallInfo) this.resolvedInfo).addParameters(actualParameters);
                 return this.resolvedInfo;
 
             default:
                 assert false : "Here shouldn't be reached!";
                 this.resolvedInfo = new UnknownEntityUsageInfo(fromLine, fromColumn, toLine,
                         toColumn);
                 return this.resolvedInfo;
             }
         }
 
         assert false : "Here shouldn't be reached!";
         this.resolvedInfo = new UnknownEntityUsageInfo(fromLine, fromColumn, toLine, toColumn);
         return this.resolvedInfo;
     }
 
     /**
      * \bhĂяosϐ̌^Ԃ
      * 
      * @return \bhĂяosϐ̌^
      */
     public UnresolvedEntityUsageInfo getOwnerClassType() {
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
     private final UnresolvedEntityUsageInfo ownerUsage;
 
 }
