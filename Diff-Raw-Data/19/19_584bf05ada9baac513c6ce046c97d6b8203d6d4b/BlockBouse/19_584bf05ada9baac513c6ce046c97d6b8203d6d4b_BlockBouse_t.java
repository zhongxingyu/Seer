 package net.minecraft.src;
 
 public class BlockBouse extends Block
 {
 	protected BlockBouse(int i, int j) 
 	{
 		super(i, j, Material.ground);
 	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
     {
        float var5 = 0.125F;
        return AxisAlignedBB.getAABBPool().addOrModifyAABBInPool((double)par2, (double)par3, (double)par4, (double)(par2 + 1), (double)((float)(par3 + 1) - var5), (double)(par4 + 1));
     }

    public void onEntityCollidedWithBlock(World par1World, int par2, int par3, int par4, Entity par5Entity)
     {
        par5Entity.motionX *= 0.4D;
        par5Entity.motionZ *= 0.4D;
     }
 }
