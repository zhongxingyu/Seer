 package job;
 
 import java.io.IOException;
 
 import os.Init;
 import os.Pipe;
 
 /**
  * job/Ps.java <br>
  * <br>
  * Report a snapshot of the current processes.
  * 
  * @author Jan Masopust
  * 
  * @version 0.4
  * 
  * @team <i>OutOfMemory</i> for KIV/OS 2013
  * @teamLeader Radek Petruška radekp25@students.zcu.cz
  * 
  */
 public class Ps extends Job {
 	
 	private static final String ARG = "-d";
 
 	/**
 	 * Classic constructor.
 	 * 
 	 * @param PID
 	 * @param stdERR
 	 */
 	public Ps(Integer PID, Pipe stdERR) {
 		super(PID, stdERR);
 	}
 
 	@Override
 	public String getManual() {
 		return "ps - report a snapshot of the current processes\n";
 	}
 
 	@Override
 	protected void getJobDone() throws InterruptedException {
 		
 		boolean dead = this.checkParams();
 		
 		String str = "\n" + Init.getAllJobs(dead);
 		try {
 			pushData(str.toCharArray(), 0, str.length());
 		} catch (IOException e) {
 			pushError("Write error");
 		}
 	}
 
 	/**
 	 * Checks if the user wanted to write dead Jobs aswell.
 	 * 
 	 * @return TRUE if we should write dead Jobs too.
 	 */
 	private boolean checkParams() {
 		
 		if(this.arguments == null){
 			return (false);
 		}
 		
 		if(this.arguments.length < 1){
 			return (false);
 		}
 		
 		if(this.arguments[0].equalsIgnoreCase(ARG)){
 			return(true);
 		}
 			
 		return (false);
 	}
 }
