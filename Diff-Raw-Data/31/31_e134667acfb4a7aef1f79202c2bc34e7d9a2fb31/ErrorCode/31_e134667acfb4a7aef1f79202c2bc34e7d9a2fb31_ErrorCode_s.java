 public enum ErrorCode {
   Success(0),
   Delayed(1),
   WriteToReadOnly(2),
   NoTransaction(3),
   VersionNotFound(4),
   OwnershipConflict(5),
   DuplicateTransaction(6),
   NeedsReadOnly(7),
   NeedsReadWrite(8),
   CacheSyncFailed(9),
   FileDoesNotExist(10),
   FileAlreadyExists(11),
   Timeout(20),
   FileTooLarge(30),//follows specs from assignment 1
   NetworkTimeout(35),
   Unknown(40);
 
 
   private int id;
 
   ErrorCode(int id) {
     if (ErrorMap.map.put(id, this) != null) {
       throw new IllegalStateException("Duplicate wire ID detected.");
     }
     this.id = id;
   }
 
   public int getId() {
     return id;
   }
 
   /**
    * Determine if this error is transient. A transient error is one
    * that may be resolved using a SyncRequest to update the cache.
    */
   public boolean isTransient() {
    return this.equals(ErrorCode.NeedsReadOnly) || 
      this.equals(ErrorCode.NeedsReadWrite);
   }
 }
