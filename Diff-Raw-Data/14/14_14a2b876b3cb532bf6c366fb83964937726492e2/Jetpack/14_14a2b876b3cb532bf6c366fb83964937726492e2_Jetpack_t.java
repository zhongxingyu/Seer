 package ictrobot.gems.magnetic.armor;
 
 import ictrobot.gems.Gems;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.ItemArmor;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.world.World;
 import net.minecraftforge.common.EnumHelper;
 
 public class Jetpack extends ItemArmor {
   
   static EnumArmorMaterial armor = EnumHelper.addArmorMaterial("Jetpack", 0, new int[] { 0, 0, 0, 0 }, 0);
   
   public Jetpack(int par1, int par2) {
     super(par1, armor, par2, 1);
   }
 
   @SideOnly(Side.CLIENT)
   public void registerIcons(IconRegister par1RegisterIcon) {
     this.itemIcon = par1RegisterIcon.registerIcon("Gems:Jetpack");
   }
 
   public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer) {
     return "Gems:textures/models/armor/Jetpack_1.png";
   }
   
   @Override
   public void onArmorTickUpdate (World world, EntityPlayer player, ItemStack itemStack)  {
     Side side = FMLCommonHandler.instance().getEffectiveSide();
    if (side.isClient()) {
      if (JetpackKeybind.keyPressed) {
           player.addVelocity(0, 0.1, 0);
      }
    } else {
      NBTTagCompound tag = player.getEntityData();
      if (tag.getInteger("GemsJetpack")==1) {
           player.fallDistance = 0.0F;
           player.distanceWalkedModified = 0.0F;
           Gems.proxy.resetPlayerInAirTime(player);
       }
     }
   }
 }
