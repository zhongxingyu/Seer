 package assets.electrolysm.electro.oreProccessing;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import net.minecraftforge.oredict.OreDictionary;
 import assets.electrolysm.electro.electrolysmCore;
 import assets.electrolysm.electro.handlers.LoggerHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class nettedBlock extends Block {
 
 	@SideOnly(Side.CLIENT)
 	private Icon[] nettedIcon;
 	
 	public nettedBlock(int id, Material mat) {
 		super(id, Material.rock);
 
 		this.setHardness((float) (1.5 + 0.8));
 		//this.setCreativeTab(electrolysmCore.TabElectrolysm);
 	}
     
 	@Override
     public String getUnlocalizedName()
     {
        return this.getTextureName();
     }
 	/*
     @Override
     public void getSubBlocks(int id, CreativeTabs creativeTab, List list)
     {
         for (int i = 0; i < 6; i++)
         {
             list.add(new ItemStack(electrolysmCore.nettedBlock, 1, i));
         }
     }
 */
     public int idDropped(int par1, Random par2Random, int par3)
    {
 	   return 0;
    }
     
     //		"Copper", "Tin", "Iron", "Gold", "Silver", "Lead"};
     //METAs		0		   1	  2		  3			4		5
 
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister reg)
     {
         nettedIcon = new Icon[6];
 
         nettedIcon[0] = reg.registerIcon("electrolysm:netted/" + "netted" + "CopperOre");
         nettedIcon[1] = reg.registerIcon("electrolysm:netted/" + "netted" + "TinOre");
         nettedIcon[2] = reg.registerIcon("electrolysm:netted/" + "netted" + "IronOre");
         nettedIcon[3] = reg.registerIcon("electrolysm:netted/" + "netted" + "GoldOre");
         nettedIcon[4] = reg.registerIcon("electrolysm:netted/" + "netted" + "SilverOre");
         nettedIcon[5] = reg.registerIcon("electrolysm:netted/" + "netted" + "LeadOre");
 
     }
 
     ArrayList<ItemStack> copperOre = OreDictionary.getOres("oreCopper");
     ArrayList<ItemStack> tinOre = OreDictionary.getOres("oreTin");
     ArrayList<ItemStack> silverOre = OreDictionary.getOres("oreSilver");
     ArrayList<ItemStack> leadOre = OreDictionary.getOres("oreLead");
     
     public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, 
     		int par6, float par7, float par8, float par9)
     {
     	if(player.isSneaking())
     	{
     		if(player.getHeldItem() == null)
     		{
     			if(world.getBlockId(x, y, z) == this.blockID)
     			{
     				if(this.getNameFromMeta(world.getBlockMetadata(x, y, z)) != null)
     				{
     					String oreName = this.getNameFromMeta(world.getBlockMetadata(x, y, z));
     					
     				}
     			}
     		}
     	}
     	return false;
     }
     
     private void spawnItems(String oreName, EntityPlayer player, World world, int x, int y, int z)
     {
     	ItemStack net = new ItemStack(electrolysmCore.net, 1, 0);
     	
 		if(oreName.contains("oreIron"))
 		{
 			world.setBlock(x, y, z, Block.oreIron.blockID);
 		}
 		else if(oreName.contains("oreGold"))
 		{
 			world.setBlock(x, y, z, Block.oreGold.blockID);
 		}
 		else if(oreName.contains("Copper"))
 		{
 			world.setBlock(x, y, z, copperOre.get(0).itemID);
 		}
 		else if(oreName.contains("Tin"))
 		{
 			world.setBlock(x, y, z, tinOre.get(0).itemID);
 		}
 		else if(oreName.contains("Lead"))
 		{
 			world.setBlock(x, y, z, leadOre.get(0).itemID);
 		}
 		else if(oreName.contains("Silver"))
 		{
 			world.setBlock(x, y, z, silverOre.get(0).itemID);
 		}
 		else
 		{
 			String message = "Variable 'oreName' in nettedBlock.class has changed" +
 					", mid method!";
 			String message2 = "This is a BUG please report it to the MOD author!";
 			LoggerHandler.severe(message);
 			LoggerHandler.severe(message2);
 		}
 		
 		int id = world.getBlockId(x, y, z);
 		if(id == electrolysmCore.nettedBlock.blockID)
 		{
 			player.entityDropItem(net, 0);
 		}
     }
     
     
     
     		
 //		"Copper", "Tin", "Iron", "Gold", "Silver", "Lead"};
 //METAs		0		   1	  2		  3			4		5
     private String getNameFromMeta(int meta) 
     {
     	if(meta == 0)
     	{
     		return "oreCopper";
     	}
     	else if(meta == 1)
     	{
     		return "oreTin";
     	}
     	else if(meta == 2)
     	{
     		return "oreIron";
     	}
     	else if(meta == 3)
     	{
     		return "oreGold";
     	}
     	else if(meta == 4)
     	{
     		return "oreSilver";
     	}
     	else if(meta == 5)
     	{
     		 return "oreLead";
     	}
     	else
     	{
     		return null;
     	}
 	}
 
 	@Override
     @SideOnly(Side.CLIENT)
     public Icon getIcon(int side, int meta)
     {
         return nettedIcon[meta];
     }
 }
