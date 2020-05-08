 package mods.learncraft.common;
 
 import java.sql.SQLException;
 import java.util.Random;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.Explosion;
 import net.minecraft.world.World;
 
 
 public class InvisibleBlock extends Block
 {
 	
     public InvisibleBlock(int par1, Material mat)
     {
     	super(par1, mat);
    	this.setLightOpacity(0);
		this.setBlockUnbreakable();
     }
     
     public void registerIcons(IconRegister reg) 
 	{
 		this.blockIcon = reg.registerIcon("learncraft:InvisibleBlock");
 	}
 
     /**
      * Returns the quantity of items to drop on block destruction.
      */
    
    
     public int getRenderBlockPass()
     {
         return 0;
     }
     
 
     /**
      * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
      * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
      */
     
     public boolean isOpaqueCube()
     {
         return false;
     }
     
    
     public boolean isAirBlock(World world, int x, int y, int z)
     {
         return true;
     }
     
    
 }
