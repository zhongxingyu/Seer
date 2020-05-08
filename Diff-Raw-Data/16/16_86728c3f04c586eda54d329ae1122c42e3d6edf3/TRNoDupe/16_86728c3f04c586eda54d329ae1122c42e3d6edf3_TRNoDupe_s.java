 package nl.taico.tekkitrestrict.functions;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.eclipse.jdt.annotation.NonNull;
 
 import nl.taico.tekkitrestrict.Log;
 import nl.taico.tekkitrestrict.Util;
 import nl.taico.tekkitrestrict.Log.Warning;
 import nl.taico.tekkitrestrict.TRConfigCache.Dupes;
 import nl.taico.tekkitrestrict.objects.TREnums.DupeType;
 
 public class TRNoDupe {
 	public static ConcurrentHashMap<String, Integer> cmdRM = new ConcurrentHashMap<String, Integer>();
 	public static ConcurrentHashMap<String, Integer> cmdBag = new ConcurrentHashMap<String, Integer>();
 	public static ConcurrentHashMap<String, Integer> cmdTP = new ConcurrentHashMap<String, Integer>();
 	public static ConcurrentHashMap<String, Integer> cmdTransmution = new ConcurrentHashMap<String, Integer>();
 	public static ConcurrentHashMap<String, Integer> cmdTC = new ConcurrentHashMap<String, Integer>();
 	public static ConcurrentHashMap<String, Integer> cmdTCG = new ConcurrentHashMap<String, Integer>();
 	public static ConcurrentHashMap<String, Integer> cmdPedestal = new ConcurrentHashMap<String, Integer>();
 	
 	public static void handleDupe(@NonNull Player player, DupeType type, int id, int data){
 		String message = "";
 		if (type == DupeType.alcBag){
 			if (Dupes.alcBags.useCommand){
 				Integer cur = cmdBag.get(player.getName());
 				if (cur == null) cur = 0;
 				cur++;
 				
 				if (cur >= Dupes.alcBags.triggerAfter){
 					try {
 						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), convert(Dupes.alcBags.command, "AlcBag", player, id, data));
 					} catch (Exception ex) {
 						Warning.config("The command set for Anti-Dupes.AlchemyBagDupe returned an error!", false);
 					}
 					cmdBag.remove(player.getName());
 				} else {
 					cmdBag.put(player.getName(), cur);
 				}
 			}
 			message = convert(Dupes.broadcastFormat, "Alchemy Bag + BHB / Void Ring", player, id, data);
 			
 			if (Dupes.alcBags.broadcast) Util.broadcastNoConsole("[TRDupe] " + message, "tekkitrestrict.notify.dupe");
 			if (Dupes.alcBags.kick) Util.kick(player, "[TRDupe] " + message);
 		}
 		else if (type == DupeType.rmFurnace){
 			if (Dupes.rmFurnaces.useCommand){
 				Integer cur = cmdRM.get(player.getName());
 				if (cur == null) cur = 0;
 				cur++;
 				
 				if (cur >= Dupes.rmFurnaces.triggerAfter){
 					try {
 						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), convert(Dupes.rmFurnaces.command, "RMFurnace", player, id, data));
 					} catch (Exception ex) {
 						Warning.config("The command set for Anti-Dupes.RMFurnaceDupe returned an error!", false);
 					}
 					cmdRM.remove(player.getName());
 				} else {
 					cmdRM.put(player.getName(), cur);
 				}
 			}
 			message = convert(Dupes.broadcastFormat, "Red Matter Furnace", player, id, data);
 			
 			if (Dupes.rmFurnaces.broadcast) Util.broadcastNoConsole("[TRDupe] " + message, "tekkitrestrict.notify.dupe");
 			if (Dupes.rmFurnaces.kick) Util.kick(player, "[TRDupe] " + message);
 		}
 		else if (type == DupeType.tankCart){
 			if (Dupes.tankcarts.useCommand){
 				Integer cur = cmdTC.get(player.getName());
 				if (cur == null) cur = 0;
 				cur++;
 				
 				if (cur >= Dupes.tankcarts.triggerAfter){
 					try {
 						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), convert(Dupes.tankcarts.command, "TankCart", player, id, data));
 					} catch (Exception ex) {
 						Warning.config("The command set for Anti-Dupes.TankCartDupe returned an error!", false);
 					}
 					cmdTC.remove(player.getName());
 				} else {
 					cmdTC.put(player.getName(), cur);
 				}
 			}
 			message = convert(Dupes.broadcastFormat, "Tank Cart", player, id, data);
 			
 			if (Dupes.tankcarts.broadcast) Util.broadcastNoConsole("[TRDupe] " + message, "tekkitrestrict.notify.dupe");
 			if (Dupes.tankcarts.kick) Util.kick(player, "[TRDupe] " + message);
 		}
 		else if (type == DupeType.tankCartGlitch){
 			if (Dupes.tankcartGlitchs.useCommand){
 				Integer cur = cmdTCG.get(player.getName());
 				if (cur == null) cur = 0;
 				cur++;
 				
 				if (cur >= Dupes.tankcartGlitchs.triggerAfter){
 					try {
 						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), convert(Dupes.tankcartGlitchs.command, "TankCartGlitch", player, id, data));
 					} catch (Exception ex) {
 						Warning.config("The command set for Anti-Dupes.TankCartGlitch returned an error!", false);
 					}
 					cmdTCG.remove(player.getName());
 				} else {
 					cmdTCG.put(player.getName(), cur);
 				}
 			}
 			message = convert(Dupes.broadcastFormat, "Tank Cart", player, id, data);
 			
 			if (Dupes.tankcartGlitchs.broadcast) Util.broadcastNoConsole("[TRDupe] " + message, "tekkitrestrict.notify.dupe");
 			if (Dupes.tankcartGlitchs.kick) Util.kick(player, "[TRDupe] " + message);
 		}
 		else if (type == DupeType.transmution){
 			if (Dupes.transmutes.useCommand){
 				Integer cur = cmdTransmution.get(player.getName());
 				if (cur == null) cur = 0;
 				cur++;
 				
 				if (cur >= Dupes.transmutes.triggerAfter){
 					try {
 						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), convert(Dupes.transmutes.command, "Transmute", player, id, data));
 					} catch (Exception ex) {
 						Warning.config("The command set for Anti-Dupes.TransmuteDupe returned an error!", false);
 					}
 					cmdTransmution.remove(player.getName());
 				} else {
 					cmdTransmution.put(player.getName(), cur);
 				}
 			}
 			message = convert(Dupes.broadcastFormat, "Transmution Table(t)", player, id, data);
 			
 			if (Dupes.transmutes.broadcast) Util.broadcastNoConsole("[TRDupe] " + message, "tekkitrestrict.notify.dupe");
 			if (Dupes.transmutes.kick) Util.kick(player, "[TRDupe] " + message);
 		}
 		else if (type == DupeType.pedestal){
 			if (Dupes.pedestals.useCommand){
 				Integer cur = cmdPedestal.get(player.getName());
 				if (cur == null) cur = 0;
 				cur++;
 				
 				if (cur >= Dupes.pedestals.triggerAfter){
 					try {
 						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), convert(Dupes.pedestals.command, "Pedestal", player, id, data));
 					} catch (Exception ex) {
 						Warning.config("The command set for Anti-Dupes.PedestalEmcGen returned an error!", false);
 					}
 					cmdPedestal.remove(player.getName());
 				} else {
 					cmdPedestal.put(player.getName(), cur);
 				}
 			}
 			message = convert(Dupes.broadcastFormat, "DM Pedestal + Harvest Godess Band", player, id, data);
 			
 			if (Dupes.pedestals.broadcast) Util.broadcastNoConsole("[TRDupe] " + message, "tekkitrestrict.notify.dupe");
 			if (Dupes.pedestals.kick) Util.kick(player, "[TRDupe] " + message);
 		}
 		
 		Log.Dupe(message);
 	}
 	
 	@NonNull private static String convert(@NonNull String str, @NonNull String type, @NonNull Player player, int id, int data){
 		str = Log.replaceColors(str);
 		str = str.replaceAll("(?i)\\{PLAYER\\}", player.getName());
		str = str.replace("(?i)\\{TYPE\\}", type);
 		if (id == 0){
			str = str.replace("(?i)\\{ID\\}","");
			str = str.replace("(?i)\\{DATA\\}", "");
			str = str.replace("(?i)\\{ITEM\\}", "");
 		} else {
			str = str.replace("(?i)\\{ID\\}", ""+id);
			str = str.replace("(?i)\\{DATA\\}", ""+data);
			str = str.replace("(?i)\\{ITEM\\}", ""+id+":"+data);
 		}
 		str = str.replace("  ", " ");
 		return str;
 	}
 	
 	public static void playerLogout(@NonNull Player player){
 		String n = player.getName();
 		cmdRM.remove(n);
 		cmdBag.remove(n);
 		cmdPedestal.remove(n);
 		cmdTC.remove(n);
 		cmdTCG.remove(n);
 		cmdTP.remove(n);
 		cmdTransmution.remove(n);
 	}
 }
