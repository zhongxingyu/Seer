 package net.runfast.frangiblebuttons.blocks;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockButtonStone;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.Event.Result;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
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
 
     /**
      * This allows the player to create a BlockSafetyButton in-place by using a
      * vanilla glass pane on a vanilla button.
      */
     @ForgeSubscribe()
     public void onPlayerInteract(PlayerInteractEvent event) {
         EntityPlayer player = event.entityPlayer;
         ItemStack hand = player.getHeldItem();
         if (hand != null && hand.itemID == Block.thinGlass.blockID
                 && event.action == Action.RIGHT_CLICK_BLOCK
                 && !player.isSneaking()) {
             World world = player.worldObj;
             if (world.getBlockId(event.x, event.y, event.z) == Block.stoneButton.blockID) {
                 event.useBlock = Result.DENY;
                 // Trigger the button's update early, if one is pending.
                 // This is to avoid any headaches resulting from a pressed
                 // button inside the cover.
                 Block.stoneButton.updateTick(world, event.x, event.y, event.z,
                         world.rand);
                 // Replace the vanilla button with our block.
                 world.setBlock(event.x, event.y, event.z,
                         BlockSafetyButton.instance.blockID,
                         world.getBlockMetadata(event.x, event.y, event.z), 0x02);
                 if (!world.isRemote) {
                     // FIXME: This sound effect doesn't play.
                    // Some event bus or threading tomfoolery?
                    // Who knows?
                     world.playSoundEffect(event.x + 0.5, event.x + 0.5,
                             event.y + 0.5, "step.stone", 1.0F, 0.8F);
                 }
                 hand.stackSize -= 1;
             }
         }
     }
 
     public BlockSafetyButton() {
         super(assignedBlockID);
 
         setUnlocalizedName(name);
         setHardness(0.5F);
         setStepSound(soundGlassFootstep);
 
         MinecraftForge.EVENT_BUS.register(this);
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
     * Turns out this is used to render the inventory icon.
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
