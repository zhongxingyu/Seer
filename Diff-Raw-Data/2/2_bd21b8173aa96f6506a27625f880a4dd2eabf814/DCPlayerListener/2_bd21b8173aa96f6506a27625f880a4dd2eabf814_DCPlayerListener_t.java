 package com.smartaleq.bukkit.dwarfcraft;
 
 import java.util.List;
 
 import org.bukkit.event.player.PlayerEvent;
 import org.bukkit.event.player.PlayerItemEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.inventory.ItemStack;
 // import org.bukkit.block.Block;
 // import net.minecraft.server.Block;
 
 import com.smartaleq.bukkit.dwarfcraft.ui.Command;
 import com.smartaleq.bukkit.dwarfcraft.ui.Messages;
 import com.smartaleq.bukkit.dwarfcraft.ui.Out;
 
 
 public class DCPlayerListener extends PlayerListener {
 	private final DwarfCraft plugin;
 	
 	public DCPlayerListener(final DwarfCraft instance) {
 		plugin = instance;
 	}
    	
 /**
  * Reads player input and checks for a /dc command
  * this functionality may soon be obsolete and use the Command class from bukkit
  */
 	public void onPlayerCommand(PlayerChatEvent event) {
 		Player player = event.getPlayer();
 		String[] fullPlayerInput = event.getMessage().split(" ");
 		if (fullPlayerInput.length >= 1) {
 			if (fullPlayerInput[0].equalsIgnoreCase("/dc")){
 
				String[] playerInput = new String[(fullPlayerInput.length-1)];
 				for(int i=1; i < fullPlayerInput.length; i++){
 					playerInput[i-1] = fullPlayerInput[i];
 				}
 				Command input = new Command(plugin, player, playerInput);
 				if (input.execute()) {
 					event.setCancelled(true);
 					return;
 					//successful command
 				}
 				else {
 					Out.error(player, Messages.ERRORBADINPUT);
 					return;
 					//failed command don't cancel in case it was someone else's
 				}
 			}
 		}
 	}
 	
 	/**
 	 * When a player joins the server this initialized their data
 	 * from the database or creates new info for them.
 	 * 
 	 * also broadcasts a welcome "player" message
 	 */
 	public void onPlayerJoin(PlayerEvent event){
 		Player player = event.getPlayer();
 		Dwarf dwarf = Dwarf.find(player);
 		if (dwarf == null) {
 			dwarf = DataManager.createDwarf(player);
 			dwarf.initializeNew();
 		}
 		if (!DataManager.getDwarfData(dwarf)) {
 			DataManager.createDwarfData(dwarf);
 		}
 		Out.welcome(plugin.getServer(), dwarf);
 	}
 	
     /**
      * Called when a player uses an item
      * Eating food will cause extra health or less health to be gained
      *
      * @param event Relevant event details
      */
     public void onPlayerItem(PlayerItemEvent event) {
     	 //General information
     	Player player = event.getPlayer();
     	Dwarf dwarf = Dwarf.find(player);
     	List<Skill> skills = dwarf.skills;
     	
    	//Effect Specific information
     	ItemStack item = player.getItemInHand();
     	int itemId = -1;
     	if (item!=null) {
     		itemId = item.getTypeId();	
     	}
     	
     	for(Skill s: skills){
     		if (s==null)continue;
     		for(Effect e:s.effects){
     			if (e==null) continue;
     			if(e.effectType == EffectType.EAT && e.initiatorId==itemId){
     				player.setHealth((int) (player.getHealth()+e.getEffectAmount(s.level)));
     				item.setAmount(item.getAmount()-1);
     				
 				}
 			}
 		}
     }
     
     public void onPlayerMove(PlayerMoveEvent event){
  //   	Block block = (Block)(event.getPlayer().getLocation().getBlock());
 
 //    	block.
     	return;
     }
 }
