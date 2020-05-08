 package com.steamedpears.comp3004.models;
 
 import java.util.Map;
 
 public class PlayerCommand{
     public enum PlayerCardAction{
         DISCARD, BUILD, PLAY, UNDISCARD, PLAY_FREE
     }
 
    public static PlayerCommand getNullCommand(Player player){
        PlayerCommand result = new PlayerCommand();
        result.action = PlayerCardAction.DISCARD;
        result.card = player.getHand().get(0).getId();

        return result;
    }

     public PlayerCardAction action;
     public String card;
     public Map<String, Integer> leftPurchases;
     public Map<String, Integer> rightPurchases;
     public PlayerCommand followup; //for if you can perform multiple actions this turn
 }
