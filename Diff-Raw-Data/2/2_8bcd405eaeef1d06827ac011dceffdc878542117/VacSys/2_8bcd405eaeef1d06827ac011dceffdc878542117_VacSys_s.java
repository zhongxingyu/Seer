 package vacsys;
 
 /**
  * System to regulate treatment to patients based on a number of criteria
  */
 public class VacSys {
 
     /**
      * Create a system with an empty priority queue
      */
     public VacSys() {
         // TODO Create a system with an empty priority queue
     }
 
     /**
      * Create a system loaded with requests from a batch file
      *
      * @param filename batch file
      */
     public VacSys(String filename) {
         // TODO Create a system loaded with requests from the batch ﬁle given by ﬁlename
     }
 
     /**
      * Add a new request to the system
      *
      * @param name of the new patient
      * @param age  of the new patient
      * @param zip  of the new patient
      * @return successful?
      */
     public boolean insert(String name, int age, String zip) {
         // TODO Add a new request to the system
         return false;
     }
 
     /**
      * Remove the next request from the system
      *
     * @return TODO figure out what we should return...
      */
     public String remove() {
         // TODO Remove the next request from the system
         return "";
     }
 
     /**
      * Remove num requests and store them in a CSV format
      *
      * @param num      of records to save
      * @param filename to save in
      * @return
      */
     public boolean remove(int num, String filename) {
         // TODO Remove num requests and store them in a CSV format in filename
         return false;
     }
 }
