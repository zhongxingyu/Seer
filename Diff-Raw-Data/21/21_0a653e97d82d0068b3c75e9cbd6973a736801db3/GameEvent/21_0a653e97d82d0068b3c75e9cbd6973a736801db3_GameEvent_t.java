 package com.rachum.amir.skyhiking;
 
 import java.util.Collection;
 
 import com.rachum.amir.skyhiking.players.Player;
 
 
 public class GameEvent {
     
 	public enum Type {
 		GAME_BEGIN, GAME_END, ROUND_BEGIN, ROUND_END, LEVEL_BEGIN,
 		LEVEL_END, MOVE, PAY, DICE_ROLLED;
 	}
 	
 	public GameEvent(final Type type, final Game game) {
 		this.type = type;
         this.context = game;
 	}
     
 	public Game context;
     public Type type;
     public CloudLevel level = null;
     public Boolean didPay = null;
 	public Player currentPlayer = null;
 	public Player winner = null;
 	public Move move = null;
	public Collection<Card> cardsPaid = null;
 }
