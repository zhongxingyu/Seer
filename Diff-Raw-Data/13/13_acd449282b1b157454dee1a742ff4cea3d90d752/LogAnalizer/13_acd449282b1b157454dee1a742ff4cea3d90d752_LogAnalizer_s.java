 package ch.ethz.mlmq.log_analyzer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 public class LogAnalizer {
 
 	private ArrayList<File> files = new ArrayList<>();
 
 	public void addFile(File testLogFile) {
 		files.add(testLogFile);
 	}
 
 	/**
 	 * @param string
 	 * @param windowSize
 	 *            in milliseconds
 	 * @param startupCooldownTime
 	 * @return
 	 */
 	public ArrayList<Bucket> getBuckets(String messageType, long windowSize, long startupCooldownTime) {
 		ArrayList<Bucket> b = new ArrayList<>();
 		long startBucketPosition = getStartBucketTime() + startupCooldownTime;
 		long endBucketPosition = getEndBucketTime() - startupCooldownTime;
 
 		for (File file : files) {
 			try (BufferedReader din = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
 				din.readLine(); // skip header
 				processLines(messageType, windowSize, b, startBucketPosition, endBucketPosition, din);
 			} catch (IOException ex) {
 				throw new RuntimeException(ex);
 			}
 		}
 		return b;
 	}
 
 	private void processLines(String messageType, long windowSize, ArrayList<Bucket> b, long startBucketPosition, long endBucketPosition, BufferedReader din)
 			throws IOException {
 		while (true) {
 			String line = din.readLine();
 			if (line == null)
 				break;
 			processLine(messageType, b, line, startBucketPosition, endBucketPosition, windowSize);
 		}
 	}
 
 	private void processLine(String messageType, ArrayList<Bucket> b, String line, long startBucketPosition, long endBucketPosition, long windowSize) {
 		LogLine l = LogLineParser.parseLogLine(line);
 
 		if (l == null)
 			return;
 
 		long t = l.getTimestamp();
 		if (t < startBucketPosition || t > endBucketPosition)
 			return;
 
 		if (!l.getName().startsWith(messageType))
 			return;
 
 		int pos = l.getBucketPosition(startBucketPosition, windowSize);
 
 		while (b.size() <= pos)
			b.add(new Bucket());
 
 		Bucket bucket = b.get(pos);
 
 		bucket.addValue(l.getDuration());
 		bucket.addTimestamp(l.getTimestamp());
 	}
 
 	public long getStartBucketTime() {
 		long minStartBucketTime = Long.MAX_VALUE;
 		for (File file : files) {
 			HeaderInfo i = new HeaderInfo(file);
 			long x = i.getStartBucketTime();
 			if (minStartBucketTime > x) {
 				minStartBucketTime = x;
 			}
 		}
 		return minStartBucketTime;
 	}
 
 	public long getEndBucketTime() {
 		// TODO: make this implementation faster: seek to eof, read line from the end
 		long maxEndBucketTime = Long.MIN_VALUE;
 		for (File file : files) {
 			HeaderInfo i = new HeaderInfo(file);
 			long x = i.getEndBucketTime();
 			if (maxEndBucketTime < x) {
 				maxEndBucketTime = x;
 			}
 		}
 		return maxEndBucketTime;
 	}
 }
