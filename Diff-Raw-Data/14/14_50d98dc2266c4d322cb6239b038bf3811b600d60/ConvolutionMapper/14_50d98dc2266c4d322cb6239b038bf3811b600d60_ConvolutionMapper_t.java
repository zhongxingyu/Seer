 package convolution.rchannel;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.HashMap;
 
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.filecache.DistributedCache;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.Mapper;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Reporter;
 
 /**
  * ConvolutionMapper
  * 
  */
 
 public class ConvolutionMapper extends MapReduceBase implements
 		Mapper<LongWritable, Text, NullWritable, Text> {
 
	public static final String HDFS_KERNEL = "/neuro/lookup/morlet-2000.dat";
 	public static final int SIGNAL_BUFFER_SIZE = 10000000;
 	public static final int KERNEL_START_FREQ = 5;
 	public static final int KERNEL_END_FREQ = 200;
 	public static final int KERNEL_WINDOW_SIZE = 2001;
 
 	static enum Parse_Counters {
 		BAD_PARSE
 	};
 
 	private final Text out_value = new Text();
 
 	private HashMap<Integer, String> kernelMap;
 	private short[][] kernelStack = new short[KERNEL_END_FREQ+1][KERNEL_WINDOW_SIZE];
 	
 	private long[] ckConvolution = new long[KERNEL_END_FREQ+1];
 	private short[] signal = new short[SIGNAL_BUFFER_SIZE];
 	private int n = 0;
 
 	private RChannelDataPoint rec;
 	
 	private long lastTimestamp = 0;
 
 	@Override
 	public void configure(JobConf conf) {
 		
 		try {
 			String kernelCacheName = new Path(HDFS_KERNEL).getName();
 			Path [] cacheFiles = DistributedCache.getLocalCacheFiles(conf);
 			if (null != cacheFiles && cacheFiles.length > 0) {
 				for (Path cachePath : cacheFiles) {
 					if (cachePath.getName().equals(kernelCacheName)) {
 						loadKernel(cachePath);
 						break;
 					}
 				}
 				for (int i=KERNEL_START_FREQ; i <=   KERNEL_END_FREQ; i++) {
 					kernelStack[i] = ConvertStringArrayToShortArray(kernelMap.get(i).split(","));
 				}
 			}
 			AlterRatPartitions(conf);
 		} catch (IOException ioe) {
 			System.err.println("IOException reading from distributed cache");
 			System.err.println(ioe.toString());
 		}
 	}
 
 	public void AlterRatPartitions(JobConf conf) throws IOException {
 		String fpath = conf.get("map.input.file");
 		String fname = new File(fpath).getName();
 
		BufferedWriter alterout = new BufferedWriter(new FileWriter("/neuro/neurosrc/script/hive/alterrats.q", true));		
		BufferedWriter insertout = new BufferedWriter(new FileWriter("/neuro/neurosrc/script/hive/insertratsaverage.q", true));		
 		
 		String ratnumber;
 		String sessiondate;
 		String channelid;
 		
 		int indexBegin = 0;
 		int indexEnd = fname.indexOf('-');
 		
 		ratnumber = fname.substring(indexBegin, indexEnd);
 		indexBegin = indexEnd+1;
 		indexEnd = fname.indexOf('-', indexBegin);
 		sessiondate = fname.substring(indexBegin, indexEnd);
 		indexBegin = indexEnd+1;
 		indexEnd = fname.indexOf('-', indexBegin);
 		sessiondate = sessiondate + '-' + fname.substring(indexBegin, indexEnd);
 		indexBegin = indexEnd+1;
 		indexEnd = fname.indexOf('-', indexBegin);
 		sessiondate = sessiondate + '-' + fname.substring(indexBegin, indexEnd);
 		indexBegin = indexEnd+4;
 		indexEnd = fname.indexOf('.', indexBegin);
 		channelid = fname.substring(indexBegin, indexEnd);
 		
 		alterout.append("ALTER TABLE rats ADD PARTITION(rat='" + ratnumber + "',dt='" + sessiondate + "',channel='" + channelid + "');");
 		alterout.newLine();
 		if (channelid.contains("r")) {
 			alterout.newLine();			
 			alterout.append("ALTER TABLE ratsaverage ADD PARTITION(rat='" + ratnumber + "',dt='" + sessiondate + "',channel='" + channelid + "');");
 			alterout.newLine();			
 			alterout.newLine();			
 			insertout.append("INSERT OVERWRITE TABLE ratsaverage PARTITION (rat='" + ratnumber + "',dt='" + sessiondate + "',channel='" + channelid + "')");
 			insertout.newLine();			
 			insertout.append("SELECT time, frequency, convolution");
 			insertout.newLine();			
 			insertout.append("FROM rats");
 			insertout.newLine();			
 			insertout.append("WHERE rat='" + ratnumber + "'");
 			insertout.newLine();			
 			insertout.append("AND dt='" + sessiondate + "'");
 			insertout.newLine();			
 			insertout.append("AND channel='" + channelid + "'");
 			insertout.newLine();			
 			insertout.append(";");
 			insertout.newLine();			
 			insertout.newLine();			
 
 		} else if (channelid.contains("3")) {
 			alterout.newLine();
 			alterout.append("ALTER TABLE ratsaverage ADD PARTITION(rat='" + ratnumber + "',dt='" + sessiondate + "',channel='avg');");
 			alterout.newLine();
 			insertout.append("INSERT OVERWRITE TABLE ratsaverage PARTITION (rat='" + ratnumber + "',dt='" + sessiondate + "', channel='avg')");
 			insertout.newLine();			
 			insertout.append("SELECT time, frequency, AVG(convolution)");
 			insertout.newLine();			
 			insertout.append("FROM rats");
 			insertout.newLine();			
 			insertout.append("WHERE rat='" + ratnumber + "'");
 			insertout.newLine();			
 			insertout.append("AND dt='" + sessiondate + "'");
 			insertout.newLine();			
 			insertout.append("AND NOT(channel LIKE '%r%')");
 			insertout.newLine();			
 			insertout.append("GROUP BY time, frequency");
 			insertout.newLine();			
 			insertout.append(";");
 			insertout.newLine();	
 			insertout.newLine();	
 
 		}
 		
 		alterout.flush();
 		alterout.close();
 		insertout.flush();
 		insertout.close();
 
 	}
 
 	public void loadKernel(Path cachePath) throws IOException {
 		BufferedReader kernelReader = new BufferedReader(new FileReader(cachePath.toString()));
 		try {
 			String line = "";
 			int kernelFreq = KERNEL_START_FREQ;
 			this.kernelMap = new HashMap<Integer, String>();
 			while ((line = kernelReader.readLine()) != null) {
 				this.kernelMap.put(kernelFreq, line);
 				kernelFreq++;
 			}
 		} finally {
 			kernelReader.close();
 		}
 	}
 
 	public short[] ConvertStringArrayToShortArray(String[] stringArray){
 		short shortArray[] = new short[stringArray.length];
 
 		for(int i = 0; i < stringArray.length; i++){
 			shortArray[i] = Short.parseShort(stringArray[i]);
 		}
 
 		return shortArray;
 	}
 	
 	public void map(LongWritable inkey, Text value,
 		OutputCollector<NullWritable, Text> output,
 		Reporter reporter) throws IOException {
 
 		rec = RChannelDataPoint.parse(value.toString());
 
 		try {
 			
 			if (lastTimestamp > rec.getTimestamp()) {
 				throw new IOException("Timestamp not sorted at: " + 
 					lastTimestamp + " and " + 
 					rec.getTimestamp()
 					);
 			}
 			
 			lastTimestamp = rec.getTimestamp();
 			
 			if ( n == SIGNAL_BUFFER_SIZE ) {
 				n = 0;
 				for (int j = SIGNAL_BUFFER_SIZE-KERNEL_WINDOW_SIZE+1; j<SIGNAL_BUFFER_SIZE; j++) {
 				   signal[n] = signal[j];            
 				   n++;
 				} //for
 			} // if
 		
 			signal[n] = rec.getVoltage();
 
 			if (n>=KERNEL_WINDOW_SIZE-1) {
 
 				for (int k = KERNEL_START_FREQ; k <= KERNEL_END_FREQ; k++) {
 					ckConvolution[k] = 0;
 					
 					for (int i = n-KERNEL_WINDOW_SIZE+1, j=0; i <= n && j < KERNEL_WINDOW_SIZE; i++, j++) {
 						ckConvolution[k] += signal[i]*kernelStack[k][j];
 					} // for
 
 					out_value.set(
 						lastTimestamp + "," +
 						k + "," +
 						ckConvolution[k])
 					;
 					output.collect(NullWritable.get(), out_value);
 					
 				} //for
 			} // if
 
 			n++;
 
 		} catch (IOException ioe) {
 			System.err.println(ioe.getMessage());
 			System.exit(0);
 	   }
 	} // map
 
 }
