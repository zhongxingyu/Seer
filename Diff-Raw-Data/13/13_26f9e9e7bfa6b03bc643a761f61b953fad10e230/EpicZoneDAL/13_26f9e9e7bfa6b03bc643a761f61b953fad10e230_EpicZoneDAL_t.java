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
 
 package com.randomappdev.EpicZones.objects;
 
 import com.randomappdev.EpicZones.General;
 import com.randomappdev.EpicZones.Log;
 import com.randomappdev.EpicZones.Util;
 import org.yaml.snakeyaml.Yaml;
 
 import java.io.*;
 import java.util.*;
 
 @SuppressWarnings({"ResultOfMethodCallIgnored"})
 public class EpicZoneDAL
 {
     private static final String PATH = "Zones";
 
     public static Map<String, EpicZone> Load()
     {
 
         File file = new File(General.plugin.getDataFolder() + File.separator + PATH);
         Map<String, EpicZone> result = new HashMap<String, EpicZone>();
 
         if (!file.exists())
         {
             file.mkdir();
         }
 
         String fileNames[] = file.list();
 
         for (String fileName : fileNames)
         {
             EpicZone zone = Load(new File(file.getAbsolutePath() + File.separator + fileName));
            result.put(zone.getTag(), zone);
         }
 
         return result;
 
     }
 
     public static void ReloadZone(String zoneTag)
     {
         File file = new File(General.plugin.getDataFolder() + File.separator + PATH + File.separator + zoneTag);
         if (file.exists())
         {
             General.myZones.put(zoneTag, Load(file));
         }
     }
 
     private static void Init()
     {
         if (!General.plugin.getDataFolder().exists())
         {
             General.plugin.getDataFolder().mkdir();
         }
 
         File file = new File(General.plugin.getDataFolder() + File.separator + PATH);
         if (!file.exists())
         {
             file.mkdir();
         }
     }
 
     @SuppressWarnings("unchecked")
     private static EpicZone Load(File file)
     {
 
         EpicZone result = new EpicZone();
         String tag = file.getName().substring(0, file.getName().indexOf(".yml"));
         boolean mobsAdded = false;
 
         Init();
 
         if (file.exists())
         {
             Yaml yaml = new Yaml();
             HashMap<String, Object> root;
             FileInputStream stream;
             try
             {
                 stream = new FileInputStream(file);
                 root = (HashMap<String, Object>) yaml.load(stream);
 
                 result.setTag(tag);
                 result.setName(Util.getStringValueFromHashSet("name", root));
                 result.setType(Util.getStringValueFromHashSet("type", root));
                 result.setRadius(Util.getIntegerValueFromHashSet("radius", root));
                 result.setWorld(Util.getStringValueFromHashSet("world", root));
                 result.setEnterText(Util.getStringValueFromHashSet("entertext", root));
                 result.setExitText(Util.getStringValueFromHashSet("exittext", root));
                 result.setFloor(Util.getIntegerValueFromHashSet("floor", root));
                 result.setCeiling(Util.getIntegerValueFromHashSet("ceiling", root, 256));
                 result.setPVP(Util.getBooleanValueFromHashSet("pvp", root));
                 result.setFire(getFire(root));
                 result.setExplode(getExplode(root));
                 result.setSanctuary(Util.getBooleanValueFromHashSet("sanctuary", root));
                 result.setAllowEndermenPick(Util.getBooleanValueFromHashSet("endermenpick", root));
                 result.setFireBurnsMobs(Util.getBooleanValueFromHashSet("fireburnsmobs", root));
                 result.setPolygon(Util.getStringValueFromHashSet("points", root));
                 result.setRegen(getRegen(root));
 
                 ArrayList<String> mobList = (ArrayList<String>) Util.getObjectValueFromHashSet("mobs", root);
                 if (mobList != null)
                 {
                     for (String mob : mobList)
                     {
                         if (mob != null)
                         {
                             result.addMob(mob);
                             mobsAdded = true;
                         }
                     }
                 }
                 if (!mobsAdded)
                 {
                     result.addMob("ALL");
                 }
 
                 ArrayList<String> ownerList = (ArrayList<String>) Util.getObjectValueFromHashSet("owners", root);
                 if (ownerList != null)
                 {
                     for (String playerName : ownerList)
                     {
                         if (playerName != null)
                         {
                             result.addOwner(playerName);
                         }
                     }
                 }
 
                 HashSet<String> disallowedCommands = (HashSet<String>) Util.getObjectValueFromHashSet("disallowedcommands", root);
                 if (disallowedCommands != null)
                 {
                     for (String command : disallowedCommands)
                     {
                         if (command != null)
                         {
                             result.getDisallowedCommands().add(command);
                         }
                     }
                 }
 
                 ArrayList<String> childZoneList = (ArrayList<String>) Util.getObjectValueFromHashSet("childzones", root);
                 if (childZoneList != null)
                 {
                     for (String zoneName : childZoneList)
                     {
                         if (zoneName != null)
                         {
                             result.addChildTag(zoneName);
                         }
                     }
                 }
 
                 HashMap<String, Object> permissionsList = (HashMap<String, Object>) Util.getObjectValueFromHashSet("permissions", root);
                 if (permissionsList != null)
                 {
                     HashMap<String, Object> innerPermission;
                     for (String key : permissionsList.keySet())
                     {
 
                         innerPermission = (HashMap<String, Object>) permissionsList.get(key);
                         String value;
 
                         value = Util.getStringValueFromHashSet("build", innerPermission);
                         if (value != null && value.length() > 0)
                         {
                             result.addPermission(key, "BUILD", value);
                         }
 
                         value = Util.getStringValueFromHashSet("destroy", innerPermission);
                         if (value != null && value.length() > 0)
                         {
                             result.addPermission(key, "DESTROY", value);
                         }
 
                         value = Util.getStringValueFromHashSet("entry", innerPermission);
                         if (value != null && value.length() > 0)
                         {
                             result.addPermission(key, "ENTRY", value);
                         }
 
                     }
                 }
 
                 result.rebuildBoundingBox();
 
                 Log.Write("Loaded " + result.getType().toString() + " Zone [" + result.getName() + "]");
 
             } catch (FileNotFoundException e)
             {
                 Log.Write(e.getMessage());
             }
         }
 
         Save(result);
 
         return result;
 
     }
 
     @SuppressWarnings("unchecked")
     private static String getRegen(HashMap<String, Object> root)
     {
         String result = "";
 
         HashMap<String, Object> regen = (HashMap<String, Object>) Util.getObjectValueFromHashSet("regen", root);
 
         result += Util.getStringValueFromHashSet("amount", regen) + ":";
         result += Util.getStringValueFromHashSet("delay", regen) + ":";
         result += Util.getStringValueFromHashSet("interval", regen) + ":";
         result += Util.getStringValueFromHashSet("maxregen", regen) + ":";
         result += Util.getStringValueFromHashSet("mindegen", regen) + ":";
         result += Util.getStringValueFromHashSet("restdelay", regen) + ":";
         result += Util.getStringValueFromHashSet("bedbonus", regen);
 
         return result;
     }
 
     @SuppressWarnings("unchecked")
     private static String getExplode(HashMap<String, Object> root)
     {
         String result = "";
 
         HashMap<String, Object> explode = (HashMap<String, Object>) Util.getObjectValueFromHashSet("explode", root);
 
         result += Util.getStringValueFromHashSet("tnt", explode) + ":";
         result += Util.getStringValueFromHashSet("creeper", explode) + ":";
         result += Util.getStringValueFromHashSet("ghast", explode) + ":";
         result += Util.getStringValueFromHashSet("other", explode);
 
         return result;
     }
 
     @SuppressWarnings("unchecked")
     private static String getFire(HashMap<String, Object> root)
     {
         String result = "";
 
         HashMap<String, Object> fire = (HashMap<String, Object>) Util.getObjectValueFromHashSet("fire", root);
 
         result += Util.getStringValueFromHashSet("ignite", fire) + ":";
         result += Util.getStringValueFromHashSet("spread", fire);
 
         return result;
     }
 
     public static void Save(EpicZone zone)
     {
 
         if (zone != null && zone.getTag().length() > 0)
         {
 
             Yaml yaml = new Yaml();
             File file = new File(General.plugin.getDataFolder() + File.separator + PATH + File.separator + zone.getTag() + ".yml");
             HashMap<String, Object> root = new HashMap<String, Object>();
             FileOutputStream stream;
             BufferedWriter writer;
 
             root.put("name", zone.getName());
             root.put("type", zone.getType().toString());
             root.put("radius", zone.getRadius());
             root.put("world", zone.getWorld());
             root.put("entertext", zone.getEnterText());
             root.put("exittext", zone.getExitText());
             root.put("floor", zone.getFloor());
             root.put("ceiling", zone.getCeiling());
             root.put("pvp", zone.getPVP());
             root.put("mobs", zone.getMobs().toArray());
             root.put("fire", zone.getFire());
             root.put("sanctuary", zone.getSanctuary());
             root.put("fireburnsmobs", zone.getFireBurnsMobs());
 
             Map<String, Object> explode = new TreeMap<String, Object>();
             explode.put("tnt", zone.getExplode().getTNT());
             explode.put("creeper", zone.getExplode().getCreeper());
             explode.put("ghast", zone.getExplode().getGhast());
             explode.put("other", zone.getExplode().getOther());
             root.put("explode", explode);
 
             Map<String, Object> fire = new TreeMap<String, Object>();
             fire.put("ignite", zone.getFire().getIgnite());
             fire.put("spread", zone.getFire().getSpread());
             root.put("fire", fire);
 
             Map<String, Object> regen = new TreeMap<String, Object>();
             regen.put("amount", zone.getRegen().getAmount());
             regen.put("delay", zone.getRegen().getDelay());
             regen.put("interval", zone.getRegen().getInterval());
             regen.put("maxregen", zone.getRegen().getMaxRegen());
             regen.put("mindegen", zone.getRegen().getMinDegen());
             regen.put("restdelay", zone.getRegen().getRestDelay());
             regen.put("bedbonus", zone.getRegen().getBedBonus());
             root.put("regen", regen);
 
             root.put("owners", zone.getOwners());
             root.put("disallowedcommands", zone.getDisallowedCommands());
             root.put("childzones", zone.getChildrenTags().toArray());
             root.put("points", zone.getPoints());
             root.put("permissions", BuildPerms(zone.getPermissions()));
 
             try
             {
 
                 if (!file.exists())
                 {
                     File dir = new File(General.plugin.getDataFolder() + File.separator + PATH);
                     if (!dir.exists())
                     {
                         dir.mkdir();
                     }
                     file.createNewFile();
                 }
 
                 stream = new FileOutputStream(file);
                 stream.getChannel().truncate(0);
                 writer = new BufferedWriter(new OutputStreamWriter(stream));
 
                 try
                 {
                     writer.write(yaml.dump(root));
                 } finally
                 {
                     writer.close();
                 }
             } catch (IOException e)
             {
                 Log.Write(e.getMessage());
             }
 
         }
     }
 
     private static Map<String, Object> BuildPerms(Map<String, EpicZonePermission> perms)
     {
         Map<String, Object> mainMap = new HashMap<String, Object>();
         ArrayList<String> members = new ArrayList<String>();
 
         for (String permKey : perms.keySet())
         {
             EpicZonePermission perm = perms.get(permKey);
             if (!members.contains(perm.getMember().toLowerCase()))
             {
                 members.add(perm.getMember().toLowerCase());
             }
         }
 
         for (String memberName : members)
         {
             Map<String, Object> map = new HashMap<String, Object>();
             for (String permKey : perms.keySet())
             {
                 EpicZonePermission perm = perms.get(permKey);
                 if (memberName.equals(perm.getMember().toLowerCase()))
                 {
                     map.put(perm.getNode().toString().toLowerCase(), perm.getPermission().toString().toLowerCase());
                 }
             }
             if (map.size() > 0)
             {
                 mainMap.put(memberName, map);
             }
         }
 
         return mainMap;
     }
 
     public static void DeleteZone(String zoneTag)
     {
         EpicZone zone = General.myZones.get(zoneTag);
         if (zone != null)
         {
             File file = new File(General.plugin.getDataFolder() + File.separator + PATH + File.separator + zone.getTag() + ".yml");
             if (file.exists())
             {
                 if (file.delete())
                 {
                     General.LoadZones();
                 }
             }
         }
     }
 }
