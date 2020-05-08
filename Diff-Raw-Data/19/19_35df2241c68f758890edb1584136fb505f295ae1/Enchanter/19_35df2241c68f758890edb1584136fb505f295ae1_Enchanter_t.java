 package com.github.Holyvirus.Blacksmith.core.Tools.Enchanter;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.github.Holyvirus.Blacksmith.BlackSmith;
 import com.github.Holyvirus.Blacksmith.Listeners.ChatListener;
 import com.github.Holyvirus.Blacksmith.core.config;
 import com.github.Holyvirus.Blacksmith.core.Eco.iEco;
 import com.github.Holyvirus.Blacksmith.core.Tools.Cost.Cost;
 import com.github.Holyvirus.Blacksmith.core.perms.PermHandler;
 import com.github.Holyvirus.Blacksmith.core.perms.Permission;
 
 public class Enchanter {
 
 	private static boolean FC = true;
 	private static iEco eH;
 	private static HashMap<String, ItemStack> item = new HashMap<String, ItemStack>();
 	private static HashMap<String,HashMap<Enchantment, Integer>> enchants = new HashMap<String,HashMap<Enchantment, Integer>>();
 	
 	private static void init() {	
 		FC = false;
 		if(null != BlackSmith.getPlugin().getEcoHandler())
 			eH = BlackSmith.getPlugin().getEcoHandler().getEngine();
 	}
 	
	public static Boolean add(Player p, ItemStack i){
		if(eH == null)
			return false;
		
 		item.put(p.getName(), i);
		return true;
 	}
 	
 	  public static boolean validateMsg1(String m){
 	    return (m.equalsIgnoreCase("Protection")) || (m.equalsIgnoreCase("Fire_Protection")) || (m.equalsIgnoreCase("Feather_Fall")) || (m.equalsIgnoreCase("Blast_Protection")) || 
 	      (m.equalsIgnoreCase("Projectile_Protection")) || (m.equalsIgnoreCase("Respiration")) || (m.equalsIgnoreCase("Aqua_Affinity")) || 
 	      (m.equalsIgnoreCase("Sharpness")) || (m.equalsIgnoreCase("Smite")) || (m.equalsIgnoreCase("Bane_of_Arthropods")) || (m.equalsIgnoreCase("Knockback")) || 
 	      (m.equalsIgnoreCase("Fire_Aspect")) || (m.equalsIgnoreCase("Looting")) || (m.equalsIgnoreCase("Efficiency")) || (m.equalsIgnoreCase("Silk_Touch")) || 
 	      (m.equalsIgnoreCase("Unbreaking")) || (m.equalsIgnoreCase("Fortune")) || (m.equalsIgnoreCase("Power")) || (m.equalsIgnoreCase("Punch")) || (m.equalsIgnoreCase("Flame")) || 
 	      (m.equalsIgnoreCase("Infinity"));
 	  }
 
 	  public static void enchantValidate(Player p, String e, int rlvl){//shut up for now :P
 		  HashMap<Enchantment, Integer> fen = new HashMap<Enchantment, Integer>();
 		  Permission pH = BlackSmith.getPlugin().getPermHandler().getEngine();
 		  ItemStack i = null;
 		  int lvl = 0;
 		  p.sendMessage("e: " + e.toUpperCase());
 		  Enchantment en = Enchantment.getById(getEnchantId(e));
 		  p.sendMessage("en: " + en);
 	      if (p.getItemInHand().getTypeId() == ((ItemStack)item.get(p.getName())).getTypeId()) {
 	    	  i = p.getItemInHand();
 		      if (i.getAmount() == 1) {
 		    	  p.sendMessage("i = : " + i);
 		    	  if (i.containsEnchantment(en)) {
 		    		  int plvl = i.getEnchantmentLevel(en);
 		    		  lvl = plvl + rlvl;
 		    	  }else{
 		    		  lvl = rlvl;
 		    	  }
 		    	  fen.put(en, lvl);
 		    	  enchants.put(p.getName(), fen);
 		    	  ChatListener.add(p, 2);
 				  p.sendMessage("lvl: " + lvl);
 		    	  p.sendMessage("The enchant will cost you: " + getEnchantCost(p, e.toLowerCase(), rlvl) + "! Should I continue? Please type \"yes\" or \"no\"!");
 	      }else{
 	        p.sendMessage(ChatColor.RED + "You have more then one item in your slot, please try again!");
 	      }
 	    } else {
 	      p.sendMessage("iteminhand: " + p.getItemInHand());
 	      p.sendMessage("item.get: " + item.get(p.getName()));
 	    }
 	  }
 
 	  public static void enchant(Player p, String e){
 		  Permission pH = BlackSmith.getPlugin().getPermHandler().getEngine();
 		  Iterator<Enchantment> mp;
 		  Enchantment en = null;
 		  int lvl = 0;
 		  ItemStack i = p.getItemInHand();
 		  try{
 			  mp = enchants.get(p.getName()).keySet().iterator();
 			  en = mp.next();
 			  lvl = enchants.get(p.getName()).get(en);
 		  }catch(Exception ex){
 			  ex.printStackTrace();
 		  }
 		  config conf = config.Obtain();
 			if(FC)
 				init();
 			
 			double b = eH.getBalance(p);
 			double cost = getEnchantCost(p, e, lvl);
 			
 			p.sendMessage("e: " + e.toLowerCase());
 			p.sendMessage("cost: " + cost);
 			
 			if(b > cost) {
 				eH.withdraw(p, cost);
 				
 		  p.sendMessage("nen: " + en);
 		  p.sendMessage("nlvl: " + lvl);
 		  	  try{
 		  		  i.addEnchantment(en, lvl);
 				  ChatListener.remove(p);
 	    	  }catch(IllegalArgumentException ex){
 	    		  if(pH.has(p, "blacksmith.enchant.unsafe")){
 	    			  i.addUnsafeEnchantment(en, lvl);
 	      			  ChatListener.remove(p);
 	      			}else{
 	        			p.sendMessage("You are not allowed to enchant unsafely, try again!");
 	        			ChatListener.add(p, 1);
 	        		}
 	    	  }
 		  }
 	  }
 	  
 	  public static int getEnchantCost(Player p, String e, int lvl){
 		  Permission pH = BlackSmith.getPlugin().getPermHandler().getEngine();
 		  config conf = config.Obtain();
 		  int c = 0;
 		  try{
 			  c = conf.getInt("BlackSmith.EnchantmentBases." + e);
 		  }catch(Exception ex){
 			  p.sendMessage("Whoops, the price for this enchant is not defined correctly! Please inform an admin immediatly");
 			  ChatListener.remove(p);
 		  }
 		  p.sendMessage("rawcost: " + c);
 		  return pH.has(p, "blacksmith.enchant.free") ? 0 : c * lvl;
 	  }
 	  
 	  private static int getEnchantId(String e) {
 	    if (e.equalsIgnoreCase("Protection"))
 	      return 0;
 	    if (e.equalsIgnoreCase("Fire_Protection"))
 	      return 1;
 	    if (e.equalsIgnoreCase("Feather_Fall"))
 	      return 2;
 	    if (e.equalsIgnoreCase("Blast_Protection"))
 	      return 3;
 	    if (e.equalsIgnoreCase("Projectile_Protection"))
 	      return 4;
 	    if (e.equalsIgnoreCase("Respiration"))
 	      return 5;
 	    if (e.equalsIgnoreCase("Aqua_Affinity"))
 	      return 6;
 	    if (e.equalsIgnoreCase("Sharpness"))
 	      return 16;
 	    if (e.equalsIgnoreCase("Smite"))
 	      return 17;
 	    if (e.equalsIgnoreCase("Bane_of_Arthropods"))
 	      return 18;
 	    if (e.equalsIgnoreCase("Knockback"))
 	      return 19;
 	    if (e.equalsIgnoreCase("Fire_Aspect"))
 	      return 20;
 	    if (e.equalsIgnoreCase("Looting"))
 	      return 21;
 	    if (e.equalsIgnoreCase("Efficiency"))
 	      return 32;
 	    if (e.equalsIgnoreCase("Silk_Touch"))
 	      return 33;
 	    if (e.equalsIgnoreCase("Unbreaking"))
 	      return 34;
 	    if (e.equalsIgnoreCase("Fortune"))
 	      return 35;
 	    if (e.equalsIgnoreCase("Power"))
 	      return 48;
 	    if (e.equalsIgnoreCase("Punch"))
 	      return 49;
 	    if (e.equalsIgnoreCase("Flame"))
 	      return 50;
 	    if (e.equalsIgnoreCase("Infinity")) {
 	      return 51;
 	    }
 	    return 901;
 	  }
 }
