 package scheduler;
 
 import java.util.*;
 
 import strategy.*;
 
 /**
  * main class
  * 
  * @assumption
  * 1. processes' ids are continuous, start from 0 to n.
  * 
  * @author Yaxing Chen(N16929794, yc1116@nyu.edu)
  *
  */
 public class Scheduler {
 	
 	public static void main(String[] args) {
 		ArrayList<Process> proc = null;
 		Status[] procStatus = null;		// record realtime procStatus, for improving efficiency.
 										// Status (enum)
 										//@see Status.java
 		String inputFile = "";
 		int strategy = -1;
 		int mode = 0;
 		
		args = new String[1];
		args[0] = "inp2.txt";
		
 		try {
 			if(args == null || args.length == 0) {
 				throw new Exception();
 			}
 			inputFile = args[0];
 			if(args.length >= 2) {
 				strategy = Integer.parseInt(args[1]);
 			}
 			if(args.length == 3) {
 				mode = Integer.parseInt(args[2]);
 			}
 			if(strategy > 2 || (strategy < 0 && strategy != -1)) {
 				throw new Exception();
 			}
 			if(mode != 0 && mode != 1) {
 				throw new Exception();
 			}
 		} catch(Exception e) {
 			invalidParam();
 		}
 		//System.out.println(Tool.read(args[0]));
 		
 		proc = Tool.fetch(Tool.read(inputFile));
 		procStatus = new Status[proc.size()];
 		for(int i = 0; i < procStatus.length; i ++) {
 			procStatus[i] = Status.NEW;
 		}
 		
 		if(strategy >= 0) {
 			exec(strategy, proc, procStatus, false);
 		}
 		else if(strategy == -1) {
 			/**
 			 * if perform multi-strategies once, need deep copy
 			 */
 			exec(0, proc, procStatus, true);
 			exec(1, proc, procStatus, true);
 			exec(2, proc, procStatus, true);
 		}
 	}
 	
 	private static void invalidParam() {
 		System.out.println("usage: Scheduler.jar <input file path> [strategy] [mode]");
 		System.out.println(" strategy: ");
 		System.out.println("           -1[default]: all");
 		System.out.println("           0: fcfs");
 		System.out.println("           1: rr");
 		System.out.println("           2: srjf");
 		
 		System.out.println(" mode:  (invalid for rr)");
 		System.out.println("           1[default]: preemptive");
 		System.out.println("           0: un-preemptive");
 		System.exit(0);
 	}
 	
 	private static void exec(int strategy, ArrayList<Process> proc, Status[] procStatus, boolean needDeepCopy) {
 		String outputFile = "";
 		Strategy ctrl = null;
 		switch(strategy) {
 		case 0:
 			ctrl = new FCFS();
 			outputFile = "output_fcfs.txt";
 			break;
 		case 1:
 			ctrl = new RR();
 			outputFile = "output_rr.txt";
 			break;
 		case 2:
 			ctrl = new SRJF();
 			outputFile = "output_srjf.txt";
 			break;
 		default:
 			break;
 		}
 		ctrl.start(proc, procStatus, needDeepCopy);
 		Tool.writeFile(outputFile);
 		return;
 	}
 }
