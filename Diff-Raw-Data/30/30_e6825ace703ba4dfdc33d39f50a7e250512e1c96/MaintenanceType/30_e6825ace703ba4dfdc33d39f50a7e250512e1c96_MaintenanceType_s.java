 package carrental;
 
 /**
  * MaintenanceType holds information on each maintenance type
  * @author CNN
  * @version 2011-12-04
  */
 public class MaintenanceType {
 
     private final int ID;
     private String name;
    private boolean is_service;//TODO What is this boolean for?
    // Answer in a comment and I'll fix the documentation
 
     /**
      * Constructor for MaintenanceType
      * @param ID  This is the ID of the MaintenanceType
      * @param name This is the name of the MaintenanceType
      * @param is_service
      */
     public MaintenanceType(int ID, String name, boolean is_service) {
         this.ID = ID;
         this.name = name;
         this.is_service = is_service;
     }
 
     /**
      * Update object information
      * @param name  This is the name of the MaintenanceType
      * @param is_service 
      */
     public void updateObject(String name, boolean is_service) {
         this.name = name;
         this.is_service = is_service;
     }
 
     /**
      * Getter for the
      * @return 
      */
     public boolean getIs_service() {
         return is_service;
     }
 
     /**
      * Getter for the name of this object.
      * @return the name of the MaintenanceType
      */
     public String getName() {
         return name;
     }
     
     public int getID() {
         return ID;
     }
 }
