 package models;
 
 import models.Ship.*;
 import org.codehaus.jackson.JsonNode;
 import play.mvc.WebSocket;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Player {
     private String username;
     private WebSocket.Out<JsonNode> channel;
     private String gameId;
     private List<Ship> ships;
     List<String> shots;
   private boolean autoplay;
 
     private boolean turn;
 
     public Player(String name, WebSocket.Out<JsonNode> out, String id) {
         username = name;
         channel = out;
         gameId = id;
         shots = new ArrayList<String>();
         ships = new ArrayList<Ship>();
         autoplay= false;
     }
 
    protected void addShips() {
         AircraftCarrier aircraftCarrier = new AircraftCarrier();
         Battleship battleship = new Battleship();
         Submarine submarine = new Submarine();
         PatrolShip patrolShip = new PatrolShip();
         Destroyer destroyer = new Destroyer();
 
         ships.add(aircraftCarrier);
         ships.add(battleship);
         ships.add(submarine);
         ships.add(patrolShip);
         ships.add(destroyer);
     }
 
     public String getUsername() {
         return username;
     }
 
     public WebSocket.Out<JsonNode> getChannel() {
         return channel;
     }
 
     public String getGameId() {
         return gameId;
     }
 
     public void setTurn(boolean turn){
         this.turn =turn;
 
     }
 
     public boolean isTurn() {
         return turn;
     }
 
     public List<Ship> getShips() {
         return ships;
     }
 
     public void setShips(List<Ship> ships) {
         this.ships = ships;
     }
     
     public boolean isDefeated(){
         List<Ship> ships = getShips();
         if(ships.get(0).isSunk()&&ships.get(1).isSunk()&&ships.get(2).isSunk()&&ships.get(3).isSunk()&&ships.get(4).isSunk()){
             return true;
         }
        return false;
     }
 
 
     public List<String> getShots() {
         return shots;
     }
 
     public void setShots(List<String> shots) {
         this.shots = shots;
     }
 
     @Override
     public String toString() {
         return username;
     }
 
     public void addShot(String shot) {
         shots.add(shot);
     }
 
     public boolean isStrategyReady(){
         return !ships.isEmpty();
     }
 
     public boolean isAutoplay() {
         return autoplay;
     }
 
     public void setAutoplay(boolean autoplay) {
         this.autoplay = autoplay;
     }
 }
