 package com.isocraft.item;
 
 import java.util.List;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.util.Icon;
 
 import com.isocraft.api.StringsAPI;
 import com.isocraft.lib.Colours;
 import com.isocraft.lib.Reference;
 import com.isocraft.lib.Strings;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 /**
  * ISOCraft
  * 
  * Item class for the data disks
  * 
  * @author Turnermator13
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
 public class ItemDisk extends ModItemBase {
 
     @SideOnly(Side.CLIENT)
     private Icon[] icons;
     public int no = 2;
     
     public ItemDisk(int par1) {
         super(par1, true);
         this.setUnlocalizedName(Strings.I_DISK);
         this.setHasSubtypes(false);
         this.setMaxDamage(0);
         this.setMaxStackSize(1);
         this.setDataType(StringsAPI.DATA_DIGITAL);
     }
     
     @Override
     public boolean getShareTag() {
         return true;
     }
     
     @SuppressWarnings({ "unchecked", "rawtypes" })
     @Override
     @SideOnly(Side.CLIENT)
     public void addInformation(ItemStack par1, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {  
         if (par1.stackTagCompound != null) {
             if(par1.stackTagCompound.hasKey(StringsAPI.DataType)){
                 par3List.add(EnumChatFormatting.GREEN + "Data Type: " + par1.stackTagCompound.getString(StringsAPI.DataType));
             }
         }
         this.diskInfo(par1, par3List);
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister iconRegister) {
         icons = new Icon[no];
 
         for (int i = 0; i < no; ++i) {
             icons[i] = iconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":" + this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".") + 1) + "_" + i);
         }
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public boolean requiresMultipleRenderPasses() {
         return true;
     }
     
     @Override
     public Icon getIcon(ItemStack itemStack, int renderPass) {
 
             if (renderPass != 1)
                 return icons[0];
             else
                 return icons[1];
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public int getColorFromItemStack(ItemStack itemStack, int renderPass) {
 
         if (renderPass == 1)
             return Integer.parseInt(Colours.WHITE, 16);
         else {
             int colour = this.getColour(itemStack);
 
             if (colour < 0) {colour = Integer.parseInt(Colours.WHITE, 16);}
             
             return colour;
         }
     }
 
     public int getColour(ItemStack itemStack) {
         NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
 
         if (nbtTagCompound != null){
             if(nbtTagCompound.hasKey(Strings.N_DISPLAY) && nbtTagCompound.getCompoundTag(Strings.N_DISPLAY).hasKey(Strings.N_COLOUR)){
                     return Integer.parseInt(nbtTagCompound.getCompoundTag(Strings.N_DISPLAY).getString(Strings.N_COLOUR), 16);
             } else if(nbtTagCompound.hasKey(StringsAPI.DataType)){
                 int ret = Integer.parseInt(Colours.WHITE, 16);
                 
                     if(nbtTagCompound.getString(StringsAPI.DataType).equals(StringsAPI.DATA_DIGITAL)){
                         ret = Integer.parseInt(Colours.BLUE, 16);
                     } else if(nbtTagCompound.getString(StringsAPI.DataType).equals(StringsAPI.DATA_INFO)){
                         ret = Integer.parseInt(Colours.GREEN, 16);
                     } else if(nbtTagCompound.getString(StringsAPI.DataType).equals(StringsAPI.DATA_SCRIPT)){
                         ret = Integer.parseInt(Colours.ORANGE, 16);
                     } 
                     
                 return ret;
             } else{
                 return Integer.parseInt(Colours.WHITE, 16); 
             }
         } else {
             return Integer.parseInt(Colours.WHITE, 16);
         }
    }
    
    @Override
    public ItemStack specificData(ItemStack iStack){
        iStack.stackTagCompound.setString(StringsAPI.DataType, StringsAPI.DATA_DIGITAL);
        return iStack;
    }
 }
