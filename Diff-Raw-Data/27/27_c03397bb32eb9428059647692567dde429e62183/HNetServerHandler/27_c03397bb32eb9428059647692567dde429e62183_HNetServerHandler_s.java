 package com.herocraftonline.dev.heroes.inventory;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.ItemStack;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.NetServerHandler;
 import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet101CloseWindow;
 import net.minecraft.server.Packet102WindowClick;
 import net.minecraft.server.Packet106Transaction;
 import net.minecraft.server.Slot;
 
 import org.bukkit.Bukkit;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.entity.Player;
 

 public class HNetServerHandler extends NetServerHandler {
 
     @SuppressWarnings("rawtypes")
     private Map n = new HashMap();
 
     public HNetServerHandler(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
         super(minecraftserver, networkmanager, entityplayer);
     }
 
    @Override
    public void a(Packet101CloseWindow packet101closewindow) {
        Bukkit.getServer().getPluginManager().callEvent(new InventoryCloseEvent((Player) this.player.getBukkitEntity()));
        this.player.z();
    }
 
     @SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
     @Override
     public void a(Packet102WindowClick packet102windowclick) {
         if (this.player.activeContainer.f == packet102windowclick.a && this.player.activeContainer.c(this.player)) {
             // Heroes Start
             Player p = (Player) this.player.getBukkitEntity();
 
             ItemStack before = ItemStack.b(packet102windowclick.e);
 
             CraftItemStack slot = null;
            if(before != null){
                 slot = new CraftItemStack(before);
             }
 
             CraftItemStack cursor = null;
            if(this.player.inventory.j() != null){
                 cursor = new CraftItemStack(this.player.inventory.j());
             }
 
             InventoryChangeEvent event = new InventoryChangeEvent(p, slot, cursor, packet102windowclick.b);
             Bukkit.getServer().getPluginManager().callEvent(event);
 
            if(event.isCancelled()){
                 p.updateInventory();
                 return;
             }
             // Heroes End
 
             ItemStack itemstack = this.player.activeContainer.a(packet102windowclick.b, packet102windowclick.c, packet102windowclick.f, this.player);
 
             if (ItemStack.equals(packet102windowclick.e, itemstack)) {
                 this.player.netServerHandler.sendPacket(new Packet106Transaction(packet102windowclick.a, packet102windowclick.d, true));
                 this.player.h = true;
                 this.player.activeContainer.a();
                 this.player.y();
                 this.player.h = false;
             } else {
                 this.n.put(Integer.valueOf(this.player.activeContainer.f), Short.valueOf(packet102windowclick.d));
                 this.player.netServerHandler.sendPacket(new Packet106Transaction(packet102windowclick.a, packet102windowclick.d, false));
                 this.player.activeContainer.a(this.player, false);
                 ArrayList arraylist = new ArrayList();
 
                 for (int i = 0; i < this.player.activeContainer.e.size(); ++i) {
                    System.out.print("5 " + i);
                     arraylist.add(((Slot) this.player.activeContainer.e.get(i)).getItem());
                 }
 
                 this.player.a(this.player.activeContainer, arraylist);
             }
         }
     }
 }
