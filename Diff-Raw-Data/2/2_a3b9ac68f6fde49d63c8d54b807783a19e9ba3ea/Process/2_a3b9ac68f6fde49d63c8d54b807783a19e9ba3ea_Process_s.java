 package com.austingulati.opsys.project1;
 
 public class Process
 {
    private Integer id, priority, timeRemaining, timeTotal, timeInitiallyWaiting, timeWaiting = 0;
 
     public Process(Integer id, Integer timeRemaining, Integer priority)
     {
         this.id = id;
         this.timeRemaining = timeRemaining;
         this.timeTotal = timeRemaining;
         this.priority = priority;
     }
 
     public Process(Process rhs)
     {
         this.id = rhs.getId();
         this.timeRemaining = rhs.getTimeRemaining();
         this.timeTotal = timeRemaining;
         this.priority = rhs.getPriority();
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
 }
