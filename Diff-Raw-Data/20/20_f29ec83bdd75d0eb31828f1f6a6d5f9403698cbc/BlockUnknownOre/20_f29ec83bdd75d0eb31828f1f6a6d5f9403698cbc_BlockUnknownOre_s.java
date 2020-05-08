 package mods.minecraft.darth.dc.block;
 
 import mods.minecraft.darth.dc.DiscoveryCraft;
 import mods.minecraft.darth.dc.lib.Strings;
 import net.minecraft.block.material.Material;
 
 public class BlockUnknownOre extends BlockDC
 {
 
     public BlockUnknownOre(int id)
     {
         super(id, Material.rock);
        this.setUnlocalizedName(Strings.UNKNOWN_ORE_NAME);
        this.setCreativeTab(DiscoveryCraft.tabsDC);
     }
 
 }
