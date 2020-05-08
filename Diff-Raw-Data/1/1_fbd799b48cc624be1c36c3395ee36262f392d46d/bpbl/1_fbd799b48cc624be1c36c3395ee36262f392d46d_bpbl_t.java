 package me.pwnage.bukkit.BlastPick;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class bpbl extends PlayerListener
 {
     private bpm plugin;
     private static int BedrockThreshold = 8; // let's be on the safe side
 
     public bpbl(bpm plugin)
     {
         this.plugin = plugin;
     }
 
     @Override
     public void onPlayerInteract(PlayerInteractEvent event)
     {
     	// since we cannot unregister the handler, do not act when the plugin is disabled
     	if ( !this.plugin.isEnabled() ) {
     		return;
     	}
     	
         event.getAction();
         if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
         {
             return;
         }
 
         Player p = event.getPlayer();
         if (p.getItemInHand().getTypeId() == 278)
         {
             boolean canUse = false;
             if (this.plugin.perms != null)
             {
                 canUse = this.plugin.ph.has(p, "blastpick.usepick");
             }
             else canUse = event.getPlayer().isOp();
 
             if ((canUse) && (this.plugin.playersUsing.containsKey(p.getName())))
             {
                 int x = (int)p.getLocation().getPitch();
                 Location bl = event.getClickedBlock().getLocation();
                 if (x >= 35)
                 {
                     for (int pos = 0; pos < ((Integer)this.plugin.playersUsing.get(p.getName())).intValue(); pos++)
                     {
                         Block nb = p.getWorld().getBlockAt(new Location(p.getWorld(), bl.getBlockX(), bl.getBlockY() - pos, bl.getBlockZ()));
                         replaceBlock(nb, p);
                     }
                 } else if (x > -55)
                 {
                     int dir = (int)p.getLocation().getYaw();
                     
                     dir = (dir + 360) % 360;
                     if ((dir >= 300) || ((dir >= 0) && (dir <= 60)))
                     {
                         for (int pos = 0; pos < ((Integer)this.plugin.playersUsing.get(p.getName())).intValue(); pos++)
                         {
                             Block nb = p.getWorld().getBlockAt(new Location(p.getWorld(), bl.getBlockX(), bl.getBlockY(), bl.getBlockZ() + pos));
                             replaceBlock(nb, p);
                         }
                     }
                     if ((dir > 60) && (dir <= 120))
                     {
                         for (int pos = 0; pos < ((Integer)this.plugin.playersUsing.get(p.getName())).intValue(); pos++)
                         {
                             Block nb = p.getWorld().getBlockAt(new Location(p.getWorld(), bl.getBlockX() - pos, bl.getBlockY(), bl.getBlockZ()));
                             replaceBlock(nb, p);
                         }
                     }
                     if ((dir > 120) && (dir <= 210))
                     {
                         for (int pos = 0; pos < ((Integer)this.plugin.playersUsing.get(p.getName())).intValue(); pos++)
                         {
                             Block nb = p.getWorld().getBlockAt(new Location(p.getWorld(), bl.getBlockX(), bl.getBlockY(), bl.getBlockZ() - pos));
                             replaceBlock(nb, p);
                         }
                     }
                     if ((dir > 210) && (dir <= 300))
                     {
                         for (int pos = 0; pos < ((Integer)this.plugin.playersUsing.get(p.getName())).intValue(); pos++)
                         {
                             Block nb = p.getWorld().getBlockAt(new Location(p.getWorld(), bl.getBlockX() + pos, bl.getBlockY(), bl.getBlockZ()));
                             replaceBlock(nb, p);
                         }
                     }
                 } else if (x <= -35)
                 {
                     for (int pos = 0; pos < ((Integer)this.plugin.playersUsing.get(p.getName())).intValue(); pos++)
                     {
                         Block nb = p.getWorld().getBlockAt(new Location(p.getWorld(), bl.getBlockX(), bl.getBlockY() + pos, bl.getBlockZ()));
                         replaceBlock(nb, p);
                     }
                 }
             }
         }
     }
 
     public void replaceBlock(Block b, Player p)
     {
         switch (b.getType())
         {
             case FURNACE:
             case CHEST:
             case WORKBENCH:
             case DISPENSER:
                 return;
             case BEDROCK:
             	if ( b.getLocation().getBlockY() > bpbl.BedrockThreshold )
                 {
                     BlockBreakEvent bbe = new BlockBreakEvent(b, p);
                     this.plugin.getServer().getPluginManager().callEvent(bbe);
                     if(!bbe.isCancelled())
                     {
                         b.setType(Material.AIR);
                     }
             	}
                break;
             default:
                 BlockBreakEvent bbe = new BlockBreakEvent(b, p);
                 this.plugin.getServer().getPluginManager().callEvent(bbe);
                 if(!bbe.isCancelled())
                 {
                     b.setType(Material.AIR);
                 }
         }
     }
 }
