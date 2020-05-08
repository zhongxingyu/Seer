 package tcc.MotherOfPearl.items;
 
import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import tcc.MotherOfPearl.MotherOfPearl;
 import tcc.MotherOfPearl.blocks.ModBlocks;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import darkevilmac.MotherOfPearl.lib.Reference;
 
 public class ItemPortalPlacer extends Item {
     public ItemPortalPlacer(int par1) {
         super(par1);
         setCreativeTab(MotherOfPearl.tabMoP);
         setUnlocalizedName("pearlPortalPlacer");
     }
 
     public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int X, int Y, int Z, int par7, float par8, float par9, float par10) {
         if (!par3World.isRemote) {
             int direction = MathHelper.floor_double(par2EntityPlayer.rotationYaw * 4.0F / 360.0F + 0.5D) & 0x3;
             if ((direction == 1) || (direction == 3)) {
                 for (int y = 1; y < 5; y++) {
                     for (int z = -1; z < 2; z++) {
                         if (par3World.getBlockId(X, Y + y, Z + z) != 0) {
                             par2EntityPlayer.addChatMessage("No room for a portal.");
                             return false;
                         }
                     }
                 }
                 par3World.setBlock(X, Y + 1, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X + 1, Y + 1, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X, Y + 2, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X + 1, Y + 2, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X + 2, Y + 2, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X - 1, Y + 2, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X + 3, Y + 3, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X - 2, Y + 3, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X + 2, Y + 3, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X - 1, Y + 3, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X + 3, Y + 4, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X - 2, Y + 4, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X + 2, Y + 4, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X - 1, Y + 4, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X + 3, Y + 5, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X - 2, Y + 5, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X + 2, Y + 5, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X - 1, Y + 5, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X + 2, Y + 6, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X - 1, Y + 6, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X, Y + 7, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X + 1, Y + 7, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X, Y + 6, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X + 1, Y + 6, Z, ModBlocks.pearlstone.blockID);
 
                 par3World.setBlock(X, Y + 3, Z, ModBlocks.pearlFire.blockID);
             } else {
                 for (int y = 1; y < 5; y++) {
                     for (int x = -1; x < 2; x++) {
                         if (par3World.getBlockId(X + x, Y + y, Z) != 0) {
                             par2EntityPlayer.addChatMessage("No room for a portal.");
                             return false;
                         }
                     }
                 }
                 par3World.setBlock(X, Y + 1, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X + 1, Y + 1, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X, Y + 2, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X + 1, Y + 2, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X + 2, Y + 2, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X - 1, Y + 2, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X + 3, Y + 3, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X - 2, Y + 3, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X + 2, Y + 3, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X - 1, Y + 3, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X + 3, Y + 4, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X - 2, Y + 4, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X + 2, Y + 4, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X - 1, Y + 4, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X + 3, Y + 5, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X - 2, Y + 5, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X + 2, Y + 5, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X - 1, Y + 5, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X + 2, Y + 6, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X - 1, Y + 6, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X, Y + 7, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X + 1, Y + 7, Z, ModBlocks.blackPearlstone.blockID);
                 par3World.setBlock(X, Y + 6, Z, ModBlocks.pearlstone.blockID);
                 par3World.setBlock(X + 1, Y + 6, Z, ModBlocks.pearlstone.blockID);
 
                 par3World.setBlock(X, Y + 3, Z, ModBlocks.pearlFire.blockID);
             }
             return true;
         }
         return true;
     }
 
    @Override
     @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister) {
        itemIcon = par1IconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":" + this.getUnlocalizedName().substring(5));
     }
 }
