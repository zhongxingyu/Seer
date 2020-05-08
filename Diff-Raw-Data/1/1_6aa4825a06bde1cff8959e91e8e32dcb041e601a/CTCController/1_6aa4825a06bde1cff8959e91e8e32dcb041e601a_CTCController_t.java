 /*
  *
  */
 
 // Imports and Packages
 package TLTTC;
 import java.util.*;
 
 
 public class CTCController implements constData
 {
     
     private CTCMessageServer _msgServer;
     private TrainList _trainList;
     private BlockList _blockList;
     private ControllerList _controllerList;
     private ScheduleViewModel _schedule;
     private Integer _trainCount = 0;
     private CTCUI _CTCInterface;
     
     CTCController ( CTCMessageServer msgServer ) 
     {
         _msgServer = msgServer;
         _trainList = new TrainList( this );
         _blockList = new BlockList( this );
         _controllerList = new ControllerList ( this );
         _schedule = new ScheduleViewModel( this );
         _CTCInterface = new CTCUI( this );
        _CTCInterface.setVisible(true);
     }
     
     // Inbound handlers
     public void updateSchedules ( Timetable t ) 
     {
         
     }
     
     public void addTrain ( Integer tID )
     {
         if (_trainList.contains(tID))
         {
             _trainList.setActive(tID);
         }
         else
         {
             _trainList.addTrain(tID, _CTCInterface.getCurrentLine());
             _trainList.setActive(tID);
         }
         _CTCInterface.setDataModelForTable(_trainList.getTrain(tID));
     }
     
     public void removeTrain ( Integer tID )
     {
         if (_trainList.contains(tID))
         {
             _trainList.removeTrain(tID);
         }
     }
     
     public void updateOccupancy ( Integer bID )
     {   
         // determine which train this "block" belongs to
         Integer tID = _trainList.nextBlocksForTrains().get(bID);
         updateOccupancy(bID, tID);    
     }
     
     public void updateOccupancy( Integer bID, Integer tID )
     {
         Integer vacantBlockID = _trainList.getTrain(tID).getCurrentBlock();
         _blockList.getBlock(bID).setVacant();
         _trainList.getTrain(tID).setCurrentBlock(bID);
         _blockList.getBlock(bID).setCurrentTrain(tID);
         _CTCInterface.setDataModelForTable(_trainList.getTrain(tID));
     }
     
     // outbound handlers
     public void sendManualSpeed ( Double speed, Integer tID )
     {
         Hashtable<String, Object> data = new Hashtable<String, Object>();
         data.put("velocity", speed);
         data.put("trainID", tID);
         Module destination = Module.trainController;
         msg type = msg.CTC_TnCt_Send_Manual_Speed;
         _msgServer.composeMessage(destination, type, data);
     }
     
     public void sendManualMovingBlockAuthority ( Double authority, Integer tID )
     {
         Hashtable<String, Object> data = new Hashtable<String, Object>();
         data.put("authority", authority);
         data.put("trainID", tID);
         Module destination = Module.trainController;
         msg type = msg.CTC_TnCt_Send_Manual_MovingBlock;
         _msgServer.composeMessage(destination, type, data);
     }
     
     public void sendManualFixedBlockAuthority ( Integer authority, Integer tID )
     {
         Hashtable<String, Object> data = new Hashtable<String, Object>();
         data.put("authority", authority);
         data.put("trainID", tID);
         Module destination = Module.trainController;
         msg type = msg.CTC_TnCt_Send_Manual_FixedBlock;
         _msgServer.composeMessage(destination, type, data);
     }
     
     public void dispatchTrain ( String line )
     {
         Hashtable<String, Object> data = new Hashtable<String, Object>();
         data.put("trainID", _trainCount);
         data.put("line", line);
         Module destination = Module.trainModel;
         msg type = msg.CTC_TnMd_Request_Train_Creation;
         _msgServer.composeMessage(destination, type, data);
         _trainList.addTrain(_trainCount, line);
         _trainCount++;
     }
     
     public void generateSchedule ( Integer tID ) {
         Hashtable<String, Object> data = new Hashtable<String, Object>();
         // determine line for train
         String line = _trainList.getTrain(tID).getLine();
         data.put("trainID", tID);
         data.put("line", line);
         Module destination = Module.scheduler;
         msg type = msg.CTC_Sch_Generate_Schedule;
         _msgServer.composeMessage(destination, type, data);
         
     }
     
     public void closeTrackSections ( ArrayList<Integer> bIDs )
     {
         Hashtable<String, Object> data = new Hashtable<String, Object>();
         data.put("blockIDs", bIDs);
         Module destination = Module.trackModel;
         msg type = msg.CTC_TcMd_Send_Track_Closing;
         _msgServer.composeMessage(destination, type, data);   
     }
     
     public void openTrackSections ( ArrayList<Integer> bIDs )
     {
         Hashtable<String, Object> data = new Hashtable<String, Object>();
         data.put("blockIDs", bIDs);
         Module destination = Module.trackModel;
         msg type = msg.CTC_TcMd_Send_Track_Opening;
         _msgServer.composeMessage(destination, type, data);
     }
     
     public ArrayList<TrainViewModel> getTrainList ()
     {
         if (_trainList.trainCount() > 0)
         {
             return _trainList.getTrains(); // returns array list of train view models
         }
         else
         {
             return null; // be sure to check for this when called
         }
     }
     
     public void setSpeedForTrain ( Integer tID, Double speed )
     {
         // GUI calls this to set the speed of a train, so that controller can handle all the necessary interactions that must occur. Namely, a message must be sent and a model must be updated.
         
         _trainList.getTrain(tID).setSpeed(speed);
         _CTCInterface.setDataModelForTable(_trainList.getTrain(tID));
         sendManualSpeed(speed, tID);
     }
     
     public void setAuthorityForTrain ( Integer tID, Double authority)
     {
         _trainList.getTrain(tID).setMovingBlockAuthority(authority);
         _CTCInterface.setDataModelForTable(_trainList.getTrain(tID));
         sendManualMovingBlockAuthority(authority, tID);
     }
     
     public void setAuthorityForTrain( Integer tID, Integer authority)
     {
         _trainList.getTrain(tID).setFixedBlockAuthority(authority);
         _CTCInterface.setDataModelForTable(_trainList.getTrain(tID));
         sendManualFixedBlockAuthority(authority, tID);
     }
     
     public ArrayList<Integer> getRouteListingForTrain( Integer tID )
     {
         return _trainList.getTrain(tID).getRouteListing();
     }
 }
