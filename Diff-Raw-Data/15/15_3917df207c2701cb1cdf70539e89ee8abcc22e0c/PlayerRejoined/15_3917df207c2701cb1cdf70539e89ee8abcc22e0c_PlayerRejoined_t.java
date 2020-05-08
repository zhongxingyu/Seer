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
  * This message is broadcasted when a player has rejoined the game session
  * 
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  * @author  Calle Lejdbrandt, <a href="mailto:callel@kth.se">callel@kth.se</a>
  */
 public final class PlayerRejoined extends PlayerJoined 
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
    public PlayerRejoined(final Player player)
     {
 	assert player != null : "Null is not allowed";
 	this.player = player;
     }
 }
