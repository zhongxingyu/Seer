 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Protocol;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import Auction.Auction;
 import Auction.AuctionHandler;
 import Auction.Bid;
 import PropertyReader.RegistryProperties;
 import Server.ServerThread;
 import User.User;
 import User.UserHandler;
 
 /**
  *
  * @author daniela
  */
 public class CommandProtocol {
 
     private User currentUser = null;
     private UserHandler userHandler;
     private AuctionHandler auctionHandler;
     private ServerThread serverThread = null;
    private LinkedList<Bid> groupBids = LinkedList<Bid>();
     //private String analyticsBindingName, billingBindingName;
     //private static Registry rmiRegistry;
 
     //private IAnalytics analyticsService;
 
     public CommandProtocol(ServerThread serverThread) {//, String analyticsBindingName, String billingBindingName) {
         userHandler = UserHandler.getInstance();
         auctionHandler = AuctionHandler.getInstance();
         this.serverThread = serverThread;
         new RegistryProperties();
 
 
         //this.analyticsBindingName = analyticsBindingName;
         //this.billingBindingName = billingBindingName;
 
        /* try {
             rmiRegistry = LocateRegistry.getRegistry(host, port);
             analyticsService = (IAnalytics) rmiRegistry.lookup(analyticsBindingName);
             auctionHandler.setAS(analyticsService);
         } catch (RemoteException ex) {
             Logger.getLogger(CommandProtocol.class.getName()).log(Level.SEVERE, null, ex);
         } catch (NotBoundException ex) {
             Logger.getLogger(CommandProtocol.class.getName()).log(Level.SEVERE, null, ex);
 
         }*/
         
 
     }
 
     //TODO COMMANDS + ANSWERS
     public String processInput(String strInput) {
         String strOutput = "";
 
         if (currentUser == null) {
             /**
              * allowed Commands: list login (state = LOGGED_IN)
              */
             if (strInput.equals("!list")) {
                 //list all auctions
                 strOutput = listAuctions();
 
             } else if (strInput.startsWith("!login")) {
                 String[] args = strInput.split(" ");
                 //User Input must contain Username
                 if (args.length < 2 || args.length > 2) {
                     strOutput = "Error: use following login command: !login <username>";
                 } else { //length is 2 - command + username
                     String username = args[1];
                     if (userHandler.login(username, serverThread)) {
                         if (serverThread == null) {
                             strOutput = "An Error occured, no udp connection to Client.";
                             return strOutput;
                         }
                         currentUser = userHandler.getUser(username);
                         strOutput = "Successfully logged in as " + username;
                         /*try {
                             analyticsService.processEvent(new UserEvent("USER_LOGIN", new Date().getTime(), username));
                         } catch (RemoteException ex) {
                             Logger.getLogger(CommandProtocol.class.getName()).log(Level.SEVERE, null, ex);
                         }*/
                     } else {
                         strOutput = "User " + username + " is already logged in.";
                     }
                 }
             } else if (strInput.startsWith("!create")
                     || strInput.startsWith("!bid")
                     || strInput.startsWith("!logout")) {
                 strOutput = "You need to be logged in to use this Command!";
             } else {
                 strOutput = printUsage();
             }
             //User is logged in
         } else if (currentUser.isLoggedIn()) {
             /**
              * allowed Commands: list create bid logout (state = LOGGED_OUT)
              */
             if (strInput.equals("!end")) {
                /* try {
                     analyticsService.processEvent(new UserEvent("USER_DISCONNECTED", new Date().getTime(), currentUser.getUsername()));
                 } catch (RemoteException ex) {
                     Logger.getLogger(CommandProtocol.class.getName()).log(Level.SEVERE, null, ex);
                 }*/
                 currentUser.logout();
                 currentUser = null;
 
             } else if (strInput.equals("!list")) {
                 //list all auctions
                 strOutput = listAuctions();
             } else if (strInput.startsWith("!create")) {
                 String[] args = strInput.split(" ");
                 //Input must contain all arguments
                 int duration;
                 String description;
                 if (args.length < 3 || args.length > 3) {
                     strOutput = "Error! Use correct command: !create <duration> <description>";
                 } else {
                     try {
                         duration = Integer.parseInt(args[1]);
                         if (duration < 0) {
                             throw new NumberFormatException();
                         }
                     } catch (NumberFormatException e) {
                         strOutput = "Error! Command: !create <duration - in seconds> <description>";
                         return strOutput;
                     }
                     description = args[2];
                     Auction auction = auctionHandler.addAuction(currentUser, duration, description);
                     strOutput = "An auction '" + auction.getDescription() + "' with id "
                             + auction.getId() + " has been created and will end on "
                             + auction.getEndDate().toString() + ".";
 
                 }
             } else if (strInput.startsWith("!bid")) {
                 //TODO check params + bid
                 //!bid <auction-id> <amount>
                 String[] args = strInput.split(" ");
                 int id;
                 double amount;
                 if (args.length != 3) {
                     strOutput = "Error! Use correct command: !bid <auction-id> <amount>";
                 } else {
                     try {
                         id = Integer.parseInt(args[1]);
                         amount = Double.parseDouble(args[2]);
                         if (id < 0 || amount < 0) {
                             throw new NumberFormatException();
                         }
                     } catch (NumberFormatException e) {
                         strOutput = "Error! Command: !bid <id > 0> <amount > 0>";
                         return strOutput;
                     }
                     Auction a = auctionHandler.getAuction(id);
                     if (a != null) {
                         if (auctionHandler.bid(currentUser, id, amount)) {
                             strOutput = "You successfully bid with " + amount + " on '" + a.getDescription() + "'";
                         } else {
                             strOutput = "You unsuccessfully bid with " + amount + " on '"
                                     + a.getDescription() + "'. Current highest bid is " + a.getHighestBid() + ".";
                         }
                     } else {
                         strOutput = "No Auction with id " + id;
                     }
                 }
 
 
             } else if (strInput.startsWith("!groupBid"))
             {
            	 String[] args = strInput.split(" ");
                 int id;
                 double amount;
                 if (args.length != 3) {
                     strOutput = "Error! Use correct command: !groupBid <auction-id> <amount>";
                 } else {
                     try {
                         id = Integer.parseInt(args[1]);
                         amount = Double.parseDouble(args[2]);
                         if (id < 0 || amount < 0) {
                             throw new NumberFormatException();
                         }
                     } catch (NumberFormatException e) {
                         strOutput = "Error! Command: !groupBid <id > 0> <amount > 0>";
                         return strOutput;
                     }
                 
 	                Auction a = auctionHandler.getAuction(id);
 	                if (a != null) {
 	                    
 	                } else {
 	                    strOutput = "No Auction with id " + id;
 	                }	                
                 }
                 
            } else if (strInput.startsWith("!groupBid"))
            {
            	
            }        
            else if (strInput.equals("!logout")) {
                 /*try {
                     analyticsService.processEvent(new UserEvent("USER_LOGOUT", new Date().getTime(), currentUser.getUsername()));
                 } catch (RemoteException ex) {
                     Logger.getLogger(CommandProtocol.class.getName()).log(Level.SEVERE, null, ex);
                 }*/
                 userHandler.logout(currentUser);
                 //currentUser.logout();
                 currentUser = null;
                 strOutput = "Successfully logged out.";
 
             } else if (strInput.startsWith("!login")) {
                 strOutput = "You're already logged in. "
                         + "You need to logout first, before you can login again!";
             }     
             else {            	
                 strOutput = printUsage();
             }
         }
 
         return strOutput;
     }
 
     private String listAuctions() {
         String list = "", highestBidder;
         if (auctionHandler.hasAuctions()) {
         	synchronized (auctionHandler) {
 				
 			
         	Collection<Auction> auctions = auctionHandler.getAllAuctions().values();
         	
             for (Auction a : auctions ) {
                 highestBidder = "none";
                 if (a.getHighestBidder() != null) {
                     highestBidder = a.getHighestBidder().getUsername();
                 }
                 list += a.getId() + ". '" + a.getDescription() + "' " + a.getOwner().getUsername()
                         + " " + a.getEndDate() + " " + a.getHighestBid()
                         + " " + highestBidder + "\n";
             }
         	}
         }
         if (list.isEmpty()) {
             list = "There are no Auctions.";
         } else {
             //eliminate last enter
             list = list.substring(0, list.length() - 1);
         }
         return list;
     }
 
     private String printUsage() {
         if (currentUser == null) {
             return "Unknown Command!"
                     + "\nPossible Actions: !list, !login <username> or !end";
 
         } else if (currentUser.isLoggedIn()) {
             return "Unknown Command!"
                     + "\nPossible Actions: !list, !create, !bid, !logout and !end";
 
         } else {
             return "Unknown Command!";
         }
 
     }
 }
