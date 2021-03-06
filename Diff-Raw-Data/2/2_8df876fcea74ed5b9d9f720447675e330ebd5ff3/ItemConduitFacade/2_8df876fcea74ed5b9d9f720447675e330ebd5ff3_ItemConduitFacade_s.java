 package crazypants.enderio.conduit.facade;
 
 import java.util.List;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.util.StatCollector;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import crazypants.enderio.EnderIO;
 import crazypants.enderio.EnderIOTab;
 import crazypants.enderio.ModObject;
 import crazypants.enderio.conduit.IConduitBundle;
 import crazypants.enderio.conduit.power.PowerConduit;
 import crazypants.enderio.machine.painter.BasicPainterTemplate;
 import crazypants.enderio.machine.painter.PainterUtil;
 import crazypants.enderio.power.ICapacitor;
 
 public class ItemConduitFacade extends Item {
 
   public static ItemConduitFacade create() {
     ItemConduitFacade result = new ItemConduitFacade();
     result.init();
     return result;
   }
 
   protected Icon overlayIcon;
 
   protected ItemConduitFacade() {
     super(ModObject.itemConduitFacade.id);
     setCreativeTab(EnderIOTab.tabEnderIO);
     setUnlocalizedName(ModObject.itemConduitFacade.unlocalisedName);
     setMaxStackSize(64);
   }
 
   protected void init() {
     LanguageRegistry.addName(this, ModObject.itemConduitFacade.name);
     GameRegistry.registerItem(this, ModObject.itemConduitFacade.unlocalisedName);
   }
 
   @Override
   public void registerIcons(IconRegister iconRegister) {
    itemIcon = iconRegister.registerIcon("enderio:ConduitFacade");
     overlayIcon = iconRegister.registerIcon("enderio:conduitFacadeOverlay");
   }
 
   public Icon getOverlayIcon() {
     return overlayIcon;
   }
 
   @Override
   public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float par8,
       float par9, float par10) {
 
     if (world.isRemote) {
       return true;
     }
 
     ForgeDirection dir = ForgeDirection.values()[side];
     int placeX = x + dir.offsetX;
     int placeY = y + dir.offsetY;
     int placeZ = z + dir.offsetZ;
 
     if (player.canPlayerEdit(placeX, placeY, placeZ, side, itemStack) && world.isAirBlock(placeX, placeY, placeZ)
         && PainterUtil.getSourceBlockId(itemStack) > 0) {
 
       world.setBlock(placeX, placeY, placeZ, EnderIO.blockConduitBundle.blockID);
       IConduitBundle bundle = (IConduitBundle) world.getBlockTileEntity(placeX, placeY, placeZ);
       bundle.setFacadeId(PainterUtil.getSourceBlockId(itemStack));
       bundle.setFacadeMetadata(PainterUtil.getSourceBlockMetadata(itemStack));
       if (!player.capabilities.isCreativeMode) {
         itemStack.stackSize--;
       }
       return true;
     }
 
     return false;
   }
 
   @Override
   @SideOnly(Side.CLIENT)
   public boolean isFull3D() {
     return true;
   }
 
   @Override
   public void onCreated(ItemStack itemStack, World world, EntityPlayer player) {
     if (PainterUtil.getSourceBlock(itemStack) == null) {
       PainterUtil.setSourceBlock(itemStack, ModObject.itemConduitFacade.id, 0);
     }
   }
 
   public ItemStack createItemStackForSourceBlock(int id, int itemDamage) {
     if (id < 1) {
       id = ModObject.blockConduitFacade.id;
     }
     ItemStack result = new ItemStack(itemID, 1, 0);
     PainterUtil.setSourceBlock(result, id, itemDamage);
     return result;
   }
 
   @Override
   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack item, EntityPlayer par2EntityPlayer, List list, boolean par4) {
     super.addInformation(item, par2EntityPlayer, list, par4);    
     list.add(PainterUtil.getTooltTipText(item));    
   }
 
   public static final class FacadePainterRecipe extends BasicPainterTemplate {
 
     public FacadePainterRecipe() {
       super(ModObject.itemConduitFacade.actualId);
     }
 
   }
 
 }
