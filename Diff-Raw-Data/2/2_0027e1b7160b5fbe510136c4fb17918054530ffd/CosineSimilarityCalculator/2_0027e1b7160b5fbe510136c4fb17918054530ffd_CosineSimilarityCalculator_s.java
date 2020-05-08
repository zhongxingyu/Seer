 package org.acaro.cosine_similarity;
 
 import java.io.IOException;
 import java.nio.BufferUnderflowException;
 import java.nio.ByteBuffer;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.hadoop.io.SequenceFile.Reader;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.hadoop.mapreduce.lib.map.MultithreadedMapper;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 
 public class CosineSimilarityCalculator 
 	extends Mapper<Text, ByteArrayWritable, NullWritable, Text> {
 
 	private static final String MODELSDIR  = "cosineSimilarity.modelsDir";
 	private static final long MEGABYTES    = 1024*1024;
 	private List<NamedObject<List<NamedObject<byte[]>>>> files =
 			new LinkedList<NamedObject<List<NamedObject<byte[]>>>>();
 	
 	@Override
 	public void map(Text key, ByteArrayWritable value, Context context) 
 		throws IOException, InterruptedException {
 
 		ByteBuffer valueBB = ByteBuffer.wrap(value.getBytes());
 		
 		for (NamedObject<List<NamedObject<byte[]>>> file: files) {
 			
 			String filename                   = file.getName();
 			List<NamedObject<byte[]>> vectors = file.getData();
 			for (NamedObject<byte[]> vector: vectors) {
 
 				ByteBuffer entryBB = ByteBuffer.wrap(vector.getData());
 				valueBB.clear();
 				double cosine = calculateCosineSimilarity(entryBB, valueBB);
 
 				StringBuilder sb = new StringBuilder();
 				sb.append(filename)
 				  .append(": ")
 				  .append(key.toString())
 				  .append("->")
 				  .append(vector.getName())
 				  .append(" ")
 				  .append(cosine);
 				
 				// TODO: output like Eva's script
 				// print an+'\t'+row+'\t'+'%.5f'%(c)
 				context.write(NullWritable.get(), new Text(sb.toString()));
 			}			
 		}
 	}
 
 	private double calculateCosineSimilarity(ByteBuffer thisVector, ByteBuffer thatVector) {
 		double thisNorm = thisVector.getDouble();
 		double thatNorm = thatVector.getDouble();
 		float totalSum  = 0;
 
 		// empty vectors?
 		if (thisNorm == 0 || thatNorm == 0)
 			return 0;
 
 		int thisK = thisVector.getInt();
 		int thatK = thatVector.getInt();
 		while (true) {
 			try {
 				if (thisK == thatK) {
 					totalSum += thisVector.getFloat() * thatVector.getFloat();
 					thisK = thisVector.getInt();
 					thatK = thatVector.getInt();
 				} else if (thisK < thatK) {
 					thisVector.getFloat(); // skip
 					thisK = thisVector.getInt();
 				} else { // (thisK > thatK)
 					thatVector.getFloat(); // skip
 					thatK = thatVector.getInt();
 				}
 			} catch (BufferUnderflowException e) { break; } 
 		}
 
 		return totalSum / (thisNorm * thatNorm);
 	}
 	
 	@Override
 	public void setup(Context context)
 		throws IOException, InterruptedException {
 			
 			System.out.println("setup() called");
 		
 			Configuration conf = context.getConfiguration();
 			FileSystem fs = FileSystem.get(conf);
 			
 			String modelsDirectory = conf.get(MODELSDIR);
 			if (modelsDirectory == null)
 				throw new IllegalArgumentException("no models directory specified");
 			
 			Path modelsDirectoryPath = new Path(modelsDirectory);
 			FileStatus[] modelFiles = fs.listStatus(modelsDirectoryPath);
 			
 			if (modelFiles.length < 1)
 				throw new IllegalArgumentException("models directory empty");
 			
 			for (FileStatus modelFile: modelFiles) {
 				Path f = modelFile.getPath();
 				Reader reader = new SequenceFile.Reader(fs, f, conf);
 				
 				Text key = new Text();
 				ByteArrayWritable value = new ByteArrayWritable();
 				List<NamedObject<byte[]>> vectors = new LinkedList<NamedObject<byte[]>>();
 
 				while (reader.next(key, value))
 					vectors.add(new NamedObject<byte[]>(key.toString(), value.getBytes()));
 
 				reader.close();
 				files.add(new NamedObject<List<NamedObject<byte[]>>>(f.getName(), vectors));
 			}
 	}
 	
 	private static void printUsage() {
 		System.out.println("CosineSimilarityCalculator <input path> <output path> <models dir>");
 		System.exit(-1);
 	}
 	
 	public static void main(String[] args) 
 		throws IOException, InterruptedException, ClassNotFoundException {
 		
 		if (args.length < 3)
 			printUsage();
 		
         Configuration conf = new Configuration();
 
         // Add resources
         conf.addResource("hdfs-default.xml");
         conf.addResource("hdfs-site.xml");
         conf.addResource("mapred-default.xml");
         conf.addResource("mapred-site.xml");
         
         conf.set(MODELSDIR, args[2]);
         
         Job job = new Job(conf);
         job.setJobName("CosineSimilarityCalculator");
 
         job.setMapOutputKeyClass(NullWritable.class);
         job.setMapOutputValueClass(Text.class);
 
         job.setOutputKeyClass(NullWritable.class);
         job.setOutputValueClass(Text.class);
 
         job.setMapperClass(CosineSimilarityCalculator.class);
 
         // map-only job
         job.setNumReduceTasks(0);
         
         // Set the input format class
         job.setInputFormatClass(SequenceFileInputFormat.class);
         // Set the output format class
         job.setOutputFormatClass(TextOutputFormat.class);
         // Set the input path
         SequenceFileInputFormat.setInputPaths(job, args[0]);
         // Set the output path
         TextOutputFormat.setOutputPath(job, new Path(args[1]));
 
         /* Set the minimum and maximum split sizes
          * This parameter helps to specify the number of map tasks.
          * For each input split, there will be a separate map task.
          * Here each split is of size 32 MB
          */
        SequenceFileInputFormat.setMinInputSplitSize(job, 32 * MEGABYTES);
         SequenceFileInputFormat.setMaxInputSplitSize(job, 32 * MEGABYTES);
 
         // Set the jar file to run
         job.setJarByClass(CosineSimilarityCalculator.class);
 
         // Submit the job
         Date startTime = new Date();
         System.out.println("Job started: " + startTime);
         int exitCode = job.waitForCompletion(true) ? 0 : 1;
 
         if (exitCode == 0) {
         	Date end_time = new Date();
         	System.out.println("Job ended: " + end_time);
         	System.out.println("The job took " + (end_time.getTime() - startTime.getTime()) / 1000 + " seconds.");
         } else {
         	System.out.println("Job Failed!!!");
         }
 	}
 }
