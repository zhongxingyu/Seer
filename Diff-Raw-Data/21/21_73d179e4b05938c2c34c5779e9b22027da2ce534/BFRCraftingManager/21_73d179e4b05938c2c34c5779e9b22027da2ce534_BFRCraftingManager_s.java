 package adanaran.mods.bfr.crafting;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.inventory.InventoryCrafting;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import adanaran.mods.bfr.BFR;
 import adanaran.mods.bfr.items.ItemCookwareBase;
 
 /**
  * @author Adanaran
  * 
  */
 public class BFRCraftingManager {
 
 	private static BFRCraftingManager instance = new BFRCraftingManager();
 
 	/** A list of all the recipes added */
 	private ArrayList<BFRRecipe> recipes = new ArrayList<BFRRecipe>();
 
 	private BFRCraftingManager() {
 		addRecipe(new ItemStack(Item.bread), BFR.itemCakePanIron,
 				Item.bucketWater, BFR.itemFlour);
 		addRecipe(new ItemStack(Item.beefCooked), BFR.itemPanIron, Item.beefRaw);
 		addRecipe(new ItemStack(Item.porkCooked), BFR.itemPanIron, Item.porkRaw);
 		addRecipe(new ItemStack(Item.fishCooked), BFR.itemPanIron, Item.fishRaw);
 		addRecipe(new ItemStack(Item.bowlSoup), BFR.itemPotIron,
 				Item.bowlEmpty, Block.mushroomBrown, Block.mushroomRed);
 		addRecipe(new ItemStack(Item.cake), BFR.itemCakePanIron,
 				Item.bucketMilk, Item.bucketMilk, Item.bucketMilk, Item.egg,
 				Item.sugar, Item.sugar, BFR.itemFlour, BFR.itemFlour,
 				BFR.itemFlour);
 		addRecipe(new ItemStack(Item.cookie, 8), BFR.itemCakePanIron,
				new ItemStack(Item.dyePowder, 1, 3), BFR.itemFlour,
 				BFR.itemFlour);
 		addRecipe(new ItemStack(Item.pumpkinPie), BFR.itemCakePanIron,
 				Block.pumpkin, Item.egg, Item.sugar);
 		addRecipe(new ItemStack(Item.chickenCooked), BFR.itemPanIron,
 				Item.chickenRaw);
		addRecipe(new ItemStack(Item.bakedPotato), BFR.itemPotIron, Item.potato);
 	}
 
 	public static BFRCraftingManager getInstance() {
 		return instance;
 	}
 
 	public void addRecipe(ItemStack result, ItemCookwareBase cookware,
 			Object... ingredients) {
 		ArrayList arraylist = new ArrayList();
 		Object[] aobject = ingredients;
 		int i = ingredients.length;
 		for (int j = 0; j < i; ++j) {
 			Object object1 = aobject[j];
 			if (object1 instanceof ItemStack) {
 				arraylist.add(((ItemStack) object1).copy());
 			} else if (object1 instanceof Item) {
 				arraylist.add(new ItemStack((Item) object1));
 			} else {
 				if (!(object1 instanceof Block)) {
 					throw new RuntimeException("Invalid shapeless recipy!");
 				}
 				arraylist.add(new ItemStack((Block) object1));
 			}
 		}
 		this.recipes.add(new BFRRecipe(result, cookware, arraylist));
 	}
 
 	public ItemStack findMatchingRecipe(
 			InventoryCrafting par1InventoryCrafting, World par2World,
 			ItemStack cookware) {
 		for (int j = 0; j < this.recipes.size(); ++j) {
 			BFRRecipe irecipe = (BFRRecipe) this.recipes.get(j);
 			if (irecipe.matches(par1InventoryCrafting, par2World, cookware)) {
 				return irecipe.getCraftingResult(par1InventoryCrafting);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * returns the List<> of all recipes
 	 */
 	public List getRecipeList() {
 		return this.recipes;
 	}
 }
