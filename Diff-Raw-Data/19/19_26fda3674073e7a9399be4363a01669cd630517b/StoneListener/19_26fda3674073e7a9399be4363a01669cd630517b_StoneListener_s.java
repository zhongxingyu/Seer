 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package main.java.com.warheadgaming.tombstone;
 
 import java.util.Date;
import org.spout.api.entity.Entity;
 import org.spout.api.entity.Player;
 import org.spout.api.event.EventHandler;
 import org.spout.api.event.Listener;
 import org.spout.api.event.cause.PluginCause;
 import org.spout.api.geo.World;
 import org.spout.api.geo.cuboid.Block;
 import org.spout.vanilla.component.inventory.PlayerInventory;
 import org.spout.vanilla.component.substance.material.Sign;
 import org.spout.vanilla.component.substance.material.chest.Chest;
 import org.spout.vanilla.event.player.PlayerDeathEvent;
 import org.spout.vanilla.material.VanillaMaterials;
 import org.spout.vanilla.material.block.misc.Slab;
 
 /**
  *
  * @author DarkCloud
  */
 public class StoneListener implements Listener {
 
     private Tombstone plugin;
     public Block block;
     public Player player;
     public String playerName;
     public Sign mySign;
     public Chest myChest;
     public Date date = new Date();
 
     public void StoneListner() {
         plugin = Tombstone.getInstance();
     }
 
     @EventHandler
     public void DeathEvent(PlayerDeathEvent event) {
         player = event.getPlayer();
         playerName = event.getPlayer().getName();
 
         createTombstone();
         createCasket();
     }
 
     public void createTombstone() {
         World world = player.getWorld();
         float x = player.getTransform().getPosition().getX();
         float y = player.getTransform().getPosition().getY();
         float z = player.getTransform().getPosition().getZ();
 
         block = player.getWorld().getBlock(x, y - 1, z);
         block.setMaterial(VanillaMaterials.STONE);
 
         block = player.getWorld().getBlock(x, y, z);
         block.setMaterial(VanillaMaterials.STONE);
 
         block = world.getBlock(x, y + 1, z);
         block.setMaterial(Slab.STONE_BRICK);
 
         block = player.getWorld().getBlock(x, y, z - 1);
         block.setMaterial(Slab.STONE_BRICK);
 
         block = player.getWorld().getBlock(x + 1, y, z);
         block.setMaterial(Slab.STONE_BRICK);
 
         block = player.getWorld().getBlock(x - 1, y, z);
         block.setMaterial(Slab.STONE_BRICK);
 
        block = player.getWorld().getBlock(x + 1, y + 1, z);
         block.setMaterial(VanillaMaterials.FLOWER_POT_BLOCK.ROSE);
 
        block = player.getWorld().getBlock(x - 1, y + 1, z);
         block.setMaterial(VanillaMaterials.FLOWER_POT_BLOCK.ROSE);
 
        block = player.getWorld().getBlock(x, y, z + 1);
        block.setMaterial(VanillaMaterials.SIGN);

         mySign = (Sign) block.getComponent();
        mySign.setText(new String[]{"Here Lies", playerName, "Died", date.toString()}, new PluginCause(plugin));
 
     }
 
     public void createCasket() {
 
         World world = player.getWorld();
         float x = player.getTransform().getPosition().getX();
         float y = player.getTransform().getPosition().getY();
         float z = player.getTransform().getPosition().getZ();
         block = player.getWorld().getBlock(x, y - 2, z + 1);
         block.setMaterial(VanillaMaterials.CHEST);
 
 
         Chest chest = (Chest) block.getComponent();
         chest.setDouble(true);
         PlayerInventory inv = player.get(PlayerInventory.class);
 
         if (inv != null) {
             chest.getInventory().addAll(inv.getMain());
             chest.getInventory().addAll(inv.getArmor());
         }
     }
 }
