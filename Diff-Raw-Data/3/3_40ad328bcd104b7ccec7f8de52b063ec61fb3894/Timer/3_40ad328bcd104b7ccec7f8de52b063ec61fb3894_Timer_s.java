 package ru.ifmo.neerc.timer;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 
 import org.ho.yaml.Yaml;
 
 import ru.ifmo.neerc.connectors.Pcms2;
 import ru.ifmo.neerc.framework.Callback;
 import ru.ifmo.neerc.framework.Settings;
 
 
 
 public class Timer {
 	private long length, time;
 	
 	public static void main(String[] args) {		
 		new Timer().run(args);
 	}
 
 	public void run(String[] args) {
 		final TimerFrame tf = new TimerFrame();
 		
 		if (args.length == 2) {
			tf.setStatus(Integer.parseInt(args[0]));
 			tf.sync(Integer.parseInt(args[1]));
 		} else {
 			Pcms2 connection = new Pcms2(Settings.instance().host);
 			connection.hookLengthChange(new Callback<Long>() {
 				@Override
 				public void exec(Long arg) {
 					length = arg;
 					tf.sync(length - time);
 				}
 			});
 			connection.hookTimeChange(new Callback<Long>() {
 				@Override
 				public void exec(Long arg) {
 					time = arg;
 					tf.sync(length - time);
 				}
 			});
 			connection.hookStatusChange(new Callback<Integer>() {
 				@Override
 				public void exec(Integer arg) {
 					tf.setStatus(arg);
 				}
 			});
 		}
 	}	
 } 
