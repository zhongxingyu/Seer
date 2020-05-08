/*
  * Created by: Christopher Feveck
  * Purpose: To create an elevator class with the dimensions of the elevator
  * Note: The people on the floors wait in a link list. 
  * The elevator takes requests in queues.
  */
 package Elevator;
 import java.util.*;
 
 public class Elevator implements Comparable 
 {
     //Time of the event. When it runs
     private int tick;
     private int amt_elevators;
     private int currentFloor;
     private boolean direction;
     private int floors;
     private PriorityQueue <Object> requestQueue;
     private LinkedList <Object> elev_persons;
     
     public Elevator(int tick, int floors)
     {
         this.tick = tick;
         this.amt_elevators=1;
         direction = true;
         this.floors = floors;
         requestQueue = new PriorityQueue<Object>();
         elev_persons= new LinkedList <Object>();
     }
 
     public Iterator Get_RequestIterator()
     {
         return requestQueue.iterator();
     }
     
     public LinkedList Get_PersonsLinkedList()
     {
         return elev_persons;
     }
     
     
     public Iterator Get_PersonsIterator()
     {
         return elev_persons.iterator();
     }
     
     public void Set_Request(Request request)
     {
         requestQueue.add(request);
     }
     
     public Request Get_Request()
     {
         return (Request)requestQueue.peek();
     }
     
     public void Remove_Request()
     {
         requestQueue.remove();
     }
     
     
     public void Set_Persons(Person person)
     {
         elev_persons.add(person);
     }
     
     /*
     public Request Get_Request()
     {
         return (Request)requestQueue.peek();
     }
     * */
     
     public boolean Increment()
     {
         boolean result = true;
         
         if(currentFloor + 1 < floors)
             currentFloor +=1;
         else
         {
             direction = false;
             return false;
         }
         
         return result;
     }
     
     public boolean Decrement()
     {
         boolean result = true;
         
         if(currentFloor - 1 >= 0)
             currentFloor -=1;
         else
         {
             direction = true;
             result = false;
         }
         
         return result;
     }
 
     public void Set_Direction(boolean direction)
     {
         this.direction = direction;
     }
     
     public boolean Get_direction()
     {
         return direction;
     }
     public void Set_CurrentFloor(int currentFloor)
     {
         this.currentFloor = currentFloor;
     }
     
     public int Get_CurrentFloor()
     {
         return currentFloor;
     }
     public void set_amt_elevators(int amt_elevators)
     {
         this.amt_elevators=amt_elevators;
     }
     
     public int get_amt_elevators()
     {
         return amt_elevators;
     }
     
     public void set_tick(int tick)
     {        
         this.tick=tick;
     }
 
     
     public int get_tick()
     {        
         return tick;
     }    
     /*
      * Implements the comparable interface and compares the object on the queue
      * (Needed for the priority queue 0 denotes equality -1 for <, 1 for >)
      * 0 is only the concern. Anything else is a fault.
      */
     public int compareTo(Object obj )
     {
         int result=-1;
         
             if (obj instanceof Elevator)
             {
                 if(tick<((Elevator)obj).get_tick())
                 {
                     result=-1;
                 }
                 else if(tick==((Elevator)obj).get_tick())
                 {
                     result=0;
                 }
                 else
                 {
                     result=1;
                 }
                 
             }
         else if (obj instanceof Create_class)
         {
             if(tick < ((Create_class)obj).get_tick())
             {
                 result=-1;
             }
             else if(tick==((Create_class)obj).get_tick())
             {
                 result=0;
             }
             else
             {
                 result=1;
             }
             //result = (tick < ((Create_class)obj).get_tick() ? -1 : (tick == ((Create_class)obj).get_tick() ? 0 : 1));
         }
         else if (obj instanceof Person)
         {
             if(tick < ((Person)obj).get_tick())
             {
                 result=-1;
             }
             else if(tick==((Person)obj).get_tick())
             {
                 result=0;
             }
             else
             {
                 result=1;
             }
             
         }
                         else if (obj instanceof Request)
         {
             if(tick < ((Request)obj).get_tick())
             {
                 result=-1;
             }
             else if(tick==((Request)obj).get_tick())
             {
                 result=0;
             }
             else
             {
                 result=1;
             }
 
         }         
             
         
     return result;
     }
 
 }
 
