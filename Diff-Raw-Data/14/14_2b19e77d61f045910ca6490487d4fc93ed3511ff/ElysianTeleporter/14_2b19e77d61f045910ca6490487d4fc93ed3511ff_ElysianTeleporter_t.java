 package mods.elysium.dimension.portal;
 
 import java.awt.Label;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 import mods.elysium.DefaultProps;
 import mods.elysium.Elysium;
 import net.minecraft.block.Block;
 import net.minecraft.entity.Entity;
 import net.minecraft.server.management.LowerStringMap;
 import net.minecraft.util.ChunkCoordinates;
 import net.minecraft.util.Direction;
 import net.minecraft.util.LongHashMap;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.ChunkCoordIntPair;
 import net.minecraft.world.PortalPosition;
 import net.minecraft.world.Teleporter;
 import net.minecraft.world.WorldServer;
 
 public class ElysianTeleporter extends Teleporter
 {
 	private final WorldServer worldServer;
     private final Random random;
     public static ArrayList<ElysianPortalPosition> portals = new ArrayList<ElysianPortalPosition>();
 	
 	public ElysianTeleporter(WorldServer worldServer)
 	{
 		super(worldServer);
 		this.worldServer = worldServer;
 		this.random = new Random(worldServer.getSeed());
 	}
 	
 	@Override
 	public void placeInPortal(Entity entity, double par2, double par4, double par6, float par8)
 	{
 		worldServer.updateEntities();
 		if(!this.placeInExistingPortal(entity, par2, par4, par6, par8))
 		{
 			this.makePortal(entity);
 		}
 	}
 	
 	@Override
 	public boolean placeInExistingPortal(Entity entity, double par2, double par4, double par6, float par8)
 	{
 		int x = MathHelper.floor_double(entity.posX);
 		int z = MathHelper.floor_double(entity.posZ);
 		int i;
 		for(i=0; i<portals.size(); i++)
 		{
 			if((portals.get(i).dim == entity.dimension) && (Math.abs(portals.get(i).posX-x) < DefaultProps.maxportaldistance) && (Math.abs(portals.get(i).posZ-z) < DefaultProps.maxportaldistance))
 			{
 				entity.motionX = entity.motionY = entity.motionZ = 0.0D;
 				int dx = random.nextInt(2);
 				if(dx == 0) dx = -1;
 				int dz = random.nextInt(2);
 				if(dz == 0) dz = -1;
 				entity.setPosition(portals.get(i).posX+0.5D+dx, portals.get(i).posY, portals.get(i).posZ+0.5D+dz);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean makePortal(Entity entity)
 	{
 		int x, y, z;
 		
 		boolean canBuild = false;
 		
 		while(true){
 			x = MathHelper.floor_double(entity.posX) + random.nextInt(DefaultProps.maxportaldistance+1) - DefaultProps.maxportaldistance/2;
 			z = MathHelper.floor_double(entity.posZ) + random.nextInt(DefaultProps.maxportaldistance+1) - DefaultProps.maxportaldistance/2;
 			y = worldServer.getTopSolidOrLiquidBlock(x, z)-1;
 			
 			//check if can build
 			int waterBellow = 0;
 			for(int a=-2; a <= 2; a++)
 			{
 				for(int b=-2; b <= 2; b++)
 				{
 					int id = worldServer.getBlockId(x+a, y, z+b);
 					if(id == 0 || id == Block.waterStill.blockID || id == Block.waterMoving.blockID || id == Elysium.waterStill.blockID || id == Elysium.waterMoving.blockID || id == Block.blockNetherQuartz.blockID)
 						waterBellow++;
 			
 				}
 			}
 			System.out.println(waterBellow);
 			if(waterBellow > 25/2)//More then the half is water bellow
 				continue;
 			else
 				canBuild = true;
 			//check end
 			
 			//get lowest air block underneath
 			int lowest = -1;
 			for (int i = -2; i <= 2; i++) {
 				for (int j = -2; j <= 2; j++) {
 					if(lowest == -1 || worldServer.getTopSolidOrLiquidBlock(x+i, z+j) < lowest)
 						lowest = worldServer.getTopSolidOrLiquidBlock(x+i, z+j);
 				}
 			}
 			for (int j = y; j >= lowest; j--) {
 				for (int i = -2; i <= 2; i++) {
 					for (int k = -2; k <= 2; k++) {
						int block = worldServer.getBlockId(x+i, j, z+k);
						if(block == 0 || Block.blocksList[block].canBeReplacedByLeaves(worldServer, x+i, j, z+k)){
							worldServer.setBlock(x+i, j, z+k, Elysium.blockDirt.blockID);
 						}
 					}
 				}
 			}
 			
			worldServer.setBlock(x, y+9, z, Elysium.blockPortalCore.blockID);
			worldServer.setBlockMetadataWithNotify(x, y+9, z, 1, 0);
			
 			for(int i=-1; i <= 1; i++)
 			{
 				for(int j=-1; j <= 1; j++)
 				{
 					worldServer.setBlock(x+i, y+8, z+j, Block.blockNetherQuartz.blockID);
 					worldServer.setBlockMetadataWithNotify(x+i, y+8, z+j, 1, 0);
 					
 					worldServer.setBlock(x+i, y+6, z+j, Block.blockNetherQuartz.blockID);
 					worldServer.setBlockMetadataWithNotify(x+i, y+6, z+j, 2, 0);
 					worldServer.setBlock(x+i, y+5, z+j, Block.blockNetherQuartz.blockID);
 					worldServer.setBlockMetadataWithNotify(x+i, y+5, z+j, 2, 0);
 					
 					worldServer.setBlock(x+i, y+4, z+j, Block.blockGold.blockID);
 					
 					worldServer.setBlock(x+i, y+3, z+j, Block.blockNetherQuartz.blockID);
 					worldServer.setBlockMetadataWithNotify(x+i, y+3, z+j, 2, 0);
 					worldServer.setBlock(x+i, y+2, z+j, Block.blockNetherQuartz.blockID);
 					worldServer.setBlockMetadataWithNotify(x+i, y+2, z+j, 2, 0);
 				}
 			}
 			for(int i=-2; i <= 2; i++)
 			{
 				for(int j=-2; j <= 2; j++)
 				{
 					worldServer.setBlock(x+i, y+7, z+j, Block.blockNetherQuartz.blockID);
 					worldServer.setBlock(x+i, y+1, z+j, Block.blockNetherQuartz.blockID);
 				}
 			}
 			
 			
 
 			if(canBuild)
 				break;
 		}
 		entity.motionX = entity.motionY = entity.motionZ = 0.0D;
 		int dx = random.nextInt(2);
 		if(dx == 0) dx = -1;
 		int dz = random.nextInt(2);
 		if(dz == 0) dz = -1;
 		entity.setPosition(x+0.5D+dx, y+9, z+0.5D+dz);
 		
 		return true;
 	}
 }
