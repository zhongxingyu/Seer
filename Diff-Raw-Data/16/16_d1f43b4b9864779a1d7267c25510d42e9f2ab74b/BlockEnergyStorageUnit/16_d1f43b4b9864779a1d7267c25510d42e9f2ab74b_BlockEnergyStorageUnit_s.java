 package shadowhax.modjam.block;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.ITileEntityProvider;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import shadowhax.modjam.ModJam;
 import shadowhax.modjam.block.tile.TileEntityEnergyStorage;
 import shadowhax.modjam.energy.IEnergyStorageBlock;
 import shadowhax.modjam.item.ItemRefinedCrystal;
 
 public class BlockEnergyStorageUnit extends Block implements ITileEntityProvider {
 
     public BlockEnergyStorageUnit(int id) {
 
         super(id, Material.rock);
         this.setCreativeTab(ModJam.tab);
     }
 
     @Override
     public TileEntity createNewTileEntity(World world) {
 
         return new TileEntityEnergyStorage();
     }
 
     @Override
     public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
 
         TileEntity tile = world.getBlockTileEntity(x, y, z);
 
         if(tile != null && tile instanceof IEnergyStorageBlock) {
 
             TileEntityEnergyStorage theTile = (TileEntityEnergyStorage)tile;
             if(!world.isRemote) {
 
                 if(player.isSneaking()) {
 
                     System.out.println(theTile.getCurrentEnergyStored());
//                    System.out.println(theTile.energyType.energyName);
                 } else {
 
                     Item crystal = null;
 
                     if(player.getCurrentEquippedItem() != null) {
 
                         crystal = player.getCurrentEquippedItem().getItem();
 
                         if(crystal != null && crystal instanceof ItemRefinedCrystal) {
 
                             if(!(theTile.getCurrentEnergyStored() + 200 > theTile.getMaxEnergyStored())) {
 
                                 player.swingItem();
                                 theTile.modifyEnergy(200);
                                 --player.getCurrentEquippedItem().stackSize;
                             }
                             return true;
                         }
                     }
 //                    theTile.modifyEnergy(200);
                 }
             }
         }
 
         return false;
     }
 }
