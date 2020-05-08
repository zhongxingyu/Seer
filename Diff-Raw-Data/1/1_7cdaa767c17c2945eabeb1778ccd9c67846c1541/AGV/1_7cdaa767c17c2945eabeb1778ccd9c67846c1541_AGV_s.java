 package Vehicles;
 
 import Helpers.Message;
 import Helpers.Vector3f;
 import Main.Container;
 import Pathfinding.Node;
 import Pathfinding.Pathfinder;
 import java.util.ArrayList;
 import java.util.List;
 
 public class AGV extends Vehicle {
 
     private Container container;
     private Node destination;
     private Vector3f position;
     private Node[] route;
     private final float SpeedWithContainer = 72;
     private final float SpeedWithoutContainer = 144;    
     private List<Message> assignments;
     
     
     public AGV(Node startPosition){
         this.position = startPosition.getPosition();
         assignments = new ArrayList();
     }
     
     @Override
     public void setDestination(Node destination) {
         try {
             route = Pathfinding.Pathfinder.findShortest(Pathfinder.findClosestNode(position), destination, container == null);
             this.destination = route[route.length-1];
         } 
         catch (Exception ex) {
         }
     }
 
     @Override
     public Node getDestination() {
         return (destination == null) ? Pathfinder.findClosestNode(position) : destination;
     }
 
     @Override
     public Vector3f getPosition() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void update(float gameTime) throws Exception {
         if (position == destination.getPosition()){
             // send message arrived
         }
         else{
             if (container == null){
                 // follow route SpeedWithoutContainer
                 // update position
             }
             else{
                 // follow route SpeedWithContainer
                 // update position
             }
         }   
         // When the AGV has assignments
         if(!Available())
         {
             // When the AGV need's the fetch a container
             if(assignments.get(0).Fetch())
             {
                 // When the AGV has a container on him
                 if(container != null)
                 {
                     // Remove assingment because the container is fetched
                     assignments.remove(0);                  
                 }
             }
             // When the AGV need's to deliver a container
             else if(assignments.get(0).Deliver())
             {
                 // When the AGV doesn't has a contianer on him
                 if(container == null)
                 {
                     // Remove assingment because the contianer is deliverd
                     assignments.remove(0); 
                 }
             }
             else
             {
                 // When the assignment is not is Deliver, Fetch
                 throw new Exception("Wrong assignment AGV Can't Load or Unload");
             }
             // When there are no assignments left
             if(Available())
             {
                 /**
                  * 
                  * TODO Send the AGV to the nearest parking lot
                  * 
                  */
             }
         }
         
     }
 
     public Container GetContainer() throws Exception {
         if (container != null){
            return container;
         }
         else{
             throw new Exception("Their is no container.");
         }
    }
 
     public void SetContainer(Container container) throws Exception{
         if (container != null){
             throw new Exception("This vehicle can't carry more then one container.");
         }
         else{
             this.container = container;
         }
     }
     
     /**
      * When there are no assignments for the AGV
      * @return 
      */
     public boolean Available()
     {
         return assignments.isEmpty();
     }
     
     /**
      * Add's an assignment for the agv
      * @param mess 
      */
     public void AddAssignment(Message mess)
     {
         assignments.add(mess);
     }
 }
 
