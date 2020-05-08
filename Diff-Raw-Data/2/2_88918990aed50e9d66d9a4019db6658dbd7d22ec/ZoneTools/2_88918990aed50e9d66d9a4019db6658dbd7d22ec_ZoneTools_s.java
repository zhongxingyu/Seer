 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package info.jeppes.ZoneCore;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Field;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 import net.minecraft.server.v1_6_R3.NBTTagCompound;
 import net.minecraft.server.v1_6_R3.NBTTagList;
 import net.minecraft.server.v1_6_R3.NBTTagString;
 import org.bukkit.ChatColor;
 import org.bukkit.Color;
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 
 /**
  *
  * @author Jeppe
  */
 public class ZoneTools {
     
     public static boolean getBoolean(String arg) throws Exception{
         arg = arg.toLowerCase();
         switch(arg){
             case "true":
                 return true;
             case "t":
                 return true;
             case "allow":
                 return true;
             case "false":
                 return false;
             case "f":
                 return false;
             case "deny":
                 return false;
         }
         throw new Exception("Could not find any boolean");
     }
     
     public static WorldTime getWorldTime(long time){
         return WorldTime.getWorldTime(time);
     }
     
     public static byte yawToDirection(float yaw){
         yaw += 45;
         if(yaw < 0){
             yaw += 360;
         }
         
         if(0 <= yaw && yaw < 90){
             return 0;
         } else if(90 <= yaw && yaw < 180){
             return 1;
         } else if(180 <= yaw && yaw < 270){
             return 2;
         } else if(270 <= yaw && yaw < 360){
             return 3; 
         }
         return 2;
     }
     
     public static float directionToYaw(byte direction){
       
         switch(direction){
             case 0: 
                 return 0;
             case 1:
                 return 90;
             case 2:
                 return 180;
             case 3:
                 return 270;
         }
         return 180;
     }
     public static Object[] getObjectsOnPage(List<?> objList, int page, int objectsPerPage){
         int to = page * objectsPerPage;
         int from = to - objectsPerPage;
         int arraySize = objList.size() - from;
         if(arraySize > objectsPerPage){
             arraySize = objectsPerPage;
         }
         Object[] objectsOnPage = new Object[arraySize];
         int arrayPos = 0;
         for(int i = from; i < to;i++){
             if(objList.size() > i){
                 objectsOnPage[arrayPos] = objList.get(i);
                 arrayPos++;
             }
         }
         return objectsOnPage;
     }
     public static List<Object> getListSorted(Object[] array){
         List<Object> list = new ArrayList<Object>();
         if(array.length > 0){
             for(Object nameObj : array){
                 String name = nameObj.toString();
                 boolean found = false;
                 for(int i2 = 0; i2 < list.size(); i2++){
                     String listName = list.get(i2).toString();
                     int size = name.compareTo(listName);
                     if(size > 0){
                         continue;
                     }else if(size <= 0) {
                         list.add(i2,nameObj);
                         found = true;
                         break;
                     }
                 }
                 if(!found){
                     list.add(nameObj);
                 }
             }
         }
         return list;
     }
     
     
     public List<Location> getBlockOfTypeInArea(Material material, Location location, int radius){
         return getBlockOfTypeInArea(material.getId(),location,radius);
     }
     public List<Location> getBlockOfTypeInArea(Material material, byte data, Location location, int radius){
         return getBlockOfTypeInArea(material.getId(),data,location,radius);
     }
     public List<Location> getBlockOfTypeInArea(int typeId, Location location, int radius){
         return getBlockOfTypeInArea(typeId,(byte)-1,location,radius);
     }
     public List<Location> getBlockOfTypeInArea(int typeId, byte data, Location location, int radius){
         ArrayList<Location> blocks = new ArrayList();
         World world = location.getWorld();
         int dx = location.getBlockX() - radius;
         int dy = location.getBlockY() - radius;
         int dz = location.getBlockZ() - radius;
         for(int x = dx; x < dx + radius * 2; x++){
             for(int y = dy; y < dy + radius * 2; y++){
                 for(int z = dz; z < dz + radius * 2; z++){
                     int blockTypeIdAt = world.getBlockTypeIdAt(x, y, z);
                     if(blockTypeIdAt == typeId){
                         if(data >= 0){
                             if(world.getBlockAt(x, y, z).getData() == data){
                                 blocks.add(new Location(world,x,y,z));
                             }
                         } else {
                             blocks.add(new Location(world,x,y,z));
                         }
                     }
                 }
             }
         }
         return blocks;
     }
     
     public static String parseColorCodes(String originalString){
         StringBuilder newString = new StringBuilder();
         String[] split = originalString.split("&");
         boolean first = true;
         for(String colorSegment : split){
             if(first){
                 first = false;
                 newString.append(colorSegment);
                 continue;
             }
             String colorString = colorSegment.substring(0,1);
             ChatColor color = ChatColor.getByChar(colorString);
            String rest = colorString.substring(1);
             newString.append(color.toString()).append(rest);
         }
         return newString.toString();
     }
     
     public static Material getMaterialFromString(String arg){
         try{
             int id = Integer.parseInt(arg);
             return Material.getMaterial(id);
         } catch(Exception e){
             arg = arg.toUpperCase();
             if(arg.contains("COBBLE") && !arg.contains("COBBLESTONE")){
                 arg = arg.replace("COBBLE", "COBBLESTONE");
             }
             if(arg.contains("PLATE") && !arg.contains("CHESTPLATE")){
                 arg = arg.replace("PLATE", "CHESTPLATE");
             }
             if(arg.contains("RAIL") && !arg.contains("RAILS")){
                 arg = arg.replace("RAIL", "RAILS");
             }
             if(arg.contains("BREWINGSTAND") && !arg.contains("BREWINGSTANDITEM")){
                 arg = arg.replace("BREWINGSTAND", "BREWING_STAND_ITEM");
             }
             if(arg.contains("SPRUCE") && !arg.contains("SPRUCE_WOOD_")){
                 arg = arg.replace("SPRUCE", "SPRUCE_WOOD_");
             }
             if(arg.contains("BIRCH") && !arg.contains("BIRCH_WOOD_")){
                 arg = arg.replace("BIRCH", "BIRCH_WOOD_");
             }
             if(arg.contains("JUNGLE") && !arg.contains("JUNGLE_WOOD_")){
                 arg = arg.replace("JUNGLE", "JUNGLE_WOOD_");
             }
             arg = arg.replace("WOODEN", "WOOD");
             arg = arg.replace("SHOVEL", "SPADE");
             arg = arg.replace("GUNPOWDER", "SULPHUR");
             arg = arg.replace("REDSTONELAMP", "REDSTONE_LAMP_OFF");
             arg = arg.replace("REDSTONETORCH", "REDSTONE_TORCH_ON");
             arg = arg.replace("PANTS", "LEGGINGS");
             arg = arg.replace("PLANK", "WOOD");
             arg = arg.replace("melonslice", "MELON_ITEM");
             arg = arg.replace("NETHERSTAIRS", "NETHER_BRICK_STAIRS");
             arg = arg.replace("STONEBRICK", "SMOOTH_BRICK");
             arg = arg.replace("STICKYPISTON", "PISTON_STICKY_BASE");
             arg = arg.replace("GLASSPANE", "THIN_GLASS");
             arg = arg.replace("IRONBARS", "IRON_FENCE");
             arg = arg.replace("STONESTAIRS", "SMOOTH_STAIRS");
             arg = arg.replace("BRICKBLOCK", "BRICK");
             
             
             
             
             Material material = Material.getMaterial(arg);
             if(material == null){
                 material = Material.getMaterial(arg.replaceAll(" ", "_"));
                 if(material == null){
                     Material[] materials = Material.values();
                     for(Material mat : materials){
                         if(mat.name().replaceAll("_", "").equalsIgnoreCase(arg)){
                             material = mat;
                             break;
                         }
                     }
                 }
             }
             return material;
         }
     }
     
     public static int blockFaceToDirection(BlockFace facing){
         switch(facing){
             case NORTH:
                 return 3;
             case SOUTH:
                 return 1;
             case EAST:
                 return 0;
             case WEST:
                 return 2;
         }
         return 0;
     }
     
     public static ItemStack addGlow(org.bukkit.inventory.ItemStack stack) {
         net.minecraft.server.v1_6_R3.ItemStack nmsStack = (net.minecraft.server.v1_6_R3.ItemStack) CraftItemStack.asNMSCopy(stack);
         NBTTagCompound compound = nmsStack.tag;
         
         // Initialize the compound if we need to
         if (compound == null) {
             compound = new NBTTagCompound();
             nmsStack.tag = compound;
         }
         // Empty enchanting compound
         NBTTagList nbtTagList = new NBTTagList();
         compound.set("ench", nbtTagList);
         nmsStack.save(compound);
         return CraftItemStack.asCraftMirror(nmsStack);
     }
     
     public static boolean isPlayer(CommandSender cs){
         return (cs instanceof Player);
     }
     
     public static ItemStack getInkItemStackFromColor(Color color){
         return getInkItemStackFromDyeColor(DyeColor.getByColor(color));
     }
     public static ItemStack getInkItemStackFromFireworksColor(Color color){
         return getInkItemStackFromDyeColor(DyeColor.getByFireworkColor(color));
     }
     public static ItemStack getInkItemStackFromDyeColor(DyeColor dyeColor){
         for(byte b = 0; b < 16; b++){
             if(dyeColor.equals(DyeColor.getByData(b))){
                 ItemStack itemStack = new ItemStack(Material.INK_SACK);
                 MaterialData data = itemStack.getData();
                 data.setData(b);
                 itemStack.setData(data);
                 return itemStack;
             }
         }
         
         return null;
         
     }
     
     
     public static void deleteDirectory(File directory) {
         File[] files = directory.listFiles();
         if(files!=null) { //some JVMs return null for empty dirs
             for(File f: files) {
                 if(f.isDirectory()) {
                     deleteDirectory(f);
                 } else {
                     f.delete();
                 }
             }
         }
         directory.delete();
     }
     public static long getDirectorySize(File directory) {
         long size = 0;
         File[] files = directory.listFiles();
         if(files!=null) { //some JVMs return null for empty dirs
             for(File f: files) {
                 if(f.isDirectory()) {
                     size += getDirectorySize(f);
                 } else {
                     size += f.length();
                 }
             }
         }
         return size;
     }
     
 	public static void copyFolder(File srcFolderPath, File destFolderPath)
 			throws IOException {
 		if (!srcFolderPath.isDirectory()) {
 			// If it is a File the Just copy It to the new Folder
 			InputStream in = new FileInputStream(srcFolderPath);
 			OutputStream out = new FileOutputStream(destFolderPath);
 
 			byte[] buffer = new byte[1024];
 
 			int length;
 
 			while ((length = in.read(buffer)) > 0) {
 				out.write(buffer, 0, length);
 			}
 			in.close();
 			out.close();
 		} else {
 			// if it is a directory create the directory inside the new destination directory and
 			// list the contents...
 			if (!destFolderPath.exists()) {
 				destFolderPath.mkdir();
 				System.out.println("Directory copied from " + srcFolderPath
 						+ "  to " + destFolderPath + " successfully");
 			}
 			String folder_contents[] = srcFolderPath.list();
 			for (String file : folder_contents) {
 				File srcFile = new File(srcFolderPath, file);
 				File destFile = new File(destFolderPath, file);
 
 				copyFolder(srcFile, destFile);
 			}
 		}
 	}
     
     public static String formateNumberToString(double value) {
         return formateNumberToString(value,2,BigDecimal.ROUND_DOWN);
     }
     public static String formateNumberToString(double value, int decimals, int rounding) {
         BigDecimal bd = new BigDecimal(value);
         bd = bd.setScale(decimals,rounding);
         return bd.toPlainString();
     }
     
     public static int[] getTimeDDHHMMSS(long time) {     
         int seconds = (int) Math.ceil((time / 1000d) % 60);
         int minutes = (int) (time / 60000) % 60;
         int hours   = (int) (time / 3600000) % 24;
         int days    = (int) (time / 86400000) % 60;
         return new int[]{days,hours,minutes,seconds};
     }
     
     /**
      * returns the time as a String, example: 2 days 1 hour 53 minutes and 32 seconds
      * @param time
      * @return
      */
     public static String getTimeDDHHMMSSString(long time){
         int[] timeDDHHMMSS = getTimeDDHHMMSS(time);
         boolean useDays = timeDDHHMMSS[0] != 0;
         boolean useHours = timeDDHHMMSS[1] != 0;
         boolean useMinuts = timeDDHHMMSS[2] != 0;
         boolean useSeconds = timeDDHHMMSS[3] != 0;
         return  (useDays ? timeDDHHMMSS[0] + " day"+ (timeDDHHMMSS[0] == 1 ? " " : "s "): "") + 
                 (useHours ? (timeDDHHMMSS[1] + " hour"+ (timeDDHHMMSS[1] == 1 ? " " : "s ")): "") + 
                 (useMinuts ? (timeDDHHMMSS[2] + " minute"+ (timeDDHHMMSS[2] == 1 ? " " : "s ")): "") + 
                 ((useHours || useMinuts) && useSeconds ? "and " : "")+
                 (useSeconds ? (timeDDHHMMSS[3] +" second" + (timeDDHHMMSS[3] == 1 ? "" : "s")) : "");
     }
     /**
      * Works like getTimeDDHHMMSSString(Long) but will not show any number that equals 0
      * @param time
      * @return
      */
     public static String getTimeDDHHMMSSStringShort(long time){
         int[] timeDDHHMMSS = getTimeDDHHMMSS(time);
         boolean useDays = timeDDHHMMSS[0] != 0;
         boolean useHours = timeDDHHMMSS[1] != 0;
         boolean useMinuts = timeDDHHMMSS[2] != 0;
         boolean useSeconds = timeDDHHMMSS[3] != 0;
         String timeString = (useDays ? (timeDDHHMMSS[0] + " day"+ (timeDDHHMMSS[0] == 1 ? " " : "s ")): "") + 
                 (useHours ? (timeDDHHMMSS[1] + " hour"+ (timeDDHHMMSS[1] == 1 ? " " : "s ")): "") + 
                 (useMinuts ? (timeDDHHMMSS[2] + " minute"+ (timeDDHHMMSS[2] == 1 ? " " : "s ")): "") + 
                 ((useDays || useHours || useMinuts) && useSeconds ? "and " : "")+
                 (useSeconds ? (timeDDHHMMSS[3] +" second" + (timeDDHHMMSS[3] == 1 ? "" : "s")) : "");
         if(timeString.isEmpty()){
             return "0 seconds";
         } 
         return timeString;
     }
     /**
      * returns the time as a String, example: 1 hour 53 minutes and 32 seconds
      * @param time
      * @return
      */
     public static String getTimeHHMMSSString(long time){
         int[] timeDDHHMMSS = getTimeDDHHMMSS(time);
         return 
                 timeDDHHMMSS[1] +" hour" + (timeDDHHMMSS[1] == 1 ? " " : "s ") + 
                 timeDDHHMMSS[2] +" minute" + (timeDDHHMMSS[2] == 1 ? " " : "s ") + 
                 " and " + timeDDHHMMSS[3] +" second" + (timeDDHHMMSS[3] == 1 ? "" : "s"); 
     }
     /**
      * Works like getTimeHHMMSSString(Long) but will not show any number that equals 0
      * @param time
      * @return
      */
     public static String getTimeHHMMSSStringShort(long time){
         int[] timeDDHHMMSS = getTimeDDHHMMSS(time);
         boolean useHours = timeDDHHMMSS[1] != 0;
         boolean useMinuts = timeDDHHMMSS[2] != 0;
         boolean useSeconds = timeDDHHMMSS[3] != 0;
         String timeString = (useHours ? (timeDDHHMMSS[1] + " hour"+ (timeDDHHMMSS[1] == 1 ? " " : "s ")): "") + 
                 (useMinuts ? (timeDDHHMMSS[2] + " minute"+ (timeDDHHMMSS[2] == 1 ? " " : "s ")): "") + 
                 ((useHours || useMinuts) && useSeconds ? "and " : "")+
                 (useSeconds ? (timeDDHHMMSS[3] +" second" + (timeDDHHMMSS[3] == 1 ? "" : "s")) : "");
         if(timeString.isEmpty()){
             return "0 seconds";
         } 
         return timeString;
     }
 
     public static String getSimpleLocationInfo(Location location){
         if(location != null){
             return  location.getWorld().getName() +
                     "{"+location.getX()+
                     ","+location.getY()+
                     ","+location.getZ()+
                     "} Direction: "+yawToDirection(location.getYaw());
         }
         return null;
     }
     
     
 }
