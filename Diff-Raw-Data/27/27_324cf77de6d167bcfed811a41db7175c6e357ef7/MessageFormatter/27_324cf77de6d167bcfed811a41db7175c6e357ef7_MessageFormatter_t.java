 package com.minecarts.sandandgravel.game;
 
 import org.bukkit.ChatColor;
 
 
 public class MessageFormatter {
     private static String prefix = String.format(ChatColor.GOLD + "[%s]: " + ChatColor.WHITE,"SandAndGravel");
     private static String format = prefix + "%s";
     
     
     public static String format(String msg){
         return String.format(format, msg);
     }
     
     //command messages
     public static class commands{
         public static String clear = String.format(format,"Cleared game board");  
     }
     
     public static class player{
         public static String opponentLeft = format("Your opponent has left the game area.");
         
         public static String joinPlayer(String type){
             return format(String.format("You are now the %s player!",type));
         }
         public static String leftPlayer(String type){
             return format(String.format("You are no longer the %s player!",type));
         }
     }
     
     public static class game{
         public static String gameBegin = format(ChatColor.YELLOW + "Both players are READY! Lets the games begin!");
         public static String yourTurn = format("It is now your turn.");
        public static String notYourTurn = format("Please wait. It is " + ChatColor.RED + "NOT" + ChatColor.WHITE + " your turn.");
         public static String gameTie = format("The game was a tie.");
         public static String maxColumn = format("That column is full.");
         
         public static String playerWaiting(String type){
             return format(String.format("You are waiting for a %s player.",type));
         }
         public static String playerWon(String type){
             return format(String.format("%s won! Game will reset in 5 seconds.",type));
         }
     }
     
 }
