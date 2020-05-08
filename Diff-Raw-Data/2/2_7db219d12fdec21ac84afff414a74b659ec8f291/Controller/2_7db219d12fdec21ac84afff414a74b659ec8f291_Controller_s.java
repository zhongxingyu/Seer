 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Main;
 
 import Helpers.Id_Position;
 import Helpers.*;
 import Pathfinding.Node;
 import Vehicles.*;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import updateTimer.updateTimer;
 import Crane.*;
 import Network.StatsMessage;
 import Network.objPublisher;
 import Parkinglot.Parkinglot;
 import Pathfinding.Pathfinder;
 import java.io.File;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 
 /**
  *
  * @author Martin_Notebook
  */
 public class Controller {
     
     // <editor-fold defaultstate="collapsed" desc="Field">
     
     // The updateTimer class, Updates the update method
     updateTimer timer;
     float multiplier;
     
     // List with all the present transport vehicles
     List<TransportVehicle> presentVehicles;
     // List with all AGVs
     List<AGV> agvList;    
     
     // List with all the Vehicles that will arrive
     List<TransportVehicle> seaShipsToArrive;
     List<TransportVehicle> bargesToArrive;
     List<TransportVehicle> trainsToArrive;
     List<TransportVehicle> trucksToArrive;
     
     // Array with all the cranes
     Crane[] seaCranes;
     Crane[] bargeCranes;
     Crane[] truckCranes;
     Crane[] trainCranes;
    
     // List with all the storageCranes
     List<StorageCrane> storageCranes;
     
     // List with all the containers that will be send on the next deliveryTime;
     List<Id_Position> depatureContainers;
     // List with all the containers that are ready to be send away
     List<Id_Position> waitingContainers;
     
     // List with all the messages for the controller
     List<Message> messageQueue;
     
     // The simulation time of this application
     Date simulationTime;
     // The date when the next container needs to be send
     Date deliveryTime;
     // The date when the next shipment arrives
     Date shipmentTime;   
     
     // Networkhandler
     Network.objPublisher objpublisher;
     Network.StatsPublisher statsPublisher;
     private float statsPublisherMessageLimiter = 0.f;
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="Properties">
         
     /**
      * Sets the simulation seconds increment time
      * @param value Value from 0 till 100
      */
     public void SetMultiplier(float value){
         // When the value is below 0
         if(value < 0){
             multiplier = 0;
         }        
         // When the value is above 100
         if(value > 100 ){
             multiplier = 100;
         }
         else{
             multiplier = value;
         }
     }
     
     /**
      * Gets the simulation seconds increment time
      * @return seconds increment
      */
     public float GetMultiplier(){
         return multiplier;
     }
         
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="Controller">
     
     /**
      * Constructs a new controller
      * Then initializes the controller
      */
     public Controller() throws Exception
     {
         // Initializes the class variables
         Initialize();        
         // use Update-method for the timer
         timer = new updateTimer(this.getClass().getMethod("Update", new Class[] {float.class}), this);
 
         objpublisher = new objPublisher();
         statsPublisher = new Network.StatsPublisher();
         
         GenerateArrivalVehicles.objpublisher = objpublisher;
         GenerateDepartureVehicles.objpublisher = objpublisher;
         
         timer.start();
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="Initialize">
     
     /**
      * Initializes the class variables
      **/
     private void Initialize() throws Exception
     {   
         // Generates the node area
         Pathfinder.generateArea();
         // Default multiplier value
         multiplier = 100;
         
         // Initializes new ArrayLists
         messageQueue = new ArrayList();
         presentVehicles = new ArrayList();        
         agvList = new ArrayList();          
         storageCranes = new ArrayList();
         
         // Loads all the vehicles that come to the harbor
         seaShipsToArrive = MatchVehicles.GetSeaBoats();
         bargesToArrive = MatchVehicles.GetInlandBoats();
         trainsToArrive = MatchVehicles.GetTrains();
         trucksToArrive = MatchVehicles.GetTrucks();
         
         // Initializes space for the cranes        
         seaCranes = new Crane[10];
         bargeCranes = new Crane[8];
         truckCranes = new Crane[20];
         trainCranes = new Crane[4];
         
         //Er zijn in totaal  10 zeeschipkranen, 8 binnenvaartkranen, 4 treinkranen en 20 truckkranen 
         for(int i = 0; i < 10; i++){    
             // Initialize 10 seaShipCranes
             seaCranes[i] = new Crane(
                     1,
                     Crane.CraneType.seaship,
                     Pathfinder.parkinglots[i+1],
                     Pathfinder.parkinglots[46]);
         }
         for(int i = 0 ; i < 8; i++){     
             // Initialize 8 BargeCranes
             bargeCranes[i] = new Crane(
                     1,
                     Crane.CraneType.barge,
                     Pathfinder.parkinglots[i+ 12],
                     Pathfinder.parkinglots[47+ (i/4)]);
         }        
         for(int i  =0 ; i < 4; i++){         
             // Initialize 4 trainCranes
             trainCranes[i] = new Crane(
                     1,
                     Crane.CraneType.train,
                     Pathfinder.parkinglots[i+41],
                     Pathfinder.parkinglots[69 + (i/2)]);
         }        
         for (int i = 0; i < 20; i++){          
             // Initialize 20 truckCranes
             truckCranes[i] = new Crane(
                     1,
                     Crane.CraneType.truck,
                     Pathfinder.parkinglots[i+21],
                     Pathfinder.parkinglots[i+49]);
         }        
         // Initializes 100 storageAreas and there storage cranes
         for(int i = 0 ; i < 100; i++){
             storageCranes.add(new StorageCrane(
                 Pathfinder.parkinglots[71 +i],
                 Pathfinder.parkinglots[171 + i]));
         }
         
         // Adds 100 AGVs
         for(int i = 0; i < 100; i++){
             // Set there positions on the parking nodes of each storage crane
             agvList.add(new AGV(Pathfinder.parkinglots[71 +i]));
         }       
         
         // Initializes the dates
         deliveryTime = new Date(); 
         shipmentTime = new Date();
         simulationTime = new Date();
         
         // Gets the first shipment
         GetNextArrivalDate();   
         
         // Sets the simulationTime equal to the first shipment
         simulationTime.setTime(shipmentTime.getTime());
         // Sets the simulationTime 1 hour before the first shipment  
        // simulationTime.setHours(simulationTime.getHours() -1);
         
         // Gets the first delivery of containers
         // Also sets the deliveryTime
         depatureContainers = GetDepartureContainers(simulationTime);
         waitingContainers = new ArrayList();
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="Update">
     
     /**
      * Runs the timer for updating the application
      */
     public void Run() {
         timer.run();
     }
     
     /**
      * Properly teardown controller
      */
     public void Teardown() {
         statsPublisher.Close();
         objpublisher.close();
     }
     
     /**
      * Updates simulation logic
      * @param gameTime 
      */
     public void Update(float gameTime ) throws Exception{        
         // Time that passed by this update
         float elapsedTime = multiplier * gameTime;
         simulationTime.setTime(simulationTime.getTime()+ (long)(elapsedTime * 1000));
         
         System.out.println(simulationTime);
         System.out.println(gameTime);
         System.out.println(elapsedTime);
         
         // Updates the logic of each AGV
         for(AGV agv : agvList){
             agv.update(elapsedTime);
             // When an agv has a container but no assignments
             if(agv.NeedDeliverAssignment()){
                 Container con = agv.storage.peekContainer(0,0);
                 // When the container needs to be transported
                 if(simulationTime.getTime() >= con.getDepartureDateStart().getTime()){
                     switch(con.getDepartureTransportType())
                     {
                         case vrachtauto:
                             truckCranes = CranesToCheck(truckCranes,agv,con);
                             break;
                         case zeeschip:
                             seaCranes = CranesToCheck(seaCranes,agv,con);
                             break;
                         case binnenschip:
                             bargeCranes = CranesToCheck(bargeCranes,agv,con);
                             break;
                         case trein:
                             trainCranes = CranesToCheck(trainCranes,(AGV)agv,con);
                             break;             
                     }                    
                 }
                 // When it needs to be stored on the storage
                 else{
                     StorageCranesToCheck(storageCranes,agv, agv.storage.peekContainer(0, 0));
                 }
                 messageQueue.add(new Message(
                     agv,
                     Crane.class,
                     Message.ACTION.Unload,
                     agv.storage.peekContainer(0,0)));
             }
         }
         // Updates the logic of each crane
         for(Crane crane : seaCranes){
             crane.update(elapsedTime);
         }
         for(Crane crane : bargeCranes){
             crane.update(elapsedTime);
         }
         for(Crane crane : truckCranes){
             crane.update(elapsedTime);
         }
         for(Crane crane : trainCranes){
             crane.update(elapsedTime);
         }
         // Updates the logic of each storagecrane
         for(StorageCrane crane : storageCranes){
             crane.update(elapsedTime);
         }        
         // Updates the logic of each docked vehicle
         for(Vehicle vehicle : presentVehicles){
             vehicle.update(elapsedTime);
         }
         
         // Updates the vehicles that are present in the harbor
         int counter = presentVehicles.size();
         for (int i = 0; i < counter; i++) {
             TransportVehicle currentTransportVehicle = presentVehicles.get(i);
             
             if(currentTransportVehicle.Destroy()){
                 objpublisher.destroyVehicle(currentTransportVehicle);
                 presentVehicles.remove(currentTransportVehicle);
                 i--;
                 counter--;
             }            
             if(simulationTime.getTime() >= currentTransportVehicle.GetDepartureDate().getTime()){
                 switch(currentTransportVehicle.GetVehicleType())
                 {
                     case inlandBoat:
                         if(currentTransportVehicle.getDestination() == Pathfinder.parkinglots[130].node){
                             currentTransportVehicle.Departure(Pathfinder.Nodes[142]);
                         }
                         else{
                             currentTransportVehicle.Departure(Pathfinder.Nodes[152]);
                         } 
                         break;
                     case truck:
                         currentTransportVehicle.Departure(Pathfinder.Nodes[198]);
                         break;
                     case seaBoat :
                         currentTransportVehicle.Departure(Pathfinder.Nodes[122]);
                         break;
                     case train:
                         if(currentTransportVehicle.getDestination() == Pathfinder.parkinglots[206].node){
                             currentTransportVehicle.Departure(Pathfinder.Nodes[230]);
                         }
                         else{
                             currentTransportVehicle.Departure(Pathfinder.Nodes[231]);
                         }
                         break;
                 }
             }
         }     
         
         // When the next shipment arrives
         if(simulationTime.getTime() >= shipmentTime.getTime()){
             // The spawn positions for the vehicle and there parkinglots
             List<Node> startNodes = new ArrayList<Node>();
             List<Parkinglot> destParkinglots = new ArrayList<Parkinglot>();
             
             startNodes.add(Pathfinder.Nodes[111]);
             destParkinglots.add(Pathfinder.parkinglots[46]);
             // When a ship arrives send 10 cranes
             seaShipsToArrive = CheckArrival(seaShipsToArrive, 10,startNodes,destParkinglots);
             
             startNodes.clear();
             destParkinglots.clear();
             for(int i = 0; i <2; i++){
                 startNodes.add(Pathfinder.Nodes[162 + (i *10)]);
                 destParkinglots.add(Pathfinder.parkinglots[47 + i]);
             }            
             // When a barges Arrives send 4 cranes
             bargesToArrive = CheckArrival(bargesToArrive, 4, startNodes, destParkinglots);
             
             startNodes.clear();
             destParkinglots.clear();
             for(int i = 0; i <2; i++){
                startNodes.add(Pathfinder.Nodes[207 + (i *10)]);
                 destParkinglots.add(Pathfinder.parkinglots[69 + i]);
             } 
             // When a train Arrives send 2 cranes
             trainsToArrive = CheckArrival(trainsToArrive, 2, startNodes, destParkinglots);
             
             startNodes.clear();  
             destParkinglots.clear();
             for(int i = 0; i <20; i++){
                 startNodes.add(Pathfinder.Nodes[1050 + i]);
                 destParkinglots.add(Pathfinder.parkinglots[49 + i]);
             } 
             // When a truck Arrives send a crane
             trucksToArrive = CheckArrival(trucksToArrive, 1,startNodes,destParkinglots);
             
             // Gets the next shipment time
             GetNextArrivalDate();
         }        
         // When the next delivery needs to be deliverd
         if(simulationTime.getTime() >= deliveryTime.getTime()){
             // Adds all the containers that need to be deliverd
             for(Id_Position idPos : depatureContainers){
                 waitingContainers.add(idPos);
             }
             // Gets the next date when the next shipment needs to be transported
             // Gets the next shipment that needs to be deliverd
             depatureContainers = GetDepartureContainers(simulationTime);
         }
         // Updates all the messageQueue
         UpdateMessages();
         
         // Send StatsMessage every 1000ms
         statsPublisherMessageLimiter += gameTime;
         if(statsPublisherMessageLimiter >= 1.f) {
             SendStatsMessage();
             statsPublisherMessageLimiter = 0.f;
         }
     }
     
     /**
      * Updates all the messageQueue 
      * @throws Exception 
      */
     private void UpdateMessages() throws Exception{
         // Index that holds the current agv that's selected
         int indexAGV = 0;
         // When there are no agvs available
         boolean nonAble = false;
         
         // Checks every message
         for(Message message : messageQueue){
             
             // <editor-fold defaultstate="collapsed" desc="AGV">
             // When the message requests an AGV 
             if(message.RequestedObject().equals(AGV.class)){
                 if(!nonAble){
                     float distance = Float.MAX_VALUE;
                     // Skip every agv that's not available
                     for(int i = 0; i < agvList.size(); i++){
                         if(((AGV)agvList.get(i)).Available()){
                             float tempDist = Vector3f.distance(message.DestinationNode().getPosition(), agvList.get(i).getPosition());             
                             if(tempDist < distance){
                                 indexAGV = i;
                                 distance = tempDist;
                             }
                         }
                     }
                     if(distance == Float.MAX_VALUE){
                         nonAble = true;
                         break;
                     }
                     
                     // The agv that's available
                     Vehicle agv = agvList.get(indexAGV);
                     //Sets the destination of the AGV
                     //agv.setDestination(message.DestinationNode());
                     //Copies the message to the message queue
                     ((AGV)agv).SendMessage(message);
                     // When it's a fetch message send a delivery message
                     if(message.Fetch() && message.GetContainer() != null){
                         // When the destination object is a crane
                         if(message.DestinationObject().equals(Crane.class)){
                             switch(message.GetContainer().getDepartureTransportType())
                             {
                                 case vrachtauto:
                                     truckCranes = CranesToCheck(truckCranes,(AGV)agv,message);
                                     break;
                                 case zeeschip:
                                     seaCranes = CranesToCheck(seaCranes,(AGV)agv,message);
                                     break;
                                 case binnenschip:
                                     bargeCranes = CranesToCheck(bargeCranes,(AGV)agv,message);
                                     break;
                                 case trein:
                                     trainCranes = CranesToCheck(trainCranes,(AGV)agv,message);
                                     break;
                             }
                         }
                         // When the destination object is a storageCrane
                         else if (message.DestinationObject().equals(StorageCrane.class)){
                         // Check the Storage cranes
                         storageCranes = StorageCranesToCheck(storageCranes,(AGV)message.DestinationObject(), message);
                         }
                     }                
                 }
             }
             // </editor-fold>
             
             // When the message requests a crane
             else if(message.RequestedObject().equals(Crane.class)){
                 // When a vehicle requested the crane
                 if(message.DestinationObject() instanceof Vehicle){
                     // Switch between the vechile types
                     switch(((Vehicle)message.DestinationObject()).GetVehicleType()){
                         case truck:
                             truckCranes = TransportRequestsCrane(truckCranes, (Message)message);
                             break;
                         case train:
                             trainCranes = TransportRequestsCrane(trainCranes,(Message)message);
                             break;
                         case seaBoat:
                             seaCranes = TransportRequestsCrane(seaCranes,(Message)message);
                             break;
                         case inlandBoat:
                             bargeCranes = TransportRequestsCrane(bargeCranes, (Message)message);
                             break;
                         case AGV:
                             switch(message.GetContainer().getDepartureTransportType())
                             {
                                 case vrachtauto:
                                     truckCranes = CranesToCheck(truckCranes,(AGV)message.DestinationObject(),message);
                                     break;
                                 case zeeschip:
                                     seaCranes = CranesToCheck(seaCranes,(AGV)message.DestinationObject(),message);
                                     break;
                                 case binnenschip:
                                     bargeCranes = CranesToCheck(bargeCranes,(AGV)message.DestinationObject(),message);
                                     break;
                                 case trein:
                                     trainCranes = CranesToCheck(trainCranes,(AGV)message.DestinationObject(),message);
                                     break;
                             }
                             break;
                     }
                 }
                 // When an AGV wants to store it's container
                 else if(message.RequestedObject().equals(StorageCrane.class)){
                     // Check the Storage cranes
                     storageCranes = StorageCranesToCheck(storageCranes,(AGV)message.DestinationObject(), message);  
                 }
             }
         }
         // Check if there are any agvs left that have nothing todo 
         // They may deliver the containers that are ready for departure
         for(Vehicle agv : agvList){
             // When the agv is available
             if(((AGV)agv).Available()){
                 // When there are no containers waiting
                 if(waitingContainers.isEmpty()){
                     break;
                 }
                 // Checks every container in that's waiting
                 for(Id_Position idPos : waitingContainers){
                     // Storage index where the container is stored
                     int index = Integer.parseInt(idPos.ID);
                     // When the storageCrane has nothing todo
                     if(storageCranes.get(index).Available()){    
                         // The load action for the storageCrane
                         Message message = new Message(
                                 agv,
                                 storageCranes.get(index),
                                 Message.ACTION.Load,
                                 (Container)null);                        
                         storageCranes.get(index).SendMessage(message);
                         // The fetch action for the agv 
                         message = new Message(
                                 storageCranes.get(index),
                                 agv,
                                 Message.ACTION.Fetch,
                                 idPos.position);                        
                         ((AGV)agv).SendMessage(message);
                     }
                 }
             }
         }
     }
     
     // </editor-fold>
     
     /**
      * Sends a StatsMessage to all listening subscribers
      * @throws Exception 
      */
     protected void SendStatsMessage() throws Exception {
         StatsMessage msg = new StatsMessage();
         msg.date = (Date)simulationTime.clone();
         // Containers
         msg.containers_outgoing = depatureContainers.size();
         
         DateFormat df = new SimpleDateFormat("yy-MM-dd HH:mm");
         String query = "SELECT COUNT(id) " +
                        "FROM container " +
                        "WHERE arrivalDateStart <= ? " +
                        "AND arrivalDateEnd >= ? ";
         PreparedStatement stm = Database.createPreparedStatement(query);
         String date = df.format(simulationTime);
         stm.setString(1, date);
         stm.setString(2, date);
         ResultSet getCount = Database.executeQuery(stm);
         msg.containers_incoming = getCount.getLong(1);
         
         // Storage areas
         int i = 0;
         for(StorageCrane storage_crane : storageCranes) {
             msg.areas.put("Area " + ++i, storage_crane.GetStorageArea().Count());
         }
 
         // Available Vehicles
         int availableAgvs = 0,
             availableTrucks = 0,
             availableTrains = 0;
 
         for(Vehicle vehicle : presentVehicles) {
             boolean isAvailable = !vehicle.storage.isFilled();
             switch(vehicle.GetVehicleType()) {
                 case truck:
                     if(isAvailable)
                         availableTrucks++;
                 break;
                 case train:
                     if(isAvailable)
                         availableTrains++;
                 break;
                 case AGV:
                     if(vehicle instanceof AGV && ((AGV)vehicle).Available())
                        availableAgvs++; 
                 break;
             }
         }
         
         msg.vehicles.put("AGV", availableAgvs);
         msg.vehicles.put("TRUCK", availableTrucks);
         msg.vehicles.put("TRAIN", availableTrains);
         
         statsPublisher.SendStatsMessage(msg);
     }
     
     // <editor-fold defaultstate="collapsed" desc="Container Methods">
     
     /**
      * Gets all the containers that will be delivered the next time
      * @param now The simulation time
      * @return List with all the containers that need to be send
      * @throws Exception 
      */
     private List<Id_Position> GetDepartureContainers( Date now) throws Exception{
         List<Id_Position> id_positionList = new ArrayList<>();
         
         String query = "SELECT MIN(departureDateStart) AS max " +
                         "FROM container " +
                         "WHERE departureDateStart > ?";
         PreparedStatement stm = Database.createPreparedStatement(query);
         stm.setString(1, Container.df.format(now));
         ResultSet getNextDate = Database.executeQuery(stm);
         deliveryTime = Container.df.parse(getNextDate.getString("max"));
         
         System.out.println("next delivery time : "+deliveryTime);
         
         query = "SELECT locationId, storageLocation " +
                 "FROM container " +
                 "WHERE departureDateStart = ? " +
                 "ORDER BY departureDateEnd";
         stm = Database.createPreparedStatement(query);
         stm.setString(1, Container.df.format(deliveryTime));
         ResultSet getLocationId_storageLocation = Database.executeQuery(stm);
         while(getLocationId_storageLocation.next()){
             String Id = getLocationId_storageLocation.getString("locationId");
             String position = getLocationId_storageLocation.getString("storageLocation");
             id_positionList.add(new Id_Position(Id, StorageLocationToVector3f(position)));
         }
         return id_positionList;
     }
     
     /**
      * Converts a string to a Vector3f 
      * @param input
      * @return 
      */
     private Vector3f StorageLocationToVector3f(String input){
         if(input == null){
             return new Vector3f(0,0,0);
         }
         if(input.length() != 6){
             return new Vector3f(0,0,0);
         }
         
         char[] inputC = input.toCharArray();
         String a = Character.toString(inputC[0]);
         int x = Integer.parseInt(Character.toString(inputC[2]) + Character.toString(inputC[3]));
         int y = Integer.parseInt(Character.toString(inputC[0]) + Character.toString(inputC[1]));
         int z = Integer.parseInt(Character.toString(inputC[4]) + Character.toString(inputC[5]));
         return new Vector3f(x, y, z);       
     }
     
     // </editor-fold>
      
     // <editor-fold defaultstate="collapsed" desc="Arrival Methods">
     
     /**
      * Checks if from the given list vehicles are arriving
      * @param toCheck The list to check
      * @param requests The amount of cranes requested for this vehicle
      * @param startNodes The nodes the vehicles spawn
      * @param destParkinglots The destination node where the vehcile has to go
      * @return The list without the arrived vehicles
      * @throws Exception 
      */
     private List<TransportVehicle> CheckArrival(List<TransportVehicle> toCheck, int requests ,
             List<Node> startNodes, List<Parkinglot> destParkinglots) throws Exception{
         if (toCheck == null){
             throw new Exception("toCheck isn't initialized");
         }
         int counter =0 ;
         // Checks if seaShips arrive
         if(toCheck.size() > 0){
             // While there are transport vehicles arriving
             while(simulationTime.getTime() >= toCheck.get(0).GetArrivalDate().getTime()){
                 // The vehicle that arrived
                 TransportVehicle vehicle = toCheck.get(0);
                 vehicle.setPostion((Node)startNodes.get(counter));
                 vehicle.setDestination(destParkinglots.get(counter++));         
                 if(counter >= startNodes.size() || 
                    counter >= destParkinglots.size()){
                     counter = 0;
                 }
                 
                 System.out.println(vehicle.getPosition() + "   " + vehicle.getDestination().getPosition());                
                 System.out.println(vehicle.GetVehicleType().toString() + " arrived" + vehicle.GetArrivalDate() );
                 
                 // Add the vehicle that arrived
                 presentVehicles.add(vehicle);
                 objpublisher.createVehicle(vehicle);
                 
                 // Request cranes for the vehicle
                 for(int i = 0 ; i < requests; i++){
                     messageQueue.add(new Message(
                         vehicle,
                         Crane.class,
                         Message.ACTION.Unload,
                         (Container)null));
                 }
                 // When there are no vehicles left
                 if(toCheck.isEmpty()){
                     break;
                 }
                 // Removes the vehicle that arrived
                 toCheck.remove(0);
             }
         }
         return toCheck;
     }
     
     /**
      * Gets the date when the next shipment arrives
      */
     private void GetNextArrivalDate(){
         // When there are still seaShips that need to arrive
         if(!seaShipsToArrive.isEmpty()){
             shipmentTime = seaShipsToArrive.get(0).GetArrivalDate();
         }
         // When there are still barges that need to arrive
         if(!bargesToArrive.isEmpty()){
             // When the first barge arrives earlier than the other shipment
             if(shipmentTime.getTime() > bargesToArrive.get(0).GetArrivalDate().getTime()){
                 shipmentTime = bargesToArrive.get(0).GetArrivalDate();
             }
         }
         // When there are still trains that need to arrive
         if(!trainsToArrive.isEmpty()){
             // When the first train arrives earlier than the other shipment
             if(shipmentTime.getTime() > trainsToArrive.get(0).GetArrivalDate().getTime()){
                 shipmentTime = trainsToArrive.get(0).GetArrivalDate();
             }
         }
         // When there are still trucks that need to arrive
         if(!trucksToArrive.isEmpty()){
             // When the first truck arrives earlier than the other shipment
             if(shipmentTime.getTime() > trucksToArrive.get(0).GetArrivalDate().getTime()){
                 shipmentTime = trucksToArrive.get(0).GetArrivalDate();
             }
         }
         
         System.out.println("next arrivalDate : " + shipmentTime);
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="Message Methods">
         
     /**
      * Checks if a crane's available for an agv to unload it's container
      * @param toCheck The crane array to check
      * @param agv The agv that requests a crane
      * @param message The fetch message of the agv
      * @return The crane array that's checked
      * @throws Exception 
      */
     private Crane[] CranesToCheck(Crane[] toCheck,AGV agv, Message message) throws Exception{
         // Boolean if there's a crane that can handel the message
         boolean found = false;
         // Check every crane
         for(int i = 0; i< toCheck.length; i++){
             // When the agv can deliver the container to this crane
             if(toCheck[i].Available() && toCheck[i].parkinglotTransport.isFull()){
                 // Send a message to unload the agv to the crane
                 toCheck[i].SendMessage(new Message(
                     agv,
                     toCheck[i],
                     Message.ACTION.Unload,
                     message.GetContainer()));
                 // When the agv his assignments need to be destroyed
                 boolean destroy = false;
                 // When it needs to destroy it's messages
                 if(!agv.Available()){
                     if(agv.GetMessage().Deliver()){
                         destroy = true;                       
                     }
                 }
                 // Send a delivery message to the agv
                 agv.SendMessage(new Message(
                     toCheck[i],
                     agv,
                     Message.ACTION.Deliver,
                     message.GetContainer()),
                     destroy);
                 // There was a crane found to deliver the container
                 found  = true;
                 break;                
             }
         }
         // When there's no crane available to deliver the container to
         if(!found){
             messageQueue.add(new Message(
                 agv,
                 toCheck[0],
                 Message.ACTION.Unload,
                 message.GetContainer()));
         }
         // Message was handeld so remove it        
         messageQueue.remove(message);        
         return toCheck;
     }    
     
     /**
      * Checks if a crane's available for an agv to unload it's container
      * @param toCheck The crane array to check
      * @param agv The agv that requests a crane
      * @param message The fetch message of the agv
      * @return The crane array that's checked
      * @throws Exception 
      */
     private Crane[] CranesToCheck(Crane[] toCheck,AGV agv, Container con) throws Exception{
         // Boolean if there's a crane that can handel the message
         boolean found = false;
         // Check every crane
         for(int i = 0; i< toCheck.length; i++){
             // When the agv can deliver the container to this crane
             if(toCheck[i].Available() && toCheck[i].parkinglotTransport.isFull()){
                 // Send a message to unload the agv to the crane
                 toCheck[i].SendMessage(new Message(
                     agv,
                     toCheck[i],
                     Message.ACTION.Unload,
                     con));
                 // When the agv his assignments need to be destroyed
                 boolean destroy = false;
                 // When it needs to destroy it's messages
                 if(!agv.Available()){
                     if(agv.GetMessage().Deliver()){
                         destroy = true;                       
                     }
                 }
                 // Send a delivery message to the agv
                 agv.SendMessage(new Message(
                     toCheck[i],
                     agv,
                     Message.ACTION.Deliver,
                     con),
                     destroy);
                 // There was a crane found to deliver the container
                 found  = true;
                 break;                
             }
         }
         // When there's no crane available to deliver the container to
         if(!found){
             messageQueue.add(new Message(
                 agv,
                 toCheck[0],
                 Message.ACTION.Unload,
                 con));
         }      
         return toCheck;
     }       
     
     /**
      * Checks every crane if the AGV stands on the destination node
      * @param toCheck The cranes to check
      * @param message The message to check
      * @return The checked crane array
      * @throws Exception 
      */
     private Crane[] TransportRequestsCrane(Crane[] toCheck, Message message) throws Exception{
         for(int i = 0 ; i < toCheck.length; i++){
             // When the vehcile is on the position of the crane for un/loading
             if(toCheck[i].parkinglotTransport.node.equals(message.DestinationNode())){
                 // Sends the message copy to the crane
                 toCheck[i].SendMessage(message);
                 // Request an AGV to fetch the first container for the crane
                 if(message.UnLoad()){
                     messageQueue.add(new Message(
                         toCheck[i],
                         AGV.class,
                         Message.ACTION.Fetch,
                         (Container)null));
                 }  
                 //Message was handeld so remove it
                 messageQueue.remove(message);
                 break;
             }
         }
         return toCheck;
     }
     
     /**
      * Checks if a storagecrane's available for an agv to unload it's container
      * @param toCheck The crane list to check
      * @param agv The agv that requests a storagecrane
      * @param message The fetch message of the agv
      * @return The crane list that's checked
      * @throws Exception 
      */
     private List<StorageCrane> StorageCranesToCheck(List<StorageCrane> toCheck,AGV agv, Message message) throws Exception{
         // When a crane was found
         boolean found = false;
         // Check every storage Crane if the can unload an agv 
         for(StorageCrane crane : toCheck){
             if(crane.Available()){
                 // Send a message to the storagcrane to unload the agv
                 crane.SendMessage(new Message(
                         crane,
                         agv,
                         Message.ACTION.Unload,
                         message.GetContainer()));
                 // When the assignments need to be destroyed
                 boolean destroy = false;
                 // When the agv needs to destroy it's assignments
                 if(!agv.Available()){
                     if(agv.GetMessage().Deliver()){
                         destroy = true;
                     }
                 }
                 // Send a deliver assignement to the agv
                 agv.SendMessage(new Message(
                     crane,
                     agv,
                     Message.ACTION.Deliver,
                     message.GetContainer()),
                     destroy);
                 found = true;
                 break;
             }
         }
         // When there was no storage crane available
         if(!found){
             messageQueue.add(new Message(
                 agv,
                 toCheck.get(0),
                 Message.ACTION.Unload,
                 message.GetContainer()));
         }
         // Message was handeld so remove it        
         messageQueue.remove(message);        
         return toCheck;
     }
     
     
     /**
      * Checks if a storagecrane's available for an agv to unload it's container
      * @param toCheck The crane list to check
      * @param agv The agv that requests a storagecrane
      * @param message The fetch message of the agv
      * @return The crane list that's checked
      * @throws Exception 
      */
     private List<StorageCrane> StorageCranesToCheck(List<StorageCrane> toCheck,AGV agv, Container con) throws Exception{
         // When a crane was found
         boolean found = false;
         // Check every storage Crane if the can unload an agv 
         for(StorageCrane crane : toCheck){
             if(crane.Available()){
                 // Send a message to the storagcrane to unload the agv
                 crane.SendMessage(new Message(
                         crane,
                         agv,
                         Message.ACTION.Unload,
                         con));
                 // When the assignments need to be destroyed
                 boolean destroy = false;
                 // When the agv needs to destroy it's assignments
                 if(!agv.Available()){
                     if(agv.GetMessage().Deliver()){
                         destroy = true;
                     }
                 }
                 // Send a deliver assignement to the agv
                 agv.SendMessage(new Message(
                     crane,
                     agv,
                     Message.ACTION.Deliver,
                     con),
                     destroy);
                 found = true;
                 break;
             }
         }
         // When there was no storage crane available
         if(!found){
             messageQueue.add(new Message(
                 agv,
                 toCheck.get(0),
                 Message.ACTION.Unload,
                 con));
         }   
         return toCheck;
     }
     
     // </editor-fold>
 }
