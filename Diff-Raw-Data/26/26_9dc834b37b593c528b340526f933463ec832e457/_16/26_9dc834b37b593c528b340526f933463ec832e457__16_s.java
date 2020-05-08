 package com.github.r0306.RollTheDice.DiceHandlers;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 
import com.google.api.translate.Language;
import com.google.api.translate.Translate;
 
 public class _16 extends Arena implements Listener
 {
 
 	@EventHandler
	public void onChat(PlayerChatEvent event)
 	{
 		
 		Player player = event.getPlayer();
 		
 		if (isIn(player, 16))
 		{
 			
			Translate.DEFAULT.execute(event.getMessage(), Language.ENGLISH, Language.SPANISH);
 		
 		}
 		
 	}
 	
 }
