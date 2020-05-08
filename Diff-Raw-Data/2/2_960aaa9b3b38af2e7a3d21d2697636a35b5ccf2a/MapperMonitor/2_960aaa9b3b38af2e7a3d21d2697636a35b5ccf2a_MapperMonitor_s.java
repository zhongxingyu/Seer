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
 
import aj.hadoop.monitor.mapreduce.util.PickerClient;
 
 @Aspect
 public abstract class MapperMonitor {
 
 	private String HOST = "localhost";
 	private int PORT = 9999;
 
 	@Pointcut ("call(void org.apache.hadoop.mapreduce.Mapper.Context+.write" +
 			"(" +
 			"java.lang.Object, " +
 			"java.lang.Object" +
 			"))" +
 			"&& args(key, value)")
 	public void pointcut_mapper_out(Object key, Object value){}
 
 	@Before ("pointcut_mapper_out( key, value )")
 	public void logging( JoinPoint thisJoinPoint,
 			Object key,
 			Object value) {
 		PickerClient client = new PickerClient();
 		client.setHost(this.HOST);
 		client.setPORT(this.PORT);
 		client.send((String)key);
 	}
 }
