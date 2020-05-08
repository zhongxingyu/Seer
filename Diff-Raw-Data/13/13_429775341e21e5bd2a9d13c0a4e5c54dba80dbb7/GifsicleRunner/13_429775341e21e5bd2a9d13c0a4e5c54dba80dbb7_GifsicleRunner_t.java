 package org.cloudsicle.slave;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.List;
 
 /**
  * Runs the Gifsicle executable for some inputs
  */
 public class GifsicleRunner {
 
 	private int delay = 10;
 	private boolean loop = true;
 
 	/**
 	 * Initialize the class, make sure that the executable exists
 	 */
 	public GifsicleRunner() throws MissingFileException {
 		File f = new File("gifsicle");
 		if (!f.exists())
 			throw new MissingFileException("Could not find file gifsicle");
 		if (!f.canExecute())
 			throw new MissingFileException(
 					"File gifsicle has wrong permissions");
 	}
 
 	/**
 	 * Set the delay in milliseconds for each gif frame
 	 */
 	public void setDelay(int millis) {
 		this.delay = millis;
 	}
 
 	/**
 	 * Set whether or not the resulting gif should loop
 	 */
 	public void setLoops(boolean looping) {
 		this.loop = looping;
 	}
 
 	/**
 	 * Build the raw command to execute gifsicle
 	 */
 	private String buildCommand(String[] files, String output) {
 		String result = "./gifsicle";
 		result += " --delay=" + delay;
 		if (loop)
 			result += " --loop";
 		for (String f : files) {
 			result += " " + f;
 		}
 		result += " > " + output;
 		System.out.println("executing: \n" + result);
 		return result;
 	}
 
 	/**
 	 * Run some command.
 	 * 
 	 * @return true if it succeeded
 	 */
 	private boolean runCommand(String cmd) {
 		try {
			String[] bashCmd = {
					"/bin/sh",
					"-c",
					cmd};
			Process p = Runtime.getRuntime().exec(bashCmd);
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					p.getInputStream()));
 			String line = reader.readLine();
 			while (line != null) {
 				System.out.println(line);
 				line = reader.readLine();
 			}
 			p.waitFor();
 			return true;
 		} catch (InterruptedException e) {
 			return false;
 		} catch (IOException e) {
 			return false;
 		}
 	}
 
 	/**
 	 * Convert a list of existing gif files to an output gif file.
 	 * 
 	 * @return null if conversion failed.
 	 */
 	public File convert(List<File> gifs, File output) {
 		String[] files = new String[gifs.size()];
 		for (int i = 0; i < gifs.size(); i++) {
 			File gif = gifs.get(i);
 			files[i] = gif.getAbsolutePath();
 		}
 		String cmd = buildCommand(files, output.getAbsolutePath());
 		if (runCommand(cmd))
 			return output;
 		else
 			return null;
 	}
 
 }
