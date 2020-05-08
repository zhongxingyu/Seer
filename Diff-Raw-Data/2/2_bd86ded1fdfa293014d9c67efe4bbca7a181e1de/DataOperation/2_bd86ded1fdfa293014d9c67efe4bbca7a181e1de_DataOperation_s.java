 package jabara.general.io;
 
 import jabara.general.ArgUtil;
 
 import java.io.Serializable;
 
 /**
  * @author jabaraster -
  */
 public class DataOperation implements Serializable {
     private static final long   serialVersionUID = -3960649395769259898L;
 
     private final Operation     operation;
     private final IReadableData data;
 
     /**
      * @param pOperation -
      * @param pData pOperationが{@link Operation#UPDATE}以外の場合、ここで指定されたオブジェクトは使われません.
      */
     public DataOperation(final Operation pOperation, final IReadableData pData) {
         ArgUtil.checkNull(pOperation, "pOperation"); //$NON-NLS-1$
         if (this.operation == Operation.UPDATE && pData == null) {
             throw new IllegalArgumentException("pOperationがUPDATEのとき、pDataはnullであってはいけません."); //$NON-NLS-1$
         }
 
         this.operation = pOperation;
         this.data = this.operation == Operation.UPDATE ? pData : null;
     }
 
     /**
      * @return dataを返す.
      * @throws IllegalStateException operationがUPDATE以外の場合.
      */
     public IReadableData getData() {
         if (this.data == null) {
             throw new IllegalStateException("operationがUPDATEでなければこのメソッドは呼び出せません."); //$NON-NLS-1$
         }
         return this.data;
     }
 
     /**
      * @return operationを返す.
      */
     public Operation getOperation() {
         return this.operation;
     }
 
     /**
      * @return -
      */
     public boolean hasData() {
        return this.data == null;
     }
 
     /**
      * @see java.lang.Object#toString()
      */
     @SuppressWarnings("nls")
     @Override
     public String toString() {
         return "DataOperation [operation=" + this.operation + ", data=" + this.data + "]";
     }
 
     /**
      * @author jabaraster -
      */
     public enum Operation {
         /**
          * 
          */
         NOOP,
         /**
          * 
          */
         UPDATE,
         /**
          * 
          */
         DELETE, ;
     }
 }
