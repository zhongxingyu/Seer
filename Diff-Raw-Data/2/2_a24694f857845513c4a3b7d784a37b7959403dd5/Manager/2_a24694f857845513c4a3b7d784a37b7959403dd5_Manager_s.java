 package com.dsp.ass1;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Logger;
 
 import org.apache.commons.io.FilenameUtils;
 
 import com.amazonaws.auth.AWSCredentials;
 
 import com.amazonaws.services.ec2.AmazonEC2;
 import com.amazonaws.services.ec2.AmazonEC2Client;
 
 import com.amazonaws.services.s3.AmazonS3;
 import com.amazonaws.services.s3.AmazonS3Client;
 
 import com.amazonaws.services.sqs.AmazonSQS;
 import com.amazonaws.services.sqs.AmazonSQSClient;
 import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
 import com.amazonaws.services.sqs.model.Message;
 import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
 
 
 public class Manager {
     private static final Logger logger = Utils.setLogger(Logger.getLogger(Manager.class.getName()));
     private static Map <String, MissionData> missions = new HashMap <String, MissionData>();
     private static int workersShuttingDown = 0;  // Amount of workers about to shut down.
     private static int tasksPerWorker = 10;  // Default value, can be overriden by argument.
     private static List<String> workerIds = new ArrayList<String>();
 
 
     // get the approximate number of the messages from the queue with the "url".
     private static int getNumberOfMessages(AmazonSQS sqs, String url) {
         List <String> attributeNames = new ArrayList<String>();
         attributeNames.add("ApproximateNumberOfMessages");
         GetQueueAttributesResult res = sqs.getQueueAttributes(url, attributeNames);
         return Integer.parseInt(res.getAttributes().get("ApproximateNumberOfMessages"));
     }
 
 
     // Polls EC2 service for instances tagged by 'Name=worker',
     // and returns worker ID list.
     private static List<String> getWorkerIds(AmazonEC2 ec2) {
         return Utils.getInstanceIdsByTag(ec2, "Name", "worker");
     }
 
 
     // Launch/terminate workers according to workload (task queue size).
     private static void balanceWorkers(AmazonEC2 ec2, AmazonSQS sqs) {
         int tasksNum = getNumberOfMessages(sqs, Utils.tasksUrl),
             workers = getWorkerIds(ec2).size(),
             delta = ((int) Math.ceil((float)tasksNum / (float)tasksPerWorker)) - (workers - workersShuttingDown);
 
         logger.info("workers/shutting/tasks/delta: " + workers + "/" + workersShuttingDown + "/" + tasksNum + "/" + delta);
 
         // If we need more workers, launch worker instances and remember their IDs.
         if (delta > 0) {
             List<String> launched = Utils.createAmiFromSnapshot(ec2, delta, Utils.elementUserData("worker"));
 
             // Tag worker instances with 'worker' tag.
             for (String id : launched) {
                 Utils.nameInstance(ec2, id, "worker");
             }
 
         // If we have too many workers, shut down unnecessary ones.
         } else if (delta < 0) {
             Utils.sendMessage(sqs, Utils.shutdownUrl, "shutdown");
             workersShuttingDown -= delta;  // delta < 0 so we actually do addition.
         }
     }
 
 
     // Terminates a worker instance by given instance ID.
     private static void terminateWorker(AmazonEC2 ec2, AmazonSQS sqs, Message msg) {
         String body = msg.getBody();
         if (body == null) {
             logger.severe("Error in message received, body is null.");
             return;
         }
 
         String[] closed = body.split("\t");
         if (closed.length < 2 || ! closed[0].equals("closed")) {
             logger.severe("Bad message received: " + body);
             return;
         }
 
         ArrayList<String> ids = new ArrayList<String>();
         ids.add(closed[1]);
         Utils.terminateInstances(ec2, ids);
 
         // One worker instance is no longer 'shutting down', but completely terminated.
         workersShuttingDown --;
 
         // Clean 'closed' message from queue.
         Utils.deleteTaskMessage(msg, Utils.closedWorkersUrl, sqs);
     }
 
 
     // Handle a finished mission (a mission whose tasks are all finished):
     // Upload result file to S3 and inform local.
     private static void finishMission(AmazonSQS sqs, AmazonS3 s3, String missionNumber, MissionData data) {
         logger.info("Finishing mission: " + missionNumber);
 
         String link = Utils.uploadFileToS3(s3, missionNumber + "_results.txt", Utils.resultPath, data.getInfo());
 
         String msg;
         if (link == null) {
             msg = "failed task\terror uploading finish results.txt\t";
         } else {
             msg = "done task\t" + link + "\t" + missionNumber;
         }
 
         logger.info(msg);
         Utils.sendMessage(sqs, Utils.localDownUrl, msg);
         missions.remove(missionNumber);
     }
 
 
     // Handle new worker finish task: Update mission data,
     // and finish its mission if this was its last task.
     private static void finishTask(Message msg, AmazonSQS sqs, AmazonS3 s3) {
         Utils.deleteTaskMessage(msg, Utils.finishedUrl, sqs);
 
         String body = msg.getBody();
         if (body == null) {
             logger.severe("Error in message received, body is null.");
             return;
         }
 
         logger.info("Finish message: " + msg.getBody());
 
         // action = split[1], input = split[2], output = split[3], missionNumber = split[4];
         String[] split = msg.getBody().split("\t");
         if (split.length < 5) {
             logger.severe("Finished task message is too short < 5.");
             return;
         }
 
         String action = split[0],
                missionNumber = split[4];
         MissionData data = missions.get(missionNumber);
 
         if (action.equals("done PDF task") || action.equals("failed PDF task")) {
             data.appendTask(split);
         } else {
             logger.severe("Received unknown message.");
             return;
         }
 
         // Decrease remaining tasks and finish mission if all its tasks were finished.
         if (--data.remaining == 0) {
             finishMission(sqs, s3, missionNumber, data);
         } else {
             missions.put(missionNumber, data);
         }
     }
 
 
     // Receives an S3 mission link i.e. 'http://xxx/mission-id_input.txt'
     // and returns 'mission-id'.
     private static String getMissionNumber(String link) {
         return FilenameUtils.getBaseName(link).split("_")[0];
     }
 
 
     // Process a new local task message, by adding PDF tasks to worker queue,
     // and launching/terminating worker instances as necessarProcess a new
     // local task message, by adding PDF tasks to worker queue.
     private static void handleNewMission(String link, AmazonSQS sqs) {
         logger.info("handling new task: " + link);
 
         String info = Utils.LinkToString(link),
                missionNumber = getMissionNumber(link),
                msg;
 
         // Don't do anything if any error occured.
         if (info == null) {
             Utils.sendMessage(sqs, Utils.localDownUrl, "failed task\tcan't open URL\t" + missionNumber);
             return;
         }
 
         String[] tasks = info.split("\n");
 
         // Add PDF tasks to queue.
         logger.info("Adding new mission's PDF tasks to worker queue.");
         for (int i = 0; i < tasks.length; i++) {
             msg = "new PDF task\t" + tasks[i] + "\t" + missionNumber ;
             Utils.sendMessage(sqs, Utils.tasksUrl, msg);
         }
 
         // Create new missions data object.
         MissionData data = new MissionData(tasks.length);
         missions.put(missionNumber, data);
     }
 
 
     // Creates a new mission (received from local) or shuts down.
     // Returns 'true' if needs to shutdown afterwards, 'false' otherwise.
     private static boolean handleLocalMessage(Message msg, AmazonSQS sqs) {
         // Delete processed message.
         Utils.deleteTaskMessage(msg, Utils.localUpUrl, sqs);
 
         String body = msg.getBody();
         if (body == null) {
             logger.severe("Error in message received, body is null.");
             return false;
         }
 
         logger.info("Received: " + body);
 
 
         // Shutdown if 'shutdown' message received.
         if (body.equals("shutdown")) {
             return true;
         }
 
         // Else process new mission.
         String[] mission = body.split("\t");
         if (mission.length >= 2 && mission[0].equals("new task")) {
             handleNewMission(mission[1], sqs);  // mission[1] is a link.
         } else {
             logger.info("Ignoring: " + msg.getBody());
         }
 
         return false;
     }
 
 
     private static void execute(AmazonEC2 ec2, AmazonSQS sqs, AmazonS3 s3) {
         boolean shutdown = false;
         List<Message> localUpMsgs, finishedMsgs, closedMsgs;
         Message msg;
         ReceiveMessageRequest rcvLocalUp = new ReceiveMessageRequest(Utils.localUpUrl),
                               rcvFinished = new ReceiveMessageRequest(Utils.finishedUrl),
                               rcvClosed = new ReceiveMessageRequest(Utils.closedWorkersUrl);
 
         localUpMsgs = Utils.getMessages(rcvLocalUp, sqs);
         finishedMsgs = Utils.getMessages(rcvFinished, sqs);
         closedMsgs = Utils.getMessages(rcvClosed, sqs);
 
         // Process new missions as long as there are any missions left,
         // not all workers have been terminated (not just closed),
         // and shutdown flag is OFF.
         while ( ! shutdown || missions.size() > 0 || workerIds.size() > 0) {
 
             // Sleep if no messages were received.
             // Don't accept new missions if shutdown flag is ON.
             if (Utils.isEmpty(finishedMsgs)
                     && Utils.isEmpty(closedMsgs)
                     && (shutdown || Utils.isEmpty(localUpMsgs))) {
 
                 logger.fine("no messages, sleeping.");
 
                 try {
                     TimeUnit.SECONDS.sleep(30);
                 } catch(InterruptedException e) {
                     logger.severe(e.getMessage());
                     return;
                 }
             } else {
                 // Process new mission, if shutdown is OFF.
                 if ( ! shutdown && ! Utils.isEmpty(localUpMsgs)) {
                     msg = localUpMsgs.get(0);
                     shutdown = handleLocalMessage(msg, sqs);
                     if (shutdown) {
                         logger.info("Shutting down.");
                     }
                 }
 
                 // Process new worker finished task.
                 if ( ! Utils.isEmpty(finishedMsgs)){
                     msg = finishedMsgs.get(0);
                     finishTask(msg, sqs, s3);
                 }
 
                 // Process new 'worker closed' message (terminate instance).
                 if ( ! Utils.isEmpty(closedMsgs)){
                     msg = closedMsgs.get(0);
                     terminateWorker(ec2, sqs, msg);
                 }
             }
 
             // Launch/terminate workers by workload (tasks queue size).
             balanceWorkers(ec2, sqs);
 
             // Fetch more local missions if shutdown flag is OFF.
             if ( ! shutdown) {
                 localUpMsgs = Utils.getMessages(rcvLocalUp, sqs);
             }
 
             // Fetch remaining worker finish tasks and close messages.
             finishedMsgs = Utils.getMessages(rcvFinished, sqs);
             closedMsgs = Utils.getMessages(rcvClosed, sqs);
         }
 
         logger.info("No more missions, tasks, or workers.");
 
         logger.info("Shutting down.");
        Utils.sendMessage(sqs, Utils.closedWorkersUrl, "closed");  // Locals already know manager's instance ID.
     }
 
 
     // TODO set tasks/worker ratio according to the local who launched the manager instance.
     public static void main(String[] args) {
         logger.info("Starting.");
 
         // Read tasks per worker if given.
         if (args.length >= 1) {
             tasksPerWorker = Integer.parseInt(args[0]);
             logger.info("Set tasks/worker value: " + tasksPerWorker);
         }
 
         AWSCredentials creds = Utils.loadCredentials();
         if (creds == null) {
             logger.severe("Couldn't load credentials.");
             // TODO close myself
             return;
         }
 
         // Start EC2, S3 and SQS connections.
         AmazonEC2 ec2 = new AmazonEC2Client(creds);
         AmazonSQS sqs = new AmazonSQSClient(creds);
         AmazonS3 s3 = new AmazonS3Client(creds);
 
         execute(ec2, sqs, s3);
     }
 }
