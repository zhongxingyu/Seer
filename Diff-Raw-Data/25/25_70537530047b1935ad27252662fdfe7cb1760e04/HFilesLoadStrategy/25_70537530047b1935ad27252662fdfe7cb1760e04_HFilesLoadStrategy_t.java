 package com.nearinfinity.bulkloader;
 
 import com.google.common.collect.Lists;
 import com.nearinfinity.hbaseclient.HBaseClient;
 import com.nearinfinity.hbaseclient.Index;
 import com.nearinfinity.hbaseclient.TableInfo;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.*;
 import org.apache.hadoop.hbase.client.*;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.io.hfile.CacheConfig;
 import org.apache.hadoop.hbase.io.hfile.HFile;
 import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat;
 import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;
 import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.List;
 
 import static java.lang.String.format;
 
 public class HFilesLoadStrategy implements LoadStrategy {
     private static final Log LOG = LogFactory.getLog(HFilesLoadStrategy.class);
 
     private final Configuration conf;
     private final Path outputDir, inputDir;
     private final String hb_table;
     private final HTable table;
 
     public HFilesLoadStrategy(Configuration conf) throws IOException {
         this.conf = conf;
         outputDir = new Path(conf.get("output_path"));
         inputDir = new Path(conf.get("input_path"));
         hb_table = conf.get("hb_table");
         table = new HTable(conf, hb_table);
     }
 
     @Override
     public void load() throws Exception {
         HBaseAdmin admin = new HBaseAdmin(conf);
 
         HBaseAdminColumn.addDummyFamily(admin);
 
         createSamplingJob();
 
         createSplits(admin);
 
         Job job = createBulkLoadJob();
 
         deleteDummyData();
 
         loadHFileStartKeys();
 
         createSplits(admin);
 
         loadHFiles(job);
 
         deleteDummyData();
 
         HBaseAdminColumn.deleteDummyFamily(admin);
     }
 
     private void loadHFileStartKeys() throws IOException {
         List<Path> hfilePaths = new LinkedList<Path>();
         FileSystem fs = outputDir.getFileSystem(conf);
         FileStatus[] familyDirStatuses = fs.listStatus(outputDir);
         for (FileStatus stat : familyDirStatuses) {
             if (!stat.isDir()) {
                 LOG.warn("Skipping non-directory " + stat.getPath());
                 continue;
             }
             Path familyDir = stat.getPath();
             // Skip _logs, etc
             if (familyDir.getName().startsWith("_")) continue;
             Path[] hfiles = FileUtil.stat2Paths(fs.listStatus(familyDir));
             for (Path hfile : hfiles) {
                 if (hfile.getName().startsWith("_")) continue;
                 hfilePaths.add(hfile);
             }
         }
 
         List<byte[]> rowKeys = new LinkedList<byte[]>();
         for (Path hfile : hfilePaths) {
             HFile.Reader reader = HFile.createReader(fs, hfile, new CacheConfig(conf));
             try {
                 reader.loadFileInfo();
                 rowKeys.add(reader.getFirstRowKey());
             } finally {
                 reader.close();
             }
         }
 
         List<Put> putList = new LinkedList<Put>();
         for (byte[] rowKey : rowKeys) {
             Put put = new Put(rowKey).add(SamplingReducer.DUMMY_FAMILY, "spacer".getBytes(), new byte[0]);
             putList.add(put);
         }
 
         table.setAutoFlush(false);
         table.put(putList);
         table.flushCommits();
     }
 
     private void createSamplingJob() throws IOException, ClassNotFoundException, InterruptedException {
         LOG.info("Setting up the sampling job");
 
         int columnCount = conf.getInt(SamplingPartitioner.COLUMN_COUNT, 3);
         Job job = new Job(conf, "Sample data in " + inputDir.toString());
         job.setJarByClass(SamplingMapper.class);
 
         FileInputFormat.setInputPaths(job, inputDir);
         job.setInputFormatClass(TextInputFormat.class);
 
         job.setMapperClass(SamplingMapper.class);
 
         job.setMapOutputKeyClass(ImmutableBytesWritable.class);
         job.setMapOutputValueClass(Put.class);
         FileOutputFormat.setOutputPath(job, outputDir);
         job.setNumReduceTasks(2 * columnCount + 1);
         TableMapReduceUtil.initTableReducerJob(hb_table, SamplingReducer.class, job, SamplingPartitioner.class);
         printQuorum(job);
        LOG.info(String.format("Strategy Class: %s", HFilesLoadStrategy.class.getName()));
         job.waitForCompletion(true);
         deletePath(outputDir);
     }
 
     private Job createBulkLoadJob() throws Exception {
         Job job = new Job(conf, "Import from file " + inputDir.toString() + " into table " + hb_table);
 
         job.setJarByClass(BulkLoader.class);
         FileInputFormat.setInputPaths(job, inputDir);
         job.setInputFormatClass(TextInputFormat.class);
 
         job.setMapperClass(BulkLoaderMapper.class);
         job.setMapOutputKeyClass(ImmutableBytesWritable.class);
         job.setMapOutputValueClass(Put.class);
 
         FileOutputFormat.setOutputPath(job, outputDir);
 
         HFileOutputFormat.configureIncrementalLoad(job, table);
 
         printQuorum(job);
        LOG.info(String.format("Strategy Class: %s", HFilesLoadStrategy.class.getName()));
         if (!job.waitForCompletion(true)) {
             LOG.fatal("*** Bulk load map reduce job failed during run. Not loading data into HBase. ***");
             System.exit(1);
         }
 
         LOG.info("Bulk load job completed.");
         return job;
     }
 
     private void loadHFiles(Job job) throws Exception {
         LoadIncrementalHFiles fileLoader = new LoadIncrementalHFiles(conf);
         fileLoader.doBulkLoad(outputDir, table);
         long count = job.getCounters().findCounter(BulkLoader.Counters.ROWS).getValue();
         HBaseClient client = new HBaseClient(hb_table, conf.get("zk_quorum"));
         client.incrementRowCount(conf.get("sql_table_name"), count);
         deletePath(outputDir);
     }
 
     private void deleteDummyData() throws IOException {
         ResultScanner splitScanner = table.getScanner("dummy".getBytes());
         List<Delete> deleteList = new LinkedList<Delete>();
         for (Result result : splitScanner) {
             deleteList.add(new Delete(result.getRow()));
         }
 
         table.delete(deleteList);
         LOG.info("Successfully deleted dummy data.");
     }
 
     private void createSplits(HBaseAdmin admin) throws IOException {
         ResultScanner splitScanner = table.getScanner("dummy".getBytes());
         List<byte[]> splitPoints = Lists.newArrayList();
         List<byte[]> retrySplits = Lists.newArrayList();
         LOG.info("Retrieving the split points from HBase. (This may take a while.)");
         for (Result result : splitScanner) {
             splitPoints.add(result.getRow());
         }
 
         int size = splitPoints.size();
         int maxSize = Math.min(conf.getInt("max.splits", size), size);
         LOG.info(format("***Total splits %d ***", size));
         for (int i = 0; i < maxSize; i++) {
             byte[] result = splitPoints.get(i);
             try {
                 LOG.info(format("Attempting split %d of %d", i, size));
                 admin.split(hb_table.getBytes(), result);
             } catch (Exception e) {
                 retrySplits.add(result);
                 LOG.warn("Split " + Bytes.toStringBinary(result) + " threw exception");
             }
         }
         if (!retrySplits.isEmpty()) {
             retryFailedSplits(admin, retrySplits);
         }
     }
 
     private void retryFailedSplits(HBaseAdmin admin, List<byte[]> retrySplits) {
         int retryCount = 3;
         for (int x = 0; x < retrySplits.size(); x++) {
             try {
                 LOG.info("Retrying split " + Bytes.toStringBinary(retrySplits.get(x)));
                 admin.split(hb_table.getBytes(), retrySplits.get(x));
             } catch (Exception e) {
                 retryCount--;
                 if (retryCount == 0) {
                     LOG.info(format("Skipping %s too many failed retries.", Bytes.toStringBinary(retrySplits.get(x))));
                     retryCount = 3;
                 } else {
                     x--;
                     try {
                         Thread.sleep(2000);
                     } catch (InterruptedException ignored) {
                     }
                 }
             }
         }
     }
 
     private void deletePath(Path path) throws IOException {
         FileSystem fileSystem = FileSystem.get(conf);
         fileSystem.delete(path, true);
         fileSystem.close();
     }
 
     private static void printQuorum(Job job) {
         LOG.info(format("*** Using quorum %s ***", job.getConfiguration().get("hbase.zookeeper.quorum")));
     }
 }
