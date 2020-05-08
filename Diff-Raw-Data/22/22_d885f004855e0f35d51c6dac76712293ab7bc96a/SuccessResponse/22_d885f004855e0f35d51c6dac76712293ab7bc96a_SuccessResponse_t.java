 package com.custardcoding.skipbo.beans.api;
 
import com.custardcoding.skipbo.beans.Game;
 import com.custardcoding.skipbo.beans.PlayerNumber;
 import com.fasterxml.jackson.annotation.JsonInclude;
 
 /**
  *
  * @author Custard
  */
 @JsonInclude(JsonInclude.Include.NON_NULL)
 public class SuccessResponse extends Response {
    private final Game game;
     private final boolean endTurn;
     private final PlayerNumber winner;
     
    public SuccessResponse(Game game, boolean endTurn, PlayerNumber winner) {
        this.game = game;
         success = true;
         this.endTurn = endTurn;
         this.winner = winner;
     }
 
    public Game getGame() {
        return game;
    }

     public boolean isEndTurn() {
         return endTurn;
     }
 
     public PlayerNumber getWinner() {
         return winner;
     }
 }
