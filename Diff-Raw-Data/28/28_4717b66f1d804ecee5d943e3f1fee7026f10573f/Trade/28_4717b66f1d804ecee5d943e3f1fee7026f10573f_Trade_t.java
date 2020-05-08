 package org.walkingarchive.backend.model.trade;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.walkingarchive.backend.model.card.Card;
 import org.walkingarchive.backend.model.security.User;
 
 public class Trade {
     private Integer id;
     private Date created;
     private User owner;
     private List<Card> receivingCards = new ArrayList<Card>();
     private List<Card> givingCards = new ArrayList<Card>();
     private boolean active;
     
     public Trade() {}
     
     public Trade(User user, List<Card> a, List<Card> b, boolean active) {
         created = new Date();
         owner = user;
         for (Card ca : a) {
             receivingCards.add(ca);
         }
         for (Card cb : b) {
             givingCards.add(cb);
         }
         this.active = active;
     }
     
     public Integer getId() {
         return id;
     }
     
     public Date getCreated() {
         return created;
     }
 
     public void setCreated(Date created) {
         this.created = created;
     }
 
     public List<Card> getReceivingCards() {
         return receivingCards;
     }
 
     public void setReceivingCards(List<Card> receivingCards) {
         this.receivingCards = receivingCards;
     }
 
     public List<Card> getGivingCards() {
         return givingCards;
     }
 
     public void setGivingCards(List<Card> givingCards) {
         this.givingCards = givingCards;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public void setOwner(User owner) {
         this.owner = owner;
     }
 
     public String getIdString() {
         return id.toString();
     }
     
     public void addCardToReceiving(Card card) {
         receivingCards.add(card);
     }
     
     public void addCardToGiving(Card card) {
         givingCards.add(card);
     }
     
    public void removeCardFromReceiving(Card card) {
        receivingCards.remove(card);
    }
    
    public void removeCardFromGiving(Card card) {
        givingCards.remove(card);
    }
    
    public void removeAllCardsFromReceiving() {
        for (int i = receivingCards.size() - 1; i >= 0; i--) {
            receivingCards.remove(i);
        }
    }
    
    public void removeAllCardsFromGiving() {
        for (int i = givingCards.size() - 1; i >= 0; i--) {
            givingCards.remove(i);
        }
    }
    
     public User getOwner() {
         return owner;
     }
     
     public Date getDate() {
         return created;
     }
     
     public void setActive(boolean a) {
         active = a;
     }
     
     public boolean getActive() {
         return active;
     }
 }
