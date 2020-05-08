 package Client;
 
 import Server.IAuctionServer;
 import Server.Item;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.*;
 import java.rmi.*;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Main client class which is responsible for user interaction and 
  * communication with server.
  */
 public class AuctionClient extends UnicastRemoteObject implements IAuctionListener{
 
     Remote ro;
     IAuctionServer ser;
     private static final long serialVersionUID = 1L;
     HashMap<String, String> bidList;
     
     
     protected AuctionClient() throws RemoteException {
         super();
     }
 
     /**
      * Creates client object and initializes server connection.
      * 
      * @param uri URI to server
      * @throws RemoteException
      * @throws MalformedURLException
      * @throws NotBoundException
      */
     public AuctionClient(String uri) throws RemoteException, MalformedURLException, NotBoundException {
         super();
         ro = Naming.lookup(uri);
         ser = (IAuctionServer) ro;
         bidList = new HashMap<>();
     }
     
     /**
      * Adds a new auction.
      * 
      * @param ownerName     Owner name
      * @param itemName      Item name
      * @param itemDesc      Item description
      * @param startBid      Start bid
      * @param maxBid        Maximum bid
      * @param auctionTime   Auction time
      * @throws RemoteException 
      */
     public void placeItemForBid(String ownerName, String itemName, String itemDesc, double startBid, double maxBid, int auctionTime) throws RemoteException {
         ser.placeItemForBid(ownerName, itemName, itemDesc, startBid, maxBid, auctionTime);
     }
     
     /**
      * Bids an auction described by a name.
      * 
      * @param bidderName    Name of the bidder
      * @param itemName      Name of the item
      * @param bid           Amount of money
      * @throws RemoteException 
      */
     public void bidOnItem(String bidderName, String itemName, double bid) throws RemoteException {
         ser.bidOnItem(bidderName, itemName, bid);
     }
     
     /**
      * Registers client to observe an item. If the item is changed, update 
      * method if called.
      * 
      * @param itemName  Name of the item
      * @param strategy  Strategy of automatic bidding
      * @throws RemoteException 
      */
     public void registerListener(String itemName, Integer strategy) throws RemoteException {
         ser.registerListener(this, itemName);
         if(strategy == 1){
             String bidderName = new String();
             BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
             System.out.println("Put bidder name");
             try {
                 bidderName = in.readLine();
             } catch (IOException ex) {
                 Logger.getLogger(AuctionClient.class.getName()).log(Level.SEVERE, null, ex);
             }
               for(Item item: ser.getItems()){
                 if(item.getItemName().equals(itemName)){
                     bidList.put(item.getItemName(),bidderName);
                     break;
                 }                    
             }          
         }
         if(strategy == 2){
             String bidderName = new String();
             BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
             System.out.println("Put bidder name");
             try {
                 bidderName = in.readLine();
             } catch (IOException ex) {
                 Logger.getLogger(AuctionClient.class.getName()).log(Level.SEVERE, null, ex);
             }
             for(Item item: ser.getItems()){
                 if(item.getItemName().equals(itemName)){
                     new WaitAndBid(ser, item, bidderName);
                     break;
                 }                    
             }
         }
     }
     
     /**
      * Gets list of items from server and prints it on a screen.
      * 
      * @throws RemoteException 
      */
     public void getItems() throws RemoteException {
 
         ArrayList<Item> items = ser.getItems();
 
         for (Item item : items) {
             System.out.println("Owner: " + item.getOwnerName());
             System.out.println("Item Name: " + item.getItemName());
             System.out.println("Item Description: " + item.getItemDesc());
             System.out.println("Start Bid: " + item.getStartBid());
             System.out.println("Max Bid: " + item.getMaxBid());
             System.out.println("Auction Time: " + item.getAuctionTime());
             System.out.println("Current Bid: " + item.getCurrentBid());
             System.out.println("Winner: " + item.getWinnerName());
             System.out.println("-----------------------------------------------");
         }
     }
     
     /**
      * Update method which is called on server side when an item is 
      * changed. Shows on client side info about update and fires proper
      * strategies.
      * 
      * @param item  The item which was changed
      * @throws RemoteException 
      */
     @Override
     public void update(Item item) throws RemoteException {
         System.out.println("###############################################");
         System.out.println("An auction is updated");
         System.out.println("Owner: " + item.getOwnerName());
         System.out.println("Item Name: " + item.getItemName());
         System.out.println("Item Description: " + item.getItemDesc());
         System.out.println("Start Bid: " + item.getStartBid());
         System.out.println("Max Bid: " + item.getMaxBid());
         System.out.println("Auction Time: " + item.getAuctionTime());
         System.out.println("Current Bid: " + item.getCurrentBid());
         System.out.println("Winner: " + item.getWinnerName());
         System.out.println("###############################################");
         if(bidList.containsKey(item.getItemName())){
             if(!item.getWinnerName().matches(bidList.get(item.getItemName()))){
                 bidOnItem(bidList.get(item.getItemName()), item.getItemName(), item.getCurrentBid()+1.0);
                 System.out.println("Aution: "+item.getItemName()+" auto-bidded to "+item.getCurrentBid()+1.0);
             }
         }
     }
     
     /**
      * Prints available commands which client can run.
      */
     public void printHelp() {
         System.out.println("Type a command to proceed:");
         System.out.println("1 - Add a new auction");
         System.out.println("2 - Bid on an item");
         System.out.println("3 - Print items");
        System.out.println("4 - Add listener");
     }
     
     /**
      * The main client application.
      * 
      * @param args System arguments. The first should be the server URI.
      */
     public static void main(String args[]) {
         System.out.println("Auction RMI v1");
         if (args.length == 0 || !args[0].startsWith("rmi:")) {
             System.err.println("Usage: java AuctionClient rmi://host.domain:port/auction");
             return;
         }
 
         try {
             AuctionClient client = new AuctionClient(args[0]);
 
             String CurLine, ownerName, itemName, itemDesc, tmp;
             double bid, maxbid;
             int auctionTime, strategy;
             BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
             while (true) {
                 client.printHelp();
                 CurLine = in.readLine();
                 switch (CurLine) {
                     case "1":
                         System.out.println("Put Owner name");
                         ownerName = in.readLine();
                         System.out.println("Put Item name");
                         itemName = in.readLine();
                         System.out.println("Put Item description");
                         itemDesc = in.readLine();
                         System.out.println("Put Starting bid");
                         bid = Double.parseDouble(in.readLine());
                         System.out.println("Put Maximum bid");
                         maxbid = Integer.parseInt(in.readLine());
                         System.out.println("Put Time of auction (sec)");
                         auctionTime = Integer.parseInt(in.readLine());
                         client.placeItemForBid(ownerName, itemName, itemDesc, bid, maxbid, auctionTime);
                         break;
                     case "2":
                         System.out.println("Put Owner name");
                         ownerName = in.readLine();
                         System.out.println("Put Item name");
                         itemName = in.readLine();
                         System.out.println("Put bid");
                         bid = Double.parseDouble(in.readLine());
                         client.bidOnItem(ownerName, itemName, bid);
                         break;
                     case "3":
                         client.getItems();
                         break;
                     case "4":
                         System.out.println("Put Item name");
                         itemName = in.readLine();
                         System.out.println("Choose one of the bidding strategies:");
                         System.out.println("0 - Manual");
                         System.out.println("1 - BidAndRoll");
                         System.out.println("2 - WaitAndBid");
                         strategy = Integer.parseInt(in.readLine());
                         client.registerListener(itemName, strategy);
                         break;
                 }
             }
         } catch (MalformedURLException ex) {
             System.err.println(args[0] + " is not a valid RMI URL");
         } catch (RemoteException ex) {
             System.err.println("Remote object threw exception " + ex);
         } catch (NotBoundException ex) {
             System.err.println("Could not find the requested remote object on the server");
         } catch (IOException ex) {
             System.err.println("Error while reading input");
         }
     }
 }
