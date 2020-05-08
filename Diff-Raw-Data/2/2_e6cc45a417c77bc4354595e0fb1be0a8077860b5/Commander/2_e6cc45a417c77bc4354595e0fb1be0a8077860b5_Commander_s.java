 /*
  * Custom system to hit my EC2 instances with a large volume of randomly generated crashes.
  *
  * Hack not really worth signing yet.
  */
 
 package com.smartass.pewpew;
 
 import com.smartass.pewpew.generators.*;
 
 import java.io.*;
 import java.nio.*;
 import java.net.*;
 import java.util.*;
 import javax.net.ssl.*;
 
 import com.amazonaws.services.ec2.*;
 import com.amazonaws.services.ec2.model.*;
 import com.amazonaws.auth.*;
 import com.amazonaws.regions.*;
 
 import org.apache.commons.codec.binary.Base64;
 
 public class Commander {
 
     static String AMI_ID = "ami-281d8018";
     static boolean useEC2 = true;
     static enum Flag { NONE, INSTANCES, HITS, DELAY, TIMEOUT };
 
     public static void main(String[] args) {
 
         System.out.println();
         System.out.println("Welcome to PewPew Commander!");
         System.out.println("\"Your one stop shop for all your DDoS needs!\" (just kidding, use only for good)");
 
         System.out.println();
         System.out.println("Preparing written orders.");
         System.out.println();
 
         int instances = 1;
         int hits = 10;
         int delay = 100;
         int timeout = 500;
 
         Flag flag = Flag.NONE;
 
         for (String arg : args) {
             if (arg.equals("--instances") || arg.equals("-i")) {
                 flag = Flag.INSTANCES;
             }
             else if (arg.equals("--hits") || arg.equals("-h")) {
                 flag = Flag.HITS;
             }
             else if (arg.equals("--delay") || arg.equals("-d")) {
                 flag = Flag.DELAY;
             }
             else if (arg.equals("--timeout") || arg.equals("-t")) {
                 flag = Flag.TIMEOUT;
             }
             else if (arg.equals("--help") || arg.equals("-h")) {
                 printHelp();
                 return;
             }
             else {
                 int input = 0;
                 try {
                     input = Integer.parseInt(arg);
                 }
                 catch (Exception e) {
                     System.out.println("Couldn't read integer input. No fractions or decimals are allowed.");
                     printHelp();
                     return;
                 }
                 if (flag == Flag.INSTANCES) {
                     instances = input;
                 }
                 else if (flag == Flag.HITS) {
                     hits = input;
                 }
                 else if (flag == Flag.DELAY) {
                     delay = input;
                 }
                 else if (flag == Flag.TIMEOUT) {
                    hits = input;
                 }
                 else {
                     System.out.println("Command not recognized : "+arg);
                     printHelp();
                     return;
                 }
                 flag = Flag.NONE;
             }
         }
 
         // Here's a config for testing the crash reporter
 
         TestConfig config = new TestConfig();
         config.url = "amazonloadbalancer-1358579984.us-west-2.elb.amazonaws.com/log/";
         config.expectedResponse = "your concerns will be noted in the captains log ;)";
         config.numHits = hits;
         config.msDelay = delay;
         config.msTimeout = timeout;
         config.urlParamGenerator = new CrashTestParameterGenerator();
 
         System.out.println("Planning to hit \""+config.url+"\" from "+instances+" instances, with "+hits+" hits each, at a "+delay+" ms delay, accepting a max lag per request of "+timeout+" ms.");
 
         ArrayList<String> cannons = new ArrayList<String>();
         ArrayList<String> instanceIds = new ArrayList<String>();
         AmazonEC2 ec2 = null;
         cannons.add("localhost");
 
         if (useEC2) {
 
             ec2 = createEC2();
 
             // Create the instances
 
             instanceIds = createEC2Instances(ec2, instances);
 
             // Wait for instances to start up
 
             cannons = instanceIdsToDNS(ec2, instanceIds);
 
         }
 
         // Wait for our sockets
 
         ArrayList<Socket> sockets = waitForSockets(cannons);
 
         // Tell instances to attack
 
         ArrayList<TestResult> results = new ArrayList<TestResult>();
 
         for (int i = 0; i < sockets.size()*2; i++) {
 
             // Blast from i instances, and get results
 
             long startTimeMS = System.currentTimeMillis();
             int testInstances = (int)Math.ceil(i/2)+1;
             TestResult result = runTest(sockets,config,testInstances);
             result.instances = testInstances;
             result.config = config;
             result.duration = System.currentTimeMillis() - startTimeMS;
             results.add(result);
         }
 
         System.out.println();
         System.out.println("==============\nTest Results\n==============");
 
         long perInstanceAvgResponseMs = 0;
         float avgHitsPerSecond = 0;
         for (int i = 0; i < results.size(); i++) {
             TestResult result = results.get(i);
             System.out.println("\n-------\nTest "+i+":\n-------");
             System.out.println("simultaneous instances: "+result.instances);
             System.out.println();
             System.out.println("instance config:");
             System.out.println("delay between hits: "+result.config.msDelay);
             System.out.println("timeout: "+result.config.msTimeout);
             System.out.println("consecutive hits: "+result.config.numHits);
             System.out.println("theorhetical max hits per second: "+((1000f/(float)result.config.msTimeout)*(float)result.instances));
             System.out.println();
             System.out.println("passed: "+result.passedTests);
             System.out.println("failed: "+result.failedTests);
             System.out.println("avg response time: "+result.msAverageResponse);
             System.out.println("avg response time per instance: "+(result.msAverageResponse/(long)result.instances));
             System.out.println("realized hits per second: "+(((float)result.passedTests/(float)result.duration)*1000f));
 
             perInstanceAvgResponseMs += (result.msAverageResponse/(long)result.instances);
             avgHitsPerSecond += (((float)result.passedTests/(float)result.duration)*1000f);
         }
 
         perInstanceAvgResponseMs /= results.size();
         avgHitsPerSecond /= results.size();
 
         System.out.println();
         System.out.println("****");
         System.out.println("Overall avg response time per instance : "+perInstanceAvgResponseMs);
         System.out.println("Overall avg hits per second : "+avgHitsPerSecond);
         System.out.println();
 
         // Terminate the instances
 
         if (useEC2) {
 
             terminateEC2Instances(ec2, instanceIds);
 
         }
     }
 
     private static void printHelp() {
         System.out.println("Options:");
         System.out.println("\t--instances, -i : Set the number of instances");
         System.out.println("\t--hits, -h : Set the number of REST calls each instance will perform");
         System.out.println("\t--delay, -d : Set the delay between each call, in ms");
         System.out.println("\t--timeout, -t : Set the timeout on each call, in ms");
     }
 
     private static AmazonEC2 createEC2() {
 
         System.out.println();
         System.out.println("Authenticating with the mercenaries.");
 
         AWSCredentials cred;
         try {
             cred = new PropertiesCredentials(new File("config.txt"));
         }
         catch (IOException e) {
             System.out.println("Can't find the autorization file.");
             return null;
         }
         AmazonEC2 ec2 = new AmazonEC2Client(cred);
 
         // It's very important to set the region correctly, or else nothing works
 
         ec2.setRegion(com.amazonaws.regions.Region.getRegion(Regions.US_WEST_2));
 
         return ec2;
     }
 
     // This creates a bunch of EC2 instances
 
     private static ArrayList<String> createEC2Instances(AmazonEC2 ec2, int numInstances) {
         String userData = "#! /bin/bash\n" +
                           "git clone https://github.com/keenon/pewpew.git /home/ec2-user/pewpew" + "\n" +
                           "java -cp /home/ec2-user/pewpew/repo/pewpew-0.1.jar com.smartass.pewpew.PewPew";
 
         System.out.println();
         System.out.println("Assembling the fleet.");
 
         DescribeInstancesResult liveStatus = ec2.describeInstances();
 
         RunInstancesResult result = ec2.runInstances(new RunInstancesRequest().withImageId(AMI_ID).withInstanceType("t1.micro").withKeyName("archkey").withSecurityGroups("default").withUserData(Base64.encodeBase64String(userData.getBytes())).withInstanceInitiatedShutdownBehavior("terminate").withMinCount(numInstances).withMaxCount(numInstances));
 
         Reservation reservation = result.getReservation();
 
         ArrayList<String> instanceIds = new ArrayList<String>();
 
         for (Instance i : reservation.getInstances()) {
             System.out.println(i.getInstanceId()+" answered our call.");
             instanceIds.add(i.getInstanceId());
         }
 
         return instanceIds;
     }
 
     // This turns ec2 instances into DNS names we can use to remote control the instances
 
     private static ArrayList<String> instanceIdsToDNS(AmazonEC2 ec2, ArrayList<String> instanceIds) {
 
         // Copy the array, because we're about to mutilate it
 
         instanceIds = new ArrayList<String>(instanceIds);
 
         System.out.println();
         System.out.println("Waiting for instances to come online.");
 
         ArrayList<String> instanceIPs = new ArrayList<String>();
 
         while (instanceIds.size() > 0) {
             System.out.print(".");
             DescribeInstancesResult status = ec2.describeInstances((new DescribeInstancesRequest()).withInstanceIds(instanceIds));
             for (Reservation r : status.getReservations()) {
                 for (Instance i : r.getInstances()) {
                     if (i.getState().getName().equals("running") && i.getPublicDnsName().length() > 0) {
                         instanceIPs.add(i.getPublicDnsName());
                         instanceIds.remove(i.getInstanceId());
                         System.out.print("\n");
                         System.out.println(i.getPublicDnsName()+" has come online. Still waiting on : "+instanceIds.size());
                     }
                 }
             }
         }
 
         System.out.println();
         System.out.println("Fleet assembled.");
 
         return instanceIPs;
     }
 
     // This turns off the instances once we're done using them
 
     private static void terminateEC2Instances(AmazonEC2 ec2, ArrayList<String> instanceIds) {
 
         System.out.println();
         System.out.println("Dispersing the fleet.");
         TerminateInstancesResult result = ec2.terminateInstances((new TerminateInstancesRequest()).withInstanceIds(instanceIds));
         System.out.println("Everyone's going home.");
 
         System.out.println();
         System.out.println("PewPew Commander, over and out.");
 
         System.out.println();
 
     }
 
     private static boolean killDisplay = false;
 
     // This does the work, once all the instances have been started up
 
     private static ArrayList<Socket> waitForSockets(ArrayList<String> cannons) {
 
         // First we need to gather all the instances
 
         ArrayList<Thread> socketThreads = new ArrayList<Thread>();
         ArrayList<WaitForSocket> socketRunnables = new ArrayList<WaitForSocket>();
 
         System.out.println();
         System.out.println("Waiting for everyone to heat up cannons. (this can take a minute or two)");
 
         for (String cannon : cannons) {
             WaitForSocket sock = new WaitForSocket(cannon);
             socketRunnables.add(sock);
 
             Thread t = new Thread(sock);
             socketThreads.add(t);
             t.start();
         }
 
         Thread display = new Thread(new DisplayRunnable(".",100,-1));
         display.start();
 
         try {
             for (Thread t : socketThreads) {
                 t.join();
             }
         }
         catch (InterruptedException e) {
             System.err.println("Asynchronous insubbordination! Bailing.");
             return null;
         }
 
         killDisplay = true;
 
         ArrayList<Socket> sockets = new ArrayList<Socket>();
         for (WaitForSocket waitForSocket : socketRunnables) {
             sockets.add(waitForSocket.getSocket());
         }
 
         return sockets;
     }
 
     // This does the work, once all the instances have been started up
 
     private static TestResult runTest(ArrayList<Socket> sockets, TestConfig config, int tests) {
 
         // Now all the threads are 
 
         System.out.println();
         System.out.println("Sending out orders to "+tests+" instances.");
         System.out.println();
 
         ArrayList<Thread> threads = new ArrayList<Thread>();
         ArrayList<CommanderRunnable> runnables = new ArrayList<CommanderRunnable>();
 
         for (int i = 0; i < tests; i++) {
             Socket socket = sockets.get(i);
 
             System.out.println("Sending firing orders to "+socket);
             CommanderRunnable cr = new CommanderRunnable(socket,config);
             runnables.add(cr);
 
             Thread t = new Thread(cr);
             threads.add(t);
             t.start();
 
         }
 
         System.out.println();
         System.out.println("All (REST API) guns blazing! Waiting for response.");
         System.out.println();
 
         try {
             for (Thread t : threads) {
                 t.join();
             }
         }
         catch (InterruptedException e) {
             System.err.println("Asynchronous insubbordination! Bailing.");
             return new TestResult();
         }
 
         System.out.println();
 
         TestResult allResults = new TestResult();
 
         for (CommanderRunnable cr : runnables) {
             TestResult result = cr.getResult();
             allResults.passedTests += result.passedTests;
             allResults.failedTests += result.failedTests;
             allResults.msAverageResponse += result.msAverageResponse;
         }
         allResults.msAverageResponse /= threads.size();
 
         System.out.println();
         System.out.println("All transmissions received.");
         System.out.println("Battle summary:");
         System.out.println("Passed tests: "+allResults.passedTests);
         System.out.println("Failed tests: "+allResults.failedTests);
         System.out.println("Average response (ms): "+allResults.msAverageResponse);
 
         return allResults;
     }
 
     private static class DisplayRunnable implements Runnable {
 
         String str;
         int delay;
         int counter;
 
         public DisplayRunnable(String str, int delay, int counter) {
             this.str = str;
             this.delay = delay;
             this.counter = counter;
         }
         
         public void run() {
             while (true) {
 
                 // If counter is not -1, then we should limit our prints
 
                 if (counter > 0) {
                     counter --;
                     if (counter == 0) return;
                 }
 
                 // Otherwise we take our cues from killDisplay
 
                 else if (killDisplay) return;
 
                 System.out.print(str);
 
                 try {
                     Thread.sleep(100);
                 }
                 catch (InterruptedException e) {
                     // Do nothing. This isn't a critical thread.
                 }
             }
         }
     }
 
     private static class WaitForSocket implements Runnable {
 
         Socket s;
         String host;
 
         public WaitForSocket(String host) {
             this.host = host;
         }
 
         public void run() {
 
             // Wait for the server to come online
 
             while (true) {
                 try {
                     s = new Socket(host,2109);
                     System.out.print("\n");
                     System.out.println(s+" is ready to receive firing orders.");
                     break;
                 }
                 catch (IOException e) {
                 }
             }
         }
 
         public Socket getSocket() {
             return s;
         }
     }
 
     private static class CommanderRunnable implements Runnable {
         
         Socket s;
         TestConfig config;
         TestResult result;
 
         public CommanderRunnable(Socket s, TestConfig config) {
             this.s = s;
             this.config = config;
         }
 
         public TestResult getResult() {
             return result;
         }
 
         public void run() {
 
             // Fire the cannon
 
             try {
 
                 ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                 oos.writeObject(config);
                 
                 ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                 result = (TestResult)ois.readObject();
 
                 return;
             }
             catch (ClassNotFoundException e) {
                 System.err.println("Transmission came through, but didn't follow the right format. Spooling down cannon.");
             }
             catch (IOException e) {
                 System.out.println("Transmission garbled. Spooling down cannon.");
             }
 
         }
     }
 
 }
