 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import com.amazonaws.services.ec2.AmazonEC2;
 import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
 
 /**
  * Responsible class for instance termination and getting the instance type
  * 
  * @author Eduardo Hernandez Marquina
  * @author Hector Veiga
  * @author Gerardo Travesedo
  * 
  */
 public class InstanceManager {
 	private static AmazonEC2 ec2;
 
 	public InstanceManager(AmazonEC2 ec2){
 		setEc2(ec2);
 	}
 
 	// Kill myself
 	public void killmePlease() {
 		// Kill myself
 		String instanceId = "";
 		try {
 			Process p = Runtime
 					.getRuntime()
 					.exec("sudo wget -q -O - http://169.254.169.254/latest/meta-data/instance-id");
 			p.waitFor();
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					p.getInputStream()));
 			instanceId = br.readLine();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		List<String> instancesToTerminate = new ArrayList<String>();
 		instancesToTerminate.add(instanceId);
 
 		TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest();
 		terminateRequest.setInstanceIds(instancesToTerminate);
 		getEc2().terminateInstances(terminateRequest);
 	}
 
 	public String getInstanceType() {
 		String instanceType = "";
 		try {
 			Process p = Runtime
 					.getRuntime()
 					.exec("sudo wget -q -O - http://169.254.169.254/latest/meta-data/instance-type");
 			p.waitFor();
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					p.getInputStream()));
 			instanceType = br.readLine();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return instanceType;
 	}
 
 	public static AmazonEC2 getEc2() {
 		return ec2;
 	}
 
 	public static void setEc2(AmazonEC2 ec2) {
 		InstanceManager.ec2 = ec2;
 	}
 }
