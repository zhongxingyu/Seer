 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.LocalSpaceInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.SingleStatementInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * ubNȊO̖\NX
  * 
  * @author higo
  *
  * @param <T> ς݂̌^
  */
 public abstract class UnresolvedSingleStatementInfo<T extends SingleStatementInfo> implements
         UnresolvedStatementInfo<T> {
 
     protected UnresolvedSingleStatementInfo(
             final UnresolvedLocalSpaceInfo<? extends LocalSpaceInfo> ownerSpace) {
         this(ownerSpace, 0, 0, 0, 0);
     }
 
     protected UnresolvedSingleStatementInfo(
             final UnresolvedLocalSpaceInfo<? extends LocalSpaceInfo> ownerSpace,
             final int fromLine, final int fromColumn, final int toLine, final int toColumn) {
         MetricsToolSecurityManager.getInstance().checkAccess();
 
         this.ownerSpace = ownerSpace;
         this.setFromLine(fromLine);
        this.setFromColumn(fromColumn);
         this.setToLine(toLine);
         this.setToColumn(toColumn);
     }
 
     protected UnresolvedLocalSpaceInfo<? extends LocalSpaceInfo> getOwnerSpace() {
         return this.ownerSpace;
     }
 
     @Override
     public final boolean alreadyResolved() {
         return null != this.resolvedInfo;
     }
 
     @Override
     public final T getResolved() {
         return this.resolvedInfo;
     }
 
     @Override
     public int compareTo(UnresolvedExecutableElementInfo<?> o) {
 
         if (null == o) {
             throw new NullPointerException();
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
 
     @Override
     public final void setFromColumn(int column) {
         if (column < 0) {
             throw new IllegalArgumentException();
         }
 
         this.fromColumn = column;
     }
 
     @Override
     public final void setFromLine(int line) {
         if (line < 0) {
             throw new IllegalArgumentException();
         }
 
         this.fromLine = line;
     }
 
     @Override
     public final void setToColumn(int column) {
         if (column < 0) {
             throw new IllegalArgumentException();
         }
 
         this.toColumn = column;
     }
 
     @Override
     public final void setToLine(int line) {
         if (line < 0) {
             throw new IllegalArgumentException();
         }
 
         this.toLine = line;
     }
 
     @Override
     public final int getFromColumn() {
         return this.fromColumn;
     }
 
     @Override
     public final int getFromLine() {
         return this.fromLine;
     }
 
     @Override
     public final int getToColumn() {
         return this.toColumn;
     }
 
     @Override
     public final int getToLine() {
         return this.toLine;
     }
 
     private UnresolvedLocalSpaceInfo<? extends LocalSpaceInfo> ownerSpace;
 
     /**
      * Jns\ϐ
      */
     private int fromLine;
 
     /**
      * Jn\ϐ
      */
     private int fromColumn;
 
     /**
      * Is\ϐ
      */
     private int toLine;
 
     /**
      * I\ϐ
      */
     private int toColumn;
 
     /**
      * ςݏۑϐ
      */
     protected T resolvedInfo;
 }
