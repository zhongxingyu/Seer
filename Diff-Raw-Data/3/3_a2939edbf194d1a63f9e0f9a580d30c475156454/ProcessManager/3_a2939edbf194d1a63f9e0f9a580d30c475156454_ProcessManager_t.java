 import java.rmi.*;
 import java.io.*;
 import java.util.*;
 
 public class ProcessManager
 {
     private HashMap<String, MigratableProcess> processMap;
     private HashMap<Thread, String> threadMap;
     
     private LinkedList<Thread> threads;
     	
     private Boolean checkingThreads; 
 
     public ProcessManager()
     {
         processMap = new HashMap<String, MigratableProcess>();
         threadMap = new HashMap<Thread, String>();
         
         threads = new LinkedList<Thread>();
         
         checkingThreads = false;
     }
     
     public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
 	for (Map.Entry<T, E> entry : map.entrySet()) {
 	    if (value.equals(entry.getValue())) {
 		return entry.getKey();
 	    }
 	}
 	return null;
     }
 
     public void setProcesses(String[] pids)
     {
         LinkedList<String> seen = new LinkedList<String>();
         for(int i = 0; i < pids.length; i++) {
             if(processMap.containsKey(pids[i])) 
                 seen.add(pids[i]);
             else
                 runProcess(pids[i]);
         }
         String[] pidArray = processMap.keySet().toArray(new String[0]);
 
         for(int i = 0; i < pidArray.length; i++) {
             if(!seen.contains(pidArray[i]))
             suspendProcess(pidArray[i]);
         }
         
         if (!checkingThreads) {
         	checkingThreads = true;
         	ThreadChecker thread = new ThreadChecker();
 	        thread.setDaemon(true);
 	        thread.start();
         }
     }
     
     public List<String> getProcesses() {
     	List<String> tmp = new ArrayList<String>();
    	tmp.addAll(processMap.keySet()); 	
    	return tmp;
     }
     
     public class ThreadChecker extends Thread {	
 		public void run() {
 	    	Thread t;
 	    	while(true) {
 	            try {
 	                Thread.sleep(10);
 	            } catch (Exception e) {
 	                e.printStackTrace();
 	            }
 	                
 	            for(int i = 0; i < threads.size(); i++) {
 	                try {
 	                    t = threads.get(i);
 	                    t.join(10);
 	                } catch(Exception e) {
 	                    e.printStackTrace();
 	                    continue;
 	                }
 	                if(!t.isAlive()) {
 	                    String filename = threadMap.get(t);
 	                    ProcessIO.delete(filename); 
 	                    processMap.remove(filename); 
 	                    threads.remove(t);
 	                    threadMap.remove(t);
 	                }
 	            } 
 	    	}
 	    }
 	}
     
     public void suspendProcess(String pid) {
         MigratableProcess p = processMap.remove(pid);
         if (p != null)
         {
             p.suspend();
             ProcessIO.writeProcess(p, pid.toString());
         }
     }
 
     // Requires a unique name for the process; otherwise replaces old process
     public void runProcess(String pid)
     {
         System.out.println("Running: " + pid);
         MigratableProcess process = ProcessIO.readProcess(pid);
         processMap.put(pid, process);
         //This must handle the timer TODO
         
         Thread thread = new Thread(process);
         threads.add(thread);
         threadMap.put(thread,pid);
         	
 	    System.out.println("Starting Thread");
         thread.start();
     }
 }
