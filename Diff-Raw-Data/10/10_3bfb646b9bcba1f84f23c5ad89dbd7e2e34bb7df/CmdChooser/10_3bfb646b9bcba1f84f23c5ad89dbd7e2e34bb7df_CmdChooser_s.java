 package de.zeltclan.tare.zeltcmds;
 
 import org.bukkit.permissions.PermissionDefault;
 
 import de.zeltclan.tare.zeltcmds.cmds.CmdPlayerInfo;
 import de.zeltclan.tare.zeltcmds.cmds.CmdLocation;
 import de.zeltclan.tare.zeltcmds.cmds.CmdMode;
 import de.zeltclan.tare.zeltcmds.cmds.CmdPlayer;
 import de.zeltclan.tare.zeltcmds.cmds.CmdServerInfo;
 import de.zeltclan.tare.zeltcmds.cmds.CmdSpawn;
 import de.zeltclan.tare.zeltcmds.cmds.CmdWorldWeather;
 import de.zeltclan.tare.zeltcmds.enums.*;
 
 class CmdChooser {
 	
 	private CmdChooser() {
 		
 	}
 	
 	static PermissionDefault getDefaultPermission(String p_perm) {
 		PermissionDefault result = PermissionDefault.getByName(p_perm);
 		return (result != null ? result : PermissionDefault.FALSE);
 	}
 	
 	static Category getCategory(String p_category) {
 		if (p_category.equalsIgnoreCase("mode")) {
 			return Category.MODE;
 		} else if (p_category.equalsIgnoreCase("player")) {
 			return Category.PLAYER;
 		} else if (p_category.equalsIgnoreCase("teleport") || p_category.equalsIgnoreCase("port")) {
 			return Category.PORT;
 		} else if (p_category.equalsIgnoreCase("serverinfo")) {
 			return Category.SERVERINFO;
 		} else if (p_category.equalsIgnoreCase("servertime") || p_category.equalsIgnoreCase("stime")) {
 			return Category.SERVERTIME;
 		} else if (p_category.equalsIgnoreCase("serverweather") || p_category.equalsIgnoreCase("sweather")) {
 			return Category.SERVERWEATHER;
 		} else if (p_category.equalsIgnoreCase("set")) {
 			return Category.SET;
 		} else if (p_category.equalsIgnoreCase("spawn")) {
 			return Category.SPAWN;
 		} else if (p_category.equalsIgnoreCase("worldtime") || p_category.equalsIgnoreCase("wtime") || p_category.equalsIgnoreCase("time")) {
 			return Category.WORLDTIME;
 		} else if (p_category.equalsIgnoreCase("worldweather") || p_category.equalsIgnoreCase("wweather") || p_category.equalsIgnoreCase("weather")) {
 			return Category.WORLDWEATHER;
 		}
 		return Category.NOCMD;
 	}
 	
 	static Type getType(Category p_category, String p_type) {
 		switch (p_category) {
 		case MODE:
 			if (p_type.equalsIgnoreCase("adventure") || p_type.equalsIgnoreCase("a")) {
 				return CmdMode.Types.ADVENTURE;
 			} else if (p_type.equalsIgnoreCase("adventure_creative") || p_type.equalsIgnoreCase("ac")) {
 				return CmdMode.Types.ADVENTURE_CREATIVE;
 			} else if (p_type.equalsIgnoreCase("adventure_survival") || p_type.equalsIgnoreCase("as")) {
 				return CmdMode.Types.ADVENTURE_SURVIVAL;
 			} else if (p_type.equalsIgnoreCase("adventure_creative_survival") || p_type.equalsIgnoreCase("acs")) {
 				return CmdMode.Types.ADVENTURE_CREATIVE_SURVIVAL;
 			} else if (p_type.equalsIgnoreCase("adventure_survival_creative") || p_type.equalsIgnoreCase("asc")) {
 				return CmdMode.Types.ADVENTURE_SURVIVAL_CREATIVE;
 			} else if (p_type.equalsIgnoreCase("creative") || p_type.equalsIgnoreCase("c")) {
 				return CmdMode.Types.CREATIVE;
 			} else if (p_type.equalsIgnoreCase("creative_adventure") || p_type.equalsIgnoreCase("ca")) {
 				return CmdMode.Types.CREATIVE_ADVENTURE;
 			} else if (p_type.equalsIgnoreCase("creative_SURVIVAL") || p_type.equalsIgnoreCase("cs")) {
 				return CmdMode.Types.CREATIVE_SURVIVAL;
 			} else if (p_type.equalsIgnoreCase("creative_adventure_survival") || p_type.equalsIgnoreCase("cas")) {
 				return CmdMode.Types.CREATIVE_ADVENTURE_SURVIVAL;
 			} else if (p_type.equalsIgnoreCase("creative_survival_adventure") || p_type.equalsIgnoreCase("csa")) {
 				return CmdMode.Types.CREATIVE_SURVIVAL_ADVENTURE;
 			} else if (p_type.equalsIgnoreCase("survival") || p_type.equalsIgnoreCase("s")) {
 				return CmdMode.Types.SURVIVAL;
 			} else if (p_type.equalsIgnoreCase("survival_adventure") || p_type.equalsIgnoreCase("sa")) {
 				return CmdMode.Types.SURVIVAL_ADVENTURE;
 			} else if (p_type.equalsIgnoreCase("survival_creative") || p_type.equalsIgnoreCase("sc")) {
 				return CmdMode.Types.SURVIVAL_CREATIVE;
 			} else if (p_type.equalsIgnoreCase("survival_adventure_creative") || p_type.equalsIgnoreCase("sac")) {
 				return CmdMode.Types.SURVIVAL_ADVENTURE_CREATIVE;
 			} else if (p_type.equalsIgnoreCase("survival_creative_adventure") || p_type.equalsIgnoreCase("sca")) {
 				return CmdMode.Types.SURVIVAL_CREATIVE_ADVENTURE;
 			}
 			break;
 		case PLAYER:
 			if (p_type.equalsIgnoreCase("clear")) {
 				return CmdPlayer.Types.CLEAR;
 			} else if (p_type.equalsIgnoreCase("direction") || p_type.equalsIgnoreCase("dir")) {
 				return CmdPlayerInfo.Types.DIRECTION;
 			} else if (p_type.equalsIgnoreCase("feed")) {
 				return CmdPlayer.Types.FEED;
 			} else if (p_type.equalsIgnoreCase("heal")) {
 				return CmdPlayer.Types.HEAL;
 			} else if (p_type.equalsIgnoreCase("info")) {
 				return CmdPlayerInfo.Types.INFO;
 			} else if (p_type.equalsIgnoreCase("kill")) {
 				return CmdPlayer.Types.KILL;
 			} else if (p_type.equalsIgnoreCase("position") || p_type.equalsIgnoreCase("pos")) {
 				return CmdPlayerInfo.Types.POSITION;
 			} else if (p_type.equalsIgnoreCase("seen")) {
 				return CmdPlayerInfo.Types.SEEN;
 			} else if (p_type.equalsIgnoreCase("time")) {
 				return CmdPlayerInfo.Types.TIME;
 			}
 			break;
 		case PORT:
 			if (p_type.equalsIgnoreCase("a2m")) {
 				return Port.A2M;
 			} else if (p_type.equalsIgnoreCase("a2p")) {
 				return Port.A2P;
 			} else if (p_type.equalsIgnoreCase("a2w")) {
 				return Port.A2W;
 			} else if (p_type.equalsIgnoreCase("m2b")) {
 				return Port.M2B;
 			} else if (p_type.equalsIgnoreCase("m2c")) {
 				return Port.M2C;
 			} else if (p_type.equalsIgnoreCase("m2e")) {
 				return Port.M2E;
 			} else if (p_type.equalsIgnoreCase("m2h")) {
 				return Port.M2H;
 			} else if (p_type.equalsIgnoreCase("m2p")) {
 				return Port.M2P;
 			} else if (p_type.equalsIgnoreCase("m2s")) {
 				return Port.M2S;
 			} else if (p_type.equalsIgnoreCase("m2w")) {
 				return Port.M2W;
 			} else if (p_type.equalsIgnoreCase("m2wc")) {
 				return Port.M2WC;
 			} else if (p_type.equalsIgnoreCase("m2we")) {
 				return Port.M2WE;
 			} else if (p_type.equalsIgnoreCase("p2b")) {
 				return Port.P2B;
 			} else if (p_type.equalsIgnoreCase("p2c")) {
 				return Port.P2C;
 			} else if (p_type.equalsIgnoreCase("p2e")) {
 				return Port.P2E;
 			} else if (p_type.equalsIgnoreCase("p2h")) {
 				return Port.P2H;
 			} else if (p_type.equalsIgnoreCase("p2m")) {
 				return Port.P2M;
 			} else if (p_type.equalsIgnoreCase("p2p")) {
 				return Port.P2P;
 			} else if (p_type.equalsIgnoreCase("p2s")) {
 				return Port.P2S;
 			} else if (p_type.equalsIgnoreCase("p2w")) {
 				return Port.P2W;
 			} else if (p_type.equalsIgnoreCase("p2wc")) {
 				return Port.P2WC;
 			} else if (p_type.equalsIgnoreCase("p2we")) {
 				return Port.P2WE;
 			} else if (p_type.equalsIgnoreCase("w2m")) {
 				return Port.W2M;
 			} else if (p_type.equalsIgnoreCase("w2p")) {
 				return Port.W2P;
 			} else if (p_type.equalsIgnoreCase("w2s")) {
 				return Port.W2S;
 			} else if (p_type.equalsIgnoreCase("w2w")) {
 				return Port.W2W;
 			}
 			break;
 		case SERVERINFO:
 			if (p_type.equalsIgnoreCase("blacklist") || p_type.equalsIgnoreCase("banlist")) {
 				return CmdServerInfo.Types.BLACKLIST;
 			} else if (p_type.equalsIgnoreCase("info") || p_type.equalsIgnoreCase("information")) {
 				return CmdServerInfo.Types.INFO;
 			} else if (p_type.equalsIgnoreCase("onlinelist") || p_type.equalsIgnoreCase("online")) {
 				return CmdServerInfo.Types.ONLINELIST;
 			} else if (p_type.equalsIgnoreCase("oplist") || p_type.equalsIgnoreCase("ops")) {
 				return CmdServerInfo.Types.OPLIST;
 			} else if (p_type.equalsIgnoreCase("whitelist")) {
 				return CmdServerInfo.Types.WHITELIST;
 			} else if (p_type.equalsIgnoreCase("worldlist") || p_type.equalsIgnoreCase("worlds")) {
 				return CmdServerInfo.Types.WORLDLIST;
 			}
 			break;
 		case SERVERTIME:
 			return Default.NOCHANGE;
 		case SERVERWEATHER:
 			if (p_type.equalsIgnoreCase("rain")) {
				return CmdWorldWeather.Types.RAIN;
 			} else if (p_type.equalsIgnoreCase("storm")) {
				return CmdWorldWeather.Types.STORM;
 			} else if (p_type.equalsIgnoreCase("sun")) {
				return CmdWorldWeather.Types.SUN;
 			}
 			break;
 		case SET:
 			if (p_type.equalsIgnoreCase("bedspawn") || p_type.equalsIgnoreCase("home") ) {
 				return CmdLocation.Types.BEDSPAWN;
 			} else if (p_type.equalsIgnoreCase("spawn")) {
 				return CmdLocation.Types.SPAWN;
 			}
 			break;
 		case SPAWN:
 			if (p_type.equalsIgnoreCase("cursor")) {
 				return CmdSpawn.Types.CURSOR;
 			} else if (p_type.equalsIgnoreCase("player")) {
 				return CmdSpawn.Types.PLAYER;
 			}
 			break;
 		case WORLDTIME:
 			return Default.NOCHANGE;
 		case WORLDWEATHER:
 			if (p_type.equalsIgnoreCase("rain")) {
 				return CmdWorldWeather.Types.RAIN;
 			} else if (p_type.equalsIgnoreCase("storm")) {
 				return CmdWorldWeather.Types.STORM;
 			} else if (p_type.equalsIgnoreCase("sun")) {
 				return CmdWorldWeather.Types.SUN;
 			}
 			break;
 		default:
 			break;
 		}
 		return Default.NOTYPE;
 	}
 	
 	static MessageType getMessageType(Category p_category, Type p_type) {
 		switch (p_category) {
 		case PLAYER:
 			if (p_type instanceof CmdPlayerInfo.Types) {
 				return MessageType.NOMESSAGE;
 			} else {
 				return MessageType.MESSAGE;
 			}
 		case SERVERINFO:
 			return MessageType.NOMESSAGE;
 		default:
 			return MessageType.MESSAGE;
 		}
 	}
 	
 	static PermissionExtension getPermissionExtension(Category p_category, Type p_type) {
 		switch (p_category) {
 		case MODE:
 			return PermissionExtension.PLAYER;
 		case PLAYER:
 			if (p_type instanceof CmdPlayer.Types) {
 				return PermissionExtension.PLAYER;
 			} else {
 				return PermissionExtension.NOEXTENSION;
 			}
 		case SET:
 			if (p_type instanceof CmdLocation.Types && p_type == CmdLocation.Types.BEDSPAWN) {
 				return PermissionExtension.PLAYER;
 			} else {
 				return PermissionExtension.NOEXTENSION;
 			}
 		case SPAWN:
 			if (p_type instanceof CmdSpawn.Types && p_type == CmdSpawn.Types.PLAYER) {
 				return PermissionExtension.PLAYER;
 			} else {
 				return PermissionExtension.NOEXTENSION;
 			}
 		case WORLDTIME:
 			return PermissionExtension.WORLD;
 		case WORLDWEATHER:
 			return PermissionExtension.WORLD;
 		default:
 			return PermissionExtension.NOEXTENSION;
 		}
 	}
 }
