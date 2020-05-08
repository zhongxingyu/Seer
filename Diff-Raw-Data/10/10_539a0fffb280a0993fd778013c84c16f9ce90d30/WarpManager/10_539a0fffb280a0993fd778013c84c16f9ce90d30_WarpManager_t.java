 package nl.lolmewn.sortal;
 
 import com.google.common.io.Files;
 import java.io.*;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 /**
  *
  * @author Lolmewn
  */
 public class WarpManager {
 
     private Main plugin;
     File warpFile = new File("plugins" + File.separator
             + "Sortal" + File.separator + "warps.yml");
     File signFile = new File("plugins" + File.separator
             + "Sortal" + File.separator + "signs.yml");
     File userFile = new File("plugins" + File.separator
             + "Sortal" + File.separator + "users.yml");
     private HashMap<String, Warp> warps = new HashMap<String, Warp>();
     private HashMap<String, SignInfo> signs = new HashMap<String, SignInfo>();
     private HashMap<String, UserInfo> users = new HashMap<String, UserInfo>();
 
     private Main getPlugin() {
         return this.plugin;
     }
 
     public WarpManager(Main m) {
         this.plugin = m;
         try {
             this.checkOldVersion();
             this.loadWarps();
             this.loadSigns();
             this.loadUsers();
         } catch (Exception e) {
             //Be ready to do stuff, even if loading fails
         }
     }
 
     private void loadWarps() {
         if (this.getPlugin().getSettings().useMySQL()) {
             ResultSet set = this.getPlugin().getMySQL().executeQuery("SELECT * FROM "
                     + this.getPlugin().getWarpTable());
             if (set == null) {
                 this.getPlugin().getLogger().severe("Something is wrong with your MySQL database!");
                 this.getPlugin().getLogger().severe("Plugin is disabling!");
                 this.getPlugin().getServer().getPluginManager().disablePlugin(this.getPlugin());
                 return;
             }
             try {
                 while (set.next()) {
                     Warp w = this.addWarp(set.getString("name"), new Location(
                             this.getPlugin().getServer().getWorld(set.getString("world")),
                             set.getInt("x"), set.getInt("y"), set.getInt("z"),
                             set.getFloat("yaw"), set.getFloat("pitch")));
                     if (set.getInt("uses") != -1) {
                         w.setUses(set.getInt("uses"));
                         w.setUsed(set.getInt("used"));
                         w.setUsedTotalBased(set.getBoolean("usedTotalBased"));
                     }
                     if (set.getInt("price") != -1) {
                         w.setPrice(set.getInt("price"));
                         w.setHasPrice(true);
                     }
                     if (set.getString("owner") != null) {
                         w.setOwner(set.getString("owner"));
                     }
                     if (this.getPlugin().getSettings().isDebug()) {
                         this.getPlugin().getLogger().log(Level.INFO, String.format("Warp loaded: %s", set.getString("name")));
                     }
                 }
             } catch (SQLException ex) {
                 this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
             }
             this.getPlugin().getLogger().log(Level.INFO, String.format("Warps loaded: %s", this.warps.size()));
             return;
         }
         if (!this.warpFile.exists()) {
             try {
                 this.warpFile.createNewFile();
             } catch (IOException ex) {
                 this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
             } finally {
                 return;
             }
         }
         YamlConfiguration c = YamlConfiguration.loadConfiguration(this.warpFile);
         for (String key : c.getConfigurationSection("").getKeys(false)) {
             Warp w = this.addWarp(key, new Location(this.getPlugin().getServer().getWorld(c.getString(key + ".world")), c.getDouble(key + ".x"), c.getDouble(key + ".y"), c.getDouble(key + ".z"),
                     (float) c.getDouble(key + ".yaw"), (float) c.getDouble(key + ".pitch")));
             if (c.getInt(key + ".uses", -1) != -1) {
                 w.setUses(c.getInt(key + ".uses"));
                 w.setUsed(c.getInt(key + ".used"));
                 w.setUsedTotalBased(c.getBoolean(key + ".usedTotalBased"));
             }
             if (c.contains(key + ".owner")) {
                 w.setOwner(key + ".owner");
             }
             if (c.getInt(key + ".price", -1) != -1) {
                 w.setPrice(c.getInt(key + ".price"));
                 w.setHasPrice(true);
             }
         }
         this.getPlugin().getLogger().log(Level.INFO, String.format("Warps loaded: %s", this.warps.size()));
     }
 
     private void loadSigns() {
         if (this.getPlugin().getSettings().useMySQL()) {
             ResultSet set = this.getPlugin().getMySQL().executeQuery("SELECT * FROM "
                     + this.getPlugin().getSignTable());
             if (set == null) {
                 this.getPlugin().getLogger().severe("Something is wrong with your MySQL database!");
                 this.getPlugin().getLogger().severe("Plugin is disabling!");
                 this.getPlugin().getServer().getPluginManager().disablePlugin(this.getPlugin());
                 return;
             }
             try {
                 while (set.next()) {
                     Location loc = new Location(
                             this.getPlugin().getServer().getWorld(set.getString("world")),
                             set.getInt("x"),
                             set.getInt("y"),
                             set.getInt("z"));
                     this.addSign(loc).setWarp(set.getString("warp"));
                     if (set.getInt("price") != -1) {
                         SignInfo added = this.getSign(loc);
                         if (added == null) {
                             //dafuq?
                             continue;
                         }
                         added.setPrice(set.getInt("price"));
                     }
                     if (set.getInt("uses") != -1) {
                         this.getSign(loc).setUses(set.getInt("uses"));
                         this.getSign(loc).setUsedTotalBased(set.getBoolean("usedTotalBased"));
                         this.getSign(loc).setUsed(set.getInt("used"));
                     }
                 }
             } catch (SQLException ex) {
                 this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
             }
             this.getPlugin().getLogger().log(Level.INFO, String.format("Signs loaded: %s", this.signs.size()));
             return;
         }
         if (!this.signFile.exists()) {
             try {
                 this.signFile.createNewFile();
             } catch (IOException ex) {
                 this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
             } finally {
                 return;
             }
         }
         YamlConfiguration c = YamlConfiguration.loadConfiguration(this.signFile);
         for (String key : c.getConfigurationSection("").getKeys(false)) {
             //Key is location
             if (!key.contains(",")) {
                 //dafuq?
                 continue;
             }
             String[] splot = key.split(",");
             Location loc = new Location(this.getPlugin().getServer().getWorld(splot[0]),
                     Integer.parseInt(splot[1]),
                     Integer.parseInt(splot[2]),
                     Integer.parseInt(splot[3]));
             String extra = c.getString(key);
             if (extra.contains(",")) {
                 String[] split = extra.split(",");
                 String warp = split[0];
                 int price = Integer.parseInt(split[1]);
                 this.addSign(loc);
                 this.getSign(loc).setWarp(warp);
                 if (price != -1) {
                     this.getSign(loc).setPrice(price);
                 }
                 if (split.length > 4) {
                     int uses = Integer.parseInt(split[2]);
                     if (uses != -1) {
                         this.getSign(loc).setUses(uses);
                         this.getSign(loc).setUsed(Integer.parseInt(split[3]));
                         this.getSign(loc).setUsedTotalBased(Boolean.getBoolean(split[4]));
                     }
                 }
                 continue;
             }
             this.addSign(loc).setWarp(extra);
         }
         this.getPlugin().getLogger().log(Level.INFO, String.format("Signs loaded: %s", this.signs.size()));
     }
 
     private void loadUsers() {
         if (this.getPlugin().getSettings().useMySQL()) {
             ResultSet set = this.getPlugin().getMySQL().executeQuery("SELECT * FROM " + this.getPlugin().getUserTable());
             if (set == null) {
                 this.getPlugin().getLogger().severe("Something is wrong with your MySQL database!");
                 this.getPlugin().getLogger().severe("Plugin is disabling!");
                 this.getPlugin().getServer().getPluginManager().disablePlugin(this.getPlugin());
                 return;
             }
             try {
                 while (set.next()) {
                     String player = set.getString("player");
                    UserInfo info = this.getUserInfo(player);
                     if (set.getString("warp") == null) {
                         //location
                         info.addtoUsedLocation(new Location(this.getPlugin().getServer().getWorld(set.getString("world")),
                                 set.getInt("x"), set.getInt("y"), set.getInt("z")), set.getInt("used"));
                         continue;
                     }
                     info.addtoUsedWarp(set.getString("warp"), set.getInt("used"));
                 }
             } catch (SQLException ex) {
                 this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
             }
             return;
         }
         YamlConfiguration c = YamlConfiguration.loadConfiguration(this.userFile);
         for (String player : c.getConfigurationSection("").getKeys(false)) {
            UserInfo info = this.getUserInfo(player);
             for (String key : c.getConfigurationSection(player).getKeys(false)) {
                 if (!key.contains(",")) {
                     //warp
                     info.addtoUsedWarp(key, c.getInt(player + "." + key));
                     continue;
                 }
                 String[] splot = key.split(",");
                 Location loc = new Location(this.getPlugin().getServer().getWorld(splot[0]),
                         Integer.parseInt(splot[1]),
                         Integer.parseInt(splot[2]),
                         Integer.parseInt(splot[3]));
                 info.addtoUsedLocation(loc, c.getInt(player + "." + key));
             }
         }
         this.getPlugin().getLogger().log(Level.INFO, String.format("Users loaded: %s", this.users.size()));
     }
 
     protected Warp addWarp(String name, Location loc) {
         this.warps.put(name, new Warp(name, loc));
         Warp w = this.getWarp(name);
         if (this.getPlugin().getSettings().useMySQL()) {
             w.save(this.getPlugin().getMySQL(), this.getPlugin().getWarpTable());
         } else {
             w.save(this.warpFile);
         }
         return w;
     }
 
     protected Warp addWarp(String name, Location loc, int price) {
         Warp w = this.addWarp(name, loc);
         w.setPrice(price);
         return w;
     }
 
     protected SignInfo addSign(Location loc) {
         this.signs.put(loc.getWorld().getName() + ","
                 + loc.getBlockX() + ","
                 + loc.getBlockY() + ","
                 + loc.getBlockZ(), new SignInfo(
                 loc.getWorld().getName(),
                 loc.getBlockX(),
                 loc.getBlockY(),
                 loc.getBlockZ()));
         return this.getSign(loc);
     }
 
     protected UserInfo addUserInfo(String name) {
         this.users.put(name, new UserInfo(name));
         return this.getUserInfo(name);
     }
 
     protected UserInfo getUserInfo(String name) {
         if (this.hasUserInfo(name)) {
             return this.users.get(name);
         }
         return this.addUserInfo(name);
     }
 
     protected SignInfo getSign(Location loc) {
         return this.signs.get(loc.getWorld().getName() + ","
                 + loc.getBlockX() + ","
                 + loc.getBlockY() + ","
                 + loc.getBlockZ());
     }
 
     protected Warp removeWarp(String name) {
         if (!this.hasWarp(name)) {
             return null;
         }
         if (this.getPlugin().getSettings().useMySQL()) {
             this.warps.get(name).delete(this.getPlugin().getMySQL(), this.getPlugin().getWarpTable());
         } else {
             this.warps.get(name).delete(this.warpFile);
         }
         return this.warps.remove(name);
     }
 
     protected void removeSign(Location loc) {
         SignInfo s = this.getSign(loc);
         if (s == null) {
             return;
         }
         if (this.getPlugin().getSettings().useMySQL()) {
             s.delete(this.getPlugin().getMySQL(), this.getPlugin().getSignTable());
         } else {
             s.delete(this.signFile);
         }
     }
 
     protected boolean hasSignInfo(Location loc) {
         return this.getSign(loc) == null ? false : true;
     }
 
     protected boolean hasWarp(String name) {
         return this.warps.containsKey(name);
     }
 
     protected boolean hasUserInfo(String user) {
         return this.users.containsKey(user);
     }
 
     public Warp getWarp(String name) {
         return this.warps.get(name);
     }
 
     public void saveData() {
         for (String name : this.warps.keySet()) {
             if (this.getPlugin().getSettings().isDebug()) {
                 this.getPlugin().getLogger().log(Level.INFO, String.format("[Debug] Saving warp %s", name));
             }
             if (this.getPlugin().getSettings().useMySQL()) {
                 this.warps.get(name).save(this.getPlugin().getMySQL(), this.getPlugin().getWarpTable());
             } else {
                 this.warps.get(name).save(this.warpFile);
             }
         }
         for (String loc : this.signs.keySet()) {
             if (this.getPlugin().getSettings().isDebug()) {
                 this.getPlugin().getLogger().log(Level.INFO, String.format("[Debug] Saving sign at %s", loc));
             }
             if (this.getPlugin().getSettings().useMySQL()) {
                 this.signs.get(loc).save(this.getPlugin().getMySQL(), this.getPlugin().getSignTable());
             } else {
                 this.signs.get(loc).save(this.signFile);
             }
         }
         for (String user : this.users.keySet()) {
             if (this.getPlugin().getSettings().isDebug()) {
                 this.getPlugin().getLogger().log(Level.INFO, String.format("[Debug] Saving user %s", user));
             }
             if (this.getPlugin().getSettings().useMySQL()) {
                 this.users.get(user).save(this.getPlugin().getMySQL(), this.getPlugin().getUserTable());
             } else {
                 this.users.get(user).save(this.userFile);
             }
         }
     }
 
     public Set<Warp> getWarps() {
         Set<Warp> warpSet = new HashSet<Warp>();
         for (String warp : this.warps.keySet()) {
             warpSet.add(this.warps.get(warp));
         }
         return warpSet;
     }
 
     public Set<SignInfo> getSigns() {
         Set<SignInfo> warpSet = new HashSet<SignInfo>();
         for (String warp : this.signs.keySet()) {
             warpSet.add(this.signs.get(warp));
         }
         return warpSet;
     }
 
     private void checkOldVersion() {
         File old = new File("plugins" + File.separator + "Sortal" + File.separator + "warps.txt");
         if (old.exists()) {
             BufferedReader in1 = null;
             try {
                 //Old version!
                 this.getPlugin().getLogger().log(Level.INFO, "Old Sortal saving system found, converting!");
                 in1 = new BufferedReader(new FileReader(old));
                 String str;
                 while ((str = in1.readLine()) != null) {
                     if (str.startsWith("#")) {
                         continue;
                     }
                     String warp = "unknownWarp";
                     try {
                         String[] split = str.split("=");
                         warp = split[0];
                         String[] rest = split[1].split(",");
                         Warp w = this.addWarp(warp, new Location(
                                 this.getPlugin().getServer().getWorld(rest[0]),
                                 Double.parseDouble(rest[1]),
                                 Double.parseDouble(rest[2]),
                                 Double.parseDouble(rest[3])));
                         if (rest.length == 5) {
                             //Also has a price
                             w.setPrice(Integer.parseInt(rest[4]));
                         }
                         this.getPlugin().getLogger().log(Level.INFO, String.format("Converted warp %s", warp));
                     } catch (ArrayIndexOutOfBoundsException e) {
                         this.getPlugin().getLogger().info(String.format("Warp %s couldn't be converted : Too little arguments!", warp));
                     }
                 }
             } catch (IOException ex) {
                 this.getPlugin().getLogger().log(Level.SEVERE, null, ex);
             } finally {
                 try {
                     in1.close();
                 } catch (IOException ex) {
                     this.getPlugin().getLogger().log(Level.WARNING, null, ex);
                 }
             }
             try {
                 Files.move(old, new File(old.getParentFile(), "warps_old.txt"));
             } catch (IOException ex) {
                 this.getPlugin().getLogger().log(Level.WARNING, null, ex);
             }
         }
         File oldSigns = new File(old.getParentFile(), "signs.txt");
         if (oldSigns.exists()) {
             try {
                 BufferedReader in1 = new BufferedReader(new FileReader(oldSigns));
                 String str;
                 while ((str = in1.readLine()) != null) {
                     if (str.startsWith("#")) {
                         continue;
                     }
                     if (!str.contains("=")) {
                         continue;
                     }
                     String[] split = str.split("=");
                     String warp = split[1];
                     String[] rest = split[0].split(",");
                     if (rest.length == 3) {
                         if (isInt(rest[0]) && isInt(rest[1]) && isInt(rest[2])) {
                             SignInfo s = this.addSign(new Location(this.getPlugin().getServer().getWorlds().get(0), Integer.parseInt(rest[0]), Integer.parseInt(rest[1]), Integer.parseInt(rest[2])));
                             s.setWarp(warp);
                             continue;
                         }
                         continue;
                     }
                     if (rest.length == 4) {
                         if (isInt(rest[0]) && isInt(rest[1]) && isInt(rest[2])) {
                             SignInfo s = this.addSign(new Location(this.getPlugin().getServer().getWorld(rest[3]), Integer.parseInt(rest[0]), Integer.parseInt(rest[1]), Integer.parseInt(rest[2])));
                             s.setWarp(warp);
                             continue;
                         } else if (isInt(rest[3]) && isInt(rest[1]) && isInt(rest[2])) {
                             SignInfo s = this.addSign(new Location(this.getPlugin().getServer().getWorld(rest[0]), Integer.parseInt(rest[1]), Integer.parseInt(rest[2]), Integer.parseInt(rest[3])));
                             s.setWarp(warp);
                             continue;
                         } else {
                             continue;
                         }
                     }
                 }
                 in1.close();
                 this.getPlugin().getLogger().log(Level.INFO, String.format("Managed to save %s signs!", this.warps.size()));
             } catch (FileNotFoundException e) {
                 this.getPlugin().getLogger().log(Level.WARNING, null, e);
             } catch (IOException e) {
                 this.getPlugin().getLogger().log(Level.WARNING, null, e);
             }
             try {
                 Files.move(oldSigns, new File(oldSigns.getParentFile(), "signs_old.txt"));
             } catch (IOException ex) {
                 this.getPlugin().getLogger().log(Level.WARNING, null, ex);
             }
             this.saveData();
         }
     }
 
     public boolean isInt(String str) {
         try {
             Integer.parseInt(str);
             return true;
         } catch (NumberFormatException e) {
             return false;
         }
     }
 }
