 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package SvenBrnn.sqlPermissions;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import org.bukkit.util.config.Configuration;
 
 /**
  *
  * @author Sven
  */
 public class sqlPermissionsPermisionEditor {
 
     private sqlPermissions plugin;
 
     sqlPermissionsPermisionEditor(sqlPermissions plugin) {
         this.plugin = plugin;
     }
 
     public void loadPermissionsToDatabase(String world) throws Exception {
         String worldFile = "plugins/Permissions/" + world + ".yml";
         File f = new File(worldFile);
         if (!f.exists()) {
             //System.out.println("[sqlPermissions][DEBUG] plugins/Permissions/" + world + ".yml does not exist!");
             return;
         }
 
         Configuration cfg = new Configuration(f);
         cfg.load();
         String copys = (String) cfg.getProperty("plugin.permissions.copies");
         if (copys != null && !copys.equals("null") && !copys.equals("")) {
             //System.out.println("[sqlPermissions][DEBUG] " + world + " Copies " + copys + "!");
             return;
         }
 
         int worldID = checkAndAddWorld(world, cfg);
         //System.out.println("[sqlPermissions][DEBUG] World is in DB now!");
         List groupList = cfg.getKeys("groups");
         if (groupList != null) {
             Iterator groups = groupList.iterator();
             while (groups.hasNext()) {
                 Object grp = (String) groups.next();
                 int grpID;
                 grpID = checkAndAddGroup((String) grp, worldID, cfg);
                 if (grpID != -1) {
                     String query = "DELETE FROM perm_grp_to_perm WHERE grpID='" + grpID + "'";
                     plugin.database.executeChangeQuery(query);
                     List permList = cfg.getList("groups." + grp + ".permissions");
                     if (permList != null) {
                         Iterator perms = permList.iterator();
                         while (perms.hasNext()) {
                             Object perm = (String) perms.next();
                             checkAndAddPerm((String) perm, grpID, cfg);
                         }
                         //System.out.println("[sqlPermissions][DEBUG] Group " + grp + " is in DB now!");
                     }
                 }
             }
         }
 
         List userList = cfg.getKeys("users");
         if (userList != null) {
             Iterator users = userList.iterator();
             while (users.hasNext()) {
                 String usr = (String) users.next();
                 int usrID = checkAndAddUser(usr, worldID, cfg);
                 if (usrID != -1) {
                     String query = "DELETE FROM perm_user_to_perm WHERE usrID='" + usrID + "'";
                     plugin.database.executeChangeQuery(query);
                     List permList = cfg.getList("users." + usr + ".permissions");
                     if (permList != null) {
                         Iterator perms = permList.iterator();
                         while (perms.hasNext()) {
                             Object perm = (String) perms.next();
                             checkAndAddPermUsr((String) perm, usrID, cfg);
                         }
                     }
                 }
             }
         }
     }
 
     private int checkAndAddGroup(String grp, int world, Configuration cfg) throws Exception {
         try {
             String query = "SELECT id FROM perm_groups WHERE name='" + this.escapeString(grp) + "' AND world='" + world + "'";
             String[][] res = plugin.database.executeQuery(query);
 
             Boolean defBool = (Boolean) cfg.getProperty("groups." + grp + ".default");
             String def;
             if (defBool != null && defBool) {
                 def = "true";
             } else {
                 def = "false";
             }
             String instance;
             Object ins = cfg.getProperty("groups." + grp + ".inheritance");
             String rank = cfg.getString("groups." + grp + ".rank");
             String sufix = (String) cfg.getProperty("groups." + grp + ".info.sufix");
             String prefix = (String) cfg.getProperty("groups." + grp + ".info.prefix");
             Boolean buildBool = (Boolean) cfg.getProperty("groups." + grp + ".info.build");
             String build;
             if (buildBool != null && buildBool) {
                 build = "true";
             } else {
                 build = "false";
             }
 
             if (res.length > 0) {
                 if (rank != null) {
                     query = "UPDATE perm_groups SET name='" + this.escapeString(grp) + "', world='" + world + "', def='" + def + "', prefix='" + this.escapeString(prefix) + "', sufix='" + this.escapeString(sufix) + "', build='" + build + "', rank='" + rank + "' WHERE id='" + res[0][0] + "'";
                 } else {
                     query = "UPDATE perm_groups SET name='" + this.escapeString(grp) + "', world='" + world + "', def='" + def + "', prefix='" + this.escapeString(prefix) + "', sufix='" + this.escapeString(sufix) + "', build='" + build + "' WHERE id='" + res[0][0] + "'";
                 }
                 //System.out.println("[DEBUG] " + query);
                 plugin.database.executeChangeQuery(query);
                 int gID = new Integer(res[0][0]);
 
                 query = "DELETE FROM perm_instances WHERE grpID='" + gID + "'";
                 plugin.database.executeChangeQuery(query);
 
                 if (gID != -1) {
                     if (ins instanceof ArrayList) {
                         for (int i = 0; i < ((ArrayList) ins).size(); i++) {
                             instance = (String) ((ArrayList) ins).get(i);
                             //System.out.println("[DEBUG] " + instance);
                             if (instance != null && !instance.equals("") && !instance.equals("null")) {
                                 int insID = checkAndAddGroup(instance, world, cfg);
                                 query = "SELECT grpID, refTo FROM perm_instances WHERE grpID='" + gID + "' AND refTo='" + insID + "'";
                                 res = plugin.database.executeQuery(query);
                                 if (res.length == 0) {
                                     query = "INSERT INTO perm_instances(grpID, refTo) VALUES('" + gID + "', '" + insID + "')";
                                     plugin.database.executeChangeQuery(query);
                                 }
                             }
                         }
                     } else {
                         instance = (String) ins;
                         //System.out.println("[DEBUG] " + instance);
                         if (instance != null && !instance.equals("") && !instance.equals("null")) {
                             int insID = checkAndAddGroup(instance, world, cfg);
                             query = "SELECT grpID, refTo FROM perm_instances WHERE grpID='" + gID + "' AND refTo='" + insID + "'";
                             res = plugin.database.executeQuery(query);
                             if (res.length == 0) {
                                 query = "INSERT INTO perm_instances(grpID, refTo) VALUES('" + gID + "', '" + insID + "')";
                                 plugin.database.executeChangeQuery(query);
                             }
                         }
                     }
                 }
 
                 return gID;
             }
 
             if (rank != null) {
                 query = "INSERT INTO perm_groups(name, world, def, prefix, sufix, build, rank) VALUES('" + this.escapeString(grp) + "','" + world + "','" + def + "','" + prefix + "','" + sufix + "','" + build + "','" + rank + "')";
             } else {
                 query = "INSERT INTO perm_groups(name, world, def, prefix, sufix, build) VALUES('" + this.escapeString(grp) + "','" + world + "','" + def + "','" + prefix + "','" + sufix + "','" + build + "')";
             }
 
             //System.out.println("[DEBUG] " + query);
             plugin.database.executeChangeQuery(query);
 
             query = "SELECT id FROM perm_groups WHERE name='" + this.escapeString(grp) + "' AND world='" + world + "'";
             //System.out.println("[DEBUG] " + query);
             res = plugin.database.executeQuery(query);
             int gID = -1;
             if (res.length > 0) {
                 gID = new Integer(res[0][0]);
             }
             if (gID != -1) {
                 if (ins instanceof ArrayList) {
                     for (int i = 0; i < ((ArrayList) ins).size(); i++) {
                         instance = (String) ((ArrayList) ins).get(i);
                         //System.out.println("[DEBUG] " + instance);
                         if (instance != null && !instance.equals("") && !instance.equals("null")) {
                             int insID = checkAndAddGroup(instance, world, cfg);
                             query = "SELECT grpID, refTo FROM perm_instances WHERE grpID='" + gID + "' AND refTo='" + insID + "'";
                             res = plugin.database.executeQuery(query);
                             if (res.length == 0) {
                                 query = "INSERT INTO perm_instances(grpID, refTo) VALUES('" + gID + "', '" + insID + "')";
                                 plugin.database.executeChangeQuery(query);
                             }
                         }
                     }
                 } else {
                     instance = (String) ins;
                     //System.out.println("[DEBUG] " + instance);
                     if (instance != null && !instance.equals("") && !instance.equals("null")) {
                         int insID = checkAndAddGroup(instance, world, cfg);
                         query = "SELECT grpID, refTo FROM perm_instances WHERE grpID='" + gID + "' AND refTo='" + insID + "'";
                         res = plugin.database.executeQuery(query);
                         if (res.length == 0) {
                             query = "INSERT INTO perm_instances(grpID, refTo) VALUES('" + gID + "', '" + insID + "')";
                             plugin.database.executeChangeQuery(query);
                         }
                     }
                 }
             }
             return gID;
         } catch (Exception ex) {
             System.out.println(ex.getMessage());
             throw ex;
         }
     }
 
     private int checkAndAddUser(String usr, int world, Configuration cfg) throws Exception {
         String query = "SELECT id FROM perm_users WHERE login='" + this.escapeString(usr) + "' AND world='" + world + "'";
         String[][] res = plugin.database.executeQuery(query);
         String grp = (String) cfg.getProperty("users." + usr + ".group");
         int grpID = checkAndAddGroup(grp, world, cfg);
         if (res.length > 0) {
             query = "UPDATE perm_users SET login='" + this.escapeString(usr) + "', world='" + world + "', grp='" + grpID + "' WHERE id='" + res[0][0] + "'";
             //System.out.println("[DEBUG] " + query);
             plugin.database.executeChangeQuery(query);
 
             return new Integer(res[0][0]);
         }
 
         query = "INSERT INTO perm_users(login, world, grp) VALUES('" + this.escapeString(usr) + "','" + world + "','" + grpID + "')";
         //System.out.println("[DEBUG] " + query);
         plugin.database.executeChangeQuery(query);
 
         query = "SELECT id FROM perm_users WHERE login='" + this.escapeString(usr) + "' AND world='" + world + "'";
         //System.out.println("[DEBUG] " + query);
         res = plugin.database.executeQuery(query);
         if (res.length > 0) {
             return new Integer(res[0][0]);
         }
         return -1;
     }
 
     private int checkAndAddWorld(String world, Configuration cfg) throws Exception {
         int ret = -1;
         try {
             String query = "SELECT id FROM perm_worlds WHERE world='" + this.escapeString(world) + "'";
             String[][] res = plugin.database.executeQuery(query);
 
             String copies = (String) cfg.getProperty("plugin.permissions.copies");
             String system = (String) cfg.getProperty("plugin.permissions.system");
 
             if (res.length > 0) {
                 query = "UPDATE perm_worlds SET world='" + this.escapeString(world) + "', copies='" + this.escapeString(copies) + "', system='" + this.escapeString(system) + "' WHERE id='" + res[0][0] + "'";
                 plugin.database.executeChangeQuery(query);
 
                 return new Integer(res[0][0]);
             }
 
             query = "INSERT INTO perm_worlds(world, copies, system) VALUES('" + this.escapeString(world) + "','" + this.escapeString(copies) + "','" + this.escapeString(system) + "')";
             plugin.database.executeChangeQuery(query);
 
             query = "SELECT id FROM perm_worlds WHERE world='" + this.escapeString(world) + "'";
             res = plugin.database.executeQuery(query);
 
             if (res.length > 0) {
                 ret = new Integer(res[0][0]);
             }
 
         } catch (Exception ex) {
             System.out.println(ex.getMessage());
             throw ex;
         }
         return ret;
     }
 
     private void checkAndAddPerm(String perm, int grp, Configuration cfg) throws Exception {
         try {
             String query = "SELECT id FROM perm_permissions WHERE name='" + this.escapeString(perm) + "'";
             String[][] res = plugin.database.executeQuery(query);
             int permID = 0;
             if (res.length > 0) {
                 permID = new Integer(res[0][0]);
             } else {
                 query = "INSERT INTO perm_permissions(name) VALUES('" + this.escapeString(perm) + "')";
                 plugin.database.executeChangeQuery(query);
 
                 query = "SELECT id FROM perm_permissions WHERE name='" + this.escapeString(perm) + "'";
                 res = plugin.database.executeQuery(query);
                 if (res.length > 0) {
                     permID = new Integer(res[0][0]);
                 } else {
                     return;
                 }
             }
             query = "SELECT grpID, permID FROM perm_grp_to_perm WHERE grpID='" + grp + "' AND permID='" + permID + "'";
             res = plugin.database.executeQuery(query);
             if (res.length > 0) {
                 return;
             }
 
             query = "INSERT INTO perm_grp_to_perm(grpID, permID) VALUES(" + grp + "," + permID + ")";
             //System.out.println("[DEBUG] " + query);
             plugin.database.executeChangeQuery(query);
         } catch (Exception ex) {
             System.out.println(ex.getMessage());
             throw ex;
         }
     }
 
     private void checkAndAddPermUsr(String perm, int usr, Configuration cfg) throws Exception {
         try {
             String query = "SELECT id FROM perm_permissions WHERE name='" + this.escapeString(perm) + "'";
             String[][] res = plugin.database.executeQuery(query);
             int permID = 0;
             if (res.length > 0) {
                 permID = new Integer(res[0][0]);
             } else {
                 query = "INSERT INTO perm_permissions(name) VALUES('" + this.escapeString(perm) + "')";
                 plugin.database.executeChangeQuery(query);
 
                 query = "SELECT id FROM perm_permissions WHERE name='" + this.escapeString(perm) + "'";
                 res = plugin.database.executeQuery(query);
                 if (res.length > 0) {
                     permID = new Integer(res[0][0]);
                 } else {
                     return;
                 }
             }
             query = "SELECT usrID, permID FROM perm_user_to_perm WHERE usrID='" + usr + "' AND permID='" + permID + "'";
             res = plugin.database.executeQuery(query);
             if (res.length > 0) {
                 return;
             }
 
             query = "INSERT INTO perm_user_to_perm(usrID, permID) VALUES(" + usr + "," + permID + ")";
             //System.out.println("[DEBUG] " + query);
             plugin.database.executeChangeQuery(query);
         } catch (Exception ex) {
             System.out.println(ex.getMessage());
             throw ex;
         }
     }
 
     public void loadPermissionsFromDatabase(String world) throws Exception {
         String worldFile = "plugins/Permissions/" + world + ".yml";
         File fi = new File(worldFile);
         //Check if world is in DB
         String[][] worldArr = plugin.database.executeQuery("SELECT * FROM perm_worlds WHERE world='" + this.escapeString(world) + "'");
         if (worldArr.length == 0) {
             System.out.println("[sqlPermissions] World " + world + " does not exist in db!");
             if (fi.exists()) {
                 System.out.println("[sqlPermissions] World " + world + " file found. Loading file to DB!");
                 loadPermissionsToDatabase(world);
             }
             return;
         }
         //Delete old Config
         if (fi.exists()) {
             fi.delete();
         }
 
         //Create new Config
         Configuration cfg = new Configuration(fi);
 
         //Write World settings to Config
         String copies = worldArr[0][2];
         String system = worldArr[0][3];
         int worldID = new Integer(worldArr[0][0]);
 
         if (copies == null || copies.equals("null") || copies.equals("")) {
             cfg.setProperty("plugin.permissions.copies", null);
         } else {
             cfg.setProperty("plugin.permissions.copies", copies);
         }
 
         cfg.setProperty("plugin.permissions.system", system);
         //If World Copies an Other World Return
         if (copies != null && !copies.equals("") && !copies.equals("null")) {
             cfg.save();
             return;
         }
         cfg.save();
 
         //Get Groups from DB
         String[][] groupArr = plugin.database.executeQuery("SELECT * FROM perm_groups WHERE world='" + worldID + "'");
         //Write Groups to Config
         for (int i = 0; i < groupArr.length; i++) {
             cfg = new Configuration(fi);
             cfg.load();
             //Default Settings
             if (groupArr[i][3].equals("true")) {
                 cfg.setProperty("groups." + groupArr[i][2] + ".default", true);
             } else {
                 cfg.setProperty("groups." + groupArr[i][2] + ".default", false);
             }
             if (groupArr[i][4].equals("null")) {
                 cfg.setProperty("groups." + groupArr[i][2] + ".info.prefix", "");
             } else {
                 cfg.setProperty("groups." + groupArr[i][2] + ".info.prefix", groupArr[i][4]);
             }
             if (groupArr[i][5].equals("null")) {
                 cfg.setProperty("groups." + groupArr[i][2] + ".info.sufix", "");
             } else {
                 cfg.setProperty("groups." + groupArr[i][2] + ".info.sufix", groupArr[i][5]);
             }
             if (groupArr[i][6].equals("true")) {
                 cfg.setProperty("groups." + groupArr[i][2] + ".info.build", true);
             } else {
                 cfg.setProperty("groups." + groupArr[i][2] + ".info.build", false);
             }
             if (groupArr[i][7] != null && !groupArr[i][7].equals("")) {
                 cfg.setProperty("groups." + groupArr[i][2] + ".rank", new Integer(groupArr[i][7]));
             }
 
             int grpID = new Integer(groupArr[i][1]);
             //Get Instances from DB
             List<String> insList = new ArrayList<String>();
             String query = "SELECT g.name FROM perm_groups g, perm_instances i WHERE i.grpID='" + grpID + "' AND i.refTo=g.id";
             String[][] ins = plugin.database.executeQuery(query);
 
             for (String[] in : ins) {
                 insList.add(in[0]);
             }
             if (insList.size() > 0) {
                 cfg.setProperty("groups." + groupArr[i][2] + ".inheritance", insList);
             } else {
                 cfg.setProperty("groups." + groupArr[i][2] + ".inheritance", null);
             }
 
             //Get Group Permissions from DB
             String[][] grPermArr = plugin.database.executeQuery("SELECT p.name FROM perm_grp_to_perm gp, perm_permissions p WHERE gp.grpID='" + grpID + "' AND gp.permID=p.id");
             List<String> permList = new ArrayList<String>();
             for (String[] grPerm : grPermArr) {
                 permList.add(grPerm[0]);
             }
             if (permList.size() > 0) {
                 cfg.setProperty("groups." + groupArr[i][2] + ".permissions", permList);
             } else {
                 cfg.setProperty("groups." + groupArr[i][2] + ".permissions", null);
             }
             cfg.save();
         }
 
         //Get Users From DB
         String[][] userArr = plugin.database.executeQuery("SELECT u.id, u.login, g.name FROM perm_users u, perm_groups g WHERE u.world='" + worldID + "' AND u.grp=g.id");
         //Write Users to File
         for (int i = 0; i < userArr.length; i++) {
             cfg = new Configuration(fi);
             cfg.load();
             int userID = new Integer(userArr[i][0]);
             //Write User Group
             cfg.setProperty("users." + userArr[i][1] + ".group", userArr[i][2]);
 
             //Get User Permissions
             List<String> userPermList = new ArrayList<String>();
             String[][] userPermArr = plugin.database.executeQuery("SELECT p.name FROM perm_user_to_perm up, perm_permissions p WHERE up.usrID='" + userID + "' AND up.permID=p.id");
             for (String[] usPerm : userPermArr) {
                 userPermList.add(usPerm[0]);
             }
             //Write User permission if there are some
             if (userPermList.size() > 0) {
                 cfg.setProperty("users." + userArr[i][1] + ".permissions", userPermList);
             }
             cfg.save();
         }
     }
 
     private String escapeString(String str) {
         if (str != null) {
             String[] split = str.split("'");
             str = "";
             str = split[0];
             for (int i = 1; i < split.length; i++) {
                 str += "\\'" + split[i];
             }
             //System.out.println("[DEBUG] " + str);
         }
         return str;
     }
 }
