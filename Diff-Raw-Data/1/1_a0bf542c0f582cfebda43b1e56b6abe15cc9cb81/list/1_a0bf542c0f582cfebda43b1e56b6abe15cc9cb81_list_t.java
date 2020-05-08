 package nl.giantit.minecraft.GiantShop.core.Commands;
 
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.Misc.Heraut;
 import nl.giantit.minecraft.GiantShop.Misc.Messages;
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.GiantShop.core.perm;
 import nl.giantit.minecraft.GiantShop.core.Database.db;
 import nl.giantit.minecraft.GiantShop.core.Items.Items;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  *
  * @author Giant
  */
 public class list {
 	
 	public static void list(Player player, String[] args) {
 		Messages msgs = GiantShop.getPlugin().getMsgHandler();
 		Items iH = GiantShop.getPlugin().getItemHandler();
 		perm perms = perm.Obtain();
 		config conf = config.Obtain();
 		if(perms.has(player, "giantshop.shop.list")) {
 			String name = GiantShop.getPlugin().getPubName();
 			int perPage = conf.getInt("GiantShop.global.perPage");
 			int curPag = 0;
 			
 			if(args.length >= 2) {
 				try{
 					curPag = Integer.parseInt(args[1]);
 				}catch(NumberFormatException e) {
 					curPag = 1;
 				}
 			}else
 				curPag = 1;
 
 			curPag = (curPag > 0) ? curPag : 1;
 		
 			db DB = db.Obtain();
 			ArrayList<String> fields = new ArrayList<String>();
 			fields.add("itemID");
 			fields.add("type");
 			fields.add("perStack");
 			fields.add("sellFor");
 			fields.add("buyFor");
 			fields.add("stock");
 			fields.add("shops");
 			
 			HashMap<String, String> order = new HashMap<String, String>();
 			order.put("itemID", "ASC");
 			order.put("type", "ASC");
 			ArrayList<HashMap<String, String>> data = DB.select(fields).from("#__items").orderBy(order).execQuery();
 			
 			int pages = ((int)Math.ceil((double)data.size() / (double)perPage) < 1) ? 1 : (int)Math.ceil((double)data.size() / (double)perPage);
 			int start = (curPag * perPage) - perPage;
 			if(data.size() <= 0) {
 				Heraut.say(player, msgs.getMsg(Messages.msgType.ERROR, "noItems"));
 			}else if(curPag > pages) {
 				Heraut.say(player, "&e[&3" + name + "&e]&c My Item list only has &e" + pages + " &cpages!!");
 			}else{
 				Heraut.say(player, "&e[&3" + name + "&e]&f Item list. Page: &e" + curPag + "&f/&e" + pages);
 				
 				for(int i = start; i < (((start + perPage) > data.size()) ? data.size() : (start + perPage)); i++) {
 					HashMap<String, String> entry = data.get(i);
 					
 					HashMap<String, String> params = new HashMap<String, String>();
 					params.put("id", entry.get("itemID"));
 					params.put("type", (!entry.get("type").equals("-1") ? entry.get("type") : "0"));
 					params.put("name", iH.getItemNameByID(Integer.parseInt(entry.get("itemID")), Integer.parseInt(params.get("type"))));
 					params.put("perStack", entry.get("perStack"));
 					params.put("sellFor", entry.get("sellFor"));
 					params.put("buyFor", entry.get("buyFor"));
 					
 					if(conf.getBoolean("GiantShop.global.useStock") == true)
 						params.put("stock", (!entry.get("stock").equals("-1") ? entry.get("stock") : "unlimited"));
 					
 					// Future stuff
 					/* if(conf.getBoolean("GiantShop.Location.useGiantShopLocation") == true) {
 					 *		ArrayList<Indaface> shops = GiantShop.getPlugin().getLocationHandler().parseShops(entry.get("shops"));
 					 *		for(Indaface shop : shops) {
 					 *			if(shop.inShop(player.getLocation())) {
 					 *				Heraut.say(player, msgs.getMsg(Messages.msgType.MAIN, "itemListEntry", params));
 					 *				break;
 					 *			}
 					 *		}
 					 * }else
 					 */
 					
 					Heraut.say(player, msgs.getMsg(Messages.msgType.MAIN, "itemListEntry", params));
 				}
 			}
 			
 		}else{
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("command", "list");
 
 			Heraut.say(player, msgs.getMsg(Messages.msgType.ERROR, "noPermissions", data));
 		}
 	}
 	
 	public static void listConsole(CommandSender sender, String[] args) {
 		Messages msgs = GiantShop.getPlugin().getMsgHandler();
 		Items iH = GiantShop.getPlugin().getItemHandler();
 		config conf = config.Obtain();
 		
 		String name = GiantShop.getPlugin().getPubName();
 		int perPage = conf.getInt("GiantShop.global.perPage");
 		int curPag = 0;
 		
 		if(args.length >= 2) {
 			try{
 				curPag = Integer.parseInt(args[1]);
 			}catch(NumberFormatException e) {
 				curPag = 1;
 			}
 		}else
 			curPag = 1;
 			curPag = (curPag > 0) ? curPag : 1;
 	
 		db DB = db.Obtain();
 		ArrayList<String> fields = new ArrayList<String>();
 		fields.add("itemID");
 		fields.add("type");
 		fields.add("perStack");
 		fields.add("sellFor");
 		fields.add("buyFor");
 		fields.add("stock");
 		fields.add("shops");
 		
 		HashMap<String, String> order = new HashMap<String, String>();
 		order.put("itemID", "ASC");
 		order.put("type", "ASC");
 		ArrayList<HashMap<String, String>> data = DB.select(fields).from("#__items").orderBy(order).execQuery();
 		
 		int pages = ((int)Math.ceil((double)data.size() / (double)perPage) < 1) ? 1 : (int)Math.ceil((double)data.size() / (double)perPage);
 		int start = (curPag * perPage) - perPage;
 		if(data.size() <= 0) {
 			Heraut.say(sender, msgs.getConsoleMsg(Messages.msgType.ERROR, "noItems"));
 		}else if(curPag > pages) {
 			Heraut.say(sender, "[" + name + "] My Item list only has " + pages + " pages!!");
 		}else{
 			Heraut.say(sender, "[" + name + "] Item list. Page: " + curPag + "/" + pages);
 			
 			for(int i = start; i < (((start + perPage) > data.size()) ? data.size() : (start + perPage)); i++) {
 				HashMap<String, String> entry = data.get(i);
 				
 				HashMap<String, String> params = new HashMap<String, String>();
 				params.put("id", entry.get("itemID"));
 				params.put("type", (!entry.get("type").equals("-1") ? entry.get("type") : "0"));
 				params.put("name", iH.getItemNameByID(Integer.parseInt(entry.get("itemID")), Integer.parseInt(params.get("type"))));
 				params.put("perStack", entry.get("perStack"));
 				params.put("sellFor", entry.get("sellFor"));
 				params.put("buyFor", entry.get("buyFor"));
 				
 				if(conf.getBoolean("GiantShop.global.useStock") == true)
 					params.put("stock", (!entry.get("stock").equals("-1") ? entry.get("stock") : "unlimited"));
 					
 				Heraut.say(sender, msgs.getConsoleMsg(Messages.msgType.MAIN, "itemListEntry", params));
 			}
 		}	
 	}
 }
