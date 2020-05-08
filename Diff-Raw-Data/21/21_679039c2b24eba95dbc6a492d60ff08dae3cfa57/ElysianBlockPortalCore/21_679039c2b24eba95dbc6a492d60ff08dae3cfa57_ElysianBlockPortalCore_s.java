 package mods.elysium.dimension.portal;
 
 import java.util.Random;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import mods.elysium.DefaultProps;
 import mods.elysium.Elysium;
 import mods.elysium.block.ElysianBlockContainer;
 import mods.elysium.client.particle.ElysianEntityFX;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.ItemStack;
 import net.minecraft.src.ModLoader;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.world.Explosion;
 import net.minecraft.world.World;
 
 public class ElysianBlockPortalCore extends ElysianBlockContainer
 {
 	public ElysianBlockPortalCore(int id, Material mat)
 	{
 		super(id, mat);
 		this.setBlockBounds(0.5F, 1F, 0.5F, 0.5F, 1F, 0.5F);
 		this.setTickRandomly(true);
 		this.setCreativeTab(null);
 	}
 	
 	@Override
 	public AxisAlignedBB getCollisionBoundingBoxFromPool(World wolrd, int x, int y, int z)
 	{
 		return null;
 	}
 	
 	@Override
 	public boolean isOpaqueCube()
 	{
 		return false;
 	}
 	
 	@Override
 	public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion explosion)
 	{
 		world.setBlock(x, y, z, this.blockID);
 	}
 	
 	@Override
 	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
 	{
 		if((entity.riddenByEntity == null) && (entity.ridingEntity == null))
 		{
 			if(entity instanceof EntityPlayerMP)
 			{
 				EntityPlayerMP player = (EntityPlayerMP) entity;
 				ElysianTileEntityPortal tile = (ElysianTileEntityPortal)world.getBlockTileEntity(x, y, z);
 				
 				if((!world.isRemote) && (tile.timebeforetp == DefaultProps.ticksbeforeportalteleport-1))
 				{
 					player.addChatMessage(getWorldMessage(player));
 				}
 				
 				if(tile.timebeforetp == 0)
 				{
 					tile.timebeforetp = -1;
 					
 					if(player.dimension == Elysium.DimensionID)
 					{
 						player.mcServer.getConfigurationManager().transferPlayerToDimension(player, 0, new ElysianTeleporter(player.mcServer.worldServerForDimension(0)));
 					}
 					else
 					{
 						player.mcServer.getConfigurationManager().transferPlayerToDimension(player, Elysium.DimensionID, new ElysianTeleporter(player.mcServer.worldServerForDimension(Elysium.DimensionID)));
 					}
 				}
 				else if(player.prevPosY == player.posY)
 				{
 					tile.wasCollided = true;
 					if(tile.timebeforetp == -1) tile.timebeforetp = DefaultProps.ticksbeforeportalteleport;
 				}
 			}
 		}
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void randomDisplayTick(World world, int x, int y, int z, Random random)
 	{
 		double rad = ((ElysianTileEntityPortal)world.getBlockTileEntity(x, y, z)).radius;
 		
 		int part = random.nextInt(20);
 		for(int i = 0; i < part; i++)
 		{
 			int deg = random.nextInt(360);
 			ElysianEntityFX entityfx = new ElysianEntityFX(world, x+rad*Math.cos(Math.toRadians(deg))+0.5D, y+random.nextInt(100), z+rad*Math.sin(Math.toRadians(deg))+0.5D, 0, 0.1D, 0);
 			entityfx.setRBGColorF(1F, 1F, 0F);
 			entityfx.setBrightness(125);
			entityfx.setTextureFile("elysium:/textures/misc/particles/beam.png");
 			ModLoader.getMinecraftInstance().effectRenderer.addEffect(entityfx);
 		}
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world)
 	{
 		return new ElysianTileEntityPortal();
 	}
 	
 	/**
 	 * Returns the correct warping message for elysium depending on where the player is.
 	 * @param player
 	 * @return Message to where the player is warping
 	 */
 	public String getWorldMessage(EntityPlayer player)
 	{
 		if(player.dimension == 0)
 		{
 			return "Teleporting to the " + EnumChatFormatting.GRAY + "Elysium";
 		}
 		else
 		{
 			return "Teleporting to the " + EnumChatFormatting.GRAY + "Overworld";
 		}
 	}
 }
