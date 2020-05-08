 package loecraftpack.common.items;
 
 import loecraftpack.LoECraftPack;
 import loecraftpack.common.blocks.BlockAppleBloomLeaves;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import cpw.mods.fml.common.Mod.Block;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemLeavesAppleBloom extends ItemBlock
 {
 	protected int bloomStage = 2;
 	protected BlockAppleBloomLeaves leaf;
 	
 	public ItemLeavesAppleBloom(int par1, Block leaf)
     {
         super(par1);
         this.setMaxDamage(0);
         this.setHasSubtypes(true);
         this.leaf = (BlockAppleBloomLeaves)leaf;
         System.out.println("leaf passed: "+ this.leaf);
     }
 	
     public int getMetadata(int par1)
     {
         return par1 | 4;
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * Gets an icon index based on an item's damage value
      */
     public Icon getIconFromDamage(int meta)
     {
     	return leaf.getBlockTextureFromSideAndMetadata(0, meta);
     }
 
     @SideOnly(Side.CLIENT)
     public int getColorFromItemStack(ItemStack par1ItemStack, int par2)
     {
         int meta = par1ItemStack.getItemDamage();
         return leaf.getRenderColor(meta);
     }
     
     public String getUnlocalizedName(ItemStack par1ItemStack)
     {
         int meta = par1ItemStack.getItemDamage() & 3;
         
         if (this.getBlockID() == LoECraftPack.blockZapAppleLeavesCharged.blockID)
         	return super.getUnlocalizedName();
         else if (meta < bloomStage)
         	return super.getUnlocalizedName()+".normal";
         else return super.getUnlocalizedName()+".blooming";
 
     }
 }
