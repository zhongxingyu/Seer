 package me.furt.industrial.block;
 
 import me.furt.industrial.IndustrialInc;
 
 import org.bukkit.World;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.getspout.spoutapi.material.block.GenericCubeCustomBlock;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class BlockMiningMachine extends GenericCubeCustomBlock{
 
 	public BlockMiningMachine(IndustrialInc plugin) {
        super(plugin, "Mining Machine", "https://dl.dropboxusercontent.com/u/17392489/MiningMachine.png", 16);
         this.setRotate(true);
         this.setHardness(0.5F);
     }
  
     public void onNeighborBlockChange(World world, int x, int y, int z, int changedId) {
     }
  
     public void onBlockPlace(World world, int x, int y, int z) {
     }
  
     public void onBlockPlace(World world, int x, int y, int z, LivingEntity living) {
     }
  
     public void onBlockDestroyed(World world, int x, int y, int z) {
     }
  
     public boolean onBlockInteract(World world, int x, int y, int z, SpoutPlayer player) {
         return true;
     }
  
     public void onEntityMoveAt(World world, int x, int y, int z, Entity entity) {
     }
  
     public void onBlockClicked(World world, int x, int y, int z, SpoutPlayer player) {
     }
  
     public boolean isProvidingPowerTo(World world, int x, int y, int z, BlockFace face) {
         return false;
     }
  
     public boolean isIndirectlyProvidingPowerTo(World world, int x, int y, int z, BlockFace face) {
         return false;
     }
 }
