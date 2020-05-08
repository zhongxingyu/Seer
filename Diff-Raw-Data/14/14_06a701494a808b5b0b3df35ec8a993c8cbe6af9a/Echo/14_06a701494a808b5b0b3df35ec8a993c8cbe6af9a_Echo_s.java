 package job;
 
 import java.io.IOException;
 
 import os.Pipe;
 
 /**
  * job/Echo.java <br>
  * <br>
  * Echo the string(s) to standard output.
  * 
  * @author Jan Masopust
  * 
  * @version 0.5
  * 
  * @team <i>OutOfMemory</i> for KIV/OS 2013
  * @teamLeader Radek Petruška radekp25@students.zcu.cz
  * 
  */
 public class Echo extends Job {
 
 	/**
 	 * Classic constructor.
 	 * 
 	 * @param PID
 	 * @param stdERR
 	 */
 	public Echo(Integer PID, Pipe stdERR) {
 		super(PID, stdERR);
 	}
 
 	@Override
 	public String getManual() {
		// TODO I'll make it better don't worry.
 		return "echo - display a line of text";
 	}
 
 	@Override
 	protected void getJobDone() throws InterruptedException {
 		
 		if (this.isStdOutOk() == false){
 			this.pushError("Error: Nowhere to write!");
 			return;
 		}
 
 		if (this.arguments == null) {
 			this.noArgs();
 			return;
 		}
 
 		if (this.arguments.length == 0) {
 			this.noArgs();
 			return;
 		}
 		
 		StringBuilder text = new StringBuilder(100);
 
 		for (int i = 0; i < this.arguments.length; i++) {
 			/** Adds space for each argument except the last **/
 			text.append(this.arguments[i]);
 			text.append(" ");
 		}
 		
 		char [] data = text.toString().toCharArray();
 		
 		try {
 			this.pushData(data, 0, data.length);
 			
 		} catch (IOException e) {
 			this.pushError(e.getMessage());
 			
 		}
 		
 	}
 
 	/**
 	 * If this job had no argumetns.
 	 * @throws InterruptedException 
 	 */
 	private void noArgs() throws InterruptedException {
 		
 		if(this.isStdInOk() == false){
 			// nothing to write whatsoever.
 			return;
 		}
 		
 		char [] data = new char [BUF_SIZE];
 		int index = 1;
 
 		while(index > 0){
 			
 			try {
 				index = this.getData(data);
 				this.pushData(data, 0, index);
 
 			} catch (IOException e) {
 				this.pushError(e.getMessage());
 			}
 		}
 
 		return;
 	}
 }
