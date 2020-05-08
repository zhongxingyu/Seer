 package main;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 import communication.Messages;
 import communication.MessagesFactory;
 import communication.TCPClient;
 import filesystem.FileOperations;
 import filesystem.FileState;
 import filesystem.SDFS;
 import membership.Proc;
 import membership.ProcState;
 import misc.MiscTool;
 import misc.TimeMachine;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import javax.swing.text.Utilities;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Scanner;
 
 import static communication.Messages.*;
 
 public class MainEntry {
 
     private static Scanner in  = new Scanner(System.in);
     private static CommandMap commandMap = CommandMap.getInstance();
     private static Proc proc;
     private static Integer localPort;
 
     private static Logger logger = Logger.getLogger(MainEntry.class);
 
     public static void main(String[] args) {
 
         log4jConfigure();
         init();
         work();
 
     }
 
     private static void log4jConfigure() {
         PropertyConfigurator.configure("log4j.properties");
         System.out.println("configure log4j successfully");
     }
 
 
     private static void init() {
         commandMap.initialize();
 
         printWelcomeMessage();
     }
 
     public static void work() {
 
         while(true) {
             String cmd = inputCommand();
             if(cmd.length() ==0) {
                 continue;
             }
 
             String funcName = commandMap.findCommand(cmd);
 
             if(funcName == null) {
                 //TODO: wrong command
             } else if(funcName.equals("quit")) {
                 break;
             } else if(funcName.equals("printHelp")) {
                 CommandMap.printHelp();
             } else {
                 MiscTool.callStaticMethod(MainEntry.class, funcName);
             }
         }
 
         System.out.println("Program quits");
     }
 
     private static void start() {
         localPort = MiscTool.inputPortNumber(in);
         proc = new Proc(localPort);
         proc.init();
     }
 
     private static void joinGroup() {
         String address = MiscTool.inputAddress(in);
         System.out.println("Start connecting to " + address);
         TCPClient tcpClient = new TCPClient(address);
         tcpClient.setProc(proc);
         if(tcpClient.connect()) {
             Message m = MessagesFactory.generateJoinMessage(
                     proc.getId(), proc.getIdentifier().getIP(), localPort, proc.increaseAndGetTimeStamp());
             tcpClient.sendData(m);
             tcpClient.close();
         }
     }
 
     private static void showMemberList() {
         int pos = 0;
         for(Messages.ProcessIdentifier identifier : proc.getMemberList()) {
             Integer timeStamp;
             String address;
             Long localTime;
             ProcState procState;
 
             if(isMySelf(identifier)) {
                 timeStamp = proc.getTimeStamp();
                 address = "127.0.0.1:" + proc.getTcpPort();
                 localTime = TimeMachine.getTime();
                 procState = ProcState.available;
             } else {
                 timeStamp = identifier.getTimestamp();
                 address = identifier.getIP() + ":" +identifier.getPort();
                 localTime = proc.getMemberList().getTime(identifier);
                 procState = proc.getMemberList().getState(identifier);
             }
             System.out.println(
                     identifier.getId() + '\t' + address + '\t' + timeStamp + '\t' + localTime + '\t' + procState);
             ++pos;
         }
     }
 
     private static void showFileList() {
         for(FileIdentifier fileIdentifier : proc.getSDFS().getFileList()) {
             ProcessIdentifier identifier = fileIdentifier.getFileStoringProcess();
             if(!proc.getSDFS().isValid(fileIdentifier)) {
                 continue;
             }
             String address;
             Integer timeStamp;
             Long localTime;
             FileState fileState;
 
             if(isMySelf(identifier)) {
                 address = "127.0.0.1:" + proc.getTcpPort();
                 timeStamp = proc.getTimeStamp();
 //                localTime = TimeMachine.getTime();
                 localTime = proc.getSDFS().getFileLocalTime(fileIdentifier);
             } else {
                 address = identifier.getIP()+":"+identifier.getPort();
                 timeStamp = proc.getSDFS().getFileTimeStamp(fileIdentifier);
                 localTime = proc.getSDFS().getFileLocalTime(fileIdentifier);
             }
             fileState = proc.getSDFS().getFileState(fileIdentifier);
             System.out.println(
                     fileIdentifier.getFilepath() + '\t' + address + '\t' + timeStamp + '\t' + localTime + '\t' +fileState);
         }
     }
 
     private static void putFile() {
        long startTime = System.currentTimeMillis();
         String fileName = MiscTool.inputFileName(in);
         proc.getSDFS().addFileLocally(fileName);
         long usingTime = System.currentTimeMillis() - startTime;
 
         logger.info("put command uses " + usingTime + " ms");
     }
 
     private static void deleteFile() {
         String fileName = MiscTool.inputFileName(in);
         proc.getSDFS().deleteFile(fileName);
     }
 
     private static void getFile(){
         String remoteFileName = MiscTool.inputFileName(in);
         String localFileName = MiscTool.inputFileName(in);
         long startTime = System.currentTimeMillis();
         proc.getSDFS().getRemoteFile(remoteFileName, localFileName);
         long usingTime = System.currentTimeMillis() - startTime;
 
         logger.info("Get command uses " + usingTime + " ms");
     }
 
     private static boolean isMySelf(ProcessIdentifier identifier) {
         return identifier.getId().equals(proc.getId());
     }
 
     private static String inputCommand() {
         System.out.print(">");
         return in.nextLine();
     }
 
     private static void printWelcomeMessage() {
         System.out.println("Welcome to the fictitious Group-R-Us Inc.!");
         System.out.println("Author: Muhammad Naveed, Junjie Hu");
     }
 
     public Proc getProc() {
         return proc;
     }
 }
