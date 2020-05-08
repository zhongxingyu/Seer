 package model;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  *
  * @author Daniel Bond
  */
 public class SimulationStats {
     
     private static int truckCount = 0;
     private static int carCount = 0;
     private static BufferedWriter outputStream;
     private static ArrayList<Event> allEvents = new ArrayList<>(32);
     
     
     
     public static void reset(){
         truckCount = 0;
         carCount = 0;
     }
     
     public static void incrementCars() {
         carCount++;
     }
     
     public static void incrementTrucks() {
         truckCount++;
     }
     
     public static int getCarCount() {
         return carCount;
     }
     
     public static int getTruckCount() { 
         return truckCount;
     }
     
     public static void setCarCount(int value) {
         carCount = value;
     }
     
     public static void setTruckCount(int value) { 
         truckCount = value;
     }
     
     public static void startBuffer(String filePath){
         try{
             outputStream = new BufferedWriter(new FileWriter(filePath));
         } catch(IOException e){
             e.printStackTrace();
         }
         
     }
     
     public static void addEvent(Event e){
         allEvents.add(e);
     }
     
     public static void outputEvents(){
         System.out.println(allEvents.size());
         for(Event e: allEvents){ //print single events details
             try{
                outputStream.write("name of event: " + e.getName());
                 outputStream.newLine();
                 outputStream.write("Time step: " + e.getTimeStep());
                 outputStream.newLine();
                 outputStream.write("Aggression value: " + e.getAggression());
                 outputStream.newLine();
                 outputStream.write("Vehicle Minimum Density: " + e.getMinDensity());
                 outputStream.newLine();
                 outputStream.write("Vehicle Maximum Density: " + e.getMaxDensity());
                 outputStream.newLine();
                 outputStream.write("Ratio of Cars in Simulation: " + e.getRatioCars());
                 outputStream.newLine();
                 outputStream.write("Ratio of Trucks in Simulation: " + e.getRatioTrucks());
                 outputStream.newLine();
                 outputStream.write("Maximum Speed of Vehicles in Simulation: " + e.getMaxSpeed());
                 outputStream.newLine();
             }catch(IOException ioe){
                 ioe.printStackTrace();
             }
         } 
         //print a summary of the whole set of events
         try{
             outputStream.newLine();
            outputStream.newLine();
             outputStream.write("Total output of Cars: " + carCount);
             outputStream.newLine();
             outputStream.write("Total output of Trucks: " + truckCount);
             outputStream.newLine();
             outputStream.write("Total vehicles outputted in Simulation: " + (carCount + truckCount));
             outputStream.newLine();
            outputStream.write("Simulation was run for: ");
         }catch(IOException ioex){
             ioex.printStackTrace();
         }
         
         reset(); //reset the counters once we output.
     }
 }
