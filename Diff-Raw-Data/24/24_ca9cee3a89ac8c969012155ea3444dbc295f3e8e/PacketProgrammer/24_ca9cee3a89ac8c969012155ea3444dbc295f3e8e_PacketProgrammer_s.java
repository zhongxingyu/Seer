 package obscuron.mkin.network.packet;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 
 import net.minecraft.item.ItemStack;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import net.minecraftforge.common.DimensionManager;
 import obscuron.mkin.item.ItemCard;
 import obscuron.mkin.lib.Reference;
 import obscuron.mkin.tileentity.TileProgrammer;
 import obscuron.mkin.util.NBTWrapper;
 import cpw.mods.fml.common.network.PacketDispatcher;
 
 public class PacketProgrammer extends KineticPacket {
     
     public static final byte packetID = 0;
     
     public int dimension;
     
     public int x;
     public int y;
     public int z;
     
     public byte typeState;
     
     @Override
     public void readInfo(DataInputStream inputStream) throws Exception {
         dimension = inputStream.readInt();
         
         x = inputStream.readInt();
         y = inputStream.readInt();
         z = inputStream.readInt();
         
         typeState = inputStream.readByte();
     }
     
     public void readInfo(TileProgrammer tileProgrammer, byte typeState) {
         dimension = tileProgrammer.getWorldObj().provider.dimensionId;
         
         x = tileProgrammer.xCoord;
         y = tileProgrammer.yCoord;
         z = tileProgrammer.zCoord;
         
         this.typeState = typeState;
     }
     
     @Override
     public void execute() {
         World world = DimensionManager.getWorld(dimension);
         TileEntity tile = world.getBlockTileEntity(x, y, z);
         if (tile instanceof TileProgrammer) {
             TileProgrammer tileProgrammer = (TileProgrammer) tile;
             ItemStack itemStack = tileProgrammer.getStackInSlot(0);
             ItemStack card = tileProgrammer.getStackInSlot(1);
             if (card != null && itemStack != null) {
                 NBTWrapper tags = new NBTWrapper(card, ItemCard.TAG_NAME);
                if (tags.getByte("id") == 0) {
                    tags.setByte("id", (byte) (typeState + 1));
                    tags.setItem("itemInfo", itemStack);
                    tileProgrammer.onInventoryChanged();
                }
             }
         }
     }
 
     @Override
     public void sendPacket() {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         DataOutputStream outputStream = new DataOutputStream(bos);
         
         try {
             outputStream.writeByte(packetID);
             outputStream.writeInt(dimension);
             outputStream.writeInt(x);
             outputStream.writeInt(y);
             outputStream.writeInt(z);
             outputStream.writeByte(typeState);
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         
         Packet250CustomPayload packet = new Packet250CustomPayload();
         packet.channel = Reference.CHANNEL_NAME;
         packet.data = bos.toByteArray();
         packet.length = bos.size();
         
         PacketDispatcher.sendPacketToServer(packet);
     
     }
 
     
 
 }
