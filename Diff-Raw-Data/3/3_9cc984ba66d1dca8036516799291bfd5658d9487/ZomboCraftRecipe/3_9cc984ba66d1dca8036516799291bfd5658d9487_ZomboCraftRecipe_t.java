 package me.ayan4m1.plugins.zombo;
 
 import java.util.ArrayList;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class ZomboCraftRecipe {
 	private ArrayList<ItemStack> 	reagents = new ArrayList<ItemStack>();
 	private ArrayList<String>	    enchants = new ArrayList<String>();
 	private Material				outputType = Material.AIR;
 	private String					name = "";
 	private Integer					xpCost = 0;
 
 	public ArrayList<ItemStack> getReagents() {
 		return reagents;
 	}
 
 	public void setReagents(ArrayList<ItemStack> reagents) {
 		this.reagents = reagents;
 	}
 
 	public ArrayList<String> getEnchants() {
 		return enchants;
 	}
 
 	public void setEnchants(ArrayList<String> outputEffects) {
 		this.enchants = outputEffects;
 	}
 
 	public Material getOutputType() {
 		return outputType;
 	}
 
 	public void setOutputType(Material outputType) {
 		this.outputType = outputType;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Integer getXpCost() {
 		return xpCost;
 	}
 
 	public void setXpCost(Integer xpCost) {
 		this.xpCost = xpCost;
 	}
 
 	public ZomboCraftRecipe() {
 		this.outputType = Material.AIR;
 		this.enchants = new ArrayList<String>();
 		this.reagents = new ArrayList<ItemStack>();
 	}
 
 	public ZomboCraftRecipe(Material outputType, ArrayList<ItemStack> reagents) {
 		this.outputType = outputType;
 		this.enchants = new ArrayList<String>();
 		this.reagents = reagents;
 	}
 
 	public ZomboCraftRecipe(Material outputType, ArrayList<String> enchants, ArrayList<ItemStack> reagents) {
 		this.outputType = outputType;
 		this.enchants = enchants;
 		this.reagents = reagents;
 	}
 
 	public void addEnchant(String enchantmentName) {
 		this.enchants.add(enchantmentName);
 	}
 
 	public boolean craftable(Inventory inventory) {
 		for(ItemStack item : reagents) {
 			if (item != null && inventory.contains(item)) {
 				return false;
 			}
 		}
		if (!inventory.contains(outputType)) {
			return false;
		}
 		return true;
 	}
 }
