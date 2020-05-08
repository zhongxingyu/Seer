 package net.lomeli.diving.armor;
 
 import net.lomeli.diving.config.RegisterItems;
 import net.lomeli.diving.lib.ReferenceStrings;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.ItemArmor;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.world.World;
 
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemGear extends ItemArmor
 {
     private int amount;
     private String texture;
     private String armorTexture;
 
     public ItemGear(int par1, EnumArmorMaterial par2EnumArmorMaterial,
             int par3, int par4, int uses, int amount, String texture,
             String armor)
     {
         super(par1, par2EnumArmorMaterial, par3, par4);
         this.setMaxDamage(uses + 1);
         this.amount = amount;
         this.texture = texture;
         armorTexture = armor;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister iconRegister)
     {
         this.itemIcon = iconRegister.registerIcon(ReferenceStrings.modID
                 .toLowerCase() + ":" + texture);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer)
     {
        return "/mods/divinggear/textures/models/" + armorTexture + ".png";
     }
 
     @Override
     public void onArmorTickUpdate(World world, EntityPlayer player,
             ItemStack itemStack)
     {
         if (player.isWet())
         {
             player.addPotionEffect(new PotionEffect(16, -1, 10));
             if (player.getAir() <= 0)
             {
                 if (itemStack.getItemDamage() < itemStack.getMaxDamage() - 1)
                 {
                     player.setAir(amount);
                     itemStack.damageItem(1, player);
                     player.sendChatToPlayer("[DivingGear] "
                             + ((itemStack.getItemDamage() - itemStack
                                     .getMaxDamage()) * -1 - 1) + " uses left.");
                 }
             }
         } else
         {
             if (itemID == RegisterItems.Snorkel.itemID)
             {
                 itemStack.setItemDamage(0);
             } else
             {
             }
         }
     }
 
 }
