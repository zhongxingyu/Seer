 package se.umu.cs.jsgajn.gcom.testapp;
 
 import java.rmi.AlreadyBoundException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.util.Scanner;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import se.umu.cs.jsgajn.gcom.Client;
 import se.umu.cs.jsgajn.gcom.management.ManagementModule;
 import se.umu.cs.jsgajn.gcom.management.ManagementModuleImpl;
 
 public class ChatMember implements Client {
     private static final Logger logger = LoggerFactory.getLogger(ChatMember.class);
     private ManagementModule groupMember;
 
     public ChatMember(String gnsHost, int gnsPort, String groupName)
         throws RemoteException, AlreadyBoundException, NotBoundException {
         this(gnsHost, gnsPort, groupName, -1); // TODO: FIx init clientPort
     }
 
     public ChatMember(String gnsHost, int gnsPort, String groupName, int clientPort)
         throws RemoteException, AlreadyBoundException, NotBoundException {
         if (clientPort < 1) {
             this.groupMember = new ManagementModuleImpl(this, gnsHost, gnsPort, groupName);
         } else {
             this.groupMember = new ManagementModuleImpl(this, gnsHost, gnsPort, groupName, clientPort);
         }
 
         new Thread (new Runnable() {
                 public void run() {
                     while (true) {
                         logger.info(ManagementModule.PID + " - message: ");
                         Scanner sc = new Scanner(System.in);
                         String msg;
                         msg = sc.nextLine();
                         groupMember.send(msg);
                     }}}).start();
     }
 
     public void deliver(Object m) {
        System.out.println(m.toString());
     }
 
     private static void usage() {
         System.out.println("Usage: java ChatMember [host] [port] [groupname]");
         System.out.println("Usage: java ChatMember [host] [groupname] // port 1099 will be used");
     }
 
     public static void main(String[] args) {
         try {
             if (args.length == 4) {
                 new ChatMember(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));
             } else if (args.length == 3) {
                 new ChatMember(args[0], Integer.parseInt(args[1]), args[2]);
             } else if (args.length == 2) {
                 new ChatMember(args[0], 1099, args[1]);
             } else {
                 usage();
             }
         } catch (NumberFormatException e) {
             e.printStackTrace();
         } catch (RemoteException e) {
             // Om det är nåt fel hos GNSen
             e.printStackTrace();
         } catch (AlreadyBoundException e) {
             // Om man inte kan binda sig själv till sitt register
             // t.ex. om man redan är bunden dit
             e.printStackTrace();
         } catch (NotBoundException e) {
             // Ifall man försöker ansluta till GNSen men den inte
             // gick att binda tidigare
             e.printStackTrace();
         }
     }
 }
