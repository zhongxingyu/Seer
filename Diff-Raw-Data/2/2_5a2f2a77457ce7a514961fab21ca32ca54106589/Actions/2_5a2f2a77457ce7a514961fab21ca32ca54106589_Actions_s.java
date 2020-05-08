 package net.amoebaman.kitmaster;
 
 import net.amoebaman.kitmaster.controllers.InventoryController;
 import net.amoebaman.kitmaster.enums.Attribute;
 import net.amoebaman.kitmaster.enums.ClearKitsContext;
 import net.amoebaman.kitmaster.enums.GiveKitContext;
 import net.amoebaman.kitmaster.enums.GiveKitResult;
 import net.amoebaman.kitmaster.enums.PermsResult;
 import net.amoebaman.kitmaster.handlers.HistoryHandler;
 import net.amoebaman.kitmaster.handlers.KitHandler;
 import net.amoebaman.kitmaster.handlers.TimeStampHandler;
 import net.amoebaman.kitmaster.objects.Kit;
 import net.amoebaman.kitmaster.utilities.ClearKitsEvent;
 import net.amoebaman.kitmaster.utilities.GiveKitEvent;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.potion.PotionEffect;
 
 public class Actions {
 	
 	/**
 	 * Gives a player a kit.
 	 * This method will consider and apply all attributes of the given kit, including timeouts, permissions, and inheritance.
 	 * If the kit has a parent, a recursive call will be made to this method <i>prior</i> to the application of the initial kit.
 	 * @param player The player to give the kit to
 	 * @param kit The kit to give
 	 * @param override True if this operation should ignore checks for permissions, and timeouts
 	 * @return A GiveKitResult signifying the success or reason for failure of giving the kit
 	 */
 	public static GiveKitResult giveKit(Player player, Kit kit, boolean override){
 		return giveKit(player, kit, override ? GiveKitContext.PLUGIN_GIVEN_OVERRIDE : GiveKitContext.PLUGIN_GIVEN);
 	}
 
 	public static boolean debugNextGiveKit = false;
 	
 	public static GiveKitResult giveKit(Player player, Kit kit, GiveKitContext context){
 		boolean debug = debugNextGiveKit;
 		debugNextGiveKit = false;
 		
 		/*
 		 * We can't give a player a null kit
 		 * Return a result that reflects this
 		 */
 		if(kit == null)
 			return GiveKitResult.FAIL_NULL_KIT;
 		if(debug)
 			KitMaster.logger().info("Attempting to give " + player.getName() + " the " + kit.name + " kit");
 		/*
 		 * Clone the kit to prevent accidental mutation damage to the base kit
 		 */
 		kit = kit.clone();
 		/*
 		 * Check if the player has permission to take this kit in the given manner
 		 * Ignore these checks if the context overrides them
 		 */
 		if(!context.overrides){
 			PermsResult perms = KitHandler.getKitPerms(player, kit);
 			if(debug)
 				KitMaster.logger().info("Permissions result: " + perms);
 			switch(perms){
 				case COMMAND_ONLY:
 					if(context == GiveKitContext.SIGN_TAKEN){
 						player.sendMessage(ChatColor.ITALIC + "You can't take the " + kit.name + " kit from signs");
 						return GiveKitResult.FAIL_NO_PERMS;
 					}
 					break;
 				case SIGN_ONLY:
 					if(context == GiveKitContext.COMMAND_TAKEN){
 						player.sendMessage(ChatColor.ITALIC + "You can't take the " + kit.name + " kit by command");
 						return GiveKitResult.FAIL_NO_PERMS;
 					}
 					break;
 				case INHERIT_COMMAND_ONLY:
 					if(context == GiveKitContext.SIGN_TAKEN){
 						player.sendMessage(ChatColor.ITALIC + "You can't take " + kit.getParent().name + " kits from signs");
 						return GiveKitResult.FAIL_NO_PERMS;
 					}
 				case INHERIT_SIGN_ONLY:
 					if(context == GiveKitContext.COMMAND_TAKEN){
 						player.sendMessage(ChatColor.ITALIC + "You can't take " + kit.getParent().name + " kits by command");
 						return GiveKitResult.FAIL_NO_PERMS;
 					}
 					break;
 				case NONE:
 					player.sendMessage(ChatColor.ITALIC + "You can't take the " + kit.name + " kit");
 					return GiveKitResult.FAIL_NO_PERMS;
 				case INHERIT_NONE:
 					player.sendMessage(ChatColor.ITALIC + "You can't take " + kit.getParent().name + " kits");
 					return GiveKitResult.FAIL_NO_PERMS;
 				default:
 			}
 		}
 		/*
 		 * Perform operations for the parent kit
 		 * Obviously these don't need to happen if there is no parent kit
 		 */
 		Kit parentKit = kit.getParent();
 		if(debug)
 			KitMaster.logger().info("Parent kit: " + parentKit);
 		if(parentKit != null)
 			/*
 			 * Check timeouts for the parent kit
 			 * Don't perform these checks if the context overrides them or the player has an override permission
 			 */
 			if(!context.overrides && !TimeStampHandler.hasOverride(player, parentKit)){
 				if(debug){
 					KitMaster.logger().info("Checking parent timestamp: " + TimeStampHandler.getTimeStamp(player, parentKit));
 					KitMaster.logger().info("Checking parent timeout: " + TimeStampHandler.timeoutRemaining(player, parentKit));
 				}
 				switch(TimeStampHandler.timeoutCheck(player, parentKit)){
 					case FAIL_TIMEOUT:
 						player.sendMessage(ChatColor.ITALIC + "You need to wait " + TimeStampHandler.timeoutRemaining(player, parentKit) + " before using a " + parentKit.name + " kit");
 						return GiveKitResult.FAIL_TIMEOUT;
 					case FAIL_SINGLE_USE:
 						player.sendMessage(ChatColor.ITALIC + "You can only use a " + parentKit.name + " kit once");
 						return GiveKitResult.FAIL_SINGLE_USE;
 					default: }
 			}
 		/*
 		 * Check timeouts for the current kit
 		 * Don't perform these checks if the context overrides them or the player has an override permission
 		 */
 		if(!context.overrides && !TimeStampHandler.hasOverride(player, kit)){
 			if(debug){
 				KitMaster.logger().info("Checking timestamp: " + TimeStampHandler.getTimeStamp(player, parentKit));
 				KitMaster.logger().info("Checking timeout: " + TimeStampHandler.timeoutRemaining(player, kit));
 			}
 			switch(TimeStampHandler.timeoutCheck(player, kit)){
 				case FAIL_TIMEOUT:
 					player.sendMessage(ChatColor.ITALIC + "You need to wait " + TimeStampHandler.timeoutRemaining(player, kit) + " before using the " + kit.name + " kit");
 					return GiveKitResult.FAIL_TIMEOUT;
 				case FAIL_SINGLE_USE:
 					player.sendMessage(ChatColor.ITALIC + "You can only use the " + kit.name + " kit once");
 					return GiveKitResult.FAIL_SINGLE_USE;
 				default: }
 		}
 		/*
 		 * Check if the player can afford the kit
 		 * Don't perform these checks if the economy is not enabled, or if the contexts overrides them or the player has an override permission
 		 */
 		if(KitMaster.getEcon() != null)
 			if(KitMaster.getEcon().getBalance(player.getName()) < kit.doubleAttribute(Attribute.COST) && !player.hasPermission("kitmaster.nocharge") && !player.hasPermission("kitmaster.nocharge." + kit.name)){
 				player.sendMessage(ChatColor.ITALIC + "You need " + kit.doubleAttribute(Attribute.COST) + " " + KitMaster.getEcon().currencyNameSingular() + " to take the " + kit.name + " kit");
 				return GiveKitResult.FAIL_COST;
 			}
 		/*
 		 * Check if the player has taken any kits that restrict further kit usage
 		 */
 		if(debug)
 			KitMaster.logger().info("Checking history: " + HistoryHandler.getHistory(player));
 		for(Kit other : HistoryHandler.getHistory(player))
 			if(other.booleanAttribute(Attribute.RESTRICT_KITS)){
 				player.sendMessage(ChatColor.ITALIC + "You've already taken a kit that doesn't allow you to take further kits");
 				return GiveKitResult.FAIL_RESTRICTED;
 			}
 		/*
 		 * Create and call a GiveKitEvent so that other plugins can modify or attempt to cancel the kit
 		 * If the event comes back cancelled and the context doesn't override it, end here
 		 */
 		GiveKitEvent kitEvent = new GiveKitEvent(player, kit, context);
 		kitEvent.callEvent();
 		if (kitEvent.isCancelled() && !context.overrides)
 			return GiveKitResult.FAIL_CANCELLED;
 		/*
 		 * Apply the kit's clearing properties
 		 * Don't perform this operation if the kit is a parent
 		 */
 		if(context != GiveKitContext.PARENT_GIVEN)
 			applyKitClears(player, kit);
 		/*
 		 * Apply the parent kit
 		 */
 		if(parentKit != null)
 			giveKit(player, parentKit, GiveKitContext.PARENT_GIVEN);
 		/*
 		 * Add the kit's items to the player's inventory
 		 */
 		InventoryController.addItemsToInventory(player, kitEvent.getKit().items, parentKit != null && kit.booleanAttribute(Attribute.UPGRADE));
 		/*
 		 * Apply the kit's potion effects to the player
 		 */
 		if(kit.booleanAttribute(Attribute.INFINITE_EFFECTS))
 			for(int i = 0; i < kit.effects.size(); i++)
 				kit.effects.set(i, new PotionEffect(kit.effects.get(i).getType(), Integer.MAX_VALUE, kit.effects.get(i).getAmplifier()));
 		player.addPotionEffects(kitEvent.getKit().effects);
 		/*
 		 * Grant the kit's permissions to the player
 		 * Don't perform this operation if the permission handle is not enabled
 		 */
 		if(KitMaster.getPerms() != null)
 			for(String node : kit.permissions)
				KitMaster.getPerms().playerAdd("world", player.getName(), node);
 		/*
 		 * Apply the kit's economic attributes
 		 * Don't perform this operation if the economy handle is not enabled, or if the player has  an override permission
 		 */
 		if(KitMaster.getEcon() != null && !player.hasPermission("kitmaster.nocharge") && !player.hasPermission("kitmaster.nocharge." + kit.name)){
 			KitMaster.getEcon().bankWithdraw(player.getName(), kit.doubleAttribute(Attribute.COST));
 			KitMaster.getEcon().bankDeposit(player.getName(), kit.doubleAttribute(Attribute.CASH));
 		}
 		/*
 		 * Record that this kit was taken
 		 * Stamp the time, and add the kit to the player's history
 		 */
 		TimeStampHandler.setTimeStamp(kit.booleanAttribute(Attribute.GLOBAL_TIMEOUT) ? null : player, kit);
 		HistoryHandler.addToHistory(player, kit);
 		/*
 		 * Notify the player of their good fortune
 		 */
 		if(context == GiveKitContext.COMMAND_TAKEN || context == GiveKitContext.SIGN_TAKEN)
 			player.sendMessage(ChatColor.ITALIC + kit.name + " kit taken");
 		if(context == GiveKitContext.COMMAND_GIVEN || context == GiveKitContext.PLUGIN_GIVEN || context == GiveKitContext.PLUGIN_GIVEN_OVERRIDE)
 			player.sendMessage(ChatColor.ITALIC + "You were given the " + kit.name + " kit");
 		/*
 		 * Return the success of the mission
 		 */
 		return GiveKitResult.SUCCESS;
 		
 	}
 	
 	/**
 	 * Clears all attributes for all kits in history and clears the history for a player.
 	 * @param player The target player.
 	 */
 	public static void clearKits(Player player){
 		clearAll(player, true, ClearKitsContext.PLUGIN_ORDER);
 	}
 	
 	private static void applyKitClears(Player player, Kit kit){
 		if(kit.booleanAttribute(Attribute.CLEAR_ALL) || (kit.booleanAttribute(Attribute.CLEAR_INVENTORY) && kit.booleanAttribute(Attribute.CLEAR_EFFECTS) && kit.booleanAttribute(Attribute.CLEAR_PERMISSIONS))){
 			clearAll(player, true, ClearKitsContext.KIT_ATTRIBUTE);
 			HistoryHandler.resetHistory(player);
 		}
 		else{
 			if(kit.booleanAttribute(Attribute.CLEAR_INVENTORY))
 				clearInventory(player, true, ClearKitsContext.KIT_ATTRIBUTE);
 			if(kit.booleanAttribute(Attribute.CLEAR_EFFECTS))
 				clearEffects(player, true, ClearKitsContext.KIT_ATTRIBUTE);
 			if(kit.booleanAttribute(Attribute.CLEAR_PERMISSIONS) && KitMaster.getPerms() != null)
 				clearPermissions(player, true, ClearKitsContext.KIT_ATTRIBUTE);
 		}
 	}
 	
  	protected static void clearAll(Player player, boolean callEvent, ClearKitsContext context){
 		ClearKitsEvent event = new ClearKitsEvent(player, context);
 		if(callEvent)
 			event.callEvent();
 		if(!event.isCancelled()){
 			if(event.clearsInventory())
 				clearInventory(player, false, context);
 			if(event.clearsEffects())
 				clearEffects(player, false, context);
 			if(event.clearsPermissions())
 				clearPermissions(player, false, context);
 		}
 		HistoryHandler.resetHistory(player);
 	}
 	
 	private static void clearInventory(Player player, boolean callEvent, ClearKitsContext context){
 		ClearKitsEvent event = new ClearKitsEvent(player, true, false, false, context);
 		if(callEvent)
 			event.callEvent();
 		if(!event.isCancelled() && event.clearsInventory()){
 			player.getInventory().clear();
 			player.getInventory().setArmorContents(null);
 		}
 	}
 	
 	private static void clearEffects(Player player, boolean callEvent, ClearKitsContext context){
 		ClearKitsEvent event = new ClearKitsEvent(player, false, true, false, context);
 		if(callEvent)
 			event.callEvent();
 		if(!event.isCancelled() && event.clearsEffects()){
 			for(PotionEffect effect : player.getActivePotionEffects())
 				player.removePotionEffect(effect.getType());
 		}
 	}
 	
 	private static void clearPermissions(Player player, boolean callEvent, ClearKitsContext context){
 		ClearKitsEvent event = new ClearKitsEvent(player, false, false, true, context);
 		if(callEvent)
 			event.callEvent();
 		if(!event.isCancelled() && event.clearsPermissions()){
 			if(KitMaster.getPerms() != null)
 				for(Kit last : HistoryHandler.getHistory(player))
 					for(String node : last.permissions)
 						KitMaster.getPerms().playerRemove(player, node);
 		}
 	}
 	
 }
