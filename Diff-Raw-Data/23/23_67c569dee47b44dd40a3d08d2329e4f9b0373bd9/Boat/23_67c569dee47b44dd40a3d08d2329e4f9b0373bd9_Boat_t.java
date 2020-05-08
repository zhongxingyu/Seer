 package Vehicles;
 
 import Helpers.Vector3f;
 import Main.Container;
 import Pathfinding.Node;
 import Pathfinding.Pathfinder;
 import Storage.Storage_Area;
 import java.util.Date;
 
 /**
  * The boat.
  * @author Tonnie Boersma
  */
 public class Boat extends TransportVehicle {
     
     private Date arrivalDate;
     private Date departureDate;
     private String arrivalCompany;
     public Storage_Area storage;
     private Vector3f position;
     private Node destination;
     private Node[] route;
     private float speed/*= X*/;
 
     /**
      * Generate a new boat, with an empty containerfield.
      * @param arrivalDate The arrival date.
      * @param departureDate The Departure date.
      * @param containerArraySize The ships containerfield sizes.
      * @param startPosition The spawn position.
      * @throws Exception If a variable is equel to null.
      */
     public Boat(Date arrivalDate, Date departureDate, String arrivalCompany, Vector3f containerArraySize, Node startPosition) throws Exception
     {
         if (arrivalDate == null || departureDate == null || arrivalCompany == null || containerArraySize == null || startPosition == null){
             throw new Exception("\nThe input variable can't be null:"+
                     "\narrivalDate: " + arrivalDate +
                     "\ndepartureDate: " + departureDate +
                     "\narrivalCompany: " + arrivalCompany +
                     "\ncontainerArraySize: " + containerArraySize +
                     "\nstartPosition: " + startPosition);
         }
         else{
             this.position = startPosition.getPosition();
             this.destination = startPosition;
             this.arrivalDate = arrivalDate;
             this.departureDate = departureDate;
             this.arrivalCompany = arrivalCompany;
             storage = new Storage_Area((int)containerArraySize.x, (int)containerArraySize.z, (int)containerArraySize.y, position);
         }
     }
     
     @Override
     public void setDestination(Node destination) {
         try {
             this.destination = destination;
             route = Pathfinding.Pathfinder.findShortest(Pathfinder.findClosestNode(position), destination);
         } 
         catch (Exception ex) {
 
         }
     }
 
     @Override
     public Node getDestination() {
        return (route.length == 0) ? Pathfinder.findClosestNode(position) : route[0];
     }
 
     @Override
     public Vector3f getPosition() {
         return position;
     }
     
     public void setPostion(Vector3f position){
         this.position = position;        
     }
 
     @Override
     public void update(int gameTime) throws Exception {
         if (position == destination.getPosition()){
             // send message
             // wait for message depart
         }
         else{
             // follow route 
             // update position
         } 
     }
     
     @Override
     public Date GetArrivalDate(){
         return arrivalDate;
     }
     
     @Override
     public Date GetDepartureDate(){
         return departureDate;
     }
     
     @Override
     public String GetCompany(){
         return arrivalCompany;
     }
     
     @Override
     public boolean MatchesContainer(Container container){        
         return this.GetArrivalDate().equals(container.getArrivalDateStart()) && 
                 this.GetDepartureDate().equals(container.getArrivalDateEnd()) &&
                 this.GetCompany().equals(container.getArrivalCompany());
     }
     
     @Override
     public String toString(){
         return  "\n" + Container.df.format(arrivalDate) + " <-> " + Container.df.format(departureDate) +
                 "\n" + "ArrivalCompany: " + arrivalCompany +
                 "\n" + "ContainerfieldLenght: " + storage.getLength() + 
                 "\n" + "ContainerfieldWidth: " + storage.getWidth() + 
                 "\n" + "ContainerfieldHeight: " + storage.getHeight() + 
                 "\n" + "_____________________________(" + storage.Count() + ")"+ 
                 "\n" + storage;
     }
 
     @Override
     public Storage_Area GetStorage() {
         return storage;
     }
 
     @Override
     public void SetStorage(Storage_Area sa) {
         storage = sa;
     }
     
 //    /**
 //     * Gets the bestcontainer from the given row.
 //     * @param rowIndex The row to get the container from.
 //     * @return The container with the latest departure date.
 //     * @throws Exception If the row is empty.
 //     */
 //    public Container GetBestContainer(int rowIndex) throws Exception {
 //        if (0 > rowIndex || rowIndex > size.x){
 //            throw new Exception("Row " + rowIndex + " doesn't exist on this ship.");
 //        }
 //        else{
 //            Date max = null;
 //            for (int z = 0; z < size.z; z++) {
 //                if (containerField[z][rowIndex].count() > 0){
 //                    Date now = ((Container)containerField[z][rowIndex].peek()).getDepartureDateStart();
 //                    if (max == null){
 //                        max = now;
 //                    }
 //                    else if (max.before(now)){
 //                        max = now;
 //                    }
 //                }
 //            }
 //            for (int z = 0; z < size.z; z++) {
 //                if (containerField[z][rowIndex].count() > 0){
 //                    if(max == ((Container)containerField[z][rowIndex].peek()).getDepartureDateStart()){
 //                        return (Container)containerField[z][rowIndex].pop();
 //                    }
 //                }
 //            }
 //            if (max == null){
 //                throw new Exception("Their are no containers in the row");
 //            }
 //        }
 //        return null;
 //    }
 //
 //    /**
 //     * Places the container in the row.
 //     * @param container The container to place on the ship.
 //     * @param rowIndex The row to place the container.
 //     * @throws Exception If tthe row is full.
 //     */
 //    public void SetContainer(Container container, int rowIndex) throws Exception {
 //        if (container == null){
 //            throw new Exception("Can't place an empty container.");
 //        }
 //        else if (0 > rowIndex || rowIndex > size.x){
 //            throw new Exception("Row " + rowIndex + " doesn't exist on this ship.");
 //        }  
 //        else{
 //            int counter = 0;
 //            for (int z = 0; z < size.z; z++) {
 //                counter += containerField[z][rowIndex].count();
 //            }
 //            if (counter >= size.y*size.z){
 //                throw new Exception("Row " + rowIndex + " is full can't place container.");
 //            }
 //            else{
 //                for (int z = 0; z < size.z; z++) {
 //                    if(containerField[z][rowIndex].count() < size.y){
 //                        containerField[z][rowIndex].push(container);
 //                        break;
 //                    }
 //                }
 //            }
 //        }
 //    }
 //    
 //    /**
 //     * Sets the container at the position.
 //     * This is still buggy and should be followed by the CheckShip() methode.
 //     * @param container The container to place on the ship.
 //     * @param containerPos The position to place the container.
 //     * @throws Exception If their is allready a container at the position, or if the container is equel to zero.
 //     */
 //    public void SetContainerManual(Container container, Vector3f containerPos) throws Exception {
 //        int x = (int)containerPos.x;
 //        int y = (int)containerPos.y;
 //        int z = (int)containerPos.z;
 //        
 //        // <editor-fold defaultstate="collapsed" desc="generate Exception">
 //        String ExceptionString = "";
 //        if (container == null){
 //            ExceptionString += ("\nCan't place a null container");
 //        }
 //        if (0 > x || x > size.x){
 //            ExceptionString += ("\nThe X index needs to be between 0 and " + size.x + 
 //                                ".\nUsed index: " + x);
 //        }
 //        if (0 > y || y > size.y){
 //            ExceptionString += ("\nThe Y index needs to be between 0 and " + size.y + 
 //                                ".\nUsed index: " + y);
 //        }
 //        if (0 > z || z > size.z){
 //            ExceptionString += ("\nThe Z index needs to be between 0 and " + size.z + 
 //                                ".\nUsed index: " + z);
 //        }
 //        // </editor-fold>
 //        
 //        if (ExceptionString.length() == 0){
 //            if (containerField[z][x].HasHole(y)){
 //                containerField[z][x].SetStackItem(container, y);
 //            }
 //            else{
 //                throw new Exception("Their is allready a container.");
 //            }
 //        }
 //        else{
 //            throw new Exception(ExceptionString);
 //        }
 //    }
 //    
 //    /**
 //     * Checks if their are holes in the stacked containers.
 //     * @return True if their are no holes.
 //     */
 //    public boolean CheckShip(){
 //        for (int x = 0; x < size.x; x++) {
 //            for (int z = 0; z < size.z; z++) {
 //                if (containerField[z][x].HasHole()){
 //                    System.out.println(z + ":" + x);
 //                    return false;
 //                }
 //            }
 //        }
 //        return true;
 //    }
 }
 
 
