 package main.java.multitallented.plugins.herostronghold.effects;
 
import multitallented.redcastlemedia.bukkit.herostronghold.effect.Effect;
 import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
 import multitallented.redcastlemedia.bukkit.herostronghold.events.PlayerInRegionEvent;
 import multitallented.redcastlemedia.bukkit.herostronghold.region.Region;
 import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionManager;
 import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionType;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 
 /**
  *
  * @author Multitallented
  */
 public class EffectTeleport extends Effect {
     
     public EffectTeleport(HeroStronghold plugin) {
         super(plugin);
         registerEvent(new TeleportListener(this));
     }
     
     @Override
     public void init(HeroStronghold plugin) {
         super.init(plugin);
     }
     
     public class TeleportListener implements Listener {
         private final EffectTeleport effect;
         public TeleportListener(EffectTeleport effect) {
             this.effect = effect;
         }
         //TODO add sign data so that I can switch exit teleporters with a sign
         
         @EventHandler
         public void onCustomEvent(PlayerInRegionEvent event) {
             if (!event.getRegionLocation().getBlock().getRelative(BlockFace.UP).equals(event.getPlayer().getLocation().getBlock())) {
                 return;
             }
             Location l = event.getRegionLocation();
             RegionManager rm = getPlugin().getRegionManager();
             Region r = rm.getRegion(l);
             RegionType rt = rm.getRegionType(r.getType());
             
             //Check if the region is a teleporter
             if (effect.regionHasEffect(rt.getEffects(), "teleport") == 0) {
                 return;
             }
             
             Block block = l.getBlock().getRelative(BlockFace.UP);
             if (!(block.getState() instanceof Sign)) {
                 return;
             }
 
             Sign sign = (Sign) block.getState();
             if (!sign.getLine(0).equalsIgnoreCase("[Teleport]")) {
                 return;
             }
             Region currentRegion = null;
             try {
                 currentRegion = rm.getRegionByID(Integer.parseInt(sign.getLine(1)));
             } catch (Exception e) {
                 return;
             }
             if (currentRegion == null) {
                 return;
             }
             
             if (r.getOwners().isEmpty() || currentRegion.getOwners().isEmpty()) {
                 return;
             }
             //TODO add more error messages
             boolean ownerCheck = false;
             for (String s : r.getOwners()) {
                 if (currentRegion.isOwner(s)) {
                     ownerCheck = true;
                     break;
                 }
             }
             if (!ownerCheck) {
                 return;
             }
             
             if (effect.regionHasEffect(rm.getRegionType(currentRegion.getType()).getEffects(), "teleport") == 0) {
                 return;
             }
             
             Location targetLoc = currentRegion.getLocation();
             
             
             //Check to see if the HeroStronghold has enough reagents
             if (!effect.hasReagents(l))
                 return;
             
             //Run upkeep but don't need to know if upkeep occured
             effect.forceUpkeep(l);
            event.getPlayer().teleport(targetLoc.getBlock().getRelative(BlockFace.NORTH, 2).getLocation());
             event.getPlayer().sendMessage(ChatColor.GOLD + "[HeroStronghold] You have been teleported!");
         }
     }
     
 }
