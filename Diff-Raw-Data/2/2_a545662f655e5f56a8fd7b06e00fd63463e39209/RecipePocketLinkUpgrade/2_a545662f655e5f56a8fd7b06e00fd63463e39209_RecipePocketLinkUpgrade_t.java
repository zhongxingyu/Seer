 package fuj1n.globalLinkMod.common.items.recipe;
 
 import net.minecraft.block.Block;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.inventory.InventoryCrafting;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.IRecipe;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.world.World;
 import fuj1n.globalLinkMod.GlobalChests;
 
 public class RecipePocketLinkUpgrade implements IRecipe{
 
 	@Override
 	public boolean matches(InventoryCrafting inventorycrafting, World world) {
 		ItemStack[] stacks = new ItemStack[inventorycrafting.getSizeInventory()];
 		for(int i = 0; i < stacks.length; i++){
 			stacks[i] = inventorycrafting.getStackInSlot(i);
 		}
 		for(int i = 0; i < stacks.length || stacks.length == 0; i++){
 			if(stacks.length == 0 || stacks[i] == null){
 				return false;
 			}
 		}
 		
		if(stacks.length > 4 && stacks[4] != null && stacks[4].getItem() == GlobalChests.pocketLink){
 			NBTTagList enchantments = stacks[4].getEnchantmentTagList();
 			NBTTagCompound nbt1 = null;
 			if(enchantments != null && enchantments.tagAt(0) != null){
 				nbt1 = (NBTTagCompound) enchantments.tagAt(0);
 			}
 			
 			Enchantment ench = null;
 			int enchantLevel = 0;
 			
 			if(nbt1 != null){
 				ench = Enchantment.enchantmentsList[nbt1.getShort("id")];
 				enchantLevel = nbt1.getShort("lvl");
 			}
 			if(ench == GlobalChests.enchantmentRange && enchantLevel > 0 || enchantLevel == 0){
 				switch(enchantLevel){
 				case 0:
 					return stacks[0].isItemEqual(new ItemStack(Item.ingotIron)) && stacks[1].isItemEqual(new ItemStack(Item.diamond)) && stacks[2].isItemEqual(new ItemStack(Item.ingotGold)) && stacks[3].isItemEqual(new ItemStack(Item.diamond)) && stacks[4].isItemEqual(new ItemStack(GlobalChests.pocketLink)) && stacks[5].isItemEqual(new ItemStack(Item.diamond)) && stacks[6].isItemEqual(new ItemStack(Item.ingotGold)) && stacks[7].isItemEqual(new ItemStack(Item.diamond)) && stacks[8].isItemEqual(new ItemStack(Item.ingotIron));
 				case 1:
 					return stacks[0].isItemEqual(new ItemStack(Block.blockIron)) && stacks[1].isItemEqual(new ItemStack(Item.diamond)) && stacks[2].isItemEqual(new ItemStack(Block.blockGold)) && stacks[3].isItemEqual(new ItemStack(Item.diamond)) && stacks[4].isItemEqual(new ItemStack(GlobalChests.pocketLink)) && stacks[5].isItemEqual(new ItemStack(Item.diamond)) && stacks[6].isItemEqual(new ItemStack(Block.blockGold)) && stacks[7].isItemEqual(new ItemStack(Item.diamond)) && stacks[8].isItemEqual(new ItemStack(Block.blockIron));
 				case 2:
 					return stacks[0].isItemEqual(new ItemStack(Item.diamond)) && stacks[1].isItemEqual(new ItemStack(Item.diamond)) && stacks[2].isItemEqual(new ItemStack(Item.diamond)) && stacks[3].isItemEqual(new ItemStack(Block.blockDiamond)) && stacks[4].isItemEqual(new ItemStack(GlobalChests.pocketLink)) && stacks[5].isItemEqual(new ItemStack(Block.blockDiamond)) && stacks[6].isItemEqual(new ItemStack(Item.diamond)) && stacks[7].isItemEqual(new ItemStack(Item.diamond)) && stacks[8].isItemEqual(new ItemStack(Item.diamond));
 				case 3:
 					return stacks[0].isItemEqual(new ItemStack(Block.blockDiamond)) && stacks[1].isItemEqual(new ItemStack(Item.netherStar)) && stacks[2].isItemEqual(new ItemStack(Block.blockDiamond)) && stacks[3].isItemEqual(new ItemStack(Item.diamond)) && stacks[4].isItemEqual(new ItemStack(GlobalChests.pocketLink)) && stacks[5].isItemEqual(new ItemStack(Item.diamond)) && stacks[6].isItemEqual(new ItemStack(Block.blockDiamond)) && stacks[7].isItemEqual(new ItemStack(Item.netherStar)) && stacks[8].isItemEqual(new ItemStack(Block.blockDiamond));
 				case 4:
 					return stacks[0].isItemEqual(new ItemStack(Item.netherStar)) && stacks[1].isItemEqual(new ItemStack(Block.blockDiamond)) && stacks[2].isItemEqual(new ItemStack(Item.netherStar)) && stacks[3].isItemEqual(new ItemStack(Block.blockDiamond)) && stacks[4].isItemEqual(new ItemStack(GlobalChests.pocketLink)) && stacks[5].isItemEqual(new ItemStack(Block.blockDiamond)) && stacks[6].isItemEqual(new ItemStack(Item.netherStar)) && stacks[7].isItemEqual(new ItemStack(Block.dirt)) && stacks[8].isItemEqual(new ItemStack(Item.netherStar));
 				default:
 					return false;
 				}
 			}
 			else{
 				return false;
 			}
 		}else{
 			return false;
 		}
 	}
 
 	@Override
 	public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
 		ItemStack[] stacks = new ItemStack[inventorycrafting.getSizeInventory()];
 		for(int i = 0; i < stacks.length; i++){
 			stacks[i] = inventorycrafting.getStackInSlot(i);
 		}
 		
 		NBTTagList enchantments = stacks[4].getEnchantmentTagList();
 		NBTTagCompound nbt1 = null;
 		if(enchantments != null && enchantments.tagAt(0) != null){
 			nbt1 = (NBTTagCompound) enchantments.tagAt(0);
 		}
 		Enchantment ench = null;
 		int enchantLevel = 0;
 		if(nbt1 != null){
 			ench = Enchantment.enchantmentsList[nbt1.getShort("id")];
 			enchantLevel = nbt1.getShort("lvl");
 		}
 		ItemStack is = new ItemStack(GlobalChests.pocketLink);
 		is.addEnchantment(GlobalChests.enchantmentRange, enchantLevel + 1);
 		return is;
 	}
 
 	@Override
 	public int getRecipeSize() {
 		return 3;
 	}
 
 	@Override
 	public ItemStack getRecipeOutput() {
 		return new ItemStack(GlobalChests.pocketLink, 0, 1);
 	}
 
 }
