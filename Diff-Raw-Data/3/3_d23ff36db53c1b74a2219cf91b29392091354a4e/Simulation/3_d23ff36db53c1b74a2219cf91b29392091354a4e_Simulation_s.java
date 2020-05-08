 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Simulation;
 
 import java.util.GregorianCalendar;
 
 /**
  *
  * @author nathantilsley
  */
 public class Simulation {
     
     private int nextArriveTime; // represented in minutes
     
     private int status; // 0 = ontime 1 = cancelled 2 = delayed
     
     private int currentTime; // represented in minutes
     
     private boolean changed; // 0 = not changed, 1 = changed
     
     private int busStopID;
     
     private int delay;
     
     private int cancel;
     
     private int serviceNumber;
     
     private GregorianCalendar date;
     
     private String message;
     
     public Simulation(int reqBusStopID, int reqNextArriveTime, int reqStatus, int reqCurrentTime, GregorianCalendar reqDate, int reqServiceNumber)
     {
         busStopID = reqBusStopID;
         nextArriveTime = reqNextArriveTime;
         status = reqStatus;
         currentTime = reqCurrentTime;
         date = reqDate;
         changed = false;
         serviceNumber = reqServiceNumber;
         delay = 0;
         message = "-";
         cancel = 0;  
     }
     
     public int getStatus()
     {
         return status;
     }
     
     public int getCurrentTime()
     {
         return currentTime;
     }
     
     public boolean getChanged()
     {
         return changed;
     }
     
     public int getNextArriveTime()
     {
         return nextArriveTime;
     }
     
     public GregorianCalendar getDate()
     {
         return date;
     }
     
     public int getBusStopID()
     {
         return busStopID;
     }
     
     public int getServiceNumber()
     {
         return serviceNumber;
     }
     
     public String getMessage()
     {
       return message;
     }
     
     public int getDelay()
     {
       return delay;
     }
    
     public void setStatus(int newStatus)
     {
         status = newStatus;
     }
     
     public void setNextArriveTime(int newArrivalTime)
     {
         nextArriveTime = newArrivalTime;
     }
     
     public void setCurrentTime(int newCurrentTime)
     {
         currentTime = newCurrentTime;
     }
     
     public void setChanged(boolean newChanged)
     {
         changed = newChanged;
     }
     
     public void setDate(GregorianCalendar newDate)
     {
         date = newDate;
     }
     
     public void setBusStopID(int newBusStopID)
     {
         busStopID = newBusStopID;
     }
     
     public void setServiceNumber(int newServiceNumber)
     {
         serviceNumber = newServiceNumber;
     }
     
     public void setDelay(int newDelay)
     {
       delay = newDelay;
     }
     
     public void setMessage(String newMessage)
     {
       message = newMessage;
     }
     
     public void setCancel(int newCancel)
     {
       cancel = newCancel;
     }
     
     public int getHours(int time)
     {
         return (int)Math.floor(time / 60);
     }
     
     public int getMinutes(int time)
     {
         return time % 60;
     }
     
     public String getRandomCancelMessage()
     {
       int rand = (int) (2 * Math.random());
       
       String message = "";
       
       switch(rand)
       {
         case 0:
           message = "The Driver is ill, this service has be cancelled. We apoigise for the inconvenience";
           break;
         case 1:
           message = "The Bus has broke down, this service has be cancelled. We apoigise for the inconvenience";
           break;
         case 2:
           message = "There has been a flood, this service has be cancelled. We apoigise for the inconvenience";
           break;
         default: 
           message = "There has been an earthquake, this service has be cancelled. We apoigise for the inconvenience";
           break;
       }
       return message;
     }
     
     public String getRandomDelayMessage()
     {
       int rand = (int) (2 * Math.random());
       
       String message = "";
       
       switch(rand)
       {
         case 0:
           message = "The Driver is ill, this service has be delayed. We apoigise for the inconvenience";
           break;
         case 1:
           message = "The Bus has broke down, this service has be delayed. We apoigise for the inconvenience";
           break;
         case 2:
           message = "There has been a flood, this service has be delayed. We apoigise for the inconvenience";
           break;
         default: 
           message = "There has been an earthquake, this service has be delayed. We apoigise for the inconvenience";
           break;
       }
       return message;
    } 
}
 }
