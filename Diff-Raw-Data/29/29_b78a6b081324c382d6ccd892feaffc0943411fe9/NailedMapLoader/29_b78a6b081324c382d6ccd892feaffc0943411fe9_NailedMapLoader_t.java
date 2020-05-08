 package jk_5.nailed.map;
 
 import com.google.common.collect.Lists;
 import cpw.mods.fml.common.eventhandler.EventPriority;
 import cpw.mods.fml.common.eventhandler.SubscribeEvent;
 import cpw.mods.fml.common.gameevent.PlayerEvent;
 import jk_5.nailed.NailedLog;
 import jk_5.nailed.api.NailedAPI;
 import jk_5.nailed.api.events.MapCreatedEvent;
 import jk_5.nailed.api.events.MapRemovedEvent;
 import jk_5.nailed.api.events.PlayerChangedDimensionEvent;
 import jk_5.nailed.api.map.*;
 import jk_5.nailed.api.player.Player;
 import jk_5.nailed.map.gen.NailedWorldProvider;
 import jk_5.nailed.players.TeamUndefined;
 import lombok.Getter;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.util.ChunkCoordinates;
 import net.minecraft.util.EntityDamageSource;
 import net.minecraft.world.World;
 import net.minecraftforge.common.DimensionManager;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.entity.EntityJoinWorldEvent;
 import net.minecraftforge.event.entity.living.LivingAttackEvent;
 import net.minecraftforge.event.entity.living.LivingDeathEvent;
 import net.minecraftforge.event.entity.living.LivingHurtEvent;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent;
 import net.minecraftforge.event.world.BlockEvent;
 import net.minecraftforge.event.world.ChunkEvent;
 import net.minecraftforge.event.world.ChunkWatchEvent;
 import net.minecraftforge.event.world.WorldEvent;
 
 import java.io.File;
 import java.util.List;
 import java.util.Random;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class NailedMapLoader implements MapLoader {
 
     private static NailedMapLoader instance;
     @Getter private final File mapsFolder = new File("maps");
     @Getter private Map lobby;
     @Getter private final List<Map> maps = Lists.newArrayList();
     @Getter private Random randomSpawnpointSelector = new Random();
 
     public static NailedMapLoader instance(){
         if(NailedAPI.getMapLoader() == null){
             NailedAPI.setMapLoader(new NailedMapLoader());
         }
         return instance;
     }
 
     public NailedMapLoader(){
         instance = this;
     }
 
     @Override
     public void registerMap(Map map){
         if(map.getID() == 0) this.lobby = map;
         this.maps.add(map);
         NailedLog.info("Registered " + map.getSaveFileName());
     }
 
     @Override
     public Map createMapServer(Mappack pack){
         PotentialMap potentialMap = new PotentialMap(pack);
         pack.prepareWorld(potentialMap.getSaveFolder());
         Map map = pack.createMap(potentialMap);
         map.initMapServer();
         MinecraftForge.EVENT_BUS.post(new MapCreatedEvent(map));
         return map;
     }
 
     @Override
     public Map getMap(String name){
         for(Map map : this.maps){
             if(map.getSaveFileName().equalsIgnoreCase(name)){
                 return map;
             }
         }
         return null;
     }
 
     @Override
     public Map getMap(int id){
         for(Map map : this.maps){
             if(map.getID() == id){
                 return map;
             }
         }
         if(DimensionManager.isDimensionRegistered(id)){
             return new WrappedMap(id);
         }else{
             return null;
         }
     }
 
     @Override
     public Map getMap(World world){
         if(world == null) return null;
         return this.getMap(world.provider.dimensionId);
     }
 
     @SubscribeEvent
     @SuppressWarnings("unused")
     public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event){
         if(event.player.worldObj.provider instanceof NailedWorldProvider){
             ((NailedWorldProvider) event.player.worldObj.provider).sendMapData(event.player);
         }
         //SkinSyncManager.getInstance().sendSkinToClient(event.player, new File("testskin.png"), "skinTest");
         //SkinSyncManager.getInstance().setPlayerSkin(event.player, "skinTest");
     }
 
     @SubscribeEvent(priority = EventPriority.HIGH)
     @SuppressWarnings("unused")
     public void onWorldLoad(WorldEvent.Load event){
         Map map = this.getMap(event.world);
         if(map != null) map.setWorld(event.world);
     }
 
     @SubscribeEvent
     @SuppressWarnings("unused")
     public void onChangeDimension(PlayerChangedDimensionEvent event){
         event.oldMap.onPlayerLeft(event.player);
         event.newMap.onPlayerJoined(event.player);
         for(Map map : this.maps){
             map.getSignCommandHandler().onPlayerLeftMap(event.oldMap, event.player);
             map.getSignCommandHandler().onPlayerJoinMap(event.newMap, event.player);
         }
     }
 
     @SubscribeEvent
     @SuppressWarnings("unused")
     public void onBlockBreak(BlockEvent.BreakEvent event){
         Mappack mappack = this.getMap(event.world).getMappack();
         if(mappack != null && mappack.getMappackMetadata().isPreventingBlockBreak()){
             event.setCanceled(true);
         }
     }
 
     @SubscribeEvent
     @SuppressWarnings("unused")
     public void onEntitySpawn(EntityJoinWorldEvent event){
         Map map = this.getMap(event.world);
         if(event.entity instanceof EntityPlayer && map.getMappack() != null){
             Mappack mappack = map.getMappack();
             EntityPlayer player = (EntityPlayer) event.entity;
             ChunkCoordinates worldSpawn = event.world.getSpawnPoint();
             Spawnpoint spawn = mappack.getMappackMetadata().getSpawnPoint();
            if(Math.floor(player.posX) == worldSpawn.posX && Math.floor(player.posZ) == worldSpawn.posZ){
                event.entity.setLocationAndAngles(spawn.posX + 0.5, spawn.posY, spawn.posZ + 0.5, spawn.yaw, spawn.pitch);
                 player.setGameType(mappack.getMappackMetadata().getGamemode());
                 map.onPlayerJoined(NailedAPI.getPlayerRegistry().getOrCreatePlayer(player.getGameProfile()));
             }
         }
     }
 
     @SubscribeEvent
     @SuppressWarnings("unused")
     public void onDamage(LivingHurtEvent event){
         if(event.entity instanceof EntityPlayer && event.source instanceof EntityDamageSource){
             EntityDamageSource source = (EntityDamageSource) event.source;
             if(source.getEntity() instanceof EntityPlayer){
                 Map map = this.getMap(event.entity.worldObj);
                 if(map.getMappack() != null){
                     if(!map.getMappack().getMappackMetadata().isPvpEnabled()){
                         if(event.source instanceof PvpIgnoringDamageSource){
                             if(((PvpIgnoringDamageSource) event.source).disableWhenPvpDisabled()){
                                 event.setCanceled(true);
                             }
                         }else{
                             event.setCanceled(true);
                         }
                     }
                 }
             }
         }
     }
 
     @SubscribeEvent
     @SuppressWarnings("unused")
     public void onAttack(LivingAttackEvent event){
         if(event.source.getEntity() instanceof EntityPlayer && event.entityLiving instanceof EntityPlayer){
             Mappack mappack = this.getMap(event.entity.worldObj).getMappack();
             if(mappack != null && !mappack.getMappackMetadata().isPvpEnabled()){
                 event.setCanceled(true);
             }
         }
     }
 
     @SubscribeEvent
     @SuppressWarnings("unused")
     public void onDie(LivingDeathEvent event){
         if(!(event.entity instanceof EntityPlayer)) return;
         Player player = NailedAPI.getPlayerRegistry().getPlayer((EntityPlayer) event.entity);
         if(player == null) return;
         if(!player.getCurrentMap().getInstructionController().isRunning()) return;
         if(player.getSpawnpoint() != null){
             player.getEntity().setSpawnChunk(player.getSpawnpoint(), true);
         }else if(player.getTeam() instanceof TeamUndefined){
             Map map = player.getCurrentMap();
             Mappack mappack = map.getMappack();
             if(mappack != null && mappack.getMappackMetadata().isChoosingRandomSpawnpointAtRespawn()){
                 List<Spawnpoint> spawnpoints = mappack.getMappackMetadata().getRandomSpawnpoints();
                 Spawnpoint chosen = spawnpoints.get(this.randomSpawnpointSelector.nextInt(spawnpoints.size()));
                 player.getEntity().setSpawnChunk(chosen, true);
             }
         }else{
             if(player.getTeam().shouldOverrideDefaultSpawnpoint()){
                 Spawnpoint coords = player.getTeam().getSpawnpoint();
                 player.getEntity().setSpawnChunk(coords, true);
             }
         }
     }
 
     @SubscribeEvent
     @SuppressWarnings("unused")
     public void onChunkLoad(ChunkEvent.Load event){
         for(Map map : this.maps){
             map.getSignCommandHandler().onChunkLoad(event);
         }
     }
 
     @SubscribeEvent
     @SuppressWarnings("unused")
     public void onChunkUnload(ChunkEvent.Unload event){
         for(Map map : this.maps){
             map.getSignCommandHandler().onChunkUnload(event);
         }
     }
 
     @SubscribeEvent
     @SuppressWarnings("unused")
     public void onWatch(ChunkWatchEvent.Watch event){
         for(Map map : this.maps){
             map.getSignCommandHandler().onWatch(event);
         }
     }
 
     @SubscribeEvent
     @SuppressWarnings("unused")
     public void onUnwatch(ChunkWatchEvent.UnWatch event){
         for(Map map : this.maps){
             map.getSignCommandHandler().onUnwatch(event);
         }
     }
 
     @SubscribeEvent
     @SuppressWarnings("unused")
     public void onInteract(PlayerInteractEvent event){
         //TODO: replace this
         //for(Map map : this.maps){
         for(int i = 0; i < this.maps.size(); i++){
             this.maps.get(i).getSignCommandHandler().onInteract(event);
         }
     }
 
     @Override
     public void removeMap(Map map){
         DimensionManager.unloadWorld(map.getID());
         this.maps.remove(map);
         MinecraftForge.EVENT_BUS.post(new MapRemovedEvent(map));
         NailedLog.info("Unloaded map " + map.getSaveFileName());
     }
 
     public void checkShouldStart(Map map){
         if(map.getMappack() == null) return;
         String startWhen = map.getMappack().getMappackMetadata().getStartWhen();
         if(startWhen.equals("false")) return;
         if(startWhen.startsWith("equals(")){
             String s[] = startWhen.substring(7, startWhen.length() - 1).split(",");
             if(s[0].equals("joinedPlayers")){
                 int players = Integer.parseInt(s[1]);
                 if(map.getAmountOfPlayers() >= players){
                     map.getInstructionController().startGame();
                 }else{
                     InstructionController controller = map.getInstructionController();
                     Object data = controller.load("watchunready");
                     if(data == null) controller.save("watchunready", false);
                     if((Boolean) controller.load("watchunready")){
                         controller.stopGame();
                     }
                 }
             }
         }
     }
 }
