 package biomesoplenty.client.render.blocks;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.EntityRenderer;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.util.IIcon;
 import net.minecraft.world.IBlockAccess;
 import biomesoplenty.api.BOPBlockHelper;
 import biomesoplenty.common.blocks.BlockBOPFoliage;
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 
 public class FoliageRenderer implements ISimpleBlockRenderingHandler
 {
 	private final int GRASSTOP = 6;
 
 	@Override
 	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
 	{
 		// Doesn't render in inventory
 	}
 
 	@Override
 	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
 	{
 		int meta = world.getBlockMetadata(x, y, z);
 		
 		if (block == BOPBlockHelper.get("foliage"))
 		{
 			if (meta == 0)
 				return renderBlockAlgae(renderer, block, x, y, z);
 			else if (meta == 13)
 				return renderBlockClover(renderer, block, x, y, z);
 			else
 				return renderCrossedSquares(block, x, y, z, renderer);
 		}
 		else
 		{
 			if (meta == 0)
 				return renderBlockAlgae(renderer, block, x, y, z);
 			else
 				return renderCrossedSquares(block, x, y, z, renderer);
 		}
 	}
 
 	@Override
 	public boolean shouldRender3DInInventory(int modelId)
 	{
 		return false;
 	}
 
 	@Override
 	public int getRenderId()
 	{
 		return RenderUtils.foliageModel;
 	}
 	
     public boolean renderBlockAlgae(RenderBlocks renderer, Block block, int x, int y, int z)
     {
         Tessellator tessellator = Tessellator.instance;
 		//TODO:						  blockAccess
 		IBlockAccess world = renderer.field_147845_a;
 		//TODO:				  getBlockIconFromSide()
         IIcon icon = renderer.func_147777_a(block, 1);
 
         //Need to make public: overrideBlockTexture
         
         //TODO:		hasOverrideBlockTexture()
         if (renderer.func_147744_b())
         {
         	//TODO:			overrideBlockTexture
             icon = renderer.field_147840_d;
         }
         
 		float cf = 1.0F;
 		//TODO:			colorMUltiplier()
 		int cl = block.func_149720_d(world, x, y, z);
 		float c1 = (cl >> 16 & 255) / 255.0F;
 		float c2 = (cl >> 8 & 255) / 255.0F;
 		float c3 = (cl & 255) / 255.0F;
 
 		if (EntityRenderer.anaglyphEnable)
 		{
 			float f4 = (c1 * 30.0F + c2 * 59.0F + c3 * 11.0F) / 100.0F;
 			float f5 = (c1 * 30.0F + c2 * 70.0F) / 100.0F;
 			float f6 = (c1 * 30.0F + c3 * 70.0F) / 100.0F;
 			c1 = f4;
 			c2 = f5;
 			c3 = f6;
 		}
 
 		tessellator.setColorOpaque_F(cf * c1, cf * c2, cf * c3);
 
         float f = 0.015625F;
         double d0 = (double)icon.getMinU();
         double d1 = (double)icon.getMinV();
         double d2 = (double)icon.getMaxU();
         double d3 = (double)icon.getMaxV();
         long l = (long)(x * 3129871) ^ (long)z * 116129781L ^ (long)y;
         l = l * l * 42317861L + l * 11L;
         int i1 = (int)(l >> 16 & 3L);
         //TODO:							getMixedBrightnessForBlock()
         tessellator.setBrightness(block.func_149677_c(world, x, y, z));
         float f1 = (float)x + 0.5F;
         float f2 = (float)z + 0.5F;
         float f3 = (float)(i1 & 1) * 0.5F * (float)(1 - i1 / 2 % 2 * 2);
         float f4 = (float)(i1 + 1 & 1) * 0.5F * (float)(1 - (i1 + 1) / 2 % 2 * 2);
         tessellator.addVertexWithUV((double)(f1 + f3 - f4), (double)((float)y + f), (double)(f2 + f3 + f4), d0, d1);
         tessellator.addVertexWithUV((double)(f1 + f3 + f4), (double)((float)y + f), (double)(f2 - f3 + f4), d2, d1);
         tessellator.addVertexWithUV((double)(f1 - f3 + f4), (double)((float)y + f), (double)(f2 - f3 - f4), d2, d3);
         tessellator.addVertexWithUV((double)(f1 - f3 - f4), (double)((float)y + f), (double)(f2 + f3 - f4), d0, d3);
         tessellator.addVertexWithUV((double)(f1 - f3 - f4), (double)((float)y + f), (double)(f2 + f3 - f4), d0, d3);
         tessellator.addVertexWithUV((double)(f1 - f3 + f4), (double)((float)y + f), (double)(f2 - f3 - f4), d2, d3);
         tessellator.addVertexWithUV((double)(f1 + f3 + f4), (double)((float)y + f), (double)(f2 - f3 + f4), d2, d1);
         tessellator.addVertexWithUV((double)(f1 + f3 - f4), (double)((float)y + f), (double)(f2 + f3 + f4), d0, d1);
         return true;
     }
     
     public boolean renderBlockClover(RenderBlocks renderer, Block block, int x, int y, int z)
     {
         Tessellator tessellator = Tessellator.instance;
 		//TODO:						  blockAccess
 		IBlockAccess world = renderer.field_147845_a;
 		//TODO:				  getBlockIconFromSideAndMetadata()
         IIcon icon = renderer.func_147787_a(block, 1, 13);
 
         //TODO:		hasOverrideBlockTexture()
         if (renderer.func_147744_b())
         {
         	//TODO:			overrideBlockTexture
             icon = renderer.field_147840_d;
         }
         
 		float cf = 1.0F;
 		//TODO:			colorMultiplier()
 		int cl = block.func_149720_d(world, x, y, z);
 		float c1 = (cl >> 16 & 255) / 255.0F;
 		float c2 = (cl >> 8 & 255) / 255.0F;
 		float c3 = (cl & 255) / 255.0F;
 
 		if (EntityRenderer.anaglyphEnable)
 		{
 			float f4 = (c1 * 30.0F + c2 * 59.0F + c3 * 11.0F) / 100.0F;
 			float f5 = (c1 * 30.0F + c2 * 70.0F) / 100.0F;
 			float f6 = (c1 * 30.0F + c3 * 70.0F) / 100.0F;
 			c1 = f4;
 			c2 = f5;
 			c3 = f6;
 		}
 
 		tessellator.setColorOpaque_F(cf * c1, cf * c2, cf * c3);
 
         float f = 0.1F;
         double d0 = (double)icon.getMinU();
         double d1 = (double)icon.getMinV();
         double d2 = (double)icon.getMaxU();
         double d3 = (double)icon.getMaxV();
         long l = (long)(x * 3129871) ^ (long)z * 116129781L ^ (long)y;
         l = l * l * 42317861L + l * 11L;
         int i1 = (int)(l >> 16 & 3L);
         //TODO:							getMixedBrightnessForBlock()
         tessellator.setBrightness(block.func_149677_c(world, x, y, z));
         float f1 = (float)x + 0.5F;
         float f2 = (float)z + 0.5F;
         float f3 = (float)(i1 & 1) * 0.5F * (float)(1 - i1 / 2 % 2 * 2);
         float f4 = (float)(i1 + 1 & 1) * 0.5F * (float)(1 - (i1 + 1) / 2 % 2 * 2);
         tessellator.addVertexWithUV((double)(f1 + f3 - f4), (double)((float)y + f), (double)(f2 + f3 + f4), d0, d1);
         tessellator.addVertexWithUV((double)(f1 + f3 + f4), (double)((float)y + f), (double)(f2 - f3 + f4), d2, d1);
         tessellator.addVertexWithUV((double)(f1 - f3 + f4), (double)((float)y + f), (double)(f2 - f3 - f4), d2, d3);
         tessellator.addVertexWithUV((double)(f1 - f3 - f4), (double)((float)y + f), (double)(f2 + f3 - f4), d0, d3);
         tessellator.addVertexWithUV((double)(f1 - f3 - f4), (double)((float)y + f), (double)(f2 + f3 - f4), d0, d3);
         tessellator.addVertexWithUV((double)(f1 - f3 + f4), (double)((float)y + f), (double)(f2 - f3 - f4), d2, d3);
         tessellator.addVertexWithUV((double)(f1 + f3 + f4), (double)((float)y + f), (double)(f2 - f3 + f4), d2, d1);
         tessellator.addVertexWithUV((double)(f1 + f3 - f4), (double)((float)y + f), (double)(f2 + f3 + f4), d0, d1);
         return true;
     }
 
 	private boolean renderCrossedSquares(Block par1Block, int par2, int par3, int par4, RenderBlocks renderer)
 	{
 		Tessellator tessellator = Tessellator.instance;
 		//TODO:						  blockAccess
 		IBlockAccess world = renderer.field_147845_a;
 		
 		//TODO:								getMixedBrightnessForBlock()
 		tessellator.setBrightness(par1Block.func_149677_c(world, par2, par3, par4));
 		float f = 1.0F;
 		//TODO:				colorMultiplier()
 		int l = par1Block.func_149720_d(world, par2, par3, par4);
 		float f1 = (l >> 16 & 255) / 255.0F;
 		float f2 = (l >> 8 & 255) / 255.0F;
 		float f3 = (l & 255) / 255.0F;
 
 		if (EntityRenderer.anaglyphEnable)
 		{
 			float f4 = (f1 * 30.0F + f2 * 59.0F + f3 * 11.0F) / 100.0F;
 			float f5 = (f1 * 30.0F + f2 * 70.0F) / 100.0F;
 			float f6 = (f1 * 30.0F + f3 * 70.0F) / 100.0F;
 			f1 = f4;
 			f2 = f5;
 			f3 = f6;
 		}
 
 		//TODO:																										  getBlock()
 		if ((world.getBlockMetadata(par2, par3, par4) == 8 || world.getBlockMetadata(par2, par3, par4) == 9) && world.func_147439_a(par2, par3, par4) == BOPBlockHelper.get("foliage"))
 		{
 			tessellator.setColorOpaque_F(f, f, f);
 		}
 		else
 		{
 			tessellator.setColorOpaque_F(f * f1, f * f2, f * f3);
 		}
 		
 		double d0 = par2;
 		double d1 = par3;
 		double d2 = par4;
 
 		if (par1Block == BOPBlockHelper.get("foliage"))
 		{
 			long i1;
 			if (world.getBlockMetadata(par2, par3, par4) == GRASSTOP) {
 				i1 = par2 * 3129871 ^ par4 * 116129781L ^ par3 - 1;
 			} else {
 				i1 = par2 * 3129871 ^ par4 * 116129781L ^ par3;
 			}
 
 			i1 = i1 * i1 * 42317861L + i1 * 11L;
 			d0 += ((i1 >> 16 & 15L) / 15.0F - 0.5D) * 0.5D;
 			d1 += ((i1 >> 20 & 15L) / 15.0F - 1.0D) * 0.2D;
 			d2 += ((i1 >> 24 & 15L) / 15.0F - 0.5D) * 0.5D;
 		}
 
 		//TODO:														getBlock()
 		if (world.getBlockMetadata(par2, par3, par4) == 10 && world.func_147439_a(par2, par3, par4) == BOPBlockHelper.get("flowers")) 
 		{
			//TODO:	 drawCrossedSquares()
			renderer.func_147730_a(par1Block, world.getBlockMetadata(par2, par3, par4), d0, d1 - 1, d2, 1.0F);
 		} 
 		//TODO:															getBlock()
 		else if (world.getBlockMetadata(par2, par3, par4) == 8 && world.func_147439_a(par2, par3, par4) == BOPBlockHelper.get("foliage")) 
 		{
 			renderBerryBush(d0, d1, d2, 1.0F, f1, f2, f3, renderer);
 		}
 		//TODO:															getBlock()
 		else if (world.getBlockMetadata(par2, par3, par4) == 9 && world.func_147439_a(par2, par3, par4) == BOPBlockHelper.get("foliage")) 
 		{
 			renderShrub(d0, d1, d2, 1.0F, f1, f2, f3, renderer);
 		}
 		else 
 		{
 			//TODO:	 drawCrossedSquares()	 getIcon()
 			renderer.func_147765_a(par1Block.func_149691_a(0, world.getBlockMetadata(par2, par3, par4)), d0, d1, d2, 1.0F);
 		}
 		
 		return true;
 	}
 	
 	private static void renderBerryBush(double par1, double par2, double par3, float par4, float par5, float par6, float par7, RenderBlocks renderer)
 	{
         Tessellator tessellator = Tessellator.instance;
         //TODO:					   getBlockIconFromSideAndMetadata()
         IIcon berryBush = renderer.func_147787_a(BOPBlockHelper.get("foliage"), 0, 8);
         IIcon berryBushBerry = ((BlockBOPFoliage)BOPBlockHelper.get("foliage")).berryBushBerry;
 
 		tessellator.setColorOpaque_F(par4 * par5, par4 * par6, par4 * par7);
 		//TODO:	 drawCrossedSquares()
 		renderer.func_147765_a(berryBush, par1, par2, par3, par4);
 		tessellator.setColorOpaque_F(par4, par4, par4);
 		//TODO:	 drawCrossedSquares()
 		renderer.func_147765_a(berryBushBerry, par1, par2, par3, par4);
 	}
 	
 	private static void renderShrub(double par1, double par2, double par3, float par4, float par5, float par6, float par7, RenderBlocks renderer)
 	{
         Tessellator tessellator = Tessellator.instance;
         //TODO:					   getBlockIconFromSideAndMetadata()
         IIcon shrubLeaf = renderer.func_147787_a(BOPBlockHelper.get("foliage"), 0, 9);
         IIcon shrubBranch = ((BlockBOPFoliage)BOPBlockHelper.get("foliage")).shrubBranch;
 
 		tessellator.setColorOpaque_F(par4 * par5, par4 * par6, par4 * par7);
 		//TODO:	 drawCrossedSquares()
 		renderer.func_147765_a(shrubLeaf, par1, par2, par3, par4);
 		tessellator.setColorOpaque_F(par4, par4, par4);
 		//TODO:	 drawCrossedSquares()
         renderer.func_147765_a(shrubBranch, par1, par2, par3, par4);
 	}
 }
