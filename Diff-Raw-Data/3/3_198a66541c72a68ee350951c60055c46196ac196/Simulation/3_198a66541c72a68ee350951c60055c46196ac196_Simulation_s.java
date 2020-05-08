 import java.util.ArrayList;
 import java.util.Random;
 import java.util.ArrayDeque;
 /**
  * The simulation class simulates passengers checking in
  * to flights at JavAir airlines.  Here a 'passenger' is
  * just represented by the amount of time it takes them
  * to check in on the flight. An ArrayDeque manages the
  * queue at each desk. An ArrayList manages the list of
  * desks.  
  * 
  * @author Philip Hale
  * @version final
  */
 public class Simulation  {
     private ArrayList<ArrayDeque<Integer>> desks;
     private Random rand;
     public static final int MAX_QUEUE_SIZE = 4;
     private int passengerCount;
     /**
      * Sole constructor of the Simulation class. 
      * 
      * @param minutes The amount of time in minutes to
      *                run the simulation.
      */
     public Simulation(int minutes)  {
         desks = new ArrayList<ArrayDeque<Integer>>();
         rand = new Random();
         startSim(minutes);
     }
     
     /**
      * Manages the simulation. Every other minute a 
      * new passenger arrives. Every minute the queues
      * are processed, which simulates time passing as
      * passengers slowly check in. The result of the
      * simulation is printed as the total amount of
      * time elapsed, and the total number of check 
      * in desks required. 
      * 
      * @param minutes The amount of time in minutes to
      *                run the simulation.
      */
     public void startSim(int minutes)  {
         for (int i = 1; i<minutes+1; i++)  {
            if (i%2 == 0)  {
                 newPassenger();
                 passengerCount++;
             }
             processQueues();
         }
         System.out.println("The simulation has completed.");
         System.out.println("    Time elapsed in minutes: " + minutes);
         System.out.println("    Minimum number of check in desks required: " + desks.size());
         System.out.println("    Passengers: " + passengerCount);
     }
     
     /**
      * Simulates the arrival of a new passenger at JavAir
      * airways.  It has been found that 20% of passengers 
      * buy a meal ticket, 30% a drink ticket and 50% a 
      * budget ticket. A random number generated is used
      * for this probability function.  The type of ticket
      * that a passenger buys determines the amount of time
      * taken to check in. For meal tickets this is 10 minutes, 
      * for drink tickets 8 minutes and for budget tickets 6
      * minutes.
      */
     private void newPassenger()  {
         int i = rand.nextInt(100);
         if (i < 20)  {
             addPassenger(10);
         }
         if (i > 19 && i< 50)  {
             addPassenger(8);
         }
         if (i > 49)  {
             addPassenger(6);
         }
         
     }
     
     /**
      * Sorts the passengers into check in desks.  The passengers
      * just enter the first queue that contains less than 4 people.
      * If there are no desks available with queue sizes of 3 or less, 
      * a new check in desk is opened.  Here the ArrayDeque is operating
      * in 'queue mode', adding to the bottom of the stack.
      * 
      * @param time The time taken for the passenger to be processed at
      *             the check-in desk.
      * @return true if the passenger successfully joins the queue,
      *              false otherwise
      */
     private boolean addPassenger(int time)  {
         int failCount = 0;  // keeps track of the number of full queues
         for (ArrayDeque<Integer> desk : desks)  {
             if (desk.size() < MAX_QUEUE_SIZE)  { // if there is space at the desk
                 return desk.offer((Integer) time); // add the person to the queue at that desk
             }
             else  {
                 failCount++;
             }
         }
         
         if (failCount == desks.size())  { // if all the desks are full
             desks.add(new ArrayDeque<Integer>()); // open a new desk
             addPassenger(time);
         }
         return false;
     }
     
     /**
      * Simulates the ongoing checking in process as everyone waits 
      * in line.  The amount of time left to check in for each
      * passenger at the 'front' (tail) of the queue is decremented
      * once per minute.  If subsequently the time remaining is zero,
      * that passenger leaves the queue (pop).  Here the ArrayDeque
      * functions as a stack, and uses methods inhereted from Stack.
      */
     private void processQueues()  {
         for (ArrayDeque<Integer> desk : desks)  {
             if (desk.isEmpty())  {
                 break;
             }
             int passenger = desk.peek().intValue();
             passenger--;
             desk.pop();
             desk.push(passenger);
             
             if (desk.peek().intValue() == 0)  {
                 desk.pop();
             }
         }
         
     }
 
 
 }
