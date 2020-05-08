 package rpg.items.armor.mage;
 
 import java.util.List;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.ItemArmor;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.IArmorTextureProvider;
 import rpg.config.base.mage.MageArmor;
 import rpg.lib.Reference;
 import rpg.playerinfo.PlayerInformation;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SuppressWarnings("deprecation")
 public class MageTrainingArmor extends ItemArmor implements
         IArmorTextureProvider {
 
     public MageTrainingArmor(int itemid, EnumArmorMaterial material, int par3,
             int par4, String name) {
         super(itemid, material, par3, par4);
         this.setUnlocalizedName(name);
     }
 
     @Override
     @SuppressWarnings({ "unchecked", "rawtypes" })
     public void addInformation(ItemStack par1ItemStack, EntityPlayer player,
             List par3List, boolean par4) {
         PlayerInformation PlayerInfoFake = PlayerInformation.forPlayer(player);
         // Checks the players class and colored item name
         // accordingly
         if (PlayerInfoFake.getPlayersClass().equals("Mage")
                 && player.experienceLevel >= 1) {
             par3List.add("Class: \u00a7AMage, \u00a74Alchemist");
             par3List.add("Level: \u00a7A1");
         } else if (PlayerInfoFake.getPlayersClass().equals("Alchemist")
                 && player.experienceLevel >= 1) {
             par3List.add("Class: \u00a74Mage, \u00a7AAlchemist");
             par3List.add("Level: \u00a741");
         } else if (PlayerInfoFake.getPlayersClass().equals("Mage")
                 && player.experienceLevel != 1) {
             par3List.add("Class: \u00a7AMage, \u00a74Alchemist");
             par3List.add("Level: \u00a741");
         } else if (PlayerInfoFake.getPlayersClass().equals("Alchemist")
                 && player.experienceLevel != 1) {
             par3List.add("Class: \u00a74Mage, \u00a7AAlchemist");
             par3List.add("Level: \u00a741");
         } else if ((!PlayerInfoFake.getPlayersClass().equals("Mage") || !PlayerInfoFake
                 .getPlayersClass().equals("Alchemist"))
                 && player.experienceLevel == 1) {
             par3List.add("Class: \u00a74Mage, \u00a74Alchemist");
             par3List.add("Level: \u00a7A1");
         } else {
             par3List.add("Class: \u00a74Mage, \u00a74Alchemist");
             par3List.add("Level: \u00a741");
         }
     }
 
     @Override
     public String getArmorTextureFile(ItemStack itemstack) {
         if (itemstack.itemID == MageArmor.hatTraining.itemID
                 || itemstack.itemID == MageArmor.robeTraining.itemID
                 || itemstack.itemID == MageArmor.sandalsTraining.itemID)
            return "/mods/rpg/textures/armor/mageTraining_1.png";
         else
             return null;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister iconregister) {
         if (this.itemID == MageArmor.hatTraining.itemID) {
             this.itemIcon = iconregister.registerIcon(Reference.MOD_ID + ":"
                     + this.getUnlocalizedName().substring(5));
         } else if (this.itemID == MageArmor.robeTraining.itemID) {
             this.itemIcon = iconregister.registerIcon(Reference.MOD_ID + ":"
                     + this.getUnlocalizedName().substring(5));
         } else if (this.itemID == MageArmor.sandalsTraining.itemID) {
             this.itemIcon = iconregister.registerIcon(Reference.MOD_ID + ":"
                     + this.getUnlocalizedName().substring(5));
         }
     }
 }
