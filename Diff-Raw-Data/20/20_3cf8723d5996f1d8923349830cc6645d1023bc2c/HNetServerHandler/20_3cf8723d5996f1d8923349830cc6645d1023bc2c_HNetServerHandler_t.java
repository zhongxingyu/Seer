 package com.herocraftonline.dev.inventory;
 
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.NetServerHandler;
 import net.minecraft.server.NetworkManager;
 import net.minecraft.server.Packet101CloseWindow;
 
import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.api.InventoryCloseEvent;
 
 public class HNetServerHandler extends NetServerHandler {
 
     public HNetServerHandler(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
         super(minecraftserver, networkmanager, entityplayer);
     }
 
     @Override
     public void a(Packet101CloseWindow packet101closewindow) {
        Bukkit.getServer().getPluginManager().callEvent(new InventoryCloseEvent((Player) this.player.getBukkitEntity()));
         this.player.z();
     }
 }
