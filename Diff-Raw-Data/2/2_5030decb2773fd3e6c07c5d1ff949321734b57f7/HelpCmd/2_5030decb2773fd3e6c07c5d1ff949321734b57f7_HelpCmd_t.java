 package com.undeadscythes.udsplugin.commands;
 
 import com.undeadscythes.udsplugin.*;
 import java.util.*;
 
 /**
  * Get help on certain commands.
  * @author UndeadScythes
  */
 public class HelpCmd extends PlayerCommandExecutor {
     private enum Usage {
         HELP(Perm.HELP, "/help [page or command]", "Show these help pages.", false, false),
         TICKET(Perm.TICKET, "/ticket", "Get the link to post suggestions and bugs to.", false, false),
         ACCEPTRULES(Perm.ACCEPTRULES, "/acceptrules", "Accept the rules and get build rights.", false, false),
         ADMIN(Perm.ADMIN_HELP, "/help admin", "Show more admin commands.", true, false),
         ADMIN_LOCKDOWN(Perm.LOCKDOWN, "/lockdown", "Toggle the server's lockdown mode.", false, true),
         ADMIN_LOCKDOWN_PASS(Perm.LOCKDOWN, "/lockdown <player>", "Issue a lockdown pass.", false, true),
         ADMIN_SETSPAWN(Perm.SETSPAWN, "/setspawn", "Set the world spawn point.", false, true),
         MOD(Perm.MOD_HELP, "/help mod", "Show more mod commands.", true, false),
         MOD_A(Perm.A, "/a", "Switch to the admin chat channel.", false, true),
         MOD_A_MSG(Perm.A, "/a <message>", "Send a message in the admin chat channel.", false, true),
         MOD_BROADCAST(Perm.BROADCAST, "/b <message>", "Send a server message.", false, true),
         MOD_BAN(Perm.BAN, "/ban <player>", "Ban a player from the server.", false, true),
         MOD_UNBAN(Perm.UNBAN, "/unban <player>", "Unban a player from the server.", false, true),
         MOD_BUTCHER(Perm.BUTCHER, "/butcher", "Kill hostile mobs around you.", false, true),
         MOD_BUTCHER_ALL(Perm.BUTCHER, "/butcher a", "Kill all mobs around you.", false, true),
         MOD_ENCHANT(Perm.ENCHANT, "/enchant <enchantment> [level]", "Enchant an item.", false, true),
         MOD_ENCHANT_LIST(Perm.ENCHANT, "/enchant", "Get a list of available enchantments.", false, true),
         MOD_GOD(Perm.GOD, "/god [player]", "Toggle a players god mode", false, true),
         MOD_HEAL(Perm.HEAL, "/heal [player]", "Heal a player.", false, true),
         MOD_INVSEE(Perm.INVSEE, "/invsee <player>", "Get a copy of a players inventory.", false, true),
         MOD_INVSEE_RESET(Perm.INVSEE, "/invsee", "Get your inventory back.", false, true),
         MOD_KICK(Perm.KICK, "/kick <player> [reason]", "Kick a player from the server.", false, true),
         MOD_NICK_OTHER(Perm.NICK_OTHER, "/nick <player> <nickname>", "Change a players nickname.", false, true),
         MOD_POWERTOOL(Perm.POWERTOOL, "/powertool <command>", "Set a powertool.", false, true),
         MOD_PROMOTE(Perm.PROMOTE, "/promote <player>", "Promote a player.", false, true),
         MOD_DEMOTE(Perm.DEMOTE, "/demote <player>", "Demote a player.", false, true),
         MOD_SIGNS(Perm.SIGNS, "/signs", "View a list of special signs you can make.", false, true),
         MOD_SPAWNER(Perm.SPAWNER, "/spawner <monster>", "Set the mob type of a spawner.", false, true),
         MOD_TGM(Perm.TGM, "/tgm [player]", "Toggle a players game mode.", false, true),
         MOD_TP(Perm.TP, "/tp <player>", "Teleport to another player.", false, true),
         MOD_TP_OTHER(Perm.TP, "/tp <player> <player>", "Teleport one player to another.", false, true),
         MOD_XP(Perm.XP, "/xp <player> <level>", "Give a player experience.", false, true),
         MOD_XP_RESET(Perm.XP, "/xp <player> reset", "Reset a players level.", false, true),
         WARDEN(Perm.WARDEN_HELP, "/help warden", "Show more warden commands.", true, false),
         WARDEN_DAY(Perm.DAY, "/day", "Set the server time to day.", false, true),
         WARDEN_NIGHT(Perm.NIGHT, "/night", "Set the server time to night.", false, true),
         WARDEN_JAIL(Perm.JAIL, "/jail <player> [time] [bail]", "Jail a player.", false, true),
         WARDEN_UNJAIL(Perm.UNJAIL, "/unjail <player>", "Release a player from jail.", false, true),
         WARDEN_SETWARP(Perm.SETWARP, "/setwarp <name> [rank] [price]", "Set a warp point.", false, true),
         WARDEN_DELWARP(Perm.DELWARP, "/delwarp <warp>", "Delete a warp point.", false, true),
         WARDEN_SUN(Perm.SUN, "/sun", "Set the server weather to sunny.", false, true),
         WARDEN_RAIN(Perm.RAIN, "/rain [duration]", "Set the server weather to rainy.", false, true),
         WARDEN_STORM(Perm.STORM, "/storm [duration]", "Set the server weather to stormy.", false, true),
         VIP(Perm.VIP_HELP, "/help vip", "Show more vip commands.", true, false),
         VIP_I(Perm.I, "/i <item>[:data] [amount]", "Spawn an item.", false, true),
         VIP_SPAWNS(Perm.VIP, "/vip", "Check the number of daily spawns you have left.", false, true),
         VIP_ITEMS(Perm.VIP, "/vip spawns", "Check which items are whitelisted.", false, true),
         VIP_WE(Perm.WE_VIP, "/we help", "Check which WE commands you can use.", false, true),
         BACK(Perm.BACK, "/back", "Return to your position before teleporting.", false, false),
         BOUNTY(Perm.BOUNTY, "/bounty <player> <amount>", "Place a bounty on a player.", false, false),
         BOUNTY_LIST(Perm.BOUNTY, "/bounty", "Check the bounties list.", false, false),
         CALL(Perm.CALL, "/call <player>", "Request a teleport to another player.", false, false),
        CHALLENGE(Perm.CHALLENGE, "/challenge <player> <wager>", "Challenge a player to a duel.", false, false),
         CHECK(Perm.CHECK, "/check", "Teleport to your saved checkpoint.", false, false),
         CHAT(Perm.CHAT_HELP, "/help chat", "Show more chat commands.", true, false),
         CHAT_IGNORE(Perm.IGNORE, "/ignore <player>", "Ignore or unignore a player in chat.", false, true),
         CHAT_PRIVATE(Perm.PRIVATE, "/private <chat room>", "Enter or exit private chat rooms.", false, true),
         CHAT_P(Perm.P, "/p", "Switch to the chat room channel.", false, true),
         CHAT_P_MSG(Perm.P, "/p <message>", "Send a message to the chat room.", false, true),
         CHAT_TELL(Perm.TELL, "/tell <player> <message>", "Send a private message to a player.", false, true),
         CHAT_R(Perm.R, "/r <message>", "Reply to a private message.", false, true),
         CI(Perm.CI, "/ci", "Clear all the items from your inventory. (Not reversible)", false, false),
         CITY(Perm.CITY, "/help city", "Show more city commands.", true, false),
         CITY_NEW(Perm.CITY, "/city new", "Found a new city where you are stood.", false, true),
         CITY_INVITE(Perm.CITY, "/city invite <city> <player>", "Invite a player to a city.", false, true),
         CITY_SET(Perm.CITY, "/city set <city>", "Set the warp point for your city.", false, true),
         CITY_LIST(Perm.CITY, "/city list", "Get a list of cities on the server.", false, true),
         CITY_WARP(Perm.CITY, "/city warp <city>", "Teleport to a city.", false, true),
         CITY_LEAVE(Perm.CITY, "/city leave <city>", "Leave a city.", false, true),
         CITY_BANISH(Perm.CITY, "/city banish <city> <player>", "Banish a player from a city.", false, true),
         CLAN(Perm.CLAN, "/help clan", "Show more clan commands.", true, false),
         CLAN_HELP(Perm.CLAN, "/clan help [page]", "Show these help pages.", false, true),
         CLAN_C(Perm.C, "/c", "Switch to the clan chat channel.", false, true),
         CLAN_C_MSG(Perm.C, "/c <message>", "Send a message in the clan chat channel.", false, true),
         CLAN_BASE(Perm.CLAN, "/clan base", "Teleport to your clan base.", false, true),
         CLAN_BASE_MAKE(Perm.CLAN, "/clan base make", "Make a base for your clan.", false, true),
         CLAN_BASE_CLEAR(Perm.CLAN, "/clan base clear", "Clear your clan base.", false, true),
         CLAN_BASE_SET(Perm.CLAN, "/clan base set", "Set your bases warp point.", false, true),
         CLAN_DISBAND(Perm.CLAN, "/clan disband", "Disband your clan.", false, true),
         CLAN_INVITE(Perm.CLAN, "/clan invite <player>", "Invite a player to your clan.", false, true),
         CLAN_KICK(Perm.CLAN, "/clan kick <player>", "Kick a player from your clan.", false, true),
         CLAN_LEAVE(Perm.CLAN, "/clan leave", "Leave your clan.", false, true),
         CLAN_LIST(Perm.CLAN, "/clan list", "View a list of clans.", false, true),
         CLAN_MEMBERS(Perm.CLAN, "/clan members", "View a list of your fellow clan members.", false, true),
         CLAN_MEMBERS_OTHER(Perm.CLAN, "/clan members <clan>", "View a list of clan members.", false, true),
         CLAN_NEW(Perm.CLAN, "/clan new <name>", "Make a new clan.", false, true),
         CLAN_OWNER(Perm.CLAN, "/clan owner <player>", "Make another member the owner of your clan.", false, true),
         CLAN_RENAME(Perm.CLAN, "/clan rename <name>", "Rename your clan.", false, true),
         CLAN_STATS(Perm.CLAN, "/clan stats", "View your clans stats.", false, true),
         CLAN_STATS_OTHER(Perm.CLAN, "/clan stats <clan>", "View another clans stats.", false, true),
         FACE(Perm.FACE, "/face <direction>", "Turn you character to face a direction.", false, false),
         FACE_GET(Perm.FACE, "/face", "Find out which way you are facing.", false, false),
         GIFT(Perm.GIFT, "/gift <player> [message]", "Send a player a gift.", false, false),
         HOME(Perm.HOME, "/help home", "Show more home commands.", true, false),
         HOME_HELP(Perm.HOME, "/home help [page]", "Show these help pages.", false, true),
         HOME_TP(Perm.HOME, "/home", "Teleport to your home.", false, true),
         HOME_TP_OTHER(Perm.HOME, "/home <player>", "Teleport to a room mates home.", false, true),
         HOME_ADD(Perm.HOME, "/home add <player>", "Add a player as a room mate.", false, true),
         HOME_KICK(Perm.HOME, "/home kick <player>", "Kick a player from your room mates.", false, true),
         HOME_BOOT(Perm.HOME, "/home boot <player>", "Forcibly boot a player from your home.", false, true),
         HOME_EXPAND(Perm.HOME, "/home expand <direction>", "Expand your home protection.", false, true),
         HOME_LOCK(Perm.HOME, "/home lock", "Lock your house.", false, true),
         HOME_UNLOCK(Perm.HOME, "/home unlock", "Unlock your house.", false, true),
         HOME_MAKE(Perm.HOME, "/home make", "Set a new home protection.", false, true),
         HOME_CLEAR(Perm.HOME, "/home clear", "Clear your home protection.", false, true),
         HOME_ROOMIES(Perm.HOME, "/home roomies", "Check your current room mates.", false, true),
         HOME_SELL(Perm.HOME, "/home sell <player> <price>", "Sell your home to another player.", false, true),
         HOME_SET(Perm.HOME, "/home set", "Set your home warp point.", false, true),
         KIT(Perm.KIT, "/kit <name>", "Purchase a kit.", false, false),
         KIT_LIST(Perm.KIT, "/kit", "Check the available kits.", false, false),
         MAP(Perm.MAP, "/map", "Get a copy of the spawn map.", false, false),
         ME(Perm.ME, "/me <action>", "Perform a chat action.", false, false),
         NICK(Perm.NICK, "/nick <nickname>", "Change your nickname.", false, false),
         MONEY(Perm.MONEY, "/help money", "Show more money commands.", true, false),
         MONEY_(Perm.MONEY, "/money", "Check how much money you have.", false, true),
         MONEY_PRICES(Perm.MONEY, "/money prices", "Check the server prices.", false, true),
         MONEY_PAY(Perm.MONEY, "/money pay <player> <amount>", "Pay a player some money.", false, true),
         MONEY_RANK(Perm.MONEY, "/money rank", "Check the money rankings.", false, true),
         MONEY_GRANT(Perm.MONEY_ADMIN, "/money grant <player> <amount>", "Give a player some money.", false, true),
         MONEY_SET(Perm.MONEY_ADMIN, "/money set <player> <amount>", "Set a players account.", false, true),
         PAYBAIL(Perm.PAYBAIL, "/paybail", "Pay your bail to get out of jail.", false, false),
         PET_GIVE(Perm.PET, "/pet give <player>", "Give your pet to a player.", false, false),
         PET_SELL(Perm.PET, "/pet sell <player> <price>", "Sell your pet to a player.", false, false),
         REGION(Perm.REGION, "/help region", "Show more region commands.", true, false),
         REGION_HELP(Perm.REGION, "/region help [page]", "Show these help pages.", false, true),
         REGION_ADDMEMBER(Perm.REGION, "/region addmember <region> <player>", "Add a member.", false, true),
         REGION_DELMEMBER(Perm.REGION, "/region delmember <region> <player>", "Remove a member.", false, true),
         REGION_FLAG(Perm.REGION, "/region flag <region> <flag>", "Toggle a region flag.", false, true),
         REGION_INFO(Perm.REGION, "/region info <region>", "Get detailed info on a region.", false, true),
         REGION_LIST(Perm.REGION, "/region list <type>", "List regions of a certain type.", false, true),
         REGION_OWNER(Perm.REGION, "/region owner <region> <player>", "Set the owner of a region.", false, true),
         REGION_RENAME(Perm.REGION, "/region rename <region> <name>", "Rename a region.", false, true),
         REGION_RESET(Perm.REGION, "/region reset <region>", "Rest a region with new points.", false, true),
         REGION_SELECT(Perm.REGION, "/region select <region>", "Select a regions points.", false, true),
         REGION_SET(Perm.REGION, "/region set <name> [type]", "Set a new region.", false, true),
         REGION_DEL(Perm.REGION, "/region del <region>", "Delete a region.", false, true),
         REGION_TP(Perm.REGION, "/region tp <region>", "Teleport to a region.", false, true),
         REGION_VERT(Perm.REGION, "/region vert", "Move your points to select bedrock to build limit.", false, true),
         RULES(Perm.RULES, "/rules", "View the server rules.", false, false),
         SCUBA(Perm.SCUBA, "/scuba", "Use scuba gear to dive underwater.", false, false),
         SERVER(Perm.SERVER, "/help server", "Show more server commands.", true, false),
         SERVER_STOP(Perm.SERVER, "/server stop", "Stop the server.", false, true),
         SHOP(Perm.SHOP, "/help shop", "Show more shop commands.", true, false),
         SHOP_HELP(Perm.SHOP, "/shop help [page]", "Show these help pages.", false, true),
         SHOP_TP(Perm.SHOP, "/shop", "Teleport to your shop.", false, true),
         SHOP_TP_OTHER(Perm.SHOP, "/shop <player>", "Teleport to another players shop.", false, true),
         SHOP_BUY(Perm.SHOP, "/shop buy", "Buy the shop plot you are standing in.", false, true),
         SHOP_CLEAR(Perm.SHOP, "/shop clear", "Put your shop back up for sale.", false, true),
         SHOP_HIRE(Perm.SHOP, "/shop hire <player>", "Hire a player to work in your shop.", false, true),
         SHOP_FIRE(Perm.SHOP, "/shop fire <player>", "Fire one of your workers.", false, true),
         SHOP_ITEM(Perm.SHOP, "/shop item", "Check the in game name for an item.", false, true),
         SHOP_MAKE(Perm.SHOP_ADMIN, "/shop make", "Make a new shop plot.", false, true),
         SHOP_SELL(Perm.SHOP, "/shop sell <player> <price>", "Sell your shop to another player.", false, true),
         SHOP_SET(Perm.SHOP, "/shop set", "Set the teleport point for your shop.", false, true),
         SHOP_SIGN(Perm.SHOP, "/shop sign", "Check how to format shop signs.", false, true),
         SHOP_WORKERS(Perm.SHOP, "/shop workers", "See who your workers are.", false, true),
         SIT(Perm.SIT, "/sit", "Sit on a stair block or get up again.", false, false),
         SPAWN(Perm.SPAWN, "/spawn", "Teleport to spawn.", false, false),
         STACK(Perm.STACK, "/stack", "Stack the items in your inventory.", false, false),
         STATS(Perm.STATS, "/stats <player>", "View stats on a player.", false, false),
         VIP_BUY(Perm.VIP_BUY, "/vip", "Rent VIP rank.", false, false),
         WARP_LIST(Perm.WARP, "/warp", "Get a list of availbable warp points.", false, false),
         WARP(Perm.WARP, "/warp <warp>", "Teleport to a warp point.", false, false),
         WE(Perm.WE, "/help we", "Show more WE commands.", true, false),
         WHERE(Perm.WHERE, "/where", "Get details on your surroundings.", false, false),
         WHO(Perm.WHO, "/who", "Get a list of online players.", false, false),
         WHOIS(Perm.WHOIS, "/whois <player>", "Find out what a players MC name is.", false, false);
 
         private Perm perm;
         private String usage;
         private String description;
         private boolean extension;
         private boolean extended;
 
         private Usage(Perm perm, String usage, String description, boolean extended, boolean extension) {
             this.perm = perm;
             this.usage = usage;
             this.description = description;
             this.extension = extension;
             this.extended = extended;
         }
 
         public static Usage get(String name) {
             for(Usage use : values()) {
                 if(use.name().equalsIgnoreCase(name) || use.name().matches("[A-Z]*_" + name.toUpperCase())) {
                     return use;
                 }
             }
             return null;
         }
 
         public String cmd() {
             if(extension) {
                 return name().toLowerCase();
             } else {
                 return name().toLowerCase().split("_")[0];
             }
         }
     }
     /**
      * @inheritDocs
      */
     @Override
     public void playerExecute(SaveablePlayer player, String[] args) {
         if(maxArgsHelp(2)) {
             if(args.length == 0 || (args.length == 1 && args[0].matches("[0-9][0-9]*"))) {
                 TreeSet<Usage> usages = new TreeSet<Usage>();
                 for(Usage usage : Usage.values()) {
                     if(player.hasPermission(usage.perm) && !usage.extension) {
                         usages.add(usage);
                     }
                 }
                 int page = 1;
                 if(args.length == 1) {
                     page = Integer.parseInt(args[0]);
                 }
                 sendPage(page, player, usages, "Help");
             } else if(args.length == 1 || (args.length == 2 && args[1].matches("[0-9][0-9]*"))) {
                 Usage usage = Usage.get(args[0]);
                 if(usage != null) {
                     if(usage.extended) {
                         int page = 1;
                         if(args.length == 2) {
                             page = Integer.parseInt(args[1]);
                         }
                         TreeSet<Usage> extensions = new TreeSet<Usage>();
                         for(Usage extension : Usage.values()) {
                             if(extension.cmd().contains(usage.cmd() + "_") && player.hasPermission(extension.perm) && extension.extension) {
                                 extensions.add(extension);
                             }
                         }
                         sendPage(page, player, extensions, usage.cmd().replaceFirst("[a-z]", usage.cmd().substring(0, 1).toUpperCase()) + " Help");
                     } else {
                         player.sendMessage(Color.ITEM + usage.usage + Color.TEXT + " - " + usage.description);
                     }
                 } else {
                     player.sendMessage(Color.ERROR + "No command exists by that name.");
                 }
             }
         }
     }
 
     private void sendPage(int page, SaveablePlayer player, TreeSet<Usage> list, String title) {
         int pages = (list.size() + 8) / 9;
         if(pages == 0) {
             player.sendMessage(Color.MESSAGE + "There is no help available.");
         } else if(page > pages) {
             player.sendMessage(Message.NO_PAGE);
         } else {
             player.sendMessage(Color.MESSAGE + "--- " + title + " " + (pages > 1 ? "Page " + page + "/" + pages + " " : "") + "---");
             int posted = 0;
             int skipped = 1;
             for(Usage usage : list) {
                 if(skipped > (page - 1) * 9 && posted < 9) {
                     player.sendMessage(Color.ITEM + usage.usage + Color.TEXT + " - " + usage.description);
                     posted++;
                 } else {
                     skipped++;
                 }
             }
         }
     }
 }
