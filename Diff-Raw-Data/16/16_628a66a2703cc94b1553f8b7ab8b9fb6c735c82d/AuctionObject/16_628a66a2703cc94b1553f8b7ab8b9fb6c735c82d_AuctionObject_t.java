 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package longcatarmy.src;
 
 import java.util.List;
 import java.util.Date;
 import java.util.HashMap;
 
 /**
  *
 * @author Emelie Svensson, Alexander Lissenko
  */
 public class AuctionObject {
     public String title;
     public String info;
     public Double price;
     public Date expireDate; 
     //public Customer creator;
     public HashMap<Customer, Double> bidderMap; //för att kunna presentera listan på smidigt sätt
     public List<HashMap> bidderList;
     public List<Customer> flagList;
     public Long id;
     
     public AuctionObject(String title, String info, Double price, Date expireDate){
         //this.creator = creator;
         this.title = title;
         this.info = info;
         this.price = price;
         this.expireDate = expireDate;
     }
     
     public void addFlag(Customer c){
         //implementeras senare om tid finns
     }
     
     public void setBid(Customer bidder, Double price){
         if(this.price < price) {
             //concurrency-problem! löses av ejb senare
             bidderMap.put(bidder, price);
             bidderList.add(bidderMap);
             this.price += price;
         }
         else {
             //nåt felmeddelande, bud kan ej vara under aktuellt bud
         }
     }
     public String getTitle(){
         return title;
     }
     public String getInfo(){
         return info;
     }
     public Double getPrice(){
         return price;
     }
     public Date getExpire() {
         return expireDate;
     }
     /*public Customer getCreator(){
         return creator;
     }*/
     public List<HashMap> getBidder(){
         return bidderList;
     }
     public List<Customer> getFlagList(){
         return flagList;
     }
     public Long getId(){
         return id;
     }
 }
