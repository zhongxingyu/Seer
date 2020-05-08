 package com.rperce.compactstuff;
 
 import java.util.List;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.ItemArmor;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingFallEvent;
 
 import com.rperce.compactstuff.client.CSIcons;
 import com.rperce.compactstuff.client.ImageFiles;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemCompactArmor extends ItemArmor {
     private String path;
 
     public ItemCompactArmor(int id, EnumArmorMaterial material, int rend,
             int armorType, String path) {
         super(id, material, rend, armorType);
         System.out.println(path + " :: " + rend);
         this.setCreativeTab(CompactStuff.compactTab);
         this.path = CSIcons.PREFIX + path;
     }
 
     @Override
     public void registerIcons(IconRegister ir) {
         this.itemIcon = ir.registerIcon(this.path);
     }
 
     @ForgeSubscribe
     public static void onEntityLivingFallEvent(LivingFallEvent evt) {
         if (evt.entity instanceof EntityPlayer) {
             EntityPlayer player = (EntityPlayer) evt.entity;
             ItemStack boots = player.inventory.armorItemInSlot(0);
             if (boots == null) return;
             if (Ref.matches(boots, Ref.WOVEN_BOOTS, Ref.ADV_BOOTS)) {
                 if (evt.distance > 5) boots.damageItem(1, player);
                 evt.setCanceled(true);
             }
         }
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public String getArmorTexture(ItemStack stack, Entity entity, int slot,
             String type) {
         if (Ref.matches(stack, Ref.DIORITE_HELM, Ref.DIORITE_PLATE,
                 Ref.DIORITE_BOOTS))
             return ImageFiles.COBBLE_1.path;
         else if (Ref.matches(stack, Ref.DIORITE_PANTS))
             return ImageFiles.COBBLE_2.path;
         else if (Ref.matches(stack, Ref.METCARB_HELM, Ref.METCARB_PLATE,
                 Ref.METCARB_BOOTS))
             return ImageFiles.CARBON_1.path;
         else if (Ref.matches(stack, Ref.METCARB_PANTS))
             return ImageFiles.CARBON_2.path;
         else if (Ref.matches(stack, Ref.WOVEN_HELM, Ref.WOVEN_PLATE,
                 Ref.WOVEN_BOOTS))
             return ImageFiles.WOVEN_1.path;
         else if (Ref.matches(stack, Ref.WOVEN_PANTS))
             return ImageFiles.WOVEN_2.path;
         else if (Ref.matches(stack, Ref.ADV_HELM, Ref.ADV_PLATE, Ref.ADV_BOOTS))
             return ImageFiles.ADVANCED_1.path;
         else
             return ImageFiles.ADVANCED_2.path;
     }
 
     /** Warnings suppressed due to override constraints */
     @SuppressWarnings({ "rawtypes", "unchecked" })
     @SideOnly(Side.CLIENT)
     @Override
     public void addInformation(ItemStack thisStack, EntityPlayer player,
             List list, boolean wut) {
         if (Ref.matches(thisStack, Ref.WOVEN_HELM)) {
             list.add("2x water breathing");
         } else if (Ref.matches(thisStack, Ref.WOVEN_PLATE)) {
             list.add("Retaliate on melee");
         } else if (Ref.matches(thisStack, Ref.WOVEN_PANTS)) {
             list.add("Fire extinguishing");
         } else if (Ref.matches(thisStack, Ref.WOVEN_BOOTS)) {
             list.add("No fall damage");
         } else if (Ref.matches(thisStack, Ref.ADV_HELM)) {
             list.add("Infinite water breathing");
             list.add("Infinite durability");
         } else if (Ref.matches(thisStack, Ref.ADV_PLATE)) {
             list.add("Force field to hostiles");
             list.add("Infinite durability");
         } else if (Ref.matches(thisStack, Ref.ADV_PANTS)) {
             list.add("Fire immunity");
             list.add("Infinite durability");
         } else if (Ref.matches(thisStack, Ref.ADV_BOOTS)) {
             list.add("No fall damage");
             list.add("Infinite durability");
         }
     }
 }
