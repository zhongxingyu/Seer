 package net.minecraft.src.redstoneExtended.Util;
 
 import net.minecraft.client.Minecraft;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class IdManager {
     public enum IdType {
         Block() {
             @Override
             public int generateId() {
                 for (int i = net.minecraft.src.Block.blocksList.length - 1; i >= 0; --i) {
                     if ((net.minecraft.src.Block.blocksList[i] == null) && !IdManager.getInstance().reservedIds.get(Block).contains(i))
                         return i;
                 }
 
                 return -1;
             }
 
             @Override
             public boolean isIdUsed(int id) {
                 return net.minecraft.src.Block.blocksList[id] != null;
             }
         },
         Item() {
             @Override
             public int generateId() {
                 for (int i = net.minecraft.src.Item.itemsList.length - 1; i >= 0; --i) {
                     if ((net.minecraft.src.Item.itemsList[i] == null) && !IdManager.getInstance().reservedIds.get(Item).contains(i))
                         return i - 256;
                 }
 
                 return -1;
             }
 
             @Override
             public boolean isIdUsed(int id) {
                 return net.minecraft.src.Item.itemsList[id] != null;
             }
         };
 
         public abstract int generateId();
 
         public abstract boolean isIdUsed(int id);
     }
 
     private static IdManager instance;
 
     private HashMap<IdType, LinkedList<Integer>> reservedIds;
 
     public IdManager() {
     }
 
     public static IdManager getInstance() {
         if (instance == null)
             instance = new IdManager();
 
         return instance;
     }
 
     public int getId(String name, IdType idType) {
         File configDir = new File(Minecraft.getMinecraftDir(), "/mods/mod_redstoneExtended/");
 
         if (configDir.exists() || configDir.mkdir()) {
             File idFile = new File(configDir, "ids.properties");
 
             if (!idFile.exists()) {
                 try {
                     if (!idFile.createNewFile())
                         LoggingUtil.logError("Couldn't create id file because it already exists");
                 } catch (IOException e) {
                     LoggingUtil.logError("Couldn't create id file: " + e.getMessage());
                     return idType.generateId();
                 }
             }
 
             Properties ids = new Properties();
 
             try {
                 ids.load(new FileInputStream(idFile));
             } catch (FileNotFoundException e) {
                 LoggingUtil.logError("Couldn't load id file: File not found");
                 return idType.generateId();
             } catch (IOException e) {
                 LoggingUtil.logError("Couldn't load id file: " + e.getMessage());
             }
 
             if (reservedIds == null) {
                 LoggingUtil.logDebug("Initializing reserved id list");
                 reservedIds = new HashMap<IdType, LinkedList<Integer>>();
 
                String patIdTypeStr = "";
                 for (IdType idTypePossibility : IdType.values()) {
                    if (!patIdTypeStr.equals(""))
                        patIdTypeStr += "|";
                    patIdTypeStr += idTypePossibility.name();
                }

                Pattern pattern = Pattern.compile("^(" + patIdTypeStr + ")\\.[a-zA-Z0-9]+$");
 
                for (String propertyName : ids.stringPropertyNames()) {
                    Matcher matcher = pattern.matcher(propertyName);
 
                    if (matcher.matches()) {
                        String strReservedId = ids.getProperty(propertyName);
                        int reservedId = Integer.parseInt(strReservedId);
 
                        if (!reservedIds.containsKey(idType))
                            reservedIds.put(idType, new LinkedList<Integer>());
 
                        reservedIds.get(idType).add(reservedId);
                     }
                 }
             }
 
             String propertyName = idType.name() + "." + name;
             String strId = ids.getProperty(propertyName);
 
             int id;
 
             boolean idFileUpdateNecessary = false;
 
             if (strId != null) {
                 id = Integer.parseInt(strId);
 
                 if (idType.isIdUsed(id)) {
                     int newId = idType.generateId();
                     String strNewId = Integer.toString(newId);
 
                     ids.setProperty(propertyName, strNewId);
                     id = newId;
                     LoggingUtil.logError("ID CONFLICT DETECTED: Id " + strId + " for " + idType.name() + " " + name + " is already in use, now using " + strNewId);
                     strId = strNewId;
 
                     idFileUpdateNecessary = true;
                 }
             } else {
                 id = idType.generateId();
                 strId = Integer.toString(id);
 
                 ids.setProperty(propertyName, strId);
                 LoggingUtil.logDebug("Assigned Id " + strId + " to " + idType.name() + " " + name);
 
                 idFileUpdateNecessary = true;
             }
 
             if (idFileUpdateNecessary) {
                 try {
                     ids.store(new FileOutputStream(idFile), null);
                 } catch (FileNotFoundException e) {
                     LoggingUtil.logError("Couldn't save id file: File not found");
                 } catch (IOException e) {
                     LoggingUtil.logError("Couldn't save id file: " + e.getMessage());
                 }
             }
 
             LoggingUtil.logDebug("Using Id " + strId + " for " + idType.name() + " " + name);
             return id;
         } else {
             LoggingUtil.logError("Config directory could not be created");
             return idType.generateId();
         }
     }
 }
