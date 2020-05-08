 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ManagementClient;
 
 import Common.IAnalytics;
 import Common.IBillingLogin;
 import Common.IBillingSecure;
 import Common.IManagementClientCallback;
 import PropertyReader.RegistryProperties;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.rmi.AccessException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author daniela
  */
 public class ManagementClient {  //implements IManagementClientCallback, Serializable {
 
     //private String id = UUID.randomUUID().toString();
     private IBillingSecure billingService = null;
     private IBillingLogin billingLogin;
     private String analyticsBindingName, billingBindingName;
     private static Registry rmiRegistry;
     private BufferedReader stdIn;
     private IAnalytics analyticsService;
     private int port = RegistryProperties.getPort();
     private String host = RegistryProperties.getHost();
     private String storedMessages = "";
     private boolean printEvents = true;
     private IManagementClientCallback callback;
     private ArrayList<String> mySubscriptions;
 
     public static void main(String[] args) throws RemoteException {
 
         new RegistryProperties();
         //args: bindingNames 0-analytics 1-billing
         if (args.length == 2) {
             //System.out.println(args[0] + " " + args[1]);
             ManagementClient client = new ManagementClient();
             client.setAnalyticsBindingName(args[0]);
             client.setBillingBindingName(args[1]);
             //client.setCallbackObject(client);
             client.start();
         } else {
 
             System.out.println("Invalid input arguments! \n"
                     + "Please provide binding name from Analytics and Billing Server");
         }
 
         //analyticsBindingName = args[0];
         //billingBindingName = args[1];
 
     }
 
     private ManagementClient() {
         //throw new UnsupportedOperationException("Not yet implemented");
     }
 
    /*public void setCallbackObject(IManagementClientCallback callback) {
     //this.callback = callback;
     }
     */
     private void close() {
         billingService = null;
         billingLogin = null;
         rmiRegistry =null;
         analyticsService = null;
         callback = null;
         try {
             stdIn.close();
         } catch (IOException ex) {
             Logger.getLogger(ManagementClient.class.getName()).log(Level.SEVERE, null, ex);
         }
         
     }
     
     private void openConnection() {
         try {
             ManagementClientCallback mcc = new ManagementClientCallback(this);
             callback = (IManagementClientCallback) UnicastRemoteObject.exportObject(mcc, 0);
 
             rmiRegistry = LocateRegistry.getRegistry(host, port);
 
             System.out.println("registry located");
 
             analyticsService = (IAnalytics) rmiRegistry.lookup(analyticsBindingName);
             billingLogin = (IBillingLogin) rmiRegistry.lookup(billingBindingName);
 
 
         } catch (AccessException ex) {
             Logger.getLogger(ManagementClient.class.getName()).log(Level.SEVERE, null, ex);
         } catch (RemoteException ex) {
             Logger.getLogger(ManagementClient.class.getName()).log(Level.SEVERE, null, ex);
         } catch (NotBoundException ex) {
             System.out.println("Problems with ServerConnection ");
             System.out.println("analyticsService: " + analyticsBindingName + " " + analyticsService == null);
             System.out.println("billingLogin: " + billingBindingName + " " + billingLogin == null);
             Logger.getLogger(ManagementClient.class.getName()).log(Level.SEVERE, null, ex);
         } catch (NullPointerException ex) {
             System.out.println("Problems with ServerConnection ");
             System.out.println("analyticsService: " + analyticsBindingName);
             System.out.println("billingLogin: " + billingBindingName);
         }
 
     }
 
     private void start() {
        mySubscriptions = new ArrayList();
         
         openConnection();
         
       /* if(subscribeAll) {
            try {
                analyticsService.subscribe("(USER_.*)|(BID_.*)|(AUCTION_.*)", callback);
            } catch (RemoteException ex) {
                Logger.getLogger(ManagementClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/
         
         stdIn = new BufferedReader(new InputStreamReader(System.in));
         String fromUser;
        /*try {
         System.out.println(analyticsService.subscribe("blubb", callback));
         } catch (RemoteException ex) {
         Logger.getLogger(ManagementClient.class.getName()).log(Level.SEVERE, null, ex);
         }*/
 
 
         try {
             //reading UserInput
             while ((fromUser = stdIn.readLine()) != null && !fromUser.equals("!end")) {
                 processInput(fromUser);
                 //if(!kommSchon(fromUser)) break;
             }
 
         } catch (IOException e) {
             System.err.println("I/O Fehler");
         } finally {
             System.out.println("Bye.");
             close();
         }
 
 
     }
 
     private void processInput(String fromUser) {
         String[] input;
 
         if (fromUser.startsWith("!login") || fromUser.startsWith("!steps") || fromUser.startsWith("!addStep") || fromUser.startsWith("!removeStep") || fromUser.startsWith("!bill") || fromUser.startsWith("!logout")) {
             //talk to billing server
             if (billingService == null) {
                 input = fromUser.split(" ");
                 if (input.length == 3) {
                     try {
                         billingService = billingLogin.login(input[1], input[2]);
                         if (billingService != null) {
                             System.out.println("Successfully logged in as " + input[1]);
                         } else {
                             System.out.println("Wrong username or password, please try again.");
                         }
                     } catch (RemoteException ex) {
                         Logger.getLogger(ManagementClient.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 } else {
                     System.out.println("Please login! Usage: !login <username> <pw>");
                 }
 
             } else {
                 if (fromUser.equals("!logout")) {
                     billingService = null;
                 } else if (fromUser.equals("!steps")) {
                     try {
                         System.out.println(billingService.getPriceSteps().toString());
                     } catch (RemoteException e) {
                         // TODO Handle error
                         e.printStackTrace();
                     }
                 } else if (fromUser.startsWith("!addStep")) {
                     input = fromUser.split(" ");
                     if (input.length == 5) {
                         try {
                             try {
                                 billingService.createPriceStep(Double.parseDouble(input[1]), Double.parseDouble(input[2]), Double.parseDouble(input[3]), Double.parseDouble(input[4]));
                             } catch (RemoteException ex) {
                                 Logger.getLogger(ManagementClient.class.getName()).log(Level.SEVERE, null, ex);
                             }
                         } catch (NumberFormatException ex) {
                             System.out.println("Usage: !addStep <startPrice> <endPrice> <fixedPrice> <variablePricePercent>");
                         }
                     } else {
                         System.out.println("Usage: !addStep <startPrice> <endPrice> <fixedPrice> <variablePricePercent>");
 
                     }
                 } else if (fromUser.startsWith("!removeStep")) {
                     input = fromUser.split(" ");
                     if (input.length == 3) {
                         try {
                             try {
                                 billingService.deletePriceStep(Double.parseDouble(input[1]), Double.parseDouble(input[2]));
                             } catch (RemoteException ex) {
                                 Logger.getLogger(ManagementClient.class.getName()).log(Level.SEVERE, null, ex);
                             }
                         } catch (NumberFormatException ex) {
                             System.out.println("Usage: !removeStep <startPrice> <endPrice>");
                         }
                     } else {
                         System.out.println("Usage: !removeStep <startPrice> <endPrice>");
                     }
                 } else if (fromUser.startsWith("!bill")) {
                     input = fromUser.split(" ");
                     if (input.length == 2) {
                         try {
                             try {
                                 System.out.append(billingService.getBill(input[1]).toString());
                             } catch (RemoteException ex) {
                                 System.out.println("No Entries for this User.");
                                 Logger.getLogger(ManagementClient.class.getName()).log(Level.SEVERE, null, ex);
                             }
                         } catch (NumberFormatException ex) {
                             System.out.println("Usage: !bill <userName>");
                         }
                     } else {
                         System.out.println("Usage: !bill <userName>");
                     }
                 } else {
                     System.out.println("Unknown Command");
                 }
 
             }
         } else if (fromUser.startsWith("!subscribe")) {
             input = fromUser.split(" ");
             if (input.length == 2) {
                 try {
                     String id = analyticsService.subscribe(input[1], (IManagementClientCallback) callback);
                     mySubscriptions.add(id);
                     if(id.startsWith("invalid")) {
                         System.out.println("invalid filter");
                     } else {
                     System.out.println("Created subscription with ID " + id + " for events using '" + input[1] + "'");
                     }
                 } catch (RemoteException ex) {
                     Logger.getLogger(ManagementClient.class.getName()).log(Level.SEVERE, null, ex);
                 }
             } else {
                 System.out.println("Usage: !subscribe <filterRegex>");
             }
         } else if (fromUser.startsWith("!unsubscribe")) {
             input = fromUser.split(" ");
             if (input.length == 2) {
                 if (mySubscriptions.contains(input[1])) {
                     try {
                         analyticsService.unsubscribe(input[1]);
                         System.out.println("subscription " + input[1] + " terminated");
                     } catch (RemoteException ex) {
                         Logger.getLogger(ManagementClient.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 } else {
                     System.out.println("You have no subscription with id " + input[1]);
                 }
             } else {
                 System.out.println("Usage: !unsubscribe <id>");
             }
 
         } else if (fromUser.equals("!auto")) {
             if (!printEvents) {
                 System.out.println("Automatic printing of events activated");
 
                 printEvents = true;
             }
         } else if (fromUser.equals("!hide")) {
             if (printEvents) {
                 System.out.println("Automatic printing of events disabled");
 
                 printEvents = false;
             }
         } else if (fromUser.equals("!print")) {
             if (!storedMessages.isEmpty()) {
                 System.out.println(storedMessages);
                 storedMessages = "";
             }
 
         } else {
             System.out.println("Unknown Command");
         }
 
     }
 
     public void receiveMessage(String msg) {
         if (printEvents) {
             System.out.println(msg);
         } else {
             storedMessages += msg + "\n";
         }
     }
 
     public String getAnalyticsBindingName() {
         return analyticsBindingName;
     }
 
     public void setAnalyticsBindingName(String analyticsBindingName) {
         this.analyticsBindingName = analyticsBindingName;
     }
 
     public String getBillingBindingName() {
         return billingBindingName;
     }
 
     public void setBillingBindingName(String billingBindingName) {
         this.billingBindingName = billingBindingName;
     }
 
     /*public void receiveEvent(Event event) throws RemoteException {
      if (printEvents) {
      System.out.println(event);
      } else {
      storedMessages += event;
      }
      }*/
 
     /* public String getID() {
      return id;
      }*/
 }
