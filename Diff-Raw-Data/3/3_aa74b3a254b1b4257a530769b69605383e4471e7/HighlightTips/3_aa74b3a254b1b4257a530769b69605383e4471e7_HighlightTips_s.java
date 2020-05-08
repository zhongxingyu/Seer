 package agaricus.mods.highlighttips;
 
 import cpw.mods.fml.client.registry.KeyBindingRegistry;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.TickType;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.FMLRelauncher;
 import cpw.mods.fml.relauncher.Side;
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityFurnace;
 import net.minecraft.util.*;
 import net.minecraft.world.World;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.liquids.ILiquidTank;
 import net.minecraftforge.liquids.ITankContainer;
 import net.minecraftforge.liquids.LiquidStack;
 
 import java.util.EnumSet;
 import java.util.logging.Level;
 
 @Mod(modid = "HighlightTips", name = "HighlightTips", version = "1.0-SNAPSHOT") // TODO: version from resource
 @NetworkMod(clientSideRequired = false, serverSideRequired = false)
 public class HighlightTips implements ITickHandler {
 
     private static final int DEFAULT_KEY_TOGGLE = 62; // F4 - see http://www.minecraftwiki.net/wiki/Key_codes
     private static final double DEFAULT_RANGE = 300;
     private static final int DEFAULT_X = 0;
     private static final int DEFAULT_Y = 0;
     private static final int DEFAULT_COLOR = 0xffffff;
 
     private boolean enable = true;
     private int keyToggle = DEFAULT_KEY_TOGGLE;
     private double range = DEFAULT_RANGE;
     private int x = DEFAULT_X;
     private int y = DEFAULT_Y;
     private int color = DEFAULT_COLOR;
 
     private ToggleKeyHandler toggleKeyHandler;
 
     @Mod.PreInit
     public void preInit(FMLPreInitializationEvent event) {
         Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
 
         try {
             cfg.load();
 
             enable = cfg.get(Configuration.CATEGORY_GENERAL, "enable", true).getBoolean(true);
             keyToggle = cfg.get(Configuration.CATEGORY_GENERAL, "key.toggle", DEFAULT_KEY_TOGGLE).getInt(DEFAULT_KEY_TOGGLE);
             range = cfg.get(Configuration.CATEGORY_GENERAL, "range", DEFAULT_RANGE).getDouble(DEFAULT_RANGE);
             x = cfg.get(Configuration.CATEGORY_GENERAL, "x", DEFAULT_X).getInt(DEFAULT_X);
             y = cfg.get(Configuration.CATEGORY_GENERAL, "y", DEFAULT_Y).getInt(DEFAULT_Y);
             color = cfg.get(Configuration.CATEGORY_GENERAL, "color", DEFAULT_COLOR).getInt(DEFAULT_COLOR);
         } catch (Exception e) {
             FMLLog.log(Level.SEVERE, e, "HighlightTips had a problem loading it's configuration");
         } finally {
             cfg.save();
         }
 
         if (!FMLRelauncher.side().equals("CLIENT")) {
             // gracefully disable on non-client (= server) instead of crashing
             enable = false;
         }
 
         if (!enable) {
             FMLLog.log(Level.INFO, "HighlightTips disabled");
             return;
         }
 
         TickRegistry.registerTickHandler(this, Side.CLIENT);
         KeyBindingRegistry.registerKeyBinding(toggleKeyHandler = new ToggleKeyHandler(keyToggle));
     }
 
     private String describeBlock(int id, int meta, TileEntity tileEntity) {
         StringBuilder sb = new StringBuilder();
 
         describeBlockID(sb, id, meta);
         describeTileEntity(sb, tileEntity);
 
         return sb.toString();
     }
 
     private void describeTileEntity(StringBuilder sb, TileEntity te) {
         if (te == null) return;
 
         if (te instanceof ITankContainer) {
             sb.append(" ITankContainer: ");
 
             ILiquidTank[] tanks = ((ITankContainer) te).getTanks(ForgeDirection.UP);
             for (ILiquidTank tank : tanks) {
                 sb.append(describeLiquidStack(tank.getLiquid()));
                 sb.append(' ');
                 //sb.append(tank.getTankPressure()); // TODO: tank capacity *used*? this is not it..
                 //sb.append('/');
                 sb.append(tank.getCapacity());
                 int pressure = tank.getTankPressure();
                 if (pressure < 0) {
                     sb.append(pressure);
                 } else {
                     sb.append('+');
                     sb.append(pressure);
                 }
                 sb.append(' ');
             }
         }
 
         if (te instanceof IInventory) {
             IInventory inventory = (IInventory) te;
 
             sb.append(" IInventory: ");
             sb.append(inventoryName(inventory));
             sb.append(" (");
             sb.append(inventory.getSizeInventory());
             sb.append(" slots)");
         }
 
         sb.append(' ');
         sb.append(te.getClass().getName());
     }
 
     private String inventoryName(IInventory inventory) {
         if (!inventory.isInvNameLocalized()) {
             return StatCollector.translateToLocal(inventory.getInvName());
         } else {
             return inventory.getInvName();
         }
     }
 
     private String describeLiquidStack(LiquidStack liquidStack) {
         if (liquidStack == null) return "Empty";
 
         ItemStack itemStack = liquidStack.canonical().asItemStack();
         if (itemStack == null) return "Empty";
 
         return itemStack.getDisplayName();
     }
 
     private void describeBlockID(StringBuilder sb, int id, int meta) {
         Block block = Block.blocksList[id];
 
         if (block == null) {
             sb.append("block #"+id);
             return;
         }
 
         // block info
         sb.append(id);
         sb.append(':');
         sb.append(meta);
         sb.append(' ');
         String blockName = block.getLocalizedName();
        sb.append(blockName);
 
         // item info, if it was mined (this often has more user-friendly information, but sometimes is identical)
         sb.append("  ");
         int itemDropDamage = block.damageDropped(meta);
 
         if (Item.itemsList[id + 256] != null) {
             ItemStack itemDropStack = new ItemStack(id, 1, itemDropDamage);
             String itemDropName = itemDropStack.getDisplayName();
             if (!blockName.equals(itemDropName)) {
                 sb.append(itemDropName);
             }
 
             // item info guess if item damage corresponds to block metadata, not necessarily if mined - sometimes more informative
             try {
                 ItemStack itemMetaStack = new ItemStack(id, 1, meta);
                 String itemMetaName = itemMetaStack.getDisplayName();
                 if (itemMetaName != null && !blockName.equals(itemMetaName) && !itemDropName.equals(itemMetaName)) {
                     sb.append(' ');
                     sb.append(itemMetaName);
                 }
             } catch (Throwable t) {
 
             }
         }
         if (itemDropDamage != meta) {
             sb.append(' ');
             sb.append(itemDropDamage);
         }
     }
 
     // based on net/minecraft/item/Item, copied since it is needlessly protected
     protected MovingObjectPosition getMovingObjectPositionFromPlayer(World par1World, EntityPlayer par2EntityPlayer)
     {
         float f = 1.0F;
         float f1 = par2EntityPlayer.prevRotationPitch + (par2EntityPlayer.rotationPitch - par2EntityPlayer.prevRotationPitch) * f;
         float f2 = par2EntityPlayer.prevRotationYaw + (par2EntityPlayer.rotationYaw - par2EntityPlayer.prevRotationYaw) * f;
         double d0 = par2EntityPlayer.prevPosX + (par2EntityPlayer.posX - par2EntityPlayer.prevPosX) * (double)f;
         double d1 = par2EntityPlayer.prevPosY + (par2EntityPlayer.posY - par2EntityPlayer.prevPosY) * (double)f + 1.62D - (double)par2EntityPlayer.yOffset;
         double d2 = par2EntityPlayer.prevPosZ + (par2EntityPlayer.posZ - par2EntityPlayer.prevPosZ) * (double)f;
         Vec3 vec3 = par1World.getWorldVec3Pool().getVecFromPool(d0, d1, d2);
         float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
         float f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
         float f5 = -MathHelper.cos(-f1 * 0.017453292F);
         float f6 = MathHelper.sin(-f1 * 0.017453292F);
         float f7 = f4 * f5;
         float f8 = f3 * f5;
         double d3 = 5.0D;
         if (par2EntityPlayer instanceof EntityPlayerMP)
         {
             d3 = ((EntityPlayerMP)par2EntityPlayer).theItemInWorldManager.getBlockReachDistance();
         }
         Vec3 vec31 = vec3.addVector((double)f7 * d3, (double)f6 * d3, (double)f8 * d3);
         return par1World.rayTraceBlocks_do_do(vec3, vec31, false, false); // "ray traces all blocks, including non-collideable ones"
     }
 
     @Override
     public void tickEnd(EnumSet<TickType> type, Object... tickData) {
         if (!toggleKeyHandler.showInfo) return;
 
         Minecraft mc = Minecraft.getMinecraft();
         GuiScreen screen = mc.currentScreen;
         if (screen != null) return;
 
         float partialTickTime = 1;
         MovingObjectPosition mop = getMovingObjectPositionFromPlayer(mc.theWorld, mc.thePlayer);
         String s;
 
         if (mop == null) {
             return;
         } else if (mop.typeOfHit == EnumMovingObjectType.ENTITY) {
             // TODO: find out why this apparently never triggers
             s = "entity " + mop.entityHit.getClass().getName();
         } else if (mop.typeOfHit == EnumMovingObjectType.TILE) {
             int id = mc.thePlayer.worldObj.getBlockId(mop.blockX, mop.blockY, mop.blockZ);
             int meta = mc.thePlayer.worldObj.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);
             TileEntity tileEntity = mc.thePlayer.worldObj.blockHasTileEntity(mop.blockX, mop.blockY, mop.blockZ) ? mc.thePlayer.worldObj.getBlockTileEntity(mop.blockX, mop.blockY, mop.blockZ) : null;
 
             try {
                 s = describeBlock(id, meta, tileEntity);
             } catch (Throwable t) {
                 s = id + ":" + meta + "  - " + t;
                 t.printStackTrace();
             }
 
 
         } else {
             s = "unknown";
         }
 
         mc.fontRenderer.drawStringWithShadow(s, x, y, color);
     }
 
     @Override
     public void tickStart(EnumSet<TickType> type, Object... tickData) {
 
     }
 
     @Override
     public EnumSet<TickType> ticks() {
         return EnumSet.of(TickType.RENDER);
     }
 
     @Override
     public String getLabel() {
         return "HighlightTips";
     }
 }
