 package net.medsouz.omg.net;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import net.medsouz.omg.OpenModularGuns;
import net.medsouz.omg.item.ItemGun;
 import net.minecraft.client.multiplayer.NetClientHandler;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.projectile.EntityArrow;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 
 public class PacketHandler implements IPacketHandler {
 
 	@Override
 	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
 		Side side = FMLCommonHandler.instance().getEffectiveSide();
 		ByteArrayInputStream bis = new ByteArrayInputStream(packet.data);
 		DataInputStream dis = new DataInputStream(bis);
 		if(side == Side.SERVER){
 			try {
 				if(dis.readInt() == 0){
 					if(player instanceof EntityPlayer){
 						EntityPlayer ep = (EntityPlayer)player;
						if(ep.getHeldItem() != null && ep.getHeldItem().getItem() instanceof ItemGun){
							if(OpenModularGuns.lastShots.containsKey(ep.username)){
								if(OpenModularGuns.lastShots.get(ep.username) > System.currentTimeMillis() - 900){
									System.out.println(ep.username + " is firing too fast!");
									return;
								}
 							}
							OpenModularGuns.lastShots.put(ep.username, System.currentTimeMillis());
							EntityArrow e = new EntityArrow(ep.worldObj, ep, 2.0f);
							ep.worldObj.spawnEntityInWorld(e);
 						}
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static void sendPacketShot(){
 		ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
 		DataOutputStream outputStream = new DataOutputStream(bos);
 		try{
 			outputStream.writeInt(0);//ID = 0
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		Packet250CustomPayload packet = new Packet250CustomPayload();
 		packet.channel = "omg";
 		packet.data = bos.toByteArray();
 		packet.length = bos.size();
 		PacketDispatcher.sendPacketToServer(packet);
 	}
 
 }
