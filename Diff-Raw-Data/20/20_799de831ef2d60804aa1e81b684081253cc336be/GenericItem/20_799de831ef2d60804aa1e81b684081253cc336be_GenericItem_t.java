 package com.drblakl.letsmod;
 
import com.drblakl.letsmod.lib.Reference;

 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 
 public class GenericItem extends Item {
        
     public GenericItem(int itemID) {
         super(itemID);
         setMaxStackSize(64);
         setCreativeTab(CreativeTabs.tabMisc);
         setUnlocalizedName("genericItem");
     }
     
     public GenericItem(int itemID, int maxStackSize, CreativeTabs tab, int texture, String name){
         super(itemID);
         setMaxStackSize(maxStackSize);
         setCreativeTab(tab);
         setUnlocalizedName(name);
     }
    
    // Registers Icon to the Mod
    public void registerIcons(IconRegister par1iconregister){
        this.itemIcon = par1iconregister.registerIcon(Reference.MOD_ID + ":" + "genericItem");
    }
 }
