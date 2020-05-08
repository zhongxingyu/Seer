 package com.cane.item;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.cane.CaneCraft;
 import com.cane.block.BlockCane;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import net.minecraftforge.oredict.OreDictionary;
 
 public class ItemCane extends ItemCC
 {
 	public static String[] itemNames = new String[]{"Iron", "Copper", "Tin", "Silver",
 		"Gold", "Diamond", "Redstone", "Compressed Iron", "Compressed Copper",
 		"Compressed Tin", "Compressed Silver", "Compressed Gold",
 		"Compressed Diamond", "Compressed Redstone", "Soul"};
 	
 	public static ItemStack[] output;
 	
 	public ItemCane(int id)
 	{
 		super(id);
 		this.setHasSubtypes(true);
 	}
 	
 	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int face, float par8, float par9, float par10)
     {
         int prevBlockID = world.getBlockId(x, y, z);
 
         if (prevBlockID == Block.snow.blockID)
         {
             face = 1;
         }
         else if (prevBlockID != Block.vine.blockID && prevBlockID != Block.tallGrass.blockID && prevBlockID != Block.deadBush.blockID)
         {
             if (face == 0)
             {
                 --y;
             }
 
             if (face == 1)
             {
                 ++y;
             }
 
             if (face == 2)
             {
                 --z;
             }
 
             if (face == 3)
             {
                 ++z;
             }
 
             if (face == 4)
             {
                 --x;
             }
 
             if (face == 5)
             {
                 ++x;
             }
         }
 
         if (!player.canPlayerEdit(x, y, z, face, itemStack))
         {
             return false;
         }
         else if (itemStack.stackSize == 0)
         {
             return false;
         }
         else
         {
         	BlockCane block = CaneCraft.Blocks.cane;
         	int metadata = itemStack.getItemDamage();
         	if(block.canPlaceBlockAtMetadata(world, x, y, z, metadata))
             {
                 if (world.setBlockAndMetadataWithNotify(x, y, z, block.blockID, metadata))
                 {
                     if (world.getBlockId(x, y, z) == block.blockID)
                     {
                         block.onBlockPlacedBy(world, x, y, z, player);
                        block.onPostBlockPlaced(world, x, y, z, metadata);
 
                         world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), block.stepSound.getPlaceSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
                         --itemStack.stackSize;
                     }
                 }
             }
 
             return true;
         }
     }
 	
 	@Override
 	public int getIconFromDamage(int damage)
 	{
 		return damage;
 	}
 	
 	@Override
 	public String getItemNameIS(ItemStack itemStack)
 	{
 		return "cane"+itemStack.getItemDamage();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void getSubItems(int val, CreativeTabs tab, List list)
 	{
 		for(int i = 0; i < itemNames.length; i++)
 		{
 			list.add(new ItemStack(this, 1, i));
 		}
 	}
 
 	public static void initOutputs()
 	{
 		output = new ItemStack[]
    		{
    			new ItemStack(Item.ingotIron),
    			null,
    			null,
    			null,
    			new ItemStack(Item.ingotGold),
    			new ItemStack(Item.diamond),
    			new ItemStack(CaneCraft.Items.caneUtil, 1, 18),
    			
    			new ItemStack(Item.ingotIron, 9),
    			null,
    			null,
    			null,
    			new ItemStack(Item.ingotGold, 9),
    			new ItemStack(Item.diamond, 9),
    			new ItemStack(CaneCraft.Items.caneUtil, 1, 19),
    			
    			new ItemStack(CaneCraft.Items.caneUtil, 1, 20)
    		};
 		
 		output[1] = getOre("ingotCopper", 1);
 		output[2] = getOre("ingotTin", 1);
 		output[3] = getOre("ingotSilver", 1);
 		
 		output[8] = getOre("ingotCopper", 9);
 		output[9] = getOre("ingotTin", 9);
 		output[10] = getOre("ingotSilver", 9);
 	}
 	
 	private static ItemStack getOre(String s, int q)
 	{
 		ArrayList<ItemStack> ores = OreDictionary.getOres(s);
 		
 		if(ores.size() > 0)
 		{
 			ItemStack svar = ores.get(0).copy();
 			svar.stackSize = q;
 			return svar;
 		}
 		
 		return null;
 	}
 }
