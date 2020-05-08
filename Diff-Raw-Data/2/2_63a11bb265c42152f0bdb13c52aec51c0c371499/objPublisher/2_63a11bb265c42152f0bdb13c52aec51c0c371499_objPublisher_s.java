 package Network;
 
 import Helpers.Vector3f;
 import Main.Container;
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectOutputStream;
 import java.util.List;
 import java.util.Random;
 import org.zeromq.*;
 
 /**
  *
  * @author EightOneGulf
  */
 public class objPublisher {
     private ZMQ.Socket publisher;
     private ZMQ.Context context;
     
     /**
      * Constructor
      */
     public objPublisher() {
         //  Prepare our context and socket
         context = ZMQ.context(1);
         publisher = context.socket(ZMQ.PUB);
         publisher.bind("tcp://*:6001");
         System.out.println("Net: Listening on 6001");
     }
     
     /**
      * Close socket
      */
     public void close() {
         publisher.close();
         context.term();
     }
     
     public void createVehicle(Vehicles.TransportVehicle vehicle){
         //42Byte total
         byte[] b = new byte[42];
         int vehicleID = vehicle.Id;
         byte vehicleType = 0;
         
         if(vehicle.GetVehicleType() == Vehicles.TransportVehicle.VehicleType.AGV){
             vehicleType = 0;
         }else if(vehicle.GetVehicleType() == Vehicles.TransportVehicle.VehicleType.inlandBoat){
             vehicleType = 1;
         }else if(vehicle.GetVehicleType() == Vehicles.TransportVehicle.VehicleType.seaBoat){
             vehicleType = 2;
         }else if(vehicle.GetVehicleType() == Vehicles.TransportVehicle.VehicleType.train){
             vehicleType = 3;
         }else if(vehicle.GetVehicleType() == Vehicles.TransportVehicle.VehicleType.truck){
             vehicleType = 4;
         }
         
 
         
         
         Helpers.Vector3f pos = vehicle.getPosition();
         Helpers.Vector3f des = vehicle.getDestination().getPosition();
         
         b[0] = 0;       //Operation ID
         
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(vehicleID), b, 1);
 
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(pos.x), b, 5);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(pos.y), b, 9);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(pos.z), b, 13);
 
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(des.x), b, 17);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(des.y), b, 21);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(des.z), b, 25);
 
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(vehicle.storage.getWidth()), b, 29);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(vehicle.storage.getLength()), b, 33);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(vehicle.storage.getHeight()), b, 37);
         
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(vehicleType), b, 41);
         
         publisher.send(b, 0);
         System.out.println("Net: Creating " + vehicleID);
     }
 
     public void syncVehicle(Vehicles.Vehicle vehicle){        
         //29Byte total
         byte[] b = new byte[29];
 
         Helpers.Vector3f pos = vehicle.getPosition();
         Helpers.Vector3f des = vehicle.getDestination().getPosition();
         int vehicleID = vehicle.Id;
 
         b[0] = 1;       //Operation ID
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(vehicleID), b, 1);
 
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(pos.x), b, 5);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(pos.y), b, 9);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(pos.z), b, 13);
 
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(des.x), b, 17);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(des.y), b, 21);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(des.z), b, 25);
 
         publisher.send(b, 0);
         System.out.println("Net: Syncing " + vehicleID);
     }
     
     public void destroyVehicle(Vehicles.Vehicle vehicle){
         //5Byte total
         byte[] b = new byte[29];
         int vehicleID = vehicle.Id;
 
         b[0] = 2;       //Operation ID
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(vehicleID), b, 1);
         
         publisher.send(b, 0);
         System.out.println("Net: Destroying " + vehicleID);
     }
     
     public void createStorage(int vehicleId, List<Container> containers){
         byte[] b = new byte[9+(containers.size()*16)];
 
         b[0] = 3;       //Operation ID
         
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(vehicleId), b, 1);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(containers.size()), b, 5);
         for (int i = 0; i < containers.size(); i++) {
             Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(containers.get(i).getId()), b, 9+16*i); // containerId
             Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(containers.get(i).getArrivalPosition().x), b, 13+16*i); // posX
             Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(containers.get(i).getArrivalPosition().y), b, 17+16*i); // posY
             Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(containers.get(i).getArrivalPosition().z), b, 21+16*i); // posZ
         }
         publisher.send(b, 0);
         System.out.println("Net: CreateStorage for " + vehicleId);
     }
     public void addContainerOnStorage(Vector3f position, int containerId, int VehicleId){
         byte[] b = new byte[21]; 
         b[0] = 5; 
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(VehicleId), b, 1);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(containerId), b, 5);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(position.x), b, 9);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(position.y), b, 13);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(position.z), b, 17);
         
         publisher.send(b, 0);
         System.out.println("Net: Add container " + containerId);
     }
     public void removeContainerFromStorage(int VehicleId, int ContainerId){
         byte[] b = new byte[9]; 
         b[0] = 4; 
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(VehicleId), b, 1);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(ContainerId), b, 5);
         
         publisher.send(b, 0);
         System.out.println("Net: Remove container " + ContainerId);
     }
     
     public void createCrane(Crane.Crane crane){
         //22Byte total
         byte[] b = new byte[22];
         int vehicleID = crane.getID();
         byte vehicleType = 0;
         
         if(crane.getType() == Crane.Crane.CraneType.storage){
             vehicleType = 0;
         }else if(crane.getType() == Crane.Crane.CraneType.barge){
             vehicleType = 1;
         }else if(crane.getType() == Crane.Crane.CraneType.seaship){
             vehicleType = 2;
         }else if(crane.getType() == Crane.Crane.CraneType.train){
             vehicleType = 3;
         }else if(crane.getType() == Crane.Crane.CraneType.truck){
             vehicleType = 4;
         }
         
 
         
         
         Helpers.Vector3f pos = crane.getPosition();
         
         b[0] = 6;       //Operation ID
         
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(vehicleID), b, 1);
 
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(pos.x), b, 5);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(pos.y), b, 9);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(pos.z), b, 13);
         
        Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(crane.rotation), b, 17);        
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(vehicleType), b, 21);
         
         publisher.send(b, 0);
         System.out.println("Net: Creating Crane " + vehicleID);
     }
     
     public void syncCrane(Crane.Crane crane){
         //17Byte total
         byte[] b = new byte[17];
         int vehicleID = crane.getID();
         byte vehicleType = 0;
         
         if(crane.getType() == Crane.Crane.CraneType.storage){
             vehicleType = 0;
         }else if(crane.getType() == Crane.Crane.CraneType.barge){
             vehicleType = 1;
         }else if(crane.getType() == Crane.Crane.CraneType.seaship){
             vehicleType = 2;
         }else if(crane.getType() == Crane.Crane.CraneType.train){
             vehicleType = 3;
         }else if(crane.getType() == Crane.Crane.CraneType.truck){
             vehicleType = 4;
         }
         
 
         
         
         Helpers.Vector3f pos = crane.getPosition();
         
         b[0] = 7;       //Operation ID
         
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(vehicleID), b, 1);
 
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(pos.x), b, 5);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(pos.y), b, 9);
         Helpers.byteHelper.addToArray(Helpers.byteHelper.toByta(pos.z), b, 13);
         
         
         publisher.send(b, 0);
         System.out.println("Net: Creating Crane " + vehicleID);
     }
 }
