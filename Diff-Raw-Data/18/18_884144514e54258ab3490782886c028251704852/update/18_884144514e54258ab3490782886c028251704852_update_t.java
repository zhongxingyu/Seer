 package nl.giantit.minecraft.GiantShop.Locationer.core.Commands.chat;
 
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.GiantShop.core.Database.db;
 import nl.giantit.minecraft.GiantShop.Misc.Heraut;
 import nl.giantit.minecraft.GiantShop.Misc.Messages;
 import nl.giantit.minecraft.GiantShop.core.perm;
 import nl.giantit.minecraft.GiantShop.Locationer.Locationer;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import nl.giantit.minecraft.GiantShop.Misc.Misc;
 
 /**
  *
  * @author Giant
  */
 public class update {
 	
 	private static config conf = config.Obtain();
 	private static db DB = db.Obtain();
 	private static perm perms = perm.Obtain();
 	private static Messages mH = GiantShop.getPlugin().getMsgHandler();
 	private static Locationer lH = GiantShop.getPlugin().getLocHandler();
 	private static HashMap<Player, HashMap<String, String>> stored = new HashMap<Player, HashMap<String, String>>();
 	
 	private static void select(Player player, String[] args) {
 		String name = null;
 		String world = null;
 
 		for(int i = 0; i < args.length; i++) {
 			if(args[i].startsWith("-n:")) {
 				name = args[i].replaceFirst("-n:", "");
 				continue;
 			}else if(args[i].startsWith("-w:")) {
 				world = args[i].replaceFirst("-w:", "");
 				continue;
 			}
 		}
 
 		if(name != null) {
 			if(world == null)
 				world = player.getWorld().getName();
 
 			ArrayList<String> fields = new ArrayList<String>();
 			fields.add("locMinX");
 			fields.add("locMinY");
 			fields.add("locMinZ");
 			fields.add("locMaxX");
 			fields.add("locMaxY");
 			fields.add("locMaxZ");
 
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("name", name);
 			data.put("world", world);
 
 			ArrayList<HashMap<String, String>> resSet = DB.select(fields).from("#__shops").where(data).execQuery();
 			if(!resSet.isEmpty()) {
 				HashMap<String, String> res = resSet.get(0);
 				res.put("name", name);
 				res.put("world", world);
				res.put("oname", name);
				res.put("oworld", world);
 				
 				stored.put(player, res);
 				
 				data = new HashMap<String, String>();
 				data.put("shop", name);
 				data.put("world", world);
 
 				Heraut.say(player, mH.getMsg(Messages.msgType.ADMIN, "shopSelected", data));
 			}else{
 				data = new HashMap<String, String>();
 				data.put("shop", name);
 				data.put("world", world);
 
 				Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "shopNotFound", data));
 			}
 		}else{
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("command", "loc update select");
 
 			Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "syntaxError", data));
 		}
 	}
 	
 	private static void set (Player player, String[] args) {
 		String type = "name";
 		String name = stored.get(player).get("name");
 
 		for(int i = 0; i < args.length; i++) {
 			if(args[i].startsWith("-t:")) {
 				type = args[i].replaceFirst("-t:", "");
 				continue;
 			}else if(args[i].startsWith("-n:")) {
 				name = args[i].replaceFirst("-n:", "");
 				continue;
 			}
 		}
 
 		if(Misc.isEitherIgnoreCase(type, "name", "n")) {
 			if(name != null && !name.equals("")) {
 				ArrayList<String> fields = new ArrayList<String>();
 				fields.add("id");
 				HashMap<String, String> data = new HashMap<String, String>();
 				data.put("name", name);
 				data.put("world", stored.get(player).get("world"));
 
 				DB.select(fields).from("#__shops").where(data);
 				if(DB.execQuery().isEmpty()) {
 					HashMap<String, String> p = stored.get(player);
 				
 					p.put("name", name);
 
 					stored.put(player, p);
 					
 					data = new HashMap<String, String>();
 					data.put("shop", name);
					
					Heraut.say(player, mH.getMsg(Messages.msgType.ADMIN, "nameUpdated", data));
 				}else{
 					data = new HashMap<String, String>();
 					data.put("shop", name);
 					data.put("world", stored.get(player).get("world"));
 
 					Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "shopNameTaken", data));
 				}
 			}else{
 				HashMap<String, String> data = new HashMap<String, String>();
 				data.put("command", "loc update set -t:name");
 
 				Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "syntaxError", data));
 			}
 		}else if(Misc.isEitherIgnoreCase(type, "world", "w")) {
 			HashMap<String, Location> points = lH.getPlayerPoints(player);
 			if(points.size() < 2) {
 				Heraut.say(player, "You need to set 2 points to update a shops location!");
 				return;
 			}
 			
 			if(!points.containsKey("min") || !points.containsKey("max")) {
 				Heraut.say(player, "Failed to update location. Invalid points passed!");
 				return;
 			}
 			
 			Location loc1, loc2;
 			loc1 = points.get("min");
 			loc2 = points.get("max");
 			
 			if(!loc1.getWorld().getName().equals(loc2.getWorld().getName())) {
 				Heraut.say(player, "Failed to update location. Points not in same world!");
 				return;
 			}
 			
 			double minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
 			double minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
 			double minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
 			double maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
 			double maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
 			double maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
 			
 			while (maxX - minX < 4) maxX += 1;
 			while (maxY - minY < 2) maxY += 1;
 			while (maxZ - minZ < 4) maxZ += 1;
 			
 			Location l = new Location(loc1.getWorld(), minX, minY, minZ);
 			Location l2 = new Location(loc1.getWorld(), maxX, maxY, maxZ);
 			
 			if(!lH.inShop(l, stored.get(player).get("name")) && !lH.inShop(l2, stored.get(player).get("name"))) {
 				HashMap<String, String> p = stored.get(player);
 				
 				p.put("world", loc1.getWorld().getName());
 				p.put("locMinX", String.valueOf(minX));
 				p.put("locMinY", String.valueOf(minY));
 				p.put("locMinZ", String.valueOf(minZ));
 				p.put("locMaxX", String.valueOf(maxX));
 				p.put("locMaxY", String.valueOf(maxY));
 				p.put("locMaxZ", String.valueOf(maxZ));
 				
 				stored.put(player, p);
 				
 				HashMap<String, String> data = new HashMap<String, String>();
 				data.put("shop", name);
 				data.put("world", loc1.getWorld().getName());
 				data.put("minX", String.valueOf(minX));
 				data.put("minY", String.valueOf(minY));
 				data.put("minZ", String.valueOf(minZ));
 				data.put("maxX", String.valueOf(maxX));
 				data.put("maxY", String.valueOf(maxY));
 				data.put("maxZ", String.valueOf(maxZ));
 				
 				lH.remPlayerPoint(player);
 				Heraut.say(player, mH.getMsg(Messages.msgType.ADMIN, "worldUpdated", data));
 			}else{
 				HashMap<String, String> data = new HashMap<String, String>();
 				data.put("shop", name);
 				data.put("world", loc1.getWorld().getName());
 				data.put("minX", String.valueOf(minX));
 				data.put("minY", String.valueOf(minY));
 				data.put("minZ", String.valueOf(minZ));
 				data.put("maxX", String.valueOf(maxX));
 				data.put("maxY", String.valueOf(maxY));
 				data.put("maxZ", String.valueOf(maxZ));
 
 				Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "locTaken", data));
 			}
 		}else if(Misc.isEitherIgnoreCase(type, "loc", "l")) {
 			HashMap<String, Location> points = lH.getPlayerPoints(player);
 			if(points.size() < 2) {
 				Heraut.say(player, "You need to set 2 points to update a shops location!");
 				return;
 			}
 			
 			if(!points.containsKey("min") || !points.containsKey("max")) {
 				Heraut.say(player, "Failed to update location. Invalid points passed!");
 				return;
 			}
 			
 			Location loc1, loc2;
 			loc1 = points.get("min");
 			loc2 = points.get("max");
 			
 			if(!loc1.getWorld().getName().equals(loc2.getWorld().getName())) {
 				Heraut.say(player, "Failed to update location. Points not in same world!");
 				return;
 			}
 			
 			double minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
 			double minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
 			double minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
 			double maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
 			double maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
 			double maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
 			
 			while (maxX - minX < 4) maxX += 1;
 			while (maxY - minY < 2) maxY += 1;
 			while (maxZ - minZ < 4) maxZ += 1;
 			
 			Location l = new Location(loc1.getWorld(), minX, minY, minZ);
 			Location l2 = new Location(loc1.getWorld(), maxX, maxY, maxZ);
 			
 			if(!lH.inShop(l, stored.get(player).get("name")) && !lH.inShop(l2, stored.get(player).get("name"))) {
 				HashMap<String, String> p = stored.get(player);
 				
 				p.put("locMinX", String.valueOf(minX));
 				p.put("locMinY", String.valueOf(minY));
 				p.put("locMinZ", String.valueOf(minZ));
 				p.put("locMaxX", String.valueOf(maxX));
 				p.put("locMaxY", String.valueOf(maxY));
 				p.put("locMaxZ", String.valueOf(maxZ));
 				
 				stored.put(player, p);
 				
 				HashMap<String, String> data = new HashMap<String, String>();
 				data.put("shop", name);
 				data.put("world", loc1.getWorld().getName());
 				data.put("minX", String.valueOf(minX));
 				data.put("minY", String.valueOf(minY));
 				data.put("minZ", String.valueOf(minZ));
 				data.put("maxX", String.valueOf(maxX));
 				data.put("maxY", String.valueOf(maxY));
 				data.put("maxZ", String.valueOf(maxZ));
 				
 				lH.remPlayerPoint(player);
 				Heraut.say(player, mH.getMsg(Messages.msgType.ADMIN, "locUpdated", data));
 			}else{
 				HashMap<String, String> data = new HashMap<String, String>();
 				data.put("shop", name);
 				data.put("world", loc1.getWorld().getName());
 				data.put("minX", String.valueOf(minX));
 				data.put("minY", String.valueOf(minY));
 				data.put("minZ", String.valueOf(minZ));
 				data.put("maxX", String.valueOf(maxX));
 				data.put("maxY", String.valueOf(maxY));
 				data.put("maxZ", String.valueOf(maxZ));
 
 				Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "locTaken", data));
 			}
 		}else{
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("command", "loc update set");
 
 			Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "syntaxError", data));
 		}
 	}
 	
 	private static void save(Player player, String[] args) {
 		HashMap<String, String> tmp = stored.get(player);
 		
		String oname = tmp.remove("oname");
		String oworld = tmp.remove("oworld");
		
 		HashMap<String, String> data = new HashMap<String, String>();
		data.put("name", oname);
		data.put("world", oworld);
 
 		DB.update("#__shops").set(tmp).where(data).updateQuery();
 		
		lH.removeShop(oname, oworld);
 		
 		double minX = Double.parseDouble(tmp.get("locMinX"));
 		double minY = Double.parseDouble(tmp.get("locMinY"));
 		double minZ = Double.parseDouble(tmp.get("locMinZ"));
 		double maxX = Double.parseDouble(tmp.get("locMaxX"));
 		double maxY = Double.parseDouble(tmp.get("locMaxY"));
 		double maxZ = Double.parseDouble(tmp.get("locMaxZ"));
 		
 		Location l = new Location(GiantShop.getPlugin().getSrvr().getWorld(tmp.get("world")), minX, minY, minZ);
 		Location l2 = new Location(GiantShop.getPlugin().getSrvr().getWorld(tmp.get("world")), maxX, maxY, maxZ);
 		
 		ArrayList<Location> loc = new ArrayList<Location>();
 		loc.add(l);
 		loc.add(l2);
 		
 		lH.addShop(loc, tmp.get("name"));
 		
 		data = new HashMap<String, String>();
 		data.put("shop", tmp.get("name"));
 		data.put("world", tmp.get("world"));
 		data.put("minX", String.valueOf(minX));
 		data.put("minY", String.valueOf(minY));
 		data.put("minZ", String.valueOf(minZ));
 		data.put("maxX", String.valueOf(maxX));
 		data.put("maxY", String.valueOf(maxY));
 		data.put("maxZ", String.valueOf(maxZ));
 		
 		stored.remove(player);
 		Heraut.say(player, mH.getMsg(Messages.msgType.ADMIN, "shopUpdated", data));
 	}
 	
 	public static void update(Player player, String[] args) {
 		if(perms.has(player, "giantshop.location.update")) {
 			if(args.length > 1) {
 				if(Misc.isEitherIgnoreCase(args[1], "select", "s")) {
 					update.select(player, args);
 				}else if(args[1].equalsIgnoreCase("set")) {
 					if(stored.containsKey(player)) {
 						update.set(player, args);
 					}else{
 						Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "noShopSelected"));
 					}
 				}else if(args[1].equalsIgnoreCase("save")) {
 					if(stored.containsKey(player)) {
 						update.save(player, args);
 					}else{
 						Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "noShopSelected"));
 					}
 				}else if(args[1].equalsIgnoreCase("show")) {
 					if(stored.containsKey(player)) {
 						HashMap<String, String> data = stored.get(player);
 						Heraut.say(player, "Here's the result for shop " + data.get("name") + " in word " + data.get("world") + "!");
 						Heraut.say(player, "minX: " + data.get("locMinX"));
 						Heraut.say(player, "minY: " + data.get("locMinY"));
 						Heraut.say(player, "minZ: " + data.get("locMinZ"));
 						Heraut.say(player, "maxX: " + data.get("locMaxX"));
 						Heraut.say(player, "maxY: " + data.get("locMaxY"));
 						Heraut.say(player, "maxZ: " + data.get("locMaxZ"));
 					}else{
 						Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "noShopSelected"));
 					}
 				}else{
 					HashMap<String, String> data = new HashMap<String, String>();
 					data.put("command", "loc update");
 
 					Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "syntaxError", data));
 				}
 			}else{
 				HashMap<String, String> data = new HashMap<String, String>();
 				data.put("command", "loc update");
 
 				Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "syntaxError", data));
 			}
 		}else{
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("command", "loc update");
 
 			Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "noPermissions", data));
 		}
 	}
 }
