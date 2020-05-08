 /** NightShade */ /* Aug 14, 2006 */
 
 package bisbat;
 
 import java.io.*;
 
 public class RecieveGameOutput extends Thread {
 	/* Thread: constantly prints a given input stream */
 	
 	private BufferedReader reader;
 	private Bisbat bisbat;
 
 	public RecieveGameOutput (Bisbat bisbat, InputStreamReader i) {
		this.bisbat = bisbat;
 		reader = new BufferedReader(i);
 	}
 	
 	public void run(){
 		String line = "Starting PrintSteam";
 		String buffer = "";
 		while (line != null){
 			try {
 				
 				line = reader.readLine();
 				if(line == null) {
 					// Then we are done reading lines (output from game is closed.)
 					return;
 				} else if(!line.equals("")) {
 					buffer += line;
 					if(line.equals(bisbat.getPrompt())) {
 						//Handle the buffer then clear it.
 						
 						buffer = "";
 					}
 					System.out.println(line);
 					
 				}
 				
 			} catch (IOException e) {
 				System.err.println("PrintSteam failed");
 				e.printStackTrace();
 			}
 		}
 	}
 }
 
