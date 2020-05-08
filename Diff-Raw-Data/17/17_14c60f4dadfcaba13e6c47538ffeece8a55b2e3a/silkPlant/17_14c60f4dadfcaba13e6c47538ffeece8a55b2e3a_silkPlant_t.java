 package mods.Electrolysm.electro.world.plant;
 
 import java.awt.Paint;
 import java.awt.PaintContext;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.ColorModel;
 
 import mods.Electrolysm.electro.electrolysmCore;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockTallGrass;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 
public class silkPlant extends Block{
 
 	private static String nameID = "silkPlant";
 	
	public silkPlant(int par1) {
		super(par1, Material.vine);
 		// TODO Auto-generated constructor stub

 		this.setCreativeTab(electrolysmCore.TabElectrolysm);
 		this.setUnlocalizedName(nameID);
 	}
 	
 	@Override
 	public void registerIcons(IconRegister reg)
 	{
 			this.blockIcon = reg.registerIcon("electrolysm:" + nameID);
 	}
 
 
 }
