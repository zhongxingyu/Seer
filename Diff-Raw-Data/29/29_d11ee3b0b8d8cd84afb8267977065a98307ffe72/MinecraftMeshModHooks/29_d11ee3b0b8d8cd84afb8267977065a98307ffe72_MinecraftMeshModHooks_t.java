 package org.minecraftmesh.server;
 
 import java.util.ArrayList;
 
 import net.minecraft.src.Block;
import net.minecraft.src.CraftingManager;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
 
 
 public class MinecraftMeshModHooks
 {
 	protected static ArrayList<Block> moddedBlocks = new ArrayList<Block>();
 	protected static ArrayList<Item> moddedItems = new ArrayList<Item>();
 	
	public static void addRecipe(ItemStack itemstack, Object o[])
    {
        CraftingManager.getInstance().addRecipe(itemstack, o);
    }

    public static void addShapelessRecipe(ItemStack itemstack, Object o[])
    {
        CraftingManager.getInstance().addShapelessRecipe(itemstack, o);
    }
	
 	public static void addBlockWithSpecialBlockItem(Block block, ItemBlock itemblock)
 	{
 		try
 		{
 			if(block == null)
 			{
 				MinecraftMesh.getLogger().severe("Block could't be registered because it was null.");
 				return;
 				
 			} else if(block.blockID >= Block.blocksList.length)
 			{
 				MinecraftMesh.getLogger().severe("Block: \"" + block.getBlockName() + "\" (" + block.blockID + ") couldn't be registered because the ID was out of range.");
 				return;
 			}
 			
 			moddedBlocks.add(block);
 			moddedItems.add(itemblock);
 			Item.itemsList[block.blockID] = itemblock;
 		
 		} catch(Exception e) {	e.printStackTrace();	}
 	}
 	
 	public static void addBlock(Block block)
 	{
 		try
 		{
 			if(block == null)
 			{
 				MinecraftMesh.getLogger().severe("Block could't be registered because it was null.");
 				return;
 				
 			} else if(block.blockID >= Block.blocksList.length)
 			{
 				MinecraftMesh.getLogger().severe("Block: \"" + block.getBlockName() + "\" (" + block.blockID + ") couldn't be registered because the ID was out of range.");
 				return;
 			}
 			
 			ItemBlock item = new ItemBlock(block.blockID - 256);
 			
 			moddedBlocks.add(block);
 			moddedItems.add(item);
 			Item.itemsList[block.blockID] = item;
 			
 		} catch(Exception e) {	e.printStackTrace();	}
 	}
 	
 	public static void addItem(Item item)
 	{
 		try
 		{
 			if(item == null)
 			{
 				MinecraftMesh.getLogger().severe("Item could not be registered because it was null.");
 				return;
 			}
 			
 			Item.itemsList[item.shiftedIndex] = item;
 			moddedItems.add(item);
 			
 		} catch(Exception e) {	e.printStackTrace();	}
 	}
 }
