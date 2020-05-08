 package pl.softwaremill.demo.tools;
 
 import com.xerox.amazonws.ec2.*;
 import pl.softwaremill.demo.impl.sdb.AwsAccessKeys;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Adam Warski (adam at warski dot org)
  */
 public class StartNewInstance {
     private final AwsAccessKeys awsAccessKeys;
 
     public StartNewInstance(AwsAccessKeys awsAccessKeys) {
         this.awsAccessKeys = awsAccessKeys;
     }
 
     public void start() throws EC2Exception, LoadBalancingException {
         Jec2 ec2 = createJec2();
         List<String> instanceIds = startInstance(ec2);
         registerWithElb(instanceIds);
     }
 
     private Jec2 createJec2() {
         return new Jec2(awsAccessKeys.getAccessKeyId(), awsAccessKeys.getSecretAccessKey(),
                 true, "ec2.eu-west-1.amazonaws.com");
     }
 
     public List<String> startInstance(Jec2 ec2) throws EC2Exception {
        LaunchConfiguration launchConfiguration = new LaunchConfiguration("ami-e287b196");
         launchConfiguration.setSecurityGroup(Arrays.asList("SoftDevCon"));
         launchConfiguration.setKeyName("confitura");
         launchConfiguration.setAvailabilityZone("eu-west-1c");
         ReservationDescription reservationDescription = ec2.runInstances(launchConfiguration);
 
         List<String> instanceIds = new ArrayList<String>();
         for (ReservationDescription.Instance instance : reservationDescription.getInstances()) {
             System.out.println("Started instance " + instance);
             instanceIds.add(instance.getInstanceId());
         }
 
         return instanceIds;
     }
 
     public void registerWithElb(List<String> instanceIds) throws LoadBalancingException {
         LoadBalancing loadBalancing = createLoadBalancing();
         loadBalancing.registerInstancesWithLoadBalancer("SoftDevConLB", instanceIds);
         System.out.println("Registered " + instanceIds + " with the load balancer.");
     }
 
     private LoadBalancing createLoadBalancing() {
         return new LoadBalancing(awsAccessKeys.getAccessKeyId(),
                 awsAccessKeys.getSecretAccessKey(), true, "elasticloadbalancing.eu-west-1.amazonaws.com");
     }
 
     public static void main(String[] args) throws IOException, LoadBalancingException, EC2Exception {
         new StartNewInstance(AwsAccessKeys.createFromResources()).start();
     }
 }
