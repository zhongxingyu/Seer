 package com.circuitlocution.alchemicalcauldron;
 import java.util.logging.Logger;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.bukkit.entity.MobType;
 import org.bukkit.util.config.ConfigurationNode;
 
 
 public class Recipe extends Object {
 	private final Logger log = Logger.getLogger("Minecraft_alchemical_cauldron");
 
 	protected Material reagent1;
 	protected byte reagent1_data = -1; //cast to byte in advance, for speed
 	protected Material reagent2;
 	protected byte reagent2_data = -1; //cast to byte in advance, for speed
 	protected Material reagent3;
 	protected byte reagent3_data = -1; //cast to byte in advance, for speed
 	protected boolean reagent3_consumed = true; //normally eats one of the thing you're hitting with
 	
 	protected String product_type; //todo: enum?  one of "item", "block" or "mob"
 	protected byte product_data = -1;
 	protected Material product = null;
 	protected int product_quantity = 1;
 	protected MobType product_mob = null;
 	
 	public String getProductName(){
 		if (product_type.equals("mob")){
 			return product_mob.getName();
 		} else {
 			return product.name();
 		}
 	}
 	
 	
 	public Recipe(ConfigurationNode recipe){ //whatever type we can get for this
 		
 		product_type = recipe.getString("type", "block");
 		
 		if(product_type.equals("block") || product_type.equals("item")){
 			product = Material.matchMaterial(recipe.getString("product", "ERROR"));
 			if (product == null){
 				log.warning("Product of recipe not found for string: " + recipe.getString("product", "<empty>"));
 			}
 		} else if (product_type.equals("mob")){
 			product_mob = MobType.fromName(recipe.getString("product", "ERROR"));
 			if (product_mob == null){
 				log.warning("Product of recipe not found for string: " + recipe.getString("product", "<empty>"));
 			}
 		}
 		
 		reagent1 = Material.matchMaterial(recipe.getString("reagent1", ""));
 		if (reagent1 == null){
 			log.warning("In recipe for " + product + ", reagent1 isn't valid: " + recipe.getString("reagent1", "<empty>"));
 		}
 		
 		reagent2 = Material.matchMaterial(recipe.getString("reagent2", ""));
 		if (reagent2 == null){
 			log.warning("In recipe for " + product + ", reagent2 isn't valid: " + recipe.getString("reagent2", "<empty>"));
 		}
 		reagent3 = Material.matchMaterial(recipe.getString("reagent3", ""));
 		if (reagent3 == null){
 			log.warning("In recipe for " + product + ", reagent3 isn't valid: " + recipe.getString("reagent3", "<empty>"));
 		}
 
 		String r1_data = recipe.getString("reagent1_data", null);
 		String r2_data = recipe.getString("reagent2_data", null);
 		String r3_data = recipe.getString("reagent3_data", null);
 		String p_data = recipe.getString("product_data", null);
 		
 		if (r1_data != null){
 			if (reagent1 == Material.INK_SACK){
 				reagent1_data = (byte) (15 - DyeColor.valueOf(r1_data).getData());
 			} else if (reagent1 == Material.WOOL){
 				reagent1_data = (byte) (DyeColor.valueOf(r1_data).getData());
 			}
 			
 		}
 		if (r2_data != null){
 			if (reagent2 == Material.INK_SACK){
 				reagent2_data = (byte) (15 - DyeColor.valueOf(r2_data).getData());
 			} else if (reagent2 == Material.WOOL){
 				reagent2_data = (byte) (DyeColor.valueOf(r2_data).getData());
 			}
 			
 		}
 		if (r3_data != null){
 			if (reagent3 == Material.INK_SACK){
 				reagent3_data = (byte) (15 - DyeColor.valueOf(r3_data).getData());
 			} else if (reagent3 == Material.WOOL){
 				reagent3_data = (byte) (DyeColor.valueOf(r3_data).getData());
 			}
 		}
 		if (p_data != null){
 			if (product == Material.INK_SACK){
 				product_data = (byte) (15 - DyeColor.valueOf(p_data).getData());
 			} else if (product == Material.WOOL){
 				product_data = (byte) (DyeColor.valueOf(p_data).getData());
 			}
 		}
 		
 		return;
 	}
 	
 	public String toString(){
 		String str = "" + reagent1.name();
 		if (reagent1_data > -1){
 			str += reagent1_data;
 		}
 		str += "_" + reagent2.name();
 		if (reagent2_data > -1){
 			str += reagent2_data;
 		}
 		str += "_" + reagent3.name();
 		if (reagent3_data > -1){
 			str += reagent3_data;
 		}
 		
 		return str;
 	}
 
 	public boolean isValid(){
 		if (reagent1 == null || reagent2 == null || reagent3 == null){
 			return false;
 		}
 		if (product == null && product_mob == null){
 			return false;
 		}
 		return true;
 	}
 	
 }
 
 
 
 
