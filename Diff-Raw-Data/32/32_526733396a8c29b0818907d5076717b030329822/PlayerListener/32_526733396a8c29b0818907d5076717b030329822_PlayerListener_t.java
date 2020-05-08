 package net.marcuswhybrow.minecraft.law.listeners;
 
 import net.marcuswhybrow.minecraft.law.interfaces.ImprisonmentListener;
 import net.marcuswhybrow.minecraft.law.Law;
 import net.marcuswhybrow.minecraft.law.LawWorld;
 import net.marcuswhybrow.minecraft.law.Settings;
 import net.marcuswhybrow.minecraft.law.prison.PrisonCell;
 import net.marcuswhybrow.minecraft.law.utilities.Colorise;
 import net.marcuswhybrow.minecraft.law.utilities.MessageDispatcher;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
 
 public class PlayerListener extends org.bukkit.event.player.PlayerListener {
 	private Law law = Law.get();
 	
 	@Override
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
 		LawWorld lawWorld = law.getLawWorld(player.getWorld());
 		
 		if (lawWorld == null) {
 			// This could only happen if a World was added at runtime
 			return;
 		}
 		
 		if (lawWorld.hasUnsecuredPrisoner(player.getName())) {
 			PrisonCell cell = lawWorld.getPrisonerCell(player.getName());
 			if (cell != null) {
 				for (ImprisonmentListener listener : Law.get().getImprisonmentListeners()) {
 					listener.onImprison(player, cell);
 				}
 			}
 		}
 		
 		if (lawWorld.hasUnreleasedPrisoner(player.getName())) {
 			PrisonCell cell = lawWorld.getPrisonerCell(player.getName());
 			for (ImprisonmentListener listener : Law.get().getImprisonmentListeners()) {
 				listener.onFree(player, cell);
 			}
 		}
 	}
 	
 	@Override
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		LawWorld lawWorld;
 		
 		if (player != null) {
 			lawWorld = law.getLawWorldForPlayer(player);
 			if (lawWorld != null) {
 				if (lawWorld.hasPrisoner(player.getName())) {
 					
 					if (event.hasItem()) {
 						int itemInHandTypeId = event.getItem().getTypeId();
 						if (Settings.get().isUsableId(itemInHandTypeId)) {
 							event.setCancelled(true);
 							MessageDispatcher.sendMessage(player, Colorise.error("You cannot use items as a prisoner"));
 							return;
 						}
 					}
 					
 					Block clickedBlock = event.getClickedBlock();
 					if (clickedBlock != null) {
 						if (Settings.get().isSwitchableId(clickedBlock.getTypeId())) {
 							event.setCancelled(true);
 							MessageDispatcher.sendMessage(player, Colorise.error("You cannot interact as a prisoner"));
 							return;
 						}
 					}
 				}
 			}
 		}
 		
 		super.onPlayerInteract(event);
 	}
	
	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		LawWorld lawWorld = Law.get().getLawWorldForPlayer(player);
		PrisonCell cell = lawWorld.getPrisonerCell(player.getName());
		if (cell != null) {
			event.setRespawnLocation(cell.getLocation());
		}
		
		super.onPlayerRespawn(event);
	}
 }
