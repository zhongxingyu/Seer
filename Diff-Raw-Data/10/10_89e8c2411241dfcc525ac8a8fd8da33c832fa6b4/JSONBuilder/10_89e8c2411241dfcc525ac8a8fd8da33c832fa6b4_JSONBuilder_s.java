 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package capstone.server;
 
 import capstone.game.GameSession;
 import capstone.player.Player;
 
 /**
  *
  * @author Max
  * Utility class to convert the necessary information from GameSession into a JSON object
  */
 public class JSONBuilder {
     
     public static String buildJSON(GameSession game, RemotePlayer target){
         StringBuilder builder = new StringBuilder();
         
         builder = builder
         .append("{")
         .append(buildPlayerNumber(game, target))
         .append(buildIsTurn(game,target))
         .append(buildGameStatus(game, target))
         .append(buildBoardObject(game))
         .append("}");
         
         return builder.toString();
     }
     
     public static String buildPlayerNumber(GameSession game, RemotePlayer target){
     	StringBuilder builder = new StringBuilder();
     	
     	int playerNumber = game.getPlayerNumber(target);
     	
 		builder = builder.append("\"PlayerNumber\":\"").append(Integer.toString(playerNumber)).append("\"");
 		
 		return builder.toString();
     }
     
     public static String buildIsTurn(GameSession game, RemotePlayer target){
     	StringBuilder builder = new StringBuilder();
     	
     	String turn = Boolean.toString(target.isActive());
     	
 		builder = builder.append("\"isTurn\":\"").append(turn).append("\"");
 		
 		return builder.toString();
     }
     
     public static String buildGameStatus(GameSession game, RemotePlayer target){
     	StringBuilder builder = new StringBuilder();
     	
     	String winner = Integer.toString(game.getCurrentGame().getWinner());
     	
 		builder = builder.append("\"Status\":\"").append(winner).append("\"");
 		
 		return builder.toString();
     }
     
     public static String buildBoardObject(GameSession game){
     	StringBuilder builder = new StringBuilder();
     	
     	builder = builder.append("\"Board\":").append(buildBoardOnly(game));
     	
     	return builder.toString();
     }
     
     //Returns an array of length 9 containing all subgames
     public static String buildBoardOnly(GameSession game){
     	StringBuilder builder = new StringBuilder();
         
     	builder = builder.append("[");
     	for (int a = 0; a < 3; a++){
     		for(int b=0; b<3; b++){
                     //Build each of the subgames
                     int[][] sub = game.getCurrentGame().GetSubBoard(a, b);
                     if(a<2||b<2){
                         builder=builder.append(",");
                     }
                 }
     	}
     	builder = builder.append("]");
     	
     	return builder.toString();
     }
     
     //Returns an array of length 9 that encodes a subgame state
     public static String buildSub(int[][] board){
     	StringBuilder builder = new StringBuilder();
     	builder=builder.append("[");
     	for (int i = 0; i < 3; i++){
     		for(int j=0; j<3; j++){
                    builder=builder.append("\""+Integer.toString(board[i][j])+"\"");
                     if(i<2||j<2){
                         builder=builder.append(",");
                     }
                 }
     	}
     	builder=builder.append("]");
     	return builder.toString();
     }
 }
