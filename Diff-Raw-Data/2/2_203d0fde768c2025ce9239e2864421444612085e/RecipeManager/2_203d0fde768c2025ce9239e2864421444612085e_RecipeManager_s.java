 package team.GunsPlus.Manager;
 
 import java.util.List;
 
 import me.znickq.furnaceapi.SpoutFurnaceRecipe;
 import me.znickq.furnaceapi.SpoutFurnaceRecipes;
 
 import org.bukkit.Bukkit;
 import org.bukkit.inventory.FurnaceRecipe;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.inventory.SpoutShapedRecipe;
 import org.getspout.spoutapi.inventory.SpoutShapelessRecipe;
 import org.getspout.spoutapi.material.MaterialData;
 
 import team.GunsPlus.GunsPlus;
 import team.GunsPlus.Util.Util;
 
 public class RecipeManager {
 	
 	public enum RecipeType{
 		SHAPED, SHAPELESS, FURNACE;
 	}
 	
 	public static void addRecipe(RecipeType type, List<ItemStack> ingredients, ItemStack result) throws Exception{
 		if(type.equals(RecipeType.SHAPED)&&ingredients.size()!=9) throw new Exception( "Number of ingredients in a shaped recipe must be 9!");
 		if(type.equals(RecipeType.FURNACE)&&(ingredients.size()==0||ingredients.get(0)==null)) throw new Exception( "You need at least one ingredient for a furnace recipe!");
		boolean useSpout = Util.containsCustomItems(ingredients)||Util.isCustomItem(result);
 		switch(type){
 			case SHAPED:
 				addShapedRecipe(ingredients, result, useSpout);
 			case SHAPELESS:
 				addShapelessRecipe(ingredients, result, useSpout);
 			case FURNACE:
 				addFurnaceRecipe(ingredients.get(0), result, useSpout);
 		}
 	}
 	
 	public static void addRecipe(String type, List<ItemStack> ingredients, ItemStack result) throws Exception{
 		try{
 			addRecipe(RecipeType.valueOf(type.toUpperCase()), ingredients, result);
 		}catch(Exception e){
 			Util.warn(" The recipe type "+type+" is invalid!");
 			Util.debug(e);
 		}
 	}
 	
 	private static void addShapedRecipe(List<ItemStack> ingredients, ItemStack result, boolean spout){
 		char[] name = {'a','b','c',
 						'd','e','f',
 						'g','h','i'};
 		int i = 0;
 		if(spout){
 			SpoutShapedRecipe x = (SpoutShapedRecipe)new SpoutShapedRecipe(result);
 			x.shape("abc","def","ghi");
 			for(ItemStack item : ingredients) {
 				if(item.getTypeId()==0){
 					i++;
 					continue;
 				}
 				SpoutItemStack ingred = new SpoutItemStack(item);
 				x.setIngredient(name[i], MaterialData.getMaterial(ingred.getTypeId(),(short)ingred.getDurability()));
 				i++;
 			}
 			SpoutManager.getMaterialManager().registerSpoutRecipe(x);
 		}
 		else{
 			ShapedRecipe x = (ShapedRecipe)new ShapedRecipe(result);
 			x.shape("abc", "def", "ghi");
 			for(ItemStack item : ingredients) {
 				if(item.getTypeId()==0){
 					i++;
 					continue;
 				}
 				ItemStack ingred = new ItemStack(item);
 				x.setIngredient(name[i], ingred.getType());
 				i++;
 			}
 			Bukkit.addRecipe(x);
 		}
 	}
 	
 	private static void addShapelessRecipe(List<ItemStack> ingredients, ItemStack result, boolean spout){
 		if(spout){
 			SpoutShapelessRecipe x = new SpoutShapelessRecipe(result);
 			for(ItemStack item : ingredients){
 				SpoutItemStack ingred = new SpoutItemStack(item);
 				x.addIngredient(MaterialData.getMaterial(ingred.getTypeId(),ingred.getDurability()));
 			}
 			SpoutManager.getMaterialManager().registerSpoutRecipe(x);
 		}else{
 			ShapelessRecipe x = new ShapelessRecipe(result);
 			for(ItemStack item : ingredients){
 				ItemStack ingred = new ItemStack(item);
 				x.addIngredient(ingred.getType());
 			}
 			Bukkit.addRecipe(x);
 		}
 	}
 	
 	private static void addFurnaceRecipe(ItemStack input, ItemStack result, boolean spout){
 		if(spout){
 			if(GunsPlus.useFurnaceAPI) {
 				SpoutFurnaceRecipe x = new SpoutFurnaceRecipe(new SpoutItemStack(input), new SpoutItemStack(result));
 				SpoutFurnaceRecipes.registerSpoutRecipe(x);
 			}
 		}else{
 			FurnaceRecipe x = new FurnaceRecipe(new ItemStack(result), new ItemStack(input).getType() );
 			Bukkit.addRecipe(x);
 		}
 	}
 }
