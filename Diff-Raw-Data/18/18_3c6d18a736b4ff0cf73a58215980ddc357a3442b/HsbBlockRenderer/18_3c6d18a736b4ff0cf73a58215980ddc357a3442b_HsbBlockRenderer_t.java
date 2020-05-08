 package hsb;
 
 import hsb.config.HsbItems;
 import hsb.tileentitys.TileEntityHsb;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.IBlockAccess;
 import net.minecraftforge.client.ForgeHooksClient;
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 
 public class HsbBlockRenderer implements ISimpleBlockRenderingHandler {
 	@Override
 	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
 			Block block, int modelId, RenderBlocks renderer) {
 		
 		if(block == HsbItems.blockHsb)
 		{
 			TileEntity te = world.getBlockTileEntity(x, y, z);
 		
			if(te instanceof TileEntityHsb && ((TileEntityHsb) te).getConnectedTerminal().getCamoBlockId() != -1) {
 				//Render Camo-block
				int blockId = ((TileEntityHsb) te).getConnectedTerminal().getCamoBlockId();
 				Block camoBlock = Block.blocksList[blockId];
 				if(camoBlock != null)
 				{
					if(camoBlock.getTextureFile()!=null)
					{
						ForgeHooksClient.bindTexture(camoBlock.getTextureFile(), 1);
						renderer.renderBlockByRenderType(camoBlock, x, y, z);
						return true;
					}
 				}
 			}
 
 				
 
 
 			//render normal Block
 			ForgeHooksClient.bindTexture(ModHsb.proxy.TEXTURE_BLOCKS, 0);
 			renderer.renderStandardBlock(block, x, y, z);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean shouldRender3DInInventory() {
		return true;
 	}
 
 	@Override
 	public int getRenderId() {
 		return ClientProxy.HSBRENDERER_ID;
 	}
 
 	@Override
 	public void renderInventoryBlock(Block block, int metadata, int modelID,
 			RenderBlocks renderer) {
 
 	}
 }
