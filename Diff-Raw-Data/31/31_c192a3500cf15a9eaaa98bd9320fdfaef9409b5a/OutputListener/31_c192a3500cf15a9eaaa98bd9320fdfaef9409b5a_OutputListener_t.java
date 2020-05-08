 package com.github.ashtonkem.Processes;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 public class OutputListener implements Runnable {
 	private Process process;
 
 	public OutputListener(Process p) {
 		process = p;
 	}
 
 	public void run() {
 
 		try {
 			BufferedReader output = new BufferedReader(new InputStreamReader(
 					process.getInputStream()));
 
 			String line;
 			while (true) {
 				line = output.readLine();
 				if (line != null)
 					System.out.println(line);
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
	
 }
