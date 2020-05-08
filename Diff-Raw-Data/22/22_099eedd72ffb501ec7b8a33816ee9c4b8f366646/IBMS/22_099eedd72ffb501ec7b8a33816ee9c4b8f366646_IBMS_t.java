 package ibms;
 
 /*
  * A very simple application illustrating how to use the interface.
  * Prints the names of all the drivers in the database.
  * @author John Sargeant
  */
 public class IBMS {
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         database.openBusDatabase();
         int[] driverIDs = DriverInfo.getDrivers();
         String[] driverNames = new String [driverIDs.length];
         for (int i=0; i<driverIDs.length; i++)
             System.out.println(DriverInfo.getName(driverIDs[i]));
     }
 }
