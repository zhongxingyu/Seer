 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Server.AnalyticsServer;
 
 import Events.Event;
 import java.rmi.RemoteException;
 import java.rmi.registry.Registry;
 import PropertyReader.RegistryProperties;
 import Common.IManagementClientCallback;
 import Common.IProcessEvent;
 import Common.ISubscribe;
 import Common.IUnsubscribe;
 import Server.RMIRegistry;
 import java.rmi.Remote;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author daniela
  */
 public class AnalyticsServer implements ISubscribe, IProcessEvent, IUnsubscribe {
 
     //ThreadPool
     private ExecutorService executer;
     //Port from the RMI-Registry
     private int port = RegistryProperties.getPort();
     private Registry rmiRegistry;
     //Remote Object of this Server to export
     private Remote remoteAnalyticsServer;
     //Name of this Server in the rmiRegistry - from the properties file
     private String bindingName;
     //List with all subscriptions containing String ID + SubscriptionObject
     private HashMap<String, Subscription> subscriptions = new HashMap<String, Subscription>();
     private static long serverStarttime = new Date().getTime();
 
     /**
      * starts a AnalyticsServer as Thread
      *
      * @param args should contain bindingName
      * @throws RemoteException
      */
     public static void main(String[] args) throws RemoteException {
         AnalyticsServer server = new AnalyticsServer();
         if (args.length == 1) {
             server.setBindingName(args[0]);
             server.start();
         } else {
             System.err.println("Invalid Arguments.");
         }
     }
 
     /**
      * adds a Task to the ThreadPool
      *
      * @param task
      */
     public void addTask(Runnable task) {
         executer.execute(task);
     }
 
     public void start() {
    
         rmiRegistry = RMIRegistry.getRmiRegistry();
         try {
             remoteAnalyticsServer = UnicastRemoteObject.exportObject(this, 0);
             rmiRegistry.rebind(bindingName, remoteAnalyticsServer);
 
         } catch (RemoteException ex) {
             Logger.getLogger(AnalyticsServer.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     public void setBindingName(String bindingName) {
         this.bindingName = bindingName;
     }
 
     public String subscribe(String filter, IManagementClientCallback callbackObject) throws RemoteException {
 
         Subscription newSubscription = new Subscription(filter, callbackObject, this);
         String id = newSubscription.getID();
         subscriptions.put(id, newSubscription);
         return id;
 
     }
 
     public void processEvent(Event event) throws RemoteException {
         //TODO calculate statistics
         //if event !instanceof StatisticsEvent
 
 
         //send Event to all subscribers, they decide whether they need it or not
         for (Subscription subscription : subscriptions.values()) {
             subscription.sendEvent(event);
         }
 
     }
 
     public void unsubscribe(String id) throws RemoteException {
         if (subscriptions.containsKey(id)) {
             subscriptions.remove(id);
         }
     }
 
     public long getStarttime() {
         return serverStarttime;
     }
 }
