 package net.mms_projects.tostream;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class FfmpegWrapper extends Thread {
 
     private static final Pattern FRAME_PATTERN = Pattern.compile(
         "frame(\\s*)=(\\s+)(\\d+)", Pattern.CASE_INSENSITIVE
     );
     private static final Pattern FRAME_RATE_PATTERN = Pattern.compile(
         "fps(\\s*)=(\\s+)(\\d+)", Pattern.CASE_INSENSITIVE
     );
 
 	List<EncoderOutputListener> listeners = new ArrayList<EncoderOutputListener>();
 	Process process;
 	InputStream input;
 	BufferedReader reader;
 
 	public void addListener(EncoderOutputListener toAdd) {
 		listeners.add(toAdd);
 	}
 
 	public void startEncoder()
 	{
		ProcessBuilder builder = new ProcessBuilder("avconv", "-y", "-f", "x11grab", "-r", "60", "-i", ":0.0", "bla.flv");
 		builder.redirectErrorStream(true);
 		try {
 			process = builder.start();
 			input = process.getInputStream();
 			reader = new BufferedReader(new InputStreamReader(input));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void stopEncoder()
 	{
		input  = null;
		reader = null;
 		process.destroy();
 	}
 
 	@Override
 	public void run() {
 		try {
 			while (true) {
 				String line;
				if (reader != null) {
					try {
 						while ((line = reader.readLine()) != null) {
 							Matcher matcher;
 							
 							int framerate = 0;
 							int frame     = 0;
 							matcher = FRAME_RATE_PATTERN.matcher(line);
 							if (matcher.find()) {
 								framerate = Integer.parseInt(matcher.group(3));
 							}
 							matcher = FRAME_PATTERN.matcher(line);
 							if (matcher.find()) {
 								frame = Integer.parseInt(matcher.group(3));
 							}
 							for (EncoderOutputListener listener : listeners) {
 								listener.onStatusUpdate(frame, framerate);
 								listener.onOutput(line + "\n");
 							}
 						}
					} catch (Exception e) {
						System.out.println("Error: '" + e.getClass() + "' thrown in the FfmpegWrapper.");
 					}
 				}
 				Thread.sleep(500);
 			}
 		} catch (InterruptedException e) {
 			System.out.println("Child interrupted.");
 		}
 		System.out.println("Exiting child thread.");
 	}
 
 }
