 package pls.stats;
 
 import java.util.Comparator;
 
 import org.apache.hadoop.fs.FileStatus;
 
 public class NumberFolderComparator implements Comparator<FileStatus> {
 	@Override
 	public int compare(FileStatus fs1, FileStatus fs2) {
 		return (int)Math.signum(getRunNumber(fs1) - getRunNumber(fs2));
 	}
 	
 	private long getRunNumber(FileStatus fs) {
 		String name = fs.getPath().getName();
		if (name.matches("\\d+")) {
 			return Long.parseLong(name);
 		} else {
 			return Long.MAX_VALUE;
 		}
 	}
 }
