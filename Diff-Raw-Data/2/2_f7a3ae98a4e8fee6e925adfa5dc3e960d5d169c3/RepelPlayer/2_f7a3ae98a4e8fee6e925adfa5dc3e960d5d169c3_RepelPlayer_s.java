 package ictrobot.gems.magnetic.item;
 
 import java.util.List;
 
 import ictrobot.core.Core;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.ItemTool;
 import net.minecraft.world.World;
 
 public class RepelPlayer extends ItemTool{
 
   public int Level;
   
   public RepelPlayer(int par1, EnumToolMaterial par2EnumToolMaterial, int TmpLevel) {
     super(par1, 0, par2EnumToolMaterial, Block.blocksList);
    setMaxDamage((12*Level)-1);
     setUnlocalizedName("RepelPlayerLvl" + TmpLevel);
     setCreativeTab(CreativeTabs.tabTools);
     setTextureName(Core.ModID + ":RepelPlayer");
     setMaxStackSize(1);
     Level = TmpLevel;
   }
   
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
     par3List.add("Level " + Level);
   }
     
   @Override
   public boolean canHarvestBlock(Block block) {
     return false;
   }
 
   @Override
   public float getStrVsBlock(ItemStack is, Block block, int meta) {
     return 0F;
   }
 
   @Override
   public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
     player.motionY = (0.6*Level); 
     //itemStack.setItemDamage(itemStack.getItemDamage() - 1);
     itemStack.damageItem(1, player);
     return itemStack;
   }
 }
