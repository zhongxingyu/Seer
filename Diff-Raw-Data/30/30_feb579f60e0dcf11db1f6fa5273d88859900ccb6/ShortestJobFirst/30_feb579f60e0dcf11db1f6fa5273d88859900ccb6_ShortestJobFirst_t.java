 package org.processmanagement.scheduling;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import org.processmanagement.processes.Process;
 
 public class ShortestJobFirst extends Scheduler{
 
 	/**
 	 * Sorting class to sort queue by burst time
 	 */
 	private ArrayList<Process> sortQueue(ArrayList<Process> list){
 		
 		//java sort method with a comparator class to compare next burst times
 		Collections.sort(list, new Comparator<Process>() {
 			@Override
 			public int compare(Process process1, Process process2) {
 				 return process1.getCurBurst() - process2.getCurBurst();
 			}
 		});
 		return list;
 	}
 	
 	/**
 	 * Method that starts the shortest job first scheduler simulation.
 	 */			
 	public void start(){
 		for(Process p:pList){
 			for(@SuppressWarnings("unused") Integer b:p.getBurst()){
			totalBurst += b;
 			
 			}
 			readyQueue.add(p);
 		}
 		
 		printProcesses();
 		size = pList.size();
 		readyQueue = sortQueue(readyQueue);//sorts the processList
 		System.out.println("Running Processes...");
 		//step through the queue and simulate the burst time for each process	
 		sjf();
 		printStats();
 	}
 
 	public void sjf() {
 		int elapsedBurst = 0;
 		Process curProcess = new Process();
 		
 		while(!readyQueue.isEmpty()){
 			sortQueue(readyQueue);
 			
 			//get the first process in the queue that has arrived
 			int i = 0;
 			boolean found = false;
 			while(i<readyQueue.size()&&!found){
 				if(readyQueue.get(i).getArrivalTime()<=elapsedBurst){
 					curProcess = readyQueue.get(i);
 					found = true;
 				}
 				i++;
 			}
 			//end find first process in queue that has arrived
 
 			if(found){//there is a process to run
 				String n = interruptedBy(curProcess, elapsedBurst);
 				
 				if(n.equals(curProcess.getName())){//the segment
 					
 					if(curProcess.getBurst().size()==1){ //job is on it's last segment and will finish
 		
 						System.out.print(elapsedBurst + "---[" + curProcess.getName() + "]---");//print nat section
 						//calc the current wait time for the current burst section of the process
 						int curWait = curProcess.getWaitTime() + (elapsedBurst - curProcess.getArrivalTime());
 						curProcess.setWaitTime(curWait);//update total wait time for process
 						
 						elapsedBurst += curProcess.getCurBurst();
 					
 						curProcess.setCompletionTime(elapsedBurst - curProcess.getFArrivalTime());
 						curProcess.getBurst().remove(0);
 						readyQueue.remove(curProcess);//remove the process
 						
 						//update totals
 						totalWait += curProcess.getWaitTime();
 						totalComp += curProcess.getCompletionTime();
 						
 					}
 					else{//job will not finish within the current burst segment
 						System.out.print(elapsedBurst + "---[" + curProcess.getName() + "]---");//print nat
 						
 						int curWait = curProcess.getWaitTime() + (elapsedBurst - curProcess.getArrivalTime());//current segment wait
 						curProcess.setWaitTime(curWait); //set total process wait
 					
 						elapsedBurst += curProcess.getCurBurst(); 
 						//stores the burst time just processed before removing it
 						curProcess.getBurst().remove(0);
 						
						sendToIO(curProcess, elapsedBurst);//sent the process to the IOQueue
 					}
 				}
 				else{//process will be interrupted
 					int stopTime = interruptedAt(n);
 					System.out.print(elapsedBurst + "---[" + curProcess.getName() + "]---");//print nat
 					curProcess.decCurBurst(stopTime - elapsedBurst);
 					elapsedBurst = stopTime;
 					
 				}
 			}
 			else{
 				System.out.print(elapsedBurst + "---[IDLE]---");
 				elapsedBurst = findNextArrivalTime();
 			}
 			
 		}
 		System.out.print(elapsedBurst);
 	}
 	//this method simulates the process being put through IO
	public void sendToIO(Process curProcess, int elapsedBurst){
 		int delay = 0;
 		if(elapsedBurst<freeAt){
 			delay = freeAt - elapsedBurst;
 		}
 		curProcess.setArrivalTime(elapsedBurst+curProcess.getCurIoTime()+delay);
 		freeAt += curProcess.getCurIoTime();
 		curProcess.getIoTime().remove(0);
 	}
 	public int findNextArrivalTime(){
 		int nextTime = readyQueue.get(0).getArrivalTime();
 		for(Process p:readyQueue){
 			if(p.getArrivalTime()<nextTime)
 				nextTime = p.getArrivalTime();
 		}
 		return nextTime;
 	}
 	public String interruptedBy(Process curProcess, int elapsedBurst){
 		String result = curProcess.getName();
 		for(Process p:readyQueue){
 			if(p.getArrivalTime()<(elapsedBurst + curProcess.getCurBurst())&&p.getCurBurst()<curProcess.getCurBurst()){
 				result = p.getName();
 			}
 		}
 		return result;
 		
 	}
 	public int interruptedAt(String name){
 		for(Process p:readyQueue){
 			if(p.getName().equals(name)){
 				return p.getArrivalTime();
 			}
 		}
 		return 0;
 	}
 	
 }
