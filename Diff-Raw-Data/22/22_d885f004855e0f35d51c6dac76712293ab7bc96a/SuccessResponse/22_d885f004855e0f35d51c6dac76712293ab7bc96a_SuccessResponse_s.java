 package com.custardcoding.skipbo.beans.api;
 
 import com.custardcoding.skipbo.beans.PlayerNumber;
 import com.fasterxml.jackson.annotation.JsonInclude;
 
 /**
  *
  * @author Custard
  */
 @JsonInclude(JsonInclude.Include.NON_NULL)
 public class SuccessResponse extends Response {
     private final boolean endTurn;
     private final PlayerNumber winner;
     
    public SuccessResponse(boolean endTurn, PlayerNumber winner) {
         success = true;
         this.endTurn = endTurn;
         this.winner = winner;
     }
 
     public boolean isEndTurn() {
         return endTurn;
     }
 
     public PlayerNumber getWinner() {
         return winner;
     }
 }
