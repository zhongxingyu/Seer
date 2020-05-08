 package com.github.Holyvirus.Blacksmith.core.Tools.Cost;
 
 import com.github.Holyvirus.Blacksmith.BlackSmith;
 import com.github.Holyvirus.Blacksmith.core.Eco.mEco;
 import com.github.Holyvirus.Blacksmith.core.Items.ItemID;
 import com.github.Holyvirus.Blacksmith.core.Tools.Materials.Materials;
 
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 
 public class MaterialEn {
 
 	private static boolean FC = true;
 	private static mEco mE;
 	private static Materials mH;
 	
 	private static void init() {
 		FC = false;
 		mH = Materials.getInstance();
 		
 		if(null != BlackSmith.getPlugin().getMatEngine())
 			mE = BlackSmith.getPlugin().getMatEngine();
 	}
 	
 	public static Boolean useMatEn() {
 		if(FC)
 			init();
 		
 		return mE != null;
 	}
 	
 	public static List<String> calcCost(ItemStack i, Boolean asString) {
 		if(!asString) {} // I hate warnings :)
 			
 		if(FC)
 			init();
 		
 		List<String> s = new ArrayList<String>();
 		
 		if(i == null) {
 			s.add("No item not given!");
 			return s;
 		}
 		
 		String item = BlackSmith.getPlugin().getItemHandler().getItemNameByID(i.getTypeId()).replaceAll(" ", "_");
 		s = mH.getCostString(item);
 		
 		if(i.getEnchantments().size() > 0) {
 			HashMap<ItemID, Integer> t = mH.getCostMap("enchantmentModifier");
 			HashMap<String, Integer> tmp = new HashMap<String, Integer>();
 			for(Map.Entry<Enchantment, Integer> entry : i.getEnchantments().entrySet()) {
 				int lvl = entry.getValue();
 				for(Map.Entry<ItemID, Integer> e : t.entrySet()) {
 					ItemID iID = e.getKey();
 					String n = BlackSmith.getPlugin().getItemHandler().getItemNameByID(iID.getId(), iID.getType());
 					if(!tmp.containsKey(n)) {
 						tmp.put(n, e.getValue() * lvl);
 					}else{
 						tmp.put(n, tmp.get(n) + (e.getValue() * lvl));
 					}
 				}
 			}
 			
 			for(Map.Entry<String, Integer> e : tmp.entrySet()) {
 				StringBuilder ss = new StringBuilder();
 				ss.append(e.getValue());
 				ss.append(" of " +  e.getKey());
 				s.add(ss.toString());
 			}
 		}
 		
 		return s;
 	}
 	
 	public static Boolean hasEnough(Player p, ItemStack i) {
 		if(FC)
 			init();
 		
 		if(mE == null)
 			return false;
 		
 		String item = BlackSmith.getPlugin().getItemHandler().getItemNameByID(i.getTypeId()).replaceAll(" ", "_");
 		HashMap<ItemID, Integer> t = mH.getCostMap(item);
 		
 		int c = 0;
 		int s = t.size();
 		
 		for(Map.Entry<ItemID, Integer> e : t.entrySet()) {
 			ItemStack a = new ItemStack(e.getKey().getId(), (e.getKey().getType() == null ? 0 : e.getKey().getType()));
 			if(mE.getBalance(p, a) >= e.getValue())
 				c++;
 		}
 		
 		
 		if(i.getEnchantments().size() > 0) {
 			HashMap<ItemID, Integer> m = mH.getCostMap("enchantmentModifier");
 			for(Map.Entry<Enchantment, Integer> entry : i.getEnchantments().entrySet()) {
 				int lvl = entry.getValue();
 				for(Map.Entry<ItemID, Integer> e : m.entrySet()) {
 					s++;
 					ItemStack a = new ItemStack(e.getKey().getId(), (e.getKey().getType() == null ? 0 : e.getKey().getType()));
 					
 					if(mE.getBalance(p, a) >= (e.getValue() * lvl))
 						c++;
 				}
 			}
 		}
 		
 		return c == s;
 	}
 	
 	public static void take(Player p, ItemStack i) {
 		if(FC)
 			init();
 		
 		if(mE == null)
 			return;
 		
 		String item = BlackSmith.getPlugin().getItemHandler().getItemNameByID(i.getTypeId()).replaceAll(" ", "_");
 		HashMap<ItemID, Integer> t = mH.getCostMap(item);
 		
 		for(Map.Entry<ItemID, Integer> e : t.entrySet()) {
 			ItemStack a = new ItemStack(e.getKey().getId(), (e.getKey().getType() == null ? 0 : e.getKey().getType()));
 			
 			if(mE.getBalance(p, a) >= e.getValue())
 				mE.withdraw(p, a, e.getValue());
 		}
 		
 		if(i.getEnchantments().size() > 0) {
 			HashMap<ItemID, Integer> m = mH.getCostMap("enchantmentModifier");
 			t.putAll(m);
 			for(Map.Entry<Enchantment, Integer> entry : i.getEnchantments().entrySet()) {
 				int lvl = entry.getValue();
 				for(Map.Entry<ItemID, Integer> e : m.entrySet()) {
 					ItemStack a = new ItemStack(e.getKey().getId(), (e.getKey().getType() == null ? 0 : e.getKey().getType()));
 					
 					if(mE.getBalance(p, a) >= (e.getValue() * lvl))
 						mE.withdraw(p, a, (e.getValue() * lvl));
 				}
 			}
 		}
 	}
 }
