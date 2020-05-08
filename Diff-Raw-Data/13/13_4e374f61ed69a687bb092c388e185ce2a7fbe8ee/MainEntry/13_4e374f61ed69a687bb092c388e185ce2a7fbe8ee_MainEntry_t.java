 package main;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 import communication.Messages;
 import communication.MessagesFactory;
 import communication.TCPClient;
 import membership.Proc;
 import misc.MiscTool;
 import org.apache.log4j.PropertyConfigurator;
 
 import javax.swing.text.Utilities;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Scanner;
 
 import static communication.Messages.JoinMessage;
 import static communication.Messages.Message;
 import static communication.Messages.MessageType;
 
 public class MainEntry {
 
     private static Scanner in  = new Scanner(System.in);
     private static CommandMap commandMap = CommandMap.getInstance();
     private static Proc proc;
     private static Integer localPort;
 
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
         }
     }
 
     private static void showMemberList() {
         for(Messages.ProcessIdentifier identifier : proc.getMemberList()) {
             Integer timeStamp;
             String address;
             if(identifier.getId().equals(proc.getId())) {
                 timeStamp = proc.getTimeStamp();
                 address = "localhost:" + proc.getTcpPort();
             } else {
                 timeStamp = identifier.getTimestamp();
                 address = identifier.getIP() + ":" +identifier.getPort();
             }
             System.out.println(identifier.getId() + '\t' + address + '\t' + timeStamp);
         }
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
