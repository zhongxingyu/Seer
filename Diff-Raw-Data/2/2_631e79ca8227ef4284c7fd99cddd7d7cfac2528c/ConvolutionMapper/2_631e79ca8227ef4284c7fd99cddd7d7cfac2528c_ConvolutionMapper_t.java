 package neurohadoop;
 
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.File;
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
 
 //import org.apache.log4j.Logger;
 
 /*
  * ConvolutionMapper
  */
 public class ConvolutionMapper extends MapReduceBase implements
 		Mapper<LongWritable, Text, NullWritable, RatWritable> {
 
 	public static final String HDFS_KERNEL = "lookup/morlet-2000.dat";
 	public static final int SIGNAL_BUFFER_SIZE = 16777216;
 	public static final int KERNEL_START_FREQ = 5;
 	public static final int KERNEL_END_FREQ = 200;
 	public static final int KERNEL_WINDOW_SIZE = 2001;
 
 	static enum Parse_Counters {
 		BAD_PARSE
 	};
 
 	private String sessiondate;
 	private final RatWritable out_value = new RatWritable();
 	private HashMap<Integer, String> kernelMap;
 	private short[][] kernelStack = new short[KERNEL_END_FREQ + 1][KERNEL_WINDOW_SIZE];
 	private int n = 0;
 
 	private float[] signal = new float[SIGNAL_BUFFER_SIZE];
 	private float[] kernel = new float[SIGNAL_BUFFER_SIZE];
 	private long[] timestamp = new long[SIGNAL_BUFFER_SIZE];
 
 	private OutputCollector<NullWritable, RatWritable> saveOutput;
 	private RatInputFormat rec;
 	private long lastTimestamp = 0;
 	// private static final Logger logger =
 	// Logger.getLogger(ConvolutionMapper.class);
 	private long tempTime;
 
 	@Override
 	public void configure(JobConf conf) {
 		tempTime = System.currentTimeMillis();
 		String fpath = conf.get("map.input.file");
 		String fname = new File(fpath).getName();
 		int indexBegin = 0;
 		int indexEnd = fname.indexOf('-');
 		indexBegin = indexEnd + 1;
 		indexEnd = fname.indexOf('-', indexBegin);
 		sessiondate = fname.substring(indexBegin, indexEnd);
 		indexBegin = indexEnd + 1;
 		indexEnd = fname.indexOf('-', indexBegin);
 		sessiondate = sessiondate + '-' + fname.substring(indexBegin, indexEnd);
 		indexBegin = indexEnd + 1;
 		indexEnd = fname.indexOf('-', indexBegin);
 		sessiondate = sessiondate + '-' + fname.substring(indexBegin, indexEnd);
 		indexBegin = indexEnd + 4;
 		indexEnd = fname.indexOf('.', indexBegin);
 		try {
 			String kernelCacheName = new Path(HDFS_KERNEL).getName();
 			Path[] cacheFiles = DistributedCache.getLocalCacheFiles(conf);
 			if (null != cacheFiles && cacheFiles.length > 0) {
 				for (Path cachePath : cacheFiles) {
 					if (cachePath.getName().equals(kernelCacheName)) {
 						loadKernel(cachePath);
 						break;
 					} // if
 				} // for
 				for (int i = KERNEL_START_FREQ; i <= KERNEL_END_FREQ; i++) {
 					kernelStack[i] = ConvertStringArrayToShortArray(kernelMap
 							.get(i).split(","));
 				} // for
 			} // if
 		} catch (IOException ioe) {
 			System.err.println("IOException reading from distributed cache");
 			System.err.println(ioe.toString());
 		} // try
 		System.out.println("Load Kernel: "
 				+ (System.currentTimeMillis() - tempTime));
 		tempTime = System.currentTimeMillis();
 	} // configure
 
 	public void loadKernel(Path cachePath) throws IOException {
 		BufferedReader kernelReader = new BufferedReader(new FileReader(
 				cachePath.toString()));
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
 	} // loadKernel
 
 	public short[] ConvertStringArrayToShortArray(String[] stringArray) {
 		short shortArray[] = new short[stringArray.length];
 		for (int i = 0; i < stringArray.length; i++) {
 			shortArray[i] = Short.parseShort(stringArray[i]);
 		}
 		return shortArray;
 	} // Convert
 
 	@Override
 	public void map(LongWritable inkey, Text value,
 			OutputCollector<NullWritable, RatWritable> output, Reporter reporter)
 			throws IOException {
 		saveOutput = output;
 		rec = RatInputFormat.parse(value.toString());
 		try {
 			if (lastTimestamp > rec.getTimestamp()) {
 				throw new IOException("Timestamp not sorted at: "
 						+ lastTimestamp + " and " + rec.getTimestamp());
 			}
 			lastTimestamp = rec.getTimestamp();
 			timestamp[n] = lastTimestamp;
 			signal[n] = rec.getVoltage();
 			n++;
 		} catch (IOException ioe) {
 			System.err.println(ioe.getMessage());
 			System.exit(0);
 		} // catch
 	} // map
 
 	@Override
 	public void close() throws IOException {
 		FloatFFT_1D fft = new FloatFFT_1D(SIGNAL_BUFFER_SIZE / 2);
 		try {
 
 			for (int i = n; i < SIGNAL_BUFFER_SIZE / 2; i++) {
 				signal[i] = 0;
 			}
 			System.out.println("Load Data: "
 					+ (System.currentTimeMillis() - tempTime));
 
 			tempTime = System.currentTimeMillis();
 			fft.realForwardFull(signal);
 			System.out.println("Signal FFT: "
 					+ (System.currentTimeMillis() - tempTime));
 
			for (short k = KERNEL_START_FREQ; k <= KERNEL_END_FREQ; k++) {
 
 				// Kernel FFT
 				tempTime = System.currentTimeMillis();
 				for (int j = 0; j < KERNEL_WINDOW_SIZE; j++) {
 					kernel[j] = (float) kernelStack[k][j];
 				}
 				for (int j = KERNEL_WINDOW_SIZE; j < SIGNAL_BUFFER_SIZE / 2; j++) {
 					kernel[j] = 0;
 				}
 				fft.realForwardFull(kernel);
 				System.out.println("Kernel FFT: "
 						+ (System.currentTimeMillis() - tempTime));
 
 				// Product
 				tempTime = System.currentTimeMillis();
 				float temp;
 				for (int i = 0; i < SIGNAL_BUFFER_SIZE; i = i + 2) {
 					temp = kernel[i];
 					kernel[i] = kernel[i] * signal[i] - kernel[i + 1]
 							* signal[i + 1];
 					kernel[i + 1] = -(temp * signal[i + 1] + kernel[i + 1]
 							* signal[i]);
 				}
 				System.out.println("Product: "
 						+ (System.currentTimeMillis() - tempTime));
 
 				// Inverse FFT
 				tempTime = System.currentTimeMillis();
 				fft.complexInverse(kernel, true);
 				System.out.println("Inverse FFT: "
 						+ (System.currentTimeMillis() - tempTime));
 
 				// Output
 				tempTime = System.currentTimeMillis();
 				int t = KERNEL_WINDOW_SIZE - 1;
 				for (int i = (SIGNAL_BUFFER_SIZE / 2 - KERNEL_WINDOW_SIZE + 1) * 2; i > (SIGNAL_BUFFER_SIZE / 2 - n) * 2; i = i - 2) {
 					out_value.timestamp = t;
 					out_value.frequency = k;
 					out_value.convolution = (float)Math.pow(kernel[i],2);
 					saveOutput.collect(NullWritable.get(), out_value);
 					t++;
 				}
 				System.out.println("Output Data: "
 						+ (System.currentTimeMillis() - tempTime));
 			} // for
 
 		} catch (IOException ioe) {
 			System.err.println(ioe.getMessage());
 			System.exit(0);
 		} // try
 	}
 }
