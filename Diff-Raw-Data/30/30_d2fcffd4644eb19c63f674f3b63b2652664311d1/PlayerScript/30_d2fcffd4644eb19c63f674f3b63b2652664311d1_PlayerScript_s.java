 /*
  * PlayerScript.java
  * 
  * Version 0.7.1
  *
  * Last Edited
  * 07/06/2011
  * 
  * written by codename_B
  * 
  */
 
 package com.ubempire.render;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.CreatureSpawner;
 //import org.bukkit.block.CreatureSpawner;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Chicken;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.Squid;
 import org.bukkit.entity.Wolf;
 import org.bukkit.entity.Zombie;
 import org.bukkit.util.config.Configuration;
 
 public class PlayerScript {
 
     BananaMapRender plugin;
     String directory;
     
     PlayerScript(BananaMapRender plugin) {
         this.plugin = plugin;
         directory = plugin.getDir();
     }
     
     private boolean isDay(World world)
 	{
 	long time = world.getFullTime()/1000;
 	//System.out.println(time);
 	if(time<13||time>23) return true;
 	return false;
 	}
     
 	public String updateMapMarkers(World world) 
 	{
 		int tilesize = 16;
 		String mainDirectory = plugin.getDir(world.getName());
 		
 		try {
 			new File(mainDirectory+"/players.js").createNewFile();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		PrintWriter out = null;
 		try {
 			out = new PrintWriter(new FileWriter(mainDirectory+"/players.js"));
 			double multiplier = 0.705882353;
 			double Xs = (((world.getSpawnLocation().getX()/256)*multiplier));
 			double Zs = ((0-(world.getSpawnLocation().getZ()/256)*multiplier));
 			out.print("map.setView(new L.LatLng("+(Zs+0)+", "+(Xs-0)+"), "+"9"+").addLayer(cloudmade);");
 
 			
 			Configuration c = RegionReader.getRegions(world);
 			if(c!=null)
 			{
 			List<String> regions = c.getKeys("regions");
 			for(String region : regions) 
 			{
 			//String region = "castleything";
 	        	double minZ = ((((c.getDouble("regions." + region + ".min.x",Double.NaN))/256)*multiplier));
 	        	double minX = 0-((((c.getDouble("regions." + region + ".min.z",Double.NaN))/256)*multiplier));
 	        	double maxZ = ((((c.getDouble("regions." + region + ".max.x",Double.NaN))/256)*multiplier));
 	        	double maxX = 0-((((c.getDouble("regions." + region + ".max.z",Double.NaN))/256)*multiplier));
 	        
 	        //System.out.println("var minX = -1;var minY = -1;var maxX = 1;var maxY = 1;p1 = new L.LatLng(minX, minY),p2 = new L.LatLng(minX, maxY),p3 = new L.LatLng(maxX, minY),p4 = new L.LatLng(maxX, maxY),polygonPoints = [p1, p2, p4, p3];var polygon = new L.Polygon(polygonPoints);map.addLayer(polygon);");	
 	        //System.out.print("var minX = "+minZ+";var minZ = "+minX+";var maxX = "+maxZ+";var maxZ = "+maxX+";p1 = new L.LatLng(minX, minZ),p2 = new L.LatLng(minX, maxZ),p3 = new L.LatLng(maxX, minZ),p4 = new L.LatLng(maxX, maxZ),polygonPoints = [p1, p2, p4, p3];var polygon = new L.Polygon(polygonPoints);map.addLayer(polygon);");
 	        out.print("var minX = "+minX+";var minZ = "+minZ+";var maxX = "+maxX+";var maxZ = "+maxZ+";p1 = new L.LatLng(minX, minZ),p2 = new L.LatLng(minX, maxZ),p3 = new L.LatLng(maxX, minZ),p4 = new L.LatLng(maxX, maxZ),polygonPoints = [p1, p2, p4, p3];var polygon = new L.Polygon(polygonPoints);map.addLayer(polygon);polygon.bindPopup(\""+region+"\");");
 			
 			}}
 			
 			if(plugin.getConfiguration().getBoolean("show-tiles", true))
 			{
 			for(Chunk chunk : world.getLoadedChunks())
 			{
 				for(BlockState b: chunk.getTileEntities())
 				{
 					
 					
 					
 					if(b instanceof CreatureSpawner)
 						{
 						if(Math.random()>0.8)
 						{
 						Location player = b.getBlock().getLocation();
 						double Xc = (((player.getX()/256)*multiplier));
 						double Zc = ((0-(player.getZ()/256)*multiplier));
 						out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/Spawner.png',iconSize: new L.Point("+20+","+20+"),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+"),popupAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+")});var icon = new MyIcon();var markerLocation = new L.LatLng("+(Zc+0)+", "+(Xc-0)+");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
 						}
 						}
 					if(b instanceof Sign)
 					{
 						String[] SignLines = ((Sign) b).getLines();
 						if(SignLines.length>0)
 						{
 							
 							if(SignLines[0].equals("[BMR]"))
 							{
 								Location player = b.getBlock().getLocation();
 								double Xc = (((player.getX()/256)*multiplier));
 								double Zc = ((0-(player.getZ()/256)*multiplier));
 
 								out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/Sign.png',iconSize: new L.Point("+32+","+32+"),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point("+(Zc)+","+(Xc-0)+"),popupAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+")});var icon = new MyIcon();var markerLocation = new L.LatLng("+(Zc+0)+", "+(Xc-0)+");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);marker.bindPopup(\"");
 							
 								int length = SignLines.length;
 								for(int i=1; i<length; i++)
 								{
 								//System.out.print(SignLines[i]);
 								out.print(SignLines[i]+"<br/>");
 								}
 								out.print("\");");
 							}
 						}
 					}
 					
 					
 				}
 				
 				
 				
 			}
 			}
 			
 			
 			if(isDay(world)) out.print("document.getElementById('time').innerHTML=' Day';");
 			else out.print("document.getElementById('time').innerHTML=' Night';");
 			
 			if(plugin.getConfiguration().getBoolean("show-entities", true))
 			{
 				int limit = plugin.getConfiguration().getInt("entity-limit", 100);
 				int i = 0;
 			for(Entity entity : world.getEntities())
 			{
 				
 				
 				if(i<limit)
 				{				
 				//System.out.println(limit-i);	
 				if(entity instanceof Pig)
 				{i++;
 					Entity player = entity;
 					double Xc = (((player.getLocation().getX()/256)*multiplier));
 					double Zc = ((0-(player.getLocation().getZ()/256)*multiplier));
 					out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/Pig.png',iconSize: new L.Point("+tilesize+","+tilesize+"),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+"),popupAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+")});var icon = new MyIcon();var markerLocation = new L.LatLng("+(Zc+0)+", "+(Xc-0)+");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
 					
 				}
 				if(entity instanceof Cow)
 				{i++;
 					Entity player = entity;
 					double Xc = (((player.getLocation().getX()/256)*multiplier));
 					double Zc = ((0-(player.getLocation().getZ()/256)*multiplier));
 					out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/Cow.png',iconSize: new L.Point("+tilesize+","+tilesize+"),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+"),popupAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+")});var icon = new MyIcon();var markerLocation = new L.LatLng("+(Zc+0)+", "+(Xc-0)+");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
 				}
 				if(entity instanceof Sheep)
 				{i++;
 					Entity player = entity;
 					double Xc = (((player.getLocation().getX()/256)*multiplier));
 					double Zc = ((0-(player.getLocation().getZ()/256)*multiplier));
 					out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/Sheep.png',iconSize: new L.Point("+tilesize+","+tilesize+"),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+"),popupAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+")});var icon = new MyIcon();var markerLocation = new L.LatLng("+(Zc+0)+", "+(Xc-0)+");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
 				}
 				if(entity instanceof Chicken)
 				{i++;
 					Entity player = entity;
 					double Xc = (((player.getLocation().getX()/256)*multiplier));
 					double Zc = ((0-(player.getLocation().getZ()/256)*multiplier));
 					out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/Chicken.png',iconSize: new L.Point("+tilesize+","+tilesize+"),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+"),popupAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+")});var icon = new MyIcon();var markerLocation = new L.LatLng("+(Zc+0)+", "+(Xc-0)+");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
 				}
 				if(entity instanceof Creeper)
 				{i++;
 					Entity player = entity;
 					double Xc = (((player.getLocation().getX()/256)*multiplier));
 					double Zc = ((0-(player.getLocation().getZ()/256)*multiplier));
 					out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/Creeper.png',iconSize: new L.Point("+tilesize+","+tilesize+"),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+"),popupAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+")});var icon = new MyIcon();var markerLocation = new L.LatLng("+(Zc+0)+", "+(Xc-0)+");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
 				}
 				if(entity instanceof Skeleton)
 				{i++;
 					Entity player = entity;
 					double Xc = (((player.getLocation().getX()/256)*multiplier));
 					double Zc = ((0-(player.getLocation().getZ()/256)*multiplier));
 					out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/Skeleton.png',iconSize: new L.Point("+tilesize+","+tilesize+"),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+"),popupAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+")});var icon = new MyIcon();var markerLocation = new L.LatLng("+(Zc+0)+", "+(Xc-0)+");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
 				}
 				if(entity instanceof Spider)
 				{i++;
 					Entity player = entity;
 					double Xc = (((player.getLocation().getX()/256)*multiplier));
 					double Zc = ((0-(player.getLocation().getZ()/256)*multiplier));
 					out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/Spider.png',iconSize: new L.Point("+tilesize+","+tilesize+"),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+"),popupAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+")});var icon = new MyIcon();var markerLocation = new L.LatLng("+(Zc+0)+", "+(Xc-0)+");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
 				}
 				if(entity instanceof Zombie)
 				{i++;
 					Entity player = entity;
 					double Xc = (((player.getLocation().getX()/256)*multiplier));
 					double Zc = ((0-(player.getLocation().getZ()/256)*multiplier));
 					out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/Zombie.png',iconSize: new L.Point("+tilesize+","+tilesize+"),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+"),popupAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+")});var icon = new MyIcon();var markerLocation = new L.LatLng("+(Zc+0)+", "+(Xc-0)+");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
 				}
 				if(entity instanceof Wolf)
 				{i++;
 					Entity player = entity;
 					double Xc = (((player.getLocation().getX()/256)*multiplier));
 					double Zc = ((0-(player.getLocation().getZ()/256)*multiplier));
 					out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/Wolf.png',iconSize: new L.Point("+tilesize+","+tilesize+"),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+"),popupAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+")});var icon = new MyIcon();var markerLocation = new L.LatLng("+(Zc+0)+", "+(Xc-0)+");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
 				}
 				if(entity instanceof PigZombie)
 				{i++;
 					Entity player = entity;
 					double Xc = (((player.getLocation().getX()/256)*multiplier));
 					double Zc = ((0-(player.getLocation().getZ()/256)*multiplier));
 					out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/PigZombie.png',iconSize: new L.Point("+tilesize+","+tilesize+"),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+"),popupAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+")});var icon = new MyIcon();var markerLocation = new L.LatLng("+(Zc+0)+", "+(Xc-0)+");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
 				}
 				if(entity instanceof Ghast)
 				{i++;
 					Entity player = entity;
 					double Xc = (((player.getLocation().getX()/256)*multiplier));
 					double Zc = ((0-(player.getLocation().getZ()/256)*multiplier));
 					out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/Ghast.png',iconSize: new L.Point("+tilesize+","+tilesize+"),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+"),popupAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+")});var icon = new MyIcon();var markerLocation = new L.LatLng("+(Zc+0)+", "+(Xc-0)+");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
 				}
 				if(entity instanceof Slime)
 				{i++;
 					Entity player = entity;
 					double Xc = (((player.getLocation().getX()/256)*multiplier));
 					double Zc = ((0-(player.getLocation().getZ()/256)*multiplier));
 					out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/Slime.png',iconSize: new L.Point("+tilesize+","+tilesize+"),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+"),popupAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+")});var icon = new MyIcon();var markerLocation = new L.LatLng("+(Zc+0)+", "+(Xc-0)+");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
 				}
 				if(entity instanceof Squid)
 				{i++;
 					Entity player = entity;
 					double Xc = (((player.getLocation().getX()/256)*multiplier));
 					double Zc = ((0-(player.getLocation().getZ()/256)*multiplier));
 					out.print("var MyIcon = L.Icon.extend({iconUrl: 'entity/Squid.png',iconSize: new L.Point("+tilesize+","+tilesize+"),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+"),popupAnchor: new L.Point("+(Zc+0)+","+(Xc-0)+")});var icon = new MyIcon();var markerLocation = new L.LatLng("+(Zc+0)+", "+(Xc-0)+");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);");
 				}
 			}
 			}
 			}
 			for(Player player : world.getPlayers())
 			{
				double Zc = (((player.getLocation().getX()/256)*multiplier));
				double Xc = 0-(((player.getLocation().getZ()/256)*multiplier)+multiplier);
 				//out.println("var markerLocation = new L.LatLng("+(Zc+0)+", "+(Xc-0)+"); var marker = new L.Marker(markerLocation); map.addLayer(marker); marker.bindPopup('<b>"+player.getName()+"</b><br/>"+player.getLocation().getX()+","+player.getLocation().getZ()+"').openPopup();");
 				//out.print("var imageBounds = new L.LatLngBounds(new L.LatLng("+((Zc+0)+0.1)+","+((Xc-0)+0.1)+"),new L.LatLng("+(Zc+0)+","+(Xc-0)+"));var image = new L.ImageOverlay(\"http://minotar.net/avatar/"+player.getName()+"/64\", imageBounds);map.addLayer(image);");
 				out.print("var MyIcon = L.Icon.extend({iconUrl: 'http://minotar.net/avatar/"+player.getName()+"/32.png',iconSize: new L.Point(32, 32),shadowSize: new L.Point(0, 0),iconAnchor: new L.Point("+(Zc)+","+(Xc)+"),popupAnchor: new L.Point("+(Zc)+"/2,"+(Xc)+"/2)});var icon = new MyIcon();var markerLocation = new L.LatLng("+(Zc)+", "+(Xc)+");var marker = new L.Marker(markerLocation, {icon: icon});map.addLayer(marker);marker.bindPopup(\"<b>"+player.getName()+"</b>\");");
 				out.print("");
 				out.print("document.getElementById(\"players\").innerHTML=document.getElementById(\"players\").innerHTML+\"<a href='#' onclick='map.setView(new L.LatLng("+(Zc)+", "+(Xc)+"), 9);'>\"+\""+player.getName()+"</a>\";");
 			}
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		out.close();
 
 
 		return "Error while writing player.js!";
 
 	}
 
 }
