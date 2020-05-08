 package loecraftpack.common.blocks;
 
 import java.util.List;
 import java.util.Random;
 
 import loecraftpack.LoECraftPack;
 import loecraftpack.common.entity.EntityElectricBlock;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockZapAppleLeavesCharged extends BlockZapAppleLeaves
 {
 	
 	public BlockZapAppleLeavesCharged(int id) {
 		super(id);
 		this.setLightValue(0.5f);
 		this.setUnlocalizedName("leavesZapCharged");
 		this.appleType = 1;
 		bloomStage = 4;//no bloom stage
 		growthRate = 150/41;//average of one day(in-game) to reach stage 4
 	}
 	
 	@Override
 	public void dropBlockAsItemWithChance(World world, int xCoord, int yCoord, int zCoord, int meta, float par6, int fortune)
     {
         if (!world.isRemote)
         {
         	int j1 = saplingDropRate;
 
             if (fortune > 0)
             {
                 j1 -= (saplingDropRate/10) << fortune;
 
                 if (j1 < 10)
                 {
                     j1 = 10;
                 }
             }
 
             if (world.rand.nextInt(j1) == 0)
             {
                 int k1 = this.idDropped(meta, world.rand, fortune);
                 this.dropBlockAsItem_do(world, xCoord, yCoord, zCoord, new ItemStack(k1, 1, this.damageDropped(meta)));
             }
             
             if (!sheared)
     		{
             	int j = world.rand.nextInt(fortune + 1) - 1;
             	
                 if (j < 0)
                 {
                     j = 0;
                 }
                 
             	this.dropAppleThruTree(world, xCoord, yCoord, zCoord, new ItemStack(apple, j + 1, appleType));
     		}
             //reset sheared
             sheared = false;
         }
     }
 	
 	@Override
 	public void attemptGrow(World world, int xCoord, int yCoord, int zCoord, Random random)
 	{
 		int meta = world.getBlockMetadata(xCoord, yCoord, zCoord);
 		if ((meta & 4) == 0)
         {
         	//chance to weaken
 			if (random.nextDouble()*(growthRate) <= 1)
         	{
         		if (meta == 3)
         		{
         			if (world.setBlock(xCoord, yCoord, zCoord, LoECraftPack.blockZapAppleLeaves.blockID, LoECraftPack.blockZapAppleLeaves.bloomStage, 2))
         				tellClientOfChange(world, xCoord, yCoord, zCoord, LoECraftPack.blockZapAppleLeaves.blockID);
     			}
         		else
         		{
         			if (world.setBlock(xCoord, yCoord, zCoord, this.blockID, meta + 1, 2))
         				tellClientOfChange(world, xCoord, yCoord, zCoord, this.blockID);
         		}
         	}
         }
 	}
 	
 	@Override
 	public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z)
     {
		for (Object spark : world.getEntitiesWithinAABB(EntityElectricBlock.class, AxisAlignedBB.getBoundingBox(x, y, z, x+1, y+1, z+1)))
			((EntityElectricBlock)spark).setDead(); //Remove spark effect entities
		
 		int meta = world.getBlockMetadata(x, y, z);
 		if (!sheared &&  (meta&4)==0)
 		{
 			return world.setBlock(x, y, z, LoECraftPack.blockZapAppleLeaves.blockID, meta&12, 2);
 		}
         return world.setBlockToAir(x, y, z);
     }
 	
 	//TODO add spark effect, when charged
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void randomDisplayTick(World world, int xCoord, int yCoord, int zCoord, Random random)
     {
 		//charged apple effect
		if (random.nextInt(30) <= 25)
 		{
 			//spawn electric animation
 			EntityElectricBlock eleField = new EntityElectricBlock(world);
 			eleField.setPosition(xCoord+0.5, yCoord-0.1, zCoord+0.5);
 			world.spawnEntityInWorld(eleField);
 		}
 				
 		//water droplet code
         if (world.canLightningStrikeAt(xCoord, yCoord + 1, zCoord) && !world.doesBlockHaveSolidTopSurface(xCoord, yCoord - 1, zCoord) && random.nextInt(15) == 1)
         {
             double d0 = (double)((float)xCoord + random.nextFloat());
             double d1 = (double)yCoord - 0.05D;
             double d2 = (double)((float)zCoord + random.nextFloat());
             world.spawnParticle("dripWater", d0, d1, d2, 0.0D, 0.0D, 0.0D);
         }
     }
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
     {
         par3List.add(new ItemStack(par1, 1, 0));
     }
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister)
 	{
 		icon[0] = iconRegister.registerIcon("loecraftpack:trees/leaves_zapapple_charge");
 		icon[1] = iconRegister.registerIcon("loecraftpack:trees/leaves_zapapple_charge_opaque");
 	}
 }
