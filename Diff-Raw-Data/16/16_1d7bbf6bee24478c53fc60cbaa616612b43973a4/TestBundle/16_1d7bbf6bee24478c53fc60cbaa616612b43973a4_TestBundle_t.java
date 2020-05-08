 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.xerox.amazonws.ec2.BundleInstanceInfo;
 import com.xerox.amazonws.ec2.ConsoleOutput;
 import com.xerox.amazonws.ec2.Jec2;
 import com.xerox.amazonws.ec2.UploadPolicy;
 
 // args : 
 // 0 - instance id
 // 1 - bucket
 // 2 - prefix
 
 public class TestBundle {
     private static Log logger = LogFactory.getLog(TestJec2.class);
 
 	public static void main(String [] args) throws Exception {
 		Properties props = new Properties();
 		props.load(TestJec2.class.getClassLoader().getResourceAsStream("aws.properties"));
 
		Jec2 ec2 = new Jec2(props.getProperty("aws.accessId"), props.getProperty("aws.secretKey"));
 		List<String> params = new ArrayList<String>();
 		ConsoleOutput cons = ec2.getConsoleOutput(args[0]);
 		logger.info("console output = "+cons.getOutput());
 		BundleInstanceInfo info = ec2.bundleInstance(args[0], props.getProperty("aws.accessId"), 
								args[1], args[2], new UploadPolicy(60*12, args[1], "ec2-bundle-read", args[2]));
 		logger.info("Bunding instance "+args[0]);
 
 		List<BundleInstanceInfo> tasks = ec2.describeBundleTasks(new String [] {});
 		for (BundleInstanceInfo task : tasks) {
 			logger.info("bundle id : "+task.getBundleId());
 			logger.info("state : "+task.getState());
 			logger.info("state : "+task.getState());
 		}
//		info = ec2.cancelBundleInstance(info.getBundleId());
//		logger.info("Cancelled bundle task : "+info.getBundleId());
 	}
 }
 
