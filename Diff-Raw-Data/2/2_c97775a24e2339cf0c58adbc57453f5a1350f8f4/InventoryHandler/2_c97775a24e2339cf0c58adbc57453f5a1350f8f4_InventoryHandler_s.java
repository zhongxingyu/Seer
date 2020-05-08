 package nl.giantit.minecraft.GiantShop.core.Tools;
 
 import nl.giantit.minecraft.GiantShop.core.config;
 
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class InventoryHandler {
 
 	public static boolean isTool(ItemStack i) {
 		if(i == null)
 			return false;
 		
 		switch(i.getTypeId()) {
 			case 256:
 			case 257:
 			case 258:
 			case 259:
 			case 261:
 			case 267:
 			case 268:
 			case 269:
 			case 270:
 			case 271:
 			case 272:
 			case 273:
 			case 274:
 			case 275:
 			case 276:
 			case 277:
 			case 278:
 			case 279:
 			case 283:
 			case 284:
 			case 285:
 			case 286:
 			case 290:
 			case 291:
 			case 292:
 			case 293:
 			case 294:
 			case 298:
 			case 299:
 			case 300:
 			case 301:
 			case 302:
 			case 303:
 			case 304:
 			case 305:
 			case 306:
 			case 307:
 			case 308:
 			case 309:
 			case 310:
 			case 311:
 			case 312:
 			case 313:
 			case 314:
 			case 315:
 			case 316:
 			case 317:
 			case 346:
 			case 359:
 				return true;
 		}
 		
 		return false;
 	}
 	
 	public static Boolean acceptBroken(ItemStack i) {
 		config conf = config.Obtain();
 		if(conf.getBoolean("GiantShop.global.AcceptBrokenTools") && isTool(i))
 			return true;
 		
 		return false;
 	}
 	
 	public static int hasAmount(Inventory inv, ItemStack item) {
 		MaterialData type = item.getData();
 		ArrayList<ItemStack> properStack = new ArrayList<ItemStack>();
 		int amount = 0;
 
 		HashMap<Integer, ? extends ItemStack> stacky = inv.all(item.getTypeId());
 		for(Map.Entry<Integer, ? extends ItemStack> stack : stacky.entrySet()) {
 			ItemStack tmp = stack.getValue();
 
 			if(type == null && tmp.getType() == null) {
 				properStack.add(tmp);
 				amount += tmp.getAmount();
 			}else{
 				if(item.getTypeId() == 373 && type != null && tmp.getData() != null && item.getDurability() == tmp.getDurability()) {
 					properStack.add(tmp);
 					amount += tmp.getAmount();
 				}else if(type != null && tmp.getData() != null && item.getDurability() == tmp.getDurability()) {
 					properStack.add(tmp);
 					amount += tmp.getAmount();
 				}else if(isTool(tmp) && acceptBroken(tmp)) {
 					properStack.add(tmp);
 					amount += tmp.getAmount();
 				}
 			}
 		}
 		
 		return amount;
 	}
 	
 	public static void removeItem(Inventory inventory, ItemStack item) {
 		int amt = item.getAmount();
 		ItemStack[] items = inventory.getContents();
 		for(int i = 0; i < items.length; i++) {
			if(items[i] != null && items[i].getTypeId() == item.getTypeId() && items[i].getDurability() == item.getDurability() || (isTool(items[i]) && acceptBroken(items[i]))) {
 				if(items[i].getAmount() > amt) {
 					items[i].setAmount(items[i].getAmount() - amt);
 					break;
 				}else if(items[i].getAmount() == amt) {
 					items[i] = null;
 					break;
 				}else{
 					amt -= items[i].getAmount();
 					items[i] = null;
 				}
 			}
 		}
 		
 		inventory.setContents(items);
 	}
 	
 }
