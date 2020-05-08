 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.LocalVariableInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ModifierInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetConstructorInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * RXgN^\NX
  * 
  * @author higo
  *
  */
 public final class UnresolvedConstructorInfo extends
         UnresolvedCallableUnitInfo<TargetConstructorInfo> {
 
     /**
      * Kvȏ^āCIuWFNg
      * 
      * @param ownerClass LNX
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public UnresolvedConstructorInfo(final UnresolvedClassInfo ownerClass, final int fromLine,
             final int fromColumn, final int toLine, final int toColumn) {
 
         super(ownerClass, fromLine, fromColumn, toLine, toColumn);
     }
 
     /**
      * Os
      */
     @Override
     public TargetConstructorInfo resolve(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == usingClass) || (null == classInfoManager) || (null == methodInfoManager)) {
             throw new NullPointerException();
         }
 
         // ɉς݂łꍇ́CLbVԂ
         if (this.alreadyResolved()) {
             return this.getResolved();
         }
 
         // CqCOCԂlCsC擾
         final Set<ModifierInfo> methodModifiers = this.getModifiers();
         final boolean privateVisible = this.isPrivateVisible();
         final boolean namespaceVisible = this.isNamespaceVisible();
         final boolean inheritanceVisible = this.isInheritanceVisible();
         final boolean publicVisible = this.isPublicVisible();
 
         final int constructorFromLine = this.getFromLine();
         final int constructorFromColumn = this.getFromColumn();
         final int constructorToLine = this.getToLine();
         final int constructorToColumn = this.getToColumn();
 
         // MethodInfo IuWFNg𐶐D
         this.resolvedInfo = new TargetConstructorInfo(methodModifiers, usingClass, privateVisible,
                 namespaceVisible, inheritanceVisible, publicVisible, constructorFromLine,
                 constructorFromColumn, constructorToLine, constructorToColumn);
 
         // ^p[^Cς݃RXgN^ɒǉ
         for (final UnresolvedTypeParameterInfo unresolvedTypeParameter : this.getTypeParameters()) {
 
             final TypeParameterInfo typeParameter = unresolvedTypeParameter.resolve(usingClass,
                    this.resolvedInfo, classInfoManager, fieldInfoManager, methodInfoManager);
             this.resolvedInfo.addTypeParameter(typeParameter);
         }
 
         // Cς݃RXgN^ɒǉ
         for (final UnresolvedParameterInfo unresolvedParameterInfo : this.getParameters()) {
 
             final TargetParameterInfo parameterInfo = unresolvedParameterInfo.resolve(usingClass,
                     this.resolvedInfo, classInfoManager, fieldInfoManager, methodInfoManager);
             this.resolvedInfo.addParameter(parameterInfo);
         }
 
         // ubNCς݃IuWFNgɒǉ
         this.resolveInnerBlock(usingClass, this.resolvedInfo, classInfoManager, fieldInfoManager,
                 methodInfoManager);
 
         // \bhŒ`Ăe[Jϐɑ΂
         for (final UnresolvedLocalVariableInfo unresolvedLocalVariable : this.getLocalVariables()) {
 
             final LocalVariableInfo localVariable = unresolvedLocalVariable.resolve(usingClass,
                     this.resolvedInfo, classInfoManager, fieldInfoManager, methodInfoManager);
             this.resolvedInfo.addLocalVariable(localVariable);
         }
 
         this.resolveVariableUsages(usingClass, this.resolvedInfo, classInfoManager,
                 fieldInfoManager, methodInfoManager);
 
         return this.resolvedInfo;
     }
 
     /**
      * CX^Xo[ǂԂ
      * 
      * @return CX^Xo[Ȃ̂ true Ԃ
      */
     @Override
     public boolean isInstanceMember() {
         return true;
     }
 
     /**
      * X^eBbNo[ǂԂ
      * 
      * @return X^eBbNo[ł͂Ȃ̂ false Ԃ
      */
     @Override
     public boolean isStaticMember() {
         return false;
     }
 
     /**
      * ȂɂȂ
      */
     @Override
     public void setInstanceMember(boolean instance) {
     }
 
 }
