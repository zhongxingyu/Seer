 /**
  * Programmer: Jacob Scott
  * Program Name: Rotater
  * Description:
  * Date: Apr 8, 2011
  */
 package com.jascotty2.signrotate;
 
 import com.jascotty2.CSV;
 import com.jascotty2.CheckInput;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 
 /**
  * @author jacob
  */
 public class Rotater {
 
     ArrayList<Location> signs = new ArrayList<Location>();
     SignRotate plugin = null;
     protected SignSaver delaySave = null;
     public AddSignListener signAdder = new AddSignListener();
     protected SignRotater rotater = new SignRotater();
 
     public Rotater(SignRotate signPlugin) {
         plugin = signPlugin;
 
     } // end default constructor
 
     public void start() {
         if (rotater != null) {
             rotater.cancel();
         }
         rotater = new SignRotater();
         (new Timer()).schedule(rotater, plugin.config.rotateWait);
     }
 
     public void stop() {
         if (rotater != null) {
             rotater.cancel();
             rotater = null;
         }
     }

     public boolean load() {
         if (SRConfig.signDBFile.exists()) {
             try {
                 ArrayList<String[]> signdb = CSV.loadFile(SRConfig.signDBFile);
                 for (String[] s : signdb) {
                     if (s.length == 4 && plugin.getServer().getWorld(s[0]) != null) {
                         signs.add(new Location(plugin.getServer().getWorld(s[0]),
                                 CheckInput.GetDouble(s[1], 0),
                                 CheckInput.GetDouble(s[2], 0),
                                 CheckInput.GetDouble(s[3], 0)));
                     }
                 }
                 return true;
             } catch (Exception ex) {
                 SignRotate.Log(Level.SEVERE, ex);
             }
             return false;
         }
         return true;
     }
 
     public boolean save() {
         try {
             if (delaySave != null) {
                 delaySave.cancel();
                 delaySave = null;
             }
         } catch (Exception e) {
             SignRotate.Log(Level.SEVERE, e);
         }
         try {
             ArrayList<String> file = new ArrayList<String>();
             for (Location l : signs) {
                 file.add(l.getWorld().getName() + ","
                         + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ());
             }
             return CSV.saveFile(SRConfig.signDBFile, file);
         } catch (Exception e) {
             SignRotate.Log(Level.SEVERE, e);
         }
         return false;
     }
 
     public long getWait() {
         return plugin.config.rotateWait;
     }
 
     public void setWait(long wait) {
         plugin.config.rotateWait = wait < SRConfig.MIN_WAIT ? SRConfig.MIN_WAIT : wait;
     }
 
     protected class SignSaver extends TimerTask {
 
         public void start(long wait) {
             (new Timer()).schedule(this, wait);
         }
 
         @Override
         public void run() {
             save();
         }
     }
 
     public class SignRotater extends TimerTask {

         @Override
         public void run() {
            for (Location l : signs.toArray(new Location[0])) {
                 if (!(l.getBlock().getState() instanceof Sign)) {
                     signs.remove(l);
                     if (delaySave != null) {
                         delaySave.cancel();
                     }
                     delaySave = new SignSaver();
                     delaySave.start(SRConfig.saveWait);
                 } else {
                     if (plugin.config.isClockwise) {
                         if (l.getBlock().getData() == 0xF) {
                             l.getBlock().setData((byte) 0);
                         } else {
                             l.getBlock().setData((byte) (l.getBlock().getData() + 1));
                         }
                     } else {
                         if (l.getBlock().getData() == 0x0) {
                             l.getBlock().setData((byte) 0xF);
                         } else {
                             l.getBlock().setData((byte) (l.getBlock().getData() - 1));
                         }
                     }
                 }
             }
            if (rotater != null) {
                start();
            }
         }
     }
 
     public class AddSignListener extends PlayerListener {
 
         ArrayList<Player> waiting = new ArrayList<Player>();
 
         /**
          * 
          * @param player
          * @return true if is in the wait queue
          */
         public boolean toggleWait(Player player) {
             if (waiting.contains(player)) {
                 waiting.remove(player);
                 return false;
             } else {
                 waiting.add(player);
                 return true;
             }
         }
 
         @Override
         public void onPlayerInteract(PlayerInteractEvent event) {
             if (waiting.contains(event.getPlayer())
                     && (event.getAction() == Action.LEFT_CLICK_BLOCK
                     || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                 //waiting.remove(event.getPlayer());
                 if (event.getClickedBlock().getState() instanceof Sign) {
                     if (event.getClickedBlock().getType() == Material.SIGN_POST) {
                        if (signs.contains(event.getClickedBlock().getLocation())) {
                             signs.remove(event.getClickedBlock().getLocation());
                        } else {
                             signs.add(event.getClickedBlock().getLocation());
                         }
                         if (delaySave != null) {
                             delaySave.cancel();
                         }
                         delaySave = new SignSaver();
                         delaySave.start(SRConfig.saveWait);
                     } else {
                         event.getPlayer().sendMessage("only sign posts are supported");
                     }
 
                 } else {
                     event.getPlayer().sendMessage("this is not a sign");
                 }
             }
         }
     }
 } // end class Rotater
 
