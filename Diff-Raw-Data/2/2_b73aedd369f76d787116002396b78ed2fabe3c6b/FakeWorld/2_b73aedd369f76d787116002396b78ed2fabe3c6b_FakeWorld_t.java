 package pickitup;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL14;
 
 import java.util.Map;
 import java.util.LinkedHashMap;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.GLAllocation;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.RenderHelper;
 import net.minecraft.client.renderer.OpenGlHelper;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.ChunkCoordinates;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Vec3;
 import net.minecraft.util.Vec3Pool;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.client.ForgeHooksClient;
 
 
 @SideOnly(Side.CLIENT)
 public class FakeWorld implements IBlockAccess {
     private static final Tessellator tessellator = new Tessellator();
     public static final FakeWorld instance = new FakeWorld(10);
     public final Vec3Pool vecPool = new Vec3Pool(10, 100);
     public final RenderBlocks rb = new RenderBlocks(this);
 
     public int glListBase;
     public NBTTagCompound old_block = null;
     public int id = 40;
     public int meta = 0;
     public Block block = Block.blocksList[40];
     public TileEntity te = null;
     public ChunkCoordinates where = null;
     public LRU frozenBlocks;
 
     public FakeWorld(int frozenCache) {
         frozenBlocks = new LRU(frozenCache);
         glListBase = GLAllocation.generateDisplayLists(frozenCache + 1);
     }
 
     public static void renderHeldBlock(double partialTick) {
         instance.doRender(partialTick);
     }
 
     public static void freeze(String username, int x, int y, int z) {
         instance.freezeRender(username, x, y, z);
     }
 
     public void freezeRender(String username, int x, int y, int z) {
         boolean[] used = new boolean[frozenBlocks.CAPACITY + 1];
         for (int i=0; i<used.length; i++) {
             used[i] = false;
         }
 
         for (Map.Entry<String, FrozenRender> entry : frozenBlocks.entrySet()) {
             used[entry.getValue().listOffset] = true;
         }
 
         int list = -1;
         for (int i=0; i<used.length; i++) {
             if (!used[i]) {
                 list = i;
                 break;
             }
         }
 
         if (list == -1) {
             System.out.println("PickItUp: No display list available.  This shouldn't be possible.");
             return;
         }
 
         FrozenRender frozen = new FrozenRender(list, Minecraft.getMinecraft().theWorld, x, y, z);
         frozenBlocks.put(username, frozen);
     }
 
     public void doRender(double partialTick) {
         for (Object entity : Minecraft.getMinecraft().theWorld.getLoadedEntityList()) {
             if (entity instanceof EntityPlayer) {
                 doRender( (EntityPlayer) entity, partialTick);
             }
         }
     }
 
     public void doRender(EntityPlayer player, double partialTick) {
         NBTTagCompound block_tag = PickItUp.getBlockHeld(player);
         // Do nothing if we're not holding a block.
         if (block_tag == null) {
             old_block = null;
             return;
         }
 
         // If the block we're rendering changed, grab its details.
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
         where = PickItUp.getHeldRenderCoords(player, (float)partialTick);
         if (where != null) {
             // Turn the lightmap back on, so that we match the standard pathway
             // exactly.
             Minecraft.getMinecraft().entityRenderer.enableLightmap(partialTick);
 
             // Make the block partially transparent.
             // Basics.
             boolean was_blending = GL11.glGetBoolean(GL11.GL_BLEND);
             GL11.glEnable(GL11.GL_BLEND);
             boolean was_alpha_testing = GL11.glGetBoolean(GL11.GL_ALPHA_TEST);
             GL11.glEnable(GL11.GL_ALPHA_TEST);
             boolean was_culling = GL11.glGetBoolean(GL11.GL_CULL_FACE);
             GL11.glEnable(GL11.GL_CULL_FACE);
             // Set the alpha value to use a constant.
             GL11.glBlendFunc(GL11.GL_CONSTANT_COLOR, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
 
             if (player == Minecraft.getMinecraft().thePlayer) {
                 GL14.glBlendColor(0.75f, 0.75f, 0.75f, 0.75f);
             } else {
                GL14.glBlendColor(0.5f, 0.5f, 0.5f, 0.5f);
             }
 
             // Check for a special renderer.
             boolean normal_render = true;
             if (TileEntityRenderer.instance.hasSpecialRenderer(te)) {
                 if (renderFrozen(player, where, (float)partialTick)) {
                     // Success!  Nothing more to do here.
                     normal_render = false;
                 } else {
                     // Special rendering, and no frozen version available.
                     // Just substitute in a block of quartz; it's a nice
                     // neutral block.
                     block = Block.blockNetherQuartz;
                 }
             }
 
             if (normal_render) {
                 // Render it as a normal block.
                 doNormalRender(player, where, block, (float)partialTick);
             }
 
             // Undo all the setup we did before.
             GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
             if (!was_culling) {
                 GL11.glDisable(GL11.GL_CULL_FACE);
             }
             if (!was_alpha_testing) {
                 GL11.glDisable(GL11.GL_ALPHA_TEST);
             }
             if (!was_blending) {
                 GL11.glDisable(GL11.GL_BLEND);
             }
             Minecraft.getMinecraft().entityRenderer.disableLightmap(partialTick);
         }
     }
 
     public void doNormalRender(EntityPlayer player, ChunkCoordinates where,
                                Block block, float partialTick) {
         // Swap in our private Tessellator, so that RenderBlocks uses it.
         Tessellator normal_tessellator = Tessellator.instance;
         Tessellator.instance = tessellator;
 
         // Set up the Tessellator.
         tessellator.startDrawingQuads();
         Vec3 loc = Minecraft.getMinecraft().thePlayer.getPosition(partialTick);
         tessellator.setTranslation(-loc.xCoord,
                                             -loc.yCoord,
                                             -loc.zCoord);
         rb.setRenderBoundsFromBlock(block);
 
         // Do the actual rendering.
         rb.renderBlockByRenderType(block, where.posX, where.posY, where.posZ);
         tessellator.draw();
 
         // Restore the normal tessellator.
         tessellator.setTranslation(0D,0D,0D);
         Tessellator.instance = normal_tessellator;
     }
 
     public boolean renderFrozen(EntityPlayer player, ChunkCoordinates where,
                                 float partialTick) {
         FrozenRender frozen = frozenBlocks.get(player.username);
         if (frozen == null) {
             return false;
         }
 
         frozen.renderAt(where.posX, where.posY, where.posZ, partialTick);
 
         return true;
     }
 
     /**
      * Returns the block ID at coords x,y,z
      */
     public int getBlockId(int x, int y, int z) {
         if (where == null  || (x == where.posX && y == where.posY &&
                                z == where.posZ)) {
             return id;
         } else {
             return 0;
         }
     }
 
     /**
      * Returns the TileEntity associated with a given block in X,Y,Z coordinates, or null if no TileEntity exists
      */
     public TileEntity getBlockTileEntity(int x, int y, int z) {
         if (where == null  || (x == where.posX && y == where.posY &&
                                z == where.posZ)) {
             return te;
         } else {
             return null;
         }
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * Any Light rendered on a 1.8 Block goes through here
      */
     public int getLightBrightnessForSkyBlocks(int x, int y, int z, int l) {
         return 15 << 20 | 15 << 4;
     }
 
     /**
      * Returns the block metadata at coords x,y,z
      */
     public int getBlockMetadata(int x, int y, int z) {
         if (TileEntityRenderer.instance.hasSpecialRenderer(te)) {
             return 0;
         }
 
         if (where == null  || (x == where.posX && y == where.posY &&
                                z == where.posZ)) {
             return meta;
         } else {
             return 0;
         }
     }
 
     @SideOnly(Side.CLIENT)
     public float getBrightness(int x, int y, int z, int l) {
         return 1.0F;
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * Returns how bright the block is shown as which is the block's light value looked up in a lookup table (light
      * values aren't linear for brightness). Args: x, y, z
      */
     public float getLightBrightness(int x, int y, int z) {
         return 1.0F;
     }
 
     /**
      * Returns the block's material.
      */
     public Material getBlockMaterial(int x, int y, int z) {
         return null;
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * Returns true if the block at the specified coordinates is an opaque cube. Args: x, y, z
      */
     public boolean isBlockOpaqueCube(int x, int y, int z) {
         return false;
     }
 
     /**
      * Indicate if a material is a normal solid opaque cube.
      */
     public boolean isBlockNormalCube(int x, int y, int z) {
         return false;
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * Returns true if the block at the specified coordinates is empty
      */
     public boolean isAirBlock(int x, int y, int z) {
         return true;
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * Gets the biome for a given set of x/z coordinates
      */
     public BiomeGenBase getBiomeGenForCoords(int x, int z) {
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
     public boolean doesBlockHaveSolidTopSurface(int x, int y, int z) {
         return false;
     }
 
     /**
      * Return the Vec3Pool object for this world.
      */
     public Vec3Pool getWorldVec3Pool() {
         return vecPool;
     }
 
     /**
      * Is this block powering in the specified direction Args: x, y, z, direction
      */
     public int isBlockProvidingPowerTo(int x, int y, int z, int direction) {
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
 
     public class FrozenRender {
         public int listOffset;
 
         public FrozenRender(int list, World world, int x, int y, int z) {
             listOffset = list;
 
             // Begin capturing the FrozenRender as a display list.
             GL11.glNewList(glListBase + listOffset, GL11.GL_COMPILE);
 
             // Get the correct Block and TileEntity for what we're freezing.
             Block frozenBlock = Block.blocksList[Minecraft.getMinecraft().theWorld.getBlockId(x, y, z)];
             TileEntity frozenTE = Minecraft.getMinecraft().theWorld.getBlockTileEntity(x, y, z);
 
             // Grab and prime the usual RenderBlocks.
             RenderBlocks gRB = Minecraft.getMinecraft().renderGlobal.globalRenderBlocks;
             gRB.setRenderBoundsFromBlock(frozenBlock);
 
             // Set up the Tessellator.
             Tessellator.instance.setTranslation((double)-x, (double)-y, (double)-z);
 
             for (int pass=0; pass<2; pass++) {
                 // Set the render pass.
                 ForgeHooksClient.setRenderPass(pass);
 
                 // Make sure we're set up properly for drawing.
                 Minecraft.getMinecraft().renderEngine.bindTexture("/terrain.png");
                 RenderHelper.disableStandardItemLighting();
                 GL11.glColor4f(1f,1f,1f,1f);
                 GL11.glEnable(GL11.GL_BLEND);
                 GL11.glEnable(GL11.GL_ALPHA_TEST);
                 GL11.glEnable(GL11.GL_CULL_FACE);
                 GL11.glBlendFunc(GL11.GL_CONSTANT_COLOR, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
 
                 // Do the actual rendering.
                 if (block.canRenderInPass(pass)) {
                     Tessellator.instance.startDrawingQuads();
                     gRB.renderBlockByRenderType(frozenBlock, x, y, z);
                     Tessellator.instance.draw();
                 }
                 if (TileEntityRenderer.instance.hasSpecialRenderer(frozenTE)) {
                     if (frozenTE.shouldRenderInPass(pass)) {
                         TileEntityRenderer.instance.renderTileEntityAt(frozenTE, 0D, 0D, 0D, 0f);
                     }
                 }
 
                 // Make sure we've cleaned up after ourselves.
                 Minecraft.getMinecraft().renderEngine.bindTexture("/terrain.png");
                 RenderHelper.disableStandardItemLighting();
                 GL11.glColor4f(1f,1f,1f,1f);
                 GL11.glEnable(GL11.GL_BLEND);
                 GL11.glEnable(GL11.GL_ALPHA_TEST);
                 GL11.glEnable(GL11.GL_CULL_FACE);
                 GL11.glBlendFunc(GL11.GL_CONSTANT_COLOR, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
                 OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
                 GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                 OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
 
             }
 
             // Capture complete!
             ForgeHooksClient.setRenderPass(-1);
             Tessellator.instance.setTranslation(0D,0D,0D);
             GL11.glEndList();
         }
 
         public void renderAt(int x, int y, int z, float partialTick) {
             // Save the current camera state.
             GL11.glPushMatrix();
 
             // Move the camera to the player's location.
             Vec3 loc = Minecraft.getMinecraft().thePlayer.getPosition(partialTick);
             GL11.glTranslated(-loc.xCoord, -loc.yCoord, -loc.zCoord);
 
             // Offset by the block's location.
             GL11.glTranslatef((float)x, (float)y, (float)z);
 
             // Replay the frozen rendering.
             GL11.glCallList(glListBase + listOffset);
 
             // Restore the original camera state.
             GL11.glPopMatrix();
         }
     }
 
     public class LRU extends LinkedHashMap<String,FrozenRender> {
         static final long serialVersionUID = 359501;
         public final int CAPACITY;
         public LRU(int max) {
             super(max, 0.75F, true);
 
             CAPACITY = max;
         }
 
         // Called when a new element is inserted.  A return value of true
         // will cause the provided entry to be removed from the map.
         protected boolean removeEldestEntry(Map.Entry<String,FrozenRender> eldest) {
             return (size() >= CAPACITY);
         }
     }
 }
