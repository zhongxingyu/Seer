 /** 
 SFMTASimulation.java
 CIS 111B
 Group E: Teddy Deng, Katherine Soohoo,Rebecca A. Johnson
 We assume the csv data files are in the same directory as this file.
 */
 
 import java.io.*; // To use the PrintWriter class
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 
 
 /**
 SFMTASimulation class creates a program to simulate the operation
 of the San Francisco MUNI bus and light rail vehicle system
 */
 public class SFMTASimulation {
     
     /* 
     We create an ArrayList containing all of the stations, ordered by station ID. 
     */
     private ArrayList<Station> stations = new ArrayList<Station>();
     private ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
     private ArrayList<Person>  people   = new ArrayList<Person>(); // or two: drivers and passengers?
     
     // these 2 lines adopted from Vehicle.java main()
     private String[][] LTaravalRoute = createRouteArray("LTaraval.csv");
     private Bus test = new Bus(LTaravalRoute);
     
     
     /**
     main method instantiates an object of the class and runs the simulation.
     @param String[] args
     */
     public static void main(String[] args) {    
         /*
         Instantiate an SFMTASimulation object. This allows us to use
         the instance variables, available to all the class's methods.
         */
         SFMTASimulation ourMuni = new SFMTASimulation();
         
         // this method puts everything in motion
         ourMuni.runSimulation();
         
     }
     
     private void runSimulation() {
         
         initializeStations();
         initializeVehicles(); //or separate functions to initialize lrvs and buses
         initializePeople(); // or passengers and drivers separately
         
         //These two tasks together satisfy task 5
         printStationPeopleCount();
         printStationDriverCount();
         
         // Now kick off the movement and action
         
         
     }
     
     
     /**
     */
     private void initializeStations() {
         //method variable declarations
         String inputStr;
         String name;
         int stationID;
         StringTokenizer strToken;
         int arrayIndex;
         /* set the following 2 to null to avoid "variable *putFile might not have been initialized" compiler errors.
         */
         Scanner inputFile = null;
        PrintWriter outputFile = null;
         
         // Open the passengers file.
         try {
             File file = new File("passengers.csv");
             inputFile = new Scanner(file);
         }
         catch (FileNotFoundException e) {
             System.out.println("File not found.");
         }
         // Read until the end of the file.
         while (inputFile.hasNext())
         {
             inputStr = inputFile.nextLine();
             
             /* inputStr should look something like this:
             Isabella,17364,15204
             Passenger name, origin station ID, destination station ID
             We want to extract the name and origin to store in a Station object.
             */
             strToken = new StringTokenizer(inputStr, ",");
             
             name = strToken.nextToken();
             stationID = Integer.parseInt(strToken.nextToken());
             
             /* Now that we have the name of the passenger and place they're waiting, we'll create a new station object or add the passenger to the object containing this station ID, if it already exists.
             */
             arrayIndex = findInArray(stationID);
             if (arrayIndex >= 0) { //there is already an object for this station
                 // add this passenger to this station's queue
                 stations.get(arrayIndex).queuePassenger(name); 
             }
             else {
                 
             }
             
         }
         inputFile.close();// close the file when done.
     } // initializeStations method
     
     
     private void initializeVehicles() {}
     
     
     private void initializePeople() {}
     
      
     /**
     printStationPeopleCount method
     For each station print out on one line, separated by a comma "," the Stop
     ID, and the number of people waiting there. Save to StationPeopleCount.txt
     */
     private void printStationPeopleCount() {
         
         
         /*
         Note, this block probably needs to be moved to a different method, to be called after the drivers have also been accounted for. And probably needs to return s.getDriverCount() as well.
         */
         try {
             outputFile = new PrintWriter("StationPeopleCount.txt");
         }
         catch (Exception e) {
             System.out.println("Error");
         }
         for (Station s : stations)
             outputFile.println(s.getStationID() + "," + s.getPassengerCount());
             
         outputFile.close();
         
         
     } // printStationPeopleCount()
     
     
     /**
     printStationDriverCount method
     For each origin or terminal station, print out on one line separated by a
     comma "," the Stop ID and the number of drivers waiting there.
     Save to StationDriverCount.txt
     */
     private void printStationDriverCount() {
         
     }
     
     
     private int findInArray(int id) {
         
         
         return -1;
     }
     
     
     /** 
      * The createRouteArray method reads a route file and returns a two-dimensional
      * array with the first column holding the route name and the second column
      * holding the stop ID.
      * @param fileName The name of the file.
      * @return routeArray A two-dimensional array separating the route name
      *          and stop ID into two columns.
      */
     public static String[][] createRouteArray(String fileName) {
         // Calls the getNumOfStops method to determine the number of stops.
         int numOfStops = getNumOfStops(fileName); 
         Scanner inputFile = null;
         
         // Creates a two-dimensional array with a length equal to the number of stops.
         String[][] routeArray = new String[numOfStops][numOfStops];
         
         String line;     // Store the line being read
         String[] tokens; // Stores the tokenized string
         
         try {
             inputFile = new Scanner(new File(fileName));
         }
         catch (FileNotFoundException e) {
             System.out.println("File not found.");
         }
         
         inputFile.nextLine(); // Skips first line
         
         for (int i = 0; inputFile.hasNext(); i++) {
             line = inputFile.nextLine();
             tokens = line.split(",");
             
             routeArray[i][0] = tokens[0]; // Stores the route name
             routeArray[i][1] = tokens[1]; // Stores the stop ID
         }
         
         inputFile.close();
         
         return routeArray;
     }
     
     /**
      * The getNumOfStops reads a route file and counts the number of stops.
      * @param fileName The name of the file.
      * @return stopCount The number of stops in the route.
      */
     public static int getNumOfStops(String fileName) {
         int stopCount = 0;  // Counter for stops in a route
         Scanner inputFile = null;
         
         try {
             inputFile = new Scanner(new File(fileName));
         }
         catch (FileNotFoundException e) {
             System.out.println("File not found.");
         }
         
         inputFile.nextLine(); // Skips the first line
         
         while (inputFile.hasNextLine()) {
             inputFile.nextLine();   // Reads next line in the file
             stopCount++;            // Adds to the stops counter
         }
         
         inputFile.close();
         
         return stopCount;
     }
     
 } // SFMTASimulation
 
 
 class Station {
     
     private int stationID; // unique station identifier
     private ArrayList<String> passengers; // passengers waiting at station
     private ArrayList<String> drivers;    // drivers waiting at station
     private boolean isOriginOrTerminus;   
     
     /**
     constructor, no-argument
     This shouldn't be called, as we probably always want the station ID provided. 
     */
     public Station() {
         
         stationID  = 0;
         passengers = new ArrayList<String>();
         drivers    = new ArrayList<String>();
         isOriginOrTerminus = false;
     }
     
     /**
     constructor, one-argument
     @param id int station ID.
     */
     public Station(int id) {
         
         stationID  = id;
         passengers = new ArrayList<String>();
         drivers    = new ArrayList<String>();
         isOriginOrTerminus = false;
     }
     
     
     /**
     constructor, two-argument
     @param id int station ID.
     @param p String passenger name to add to passengers.
     */
     public Station(int id, String p) {
         
         stationID  = id;
         passengers = new ArrayList<String>();
         drivers    = new ArrayList<String>();
         isOriginOrTerminus = false;
         
         passengers.add( p);
     }
     
     
     /**
     constructor, three-argument
     @param id int station ID.
     @param d String driver name to add to drivers.
     @param driverFlag boolean, indicates this new station has a driver waiting
     */
     public Station(int id, String d, boolean driverFlag) {
         
         stationID  = id;
         passengers = new ArrayList<String>();
         drivers    = new ArrayList<String>();
         isOriginOrTerminus = true; // drivers wait at origin or terminus
         
         drivers.add( d);
     }
     
     
     /**
     getStationID method
     @return the int station ID of this station
     */
     public int getStationID() {
         
         return stationID;
     }
     
     
     /**
     getPassengerCount method
     @return the int count of passengers at this station
     */
     public int getPassengerCount() {
         
         return passengers.size();
     }
     
     
     /**
     getDriverCount method
     @return the int count of drivers at this station
     */
     public int getDriverCount() {
         
         return drivers.size();
     }
     
     
     /**
     getIsOriginOrTerminus method
     @return the boolean flag of whether station is origin or terminus
     */
     public boolean getIsOriginOrTerminus() {
         
         return isOriginOrTerminus;
     }
     
     
     /**
     queuePassenger method
     Called when we have a new passenger waiting at this station.
     @param p String passenger to add to passengers
     */
     public void queuePassenger(String p) {
         
         passengers.add(p);
     }
     
     
     /**
     queueDriver method
     Called when we have a new driver waiting at this station.
     @param d String driver to add to drivers
     */
     public void queueDriver(String d) {
         
         drivers.add(d);
     }
     
     
     /**
     popPassenger method
     Called when the first passenger in line has left this station.
     */
     public void popPassenger() {
         
         passengers.remove(0);
     }
     
     
     /**
     popDriver method
     Called when the first driver in line has left this station.
     */
     public void popDriver() {
         
         drivers.remove(0);
     }
     
     
     /**
     setIsOriginOrTerminus method
     @param is - the boolean flag of whether station is origin or terminus
     */
     public void setIsOriginOrTerminus(boolean is) {
         
         isOriginOrTerminus = is;
     }
     
         
 }
 
 
 class Vehicle {
 
     private enum Direction {INBOUND, OUTBOUND}
 
     private int idNumber;           // The identification number of the vehicle
     private int stopID;             // The stop ID of where the vehicle is currently located
     private int stopIndex;          // Counter to keep track of stops made
     private Direction vehicleDir;   // Direction of the vehicle
     
     private int numOfCoaches;   // Number of coaches for a vehicle
     private int maxCapacity;    // Maximum number of passengers a vehicle can hold
     
     private ArrayList<String> passengerList; // List of passenger aboard the vehicle
     private String[][] routeList;            // List of stations where the vehicle stops
     
     /**
      * No-Arg Constructor
      */
     public Vehicle() {
         idNumber = 0;
         stopID = 0;
         stopIndex = 0;
         vehicleDir = Direction.INBOUND;
         
         numOfCoaches = 0;
         maxCapacity = 0;
         
         passengerList = new ArrayList<String>(0);
         routeList = new String[0][0];
     }
     
     /**
      * One-Argument Constructor
      * @param route The route that the object will follow.
      */
     public Vehicle(String[][] route) {
         idNumber = generateRandIDNum();
         stopID = Integer.parseInt(route[0][1]);
         stopIndex = 0;
         vehicleDir = Direction.INBOUND;
         
         numOfCoaches = generateRandCoachNum();
         maxCapacity = numOfCoaches * 20;
         
         passengerList = new ArrayList<String>(maxCapacity);
         routeList = route;
     }
     
     /**
      * The generateRandIDNum generates and returns a random number ranging
      * from 1 to 999.
      */
     private int generateRandIDNum() {
         Random randGenerator = new Random();
         int randNum = randGenerator.nextInt(998) + 1;
         return randNum;
     }
     
     /**
      * The generateRandCoachNum generates a random number from 1 to 2.
      */
     private int generateRandCoachNum() {
         Random randGenerator = new Random();
         int randNum = randGenerator.nextInt(2) + 1;
         return randNum;
     }
     
     /**
      * The getIDNumber method returns the object's ID number.
      * @return An integer value of the object's ID number.
      */
     public int getIDNumber() {
         return idNumber;
     }
     
     /**
      * The getStopID method returns the object's the id of the current stop.
      * @return An integer value of the object's current stop id.
      */
     public int getStopID() {
         return stopID;
     }
     
     /**
      * The getDirection method retursn the object's current direction.
      * @return The Direction type of the object (INBOUND or OUTBOUND)
      */
     public Direction getDirection() {
         return vehicleDir;
     }
     
     /**
      * The getNumOfCoaches method returns the object's number of coaches.
      * @return An integer value of the number of coaches the object has.
      */
     public int getNumOfCoaches() {
         return numOfCoaches;
     }
     
     /**
      * The getMaxCapacity method returns the object's max capacity of passengers.
      * @return An integer value of the object's maximum capacity.
      */
     public int getMaxCapacity() {
         return maxCapacity;
     }
     
     /**
      * The getNumOfPassengers method returns the number of passengers in the vehicle.
      * @return An integer value of the current of number of passengers in the vehicle.
      */
     public int getNumOfPassengers() {
         return passengerList.size();
     }
     
     /**
      * The getPassengerList method returns an array of the passengers' name
      * in each element.
      * @return list An array of the passengers' name. 
      */
     public String[] getPassengerList() {
         String[] list = new String[passengerList.size()];
     
         for (int i = 0; i < list.length; i++)
             list[i] = passengerList.get(i);
             
         return list;
     }
     
     /**
      * The addPassenger methods adds a passenger to the vehicle.
      * @param name The name of the passenger.
      */
     public void addPassenger(String name) {
         if (getNumOfPassengers() == getMaxCapacity())
             System.out.println("The vehicle is full!");
         else
             passengerList.add(name);
     }
     
     /**
      * The removePassenger method removes a passenger from the vehicle.
      * @param name The name of the passenger.
      */
     public void removePassenger(String name) {
         for (int i = 0; i < passengerList.size(); i++) {
             if (name.equals(passengerList.get(i)))
                 passengerList.remove(i);
             else
                 System.out.println("Passenger does not exist!");
         }
     }
     
     /**
      * The goToNextStop method moves the vehicle to each stop listed in the route.
      * Once an origin or terminus is reached, the vehicle changes direction.
      */
     public void goToNextStop() {
         // If vehicle is INBOUND, move forward in the array.
         if (vehicleDir.equals(Direction.INBOUND)) {
             if (stopIndex < routeList.length - 1) {
                 stopIndex++;    
             }
             else {
                 switchDirection();
                 stopIndex--;
             }
         }
         
         // If vehicle is OUTBOUND, move backwards in the array.
         else if (vehicleDir.equals(Direction.OUTBOUND)) {
             if (stopIndex != 0) {
                 stopIndex--;
             }
             else {
                 switchDirection();
                 stopIndex++;
             }
         }
         
         // Store the current station's stop ID.
         stopID = Integer.parseInt(routeList[stopIndex][1]);    
     }
     
     /**
      * The switchDirection method changes the vehicle's direction.
      */
     public void switchDirection() {
         if (vehicleDir.equals(Direction.INBOUND))
             vehicleDir = Direction.OUTBOUND;
         else if (vehicleDir.equals(Direction.OUTBOUND))
             vehicleDir = Direction.INBOUND;
     }
 
 }
 
 class LRV extends Vehicle {
 
     private enum Type {LRV}
     Type vType;
 
     public LRV() {
         super();
         vType = Type.LRV;
     }
     
     public LRV(String[][] route) {
         super(route);
         vType = Type.LRV;
     }
 }
 
 class Person {}
