 package com.eolwral.osmonitor;
 
 import java.util.Map;
 
 public class ProcList {
 	// Options controlling how collection is done.
 	private static int Filter;
 	private static int Algorithm;
 	private static int Sort;
 	private static int Order;
 
 	// Constants for some of the options.
 	public static final int doSortPID = 1; 
 	public static final int doSortLoad = 2; 
 	public static final int doSortMem = 3; 
 	public static final int doSortThreads = 4; 
 	public static final int doSortName = 5;
 
 	public static final int doOrderASC = 0; 
 	public static final int doOrderDESC = 1; 
 
 	// Option setters.
	private static void SetProcessFilter(int Filter) {
 		ProcList.Filter = Filter;
 	}
 
	private static void SetProcessAlgorithm(int Algorithm) {
 		ProcList.Algorithm = Algorithm;
 	}
 
	private static void SetProcessSort(int Sort) {
 		ProcList.Sort = Sort;
 	}
 
	private static void SetProcessOrder(int Order) {
 		ProcList.Order = Order;
 	}
 
 	// Collect a snapshot of processes. The returned object is immutable
 	// outside this class.
 	public static ProcList Collect() {
 		return new ProcList();
 	}
 
 	// Information about a single process.
 	private static class Proc {
 		public int uid;
 		public int load;
 		public long utime;
 		public long stime;
 		public String time;
 		public int threads;
 		public long rss;
 		public long nice;
 		public String name;
 		public String owner;
 		public String status;
 	};
 
 	// A process list snapshot is a mapping of {position -> PID} and a mapping
 	// of {PID -> process info}.
	private Map<Integer, Integer> pid;
 	private Map<Integer, Proc> proc;
 	
 	// Accessors for the {position -> PID} mapping.
 	public int GetProcessPID(int position) {
		return pid.get(position);
 	}
 	
 	// Accessors for the {PID -> process info} mapping.
     public int GetProcessUID(int pid) {
     	return proc.get(pid).uid;
     }
 
     public int GetProcessLoad(int pid) {
     	return proc.get(pid).load;
     }
 
     public long GetProcessUTime(int pid) {
     	return proc.get(pid).utime;
     }
 
     public long GetProcessSTime(int pid) {
     	return proc.get(pid).stime;
     }
 
     public String GetProcessTime(int pid) {
     	return proc.get(pid).time;
     }
 
     public int GetProcessThreads(int pid) {
     	return proc.get(pid).threads;
     }
 
     public long GetProcessRSS(int pid) {
     	return proc.get(pid).rss;
     }
 
     public long GetProcessNice(int pid) {
     	return proc.get(pid).nice;
     }
 
     public String GetProcessName(int pid) {
     	return proc.get(pid).name;
     }
 
     public String GetProcessOwner(int pid) {
     	return proc.get(pid).owner;
     }
 
     public String GetProcessStatus(int pid) {
     	return proc.get(pid).status;
     }
 }
