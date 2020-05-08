 /*
 
 This file is part of EpicZones
 
 Copyright (C) 2011 by Team ESO
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 
  */
 
 /**
  * @author jblaske@gmail.com
  * @license MIT License
  */
 
 package com.randomappdev.EpicZones;
 
 import com.randomappdev.EpicZones.integration.EpicSpout;
 import com.randomappdev.EpicZones.integration.PermissionsManager;
 import com.randomappdev.EpicZones.objects.EpicZone;
 import com.randomappdev.EpicZones.objects.EpicZone.ZoneType;
 import com.randomappdev.EpicZones.objects.EpicZoneDAL;
 import com.randomappdev.EpicZones.objects.EpicZonePlayer;
 import com.randomappdev.EpicZones.objects.EpicZonePlayer.EpicZoneMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import javax.annotation.Nullable;
 import java.awt.*;
 import java.io.File;
 import java.util.*;
 
 @SuppressWarnings({"ResultOfMethodCallIgnored"})
 public class General
 {
 
     public static Map<String, EpicZone> myZones = new HashMap<String, EpicZone>();
     public static Map<String, EpicZone> myGlobalZones = new HashMap<String, EpicZone>();
     public static Map<String, EpicZonePlayer> myPlayers = new HashMap<String, EpicZonePlayer>();
     public static String version;
     public static EpicZones plugin;
     public static boolean SpoutEnabled = false;
     public static boolean HeroChatEnabled = false;
 
     private static final String ZONE_FILE = "zones.txt";
 
     // private static File myFile;
 
     public static EpicZonePlayer getPlayer(String name)
     {
         return myPlayers.get(name.toLowerCase());
     }
 
     public static void addPlayer(@Nullable Player player)
     {
         if (player != null)
         {
             if (!myPlayers.containsKey(player.getName().toLowerCase()))
             {
                 myPlayers.put(player.getName().toLowerCase(), new EpicZonePlayer(player));
             }
             Security.UpdatePlayerSecurity(player);
         } else
         {
             myPlayers.put("console", new EpicZonePlayer("console"));
         }
     }
 
     public static void removePlayer(String playerName)
     {
         EpicZonePlayer ezp = myPlayers.get(playerName.toLowerCase());
         if (ezp != null)
         {
             if (ezp.getMode() != EpicZoneMode.None)
             {
                 if (ezp.getEditZone() != null)
                 {
                     ezp.getEditZone().HidePillars();
                 }
             }
             myPlayers.remove(playerName.toLowerCase());
         }
     }
 
     public static void LoadZones()
     {
 
         myZones.clear();
 
         if (loadZonesFromText())
         {
             Log.Write("Converting Zones.txt into new zone format...");
             SaveZones();
             File file = new File(plugin.getDataFolder() + File.separator + ZONE_FILE);
             file.renameTo(new File(plugin.getDataFolder() + File.separator + ZONE_FILE + ".old"));
             myZones.clear();
         }
 
         myZones = EpicZoneDAL.Load();
 
         reconsileGlobalZones();
         reconcileChildren();
         SaveZones();
 
     }
 
     public static boolean loadZonesFromText()
     {
         try
         {
 
             String line;
             File file = new File(plugin.getDataFolder() + File.separator + ZONE_FILE);
 
             if (file.exists())
             {
                 try
                 {
 
                     Scanner scanner = new Scanner(file);
                     myZones.clear();
 
                     try
                     {
                         while (scanner.hasNext())
                         {
                             EpicZone newZone;
                             line = scanner.nextLine().trim();
                             if (line.startsWith("#") || line.isEmpty())
                             {
                                 continue;
                             }
                             newZone = new EpicZone(line);
                             General.myZones.put(newZone.getTag(), newZone);
                         }
 
                     } finally
                     {
                         scanner.close();
                     }
                 } catch (Exception e)
                 {
                     Log.Write(e.getMessage());
                 }
 
                 reconcileChildren();
 
                 return true;
 
             }
 
         } catch (Exception e)
         {
             Log.Write(e.getMessage());
         }
         return false;
 
     }
 
     public static void SaveZones()
     {
         for (String zoneTag : myZones.keySet())
         {
             EpicZoneDAL.Save(myZones.get(zoneTag));
         }
     }
 
     public static void reconsileGlobalZones()
     {
         for (String zoneTag : myZones.keySet())
         {
             EpicZone zone = myZones.get(zoneTag);
             if (zone.getType() == ZoneType.GLOBAL)
             {
                 myGlobalZones.put(zone.getTag().toLowerCase(), zone);
             }
         }
 
         for (World world : plugin.getServer().getWorlds())
         {
             AddWorld(world);
         }
 
     }
 
     public static void AddWorld(World world)
     {
         Log.Write("Adding World: " + world.getName().toLowerCase());
         if (myGlobalZones.get(world.getName().toLowerCase()) == null)
         {
 
             EpicZone newGlobal = new EpicZone();
 
             newGlobal.setTag(world.getName().toLowerCase());
             newGlobal.setName(world.getName());
             newGlobal.setRadius(1000);
             newGlobal.setType("GLOBAL");
             newGlobal.setMobs("all");
             newGlobal.setWorld(world.getName());
 
             for (String zoneTag : myZones.keySet())
             {
                 EpicZone zone = myZones.get(zoneTag);
                 if (zone.getWorld().equalsIgnoreCase(world.getName()))
                 {
                     newGlobal.addChild(zone);
                 }
             }
 
             myZones.put(newGlobal.getTag(), newGlobal);
             myGlobalZones.put(world.getName().toLowerCase(), newGlobal);
 
             EpicZoneDAL.Save(myZones.get(newGlobal.getTag()));
 
             Log.Write("Global Zone Created For World [" + world.getName() + "]");
 
         }
     }
 
     public static void removeWorld(World world)
     {
         myZones.remove(world.getName().toLowerCase());
         myGlobalZones.remove(world.getName().toLowerCase());
     }
 
     private static void reconcileChildren()
     {
 
         ArrayList<String> badChildren = new ArrayList<String>();
 
         try
         {
             for (String zoneTag : myZones.keySet())
             {
                 EpicZone zone = myZones.get(zoneTag);
                 if (zone.hasChildren())
                 {
                     for (String child : zone.getChildrenTags())
                     {
                         EpicZone childZone = myZones.get(child);
                         if (childZone != null)
                         {
                             childZone.setParent(zone);
                             zone.addChild(childZone);
                         } else
                         {
                             Log.Write("The zone [" + zoneTag + "] has an invalid child > [" + child + "]");
                             badChildren.add(child);
                         }
                     }
                     if (badChildren.size() > 0)
                     {
                         Log.Write("Removing invalid children from [" + zoneTag + "]");
                         for (String badChild : badChildren)
                         {
                             zone.removeChild(badChild);
                         }
                         badChildren = new ArrayList<String>();
                     }
                 }
             }
         } catch (Exception e)
         {
             Log.Write(e.getMessage());
         }
     }
 
     public static void WarnPlayer(Player player, EpicZonePlayer ezp, String message)
     {
         if (ezp.getLastWarned().before(new Date()))
         {
             Message.Send(player, message);
             ezp.Warn();
         }
     }
 
     public static boolean ShouldCheckPlayer(EpicZonePlayer ezp)
     {
         boolean result = false;
         if (ezp != null)
         {
             if (ezp.getLastCheck().before(new Date()))
             {
                 result = true;
             }
         }
         return result;
     }
 
     public static boolean IsNumeric(String data)
     {
         return data.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+");
     }
 
     public static boolean BorderLogic(Point point, Player player)
     {
 
         if (Config.enableRadius && point != null && player != null)
         {
 
             EpicZonePlayer ezp = General.getPlayer(player.getName());
             EpicZone globalZone = myGlobalZones.get(player.getWorld().getName().toLowerCase());
 
             double xSquared = point.x * point.x;
             double ySquared = point.y * point.y;
             double distanceFromCenter = Math.sqrt(xSquared + ySquared);
 
             if (globalZone != null && ezp != null)
             {
                 if (distanceFromCenter <= globalZone.getRadius())
                 {
                     if (ezp.getPastBorder())
                     {
                         WarnPlayer(player, ezp, "You are inside the map radius border.");
                         ezp.setPastBorder(false);
                     }
                     return true;
                 } else
                 {
                     if (PermissionsManager.hasPermission(player, "epiczones.ignoremapradius"))
                     {
                         if (!ezp.getPastBorder())
                         {
                             WarnPlayer(player, ezp, "You are outside the map radius border.");
                             ezp.setPastBorder(true);
                         }
                         return true;
                     } else
                     {
                         return false;
                     }
                 }
             } else
             {   // No border defined for the world in config.
                 return true;
             }
         } else
         {
             return true;
         }
     }
 
     public static EpicZone GetZoneForPlayer(@Nullable Player player, String worldName, int playerHeight, Point playerPoint)
     {
         EpicZone result = null;
         if (player != null)
         {
             EpicZonePlayer ezp = getPlayer(player.getName());
             if (ezp.getCurrentZone() != null)
             {
                 result = IsPlayerWithinZone(ezp.getCurrentZone(), worldName, playerHeight, playerPoint);
             }
         }
         if (result == null)
         {
             for (String zoneTag : myZones.keySet())
             {
                 EpicZone zone = myZones.get(zoneTag);
                 result = IsPlayerWithinZone(zone, worldName, playerHeight, playerPoint);
                 if (result != null)
                 {
                     break;
                 }
             }
         }
         return result;
     }
 
     private static EpicZone IsPlayerWithinZone(EpicZone zone, String worldName, int playerHeight, Point playerPoint)
     {
         EpicZone result = null;
         if (zone.IsPointWithin(worldName, playerHeight, playerPoint))
         {
             result = zone;
             if (zone.hasChildren())
             {
                 EpicZone childResult;
                 for (String zoneTag : zone.getChildren().keySet())
                 {
                     childResult = IsPlayerWithinZone(myZones.get(zoneTag), worldName, playerHeight, playerPoint);
                     if (childResult != null)
                     {
                         result = childResult;
                         break;
                     }
                 }
             }
         }
         return result;
     }
 
     public static boolean PlayerMovementLogic(Player player, Location fromLoc, Location toLoc)
     {
         boolean result = true;
         if (player != null && fromLoc != null && toLoc != null)
         {
             EpicZonePlayer ezp = General.getPlayer(player.getName());
             if (ezp != null)
             {
                 if (General.ShouldCheckPlayer(ezp))
                 {
                     if (!ezp.isTeleporting())
                     {
                         int playerHeight = toLoc.getBlockY();
                         Point playerPoint = new Point(toLoc.getBlockX(), toLoc.getBlockZ());
                         EpicZone zone;
                         if (General.BorderLogic(playerPoint, player))
                         {
                             zone = General.GetZoneForPlayer(player, toLoc.getWorld().getName(), playerHeight, playerPoint);
                             if (zone != null)
                             {
                                 if (ZonePermissionsHandler.hasPermissions(player, zone, "entry"))
                                 {
                                     if ((ezp.getCurrentZone() != null && !ezp.getCurrentZone().getTag().equals(zone.getTag())) || ezp.getCurrentZone() == null)
                                     {
                                         if (ezp.getCurrentZone() != null)
                                         {
                                             ezp.setPreviousZoneTag(ezp.getCurrentZone().getTag());
                                             if (ezp.getCurrentZone().getExitText().length() > 0)
                                             {
                                                 Message.Send(player, ezp.getCurrentZone().getExitText());
                                             }
                                         }
                                         ezp.setCurrentZone(zone);
                                         if (zone.getEnterText().length() > 0)
                                         {
                                             Message.Send(player, zone.getEnterText());
                                         }
                                         if (General.SpoutEnabled)
                                         {
                                             EpicSpout.UpdatePlayerZone(ezp, zone);
                                         }
                                     }
                                     ezp.setCurrentLocation(fromLoc);
                                 } else
                                 {
                                     General.WarnPlayer(player, ezp, Message.get(Message.Message_ID.Info_00130_NoPermToEnter, zone.getName()));
                                     ezp.setIsTeleporting(true);
                                     player.teleport(ezp.getCurrentLocation());
                                     ezp.setIsTeleporting(false);
                                     result = false;
                                 }
                             }
                         }
                         ezp.Check();
                     }
                     ezp.setHasMoved(true);
                 }
             }
         }
         return result;
     }
 }
