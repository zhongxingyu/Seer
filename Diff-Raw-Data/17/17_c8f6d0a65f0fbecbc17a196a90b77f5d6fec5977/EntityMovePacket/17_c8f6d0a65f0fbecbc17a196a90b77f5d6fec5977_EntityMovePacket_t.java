 package hunternif.mc.dota2items.network;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteArrayDataOutput;
 
 import cpw.mods.fml.relauncher.Side;
 
 //NOTE I hope I can get rid of this packet
 public class EntityMovePacket extends CustomPacket {
 	private int entityID;
 	private double x;
 	private double y;
 	private double z;
 	
 	public EntityMovePacket() {}
 	
 	public EntityMovePacket(Entity entity) {
 		entityID = entity.entityId;
 		x = entity.posX;
 		y = entity.posY;
 		z = entity.posZ;
 	}
 
 	@Override
 	public void write(ByteArrayDataOutput out) {
 		out.writeInt(entityID);
 		out.writeDouble(x);
 		out.writeDouble(y);
 		out.writeDouble(z);
 	}
 
 	@Override
 	public void read(ByteArrayDataInput in) throws ProtocolException {
 		entityID = in.readInt();
 		x = in.readDouble();
 		y = in.readDouble();
 		z = in.readDouble();
 	}
 
 	@Override
 	public void execute(EntityPlayer player, Side side) throws ProtocolException {
 		if (side.isClient()) {
 			Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(entityID);
 			if (entity != null) {
 				entity.setPosition(x, y, z);
				entity.lastTickPosX = entity.prevPosX = x;
				entity.lastTickPosY = entity.prevPosY = y;
				entity.lastTickPosZ = entity.prevPosZ = z;
 			}
 		} else {
 			throw new ProtocolException("Cannot send this packet to the server!");
 		}
 	}
 }
