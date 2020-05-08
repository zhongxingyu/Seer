 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.DataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedExpressionInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * Ăяo\ȒP(\bhRXgN^)\NX
  * 
  * @author higo
  */
 
 @SuppressWarnings("serial")
 public abstract class CallableUnitInfo extends LocalSpaceInfo implements Visualizable, Modifier,
         TypeParameterizable {
 
     /**
      * IuWFNg
      * 
      * @param modifiers CqSet
      * @param ownerClass LNX
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     CallableUnitInfo(final Set<ModifierInfo> modifiers, final ClassInfo ownerClass,
             final boolean privateVisible, final boolean namespaceVisible,
             final boolean inheritanceVisible, final boolean publicVisible, final int fromLine,
             final int fromColumn, final int toLine, final int toColumn) {
 
         super(ownerClass, fromLine, fromColumn, toLine, toColumn);
 
         this.privateVisible = privateVisible;
         this.namespaceVisible = namespaceVisible;
         this.inheritanceVisible = inheritanceVisible;
         this.publicVisible = publicVisible;
 
         this.parameters = new LinkedList<ParameterInfo>();
 
         this.typeParameters = new LinkedList<TypeParameterInfo>();
         this.typeParameterUsages = new HashMap<TypeParameterInfo, TypeInfo>();
         this.thrownExceptions = new LinkedList<ClassTypeInfo>();
 
         this.unresolvedUsage = new HashSet<UnresolvedExpressionInfo<?>>();
 
         this.callers = new TreeSet<CallableUnitInfo>();
 
         this.modifiers = new HashSet<ModifierInfo>();
         this.modifiers.addAll(modifiers);
     }
 
     /**
      * `ꂽϐSetԂ
      * 
      * @return `ꂽϐSet
      */
     @Override
     public Set<VariableInfo<? extends UnitInfo>> getDefinedVariables() {
         final Set<VariableInfo<? extends UnitInfo>> definedVariables = new HashSet<VariableInfo<? extends UnitInfo>>();
         definedVariables.addAll(super.getDefinedVariables());
         definedVariables.addAll(this.getParameters());
         return Collections.unmodifiableSet(definedVariables);
     }
 
     /**
      * \bhԂ̏̎́C`ĂNXl邽߂ɒ`ĂD
      */
     @Override
     final public int compareTo(final Position o) {
 
         if (null == o) {
             throw new IllegalArgumentException();
         }
 
         if (o instanceof CallableUnitInfo) {
 
             final ClassInfo ownerClass = this.getOwnerClass();
             final ClassInfo correspondOwnerClass = ((CallableUnitInfo) o).getOwnerClass();
             final int classOrder = ownerClass.compareTo(correspondOwnerClass);
             if (classOrder != 0) {
                 return classOrder;
             }
         }
 
         return super.compareTo(o);
     }
 
     /**
      * Ăяojbg̃nbVR[hԂ
      */
     @Override
     final public int hashCode() {
         return this.getFromLine() + this.getFromColumn() + this.getToLine() + this.getToColumn();
     }
 
     public int compareArgumentsTo(final CallableUnitInfo target) {
         // ̌Ŕr
         final int parameterNumber = this.getParameterNumber();
         final int correspondParameterNumber = target.getParameterNumber();
         if (parameterNumber < correspondParameterNumber) {
             return 1;
         } else if (parameterNumber > correspondParameterNumber) {
             return -1;
         } else {
 
             // ̌^ŔrD珇ԂɁD
             final Iterator<ParameterInfo> parameterIterator = this.getParameters().iterator();
             final Iterator<ParameterInfo> correspondParameterIterator = target.getParameters()
                     .iterator();
             while (parameterIterator.hasNext() && correspondParameterIterator.hasNext()) {
                 final ParameterInfo parameter = parameterIterator.next();
                 final ParameterInfo correspondParameter = correspondParameterIterator.next();
                 final String typeName = parameter.getType().getTypeName();
                 final String correspondTypeName = correspondParameter.getType().getTypeName();
                 final int typeOrder = typeName.compareTo(correspondTypeName);
                 if (typeOrder != 0) {
                     return typeOrder;
                 }
             }
 
             return 0;
         }
     }
 
     /**
      * ̃IuWFNgCŗ^ꂽgČĂяoƂł邩ǂ𔻒肷D
      * 
      * @param actualParameters ̃Xg
      * @return Ăяoꍇ trueCłȂꍇ false
      */
     boolean canCalledWith(final List<ExpressionInfo> actualParameters) {
 
         if (null == actualParameters) {
             throw new IllegalArgumentException();
         }
 
         final ExpressionInfo[] actualParameterArray = actualParameters
                 .toArray(new ExpressionInfo[0]);
         final ParameterInfo[] dummyParameterArray = this.getParameters().toArray(
                 new ParameterInfo[0]);
         int checkedActualIndex = -1;
 
         for (int index = 0; index < dummyParameterArray.length; index++) {
 
             final ParameterInfo dummyParameter = dummyParameterArray[index];
             final TypeInfo dummyType = dummyParameter.getType();
 
             //ϒ̏ꍇ
             if (dummyParameter instanceof VariableLengthParameterInfo) {
 
                 // TODO ̂ƂȂOKɂĂD̕Kv
                 checkedActualIndex = index;
                 continue;
             }
 
             //NXQƌ^̏ꍇ
             else if (dummyType instanceof ClassTypeInfo) {
 
                 // ̐Ȃ̂ŌĂяos
                 if (!(index < actualParameterArray.length)) {
                     return false;
                 }
 
                 final ClassInfo dummyClass = ((ClassTypeInfo) dummyType).getReferencedClass();
                 final ExpressionInfo actualParameter = actualParameterArray[index];
                 final TypeInfo actualType = actualParameter.getType();
 
                 // ̌^UnknownTypeInfôƂ͂ǂ悤Ȃ̂OKɂ
                 if (actualType instanceof UnknownTypeInfo) {
                     checkedActualIndex = index;
                     continue;
                 }
 
                 //@ null łOK
                 if (actualParameter instanceof NullUsageInfo) {
                     checkedActualIndex = index;
                     continue;
                 }
 
                 // Object^̂Ƃ́CNXQƌ^Cz^C^p[^^OK
                 final ClassInfo objectClass = DataManager.getInstance().getClassInfoManager()
                         .getClassInfo(new String[] { "java", "lang", "Object" });
                 if (((ClassTypeInfo) dummyType).getReferencedClass().equals(objectClass)) {
 
                     if (actualType instanceof ReferenceTypeInfo) {
                         checkedActualIndex = index;
                         continue;
                     }
                 }
 
                 // Object^łȂƂ́CNX^̏ꍇɂďڂׂ
                 else {
 
                     if (!(actualType instanceof ClassTypeInfo)) {
                         return false;
                     }
 
                     final ClassInfo actualClass = ((ClassTypeInfo) actualType).getReferencedClass();
 
                     // CɑΏۃNXłꍇ́Čp֌WlD
                     // ܂C̃TuNXłȂꍇ́CĂяo\ł͂Ȃ
                     if ((actualClass instanceof TargetClassInfo)
                             && (dummyClass instanceof TargetClassInfo)) {
 
                         // ƓQƌ^iNXjłȂC̃TuNXłȂꍇ͊YȂ
                         if (actualClass.equals(dummyClass)) {
                             checkedActualIndex = index;
                             continue;
 
                         } else if (actualClass.isSubClass(dummyClass)) {
                             checkedActualIndex = index;
                             continue;
 
                         } else {
                             return false;
                         }
                     }
 
                     // CƂɊONXłꍇ́CȂŌĂяo\ƂD
                     // Ȃƃ_Ƃ͌ĐłȂꍇD
                     // CɊONXłꍇ́C/*ꍇ̂݌Ăяo*/\Ƃ
                     else if ((actualClass instanceof ExternalClassInfo)
                             && (dummyClass instanceof ExternalClassInfo)) {
                         checkedActualIndex = index;
                         continue;
                     }
 
                     // ONXCΏۃNX̏ꍇ́CĂяo\Ƃ
                     // Ȃƃ_Ƃ͌ĐłȂꍇD
                     else if ((actualClass instanceof TargetClassInfo)
                             && (dummyClass instanceof ExternalClassInfo)) {
                         checkedActualIndex = index;
                         continue;
                     }
 
                     // ΏۃNXCONX̏ꍇ́CĂяo\Ƃ
                     // Ȃƃ_Ƃ͌ĐłȂꍇD
                     else {
                         checkedActualIndex = index;
                         continue;
                     }
                 }
 
             }
             // v~eBu^̏ꍇ
             else if (dummyType instanceof PrimitiveTypeInfo) {
 
                 // ̐Ȃ̂ŌĂяos
                 if (!(index < actualParameterArray.length)) {
                     return false;
                 }
 
                 // ̌^UnknownTypeInfôƂ͂ǂ悤Ȃ̂OKɂ
                 final TypeInfo actualType = actualParameterArray[index].getType();
                 if (actualType instanceof UnknownTypeInfo) {
                     checkedActualIndex = index;
                     continue;
                 }
 
                 // v~eBu^łȂꍇ͌Ăяos
                 if (!(actualType instanceof PrimitiveTypeInfo)) {
                     return false;
                 }
 
                 checkedActualIndex = index;
                 continue;
             }
 
             // z^̏ꍇ
             else if (dummyType instanceof ArrayTypeInfo) {
 
                 // ̐Ȃ̂ŌĂяos
                 if (!(index < actualParameterArray.length)) {
                     return false;
                 }
 
                 // ̌^UnknownTypeInfôƂ͂ǂ悤Ȃ̂OKɂ
                 final TypeInfo actualType = actualParameterArray[index].getType();
                 if (actualType instanceof UnknownTypeInfo) {
                     checkedActualIndex = index;
                     continue;
                 }
 
                 // z^łȂꍇ͌Ăяos
                 if (!(actualType instanceof ArrayTypeInfo)) {
                     return false;
                 }
 
                 final ClassInfo objectClass = DataManager.getInstance().getClassInfoManager()
                         .getClassInfo(new String[] { "java", "lang", "Object" });
 
                 // ̗vf^java.lang.Object̏ꍇ́C̗vf^͂Ȃł
                 final TypeInfo dummyElementType = ((ArrayTypeInfo) dummyType).getElementType();
                 if ((dummyElementType instanceof ClassTypeInfo)
                         && (((ClassTypeInfo) dummyElementType).getReferencedClass()
                                 .equals(objectClass))) {
                     checkedActualIndex = index;
                     continue;
                 }
 
                 // ̗vf^java.lang.ObjectłȂꍇ́C̗vf^ƓłC
                 // ꍇ̂
                 else {
 
                     final TypeInfo actualElementType = ((ArrayTypeInfo) actualType)
                             .getElementType();
                     final int actualDimenstion = ((ArrayTypeInfo) actualType).getDimension();
                     final int dummyDimenstion = ((ArrayTypeInfo) dummyType).getDimension();
 
                     if (actualElementType.equals(dummyElementType)
                             && (actualDimenstion == dummyDimenstion)) {
                         checkedActualIndex = index;
                         continue;
                     }
 
                     return false;
                 }
             }
 
             // ^p[^^̏ꍇ
             else if (dummyType instanceof TypeParameterTypeInfo) {
 
                 // ̐Ȃ̂ŌĂяos
                 if (!(index < actualParameterArray.length)) {
                     return false;
                 }
 
                 // TODO ̂ƂCȂOKɂĂD̕Kv
                 checkedActualIndex = index;
                 continue;
             }
         }
 
         // ׂĂ̎ɂă`FbNĂ̂ł΁CĂяo\Ƃ
        if (actualParameterArray.length <= checkedActualIndex + 1) {
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * ̃\bḧ List ԂD
      * 
      * @return ̃\bḧ List
      */
     public final List<ParameterInfo> getParameters() {
         return Collections.unmodifiableList(this.parameters);
     }
 
     /**
      * ̃\bḧ̐Ԃ
      * 
      * @return ̃\bḧ̐
      */
     public final int getParameterNumber() {
         return this.parameters.size();
     }
 
     /**
      * ^p[^̎gpǉ
      * 
      * @param typeParameterInfo ^p[^ 
      * @param usedType ^p[^ɑĂ^
      */
     @Override
     public void addTypeParameterUsage(final TypeParameterInfo typeParameterInfo,
             final TypeInfo usedType) {
 
         if ((null == typeParameterInfo) || (null == usedType)) {
             throw new IllegalArgumentException();
         }
 
         this.typeParameterUsages.put(typeParameterInfo, usedType);
     }
 
     /**
      * ^p[^gp̃}bvԂ
      * 
      * @return ^p[^gp̃}bv
      */
     @Override
     public Map<TypeParameterInfo, TypeInfo> getTypeParameterUsages() {
         return Collections.unmodifiableMap(this.typeParameterUsages);
     }
 
     /**
      * Ŏw肳ꂽ^p[^ǉ
      * 
      * @param typeParameter ǉ^p[^
      */
     @Override
     public final void addTypeParameter(final TypeParameterInfo typeParameter) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == typeParameter) {
             throw new NullPointerException();
         }
 
         this.typeParameters.add(typeParameter);
     }
 
     /**
      * w肳ꂽCfbNX̌^p[^Ԃ
      * 
      * @param index ^p[^̃CfbNX
      * @return@w肳ꂽCfbNX̌^p[^
      */
     @Override
     public final TypeParameterInfo getTypeParameter(final int index) {
         return this.typeParameters.get(index);
     }
 
     /**
      * ^p[^ List ԂD
      * 
      * @return ̃NX̌^p[^ List
      */
     @Override
     public final List<TypeParameterInfo> getTypeParameters() {
         return Collections.unmodifiableList(this.typeParameters);
     }
 
     @Override
     public TypeParameterizable getOuterTypeParameterizableUnit() {
         final ClassInfo ownerClass = this.getOwnerClass();
         return ownerClass;
     }
 
     /**
      * Ŏw肳ꂽOǉ
      * 
      * @param thrownException ǉO
      */
     public final void addThrownException(final ClassTypeInfo thrownException) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == thrownException) {
             throw new IllegalArgumentException();
         }
 
         this.thrownExceptions.add(thrownException);
     }
 
     /**
      * X[O List ԂD
      * 
      * @return X[O List
      */
     public final List<ClassTypeInfo> getThrownExceptions() {
         return Collections.unmodifiableList(this.thrownExceptions);
     }
 
     /**
      * ̌ĂяojbgŁCOłȂNXQƁCtB[hQƁEC\bhĂяoǉD vOCĂԂƃ^CG[D
      * 
      * @param entityUsage OłȂNXQƁCtB[hQƁEC\bhĂяo
      */
     public void addUnresolvedUsage(final UnresolvedExpressionInfo<?> entityUsage) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == entityUsage) {
             throw new NullPointerException();
         }
 
         this.unresolvedUsage.add(entityUsage);
     }
 
     /**
      * ̌ĂяojbgŁCOłȂNXQƁCtB[hQƁEC\bhĂяo Set ԂD
      * 
      * @return ̃\bhŁCOłȂNXQƁCtB[hQƁEC\bhĂяo Set
      */
     public Set<UnresolvedExpressionInfo<?>> getUnresolvedUsages() {
         return Collections.unmodifiableSet(this.unresolvedUsage);
     }
 
     /**
      * Cq Set Ԃ
      * 
      * @return Cq Set
      */
     @Override
     public final Set<ModifierInfo> getModifiers() {
         return Collections.unmodifiableSet(this.modifiers);
     }
 
     /**
      * qNXQƉ\ǂԂ
      * 
      * @return qNXQƉ\ȏꍇ true, łȂꍇ false
      */
     @Override
     public final boolean isInheritanceVisible() {
         return this.inheritanceVisible;
     }
 
     /**
      * OԂQƉ\ǂԂ
      * 
      * @return OԂQƉ\ȏꍇ true, łȂꍇ false
      */
     @Override
     public final boolean isNamespaceVisible() {
         return this.namespaceVisible;
     }
 
     /**
      * NX̂ݎQƉ\ǂԂ
      * 
      * @return NX̂ݎQƉ\ȏꍇ true, łȂꍇ false
      */
     @Override
     public final boolean isPrivateVisible() {
         return this.privateVisible;
     }
 
     /**
      * ǂłQƉ\ǂԂ
      * 
      * @return ǂłQƉ\ȏꍇ true, łȂꍇ false
      */
     @Override
     public final boolean isPublicVisible() {
         return this.publicVisible;
     }
 
     /**
      * ̃\bhĂяoĂ郁\bh܂̓RXgN^ǉDvOCĂԂƃ^CG[D
      * 
      * @param caller ǉĂяo\bh
      */
     public final void addCaller(final CallableUnitInfo caller) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == caller) {
             return;
         }
 
         this.callers.add(caller);
     }
 
     /**
      * ̃\bhĂяoĂ郁\bh܂̓RXgN^ SortedSet ԂD
      * 
      * @return ̃\bhĂяoĂ郁\bh SortedSet
      */
     public final SortedSet<CallableUnitInfo> getCallers() {
         return Collections.unmodifiableSortedSet(this.callers);
     }
 
     /**
      * ONX̃RXgN^A\bḧʒuɓ_~[̒l 
      */
     protected final static int getDummyPosition() {
         return dummyPosition--;
     }
 
     /**
      * CallableUnitInfõVOl`̃eLXg\Ԃ
      * 
      * @return CallableUnitInfõVOl`̃eLXg\
      */
     public abstract String getSignatureText();
 
     /**
      * NX̂ݎQƉ\ǂۑ邽߂̕ϐ
      */
     private final boolean privateVisible;
 
     /**
      * OԂQƉ\ǂۑ邽߂̕ϐ
      */
     private final boolean namespaceVisible;
 
     /**
      * qNXQƉ\ǂۑ邽߂̕ϐ
      */
     private final boolean inheritanceVisible;
 
     /**
      * ǂłQƉ\ǂۑ邽߂̕ϐ
      */
     private final boolean publicVisible;
 
     /**
      * Cqۑ邽߂̕ϐ
      */
     private final Set<ModifierInfo> modifiers;
 
     /**
      * ^p[^ۑϐ
      */
     private final List<TypeParameterInfo> typeParameters;
 
     /**
      * X[Oۑϐ
      */
     private final List<ClassTypeInfo> thrownExceptions;
 
     /**
      * ̃NXŎgpĂ^p[^ƎۂɌ^p[^ɑĂ^̃yA.
      * ̃NXŒ`Ă^p[^ł͂ȂD
      */
     private final Map<TypeParameterInfo, TypeInfo> typeParameterUsages;
 
     /**
      * ̃Xg̕ۑ邽߂̕ϐ
      */
     protected final List<ParameterInfo> parameters;
 
     /**
      * ̃\bhĂяoĂ郁\bhꗗۑ邽߂̕ϐ
      */
     private final SortedSet<CallableUnitInfo> callers;
 
     /**
      * OłȂNXQƁCtB[hQƁEC\bhĂяoȂǂۑ邽߂̕ϐ
      */
     private final transient Set<UnresolvedExpressionInfo<?>> unresolvedUsage;
 
     /**
      * ONX̃RXgN^A\bḧʒuɓ_~[̒lB
      */
     private static int dummyPosition = -1;
 }
