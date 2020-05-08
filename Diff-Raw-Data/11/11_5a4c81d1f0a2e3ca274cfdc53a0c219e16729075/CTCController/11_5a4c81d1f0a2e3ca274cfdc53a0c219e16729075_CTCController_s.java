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
     
     CTCController ( CTCMessageServer msgServer ) 
     {
         _msgServer = msgServer;
         _trainList = new TrainList( this );
         _blockList = new BlockList( this );
         _controllerList = new ControllerList ( this );
         _schedule = new ScheduleViewModel( this );
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
             System.out.println("CTC: train " + tID + " Activated!");
         }
     }
     
     public void removeTrain ( Integer tID )
     {
         if (_trainList.contains(tID))
         {
             _trainList.removeTrain(tID);
             System.out.println("CTC: train " + tID + " Removed!"); 
         }
     }
     
     public void updateOccupancy ( Integer bID )
     {   
         // determine which train this "block" belongs to
        Integer tID = _trainList.nextBlocksForTrains().get(bID);
         System.out.println("CTC: block " + bID + "is now occupied!");
         // updateOccupancy(bID, tID);    
     }
     
     public void updateOccupancy( Integer bID, Integer tID )
     {
        Integer vacantBlockID = _trainList.getTrain(tID).getCurrentBlock();
        _blockList.getBlock(bID).setVacant();
        _trainList.getTrain(tID).setCurrentBlock(bID);
        _blockList.getBlock(bID).setCurrentTrain(tID);
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
 }
