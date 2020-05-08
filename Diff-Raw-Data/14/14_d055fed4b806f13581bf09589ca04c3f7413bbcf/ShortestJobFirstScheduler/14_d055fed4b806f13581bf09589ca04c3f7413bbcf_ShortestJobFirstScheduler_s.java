 package com.austingulati.opsys.project1;
 
 class ShortestJobFirstScheduler extends Scheduler
 {
     private Boolean preemption = false;
 
     public String getName()
     {
         return "Shortest Job First (SJF) with" + (preemption ? " preemption" : "out preemption");
     }
 
     public ShortestJobFirstScheduler enablePreemption()
     {
         preemption = true;
         return this;
     }
 
     public ShortestJobFirstScheduler disablePreemption()
     {
         preemption = false;
         return this;
     }
 
     public Process getNextProcess(Integer processor)
     {
         Process currentProcess = runningProcesses.get(processor);
         if(currentProcess != null && preemption == false)
         {
             return currentProcess;
         }
         if(waitingProcesses.size() == 0)
         {
             return null;
         }
 
         Integer shortestProcess = 0;
         Integer shortestTime = waitingProcesses.get(0).getTimeRemaining();
         for(Integer i = 0; i < waitingProcesses.size(); ++i)
         {
             Process process = waitingProcesses.get(i);
             if(process.getTimeRemaining() < shortestTime)
             {
                 shortestTime = process.getTimeRemaining();
                 shortestProcess = i;
             }
         }
         if(currentProcess != null && preemption)
         {
             if(shortestTime < currentProcess.getTimeRemaining())
             {
                 addProcess(currentProcess);
                 runningProcesses.set(processor, null);
             }
         }
         return waitingProcesses.remove((int) shortestProcess);
     }
 }
