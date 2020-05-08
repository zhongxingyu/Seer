 package org.walkingarchive.backend.model.card;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.walkingarchive.backend.model.security.User;
 
 public class Deck {
     private Integer id;
     private List<Card> collection = new ArrayList<Card>();
     private User owner;
     private String name;
     
     public Deck() {}
     
     public Deck(User owner, String name) {
         this.owner = owner;
         this.name = name;
     }
 
     public Deck(User owner, List<Card> collection) {
         this.owner = owner;
         this.collection = collection;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
     
     public User getOwner() {
         return owner;
     }
     
     public List<Card> getCollection() {
         return collection;
     }
     
     public void addCardToCollection(Card card) {
         collection.add(card);
     }
     
     public void removeCardFromCollection(Card card) {
         collection.remove(card);
     }
     
     public boolean contains(Card card) {
         return collection.contains(card);
     }
     
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setCollection(List<Card> collection) {
         this.collection = collection;
     }
 
     public void setOwner(User owner) {
         this.owner = owner;
     }
 
     public List<Card> getCardsWithType(String type) {
         List<Card> result = new ArrayList<Card>();
         
         for(Card card : collection) {
             if(card.getType() == type) {
                 result.add(card);
             }
         }
         
         return result;
     }
 }
