 package com.danielvizzini.baseball;
 
 import java.io.IOException;
 import java.util.ArrayDeque;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.ArrayWritable;
 import org.apache.hadoop.io.FloatWritable;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.Mapper;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Reducer;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.hadoop.mapred.TextInputFormat;
 import org.apache.hadoop.mapred.TextOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 public class Baseball extends Configured implements Tool {
 
 	public static final int MIN_NUM_ATBATS = 100;
 	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntArrayWritable> {
 		
 		//legacy code from tutorial
 		static enum Counters { INPUT_LINES }
 
 		private long numRecords = 0;
 		private String inputFile;
 
 		@Override
 		public void configure(JobConf job) {
 			inputFile = job.get("map.input.file");
 		}
 
 		//create queues so dates do not need to be recalculated
 		ArrayDeque<String> batterQueue = new ArrayDeque<String>();
 		ArrayDeque<Integer> pitchesQueue = new ArrayDeque<Integer>();
 		ArrayDeque<Integer> rbisQueue = new ArrayDeque<Integer>();
 		ArrayDeque<Integer> basesQueue = new ArrayDeque<Integer>();
 		
 		//keep track of inning totals
 		int pitchesInInning = 0;
 		int rbisInInning = 0;
 		int basesInInning = 0;
 
 		public void map(LongWritable key, Text value, OutputCollector<Text, IntArrayWritable> output, Reporter reporter) throws IOException {
 			
 			//ready input
 			String file = value.toString();
 			StringTokenizer tokenizer = new StringTokenizer(file, "|");
 			
 			//instantiate to non-existent for comparison
 			int[] halfInning = {0,0};
 			
 			//begin iteration through batters
 			while (true) {
 								
 				//iterate
 				String line = (tokenizer.hasMoreTokens()) ? tokenizer.nextToken() : null;
 				String[] csv = {}; 	
 				
 				//even if line is null, last half-inning will still need to be collected
 				if (line != null) {
 					//break up records
 					csv = line.split(",");
 
 					//ignore badly formatted
 					if(csv.length == 0) continue;					
 
 					//ignore non-play records
 					if(!csv[0].equals("play")) continue;
 					
 					//ignore badly formatted
 					if(csv.length != 7) continue;					
 				}
 				
 				//mark end of half-inning (short-circuiting prevents null pointer)
 				if (line == null || Integer.parseInt(csv[1]) != halfInning[0] || Integer.parseInt(csv[2]) != halfInning[1]) {
 					
 					//populate last half-inning
 					while (!batterQueue.isEmpty()) {
 						
 						//instantiate and set key
 						Text batter = new Text();
						batter.set(batterQueue.pop());					
 
 						/**
						 * To be passed to reducer
 						 * index 0: number of pitches
 						 * index 1: pitches in inning
 						 * index 2: num batter's rbis
 						 * index 3: rbis in inning
 						 * index 4: num batter's bases
 						 * index 5: bases in inning
 						 */
 						IntWritable[] mappedValues = new IntWritable[6];
 						
 						//set values
 						mappedValues[0] = new IntWritable(pitchesQueue.pop());
						mappedValues[1] = new IntWritable(pitchesInInning);
 						mappedValues[2] = new IntWritable(rbisQueue.pop());
						mappedValues[3] = new IntWritable(rbisInInning);
 						mappedValues[4] = new IntWritable(basesQueue.pop());
						mappedValues[5] = new IntWritable(basesInInning);
 						
 						//store for Hadoop
 						output.collect(batter, new IntArrayWritable(mappedValues));
 						reporter.incrCounter(Counters.INPUT_LINES, 1L);
 						
 					}
 					
 					//having collected last half-inning, break
 					if (line == null) break;
 					
 					//update half inning
 					halfInning[0] = Integer.parseInt(csv[1]);
 					halfInning[1] = Integer.parseInt(csv[2]);
 					
 					//reset values
 					batterQueue = new ArrayDeque<String>();
 					pitchesQueue = new ArrayDeque<Integer>();
 					rbisQueue = new ArrayDeque<Integer>();
 					basesQueue = new ArrayDeque<Integer>();
 					pitchesInInning = 0;
 					rbisInInning = 0;
 					basesInInning = 0;
 					
 				}
 						
 				//mark batter
 				batterQueue.add(csv[3]);
 				
 				//calculate statistics
 				int pitches = Baseball.countPitches(csv[5]);
 				int rbis = Baseball.countRbis(csv[6]);
 				int bases = Baseball.countBases(csv[6]);
 				
 				//update totals
 				pitchesInInning += pitches;
 				rbisInInning += rbis;
 				basesInInning += bases;
 				
 				//update queues
 				pitchesQueue.add(pitches);
 				rbisQueue.add(rbis);
 				basesQueue.add(bases);
 				
 			}
 
 			//legacy code from tutorial
 			reporter.setStatus("Finished processing " + ++numRecords  + " records " + "from the input file: " + inputFile);
 		}
 	}
 
 	public static class Reduce extends MapReduceBase implements Reducer<Text, IntArrayWritable, Text, FloatArrayWritable> {
 
 		public void reduce(Text key, Iterator<IntArrayWritable> values, OutputCollector<Text, FloatArrayWritable> output, Reporter reporter) throws IOException {
 			
 			int numAtBats = 0;
 
 			//calculate batter's totals
 			int pitchesNumerator = 0;
 			int pitchesDenominator = 0;
 			int rbisNumerator = 0;
 			int rbisDenominator = 0;
 			int basesNumerator = 0;
 			int basesDenominator = 0;
 			
 			//calculate statistics
 			while (values.hasNext()) {
 				ArrayWritable value = values.next();
 				pitchesNumerator += ((IntWritable) value.get()[0]).get();
 				pitchesDenominator += ((IntWritable) value.get()[1]).get();
 				rbisNumerator += ((IntWritable) value.get()[2]).get();
 				rbisDenominator += ((IntWritable) value.get()[3]).get();
 				basesNumerator += ((IntWritable) value.get()[4]).get();
 				basesDenominator += ((IntWritable) value.get()[5]).get();
 				numAtBats++;
 			}
 			
 			//only include batter's with three valid statistics
 			if (pitchesDenominator == 0 || rbisDenominator == 0 || basesDenominator == 0) return;
 				
 			//only include batters with MIN_NUM_ATBATS
 			if (numAtBats < MIN_NUM_ATBATS) return;
 			
 			//record aggregate statistics
 			FloatWritable[] reducedValues = new FloatWritable[3];
 			reducedValues[0] = new FloatWritable(Float.valueOf(pitchesNumerator) / Float.valueOf(pitchesDenominator));
 			reducedValues[1] = new FloatWritable(Float.valueOf(rbisNumerator) / Float.valueOf(rbisDenominator));
 			reducedValues[2] = new FloatWritable(Float.valueOf(basesNumerator) / Float.valueOf(basesDenominator));
 			
 			output.collect(key, new FloatArrayWritable(reducedValues));
 			
 		}				
 	}
 
 	public int run(String[] args) throws Exception {
 		JobConf conf = new JobConf(getConf(), Baseball.class);
 		conf.setJobName("baseball");
 		
 		conf.setOutputKeyClass(Text.class);
 		conf.setOutputValueClass(IntArrayWritable.class);
 		
 		conf.setMapperClass(Map.class);
 		conf.setReducerClass(Reduce.class);
 		
 		conf.setInputFormat(TextInputFormat.class);
 		conf.setOutputFormat(TextOutputFormat.class);
 		
 		FileInputFormat.setInputPaths(conf, new Path(args[0]));
 		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
 		
 		JobClient.runJob(conf);
 		return 0;
 
 	}
 	
 	public static void main(String[] args) throws Exception {
 		
 		int res = ToolRunner.run(new Configuration(), new Baseball(), args);
 	    System.exit(res);
 	    
 	}
 	
 	/**
 	 * @param pitch field of play record
 	 * @return all actions that constitute a pitch, as defined by http://www.retrosheet.org/eventfile.htm
 	 */
 	static int countPitches(String str) {
 		int pitches = 0;
 		Matcher matcher = Pattern.compile("[BCFHKLMOPQRSTUXY]").matcher(str);
 		while (matcher.find()) pitches++;
 		return pitches;
 	}
 	
 	/**
 	 * @param event field of play record
 	 * @return all runs scored during event
 	 */
 	static int countRbis(String str) {
 		int rbis = 0;
 		Matcher matcher = Pattern.compile("-H|HR").matcher(str);
 		while (matcher.find()) rbis++; 
 		return rbis;
 	}
 	
 	static int countBases(String str) {
 		//hit by pitch
 		if (str.startsWith("HP")) return 1;
 		
 		switch (str.charAt(0)) {
 		//single, intentional walk, and walk
 		case 'S':
 		case 'I':
 		case 'W':
 			return 1;
 		case 'D':
 			return 2;
 		case 'T':
 			return 3;
 		case 'H':
 			return 4;
 		default:
 			return 0;
 		}
 	}
 	
 	public static void replaceAll(StringBuilder builder, String from, String to) {
 	    int index = builder.indexOf(from);
 	    while (index != -1) {
 	        builder.replace(index, index + from.length(), to);
 	        index += to.length(); // Move to the end of the replacement
 	        index = builder.indexOf(from, index);
 	    }
 	}
 
 
 }
