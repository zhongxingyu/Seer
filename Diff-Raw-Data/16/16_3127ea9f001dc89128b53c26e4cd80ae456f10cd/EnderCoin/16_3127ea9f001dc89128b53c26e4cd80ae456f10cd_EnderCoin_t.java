 package com.github.soniex2.endermoney.core.item;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 import com.github.soniex2.endermoney.core.CoinRecipe;
 import com.github.soniex2.endermoney.core.EnderMoney;
 
 import net.minecraft.client.renderer.texture.IIconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 public class EnderCoin extends Item {
 
 	private int[] metadata;
 
 	private Random rand = new Random();
 
 	public EnderCoin(int[] values) {
 		super();
 		setMaxStackSize(64);
 		setCreativeTab(EnderMoney.tab);
 		setUnlocalizedName("endermoneycore.endercoin");
 		this.setHasSubtypes(true);
 		this.setTextureName("EnderCoin");
 		metadata = Arrays.copyOf(values, values.length);
 	}
 
 	public void registerRecipes() {
 		for (int x = 0; x < metadata.length; x++) {
 			CoinRecipe handler = new CoinRecipe(this, x);
 			registerRecipes(metadata[x], 0, new int[9], handler);
			handler.register();
 		}
 	}
 
 	private void registerRecipes(int left, int index, int[] recipe,
 			CoinRecipe handler) {
 		if (left > 0) {
 			if (index >= 9)
 				return;
 			for (int x = 0; x < metadata.length; x++) {
 				int v = metadata[x];
 				if ((index == 0 || v >= metadata[recipe[index - 1]]) && v <= left) {
 					recipe[index] = x;
 					registerRecipes(left - v, index + 1, recipe, handler);
 				}
 			}
 		} else if (left == 0) {
 			handler.addRecipe(Arrays.copyOf(recipe, index));
 		}
 	}
 
 	@Override
 	public int getColorFromItemStack(ItemStack is, int pass) {
 		int v = is.getItemDamage();
 		rand.setSeed(v);
 		int r = 255 - rand.nextInt(224) & 255;
 		int g = 255 - rand.nextInt(224) & 255;
 		int b = 255 - rand.nextInt(224) & 255;
 		return r << 16 | g << 8 | b;
 	}
 
 	@Override
 	public String getItemStackDisplayName(ItemStack is) {
 		int v = is.getItemDamage();
 		if (v >= 0 && v < metadata.length)
 			return "$" + metadata[v] + " EnderCoin";
 		else
 			return "INVALID";
 	}
 
 	@Override
 	public void registerIcons(IIconRegister ireg) {
 		itemIcon = ireg.registerIcon("endermoneycore:coin");
 	}
 
 	@Override
 	public void func_150895_a(Item item, CreativeTabs tab, List list) {
 
 	}
 }
