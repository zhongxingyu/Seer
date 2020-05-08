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
public class MapperMonitor {
 
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
 
	@Pointcut ("call(void write" +
 			"(" +
 			"java.lang.Object, " +
 			"java.lang.Object" +
 			"))" +
            "&& target(org.apache.hadoop.mapreduce.Mapper$Context+)" +
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
        
        String ret = "";
 		try {
 	        String outfile = "/tmp" + "/" + this.LOGFILE;
 	        FileOutputStream fos = new FileOutputStream(outfile, true);
 	        OutputStreamWriter out = new OutputStreamWriter(fos);
            ret += "key:" + (String)key;
	        out.write(ret);
 	        out.close();
 		} catch (IOException ioe) {
 			System.out.println(ioe);
 		} catch (Exception e) {
 			System.out.println(e);
 		}
		System.err.println("** [POINTCUT]" + ret);
		System.out.println("** [POINTCUT]" + ret);
 	}
 }
