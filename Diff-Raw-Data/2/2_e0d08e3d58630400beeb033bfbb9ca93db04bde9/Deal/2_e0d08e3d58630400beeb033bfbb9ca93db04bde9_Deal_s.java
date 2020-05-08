 package org.nobel.highriseapi.entities;
 
 import java.util.List;
 
 import org.nobel.highriseapi.entities.base.EntityWithName;
 import org.simpleframework.xml.Element;
 import org.simpleframework.xml.ElementList;
 
 public class Deal extends EntityWithName {
 
     public enum PriceType {
         fixed, hour, month, year
     }
 
     public enum Status {
         lost, pending, won;
     }
 
     private static final long serialVersionUID = 4575876434128342385L;
 
     @Element
     private String background;
 
    @Element
     private Category category;
 
     @Element
     private String currency;
 
     @ElementList(name = "parties")
     private List<Party> parties;
 
     @Element
     private Party party;
 
     @Element
     private Integer price;
 
     @Element(name = "price-type", required = false)
     private PriceType priceType;
 
     @Element
     private Status status;
 
     public String getBackground() {
         return background;
     }
 
     public org.nobel.highriseapi.entities.Category getCategory() {
         return category;
     }
 
     public String getCurrency() {
         return currency;
     }
 
     public List<Party> getParties() {
         return parties;
     }
 
     public Party getParty() {
         return party;
     }
 
     public Integer getPrice() {
         return price;
     }
 
     public PriceType getPriceType() {
         return priceType;
     }
 
     public Status getStatus() {
         return status;
     }
 
     public void setBackground(String background) {
         this.background = background;
     }
 
     public void setCategory(Category category) {
         this.category = category;
     }
 
     public void setCurrency(String currency) {
         this.currency = currency;
     }
 
     public void setParties(List<Party> parties) {
         this.parties = parties;
     }
 
     public void setParty(Party party) {
         this.party = party;
     }
 
     public void setPrice(Integer price) {
         this.price = price;
     }
 
     public void setPriceType(PriceType priceType) {
         this.priceType = priceType;
     }
 
     public void setStatus(Status status) {
         this.status = status;
     }
 
 }
