 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.SortedSet;
 
 
 /**
  * switch ubN\NX
  * 
  * @author higo
  * 
  */
 @SuppressWarnings("serial")
 public final class SwitchBlockInfo extends ConditionalBlockInfo {
 
     /**
      * switch ubN
      *
      * @param ownerClass LNX
      * @param outerSpace ÕubN
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public SwitchBlockInfo(final TargetClassInfo ownerClass, final LocalSpaceInfo outerSpace,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn) {
         super(ownerClass, outerSpace, fromLine, fromColumn, toLine, toColumn);
     }
 
     /**
      * switch̃eLXg\i^jԂ
      * 
      * @return switch̃eLXg\i^j
      */
     @Override
     public String getText() {
 
         final StringBuilder sb = new StringBuilder();
 
         sb.append("switch (");
 
        final ConditionInfo conditionInfo = this.getConditionalClause().getCondition();
        sb.append(conditionInfo.getText());
 
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
 }
