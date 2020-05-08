 package com.steamedpears.comp3004.models;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.steamedpears.comp3004.SevenWonders;
 import com.steamedpears.comp3004.models.assets.AssetMap;
 import com.steamedpears.comp3004.models.assets.AssetSet;
 import com.steamedpears.comp3004.models.players.Player;
 import org.apache.log4j.Logger;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static com.steamedpears.comp3004.models.assets.Asset.*;
 
 public class Card {
     //constants/////////////////////////////////////////////////////
     public static final String PLAYER_LEFT =    "left";
     public static final String PLAYER_RIGHT =   "right";
     public static final String PLAYER_SELF =    "self";
 
     public static final String PROP_CARD_NAME =                 "name";
     public static final String PROP_CARD_COLOR =                "guild";
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
 
     private static Logger log = Logger.getLogger(Card.class);
 
     //static methods////////////////////////////////////////////////
 
     /**
      * Parses the given JSON array describing a list of cards into a Java List of Cards in the same order
      * @param deck JSON array of cards
      * @return the List of Cards
      */
     public static List<Card> parseDeck(JsonArray deck){
         List<Card> cards = new ArrayList<Card>();
         for(JsonElement element: deck){
             cards.add(new Card(element.getAsJsonObject()));
         }
         return cards;
     }
 
     //instance variables////////////////////////////////////////////
     private String color;
     private String name;
     private String image;
     private URL imageURL;
     private int minPlayers;
     private int age;
     //The base amount of assets this card yields
     private AssetMap baseAssets;
     //If multiplierAssets is not null, multiply the values in baseAssets
     //by the number of occurences of these assets in multiplierTargets (a combination of self, left, and right)
     private AssetSet multiplierAssets;
     private Set<String> multiplierTargets;
     //The building the card is free to play for
     private String freeFor;
     //the cost of playing this card
     private AssetMap cost;
     //the resources this card makes discounted
     private AssetSet discountsAssets;
     //the players to which this card's discountsAssets applies to
     private Set<String> discountsTargets;
     //if true, the player may choose only *one* of the keys of baseAssets
     private boolean isChoice;
     private String id;
 
     //constructor///////////////////////////////////////////////////
 
     /**
      * Creates a Card that matches the give JSON object
      * @param obj the JSON object describing this Card
      */
     public Card(JsonObject obj){
         this.name = obj.has(PROP_CARD_NAME) ? obj.getAsJsonPrimitive(PROP_CARD_NAME).getAsString() : "";
         this.color = obj.has(PROP_CARD_COLOR) ? obj.getAsJsonPrimitive(PROP_CARD_COLOR).getAsString() : "";
         this.cost = new AssetMap(obj, PROP_CARD_COST);
         this.minPlayers = obj.has(PROP_CARD_MIN_PLAYERS) ? obj.getAsJsonPrimitive(PROP_CARD_MIN_PLAYERS).getAsInt() : 0;
         this.age = obj.has(PROP_CARD_AGE) ? obj.getAsJsonPrimitive(PROP_CARD_AGE).getAsInt() : 0;
         this.id = this.getName().replace(" ","")+"_"+this.age+"_"+this.minPlayers;
         this.image = SevenWonders.PATH_IMG_CARDS+getId()+SevenWonders.IMAGE_TYPE_SUFFIX;
 
         //figure out what this card actually does
         this.baseAssets = new AssetMap(obj, PROP_CARD_BASE_ASSETS);
         this.multiplierAssets = new AssetSet(obj,PROP_CARD_MULTIPLIER_ASSETS);
         this.multiplierTargets = new AssetSet(obj,PROP_CARD_MULTIPLIER_TARGETS);
         this.discountsAssets = new AssetSet(obj,PROP_CARD_DISCOUNTS_ASSETS);
         this.discountsTargets = new AssetSet(obj,PROP_CARD_DISCOUNTS_TARGETS);
         this.isChoice = obj.has(PROP_CARD_CHOICE) && obj.getAsJsonPrimitive(PROP_CARD_CHOICE).getAsBoolean();
         this.freeFor = obj.has(PROP_CARD_FREE_FOR) ? obj.getAsJsonPrimitive(PROP_CARD_FREE_FOR).getAsString() : "";
     }
 
     //getters////////////////////////////////////////////////////////
 
     /**
      * Gets the name of the Card
      * @return the name of the Card
      */
     public String getName(){
         return name;
     }
 
     /**
      * Gets the colour of the Card
      * @return the colour of the Card
      */
     public String getColor(){
         return color;
     }
 
     /**
      * Gets the URL of the Card's image
      * @return the URL of the Card's image
      */
     public URL getImagePath(){
         if(imageURL==null){
             URL result = Card.class.getResource(image);
             if(result==null){
                log.warn("Missing file " + image);
                 try {
                     result = new URL("file://");
                 } catch (MalformedURLException e) {
                     log.error("Null file is malformed, file loading is compromised", e);
                     System.exit(-1);
                 }
             }
             imageURL = result;
         }
         return imageURL;
     }
 
     /**
      * Gets the minimum number of Players necessary to use this card in a game
      * @return the minimum number of Players necessary to use this card in a game
      */
     public int getMinPlayers(){
         return minPlayers;
     }
 
     /**
      * Gets the age this Card is to be part of
      * @return the age this Card is to be part of
      */
     public int getAge(){
         return age;
     }
 
     /**
      * Gets the name of the building that, if already constructed, makes this Card free to play
      * @return the name of the building that, if already constructed, makes this Card free to play
      */
     public String getFreeFor(){
         return freeFor;
     }
 
     /**
      * Gets the Asset cost of playing this card
      * @return the Asset cost of playing this card
      */
     public AssetMap getCost(Player player){
         for(Card card: player.getPlayedCards()){
             if(card.getName().equals(freeFor)){
                 return new AssetMap();
             }
         }
         return cost;
     }
 
     /**
      * Gets the Assets that this Card will make cheaper to buy from neighbours
      * @return the Assets that this Card will make cheaper to buy from neighbours
      */
     public AssetSet getDiscountsAssets(){
         return discountsAssets;
     }
 
     /**
      * Gets the neighbours for which #getDiscountsAssets applies to
      * @return the neighbours for which #getDiscountsAssets applies to
      */
     public Set<String> getDiscountsTargets(){
         return discountsTargets;
     }
 
     /**
      * Charges the given Player the cost of playing this card
      * @param player the player being charged
      */
     public void playCard(Player player){
         player.changeGold(-getCost(player).get(ASSET_GOLD));
     }
 
     /**
      * Gets the Assets this Card would yield the given Player, if played, before optional choices
      * @param player the player that would play this card
      * @return the Assets
      */
     public AssetMap getAssets(Player player){
         AssetMap result = new AssetMap();
         if(!isChoice){
             int multiplier = 1;
             if(!multiplierAssets.isEmpty()){
                 multiplier = 0;
                 if(multiplierTargets.isEmpty()){
                     multiplierTargets.add(PLAYER_SELF);
                 }
                 for(String target: multiplierTargets){
                     Player targetPlayer;
                     if(target.equals(PLAYER_LEFT)){
                         targetPlayer = player.getPlayerLeft();
                     }else if(target.equals(PLAYER_RIGHT)){
                         targetPlayer = player.getPlayerRight();
                     }else{ //self
                         targetPlayer = player;
                     }
                     Map<String, Integer> targetPlayerAssets = targetPlayer.getConcreteAssets();
                     for(String multiplierAsset: multiplierAssets){
                         multiplier+=targetPlayerAssets.get(multiplierAsset);
                     }
                 }
             }
             for(String asset: baseAssets.keySet()){
                 result.put(asset, baseAssets.get(asset)*multiplier);
             }
         }
         return result;
     }
 
     /**
      * Gets the optional Assets this Card would yield the given Player, if played
      * @param player the player that would play this card
      * @return the Assets
      */
     public AssetSet getAssetsOptional(Player player){
         AssetSet result = new AssetSet();
         if(isChoice){
             for(String asset: baseAssets.keySet()){
                 result.add(asset);
             }
         }
         return result;
     }
 
     /**
      * Gets the unique ID for this Card
      * @return a unique ID
      */
     public String getId(){
         return this.id;
     }
 
     @Override
     public int hashCode(){
         return getId().hashCode();
     }
 
     /**
      * Determines whether the given Player can afford this Card, if they perform the purchases stated in the command
      * @param player the Player that would play the Card
      * @param command the command that would play the Card
      * @return whether the Player can afford this Card
      */
     public boolean canAfford(Player player, PlayerCommand command) {
         AssetMap costLessBase = new AssetMap();
         AssetMap playerAssets = player.getAssets();
 
        costLessBase.add(cost);
         costLessBase.subtract(playerAssets);
         costLessBase.subtract(command.leftPurchases);
         costLessBase.subtract(command.rightPurchases);
 
         return costLessBase.isEmpty() ||  costLessBase.existsValidChoices(player.getOptionalAssetsComplete());
     }
 
 
 }
