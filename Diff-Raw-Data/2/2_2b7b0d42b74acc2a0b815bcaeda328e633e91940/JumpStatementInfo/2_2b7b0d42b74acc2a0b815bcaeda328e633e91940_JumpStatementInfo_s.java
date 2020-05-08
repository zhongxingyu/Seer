 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Set;
 
 
 public abstract class JumpStatementInfo extends SingleStatementInfo {
 
     public JumpStatementInfo(final LocalSpaceInfo ownerSpace, final LabelInfo destinationLabel,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn) {
         super(ownerSpace, fromLine, fromColumn, toLine, toColumn);
 
         this.destinationLabel = destinationLabel;
     }
 
     @Override
     public final Set<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> getVariableUsages() {
         return VariableUsageInfo.EmptySet;
     }
 
     /**
      * `ꂽϐSetԂ
      * 
      * @return `ꂽϐSet
      */
     @Override
     public final Set<VariableInfo<? extends UnitInfo>> getDefinedVariables() {
         return VariableInfo.EmptySet;
     }
 
     /**
      * ĂяoSetԂ
      * 
      * @return ĂяoSet
      */
     @Override
     public final Set<CallInfo<?>> getCalls() {
         return CallInfo.EmptySet;
     }
 
     @Override
     public String getText() {
         final StringBuilder text = new StringBuilder(this.getReservedKeyword());
         if (null != this.getDestinationLabel()) {
             text.append(" ").append(this.getDestinationLabel().getName());
         }
         text.append(";");
         return text.toString();
     }
 
     protected abstract String getReservedKeyword();
 
     public BlockInfo getCorrespondingBlock() {
 
         if (null != this.getDestinationLabel()) {
             return (BlockInfo) this.getDestinationLabel().getLabeledStatement();
         } else {
 
             for (BlockInfo ownerBlock = (BlockInfo) this.getOwnerSpace();; ownerBlock = (BlockInfo) ownerBlock
                     .getOwnerSpace()) {
 
                if (ownerBlock.isLoopStatement()) {
                     return ownerBlock;
                 }
 
                 if (!(ownerBlock.getOwnerSpace() instanceof BlockInfo)) {
                     break;
                 }
             }
 
             assert false : "Here shouldn't be reached!";
             return null;
         }
     }
 
     //public abstract StatementInfo getFollowingStatement();
 
     public LabelInfo getDestinationLabel() {
         return this.destinationLabel;
     }
 
     private final LabelInfo destinationLabel;
 
 }
