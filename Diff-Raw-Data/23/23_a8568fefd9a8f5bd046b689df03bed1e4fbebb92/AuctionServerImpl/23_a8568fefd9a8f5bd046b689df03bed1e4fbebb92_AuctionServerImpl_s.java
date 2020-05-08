 package Server;
 
 import Client.IAuctionListener;
 import java.io.Serializable;
 import java.rmi.*;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  * Implements server methods responsible for auction system.
  * 
  */
 final class AuctionServerImpl extends Observable implements IAuctionServer{
     
    /**
     * Represents an observer (client).
     * 
     * @author Patryk Orwat
     */
     private class WrappedObserver implements Observer, Serializable {
         private static final long serialVersionUID = 1L;
         private IAuctionListener ro = null;
         private String itemName = null;
 
         public WrappedObserver(IAuctionListener ro, String name) {
             this.ro = ro;
             this.itemName = name;
         }
 
         @Override
         public void update(Observable o, Object arg) {
             try {
                 if(this.itemName.equals(((Item)arg).getItemName())){
                     ro.update((Item)arg);
                 }
             } catch (RemoteException err) {
                 System.out.println("Remote exception observer:" + this);
             }
         }
     }
     
     private static volatile AuctionServerImpl instance = null;
     private ArrayList<Item> items;
     
     /** 
      * Singleton pattern implementation.
      * source: http://en.wikipedia.org/wiki/Singleton_pattern
      * "should not be used prior to J2SE 5.0"
      * 
      * @return AuctionServerImpl the only object in the application
      * @throws RemoteException 
      */
     public static AuctionServerImpl getInstance() throws RemoteException {
         if (instance == null) {
             synchronized (AuctionServerImpl.class) {
                 if (instance == null) {
                     instance = new AuctionServerImpl();
                 }
             }
         }
         return instance;
     }
     
     /**
      * Private constructor of the class to prevent the creation of more than one
      * object (singleton pattern).
      * 
      * @throws RemoteException 
      */
     private AuctionServerImpl() throws RemoteException {
         super();
         items = new ArrayList<>();
         items.add(new Item("Jan Marcinowski", "Budzik", "Dobry budzik.", 20.0, 30.0, 14));
         items.add(new Item("Piotr Piotrowski", "Laptop", "Kiepski laptop z 2007 roku.", 10.0, 30.0, 30));
        items.add(new Item("Krzysztof Jackowski", "Kubek", "Bardzo pojemny pojemnik na napoje.", 1.0, 30.0, 30));
     }
     
     /**
      * Creates a new Item object and adds to the list of items.
      * 
      * @param ownerName owner name
      * @param itemName  item name
      * @param itemDesc  iten description
      * @param startBid  start bid
      * @param maxBid    max bid
      * @param auctionTime aution time
      * @throws RemoteException 
      */
     @Override
     public void placeItemForBid(String ownerName, String itemName, String itemDesc, double startBid, double maxBid, int auctionTime) throws RemoteException {
         items.add(new Item(ownerName, itemName, itemDesc, startBid, maxBid, auctionTime));
     }
     
     /**
      * Bids one Item. If an Item is changed, observers are noitfied.
      * 
      * @param bidderName    name of the bidder
      * @param itemName      name of the item
      * @param bid           value of the bid
      * @throws RemoteException 
      */
     @Override
     public void bidOnItem(String bidderName, String itemName, double bid) throws RemoteException {
         for(Item item:items){
             if(item.getItemName().matches(itemName)  && item.getCurrentBid() < bid){
                 item.setCurrentBid(bid);
                 item.setWinnerName(bidderName);
                 if(item.getMaxBid() <= bid){
                     item.setAuctionTime(0);
                 }
                 setChanged();
                 notifyObservers(item);
             }
         }
     }
     
     /**
      * Returns a list of items.
      * 
      * @return ArrayList<Item> list of items
      * @throws RemoteException 
      */
     @Override
     public ArrayList<Item> getItems() throws RemoteException {
         return items;
     }
     
     /**
      * Register a client to observe an Item (observer pattern).
      * 
      * @param al       client object
      * @param itemName Item name
      * @throws RemoteException 
      */
     @Override
     public void registerListener(IAuctionListener al, String itemName) throws RemoteException {
         System.out.println("Registered new client for: "+itemName);
         WrappedObserver mo = new WrappedObserver(al, itemName);
         addObserver(mo);
         setChanged();
         notifyObservers(items.get(0));
     }
 }
