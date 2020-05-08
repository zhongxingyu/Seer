 package huntingTraps.common;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.world.World;
 
 public class FakeGrass extends Block
 {
 	int b = 1;
 	
 	public FakeGrass(int i, int j, Material ground)
 	{
 		super(i, Material.ground);
         this.setBlockBounds(0.0F, 0.99F, 0.0F, 1.0F, 1.0F, 1.0F);
 	}
 	
 	public void setBlockBoundsForItemRender(World world, int x, int y, int z)
     {
 		float f = 0.0625F;
         this.setBlockBounds(x + f, y + f, z + f, x + 1 - f, y + 1 - f, z + 1 - f);
     }
 	
 	public boolean isOpaqueCube()
 	{
 		return false;
 	}
 	
 	public boolean renderAsNormalBlock()
 	{
 		return false;
 	}
 	
 	public boolean blocksMovement()
     {
         return false;
     }
 }
