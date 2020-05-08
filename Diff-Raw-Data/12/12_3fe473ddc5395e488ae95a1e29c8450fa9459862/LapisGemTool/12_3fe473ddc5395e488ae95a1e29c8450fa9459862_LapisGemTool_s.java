 package ictrobot.gems.tools.lapisgem;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.ItemTool;
 
 public class LapisGemTool extends ItemTool {
   
   public LapisGemTool(int id, float speed, EnumToolMaterial ToolMaterial) {
     super(id, speed, ToolMaterial, Block.blocksList);
     func_111206_d("Gems:LapisGemTool");
   }
   
   public boolean canHarvestBlock(Block par1Block, ItemStack itemStack)
   {
   return this.canHarvestBlock(par1Block);
   }
 
   public boolean canHarvestBlock(Block par1Block)
   {
   return true;
  }
  @Override
  public float damageVsEntity = 5;
  
 }
 
 
 
 
