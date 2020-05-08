 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * switch  case Gg\NX
  * 
  * @author higo
  */
 @SuppressWarnings("serial")
 public class CaseEntryInfo extends UnitInfo implements StatementInfo {
 
     /**
      * Ή switch ubN^ case Gg
      * 
      * @param ownerSwitchBlock  case Gg switch ubN
      * @param label  case Gg̃x
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public CaseEntryInfo(final SwitchBlockInfo ownerSwitchBlock, final ExpressionInfo label,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn) {
 
         super(fromLine, fromColumn, toLine, toColumn);
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == ownerSwitchBlock) || (null == label)) {
             throw new IllegalArgumentException();
         }
 
         this.ownerSwitchBlock = ownerSwitchBlock;
         this.label = label;
 
         this.label.setOwnerExecutableElement(this);
     }
 
     /**
      * Ή switch ubN^ case Gg
      * 
      * @param ownerSwitchBlock  case Gg switch ubN
      * @param breakStatement  case Gg break ǂ
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     protected CaseEntryInfo(final SwitchBlockInfo ownerSwitchBlock, final int fromLine,
             final int fromColumn, final int toLine, final int toColumn) {
 
         super(fromLine, fromColumn, toLine, toColumn);
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == ownerSwitchBlock) {
             throw new IllegalArgumentException();
         }
 
         this.ownerSwitchBlock = ownerSwitchBlock;
         this.label = null;
     }
 
     /**
      * ̕icase GgjŗpĂϐp̈ꗗԂD
      * ǂ̕ϐpĂȂ̂ŁCsetԂ
      * 
      * @return ϐpSet
      */
     @Override
     public Set<VariableUsageInfo<? extends VariableInfo<? extends UnitInfo>>> getVariableUsages() {
         return VariableUsageInfo.EmptySet;
     }
 
     /**
      * ϐ`SetԂ
      * 
      * @return ϐ`SetԂ
      */
     @Override
     public Set<VariableInfo<? extends UnitInfo>> getDefinedVariables() {
         return VariableInfo.EmptySet;
     }
 
     /**
      * ĂяoSetԂ
      * 
      * @return ĂяoSet
      */
     @Override
     public Set<CallInfo<?>> getCalls() {
         return CallInfo.EmptySet;
     }
 
     /**
      * caseGg̃eLXg\iString^jԂ
      * 
      * @return caseGg̃eLXg\iString^j
      */
     @Override
     public String getText() {
 
         final StringBuilder sb = new StringBuilder();
 
         sb.append("case ");
 
         final ExpressionInfo expression = this.getLabel();
         sb.append(expression.getText());
 
         sb.append(":");
 
         return sb.toString();
     }
 
     /**
      *  case Gg switch ubNԂ
      * 
      * @return  case Gg switch ubN
      */
     public final SwitchBlockInfo getOwnerSwitchBlock() {
         return this.ownerSwitchBlock;
     }
 
     @Override
     public final LocalSpaceInfo getOwnerSpace() {
         return this.getOwnerSwitchBlock();
     }
 
     @Override
     public CallableUnitInfo getOwnerMethod() {
         return this.getOwnerSwitchBlock().getOwnerMethod();
     }
 
     /**
      *  case Gĝ̃xԂ
      * 
      * @return  case Gg̃x
      */
     public final ExpressionInfo getLabel() {
         return this.label;
     }
 
     /**
      * caseGg̃nbVR[hԂ
      */
     @Override
     public final int hashCode() {
        return this.getOwnerSwitchBlock().hashCode() + this.getLabel().hashCode();
     }
 
     /**
      * ̎œ\OSetԂ
      * 
      * @return@̎œ\OSet
      */
     @Override
     public Set<ClassTypeInfo> getThrownExceptions() {
         return Collections.unmodifiableSet(new HashSet<ClassTypeInfo>());
     }
 
     /**
      *  case Gg switch ubNۑ邽߂̕ϐ
      */
     private final SwitchBlockInfo ownerSwitchBlock;
 
     /**
      *  case Gg̃xۑϐ
      */
     private ExpressionInfo label;
 }
