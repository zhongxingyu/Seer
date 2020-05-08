 package com.cyntain.Fm.Item;
 
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumAction;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.world.World;
 
 import com.cyntain.Fm.CreativeTab.CreativeTabFm;
import com.cyntain.Fm.lib.ItemIDs;
 import com.cyntain.Fm.lib.Reference;
 import com.cyntain.Fm.lib.Strings;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 
 public class itemZeoliteVial extends ItemFood  {
     
    
     
     private int[] potionEffect = {14};
     private int[] potionDuration = {60};
     private int[] potionTeir = {3};
     
     
     
     public itemZeoliteVial(int id){
         super(id, 0, 0F, false);
          this.setUnlocalizedName(Strings.ZEOLITE_DUST_GLASS_NAME);
          this.setAlwaysEdible();
          this.setCreativeTab(CreativeTabFm.tabsFuelMod);
         this.maxStackSize = 16;
     }
 
 
     @SideOnly(Side.CLIENT)
     @Override
             public void updateIcons(IconRegister iconRegister) {
                     this.iconIndex = iconRegister.registerIcon(Reference.MOD_ID + ":" + this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".") + 1) );
       
                     
         }
     
 
 
 public void onFoodEaten(ItemStack itemStack, World world, EntityPlayer player){
    //--itemStack.stackSize;
   System.out.println("does this code run");
     player.getFoodStats().addStats(this);
     this.addPotionEffects(itemStack, world, player);
     player.inventory.addItemStackToInventory(new ItemStack(Item.glassBottle));
    player.inventory.consumeInventoryItem(ItemIDs.ZEOLITE_DUST_VIAL);
 }
 
 
 private void addPotionEffects(ItemStack itemStack, World world, EntityPlayer player) {
 
   player.addPotionEffect(new PotionEffect(potionEffect[0], potionDuration[0] * 20, potionTeir[0]));
     
 }
     public EnumAction onItemRightClick(ItemStack itemStack){
         return EnumAction.drink;
     }
 
     public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player){
         player.setItemInUse(itemStack, this.getMaxItemUseDuration(itemStack));
         return itemStack;
     }    
         public boolean hasEffect(){
     
         return true;
     }
     
     
 }
