 package ccm.deathTimer.server;
 
 import ccm.deathTimer.timerTypes.DeathTimer;
 import ccm.deathTimer.timerTypes.IStopwatchBase;
 import ccm.deathTimer.timerTypes.ITimerBase;
 import ccm.deathTimer.utils.Pair;
 import com.google.common.collect.HashMultimap;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.IPlayerTracker;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.common.registry.GameRegistry;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.management.ServerConfigurationManager;
 import net.minecraft.world.ChunkCoordIntPair;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.player.PlayerDropsEvent;
 import net.minecraftforge.event.world.ChunkEvent;
 
 import java.util.ArrayList;
 
 /**
  * Sends user all timers when he/she logs in.
  *
  * @author Dries007
  */
 public class EventTracker implements IPlayerTracker
 {
     public static HashMultimap<Long, DeathTimer> ChuncksToTrackMap = HashMultimap.create();
 
     public EventTracker()
     {
         MinecraftForge.EVENT_BUS.register(this);
         GameRegistry.registerPlayerTracker(this);
     }
 
     @ForgeSubscribe
     public void handleDeath(final PlayerDropsEvent e)
     {
         ArrayList<Pair<Long, DeathTimer>> toRemove = new ArrayList<Pair<Long, DeathTimer>>();
         for (final long key : EventTracker.ChuncksToTrackMap.keySet())
         {
             for (final DeathTimer dat : EventTracker.ChuncksToTrackMap.get(key))
             {
                 if (dat.username.equals(e.entityPlayer.username)) toRemove.add(new Pair<Long, DeathTimer>(key, dat));
             }
         }
         for (Pair<Long, DeathTimer> p : toRemove)
         {
            p.getValue().time = -1;
            p.getValue().sendAutoUpdate();
             EventTracker.ChuncksToTrackMap.remove(p.key, p.value);
         }
 
         if (!e.entityPlayer.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory"))
         {
             final DeathTimer dat = new DeathTimer(e.entityPlayer);
             EventTracker.ChuncksToTrackMap.put(dat.chunkKey, dat);
         }
     }
 
     @ForgeSubscribe
     public void chunkUnload(final ChunkEvent.Unload e)
     {
         if (FMLCommonHandler.instance().getEffectiveSide().isClient()) return;
         final ServerConfigurationManager cm = MinecraftServer.getServer().getConfigurationManager();
         for (final DeathTimer dat : EventTracker.ChuncksToTrackMap.get(ChunkCoordIntPair.chunkXZ2Int(e.getChunk().xPosition, e.getChunk().zPosition)))
         {
             final EntityPlayerMP p = cm.getPlayerForUsername(dat.username);
             if (p == null) continue;
 
             dat.isLoaded = false;
             dat.sendAutoUpdate();
         }
     }
 
     @ForgeSubscribe
     public void chunkLoad(final ChunkEvent.Load e)
     {
         if (FMLCommonHandler.instance().getEffectiveSide().isClient()) return;
         final ServerConfigurationManager cm = MinecraftServer.getServer().getConfigurationManager();
         for (final DeathTimer dat : EventTracker.ChuncksToTrackMap.get(ChunkCoordIntPair.chunkXZ2Int(e.getChunk().xPosition, e.getChunk().zPosition)))
         {
             final EntityPlayerMP p = cm.getPlayerForUsername(dat.username);
             if (p == null) continue;
 
             dat.isLoaded = true;
             dat.sendAutoUpdate();
         }
     }
 
     @Override
     public void onPlayerLogin(final EntityPlayer player)
     {
         for (final ITimerBase data : ServerTimer.getInstance().timerList.values())
         {
             if (data.isRelevantFor(player)) PacketDispatcher.sendPacketToPlayer(data.getPacket(), (Player) player);
         }
 
         for (final IStopwatchBase data : ServerTimer.getInstance().stopwatchList.values())
         {
             if (data.isRelevantFor(player)) PacketDispatcher.sendPacketToPlayer(data.getPacket(), (Player) player);
         }
     }
 
     @Override
     public void onPlayerLogout(final EntityPlayer player)
     {
 
     }
 
     @Override
     public void onPlayerChangedDimension(final EntityPlayer player)
     {
 
     }
 
     @Override
     public void onPlayerRespawn(final EntityPlayer player)
     {
 
     }
 }
