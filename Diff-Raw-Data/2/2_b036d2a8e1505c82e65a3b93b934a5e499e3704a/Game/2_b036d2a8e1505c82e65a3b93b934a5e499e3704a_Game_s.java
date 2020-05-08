 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ogo.spec.game.lobby;
 
 import ogo.spec.game.multiplayer.GameProto.Token;
 import ogo.spec.game.multiplayer.client.TokenChangeListener;
 
 /**
  * Main game class.
  */
 public class Game implements TokenChangeListener
 {
 
     int nextId;
     long lastMessage = -1;
     int counter = 0;
 
     public Game()
     {
     }
 
     /**
      * Merge info into the token.
      *
      * This method will merge the two token chains. One from the current game
      * state, and one from the token sent by the previous host. The data from
      * the previous token should be preferred.
      *
      * @param token Token to be processed
      */
    public void mergeInfo(Token.Builder token)
     {
         return token;
     }
 
     Token.Builder copyToken(Token token)
     {
         Token.Builder builder = Token.newBuilder();
 
         builder.mergeFrom(token);
 
         return builder;
     }
 
     /**
      * Keep stats.
      */
     void runStats()
     {
         counter++;
         long time = System.currentTimeMillis();
         if(lastMessage == -1 || time - lastMessage >  1000){
             long diff = time - lastMessage;
             System.out.println("TPS: " + counter + "/" + diff + " = " + 1000.0*counter/diff);
             lastMessage = time;
             counter = 0;
         }
     }
 
     public Token tokenChanged(Token token)
     {
         runStats();
         nextId = token.getLastId();
 
         // first copy the token
         Token.Builder builder = copyToken(token);
 
         mergeInfo(builder);
 
         builder.setLastId(nextId);
 
         return builder.build();
     }
 }
