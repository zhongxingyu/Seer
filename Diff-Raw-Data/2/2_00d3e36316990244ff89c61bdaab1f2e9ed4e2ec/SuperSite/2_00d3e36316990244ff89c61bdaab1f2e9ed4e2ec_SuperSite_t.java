 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package longcat.auction.src;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author emesven
  */
 public class SuperSite {
 
     //**********************************
     CustomerCatalogue customerCatalogue;
     AuctionCatalogue auctionCatalogue;
     //**********************************
 
     public SuperSite() {
         customerCatalogue = CustomerCatalogue.getDefault();
         auctionCatalogue = AuctionCatalogue.getDefault();
     }
 
     public CustomerCatalogue getCustomerCatalogue() {
         return customerCatalogue;
     }
 
     public AuctionCatalogue getAuctionCatalogue() {
         return auctionCatalogue;
     }
 
     public void createNewAuction(Customer cust, AuctionObject obj) {
 
         // Customer temp = cust; //Varför temp? **************************************************************
         cust.addMySellAuctionList(obj);
       // auctionCatalogue.add(obj);
         customerCatalogue.update(cust);
     }
 
     public void doBid(Customer cust, Double price, AuctionObject obj) { //Ska vi använda denna? ja
 
 
         if (price > obj.getPrice()) {
             obj.setPrice(price);
             Customer temp = cust; // varför denna?***********************************************************'
             cust.addMyBuyAuctionList(obj);
             customerCatalogue.update(cust);
             auctionCatalogue.update(obj);
         }
     }
 
     protected void initTestData() {
 
         Date today = new Date();
         Customer test1 = new Customer("apa@hej.com", "apa", "password", "11111",
                 "seQuest", "addressgatan1");
         Customer test2 = new Customer("bepa@hej.com", "bepa", "password", "22222",
                 "seQuest", "addressgatan2");
         Customer test3 = new Customer("cepa@hej.com", "cepa", "password", "33333",
                 "seQuest", "addressgatan3");
         Customer test4 = new Customer("depa@hej.com", "depa", "password", "44444",
                 "seQuest", "addressgatan4");
 
         AuctionObject testobj1 = new AuctionObject("Fisk", "info", 10.00, today);
         AuctionObject testobj2 = new AuctionObject("Mås", "info", 101.00, today);
         AuctionObject testobj3 = new AuctionObject("Katt", "info", 102.00, today);
         AuctionObject testobj4 = new AuctionObject("Hest", "info", 103.00, today);
         AuctionObject testobj5 = new AuctionObject("Dawgh", "info", 104.00, today);
         AuctionObject testobj6 = new AuctionObject("ko", "info", 101.00, today);
         AuctionObject testobj7 = new AuctionObject("häst", "info", 102.00, today);
         AuctionObject testobj8 = new AuctionObject("katt", "info", 103.00, today);
 
         customerCatalogue.add(test1);
         customerCatalogue.add(test2);
         customerCatalogue.add(test3);
         customerCatalogue.add(test4);
 
         createNewAuction(test1, testobj1);
         createNewAuction(test1, testobj2);
         createNewAuction(test1, testobj3);
         createNewAuction(test1, testobj4);
         createNewAuction(test1, testobj5);
         createNewAuction(test2, testobj6);
         createNewAuction(test3, testobj7);
         createNewAuction(test4, testobj8);
 
         doBid(test1, 10000.0, testobj6);
         /*
          auctionCatalogue.add(testobj1);
          auctionCatalogue.add(testobj2);
          auctionCatalogue.add(testobj3);
          auctionCatalogue.add(testobj4);
          auctionCatalogue.add(testobj5);
          auctionCatalogue.add(testobj6);
          auctionCatalogue.add(testobj7);
         
          auctionCatalogue.add(testobj8);
          */
         //auctionCatalogue.doBid(test1, 1000.1, testobj6);
 
 
         /*shop.getCustomerRegistry().add(new Customer(new Address("aaa", 1, "aaa"),
 
          Customer c = shop.getCustomerRegistry().getByName("arne").get(0);
          c.addProductToCart(shop.getProductCatalogue().getByName("banana").get(0));
          c.addProductToCart(shop.getProductCatalogue().getByName("apple").get(0));
          c.addProductToCart(shop.getProductCatalogue().getByName("pear").get(0));
 
          shop.getOrderBook().add(new PurchaseOrder(c, c.getCart().getAsOrderItems()));*/
 
     }
 }
