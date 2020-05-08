 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 @SuppressWarnings("serial")
 public class ContinueStatementInfo extends JumpStatementInfo {
 
     public ContinueStatementInfo(LocalSpaceInfo ownerSpace, LabelInfo destinationLabel,
             int fromLine, int fromColumn, int toLine, int toColumn) {
         super(ownerSpace, destinationLabel, fromLine, fromColumn, toLine, toColumn);
     }
 
     @Override
     protected String getReservedKeyword() {
         return "continue";
     }
 
     @Override
     public StatementInfo getFollowingStatement() {
         if (null != this.getDestinationLabel()) {
             return this.getDestinationLabel().getLabeledStatement();
         } else {
            if (this.getOwnerSpace() instanceof BlockInfo
                    && ((BlockInfo) this.getOwnerSpace()).isLoopStatement()) {
                return (BlockInfo) this.getOwnerSpace();
             }
            assert false;
             return null;
         }
     }
 
 }
