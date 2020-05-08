 package com.censoredsoftware.demigods.conversation;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.Elements;
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.deity.Deity;
 import com.censoredsoftware.demigods.helper.ListedConversation;
 import com.censoredsoftware.demigods.language.Translation;
 import com.censoredsoftware.demigods.location.DLocation;
 import com.censoredsoftware.demigods.player.DCharacter;
 import com.censoredsoftware.demigods.player.DPlayer;
 import com.censoredsoftware.demigods.player.Notification;
 import com.censoredsoftware.demigods.structure.Structure;
 import com.censoredsoftware.demigods.util.*;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.conversations.*;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.ExperienceOrb;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import java.lang.reflect.Field;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 @SuppressWarnings("unchecked")
 public class Prayer implements ListedConversation
 {
 
 	@Override
 	public org.bukkit.event.Listener getUniqueListener()
 	{
 		return new Listener();
 	}
 
 	/**
 	 * Defines categories that can be used during prayer.
 	 */
 	public enum Menu
 	{
 		CONFIRM_FORSAKE('F', new ConfirmForsake()), CANCEL_FORSAKE('X', new CancelForsake()), CONFIRM_CHARACTER('C', new ConfirmCharacter()), CREATE_CHARACTER('1', new CreateCharacter()), VIEW_CHARACTERS('2', new ViewCharacters()), VIEW_WARPS('3', new ViewWarps()), FORSAKE_CHARACTER('4', new Forsake()), VIEW_NOTIFICATIONS('5', new ViewNotifications());
 
 		private final char id;
 		private final Elements.Conversations.Category category;
 
 		private Menu(char id, Elements.Conversations.Category category)
 		{
 			this.id = id;
 			this.category = category;
 		}
 
 		public char getId()
 		{
 			return this.id;
 		}
 
 		public Elements.Conversations.Category getCategory()
 		{
 			return this.category;
 		}
 
 		public static Menu getFromId(char id)
 		{
 			for(Menu menu : Menu.values())
 				if(menu.getId() == id) return menu;
 			return null;
 		}
 	}
 
 	@Override
 	public Conversation startMenu(Player player)
 	{
 		return startPrayer(player);
 	}
 
 	public static Conversation startPrayer(Player player)
 	{
 		try
 		{
 			Map<Object, Object> conversationContext = Maps.newHashMap();
 
 			if(!Demigods.isRunningSpigot())
 			{
 				// Compatibility with vanilla Bukkit
 				Field sessionDataField = ConversationContext.class.getDeclaredField("sessionData");
 				sessionDataField.setAccessible(true);
 				if(DataManager.hasKeyTemp(player.getName(), "prayer_context")) conversationContext = (Map<Object, Object>) sessionDataField.get(DataManager.getValueTemp(player.getName(), "prayer_context"));
 			}
 			else
 			{
 				// Grab the context Map
 				if(DataManager.hasKeyTemp(player.getName(), "prayer_context")) conversationContext.putAll(((ConversationContext) DataManager.getValueTemp(player.getName(), "prayer_context")).getAllSessionData());
 			}
 
 			// Build the conversation and begin
 			Conversation prayerConversation = Demigods.conversation.withEscapeSequence("/exit").withLocalEcho(false).withInitialSessionData(conversationContext).withFirstPrompt(new StartPrayer()).buildConversation(player);
 			prayerConversation.begin();
 
 			return prayerConversation;
 		}
 		catch(NoSuchFieldException ignored)
 		{}
 		catch(IllegalAccessException ignored)
 		{}
 		return null;
 	}
 
 	// Main prayer menu
 	static class StartPrayer extends ValidatingPrompt
 	{
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 
 			// Clear chat
 			Demigods.message.clearRawChat(player);
 
 			// Send NoGrief menu
 			Demigods.message.clearRawChat(player);
 			player.sendRawMessage(ChatColor.AQUA + " -- Prayer Menu --------------------------------------");
 			player.sendRawMessage(" ");
 			for(String message : Demigods.language.getTextBlock(Translation.Text.PRAYER_INTRO))
 				player.sendRawMessage(message);
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.GRAY + " To begin, choose an option by entering its number in the chat:");
 			player.sendRawMessage(" ");
 
 			for(Menu menu : Menu.values())
 				if(menu.getCategory().canUse(context)) player.sendRawMessage(ChatColor.GRAY + "   [" + menu.getId() + ".] " + menu.getCategory().getChatName());
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			try
 			{
 				Menu menu = Menu.getFromId(Character.toUpperCase(message.charAt(0)));
 				return menu != null && menu.getCategory().canUse(context);
 			}
 			catch(Exception ignored)
 			{}
 			return false;
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			return Menu.getFromId(Character.toUpperCase(message.charAt(0))).getCategory();
 		}
 	}
 
 	// Warps
 	static class ViewWarps extends ValidatingPrompt implements Elements.Conversations.Category
 	{
 		@Override
 		public String getChatName()
 		{
 			return ChatColor.LIGHT_PURPLE + "View Warps " + ChatColor.GRAY + "(& Invites)";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			return DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent() != null;
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 
 			Demigods.message.clearRawChat(player);
 			player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Viewing Warps & Invites"));
 			player.sendRawMessage(" ");
 
 			if(character.getMeta().hasWarps() || character.getMeta().hasInvites())
 			{
 				player.sendRawMessage(ChatColor.LIGHT_PURPLE + "  Light purple" + ChatColor.GRAY + " represents the warp(s) at this location.");
 				player.sendRawMessage(" ");
 
 				for(Map.Entry<String, Object> entry : character.getMeta().getWarps().entrySet())
 				{
 					Location location = DLocation.Util.load(UUID.fromString(entry.getValue().toString())).toLocation();
 					player.sendRawMessage((player.getLocation().distance(location) < 8 ? ChatColor.LIGHT_PURPLE : ChatColor.GRAY) + "    " + StringUtils.capitalize(entry.getKey().toLowerCase()) + ChatColor.GRAY + " (" + StringUtils.capitalize(location.getWorld().getName().toLowerCase()) + ": " + Math.round(location.getX()) + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ()) + ")");
 				}
 				for(Map.Entry<String, Object> entry : character.getMeta().getInvites().entrySet())
 				{
 					Location location = DLocation.Util.load(UUID.fromString(entry.getValue().toString())).toLocation();
 					player.sendRawMessage((player.getLocation().distance(location) < 8 ? ChatColor.LIGHT_PURPLE : ChatColor.GRAY) + "    " + StringUtils.capitalize(entry.getKey().toLowerCase()) + ChatColor.GRAY + " (" + StringUtils.capitalize(location.getWorld().getName().toLowerCase()) + ": " + Math.round(location.getX()) + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ()) + ") " + ChatColor.GREEN + "Invited by [ALLAN!!]"); // TODO: Invited by
 				}
 
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.GRAY + "  Type " + ChatColor.YELLOW + "new <warp name>" + ChatColor.GRAY + " to create a warp at this Altar,");
 				player.sendRawMessage(ChatColor.YELLOW + "  warp <warp name>" + ChatColor.GRAY + " to teleport to a warp, or " + ChatColor.YELLOW + "delete");
 				player.sendRawMessage(ChatColor.YELLOW + "  <warp name>" + ChatColor.GRAY + " remove a warp. You can also invite a player");
 				player.sendRawMessage(ChatColor.GRAY + "  by using " + ChatColor.YELLOW + "invite <player/character> <warp name>" + ChatColor.GRAY + ".");
 			}
 			else
 			{
 				player.sendRawMessage(ChatColor.RED + "    You have no warps or invites!");
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.GRAY + "  Type " + ChatColor.YELLOW + "new <warp name>" + ChatColor.GRAY + " to create a warp at this Altar.");
 			}
 
 			// Display notifications if available
 			if(context.getSessionData("warp_notifications") != null && !((List<Translation.Text>) context.getSessionData("warp_notifications")).isEmpty())
 			{
 				// Grab the notifications
 				List<Translation.Text> notifications = (List<Translation.Text>) context.getSessionData("warp_notifications");
 
 				player.sendRawMessage(" ");
 
 				// List them
 				for(Translation.Text notification : notifications)
 					player.sendRawMessage("  " + Demigods.language.getText(notification));
 
 				// Remove them
 				notifications.clear();
 			}
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			// Define variables
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 			String arg0 = message.split(" ")[0];
 			String arg1 = message.split(" ").length >= 2 ? message.split(" ")[1] : null;
 			String arg2 = message.split(" ").length >= 3 ? message.split(" ")[2] : null;
 
 			return message.equalsIgnoreCase("menu") || arg0.equalsIgnoreCase("new") && StringUtils.isAlphanumeric(arg1) && !character.getMeta().getWarps().containsKey(arg1.toLowerCase()) || ((arg0.equalsIgnoreCase("warp") || arg0.equalsIgnoreCase("delete")) && (character.getMeta().getWarps().containsKey(arg1.toLowerCase()) || character.getMeta().getInvites().containsKey(arg1.toLowerCase())) || (arg0.equalsIgnoreCase("invite") && (DCharacter.Util.charExists(arg1) || Bukkit.getOfflinePlayer(arg1) != null && DPlayer.Util.getPlayer(Bukkit.getOfflinePlayer(arg1)).getCurrent() != null) && arg2 != null && character.getMeta().getWarps().containsKey(arg2.toLowerCase())));
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 			String arg0 = message.split(" ")[0];
 			String arg1 = message.split(" ").length >= 2 ? message.split(" ")[1] : null;
 			String arg2 = message.split(" ").length >= 3 ? message.split(" ")[2] : null;
 
 			// Create and save the notification list
 			context.setSessionData("warp_notifications", Lists.newArrayList());
 			List<Translation.Text> notifications = (List<Translation.Text>) context.getSessionData("warp_notifications");
 
 			Demigods.message.clearRawChat(player);
 
 			if(message.equalsIgnoreCase("menu"))
 			{
 				// THEY WANT THE MENU!? SOCK IT TO 'EM!
 				return new StartPrayer();
 			}
 			if(arg0.equalsIgnoreCase("new"))
 			{
 				// Save notification
 				notifications.add(Translation.Text.NOTIFICATION_WARP_CREATED);
 
 				// Add the warp
 				character.getMeta().addWarp(arg1, player.getLocation());
 
 				// Return to view warps
 				return new ViewWarps();
 			}
 			else if(arg0.equalsIgnoreCase("delete"))
 			{
 				// Save notification
 				notifications.add(Translation.Text.NOTIFICATION_WARP_DELETED);
 
 				// Remove the warp/invite
 				if(character.getMeta().getWarps().containsKey(arg1.toLowerCase())) character.getMeta().removeWarp(arg1);
 				else if(character.getMeta().getInvites().containsKey(arg1.toLowerCase())) character.getMeta().removeInvite(arg1);
 
 				// Return to view warps
 				return new ViewWarps();
 			}
 			else if(arg0.equalsIgnoreCase("invite"))
 			{
 				// Save notification
 				notifications.add(Translation.Text.NOTIFICATION_INVITE_SENT);
 
 				// Define variables
 				DCharacter invitee = DCharacter.Util.charExists(arg1) ? DCharacter.Util.getCharacterByName(arg1) : DPlayer.Util.getPlayer(Bukkit.getOfflinePlayer(arg1)).getCurrent();
 				Location warp = DLocation.Util.load(UUID.fromString(character.getMeta().getWarps().get(arg2).toString())).toLocation();
 
 				// Add the invite
 				invitee.getMeta().addInvite(character.getName(), warp);
 
 				// Message the player if they're online
 				if(invitee.getOfflinePlayer().isOnline())
 				{
 					invitee.getOfflinePlayer().getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "You've been invited to a warp by " + character.getName() + "!");
 					invitee.getOfflinePlayer().getPlayer().sendMessage(ChatColor.GRAY + "Go to an Altar to accept this invite.");
 				}
 
 				// Return to warps menu
 				return new ViewWarps();
 			}
 			else if(arg0.equalsIgnoreCase("warp"))
 			{
 				// Disable prayer
 				DPlayer.Util.togglePrayingSilent(player, false, true);
 
 				// Teleport and message
 				if(character.getMeta().getWarps().containsKey(arg1.toLowerCase())) player.teleport(DLocation.Util.load(UUID.fromString(character.getMeta().getWarps().get(arg1.toLowerCase()).toString())).toLocation());
 				else if(character.getMeta().getInvites().containsKey(arg1.toLowerCase()))
 				{
 					player.teleport(DLocation.Util.load(UUID.fromString(character.getMeta().getInvites().get(arg1.toLowerCase()).toString())).toLocation());
 					character.getMeta().removeInvite(arg1.toLowerCase());
 				}
 				player.sendMessage(ChatColor.GRAY + "Teleported to " + ChatColor.LIGHT_PURPLE + StringUtils.capitalize(arg1.toLowerCase()) + ChatColor.GRAY + ".");
 			}
 			return null;
 		}
 	}
 
 	// Notifications
 	static class ViewNotifications extends ValidatingPrompt implements Elements.Conversations.Category
 	{
 		@Override
 		public String getChatName()
 		{
 			return ChatColor.GREEN + "View Notifications";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 			return character != null && character.getMeta().hasNotifications();
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 
 			Demigods.message.clearRawChat(player);
 			player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Viewing Notifications"));
 			player.sendRawMessage(" ");
 
 			for(String string : character.getMeta().getNotifications())
 			{
 				Notification notification = Notification.Util.load(UUID.fromString(string));
 				// Determine color
 				ChatColor color;
 				switch(notification.getDanger())
 				{
 					case GOOD:
 						color = ChatColor.GREEN;
 						break;
 					case BAD:
 						color = ChatColor.RED;
 						break;
 					case NEUTRAL:
 					default:
 						color = ChatColor.YELLOW;
 						break;
 				}
 
 				// Set expires
 				String expires = notification.hasExpiration() ? ChatColor.GRAY + " (expires in " + Times.getTimeTagged(notification.getExpiration(), true) + ")" : "";
 
 				// Send the notification
 				player.sendRawMessage(color + "    " + notification.getMessage() + expires);
 			}
 
 			player.sendRawMessage(" ");
 			for(String message : Demigods.language.getTextBlock(Translation.Text.NOTIFICATIONS_PRAYER_FOOTER))
 				player.sendRawMessage(message);
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			return message.equalsIgnoreCase("clear") || message.equalsIgnoreCase("menu");
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			// Define variables
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 
 			if(message.equalsIgnoreCase("menu"))
 			{
 				// THEY WANT THE MENU!? SOCK IT TO 'EM!
 				return new StartPrayer();
 			}
 			else if(message.equalsIgnoreCase("clear"))
 			{
 				// Clear them
 				for(String string : character.getMeta().getNotifications())
 					Notification.remove(Notification.Util.load(UUID.fromString(string)));
 				character.getMeta().clearNotifications();
 
 				// Send to the menu
 				return new StartPrayer();
 			}
 			return null;
 		}
 	}
 
 	// Character viewing
 	static class ViewCharacters extends ValidatingPrompt implements Elements.Conversations.Category
 	{
 		@Override
 		public String getChatName()
 		{
 			return ChatColor.YELLOW + "View Characters";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			return DPlayer.Util.getPlayer((Player) context.getForWhom()).getCharacters() != null && !DPlayer.Util.getPlayer((Player) context.getForWhom()).getCharacters().isEmpty();
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 
 			Demigods.message.clearRawChat(player);
 
 			player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Viewing Character"));
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.LIGHT_PURPLE + "  Light purple" + ChatColor.GRAY + " represents your current character.");
 			player.sendRawMessage(" ");
 
 			for(DCharacter character : DPlayer.Util.getPlayer(player).getCharacters())
 				player.sendRawMessage((character.isActive() ? ChatColor.LIGHT_PURPLE : ChatColor.GRAY) + "    " + character.getName() + ChatColor.GRAY + " [" + character.getDeity().getColor() + character.getDeity().getName() + ChatColor.GRAY + " / Fav: " + Strings.getColor(character.getMeta().getFavor(), character.getMeta().getMaxFavor()) + character.getMeta().getFavor() + ChatColor.GRAY + " (of " + ChatColor.GREEN + character.getMeta().getMaxFavor() + ChatColor.GRAY + ") / Asc: " + ChatColor.GREEN + character.getMeta().getAscensions() + ChatColor.GRAY + "]");
 
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.GRAY + "  Type " + ChatColor.YELLOW + "<character name> info" + ChatColor.GRAY + " for detailed information or");
 			player.sendRawMessage(ChatColor.GRAY + "  type " + ChatColor.YELLOW + "<character name> switch" + ChatColor.GRAY + " to change your current");
 			player.sendRawMessage(ChatColor.GRAY + "  character.");
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.GRAY + "  Use " + ChatColor.YELLOW + "menu" + ChatColor.GRAY + " to return to the main menu.");
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			String[] splitMsg = message.split(" ");
 			DPlayer player = DPlayer.Util.getPlayer((Player) context.getForWhom());
 			DCharacter character = DCharacter.Util.getCharacterByName(splitMsg[0]);
 			return message.equalsIgnoreCase("menu") || splitMsg.length == 2 && (DPlayer.Util.hasCharName((Player) context.getForWhom(), splitMsg[0]) && (splitMsg[1].equalsIgnoreCase("info") || (DPlayer.Util.hasCharName((Player) context.getForWhom(), splitMsg[0]) && splitMsg[1].equalsIgnoreCase("switch")) && (player.getCurrent() == null || !player.getCurrent().getName().equalsIgnoreCase(character.getName()))));
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			String arg0 = message.split(" ")[0];
 			String arg1 = message.split(" ").length == 2 ? message.split(" ")[1] : "";
 
 			if(message.equalsIgnoreCase("menu")) return new StartPrayer();
 			if(arg1.equalsIgnoreCase("info"))
 			{
 				context.setSessionData("viewing_character", arg0);
 				return new DetailedInfo();
 			}
 			else if(arg1.equalsIgnoreCase("switch")) DPlayer.Util.getPlayer((Player) context.getForWhom()).switchCharacter(DCharacter.Util.getCharacterByName(arg0));
 			return null;
 		}
 
 		// Detailed character info
 		class DetailedInfo extends ValidatingPrompt
 		{
 			@Override
 			public String getPromptText(ConversationContext context)
 			{
 				// Define variables
 				Player player = (Player) context.getForWhom();
 				DCharacter character = DCharacter.Util.getCharacterByName(context.getSessionData("viewing_character").toString());
 				String status = character.isActive() ? ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + "(Current) " + ChatColor.RESET : ChatColor.RED + "" + ChatColor.ITALIC + "(Inactive) " + ChatColor.RESET;
 
 				// Clear chat
 				Demigods.message.clearRawChat(player);
 
 				// Send the player the info
 				player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Viewing Character"));
 				player.sendRawMessage(" ");
 				player.sendRawMessage("    " + status + ChatColor.YELLOW + character.getName() + ChatColor.GRAY + " > Allied to " + character.getDeity().getColor() + character.getDeity() + ChatColor.GRAY + " of the " + ChatColor.GOLD + character.getAlliance() + "s");
 				player.sendRawMessage(ChatColor.GRAY + "  --------------------------------------------------");
 				player.sendRawMessage(ChatColor.GRAY + "    Health: " + ChatColor.WHITE + Strings.getColor(character.getHealth(), 20) + character.getHealth() + ChatColor.GRAY + " (of " + ChatColor.GREEN + 20 + ChatColor.GRAY + ")" + ChatColor.GRAY + "  |  Hunger: " + ChatColor.WHITE + Strings.getColor(character.getHunger(), 20) + character.getHunger() + ChatColor.GRAY + " (of " + ChatColor.GREEN + 20 + ChatColor.GRAY + ")" + ChatColor.GRAY + "  |  Exp: " + ChatColor.WHITE + Math.round(character.getExperience())); // TODO: Exp isn't correct.
 				player.sendRawMessage(ChatColor.GRAY + "  --------------------------------------------------");
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.GRAY + "    Ascensions: " + ChatColor.GREEN + character.getMeta().getAscensions());
 				player.sendRawMessage(ChatColor.GRAY + "    Favor: " + Strings.getColor(character.getMeta().getFavor(), character.getMeta().getMaxFavor()) + character.getMeta().getFavor() + ChatColor.GRAY + " (of " + ChatColor.GREEN + character.getMeta().getMaxFavor() + ChatColor.GRAY + ") " + ChatColor.YELLOW + "+5 every " + Demigods.config.getSettingInt("regeneration.favor") + " seconds"); // TODO: This should change with "perks" (assuming that we implement faster favor regeneration perks).
 				player.sendRawMessage(" ");
 				if(character.isActive()) player.sendRawMessage(ChatColor.GRAY + "  Type " + ChatColor.YELLOW + "back" + ChatColor.GRAY + " to return to your characters.");
 				else
 				{
 					player.sendRawMessage(ChatColor.GRAY + "  Type " + ChatColor.YELLOW + "back" + ChatColor.GRAY + " to return to your characters or type " + ChatColor.YELLOW + "switch");
 					player.sendRawMessage(ChatColor.GRAY + "  to change your current character to " + character.getDeity().getColor() + character.getName() + ChatColor.GRAY + ".");
 				}
 
 				return "";
 			}
 
 			@Override
 			protected boolean isInputValid(ConversationContext context, String message)
 			{
 				DPlayer player = DPlayer.Util.getPlayer((Player) context.getForWhom());
 				DCharacter character = DCharacter.Util.getCharacterByName(context.getSessionData("viewing_character").toString());
 
 				return message.equalsIgnoreCase("back") || (message.equalsIgnoreCase("switch") && (player.getCurrent() == null || !player.getCurrent().getName().equalsIgnoreCase(character.getName())));
 			}
 
 			@Override
 			protected Prompt acceptValidatedInput(ConversationContext context, String message)
 			{
 				if(message.equalsIgnoreCase("back")) return new ViewCharacters();
 				else if(message.equalsIgnoreCase("switch")) DPlayer.Util.getPlayer((Player) context.getForWhom()).switchCharacter(DCharacter.Util.getCharacterByName(context.getSessionData("viewing_character").toString()));
 				return null;
 			}
 		}
 	}
 
 	// Deity forsaking
 	static class Forsake extends ValidatingPrompt implements Elements.Conversations.Category
 	{
 		@Override
 		public String getChatName()
 		{
 			return ChatColor.DARK_RED + "Forsake Current Deity";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 			return character != null && ((Player) context.getForWhom()).hasPermission("demigods.basic.forsake") && !DataManager.hasKeyTemp(((Player) context.getForWhom()).getName(), "currently_creating") && !DataManager.hasKeyTemp(((Player) context.getForWhom()).getName(), "currently_forsaking");
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 			Deity deity = character.getDeity();
 
 			Demigods.message.clearRawChat(player);
 			player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Forsake Current Deity"));
 			player.sendRawMessage(" ");
 			player.sendRawMessage("  " + deity.getColor() + deity.getName() + ChatColor.GRAY + " is angry with your decision and demands");
 			player.sendRawMessage(ChatColor.GRAY + "  payment from you before forsaking!");
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.GRAY + "  Are you sure that you want to forsake " + deity.getColor() + deity.getName() + ChatColor.GRAY + "? " + ChatColor.GRAY + "(y/n)");
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			return message.equalsIgnoreCase("y") || message.equalsIgnoreCase("n");
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			if(message.equalsIgnoreCase("n")) return new StartPrayer();
 			else if(message.equalsIgnoreCase("y"))
 			{
 				// Define variables
 				Player player = (Player) context.getForWhom();
 				DCharacter character = DPlayer.Util.getPlayer((Player) context.getForWhom()).getCurrent();
 				Deity deity = character.getDeity();
 
 				Demigods.message.clearRawChat(player);
 				player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Forsake Current Deity"));
 				player.sendRawMessage(" ");
 				player.sendRawMessage("  " + deity.getColor() + deity.getName() + ChatColor.GRAY + " requires that you bring the following items");
 				player.sendRawMessage(ChatColor.GRAY + "  before forsaking:");
 				player.sendRawMessage(" ");
 				for(Map.Entry<Material, Integer> entry : deity.getForsakeItems().entrySet())
 					player.sendRawMessage(ChatColor.GRAY + "    " + Unicodes.rightwardArrow() + " " + ChatColor.YELLOW + entry.getValue() + " " + Strings.beautify(entry.getKey().name()).toLowerCase() + (entry.getValue() > 1 ? "s" : ""));
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.GRAY + "  Return to an Altar after obtaining these items to finish");
 				player.sendRawMessage(ChatColor.GRAY + "  forsaking.");
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.AQUA + "  Your prayer has been disabled.");
 				player.sendRawMessage(" ");
 
 				// Save temporary data, end the conversation, and return
 				DataManager.saveTemp(((Player) context.getForWhom()).getName(), "currently_forsaking", true);
 				DataManager.saveTimed(player.getName(), "currently_forsaking", true, 600);
 				DPlayer.Util.togglePrayingSilent(player, false, true);
 			}
 			return null;
 		}
 	}
 
 	// Forsaking confirmation
 	static class ConfirmForsake extends ValidatingPrompt implements Elements.Conversations.Category
 	{
 		@Override
 		public String getChatName()
 		{
 			return ChatColor.DARK_RED + "Finish Forsaking";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			Player player = (Player) context.getForWhom();
 			return DataManager.hasKeyTemp(player.getName(), "currently_forsaking") && DataManager.hasTimed(player.getName(), "currently_forsaking");
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			Deity deity = DPlayer.Util.getPlayer(player).getCurrent().getDeity();
 
 			// Clear chat
 			Demigods.message.clearRawChat(player);
 
 			// Ask them if they have the items
 			player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Forsaking " + deity.getName()));
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.AQUA + "  Do you have the following items in your inventory? " + ChatColor.GRAY + "(y/n)");
 			player.sendRawMessage(" ");
 			for(Map.Entry<Material, Integer> entry : deity.getForsakeItems().entrySet())
 				player.sendRawMessage(ChatColor.GRAY + "    " + Unicodes.rightwardArrow() + " " + ChatColor.YELLOW + entry.getValue() + " " + Strings.beautify(entry.getKey().name()).toLowerCase() + (entry.getValue() > 1 ? "s" : ""));
 
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			return message.contains("y") || message.contains("n");
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			Player player = (Player) context.getForWhom();
 
 			// Open inventory
 			Inventory inv = Bukkit.getServer().createInventory(player, 9, "Place Items Here");
 			player.openInventory(inv);
 
 			return null;
 		}
 	}
 
 	// Forsaking cancellation
 	static class CancelForsake extends MessagePrompt implements Elements.Conversations.Category
 	{
 		@Override
 		public String getChatName()
 		{
 			return ChatColor.DARK_RED + "Cancel Forsaking";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			Player player = (Player) context.getForWhom();
 			return DataManager.hasKeyTemp(player.getName(), "currently_forsaking") && DataManager.hasTimed(player.getName(), "currently_forsaking");
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			Deity deity = DPlayer.Util.getPlayer(player).getCurrent().getDeity();
 
 			// Cancel the temp data
 			DataManager.removeTemp(player.getName(), "currently_forsaking");
 			DataManager.removeTimed(player.getName(), "currently_forsaking");
 
 			return "";
 		}
 
 		@Override
 		protected Prompt getNextPrompt(ConversationContext context)
 		{
 			return new StartPrayer();
 		}
 	}
 
 	// Character creation
 	static class CreateCharacter extends ValidatingPrompt implements Elements.Conversations.Category
 	{
 		@Override
 		public String getChatName()
 		{
 			return ChatColor.GREEN + "Create Character";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			return ((Player) context.getForWhom()).hasPermission("demigods.basic.create") && !DataManager.hasKeyTemp(((Player) context.getForWhom()).getName(), "currently_creating") && !DataManager.hasKeyTemp(((Player) context.getForWhom()).getName(), "currently_forsaking");
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			Demigods.message.clearRawChat((Player) context.getForWhom());
 			return ChatColor.AQUA + "Continue to character creation?" + ChatColor.GRAY + " (y/n)";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			return message.contains("y") || message.contains("n");
 		}
 
 		@Override
 		protected ValidatingPrompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			if(message.contains("y")) return new ChooseName();
 			return new StartPrayer();
 		}
 
 		class ChooseName extends ValidatingPrompt
 		{
 			@Override
 			public String getPromptText(ConversationContext context)
 			{
 				Player player = (Player) context.getForWhom();
 				Demigods.message.clearRawChat(player);
 				player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Creating Character"));
 				player.sendRawMessage(" ");
 
 				if(context.getSessionData("name_errors") == null)
 				{
 					// No errors, continue
 					player.sendRawMessage(ChatColor.AQUA + "  Enter a name: " + ChatColor.GRAY + "(Alpha-Numeric Only)");
 				}
 				else
 				{
 					// Grab the errors
 					List<Translation.Text> errors = (List<Translation.Text>) context.getSessionData("name_errors");
 
 					// List the errors
 					for(Translation.Text error : errors)
 					{
 						player.sendRawMessage(ChatColor.RED + "  " + Demigods.language.getText(error).replace("{maxCaps}", String.valueOf(Demigods.config.getSettingInt("character.max_caps_in_name"))));
 					}
 
 					// Ask for a new name
 					player.sendRawMessage(" ");
 					player.sendRawMessage(ChatColor.AQUA + "  Enter a different name: " + ChatColor.GRAY + "(Alpha-Numeric Only)");
 				}
 
 				return "";
 			}
 
 			@Override
 			protected boolean isInputValid(ConversationContext context, String name)
 			{
 				Player player = (Player) context.getForWhom();
 
 				if(name.length() < 4 || name.length() > 14 || !StringUtils.isAlphanumeric(name) || Strings.hasCapitalLetters(name, Demigods.config.getSettingInt("character.max_caps_in_name")) || DPlayer.Util.hasCharName(player, name))
 				{
 					// Create the list
 					List<Translation.Text> errors = Lists.newArrayList();
 
 					// Check the errors
 					if(name.length() < 4 || name.length() >= 14) errors.add(Translation.Text.ERROR_NAME_LENGTH);
 					if(!StringUtils.isAlphanumeric(name)) errors.add(Translation.Text.ERROR_ALPHA_NUMERIC);
 					if(Strings.hasCapitalLetters(name, Demigods.config.getSettingInt("character.max_caps_in_name"))) errors.add(Translation.Text.ERROR_MAX_CAPS);
 					if(DCharacter.Util.charExists(name) || Strings.containsAnyInCollection(name, Demigods.language.getBlackList())) errors.add(Translation.Text.ERROR_CHAR_EXISTS);
 
 					// Save the info
 					context.setSessionData("name_errors", errors);
 					return false;
 				}
 				else
 				{
 					context.setSessionData("name_errors", null);
 					return true;
 				}
 			}
 
 			@Override
 			protected ConfirmName acceptValidatedInput(ConversationContext context, String name)
 			{
 				context.setSessionData("chosen_name", name);
 				return new ConfirmName();
 			}
 		}
 
 		class ConfirmName extends ValidatingPrompt
 		{
 			@Override
 			public String getPromptText(ConversationContext context)
 			{
 				Demigods.message.clearRawChat((Player) context.getForWhom());
 				return ChatColor.GRAY + "Are you sure you want to use " + ChatColor.YELLOW + context.getSessionData("chosen_name") + ChatColor.GRAY + "? (y/n)";
 			}
 
 			@Override
 			protected boolean isInputValid(ConversationContext context, String message)
 			{
 				return message.contains("y") || message.contains("n");
 			}
 
 			@Override
 			protected Prompt acceptValidatedInput(ConversationContext context, String message)
 			{
 				if(message.contains("y")) return new ChooseAlliance();
 				else
 				{
 					context.setSessionData("chosen_name", null);
 					return new ChooseName();
 				}
 			}
 		}
 
 		class ChooseAlliance extends ValidatingPrompt
 		{
 			@Override
 			public String getPromptText(ConversationContext context)
 			{
 				Player player = (Player) context.getForWhom();
 
 				Demigods.message.clearRawChat(player);
 				player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Creating Character"));
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.AQUA + "  Please choose an Alliance: " + ChatColor.GRAY + "(Type in the name of the Alliance)");
 				player.sendRawMessage(" ");
 
 				for(String alliance : Deity.Util.getLoadedPlayableDeityAlliances())
 					player.sendRawMessage(ChatColor.GRAY + "    " + Unicodes.rightwardArrow() + " " + ChatColor.YELLOW + StringUtils.capitalize(alliance.toLowerCase()));
 
 				return "";
 			}
 
 			@Override
 			protected boolean isInputValid(ConversationContext context, String alliance)
 			{
				return Deity.Util.getLoadedPlayableDeityAlliances().contains(alliance);
 			}
 
 			@Override
 			protected Prompt acceptValidatedInput(ConversationContext context, String alliance)
 			{
 				context.setSessionData("chosen_alliance", alliance);
 				return new ConfirmAlliance();
 			}
 		}
 
 		class ConfirmAlliance extends ValidatingPrompt
 		{
 			@Override
 			public String getPromptText(ConversationContext context)
 			{
 				Demigods.message.clearRawChat((Player) context.getForWhom());
 				return ChatColor.GRAY + "Are you sure you want to join the " + ChatColor.YELLOW + StringUtils.capitalize(((String) context.getSessionData("chosen_alliance")).toLowerCase()) + "s" + ChatColor.GRAY + "? (y/n)";
 			}
 
 			@Override
 			protected boolean isInputValid(ConversationContext context, String message)
 			{
 				return message.contains("y") || message.contains("n");
 			}
 
 			@Override
 			protected Prompt acceptValidatedInput(ConversationContext context, String message)
 			{
 				if(message.contains("y")) return new ChooseDeity();
 				else
 				{
 					context.setSessionData("chosen_alliance", null);
 					return new ChooseAlliance();
 				}
 			}
 		}
 
 		class ChooseDeity extends ValidatingPrompt
 		{
 			@Override
 			public String getPromptText(ConversationContext context)
 			{
 				Player player = (Player) context.getForWhom();
 
 				Demigods.message.clearRawChat(player);
 				player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Creating Character"));
 				player.sendRawMessage(" ");
 				player.sendRawMessage(ChatColor.AQUA + "  Please choose a Deity: " + ChatColor.GRAY + "(Type in the name of the Deity)");
 				player.sendRawMessage(" ");
 
 				for(Deity deity : Deity.Util.getLoadedPlayableDeitiesInAlliance((String) context.getSessionData("chosen_alliance")))
 					if(player.hasPermission(deity.getPermission())) player.sendRawMessage(ChatColor.GRAY + "    " + Unicodes.rightwardArrow() + " " + ChatColor.YELLOW + StringUtils.capitalize(deity.getName()));
 
 				return "";
 			}
 
 			@Override
 			protected boolean isInputValid(ConversationContext context, String deityName)
 			{
 				return Deity.Util.getDeity(deityName) != null;
 			}
 
 			@Override
 			protected Prompt acceptValidatedInput(ConversationContext context, String deityName)
 			{
 				context.setSessionData("chosen_deity", deityName);
 				return new ConfirmDeity();
 			}
 		}
 
 		class ConfirmDeity extends ValidatingPrompt
 		{
 			@Override
 			public String getPromptText(ConversationContext context)
 			{
 				Demigods.message.clearRawChat((Player) context.getForWhom());
 				Deity deity = Deity.Util.getDeity((String) context.getSessionData("chosen_deity"));
 				return ChatColor.GRAY + "Are you sure you want to use " + deity.getColor() + deity.getName() + ChatColor.GRAY + "? (y/n)";
 			}
 
 			@Override
 			protected boolean isInputValid(ConversationContext context, String message)
 			{
 				return message.contains("y") || message.contains("n");
 			}
 
 			@Override
 			protected Prompt acceptValidatedInput(ConversationContext context, String message)
 			{
 				if(message.contains("y"))
 				{
 					// Define variables
 					Player player = (Player) context.getForWhom();
 					String chosenDeity = (String) context.getSessionData("chosen_deity");
 
 					// Give the player further directions
 					Demigods.message.clearRawChat(player);
 					player.sendRawMessage(ChatColor.AQUA + "  Before you can confirm your lineage with " + ChatColor.YELLOW + StringUtils.capitalize(chosenDeity) + ChatColor.AQUA + ",");
 					player.sendRawMessage(ChatColor.AQUA + "  you must first sacrifice the following items:");
 					player.sendRawMessage(" ");
 					for(Map.Entry<Material, Integer> entry : Deity.Util.getDeity(chosenDeity).getClaimItems().entrySet())
 						player.sendRawMessage(ChatColor.GRAY + "    " + Unicodes.rightwardArrow() + " " + ChatColor.YELLOW + entry.getValue() + " " + Strings.beautify(entry.getKey().name()).toLowerCase() + (entry.getValue() > 1 ? "s" : ""));
 					player.sendRawMessage(" ");
 					player.sendRawMessage(ChatColor.GRAY + "  After you obtain these items, return to an Altar to");
 					player.sendRawMessage(ChatColor.GRAY + "  confirm your new character.");
 					player.sendRawMessage(" ");
 					player.sendRawMessage(ChatColor.AQUA + "  Your prayer has been disabled.");
 					player.sendRawMessage(" ");
 
 					// Save temporary data, end the conversation, and return
 					DataManager.saveTemp(((Player) context.getForWhom()).getName(), "currently_creating", true);
 					DataManager.saveTimed(player.getName(), "currently_creating", true, 600);
 					DPlayer.Util.togglePrayingSilent(player, false, true);
 					return null;
 				}
 				else
 				{
 					context.setSessionData("chosen_deity", null);
 					return new ChooseDeity();
 				}
 			}
 		}
 	}
 
 	// Character confirmation
 	static class ConfirmCharacter extends ValidatingPrompt implements Elements.Conversations.Category
 	{
 		@Override
 		public String getChatName()
 		{
 			return ChatColor.GREEN + "Confirm Character";
 		}
 
 		@Override
 		public boolean canUse(ConversationContext context)
 		{
 			Player player = (Player) context.getForWhom();
 			return DataManager.hasKeyTemp(player.getName(), "currently_creating") && DataManager.hasTimed(player.getName(), "currently_creating");
 		}
 
 		@Override
 		public String getPromptText(ConversationContext context)
 		{
 			// Define variables
 			Player player = (Player) context.getForWhom();
 			String chosenDeity = (String) context.getSessionData("chosen_deity");
 
 			// Clear chat
 			Demigods.message.clearRawChat(player);
 
 			// Ask them if they have the items
 			player.sendRawMessage(ChatColor.YELLOW + Titles.chatTitle("Confirming Character"));
 			player.sendRawMessage(" ");
 			player.sendRawMessage(ChatColor.AQUA + "  Do you have the following items in your inventory?" + ChatColor.GRAY + " (y/n)");
 			player.sendRawMessage(" ");
 			for(Map.Entry<Material, Integer> entry : Deity.Util.getDeity(chosenDeity).getClaimItems().entrySet())
 				player.sendRawMessage(ChatColor.GRAY + "    " + Unicodes.rightwardArrow() + " " + ChatColor.YELLOW + entry.getValue() + " " + Strings.beautify(entry.getKey().name()).toLowerCase() + (entry.getValue() > 1 ? "s" : ""));
 			return "";
 		}
 
 		@Override
 		protected boolean isInputValid(ConversationContext context, String message)
 		{
 			return message.contains("y") || message.contains("n");
 		}
 
 		@Override
 		protected Prompt acceptValidatedInput(ConversationContext context, String message)
 		{
 			Player player = (Player) context.getForWhom();
 
 			// Open inventory
 			Inventory inv = Bukkit.getServer().createInventory(player, 9, "Place Your Tributes Here");
 			player.openInventory(inv);
 
 			return null;
 		}
 	}
 
 	public static class Listener implements org.bukkit.event.Listener
 	{
 		@EventHandler(priority = EventPriority.HIGH)
 		public void prayerInteract(PlayerInteractEvent event)
 		{
 			if(event.getClickedBlock() == null || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
 
 			// Define variables
 			Player player = event.getPlayer();
 
 			// First we check if the player is clicking a prayer block
 			if(Structures.isClickableBlockWithFlag(event.getClickedBlock().getLocation(), Structure.Flag.PRAYER_LOCATION))
 			{
 				if(!DPlayer.Util.isPraying(player))
 				{
 					if(DPlayer.Util.getPlayer(player).canPvp())
 					{
 						for(String message : Demigods.language.getTextBlock(Translation.Text.PVP_NO_PRAYER))
 							player.sendMessage(message);
 						event.setCancelled(true);
 						return;
 					}
 
 					// Toggle praying
 					DPlayer.Util.togglePraying(player, true);
 
 					// Tell nearby players that the user is praying
 					for(Entity entity : player.getNearbyEntities(20, 20, 20))
 						if(entity instanceof Player) ((Player) entity).sendMessage(ChatColor.AQUA + Demigods.language.getText(Translation.Text.KNELT_FOR_PRAYER).replace("{player}", ChatColor.stripColor(player.getDisplayName())));
 				}
 				else if(DPlayer.Util.isPraying(player))
 				{
 					// Toggle prayer to false
 					DPlayer.Util.togglePraying(player, false);
 				}
 
 				event.setCancelled(true);
 			}
 		}
 
 		@EventHandler(priority = EventPriority.MONITOR)
 		public void confirmDeity(InventoryCloseEvent event)
 		{
 			try
 			{
 				if(!(event.getPlayer() instanceof Player)) return;
 				Player player = (Player) event.getPlayer();
 
 				// If it isn't a confirmation chest then exit
 				if(!event.getInventory().getName().contains("Place Your Tributes Here")) return;
 
 				// Exit if this isn't for character creation
 				if(!DPlayer.Util.isPraying(player)) return;
 
 				// Define variables
 				ConversationContext prayerContext = DPlayer.Util.getPrayerContext(player);
 				String chosenName = (String) prayerContext.getSessionData("chosen_name");
 				Deity deity = Deity.Util.getDeity((String) prayerContext.getSessionData("chosen_deity"));
 				String deityAlliance = deity.getAlliance();
 
 				// Check the chest items
 				int items = 0;
 				int neededItems = deity.getClaimItems().size();
 
 				for(Map.Entry<Material, Integer> entry : deity.getClaimItems().entrySet())
 					if(event.getInventory().containsAtLeast(new ItemStack(entry.getKey()), entry.getValue())) items++;
 
 				// Stop their praying
 				DPlayer.Util.togglePrayingSilent(player, false, true);
 
 				// Clear chat and send update
 				Demigods.message.clearRawChat(player);
 				player.sendMessage(ChatColor.YELLOW + "The " + deityAlliance + "s are pondering your offerings...");
 
 				if(neededItems == items)
 				{
 					// Accepted, finish everything up!
 					DCharacter.Util.create(DPlayer.Util.getPlayer(player), deity.getName(), chosenName, true);
 
 					// Message them and do cool things
 					player.sendMessage(ChatColor.GREEN + Demigods.language.getText(Translation.Text.CHARACTER_CREATE_COMPLETE).replace("{deity}", deity.getName()));
 					player.getWorld().strikeLightningEffect(player.getLocation());
 
 					for(int i = 0; i < 20; i++)
 						player.getWorld().spawn(player.getLocation(), ExperienceOrb.class);
 
 					// Remove temp data
 					DataManager.removeTemp(player.getName(), "currently_creating");
 					DataManager.removeTimed(player.getName(), "currently_creating");
 
 					// Clear the prayer session
 					DPlayer.Util.clearPrayerSession(player);
 				}
 				else
 				{
 					player.sendMessage(ChatColor.RED + "You have been denied entry into the lineage of " + deity.getName() + "!");
 				}
 
 				// Clear the confirmation case
 				event.getInventory().clear();
 			}
 			catch(Exception e)
 			{
 				// Print error for debugging
 				e.printStackTrace();
 			}
 		}
 
 		@EventHandler(priority = EventPriority.MONITOR)
 		public void forsakeDeity(InventoryCloseEvent event)
 		{
 			try
 			{
 				if(!(event.getPlayer() instanceof Player)) return;
 				Player player = (Player) event.getPlayer();
 
 				// If it isn't a confirmation chest then exit
 				if(!event.getInventory().getName().contains("Place Items Here")) return;
 
 				// Exit if this isn't for character creation
 				if(!DPlayer.Util.isPraying(player)) return;
 
 				// Define variables
 				DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 				Deity deity = character.getDeity();
 
 				// Check the chest items
 				int items = 0;
 				int neededItems = deity.getForsakeItems().size();
 
 				for(Map.Entry<Material, Integer> entry : deity.getForsakeItems().entrySet())
 					if(event.getInventory().containsAtLeast(new ItemStack(entry.getKey()), entry.getValue())) items++;
 
 				// Stop their praying
 				DPlayer.Util.togglePrayingSilent(player, false, true);
 
 				// Clear chat and send update
 				Demigods.message.clearRawChat(player);
 				player.sendMessage(ChatColor.YELLOW + deity.getName() + " is debating your departure...");
 
 				if(neededItems == items)
 				{
 					// Accepted, delete the character and message the player
 					character.remove();
 					player.sendMessage(ChatColor.GREEN + "You are now free from the will of " + deity.getName() + "!");
 
 					// Add potion effects for fun
 					PotionEffect potion = new PotionEffect(PotionEffectType.WEAKNESS, 1200, 3);
 					player.addPotionEffect(potion);
 
 					// Remove temp
 					DataManager.removeTemp(player.getName(), "currently_forsaking");
 					DataManager.removeTimed(player.getName(), "currently_forsaking");
 
 					// Clear the prayer session
 					DPlayer.Util.clearPrayerSession(player);
 				}
 				else
 				{
 					player.sendMessage(ChatColor.RED + deity.getName() + " has denied your forsaking!");
 				}
 
 				// Clear the confirmation case
 				event.getInventory().clear();
 			}
 			catch(Exception e)
 			{
 				// Print error for debugging
 				e.printStackTrace();
 			}
 		}
 
 		@EventHandler(priority = EventPriority.MONITOR)
 		private void onPlayerMove(PlayerMoveEvent event)
 		{
 			// Define variables
 			Player player = event.getPlayer();
 
 			if(DPlayer.Util.isPraying(player) && event.getTo().distance((Location) DataManager.getValueTemp(player.getName(), "prayer_location")) >= Demigods.config.getSettingInt("zones.prayer_radius")) DPlayer.Util.togglePraying(player, false);
 		}
 	}
 }
