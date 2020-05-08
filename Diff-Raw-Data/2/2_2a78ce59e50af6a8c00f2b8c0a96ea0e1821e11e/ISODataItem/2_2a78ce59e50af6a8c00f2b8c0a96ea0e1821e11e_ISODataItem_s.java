 package com.isocraft.api;
 
 import java.util.HashMap;
 import java.util.List;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.world.World;
 
 /**
  * ISOCraft
  * 
  * Class which when extended allows for the item to be used in all data slots supporting the ISOCraft API
  * 
  * @author Turnermator13
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
public abstract class ISODataItem extends Item {
 
     private String DataType = null;
     public static HashMap<Integer, ISODataItem> ISODataList = new HashMap<Integer, ISODataItem>();
     public int DefaultSize = 512;
        
     public ISODataItem(int par1) {
         super(par1);
         ISODataList.put(256 + par1, this);
     }
 
     public ISODataItem setDataType(String par1Str) {
         this.DataType = par1Str;
         return this;
     }
     
     public String getDataType(){
         return this.DataType;
     }
 
     public boolean hasDataType(String type, ItemStack it, boolean locked){
         
         boolean ret = false;
         boolean track = false;
         
         if(this.DataType.equals(StringsAPI.DATA_DIGITAL)){
             if (it.stackTagCompound != null) {
                 if(it.stackTagCompound.hasKey(StringsAPI.DataType)){
                     if(type.equals(it.stackTagCompound.getString(StringsAPI.DataType))){
                         ret = true;
                         track = true;
                     }
                 }
             }
             if(!track){
                 if(!locked){
                     ret = type.equals(StringsAPI.DATA_INFO) || type.equals(StringsAPI.DATA_SCRIPT);
                 }
             }
         }
         if(!ret){
             if(type.equals(StringsAPI.DATA_DIGITAL)){
                 ret = this.DataType.equals(StringsAPI.DATA_INFO) || this.DataType.equals(StringsAPI.DATA_SCRIPT);
             }
         }
         if(!ret){
             ret = this.DataType.equals(type) || type.equals(StringsAPI.DATA_ANY) && DataType != null;
         }
         return ret;    
     }
 
     
     @Override
     public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
         if(this.DataType == StringsAPI.DATA_DIGITAL || this.DataType == StringsAPI.DATA_INFO || this.DataType == StringsAPI.DATA_SCRIPT){
             par1ItemStack.stackTagCompound.setInteger(StringsAPI.DriveSize, this.DefaultSize);
             par1ItemStack.stackTagCompound.setInteger(StringsAPI.UsedDriveSpace, 0);
         }
     }
     
     public ItemStack creativeData(){
         ItemStack iStack = new ItemStack(this);
         if (iStack.stackTagCompound == null) {
             iStack.stackTagCompound = new NBTTagCompound();
         }
         iStack.stackTagCompound.setInteger(StringsAPI.DriveSize, this.DefaultSize);
         iStack.stackTagCompound.setInteger(StringsAPI.UsedDriveSpace, 0);
         iStack = ISODataList.get(iStack.getItem().itemID).specificData(iStack);
         return iStack;
     }
     
     @SuppressWarnings({ "rawtypes" })
     @Override
     @SideOnly(Side.CLIENT)
     public void addInformation(ItemStack par1, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
         this.diskInfo(par1, par3List);
     }
     
     @SuppressWarnings({ "unchecked", "rawtypes" })
     @SideOnly(Side.CLIENT)
     public void diskInfo(ItemStack par1, List par3List){
         if(this.DataType == StringsAPI.DATA_DIGITAL || this.DataType == StringsAPI.DATA_INFO || this.DataType == StringsAPI.DATA_SCRIPT){
             if (par1.stackTagCompound != null) {
                 if(par1.stackTagCompound.hasKey(StringsAPI.DriveSize) && par1.stackTagCompound.hasKey(StringsAPI.UsedDriveSpace)){
                     par3List.add(EnumChatFormatting.BLUE +  "Drive Space Used: " + Integer.toString(par1.stackTagCompound.getInteger(StringsAPI.UsedDriveSpace)) + "/" + Integer.toString(par1.stackTagCompound.getInteger(StringsAPI.DriveSize)) + " MB");
                 }
             }
         }
     }
     
     public ItemStack specificData(ItemStack iStack){
         return iStack;
     }
 }
