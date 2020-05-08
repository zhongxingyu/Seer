 package com.dsp.ass1;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Logger;
 
 import com.amazonaws.auth.AWSCredentials;
 
 import com.amazonaws.services.ec2.AmazonEC2;
 import com.amazonaws.services.ec2.AmazonEC2Client;
 
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 
 import com.amazonaws.services.sqs.AmazonSQS;
 import com.amazonaws.services.sqs.AmazonSQSClient;
 import com.amazonaws.services.sqs.model.Message;
 import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
 
 
 public class LocalApplication {
 
     private static final Logger logger = Utils.setLogger(Logger.getLogger(LocalApplication.class.getName()));
     private static int tasksPerWorker = 10;
 
     // Writes data to file.
     private static void WriteToFile(String fileName, String data) {
         PrintWriter out;
 
         try {
             out = new PrintWriter(fileName);
         } catch (FileNotFoundException e) {
             logger.severe(e.getMessage());
             return;
         }
 
         out.println(data);
         out.close();
         logger.info("Results: " + fileName);
     }
 
 
     // Wraps a string with <html> stuff.
     private static String StringToHTMLString(String info) {
         StringBuilder content = new StringBuilder();
 
         content.append("<!DOCTYPE html>\n");
         content.append("<html>\n");
         content.append("<head>\n");
         content.append("<title>Results</title>\n");
         content.append("</head>\n");
         content.append("<body>\n");
 
         String[] split = info.split("\n");
         for (int i=0 ; i<split.length; i++) {
             content.append(split[i]);
             content.append("\n<br>\n");
         }
 
         content.append("</body>\n");
         content.append("</html>\n");
         return content.toString();
     }
 
 
     // Reads a file and returns its content as a string.
     private static String readFileAsString(String filePath) {
         StringBuffer fileData = new StringBuffer();
         BufferedReader reader;
         char[] buf = new char[1024];
         int numRead = 0;
 
         try {
             reader = new BufferedReader(new FileReader(filePath));
         }
         catch (FileNotFoundException e) {
             logger.severe(e.getMessage());
             return null;
         }
 
         try {
             while((numRead = reader.read(buf)) != -1) {
                 String readData = String.valueOf(buf, 0, numRead);
                 fileData.append(readData);
             }
         } catch (IOException e) {
             logger.severe(e.getMessage());
             return null;
         }
 
         try {
             reader.close();
         } catch (IOException e) {
             logger.severe(e.getMessage());
         }
 
         return fileData.toString();
     }
 
 
     // Process (or ignores) all messages.
     private static String handleMessage(Message msg, AmazonS3 s3, String missionNumber) {
         String body = msg.getBody();
 
         if (body == null) {
             logger.severe("Error in message received, body is null.");
             return null;
         }
 
         logger.info("Received: " + body);
 
         String[] parts = msg.getBody().split("\t");
 
         if ( ! parts[0].equals("closed")
                 || parts.length <= 2
                 || ! parts[2].equals(missionNumber)  // If we reached here then length >= 3
                 || (! parts[0].equals("done task") && ! parts[0].equals("failed task"))) {
 
             logger.info("Ignoring: " + body);
             return null;
         }
 
         return parts[1];
     }
 
 
     // Returns existing manager instance ID or null if no manager exists.
     private static String getManager(AmazonEC2 ec2) {
         logger.info("Requesting manager instance.");
 
         ArrayList<String> ids = Utils.getInstanceIdsByTag(ec2, "Name", "manager");
         if (ids.size() > 0) {
             logger.info("Existing manager found.");
             return ids.get(0);
         } else {
             logger.info("No manager instance exists.");
             return null;
         }
     }
 
 
     // Creates a manager instance and returns its ID.
     private static String createManager(AmazonEC2 ec2, int tasksPerWorker) {
         logger.info("Creating new manager instance.");
 
        String userData = Utils.elementUserData("manager", ""+tasksPerWorker);
         List<String> ids = Utils.createAmiFromSnapshot(ec2, 1, userData);
         if (ids.size() != 1) {
             logger.severe("Couldn't create manager.");
             return null;
         } else {
             return ids.get(0);
         }
     }
 
 
     // Get manager if it exists, or create a new one if not,
     // and return its instance id, or null if an error occured.
     private static String getOrCreateManager(AmazonEC2 ec2, int tasksPerWorker) {
         // Search for existing manager.
         String id = getManager(ec2);
         if (id != null) {
             return id;
         }
 
         // Else create a new one.
         id = createManager(ec2, tasksPerWorker);
         if (id == null) {
             return null;
         }
 
         // Tag manager with "Name=manager".
         Utils.nameInstance(ec2, id, "manager");
 
         return id;
     }
 
 
     // Sends a 'shutdown' message to the manager.
     private static void shutdownManager(AmazonSQS sqs, String managerId) {
         logger.info("Shutting down manager.");
         Utils.sendMessage(sqs, Utils.localUpUrl, "shutdown");
 
     }
 
 
     private static void execute(AmazonEC2 ec2, AmazonS3 s3, AmazonSQS sqs, String mission, String managerId, boolean terminateManager) {
         // Upload new mission and inform manager.
         String missionNumber = Long.toString(System.currentTimeMillis());
 
         String uploadLink = Utils.uploadFileToS3(s3, missionNumber + "_input.txt", Utils.inputsPath, mission),
                finishedLink = null;
 
         Utils.sendMessage(sqs, Utils.localUpUrl, "new task\t" + uploadLink);
 
         // Process messages indefinitely until manager is finished working for us.
 
         ReceiveMessageRequest req = new ReceiveMessageRequest(Utils.localDownUrl);
         List<Message> msgs = Utils.getMessages(req, sqs);
         Message msg;
 
         while (true) {
             // Sleep if no messages arrived, and retry to re-fetch new ones afterwards.
             if (msgs == null || msgs.size() == 0) {
                 logger.info("No messages, sleeping.");
 
                 try {
                     TimeUnit.SECONDS.sleep(5);
                 } catch(InterruptedException e) {
                     logger.severe(e.getMessage());
                     return;
                 }
 
             // Process top message in queue if there are any messages.
             } else if (finishedLink != null) {
                 msg = msgs.get(0);
                 finishedLink = handleMessage(msg, s3 , missionNumber);
 
                 // Proccess 'regular' message (not a 'manager closed' message).
                 if ( ! finishedLink.equals("closed")) {
                     // Only handled messages are deleted.
                     Utils.deleteTaskMessage(msg, Utils.localDownUrl, sqs);
 
                     // Exit loop if we don't need to terminateManager manager (given as argument on startup).
                     if (terminateManager) {
                         shutdownManager(sqs, managerId);
                     } else {
                         break;
                     }
                 // Terminate manager if we need to and if it shutdown properly.
                 } else {
                     if (terminateManager) {
                         // Only handled messages are deleted.
                         Utils.deleteTaskMessage(msg, Utils.localDownUrl, sqs);
 
                         ArrayList<String> ids = new ArrayList<String>();
                         ids.add(managerId);
                         Utils.terminateInstances(ec2, ids);
 
                         break;
                     }
                 }
             }
 
             // Read next messages in queue and repeat.
             msgs = Utils.getMessages(req, sqs);
         }
 
         // if the task failed
         if ( ! Utils.checkResult(finishedLink)){
             logger.severe("Mission failed: " + finishedLink);
             return;
         }
 
         // Create <html> summary file.
         String resultContent = Utils.LinkToString(finishedLink);
         if (resultContent == null) {
             return;
         }
 
         WriteToFile(missionNumber + "_results.html", StringToHTMLString(resultContent));
 
         logger.info("Shutting down.");
     }
 
 
     public static void main (String[] args) {
         logger.info("Starting.");
 
         // Exit if no arguments were given.
         if (args.length == 0) {
             logger.severe("no input: closing.");
             return;
         }
 
         // Read input.
         String mission = readFileAsString(args[0]);
         if (mission == null) {
             return;
         }
 
         // Load credentials.
         AWSCredentials creds = Utils.loadCredentials();
         if (creds == null) {
             logger.severe("Couldn't load credentials.");
             return;
         }
 
         // Read tasksPerWorker value if given.
         if (args.length > 1) {
             tasksPerWorker = Integer.parseInt(args[0]);
         }
 
         // Start ec2 connection and manager.
         AmazonEC2 ec2 = new AmazonEC2Client(creds);
         String managerId = getOrCreateManager(ec2, tasksPerWorker);
         if (managerId == null) {
             return;
         }
 
         // Start S3 and SQS connections.
         AmazonS3 s3 = Utils.createS3(creds);
         AmazonSQS sqs = new AmazonSQSClient(creds);
 
         boolean terminateManager = false;  // Tells manager to terminateManager after completing mission.
         if (args.length >= 2 && args[1].equals("shutdown")) {
             terminateManager = true;
         }
 
         execute(ec2, s3, sqs, mission, managerId, terminateManager);
     }
 }
