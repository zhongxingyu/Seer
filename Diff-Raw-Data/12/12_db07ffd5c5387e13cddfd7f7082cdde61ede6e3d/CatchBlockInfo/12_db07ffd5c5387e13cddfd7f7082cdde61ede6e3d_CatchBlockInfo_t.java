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
 public final class CatchBlockInfo extends BlockInfo implements SubsequentialBlockInfo<TryBlockInfo> {
 
     /**
      * ExecutableElementƗO͂ƂāCExecutableElement܂tryɑΉāCexceptionLb`catchBlockԂD
      * Ĉ悤ȃLb`߂Ȃꍇnull ԂD
      * 
      * @param element
      * @param exception
      * @return
      */
     public static CatchBlockInfo getCorrespondingCatchBlock(final ExecutableElementInfo element,
             final ReferenceTypeInfo exception) {
 
        assert exception instanceof ClassTypeInfo : "exception must be an instance of ClassTypeInfo";
        final ClassInfo exceptionClass = ((ClassTypeInfo) exception).getReferencedClass();

         for (LocalSpaceInfo ownerSpace = element.getOwnerSpace(); ownerSpace instanceof BlockInfo; ownerSpace = ((BlockInfo) ownerSpace)
                 .getOwnerSpace()) {
 
             if (ownerSpace instanceof TryBlockInfo) {
                 for (final CatchBlockInfo catchBlock : ((TryBlockInfo) ownerSpace)
                         .getSequentCatchBlocks()) {
                     final VariableInfo<?> caughtVariable = catchBlock.getCaughtException();

                    assert caughtVariable.getType() instanceof ClassTypeInfo : "caughtVariable.getType() must return an instance of ClassTypeInfo";
                    final ClassInfo variableClass = ((ClassTypeInfo) caughtVariable.getType())
                            .getReferencedClass();

                    if (exceptionClass.equals(variableClass)
                            || exceptionClass.isSubClass(variableClass)) {
                         return catchBlock;
                     }
                 }
 
             }
         }
 
         return null;
     }
 
     /**
      * Ή try ubN^ catch ubN
      * 
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public CatchBlockInfo(final int fromLine, final int fromColumn, final int toLine,
             final int toColumn) {
 
         super(fromLine, fromColumn, toLine, toColumn);
     }
 
     /**
      * Ή try ubNԂ
      * ̃\bh͏p~邽߁Cgp͐Ȃ
      * {@link CatchBlockInfo#getOwnerBlock()} gpׂłD
      * 
      * @return Ή try ubN
      * @deprecated
      */
     public final TryBlockInfo getOwnerTryBlock() {
         return this.ownerTryBlock;
     }
 
     /**
      * Ή try ubNԂ
      * 
      * @return Ή try ubN
      */
     @Override
     public TryBlockInfo getOwnerBlock() {
         assert null != this.ownerTryBlock : "this.ownerTryBlock must not be null!";
         return this.ownerTryBlock;
     }
 
     @Override
     public void setOwnerBlock(final TryBlockInfo ownerTryBlock) {
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == ownerTryBlock) {
             throw new NullPointerException();
         }
 
         if (null != this.ownerTryBlock) {
             throw new IllegalStateException();
         }
 
         this.ownerTryBlock = ownerTryBlock;
     }
 
     /**
      * catchO\ϐ̏Ԃ
      * @return catchO\ϐ̏
      */
     public final LocalVariableInfo getCaughtException() {
         assert null != this.caughtException : "this.caughtException must not be null!";
         return this.caughtException;
     }
 
     /**
      * catch߂Ŏ󂯂OZbg
      *  
      * @param caughtException catch߂Ŏ󂯂O
      */
     public void setCaughtException(final LocalVariableInfo caughtException) {
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
 
         sb.append("catch (");
 
         final LocalVariableInfo caughtException = this.getCaughtException();
         sb.append(caughtException.getType().getTypeName());
         sb.append(" ");
         sb.append(caughtException.getName());
 
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
 
     @Override
     public ExecutableElementInfo copy() {
 
         final int fromLine = this.getFromLine();
         final int fromColumn = this.getFromColumn();
         final int toLine = this.getToLine();
         final int toColumn = this.getToColumn();
 
         final CatchBlockInfo newCatchBlock = new CatchBlockInfo(fromLine, fromColumn, toLine,
                 toColumn);
 
         final TryBlockInfo ownerTryBlock = this.getOwnerBlock();
         newCatchBlock.setOwnerBlock(ownerTryBlock);
 
         final LocalVariableInfo caughtException = this.getCaughtException();
         newCatchBlock.setCaughtException(caughtException);
 
         final UnitInfo outerUnit = this.getOuterUnit();
         newCatchBlock.setOuterUnit(outerUnit);
 
         for (final StatementInfo statement : this.getStatementsWithoutSubsequencialBlocks()) {
             newCatchBlock.addStatement((StatementInfo) statement.copy());
         }
 
         return newCatchBlock;
     }
 
     private TryBlockInfo ownerTryBlock;
 
     private LocalVariableInfo caughtException;
 }
