 package civ;
 
 public class Round{
     private static int number = 0;
     private static int turn = 0;
    private static Player[] players = {Player.getInstance("Player")};
     private static Player activePlayer = null;
     private static GameMap gm;
     
     static void next(){
         if(number%players.length == 0){
             ++turn;
         }
         activePlayer = players[number%players.length];
         gm = GameMap.getInstance();
         if(gm.isInited()){
             gm.resetUnits();
             spawnBarbarian();
         }
         ++number;
     }
 
     public static void addPlayer(Player player){
         Player[] temp = new Player[players.length+1];
         int i=0;
         for(Player tempPlayer : players){
             temp[i++] = tempPlayer;
         }
         temp[i] = player;
     }
 
     public static void delPlayer(Player player){
         Player[] temp = new Player[players.length-1];
         int i=0;
         for(Player tempPlayer : players){
             if(!tempPlayer.equals(player)){
                 temp[i++] = tempPlayer;
             }
         }
     }
 
     private static void spawnBarbarian(){
         // Make random number chance here
         if(true){
             for(int y=0; y<gm.getHeight(); ++y){
                 for(int x=0; x<gm.getWidth(); ++x){
                     Tile t = gm.getTile(x, y);
                     // Make another random chance here
                     if(!t.getTerrain().getName().equals("Sea") &&
                     !t.getTerrain().getName().equals("Ocean") &&
                     !t.isExplored() &&
                     !t.hasUnit()){
                         t.setUnit(new Barbarian(t));
                         return;
                     }
                 }
             }
         }
     }
 
     public static Player[] getPlayers(){
         return players;
     }
 
     public static Player getActivePlayer(){
         return activePlayer;
     }
 }
