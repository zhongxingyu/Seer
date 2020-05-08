 package com.steamedpears.comp3004.models;
 
 import com.steamedpears.comp3004.models.players.AIPlayer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static com.steamedpears.comp3004.models.PlayerCommand.PlayerCardAction.*;
 import static com.steamedpears.comp3004.models.Asset.*;
 
 public abstract class Player extends Thread{
     //static variables//////////////////////////////////////////////////////
     private static int currentId = 0;
 
     //static methods////////////////////////////////////////////////////////
     private static int getNextId(){
         return currentId++;
     }
 
     public static Player getAIPlayer(Wonder wonder, SevenWondersGame game){
         //TODO: actually get an AI player
         return new AIPlayer(wonder, game){
 
             @Override
             protected PlayerCommand handleTurn() {
                return null;
             }
         };
     }
 
     //instance variables////////////////////////////////////////////////////
     private Wonder wonder;
     private List<Card> playedCards;
     private Player playerLeft;
     private Player playerRight;
     private SevenWondersGame game;
     private PlayerCommand currentCommand;
     private List<Card> hand;
     private List<Integer> militaryResults;
     private int gold;
     private int id;
 
     //constructor///////////////////////////////////////////////////////////
     public Player(Wonder wonder, SevenWondersGame game, int id){
         this.wonder = wonder;
         this.game = game;
         this.playedCards = new ArrayList<Card>();
         this.hand = new ArrayList<Card>();
         this.gold = 0;
         this.militaryResults = new ArrayList<Integer>();
         this.id = id;
     }
 
     public Player(Wonder wonder, SevenWondersGame game){
         this(wonder, game, getNextId());
     }
 
     private void discardCard(Card card){
         //TODO: remove card from hand, get 3 gold
     }
 
     private void buildWonder(Card card){
         //TODO: buildup wonder, return whether successful
     }
 
     private void playCard(Card card){
         //TODO: play card, return whether successful
     }
 
     private void undiscard(Card card){
         //TODO: take this card out of this discard, and play it
     }
 
     private void playFree(Card card){
         //TODO: take this card out of this discard, and play it
     }
 
     public final void takeTurn(PlayerCommand command) throws Exception {
         if(command!=null){
             Card card = game.getCardById(command.card);
             if(command.action.equals(BUILD)){
                 buildWonder(card);
             }else if(command.action.equals(PLAY)){
                 playCard(card);
             }else if(command.action.equals(DISCARD)){
                 discardCard(card);
             }else if(command.action.equals(UNDISCARD)){
                 undiscard(card);
             }else if(command.action.equals(PLAY_FREE)){
                 playFree(card);
             }
         }
         if(command.followup!=null){
             takeTurn(command.followup);
         }
     }
 
     //extend with a GUI or AI
     //when handleTurn terminates, currentCommand should be set with the Player's desired command
     protected abstract PlayerCommand handleTurn();
 
     public final void run(){
         handleTurn();
     }
 
     //setters///////////////////////////////////////////////////////////////////
     public final void setPlayerLeft(Player playerLeft){
         this.playerLeft = playerLeft;
     }
 
     public final void setPlayerRight(Player playerRight){
         this.playerRight = playerRight;
     }
 
     protected final void setCurrentCommand(PlayerCommand currentCommand){
         this.currentCommand = currentCommand;
     }
 
     public final void setHand(List<Card> hand){
         this.hand = hand;
     }
 
     //getters///////////////////////////////////////////////////////////////////
     public final boolean isValid(PlayerCommand command){
         //TODO: determine if the given command is a valid one for the player to perform
 
         return false;
     }
 
     public final Player getPlayerLeft(){
         return playerLeft;
     }
 
     public final Player getPlayerRight(){
         return playerRight;
     }
 
     // probably not going to break anything if this isn't final
     public List<Card> getHand(){
         return hand;
     }
 
     public final int getGold(){
         return gold;
     }
 
     public final List<Integer> getMilitaryResults(){
         return militaryResults;
     }
 
     public final int getMilitaryDefeats(){
         int result = 0;
         for(int value: getMilitaryResults()){
             if(value==-1){
                 ++result;
             }
         }
 
         return result;
     }
 
     public final PlayerCommand getCurrentCommand(){
         return currentCommand;
     }
 
     public final Wonder getWonder(){
         return wonder;
     }
 
     public final int getPlayerId(){
         return id;
     }
 
     @Override
     public int hashCode(){
         return getPlayerId();
     }
 
     /**
      * gets all the assets that aren't captured in the cards or wonder of the player
      * @return the map
      */
     public final Map<String, Integer> getPrivateAssets(){
         Map<String, Integer> privateAssets = new HashMap<String, Integer>();
         privateAssets.put(ASSET_GOLD, getGold());
         privateAssets.put(ASSET_DEFEAT, getMilitaryDefeats());
 
         return privateAssets;
     }
 
     /**
      * gets all the Assets the player definitely has before any decisions
      * @return the map
      */
     public final Map<String, Integer> getAssets(){
         List<Map<String, Integer>> collectedAssets = new ArrayList<Map<String, Integer>>();
 
         for(Card card: playedCards){
             collectedAssets.add(card.getAssets(this));
         }
 
         collectedAssets.add(wonder.getAssets(this));
 
         collectedAssets.add(getPrivateAssets());
 
         return sumAssets(collectedAssets);
     }
 
     /**
      * get all the asset choices the player can make
      * @return the list of choices
      */
     public final List<Set<String>> getOptionalAssetsComplete(){
         List<Set<String>> collectedAssets = new ArrayList<Set<String>>();
 
         for(Card card: playedCards){
             Set<String> options = card.getAssetsOptional(this);
             if(options!=null && !options.isEmpty()){
                 collectedAssets.add(options);
             }
         }
 
         collectedAssets.addAll(wonder.getOptionalAssetsComplete(this));
 
         return collectedAssets;
     }
 
     /**
      * get a map representing the total of all the asset choices a player can make
      * e.g. wood/stone and stone/clay -> wood:1, stone:2, clay:1
      * @return the map
      */
     public final Map<String, Integer> getOptionalAssetsSummary(){
         List<Set<String>> options = getOptionalAssetsComplete();
 
         Map<String, Integer> result = new HashMap<String, Integer>();
 
         for(Set<String> option: options){
             for(String key: option){
                 if(result.containsKey(key)){
                     result.put(key, result.get(key)+1);
                 }else{
                     result.put(key, 1);
                 }
             }
         }
 
         return result;
     }
 
     /**
      * get a map of all the assets a player can definitely trade before making any decisions
      * @return the map
      */
     public final Map<String, Integer> getAssetsTradeable(){
         List<Map<String, Integer>> collectedAssets = new ArrayList<Map<String, Integer>>();
 
         for(Card card: playedCards){
             if(card.getColor().equals(COLOR_BROWN) || card.getColor().equals(COLOR_GREY)){
                 collectedAssets.add(card.getAssets(this));
             }
         }
 
         collectedAssets.add(wonder.getTradeableAssets());
 
         return sumAssets(collectedAssets);
     }
 
     /**
      * get a list of all the tradeable asset choices a player can make
      * @return the list
      */
     public final List<Set<String>> getOptionalAssetsCompleteTradeable(){
         List<Set<String>> collectedAssets = new ArrayList<Set<String>>();
 
         for(Card card: playedCards){
             if(card.getColor().equals(COLOR_BROWN) || card.getColor().equals(COLOR_GREY)){
                 Set<String> options = card.getAssetsOptional(this);
                 if(options!=null && !options.isEmpty()){
                     collectedAssets.add(options);
                 }
             }
         }
 
         collectedAssets.addAll(wonder.getOptionalAssetsComplete(this));
 
         return collectedAssets;
     }
 
 }
