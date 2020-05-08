 package ch.ethz.mlmq.log_analyzer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.RandomAccessFile;
 
 public class HeaderInfo {
 
 	private final File file;
 	@SuppressWarnings("unused")
 	private String header;
 	private String firstLine;
 	private String lastLine;
 
 	public HeaderInfo(File file) {
 		this.file = file;
 	}
 
 	public long getStartBucketTime() {
 		try (BufferedReader din = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
 			this.header = din.readLine();
 			this.firstLine = din.readLine();
 		} catch (IOException ex) {
 			throw new RuntimeException(ex);
 		}
 		LogLine l = LogLineParser.parseLogLine(firstLine);
 		return l == null ? Long.MAX_VALUE : l.getTimestamp();
 	}
 
 	public long getEndBucketTime() {
 		try (RandomAccessFile ramFile = new RandomAccessFile(file, "r")) {
 
 			// memo - assume a performance log line not to be longer than 256 bytes
 			byte[] buffer = new byte[256];
 			long pos = ramFile.length() - buffer.length;
 			if (pos > 0)
 				ramFile.seek(pos);
 
 			int numBytes = ramFile.read(buffer);
 
 			lastLine = new String(buffer, 0, numBytes);
 
 			int idx = lastLine.lastIndexOf("\n", lastLine.length() - 2);
 
 			lastLine = lastLine.substring(idx + 1, lastLine.length() - 1);
 
 			LogLine l = LogLineParser.parseLogLine(lastLine);
 			return l == null ? Long.MIN_VALUE : l.getTimestamp();
 
 		} catch (IOException ex) {
 			throw new RuntimeException(ex);
		} catch (Exception ex) {
			// fix java.lang.StringIndexOutOfBoundsException, maybe there are even more errors here...
			return Long.MIN_VALUE;
 		}
 	}
 }
