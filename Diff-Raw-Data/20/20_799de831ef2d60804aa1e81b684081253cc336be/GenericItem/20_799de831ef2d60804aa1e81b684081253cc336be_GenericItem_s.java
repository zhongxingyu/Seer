 package com.drblakl.letsmod;
 
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
 }
