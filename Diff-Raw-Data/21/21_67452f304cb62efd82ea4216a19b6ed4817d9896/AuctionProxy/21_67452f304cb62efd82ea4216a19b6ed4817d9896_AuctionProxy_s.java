 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package admin.src;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import longcat.auction.src.AuctionObject;
 import longcat.auction.src.Customer;
 
 /**
  *
  * När ska
  *
  * @XmlElement användas?????
  * @author Alexander Lissenko
  * Inga @XmlElement vid setters /Henrik
  */
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.PROPERTY)
 public class AuctionProxy {
 
     private AuctionObject obj;
 
     protected AuctionProxy() {
     }
 
     public AuctionProxy(AuctionObject obj) {
         this.obj = obj;
     }
 
     @XmlElement
     public String getTitle() {
         return obj.getName();
     }
 
    public void setTitle(String title) {
        //admin ska kunna ändra title, lägg till en setTitle i AuctionObject
     }
 
     @XmlElement
     public String getInfo() {
         return obj.getInfo();
     }
 
     public void setInfo(String info) {
         obj.editInfo(info);
     }
 
     @XmlElement
     public Double getPrice() {
         return obj.getPrice();
     }
 
     public void setPrice(Double price) {
         obj.setPrice(price);
     }
 
     @XmlElement
     public Date getExpire() {
         return obj.getExpire();
     }
 
     public void setExpire(Date expire) {
         //lägg till setExpire i AuctionObject, alternativt skit i denna metod (inte så viktig)
     }
 
     /*@XmlElement
      public List<HashMap> getBidder(){
      return obj.getBidder();
      }*/
     /*@XmlElement
      public List<Customer> getFlagList(){
      return obj.getFlagList();
      }*/
     @XmlElement
     public Long getId() {
         return obj.getId();
     }
 }
