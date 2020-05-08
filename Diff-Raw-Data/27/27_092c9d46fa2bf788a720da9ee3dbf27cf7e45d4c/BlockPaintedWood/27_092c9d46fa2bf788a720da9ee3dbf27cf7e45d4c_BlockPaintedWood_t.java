 package emasher.sockets;
 
 import java.util.List;
 
 import cpw.mods.fml.common.*;
 import cpw.mods.fml.relauncher.*;
 import net.minecraft.world.biome.*;
 import net.minecraft.world.*;
 import net.minecraft.world.chunk.*;
 import net.minecraft.block.*;
 import net.minecraft.item.*;
 import net.minecraft.tileentity.*;
 import net.minecraft.block.material.*;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.*;
 import net.minecraft.entity.player.*;
 import net.minecraft.entity.*;
 import net.minecraft.util.*;
 import net.minecraft.potion.*;
 import java.util.*;
 
 import emasher.core.EmasherCore;
 
 public class BlockPaintedWood extends Block
 {
 	private static final int NUM_BLOCKS = 16;
 	private Icon[] textures = new Icon[16];
 
 	public BlockPaintedWood(int par1, int par2, Material par4Material) 
 	{
 		super(par1, par4Material);
 		this.setCreativeTab(SocketsMod.tabSockets);
 		this.setBurnProperties(this.blockID, 5, 5);
 	}
 	
 	public Icon getIcon(int par1, int par2)
     {
 		return textures[par2];
     }
 	
 	@Override
     public void registerIcons(IconRegister par1IconRegister)
     {
 		for(int i = 0; i < 16; i++)
 		{
 			textures[i] = par1IconRegister.registerIcon("sockets:tile" + (16 + i));
 		}
     }
 	
	@Override
 	@SideOnly(Side.CLIENT)
     public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
     {
         for (int var4 = 0; var4 < NUM_BLOCKS; ++var4)
         {
             par3List.add(new ItemStack(par1, 1, var4));
         }
         
         
     }
	
	@Override
	public int damageDropped(int meta)
	{
		return meta;
	}
 }
