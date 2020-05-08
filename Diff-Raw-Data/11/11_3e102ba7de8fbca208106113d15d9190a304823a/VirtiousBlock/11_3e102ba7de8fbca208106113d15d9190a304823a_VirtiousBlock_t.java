 package teamm.mods.virtious.block;
 
 import java.util.Random;
 
 import teamm.mods.virtious.Virtious;
 import teamm.mods.virtious.lib.VirtiousBlocks;
 import teamm.mods.virtious.lib.VirtiousItems;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 
 public class VirtiousBlock extends Block
 {
 	public VirtiousBlock(int id, Material mat) 
 	{
 		super(id, mat);
 		this.setCreativeTab(Virtious.tabVirtiousBlocks);
 	}
 	
 	public void registerIcons(IconRegister r)
 	{
 		this.blockIcon = r.registerIcon("virtious:" + this.getUnlocalizedName().substring(5));
 	}
 	
 	public int idDropped(int par1, Random par2Random, int par3)
 	{
 		return blockID == VirtiousBlocks.virtianstone.blockID ? VirtiousBlocks.virtiancobblestone.blockID :
 			blockID == VirtiousBlocks.oreAquieus.blockID ? VirtiousItems.shardAquieus.itemID :
			blockID == VirtiousBlocks.oreTak.blockID ? VirtiousItems.tak.itemID :
 			blockID == VirtiousBlocks.orePluthorium.blockID ? VirtiousItems.gemPluthorium.itemID : 
 			blockID == VirtiousBlocks.oreDeepTak.blockID ? VirtiousItems.tak.itemID :
 			blockID == VirtiousBlocks.oreIlluminous.blockID ? VirtiousItems.gemIlluminous.itemID:
 			blockID == VirtiousBlocks.oreDeepIlluminous.blockID ? VirtiousItems.gemIlluminous.itemID :
 			this.blockID;
 	}
 	
 	/**
      * Returns the quantity of items to drop on block destruction.
      */
     public int quantityDropped(Random par1Random)
     {
         return blockID == VirtiousBlocks.oreAquieus.blockID ? 2 + par1Random.nextInt(3) : blockID == VirtiousBlocks.oreIlluminous.blockID ? 2 + par1Random.nextInt(3) : blockID == VirtiousBlocks.oreDeepIlluminous.blockID ? 2 + par1Random.nextInt(3) : 1;
     }
 }
