 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression;
 
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.ASTParseException;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.BuildDataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.StateDrivenDataBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.ExpressionStateManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.token.AstToken;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExpressionInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedExpressionInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedParenthesesExpressionInfo;
 
 
 /**
  * ʎ\zNX 
  * <br>
  * \vfł͂邪C̍\؏ɉe^Ă͂Ȃ߁C 
  * {@link ExpressionBuilder}̎qNXɂ͂ĂȂD
  * 
  * @author g-yamada
  * 
  */
 public class ParenthesesExpressionBuilder extends
         StateDrivenDataBuilder<UnresolvedParenthesesExpressionInfo> {
 
     /**
      * IuWFNg
      * 
      * @param expressionManager ExpressionElementManager
      * @param buildDataManager 
      */
     public ParenthesesExpressionBuilder(final ExpressionElementManager expressionManager,
             BuildDataManager buildDataManager) {
         if (null == buildDataManager || null == expressionManager) {
             throw new IllegalArgumentException();
         }
 
         this.buildDataManager = buildDataManager;
         this.expressionManager = expressionManager;
 
         this.addStateManager(this.expressionStateManger);
     }
 
     @Override
     public void entered(final AstVisitEvent e) {
         super.entered(e);
     }
 
     /**
      * exit m[hʎȂCUnresolvedParenthesesExpressionInfo閽߂
      */
     @Override
     public void exited(AstVisitEvent e) throws ASTParseException {
         super.exited(e);
         final AstToken token = e.getToken();
         if (this.isActive() && this.expressionStateManger.inExpression()
                 && token.isParenthesesExpression()) {
             this.buildParenthesesExpressionBuilder(e);
         }
     }
 
     /**
      * ߂ĎۂUnresolvedParenthesesExpressionInfo
      */
     protected void buildParenthesesExpressionBuilder(final AstVisitEvent e) {
         final ExpressionElement parentheticElement = expressionManager.popExpressionElement();
         
        UnresolvedExpressionInfo<? extends ExpressionInfo> parentheticExpression = parentheticElement
                .getUsage();        
         if (null == parentheticExpression) {
            if (!(parentheticElement instanceof IdentifierElement)) {
                 throw new NotImplementedException();
             }
             // ɂ̂(a)݂Ɋʎ̒ɕϐĂƂD
            final IdentifierElement identifier = (IdentifierElement) parentheticElement;
 
            parentheticExpression = identifier.resolveAsVariable(buildDataManager, false,
                     this.expressionStateManger.inAssignmentee());
         }
 
         // expressionAnalyzeStack̓ɊʎvbV
         final UnresolvedParenthesesExpressionInfo paren = new UnresolvedParenthesesExpressionInfo(
                 parentheticExpression);
         paren.setOuterUnit(this.buildDataManager.getCurrentUnit());
         paren.setFromLine(e.getStartLine());
         paren.setFromColumn(e.getStartColumn());
         paren.setToLine(e.getEndLine());
         paren.setToColumn(e.getEndColumn());
         expressionManager.pushExpressionElement(new UsageElement(paren));
     }
 
     @Override
     public void stateChanged(StateChangeEvent<AstVisitEvent> event) {
         // nothing to do
     }
 
     protected final ExpressionElementManager expressionManager;
 
     protected final BuildDataManager buildDataManager;
 
     private final ExpressionStateManager expressionStateManger = new ExpressionStateManager();
 }
