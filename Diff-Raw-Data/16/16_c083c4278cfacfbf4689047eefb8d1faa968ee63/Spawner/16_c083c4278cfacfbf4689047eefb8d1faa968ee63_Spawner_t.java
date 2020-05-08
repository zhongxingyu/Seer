 package process;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 public class Spawner
 {
 	private File workingDirectory;
 	
 	public Spawner() {
 		
 	}
 	
	/**
	 * Set the working directory of processes spawned.
	 * @param wd
	 */
 	public Spawner(String wd) {
 		workingDirectory = new File(wd);
 	}
 	
 	/**
 	 * Spawns the desired process and return the output as
 	 * a String.
 	 * @param command
 	 * @return
 	 */
	public String spawnProcess(String[] command) {
 		ProcessBuilder processBuilder = new ProcessBuilder(command);
 		processBuilder.directory(workingDirectory);
 		processBuilder.redirectErrorStream(true);
 		Process process;
 		try {
 			process = processBuilder.start();
 		}
 		catch(IOException e) {
 			return null;
 		}
 		
 		InputStream is = process.getInputStream();
 		InputStreamReader isr = new InputStreamReader(is);
 		BufferedReader br = new BufferedReader(isr);
 		
 		String line;
 		String output = "";
 		
 		try {
 			while ((line = br.readLine()) != null) {
 				output += line + "\n";
 			}
 		}
 		catch(Exception e) {
 			process.destroy();
 			e.printStackTrace();
 		}
 		
 		return output;
 	}
 }
