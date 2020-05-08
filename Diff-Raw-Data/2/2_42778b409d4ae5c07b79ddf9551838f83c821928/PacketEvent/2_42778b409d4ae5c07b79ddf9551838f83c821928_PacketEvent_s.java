 package btwmods.network;
 
 import java.util.EventObject;
 
 import net.minecraft.src.EntityPlayerMP;
 import net.minecraft.src.NetServerHandler;
 import net.minecraft.src.Packet;
 
 public class PacketEvent extends EventObject {
 
 	public enum Type {
		SENT_TO_PLAYER, RECEIVED_FROM_PLAYER, SERVER_SENT, SERVER_RECIEVED
 	};
 
 	private EntityPlayerMP player = null;
 	private NetServerHandler netServerHandler = null;
 	private Packet packet;
 	private Type type;
 	
 	public EntityPlayerMP getPlayer() {
 		return player;
 	}
 	
 	public Packet getPacket() {
 		return packet;
 	}
 	
 	public Type getType() {
 		return type;
 	}
 	
 	public NetServerHandler getNetServerHandler() {
 		return netServerHandler;
 	}
 	
 	public static PacketEvent ReceivedPlayerPacket(EntityPlayerMP player, Packet packet, NetServerHandler netHandler) {
 		PacketEvent event = new PacketEvent(Type.RECEIVED_FROM_PLAYER, player, packet, netHandler);
 		return event;
 	}
 
 	public static PacketEvent SentPlayerPacket(EntityPlayerMP player, Packet packet, NetServerHandler netHandler) {
 		PacketEvent event = new PacketEvent(Type.SENT_TO_PLAYER, player, packet, netHandler);
 		return event;
 	}
 
 	private PacketEvent(Type type, EntityPlayerMP player, Packet packet, NetServerHandler netServerHandler) {
 		super(player);
 		this.type = type;
 		this.player = player;
 		this.packet = packet;
 		this.netServerHandler = netServerHandler;
 	}
 }
