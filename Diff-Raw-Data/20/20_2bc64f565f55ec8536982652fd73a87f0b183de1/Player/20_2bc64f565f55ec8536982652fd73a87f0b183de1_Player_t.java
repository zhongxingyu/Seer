 package com.steamedpears.comp3004.models;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import static com.steamedpears.comp3004.models.PlayerCommand.PlayerCardAction.*;
 import static com.steamedpears.comp3004.models.Asset.*;
 
 public abstract class Player extends Thread{
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
 
     //constructor///////////////////////////////////////////////////////////
     public Player(Wonder wonder, SevenWondersGame game){
         this.wonder = wonder;
         this.game = game;
         this.playedCards = new ArrayList<Card>();
         this.gold = 0;
         this.militaryResults = new ArrayList<Integer>();
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
             if(command.action.equals(BUILD)){
                 buildWonder(command.card);
             }else if(command.action.equals(PLAY)){
                 playCard(command.card);
             }else if(command.action.equals(DISCARD)){
                 discardCard(command.card);
             }else if(command.action.equals(UNDISCARD)){
                 undiscard(command.card);
             }else if(command.action.equals(PLAY_FREE)){
                 playFree(command.card);
             }
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
 
     public final List<Card> getHand(){
         return hand;
     }
 
     public final int getGold(){
         return gold;
     }
 
     public final List<Integer> getMilitaryResults(){
         return militaryResults;
     }
 
     public final PlayerCommand getCurrentCommand(){
         return currentCommand;
     }
 
     public final Map<String, Integer> getAssets(){
         //TODO: get the assets a player definitely has
 
         return null;
     }
 
     public final List<Map<String, Integer>> getOptionalAssetsComplete(){
         //TODO: get a list of all the asset choices a player can make
 
         return null;
     }
 
     public final Map<String, Integer> getOptionalAssetsSummary(){
         //TODO: get a map representing the total of all the asset choices a player can make
         //e.g. wood/stone and stone/clay -> wood:1, stone:2, clay:1
         return null;
     }
 
     public final Map<String, Integer> getAssetsTradeable(){
         //TODO: get the tradeable assets a player definitely has
 
         return null;
     }
 
     public final List<Map<String, Integer>> getOptionalAssetsCompleteTradeable(){
         //TODO: get a list of all the tradeable asset choices a player can make
 
         return null;
     }
 
     public final int getCountOfAsset(String assetName){
         //TODO: make this suck less, maybe? Do we even want this method?
         return getAsset(getAssets(), assetName);
     }
 
 }
