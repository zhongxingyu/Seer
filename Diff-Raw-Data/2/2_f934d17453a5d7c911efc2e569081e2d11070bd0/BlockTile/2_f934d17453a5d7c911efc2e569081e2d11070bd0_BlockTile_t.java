 package steamcraft.blocks;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 
 public class BlockTile extends Block{
     public BlockTile() {
         super(Material.rock);
         setResistance(10F);
        setStepSound(Block.soundTypeStone);
     }
 }
