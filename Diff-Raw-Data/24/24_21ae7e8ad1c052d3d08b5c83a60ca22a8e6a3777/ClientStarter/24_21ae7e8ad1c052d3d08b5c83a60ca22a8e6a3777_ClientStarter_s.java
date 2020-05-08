 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ClientGUI;
 
 import DashboardGUI.GUIDashboard;
 import dispatcherMonitor.Dispatcher;
 import dispatcherMonitor.Monitor;
 import serverFassaden.CallCenterServerFassade;
 import serverFassaden.interfaces.ICallCenterServerFassade;
 
 import java.rmi.Naming;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 /**
  * @author aaz527
  */
 public class ClientStarter {
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws Exception {
        // TODO
        // Monitor starten
        // Dispatcher starten
        // ClientGUI starten
        // DashboardGUI starten

         HashMap<String, ICallCenterServerFassade> serverMap;
 
         serverMap = new HashMap<String, ICallCenterServerFassade>();
 
 //        for (int i = 0; i < args.length; i = i + 2) {
 //            //args[0] = zB localhost     + name des Servers = args[1] ( entspricht args[0] bei SystemRunner Main)
 //            String url = new String("rmi://" + args[i] + ":1099/" + args[i + 1]);
 //            serverMap.put(args[i + 1], (CallCenterServerFassade) lookup(url));
 //        }
 
         String url = new String("//localhost/SYS1");
         serverMap.put("SYS1", (ICallCenterServerFassade) Naming.lookup(url));
         url = new String("//localhost/SYS2");
         serverMap.put("SYS2", (ICallCenterServerFassade) Naming.lookup(url));
 
 
         Monitor monitor = new Monitor();
         Dispatcher dispatcher = new Dispatcher(monitor, serverMap);
         GUIClient guiClient = new GUIClient(dispatcher);
         guiClient.setVisible(true);
         GUIDashboard dashboard = new GUIDashboard(monitor);
         dashboard.setVisible(true);
     }
 }
