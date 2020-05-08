 package emasher.api;
 
 import emasher.sockets.SocketsMod;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.ChatMessageComponent;
 import net.minecraft.util.ChunkCoordinates;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.event.ForgeSubscribe;
 
 import java.util.List;
 
 public class Util
 {
 	public static EntityPlayer createFakePlayer(World world, int x, int y, int z)
 	{
 		EntityPlayer player = new EntityPlayer(world, "[Engineer's Toolbox]") {
 			@Override
 			public void sendChatToPlayer(ChatMessageComponent var1) {
 			}
 
 			@Override
 			public boolean canCommandSenderUseCommand(int var1, String var2) {
 				return false;
 			}
 
 			@Override
 			public ChunkCoordinates getPlayerCoordinates() {
 				return null;
 			}
 		};
 		
 		player.posX = x;
 		player.posY = y;
 		player.posZ = z;
 		player.prevPosX = x;
 		player.prevPosY = y;
 		player.prevPosZ = z;
 		
 		return player;
 	}
 
     public static boolean swapBlocks(World world, int x, int y, int z, int nx, int ny, int nz)
     {
         int id1 = world.getBlockId(x, y ,z);
         int meta1 = world.getBlockMetadata(x, y, z);
         int id2 = world.getBlockId(nx, ny, nz);
         int meta2 = world.getBlockMetadata(nx, ny, nz);
 
         if(id1 == SocketsMod.miniPortal.blockID) return false;
         if(id2 == SocketsMod.miniPortal.blockID) return false;
 
         Block b1 = Block.blocksList[id1];
         Block b2 = Block.blocksList[id2];
 
         if(b1 != null && b1.blockHardness < 0) return false;
         if(b2 != null && b2.blockHardness < 0) return false;
 
         TileEntity te1 = world.getBlockTileEntity(x, y, z);
         NBTTagCompound nbt1 = new NBTTagCompound();
         if(te1 != null) te1.writeToNBT(nbt1);
         world.setBlockToAir(x, y, z);
         world.removeBlockTileEntity(x, y, z);
 
 
         TileEntity te2 = world.getBlockTileEntity(nx, ny, nz);
         NBTTagCompound nbt2 = new NBTTagCompound();
         if(te2 != null) te2.writeToNBT(nbt2);
         world.setBlockToAir(nx, ny, nz);
         world.removeBlockTileEntity(nx, ny, nz);
 
         world.setBlock(x, y, z, id2, meta2, 3);
         if(b2 instanceof BlockContainer)
         {
             TileEntity te = world.getBlockTileEntity(x, y, z);
             if(te != null)
             {
                 te.readFromNBT(nbt2);
                 te.xCoord = x;
                 te.yCoord = y;
                 te.zCoord = z;
 
                 if(te instanceof SocketTileAccess) for(int i = 0; i < 6; i++)
                 {
                     ForgeDirection d = ForgeDirection.getOrientation(i);
                     SocketModule m = ((SocketTileAccess) te).getSide(d);
                     m.onSocketPlaced(((SocketTileAccess) te).getConfigForSide(d), (SocketTileAccess)te, d);
                 }
             }
         }
 
         world.setBlock(nx, ny, nz, id1, meta1, 3);
         if(b1 instanceof BlockContainer)
         {
             TileEntity te = world.getBlockTileEntity(nx, ny, nz);
             if(te != null)
             {
                 te.readFromNBT(nbt1);
                 te.xCoord = nx;
                 te.yCoord = ny;
                 te.zCoord = nz;
 
                 if(te instanceof SocketTileAccess) for(int i = 0; i < 6; i++)
                 {
                     ForgeDirection d = ForgeDirection.getOrientation(i);
                     SocketModule m = ((SocketTileAccess) te).getSide(d);
                     m.onSocketPlaced(((SocketTileAccess) te).getConfigForSide(d), (SocketTileAccess)te, d);
                 }
             }
         }
 
         return true;
     }
 
     public static boolean canMoveBlock(World world, int x, int y, int z, int nx, int ny, int nz)
     {
         if(ny >= 255 || ny <= 0) return false;
         if(! world.isAirBlock(nx, ny, nz)) return false;
         int id = world.getBlockId(x, y, z);
         if(id == SocketsMod.miniPortal.blockID) return false;
         Block b = Block.blocksList[id];
         return ! (b != null && b.blockHardness < 0);
     }
 
     public static boolean moveBlock(World world, int x, int y, int z, int nx, int ny, int nz)
     {
         if(ny >= 255 || ny <= 0) return false;
         if(! world.isAirBlock(nx, ny, nz)) return false;
         int id = world.getBlockId(x, y, z);
         if(id == SocketsMod.miniPortal.blockID) return false;
         Block b = Block.blocksList[id];
         if(b != null && b.blockHardness < 0) return false;
         int meta = world.getBlockMetadata(x, y, z);
 
         TileEntity te = world.getBlockTileEntity(x, y, z);
         NBTTagCompound nbt = new NBTTagCompound();
         if(te != null)
         {
             te.writeToNBT(nbt);
             world.removeBlockTileEntity(x, y, z);
         }
 
         world.setBlockToAir(x, y, z);
 
         world.setBlock(nx, ny, nz, id, meta, 3);
 
        List ents = world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getAABBPool().getAABB(x - 1, y, z - 1, x, y + 2, z));
         for(Object e: ents) {
             if(e instanceof Entity)
             {
                 Entity ent = (Entity)e;
                System.out.println("Test"); //TODO
                ent.setPosition(ent.posX + (nx - x), ent.posY + (ny - y), ent.posZ + (nz - z));
             }
         }
 
         te = world.getBlockTileEntity(nx, ny, nz);
         if(te != null)
         {
             te.readFromNBT(nbt);
             te.xCoord = nx;
             te.yCoord = ny;
             te.zCoord = nz;
 
             if(te instanceof SocketTileAccess) for(int i = 0; i < 6; i++)
             {
                 ForgeDirection d = ForgeDirection.getOrientation(i);
                 SocketModule m = ((SocketTileAccess) te).getSide(d);
                 m.onSocketPlaced(((SocketTileAccess) te).getConfigForSide(d), (SocketTileAccess)te, d);
             }
         }
 
         return true;
     }
 	
 
 }
