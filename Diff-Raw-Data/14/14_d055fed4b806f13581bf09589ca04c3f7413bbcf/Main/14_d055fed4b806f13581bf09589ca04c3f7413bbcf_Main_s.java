 package com.austingulati.opsys.project1;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Queue;
 import java.util.PriorityQueue;
 import java.util.Comparator;
 import java.util.Random;
 
 public class Main
 {
     public static final Integer N = 20; // Number of processes
     public static final Integer CPU_COUNT = 2;
     public static final Integer CONTEXT_SWITCH = 15;
     public static Integer PART2 = 0;
 
     public static void main(String[] args)
     {
         // Check input
         if(args.length > 0 && args[0].equals("-PART2"))
         {
             PART2 = 1;
         }
 
         // Create ExponentialRandom object for random times
         ExponentialRandom expRand = new ExponentialRandom();
         // use via expRand.nextInt()
 
         // Create processes
         Queue<Process> allProcesses = new PriorityQueue<Process>(N, new Comparator<Process>() {
             public int compare(Process left, Process right) {
                 if(left.getTimeRequested() < right.getTimeRequested()) return -1;
                 else if(left.getTimeRequested() == right.getTimeRequested()) return 0;
                 return 1;
             }
         });
         Random random = new Random();
         for(Integer i = 0; i < N; ++i)
         {
             Integer time = random.nextInt(350) + 50;
            Integer priority = random.nextInt(4);
             Integer requestedAt = 0;
             if(i > N/10)
             {
                 requestedAt = expRand.nextInt();
             }
             Process process = new Process(i, time, priority, requestedAt);
             allProcesses.add(process);
         }
 
         // Create schedulers
         List<Scheduler> schedulers = new ArrayList<Scheduler>();
 
         // Add schedulers to the list
         schedulers.addAll(Arrays.asList(new FirstComeScheduler(), new ShortestJobFirstScheduler(), (new ShortestJobFirstScheduler()).enablePreemption(), new RoundRobinScheduler(), new PreemptivePriorityScheduler()));
 
         // Add processes to schedulers
         // for(Scheduler scheduler : schedulers)
         // {
         //     scheduler.addProcesses(processes);
         // }
 
         // Run schedulers
         for(Scheduler scheduler : schedulers)
         {
             Queue<Process> currentProcesses = new PriorityQueue<Process>(allProcesses);
             if(PART2 == 0)
             {
                 scheduler.addProcesses(currentProcesses);
             }
             else
             {
                 addProcessesRequestedNow(allProcesses, currentProcesses, scheduler, 0);
             }
             System.out.println("Using scheduler: " + scheduler.getName());
             while(scheduler.hasUnfinishedProcesses() || (PART2 == 1 && currentProcesses.size() > 0))
             {
                 scheduler.tick();
                 if(PART2 == 1)
                 {
                     addProcessesRequestedNow(allProcesses, currentProcesses, scheduler, scheduler.getTime());
                 }
                 // System.out.printf("[time %d]\n", scheduler.getTime());
                 // if(scheduler.getTime() > 800) break;
             }
             System.out.println();
 
             // Print final results
             scheduler.printResults();
 
             for(Process process : allProcesses)
             {
                 process.reset();
             }
             // try {
             // Thread.sleep(10000);
             // } catch(Exception e) {}
         }
 
         // Run system
        // System.out.println("   TIME\t|  FCFS\t|   SJF\t| SJF+P\t| RR\t");
        // while(schedulers.size() > 0)
        // {
        //     System.out.printf("%7d\t| ", schedulers.get(0).getTime());
        //     for(Scheduler scheduler : schedulers)
        //     {
        //         scheduler.tick();
        //         System.out.printf("%5d\t| ", scheduler.getCurrentProcess().getId());
        //     }
        //     System.out.println();
 //        }
     }
 
     public static void addProcessesRequestedNow(Queue<Process> allProcesses, Queue<Process> currentProcesses, Scheduler scheduler, Integer time)
     {
         Process nextProcess = currentProcesses.peek();
         if(nextProcess == null) return;
         while(nextProcess.getTimeRequested() <= time)
         {
             nextProcess = currentProcesses.remove();
             scheduler.addProcess(nextProcess);
             // allProcesses.add(nextProcess);
             // System.out.printf("Added process %d (requested at %d)\n", nextProcess.getId(), nextProcess.getTimeRequested());
             nextProcess = currentProcesses.peek();
             if(nextProcess == null) break;
             // System.out.printf("Maybe adding process %d (requested at %d)\n", nextProcess.getId(), nextProcess.getTimeRequested());
         }
     }
 }
