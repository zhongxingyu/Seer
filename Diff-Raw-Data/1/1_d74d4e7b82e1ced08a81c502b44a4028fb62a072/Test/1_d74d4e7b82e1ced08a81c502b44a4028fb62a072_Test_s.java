 /*
  * A very simple application illustrating how to use the interface.
  * Prints the names of all the drivers in the database.
  * @author John Sargeant
  */
 public class Test {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         database.openBusDatabase();
         int[] driverIDs = DriverInfo.getDrivers();
         //String[] driverNames = new String [driverIDs.length];
         
         //for (int i=0; i<driverIDs.length; i++)
            //if driverIDs[i].findDriver
            // System.out.println("ID: "+driverIDs[i]);
         
         for (int i=0; i<driverIDs.length; i++)
           System.out.println("Driver: " + DriverInfo.getName(driverIDs[i]) + " ID: " + driverIDs[i]);   
        
         int ID = 2052;
         String name = "Wayes Chawdoury";
         System.out.println("This is " + DriverInfo.isInDatabase(ID,name));
         
         
         System.out.println("Number: " + DriverInfo.getNumber(ID));
         System.out.println("ID: " + DriverInfo.findDriver(DriverInfo.getNumber(ID)));
         System.out.println("Number: " + DriverInfo.getName(ID));
         
         String ID2 = "2052";
         name = "Waud Al-Amri";
         System.out.println("This is " + DriverInfo.isInDatabase(Integer.parseInt(ID2),name));
     }
 
 }
