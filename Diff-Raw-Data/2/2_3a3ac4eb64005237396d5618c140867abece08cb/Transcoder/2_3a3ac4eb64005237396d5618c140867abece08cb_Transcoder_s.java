 package cubrikproject.tud.likelines.service.impl;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 /**
  * The Transcoder class converts downloaded videos into a specific format required by the motion analysis tool.
  * 
  * Basically, it is a wrapper for:
  * ffmpeg -y -i SRC -target pal-vcd DST
  * 
  * @author R. Vliegendhart
  */
 public class Transcoder {
 	/** path to the binary */
 	private final String ffmpegPath;
 
 	/**
 	 * Constructs a Transcoder object.
 	 * @param ffmpegPath Path to ffmpeg
 	 */
 	public Transcoder(String ffmpegPath) {
 		this.ffmpegPath = ffmpegPath;
 	}
 	
 	/**
 	 * Transcodes a file.
 	 * 
 	 * @param source The video to be transcoded
 	 * @param destination The location to store the transcoded video
 	 * @return A Process handle
 	 * @throws IOException
 	 */
 	public Process transcode(String source, String destination) throws IOException {
 		final ProcessBuilder pb = new ProcessBuilder(ffmpegPath, "-y", "-i", source, "-target", "pal-vcd", destination);
 		return pb.start();
 	}
 	
 	/**
 	 * Transcodes a file (blocking).
 	 * 
 	 * @param source The video to be transcoded
 	 * @param destination The location to store the transcoded video
	 * @return A Process handle
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public int transcodeAndWait(String source, String destination) throws IOException, InterruptedException {
 		final Process proc = transcode(source, destination);
 		BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
 		while (br.readLine() != null);
 		return proc.waitFor();
 	}
 }
