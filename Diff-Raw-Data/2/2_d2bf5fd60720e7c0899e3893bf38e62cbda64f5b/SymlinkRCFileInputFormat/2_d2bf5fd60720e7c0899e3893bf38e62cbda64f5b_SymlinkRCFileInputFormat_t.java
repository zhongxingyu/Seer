 package us.yuxin.hump.io;
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.ContentSummary;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hive.conf.HiveConf;
 import org.apache.hadoop.hive.ql.io.ContentSummaryInputFormat;
 import org.apache.hadoop.hive.ql.io.RCFileInputFormat;
 import org.apache.hadoop.hive.ql.io.RCFileRecordReader;
 import org.apache.hadoop.hive.ql.io.ReworkMapredInputFormat;
 import org.apache.hadoop.hive.ql.plan.MapredWork;
 import org.apache.hadoop.hive.ql.plan.PartitionDesc;
 import org.apache.hadoop.hive.serde2.columnar.BytesRefArrayWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.hadoop.mapred.FileSplit;
 import org.apache.hadoop.mapred.InputSplit;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.RecordReader;
 import org.apache.hadoop.mapred.Reporter;
 
 
 public class SymlinkRCFileInputFormat<K extends LongWritable, V extends BytesRefArrayWritable>
   extends RCFileInputFormat<K, V> implements ContentSummaryInputFormat, ReworkMapredInputFormat {
 
   long minSplitSize = SequenceFile.SYNC_INTERVAL;
   long blockSize = 1024 * 1024 * 64;
   final static double SPLIT_SLOP = 1.1;
 
   public final static String SYMLINK_FILE_SIGN_V1 = "SYMLINK.RCFILE.V1";
 
 
   public SymlinkRCFileInputFormat() {
     super();
   }
 
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public RecordReader<K, V> getRecordReader(InputSplit split, JobConf job,
 		Reporter reporter) throws IOException {
 
 		reporter.setStatus(split.toString());
 		// FileSplit s = (FileSplit)split;
 		// LOG.info("GETRR: " + s.getPath().toString() + ":" + s.getLength());
 		return new RCFileRecordReader(job, (FileSplit) split);
 	}
 
 
   @Override
   public boolean validateInput(FileSystem fs, HiveConf conf, ArrayList<FileStatus> files) throws IOException {
     return true;
   }
 
 
   @Override
   public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException {
     Path[] symlinksDirs = getInputPaths(job);
 
     if (symlinksDirs.length == 0) {
       throw new IOException("No input paths specified in job");
     }
 
     List<FileInfo> targetPaths = new ArrayList<FileInfo>();
     getTargetPathsFromSymlinksDirs(job, symlinksDirs, targetPaths);
 
     job.setLong("mapreduce.input.num.files", targetPaths.size());
     long totalSize = 0;
 
     for (FileInfo fi: targetPaths) {
       totalSize += fi.length;
     }
 
     long goalSize = totalSize / (numSplits == 0 ? 1: numSplits);
     long minSize = Math.max(job.getLong("mapred.min.split.size", 1), minSplitSize);
 
     ArrayList<FileSplit> splits = new ArrayList<FileSplit>(numSplits);
 
     for (FileInfo fi: targetPaths) {
       Path path = fi.path;
       long length = fi.length;
 
       if (length != 0 ) /*isSplitable is  always true */ {
         long splitSize = computeSplitSize(goalSize, minSize, blockSize);
         long bytesRemaining = length;
 
         while(((double)bytesRemaining) / splitSize > SPLIT_SLOP) {
           splits.add(new FileSplit(path, length - bytesRemaining, splitSize, new String[0]));
           bytesRemaining -= splitSize;
         }
 
         if (bytesRemaining != 0) {
           splits.add(new FileSplit(path, length - bytesRemaining, bytesRemaining, new String[0]));
         }
       }
     }
 
     LOG.debug("Total # of splits:" + splits.size());
     return splits.toArray(new FileSplit[splits.size()]);
   }
 
 
   private static void getTargetPathsFromSymlinksDirs(
     Configuration conf, Path[] symlinksDirs,
     List<FileInfo> targetPaths) throws IOException {
 
     for (Path symlinkDir: symlinksDirs) {
       FileSystem fileSystem = symlinkDir.getFileSystem(conf);
       FileStatus[] symlinks = fileSystem.listStatus(symlinkDir);
 
       for (FileStatus symlink: symlinks) {
         BufferedReader reader = new BufferedReader(
           new InputStreamReader(fileSystem.open(symlink.getPath())));
 
         String line;
         line = reader.readLine();
         if (line.equals(SYMLINK_FILE_SIGN_V1)) {
           while((line = reader.readLine()) != null) {
             int o1 = line.indexOf(',');
             // TODO error handle.
             FileInfo fi = new FileInfo();
             fi.length = Long.parseLong(line.substring(0, o1));
             int o2 = line.indexOf(',', o1 + 1);
             fi.row = Long.parseLong(line.substring(o1 + 1, o2));
             fi.path = new Path(line.substring(o2 + 1));
             fi.symlink = symlink.getPath();
 
             // Skip zero row RCFile.
             if (fi.row == 0) {
               continue;
             }
             targetPaths.add(fi);
           }
         }
         reader.close();
       }
     }
 
   }
 
   @Override
   public ContentSummary getContentSummary(Path p, JobConf job) throws IOException {
     long[] summary = {0, 0, 0};
     List<FileInfo> targetPaths = new ArrayList<FileInfo>();
     try {
       getTargetPathsFromSymlinksDirs(job, new Path[] {p}, targetPaths);
     } catch (Exception e) {
       throw new IOException("Error parsing symlinks from specified job input path.", e);
     }
 
     for (FileInfo fi: targetPaths) {
       summary[0] += fi.length;
       summary[1] += 1;
     }
 
     return new ContentSummary(summary[0], summary[1], summary[2]);
   }
 
   @Override
   public void rework(HiveConf job, MapredWork work) throws IOException {
     Map<String, PartitionDesc> pathToParts = work.getPathToPartitionInfo();
     List<String> toRemovePaths = new ArrayList<String>();
     Map<String, PartitionDesc> toAddPathToPart = new HashMap<String, PartitionDesc>();
     Map<String, ArrayList<String>> pathToAliases = work.getPathToAliases();
 
     for (Map.Entry<String, PartitionDesc> pathPartEntry: pathToParts.entrySet()) {
       String path = pathPartEntry.getKey();
       PartitionDesc partDesc = pathPartEntry.getValue();
 
       if (partDesc.getInputFileFormatClass().equals(SymlinkRCFileInputFormat.class)) {
         Path symlinkDir = new Path(path);
         FileSystem fileSystem = symlinkDir.getFileSystem(job);
         FileStatus fStatus = fileSystem.getFileStatus(symlinkDir);
 
         FileStatus symlinks[] = null;
         if (!fStatus.isDir()) {
           symlinks = new FileStatus[] {fStatus};
         } else {
           symlinks = fileSystem.listStatus(symlinkDir);
         }
 
         toRemovePaths.add(path);
         ArrayList<String> aliases = pathToAliases.remove(path);
         for (FileStatus symlink: symlinks) {
           BufferedReader reader = new BufferedReader(
             new InputStreamReader(fileSystem.open(symlink.getPath())));
           String line;
           line = reader.readLine();
           if (line.equals(SYMLINK_FILE_SIGN_V1)) {
             while((line = reader.readLine()) != null) {
               int o1 = line.indexOf(',');
               int o2 = line.indexOf(',', o1 + 1);
               long row = Long.parseLong(line.substring(o1 + 1, o2));
               String realPath = line.substring(o2 + 1);
               if (row == 0) {
                 continue;
               }
 
               toAddPathToPart.put(realPath, partDesc);
              pathToAliases.put(realPath, aliases);
             }
           }
           reader.close();
         }
       }
     }
 
     pathToParts.putAll(toAddPathToPart);
     for (String toRemove: toRemovePaths) {
       pathToParts.remove(toRemove);
     }
   }
 
 
   public static class FileInfo {
     public Path path;
     public Path symlink;
     public long length;
     public long row;
   }
 }
