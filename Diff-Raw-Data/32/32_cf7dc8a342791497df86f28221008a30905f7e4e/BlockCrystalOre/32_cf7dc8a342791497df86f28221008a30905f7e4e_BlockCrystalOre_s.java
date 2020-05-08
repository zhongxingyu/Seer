 package shadowhax.crystalluscraft.block;
 
 
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import shadowhax.crystalluscraft.CrystallusCraft;
 import shadowhax.crystalluscraft.block.tile.TileEntityCrystalOre;
 
 import java.util.List;
 import java.util.Random;
 
 public class BlockCrystalOre extends BlockContainer {
 
     public BlockCrystalOre(int id) {
 
         super(id, Material.rock);
         this.setLightOpacity(0);
         this.setTickRandomly(true);
         this.setCreativeTab(CrystallusCraft.tab);
     }
 
     @Override
     public TileEntity createNewTileEntity(World world) {
 
         return new TileEntityCrystalOre();
     }
 
     @Override
     public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List) {
 
         for(int i = 0; i < 6; i++) {
 
             par3List.add(new ItemStack(par1, 1, i));
         }
     }
 
     @Override
     public boolean isOpaqueCube() {
         return false;
     }
 
     @Override
     public boolean renderAsNormalBlock() {
         return false;
     }
 
     @Override
     public int getRenderType() {
 
         return -1;
     }
 
 //    @Override
 //    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
 //
 //        TileEntityCrystalOre tile = (TileEntityCrystalOre)world.getBlockTileEntity(x, y, z);
 //
 //        if(tile != null) {
 //
 //            if(tile.growthStage != 1) {
 //
 //                tile.growthStage = 1;
 //            }
 //        }
 //
 //        return false;
 //    }
 
     @Override
    public void updateTick(World world, int x, int y, int z, Random par5Random) {
 
         System.out.println("tick");
 
         TileEntityCrystalOre tile = (TileEntityCrystalOre)world.getBlockTileEntity(x, y, z);
 
        if(tile != null) {
 
            System.out.println("tile is not null");
            tile.growthStage++;
            System.out.println(tile.growthStage);
            tile.validate();
            world.setBlockTileEntity(x, y, z, tile);
         }
     }
 }
