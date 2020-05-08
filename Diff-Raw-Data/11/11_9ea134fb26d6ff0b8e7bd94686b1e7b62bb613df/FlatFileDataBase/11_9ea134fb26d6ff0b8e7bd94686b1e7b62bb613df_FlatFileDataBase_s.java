 package com.gmail.rmb1993.forgeperms.database;
 
 import com.gmail.rmb1993.forgeperms.ForgePermsContainer;
 import com.gmail.rmb1993.forgeperms.config.Configuration;
 import com.gmail.rmb1993.forgeperms.permissions.group.Group;
 import com.gmail.rmb1993.forgeperms.permissions.group.Track;
 import com.gmail.rmb1993.forgeperms.permissions.user.User;
 import cpw.mods.fml.common.FMLLog;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.yaml.snakeyaml.DumperOptions;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.nodes.Tag;
 import org.yaml.snakeyaml.representer.Representer;
 
 /**
  *
  * @author Ryan
  */
 public class FlatFileDataBase extends DataBase {
 
     private File location;
     private boolean global;
 
     public FlatFileDataBase(ForgePermsContainer fpc, File location) {
         super(fpc);
         this.location = location;
         if (location.exists() == false) {
             location.mkdirs();
         }
         Configuration config = fpc.config;
         global = config.isGlobal();
     }
 
     @Override
     public void loadCustomNodes() {
         File file = new File(location + "/customNodes.yml");
         if (file.exists() == false) {
             try {
                 System.out.println("Creating Example Custom Nodes");
 
                 file.createNewFile();
 
                 ArrayList<String> nodes = new ArrayList();
 
                 nodes.add("node.example.1");
                 nodes.add("node.example.2");
                 nodes.add("node.example.3");
 
                 fpc.customNodes.put("customNode.example", nodes);
 
                 Yaml yaml = new Yaml();
                 OutputStream output = null;
                 try {
                     output = new FileOutputStream(file);
                 } catch (FileNotFoundException ex) {
                     Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 OutputStreamWriter osw = new OutputStreamWriter(output);
                 yaml.dump(fpc.customNodes, osw);
                 try {
                     osw.close();
                     output.close();
                 } catch (IOException ex) {
                     Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
                 }
             } catch (IOException ex) {
                 Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         Yaml yaml = new Yaml();
         InputStream input = null;
         try {
             input = new FileInputStream(file);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             fpc.customNodes = (HashMap<String, ArrayList<String>>) yaml.load(input);
         } catch (Exception e) {
             FMLLog.log(Level.SEVERE, e, "Exception encountered attempting YAML parsing of %s", file);
         }
         try {
             input.close();
         } catch (IOException ex) {
             Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public void loadUsers() {
         File file = new File(location + "/users.yml");
         if (file.exists() == false) {
             try {
                 file.createNewFile();
             } catch (IOException ex) {
                 Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         Representer representer = new Representer();
         representer.addClassTag(User.class, new Tag("!user"));
         DumperOptions options = new DumperOptions();
         options.setExplicitStart(true);
        Yaml yaml = new Yaml(representer, options);
 
         InputStream input = null;
         try {
             input = new FileInputStream(file);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             for (Object obj : yaml.loadAll(input)) {
                 User u = (User) obj;
                 ArrayList<String> groupsToRemove = new ArrayList();
                 for (String group : u.getGroups()) {
                     Group g = getGroup(group);
                     if (g == null) {
                         groupsToRemove.add(group);
                     }
                 }
                 for (String group : groupsToRemove) {
                     u.getGroups().remove(group);
                 }
                 if (u.getGroups().isEmpty()) {
                     Group g = new Group();
                     g.setGroupName(fpc.config.getDefaultGroup());
                     u.getGroups().add(g.getGroupName());
                 }
                 System.out.println("Loaded User: " + u.getUserName());
                 fpc.users.put(u.getUserName(), u);
             }
         } catch (Exception e) {
             FMLLog.log(Level.SEVERE, e, "Exception encountered attempting YAML parsing of %s", file);
         }
         try {
             input.close();
         } catch (IOException ex) {
             Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         saveUsers();
     }
 
     @Override
     public void createUser(String userName) {
         userName = userName.toLowerCase();
         if (fpc.users.containsKey(userName)) {
             System.out.println("User " + userName + " is already in file!");
             return;
         }
 
         User u = new User();
         u.setUserName(userName);
 
         u.setCustomPermissions(new HashMap<String, List<String>>());
 
         Group g = new Group();
         g.setGroupName(fpc.config.getDefaultGroup());
         u.setGroups(new ArrayList<String>());
         u.getGroups().add(g.getGroupName());
         u.setPermissions(new HashMap<String, List<String>>());
 
         u.setVars(new HashMap<String, String>());
 
 
         File file = new File(location + "/users.yml");
         if (file.exists() == false) {
             try {
                 file.createNewFile();
             } catch (IOException ex) {
                 Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         Representer representer = new Representer();
         representer.addClassTag(User.class, new Tag("!user"));
         DumperOptions options = new DumperOptions();
         options.setExplicitStart(true);
         Yaml yaml = new Yaml(representer, options);
 
         OutputStream output = null;
         try {
             output = new FileOutputStream(file);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
         }
         OutputStreamWriter osw = new OutputStreamWriter(output);
         fpc.users.put(userName, u);
         yaml.dumpAll(fpc.users.values().iterator(), osw);
         try {
             osw.close();
             output.close();
         } catch (IOException ex) {
             Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public User getUser(String userName) {
         userName = userName.toLowerCase();
         if (fpc.users.containsKey(userName)) {
             return fpc.users.get(userName);
         }
         if (userName.equalsIgnoreCase("server")) {
             User u = new User();
             u.setUserName(userName);
             Group g = new Group();
             g.setGroupName("default");
             u.setGroups(new ArrayList<String>());
             u.getGroups().add(g.getGroupName());
             u.setPermissions(new HashMap<String, List<String>>());
             u.setVars(new HashMap<String, String>());
             return u;
         }
         return null;
     }
 
     @Override
     public void createGroup(String groupName) {
         groupName = groupName.toLowerCase();
         if (fpc.groups.containsKey(groupName)) {
             System.out.println("Group " + groupName + " is already in file!");
             return;
         }
 
         Group u = new Group();
         u.setGroupName(groupName);
 
         u.setCustomPermissions(new HashMap<String, List<String>>());
 
         u.setInheritance(new ArrayList<String>());
 
         u.setPermissions(new HashMap<String, List<String>>());
 
         u.setVars(new HashMap<String, String>());
         u.setTrack("default");
         if (fpc.tracks.containsKey("default") == false) {
             fpc.tracks.put("default", new Track());
         }
 
         Track track = fpc.tracks.get("default");
         track.addGroup(u);
 
         File file = new File(location + "/groups.yml");
         if (file.exists() == false) {
             try {
                 file.createNewFile();
             } catch (IOException ex) {
                 Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         Representer representer = new Representer();
         representer.addClassTag(Group.class, new Tag("!group"));
         DumperOptions options = new DumperOptions();
         options.setExplicitStart(true);
         Yaml yaml = new Yaml(representer, options);
         OutputStream output = null;
         try {
             output = new FileOutputStream(file);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
         }
         OutputStreamWriter osw = new OutputStreamWriter(output);
         fpc.groups.put(groupName, u);
         yaml.dumpAll(fpc.groups.values().iterator(), osw);
         try {
             osw.close();
             output.close();
         } catch (IOException ex) {
             Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public void loadGroups() {
         File file = new File(location + "/groups.yml");
         if (file.exists() == false) {
             try {
                 file.createNewFile();
             } catch (IOException ex) {
                 Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         Representer representer = new Representer();
         representer.addClassTag(Group.class, new Tag("!group"));
         DumperOptions options = new DumperOptions();
         options.setExplicitStart(true);
        Yaml yaml = new Yaml(representer, options);
         InputStream input = null;
         try {
             input = new FileInputStream(file);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         try {
             for (Object obj : yaml.loadAll(input)) {
                 Group u = (Group) obj;
                 if (fpc.tracks.containsKey(u.getTrack()) == false) {
                     fpc.tracks.put(u.getTrack(), new Track());
                 }
                 Track track = fpc.tracks.get(u.getTrack());
                 track.addGroup(u);
                 fpc.groups.put(u.getGroupName(), u);
             }
         } catch (Exception e) {
             FMLLog.log(Level.SEVERE, e, "Exception encountered attempting YAML parsing of %s", file);
             //System.exit(0);
         }
         try {
             input.close();
         } catch (IOException ex) {
             Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         for (Group g : fpc.groups.values()) {
             ArrayList<String> groupsToRemove = new ArrayList();
             for (String group : g.getInheritance()) {
                 if (g == null) {
                     groupsToRemove.add(group);
                 }
             }
             for (String group : groupsToRemove) {
                 g.getInheritance().remove(group);
             }
         }
         saveGroups();
     }
 
     @Override
     public Group getGroup(String groupName) {
         groupName = groupName.toLowerCase();
         if (fpc.groups.containsKey(groupName)) {
             return fpc.groups.get(groupName);
         }
         return null;
     }
 
     @Override
     public void removeGroup(String groupName) {
         groupName = groupName.toLowerCase();
         fpc.groups.remove(groupName);
 
         for (User u : fpc.users.values()) {
             u.getGroups().remove(groupName);
             if (u.getGroups().isEmpty()) {
                 u.getGroups().add(fpc.config.getDefaultGroup());
             }
         }
         saveUsers();
         for (Group g : fpc.groups.values()) {
             g.getInheritance().remove(groupName);
         }
         saveGroups();
     }
 
     @Override
     public void saveUsers() {
         File file = new File(location + "/users.yml");
         if (file.exists() == false) {
             try {
                 file.createNewFile();
             } catch (IOException ex) {
                 Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         Representer representer = new Representer();
         representer.addClassTag(User.class, new Tag("!user"));
         DumperOptions options = new DumperOptions();
         options.setExplicitStart(true);
         Yaml yaml = new Yaml(representer, options);
         OutputStream output = null;
         try {
             output = new FileOutputStream(file);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
         }
         OutputStreamWriter osw = new OutputStreamWriter(output);
         yaml.dumpAll(fpc.users.values().iterator(), osw);
         try {
             osw.close();
             output.close();
         } catch (IOException ex) {
             Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public void saveGroups() {
         File file = new File(location + "/groups.yml");
         if (file.exists() == false) {
             try {
                 file.createNewFile();
             } catch (IOException ex) {
                 Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         Representer representer = new Representer();
         representer.addClassTag(Group.class, new Tag("!group"));
         DumperOptions options = new DumperOptions();
         options.setExplicitStart(true);
         Yaml yaml = new Yaml(representer, options);
         OutputStream output = null;
         try {
             output = new FileOutputStream(file);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
         }
         OutputStreamWriter osw = new OutputStreamWriter(output);
         yaml.dumpAll(fpc.groups.values().iterator(), osw);
         try {
             osw.close();
             output.close();
         } catch (IOException ex) {
             Logger.getLogger(FlatFileDataBase.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 }
