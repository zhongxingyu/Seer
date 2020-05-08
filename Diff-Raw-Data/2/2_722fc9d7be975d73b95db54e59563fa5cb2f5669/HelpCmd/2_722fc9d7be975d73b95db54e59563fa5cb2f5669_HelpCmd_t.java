 package com.undeadscythes.udsplugin.commands;
 
 import com.undeadscythes.udsplugin.*;
 import com.undeadscythes.udsplugin.Color;
 import com.undeadscythes.udsplugin.CommandWrapper;
 import com.undeadscythes.udsplugin.ConfigRef;
 import com.undeadscythes.udsplugin.Message;
 import com.undeadscythes.udsplugin.Perm;
 import com.undeadscythes.udsplugin.SaveablePlayer;
 import com.undeadscythes.udsplugin.UDSPlugin;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * Get help on certain commands.
  * @author UndeadScythes
  */
 public class HelpCmd extends CommandWrapper {
     private enum Usage {
         HELP(Perm.HELP, "/help [page or command]", "Show these help pages."),
         TICKET(Perm.TICKET, "/ticket <message>", "Submit a suggestion or a bug report."),
         ACCEPTRULES(Perm.ACCEPTRULES, "/acceptrules", "Accept the rules and get membership."),
         ADMIN(Perm.ADMIN, "/admin help", "Show more admin/mod/warden commands.", true),
         ADMIN_LOCKDOWN(Perm.LOCKDOWN, "/lockdown", "Toggle the server's lockdown mode.", ADMIN),
         ADMIN_LOCKDOWN_PASS(Perm.LOCKDOWN, "/lockdown <player>", "Issue a lockdown pass.", ADMIN),
         ADMIN_MOD_A(Perm.A, "/a", "Switch to the admin chat channel.", ADMIN),
         ADMIN_MOD_A_MSG(Perm.A, "/a <message>", "Send a message in the admin chat channel.", ADMIN),
         ADMIN_MOD_BROADCAST(Perm.BROADCAST, "/b <message>", "Send a server message.", ADMIN),
         ADMIN_MOD_BAN(Perm.BAN, "/ban <player>", "Ban a player from the server.", ADMIN),
         ADMIN_MOD_UNBAN(Perm.UNBAN, "/unban <player>", "Unban a player from the server.", ADMIN),
         ADMIN_MOD_BUTCHER(Perm.BUTCHER, "/butcher", "Kill hostile mobs around you.", ADMIN),
         ADMIN_MOD_BUTCHER_ALL(Perm.BUTCHER, "/butcher a", "Kill all mobs around you.", ADMIN),
         ADMIN_MOD_ENCHANT(Perm.ENCHANT, "/enchant <enchantment> [level]", "Enchant an item.", ADMIN),
         ADMIN_MOD_ENCHANT_LIST(Perm.ENCHANT, "/enchant", "Get a list of available enchantments.", ADMIN),
         ADMIN_MOD_GOD(Perm.GOD, "/god [player]", "Toggle a players god mode", ADMIN),
         ADMIN_MOD_HEAL(Perm.HEAL, "/heal [player]", "Heal a player.", ADMIN),
         ADMIN_MOD_INVSEE(Perm.INVSEE, "/invsee <player>", "Get a copy of a players inventory.", ADMIN),
         ADMIN_MOD_INVSEE_RESET(Perm.INVSEE, "/invsee", "Get your inventory back.", ADMIN),
         ADMIN_MOD_KICK(Perm.KICK, "/kick <player> [reason]", "Kick a player from the server.", ADMIN),
         ADMIN_MOD_NICK_OTHER(Perm.NICK_OTHER, "/nick <player> <nickname>", "Change a players nickname.", ADMIN),
         ADMIN_MOD_POWERTOOL(Perm.POWERTOOL, "/powertool <command>", "Set a powertool.", ADMIN),
         ADMIN_MOD_PROMOTE(Perm.PROMOTE, "/promote <player>", "Promote a player.", ADMIN),
         ADMIN_MOD_DEMOTE(Perm.DEMOTE, "/demote <player>", "Demote a player.", ADMIN),
         ADMIN_MOD_SIGNS(Perm.SIGNS, "/signs", "View a list of special signs you can make.", ADMIN),
         ADMIN_MOD_SPAWNER(Perm.SPAWNER, "/spawner <monster>", "Set the mob type of a spawner.", ADMIN),
         ADMIN_MOD_TGM(Perm.TGM, "/tgm [player]", "Toggle a players game mode.", ADMIN),
         ADMIN_MOD_TP(Perm.TP, "/tp <player>", "Teleport to another player.", ADMIN),
         ADMIN_MOD_TP_OTHER(Perm.TP, "/tp <player> <player>", "Teleport one player to another.", ADMIN),
         ADMIN_MOD_XP(Perm.XP, "/xp <player> <level>", "Give a player experience.", ADMIN),
         ADMIN_MOD_XP_RESET(Perm.XP, "/xp <player> reset", "Reset a players level.", ADMIN),
         ADMIN_WARDEN_DAY(Perm.DAY, "/day", "Set the server time to day.", ADMIN),
         ADMIN_WARDEN_NIGHT(Perm.NIGHT, "/night", "Set the server time to night.", ADMIN),
         ADMIN_WARDEN_JAIL(Perm.JAIL, "/jail <player> [time] [bail]", "Jail a player.", ADMIN),
         ADMIN_WARDEN_UNJAIL(Perm.UNJAIL, "/unjail <player>", "Release a player from jail.", ADMIN),
         ADMIN_WARDEN_SETWARP(Perm.SETWARP, "/setwarp <name> [rank] [price]", "Set a warp point.", ADMIN),
         ADMIN_WARDEN_DELWARP(Perm.DELWARP, "/delwarp <warp>", "Delete a warp point.", ADMIN),
         ADMIN_WARDEN_SUN(Perm.SUN, "/sun", "Set the server weather to sunny.", ADMIN),
         ADMIN_WARDEN_RAIN(Perm.RAIN, "/rain [duration]", "Set the server weather to rainy.", ADMIN),
         ADMIN_WARDEN_STORM(Perm.STORM, "/storm [duration]", "Set the server weather to stormy.", ADMIN),
         MOD(Perm.MOD_HELP, "/mod help", "Show more mod/warden commands.", true),
         MOD_A(Perm.A, "/a", "Switch to the admin chat channel.", MOD),
         MOD_A_MSG(Perm.A, "/a <message>", "Send a message in the admin chat channel.", MOD),
         MOD_BROADCAST(Perm.BROADCAST, "/b <message>", "Send a server message.", MOD),
         MOD_BAN(Perm.BAN, "/ban <player>", "Ban a player from the server.", MOD),
         MOD_UNBAN(Perm.UNBAN, "/unban <player>", "Unban a player from the server.", MOD),
         MOD_BUTCHER(Perm.BUTCHER, "/butcher", "Kill hostile mobs around you.", MOD),
         MOD_BUTCHER_ALL(Perm.BUTCHER, "/butcher a", "Kill all mobs around you.", MOD),
         MOD_ENCHANT(Perm.ENCHANT, "/enchant <enchantment> [level]", "Enchant an item.", MOD),
         MOD_ENCHANT_LIST(Perm.ENCHANT, "/enchant", "Get a list of available enchantments.", MOD),
         MOD_GOD(Perm.GOD, "/god [player]", "Toggle a players god mode", MOD),
         MOD_HEAL(Perm.HEAL, "/heal [player]", "Heal a player.", MOD),
         MOD_INVSEE(Perm.INVSEE, "/invsee <player>", "Get a copy of a players inventory.", MOD),
         MOD_INVSEE_RESET(Perm.INVSEE, "/invsee", "Get your inventory back.", MOD),
         MOD_KICK(Perm.KICK, "/kick <player> [reason]", "Kick a player from the server.", MOD),
         MOD_NICK_OTHER(Perm.NICK_OTHER, "/nick <player> <nickname>", "Change a players nickname.", MOD),
         MOD_POWERTOOL(Perm.POWERTOOL, "/powertool <command>", "Set a powertool.", MOD),
         MOD_PROMOTE(Perm.PROMOTE, "/promote <player>", "Promote a player.", MOD),
         MOD_DEMOTE(Perm.DEMOTE, "/demote <player>", "Demote a player.", MOD),
         MOD_SIGNS(Perm.SIGNS, "/signs", "View a list of special signs you can make.", MOD),
         MOD_SPAWNER(Perm.SPAWNER, "/spawner <monster>", "Set the mob type of a spawner.", MOD),
         MOD_TGM(Perm.TGM, "/tgm [player]", "Toggle a players game mode.", MOD),
         MOD_TP(Perm.TP, "/tp <player>", "Teleport to another player.", MOD),
         MOD_TP_OTHER(Perm.TP, "/tp <player> <player>", "Teleport one player to another.", MOD),
         MOD_XP(Perm.XP, "/xp <player> <level>", "Give a player experience.", MOD),
         MOD_XP_RESET(Perm.XP, "/xp <player> reset", "Reset a players level.", MOD),
         MOD_WARDEN_DAY(Perm.DAY, "/day", "Set the server time to day.", MOD),
         MOD_WARDEN_NIGHT(Perm.NIGHT, "/night", "Set the server time to night.", MOD),
         MOD_WARDEN_JAIL(Perm.JAIL, "/jail <player> [time] [bail]", "Jail a player.", MOD),
         MOD_WARDEN_UNJAIL(Perm.UNJAIL, "/unjail <player>", "Release a player from jail.", MOD),
         MOD_WARDEN_SETWARP(Perm.SETWARP, "/setwarp <name> [rank] [price]", "Set a warp point.", MOD),
         MOD_WARDEN_DELWARP(Perm.DELWARP, "/delwarp <warp>", "Delete a warp point.", MOD),
         MOD_WARDEN_SUN(Perm.SUN, "/sun", "Set the server weather to sunny.", MOD),
         MOD_WARDEN_RAIN(Perm.RAIN, "/rain [duration]", "Set the server weather to rainy.", MOD),
         MOD_WARDEN_STORM(Perm.STORM, "/storm [duration]", "Set the server weather to stormy.", MOD),
         WARDEN(Perm.WARDEN_HELP, "/warden help", "Show more warden commands.", true),
         WARDEN_DAY(Perm.DAY, "/day", "Set the server time to day.", WARDEN),
         WARDEN_NIGHT(Perm.NIGHT, "/night", "Set the server time to night.", WARDEN),
         WARDEN_JAIL(Perm.JAIL, "/jail <player> [time] [bail]", "Jail a player.", WARDEN),
         WARDEN_UNJAIL(Perm.UNJAIL, "/unjail <player>", "Release a player from jail.", WARDEN),
         WARDEN_SETWARP(Perm.SETWARP, "/setwarp <name> [rank] [price]", "Set a warp point.", WARDEN),
         WARDEN_DELWARP(Perm.DELWARP, "/delwarp <warp>", "Delete a warp point.", WARDEN),
         WARDEN_SUN(Perm.SUN, "/sun", "Set the server weather to sunny.", WARDEN),
         WARDEN_RAIN(Perm.RAIN, "/rain [duration]", "Set the server weather to rainy.", WARDEN),
         WARDEN_STORM(Perm.STORM, "/storm [duration]", "Set the server weather to stormy.", WARDEN),
         VIP(Perm.VIP_HELP, "/vip help", "Show more vip commands.", true),
         VIP_I(Perm.I, "/i <item>[:data] [amount]", "Spawn an item.", VIP),
         VIP_SPAWNS(Perm.VIP, "/vip", "Check the number of daily spawns you have left.", VIP),
         VIP_ITEMS(Perm.VIP, "/vip spawns", "Check which items are whitelisted.", VIP),
         VIP_WE(Perm.WE_VIP, "/we help", "Check which WE commands you can use.", VIP),
         AFK(Perm.AFK, "/afk", "Set yourself as afk."),
         BACK(Perm.BACK, "/back", "Return to your position before teleporting."),
         BOUNTY(Perm.BOUNTY, "/bounty <player> <amount>", "Place a bounty on a player."),
         BOUNTY_LIST(Perm.BOUNTY, "/bounty", "Check the bounties list."),
         CALL(Perm.CALL, "/call <player>", "Request a teleport to another player."),
         CHALLENGE(Perm.CHALLENGE, "/challenge <player> <wager>", "Challenge a player to a duel."),
         CHECK(Perm.CHECK, "/check", "Teleport to your saved checkpoint."),
         CHAT(Perm.CHAT_HELP, "/chat help", "Show more chat commands.", true),
         CHAT_IGNORE(Perm.IGNORE, "/ignore <player>", "Ignore or unignore a player in chat.", CHAT),
         CHAT_PRIVATE(Perm.PRIVATE, "/private <chat room>", "Enter or exit private chat rooms.", CHAT),
         CHAT_P(Perm.P, "/p", "Switch to the chat room channel.", CHAT),
         CHAT_P_MSG(Perm.P, "/p <message>", "Send a message to the chat room.", CHAT),
         CHAT_TELL(Perm.TELL, "/tell <player> <message>", "Send a private message to a player.", CHAT),
         CHAT_R(Perm.R, "/r <message>", "Reply to a private message.", CHAT),
         CI(Perm.CI, "/ci", "Clear all the items from your inventory. (Not reversible)"),
         CITY(Perm.CITY, "/city help", "Show more city commands.", true),
         CITY_NEW(Perm.CITY, "/city new", "Found a new city where you are stood.", CITY),
         CITY_CLEAR(Perm.CITY, "/city clear <city>", "Remove the city protection.", CITY),
         CITY_INVITE(Perm.CITY, "/city invite <city> <player>", "Invite a player to a city.", CITY),
         CITY_MAYOR(Perm.CITY, "/city mayor <city> <player>", "Change the mayor of the city.", CITY),
         CITY_SET(Perm.CITY, "/city set", "Set the warp point for your city.", CITY),
         CITY_LIST(Perm.CITY, "/city list", "Get a list of cities on the server.", CITY),
         CITY_WARP(Perm.CITY, "/city warp <city>", "Teleport to a city.", CITY),
         CITY_LEAVE(Perm.CITY, "/city leave <city>", "Leave a city.", CITY),
         CITY_BANISH(Perm.CITY, "/city banish <city> <player>", "Banish a player from a city.", CITY),
         CLAN(Perm.CLAN, "/clan help", "Show more clan commands.", true),
         CLAN_HELP(Perm.CLAN, "/clan help [page]", "Show these clan help pages.", CLAN),
         CLAN_C(Perm.C, "/c", "Switch to the clan chat channel.", CLAN),
         CLAN_C_MSG(Perm.C, "/c <message>", "Send a message in the clan chat channel.", CLAN),
         CLAN_BASE(Perm.CLAN, "/clan base", "Teleport to your clan base.", CLAN),
         CLAN_BASE_MAKE(Perm.CLAN, "/clan base make", "Make a base for your clan.", CLAN),
         CLAN_BASE_CLEAR(Perm.CLAN, "/clan base clear", "Clear your clan base.", CLAN),
         CLAN_BASE_SET(Perm.CLAN, "/clan base set", "Set your bases warp point.", CLAN),
         CLAN_DISBAND(Perm.CLAN, "/clan disband", "Disband your clan.", CLAN),
         CLAN_INVITE(Perm.CLAN, "/clan invite <player>", "Invite a player to your clan.", CLAN),
         CLAN_KICK(Perm.CLAN, "/clan kick <player>", "Kick a player from your clan.", CLAN),
         CLAN_LEAVE(Perm.CLAN, "/clan leave", "Leave your clan.", CLAN),
         CLAN_LIST(Perm.CLAN, "/clan list", "View a list of clans.", CLAN),
         CLAN_MEMBERS(Perm.CLAN, "/clan members", "View a list of your fellow clan members.", CLAN),
         CLAN_MEMBERS_OTHER(Perm.CLAN, "/clan members <clan>", "View a list of clan members.", CLAN),
         CLAN_NEW(Perm.CLAN, "/clan new <name>", "Make a new clan.", CLAN),
         CLAN_OWNER(Perm.CLAN, "/clan owner <player>", "Change the owner of your clan.", CLAN),
         CLAN_RENAME(Perm.CLAN, "/clan rename <name>", "Rename your clan.", CLAN),
         CLAN_STATS(Perm.CLAN, "/clan stats", "View your clans stats.", CLAN),
         CLAN_STATS_OTHER(Perm.CLAN, "/clan stats <clan>", "View another clans stats.", CLAN),
         FACE(Perm.FACE, "/face <direction>", "Turn you character to face a direction."),
         FACE_GET(Perm.FACE, "/face", "Find out which way you are facing."),
         GIFT(Perm.GIFT, "/gift <player> [message]", "Send a player a gift."),
         HOME(Perm.HOME, "/home help", "Show more home commands.", true),
         HOME_HELP(Perm.HOME, "/home help [page]", "Show these home help pages.", HOME),
         HOME_MAKE(Perm.HOME, "/home make", "Set a new home protection.", HOME),
         HOME_CLEAR(Perm.HOME, "/home clear", "Clear your home protection.", HOME),
         HOME_TP(Perm.HOME, "/home", "Teleport to your home.", HOME),
         HOME_TP_OTHER(Perm.HOME, "/home <player>", "Teleport to a room mates home.", HOME),
         HOME_ADD(Perm.HOME, "/home add <player>", "Add a player as a room mate.", HOME),
         HOME_KICK(Perm.HOME, "/home kick <player>", "Kick a player from your room mates.", HOME),
         HOME_BOOT(Perm.HOME, "/home boot <player>", "Forcibly boot a player from your home.", HOME),
         HOME_EXPAND(Perm.HOME, "/home expand <direction>", "Expand your home.", HOME),
         HOME_LOCK(Perm.HOME, "/home lock", "Lock your house.", HOME),
         HOME_UNLOCK(Perm.HOME, "/home unlock", "Unlock your house.", HOME),
         HOME_POWER(Perm.HOME, "/home power", "Toggle whether players can use redstone.", HOME),
         HOME_ROOMIES(Perm.HOME, "/home roomies", "Check your current room mates.", HOME),
         HOME_SELL(Perm.HOME, "/home sell <player> <price>", "Sell your home to a player.", HOME),
         HOME_SET(Perm.HOME, "/home set", "Set your home warp point.", HOME),
         KIT(Perm.KIT, "/kit <name>", "Purchase a kit."),
         KIT_LIST(Perm.KIT, "/kit", "Check the available kits."),
         MAP(Perm.MAP, "/map", "Get a copy of the spawn map."),
         ME(Perm.ME, "/me <action>", "Perform a chat action."),
         MONEY(Perm.MONEY, "/money help", "Show more money commands.", true),
         MONEY_(Perm.MONEY, "/money", "Check how much money you have.", MONEY),
         MONEY_PRICES(Perm.MONEY, "/money prices", "Check the server prices.", MONEY),
         MONEY_PAY(Perm.MONEY, "/money pay <player> <amount>", "Pay a player some money.", MONEY),
         MONEY_RANK(Perm.MONEY, "/money rank", "Check the money rankings.", MONEY),
         MONEY_GRANT(Perm.MONEY_ADMIN, "/money grant <player> <amount>", "Give a player some money.", MONEY),
         MONEY_SET(Perm.MONEY_ADMIN, "/money set <player> <amount>", "Set a players account.", MONEY),
         NICK(Perm.NICK, "/nick <nickname>", "Change your nickname."),
         PAYBAIL(Perm.PAYBAIL, "/paybail", "Pay your bail to get out of jail."),
         PET_GIVE(Perm.PET, "/pet give <player>", "Give your pet to a player."),
         PET_SELL(Perm.PET, "/pet sell <player> <price>", "Sell your pet to a player."),
         PLOT(Perm.PLOT, "/plot help", "Show more plot commands.", true),
         PLOT_CLAIM(Perm.PLOT, "/plot claim", "Claim the area you are standing in.", PLOT),
         PLOT_NAME(Perm.PLOT, "/plot name [plot] <name>", "Rename a plot.", PLOT),
         PLOT_TP(Perm.PLOT, "/plot tp <plot>", "Teleport to a plot.", PLOT),
         PORTAL(Perm.PORTAL, "/portal help", "Show more portal commands.", true),
         PORTAL_DEST(Perm.PORTAL, "/portal dest <name> <warp>", "Change a portal warp.", PORTAL),
         PORTAL_EXIT(Perm.PORTAL, "/portal exit <name> <direction>", "Set portal exit direction.", PORTAL),
         PORTAL_LIST(Perm.PORTAL, "/portal list", "List all portals.", PORTAL),
         PORTAL_P2P(Perm.PORTAL, "/portal p2p <from> <to>", "Link two portals.", PORTAL),
         PORTAL_SET(Perm.PORTAL, "/portal set <name> [warp]", "Set a new portal.", PORTAL),
         PORTAL_REMOVE(Perm.PORTAL, "/portal remove <name>", "Remove a portal.", PORTAL),
         REGION(Perm.REGION, "/region help", "Show more region commands.", true),
         REGION_HELP(Perm.REGION, "/region help [page]", "Show these region help pages.", REGION),
         REGION_ADDMEMBER(Perm.REGION, "/region addmember <region> <player>", "Add a member.", REGION),
         REGION_DELMEMBER(Perm.REGION, "/region delmember <region> <player>", "Remove a member.", REGION),
         REGION_EXPAND(Perm.REGION, "/region expand <region> <distance> <direction>", "Grow a region.", REGION),
         REGION_CONTRACT(Perm.REGION, "/region contract <region> <distance> <direction>", "Shrink a region.", REGION),
         REGION_FLAG(Perm.REGION, "/region flag <region> <flag>", "Toggle a region flag.", REGION),
         REGION_INFO(Perm.REGION, "/region info <region>", "Get detailed info on a region.", REGION),
         REGION_LIST(Perm.REGION, "/region list <type>", "List regions of a certain type.", REGION),
         REGION_OWNER(Perm.REGION, "/region owner <region> <player>", "Set the owner of a region.", REGION),
         REGION_RENAME(Perm.REGION, "/region rename <region> <name>", "Rename a region.", REGION),
         REGION_RESET(Perm.REGION, "/region reset <region>", "Rest a region with new points.", REGION),
         REGION_SELECT(Perm.REGION, "/region select <region>", "Select a regions points.", REGION),
         REGION_SET(Perm.REGION, "/region set <name> [type]", "Set a new region.", REGION),
         REGION_DEL(Perm.REGION, "/region del <region>", "Delete a region.", REGION),
         REGION_TP(Perm.REGION, "/region tp <region>", "Teleport to a region.", REGION),
         REGION_VERT(Perm.REGION, "/region vert", "Move your points to select bedrock to build limit.", REGION),
         RULES(Perm.RULES, "/rules", "View the server rules."),
         SCUBA(Perm.SCUBA, "/scuba", "Use scuba gear to dive underwater."),
         SERVER(Perm.SERVER, "/server help", "Show more server commands.", true),
         SERVER_INFO(Perm.SERVER, "/server info", "View some basic info.", SERVER),
         SERVER_RELOAD(Perm.SERVER, "/server reload", "Reload changes from the config.yml file.", SERVER),
         SERVER_SETSPAWN(Perm.SETSPAWN, "/server setspawn", "Set the main server spawn point.", SERVER),
         SERVER_STOP(Perm.SERVER, "/server stop", "Stop the server.", SERVER),
         SHOP(Perm.SHOP, "/shop help", "Show more shop commands.", true),
         SHOP_HELP(Perm.SHOP, "/shop help [page]", "Show these shop help pages.", SHOP),
         SHOP_TP(Perm.SHOP, "/shop", "Teleport to your shop.", SHOP),
         SHOP_TP_OTHER(Perm.SHOP, "/shop <player>", "Teleport to another players shop.", SHOP),
         SHOP_BUY(Perm.SHOP, "/shop buy", "Buy the shop plot you are standing in.", SHOP),
         SHOP_CLEAR(Perm.SHOP, "/shop clear", "Put your shop back up for sale.", SHOP),
         SHOP_CHANGES(Perm.SHOP, "/shop changes", "Check out the lates changes to shops.", SHOP),
         SHOP_HIRE(Perm.SHOP, "/shop hire <player>", "Hire a player to work in your shop.", SHOP),
         SHOP_FIRE(Perm.SHOP, "/shop fire <player>", "Fire one of your workers.", SHOP),
         SHOP_ITEM(Perm.SHOP, "/shop item", "Check the in game name for an item.", SHOP),
         SHOP_MAKE(Perm.SHOP_ADMIN, "/shop make", "Make a new shop plot.", SHOP),
         SHOP_SELL(Perm.SHOP, "/shop sell <player> <price>", "Sell your shop to another player.", SHOP),
         SHOP_SET(Perm.SHOP, "/shop set", "Set the teleport point for your shop.", SHOP),
         SHOP_SIGN(Perm.SHOP, "/shop sign", "Check how to format shop signs.", SHOP),
         SHOP_WORKERS(Perm.SHOP, "/shop workers", "See who your workers are.", SHOP),
         SIT(Perm.SIT, "/sit", "Sit on a stair block or get up again."),
         SPAWN(Perm.SPAWN, "/spawn", "Teleport to spawn."),
         STACK(Perm.STACK, "/stack", "Stack the items in your inventory."),
         STATS(Perm.STATS, "/stats [player]", "View stats on a player."),
         VANISH(Perm.VANISH, "/vanish help", "Show more vanish commands.", true),
         VANISH_SELF(Perm.VANISH, "/vanish", "Make your self vanish or reappear.", VANISH),
         VANISH_LIST(Perm.VANISH, "/vanish list", "View a list of vanished players.", VANISH),
         VIP_BUY(Perm.VIP_BUY, "/vip", "Rent VIP rank."),
         WARP_LIST(Perm.WARP, "/warp", "Get a list of availbable warp points."),
         WARP(Perm.WARP, "/warp <warp>", "Teleport to a warp point."),
         WE(Perm.WE, "/we help", "Show more WE commands.", true),
         WE_EXT(Perm.WE_EXT, "/we ext [radius]", "Extinguish fires.", WE),
         WE_DRAIN(Perm.WE_DRAIN, "/we drain [radius]", "Remove lava and water.", WE),
         WE_UNDO(Perm.WE_UNDO, "/we undo", "Undo your last WE command.", WE),
         WE_COPY(Perm.WE_COPY, "/we copy", "Copy a selection.", WE),
         WE_PASTE(Perm.WE_PASTE, "/we paste", "Paste from clipboard.", WE),
         WE_SET(Perm.WE_SET, "/we set <block>", "Fill an area with blocks.", WE),
         WE_REPLACE(Perm.WE_REPLACE, "/we <old> <new>", "Replace blocks in an area.", WE),
         WE_MOVE(Perm.WE_MOVE, "/we move <direction> <distance>", "Move an area of blocks.", WE),
         WE_REGEN(Perm.WE_REGEN, "/we regen", "Regenerate a chunk from seed.", WE),
         WHERE(Perm.WHERE, "/where", "Get details on your surroundings."),
         WHO(Perm.WHO, "/who", "Get a list of online players."),
         WHOIS(Perm.WHOIS, "/whois <player>", "Find out what a players MC name is."),
         WORLD(Perm.WORLD, "/world help", "Show more world commands.", true),
         WORLD_CREATE(Perm.WORLD, "/world create <name>", "Create a new world.", WORLD),
         WORLD_DELETE(Perm.WORLD, "/world delete <world>", "Remove a world from disk.", WORLD),
         WORLD_FLAG(Perm.WORLD, "/world flag [world] <flag>", "Toggle a world flag.", WORLD),
         WORLD_FORGET(Perm.WORLD, "/world forget <world>", "Unload a world from memory.", WORLD),
         WORLD_INFO(Perm.WORLD, "/world info [world]", "Get info on a world.", WORLD),
         WORLD_LIST(Perm.WORLD, "/world list", "List the available worlds.", WORLD),
         WORLD_SETSPAWN(Perm.WORLD, "/world setspawn", "Set the spawn of the current world.", WORLD),
         WORLD_TP(Perm.WORLD, "/world tp <world>", "Teleport to a world.", WORLD);
 
         private Perm perm;
         private String usage;
         private String description;
         private boolean extended;
         private Usage extension;
 
         private Usage(final Perm perm, final String usage, final String description) {
             this(perm, usage, description, false, null);
         }
         
         private Usage(final Perm perm, final String usage, final String description, final boolean extended) {
             this(perm, usage, description, extended, null);
         }
         private Usage(final Perm perm, final String usage, final String description, final Usage extension) {
             this(perm, usage, description, false, extension);
         }
         
         private Usage(final Perm perm, final String usage, final String description, final boolean extended, final Usage extension) {
             this.perm = perm;
             this.usage = usage;
             this.description = description;
             this.extension = extension;
             this.extended = extended;
         }
         
         public static Usage getByName(final String name) {
             for(Usage use : values()) {
                 if(use.name().equalsIgnoreCase(name)) {
                     return use;
                 }
             }
             for(Usage use : values()) {
                 if(use.name().matches("[A-Z]*_" + name.toUpperCase())) {
                     return use;
                 }
             }
             return null;
         }
 
         public String cmd() {
             if(extension != null) {
                 return name().toLowerCase();
             } else {
                 return name().toLowerCase().split("_")[0];
             }
         }
 
         public Perm getPerm() {
             return perm;
         }
 
         public String getUsage() {
             return usage;
         }
 
         public String getDescription() {
             switch(this) {
                 case ACCEPTRULES:
                     return description + " (" + Color.ITEM + UDSPlugin.getConfigInt(ConfigRef.BUILD_COST) + " credits" + Color.TEXT + ")";
                 case CITY_NEW:
                     return description + " (" + Color.ITEM + UDSPlugin.getConfigInt(ConfigRef.CITY_COST) + " credits" + Color.TEXT + ")";
                 case MAP:
                     return description + " (" + Color.ITEM + UDSPlugin.getConfigInt(ConfigRef.MAP_COST) + " credits" + Color.TEXT + ")";
                 case HOME_MAKE:
                     return description + " (" + Color.ITEM + UDSPlugin.getConfigInt(ConfigRef.HOME_COST) + " credits" + Color.TEXT + ")";
                 case SHOP_BUY:
                     return description + " (" + Color.ITEM + UDSPlugin.getConfigInt(ConfigRef.SHOP_COST) + " credits" + Color.TEXT + ")";
                 case VIP:
                     return description + " (" + Color.ITEM + UDSPlugin.getConfigInt(ConfigRef.VIP_COST) + " credits" + Color.TEXT + ")";
                 case CLAN_NEW:
                     return description + " (" + Color.ITEM + UDSPlugin.getConfigInt(ConfigRef.CLAN_COST) + " credits" + Color.TEXT + ")";
                 case CLAN_BASE_SET:
                     return description + " (" + Color.ITEM + UDSPlugin.getConfigInt(ConfigRef.BASE_COST) + " credits" + Color.TEXT + ")";
                 case HOME_EXPAND:
                     return description + " (" + Color.ITEM + UDSPlugin.getConfigInt(ConfigRef.EXPAND_COST) + " credits" + Color.TEXT + ")";
                 default:
                     return description;
             }
         }
 
         public boolean isExtension() {
             return extension != null;
         }
         
         public boolean isExtension(final Usage usage) {
             return extension == usage;
         }
 
         public boolean isExtended() {
             return extended;
         }
     }
 
     @Override
     public final void playerExecute() {
         if(maxArgsHelp(2)) {
             if(args.length == 0 || (args.length == 1 && args[0].matches("[0-9][0-9]*"))) {
                 sendHelpFiles();
             } else if(args.length == 1 || (args.length == 2 && args[1].matches("[0-9][0-9]*"))) {
                 final Usage usage = Usage.getByName(args[0]);
                 if(usage == null) {
                     player.sendMessage(Color.ERROR + "No command exists by that name.");
                 } else {
                     if(usage.isExtended()) {
                         sendCommandHelp(usage);
                     } else {
                         player.sendMessage(Color.ITEM + usage.getUsage() + Color.TEXT + " - " + usage.getDescription());
                     }
                 }
             }
         }
     }
 
     private void sendHelpFiles() {
         final Set<Usage> usages = new TreeSet<Usage>();
         for(Usage usage : Usage.values()) {
            if(player.hasPermission(usage.getPerm()) && !usage.isExtension() && (usage.getPerm().getMode() == null || UDSPlugin.getWorldMode(player.getWorld()).equals(usage.getPerm().getMode()))) {
                 usages.add(usage);
             }
         }
         if(args.length == 1) {
             sendPage(Integer.parseInt(args[0]), player, usages, "Help");
         } else {
             sendPage(1, player, usages, "Help");
         }
     }
 
     private void sendCommandHelp(final Usage usage) {
         final Set<Usage> extensions = new TreeSet<Usage>();
         for(Usage extension : Usage.values()) {
             if(player.hasPermission(extension.getPerm()) && extension.isExtension(usage)) {
                 extensions.add(extension);
             }
         }
         if(args.length == 2) {
             sendPage(Integer.parseInt(args[1]), player, extensions, usage.cmd().replaceFirst("[a-z]", usage.cmd().substring(0, 1).toUpperCase()) + " Help");
         } else {
             sendPage(1, player, extensions, usage.cmd().replaceFirst("[a-z]", usage.cmd().substring(0, 1).toUpperCase()) + " Help");
         }
     }
 
     private void sendPage(final int page, final SaveablePlayer player, final Set<Usage> list, final String title) {
         final int pages = (list.size() + 8) / 9;
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
                     player.sendMessage(Color.ITEM + usage.getUsage() + Color.TEXT + " - " + usage.getDescription());
                     posted++;
                 } else {
                     skipped++;
                 }
             }
         }
     }
 }
