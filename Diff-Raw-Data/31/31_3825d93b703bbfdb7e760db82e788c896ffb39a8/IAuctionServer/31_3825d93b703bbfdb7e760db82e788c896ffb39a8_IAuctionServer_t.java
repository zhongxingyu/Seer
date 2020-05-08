 package Server;
 
 import Client.IAuctionListener;
 import java.rmi.*;
 import java.util.ArrayList;
 
 /**
  * Describes methods provided by AuctionServerImpl. Necessary for RMI. 
  * 
  */
 public interface IAuctionServer extends Remote {
     
     /**
      * Creates a new Item object and adds to the list of items.
      * 
      * @param ownerName owner name
      * @param itemName  item name
     * @param itemDesc  item description
      * @param startBid  start bid
      * @param maxBid    max bid
     * @param auctionTime auction time
      * @throws RemoteException 
      */
     public void placeItemForBid(String ownerName, String itemName, String itemDesc, double startBid, double maxBid, int auctionTime) 
 	throws RemoteException;
 		
     /**
     * Bids one Item. If an Item is changed, observers are notified.
      * 
      * @param bidderName    name of the bidder
      * @param itemName      name of the item
      * @param bid           value of the bid
      * @throws RemoteException 
      */
     public void bidOnItem(String bidderName, String itemName, double bid) throws RemoteException;
 	
     /**
      * Returns a list of items.
      * 
      * @return ArrayList<Item> list of items
      * @throws RemoteException 
      */
     public ArrayList<Item> getItems() throws RemoteException;
 
     /**
      * Register a client to observe an Item (observer pattern).
      * 
      * @param al       client object
      * @param itemName Item name
      * @throws RemoteException 
      */
     public void registerListener(IAuctionListener al, String itemName) throws RemoteException;
 }
