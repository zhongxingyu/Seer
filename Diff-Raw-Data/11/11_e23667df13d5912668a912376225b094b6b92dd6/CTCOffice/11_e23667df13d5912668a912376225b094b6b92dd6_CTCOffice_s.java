 package TLTTC;
 import java.util.*;
 import java.util.concurrent.*;
 
 
 public class CTCOffice extends Worker implements Runnable, constData {
     
     
     // ivars! Yay!
     private static String _sender = "CTC";
     private java.util.concurrent.LinkedBlockingQueue<Message> msgs;
     private Module name = Module.CTC; //CTCOffice?
     // private ArrayList<BlockViewModel> _blockList;
     // private ArrayList<ControllerViewModel> _controllerList;
     private ArrayList<TrainViewModel> _trainList;
     // 
     
     CTCOffice () {
         // Do any set-up needed, launch the GUI, and get it on!
         msgs = new java.util.concurrent.LinkedBlockingQueue<Message>();
         // generate a virtual model of the track, boi!
         
         // 
     }
     
     public void run () {
         while(true) {
             if (msgs.peek() != null)
                 {
                 
                 Message m = msgs.poll();
                 if (name == m.getDest())
                 { // hey, this was sent to me; let's do something
                     switch (m.getType()) {
 		    case TnMd_CTC_Confirm_Train_Creation: // hey a train really did get made!
 			// unpack the data from the message
 			m.getData().get("trainID");
                         break; // end Confirm train creation case
 		    case TnMd_CTC_Request_Train_Destruction: // Aw snap! Train's going away, better let everyone know
 			m.getData().get("trainID");
                         break; // end train destruction case
 
 		                case TnMd_CTC_Send_Block_Occupied: // Train has definitely moved to a new block; let e'rybody know
 		                
 		                break; // end block occupied case
 		                /*
 		                 * Other cases will be implemented here in the future.
 		                 */
 		                default:// Well shit, somebody fucked up this one.
 		                    
 		                System.out.println("Unknown message Type!");
 		                
 		                break;
                     }
                 }
                 else { // it ain't ours, get that shit outta here!
                     System.out.println("PASSING MSG: step->"+name + " source->"+m.getSource()+ " dest->"+m.getDest());
 		    m.updateSender(name);
 		    Environment.passMessage(m);
                 }
             }
         }
     }
     
     public void setMsg(Message m) {
         msgs.add(m);
     }
     
     public void send() {
         
     }
 }
 /*
 class CTCController
 {
     
     public void dispatchTrain (String line) { // probably other vars needed
     
     // Compose a message to train. Holla at your boy!
     }
     
     public void generateSchedule() { // vars?
         
     }
     
     public void setAuthority (int authority) { // yeah, need other stuff too!
         
         if (authority >= 0) { // Technically, there shouldn't be any checks on authority, but come on
             // create a message to send to the train controller
             System.out.println("Good authority, let's send it!");
         }
         else {
             System.out.println("Respect my authoritah! And make that number positive!");
         }
     }
     
     public void setSpeed(double speed) { // and other shit, too
     
     if (speed >= 0) {
         System.out.println("Good speed, let's send it!");
     }
     else {
         System.out.println("Naw man, that speed don't make no sense!");
     }
         
     }
     
     private void updateBlockForTrain(int blockID, int trainID) {
     
     }
     
     private void updateControllerForTrain(int controllerID, int trainID) {
     
     }
 }
 */
 class TrainViewModel
 {
     // ivars and such
     private final int _trainID;
     private int _fixedBlockAuthority;
     private double _movingBlockAuthority;
     private double _speed;
     private int _currentBlockID;
     private int _currentControllerID;
     
     // methods. Get/set some!
     public int getTrainID () {
         return _trainID;
     }
     
     public int getFixedBlockAuthority() {
         return _fixedBlockAuthority;   
     }
     
     public void setFixedBlockAuthority(int authority) {
         if (authority >= 0) {
             _fixedBlockAuthority = authority;
         }
     }
     
     public double getMovingBlockAuthority() {
         return _movingBlockAuthority;
     }
     
     public void setMovingBlockAuthority(double authority) {
         if (authority >= 0) {
             _movingBlockAuthority = authority;
         }
     }
     
     public double getSpeed() {
         return _speed;
     }
     
     public void setSpeed(double speed) {
         if (speed >= 0) {
             _speed = speed;
         }
     }
     
     public int getCurrentBlockID() {
         return _currentBlockID;
     }
     
     public void setCurrentBlockID(int id) {
         _currentBlockID = id;
     }
     
     public int getCurrentControllerID() {
         return _currentControllerID;
     }
     
     public int setCurrentControllerID(int id) {
         return _currentControllerID;
     }
     
     // some REAL methods:
     TrainViewModel(int tID)
     {
         _trainID = tID;
         _currentBlockID = 0;
         _currentControllerID = 0;
         _fixedBlockAuthority = 0;
         _movingBlockAuthority = 0;
         _speed = 0;
     }
     
     TrainViewModel(int tID, int bID, int cID)
     {
     // Convenience initializer to set custom block/controller ids.
         _trainID = tID;
         _currentBlockID = bID;
         _currentControllerID = cID;
         _fixedBlockAuthority = 0;
         _movingBlockAuthority = 0;
         _speed = 0;
     }
     TrainViewModel( int tID, int bID, int cID, int fBA, int mBA, int s)
     {
     // Convenience to set all ivars at init.
         _trainID = tID;
         _currentBlockID = bID;
         _currentControllerID = cID;
         _fixedBlockAuthority = fBA;
         _movingBlockAuthority = mBA;
         _speed = s;
     }
 }
 
 class BlockViewModel
 {
     private final int _blockID;
     private int _currentOccupantID;
     private final int _controllerID;
     
     // get/set methods
     public int getBlockID() {
         return _blockID;
     }
     
     public getControllerID {
         return _controllerID;
     }
     
    public int getCurrentOccupantID {
         return _currentOccupantID;
     }
     
    public int setCurrentOccupantID (int id) {
         if (id >= -1) {
             _currentOccupantID = id;
         }
     }
     
     // Constructors
    public BlockViewModel (ind bID, int cID) {
         _blockID = bID;
         _controllerID = cID;
         _currentOccupantID = -1;
     }
     
     // Other Methods
     
     public boolean isOccupied() {
         if (_currentOccupantID >= 0)
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 }
 
 /*
 class ControllerViewModel
 {
     // ivars and all that
     private final int _controllerID;
     private final ArrayList<Integer> _containedBlockIDs;
     private ArrayList<Integer> _containedTrainIDs;
     
     // get/set methods
     public int getControllerID() {
         return _controllerID;
     }
     
     public ArrayList<Integer> getContainedBlockIDs() {
         return _containedBlockIDs;
     }
     
     public ArrayList<Integer> getContainedTrainIDs() {
         return _containedTrainIDs;
     }
     
     public void addContainedTrainID (int id) {
         if (id >= 0) {
             _containedTrainIDs.add(id);
         }
     }
     
     public int removeContainedTrainID (int id) {
         int tID = -1;
         if (_containedTrainIDs.contains(id)) {
             int tID = _containedTrainIDs.remove(id);
         }
         
         return tID;
     }
     
    
 }*/
