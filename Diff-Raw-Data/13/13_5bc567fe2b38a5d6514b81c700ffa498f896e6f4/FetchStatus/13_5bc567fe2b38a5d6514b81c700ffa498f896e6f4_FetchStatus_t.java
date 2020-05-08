 package net.kokkeli.data;
 
/**
 * Fetch status enum
 * @author Hekku2
 *
 */
 public enum FetchStatus {
     WAITING(0),
     HANDLING(1),
     HANDLED(2),
     ERROR(3);
     
     private final int status;
     
     /**
     * Creates fetch status with given id.
      * @param id
      */
     private FetchStatus(int status){
         this.status = status;
     }
     
     /**
     * Id of status
     * @return id
      */
     public int getStatus(){
         return status;
     }
 }
