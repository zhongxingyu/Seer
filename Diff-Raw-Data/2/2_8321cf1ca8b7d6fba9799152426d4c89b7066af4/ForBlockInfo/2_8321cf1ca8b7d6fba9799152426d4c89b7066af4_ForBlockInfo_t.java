 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * for ubN\NX
  * 
  * @author higo
  * 
  */
 @SuppressWarnings("serial")
 public final class ForBlockInfo extends ConditionalBlockInfo {
 
     /**
      * ʒu^ for ubN
      * 
      * @param ownerClass NX
      * @param outerSpace ÕubN
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public ForBlockInfo(final TargetClassInfo ownerClass, final LocalSpaceInfo outerSpace,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn) {
         super(ownerClass, outerSpace, fromLine, fromColumn, toLine, toColumn);
 
         this.initilizerExpressions = new TreeSet<ConditionInfo>();
         this.iteratorExpressions = new TreeSet<ExpressionInfo>();
 
     }
 
     /**
      * ϐp̈ꗗԂD
      * 
      * @return ϐpSet
      */
     @Override
     public Set<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> getVariableUsages() {
         final Set<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> variableUsages = new HashSet<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>>();
         variableUsages.addAll(super.getVariableUsages());
         for (final ConditionInfo initializerExpression : this.getInitializerExpressions()) {
             variableUsages.addAll(initializerExpression.getVariableUsages());
         }
         for (final ExpressionInfo iteratorExpression : this.getIteratorExpressions()) {
             variableUsages.addAll(iteratorExpression.getVariableUsages());
         }
         return Collections.unmodifiableSet(variableUsages);
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
         for (final ConditionInfo initializerExpression : this.getInitializerExpressions()) {
             definedVariables.addAll(initializerExpression.getDefinedVariables());
         }
         for (final ExpressionInfo iteratorExpression : this.getIteratorExpressions()) {
             definedVariables.addAll(iteratorExpression.getDefinedVariables());
         }
         return Collections.unmodifiableSet(definedVariables);
     }
 
     /**
      * for̃eLXg\iString^jԂ
      * 
      * @return for̃eLXg\iString^j
      */
     @Override
     public String getText() {
 
         final StringBuilder sb = new StringBuilder();
 
         sb.append("for (");
 
         final SortedSet<ConditionInfo> initializerExpressions = this.getInitializerExpressions();
         for (final ConditionInfo initializerExpression : initializerExpressions) {
             sb.append(initializerExpression.getText());
             if (initializerExpression instanceof StatementInfo) {
                 sb.deleteCharAt(sb.length() - 1);
             }
             sb.append(",");
         }
         if (0 < initializerExpressions.size()) {
             sb.deleteCharAt(sb.length() - 1);
         }
 
         sb.append(" ; ");
 
         final ConditionalClauseInfo conditionalClause = this.getConditionalClause();
         sb.append(conditionalClause.getText());
 
         sb.append(" ; ");
 
         final SortedSet<ExpressionInfo> iteratorExpressions = this.getIteratorExpressions();
         for (final ExpressionInfo iteratorExpression : iteratorExpressions) {
             sb.append(iteratorExpression.getText());
             sb.append(",");
         }
        if (0 < iteratorExpressions.size()) {
             sb.deleteCharAt(sb.length() - 1);
         }
 
         sb.append(") {");
         sb.append(System.getProperty("line.separator"));
 
         final SortedSet<StatementInfo> statements = this.getStatements();
         for (final StatementInfo statement : statements) {
             sb.append(statement.getText());
             sb.append(System.getProperty("line.separator"));
         }
 
         sb.append("}");
 
         return sb.toString();
     }
 
     /**
      * foȑǉ
      * @param initializerExpression 
      */
     public final void addInitializerExpressions(final ConditionInfo initializerExpression) {
         MetricsToolSecurityManager.getInstance().checkAccess();
 
         if (null == initializerExpression) {
             throw new IllegalArgumentException("initializerExpression is null");
         }
 
         this.initilizerExpressions.add(initializerExpression);
     }
 
     /**
      * for̍XVǉ
      * @param iteratorExpression JԂ
      */
     public final void addIteratorExpressions(final ExpressionInfo iteratorExpression) {
         MetricsToolSecurityManager.getInstance().checkAccess();
 
         if (null == iteratorExpression) {
             throw new IllegalArgumentException("updateExpression is null");
         }
 
         this.iteratorExpressions.add(iteratorExpression);
 
         // ֋XCiteratorExpression  ExpressionExpressionInfoŕ
         {
             final int fromLine = iteratorExpression.getFromLine();
             final int fromColumn = iteratorExpression.getFromColumn();
             final int toLine = iteratorExpression.getToLine();
             final int toColumn = iteratorExpression.getToColumn();
 
             final ExpressionStatementInfo ownerStatement = new ExpressionStatementInfo(this,
                     iteratorExpression, fromLine, fromColumn, toLine, toColumn);
             iteratorExpression.setOwnerExecutableElement(ownerStatement);
         }
     }
 
     /**
      * ̃ZbgԂ
      * @return ̃Zbg
      */
     public final SortedSet<ConditionInfo> getInitializerExpressions() {
         return Collections.unmodifiableSortedSet(this.initilizerExpressions);
     }
 
     /**
      * XṼZbgԂ
      * @return XV
      */
     public final SortedSet<ExpressionInfo> getIteratorExpressions() {
         return Collections.unmodifiableSortedSet(this.iteratorExpressions);
     }
 
     @Override
     public boolean isLoopStatement() {
         return true;
     }
 
     /**
      * ۑ邽߂̕ϐ
      */
     private final SortedSet<ConditionInfo> initilizerExpressions;
 
     /**
      * XVۑ邽߂̕ϐ
      */
     private final SortedSet<ExpressionInfo> iteratorExpressions;
 }
