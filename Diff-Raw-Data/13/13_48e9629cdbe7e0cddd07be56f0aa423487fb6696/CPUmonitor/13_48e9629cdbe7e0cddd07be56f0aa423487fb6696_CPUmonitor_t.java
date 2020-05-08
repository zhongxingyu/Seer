 import java.util.ArrayList;
 import java.util.*;
 
 public class CPUmonitor {
 	
	private static ArrayList<Integer> CPUs = new ArrayList<Integer>();		
 	private int numberOfCPUs;
 
 	public CPUmonitor(int numberOfCPUs) {
 		this.numberOfCPUs = numberOfCPUs;
 	}
 
 	public synchronized void startCPUuse(int pid) {
 		while (CPUs.size() == numberOfCPUs) try { wait(); }catch(Exception e) {};
 		CPUs.add(pid);
 	}
 
 	public synchronized void endCPUuse(Integer pid) {
 		CPUs.remove(pid);
 		notifyAll(); 		
 	}
 
	public int getPIDindex(Integer pid){
		return CPUs.indexOf(pid);
	}

 }
