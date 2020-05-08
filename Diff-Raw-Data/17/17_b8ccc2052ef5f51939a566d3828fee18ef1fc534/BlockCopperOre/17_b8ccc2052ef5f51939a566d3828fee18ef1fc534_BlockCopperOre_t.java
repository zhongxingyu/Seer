 package com.cyntain.Fm.Block;
 
 import java.util.Random;
 
 import com.cyntain.Fm.CreativeTab.CreativeTabFm;
 import com.cyntain.Fm.lib.ItemIDs;
 import com.cyntain.Fm.lib.Reference;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
//import net.minecraft.item.Item;
 
 public class BlockCopperOre extends Block {
     
     public BlockCopperOre(int id, Material par2Material){
         super(id, par2Material);
         this.setCreativeTab(CreativeTabFm.tabsFuelMod);
         this.setHardness(5f);
         
     }
                 
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister iconRegister) {
         this.blockIcon = iconRegister.registerIcon(Reference.MOD_ID + ":" + this.getUnlocalizedName2());
     }
     
     public int idDropped(int par1, Random par2Random, int par3)
     {
         return ItemIDs.COPPER_CHUNK_DEFAULT;
     }
 
 }
