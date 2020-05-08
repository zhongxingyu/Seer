 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * ϐ̎gp⃁\bȟĂяoȂǁCvOvf̎gp\NX
  * 
  * @author higo
  *
  */
 public abstract class ExpressionInfo implements ConditionInfo {
 
     /**
      *
      * @param ownerMethod I[i[\bh
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     ExpressionInfo(final CallableUnitInfo ownerMethod, final int fromLine, final int fromColumn,
             final int toLine, final int toColumn) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
 
         this.ownerExecutableElement = null;
         this.ownerMethod = ownerMethod;
         this.fromLine = fromLine;
         this.fromColumn = fromColumn;
         this.toLine = toLine;
         this.toColumn = toColumn;
     }
 
     /**
      * Œ`ꂽϐSetԂ
      * 
      * @return Œ`ꂽϐSet 
      */
     @Override
     public final Set<VariableInfo<? extends UnitInfo>> getDefinedVariables() {
         return VariableInfo.EmptySet;
     }
 
     /**
      * GeBeBgp̌^ԂD
      * 
      * @return GeBeBgp̌^
      */
     public abstract TypeInfo getType();
 
     /**
      * JnsԂ
      * 
      * @return Jns
      */
     public final int getFromLine() {
         return this.fromLine;
     }
 
     /**
      * JnԂ
      * 
      * @return Jn
      */
     public final int getFromColumn() {
         return this.fromColumn;
     }
 
     /**
      * IsԂ
      * 
      * @return Is
      */
     public final int getToLine() {
         return this.toLine;
     }
 
     /**
      * IԂ
      * 
      * @return I
      */
     public final int getToColumn() {
         return this.toColumn;
     }
 
     @Override
     public final int compareTo(Position o) {
 
         if (null == o) {
             throw new NullPointerException();
         }
 
        if (o instanceof ExecutableElementInfo) {
            final ClassInfo ownerClass1 = this.getOwnerMethod().getOwnerClass();
            final ClassInfo ownerClass2 = ((ExecutableElementInfo) o).getOwnerMethod()
                    .getOwnerClass();
            int classOrder = ownerClass1.compareTo(ownerClass2);
            if (0 != classOrder) {
                return classOrder;
            }
        }

         if (this.getFromLine() < o.getFromLine()) {
             return -1;
         } else if (this.getFromLine() > o.getFromLine()) {
             return 1;
         } else if (this.getFromColumn() < o.getFromColumn()) {
             return -1;
         } else if (this.getFromColumn() > o.getFromColumn()) {
             return 1;
         } else if (this.getToLine() < o.getToLine()) {
             return -1;
         } else if (this.getToLine() > o.getToLine()) {
             return 1;
         } else if (this.getToColumn() < o.getToColumn()) {
             return -1;
         } else if (this.getToColumn() > o.getToColumn()) {
             return 1;
         }
 
         return 0;
     }
 
     /**
      * OExecutableElementԂ
      * 
      * @return OExecutableElement
      */
     public final ExecutableElementInfo getOwnerExecutableElement() {
 
         if (null == this.ownerExecutableElement) {
             throw new IllegalStateException();
         }
 
         return this.ownerExecutableElement;
     }
 
     /**
      * ̎Ԃ
      * 
      * @return ̎
      */
     public final StatementInfo getOwnerStatement() {
 
         final ExecutableElementInfo ownerExecutableElement = this.getOwnerExecutableElement();
         if (ownerExecutableElement instanceof StatementInfo) {
             return (StatementInfo) ownerExecutableElement;
         }
 
         if (ownerExecutableElement instanceof ExpressionInfo) {
             return ((ExpressionInfo) ownerExecutableElement).getOwnerStatement();
         }
 
         // ownerExecutableElement  StatementInfo ł ExpressionInfo@łȂƂIllegalStateException
         throw new IllegalStateException(
                 "ownerExecutableElement must be StatementInfo or ExpressionInfo.");
         //return null;
     }
 
     /**
      * ڂ̃I[i[łExecutableElementZbg
      * 
      * @param ownerExecutableElement ڂ̃I[i[łExecutableElement
      */
     public void setOwnerExecutableElement(final ExecutableElementInfo ownerExecutableElement) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == ownerExecutableElement) {
             throw new IllegalArgumentException();
         }
 
         this.ownerExecutableElement = ownerExecutableElement;
     }
 
     /**
      * I[i[\bhԂ
      * 
      * @return I[i[\bh
      */
     @Override
     public final CallableUnitInfo getOwnerMethod() {
         return this.ownerMethod;
     }
 
     /**
      * 𒼐ڏLԂԂ
      * 
      * @return 𒼐ڏL
      */
     @Override
     public final LocalSpaceInfo getOwnerSpace() {
         return this.getOwnerStatement().getOwnerSpace();
     }
 
     private ExecutableElementInfo ownerExecutableElement;
 
     /**
      * I[i[\bhۑ邽߂̕ϐ
      */
     private final CallableUnitInfo ownerMethod;
 
     /**
      * Jnsۑ邽߂̕ϐ
      */
     private final int fromLine;
 
     /**
      * Jnۑ邽߂̕ϐ
      */
     private final int fromColumn;
 
     /**
      * Isۑ邽߂̕ϐ
      */
     private final int toLine;
 
     /**
      * Jnۑ邽߂̕ϐ
      */
     private final int toColumn;
 
 }
