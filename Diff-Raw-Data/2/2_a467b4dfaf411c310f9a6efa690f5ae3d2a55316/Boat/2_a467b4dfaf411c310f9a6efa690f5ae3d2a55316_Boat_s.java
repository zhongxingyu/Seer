 package vehicles;
 
 import Helpers.GameTime;
 import Helpers.Vector3f;
 import Main.Container;
 import Pathfinding.Node;
 import java.sql.Date;
 
 public class Boat extends Vehicle {
     
     private Date arrivalDate;
     private Date departureDate;
     private Container[][][] containerList;
     private Node position;
     private Node destination;
     private Node[] route;
     private float speed/*= X*/;
     
     public Boat(Date arrivalDate, Date departureDate, Vector3f containerArraySize, Node startPosition)
     {
         this.position = startPosition;
         this.arrivalDate = arrivalDate;
         this.departureDate = departureDate;
         containerList = new Container[(int)containerArraySize.x][(int)containerArraySize.y][(int)containerArraySize.z];
     }
     
     @Override
     public void setDestination(Node destination) {
         try {
             this.destination = destination;
             route = Pathfinding.Pathfinder.findShortest(position, destination);
         } 
         catch (Exception ex) {
 
         }
     }
 
     @Override
     public Node getDestination() {
         return (destination == null) ? position : destination;
     }
 
     @Override
     public Vector3f getPosition() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void update(GameTime gameTime) {
         if (position == destination){
             // send message
             // wait for message depart
         }
         else{
             // follow route 
             // update position
         } 
     }
 
     @Override
     public Container GetContainer(Vector3f containerPos) throws Exception {
         int x = (int)containerPos.x;
         int y = (int)containerPos.y;
         int z = (int)containerPos.z;
         
         int clx = containerList.length;
         int cly = containerList[0].length;
         int clz = containerList[0][0].length;
         
         
         if (0 > x || x > clx){
             throw new Exception("The X index needs to be between 0 and " + clx + 
                                 ".\n Used index: " + x);
         }
         else if (0 > y || y > cly){
             throw new Exception("The Y index needs to be between 0 and " + cly + 
                                 ".\n Used index: " + y);
         }
        else if (0 > y || y > cly){
             throw new Exception("The Z index needs to be between 0 and " + clz + 
                                 ".\n Used index: " + z);
         }
         else{
             
             if (containerList[x][y][z] != null){
                 return containerList[x][y][z];
             }
             else{
                 throw new Exception("Their is no container.");
             }
         }
     }
 
     @Override
     public void SetContainer(Container container, Vector3f containerPos) throws Exception {
         int x = (int)containerPos.x;
         int y = (int)containerPos.y;
         int z = (int)containerPos.z;
         
         int clx = containerList.length;
         int cly = containerList[0].length;
         int clz = containerList[0][0].length;
         
         
         if (0 > x || x > clx){
             throw new Exception("The X index needs to be between 0 and " + clx + 
                                 ".\n Used index: " + x);
         }
         else if (0 > y || y > cly){
             throw new Exception("The Y index needs to be between 0 and " + cly + 
                                 ".\n Used index: " + y);
         }
         else if (0 > y || y > cly){
             throw new Exception("The Z index needs to be between 0 and " + clz + 
                                 ".\n Used index: " + z);
         }
         else{
             if (containerList[x][y][z] == null){
                 containerList[x][y][z] = container;
             }
             else{
                 throw new Exception("Their is allready a container.");
             }
         }
     }
 }
