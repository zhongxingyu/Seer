 package com.github.r0306.RollTheDice.DiceHandlers;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
 
 public class _16 extends Arena implements Listener
 {
 
 	@EventHandler
	public void onChat(PlayerChatEvent event) throws Exception
 	{
 		
 		Player player = event.getPlayer();
 		
 		if (isIn(player, 16))
 		{
 			
			String message = Translate.execute(event.getMessage(), Language.ENGLISH, Language.SPANISH);
			event.setMessage(message);
 		
 		}
 		
 	}
 	
 }
