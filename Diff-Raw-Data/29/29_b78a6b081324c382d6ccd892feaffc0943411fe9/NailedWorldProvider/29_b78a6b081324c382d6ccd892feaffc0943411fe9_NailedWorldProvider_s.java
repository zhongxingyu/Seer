 package jk_5.nailed.map.gen;
 
 import io.netty.buffer.ByteBuf;
 import io.netty.buffer.Unpooled;
 import jk_5.nailed.api.NailedAPI;
 import jk_5.nailed.api.map.Map;
 import jk_5.nailed.api.map.Mappack;
 import jk_5.nailed.api.map.Spawnpoint;
 import jk_5.nailed.map.MappackContainingWorldProvider;
 import jk_5.nailed.map.NailedMap;
 import jk_5.nailed.network.NailedNetworkHandler;
 import jk_5.nailed.network.NailedPacket;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.util.ChunkCoordinates;
 import net.minecraft.world.WorldProvider;
 import net.minecraft.world.chunk.Chunk;
 import net.minecraft.world.chunk.IChunkProvider;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class NailedWorldProvider extends WorldProvider implements MappackContainingWorldProvider {
 
     private int mapID;
     private Map map;
 
     @Override
     public void setDimension(int dim){
         this.mapID = dim;
         super.setDimension(dim);
     }
 
     @Override
     protected void registerWorldChunkManager(){
         this.map = NailedAPI.getMapLoader().getMap(this.dimensionId);
         this.worldChunkMgr = new VoidWorldChunkManager(this.worldObj);
     }
 
     public void writeData(ByteBuf buffer){
 
     }
 
     @Override
     public void calculateInitialWeather(){
         this.updateWeather();
     }
 
     @Override
     public void updateWeather(){
         super.updateWeather();
 
         this.worldObj.rainingStrength = 0;
         this.worldObj.prevRainingStrength = 0;
         this.worldObj.thunderingStrength = 0;
         this.worldObj.prevThunderingStrength = 0;
 
         /*if(!this.worldObj.isRemote && this.worldObj.playerEntities.isEmpty()){
             WorldServer world = (WorldServer) this.worldObj;
             world.theChunkProviderServer.unloadAllChunks();
         }
         this.map.getWeatherController().updateRaining();
         //this.weatherrenderer.updateClouds();
 
         this.worldObj.prevRainingStrength = this.worldObj.rainingStrength;
         this.worldObj.rainingStrength = (float) this.map.getWeatherController().getRainingStrength();
         this.worldObj.prevThunderingStrength = this.worldObj.thunderingStrength;
         this.worldObj.thunderingStrength = (float) this.map.getWeatherController().getThunderingStrength();
 
         if(this.worldObj.isRemote || !(this.worldObj instanceof WorldServer)) return;
         WorldServer world = (WorldServer) this.worldObj;
         if(world.areAllPlayersAsleep()){
             this.setWorldTime(getWorldTime() + this.getTimeUntilSunrise());
         }
         this.setWorldTime(getWorldTime() + 1L);
         if(this.map.isDataResyncRequired() && world.getTotalWorldTime() % 20L == 0L){
             this.map.onSynced();
             this.broadcastMapData();
         }*/
     }
 
     @Override
     public boolean canDoLightning(Chunk chunk){
         this.map.getWeatherController().tick(this.worldObj, chunk);
         return false;
     }
 
     private long getTimeUntilSunrise(){
         long dayinterval = 24000L;
         return dayinterval - this.getWorldTime() % dayinterval;
     }
 
     public void resetRainAndThunder(){
         if(this.map instanceof NailedMap){
             ((NailedMap) this.map).markDataNeedsResync();
         }
         this.map.getWeatherController().clear();
     }
 
     @Override
     public IChunkProvider createChunkGenerator(){
         return new VoidChunkProvider(this.worldObj);
     }
 
     @Override
     public String getDimensionName(){
         return "Nailed " + (this.hasMappack() ? this.getMappack().getMappackMetadata().getName() : "") + " " + this.mapID;
     }
 
     @Override
     public String getSaveFolder(){
         return null;
     }
 
     @Override
     public boolean hasMappack(){
         return this.map.getMappack() != null;
     }
 
     @Override
     public Mappack getMappack(){
         return this.map.getMappack();
     }
 
     @Override
     public ChunkCoordinates getRandomizedSpawnPoint(){
         if(this.hasMappack()){
             return new Spawnpoint(this.map.getMappack().getMappackMetadata().getSpawnPoint());
         }else{
             return super.getRandomizedSpawnPoint();
         }
     }
 
     @Override
     public int getRespawnDimension(EntityPlayerMP player){
         return player.dimension;
     }
 
     public NailedPacket.MapData getMapDataPacket(){
         ByteBuf buffer = Unpooled.buffer();
         this.writeData(buffer);
         return new NailedPacket.MapData(this.mapID, buffer);
     }
 
     public void broadcastMapData(){
         NailedNetworkHandler.sendPacketToAllPlayersInDimension(this.getMapDataPacket(), this.mapID);
     }
 
     public void sendMapData(EntityPlayer player){
         NailedNetworkHandler.sendPacketToPlayer(this.getMapDataPacket(), player);
     }
 }
