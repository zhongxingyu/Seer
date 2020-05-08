 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
 import java.util.SortedSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * catch ubN\NX
  * 
  * @author higo
  */
 @SuppressWarnings("serial")
 public final class CatchBlockInfo extends BlockInfo {
 
     /**
      * Ή try ubN^ catch ubN
      * 
      * @param ownerClass LNX
      * @param outerSpace ÕubN
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      * @param ownerTryBlock catch߂tryubN
      * @param caughtException catch߂Lb`O
      */
     public CatchBlockInfo(final TargetClassInfo ownerClass, final LocalSpaceInfo outerSpace,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn,
             final TryBlockInfo ownerTryBlock) {
 
         super(ownerClass, outerSpace, fromLine, fromColumn, toLine, toColumn);
 
         if (null == ownerTryBlock) {
             throw new NullPointerException();
         }
 
         this.ownerTryBlock = ownerTryBlock;
     }
 
     /**
      * Ή try ubNԂ
      * 
      * @return Ή try ubN
      */
     public final TryBlockInfo getOwnerTryBlock() {
         return this.ownerTryBlock;
     }

     /**
      * catchO\ϐ̏Ԃ
      * @return catchO\ϐ̏
      */
     public final LocalVariableInfo getCaughtException() {
         return caughtException;
     }

     public void setCaughtException(LocalVariableInfo caughtException) {
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == caughtException) {
             throw new NullPointerException();
         }

        if (null != this.caughtException) {
             throw new IllegalStateException();
         }
 
         this.caughtException = caughtException;
     }
 
    @Override
    public Set<VariableInfo<? extends UnitInfo>> getDefinedVariables() {
        final Set<VariableInfo<? extends UnitInfo>> definedVariables = new HashSet<VariableInfo<? extends UnitInfo>>(
                super.getDefinedVariables());
        definedVariables.add(this.caughtException);
        return Collections.unmodifiableSet(definedVariables);

    }

     /**
      * catch@̃eLXg\iString^jԂ
      * 
      * @return catch@̃eLXg\iString^j
      */
     @Override
     public String getText() {
 
         final StringBuilder sb = new StringBuilder();
 
         sb.append("catch (UNDER IMPLEMENTATION) {");
         sb.append(System.getProperty("line.separator"));
 
         final SortedSet<StatementInfo> statements = this.getStatements();
         for (final StatementInfo statement : statements) {
             sb.append(statement.getText());
             sb.append(System.getProperty("line.separator"));
         }
 
         sb.append("}");
 
         return sb.toString();
     }
 
     private final TryBlockInfo ownerTryBlock;
 
     private LocalVariableInfo caughtException;
 }
