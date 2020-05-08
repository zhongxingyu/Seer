 package net.minecraft.src;
 
 public class BlockBouse extends Block
 {
 	protected BlockBouse(int i, int j) 
 	{
 		super(i, j, Material.ground);
 	}
	
	public void onEntityCollidedWithBlock(World world, int i, int j, int k, Entity entity)
     {
        entity.motionX *= 0.50000000000000002D;
        entity.motionZ *= 0.50000000000000002D;
     }
	
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k)
     {
        float f = 0.125F;
        return AxisAlignedBB.getBoundingBoxFromPool(i, j, k, i + 1, (float)(j + 1) - f, k + 1);
     }
 }
