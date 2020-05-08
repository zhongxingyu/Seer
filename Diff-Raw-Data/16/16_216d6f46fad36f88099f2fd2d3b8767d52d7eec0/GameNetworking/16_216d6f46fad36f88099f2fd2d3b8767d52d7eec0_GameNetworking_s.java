 /**
  * Coop Network Tetris — A cooperative tetris over the Internet.
  * 
  * Copyright © 2012  Calle Lejdbrandt, Mattias Andrée, Peyman Eshtiagh
  * 
  * Project for prutt12 (DD2385), KTH.
  */
 package cnt.network;
 import cnt.game.*;
 import cnt.messages.*;
 import cnt.*;
 
 import java.io.*;
 
 
 /**
  * Game networking layer object
  *
  * @author  Calle Lejdbrandt, <a href="mailto:callel@kth.se">callel@kth.se</a>
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  */
 public class GameNetworking
 {
     //Has default constructor
     
     
     
     /**
      * The previous layer in the protocol stack
      */
     public BlackboardNetworking blackboardNetworking;
     
     /**
      * The next layer in the protocol stack
      */
     public ConnectionNetworking connectionNetworking;
 
     /**
      * The local player
      */
     private Player player = null;
     
     
     
     /**
      * Set {@link BlackboardNetworking} instance to use
      */
     public void setBlackboardNetworking(BlackboardNetworking blackboardNetworking)
     {
 	    this.blackboardNetworking = blackboardNetworking;
     }
     
     
     /**
      * Set {@link ConnectionNetworking} instance to use
      */
     public void setConnectionNetworking(ConnectionNetworking connectionNetworking)
     {
 	    this.connectionNetworking = connectionNetworking;
     }
     
     
     /**
      * Forward a message to the next layer in the protocol stack
      * 
      * @param  message  The message
      * 
      * @throws  IOException  On networking exception
      */
     public void forward(final Serializable message) throws IOException
     {
 	this.connectionNetworking.send(PacketFactory.createBroadcast(message, false));
     }
     
     
     /**
      * Invoke if the local user is sending a chat message
      * 
      * @param  message  The message
      */
     public void chat(final String message)
     {
 	if (this.player != null)
 	    Blackboard.broadcastMessage(new ChatMessage(this.player, message));
     }
     
     
     /**
      * Sets local player
      * 
      * @param  player  The local player
      */
     public void setLocalPlayer(final Player player)
     {
 	this.player = player;
     }
     
     
     /**
      * Wait for and receive a message
      * 
      * @return  The next message
      * 
      * @throws  IOException             On networking exception
      * @throws  ClassNotFoundException  If the message type is not a part of the program
      */
     @SuppressWarnings("unchecked")
     public void receive(Serializable object)
     {
 	try
 	{
 	    if (object instanceof Packet == false)
 		return;
 	   NetworkMessage message = null; 
 	    Packet packet = (Packet)object;
 	    if (packet.getMessage() instanceof Broadcast)
 		    message = packet.getMessage();
 	    else if (packet.getMessage() instanceof Whisper)
 		    message = packet.getMessage();
 		
 	    Blackboard.BlackboardMessage blackboardMessage = (Blackboard.BlackboardMessage)message.getMessage();
 	    if(blackboardMessage.checkIntegrity() == Boolean.FALSE)
 		return;
	    
 	    if (blackboardMessage instanceof FullUpdate)
 	    {
		final FullUpdate update = (FullUpdate)object;
 		if (update.isGathering() == false)
 		{
 		    for (final Player player : (Iterable<Player>)(update.data.get(PlayerRing.class)))
 			this.blackboardNetworking.receiveAndBroadcast(new PlayerJoined(player));
 		}
 	    }
 	    
 	    this.blackboardNetworking.receiveAndBroadcast(blackboardMessage);
 	}
 	catch (Exception err)
 	{
 	    return; 
 	}
     }
     
 }
 
