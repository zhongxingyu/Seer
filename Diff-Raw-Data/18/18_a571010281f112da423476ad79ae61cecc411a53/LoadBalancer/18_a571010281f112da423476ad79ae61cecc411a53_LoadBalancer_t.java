 import jobs.Job;
 import jobs.JobQueue;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import util.Util;
 
 import java.io.File;
 import java.util.Scanner;
 
 import loadbalance.Adaptor;
 
 public class LoadBalancer {
 
     private static Logger logger = Logger.getLogger(LoadBalancer.class);
     private static Scanner in = new Scanner(System.in);
     private static Adaptor adaptor;
     private static String[] parameters;
 
     private static final String BAR = "--------------------------------------------";
 
     public static void main(String[] args) {
         log4jConfigure();
         init();
         work();
     }
 
     private static void work() {
 
         printWelcomeMessage();
 
         while(true) {
             printCommandNotifier();
             String cmd = in.nextLine();
            if(cmd.length() == 0) continue;
             parameters = cmd.split(" ");
             if(parameters[0].toUpperCase().equals("QUIT")) {
                 break;
             }
 
             String funcName = CommandMap.getFuncName(parameters[0]);
             if(funcName == null)
                 System.out.println("Wrong command name");
             else
                 Util.callStaticMethod(LoadBalancer.class, funcName);
         }
 
         printGoodbyeMessage();
     }
 
     private static final String FORMAT_STRING_COMMAND = "%-25s%-25s\n";
     public static void printHelp() {
         System.out.printf(FORMAT_STRING_COMMAND, "COMMAND", "USAGE");
         System.out.printf(FORMAT_STRING_COMMAND, "connect <IP>:<PORT>", "connect the remote node");
         System.out.printf(FORMAT_STRING_COMMAND, "start <PORT>", "start the node using given port");
         System.out.printf(FORMAT_STRING_COMMAND, "lsjob", "list the jobs in queue");
         System.out.printf(FORMAT_STRING_COMMAND, "ldjob <EXECUTABLE_FILE_NAME>", "upload the job file");
         System.out.printf(FORMAT_STRING_COMMAND, "quit", "quit the program");
     }
 
     public static void connectNode() {
         if(parameters.length != 2) {
             System.out.println("Wrong command format. Should use connect <IP>:<PORT>, eg connect 127.0.0.1:20000");
             return;
         }
         String param = parameters[1];
         //TODO: WRITE YOUR CONNECT CODE HERE
         int delim = param.indexOf(':');
         String hostname = param.substring(0, delim);
         int ip = Integer.parseInt(param.substring(delim + 1));
         adaptor.tryConnect(hostname, ip);
     }
 
     public static void startNode() {
         if(parameters.length != 2) {
             System.out.println("Wrong command format. Should use start <PORT>, eg start 20000");
             return;
         }
 
         Integer port = Integer.valueOf(parameters[1]);
         adaptor = new Adaptor(port);
     }
 
     public static void loadJob() {
         if(parameters.length != 2) {
             System.out.println("Wrong command format. See help");
             return;
         }
 
         String fileName = parameters[1];
         Job job = new Job(fileName);
         if(!job.loadJobFromFile())
             System.out.println("Encountering error in loading jobs");
         if(!job.saveJobToFile())
             System.out.println("Encountering error in saving jobs");
 
         logger.info("Successfully load job " + fileName);
         adaptor.getWorkerThread().addJob(job);
     }
 
     private static final String FORMAT_STRING_LIST_JOB = "%-25s%-25s%-25s\n";
     public static void listJobs() {
         if(parameters.length != 1) {
             System.out.println("Wrong command format. No argument for list jobs command");
             return;
         }
 
         JobQueue jobQueue = adaptor.getJobQueue();
         System.out.println(BAR);
         System.out.printf(FORMAT_STRING_LIST_JOB,"Job Name", "Job Size", "IsLoadedToMemory");
         for(Job job : jobQueue) {
             System.out.printf(FORMAT_STRING_LIST_JOB, job.getFileName(), job.getSize(), job.isLoaded() ? "T" : "F");
         }
     }
 
     private static void printWelcomeMessage() {
         System.out.println("Welcome to use LoadBalancer!");
         System.out.println("Author: Raviphol Sukhajoti, Junjie Hu");
     }
 
     private static void printGoodbyeMessage() {
         System.out.println("Thanks for using. Goodbye!");
         System.out.println();
         System.out.println();
     }
 
     private static void printCommandNotifier() {
         System.out.flush();
         System.out.print(">");
     }
 
     private static void init() {
 
         CommandMap.init();
         logger.info("Load "+ CommandMap.size() + " command(s) successfully");
 
         //TODO: put all your init code here
         logger.info("Program initializes successfully");
         System.out.println(BAR);
     }
 
     private static void log4jConfigure() {
         PropertyConfigurator.configure("log4j.properties");
         System.out.println("Configure log4j successfully");
     }
 }
