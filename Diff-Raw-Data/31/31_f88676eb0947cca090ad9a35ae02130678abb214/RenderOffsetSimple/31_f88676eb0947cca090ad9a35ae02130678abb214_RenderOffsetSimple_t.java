 package spacecraft.core.render;
 
 import org.lwjgl.opengl.GL11;
 
 import spacecraft.core.block.BlockScreen;
 import spacecraft.core.block.tile.TileEntityScreen;
 import spacecraft.core.utility.RenderRegistryHelper;
 import spacecraft.core.utility.WorldSavedDataSC;
 import spacecraft.core.world.TeleporterInfo;
 import spacecraft.core.world.WorldLinkInfo;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.world.ChunkCache;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 
 public class RenderOffsetSimple implements ISimpleBlockRenderingHandler {
 	private int renderId;
 	
 	public RenderOffsetSimple() {
 		renderId = RenderRegistryHelper.getRenderId(RenderOffsetSimple.class);
 	}
 
 	@Override
 	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {}
 
 	@Override
 	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block,
 			int modelId, RenderBlocks renderer) {
 		IBlockAccess backup = renderer.blockAccess;
 		TileEntityScreen tile = ((TileEntityScreen) world.getBlockTileEntity(x, y, z));
 		renderer.blockAccess = tile.getBlockInfo();
 		Block blockToRender = tile.getBlock();
 		boolean result = false;
 		if (blockToRender != null && !(blockToRender instanceof BlockScreen)) {
 			result = renderer.renderBlockByRenderType(blockToRender, x, y, z);
 		}
 		renderer.blockAccess = backup;
 		return result;
 	}
 
 	@Override
 	public boolean shouldRender3DInInventory() {
 		return false;
 	}
 
 	@Override
 	public int getRenderId() {
 		return renderId;
 	}
 
 }
