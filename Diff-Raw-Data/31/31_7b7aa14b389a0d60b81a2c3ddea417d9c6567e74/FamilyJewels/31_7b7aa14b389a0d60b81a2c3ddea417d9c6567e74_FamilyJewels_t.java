 package com.minecarts.familyjewels;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 import com.minecarts.familyjewels.listener.PlayerListener;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.NetServerHandler;
 import net.minecraft.server.NetworkListenThread;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.event.Event;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.craftbukkit.CraftServer;
 
 
 public class FamilyJewels extends JavaPlugin{
     public static Integer[] hiddenBlocks;
 
     public void onEnable() {
         PluginManager pm = getServer().getPluginManager();
         for(Player p : getServer().getOnlinePlayers()){ this.hookNSH(p); } //Hook all the existing players
 
         hiddenBlocks = (Integer[])getConfig().getList("hidden_blocks").toArray(new Integer[]{});
         Arrays.sort(hiddenBlocks); //Make sure the array is sorted for binarySearch later
         
         getConfig().options().copyDefaults(true);
         this.saveConfig();
 
        pm.registerEvents(new PlayerListener(this),this);

         log("Enabled");
     }
 
     public void onDisable(){
         for(Player p : getServer().getOnlinePlayers()){ this.unhookNSH(p); }
         log("Disabled");
     }
 
     public void hookNSH(Player player){
         CraftPlayer craftPlayer = (CraftPlayer) player;
         CraftServer server = (CraftServer) getServer();
 
         Location loc = player.getLocation();
         NetServerHandlerHook handlerHook = new NetServerHandlerHook(server.getHandle().server, craftPlayer.getHandle().netServerHandler.networkManager, craftPlayer.getHandle());
         handlerHook.a(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
 
         //Set the old one as disconnected to prevent PacketKeepAlives from building up in the highPriority queue
         //craftPlayer.getHandle().netServerHandler.disconnected = true;
 
         //The problem with just hooking via overwriting the NSH is that inside of NetworkListenerThread there is an array
         //  of NSHs that is looped over to send a Packet0KeepAlive, unfortunately this list doesn't stay in sync
         //  with our hook, so the Packet0KeepAlives will constantly build up in the queue to send, but never actually get sent thus leaking memory.
 
         //So instead, we have to go into the class and manually replace the NSH in the array with our hook so it's correctly
         //  looped over and updated when the player disconnects.
         try{
             Field oldNSH = NetworkListenThread.class.getDeclaredField("h");
             oldNSH.setAccessible(true);
             ArrayList<NetServerHandler> nshs = (ArrayList<NetServerHandler>) oldNSH.get(((CraftServer) getServer()).getHandle().server.networkListenThread);
             for(NetServerHandler nsh : nshs){
                 if(nsh.player.name.equals(player.getName())){
                     nshs.remove(nsh);
                     nshs.add(handlerHook); //Add our hook
                     break;
                 }
             }
         } catch (NoSuchFieldException e){
             e.printStackTrace();
         } catch (IllegalAccessException e){
             e.printStackTrace();
         }
 
         
         //And now set the NSH to our new hook
         craftPlayer.getHandle().netServerHandler = handlerHook;
     }
     public void unhookNSH(Player player){
         //Nothing to do
     }
 
     public void log(String message, java.util.logging.Level level){
        getLogger().log(level, "FamilyJewels> " + message);
     }
     public void log(String message){
         this.log(message,Level.INFO);
     }
 }
