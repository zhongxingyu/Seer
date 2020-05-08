 package nl.giantit.minecraft.GiantShop.core.Commands.chat.Discount;
 
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.Misc.Heraut;
 import nl.giantit.minecraft.GiantShop.Misc.Messages;
 import nl.giantit.minecraft.GiantShop.Misc.Misc;
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.GiantShop.core.Items.Items;
 import nl.giantit.minecraft.GiantShop.core.Tools.Discount.Discount;
 import nl.giantit.minecraft.GiantShop.core.perms.Permission;
 
 import org.bukkit.entity.Player;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 public class List {
 
 	private static Permission perms = GiantShop.getPlugin().getPermHandler().getEngine();
 	private static Messages mH = GiantShop.getPlugin().getMsgHandler();
 	private static Items iH = GiantShop.getPlugin().getItemHandler();
 	private static config conf = config.Obtain();
 	private static String name = GiantShop.getPlugin().getPubName();
 	
 	private static void available(Player p, String[] args) {
		if(perms.has(p, "giantshop.discount.list")) {
 			int perPage = conf.getInt("GiantShop.global.perPage");
 			int curPag = 0;
 			
 			if(args.length > 2) {
 				try{
 					curPag = Integer.parseInt(args[2]);
 				}catch(NumberFormatException e) {
 					curPag = 1;
 				}
 			}else
 				curPag = 1;
 			
 			curPag = (curPag > 0) ? curPag : 1;
 			
 			Set<Discount> discounts = GiantShop.getPlugin().getDiscounter().getAllDiscounts(p);
 			int pages = ((int)Math.ceil((double)discounts.size() / (double)perPage) < 1) ? 1 : (int)Math.ceil((double)discounts.size() / (double)perPage);
 			int start = (curPag * perPage) - perPage;
 			
 			if(discounts.size() <= 0) {
 				Heraut.say(p, "&e[&3" + name + "&e]&c Sorry no discounts for you yet :(");
 			}else if(curPag > pages) {
 				Heraut.say(p, "&e[&3" + name + "&e]&c Your discounts list only has &e" + pages + " &cpages!!");
 			}else{
 				Heraut.say(p, "&e[&3" + name + "&e]&f Discounts. Page: &e" + curPag + "&f/&e" + pages);
 
 				Iterator<Discount> discIterator = discounts.iterator();
 				for(int i = start; i < (((start + perPage) > discounts.size()) ? discounts.size() : (start + perPage)); i++) {
 					Discount disc = discIterator.next();
 					HashMap<String, String> data = new HashMap<String, String>();
 					Integer type = disc.getItemType();
 					type = (type <= 0) ? null : type;
 					data.put("discount", String.valueOf(disc.getDiscount()));
 					data.put("item", iH.getItemNameByID(disc.getItemId(), type));
 					
 					Heraut.say(p, mH.getMsg(Messages.msgType.MAIN, "discountEntry", data));
 				}
 			}
 		}else{
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("command", "discount list");
 
 			Heraut.say(p, mH.getMsg(Messages.msgType.ERROR, "noPermissions", data));
 		}
 	}
 	
 	private static void all(Player p, String[] args) {
 		if(perms.has(p, "giantshop.admin.discount.list")) {
 			int perPage = conf.getInt("GiantShop.global.perPage");
 			int curPag = 0;
 			String user = null;
 			String group = null;
 			
 			if(args.length > 3) {
 				try{
 					curPag = Integer.parseInt(args[3]);
 				}catch(NumberFormatException e) {
 					for(int i = 2; i < args.length; i++) {
 						if(args[i].startsWith("-p:")) {
 							try{
 								curPag = Integer.parseInt(args[i].replaceFirst("-p:", ""));
 							}catch(NumberFormatException ex) {
 							}
 							continue;
 						}else if(args[i].startsWith("-u:")) {
 							user = args[i].replaceFirst("-u:", "");
 							if(Misc.getPlayer(user) != null)
 								user = Misc.getPlayer(user).getName();
 							continue;
 						}else if(args[i].startsWith("-g:")) {
 							group = args[i].replaceFirst("-g:", "");
 							continue;
 						}
 					}
 				}
 			}else
 				curPag = 1;
 			
 			curPag = (curPag > 0) ? curPag : 1;
 			
 			Set<Discount> discounts;
 			if(user != null) {
 				discounts = GiantShop.getPlugin().getDiscounter().getAllDiscounts(user, false);
 			}else if(group != null) {
 				discounts = GiantShop.getPlugin().getDiscounter().getAllDiscounts(group, true);
 			}else{
 				discounts = GiantShop.getPlugin().getDiscounter().getAllDiscounts();
 			}
 			int pages = ((int)Math.ceil((double)discounts.size() / (double)perPage) < 1) ? 1 : (int)Math.ceil((double)discounts.size() / (double)perPage);
 			int start = (curPag * perPage) - perPage;
 			
 			if(discounts.size() <= 0) {
 				Heraut.say(p, "&e[&3" + name + "&e]&c Sorry no discounts yet :(");
 			}else if(curPag > pages) {
 				Heraut.say(p, "&e[&3" + name + "&e]&c Discounts list only has &e" + pages + " &cpages!!");
 			}else{
 				Heraut.say(p, "&e[&3" + name + "&e]&f Discounts. Page: &e" + curPag + "&f/&e" + pages);
 
 				Iterator<Discount> discIterator = discounts.iterator();
 				for(int i = start; i < (((start + perPage) > discounts.size()) ? discounts.size() : (start + perPage)); i++) {
 					Discount disc = discIterator.next();
 					HashMap<String, String> data = new HashMap<String, String>();
 					Integer type = disc.getItemType();
 					type = (type <= 0) ? null : type;
 					data.put("id", String.valueOf(disc.getDiscountID()));
 					data.put("discount", String.valueOf(disc.getDiscount()));
 					data.put("item", iH.getItemNameByID(disc.getItemId(), type));
 					data.put("grplay", (disc.hasGroup() ? "group" : "player"));
 					data.put("for", (disc.hasGroup() ? disc.getGroup() : disc.getOwner()));
 					
 					Heraut.say(p, mH.getMsg(Messages.msgType.ADMIN, "discountEntry", data));
 				}
 			}
 		}else{
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("command", "discount list all");
 
 			Heraut.say(p, mH.getMsg(Messages.msgType.ERROR, "noPermissions", data));
 		}
 	}
 	
 	public static void exec(Player player, String[] args) {
 		if(args.length > 2) {
 			if(Misc.isEitherIgnoreCase(args[2], "all", "a")) {
 				// Show admin all existing discounts
 				all(player, args);
 			}else{
 				// Show users available discount.
 				available(player, args);
 			}
 		}else{
 			// Show users available discount.
 			available(player, args);
 		}
 	}
 }
