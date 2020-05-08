 package aj.hadoop.monitor.examples;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.text.*;
 import java.lang.*;
 import java.lang.management.*;
 import java.util.StringTokenizer;
 import java.util.concurrent.Phaser;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
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
 
 import org.fluentd.logger.FluentLogger;
 
 @Aspect
 public class WordCountMonitor {
 
	private static String HOST = getHostName();
 
     /** init java logger */
    public static FluentLogger LOG = FluentLogger.getLogger("keys.hanoi.keymap.trace");
 
 	/**
 	 * pointcut for map method
 	 * @param word
 	 * @param one
 	 */
 	@Pointcut ("call(void org.apache.hadoop.mapreduce.Mapper.Context.write" +
 			"(" +
 			"java.lang.Object, " +
 			"java.lang.Object" +
 			"))" +
 			"&& cflow(execution(public void com.github.karahiyo.hadoop.mapreduce.examples.WordCount$TokenizerMapper.map(..)))" +
 			"&& args(word, one)")
 	public void catch_map_method(Object word, Object one){}
 
 	/**
 	 * pointcut for reduce method
 	 * @param word
 	 * @param values
 	 * @param cntext
 	 */
 	@Pointcut ("execution(void com.github.karahiyo.hadoop.mapreduce.examples.WordCount.IntSumReducer.reduce(..))")
 	public void catch_reduce_method(){}
 
 
 	/**
 	 * logging map method using HanoiPicker
 	 * @param thisJoinPoint
 	 * @param key
 	 * @param one
 	 */
 	@Before ( value = "catch_map_method( key, one )")
 	public void logging_map_method( JoinPoint thisJoinPoint,
 			Object key,
 			Object one) {
 
         Map<String, Object> data = new HashMap<String, Object>();
         data.put("keys", key);
        LOG.log("map." + HOST, data);
 	}
 
 	/**
 	 * logging reduce method using HanoiPicker
 	 * @param thisJoinPoint
 	 * @param key
 	 * @param one
 	 */
 	@Before ( value = "catch_reduce_method()")
 	public void logging_reduce_method( JoinPoint thisJoinPoint) {
 
         Map<String, Object> data = new HashMap<String, Object>();
         Object[] params = thisJoinPoint.getArgs();
         data.put("keys", params[0]);
        LOG.log("shuffle." + HOST, data);
 	}

    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "UnknownHost";
    }
 }
