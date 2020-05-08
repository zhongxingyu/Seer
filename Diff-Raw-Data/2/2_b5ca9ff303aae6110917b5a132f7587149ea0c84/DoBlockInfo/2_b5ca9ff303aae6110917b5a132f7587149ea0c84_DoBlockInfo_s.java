 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.SortedSet;
 
 
 /**
  * do ubN\NX
  * 
  * @author higo
  * 
  */
 @SuppressWarnings("serial")
 public final class DoBlockInfo extends ConditionalBlockInfo {
 
     /**
      * ʒu^ do ubN
      * 
      * @param ownerClass LNX
      * @param outerSpace ÕubN
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public DoBlockInfo(final TargetClassInfo ownerClass, final LocalSpaceInfo outerSpace,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn) {
         super(ownerClass, outerSpace, fromLine, fromColumn, toLine, toColumn);
     }
 
     /**
      * DõeLXg\iString^jς
      * 
      * @return DõeLXg\iString^j
      */
     @Override
     public String getText() {
 
         final StringBuilder sb = new StringBuilder();
 
         sb.append("do {");
         sb.append(System.getProperty("line.separator"));
 
         final SortedSet<StatementInfo> statements = this.getStatements();
         for (final StatementInfo statement : statements) {
             sb.append(statement.getText());
             sb.append(System.getProperty("line.separator"));
         }
 
         sb.append("} while (");
 
         final ConditionalClauseInfo conditionalClause = this.getConditionalClause();
         sb.append(conditionalClause.getText());
 
        sb.append(")");
 
         return sb.toString();
 
     }
     
     @Override
     public boolean isLoopStatement() {
         return true;
     }
 }
