 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ArrayTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ArrayTypeReferenceInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * z^QƂ\NX
  * 
  * @author t-miyake, higo
  *
  */
 public final class UnresolvedArrayTypeReferenceInfo extends
         UnresolvedExpressionInfo<ArrayTypeReferenceInfo> {
 
     /**
      * QƂĂ関z^^ď
      * 
      * @param referencedType QƂĂ関z^
      */
    public UnresolvedArrayTypeReferenceInfo(final UnresolvedArrayTypeInfo referencedType) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == referencedType) {
             throw new IllegalArgumentException("referencedType is null");
         }
 
         this.referencedType = referencedType;
         this.resolvedInfo = null;
     }
 
     @Override
     public ArrayTypeReferenceInfo resolve(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == usingClass) || (null == classInfoManager)) {
             throw new NullPointerException();
         }
 
         // ɉς݂łꍇ́CLbVԂ
         if (this.alreadyResolved()) {
             return this.getResolved();
         }
 
         //@ʒu擾
         final int fromLine = this.getFromLine();
         final int fromColumn = this.getFromColumn();
         final int toLine = this.getToLine();
         final int toColumn = this.getToColumn();
 
         // QƂĂz^
         final UnresolvedArrayTypeInfo unresolvedArrayType = this.getType();
         final ArrayTypeInfo arrayType = unresolvedArrayType.resolve(usingClass, usingMethod,
                 classInfoManager, fieldInfoManager, methodInfoManager);
 
         /*// vfgp̃I[i[vfԂ
         final UnresolvedExecutableElementInfo<?> unresolvedOwnerExecutableElement = this
                 .getOwnerExecutableElement();
         final ExecutableElementInfo ownerExecutableElement = unresolvedOwnerExecutableElement
                 .resolve(usingClass, usingMethod, classInfoManager, fieldInfoManager,
                         methodInfoManager);*/
 
         this.resolvedInfo = new ArrayTypeReferenceInfo(arrayType, usingMethod, fromLine,
                 fromColumn, toLine, toColumn);
         /*this.resolvedInfo.setOwnerExecutableElement(ownerExecutableElement);*/
 
         return this.resolvedInfo;
     }
 
     /**
      * QƂĂ関z^Ԃ
      * @return QƂĂ関z^
      */
     public UnresolvedArrayTypeInfo getType() {
         return this.referencedType;
     }
 
     /**
      * QƂĂ関z^ۑ邽߂̕ϐ
      */
     private final UnresolvedArrayTypeInfo referencedType;
 
 }
