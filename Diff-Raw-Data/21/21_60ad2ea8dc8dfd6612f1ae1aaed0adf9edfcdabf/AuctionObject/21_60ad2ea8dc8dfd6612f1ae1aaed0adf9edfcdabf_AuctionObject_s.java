 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package longcat.auction.src;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Date;
 import java.util.HashMap;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /**
  *
  * @author Emelie Svensson, Alexander Lissenko
  */
 
 @Entity
 public class AuctionObject {
     
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id; //uncomment when using database ****************************'
     
     private String title;
     private String info;
     private Double price;
     @Temporal(TemporalType.DATE)
     private Date expireDate; 
     //public Customer creator;
     private HashMap<Customer, Double> bidderMap; //för att kunna presentera listan på smidigt sätt
     private List<HashMap> bidderList;
     private List<Customer> flagList;
     //private Long id;
 
     public AuctionObject() {
     }
     
     public AuctionObject(String title, String info, Double price, Date expireDate){
         //this.creator = creator;
         this.title = title;
         this.info = info;
         this.price = price;
         this.expireDate = expireDate;
         bidderMap = new HashMap<Customer, Double>();
         bidderList = new ArrayList<HashMap>();
     }
     
     public AuctionObject(Long id, String title, String info, Double price, Date expireDate){
         this.id=id;
         this.title = title;
         this.info = info;
         this.price = price;
         this.expireDate = expireDate;
         bidderMap = new HashMap<Customer, Double>();
         bidderList = new ArrayList<HashMap>();
     }
     
     public void addFlag(Customer c){
         //implementeras senare om tid finns
     }
     
     public boolean setBid(Customer bidder, Double price){
         if(this.price < price) {
             //concurrency-problem! löses av ejb senare
             bidderMap.put(bidder, price);
             bidderList.add(bidderMap);
             this.price += price;
             return true;
         }
         else {
             //nåt felmeddelande, bud kan ej vara under aktuellt bud
             System.out.println("Bud kan ej vara under " + this.price);//tillfälligt
             return false;
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
     public HashMap<Customer, Double> getBidderMap(){
         return bidderMap;
     }
     public List<Customer> getFlagList(){
         return flagList;
     }
     public Long getId(){
         return id;
     }
     //Behöver kunna ändra pris om tex ingen budar
     public void setPrice(Double price){
         this.price = price;
     }
     
     //för att kunna ändra info
     public void editInfo(String info) {
         this.info = info;
     }
 }
