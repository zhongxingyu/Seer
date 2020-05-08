 /**
  * Coop Network Tetris — A cooperative tetris over the Internet.
  * 
  * Copyright © 2012  Calle Lejdbrandt, Mattias Andrée, Peyman Eshtiagh
  * 
  * Project for prutt12 (DD2385), KTH.
  */
 package cnt.messages;
 import cnt.game.*;
 import cnt.*;
 
 
 /**
  * This message is broadcasted when a player has dropped out
  * 
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  */
public final class PlayerDropped implements Blackboard.BlackboardMessage
 {
     /**
      * Compatibility versioning for {@link java.io.Serializable}
      */
     private static final long serialVersionUID = 1L;
 	
 	
 	
     /**
      * Constructor
      * 
      * @param  player  The player
      */
     public PlayerDropped(final Player player)
     {
 	assert player != null : "Null is not allowed";
 	this.player = player;
     }
 	
 	
 	
     /**
      * The player
      */
     public final Player player;
 	
 	
 	
     /**
      * {@inheritDoc}
      */
     public String toString()
     {
 	return this.player == null ? "[no player, that's wierd]" : ("Dropped player: " + this.player.toString());
     }
 	
     /**
      * {@inheritDoc}
      */
     public Boolean checkIntegrity()
     {
 	return Boolean.valueOf(this.player != null);
     }
 }
