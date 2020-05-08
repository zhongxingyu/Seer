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
 
 public class AuctionClient extends UnicastRemoteObject implements IAuctionListener{
 
     Remote ro;
     IAuctionServer ser;
     private static final long serialVersionUID = 1L;
     HashMap<String, String> bidList;
     
     
     protected AuctionClient() throws RemoteException {
         super();
     }
 
     public AuctionClient(String uri) throws RemoteException, MalformedURLException, NotBoundException {
         super();
         ro = Naming.lookup(uri);
         ser = (IAuctionServer) ro;
         bidList = new HashMap<>();
     }
 
     public void placeItemForBid(String ownerName, String itemName, String itemDesc, double startBid, double maxBid, int auctionTime) throws RemoteException {
         ser.placeItemForBid(ownerName, itemName, itemDesc, startBid, maxBid, auctionTime);
     }
 
     public void bidOnItem(String bidderName, String itemName, double bid) throws RemoteException {
         ser.bidOnItem(bidderName, itemName, bid);
     }
 
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
                     new WaitAndBid(item, bidderName);
                     break;
                 }                    
             }
         }
     }
  
     public void getItems() throws RemoteException {
 
         ArrayList<Item> items = ser.getItems();
 
         for (Item item : items) {
             System.out.println("Owner: " + item.getOwnerName());
             System.out.println("Item Name: " + item.getItemName());
             System.out.println("Item Description: " + item.getItemDesc());
             System.out.println("Start Bid: " + item.getStartBid());
             System.out.println("Max Bid: " + item.getMaxBid());
             System.out.println("Auction Time: " + item.getAuctionTime());
             System.out.println("Winner: " + item.getWinnerName());
             System.out.println("-----------------------------------------------");
         }
     }
 
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
         System.out.println("Winner: " + item.getWinnerName());
         System.out.println("###############################################");
        if(bidList.containsKey(item)){
             bidAndRoll(item, bidList.get(item));
         }
     }
     
     public void bidAndRoll(Item item, String bidderName) throws RemoteException{
       bidOnItem(bidderName, item.getItemName(), item.getCurrentBid()+1);     
     }
 
     public void printHelp() {
         System.out.println("Type a command to proceed:");
         System.out.println("1 - Add a new auction");
         System.out.println("2 - Bid on an item");
         System.out.println("3 - Print items");
         System.out.println("4 - Adds listener");
     }
 
     public static void main(String args[]) {
         System.out.println("Auction RMI v1");
         if (args.length == 0 || !args[0].startsWith("rmi:")) {
             System.err.println("Usage: java AuctionClient rmi://host.domain:port/auction");
             return;
         }
 
         try {
             // The main app code
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
