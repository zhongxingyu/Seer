 package com.github.soniex2.endermoney.core;
 
 import java.util.ArrayList;
 import java.util.Arrays;
import java.util.Collections;
import java.util.List;
 
 import net.minecraft.inventory.InventoryCrafting;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.IRecipe;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class CoinRecipe implements IRecipe {
 
 	private ItemStack output;
	private List<int[]> recipes = new ArrayList<int[]>();
 
 	public CoinRecipe(Item enderCoin, int coinMeta) {
 		output = new ItemStack(enderCoin, 1, coinMeta);
 	}
 
 	@Override
 	public boolean matches(InventoryCrafting ctInv, World world) {
 		int size = ctInv.getSizeInventory();
 		int a = 0;
 		for (int x = 0; x < size; x++) {
 			ItemStack s = ctInv.getStackInSlot(x);
 			if (s.getItem() != output.getItem())
 				return false;
 			int m = s.getItemDamage();
 			if (m > a)
 				a = m;
 		}
 		int[] b = new int[a];
 		for (int x = 0; x < size; x++) {
 			int m = ctInv.getStackInSlot(x).getItemDamage();
 			b[m] = b[m] + 1;
 		}
 		for (int[] recipe : recipes) {
 			if (Arrays.equals(recipe, b)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public ItemStack getCraftingResult(InventoryCrafting ctInv) {
 		int size = ctInv.getSizeInventory();
 		int a = 0;
 		for (int x = 0; x < size; x++) {
 			ItemStack s = ctInv.getStackInSlot(x);
 			if (s.getItem() != output.getItem())
 				return null;
 			int m = s.getItemDamage();
 			if (m > a)
 				a = m;
 		}
 		int[] b = new int[a];
 		for (int x = 0; x < size; x++) {
 			int m = ctInv.getStackInSlot(x).getItemDamage();
 			b[m] = b[m] + 1;
 		}
 		for (int[] recipe : recipes) {
 			if (Arrays.equals(recipe, b)) {
 				return output.copy();
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public int getRecipeSize() {
 		return 9;
 	}
 
 	@Override
 	public ItemStack getRecipeOutput() {
 		return output;
 	}
 
 	public void addRecipe(int[] recipe) {
 		int a = 0;
 		for (int m : recipe) {
 			if (m > a)
 				a = m;
 		}
 		int[] b = new int[a];
 		for (int m : recipe) {
 			b[m] = b[m] + 1;
 		}
 		recipes.add(b);
 	}
 
 	public void register() {
		recipes = Collections.unmodifiableList(recipes);
 		if (recipes.size() > 0) {
 			GameRegistry.addRecipe(this);
 		}
 	}
 
 }
