 package Utilities;
 import java.util.*;
 import java.util.regex.Pattern;
 
 
 public class InputReader implements Runnable {
 	Scanner sc;
 	Pattern p;
 	String buffer;
 	String ready;
 	boolean done;
 	public InputReader(){
 		sc=new Scanner(System.in);
 		p=Pattern.compile(".");
 		done=false;
 		buffer="";
 		ready="";
 	}
 	
 	public String getInput(){
 		return buffer;
 	}
 	
 	public String getSubmitted(){
 		return ready;
 	}
 	
 	public void stop(){
 		done=true;
 	}
 	
 	public void run() {
 		while(!done){
 			if(sc.hasNext()){
 				String s=sc.next(p);
				buffer=buffer.concat(s);
 				if(s.equals("\n")){
 					ready=buffer;
 					buffer="";
 				}
 			}
 			else{
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {}
 			}
 		}
 	}
 }
