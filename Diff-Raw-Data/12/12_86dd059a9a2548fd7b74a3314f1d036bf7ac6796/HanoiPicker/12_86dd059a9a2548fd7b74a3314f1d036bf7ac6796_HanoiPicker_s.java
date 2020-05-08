 package aj.hadoop.monitor.server;
 
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
 import org.apache.hadoop.mapred.*;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Pointcut;
 import org.aspectj.lang.annotation.After;
 import org.aspectj.lang.annotation.Before;
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.Signature;
 
 import com.github.karahiyo.hanoi_picker.PickerDaemon;
 
 @Aspect
 public class HanoiPicker {
 
 	@Pointcut ( "initialization(org.apache.hadoop.mapred.TaskTracker.new()" )
 	public void atTaskTrackerNew(){}
 
 	@Around( value = "atTaskTrackerNew()" )
	public void start_picker_daemon( JoinPoint thisJoinPoint) {
 		PickerDaemon pickerDaemon = new PickerDaemon();
 		try {
 			pickerDaemon.run();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return thisJoinPoint.proceed();
 
 	}
 }
