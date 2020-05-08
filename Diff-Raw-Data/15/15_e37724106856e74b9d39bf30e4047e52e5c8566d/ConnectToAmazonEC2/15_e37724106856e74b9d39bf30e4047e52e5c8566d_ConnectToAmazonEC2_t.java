 package por;
 
 import por.util.PORPropertyConfigurator;
 import com.amazonaws.AmazonClientException;
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.ec2.AmazonEC2Client;
 import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
 import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
 import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
 import com.amazonaws.services.ec2.model.DescribeInstancesResult;
 import com.amazonaws.services.ec2.model.Instance;
 import com.amazonaws.services.ec2.model.InstanceStatus;
 import com.amazonaws.services.ec2.model.Reservation;
 import com.amazonaws.services.ec2.model.RunInstancesRequest;
 import com.amazonaws.services.ec2.model.RunInstancesResult;
 import com.amazonaws.services.ec2.model.StartInstancesRequest;
 import com.amazonaws.services.ec2.model.StopInstancesRequest;
 import com.amazonaws.services.ec2.model.Tag;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import por.util.CommandExecutor;
 import static por.util.CommandExecutor.prcs;
 
 /**
  *
  * @author rucha, poojad
  */
 public class ConnectToAmazonEC2 {
 
     private static Instance running_instance; // the EC2 instance on which the application runs
     private static AmazonEC2Client ec2client; // used to bind the access key and the secret key to the instance
     static Logger logger = Logger.getLogger(ConnectToAmazonEC2.class); // used to log events etc. on the console and in the log file
     protected static String pubDnsName = null; // public DNS name of the instance
 
     public static int connectToCloud(String pemFilePath, String accessKey, String secretKey) {
         // pemFilePath -> System path of the private key (with .pem extension) input through the GUI
         // accessKey, secretKey -> keys provided by Amazon to the user, input through the GUI
 
         //INPUT USER ACCOUNT DATA
         PropertyConfigurator.configure(PORPropertyConfigurator.logger_path); ////////////////////////////////
         logger.info("Entering the class");
         try {
             // bind the accessKey and the secretKey to the ec2 client object
             AWSCredentials awsc = new BasicAWSCredentials(accessKey, secretKey);
             ec2client = new AmazonEC2Client(awsc);
 
             // Retrieve the key-pair name from the .pem file path provided through the GUI
             String[] temp = pemFilePath.split("/");
             String keyName = temp[temp.length - 1];
             keyName = keyName.substring(0, keyName.length() - 4);
             logger.info("keyname " + keyName);
 
             // Retrieve information about instances currently belonging to the user
             DescribeInstancesRequest request = new DescribeInstancesRequest();
             DescribeInstancesResult result = ec2client.describeInstances(request);
 
             List<Reservation> reservations = result.getReservations();
 
             for (Reservation reservation : reservations) {
                 List<Instance> instances = reservation.getInstances();
 
                 for (Instance instance : instances) {
                     // Retrieve instances with a particular AMI Id, provided in the configuration file
                     if (instance.getImageId().equals(PORPropertyConfigurator.ami_id)) {
 
                         // Assumption: Only one instance with the particular AMI and key-pair name exists for the user, at a single point of time
                         String instanceName = instance.getState().getName(); // name of the state of the instance in the assumption
                         running_instance = instance;
                         pubDnsName = instance.getPublicDnsName();
                         logger.info("Public DnS Name " + pubDnsName);
                         logger.info("IP address of the instance " + instance.getPublicIpAddress()); // Public IP address of the instance
 
                         // If the instance in the assumption is stopped, start the instance to get it in the 'running' state, and use it to run the application
                         if (instanceName.equals("stopped")) {
                             // create a request object for the above 
                             StartInstancesRequest startInstancesRequest = new StartInstancesRequest();
                             List<String> instanceList = new ArrayList<>();
                             // add the instance to a list, as required by the method
                             instanceList.add(instance.getInstanceId());
                             // start the instance in the list (there will only be one instance in the list, according to the following assumption)
                             startInstancesRequest.setInstanceIds(instanceList);
                             ec2client.startInstances(startInstancesRequest);
                             do {
                                 logger.info("Instance Status = " + instance.getState().getName());
                                 // wait for the instance to come in the 'running' state
                                 Thread.sleep(5 * 1000);
                                 // get the statuses of all instances which are running
                                 DescribeInstanceStatusRequest describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(instance.getInstanceId());
                                 DescribeInstanceStatusResult describeInstanceResult = ec2client.describeInstanceStatus(describeInstanceRequest);
                                 List<InstanceStatus> state = describeInstanceResult.getInstanceStatuses();
                                 while (state.size() < 1) {
                                     // Do nothing, just wait, have thread sleep if needed
                                     describeInstanceResult = ec2client.describeInstanceStatus(describeInstanceRequest);
                                     state = describeInstanceResult.getInstanceStatuses();
                                 }
                                 instanceName = state.get(0).getInstanceState().getName();
                                request = new DescribeInstancesRequest().withInstanceIds(instance.getInstanceId());
                                result = ec2client.describeInstances(request);

                                reservations = result.getReservations();

                                for (Reservation r : reservations) {
                                    List<Instance> insts = r.getInstances();
                                    for (Instance i : insts) {
                                        pubDnsName = i.getPublicDnsName();
                                        logger.info("DNS " + pubDnsName);
                                    }
                                }
                                 logger.info("Instance Name=" + instanceName);
                             } while (!instanceName.equals("running")); // get status while the state is not 'running'
                             // the state of the instance is now 'running'
                             logger.info("Exiting the method");
                             return 0;
                         } else if (instanceName.equalsIgnoreCase("running")) { // if the state of the instance is running already, use the instance to run the application
                             logger.info("Status = " + instance.getState().getName());
                             logger.info("Exiting the method");
                             return 0;
                         }
                     }
                 }
             }
 
             // RUN EC2 INSTANCES
             // if no instance of the particular AMI Id and key-pair exists, create a new instance
             RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
 
             // specify constraints for the instance
             runInstancesRequest.withImageId(PORPropertyConfigurator.ami_id)
                     .withInstanceType("t1.micro")
                     .withMinCount(1)
                     .withMaxCount(1)
                     .withKeyName(keyName)
                     .withSecurityGroups("default");
 
             // run the instance with the above constraints
             RunInstancesResult runResult = ec2client.runInstances(runInstancesRequest);
 
             // DISPLAY IF RUNNING OR NOT
             String status = null;
             String runningId = null;
             do {
                 List<Instance> newlyCreatedList = runResult.getReservation().getInstances();
                 Instance runningInstance = newlyCreatedList.get(0);
                 running_instance = runningInstance;
                 // get instance id of the instance
                 runningId = runningInstance.getInstanceId();
                 // get public DNS name of the instance
                 pubDnsName = runningInstance.getPublicDnsName();
                 logger.info("Public DNS Name" + pubDnsName);
                 // get public IP address of the instance
                 logger.info("IP Address of the instance" + runningInstance.getPublicIpAddress());
                 logger.info("ID of instance" + runningId);
 
                 // get status of the created instance
                 DescribeInstanceStatusRequest describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(runningId);
                 DescribeInstanceStatusResult describeInstanceResult = ec2client.describeInstanceStatus(describeInstanceRequest);
                 List<InstanceStatus> state = describeInstanceResult.getInstanceStatuses();
                 while (state.size() < 1) {
                     // Do nothing, just wait, have thread sleep if needed
                     describeInstanceResult = ec2client.describeInstanceStatus(describeInstanceRequest);
                     state = describeInstanceResult.getInstanceStatuses();
                 }
 
                 status = state.get(0).getInstanceState().getName();
 
                 logger.info("Status = " + status);
             } while (!status.equals("running"));

             // Now the state of the instance is 'running', we use this instance to run the application
             request = new DescribeInstancesRequest().withInstanceIds(runningId);
             result = ec2client.describeInstances(request);
 
             reservations = result.getReservations();
 
             for (Reservation reservation : reservations) {
                 List<Instance> instances = reservation.getInstances();
                 for (Instance instance : instances) {
                     pubDnsName = instance.getPublicDnsName();
                     logger.info("DNS " + pubDnsName);
                 }
             }
             //pending, running, shutting-down, terminated, stopping, stopped
             logger.info("Status of the instance = " + status);
 
             //RETRIEVE A LIST OF ALL FILES
         } catch (AmazonClientException | InterruptedException e) {
             logger.info(e.getMessage());
             logger.info("Exiting the method");
             return -1;
         }
         logger.info("Exiting the method");
         return 0;
     }
 
     public static int fileUpload(String fileToUploadPath, String pemFilePath, String userName) {
         String command = null;
         logger.info("Entering the method");
 
         try {
             // ssh to the Amazon ec2 instance
             sshToCloud(pemFilePath);
 
             // create directory for each user, and then store it into the directory
             logger.info("Creating a directory");
             command = "mkdir -p " + userName;
             // execute the above command as if in the system terminal
             CommandExecutor.executeInTerminalCommandAndExit(command);
 
             // upload the file on the cloud, in the directory created for each user, using the scp command
             command = "scp -i " + pemFilePath + " " + fileToUploadPath + " " + PORPropertyConfigurator.instance_user_name + "@" + pubDnsName + ":~" + "/" + userName;
             CommandExecutor.executeCommandAndExit(command);
 
         } catch (IOException | InterruptedException ioe) {
             logger.info(ioe.getMessage());
             logger.info("Exiting the method");
             return -1;
         }
         logger.info("Exiting the method");
         return 0;
     }
 
     public static int fileDownload(String userName, String pemFilePath, String filNameToBeDownloaded, String saveTo) {
         String command = null;
         logger.info("Entering the method");
 
         try {
             // download the file from the cloud onto the local system, using the scp command
             command = "scp -i " + pemFilePath + " " + PORPropertyConfigurator.instance_user_name + "@" + pubDnsName + ":~/" + userName + "/" + filNameToBeDownloaded + " " + saveTo + "/";
             CommandExecutor.executeCommandAndExit(command);
 
         } catch (InterruptedException | IOException ioe) {
             logger.info(ioe.getMessage());
             logger.info("Exiting the method");
             return -1;
         }
         logger.info("Exiting the method");
         return 0;
     }
 
     public static int verify(String pemFilePath, String fileName, String userName) {
 
         String command;
         logger.info("In the start of verify");
         try {
             // upload the porserver executable onto the cloud to execute it on the cloud
             command = "scp -i " + pemFilePath + " " + PORPropertyConfigurator.executable_path + PORPropertyConfigurator.por_server_executable + " " + PORPropertyConfigurator.instance_user_name + "@" + pubDnsName + ":~";
             CommandExecutor.executeCommandAndExit(command);
 
             sshToCloud(pemFilePath);
             try {
                 // kill all other processes running the porserver on the cloud
                 command = "pkill -9 -f porserver";
                 CommandExecutor.executeInTerminalCommand(command);
 
                 // run the porserver executable uploaded on the cloud
                 command = "./porserver " + "~/" + userName + "/" + fileName + " &";
                 CommandExecutor.executeInTerminalCommandAndExit(command);
 
             } catch (IOException | InterruptedException ioe) {
                 logger.info(ioe.getMessage());
                 logger.info("Exiting the method");
                 return -1;
             }
 
         } catch (IOException | InterruptedException ioe) {
             logger.info(ioe.getMessage());
             logger.info("Exiting the method");
             return -1;
         }
         logger.info("Exiting the method");
         return 0;
     }
 
     public static void sshToCloud(String pemFilePath) throws IOException, InterruptedException {
 
         logger.info("Entering the method");
 
         String command = null;
         Runtime rt = Runtime.getRuntime();
 
         // kill all processes running the ssh command on the cloud
         CommandExecutor.executeCommandAndExit("pkill -9 -f ssh");
 
         // ssh to the cloud instance which is running
         command = "ssh -t -t -i " + pemFilePath + " " + PORPropertyConfigurator.instance_user_name + "@" + pubDnsName + " \'/bin/bash\'";
 
         logger.info("Command to be executed = " + command);
         prcs = rt.exec(command);
 
         logger.info("Exiting the method");
 
     }
 
     public static ArrayList<String> getFileList(String pemFilePath, String userName) throws IOException, InterruptedException {
 
         String line = null;
         ArrayList<String> list = new ArrayList<>();
         int exitVal = 0;
 
         logger.info("In the start of getFileList");
 
         // SSH to the cloud instance which is running
         sshToCloud(pemFilePath);
 
         // list all files present in the directory of the user, on the cloud
         String command = "cd " + userName + "; ls";
         logger.info(command);
 
         PrintWriter out = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(CommandExecutor.prcs.getOutputStream())), true);
         BufferedReader stdInput = new BufferedReader(new InputStreamReader(CommandExecutor.prcs.getInputStream()));
         BufferedReader stdError = new BufferedReader(new InputStreamReader(CommandExecutor.prcs.getErrorStream()));
 
         out.println(command);
         out.println("exit");
 
         logger.info("Output of the process - get file list");
         while ((line = stdError.readLine()) != null) {
             logger.info(line);
         }
 
         while ((line = stdInput.readLine()) != null) {
             logger.info(line);
 
             ////////////////////////////////////////////////
             if (line.endsWith("ls")) {
                 while (!(line = stdInput.readLine()).endsWith("exit")) {
                     if (line.contains("No such file or directory")) {
                         throw new RuntimeException("No such file or directory. Please enter user name again");
                     }
                     logger.info(line);
                     String[] lines = line.split("\\s+");
                     list.addAll(Arrays.asList(lines));
                 }
             }
 
         }
 
         exitVal = CommandExecutor.prcs.waitFor();
         logger.info("Process exitValue: " + exitVal);
         CommandExecutor.prcs.destroy();
 
         logger.info("List of files" + list.toString());
         logger.info("Exiting the method");
         return list;
     }
 
     public static void stopInstance() throws InterruptedException {
         // Create request to stop the instance which has been used to run the application, before exiting the application, so that you will not be charged for the instance while it is stopped
         StopInstancesRequest stopInstancesRequest = new StopInstancesRequest();
         List<String> instanceList = new ArrayList<String>();
         // Add the instance to a list, as required by the method
         instanceList.add(running_instance.getInstanceId());
         // stop the instance in the list
         stopInstancesRequest.setInstanceIds(instanceList);
         ec2client.stopInstances(stopInstancesRequest);
         String instanceState = "";
         do {
             logger.info("Instance Status = " + running_instance.getState().getName());
             // wait while the instance is stopping
             Thread.sleep(5 * 1000);
             // get a list of all stopped instances, to determine if the previously 'running' instance has been stopped
             DescribeInstancesRequest dis = new DescribeInstancesRequest();
             ArrayList<String> runningInstanceList = new ArrayList<String>();
             runningInstanceList.add(running_instance.getInstanceId());
             dis.setInstanceIds(runningInstanceList);
             DescribeInstancesResult disresult = ec2client.describeInstances(dis);
             List<Reservation> list = disresult.getReservations();
 
             System.out.println("-------------- status of instances -------------");
             for (Reservation res : list) {
                 List<Instance> instancelist = res.getInstances();
 
                 for (Instance instance : instancelist) {
 
                     logger.info("Instance Status : " + instance.getState().getName());
                     List<Tag> t1 = instance.getTags();
                     for (Tag teg : t1) {
                         logger.info("Instance Name   : " + teg.getValue());
                     }
                     instanceState = instance.getState().getName();
                     logger.info("Instance State = " + instanceState);
                 }
             }
         } while (!instanceState.equals("stopped")); // get the status of the running instance, while it is not stopped completely.
         logger.info("Exiting the method");
     }
 }
