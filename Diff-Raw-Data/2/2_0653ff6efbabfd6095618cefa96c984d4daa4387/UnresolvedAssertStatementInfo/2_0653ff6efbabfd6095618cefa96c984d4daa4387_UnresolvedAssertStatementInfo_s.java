 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.AssertStatementInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.EmptyExpressionInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExpressionInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.LocalSpaceInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * assert̖\NX
  * 
  * @author t-miyake
  *
  */
 public class UnresolvedAssertStatementInfo extends
         UnresolvedSingleStatementInfo<AssertStatementInfo> {
 
     /**
      * AT[g𐶐
      * 
      * @param ownerSpace ÕubN
      */
     public UnresolvedAssertStatementInfo(
             final UnresolvedLocalSpaceInfo<? extends LocalSpaceInfo> ownerSpace) {
         super(ownerSpace);
     }
 
     @Override
     public AssertStatementInfo resolve(TargetClassInfo usingClass, CallableUnitInfo usingMethod,
             ClassInfoManager classInfoManager, FieldInfoManager fieldInfoManager,
             MethodInfoManager methodInfoManager) {
 
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
 
         // [JXy[X
         final UnresolvedLocalSpaceInfo<?> unresolvedOwnerSpace = this.getOwnerSpace();
         final LocalSpaceInfo ownerSpace = unresolvedOwnerSpace.resolve(usingClass, usingMethod,
                 classInfoManager, fieldInfoManager, methodInfoManager);
 
         final UnresolvedExpressionInfo<?> unresolvedAssertedExpression = this
                 .getAssertedExpression();
         final ExpressionInfo assertedExpression = unresolvedAssertedExpression.resolve(usingClass,
                 usingMethod, classInfoManager, fieldInfoManager, methodInfoManager);
 
         final UnresolvedExpressionInfo<?> unresolvedMessageExpression = this.getMessageExpression();
         final ExpressionInfo messageExpression = null == unresolvedMessageExpression ? new EmptyExpressionInfo(
                usingMethod, toLine, toLine, toColumn, toColumn)
                 : unresolvedMessageExpression.resolve(usingClass, usingMethod, classInfoManager,
                         fieldInfoManager, methodInfoManager);
 
         this.resolvedInfo = new AssertStatementInfo(ownerSpace, assertedExpression,
                 messageExpression, fromLine, fromColumn, toLine, toColumn);
         return this.resolvedInfo;
     }
 
     /**
      * ؂̌ʂfalsełƂɏo͂郁bZ[W\̖ݒ肷
      * @param messageExpression
      */
     public final void setMessageExpression(
             final UnresolvedExpressionInfo<? extends ExpressionInfo> messageExpression) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == messageExpression) {
             throw new IllegalArgumentException();
         }
         this.messageExpression = messageExpression;
     }
 
     /**
      * bZ[WԂ
      * 
      * @return@bZ[W
      */
     public final UnresolvedExpressionInfo<? extends ExpressionInfo> getMessageExpression() {
         return this.messageExpression;
     }
 
     /**
      * ؎̖ݒ肷
      * @param assertedExpression ؎̖
      */
     public final void setAsserttedExpression(
             final UnresolvedExpressionInfo<? extends ExpressionInfo> assertedExpression) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == assertedExpression) {
             throw new IllegalArgumentException();
         }
 
         this.assertedExpression = assertedExpression;
     }
 
     /**
      * ؎Ԃ
      * 
      * @return@؎
      */
     public final UnresolvedExpressionInfo<? extends ExpressionInfo> getAssertedExpression() {
         return this.assertedExpression;
     }
 
     /**
      * ؎̖ۑϐ
      */
     private UnresolvedExpressionInfo<? extends ExpressionInfo> assertedExpression;
 
     /**
      * ؎falseԂƂɏo͂郁bZ[W\̖ۑ邽߂̕ϐ
      */
     private UnresolvedExpressionInfo<? extends ExpressionInfo> messageExpression;
 
 }
