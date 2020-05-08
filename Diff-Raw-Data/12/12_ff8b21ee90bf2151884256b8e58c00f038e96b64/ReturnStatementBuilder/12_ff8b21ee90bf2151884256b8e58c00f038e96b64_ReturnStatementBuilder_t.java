 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.ExpressionElementManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.token.AstToken;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExpressionInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.LocalSpaceInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedExpressionInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedLocalSpaceInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedReturnStatementInfo;
 
 
 /**
  * ^[̏\zNX
  * 
  * @author t-miyake
  *
  */
 public class ReturnStatementBuilder extends SingleStatementBuilder<UnresolvedReturnStatementInfo> {
 
     /**
      * \zς݂̎}l[W[C\zς݃f[^}l[W[^ďD
      * 
      * @param expressionManager \zςݎ}l[W[
      * @param buildDataManager \zς݃f[^}l[W[
      */
     public ReturnStatementBuilder(ExpressionElementManager expressionManager,
             BuildDataManager buildDataManager) {
         super(expressionManager, buildDataManager);
     }
 
     @Override
     protected UnresolvedReturnStatementInfo buildStatement(
             final UnresolvedLocalSpaceInfo<? extends LocalSpaceInfo> ownerSpace,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn) {
 
         final UnresolvedReturnStatementInfo returnStatement = new UnresolvedReturnStatementInfo(
                 ownerSpace);
         returnStatement.setFromLine(fromLine);
         returnStatement.setFromColumn(fromColumn);
         returnStatement.setToLine(toLine);
         returnStatement.setToColumn(toColumn);
 
         return returnStatement;
     }
 
     @Override
     public void exited(AstVisitEvent e) {
         super.exited(e);
 
         if (this.isTriggerToken(e.getToken())) {
             if (null != this.getLastBuildData()) {
                 final UnresolvedExpressionInfo<? extends ExpressionInfo> returnedExpression = this
                         .getLastBuiltExpression();
                final UnresolvedReturnStatementInfo buildingStatement = this.getLastBuildData();
                
                // TODO ĂȂ.SingleStatementBuilderStatetDrivenDataBuilderp悤ɕύXׂ
                if (null != returnedExpression
                        && returnedExpression.getToLine() < buildingStatement.getFromLine()
                        || returnedExpression.getToLine() == buildingStatement.getFromLine()
                        && returnedExpression.getToColumn() < buildingStatement.getFromColumn()) {
                    buildingStatement.setReturnedExpression(null);
                } else {
                    buildingStatement.setReturnedExpression(returnedExpression);
                }
             }
         }
     }
 
     @Override
     protected boolean isTriggerToken(AstToken token) {
         return token.isReturn();
     }
 
 }
