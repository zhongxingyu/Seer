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
 
     public static void main(String[] args) {
 
         System.out.println("Welcome to PewPew Commander!");
 
         System.out.println("Authenticating with the mercenaries.");
 
         AWSCredentials cred;
         try {
             cred = new PropertiesCredentials(new File("config.txt"));
         }
         catch (IOException e) {
             System.out.println("Can't find the autorization file.");
             return;
         }
         AmazonEC2 ec2 = new AmazonEC2Client(cred);
 
         // It's very important to set the region correctly, or else nothing works
 
         ec2.setRegion(com.amazonaws.regions.Region.getRegion(Regions.US_WEST_2));
 
         System.out.println("Preparing written orders.");
 
         TestConfig config = new TestConfig();
         config.url = "www.google.com";
         config.expectedResponse = ":)";
         config.numHits = 100;
         config.msDelay = 100;
         config.urlParamGenerator = new StaticTestParameterGenerator();
 
         // Start up the socket server
 
         int launchedInstances = 1;
 
         // Create the instances
 
         ArrayList<String> instanceIds = createEC2Instances(ec2, 1);
 
         // Wait for instances to start up
 
         ArrayList<String> cannons = instanceIdsToDNS(ec2, instanceIds);
 
         // Tell instances to attack
 
         runTests(cannons,config);
 
         // Terminate the instances
 
         terminateEC2Instances(ec2, instanceIds);
     }
 
     // This creates a bunch of EC2 instances
 
     private static ArrayList<String> createEC2Instances(AmazonEC2 ec2, int numInstances) {
         String userData = "#! /bin/bash\n" +
                           "java -cp /home/ec2-user/pewpew-0.1.jar com.smartass.pewpew.PewPew";
 
         System.out.println("Making interstellar call for backup");
 
         DescribeInstancesResult liveStatus = ec2.describeInstances();
 
         RunInstancesResult result = ec2.runInstances(new RunInstancesRequest().withImageId("ami-4041dc70").withInstanceType("t1.micro").withKeyName("archkey").withSecurityGroups("default").withUserData(Base64.encodeBase64String(userData.getBytes())).withInstanceInitiatedShutdownBehavior("terminate").withMinCount(numInstances).withMaxCount(numInstances));
 
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
 
         System.out.println("Fleet assembled. Waiting for everyone to heat up cannons.");
 
         return instanceIPs;
     }
 
     // This turns off the instances once we're done using them
 
     private static void terminateEC2Instances(AmazonEC2 ec2, ArrayList<String> instanceIds) {
 
         System.out.println("Dispersing the fleet.");
 
         TerminateInstancesResult result = ec2.terminateInstances((new TerminateInstancesRequest()).withInstanceIds(instanceIds));
 
         System.out.println("Everyone's going home. PewPew Commander, over and out.");
 
     }
 
     // This does the work, once all the instances have been started up
 
     private static void runTests(ArrayList<String> cannons,TestConfig config) {
 
         ArrayList<Thread> socketThreads = new ArrayList<Thread>();
         ArrayList<WaitForSocket> socketRunnables = new ArrayList<WaitForSocket>();
 
         System.out.println("Waiting for everyone's socket to come online");
 
         for (String cannon : cannons) {
             WaitForSocket sock = new WaitForSocket(cannon);
             socketRunnables.add(sock);
 
             Thread t = new Thread(sock);
             socketThreads.add(t);
             t.start();
         }
 
         Thread display = new Thread(new DisplayRunnable());
         display.start();
 
         try {
             for (Thread t : socketThreads) {
                 t.join();
             }
         }
         catch (InterruptedException e) {
             System.err.println("Asynchronous insubbordination! Bailing.");
             return;
         }
 
        display.interrupt();
 
         System.out.println("We're all ready to go. Sending out orders.");
 
         ArrayList<Thread> threads = new ArrayList<Thread>();
         ArrayList<CommanderRunnable> runnables = new ArrayList<CommanderRunnable>();
 
         for (WaitForSocket waitForSocket : socketRunnables) {
             CommanderRunnable cr = new CommanderRunnable(waitForSocket.getSocket(),config);
             runnables.add(cr);
 
             Thread t = new Thread(cr);
             threads.add(t);
             t.start();
         }
 
         try {
             for (Thread t : threads) {
                 t.join();
             }
         }
         catch (InterruptedException e) {
             System.err.println("Asynchronous insubbordination! Bailing.");
             return;
         }
 
         TestResult allResults = new TestResult();
 
         for (CommanderRunnable cr : runnables) {
             TestResult result = cr.getResult();
             allResults.passedTests += result.passedTests;
             allResults.failedTests += result.failedTests;
             allResults.msAverageResponse += result.msAverageResponse;
         }
         allResults.msAverageResponse /= threads.size();
 
         System.out.println("All transmissions received.");
         System.out.println("Battle summary:");
         System.out.println("Passed tests: "+allResults.passedTests);
         System.out.println("Failed tests: "+allResults.failedTests);
         System.out.println("Average response (ms): "+allResults.msAverageResponse);
 
     }
 
     private static class DisplayRunnable implements Runnable {
         public void run() {
             while (true) {
                 System.out.print(".");
                 try {
                    Thread.sleep(500);
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
                 System.out.println("Sending firing orders to "+s);
 
                 ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                 oos.writeObject(config);
 
                 System.out.println("Waiting for operation report...");
                 
                 ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                 result = (TestResult)ois.readObject();
 
                 System.out.println("Successfully received report!");
 
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
