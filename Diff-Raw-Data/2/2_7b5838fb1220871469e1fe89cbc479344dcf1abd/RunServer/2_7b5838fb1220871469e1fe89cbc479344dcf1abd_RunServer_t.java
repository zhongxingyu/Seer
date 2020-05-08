 package ua.cn.dmitrykrivenko.rmi.session.example.server;
 
 import java.io.Console;
 import java.io.File;
 import java.rmi.Naming;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import ua.cn.dmitrykrivenko.rmi.session.example.Login;
 import ua.cn.dmitrykrivenko.rmi.session.example.Session;
 import ua.cn.dmitrykrivenko.rmi.session.example.impl.LoginImpl;
 import ua.cn.dmitrykrivenko.rmi.session.example.impl.SessionImpl;
 import ua.cn.dmitrykrivenko.rmi.session.example.server.utils.SQLiteJDBC;
 
 /**
  *
  * @author Dmitry Krivenko <dmitrykrivenko@gmail.com>
  */
 public class RunServer {
 
     private static final String STOP_COMMAND = "stop";
     private static final String LOGIN_SERVICE = "rmi://localhost/RemoteLogin";
     private static final String SESSION_SERVICE = "rmi://localhost/RemoteSession";
 
     public static void main(String[] args) {
         Console console = System.console();
         if (console == null) {
             System.out.println("Cannot initialize console");
             System.exit(1);
         }
         try {
             SQLiteJDBC.createTable();
             SQLiteJDBC.fillTable();
             LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
 
             Login loginService = new LoginImpl();
             Session sessionService = new SessionImpl();
 
             Naming.rebind(LOGIN_SERVICE, loginService);
             Naming.rebind(SESSION_SERVICE, sessionService);
 
             Thread dataGenerator = new Thread(DataGenerator.INSTANCE);
             dataGenerator.start();
 
             System.out.println("Server has started...");
             console.printf("To correctly stop server use command - %s\n", STOP_COMMAND);
             while (true) {
                 String command;
                 command = console.readLine();
                 if (command.equals(STOP_COMMAND)) {
                     Naming.unbind(LOGIN_SERVICE);
                     Naming.unbind(SESSION_SERVICE);
                     UnicastRemoteObject.unexportObject(loginService, true);
                     UnicastRemoteObject.unexportObject(sessionService, true);
 
                     dataGenerator.interrupt();
                    while (dataGenerator.isAlive()) {
                     }
 
                     System.out.println("Server has stopped");
                     break;
                 }
             }
         } catch (Exception ex) {
             System.err.println("Server exception: " + ex.getMessage());
         } finally {
             File tmpFile = new File(SQLiteJDBC.DB_FILE);
             if (tmpFile.exists()) {
                 tmpFile.delete();
             }
         }
     }
 }
