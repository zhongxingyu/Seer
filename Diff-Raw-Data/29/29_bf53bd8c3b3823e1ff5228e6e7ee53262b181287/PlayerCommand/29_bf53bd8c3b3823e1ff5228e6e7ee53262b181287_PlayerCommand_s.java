 package com.steamedpears.comp3004.models;
 
 import java.util.Map;
 
 public class PlayerCommand{
     public enum PlayerCardAction{
         DISCARD, BUILD, PLAY, UNDISCARD, PLAY_FREE
     }
 
     public PlayerCardAction action;
     public String card;
     public Map<String, Integer> leftPurchases;
     public Map<String, Integer> rightPurchases;
     public PlayerCommand followup; //for if you can perform multiple actions this turn
 }
