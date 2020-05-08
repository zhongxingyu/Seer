 package net.lomeli.diving.thaumcraft.armor;
 
 import net.lomeli.diving.DivingGear;
 import net.lomeli.diving.lib.ReferenceIntegers;
 import net.lomeli.diving.lib.ReferenceStrings;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.EnumRarity;
 import net.minecraft.item.ItemArmor;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 import thaumcraft.api.aura.AuraNode;
 import thaumcraft.api.EnumTag;
 import thaumcraft.api.ObjectTags;
 import thaumcraft.api.ThaumcraftApi;
 
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemThaumADS extends ItemArmor
 {
 
     public ItemThaumADS(int par1, EnumArmorMaterial par2EnumArmorMaterial,
             int par3, int par4)
     {
         super(par1, par2EnumArmorMaterial, par3, par4);
         this.setCreativeTab(DivingGear.divingGearTab);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister iconRegister)
     {
         this.itemIcon = iconRegister.registerIcon(ReferenceStrings.modID
                .toLowerCase() + ":thaumichelm.png");
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer)
     {
         return "/mods/divinggear/textures/models/thaumicHelmModel.png";
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public EnumRarity getRarity(ItemStack par1)
     {
         return EnumRarity.uncommon;
     }
 
     @Override
     public void onArmorTickUpdate(World world, EntityPlayer player,
             ItemStack itemStack)
     {
         if (player.isWet() == true)
         {
             if (player.getAir() <= 0)
             {
                 int x = (int) player.posX;
                 int y = (int) player.posY;
                 int z = (int) player.posZ;
                 AuraNode node = getNodeWithinRange(world, x, y, z,
                         ReferenceIntegers.visArea);
                 if (node != null)
                 {
                     ThaumcraftApi.queueNodeChanges(node.key, 0, 0, false,
                             new ObjectTags().add(EnumTag.WIND,
                                     ReferenceIntegers.fluxAmount), 0, 0, 0);
                     player.setAir(ReferenceIntegers.thaumAmount);
                 }
             }
         } else
         {
 
         }
     }
 
     protected AuraNode getNodeWithinRange(World world, int x, int y, int z,
             double range)
     {
         AuraNode node = null;
 
         int nodeId = ThaumcraftApi.getClosestAuraWithinRange(world, x, y, z,
                 range);
         if (nodeId >= 0)
         {
             node = ThaumcraftApi.getNodeCopy(nodeId);
         }
 
         return node;
     }
 }
