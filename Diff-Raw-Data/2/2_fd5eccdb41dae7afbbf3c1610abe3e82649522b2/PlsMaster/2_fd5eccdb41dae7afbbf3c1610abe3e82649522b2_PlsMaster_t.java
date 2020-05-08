 package pls;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.SequenceFileInputFormat;
 import org.apache.hadoop.mapred.SequenceFileOutputFormat;
 import org.apache.hadoop.mapred.TextInputFormat;
 import org.apache.hadoop.mapred.TextOutputFormat;
 import org.apache.log4j.Logger;
 
 import pls.stats.NumberFolderComparator;
 import pls.stats.PlsJobStats;
 import pls.tsp.Greedy;
 import pls.tsp.TspLsCity;
 import pls.tsp.TspSaSolution;
 import pls.tsp.TspUtils;
 import pls.vrp.LnsExtraData;
 
 public class PlsMaster {
 
 	private static final Logger LOG = Logger.getLogger(PlsMaster.class);
 	
 	private boolean runLocal;
 	
 	//for collecting stats
 	private BlockingQueue<Path> completedJobPathsQueue = new LinkedBlockingQueue<Path>();
 	//for telling the stats thread we're done
 	private static final Path DONE_PATH = new Path("/");
 	
 	public PlsMaster() {
 		
 	}
 	
 	public PlsMaster(boolean runLocal) {
 		this.runLocal = runLocal;
 	}
 	
 	public void run(int numRuns, List<PlsSolution> startSolutions, String dir, Class mapperClass,
 			Class reducerClass, PlsMetadata metadata, String problemName) throws IOException {
 		//write out start solutions to HDFS
 		Configuration conf = new Configuration();
 		
 		FileSystem fs = FileSystem.get(conf);
 		Path testDirPath = new Path(dir);
 		Path dirPath = nextDirPath(testDirPath, fs);
 		fs.delete(dirPath, true);
 		
 		//write out initial input file
 		Path initDirPath = new Path(dirPath, "0/");
 		if (!fs.mkdirs(initDirPath)) {
 			LOG.info("Failed to create directory: " + initDirPath);
 		}
 		
 		Path initFilePath = new Path(initDirPath, "part-00000");
 		
 		long firstFinishTime = System.currentTimeMillis() + metadata.getRoundTime();
 		//write out solutions
 		SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, initFilePath, BytesWritable.class, BytesWritable.class);
 		for (PlsSolution sol : startSolutions) {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			DataOutputStream dos = new DataOutputStream(baos);
 			sol.write(dos);
 			metadata.write(dos);
 			dos.close();
 			BytesWritable solWritable = new BytesWritable(baos.toByteArray());
 			writer.append(PlsUtil.getMapSolKey(firstFinishTime), solWritable);
 		}
 		writer.close();
 		//write out metadata
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		DataOutputStream dos = new DataOutputStream(baos);
 
 		//int k = Math.max(1, (int)Math.round(startSolutions.size() / 2 + .5));
 		LOG.info("k=" + metadata.getK());
 		
 		PlsJobStats stats = new PlsJobStats(metadata, problemName, startSolutions.size(), numRuns);
 		StatsThread statsThread = new StatsThread(stats, fs, conf, startSolutions.get(0).getClass());
 		statsThread.start();
 		
 		completedJobPathsQueue.offer(initDirPath);
 		stats.reportRoundTime(0);
 		//run the waves
 		for (int i = 0; i < numRuns; i++) {
 			Path inputPath = new Path(dirPath, i + "/");
 			Path outputPath = new Path(dirPath, (i+1) + "/");
 			long start = System.currentTimeMillis();
 			LOG.info("About to run job " + i);
 			runHadoopJob(inputPath, outputPath, startSolutions.size(), mapperClass, reducerClass);
 			completedJobPathsQueue.offer(outputPath);
 			long end = System.currentTimeMillis();
 			LOG.info("Took " + (end-start) + " ms");
 			stats.reportRoundTime((int)(end-start));
 		}
 		completedJobPathsQueue.offer(DONE_PATH);
 		try {
 			statsThread.join();
 		} catch (InterruptedException ex) {
 			LOG.error("Error waiting for stats thread to join", ex);
 		}
 		
 		writeStatsFile(dirPath, stats, fs);
 	}
 
 	/**
 	 * Involves sending a solution (or location of a solution) to each node.
 	 */
 	private void runHadoopJob(Path inputPath, Path outputPath, int numMaps, Class mapperClass, Class reducerClass)
 		throws IOException {
 		JobConf conf = new JobConf();
 
 		if (runLocal) {
 			conf.set("mapred.job.tracker", "local");
 		}
 		
 		conf.setOutputKeyClass(BytesWritable.class);
 		conf.setOutputValueClass(BytesWritable.class);
 		conf.setMapOutputKeyClass(BytesWritable.class);
 		conf.setMapOutputValueClass(BytesWritable.class);
 		
 		conf.setJar("tspls.jar");
 		
 		conf.setMapperClass(mapperClass);
 		conf.setReducerClass(reducerClass);
 		
 		conf.setSpeculativeExecution(false);
 		
 		conf.setInputFormat(SequenceFileInputFormat.class);
 		conf.setOutputFormat(SequenceFileOutputFormat.class);
 		
 		conf.setNumMapTasks(numMaps);
 		conf.setNumReduceTasks(1);
 		
 		FileInputFormat.addInputPath(conf, inputPath);
 		FileOutputFormat.setOutputPath(conf, outputPath);
 		
 		JobClient.runJob(conf);
 	}
 	
 	private void writeStatsFile(Path dir, PlsJobStats stats, FileSystem fs) throws IOException {
 		Path statsPath = new Path(dir, "jobstats.stats");
 		FSDataOutputStream os = fs.create(statsPath);
 		String report = stats.makeReport();
 		os.writeUTF(report);
 		os.close();
 	}
 	
 	private class StatsThread extends Thread {
 		private final PlsJobStats stats;
 		private final FileSystem fs;
 		private final Configuration conf;
 		private final Class solClass;
 		
 		public StatsThread(PlsJobStats stats, FileSystem fs, Configuration conf, Class solClass) {
 			this.stats = stats;
 			this.fs = fs;
 			this.conf = conf;
 			this.solClass = solClass;
 		}
 		
 		public void run() {
 			Path path;
 			try {
 				while ((path = completedJobPathsQueue.take()) != DONE_PATH) {
 					Path outFilePath = new Path(path, "part-00000");
 					SequenceFile.Reader reader = new SequenceFile.Reader(fs, outFilePath, conf);
 					processRoundOutput(reader);
 					reader.close();
 				}
 			} catch (InterruptedException ex) {
 				LOG.error("Interrupted", ex);
 			} catch (IOException ex) {
 				LOG.error("Failed to read output file", ex);
 			}
 		}
 		
 		private void processRoundOutput(SequenceFile.Reader reader) throws IOException {
 			BytesWritable key = new BytesWritable();
 			BytesWritable value = new BytesWritable();
 			PlsMetadata metadata = null;
 			WritableSolution bestSol = null;
 			LnsExtraData bestExtraData = null;
 			while (reader.next(key, value)) {
 				ByteArrayInputStream bais = new ByteArrayInputStream(value.getBytes(), 0, value.getLength());
 				DataInputStream dis = new DataInputStream(bais);
 				WritableSolution sol;
 				try {
 					sol = (WritableSolution)solClass.newInstance();
 				} catch (Exception ex) {
 					LOG.error("Trouble instantiating solution class", ex);
 					continue;
 				}
 				sol.readFields(dis);
 				metadata = new PlsMetadata();
 				metadata.readFields(dis);
 				
 				LnsExtraData extraData = null;
 				if (dis.available() > 0) {
 					extraData = new LnsExtraData();
 					extraData.readFields(dis);
 				}
 				
 				if (bestSol == null || sol.getCost() < bestSol.getCost()) {
 					bestSol = sol;
 					bestExtraData = extraData;
 				}
 			}
 			
 			stats.reportBestSolCost(bestSol.getCost());
 			stats.reportMetadata(metadata);
 			stats.reportBestExtraData(bestExtraData);
 		}
 	}
 	
 	private Path nextDirPath(Path dir, FileSystem fs) throws FileNotFoundException, IOException {
 		FileStatus[] statuses = fs.listStatus(dir);
 		if (statuses.length == 0) {
 			return new Path(dir, "0");
 		}
 		Arrays.sort(statuses, new NumberFolderComparator());
 		Path lastPath = statuses[statuses.length-1].getPath();
 		String name = lastPath.getName();
		int num = Integer.parseInt(name) + 1;
 		return new Path(dir, "" + num);
 	}
 
 }
