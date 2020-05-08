 package ch.ethz.mlmq.log_analizer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 public class HeaderInfo {
 
 	private final File file;
 	private String header;
 	private String firstLine;
 
 	public HeaderInfo(File file) {
 		this.file = file;
 	}
 
 	public long getStartBucketTime() {
 		try (BufferedReader din = new BufferedReader(new InputStreamReader(
 				new FileInputStream(file)))) {
 			this.header = din.readLine();
 			this.firstLine = din.readLine();
 		} catch (IOException ex) {
 			throw new RuntimeException(ex);
 		}
 		LogLine l = LogLineParser.parseLogLine(firstLine);
 		return l.getTimestamp();
 	}
 }
