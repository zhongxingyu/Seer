 package jk_5.nailed.players;
 
 import com.mojang.authlib.GameProfile;
 import jk_5.nailed.api.ChatColor;
 import jk_5.nailed.api.Gamemode;
 import jk_5.nailed.api.NailedAPI;
 import jk_5.nailed.api.database.DataObject;
 import jk_5.nailed.api.database.DataOwner;
 import jk_5.nailed.api.map.Map;
 import jk_5.nailed.api.map.Spawnpoint;
 import jk_5.nailed.api.map.team.Team;
 import jk_5.nailed.api.player.Player;
 import jk_5.nailed.map.teleport.TeleportHelper;
 import jk_5.nailed.network.NailedNetworkHandler;
 import jk_5.nailed.network.NailedPacket;
 import jk_5.nailed.permissions.Group;
 import jk_5.nailed.permissions.NailedPermissionFactory;
 import jk_5.nailed.permissions.User;
 import jk_5.nailed.util.couchdb.DatabaseManager;
 import lombok.Getter;
 import lombok.RequiredArgsConstructor;
 import lombok.Setter;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.network.NetHandlerPlayServer;
 import net.minecraft.network.Packet;
 import net.minecraft.network.play.server.S2BPacketChangeGameState;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.util.ChatComponentText;
 import net.minecraft.util.IChatComponent;
 import net.minecraft.util.ResourceLocation;
 import net.minecraft.world.WorldSettings;
 import net.minecraftforge.permissions.api.PermissionsManager;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 @DataOwner.DataType("player")
 @RequiredArgsConstructor
 public class NailedPlayer implements Player {
 
     @Getter private final GameProfile gameProfile;
     @Setter private Map currentMap;
     @Getter private boolean online = false;
     @Getter @Setter private int teamSpeakClientID = -1;
     @Getter @Setter private int fps;
     @Getter @Setter private Spawnpoint spawnpoint;
     @Getter @Setter private int pdaID = -1;
     @Getter private NetHandlerPlayServer netHandler;
     @Getter private DataObject data = new PlayerData();
 
     public void sendNotification(String message){
         this.sendNotification(message, null);
     }
 
     public void sendNotification(String message, ResourceLocation icon){
         this.sendNotification(message, icon, 0xFFFFFF);
     }
 
     public void sendNotification(String message, ResourceLocation icon, int iconColor){
         EntityPlayerMP entity = this.getEntity();
         if(entity != null){
             NailedNetworkHandler.sendPacketToPlayer(new NailedPacket.Notification(message, icon, iconColor), entity);
         }
     }
 
     public void sendChat(String message){
         this.sendChat(new ChatComponentText(message));
     }
 
     public void sendChat(IChatComponent message){
         EntityPlayerMP entity = this.getEntity();
         if(entity != null) this.getEntity().addChatComponentMessage(message);
     }
 
     public void sendPacket(Packet packet){
         this.netHandler.sendPacket(packet);
     }
 
     public EntityPlayerMP getEntity(){
         return MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(this.getUsername());
     }
 
     public boolean isOp(){
         return MinecraftServer.getServer().getConfigurationManager().isPlayerOpped(this.getUsername());
     }
 
     public String getChatPrefix(){
         User info = this.getPermissionInfo();
         if(info != null){
             Group group = info.getMainGroup();
             if(group == null){
                 return this.getTeam().getColor() + this.getUsername() + ChatColor.RESET;
             }else{
                 return this.getTeam().getColor() + group.getPrefix() + this.getUsername() + group.getSuffix() + ChatColor.RESET;
             }
         }else{
             return this.getTeam().getColor() + this.getUsername() + ChatColor.RESET;
         }
     }
 
     public Team getTeam(){
         return this.getCurrentMap().getTeamManager().getPlayerTeam(this);
     }
 
     public void setTeam(Team team){
         this.getCurrentMap().getTeamManager().setPlayerTeam(this, team);
     }
 
     public String getUsername(){
         return this.gameProfile.getName();
     }
 
     public String getId(){
         return this.gameProfile.getId();
     }
 
     public Map getCurrentMap(){
         if(this.currentMap == null) this.currentMap = NailedAPI.getMapLoader().getLobby();
         return this.currentMap;
     }
 
     public void onLogin() {
         this.online = true;
         this.netHandler = this.getEntity().playerNetServerHandler;
     }
 
     public void onLogout() {
         this.online = false;
         this.netHandler = null;
         this.saveData();
     }
 
     public void onChangedDimension() {
         NailedNetworkHandler.sendPacketToPlayer(new NailedPacket.TimeUpdate(true, ""), this.getEntity());
     }
 
     public void onRespawn() {
         this.getEntity().setSpawnChunk(null, false);
     }
 
     public void teleportToMap(Map map){
         TeleportHelper.travelEntity(this.getEntity(), map.getSpawnTeleport());
     }
 
     public Spawnpoint getLocation(){
         EntityPlayer player = this.getEntity();
         return new Spawnpoint((int) player.posX, (int) player.posY, (int) player.posZ, player.rotationYaw, player.rotationPitch);
     }
 
     public Gamemode getGameMode(){
         return Gamemode.fromId(this.getEntity().theItemInWorldManager.getGameType().getID());
     }
 
     public void setGameMode(Gamemode mode){
         EntityPlayerMP entity = this.getEntity();
         entity.theItemInWorldManager.setGameType(WorldSettings.GameType.getByID(mode.getId()));
         this.sendPacket(new S2BPacketChangeGameState(3, mode.getId()));
     }
 
     @Override
     public String getWinnerName(){
        return this.getChatPrefix() + this.getUsername();
     }
 
     @Override
     public String getWinnerColoredName(){
         return this.getWinnerName();
     }
 
     public User getPermissionInfo(){
         if(PermissionsManager.getPermFactory() instanceof NailedPermissionFactory){
             return ((NailedPermissionFactory) PermissionsManager.getPermFactory()).getUserInfo(this.getUsername());
         }
         return null;
     }
 
     @Override
     public boolean hasPermission(String node){
         return PermissionsManager.checkPerm(this.getEntity(), node);
     }
 
     @Override
     public void sendTimeUpdate(String msg){
         NailedNetworkHandler.sendPacketToPlayer(new NailedPacket.TimeUpdate(true, msg), this.getEntity());
     }
 
     @Override
     public void onDataLoaded(){
 
     }
 
     /**************** DataOwner ****************/
 
     @Override
     public void saveData(){
         DatabaseManager.saveData(this);
     }
 
     @Override
     public void loadData(){
         DatabaseManager.loadData(this);
     }
 }
