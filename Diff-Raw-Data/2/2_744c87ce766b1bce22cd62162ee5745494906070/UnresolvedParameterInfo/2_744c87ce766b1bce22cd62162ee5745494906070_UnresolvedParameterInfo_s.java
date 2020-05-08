 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ArrayTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ModifierInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnknownTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * \߂̃NXD ^񋟂̂݁D
  * 
  * @author higo
  * 
  */
 public final class UnresolvedParameterInfo
         extends
         UnresolvedVariableInfo<TargetParameterInfo, UnresolvedCallableUnitInfo<? extends CallableUnitInfo>> {
 
     /**
      * IuWFNgDOƌ^KvD
      * 
      * @param name 
      * @param type ̌^
      * @param index Ԗڂ̈ł邩\
      * @param definitionMethod 錾Ă郁\bh
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public UnresolvedParameterInfo(final String name, final UnresolvedTypeInfo<?> type,
             final int index,
             final UnresolvedCallableUnitInfo<? extends CallableUnitInfo> definitionMethod,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn) {
         super(name, type, definitionMethod, fromLine, fromColumn, toLine, toColumn);
 
         this.index = index;
     }
 
     /**
      * CςݎQƂԂD
      * 
      * @param usingClass ̒`sĂNX
      * @param usingMethod ̒`sĂ郁\bh
      * @param classInfoManager pNX}l[W
      * @param fieldInfoManager ptB[h}l[W
      * @param methodInfoManager p郁\bh}l[W
      * @return ς݈
      */
     @Override
     public TargetParameterInfo resolve(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == usingClass) || (null == usingMethod) || (null == classInfoManager)) {
             throw new NullPointerException();
         }
 
         // ɉς݂łꍇ́CLbVԂ
         if (this.alreadyResolved()) {
             return this.getResolved();
         }
 
         // CqCp[^C^Cʒu擾
         final Set<ModifierInfo> parameterModifiers = this.getModifiers();
         final String parameterName = this.getName();
         final int index = this.getIndex();
         final UnresolvedTypeInfo<?> unresolvedParameterType = this.getType();
         TypeInfo parameterType = unresolvedParameterType.resolve(usingClass, usingMethod,
                classInfoManager, null, null);
         assert parameterType != null : "resolveTypeInfo returned null!";
         if (parameterType instanceof UnknownTypeInfo) {
             if (unresolvedParameterType instanceof UnresolvedClassReferenceInfo) {
 
                 final ExternalClassInfo externalClass = NameResolver
                         .createExternalClassInfo((UnresolvedClassReferenceInfo) unresolvedParameterType);
                 parameterType = new ClassTypeInfo(externalClass);
                 for (final UnresolvedTypeInfo<?> unresolvedTypeArgument : ((UnresolvedClassReferenceInfo) unresolvedParameterType)
                         .getTypeArguments()) {
                     final TypeInfo typeArgument = unresolvedTypeArgument.resolve(usingClass,
                             usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
                     ((ClassTypeInfo) parameterType).addTypeArgument(typeArgument);
                 }
                 classInfoManager.add(externalClass);
 
             } else if (unresolvedParameterType instanceof UnresolvedArrayTypeInfo) {
 
                 // TODO ^p[^̏i[
                 final UnresolvedTypeInfo<?> unresolvedElementType = ((UnresolvedArrayTypeInfo) unresolvedParameterType)
                         .getElementType();
                 final int dimension = ((UnresolvedArrayTypeInfo) unresolvedParameterType)
                         .getDimension();
                 final TypeInfo elementType = unresolvedElementType.resolve(usingClass, usingMethod,
                         classInfoManager, fieldInfoManager, methodInfoManager);
                 parameterType = ArrayTypeInfo.getType(elementType, dimension);
             } else {
                 assert false : "Can't resolve dummy parameter type : "
                         + unresolvedParameterType.toString();
             }
         }
         final int parameterFromLine = this.getFromLine();
         final int parameterFromColumn = this.getFromColumn();
         final int parameterToLine = this.getToLine();
         final int parameterToColumn = this.getToColumn();
 
         final CallableUnitInfo definitionMethod = this.getDefinitionUnit().getResolved();
 
         // p[^IuWFNg𐶐
         this.resolvedInfo = new TargetParameterInfo(parameterModifiers, parameterName,
                 parameterType, index, definitionMethod, parameterFromLine, parameterFromColumn,
                 parameterToLine, parameterToColumn);
         return this.resolvedInfo;
     }
 
     /**
      * ̃CfbNXԂ
      * 
      * @return@̃CfbNX
      */
     public int getIndex() {
         return this.index;
     }
 
     /**
      * ̃CfbNXۑ邽߂̕ϐ
      */
     private final int index;
 
 }
