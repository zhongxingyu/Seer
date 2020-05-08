 package de.paralleluniverse.Faithcaio.AuctionHouse;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Random;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 
 /**
  * Manages all Auctions
  *
  * @author Faithcaio
  */
 public class AuctionHouseManager {
     
     public final ArrayList<Auction> auctions;
     private static final AuctionHouse plugin = AuctionHouse.getInstance();
     private static final AuctionHouseConfiguration config = plugin.getConfigurations();
     
     public AuctionHouseManager ()
     {
         this.auctions = new ArrayList<Auction>() {};
     }
     
     
     public Auction getAuction(int id) //Get Auction with ID
     { return this.auctions.get(id); }
     
     public ArrayList<Auction> getAuctions(Player player) //Get all Auctions of player
     {
         ArrayList<Auction> auctionlist = new ArrayList<Auction>() {};  
         for (int i = 1;i == this.auctions.size();i++)
         {
             if (this.auctions.get(i).owner == player)
             { auctionlist.add( this.getAuction(i) ); }     
         }
         return auctionlist;
     }
     
     public ArrayList<Auction> getghighestBidder(Player player) //Get all highest Bids of player
     {
         ArrayList<Auction> auctionlist = new ArrayList<Auction>() {};
         for (int i = 1;i == this.auctions.size();i++)
         {
             if (this.auctions.get(i).bids.peek().getBidder() == player)
             { auctionlist.add( this.getAuction(i) ); }
         } 
         return auctionlist;
     }
     
     public ArrayList<Auction> getAuctionItem(ItemStack item) //Get all Auctions with item
     {
         ArrayList<Auction> auctionlist = new ArrayList<Auction>() {};
         for (int i = 1;i == this.auctions.size();i++)
         {
             if (this.auctions.get(i).item == item)
             { auctionlist.add( this.getAuction(i) ); }
         } 
         return auctionlist;    
     }
     
     public ArrayList<Auction> getEndingAuction(int min) //Get soon Ending Auctions
     {
         ArrayList<Auction> auctionlist = new ArrayList<Auction>() {};
         for (int i = 1;i == this.auctions.size();i++)
         {
             if (this.auctions.get(i).auctionEnd - System.currentTimeMillis() <= 1000 * 60 * min)
     
             { auctionlist.add( this.getAuction(i) ); }
         }
         //Array Sortieren nach Datumsstempel? vorgefertigte Funktion
        Collections.sort(auctionlist, new Comparator()
           {   
                public int compare(Object a1,Object a2){
                if (((Auction)a1).auctionEnd <= ((Auction)a2).auctionEnd) return 1;
                 //else
                 return -1;
            } 
        }); 
         return auctionlist; 

     }
 
 
             
     public boolean addAuction(ItemStack item, Player owner, long auctionEnd)
     {
         //Rechte zum Starten ?
         //return false;
         
         //####################################################################
         //################ZU BEARBEITEN#######################################
         int id;
         //Suche nach erstem freien Slot?? 
         id = this.auctions.size();
         //oder Random im fester Menge wenn nicht voll             
         if (this.auctions.size() >= config.auction_maxAuctions) {return false;}
         Random generator = new Random();
         do { id = generator.nextInt(config.auction_maxAuctions); }    
         while (this.auctions.get(id)!= null);
         
         //####################################################################
         this.auctions.add(new Auction(id,item,owner,auctionEnd));
         return true;   
     }
     //overloaded
     public boolean addAuction(ItemStack item, Player owner, long auctionEnd, double startBid)
     {
         //Rechte zum Starten ?
         //return false;
         //inherit addAuction;//von oben
         int id=this.auctions.size(); //Suche nach erstem freien Slot??
         this.auctions.add(new Auction(id,item,owner,auctionEnd,startBid));
         return true;   
     }
     //multiple Auctions
     public boolean addMultiAuction(ItemStack item, Player owner, long auctionEnd, double startBid, int multiAuction)
     {
         //Rechte zum Starten ?
         //return false; 
         for (int i=1; i == multiAuction; i++)
         {
           this.addAuction(item, owner, auctionEnd, startBid);    
         }
         return true;   
     }
     public boolean addMultiAuction(ItemStack item, Player owner, long auctionEnd, int multiAuction)
     {
         //Rechte zum Starten ?
         //return false; 
         for (int i=1; i == multiAuction; i++)
         {
           this.addAuction(item, owner, auctionEnd);    
         }
         return true;   
     }
 }
