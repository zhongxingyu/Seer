 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.Settings;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.MetricMeasurable;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 public abstract class MethodInfo extends CallableUnitInfo implements Comparable<MethodInfo>,
         MetricMeasurable {
 
     /**
      * \bhIuWFNg
      * 
      * @param modifiers CqSet
      * @param methodName \bh
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public MethodInfo(final Set<ModifierInfo> modifiers, final String methodName,
             final ClassInfo ownerClass, final boolean privateVisible,
             final boolean namespaceVisible, final boolean inheritanceVisible,
             final boolean publicVisible, final int fromLine, final int fromColumn,
             final int toLine, final int toColumn) {
 
         super(modifiers, ownerClass, privateVisible, namespaceVisible, inheritanceVisible,
                 publicVisible, fromLine, fromColumn, toLine, toColumn);
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == methodName) || (null == ownerClass)) {
             throw new NullPointerException();
         }
 
         this.methodName = methodName;
         this.returnType = null;
 
         this.parameters = new LinkedList<ParameterInfo>();
 
         this.callers = new TreeSet<CallableUnitInfo>();
         this.overridees = new TreeSet<MethodInfo>();
         this.overriders = new TreeSet<MethodInfo>();
     }
 
     /**
      * \bhԂ̏֌W`郁\bhDȉ̏ŏ߂D
      * <ol>
      * <li>\bh`ĂNX̖OԖ</li>
      * <li>\bh`ĂNX̃NX</li>
      * <li>\bh</li>
      * <li>\bḧ̌</li>
      * <li>\bḧ̌^i珇ԂɁj</li>
      */
     @Override
     public final int compareTo(final MethodInfo method) {
 
         if (null == method) {
             throw new NullPointerException();
         }
 
         // NXIuWFNg compareTo pD
         // NX̖OԖCNXrɗpĂD
         final ClassInfo ownerClass = this.getOwnerClass();
         final ClassInfo correspondOwnerClass = method.getOwnerClass();
         final int classOrder = ownerClass.compareTo(correspondOwnerClass);
         if (classOrder != 0) {
             return classOrder;
         }
 
         // \bhŔr
         final String name = this.getMethodName();
         final String correspondName = method.getMethodName();
         final int methodNameOrder = name.compareTo(correspondName);
         if (methodNameOrder != 0) {
             return methodNameOrder;
         }
 
         // ̌Ŕr
         final int parameterNumber = this.getParameterNumber();
         final int correspondParameterNumber = method.getParameterNumber();
         if (parameterNumber < correspondParameterNumber) {
             return 1;
         } else if (parameterNumber > correspondParameterNumber) {
             return -1;
         } else {
 
             // ̌^ŔrD珇ԂɁD
             final Iterator<ParameterInfo> parameterIterator = this.getParameters().iterator();
             final Iterator<ParameterInfo> correspondParameterIterator = method.getParameters()
                     .iterator();
             while (parameterIterator.hasNext() && correspondParameterIterator.hasNext()) {
                 final ParameterInfo parameter = parameterIterator.next();
                 final ParameterInfo correspondParameter = correspondParameterIterator.next();
                 final String typeName = parameter.getName();
                 final String correspondTypeName = correspondParameter.getName();
                 final int typeOrder = typeName.compareTo(correspondTypeName);
                 if (typeOrder != 0) {
                     return typeOrder;
                 }
             }
 
             return 0;
         }
     }
 
     /**
      * ̃\bhCŗ^ꂽgČĂяoƂł邩ǂ𔻒肷D
      * 
      * @param methodName \bh
      * @param actualParameterTypes ̌^̃Xg
      * @return Ăяoꍇ trueCłȂꍇ false
      */
     public final boolean canCalledWith(final String methodName,
             final List<EntityUsageInfo> actualParameters) {
 
         if ((null == methodName) || (null == actualParameters)) {
             throw new NullPointerException();
         }
 
         // \bhȂꍇ͊YȂ
         if (!methodName.equals(this.getMethodName())) {
             return false;
         }
 
         // ̐Ȃꍇ͊YȂ
         final List<ParameterInfo> dummyParameters = this.getParameters();
         if (dummyParameters.size() != actualParameters.size()) {
             return false;
         }
 
         // ̌^擪`FbNȂꍇ͊YȂ
         final Iterator<ParameterInfo> dummyParameterIterator = dummyParameters.iterator();
         final Iterator<EntityUsageInfo> actualParameterIterator = actualParameters.iterator();
         NEXT_PARAMETER: while (dummyParameterIterator.hasNext()
                 && actualParameterIterator.hasNext()) {
             final ParameterInfo dummyParameter = dummyParameterIterator.next();
             final EntityUsageInfo actualParameter = actualParameterIterator.next();
 
            TypeInfo actualParameterType = actualParameter.getType();

            // ^p[^̏ꍇ͂̌p^߂
            if (actualParameterType instanceof TypeParameterInfo) {
                final TypeInfo extendsType = ((TypeParameterInfo) actualParameterType)
                        .getExtendsType();
                if (null != extendsType) {
                    actualParameterType = extendsType;
                } else {
                    assert false : "Here should not be reached";
                }
            }

             // Qƌ^̏ꍇ
            if (actualParameterType instanceof ClassTypeInfo) {
 
                 // ̌^̃NX擾
                final ClassInfo actualParameterClass = ((ClassTypeInfo) actualParameterType)
                         .getReferencedClass();
 
                 // Qƌ^łȂꍇ͊YȂ
                 if (!(dummyParameter.getType() instanceof ClassTypeInfo)) {
                     return false;
                 }
 
                 // ̌^̃NX擾
                 final ClassInfo dummyParameterClass = ((ClassTypeInfo) dummyParameter.getType())
                         .getReferencedClass();
 
                 // CɑΏۃNXłꍇ́Čp֌WlD܂C̃TuNXłȂꍇ́CĂяo\ł͂Ȃ
                 if ((actualParameterClass instanceof TargetClassInfo)
                         && (dummyParameterClass instanceof TargetClassInfo)) {
 
                     // ƓQƌ^iNXjłȂC̃TuNXłȂꍇ͊YȂ
                     if (actualParameterClass.equals(dummyParameterClass)) {
                         continue NEXT_PARAMETER;
 
                     } else if (actualParameterClass.isSubClass(dummyParameterClass)) {
                         continue NEXT_PARAMETER;
 
                     } else {
                         return false;
                     }
 
                     // CɊONXłꍇ́Cꍇ̂݌Ăяo\Ƃ
                 } else if ((actualParameterClass instanceof ExternalClassInfo)
                         && (dummyParameterClass instanceof ExternalClassInfo)) {
 
                     if (actualParameterClass.equals(dummyParameterClass)) {
                         continue NEXT_PARAMETER;
                     }
 
                     return false;
 
                     // ONXCΏۃNX̏ꍇ́C̃TuNXłꍇCĂяo\Ƃ
                 } else if ((actualParameterClass instanceof TargetClassInfo)
                         && (dummyParameterClass instanceof ExternalClassInfo)) {
 
                     if (actualParameterClass.isSubClass(dummyParameterClass)) {
                         continue NEXT_PARAMETER;
                     }
 
                     return false;
 
                     // ΏۃNXCONX̏ꍇ́CĂяos\Ƃ
                 } else {
                     return false;
                 }
 
                 // v~eBu^̏ꍇ
            } else if (actualParameterType instanceof PrimitiveTypeInfo) {
 
                 // PrimitiveTypeInfo#equals gē̔D
                 // Ȃꍇ͊YȂ
                if (actualParameterType.equals(dummyParameter.getType())) {
                     continue NEXT_PARAMETER;
                 }
 
                 return false;
 
                 // z^̏ꍇ
            } else if (actualParameterType instanceof ArrayTypeInfo) {
 
                 if (!(dummyParameter.getType() instanceof ArrayTypeInfo)) {
                     return false;
                 }
 
                 if (!actualParameter.getType().equals(dummyParameter.getType())) {
                     return false;
                 }
 
                 continue NEXT_PARAMETER;
                 // TODO Javȁꍇ́C java.lang.object łOKȏKv
 
                 //  null ̏ꍇ
             } else if (actualParameter instanceof NullUsageInfo) {
 
                 // Qƌ^łȂꍇ͊YȂ
                 if (!(dummyParameter.getType() instanceof ClassInfo)) {
                     return false;
                 }
 
                 continue NEXT_PARAMETER;
                 // TODO Javȁꍇ́Cz^̏ꍇłOKȏKv
 
                 // ̌^łȂꍇ
            } else if (actualParameterType instanceof UnknownTypeInfo) {
 
                 // ̌^sȏꍇ́Č^ł낤ƂOKɂĂ
                 continue NEXT_PARAMETER;
 
             } else {
                 assert false : "Here shouldn't be reached!";
             }
         }
 
         return true;
     }
 
     /**
      * ̃\bhŗ^ꂽIuWFNgi\bhjƓǂ𔻒肷
      * 
      * @param o rΏۃIuWFNgi\bhj
      * @return ꍇ true, Ȃꍇ false
      */
     @Override
     public final boolean equals(Object o) {
 
         if (null == o) {
             return false;
         }
 
         if (!(o instanceof MethodInfo)) {
             return false;
         }
 
         return 0 == this.compareTo((MethodInfo) o) ? true : false;
     }
 
     /**
      * ̃\bh̃nbVR[hԂ
      * 
      * @return ̃\bh̃nbVR[h
      */
     @Override
     public final int hashCode() {
 
         final StringBuilder sb = new StringBuilder();
         sb.append(this.getOwnerClass().getFullQualifiedName(
                 Settings.getLanguage().getNamespaceDelimiter()));
         sb.append(this.methodName);
 
         return sb.toString().hashCode();
     }
 
     /**
      * gNXvΏۂƂĂ̖OԂ
      * 
      * @return gNXvΏۂƂĂ̖O
      */
     public final String getMeasuredUnitName() {
 
         final StringBuilder sb = new StringBuilder(this.getMethodName());
         sb.append("#");
         sb.append(this.getOwnerClass().getFullQualifiedName(
                 Settings.getLanguage().getNamespaceDelimiter()));
         return sb.toString();
     }
 
     /**
      * ̃\bh̖OԂ
      * 
      * @return \bh
      */
     public final String getMethodName() {
         return this.methodName;
     }
 
     /**
      * ̃\bh̕Ԃľ^Ԃ
      * 
      * @return Ԃľ^
      */
     public final TypeInfo getReturnType() {
 
         if (null == this.returnType) {
             throw new NullPointerException();
         }
 
         return this.returnType;
     }
 
     /**
      * ̃\bḧǉD public 錾Ă邪C vOČĂяo͂͂D
      * 
      * @param parameter ǉ
      */
     public void addParameter(final ParameterInfo parameter) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == parameter) {
             throw new NullPointerException();
         }
 
         this.parameters.add(parameter);
     }
 
     /**
      * ̃\bḧǉD public 錾Ă邪C vOČĂяo͂͂D
      * 
      * @param parameters ǉQ
      */
     public void addParameters(final List<ParameterInfo> parameters) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == parameters) {
             throw new NullPointerException();
         }
 
         this.parameters.addAll(parameters);
     }
 
     /**
      * ̃\bh̕ԂlZbgD
      * 
      * @param returnType ̃\bh̕Ԃl
      */
     public void setReturnType(final TypeInfo returnType) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == returnType) {
             throw new NullPointerException();
         }
 
         this.returnType = returnType;
     }
 
     /**
      * ̃\bḧ̐Ԃ
      * 
      * @return ̃\bḧ̐
      */
     public int getParameterNumber() {
         return this.parameters.size();
     }
 
     /**
      * ̃\bḧ List ԂD
      * 
      * @return ̃\bḧ List
      */
     public List<ParameterInfo> getParameters() {
         return Collections.unmodifiableList(this.parameters);
     }
 
     /**
      * ̃\bhĂяoĂ郁\bh܂̓RXgN^ǉDvOCĂԂƃ^CG[D
      * 
      * @param caller ǉĂяo\bh
      */
     public void addCaller(final CallableUnitInfo caller) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == caller) {
             throw new NullPointerException();
         }
 
         this.callers.add(caller);
     }
 
     /**
      * ̃\bhI[o[ChĂ郁\bhǉDvOCĂԂƃ^CG[D
      * 
      * @param overridee ǉI[o[ChĂ郁\bh
      */
     public void addOverridee(final MethodInfo overridee) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == overridee) {
             throw new NullPointerException();
         }
 
         this.overridees.add(overridee);
     }
 
     /**
      * ̃\bhI[o[ChĂ郁\bhǉDvOCĂԂƃ^CG[D
      * 
      * @param overrider ǉI[o[ChĂ郁\bh
      * 
      */
     public void addOverrider(final MethodInfo overrider) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == overrider) {
             throw new NullPointerException();
         }
 
         this.overriders.add(overrider);
     }
 
     /**
      * ̃\bhĂяoĂ郁\bh܂̓RXgN^ SortedSet ԂD
      * 
      * @return ̃\bhĂяoĂ郁\bh SortedSet
      */
     public SortedSet<CallableUnitInfo> getCallers() {
         return Collections.unmodifiableSortedSet(this.callers);
     }
 
     /**
      * ̃\bhI[o[ChĂ郁\bh SortedSet ԂD
      * 
      * @return ̃\bhI[o[ChĂ郁\bh SortedSet
      */
     public SortedSet<MethodInfo> getOverridees() {
         return Collections.unmodifiableSortedSet(this.overridees);
     }
 
     /**
      * ̃\bhI[o[ChĂ郁\bh SortedSet ԂD
      * 
      * @return ̃\bhI[o[ChĂ郁\bh SortedSet
      */
     public SortedSet<MethodInfo> getOverriders() {
         return Collections.unmodifiableSortedSet(this.overriders);
     }
 
     /**
      * \bhۑ邽߂̕ϐ
      */
     private final String methodName;
 
     /**
      * Ԃľ^ۑ邽߂̕ϐ
      */
     private TypeInfo returnType;
 
     /**
      * ̃Xg̕ۑ邽߂̕ϐ
      */
     protected final List<ParameterInfo> parameters;
 
     /**
      * ̃\bhĂяoĂ郁\bhꗗۑ邽߂̕ϐ
      */
     protected final SortedSet<CallableUnitInfo> callers;
 
     /**
      * ̃\bhI[o[ChĂ郁\bhꗗۑ邽߂̕ϐ
      */
     protected final SortedSet<MethodInfo> overridees;
 
     /**
      * I[o[ChĂ郁\bhۑ邽߂̕ϐ
      */
     protected final SortedSet<MethodInfo> overriders;
 }
