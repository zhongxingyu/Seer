 package nz.net.thoms.hadoophash;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.hadoop.io.WritableComparable;
 import org.apache.hadoop.mapred.InputFormat;
 import org.apache.hadoop.mapred.InputSplit;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.RecordReader;
 import org.apache.hadoop.mapred.Reporter;
 
 public class HashInput implements InputFormat<WritableComparable, HashValue> {
 	private static int POSTFIX_LENGTH = 4;
 
 	@Override
 	public RecordReader<WritableComparable, HashValue> getRecordReader(
 			InputSplit split, JobConf job, Reporter reporter) throws IOException {
 		return new HashRecordReader(job.get("hash"), ((HashInputSplit) split).getPrefixes(), ((HashInputSplit) split).getPasswordLength());
 	}
 
 	@Override
 	public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException {
 		numSplits = job.getInt("tasks", 100);
 		int password_length = job.getInt("password_length", 5);
 		int prefix_length =  password_length - POSTFIX_LENGTH;
		if  (prefix_length < 0) {
			prefix_length = 0;
 		}
		
 		ArrayList<HashInputSplit> splits = new ArrayList<HashInputSplit>(numSplits);
 		ArrayList<String> prefixes = Util.permute(Util.chars, prefix_length);
 		int prefixesPerSplit = prefixes.size() / numSplits;
 		if (prefixesPerSplit == 0) {
 			numSplits = 5;
 			prefixesPerSplit = prefixes.size() / numSplits;
 		}
 		if (!prefixes.isEmpty()) {
 			int lastPos = 0;
 			if (prefixesPerSplit > 0) {
 				for (int i = 0; i < numSplits; i++) {
 					splits.add(HashInputSplit.newSplit(new ArrayList<String>(prefixes.subList(lastPos, lastPos + prefixesPerSplit)), password_length));
 					lastPos = lastPos + prefixesPerSplit;
 				}
 			}
 			List<String> leftOvers = new ArrayList<String>(prefixes.subList(lastPos, prefixes.size()));
 			while (!leftOvers.isEmpty()) {
 				for (int i = 0; i < numSplits; i++) {
 					if (leftOvers.isEmpty()) {
 						break;
 					}
 					String leftover = leftOvers.remove(0);
 					splits.get(i).getPrefixes().add(leftover);
 				}
 			}
		} else {
			splits.add(HashInputSplit.newSplit(prefixes, password_length));
 		}
		
 		return splits.toArray(new InputSplit[splits.size()]);
 	}
 }
