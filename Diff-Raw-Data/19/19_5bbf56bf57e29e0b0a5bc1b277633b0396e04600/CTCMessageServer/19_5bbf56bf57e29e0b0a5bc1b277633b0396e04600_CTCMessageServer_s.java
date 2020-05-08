 /*
  * Author: Sean Moore
  * Last Updated: 20 April, 2013
  * Purpose: Message Server Class for communicating to and from CTC Modules
  */
 
 // Imports and Packages
 package TLTTC;
 import java.util.*;
 import java.util.concurrent.*;
 
 
 public class CTCMessageServer extends Worker implements Runnable, constData
 {
 
     // ivars
     private java.util.concurrent.LinkedBlockingQueue<Message> msgs;
     private Module name = Module.CTC; 
     private CTCController controller = new CTCController(this);
     
     CTCMessageServer ()
     {
         msgs = new java.util.concurrent.LinkedBlockingQueue<Message>();
     }
     
     public void run()
     {
         
         while (true) 
         {
             if (msgs.peek() != null)
             {
                 Message m = msgs.poll(); // grab the top message
                 
                 if ( name == m.getDest() )  // check if it's ours
                 {
                     messageRouter(m);
                 }
                 else { // if not, send it to the next person
                     m.updateSender(name);
                     Environment.passMessage(m);
                 }
             }
         }
     }
     
     public void setMsg (Message m)
     {
         msgs.add(m);
     }   
     
     public void send(Message m)
     {
         System.out.println("SENDING MSG ~ (start : "+m.getSource() + "), (dest : "+m.getDest()+"), (type : " + m.getType()+ ")");
         Environment.passMessage(m);
     }
     
     public void composeMessage( Module dest, msg type, Hashtable<String, Object> data ) {
         Message m = new Message(name, name, dest, type);
         
         for ( String key : data.keySet() )
         {
             m.addData( key, data.get(key) ); // adding all the data
         }
         send(m);
     }
     
     private void messageRouter(Message m)
     {
         Integer bID, tID;
         
         switch (m.getType())
         {
             // messages from Train Controller
             
             // messages from Train Model
             case TnMd_CTC_Confirm_Train_Creation:
                 // begin confirm train creation case
                 tID = (Integer) m.getData().get("trainID");
                 controller.addTrain(tID);
                 
             break; // end confirm train creation case
             case TnMd_CTC_Request_Train_Destruction:
                 // begin train destruction case
                 tID = (Integer) m.getData().get("trainID");
                 controller.removeTrain(tID);
                 
             break; // end request train destruction case
             case TnMd_CTC_Send_Block_Occupied:
                 // begin block occupied case
                 bID = (Integer) m.getData().get("blockID");
                 try 
                 {
                     boolean isStation = (boolean) m.getData().get("isStation");
                     tID = (Integer) m.getData().get("trainID");
                     if (tID != null)
                     {
                         controller.updateOccupancy(bID, tID);
                     }
                     else
                     {
                         controller.updateOccupancy(bID);
                     }
                     if (isStation)
                     {
                         controller.reachedNextStation(tID);
                     }
                 }
                 catch (Exception e)
                 {
                     controller.updateOccupancy(bID);
                 }
             break; // end block occupied case
             
             // messages from Track Model
             
             // messages from Track Controller
                 
             // messages from Scheduler
             case Sch_CTC_Send_Schedule:
                 // begin send schedule case
                 controller.updateSchedules((Timetable) m.getData().get("schedule"));
             break; // end send schedule case
         }
     }
 }
