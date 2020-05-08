 package ccm.deathTimer.timerTypes;
 
 import ccm.deathTimer.server.EventTracker;
 import ccm.deathTimer.server.ServerTimer;
 import ccm.deathTimer.utils.lib.Archive;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import net.minecraft.command.ICommandSender;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.world.ChunkCoordIntPair;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  * Specific to deaths. Don't use for other stuff.
  *
  * @author Dries007
  */
 public class DeathTimer extends PointTimer
 {
     public static final String PREFIX = "ItemDespawn-";
     public static final int PACKETID = 2;
 
     public boolean isLoaded;
     public String username;
     public long chunkKey;

     public DeathTimer()
     {
     }
 
     public DeathTimer(final EntityPlayer player)
     {
         this.label = DeathTimer.PREFIX + player.username;
         this.time = 60 * 5;
         this.isLoaded = true;
         this.username = player.username;
         this.X = (int) player.posX;
         this.Y = (int) player.posY;
         this.Z = (int) player.posZ;
        this.dim = player.dimension;
         this.chunkKey = ChunkCoordIntPair.chunkXZ2Int(this.X / 16, this.Y / 16);
 
         ServerTimer.getInstance().addTimer(this);
     }
 
     @Override
     public void tick()
     {
         if (this.isLoaded) this.time--;
 
         if (this.time <= 0) EventTracker.ChuncksToTrackMap.remove(this.chunkKey, this);
     }
 
     @Override
     public void sendAutoUpdate()
     {
         final Player p = (Player) MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(this.username);
         if (p != null) PacketDispatcher.sendPacketToPlayer(this.getPacket(), p);
     }
 
     @Override
     public Packet250CustomPayload getPacket()
     {
         final ByteArrayOutputStream streambyte = new ByteArrayOutputStream();
         final DataOutputStream stream = new DataOutputStream(streambyte);
 
         try
         {
             stream.writeInt(DeathTimer.PACKETID);
 
             stream.writeUTF(this.getLabel());
             stream.writeInt(this.getTime());
 
             stream.writeBoolean(this.useSound());
             if (this.useSound())
             {
                 stream.writeUTF(this.getSoundName());
                 stream.writeFloat(this.getSoundVolume());
                 stream.writeFloat(this.getSoundPitch());
             }
 
             stream.writeInt(this.X);
             stream.writeInt(this.Y);
             stream.writeInt(this.Z);
             stream.writeInt(this.dim);
 
             stream.writeBoolean(this.isLoaded);
 
             stream.close();
             streambyte.close();
         }
         catch (final IOException e)
         {
             e.printStackTrace();
         }
 
         return PacketDispatcher.getPacket(Archive.MOD_CHANNEL_TIMERS, streambyte.toByteArray());
     }
 
     @Override
     public ITimerBase getUpdate(final DataInputStream stream) throws IOException
     {
         final DeathTimer data = new DeathTimer();
 
         data.label = stream.readUTF();
         data.time = stream.readInt();
 
         if (stream.readBoolean())
         {
             data.soundName = stream.readUTF();
             data.soundVolume = stream.readFloat();
             data.soundPitch = stream.readFloat();
         }
 
         data.X = stream.readInt();
         data.Y = stream.readInt();
         data.Z = stream.readInt();
         data.dim = stream.readInt();
 
         data.isLoaded = stream.readBoolean();
 
         return data;
     }
 
     @Override
     public ArrayList<String> getTimerString(final ICommandSender sender)
     {
         final ArrayList<String> text = super.getTimerString(sender);
         text.add(this.isLoaded ? EnumChatFormatting.YELLOW + "Death chunk loaded!" : EnumChatFormatting.GREEN + "Death chunk unloaded!");
         return text;
     }
 
     @Override
     public boolean isRelevantFor(final ICommandSender player)
     {
         return player.getCommandSenderName().equals(this.username);
     }
 
     @Override
     public boolean isPersonal()
     {
         return true;
     }
 
     @Override
     public NBTTagCompound toNBT()
     {
         final NBTTagCompound tag = super.toNBT();
         tag.setString("class", this.getClass().getName());
 
         tag.setBoolean("isLoaded", this.isLoaded);
         tag.setString("username", this.username);
         tag.setLong("chunkKey", this.chunkKey);
 
         return tag;
     }
 
     @Override
     public ITimerBase fromNBT(final NBTTagCompound tag)
     {
         final DeathTimer out = new DeathTimer();
 
         out.time = tag.getInteger("time");
         out.label = tag.getString("label");
         if (tag.hasKey("soundName"))
         {
             out.soundName = tag.getString("soundName");
             out.soundVolume = tag.getFloat("soundVolume");
             out.soundPitch = tag.getFloat("soundPitch");
         }
 
         out.X = tag.getInteger("X");
         out.Z = tag.getInteger("Y");
         out.Y = tag.getInteger("Z");
         out.dim = tag.getInteger("dim");
 
         out.isLoaded = tag.getBoolean("isLoaded");
         out.username = tag.getString("username");
         out.chunkKey = tag.getLong("chunkKey");
 
         return out;
     }
 }
