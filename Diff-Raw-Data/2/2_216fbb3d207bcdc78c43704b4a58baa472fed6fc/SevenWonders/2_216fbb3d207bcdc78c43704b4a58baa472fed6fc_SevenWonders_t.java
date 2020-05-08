 package com.steamedpears.comp3004;
 
 public class SevenWonders {
     public static void main(String[] args){
         new SevenWonders();
     }
 
     ViewFrame view;
     SevenWondersGame game;
 
     public SevenWonders() {
         view = new ViewFrame(this);
     }
 
     public void startGame(boolean isHost,String ipAddress) {
        game =  (new Router(isHost)).getLocalGame();
     }
 
     public void exit() {
         System.exit(0);
     }
 }
