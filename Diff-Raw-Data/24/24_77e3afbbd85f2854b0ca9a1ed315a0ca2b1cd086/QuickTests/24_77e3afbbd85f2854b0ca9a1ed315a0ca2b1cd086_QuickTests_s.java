 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package longcatarmy.src;
 
 import java.util.Date;
 import java.util.List;
 import longcatarmy.src.*;
 
 /**
  *
  * @author William Axhav Bratt, Emelie Svensson
  */
 public class QuickTests {
     
     public static void main(String[] args)
     {
         SuperSite site = SuperSite.getInstance();
         Date today = new Date();
         System.out.println(today.getTime());            
         
         Customer testCust= new Customer("test@testing.com", "test1", "testpass", "031133742","teeeesstt","test street 1");
         Customer testCust2= new Customer("tevil@bad.com", "evil", "testpass", "031133742","teeeesstt","evil street 1");
         Customer testCust3= new Customer("test3@testing.com", "test3", "testpass", "031133742","teeeesstt","test street 3");
         
         site.addCustomer(testCust);
         site.addCustomer(testCust2);
         site.addCustomer(testCust3);
         
         
         //Customer creator, String title, String info, Double price, Date expireDate
         AuctionObject obj1 = new AuctionObject("hus","hus i pool",3131.3,today);
         AuctionObject obj2 = new AuctionObject("pool","pool till hus",1000.1,today);
         AuctionObject obj3 = new AuctionObject("evilness","evil",1.0,today);
         //System.out.println("test");
         //testCust.addMySellAuctionList(obj1);
         
         
         //testing supersite********************************************************
         site.newAuction(testCust, obj1);
         site.newAuction(testCust, obj2);
         //System.out.println("test");
         System.out.println(site.getAllAuctionsForUser(testCust).get(0).getTitle());
         System.out.println(site.getAllAuctions().get(1).getTitle());
         
         
         System.out.println("Innan: "+site.getAllAuctions().get(0).getPrice());
         obj1.setPrice(1.1);
         site.updateAuction(testCust, obj1);
         System.out.println("Efter: "+site.getAllAuctions().get(1).getPrice());
         
         //Remove
         site.removeAuction(testCust, obj1, false); //not sold
        // site.removeAuction(testCust, obj2);
         if((site.getAllAuctionsForUser(testCust).size())== 0)
             System.out.println("tomt");
         
         //Do a bid
         site.doBid(testCust3, 100.0, obj2); //failed bid
         System.out.println("    Number of items in myBuyAuctionList: " +testCust3.getMyBuyAuctionList().size());
         site.doBid(testCust3, 2000.0, obj2); //bid success
         System.out.println("    Number of items in myBuyAuctionList: " +testCust3.getMyBuyAuctionList().size());
         
         
         //Admin stuff BanCustomer***********************************************************
         site.newAuction(testCust2, obj1);
         //site.newAuction(testCust2, obj2);
        
         site.banCustomer(testCust2);
         site.newAuction(testCust2, obj3);
         
         System.out.println(site.getAllAuctionsForUser(testCust2).size());
         if((site.getAllAuctionsForUser(testCust2).size())== 0)
             System.out.println("banned");
         else
             System.out.println("fel");
         
         
         
         //Test all Customer get/set (set is done in constructor)****************************
           
         System.out.println("Name: "+testCust.getName() +":");
         System.out.println("    Mail:" +testCust.getEmail());
         System.out.println("    Password: " +testCust.getPassword());
         System.out.println("    Phone: " +testCust.getPhoneNr());
         System.out.println("    seqQuest: " +testCust.getSeqQuest());
         System.out.println("    address: " +testCust.getAddress());
         System.out.println("    access: " +testCust.getAccess().toString());
         System.out.println("    Number of items in mySellAuctionList: " +testCust.getMySellAuctionList().size());
         System.out.println("    Number of items in myBuyAuctionList: " +testCust.getMyBuyAuctionList().size());
         System.out.println("    rating: " +testCust.getRating().toString());
         System.out.println("    nrOfSells: " +testCust.getNrOfSells());
         
         site.soldObject(obj2, 2000.0);
         
     }
     
     
     
    
 }
