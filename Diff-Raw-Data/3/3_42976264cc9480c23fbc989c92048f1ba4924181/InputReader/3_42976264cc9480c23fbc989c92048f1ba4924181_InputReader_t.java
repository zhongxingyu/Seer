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
 		done=false;
 		ready="";
 	}
 	
 	public String getSubmitted(){
 		String store=getSetReady("");
 		return store;
 	}
 	
 	public void stop(){
 		done=true;
 		sc.close();
 		
 	}
 	
 	private synchronized String getSetReady(String newReady){
 		String store=ready;
 		ready=newReady;
 		return store;
 		
 	}
 	
 	public void run() {
 		try{
 			while (!done) {
 				if (sc.hasNextLine()) {
 					getSetReady(sc.nextLine());
 				} else {
 					try {
 						Thread.sleep(10);
 					} catch (InterruptedException e) {
 					}
 				}
 			}
 		} catch (IllegalStateException e) {}
		System.out.println("done");
 	}
 }
