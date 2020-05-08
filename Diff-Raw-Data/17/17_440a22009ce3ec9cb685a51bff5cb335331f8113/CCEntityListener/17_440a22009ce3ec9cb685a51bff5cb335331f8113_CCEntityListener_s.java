 package com.github.CubieX.ChunkClaimer;
 
 import java.io.IOException;
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.List;
import java.util.Set;

 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 import net.milkbowl.vault.permission.Permission;

 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import com.sk89q.worldguard.LocalPlayer;
 import com.sk89q.worldguard.bukkit.BukkitPlayer;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.domains.DefaultDomain;
 import com.sk89q.worldguard.protection.UnsupportedIntersectionException;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import com.sk89q.worldedit.BlockVector;
 
 public class CCEntityListener implements Listener
 {
    private ChunkClaimer plugin = null;
    WorldGuardPlugin weInst = null;
    Permission perm = null;
    Economy econ = null;
    List<Block> borderBlocks = new ArrayList<Block>();
 
    public CCEntityListener(ChunkClaimer plugin, WorldGuardPlugin weInst, Permission perm, Economy econ)
    {
       this.plugin = plugin;
       this.weInst = weInst;
       this.perm = perm;
       this.econ = econ;
 
       plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
 
    //================================================================================================
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e)
    {
       if(e.getAction() == Action.RIGHT_CLICK_BLOCK)
       {
          RegionManager wgRM = weInst.getRegionManager(e.getPlayer().getWorld());
          Chunk chunk = e.getClickedBlock().getChunk();
          String region = "fb_" + chunk.getX() + "_" + chunk.getZ(); // if there is or will be an applicable region, it is called like this
          LocalPlayer lPlayer = new BukkitPlayer(weInst, e.getPlayer());
 
          // CHUNK FINDER ======================================================
          if(e.getPlayer().getItemInHand().getType() == Material.BONE)
          {
             if((e.getPlayer().isOp()) ||                  
                   (e.getPlayer().hasPermission("chunkclaimer.buy")))
             {
                if(ChunkClaimer.language.equals("de")){e.getPlayer().sendMessage(ChatColor.GREEN + "Dies ist Chunk: X= " + chunk.getX() + " | Z= " + chunk.getZ() + " (Chunk-Koords)");}
                if(ChunkClaimer.language.equals("en")){e.getPlayer().sendMessage(ChatColor.GREEN + "This is chunk: X= " + chunk.getX() + " | Z= " + chunk.getZ() + " (Chunk-Coords)");}
 
                if(wgRM.hasRegion(region))
                {
                   if(ChunkClaimer.language.equals("de")){e.getPlayer().sendMessage(ChatColor.GREEN + plugin.getDescription().getName() + "-Protection: " + ChatColor.WHITE + region + ChatColor.GREEN + "\n" +
                         "Besitzer: " + ChatColor.WHITE + getOwnerNamesOfRegion(wgRM, region) + "\n" + ChatColor.GREEN + 
                         "Freunde: " + ChatColor.WHITE + getMemberNamesOfRegion(wgRM, region));}
 
                   if(ChunkClaimer.language.equals("en")){e.getPlayer().sendMessage(ChatColor.GREEN + plugin.getDescription().getName() + "-Protection: " + ChatColor.WHITE + region + ChatColor.GREEN + "\n" +
                         "Owners: " + ChatColor.WHITE + getOwnerNamesOfRegion(wgRM, region) + "\n" + ChatColor.GREEN + 
                         "Friends: " + ChatColor.WHITE + getMemberNamesOfRegion(wgRM, region));}
                }
                else
                {
                   if(ChunkClaimer.language.equals("de")){e.getPlayer().sendMessage(ChatColor.GREEN + "Keine " + plugin.getDescription().getName() + "-Protection gefunden.");}
                   if(ChunkClaimer.language.equals("en")){e.getPlayer().sendMessage(ChatColor.GREEN + "No " + plugin.getDescription().getName() + "-Protection found.");}
                }
 
                ChunkFinderUtil.revertBorderBlocks(e.getPlayer(), borderBlocks);
                ChunkFinderUtil.reCalculateBorderBlocks(chunk, borderBlocks);
                ChunkFinderUtil.sendBorderBlocks(e.getPlayer(), borderBlocks);                
             }
          }
          // ===================================================================
 
          // CHUNK PROTECTION CREATOR ========================================================
          if(e.getPlayer().getItemInHand().getType() == Material.BLAZE_ROD)
          {
             if((e.getPlayer().isOp()) ||
                   (e.getPlayer().hasPermission("chunkclaimer.buy")))
             {
                if(weInst.canBuild(e.getPlayer(), e.getClickedBlock()))
                {
                   int claimLimit = 0;
 
                   if(!wgRM.hasRegion(region)) // Try only to create a new region, if a ChunkClaimer protection is not already existing at this point
                   {
                      if(ChunkClaimer.claimingLimits.containsKey(perm.getPrimaryGroup(e.getPlayer())))
                      {
                         claimLimit = ChunkClaimer.claimingLimits.get(perm.getPrimaryGroup(e.getPlayer()));
                      }
                      else
                      {
                         claimLimit = 100000; // this players group is not restricted in config file for claiming chunks
                      }
 
                      if(claimLimit > wgRM.getRegionCountOfPlayer(lPlayer)) // counts owned regions of player
                      {
                         DefaultDomain owners = new DefaultDomain();
                         DefaultDomain members = new DefaultDomain();
                         owners.addPlayer(lPlayer);
 
                         // add defined groups to protection as members
                         if(null != ChunkClaimer.autoAddGroupsAsMembers)
                         {
                            for(String group : ChunkClaimer.autoAddGroupsAsMembers)
                            {
                               members.addGroup(group);
                            }
                         }
 
                         // get both points to define this region
                         BlockVector bvMin = WEWGutil.convertToSk89qBV(ChunkFinderUtil.getLowerChunkDelimitingLocation(chunk));
                         BlockVector bvMax = WEWGutil.convertToSk89qBV(ChunkFinderUtil.getUpperChunkDelimitingLocation(chunk));
                         // define a region
                         ProtectedCuboidRegion reg = new ProtectedCuboidRegion(region, bvMin, bvMax);
                         reg.setOwners(owners);
                         reg.setMembers(members);
                         reg.setPriority(1); // must be the priority of the underlying region + 1 to protect it properly in freebuild
 
                         List<ProtectedRegion> prList = new ArrayList<ProtectedRegion>();
                         prList.add(reg);
 
                         boolean conditionsOK = true;
 
                         try
                         {                     
                            for(ProtectedRegion pr : reg.getIntersectingRegions(prList))
                            {                        
                               if(wgRM.overlapsUnownedRegion(pr, lPlayer))
                               {
                                  conditionsOK = false; // a region which does not belong to this player is intersecting this region
                                  break;
                               }
                            }
                         }
                         catch (UnsupportedIntersectionException e1)
                         {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                         }
 
                         if(conditionsOK)
                         {
                           // Surrounding areas are free to create that region, so create it for that player
                           wgRM.addRegion(reg);

                            // TODO implement logging of transactions
                            if(econ.has(e.getPlayer().getName(), ChunkClaimer.basePricePerClaimedRegion)) // has player enough money?
                            {
                               EconomyResponse ecoRes = econ.withdrawPlayer(e.getPlayer().getName(), ChunkClaimer.basePricePerClaimedRegion);
                               if(ecoRes.transactionSuccess()) // claimed region successfully payed
                               {
                                  if(ChunkClaimer.language.equals("de")){e.getPlayer().sendMessage(ChatColor.GREEN + "Dir wurden " + ChatColor.WHITE + ChunkClaimer.basePricePerClaimedRegion + " " + ChunkClaimer.currency + ChatColor.GREEN + " abgezogen.");}
                                  if(ChunkClaimer.language.equals("en")){e.getPlayer().sendMessage(ChatColor.GREEN + "You have been charged with " + ChatColor.WHITE + ChunkClaimer.basePricePerClaimedRegion + " " + ChunkClaimer.currency + ChatColor.GREEN + ".");}
                                 
                                  if(WEWGutil.saveWGregionManager(wgRM)) // Try to save all region changes
                                  {
                                     ChunkFinderUtil.placeOutlineForClaimedChunk(chunk, borderBlocks);
                                     if(ChunkClaimer.language.equals("de")){e.getPlayer().sendMessage(ChatColor.GREEN + "Du bist jetzt Besitzer der Region " + region + ".");}
                                     if(ChunkClaimer.language.equals("en")){e.getPlayer().sendMessage(ChatColor.GREEN + "You are now owner of region " + region + ".");}
                                  }
                                  else
                                  {
                                     if(ChunkClaimer.language.equals("de")){e.getPlayer().sendMessage(ChunkClaimer.logPrefix + ChatColor.RED + "FEHLER beim Speichern dieser Region!");}
                                     if(ChunkClaimer.language.equals("en")){e.getPlayer().sendMessage(ChunkClaimer.logPrefix + ChatColor.RED + "ERROR while saving this region!");}
 
                                     ChunkClaimer.log.severe(ChunkClaimer.logPrefix + "ERROR while saving this region!");
                                  }
                               }
                               else
                               {
                                  // Eco transfer failed. Abort.
                                  if(ChunkClaimer.language.equals("de")){e.getPlayer().sendMessage(ChatColor.RED + "Fehler beim Bezahlen der Region " + region + ". Bitte informiere einen Admin!");}
                                  if(ChunkClaimer.language.equals("en")){e.getPlayer().sendMessage(ChatColor.RED + "Error on withdrawing amout for claiming region " + region + ". Please inform an Admin!");}
 
                                  ChunkClaimer.log.severe("Error on charging " + e.getPlayer().getName() + " for region " + region);
                               }
                            }
                            else
                            {
                               if(ChunkClaimer.language.equals("de")){e.getPlayer().sendMessage(ChatColor.GOLD + "Du hast nicht genuegend Geld (" + ChunkClaimer.basePricePerClaimedRegion + ") um dir diesen Chunk leisten zu koennen!");}
                               if(ChunkClaimer.language.equals("en")){e.getPlayer().sendMessage(ChatColor.GOLD + "You do not have enough money (" + ChunkClaimer.basePricePerClaimedRegion + ") to afford this chunk!");}
                            }                           
                         }
                         else
                         {
                            if(ChunkClaimer.language.equals("de")){e.getPlayer().sendMessage(ChatColor.GOLD + "Kauf nicht moeglich. Die Region ueberschneidet sich mit anderen Regionen die dir nicht gehoeren.");}
                            if(ChunkClaimer.language.equals("en")){e.getPlayer().sendMessage(ChatColor.GOLD + "Purchase not possible. This region is intersecting other regions you do not own.");}
                         }
                      }
                      else
                      {
                         if(ChunkClaimer.language.equals("de")){e.getPlayer().sendMessage(ChatColor.GOLD + "Du hast dein Limit von " + ChatColor.WHITE + claimLimit + ChatColor.GOLD + " Regionen die du besitzen kannst erreicht!");}
                         if(ChunkClaimer.language.equals("en")){e.getPlayer().sendMessage(ChatColor.GOLD + "You have reached your limit of " + ChatColor.WHITE + claimLimit + ChatColor.GOLD + " regions that you can claim!");}
                      }
                   }
                   else
                   {  
                      if(ChunkClaimer.language.equals("de")){e.getPlayer().sendMessage(ChatColor.GOLD + "Dieser Chunk " + ChatColor.WHITE + region + ChatColor.GOLD + " ist schon geschuetzt durch " + plugin.getDescription().getName() + "\n" +
                            "Besitzer: " + ChatColor.WHITE + getOwnerNamesOfRegion(wgRM, region));}
 
                      if(ChunkClaimer.language.equals("en")){e.getPlayer().sendMessage(ChatColor.GOLD + "This chunk " + ChatColor.WHITE + region + ChatColor.GOLD + " is already protected by " + plugin.getDescription().getName() + "\n" +
                            "Besitzer: " + ChatColor.WHITE + getOwnerNamesOfRegion(wgRM, region));}
                   }
                }
                else
                {
                   if(ChunkClaimer.language.equals("de")){e.getPlayer().sendMessage(ChatColor.GOLD + "Du kannst diese Region nicht fuer dich beanspruchen, weil du hier kein Baurecht hast.");}
                   if(ChunkClaimer.language.equals("en")){e.getPlayer().sendMessage(ChatColor.GOLD + "You are not allowed to claim this area, because you have no building rights here.");}
                }
             }
          }
          // =================================================================================
 
          // CHUNK PROTECTION REMOVER ========================================================
          if(e.getPlayer().getItemInHand().getType() == Material.STICK)
          {
             if((e.getPlayer().isOp()) ||
                   (e.getPlayer().hasPermission("chunkclaimer.buy")))
             {
                if(wgRM.hasRegion(region))
                {
                   if(wgRM.getRegion(region).isOwner(lPlayer))
                   {
                      weInst.getRegionManager(e.getPlayer().getWorld()).removeRegion(region);
 
                      try
                      {
                         weInst.getRegionManager(e.getPlayer().getWorld()).save();
                         if(ChunkClaimer.language.equals("de")){e.getPlayer().sendMessage(ChatColor.GREEN + "Region " + region + " wurde entfernt.");}
                         if(ChunkClaimer.language.equals("en")){e.getPlayer().sendMessage(ChatColor.GREEN + "Region " + region + " has been removed.");}
                      }
                      catch (IOException ex)
                      {
                         if(ChunkClaimer.language.equals("de")){e.getPlayer().sendMessage(ChunkClaimer.logPrefix + ChatColor.RED + "FEHLER beim Speichern dieser Region!");}
                         if(ChunkClaimer.language.equals("en")){e.getPlayer().sendMessage(ChunkClaimer.logPrefix + ChatColor.RED + "ERROR while saving this region!");}
 
                         ChunkClaimer.log.severe(ChunkClaimer.logPrefix + "ERROR while saving this region!");
                      }
                   }
                   else
                   {
                      if(ChunkClaimer.language.equals("de")){e.getPlayer().sendMessage(ChatColor.RED + plugin.getDescription().getName() + "-Regionen koennen nur von ihren Besitzern, oder Spielern mit den noetigen Rechten entfernt werden.");}
                      if(ChunkClaimer.language.equals("en")){e.getPlayer().sendMessage(ChatColor.RED + plugin.getDescription().getName() + "-Regions may only be removed by it's owners, or players with proper permissions.");}
                   }
                }
                else
                {
                   if(ChunkClaimer.language.equals("de")){e.getPlayer().sendMessage("Es ist keine " + plugin.getDescription().getName() + "-Region fuer diesen Chunk vorhanden.");}
                   if(ChunkClaimer.language.equals("en")){e.getPlayer().sendMessage("There is no " + plugin.getDescription().getName() + " protection present for this chunk.");}
                }
             }
          }
       }
       // =================================================================================
    }
 
    // ##########################################################################################
 
    String getOwnerNamesOfRegion(RegionManager rm, String region)
    {
       String ownerNames = "";
       String[] owners = rm.getRegion(region).getOwners().getPlayers().toArray(new String[0]);
       int ownerCount = rm.getRegion(region).getOwners().getPlayers().size();
 
       for(int i = 0; i < ownerCount; i++)
       {
          if(!((i + 1) == ownerCount))
          {  // not the last found owner
             ownerNames = ownerNames + owners[i] + ", ";
          }
          else
          {
             // last found owner, so omit the comma
             ownerNames = ownerNames + owners[i];
          }
       }
 
       return (ownerNames);
    }
 
    String getMemberNamesOfRegion(RegionManager rm, String region)
    {
       String memberNames = "";
 
       // Get member Groups
       String[] memberGroups = rm.getRegion(region).getMembers().getGroups().toArray(new String[0]);
       int memberGroupCount = rm.getRegion(region).getMembers().getGroups().size();
 
       for(int i = 0; i < memberGroupCount; i++)
       {         
          if(!((i + 1) == memberGroupCount))
          {  // not the last found memberGroup
             memberNames = memberNames + "*" + memberGroups[i] + ", ";
          }
          else
          {
             // last found memberGroup, so omit the comma
             memberNames = memberNames + "*" + memberGroups[i];
          }
       }
 
       // get member Players
       String[] memberPlayers = rm.getRegion(region).getMembers().getPlayers().toArray(new String[0]);
       int memberPlayersCount = rm.getRegion(region).getMembers().getPlayers().size();
 
       for(int i = 0; i < memberPlayersCount; i++)
       {
          if(i == 0)
          {
             if(memberGroupCount > 0)
             {
                // first member and one ore more groups are already present, so add comma in front of it
                memberNames = memberNames + ", " + memberPlayers[i];
             }
             else
             {
                // first member and no groups are present, so don't add comma
                memberNames = memberNames + memberPlayers[i];
             }
          }
          else if((i > 0) && ((i + 1) == memberPlayersCount))
          {  // one or more members present and the last one, so add comma only in front
             memberNames = memberNames + ", " + memberPlayers[i];
          }
          else
          {
             // one or more members present and not the last one, so add comma in front and behind it
             memberNames = memberNames + ", " + memberPlayers[i] + ", ";
          }
       }
 
       return (memberNames);
    }
 }
 
