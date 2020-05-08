 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ArrayTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ModifierInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ReferenceTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetMethodInfo;
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
 public final class UnresolvedParameterInfo extends UnresolvedVariableInfo<TargetParameterInfo> {
 
     /**
      * IuWFNgDOƌ^KvD
      * 
      * @param name 
      * @param type ̌^
      */
     public UnresolvedParameterInfo(final String name, final UnresolvedTypeInfo type) {
         super(name, type);
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
     public TargetParameterInfo resolveUnit(final TargetClassInfo usingClass,
             final TargetMethodInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == usingClass) || (null == usingMethod) || (null == classInfoManager)
                 || (null == fieldInfoManager) || (null == methodInfoManager)) {
             throw new NullPointerException();
         }
 
         // ɉς݂łꍇ́CLbVԂ
         if (this.alreadyResolved()) {
             return this.getResolvedUnit();
         }
 
         // CqCp[^C^Cʒu擾
         final Set<ModifierInfo> parameterModifiers = this.getModifiers();
         final String parameterName = this.getName();
         final UnresolvedTypeInfo unresolvedParameterType = this.getType();
         TypeInfo parameterType = unresolvedParameterType.resolveType(usingClass, usingMethod,
                 classInfoManager, null, null);
         assert parameterType != null : "resolveTypeInfo returned null!";
         if (parameterType instanceof UnknownTypeInfo) {
             if (unresolvedParameterType instanceof UnresolvedClassReferenceInfo) {
 
                 // TODO ^p[^̏i[
                 final ExternalClassInfo externalClass = NameResolver
                         .createExternalClassInfo((UnresolvedClassReferenceInfo) unresolvedParameterType);
                final ReferenceTypeInfo reference = new ReferenceTypeInfo(externalClass);
                 classInfoManager.add(externalClass);
 
             } else if (unresolvedParameterType instanceof UnresolvedArrayTypeInfo) {
 
                 // TODO ^p[^̏i[
                 final UnresolvedTypeInfo unresolvedElementType = ((UnresolvedArrayTypeInfo) unresolvedParameterType)
                         .getElementType();
                 final int dimension = ((UnresolvedArrayTypeInfo) unresolvedParameterType)
                         .getDimension();
                 final TypeInfo elementType = unresolvedElementType.resolveType(usingClass,
                         usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
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
 
         // p[^IuWFNg𐶐
         this.resolvedInfo = new TargetParameterInfo(parameterModifiers, parameterName,
                 parameterType, parameterFromLine, parameterFromColumn, parameterToLine,
                 parameterToColumn);
         return this.resolvedInfo;
     }
 
 }
