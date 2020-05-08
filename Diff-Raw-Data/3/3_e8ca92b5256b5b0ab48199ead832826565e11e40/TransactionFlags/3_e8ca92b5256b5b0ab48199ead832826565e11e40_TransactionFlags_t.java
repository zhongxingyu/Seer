 public enum TransactionFlags implements OrderedEnum {
   Start(1), Abort(2), Commit(3), Confirm(4);
 
  public static final TransactionFlags[] byWireId = { null, Start, Abort, Confirm };
 
   private int id;
 
   TransactionFlags(int id) {
     this.id = id;
   }
 
   public int getId() {
     return id;
   }
 }
