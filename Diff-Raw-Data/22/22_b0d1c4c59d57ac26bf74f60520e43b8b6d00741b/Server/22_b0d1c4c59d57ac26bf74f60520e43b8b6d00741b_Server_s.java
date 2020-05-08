 package mbs2.pr04;
 
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 
 public class Server extends UnicastRemoteObject implements ServerI {
 
   public Server() throws RemoteException {
   }
 
   public int count() throws RemoteException {
     return ++count;
   }
 
   private int count = 0;
 
   public static void main(String[] args) {
     setRmiCodebase();
     try {
       Server server = new Server();
       Naming.bind("//localhost:1099/ServerObject", server);
       System.out.println("Server started.");
     } catch (Exception ex) {
       ex.printStackTrace();
     }
   }
 
   private static void setRmiCodebase() {
     String codebase = System.getProperty("java.rmi.server.codebase");
     if (codebase != null)
       return;
     
     String testFile = "/" + Server.class.getName().replace('.', '/') + ".class";
     String url = Server.class.getResource(testFile).toString();
     if (url.startsWith("jar:")) {
       int pos = url.indexOf(".jar");
       codebase = "file://" + url.substring(9, pos + 4);
     } else if (url.startsWith("file:")) {
       int pos = url.indexOf("bin");
       codebase = "file://" + url.substring(5, pos+4);
     } else // WTF
       return;
     System.err.println("Setting RMI codebase to " + codebase);
     System.setProperty("java.rmi.server.codebase", codebase);
     System.setProperty("java.rmi.server.useCodebaseOnly", "true");
   }
 }
