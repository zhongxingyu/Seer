 /*
  * PlayerScript.java
  * 
  * Version 0.10
  *
  * Last Edited
  * 12/06/2011
  * 
  * written by codename_B
  * forked by K900
  *
  */
 
 package com.ubempire.render;
 
 import org.bukkit.Chunk;
 import org.bukkit.World;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.CreatureSpawner;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.*;
 import org.bukkit.util.config.Configuration;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 
 public class PlayerScript {
 
     BananaMapRender plugin;
     String directory;
 
     PlayerScript(BananaMapRender plugin) {
         this.plugin = plugin;
         directory = plugin.getDir();
     }
 
     private boolean isDay(World world) {
         long time = world.getFullTime() / 1000;
         return time < 13 || time > 23;
     }
 
     private double[] convertLocation(double d, double e) {
         double offset = 1;
         return new double[]{d / -512.0 + offset, e / -512.0 + offset};
     }
 
     String monsterName(Entity e) {
         if (e instanceof Pig) return "Pig";
         if (e instanceof Cow) return "Cow";
         if (e instanceof Sheep) return "Sheep";
         if (e instanceof Chicken) return "Chicken";
         if (e instanceof Creeper) return "Creeper";
         if (e instanceof Skeleton) return "Skeleton";
         if (e instanceof Spider) return "Spider";
         if (e instanceof Zombie) return "Zombie";
         if (e instanceof Wolf) return "Wolf";
         // TODO: IntelliJ thinks it's always false, need investigation
         if (e instanceof PigZombie) return "PigZombie";
         if (e instanceof Ghast) return "Ghast";
         if (e instanceof Slime) return "Slime";
         if (e instanceof Squid) return "Squid";
         return "Pig";
     }
 
     public String updateMapMarkers(World world) {
         int tilesize = 16;
         String filename = plugin.getDir(world.getName()) + "/players.js";
 
         try {
             new File(filename).createNewFile();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         PrintWriter out = null;
         try {
             double XZ[];
 
             out = new PrintWriter(new FileWriter(filename));
 
             XZ = convertLocation(world.getSpawnLocation().getX(), world.getSpawnLocation().getZ());
             out.print("map.setView(new L.LatLng(" + (XZ[0]) + ", " + (XZ[1]) + "),9);");
             if (world.getEnvironment() == World.Environment.NETHER) {
                 out.print("document.getElementById('time').innerHTML=' Eternity';");
             } else {
                 if (isDay(world)) out.print("document.getElementById('time').innerHTML=' Day';");
                 else out.print("document.getElementById('time').innerHTML=' Night';");
             }
 
 
             /* Get Tile Layers */
 
             File dir = new File(plugin.getDir(world.getName()));
             String[] filenames = null;
             String[] children = dir.list();
             if (children != null) {
                 filenames = new String[children.length];
                 int count = 0;
                 for (String aChildren : children) {
                     if (aChildren.endsWith(".png") && aChildren.contains(",")) {
                         filenames[count] = aChildren;
                         count++;
                     }
                 }
 
             }
 
             for (String png : filenames) {
                 if (png != null) {
                     String XK[] = png.split("[.,]");
                     int[] x0z0 = new int[XZ.length];
                     for (int i = 0; i < 2; i++)
                         x0z0[i] = Integer.parseInt(XK[i]);
                     out.print("var imageBounds = new L.LatLngBounds(new L.LatLng(" + (0 - x0z0[0]) + "," + (0 - x0z0[1]) + "),new L.LatLng(" + (0 - x0z0[0]) + "+1," + (0 - x0z0[1]) + "+1));var image = new L.ImageOverlay(\"" + XK[0] + "," + XK[1] + ".png\", imageBounds);map.addLayer(image);");
                     if (plugin.showNightTiles()) {
                         /* Show Night Tiles if option is enabled in config.txt */
                         if (!isDay(world)) {
 
                             out.print("var imageBounds = new L.LatLngBounds(new L.LatLng(" + (0 - x0z0[0]) + "," + (0 - x0z0[1]) + "),new L.LatLng(" + (0 - x0z0[0]) + "+1," + (0 - x0z0[1]) + "+1));var image = new L.ImageOverlay(\"images/night.png\", imageBounds);map.addLayer(image);");
                         }
                     }
                 }
             }
 
             Configuration c;
 
             /* WorldBorder */
 
             if (plugin.varWorldborderEnable()) {
                 c = RegionReader.getBorders(plugin);
                 if (c != null) {
                     double wbx = c.getDouble("worlds." + world.getName() + ".x", 0), wbz = c.getDouble("worlds." + world.getName() + ".z", 0);
                     double wbr = c.getDouble("worlds." + world.getName() + ".radius", 0);
                     if (c.getBoolean("round-border", c.getBoolean("worlds." + world.getName() + ".shape-round", false))) {
                         XZ = convertLocation(wbx, wbz);
                         out.print("p1 = new L.LatLng(" + XZ[0] + ", " + XZ[1] + ");var polygon = new L.Circle(p1, " + wbr * 300 + ");polygon.options.color = \"" + plugin.varWorldborderColor() + "\";polygon.options.opacity = " + plugin.varWorldborderOpacity() + ";polygon.options.fillOpacity = " + plugin.varWorldborderFillOpacity() + ";map.addLayer(polygon);");
                     } else {
                         XZ = convertLocation(wbx - wbr, wbz - wbr);
                         out.print("var minX = " + XZ[0] + ";var minZ = " + XZ[1] + ";");
                         XZ = convertLocation(wbx + wbr, wbz + wbr);
                         out.print("var maxX = " + XZ[0] + ";var maxZ = " + XZ[1] + ";");
                         out.print("p1 = new L.LatLng(minX, minZ),p2 = new L.LatLng(minX, maxZ),p3 = new L.LatLng(maxX, minZ),p4 = new L.LatLng(maxX, maxZ),polygonPoints = [p1, p2, p4, p3];var polygon = new L.Polygon(polygonPoints);polygon.options.color = \"" + plugin.varWorldborderColor() + "\";polygon.options.opacity = " + plugin.varWorldborderOpacity() + ";polygon.options.fillOpacity = " + plugin.varWorldborderFillOpacity() + ";map.addLayer(polygon);");
                     }
                 }
             }
 
             /* WorldGuard */
 
             if (plugin.varWorldguardRegionsEnable()) {
                 c = RegionReader.getRegions(plugin, world);
                 if (c != null) {
                     String rColor = plugin.varWorldguardRegionsColor();
                     double rOpacity = plugin.varWorldguardRegionsOpacity(), rFillOpacity = plugin.varWorldguardRegionsFillOpacity();
                     List<String> regions = c.getKeys("regions");
                     if (regions != null) {
                         for (String region : regions) {
                             if (region.equals("__global__")) {
                                 XZ = convertLocation(c.getDouble("regions." + region + ".min.x", 0), c.getDouble("regions." + region + ".min.z", 0));
                                 out.print("var minX = " + XZ[0] + ";var minZ = " + XZ[1] + ";");
                                 XZ = convertLocation(c.getDouble("regions." + region + ".max.x", 0), c.getDouble("regions." + region + ".max.z", 0));
                                 out.print("var maxX = " + XZ[0] + ";var maxZ = " + XZ[1] + ";");
                                 out.print("p1 = new L.LatLng(minX, minZ),p2 = new L.LatLng(minX, maxZ),p3 = new L.LatLng(maxX, minZ),p4 = new L.LatLng(maxX, maxZ),polygonPoints = [p1, p2, p4, p3];var polygon = new L.Polygon(polygonPoints);polygon.options.color = \"" + rColor + "\";polygon.options.opacity = " + rOpacity + ";polygon.options.fillOpacity = " + rFillOpacity + ";map.addLayer(polygon);polygon.bindPopup(\"" + region + "\");");
                             }
                         }
                     }
                 }
             }
 
             /* Markers */
 
             for (Chunk chunk : world.getLoadedChunks()) {
 
                 /* Tile Entities */
 
                 if (plugin.varTileEntitiesEnable()) {
                     int tps = 0;
                     for (BlockState b : chunk.getTileEntities()) {
                         if (tps > plugin.varTileEntitiesMaxPerChunk()) break;
                         tps += 1;
                         if (plugin.varTileEntitiesSpawners() && b instanceof CreatureSpawner
                                 && Math.random() < plugin.varTileEntitiesSpawnerChance()) {
                             XZ = convertLocation(b.getBlock().getLocation().getX(), b.getBlock().getLocation().getZ());
                             out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/Spawner.png',iconSize: new L.Point(" + 20 + "," + 20 + "),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point(" + (XZ[0]) + "," + (XZ[1]) + "),popupAnchor: new L.Point(" + (XZ[0]) + "," + (XZ[1]) + ")});var icon = new MyIcon();var markerLocation = new L.LatLng(" + (XZ[0]) + ", " + (XZ[1]) + ");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
                         }
                         if (plugin.varTileEntitiesSigns() && b instanceof Sign) {
                             String[] signLines = ((Sign) b).getLines();
                             if (signLines.length > 0 && signLines[0].equalsIgnoreCase("[BMR]")) {
                                 XZ = convertLocation(b.getBlock().getLocation().getX(), b.getBlock().getLocation().getZ());
                                 out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/Sign.png',iconSize: new L.Point(" + 32 + "," + 32 + "),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point(" + (XZ[0]) + "," + (XZ[1]) + "),popupAnchor: new L.Point(" + (XZ[0]) + "," + (XZ[1]) + ")});var icon = new MyIcon();var markerLocation = new L.LatLng(" + (XZ[0]) + ", " + (XZ[1]) + ");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);marker.bindPopup(\"");
 
                                 for (int i = 1; i < signLines.length; i++)
                                     if (!signLines[i].equals(""))
                                         out.print(signLines[i] + "<br/>");
                                 out.print("\");");
                             }
                         }
                     }
                 }
 
                 /* Entities */
 
                 if (plugin.varEntitiesEnable()) {
                     int eps = 0;
                     for (Entity e : chunk.getEntities()) {
                         if (!(e instanceof LivingEntity)) continue;
                         if (e instanceof Player) {
                             if (!plugin.varEntitiesPlayers()) continue;
                             if (!plugin.isPlayerHidden((Player) e)) {
                                 Player player = (Player) e;
                                 XZ = convertLocation(player.getLocation().getX(), player.getLocation().getZ());
                                 out.print("var MyIcon = L.Icon.extend({iconUrl: 'http://minotar.net/avatar/" + player.getName() + "/32.png',iconSize: new L.Point(32, 32),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point(" + (XZ[0]) + "," + (XZ[1]) + "),popupAnchor: new L.Point(" + (XZ[0]) + "/2," + (XZ[1]) + "/2)});var icon = new MyIcon();var markerLocation = new L.LatLng(" + (XZ[0]) + ", " + (XZ[1]) + ");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
                                 if (plugin.varEntitiesPlayerPopups())
                                     out.print("marker.bindPopup(\"<b>" + player.getName() + "</b>\");");
                                 out.print("");
                                 out.print("document.getElementById(\"players\").innerHTML=document.getElementById(\"players\").innerHTML+\"<a href='#' onclick='map.setView(new L.LatLng(" + (XZ[0]) + ", " + (XZ[1]) + "), 9);'>\"+\"" + player.getName() + "</a>\";");
                             }
                         } else {
                             String mn = monsterName(e);
                             if (plugin.varEntitiesTamedWolves() && mn.equalsIgnoreCase("Wolf")) {
                                 Wolf w = (Wolf) e;
                                 if (w.isTamed()) {
                                     XZ = convertLocation(e.getLocation().getX(), e.getLocation().getZ());
                                     out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/" + mn + "Tamed.png',iconSize: new L.Point(" + tilesize + "," + tilesize + "),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point(" + (XZ[0]) + "," + (XZ[1]) + "),popupAnchor: new L.Point(" + (XZ[0]) + "," + (XZ[1]) + ")});var icon = new MyIcon();var markerLocation = new L.LatLng(" + (XZ[0]) + ", " + (XZ[1]) + ");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
                                     if (w.getOwner() != null && w.getOwner() instanceof Player)
                                         out.print("marker.bindPopup(\"<b>" + ((Player) w.getOwner()).getName() + "</b>\");");
                                     continue;
                                 }
                             }
                             if (eps > plugin.varEntitiesMaxPerChunk()) continue;
                             if (!plugin.varEntitiesMob(mn)) continue;
                             eps += 1;
                             XZ = convertLocation(e.getLocation().getX(), e.getLocation().getZ());
                             out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/" + mn + ".png',iconSize: new L.Point(" + tilesize + "," + tilesize + "),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point(" + (XZ[0]) + "," + (XZ[1]) + "),popupAnchor: new L.Point(" + (XZ[0]) + "," + (XZ[1]) + ")});var icon = new MyIcon();var markerLocation = new L.LatLng(" + (XZ[0]) + ", " + (XZ[1]) + ");var " + mn + " = new L.Marker(markerLocation, {icon: icon});map.addLayer(" + mn + ");");
                         }
                     }
                 }
 
             }
            /* Attribution string */
            out.print("map.attributionControl.addAttribution(\"" + plugin.getAttributionString() + "\");");
 
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         out.close();
 
         return "Error while writing player.js!";
 
     }
 
 }
