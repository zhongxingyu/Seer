 package pickitup;
 
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.ChunkCoordinates;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Vec3;
 import net.minecraft.util.Vec3Pool;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.IBlockAccess;
 import net.minecraftforge.common.ForgeDirection;
 
 @SideOnly(Side.CLIENT)
 public class FakeWorld implements IBlockAccess {
     public static final FakeWorld instance = new FakeWorld();
     public final RenderBlocks rb = new RenderBlocks(this);
 
     public NBTTagCompound old_block = null;
     public int id = 40;
     public int meta = 0;
     public Block block = Block.blocksList[40];
     public TileEntity te = null;
 
     public static void renderHeldBlock(float partialTick) {
         instance.doRender(partialTick);
     }
 
     public void doRender(float partialTick) {
         NBTTagCompound block_tag = PickItUp.getMyBlockHeld();
         // Do nothing if we're not holding a block.
         if (block_tag == null) {
             old_block = null;
             return;
         }
 
         // If the block we're holding changed, grab its details.
         if (block_tag != old_block) {
             id = block_tag.getInteger("packed_id");
             meta = block_tag.getInteger("packed_meta");
             block = Block.blocksList[id];
 
             NBTTagCompound data = block_tag.getCompoundTag("packed_data");
             te = null;
             if (data != null && !data.hasNoTags()) {
                 te = TileEntity.createAndLoadEntity(data);
             }
         }
 
         // Render the block
         ChunkCoordinates where = PickItUp.getHeldRenderCoords(partialTick);
         EntityPlayer player = Minecraft.getMinecraft().thePlayer;
         if (where != null) {
             GL11.glEnable(GL11.GL_BLEND);
             Tessellator.instance.startDrawingQuads();
             Vec3 loc = player.getPosition(partialTick);
             Tessellator.instance.setTranslation(-loc.xCoord,
                                                 -loc.yCoord,
                                                 -loc.zCoord);
             rb.setRenderBoundsFromBlock(block);
             Tessellator.instance.setColorRGBA(255, 255, 255, 192);
             Tessellator.instance.disableColor();
             rb.renderBlockAllFaces(block, where.posX, where.posY, where.posZ);
             Tessellator.instance.draw();
             Tessellator.instance.setTranslation(0D,0D,0D);
             GL11.glDisable(GL11.GL_BLEND);
         }
     }
 
     /**
      * Returns the block ID at coords x,y,z
      */
     public int getBlockId(int i, int j, int k) {
         return id;
     }
 
     /**
      * Returns the TileEntity associated with a given block in X,Y,Z coordinates, or null if no TileEntity exists
      */
     public TileEntity getBlockTileEntity(int i, int j, int k) {
         return te;
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * Any Light rendered on a 1.8 Block goes through here
      */
     public int getLightBrightnessForSkyBlocks(int i, int j, int k, int l) {
         return 15 << 20 | 15 << 4;
     }
 
     /**
      * Returns the block metadata at coords x,y,z
      */
     public int getBlockMetadata(int i, int j, int k) {
         return meta;
     }
 
     @SideOnly(Side.CLIENT)
     public float getBrightness(int i, int j, int k, int l) {
         return 1.0F;
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * Returns how bright the block is shown as which is the block's light value looked up in a lookup table (light
      * values aren't linear for brightness). Args: x, y, z
      */
     public float getLightBrightness(int i, int j, int k) {
         return 1.0F;
     }
 
     /**
      * Returns the block's material.
      */
     public Material getBlockMaterial(int i, int j, int k) {
         return null;
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * Returns true if the block at the specified coordinates is an opaque cube. Args: x, y, z
      */
     public boolean isBlockOpaqueCube(int i, int j, int k) {
         return false;
     }
 
     /**
      * Indicate if a material is a normal solid opaque cube.
      */
     public boolean isBlockNormalCube(int i, int j, int k) {
         return false;
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * Returns true if the block at the specified coordinates is empty
      */
     public boolean isAirBlock(int i, int j, int k) {
         return true;
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * Gets the biome for a given set of x/z coordinates
      */
     public BiomeGenBase getBiomeGenForCoords(int i, int j) {
         return BiomeGenBase.plains;
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * Returns current world height.
      */
     public int getHeight() {
         return 256;
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * set by !chunk.getAreLevelsEmpty
      */
     public boolean extendedLevelsInChunkCache() {
         return false;
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * Returns true if the block at the given coordinate has a solid (buildable) top surface.
      */
     public boolean doesBlockHaveSolidTopSurface(int i, int j, int k) {
         return false;
     }
 
     /**
      * Return the Vec3Pool object for this world.
      */
     public Vec3Pool getWorldVec3Pool() {
         return null;
     }
 
     /**
      * Is this block powering in the specified direction Args: x, y, z, direction
      */
     public int isBlockProvidingPowerTo(int i, int j, int k, int l) {
         return 0;
     }
 
     /**
      * FORGE: isBlockSolidOnSide, pulled up from {@link World}
      *
      * @param x X coord
      * @param y Y coord
      * @param z Z coord
      * @param side Side
      * @param _default default return value
      * @return if the block is solid on the side
      */
     public boolean isBlockSolidOnSide(int x, int y, int z, ForgeDirection side, boolean _default) {
         return false;
     }
 }
