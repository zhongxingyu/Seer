 package jk_5.nailed.players;
 
 import com.google.common.collect.Lists;
import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.IPlayerTracker;
 import cpw.mods.fml.common.registry.GameRegistry;
 import jk_5.nailed.event.*;
 import lombok.Getter;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.ServerChatEvent;
 import net.minecraftforge.event.entity.player.PlayerEvent;
 
 import java.util.List;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class PlayerRegistry implements IPlayerTracker {
 
     private static PlayerRegistry INSTANCE = new PlayerRegistry();
 
     public static PlayerRegistry instance(){
         return INSTANCE;
     }
 
     @Getter
     private final List<Player> players = Lists.newArrayList();
 
     public PlayerRegistry() {
         GameRegistry.registerPlayerTracker(this);
         MinecraftForge.EVENT_BUS.register(this);
     }
 
     public Player getPlayer(String username){
         for(Player player : this.players){
             if(player.getUsername().equals(username)){
                 return player;
             }
         }
         return null;
     }
 
     public Player getOrCreatePlayer(String username){
         Player p = this.getPlayer(username);
         if(p != null) return p;
         p = new Player(username);
         this.players.add(p);
         MinecraftForge.EVENT_BUS.post(new PlayerCreatedEvent(p.getEntity(), p));
         return p;
     }
 
     @SuppressWarnings("unused")
     @ForgeSubscribe
     public void formatPlayerName(PlayerEvent.NameFormat event){
        if(FMLCommonHandler.instance().getEffectiveSide().isServer()) return;
         Player player = this.getOrCreatePlayer(event.username);
         if(player == null) return;
         event.displayname = player.getChatPrefix();
     }
 
     @SuppressWarnings("unused")
     @ForgeSubscribe
     public void onPlayerChat(ServerChatEvent event){
         Player player = this.getOrCreatePlayer(event.username);
         if(player == null) return;
         MinecraftForge.EVENT_BUS.post(new PlayerChatEvent(player, event.message));
     }
 
     @Override
     public void onPlayerLogin(EntityPlayer ent){
         Player player = this.getOrCreatePlayer(ent.username);
         player.onLogin();
         MinecraftForge.EVENT_BUS.post(new PlayerJoinEvent(player));
         for(Player p : this.players){
             if(p == player) continue;
             p.sendNotification(player.getUsername() + " joined");
         }
     }
 
     @Override
     public void onPlayerLogout(EntityPlayer ent){
         Player player = this.getPlayer(ent.username);
         player.onLogout();
         MinecraftForge.EVENT_BUS.post(new PlayerLeaveEvent(player));
         for(Player p : this.players){
             if(p == player) continue;
             p.sendNotification(player.getUsername() + " left");
         }
     }
 
     @Override
     public void onPlayerChangedDimension(EntityPlayer player){
         Player p = this.getPlayer(player.username);
         p.onChangedDimension();
         MinecraftForge.EVENT_BUS.post(new PlayerChangedDimensionEvent(p));
     }
 
     @Override
     public void onPlayerRespawn(EntityPlayer player){
         this.getPlayer(player.username).onRespawn();
     }
 }
