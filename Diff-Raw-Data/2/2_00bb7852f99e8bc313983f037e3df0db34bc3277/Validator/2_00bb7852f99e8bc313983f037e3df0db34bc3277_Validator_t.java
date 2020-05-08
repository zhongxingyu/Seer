 package edu.nyu;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.rmi.AlreadyBoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 final class Validator{
   private final int MAX_PASSENGER = 4;//of an amb 
   private List<Person> injured;
   private List<Ambulance> ambulances;
   private List<Location> hospitals;
   private int numHospitals = 0;
   private int totalRescued = 0;
   private Plotter plotter;
   
 
   public static void main(String[] args){
   	if(args.length==0)  		
   		throw new IllegalArgumentException("Please feed me data and result");
     Validator v = new Validator();
     Scanner input = null;
     try {
       input = new Scanner(new File(args[0]));
       v.buildInput(input);
       if(args.length == 1) input = new Scanner(System.in);
       else input = new Scanner(new File(args[1]));
     } catch (FileNotFoundException e) {
       System.err.println("Input file DNE");
       //e.printStackTrace();
       System.exit(1);
     }
     ///validate!
     try {
       v.validate(input);
       System.out.println("Valid! Total # of people rescued = " + v.totalRescued);
     } catch (Exception e) {
       System.err.println("Error: "+ e.getMessage());
       e.printStackTrace();
       System.out.println("Result invalid");
       
     }
     
   }
   
   private void draw(){
     plotter.draw();
   }
   /**
    * the core method. Reads result line by line and validate it.
    */
   private void validate(Scanner in) throws Exception{
     //String line = in.nextLine();
  //   in = in.skip("(,)");
     setUpHospitals(in.nextLine());
     while(in.hasNextLine()){
       String[] line = in.nextLine().split("\\s+");//line[0] = Ambulance
       //for(String s : line)System.out.println(s);
       int ambulanceID = Integer.parseInt(line[1]);
       Ambulance amb = ambulances.get(ambulanceID);
       
       if(line.length>3){ //pick up
         System.out.println("looking at Amb "+ ambulanceID+
             " now at "+amb.currLoc.toString()+" for pickup ");
         int i = 2;
         //for(String s: line) System.out.print(s+" ");
         if(line[2].length() >3)
           throw new IllegalArgumentException("For pick up the ambulance ID must " +
           		"be followed by rescuing personID");
         while (i < line.length-1){
           int pId = Integer.parseInt(line[i]);
           if(pId == 55) 
           		System.out.println("!!");
           Person rescuing = injured.get(pId);
           i++;
           String coordinates = line[i];
           checkCoordinateInput(coordinates); //line[2] = (x,y)
           Location ploc = getLocation(coordinates);
           int pt = getTime(coordinates);
           //System.out.println("pId: " + pId+" px: "+px+" py:" + py + " pt:" + pt);
           System.out.println("\t\tpick up person " + pId);
           checkStatusOfInjured(ploc,pt,rescuing);
           updateAmbulancePickup(rescuing, amb);
           i++;
         }
       }
       else{//drop off
         System.out.println("looking at Amb "+ ambulanceID+
             " now at "+amb.currLoc.toString()+"for dropoff ");
         if(line.length<3) 
           throw new IllegalArgumentException("specify the drop location");
         String coor = line[2];
         checkCoordinateInput(coor);
         Location loc = new Location(getX(coor), getYForTwo(coor));
         checkHospitalLocation(loc,amb);
         updateAmbulanceDropOff(loc, amb);
       }
     }
   }
   private Location getLocation(String coordinates) {
     return new Location(getX(coordinates), getY(coordinates));
   }
   private void setUpHospitals(String nextLine) {
     if (!nextLine.startsWith("Hospitals"))
       throw new IllegalArgumentException("The result must start with stating " +
       		"hospital locations!");
     String[] inputs = nextLine.substring(10).split("\\s+");
     for(int i=0,n=numHospitals*2; i<n; i++){
       int ind = Integer.parseInt(inputs[i]);
       String locs = inputs[++i];
       checkCoordinateInput(locs); //line[2] = (x,y)
       int x = getX(locs);
       int y = getYForTwo(locs);
       Location loc = new Location(x,y);
       hospitals.add(loc);
       for(Ambulance amb: ambulances){
         if(amb.getStartingHospitalId()==ind) amb.setStartingLocation(loc);
       }
      List<Location> ofInjured = new ArrayList<Location>(injured.size());
      for(Person p : injured){
        ofInjured.add(p.getLoc());
      }
      plotter.setUp(ofInjured, hospitals);
      draw();
     }
     rescuePeopleUnderHospitals();
     //System.out.println(hospitals);
   }
   private void rescuePeopleUnderHospitals() {
     for(Person p: injured){
       if(hospitals.contains(p.getLoc())){
         p.rescue();
         totalRescued++;
         System.out.println("Person#"+p.getId() + " was rescued, hospital was " +
         		"built right where he/she was at "+ p.getLoc());
       }
     }
     
   }
 
   /**
    * Drops off everyone in the ambulance to x, y hospital
    * @param x
    * @param y
    */
   private void updateAmbulanceDropOff(Location loc, Ambulance amb) {
     int timeSpent = 1;//to unload
     timeSpent += getManhattanDistance(amb.getCurrLocation(), loc); 
     amb.addTime(timeSpent);
     int rescued = amb.dropOff();
     totalRescued += rescued;
     System.out.println("Ambulance "+amb.getId()+" dropped off " + rescued+ 
         " alive people, \n\t total # of people rescued so far: " + totalRescued);
      amb.setNewLocation(loc);
     
   }
   /**
    * Updates the time remaining for people in the ambulance and 
    * the ambulance itself
    * This calculates the time required to get to the person and load him/her
    * @param dist
    * @param amb
    * @throws exception if the ambulance is full
    */
   private void updateAmbulancePickup(Person p, Ambulance amb) {
     if(amb.getNumOfPassengers()==MAX_PASSENGER)
       throw new IllegalStateException("This ambulance is full");
     amb.addRescued(p);
     int timeTaken = getManhattanDistance(amb.getCurrLocation(),p.getLoc());
     timeTaken++; //for loading p
     amb.addTime(timeTaken);
     amb.setNewLocation(p.getLoc());
   }
   /**
    * Checks if the input is valid/whether the person has been rescued or not
    * @param px
    * @param py
    * @param rescuing
    * @throws Exception
    */
   private void checkStatusOfInjured(Location ploc, int pt, Person rescuing) 
   throws Exception {
     if(!rescuing.getLoc().equals(ploc)|| rescuing.getTime()!=pt) {
       System.err.println("Rescuing is at: " + rescuing.getLoc() + " with time:"+
           rescuing.getTime() + " in data base person with same id is at"+ploc+
           " with time"  + pt);
       throw new IllegalArgumentException("The location of the person and/or time"+
                                               rescuing.getId()+" is not the same as the input");
     }
     if (rescuing.isRescued()) 
            throw new AlreadyBoundException(rescuing + " is already Rescued");
     
   }
   private void checkCoordinateInput(String locs) {
     if(!locs.matches("[(]{1}\\d{0,4}[,]{1}\\d{0,4}([,]{1}\\d{0,4})??[)]{1}")) 
         throw new IllegalArgumentException("Bad coordinate input:"+locs);
   }
   private void checkHospitalLocation(Location loc, Ambulance amb) {
     //check if hospital exists
     if(!hospitals.contains(loc))
       throw new IllegalArgumentException("Hospital does not exist at location "
           + loc);
     
     
   }
   //locs look like: (94,82,111)
   private int getX(String locs){
     return Integer.parseInt(locs.split("\\(")[1].trim().split(",")[0].trim());
   }
   //locs look like: (94, 82)
   private int getYForTwo(String locs){
     return Integer.parseInt(locs.split("\\(")[1].trim().split(",")[1].
         trim().split("\\)")[0]);
   }
   private int getY(String locs){
     //System.out.println("At getY:" + locs);
     return Integer.parseInt(locs.split("\\(")[1].trim().split(",")[1]);//.split("\\)")[0]);
   }
   private int getTime(String locs){
     return Integer.parseInt(locs.split("\\(")[1].split(",")[2].split("\\)")[0]);
   }
   private Validator(){
     injured = new ArrayList<Person>();
     ambulances = new ArrayList<Ambulance>();
     hospitals = new ArrayList<Location>();
     plotter = new Plotter();
   }
 
   private void buildInput(Scanner in){
     in.nextLine();//let the first line go
     int pId=0, ambId = 0;
     while(in.hasNextLine()){
       String[] ints = in.nextLine().split(",");
       if(ints.length==3){
         Person p = new Person(pId, Integer.parseInt(ints[0]), 
             Integer.parseInt(ints[1]), Integer.parseInt(ints[2]));
         //  System.out.println(p.toString());
         injured.add(p);  
         pId++;
       }
       //hospitals!!
       else if (ints[0].matches("\\d+")) {
         int numOfAmb = Integer.parseInt(ints[0]);
         for (int len =ambId+numOfAmb; ambId< len; ambId++) {
           ambulances.add(new Ambulance(ambId, numHospitals));
           
         }
         numHospitals++;
       }
     }
   }
 
   private class Person{
     final int id;
     final Location loc;
     final int time;
     boolean rescued = false;
     Person(int id, int x, int y, int time){
       this.id = id;
       this.loc = new Location(x,y);
       this.time = time;
     }
     boolean isAlive(int now){
       return time>=now; 
     }
     public String toString(){
       return "I am #" + id+" at ("+loc.x+","+loc.y+") with time:" + time;
     }
     int getId(){
       return id;
     }
     Location getLoc(){
       return loc;
     }
     int getTime() {
       return time;
     }
     void rescue(){
       rescued = true;
     }
     boolean isRescued(){
       return rescued;
     }
   }
   //sum of horizontal + vertical
   int getManhattanDistance(Location old, Location newL){
     return Math.abs(old.getX() -newL.getX()) + 
     Math.abs(old.getY() - newL.getY());
   }
 
   private class Ambulance{
     int id, time, hosID;
     Location currLoc = new Location(-1,-1);
     
     ArrayList<Person> carrying;
     Ambulance(int id,int hosID){
       this.id = id;
       carrying = new ArrayList<Person>();
       time = 0;
       this.hosID = hosID;
     }
     
     public void setNewLocation(Location loc) {
       currLoc = loc;
     }
 
     int getId(){
       return id;
     }
     int getStartingHospitalId(){
       return hosID;
     }
     void setStartingLocation(Location loc){
       currLoc = loc;
     }
     /**
      * Update time for this amb and for all the people in it
      * @param t
      */
     void addTime(int t){
       if(t>0){
         time += t;
         for(Person p: carrying){
           if(!p.isAlive(time)){
             System.out.println("Person:"+p.getId()+" has died in ambulance "
                 + id);
           }
         }
       }
     }
     /**
      * Clears the ambulance
      * @return the number of people alive when dropped off
      */
     public int dropOff() {
       int count = 0;
       for(Person p: carrying){
         if(p.isAlive(time)){
           count++;
           p.rescue();
         }
         
       }
       carrying.clear();
       return count;
     }
     void addRescued(Person p){
       carrying.add(p);
     }
     int getNumOfPassengers(){
       return carrying.size();
     }
     Location getCurrLocation(){
       return currLoc;
     }
     
   }
   static class Location{
     private final int x;
     private final int y;
     Location(int x, int y){
       this.x = x;
       this.y = y;
     }
     Location(Location loc) {
       this.x = loc.getX();
       this.y = loc.getY();
     }
     public int getX() {
       return x;
     }
     public int getY() {
       return y;
     }
     public String toString(){
       return "("+x+","+y+")";
     }
     @Override
     public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result + x;
       result = prime * result + y;
       return result;
     }
     @Override
     public boolean equals(Object obj) {
       if (this == obj)
         return true;
       if (obj == null || (getClass() != obj.getClass()))
         return false;
       Location other = (Location) obj;
       return x==other.x && y==other.y;
       
     }
 
     
     
   }
 
 
 }
