 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ibms;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Ben
  */
 public class JourneyPlanner {
 
     /**
      * @param args the command line arguments
      */
     public static String outMessage="";
 
     public static ArrayList<Integer> sameRoutesInBusStop(int busStopNum) {
         int[] allRoutes = BusStopInfo.getRoutes(busStopNum);
 
         int route = 65;
         ArrayList<Integer> routesAtStop =  new ArrayList<Integer>();
         while(route <= 68) {
             for(int i = 0; i < allRoutes.length;i++) {
                 if(route == allRoutes[i]) {
                     routesAtStop.add(route);
                     break;
                 }
             }
 
             route++;
         }
         return routesAtStop;
     } // sameRoutesInBusStop
 
 
     public static String getRoutes(String fromArea, String fromStop,String toArea, String toStop) {
 
         database.openBusDatabase();
 
         //Get the area number from bus stop info
         int fromAreaId = BusStopInfo.findAreaByName(fromArea);
         int toAreaId = BusStopInfo.findAreaByName(toArea);
 
         //Get the area code in the form ROM = Romiley'
         String fromAreaCode = BusStopInfo.getAreaCode(fromAreaId);
         String toAreaCode = BusStopInfo.getAreaCode(toAreaId);
 
         //Check that we are passing the correct arguments through
         System.out.println("From Area: " + fromArea + ", From bus stop: "
                         +  fromStop + ", To area: "  +  toArea
                         + "to bus stop," + toStop);
 
         //Check that we are getting the correct area codes
         System.out.println("\nFrom Area: " + fromAreaCode + ", Id: "
                         + fromAreaId + ", From bus stop: "
                         + fromStop + ", To area: "  +  toAreaCode
                         + " Id: " + toAreaId
                         + " to bus stop," + toStop);
 
 
         // Get the bus stop number for the 'from' and 'to' bus stop
         int fromBusStopId = BusStopInfo.findBusStop(fromAreaCode, fromStop);
         int toBusStopId = BusStopInfo.findBusStop(toAreaCode, toStop);
         
         //Output the bus stop codes
         System.out.println("From bus stop id " + fromBusStopId);
         System.out.println("To bus stop id " + toBusStopId);
 
         //Check that the stops are located in the relavent areas
         if(fromAreaId == 0) {
             outMessage += "Bus stop '" + fromStop + "' is not located in the "
                    + fromArea + " area.\n";
         }
         if(toAreaId == 0) {
             outMessage += "Bus stop '" + toStop + "' is not located in the "
                    + toArea + " area.\n";
         }
 
         if((fromBusStopId == 0) || toBusStopId == 0) {
             outMessage += "Select a bus stop which is located in the correct area\n";
         } else if((fromArea.equals(toArea)) && (fromStop.equals(toStop))) {
             outMessage += "The origin and destination information cannot be the same";
         } else {
             //the information is legal
             ArrayList<Integer> fromList =  new ArrayList<Integer>();
             ArrayList<Integer> toList =  new ArrayList<Integer>();
 
             fromList = sameRoutesInBusStop(fromAreaId);
             toList = sameRoutesInBusStop(toBusStopId);
 
             ArrayList<Integer> possibleRoutes = new ArrayList<Integer>();
 
             for(int i = 0 ; i < fromList.size(); i++) {
                 for(int j = 0 ; j < toList.size(); j++) {
                     if(fromList.get(i) == toList.get(j)) {
                         possibleRoutes.add(toList.get(j));
                         break;
                     }
                 }
             }
 
             //Check that the above code returns a list of routes possible
             System.out.print("Possible routes: ");
             for (int i = 0; i < possibleRoutes.size();i++) {
                 System.out.print(possibleRoutes.get(i) + " ");
             }
 
             int currentStopNum;
             int noOfStops = possibleRoutes.size();
 
             int[] stops = new int[noOfStops];
             if(noOfStops != 0) {
 
                 int routeIndex = 0;
 
                 while (routeIndex < noOfStops) {
                     currentStopNum = fromAreaId;
                     stops[routeIndex] = 0;
 
                     //While destination has not been reached
                     while (currentStopNum != toBusStopId) {
                         currentStopNum = BusStopInfo.getNextStop(currentStopNum, possibleRoutes.get(routeIndex));
                         stops[routeIndex] ++;
 
                         if (currentStopNum == 0) {
                             possibleRoutes.set(routeIndex, -1);
                             stops[routeIndex] = -1;
                             currentStopNum = fromAreaId;
                             break;
                         }
                     }
                     routeIndex ++;
 
                 }
             } else {
                 ArrayList<Integer> currentBusStopRoutes = new ArrayList<Integer>();
                 currentStopNum = fromAreaId;
                 int routeIndex = 0;
                 while(routeIndex < fromList.size()) {
                     while(currentStopNum != toBusStopId) {
                         currentStopNum = BusStopInfo.getNextStop(currentStopNum, possibleRoutes.get(routeIndex));
                         currentBusStopRoutes = sameRoutesInBusStop(currentStopNum);
 
                     }
                 }
             }
 
             outMessage += "Origin: " + fromStop + ", " + fromArea + "\n";
             outMessage += "Destination: " + toStop + ", " + toArea + "\n";
             outMessage +="\n";
             outMessage += "You can take the following routes: \n";
 
             for(int i = 0; i < possibleRoutes.size();i++) {
                 if((possibleRoutes.get(i) != -1) && (stops[i] != -1)) {
                     outMessage += "Route: " + possibleRoutes.get(i) + ".\n";
                     outMessage += "Number of stops between: " + stops[i] + "\n";
                 }
             }
         }
 
 
         return outMessage;
     }//getRoutes
 
 
       /**
    * Test method of the roster
    */
   public static void main(String[] args) throws Exception {
     try {
      System.out.println("YAY");
      database.openBusDatabase();
      int number = BusStopInfo.findBusStop("SKP", "Bus Station");
      System.out.println(number);
 
     } catch (Exception ex) {
       Logger.getLogger(JourneyPlanner.class.getName()).log(Level.SEVERE, null, ex);
     } /*catch (Exception ex) {
       System.out.println("Something went wrong: "+ex.getMessage());
       System.out.println(ex);
     }*/
   }
   //read input  
 }
