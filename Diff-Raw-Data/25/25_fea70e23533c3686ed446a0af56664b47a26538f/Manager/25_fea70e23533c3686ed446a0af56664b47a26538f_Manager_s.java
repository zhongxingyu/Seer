 package uk.ac.cam.jas250.android_ftrace_manager;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 
 public class Manager {
 	
 	private static Manager manager = null;
 	
	private Boolean tracing;
 	private int pipe_reader_pid;
 	
 	public static Manager getManager(){
 		if(manager==null)
 			manager = new Manager();
 		return manager;
 	}
 	
 	private Manager(){
 		super();
 		getTracing();
 		pipe_reader_pid = -1;
 	}
 	
	public Boolean startTracing(String output_filename){
 		if(getTracing()) //Tracing has already started
 			return false;
 		else{
 			runCommandAsRoot("echo function > /sys/kernel/debug/tracing/current_tracer");
 			runCommandAsRoot("./data/ftrace_reader "+output_filename+" &");
 			pipe_reader_pid = getNativePipePid();
 			runCommandAsRoot("echo 1 > /sys/kernel/debug/tracing/tracing_on");
 			
 			char status = getFTraceStatus();
 			if(status=='1'){
 				setTracing(true);
 				return true;
 			}else
 				return false;
 		}
 	}
 	
	public Boolean stopTracing(){
 		if(!getTracing()) //Tracing isn't on
 			return false;
 		else{
 			runCommandAsRoot("echo 0 > /sys/kernel/debug/tracing/tracing_on");
 			if(pipe_reader_pid!=-1)
 				runCommandAsRoot("kill "+pipe_reader_pid);
 			return true;
 		}
 	}
 	
 	private void runCommandAsRoot(String command){
 		try {
 			Process p = Runtime.getRuntime().exec("su");
 			OutputStreamWriter osw = new OutputStreamWriter(p.getOutputStream());
 			osw.write(command+ "\n");
 			osw.flush();
 			osw.write("exit \n");
 			osw.flush();
 			p.waitFor();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private int getNativePipePid(){
 		try {
 			Process p = Runtime.getRuntime().exec("ps");
 			p.waitFor();
 			
 			BufferedReader reader = new BufferedReader(
 		            new InputStreamReader(p.getInputStream()));
 		    String line=null;
 		    while ((line = reader.readLine())!=null) {
 		        if(line.contains("ftrace_reader")){
 		        	String[] spl = line.split(" ");
 		        	for(int i=1;i<spl.length;i++){
 		        		String part = spl[i];
 		        		try{
 		        				int pid = Integer.parseInt(part);
 		        				return pid;
 		        		}catch(NumberFormatException e){
 		        			continue;
 		        		}
 		        	}
 		        }
 		    }
 		    reader.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 	
 	private char getFTraceStatus(){
 		FileReader f=null;
 		try {
 			f = new FileReader("/sys/kernel/debug/tracing/tracing_on");
 			char status = (char) f.read();
 			f.close();
 			return status;
 		} catch (FileNotFoundException e) {
 			log("Couldn't open the tracing_on file");
 			e.printStackTrace();
 		} catch (IOException e) {
 			log("Couldn't close the tracing_on file pointer");
 			e.printStackTrace();
 		}
 		return (char)-1;
 	}
 
	public Boolean getTracing() {
 		char status = getFTraceStatus();
 		if(status == '1')
 			setTracing(true);
 		else
 			setTracing(false);
 		return tracing;
 	}
 	
 
	private void setTracing(Boolean tracing_val) {
 		tracing = tracing_val;
 	}
 	
 	private void log(String message){
 		System.out.println("Android_FTrace_Manager: "+message);
 	}
 	
 	
 
 }
