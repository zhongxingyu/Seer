 package aj.hadoop.monitor.mapreduce;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.text.*;
 import java.lang.*;
 import java.lang.management.*;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.mapreduce.*;
 import org.apache.hadoop.mapreduce.Mapper.Context;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Pointcut;
 import org.aspectj.lang.annotation.After;
 import org.aspectj.lang.annotation.Before;
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.Signature;
 
 import aj.hadoop.monitor.util.PickerClient;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.util.logging.FileHandler;
 
 @Aspect
public abstract class MapperMonitor {
 
 	private String HOST = "localhost";
 	private int PORT = 9999;
 	
 	/** log file */
 	public static final String LOGFILE = "Hanoi-MethodTraceMonitor.log";
 	
 	/** logger */
 	private Logger logger = null;
 	
 	/** file handler */
 	private FileHandler fh = null;
 	
 	/**
 	 * initialize
 	 */
 	public MapperMonitor() {
 		logger = Logger.getLogger(this.getClass().getName());
 		try {
 			fh = new FileHandler(this.LOGFILE, true);
 			logger.addHandler(fh);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		logger.log(Level.INFO, "Start trace...");
 	}
 
	@Pointcut ("call(void org.apache.hadoop.mapreduce.Mapper.Context.write" +
 			"(" +
 			"java.lang.Object, " +
 			"java.lang.Object" +
 			"))" +
			"&& cflow(execution(public void org.apache.hadoop.examples.WordCount$TokenizerMapper.map(..)))" +
 			"&& args(key, value)")
 	public void pointcut_mapper_out(Object key, Object value){}
 
 	@Before (value = "pointcut_mapper_out( key, value )")
 	public void logging( JoinPoint thisJoinPoint,
 			Object key,
 			Object value) {
 		//PickerClient client = new PickerClient();
 		//client.setHost(this.HOST);
 		//client.setPORT(this.PORT);
 		//client.send((String)key);
 		try {
 	        String outfile = "/tmp" + "/" + this.LOGFILE;
 	        FileOutputStream fos = new FileOutputStream(outfile, true);
 	        OutputStreamWriter out = new OutputStreamWriter(fos);
	        out.write((String)key);
 	        out.close();
 		} catch (IOException ioe) {
 			System.out.println(ioe);
 		} catch (Exception e) {
 			System.out.println(e);
 		}
		System.err.println("** [POINTCUT]" + (String)key);
		System.out.println("** [POINTCUT]" + (String)key);
 	}
 }
