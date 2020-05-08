 package com.dsp.ass1;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 
 import java.net.MalformedURLException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import java.util.logging.Logger;
 import java.util.logging.ConsoleHandler;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.codec.binary.Base64;
 
 import com.amazonaws.AmazonClientException;
 
 import com.amazonaws.auth.PropertiesCredentials;
 
 import com.amazonaws.services.ec2.AmazonEC2;
 
 import com.amazonaws.services.ec2.model.Instance;
 import com.amazonaws.services.ec2.model.Tag;
 import com.amazonaws.services.ec2.model.Filter;
 import com.amazonaws.services.ec2.model.Reservation;
 import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
 import com.amazonaws.services.ec2.model.DescribeInstancesResult;
 import com.amazonaws.services.ec2.model.RunInstancesResult;
 import com.amazonaws.services.ec2.model.RunInstancesRequest;
 import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
 import com.amazonaws.services.ec2.model.InstanceStateChange;
 import com.amazonaws.services.ec2.model.CreateTagsRequest;
 
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.model.CannedAccessControlList;
 import com.amazonaws.services.s3.model.PutObjectRequest;
 
 import com.amazonaws.services.sqs.AmazonSQS;
 import com.amazonaws.services.sqs.model.DeleteMessageRequest;
 import com.amazonaws.services.sqs.model.Message;
 import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
 import com.amazonaws.services.sqs.model.SendMessageRequest;
 
 
 public class Utils {
     public static final String tasksUrl = "https://sqs.us-east-1.amazonaws.com/340657073537/tasks",
             instanceType = "t1.micro",
             imageId = "ami-cfc0dba6",
             keyName = "ec2",
             securityGroup = "launch-wizard-2",
             closedWorkersUrl = "https://sqs.us-east-1.amazonaws.com/340657073537/closedworkers",
             finishedUrl = "https://sqs.us-east-1.amazonaws.com/340657073537/finished",
             localUpUrl = "https://sqs.us-east-1.amazonaws.com/340657073537/localup",
             localDownUrl = "https://sqs.us-east-1.amazonaws.com/340657073537/localdown",
             shutdownUrl = "https://sqs.us-east-1.amazonaws.com/340657073537/shutdown",
             bucket = "dsp-ass1",
             instanceIdUrl = "http://169.254.169.254/latest/meta-data/instance-id",
             resultPath = "results/",
             inputsPath = "inputs/",
             filesPath = "files/";
 
     private static final Logger logger = setLogger(Logger.getLogger(Utils.class.getName()));
 
 
     // Use custom string format for logger.
     public static Logger setLogger(Logger logger) {
         ShortFormatter formatter = new ShortFormatter();
         ConsoleHandler handler = new ConsoleHandler();
 
         logger.setUseParentHandlers(false);
         handler.setFormatter(formatter);
         logger.addHandler(handler);
 
         return logger;
     }
 
 
     public static boolean checkResult(String result) {
         return (result != null && result.startsWith("http"));
     }
 
 
     // Returns uploaded file address or null on error.
     public static String uploadFileToS3(AmazonS3 s3, String fileName, String path ,String info) {
         String fileAddress = getS3FileAddress(s3, fileName , path);
         File file = createSampleFile(info);
 
         if (file == null || fileAddress == null) {
             return null;
         } else {
             PutObjectRequest request = new PutObjectRequest(bucket, path + fileName, file);
 
             try {
                 s3.putObject(request.withCannedAcl(CannedAccessControlList.PublicRead));
                 logger.info("File saved: " + fileAddress);
             } catch (AmazonClientException e) {
                 logger.severe(e.getMessage());
                 return null;
             }
 
             return fileAddress;
         }
     }
 
     // TODO Write to stream and then save it to file,
     // INSTEAD of creating a file and writing to it. >:(
     private static File createSampleFile(String info) {
         File file;
 
         try {
             file = File.createTempFile("aws-java-sdk-", ".txt");
         } catch (IOException e) {
             logger.severe(e.getMessage());
             return null;
         }
 
         file.deleteOnExit();
 
         Writer writer;
         try {
             writer = new OutputStreamWriter(new FileOutputStream(file));
             writer.write(info);
         } catch (IOException e) {
             logger.severe(e.getMessage());
             return null;
         }
 
         try {
             writer.close();
         } catch (IOException e) {
             logger.severe(e.getMessage());
         }
 
         return file;
     }
 
 
     // Fetches S3-file url.
     public static String getS3FileAddress(AmazonS3 s3, String fileName, String path) {
         String address;
 
         try {
             address = s3.getBucketLocation(bucket);
         } catch (AmazonClientException e) {
             logger.severe(e.getMessage());
             return null;
         }
 
         return "https://s3-" + address + ".amazonaws.com/" + bucket + "/" + path + fileName;
     }
 
 
     public static PropertiesCredentials loadCredentials() {
         try {
             return new PropertiesCredentials(
                     Utils.class.getResourceAsStream("/AWSCredentials.properties"));
         } catch (IOException e) {
             logger.severe(e.getMessage());
             return null;
         }
     }
 
 
     public static void sendMessage(AmazonSQS sqs, String sqsUrl, String info) {
         try {
             sqs.sendMessage(new SendMessageRequest(sqsUrl, info));
             logger.info("Sent to queue: " + info);
         } catch (AmazonClientException e) {
             logger.severe(e.getMessage());
         }
     }
 
 
     public static void deleteTaskMessage(Message msg, String sqsUrl, AmazonSQS sqs) {
         String handle = msg.getReceiptHandle();
 
         try {
             sqs.deleteMessage(new DeleteMessageRequest(sqsUrl, handle));
             logger.info("Deleted: " + msg.getBody());
         } catch (AmazonClientException e) {
             logger.severe(e.getMessage());
         }
     }
 
 
     public static List<Message> getMessages(ReceiveMessageRequest req, AmazonSQS sqs) {
         List<Message> msgs;
 
         try {
             msgs = sqs.receiveMessage(req).getMessages();
         } catch (AmazonClientException e) {
             logger.severe(e.getMessage());
             return null;
         }
 
         return msgs;
     }
 
 
     // Join string collection with delimeter.
     private static String join(Collection<String> s, String delimiter) {
         StringBuilder builder = new StringBuilder();
         Iterator<String> iter = s.iterator();
 
         while (iter.hasNext()) {
             builder.append(iter.next());
             if (!iter.hasNext()) {
                 break;
             }
 
             builder.append(delimiter);
         }
 
         return builder.toString();
     }
 
 
     // Execute worker/manager with default user-data, to be sent using an instance request.
     public static String elementUserData(String element) {
         ArrayList<String> lines = new ArrayList<String>();
         lines.add("#!/bin/sh");
        lines.add("cd /home/ec2-user/dsp/ass1 && git pull origin master && source ./.bash_profile && mvn clean compile && ./run-" + element + " >> output.log 2>&1");
         return new String(Base64.encodeBase64(join(lines, "\n").getBytes()));
     }
 
 
     // Creates new AMI instances from pre-defined snapshot,
     // and returns launched instance IDs.
     public static List<String> createAmiFromSnapshot(AmazonEC2 ec2, int amount, String userData) {
         logger.info("Launching " + amount + " instances.");
 
         ArrayList<String> securityGroups = new ArrayList<String>();
         securityGroups.add(securityGroup);
 
         RunInstancesRequest request = new RunInstancesRequest();
 
         // Send launch request.
         request.withImageId(imageId)  // Utils.imageId
             .withInstanceType(instanceType)
             .withUserData(userData)
             .withKeyName(keyName)
             .withSecurityGroups(securityGroups)
             .withMinCount(amount)
             .withMaxCount(amount);
 
         // Build instance id list.
         ArrayList<String> ids = new ArrayList<String>();
         for (Instance instance : ec2.runInstances(request).getReservation().getInstances()) {
             ids.add(instance.getInstanceId());
         }
 
         return ids;
     }
 
 
     // Tags an instance with "Name='name'".
     public static void nameInstance(AmazonEC2 ec2, String id, String name) {
         logger.info("Tagging instance " + id + ": 'Name=" + name + "'");
 
         CreateTagsRequest tagReq = new CreateTagsRequest();
         tagReq.withResources(id).withTags(new Tag("Name", name));
 
         ec2.createTags(tagReq);
     }
 
 
     // Terminates an instances and returns a list of terminated instance ids.
     public static List<String> terminateInstances(AmazonEC2 ec2, Collection<String> ids) {
         for (String id : ids) {
             logger.info("Terminating instance id: " + id);
         }
 
         TerminateInstancesRequest request = new TerminateInstancesRequest();
         request.setInstanceIds(ids);
 
         ArrayList<String> terminated = new ArrayList<String>();
         for (InstanceStateChange state : ec2.terminateInstances(request).getTerminatingInstances()) {
             terminated.add(state.getInstanceId());
         }
 
         return terminated;
     }
 
 
     // Returns pending/running instance ID list.
     public static ArrayList<String> getInstanceIdsByTag(AmazonEC2 ec2, String key, String value) {
         logger.info("Requesting instances by tag " + key + "=" + value);
 
         // Create tag request.
         DescribeInstancesRequest instanceReq = new DescribeInstancesRequest();
         Filter managerFilter = new Filter("tag:" + key).withValues(value),
                 activeFilter = new Filter("instance-state-code").withValues("0", "16");  // 0||16 == pending||running
 
         // Send request.
         DescribeInstancesResult result = ec2.describeInstances(instanceReq.withFilters(managerFilter, activeFilter));
 
         // Build instance ID list.
         ArrayList<String> ids = new ArrayList<String>();
         for (Reservation reservation : result.getReservations()) {
             for (Instance instance : reservation.getInstances()) {
                 ids.add(instance.getInstanceId());
             }
         }
 
         logger.info(ids.size() + " instances found with tag " + key + "=" + value);
         return ids;
     }
 
 
     public static String LinkToString(String link) {
         URL url;
         String line;
         StringBuilder content = new StringBuilder();
         InputStream is;
         BufferedReader br;
 
         try {
             url = new URL(link);
         } catch (MalformedURLException e) {
             logger.severe(e.getMessage());
             return null;
         }
 
         try {
             is = url.openStream();
         } catch (IOException e) {
             logger.severe(e.getMessage());
             return null;
         }
 
         br = new BufferedReader(new InputStreamReader(is));
 
         try {
             while ( (line = br.readLine()) != null) {
                 content.append(line);
                 content.append("\n");
             }
         } catch (IOException e) {
             logger.severe(e.getMessage());
             return null;
         }
 
         try {
             br.close();
         } catch (IOException e) {
             logger.severe(e.getMessage());
         }
 
         try {
             is.close();
         } catch (IOException e) {
             logger.severe(e.getMessage());
         }
 
         return content.toString();
     }
 
 
     // checking if a list has is empty.
     public static boolean isEmpty(List<Message> messages) {
         return (messages == null || messages.size() == 0);
     }
 
 
     // Sends an HTTP GET request to URL and returns it's response.
     public static String getHTML(String urlToRead) {
         URL url;
         HttpURLConnection conn;
         BufferedReader rd;
         String line,
                result = "";
 
         try {
             url = new URL(urlToRead);
             conn = (HttpURLConnection) url.openConnection();
             conn.setRequestMethod("GET");
             rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             while ((line = rd.readLine()) != null) {
                 result += line;
             }
             rd.close();
         } catch (IOException e) {
             logger.severe(e.getMessage());
             // An empty string will be returned.
         }
 
         return result;
     }
 }
