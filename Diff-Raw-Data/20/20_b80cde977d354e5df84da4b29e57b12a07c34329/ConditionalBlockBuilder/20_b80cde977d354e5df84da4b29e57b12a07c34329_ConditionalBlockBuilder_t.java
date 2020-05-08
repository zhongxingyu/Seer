 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.innerblock;
 
 
 import java.util.Stack;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.BuildDataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.StateChangeEvent.StateChangeEventType;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.innerblock.InnerBlockStateManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.statemanager.innerblock.InnerBlockStateManager.INNER_BLOCK_STATE_CHANGE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ConditionalBlockInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedConditionalBlockInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedConditionalClauseInfo;
 
 
 public abstract class ConditionalBlockBuilder<TResolved extends ConditionalBlockInfo, T extends UnresolvedConditionalBlockInfo<TResolved>>
         extends InnerBlockBuilder<TResolved, T> {
 
     protected ConditionalBlockBuilder(final BuildDataManager targetDataManager,
             final InnerBlockStateManager blockStateManager) {
         super(targetDataManager, blockStateManager);
     }
 
     @Override
     public void stateChangend(final StateChangeEvent<AstVisitEvent> event) {
         super.stateChangend(event);
         final StateChangeEventType type = event.getType();
 
         if (type.equals(INNER_BLOCK_STATE_CHANGE.ENTER_CLAUSE)) {
             startConditionalClause(event.getTrigger());
         } else if (type.equals(INNER_BLOCK_STATE_CHANGE.EXIT_CLAUSE)) {
             endConditionalClause();
         }
     }
 
     private void startConditionalClause(final AstVisitEvent triggerEvent) {
         if (!this.buildingBlockStack.isEmpty()
                && this.buildingBlockStack.peek() == this.buildManager.getCurrentBlock()) {
             final T currentBlock = (T) this.buildingBlockStack.peek();
             final UnresolvedConditionalClauseInfo conditionalClause = currentBlock
                     .getConditionalClause();
 
             this.buildingClauseStack.push(conditionalClause);
 
             conditionalClause.setFromLine(triggerEvent.getStartLine());
             conditionalClause.setFromColumn(triggerEvent.getStartColumn());
             conditionalClause.setToLine(triggerEvent.getEndLine());
             conditionalClause.setToColumn(triggerEvent.getEndColumn());
 
             this.buildManager.startConditionalClause(conditionalClause);
         }
     }
 
     private void endConditionalClause() {
        if (!this.buildingBlockStack.isEmpty()
                && this.buildingBlockStack.peek() == this.buildManager.getCurrentBlock()) {

            final UnresolvedConditionalClauseInfo buildClause = this.buildingClauseStack.pop();
 
            if (null != buildClause) {
                this.buildManager.endConditionalClause();
            }
         }
     }
 
     private final Stack<UnresolvedConditionalClauseInfo> buildingClauseStack = new Stack<UnresolvedConditionalClauseInfo>();
 }
