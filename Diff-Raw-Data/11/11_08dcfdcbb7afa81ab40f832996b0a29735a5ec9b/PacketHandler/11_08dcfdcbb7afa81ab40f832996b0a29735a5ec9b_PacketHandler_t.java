 package mods.firstspring.advfiller;
 
 import mods.firstspring.advfiller.lib.BuildCraftProxy;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import buildcraft.api.core.LaserKind;
 
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteStreams;
 
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.Player;
 
 public class PacketHandler implements IPacketHandler {
 	@Override
 	public void onPacketData(INetworkManager network, Packet250CustomPayload packet, Player player)
 	{
 		//Stringを==で比較しちゃいけないよ、俺は迂闊だった
 		if(packet.channel.equals("advfiller_client")){
 			int minX,minY,minZ,maxX,maxY,maxZ,tileX,tileY,tileZ,left,right,up,down,forward,type;
 			boolean loop,finished,disabled,iterate,drop;
 			ByteArrayDataInput dat = ByteStreams.newDataInput(packet.data);
 			minX = dat.readInt();
 			minY = dat.readInt();
 			minZ = dat.readInt();
 			maxX = dat.readInt();
 			maxY = dat.readInt();
 			maxZ = dat.readInt();
 			tileX = dat.readInt();
 			tileY = dat.readInt();
 			tileZ = dat.readInt();
 			left = dat.readInt();
 			right = dat.readInt();
 			up = dat.readInt();
 			down = dat.readInt();
 			forward = dat.readInt();
 			type = dat.readInt();
 			loop = dat.readBoolean();
 			finished = dat.readBoolean();
 			disabled = dat.readBoolean();
 			iterate = dat.readBoolean();
 			drop = dat.readBoolean();
 			World world = CommonProxy.proxy.getWorld();
 			TileEntity tile = world.getBlockTileEntity(tileX, tileY, tileZ);
 			if(tile instanceof TileAdvFiller){
 				TileAdvFiller filler = (TileAdvFiller)tile;
 				filler.fromX = minX;
 				filler.fromY = minY;
 				filler.fromZ = minZ;
 				filler.toX = maxX;
 				filler.toY = maxY;
 				filler.toZ = maxZ;
 				if(filler.bcLoaded){
 					BuildCraftProxy.proxy.getBox(filler).deleteLasers();
 					BuildCraftProxy.proxy.getBox(filler).initialize(minX,minY,minZ,maxX,maxY,maxZ);
 					if(AdvFiller.bcFrameRenderer)
 						BuildCraftProxy.proxy.getBox(filler).createLasers(world, LaserKind.Stripes);
 				}
 				filler.setArea(left,right,up,down,forward,type);
 				filler.loopMode = loop;
 				filler.finished = finished;
 				filler.disabled = disabled;
 				filler.removeModeIteration = iterate;
 				filler.removeModeDrop = drop;
 			}
 			//マシンのフロントの表示を更新する
 			world.markBlockForUpdate(tileX, tileY, tileZ);
 		}
 		if(packet.channel.equals("advfiller_server")){
 			int x,y,z,left,right,up,down,forward,type;
 			boolean loop,iterate,drop;
 			ByteArrayDataInput dat = ByteStreams.newDataInput(packet.data);
 			x = dat.readInt();
 			y = dat.readInt();
 			z = dat.readInt();
 			left = dat.readInt();
 			if(left > AdvFiller.maxDistance)
 				left = AdvFiller.maxDistance;
 			right = dat.readInt();
 			if(right > AdvFiller.maxDistance)
 				right = AdvFiller.maxDistance;
 			up = dat.readInt();
 			if(up > AdvFiller.maxDistance)
 				up = AdvFiller.maxDistance;
 			down = dat.readInt();
 			if(down > AdvFiller.maxDistance)
 				down = AdvFiller.maxDistance;
 			forward = dat.readInt();
 			if(forward > AdvFiller.maxDistance)
 				forward = AdvFiller.maxDistance;
 			type = dat.readInt();
 			loop = dat.readBoolean();
 			iterate = dat.readBoolean();
 			drop = dat.readBoolean();
 			EntityPlayerMP entityplayer = (EntityPlayerMP)player;
 			World world = entityplayer.worldObj;
 			TileEntity tile = world.getBlockTileEntity(x, y, z);
 			if(tile instanceof TileAdvFiller){
 				TileAdvFiller filler = (TileAdvFiller)tile;
 				filler.player = entityplayer;
 				filler.doLoop = false;
 				filler.setArea(left,right,up,down,forward,type);
 				filler.loopMode = loop;
 				filler.removeModeIteration = iterate;
 				filler.removeModeDrop = drop;
 				//止めても問題ないはず
				if(filler.initializeThread != null)
					filler.initializeThread.stop();
 				filler.preInit();
 				world.markBlockForUpdate(x, y, z);
 			}
 		}
 	}
 }
