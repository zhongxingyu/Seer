 public enum Errors {
   Success(0),
   Delayed(1),
   OwnershipConflict(5),
   FileDoesNotExist(10),
   FileAlreadyExists(11),
   Timeout(20),
   FileTooLarge(30), //follows specs from assignment 1
   WriteToReadOnly(2);
 
   private int id;
 
   Errors(int id) {
    if (ErrorMap.map.put(id, this) != null) {
       throw new IllegalStateException("Duplicate wire ID detected.");
     }
     this.id = id;
   }
 
   public int getId() {
     return id;
   }
 }
