 package com.steamedpears.comp3004.models;
 
 public class PlayerCommand{
     public enum PlayerCardAction{
         DISCARD, BUILD, PLAY, UNDISCARD, PLAY_FREE
     }
 
     /**
     * Gets the null command for a given player.  This command is to discard the first cardID in the player's hand, which
      * is always a valid move.
      * @param player the player whose null move to generate
      * @return the generated null move for the given player
      */
     public static PlayerCommand getNullCommand(Player player){
         return new PlayerCommand(PlayerCardAction.DISCARD,player.getHand().get(0).getId());
     }
 
     public PlayerCommand() {
         this(null,null);
     }
 
     public PlayerCommand(PlayerCardAction action, String cardID) {
         this.action = action;
         this.cardID = cardID;
         leftPurchases = new AssetMap();
         rightPurchases = new AssetMap();
     }
 
     public PlayerCardAction action;
     public String cardID;
     public AssetMap leftPurchases;
     public AssetMap rightPurchases;
     public PlayerCommand followup; //for if you can perform multiple actions this turn
 
     @Override
     public String toString() {
         return "PlayerCommand[" + this.action.toString() + " " + this.cardID + "]";
     }
 }
