 package com.censoredsoftware.Demigods.Engine.Utility;
 
 import org.bukkit.ChatColor;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Language.Translation;
 
 // TODO Replace with YAML.
 
 public class TextUtility
 {
 	public static enum Text
 	{
 		ERROR_BIND_WEAPON_REQUIRED, DATA_RESET_KICK, ADMIN_CLEAR_DATA_STARTING, ADMIN_CLEAR_DATA_FINISHED, SUCCESS_ABILITY_BOUND, SUCCESS_ABILITY_UNBOUND, ERROR_BIND_TO_SLOT, NOTIFICATION_WARP_CREATED, NOTIFICATION_WARP_DELETED, NOTIFICATION_INVITE_SENT, ERROR_NAME_LENGTH, ERROR_CHAR_EXISTS, ERROR_ALPHA_NUMERIC, ERROR_MAX_CAPS, CREATE_OBELISK, ALTAR_SPAWNED_NEAR, PROTECTED_BLOCK, ADMIN_WAND_GENERATE_ALTAR, ADMIN_WAND_GENERATE_ALTAR_COMPLETE, ADMIN_WAND_REMOVE_ALTAR, ADMIN_WAND_REMOVE_ALTAR_COMPLETE, CREATE_SHRINE_1, CREATE_SHRINE_2, ADMIN_WAND_REMOVE_SHRINE, ADMIN_WAND_REMOVE_SHRINE_COMPLETE, NO_WARP_ALTAR, CHARACTER_CREATE_COMPLETE, KILLSTREAK, MORTAL_SLAIN_1, MORTAL_SLAIN_2, DEMI_SLAIN_1, DEMI_SLAIN_2, DEMI_BETRAY, MORTAL_BETRAY, COMMAND_BLOCKED_BATTLE, NO_PVP_ZONE, WEAKER_THAN_YOU, YOU_FAILED_DEITY
 	}
 
 	public static class English implements Translation
 	{
 		@Override
 		public int version()
 		{
 			return 1;
 		}
 
 		@Override
 		public String translator()
 		{
 			return "Censored Software";
 		}
 
 		@Override
 		public String getText(Enum text)
 		{
 			if(!(text instanceof Text)) throw new NullPointerException("No such translation.");
 			switch((Text) text)
 			{
 				case ERROR_BIND_WEAPON_REQUIRED:
 					return "A {weapon} is required to bind {ability}!";
 				case DATA_RESET_KICK:
 					return "All Demigods data has been reset, you may now rejoin!";
 				case ADMIN_CLEAR_DATA_STARTING:
 					return "Clearing all Demigods data...";
 				case ADMIN_CLEAR_DATA_FINISHED:
 					return "All Demigods data cleared successfully!";
 				case SUCCESS_ABILITY_BOUND:
 					return "{ability} has been bound to slot {slot}!";
 				case SUCCESS_ABILITY_UNBOUND:
 					return "{ability} has been unbound.";
 				case ERROR_BIND_TO_SLOT:
 					return "You can only bind to an empty slot.";
 				case NOTIFICATION_WARP_CREATED:
 					return ChatColor.GREEN + "Warp created!";
 				case NOTIFICATION_WARP_DELETED:
 					return ChatColor.RED + "Warp deleted!";
 				case NOTIFICATION_INVITE_SENT:
 					return ChatColor.GREEN + "Invite sent!";
 				case ERROR_ALPHA_NUMERIC:
 					return "Only alpha-numeric characters are allowed.";
 				case ERROR_NAME_LENGTH:
					return "Your name should be between 4 and 13 characters.";
 				case ERROR_CHAR_EXISTS:
 					return "A character with that name already exists.";
 				case ERROR_MAX_CAPS:
 					return "Please use no more than " + Demigods.config.getSettingInt("character.max_caps_in_name") + " capital letters.";
 				case CREATE_OBELISK:
 					return "You created an Obelisk!";
 				case PROTECTED_BLOCK:
 					return "That block is protected by a Deity!";
 				case ADMIN_WAND_GENERATE_ALTAR:
 					return "Generating new Altar...";
 				case ADMIN_WAND_GENERATE_ALTAR_COMPLETE:
 					return "Altar created!";
 				case ADMIN_WAND_REMOVE_ALTAR:
 					return "Right-click this Altar again to remove it.";
 				case ADMIN_WAND_REMOVE_ALTAR_COMPLETE:
 					return "Altar removed!";
 				case ALTAR_SPAWNED_NEAR:
 					return "An Altar has spawned near you...";
 				case CREATE_SHRINE_1:
 					return "The {alliance} are pleased...";
 				case CREATE_SHRINE_2:
 					return "You have created a Shrine in the name of {deity}!";
 				case ADMIN_WAND_REMOVE_SHRINE:
 					return "Right-click this Shrine again to remove it.";
 				case ADMIN_WAND_REMOVE_SHRINE_COMPLETE:
 					return "Shrine removed!";
 				case NO_WARP_ALTAR:
 					return "You've never set a warp at this Altar.";
 				case CHARACTER_CREATE_COMPLETE:
 					return "You have been accepted into the lineage of {deity}!";
 				case KILLSTREAK:
 					return "{character} is on a killstreak of {kills} kills.";
 				case MORTAL_SLAIN_1:
 					return ChatColor.YELLOW + "A mortal" + ChatColor.GRAY + " was slain by " + ChatColor.YELLOW + "another mortal" + ChatColor.GRAY + ".";
 				case MORTAL_SLAIN_2:
 					return ChatColor.YELLOW + "A mortal" + ChatColor.GRAY + " was slain by {attacker} of the {attackerAlliance} alliance.";
 				case DEMI_SLAIN_1:
 					return "{killed} of the {killedAlliance} was slain by " + ChatColor.YELLOW + "a mortal" + ChatColor.GRAY + ".";
 				case DEMI_SLAIN_2:
 					return "{killed} of the {killedAlliance} was slain by {attacker} of the {attackerAlliance} alliance.";
 				case DEMI_BETRAY:
 					return "{killed} was betrayed by {attacker} of the {alliance} alliance.";
 				case MORTAL_BETRAY:
 					return "A mortal was killed by another worthless mortal.";
 				case COMMAND_BLOCKED_BATTLE:
 					return "That command is blocked during a battle.";
 				case NO_PVP_ZONE:
 					return "No-PVP in this zone.";
 				case WEAKER_THAN_YOU:
 					return "One weaker than you has been slain by your hand.";
 				case YOU_FAILED_DEITY:
 					return "You have failed {deity}!";
 			}
 			throw new NullPointerException("No such translation.");
 		}
 
 	}
 
 	public static class Engrish implements Translation
 	{
 		@Override
 		public int version()
 		{
 			return 1;
 		}
 
 		@Override
 		public String translator()
 		{
 			return "Software that is Censored";
 		}
 
 		@Override
 		public String getText(Enum text)
 		{
 			if(!(text instanceof Text)) throw new NullPointerException("No such translation.");
 			Text get = (Text) text;
 			switch(get)
 			{
 				case PROTECTED_BLOCK:
 					return UnicodeUtility.heavyHeart() + " " + "Block is protected by God!";
 				case ADMIN_WAND_GENERATE_ALTAR:
 					return UnicodeUtility.heavyHeart() + " " + "Generate a new Altar...";
 				case ADMIN_WAND_GENERATE_ALTAR_COMPLETE:
 					return UnicodeUtility.heavyHeart() + " " + "The creation of the Altar!";
 				case ADMIN_WAND_REMOVE_ALTAR:
 					return UnicodeUtility.heavyHeart() + " " + "Right-click this Altar to remove it again.";
 				case ADMIN_WAND_REMOVE_ALTAR_COMPLETE:
 					return UnicodeUtility.heavyHeart() + " " + "Removal of the Altar!";
 				case CREATE_SHRINE_1:
 					return UnicodeUtility.heavyHeart() + " " + "{alliance} are satisfied....";
 				case CREATE_SHRINE_2:
 					return UnicodeUtility.heavyHeart() + " " + "Create a shrine of {deity}!";
 				case ADMIN_WAND_REMOVE_SHRINE:
 					return UnicodeUtility.heavyHeart() + " " + "Right-click this Shrine to remove it again.";
 				case ADMIN_WAND_REMOVE_SHRINE_COMPLETE:
 					return UnicodeUtility.heavyHeart() + " " + "Removal of the Shrine!";
 				case NO_WARP_ALTAR:
 					return UnicodeUtility.heavyHeart() + " " + "Set warp on this Altar.";
 				case CHARACTER_CREATE_COMPLETE:
 					return UnicodeUtility.heavyHeart() + " " + "You have been accepted to the genealogy of {deity}!";
 				case KILLSTREAK:
 					return UnicodeUtility.heavyHeart() + " " + "{character} killstreak {kills} kills.";
 				case MORTAL_SLAIN_1:
 					return ChatColor.YELLOW + UnicodeUtility.heavyHeart() + " " + "Man" + ChatColor.GRAY + " killed by " + ChatColor.YELLOW + "another man" + ChatColor.GRAY + ".";
 				case MORTAL_SLAIN_2:
 					return ChatColor.YELLOW + UnicodeUtility.heavyHeart() + " " + "The man" + ChatColor.GRAY + " killed by the {attackerAlliance} allies {attacker}.";
 				case DEMI_SLAIN_1:
 					return UnicodeUtility.heavyHeart() + " " + "{killed} {killedAlliance} is killed by " + ChatColor.YELLOW + "humans" + ChatColor.GRAY + ".";
 				case DEMI_SLAIN_2:
 					return UnicodeUtility.heavyHeart() + " " + "{killed} {attacker} {killedAlliance} {attackerAlliance} league-allies were killed.";
 				case DEMI_BETRAY:
 					return UnicodeUtility.heavyHeart() + " " + "The vibrant performance by {attacker}, {killed} {alliance} ally.";
 				case MORTAL_BETRAY:
 					return UnicodeUtility.heavyHeart() + " " + "Value is no longer a killer.";
 				case COMMAND_BLOCKED_BATTLE:
 					return UnicodeUtility.heavyHeart() + " " + "Command is blocked during the battle.";
 				case NO_PVP_ZONE:
 					return UnicodeUtility.heavyHeart() + " " + "No zone-PvP.";
 				case WEAKER_THAN_YOU:
 					return UnicodeUtility.heavyHeart() + " " + "Also should die too weak on your hands.";
 				case YOU_FAILED_DEITY:
 					return UnicodeUtility.heavyHeart() + " " + "{deity} has failed!";
 			}
 			return new English().getText(text);
 		}
 	}
 }
