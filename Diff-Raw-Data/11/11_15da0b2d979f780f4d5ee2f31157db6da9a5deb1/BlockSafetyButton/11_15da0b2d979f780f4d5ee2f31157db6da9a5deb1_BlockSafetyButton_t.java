 package net.runfast.frangiblebuttons.block;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockButtonStone;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.runfast.Util;
 import net.runfast.frangiblebuttons.ClientProxy;
 import net.runfast.frangiblebuttons.EffectsManager;
 import net.runfast.frangiblebuttons.FrangibleButtons;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockSafetyButton extends BlockButtonStone {
 
     public static final String name = "safetyButton";
     public static int assignedBlockID;
     protected int renderType;
 
     public static BlockSafetyButton instance;
 
     public int getRenderType() {
         return renderType;
     }
 
     public BlockSafetyButton setRenderType(int renderType) {
         this.renderType = renderType;
         return this;
     }
 
     public BlockSafetyButton() {
         super(assignedBlockID);
 
         setUnlocalizedName(name);
         setHardness(0.5F);
         setStepSound(soundGlassFootstep);
     }
 
     public void breakGlass(EntityPlayer player, int x, int y, int z) {
         // Here the server sees all the player's actions with remote=false.
         // Each player sees ONLY his own action with remote=true.
         // Thus we're forced to use custom packets to implement the effects.
         EffectsManager.broadcastGlassBreakEffect(player, x, y, z);
         player.worldObj.setBlock(x, y, z, Block.stoneButton.blockID,
                 player.worldObj.getBlockMetadata(x, y, z), 0x02);
     }
 
     @Override
     public void onBlockClicked(World world, int x, int y, int z,
             EntityPlayer player) {
         breakGlass(player, x, y, z);
     }
 
     @Override
     public boolean onBlockActivated(World world, int x, int y, int z,
             EntityPlayer player, int par6, float par7, float par8, float par9) {
         // Right-click. Do nothing.
         return true;
     }
 
     @Override
     public void onEntityCollidedWithBlock(World world, int x, int y, int z,
             Entity entity) {
         // Arrows and such have no effect.
     }
 
     @Override
     public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x,
             int y, int z) {
         return getGlassBounds(world.getBlockMetadata(x, y, z)).offset(x, y, z);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister iconRegister) {
         this.blockIcon = iconRegister.registerIcon("FrangibleButtons:"
                 + BlockSafetyButton.name);
     }
 
     public AxisAlignedBB getButtonBounds(int meta) {
         // This is the same shape as the vanilla button.
         return getBounds(meta, 1.0F, 1.5F, true);
     }
 
     public AxisAlignedBB getGlassBounds(int meta) {
         // This defines the shape of the glass enclosure.
         return getBounds(meta, 2.4F, 1.0F, false);
     }
 
     /**
      * @param meta
      *            Button metadata.
      * @param scale
      *            Uniform scaling factor.
      * @param aspect
      *            Aspect ratio.
      * @param considerPressed
      *            If true, the Button metadata (pressed / not pressed) affects
      *            the depth.
      */
     protected AxisAlignedBB getBounds(int meta, float scale, float aspect,
             boolean considerPressed) {
         // Mostly ripped off from the vanilla BlockButton code.
         int facing = meta & 7;
         boolean pressed = (meta & 8) > 0;
 
         float height = 0.125F * scale;
         float width = 0.125F * scale * aspect;
         float depth = 0.125F * scale;
 
         if (pressed && considerPressed) {
             depth = 0.0625F * scale;
         }
 
         if (facing == 1) {
             return AxisAlignedBB.getBoundingBox(0.0F, 0.5F - height,
                     0.5F - width, depth, 0.5F + height, 0.5F + width);
         } else if (facing == 2) {
             return AxisAlignedBB.getBoundingBox(1.0F - depth, 0.5F - height,
                     0.5F - width, 1.0F, 0.5F + height, 0.5F + width);
         } else if (facing == 3) {
             return AxisAlignedBB.getBoundingBox(0.5F - width, 0.5F - height,
                     0.0F, 0.5F + width, 0.5F + height, depth);
         } else if (facing == 4) {
             return AxisAlignedBB.getBoundingBox(0.5F - width, 0.5F - height,
                     1.0F - depth, 0.5F + width, 0.5F + height, 1.0F);
         }
         FrangibleButtons.log.warning("SafetyButton: invalid metadata (" + meta
                 + ")");
         return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);
     }
 
     @Override
     public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y,
             int z) {
         Util.applyBB(this, getGlassBounds(world.getBlockMetadata(x, y, z)));
     }
 
     /**
     * For some reason this becomes the inventory icon.
      */
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getBlockTextureFromSideAndMetadata(int par1, int par2) {
         return blockIcon;
     }
 
     @Override
     public boolean renderAsNormalBlock() {
         return false;
     }
 
     @Override
     public boolean canRenderInPass(int pass) {
         // A hack to make the current render pass available globally.
         ClientProxy.renderPass = pass;
 
         // this should render in both passes
         return true;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public int getRenderBlockPass() {
         // 0 is solids, 1 is alpha.
         // But allegedly returning 1 makes it render in both passes.
         return 1;
     }
 }
