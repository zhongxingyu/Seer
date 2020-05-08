 package com.steamedpears.comp3004.models;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.steamedpears.comp3004.SevenWonders;
 
 import javax.swing.*;
 import java.awt.Image;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static com.steamedpears.comp3004.models.Asset.*;
 
 public class Card {
     //constants/////////////////////////////////////////////////////
     public static final String PLAYER_LEFT =    "left";
     public static final String PLAYER_RIGHT =   "right";
     public static final String PLAYER_SELF =    "self";
 
     public static final String PROP_CARD_NAME =                 "name";
     public static final String PROP_CARD_COLOR =                "guild";
     public static final String PROP_CARD_IMAGE =                "image";
     public static final String PROP_CARD_COST =                 "cost";
     public static final String PROP_CARD_MIN_PLAYERS =          "players";
     public static final String PROP_CARD_AGE =                  "age";
     public static final String PROP_CARD_BASE_ASSETS =          "baseAssets";
     public static final String PROP_CARD_MULTIPLIER_ASSETS =    "multiplierAssets";
     public static final String PROP_CARD_MULTIPLIER_TARGETS =   "multiplierTargets";
     public static final String PROP_CARD_DISCOUNTS_ASSETS =     "discountsAssets";
     public static final String PROP_CARD_DISCOUNTS_TARGETS =    "discountsTargets";
     public static final String PROP_CARD_CHOICE =               "isChoice";
     public static final String PROP_CARD_FREE_FOR =             "freeFor";
 
     //static methods////////////////////////////////////////////////
     public static List<Card> parseDeck(JsonArray deck){
         List<Card> cards = new ArrayList<Card>();
         for(JsonElement element: deck){
             cards.add(new Card(element.getAsJsonObject()));
         }
         return cards;
     }
 
     public static List<List<Card>> generateRandomDeck(List<Card> cards, int numPlayers){
         //TODO: implement this
         return null;
     }
 
     //instance variables////////////////////////////////////////////
     private String color;
     private String name;
     private Image image;
     private int minPlayers;
     private int age;
     //The base amount of assets this card yields
     private Map<String, Integer> baseAssets;
     //If multiplierAssets is not null, multiply the values in baseAssets
     //by the number of occurences of these assets in multiplierTargets (a combination of self, left, and right)
     private Set<String> multiplierAssets;
     private Set<String> multiplierTargets;
     //The building the card is free to play for
     private String freeFor;
     //the cost of playing this card
     private Map<String, Integer> cost;
     //the resources this card makes discounted
     private Set<String> discountsAssets;
     //the players to which this card's discountsAssets applies to
     private Set<String> discountsTargets;
     //if true, the player may choose only *one* of the keys of baseAssets
     private boolean isChoice;
     private String id;
 
     //constructor///////////////////////////////////////////////////
     public Card(JsonObject obj){
         this.name = obj.has(PROP_CARD_NAME) ? obj.getAsJsonPrimitive(PROP_CARD_NAME).getAsString() : "";
         this.color = obj.has(PROP_CARD_COLOR) ? obj.getAsJsonPrimitive(PROP_CARD_COLOR).getAsString() : "";
         this.cost = convertJSONToAssetMap(obj, PROP_CARD_COST);
         this.minPlayers = obj.has(PROP_CARD_MIN_PLAYERS) ? obj.getAsJsonPrimitive(PROP_CARD_MIN_PLAYERS).getAsInt() : 0;
         this.age = obj.has(PROP_CARD_AGE) ? obj.getAsJsonPrimitive(PROP_CARD_AGE).getAsInt() : 0;
         this.image = new ImageIcon(SevenWonders.PATH_IMG+getId()+".png").getImage();
 
         //figure out what this card actually does
         this.baseAssets = convertJSONToAssetMap(obj,PROP_CARD_BASE_ASSETS);
         this.multiplierAssets = convertJSArrayToSet(obj,PROP_CARD_MULTIPLIER_ASSETS);
         this.multiplierTargets = convertJSArrayToSet(obj,PROP_CARD_MULTIPLIER_TARGETS);
         this.discountsAssets = convertJSArrayToSet(obj,PROP_CARD_DISCOUNTS_ASSETS);
         this.discountsTargets = convertJSArrayToSet(obj,PROP_CARD_DISCOUNTS_TARGETS);
         this.isChoice = obj.has(PROP_CARD_CHOICE) && obj.getAsJsonPrimitive(PROP_CARD_CHOICE).getAsBoolean();
         this.freeFor = obj.has(PROP_CARD_FREE_FOR) ? obj.getAsJsonPrimitive(PROP_CARD_FREE_FOR).getAsString() : "";
        this.id = this.getName().replace(" ","")+"_"+this.age+"_"+this.minPlayers;
     }
 
     //getters////////////////////////////////////////////////////////
     public String getName(){
         return name;
     }
 
     public String getColor(){
         return color;
     }
 
     public Image getImage(){
         return image;
     }
 
     public int getMinPlayers(){
         return minPlayers;
     }
 
     public int getAge(){
         return age;
     }
 
     public String getFreeFor(){
         return freeFor;
     }
 
     public Map<String, Integer> getCost(){
         return cost;
     }
 
     public Set<String> getDiscountsAssets(){
         return discountsAssets;
     }
 
     public Set<String> getDiscountsTargets(){
         return discountsTargets;
     }
 
     public Map<String, Integer> getAssets(Player player){
         //TODO: compute the assets this card yields if played by this player
         return null;
     }
 
     public Set<String> getAssetsOptional(Player player){
         //TODO: compute the list of assets this card yields if it isChoice
 
         return null;
     }
 
     public String getId(){
         return this.id;
     }
 
     @Override
     public int hashCode(){
         return getId().hashCode();
     }
 }
