 package com.austingulati.opsys.project1;
 
 public class Process
 {
     private Integer id, priority, timeRemaining, timeTotal = 0, timeInitiallyWaiting = 0, timeWaiting = 0, timeRequested;
 
     public Process(Integer id, Integer timeRemaining, Integer priority)
     {
         this.id = id;
         this.timeRemaining = timeRemaining;
         this.timeTotal = timeRemaining;
         this.priority = priority;
     }
 
     public Process(Integer id, Integer timeRemaining, Integer priority, Integer timeRequested)
     {
         this.id = id;
         this.timeRemaining = timeRemaining;
         this.timeTotal = timeRemaining;
         this.priority = priority;
         this.timeRequested = timeRequested;
     }
 
     public Process(Process rhs)
     {
         this.id = rhs.getId();
         this.timeRemaining = rhs.getTimeRemaining();
         this.timeTotal = rhs.getTimeTotal();
         this.timeInitiallyWaiting = rhs.getTimeInitiallyWaiting();
         this.timeWaiting = rhs.getTimeWaiting();
         this.priority = rhs.getPriority();
         this.timeRequested = rhs.getTimeRequested();
     }
 
     public Integer getTimeRemaining()
     {
         return timeRemaining;
     }
 
     public Integer getTimeWaiting()
     {
         return timeWaiting;
     }
 
     public Integer getTimeInitiallyWaiting()
     {
         return timeInitiallyWaiting;
     }
 
     public Integer getTimeTotal()
     {
         return timeTotal;
     }
 
     public Integer getId()
     {
         return id;
     }
 
     public Integer getPriority()
     {
         return this.priority;
     }
 
     public Integer getTimeRequested()
     {
         return this.timeRequested;
     }
 
     public void run()
     {
         if(timeRemaining == timeTotal)
         {
             // The process just started
             timeInitiallyWaiting = timeWaiting;
         }
         timeRemaining--;
     }
 
     public void pause()
     {
         timeWaiting++;
     }
 
     public void reset()
     {
         timeRemaining = timeTotal;
         timeWaiting = 0;
         timeInitiallyWaiting = 0;
     }
 }
