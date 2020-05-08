 package net.kokkeli.data;
 
 public enum FetchStatus {
     WAITING(0),
     HANDLING(1),
     HANDLED(2),
     ERROR(3);
     
     private final int status;
     
     /**
     * Creates role with given id.
      * @param id
      */
     private FetchStatus(int status){
         this.status = status;
     }
     
     /**
     * Id of Role
     * @return
      */
     public int getStatus(){
         return status;
     }
 }
