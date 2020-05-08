 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Simulation;
 import RosterGenerator.Duration;
 import database.TimetableInfo;
 import Simulation.Simulation;
 import database.database;
 import database.BusStopInfo;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Arrays;
 
 /**
  *
  * @author nathantilsley
  */
 public class UpdateSimulation {
     
     public static void updateSim(ArrayList<Simulation> simulation, int route)
     {
         Date date = new Date(simulation.get(0).getDate().getTimeInMillis());
         
         // current time table kind
         TimetableInfo.timetableKind timetableKind = TimetableInfo.timetableKind(date);
         
         // array of all timetable kinds, weekday = 0 saturday = 1, sunday = 2, 
         TimetableInfo.timetableKind[] timetableKinds = TimetableInfo.timetableKind.values();
          
         // now set the current time to the next minute
         int currentTime = simulation.get(0).getCurrentTime() + 1;
         
         int currentService = 0;
         
         // array to hold any services we may have to get later on
         // for cancel or delays we may need the next service
         int[] tempServiceTimes;
         
         // set the changed variable of the Simulations to false
         for(int i = 0; i < simulation.size(); i++)
         {
           simulation.get(i).setChanged(false);
         }
         
         if(route == 66)
         {
             for(int i = 0; i < simulation.size(); i++)
             {
               simulation.get(i).setCurrentTime(currentTime);
               if(simulation.get(i).getNextArriveTime() <= currentTime)
               {
                 // the bus has come to the stop, so the next bus at the stop will be the next service
                 simulation.get(i).setServiceNumber(simulation.get(i).getServiceNumber() + 1);
               if(simulation.get(i).getServiceNumber() == 2 || simulation.get(i).getServiceNumber() == 4 ||
                    simulation.get(i).getServiceNumber() == 11 || simulation.get(i).getServiceNumber() == 13)
                 {
                   if(i < 7)
                   {
                     simulation.get(i).setServiceNumber(simulation.get(i).getServiceNumber() + 1);
                     
                   } // if
                   else
                     i -= 7;
                 } // if
                 else if(simulation.get(i).getServiceNumber() == 9)
                 {
                   if(i < 6)
                   {
                     simulation.get(i).setServiceNumber(simulation.get(i).getServiceNumber() + 1);
                   } // if
                   else
                     i-= 6;
                 } // else if
                 
               // now we need to get the service times for this stop
               tempServiceTimes = TimetableInfo.getServiceTimes(route, timetableKind, simulation.get(i).getServiceNumber());
               
               System.out.println("i " + i);
               System.out.println(tempServiceTimes.length + ":" + simulation.get(i).getServiceNumber());
               
               // some bugs in the database, so might need to sort Array
               if(i != 0)
               {
                 if(tempServiceTimes[i] < tempServiceTimes[i - 1] && (tempServiceTimes[i] < 100))
                 {
                   tempServiceTimes = sortArray(tempServiceTimes);
                 }
               }
               // set the next arrival time accordingly
               simulation.get(i).setNextArriveTime(tempServiceTimes[i]);
               // generate a random status (very rare is there a cancel or delay
               simulation.get(i).setStatus(randGenerateStatus());
               // reset any delays
               simulation.get(i).setDelay(0);
               // reset the message
               simulation.get(i).setMessage("");
               // we have changed this bus stop info so let the simulation know we have
               simulation.get(i).setChanged(true);
 
               // sort out any delays, if they affect any later, bus stops
               // they should be updated accordingly
               if(simulation.get(i).getStatus() == 2)
                 setDelay(simulation, i);
 
               // same as above goes for cancellation
               // but with cancellations, we get the next Service Time and put that 
               // as the next arrive Time (see method for details)
               if(simulation.get(i).getStatus() == 1)
                 setCancel(simulation, i, route, timetableKind);
 
               // Now we want to see if this new simulation object,
               // has any cancellations or delays before it
               // which may have an effect on this array
               // can not do that o
               if(i != 0)
               {
                 // if the service before was delayed
                 if((simulation.get(i).getServiceNumber() == simulation.get(i - 1).getServiceNumber()) &&
                    simulation.get(i - 1).getStatus() == 2)
                 {
                   // then set delay the delay to the delay of the previous one
                   simulation.get(i).setDelay(simulation.get(i - 1).getDelay());
                   // plus increase the next arrival time by the delay
                   simulation.get(i).setNextArriveTime(simulation.get(i).getNextArriveTime() + simulation.get(i - 1).getDelay());
                   // also set the status to be delayed
                   simulation.get(i).setStatus(2);
                 }
 
                 // if the service before was cancelled
                 if((simulation.get(i).getServiceNumber() == simulation.get(i - 1).getServiceNumber()) &&
                     simulation.get(i - 1).getStatus() == 1)
                 {
                   // set the cancellation time to be the current cancellation time
                   simulation.get(i).setCancel(simulation.get(i).getNextArriveTime());
                   // now get the service number of the previous one
                   int serviceNo = simulation.get(i - 1).getServiceNumber();
                   // set the service number to be that service number
                   simulation.get(i).setServiceNumber(serviceNo);
                   // get the serviceTimes for the next service
                   tempServiceTimes = TimetableInfo.getServiceTimes(route, timetableKind, simulation.get(i).getServiceNumber());
                   // set the next arrival time to the corresponding service time
                   simulation.get(i).setNextArriveTime(tempServiceTimes[i]);
                   // also set the status to be cancelled
                   simulation.get(i).setStatus(1);
                   // and set the message appropriatley
                   simulation.get(i).setMessage("Service " + serviceNo + " at "
                                      + simulation.get(i).getHours(simulation.get(i).getCurrentTime()) +
                                      + simulation.get(i).getMinutes(simulation.get(i).getCurrentTime()) + 
                                      " cancelled, next service is shown");
                 } // if
               } // if
                 
             } // if
               System.out.println("Next arriveTime " + simulation.get(i).getNextArriveTime() 
                                + " service " + simulation.get(i).getServiceNumber() + 
                                " timetableKind " + timetableKind);
           } // for
         } // if
         else
         {
           for(int i = 0; i < simulation.size(); i++)
           {
             simulation.get(i).setCurrentTime(currentTime);
             if(simulation.get(i).getNextArriveTime() <= currentTime)
             {
               // the bus has come to the stop, so the next bus at the stop will be the next service
               simulation.get(i).setServiceNumber(simulation.get(i).getServiceNumber() + 1);
               // now we need to get the service times for this stop
               tempServiceTimes = TimetableInfo.getServiceTimes(route, timetableKind, simulation.get(i).getServiceNumber());
               // set the next arrival time accordingly
               
               // some bugs in the database, so might need to sort Array
               if(i != 0)
               {
                 if(tempServiceTimes[i] < tempServiceTimes[i - 1])
                 {
                   tempServiceTimes = sortArray(tempServiceTimes);
                 }
               }
               
               simulation.get(i).setNextArriveTime(tempServiceTimes[i]);
               // generate a random status (very rare is there a cancel or delay
               simulation.get(i).setStatus(randGenerateStatus());
               // reset any delays
               simulation.get(i).setDelay(0);
               // reset the message
               simulation.get(i).setMessage("");
               // we have changed this bus stop info so let the simulation know we have
               simulation.get(i).setChanged(true);
               
 
               // sort out any delays, if they affect any later, bus stops
               // they should be updated accordingly
               if(simulation.get(i).getStatus() == 2)
                 setDelay(simulation, i);
 
               // same as above goes for cancellation
               // but with cancellations, we get the next Service Time and put that 
               // as the next arrive Time (see method for details)
               if(simulation.get(i).getStatus() == 1)
                 setCancel(simulation, i, route, timetableKind);
 
               // Now we want to see if this new simulation object,
               // has any cancellations or delays before it
               // which may have an effect on this array
               // can not do that o
               if(i != 0)
               {
                 // if the service before was delayed
                 if((simulation.get(i).getServiceNumber() == simulation.get(i - 1).getServiceNumber()) &&
                    simulation.get(i - 1).getStatus() == 2)
                 {
                   // then set delay the delay to the delay of the previous one
                   simulation.get(i).setDelay(simulation.get(i - 1).getDelay());
                   // plus increase the next arrival time by the delay
                   simulation.get(i).setNextArriveTime(simulation.get(i).getNextArriveTime() + simulation.get(i - 1).getDelay());
                   // also set the status to be delayed
                   simulation.get(i).setStatus(2);
                 }
 
                 // if the service before was cancelled
                 if((simulation.get(i).getServiceNumber() == simulation.get(i - 1).getServiceNumber()) &&
                     simulation.get(i - 1).getStatus() == 1)
                 {
                   // set the cancellation time to be the current cancellation time
                   simulation.get(i).setCancel(simulation.get(i).getNextArriveTime());
                   // now get the service number of the previous one
                   int serviceNo = simulation.get(i - 1).getServiceNumber();
                   // set the service number to be that service number
                   simulation.get(i).setServiceNumber(serviceNo);
                   // get the serviceTimes for the next service
                   tempServiceTimes = TimetableInfo.getServiceTimes(route, timetableKind, simulation.get(i).getServiceNumber());
                   // set the next arrival time to the corresponding service time
                   simulation.get(i).setNextArriveTime(tempServiceTimes[i]);
                   // also set the status to be cancelled
                   simulation.get(i).setStatus(1);
                   // and set the message appropriatley
                   simulation.get(i).setMessage("Service " + serviceNo + " at "
                                      + simulation.get(i).getHours(simulation.get(i).getCurrentTime()) +
                                      + simulation.get(i).getMinutes(simulation.get(i).getCurrentTime()) + 
                                      " cancelled, next service is shown");
                 } // if
               } //if
             }// if
             System.out.println("Next arriveTime " + simulation.get(i).getNextArriveTime() 
                                + " service " + simulation.get(i).getServiceNumber() + 
                                " timetableKind " + timetableKind);
           } // for
         } // else
     } // updateSim
     
     public static ArrayList<Simulation> initialiseArrayListForRoute(int route, GregorianCalendar date)
     {
         Date newDate = new Date((long)date.getTimeInMillis());
         
         TimetableInfo.timetableKind timetableKind = TimetableInfo.timetableKind(newDate);
         
         TimetableInfo.timetableKind[] timetableKinds = TimetableInfo.timetableKind.values();
         
         int[] busStops = BusStopInfo.getBusStops(route);
         
         ArrayList<Simulation> simArray = new ArrayList<Simulation>();
         
         // temp index for accessing service times
         int j = 0;
         
         // get the service times for the start of the day
         int[] serviceTimes = TimetableInfo.getServiceTimes(route, TimetableInfo.timetableKind(newDate), 0);
         switch(route)
         {
           //for bus route 383, the database has 11 stops 3 of which are not use
           // so we ignore them
           // in adition to this the service time for hotel depart is 
           // not on the database
           case 65:
             j = 0;
             for(int i = 0; i < busStops.length; i++)
             {
               //System.out.println(serviceTimes[i]);
               if(i != 1 && i != 4 && i != 8)
               {
                Simulation sim = new Simulation(busStops[i], serviceTimes[j], 0, serviceTimes[0], date, 0);
                j++;
                simArray.add(sim);
               }
             }
             break;
           // route 384 works all good because there is no discrepancies 
           case 66:
             for(int i = 0; i < busStops.length; i++)
             {
                Simulation sim = new Simulation(busStops[i], serviceTimes[i], 0, serviceTimes[0], date, 0);
                simArray.add(sim);
             }
             break;
           case 67:
             // get the times for the next service becasue there is only 4 stops on the first service
             int[] nextServiceTimes358Out = TimetableInfo.getServiceTimes(route, TimetableInfo.timetableKind(newDate), 1);
             System.out.println(nextServiceTimes358Out.length);
             // the first service does not stop at all buses for the 358 journeys
             // so we need to get the correct ones, we use a temp index for this
             j = 0;
             for(int i = 0; i < busStops.length; i++)
             {
                // ignore thornsett, Printers arms, and the database has some random extra stops
                // which we do not want
                if(i > 2 && i != 5)
                {
                  Simulation sim;
                  if(!((timetableKinds[1].equals(timetableKind) || (timetableKinds[2].equals(timetableKind))) && i == 7 ))
                  {
                     if(i < 8 )
                     {
                       sim = new Simulation(busStops[i], serviceTimes[j], 0, serviceTimes[0], date, 0);
                     }
                     else
                     {
                       sim = new Simulation(busStops[i], nextServiceTimes358Out[j], 0, serviceTimes[0], date, 1);
                     }
 
                     j++;
                     simArray.add(sim);
                  }
                }
             }
             break;
           case 68:
             // get the times for the next service becasue there is only 4 stops on the first service
             int[] nextServiceTimes358back = TimetableInfo.getServiceTimes(route, TimetableInfo.timetableKind(newDate), 1);
             // the first service does not stop at all buses for the 358 journeys
             // so we need to get the correct ones, we use a temp index for this
             j = 0;
            int k = 0;
             for(int i = 0; i < busStops.length; i++)
             {
               // ignore thornsett, Printers arms, and the database has some random extra stops
               // which we do not want
                if(i < 11 && i != 8)
                {
                   Simulation sim;
                   if(!((timetableKinds[1].equals(timetableKind) || (timetableKinds[2].equals(timetableKind))) && i == 6))
                   {
                   if(i > 6)
                    {
                     sim = new Simulation(busStops[k], serviceTimes[k], 0, serviceTimes[0], date, 0);
                     System.out.println("i" + i + ":" + serviceTimes[k] + BusStopInfo.getFullName(busStops[i]));
                     k++;
                    }
                    else
                    {
                     sim = new Simulation(busStops[j], nextServiceTimes358back[j], 0, serviceTimes[0], date, 1);
                      System.out.println("i" + i + ":" + nextServiceTimes358back[j] + BusStopInfo.getFullName(busStops[i]));
                    }
                    j++;
                    simArray.add(sim);
                  }
                } 
             }
             break;         
         }
         return simArray;
     }
     
     // 1 / 100 chance of a delay or cancellation
     // status' 0 = onTime, 1 = cancelled, 2 = delayed 
     public static int randGenerateStatus()
     {
       int status = (int)(100 * Math.random());
       if(status == 10
 )        return 1;
       if(status == 20)
         return 2;
       else
         return 0;
     }
     
     // called if the randomly generated status is '2'
     // generates a delay between 0 - 60
     public static int randGenerateDelay()
     {
       int delay = (int)(60 * Math.random());
       return delay; 
       
     }
     
     // if you want to set a delay for a bus stop
     // we need to apply it to all later bus stops (which have the same service)
     // this method does that
     public static void setDelay(ArrayList<Simulation> simulation, int index)
     {
       int delay = randGenerateDelay();
       int serviceNo = simulation.get(index).getServiceNumber();
       for(int j = index; j < simulation.size(); j++)
       {
         // set delay attribute = to delay
         // delay decrease each time because the bus speeds up
         // set the message to the correct message
         if(simulation.get(j).getServiceNumber() == serviceNo)
         {
           simulation.get(j).setMessage("Service " + serviceNo + " at "
                                        + simulation.get(j).getHours(simulation.get(j).getNextArriveTime()) +
                                        + simulation.get(j).getMinutes(simulation.get(j).getNextArriveTime()) + 
                                        " delayed by " + simulation.get(j).getDelay());
           simulation.get(j).setDelay(delay);
           simulation.get(j).setNextArriveTime(simulation.get(j).getNextArriveTime() + delay);
           delay--;
         }
       }
       
     }
     
     // we need to apply it to all later bus stops (which have the same service)
     // this method does that
     public static void setCancel(ArrayList<Simulation> simulation, int index, int route, TimetableInfo.timetableKind timetableKind)
     {
       // so the next service is the one we want
       int serviceNo = simulation.get(index).getServiceNumber() + 1;
       
       int[] serviceTimes = TimetableInfo.getServiceTimes(route, timetableKind, serviceNo);
       
       // update the nextArriveTime accordingly
       // set the message correctly
       // set the cancel time so can be printed out
       for(int j = index; j < simulation.size(); j++)
       {
         if(simulation.get(j).getServiceNumber() == serviceNo)
         {
           simulation.get(j).setCancel(simulation.get(j).getNextArriveTime());
           simulation.get(j).setNextArriveTime(serviceTimes[j]);
           simulation.get(j).setServiceNumber(serviceNo);
           simulation.get(j).setMessage("Service " + serviceNo + " at "
                                        + simulation.get(j).getHours(simulation.get(j).getNextArriveTime()) +
                                        + simulation.get(j).getMinutes(simulation.get(j).getNextArriveTime()) + 
                                        " cancelled, next service is shown");
         }
       }
       
     }
     
     // method that will sort an array into
     // some bugs in the database
     // if time goes past 1440 (midnight)
     // it is ordered wrongly!
     public static int[] sortArray(int[] arrayToSort)
     {
       Arrays.sort(arrayToSort);
       return arrayToSort;
       
     }
     
     public static void main(String[] args)
     {
         database.openBusDatabase();
         
        int route = 68;
         
        GregorianCalendar date = new GregorianCalendar(2013, 5, 23, 0, 0, 0);
         
         Date newDate = new Date(date.getTimeInMillis());
   
         // so initalise an array for a date and a route
         ArrayList<Simulation> simArray = initialiseArrayListForRoute(route, date);
         
         // print out all the bus stops in the array
         for(int i = 0; i < simArray.size(); i++)
         {
           System.out.println("bus Stop " + simArray.get(i).getBusStopID());
         }
         
         int k = 0;
         
         // while the last service has not gone
         // keep showing the time
         while(simArray.get(simArray.size() - 1).getServiceNumber() != TimetableInfo.getNumberOfServices(route, TimetableInfo.timetableKind(newDate)));
         {
           updateSim(simArray, 66);
           System.out.println("current time " + simArray.get(0).getCurrentTime());
           k++;
         }
     }
     
 }
