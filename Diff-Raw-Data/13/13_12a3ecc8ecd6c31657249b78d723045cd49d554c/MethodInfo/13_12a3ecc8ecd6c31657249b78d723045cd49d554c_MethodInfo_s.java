 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.Settings;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.MetricMeasurable;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * \bh\NX
  * 
  * @author higo
  *
  */
 public abstract class MethodInfo extends CallableUnitInfo implements MetricMeasurable, Member {
 
     /**
      * \bhIuWFNg
      * 
      * @param modifiers CqSet
      * @param methodName \bh
      * @param ownerClass `ĂNX
      * @param privateVisible private
      * @param namespaceVisible Oԉ
      * @param inheritanceVisible qNX
      * @param publicVisible public
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     MethodInfo(final Set<ModifierInfo> modifiers, final String methodName,
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
 
         this.overridees = new TreeSet<MethodInfo>();
         this.overriders = new TreeSet<MethodInfo>();
     }
 
     /**
      * ̃\bhCŗ^ꂽgČĂяoƂł邩ǂ𔻒肷D
      * 
      * @param methodName \bh
      * @param actualParameters ̃Xg
      * @return Ăяoꍇ trueCłȂꍇ false
      */
     public final boolean canCalledWith(final String methodName,
             final List<ExpressionInfo> actualParameters) {
 
         if ((null == methodName) || (null == actualParameters)) {
             throw new IllegalArgumentException();
         }
 
         // \bhȂꍇ͊YȂ
         if (!methodName.equals(this.getMethodName())) {
             return false;
         }
 
         return super.canCalledWith(actualParameters);
     }
 
     /**
      * ̃\bhŗ^ꂽIuWFNgi\bhjƓǂ𔻒肷
      * 
      * @param o rΏۃIuWFNgi\bhj
      * @return ꍇ true, Ȃꍇ false
      */
     @Override
     public final boolean equals(Object o) {
 
         if (this == o) {
             return true;
         }
 
         if (!(o instanceof MethodInfo)) {
             return false;
         }
 
         return 0 == this.compareTo((MethodInfo) o);
     }
 
     /**
      * gNXvΏۂƂĂ̖OԂ
      * 
      * @return gNXvΏۂƂĂ̖O
      */
     @Override
     public final String getMeasuredUnitName() {
 
         final StringBuilder sb = new StringBuilder();
 
         final String fullQualifiedName = this.getOwnerClass().getFullQualifiedName(
                 Settings.getInstance().getLanguage().getNamespaceDelimiter());
         sb.append(fullQualifiedName);
 
         sb.append("#");
 
         final String methodName = this.getMethodName();
         sb.append(methodName);
 
         sb.append("(");
 
        for (final ParameterInfo parameter : this.getParameters()) {
            final TypeInfo parameterType = parameter.getType();
            sb.append(parameterType.getTypeName());
            sb.append(" ");
         }
        sb.deleteCharAt(sb.length() - 1);
 
         sb.append(")");
 
         return sb.toString();
     }
 
     @Override
     public final String getSignatureText(){
     
         final StringBuilder text = new StringBuilder();
         
         text.append(this.getReturnType().getTypeName());
         text.append(" ");
         text.append(this.getMethodName());
         
         text.append("(");
         for(final ParameterInfo parameter : this.getParameters()){
             text.append(parameter.getType().getTypeName());
             text.append(",");
         }
         if(0 < this.getParameterNumber()){
             text.deleteCharAt(text.length() -1);
         }
         text.append(")");
         
         return text.toString();
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
      * ̃\bhI[o[ChĂ郁\bhꗗۑ邽߂̕ϐ
      */
     protected final SortedSet<MethodInfo> overridees;
 
     /**
      * I[o[ChĂ郁\bhۑ邽߂̕ϐ
      */
     protected final SortedSet<MethodInfo> overriders;
 }
