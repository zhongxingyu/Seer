 /**
  * This is the main plugin source file. I know it contains tons of code instead
  * of being nicely encapsulated into individual classes and objects. But, once
  * you get past the idea of how hard it is to scroll around in this file it 
  * actually becomes quite nice because you have very little redirection. By
  * redirection I mean you dont have to jump through 3 different source files
  * trying to trace the execution path to understand how something works. I
  * would however like to find a way to split some out and group it up but at
  * the moment I have other things I am working on and doing major moves of code
  * around can cause lots of major problems so it would require testing time I
  * do not quite have at the moment. 
  * 
  * So try to bare with it and love it instead of hate it!
  */
 package com.kmcguire.KFactions;
 
 import com.dthielke.herochat.ChannelChatEvent;
 import com.dthielke.herochat.MessageFormatSupplier;
 import com.dthielke.herochat.StandardChannel;
 import com.kmcguire.BukkitUpdateChecker.UpdateChecker;
 import com.kmcguire.BukkitUpdateChecker.Version;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventException;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.plugin.EventExecutor;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /** This is used to represent the command. Its usage is forced by Bukkit in order
  *  to call the method which handles command execution. At the time of writting
  *  this I only use it with the auto-claim feature so I can callback and have
  *  the code for /f claim executed instead having to implement that directly
  *  in the auto-claimer and thus duplicate code.
  * 
  */
 class __LocalCommandObject extends Command {
     public __LocalCommandObject(String cmd) {
         super(cmd);
     }
 
     @Override
     public boolean execute(CommandSender paramCommandSender, String paramString, String[] paramArrayOfString) {
         return false;
     }
 }
 
 class DataDumper implements Runnable {
     P           p;
     DataDumper(P p) {
         this.p = p;
     }
 
     @Override
     public void run() {
         synchronized(p) {
             try {
                 //SLAPI.save(p.factions, "plugin.data.factions");
                 p.DumpHumanReadableData();
                 p.smsg(Language.get("DATASAVE_SUCCESS"));
             } catch (Exception ex) {
                 ex.printStackTrace();
                 p.smsg(Language.get("DATASAVE_FAILED"));
             }
             /// SCHEDULE OURSELVES TO RUN AGAIN ONCE WE ARE DONE
             p.getServer().getScheduler().scheduleAsyncDelayedTask(p, this, 20 * 60 * 10);
         }
     }
 }
 
 class LocaleFile {
     private HashMap<String, Object>     dict;
     
     public String getString(String key) {
         Object          out;
         
         out = dict.get(key);
         
         if (out instanceof String) {
             return (String)out;
         }
         
         return null;
     }
     
     public List<String> getStringList(String key) {
         Object          out;
         
         out = dict.get(key);
         
         if (out instanceof List) {
             return (List<String>)out;
         }
         
         return null;        
     }
     
     public LocaleFile(byte[] buf) 
         throws UnsupportedEncodingException {
         List<String>            lines;
         String                  key;
         String                  value;
         boolean                 isList;
         List                    listElement;
         
         lines = readLines(buf);
         
         dict = new HashMap<String, Object>();
         isList = false;
         listElement = null;
         
         for (String line : lines) {
             //Bukkit.getLogger().info(String.format("line:%s", line));
             if (line.startsWith(":")) {
                 if (listElement != null) {
                     line = line.substring(1);
                     listElement.add(line);
                     //Bukkit.getLogger().info(String.format("***%s", line));
                 }
             } else {
                 if (line.indexOf("=") > -1) {
                     isList = false;
                     key = line.substring(0, line.indexOf("=")).trim();
                     value = line.substring(line.indexOf("=") + 1);
                     //Bukkit.getLogger().info(String.format("%s=%s", key, value));
                     dict.put(key, value);
                 } else {
                     //Bukkit.getLogger().info(String.format("list:%s", line.trim()));
                     listElement = new ArrayList();
                     dict.put(line.trim(), listElement);
                 }
             }
             continue;
         }
     }
     
     private byte[] copyBytes(byte[] buf, int offset, int length) {
         byte[]      out;
         
         out = new byte[length];
         
         for (int x = 0; x < length; ++x) {
             out[x] = buf[offset + x];
         }
         
         return out;
     }
     
     private List readLines(byte[] buf)
             throws UnsupportedEncodingException {
         List<String>                list;
         byte[]                      line;
         int                         x;
         int                         offset;
         
         list = new ArrayList<String>();
         
         x = 0;
         offset = 0;
         
         /* skip the UTF-8 signature */
         if (buf[0] == 0xef && buf[1] == 0xbb && buf[2] == 0xbf) {
             x = 3;
             offset = 3;
         }
         
         for (; x < buf.length; ++x) {
             if (buf[x] == 0x0a || buf[x] == 0x0d) {
                 /* we dont have an actual line */
                 if (x - offset < 1) {
                     offset = x + 1;
                     continue;
                 }
                 
                 line = copyBytes(buf, offset, x - offset);
                 list.add(new String(line, "UTF-8"));
                 offset = x + 1;
                 //Bukkit.getLogger().info(String.format("[%d:%d]:%s", buf[offset+0], buf[offset+1], new String(line)));
                 continue;
             }
         }
         
         if (x - offset > 0) {
             line = new byte[x - offset];
             line = copyBytes(buf, offset, x - offset);
             list.add(new String(line, "UTF-8"));
         }
         
         return list;
     }
 }
 
 class Language {
     private static LocaleFile              lang;
     private static LocaleFile              langdef;     
     private static String                  langName;
     
     public static String get(String id) {
         String          o;
         
         o = lang.getString(id);
         if (o == null) {
             Bukkit.getLogger().info(String.format("WARNING: LANG[%s] ID:%s NOT FOUND", langName, id));
             o = langdef.getString(id);
             if (o == null) {
                 Bukkit.getLogger().info(String.format("WARNING: LANG[locale.en] ID:%s NOT FOUND", id));
             }
         }
         
         return o;
     }
     
     public static List<String> getList(String id) {
         List<String>            l;
         
         l = lang.getStringList(id);
         if (l == null) {
             Bukkit.getLogger().info(String.format("WARNING: LANG[%s] ID:%s NOT FOUND", langName, id));
             l = langdef.getStringList(id);
             if (l == null) {
                 Bukkit.getLogger().info(String.format("WARNING: LANG[locale.en] ID:%s NOT FOUND", id));
             }
         }
         
         return l;
     }
     
     public static byte[] readInputStream(InputStream is) 
                 throws IOException {
         ByteArrayOutputStream           buf;
         int                             cnt;
         byte[]                          _buf;
         
         buf = new ByteArrayOutputStream();
         _buf = new byte[1024];
         
         while ((cnt = is.read(_buf, 0, 1024)) > -1) {
             buf.write(_buf, 0, cnt);
         }
         
         return buf.toByteArray();
     }
     
     public static boolean loadFrom(String name) {
         lang = loadLocaleFile(name);
         if (lang == null) {
             return false;
         }
         
         return true;
     }
     
     public static LocaleFile loadLocaleFile(String name) {
         InputStream             is;
         byte[]                  buf;
         LocaleFile              lf;
 
         is = Language.class.getClassLoader().getResourceAsStream(name);
         lf = null;
         
         if (is == null) {
             return null;
         }
         
         try {
             buf = readInputStream(is);
             lf = new LocaleFile(buf);
         } catch (IOException ex) {
             ex.printStackTrace();
         }
         
         return lf;
     }
     
     static {
         langdef = loadLocaleFile("locale.en");
         lang = null;
     }
 }
 
 public class P extends JavaPlugin implements Listener {
     public Map<String, Faction>                 factions;
     private boolean                             saveToDisk;
     public static final File                    fdata;
     
     private static HashMap<String, Long>        seeChunkLast;
     private HashMap<String, Long>               scannerWait;
     private HashMap<String, FactionPlayer>      fpquickmap;
     
     private UpdateChecker                       updateChecker;
 
     // IF A PLAYER NAME IS SPECIFIED IN THIS SET THEN THEY WILL
     // AUTOMATICALLY TRY TO CLAIM THE LAND IF IT IS UNCLAIMED
     // THEY MUST HAVE THE PROPER RANK TO CLAIM LAND OR ELSE THEY
     // WILL REPEATEDLY GET AN ERROR MESSAGE
     private HashSet<String>                     autoClaim;
     // IF A PLAYER NAME IS SPECIFIED IN THIS SET THEN THEY WILL
     // BE SENT A FEW LINES OF CHAT DISPLAYING A FACTION MAP EACH
     // TIME THEY TRANSITION FROM ONE CHUNK TO ANOTHER
     private HashSet<String>                     mapView;
     
     static final int    NOPVP =        0x01;
     static final int    NOBOOM =       0x02;
     static final int    NODECAY =      0x04;
     
     // THIS HOLDS THE MAPPING OF AMOUNT OF CHARGE TO SPECIFIC HOLDABLE
     // ITEMS AND BLOCKS.
     public HashMap<Long, Integer>                emcMap;
     
     // THIS ARE ALL GOING TO BE CONFIGURATION VALUES
     public double           landPowerCostPerHour;
     public HashSet<String>  worldsEnabled; 
     boolean                 enabledScanner;
     long                    scannerWaitTime;
     double                  scannerChance;
     boolean                 friendlyFire;
     HashSet<String>         noGriefPerWorld;
     double                  powerUsedToProtectBlock;
     boolean                 randomModifierOnProtectBlock;
     double                  percentageofPowerOnTeleport;
     boolean                 repawnAtFactionHome;
     //
     double                  powerUsedEachClaim;
     int                     numberOfFreeClaims;
     //
     String                  bypassPermission;
     boolean                 requirePermission;
     String                  usagePermission;
     //
     private int             opRank;
     
     
     // THIS HOLDS THE SPAWN LOCATION _IF_ IT HAS BEEN ENABLED, THIS IS
     // ACTUALLY AN UNDOCUMENTED FEATURE AT THE MOMENT
     public Location     gspawn = null;
     
     // THIS IS USED TO UPGRADE FROM OLDER DATA FORMATS
     public boolean      upgradeCatch;  
     
     // SHOULD HAVE ONCE BEEN USED BY SOME WORKAROUND CODE IN AN
     // EXTERNAL PLUGIN BUT NOT SURE ANYMORE
     public static P     __ehook;
     
     static {
         fdata = new File("kfactions.data.yml");
     }
     
     public P() {        
         __ehook = this;
     }
     
     public Map<String, Faction> LoadHumanReadableData() throws InvalidConfigurationException {
         return LoadHumanReadableData(fdata);
     }
     
     public Map<String, Faction> LoadHumanReadableData(File _fdata) throws InvalidConfigurationException {
         YamlConfiguration                   cfg;
         ConfigurationSection                cfg_root;
         ConfigurationSection                cfg_chunks;
         ConfigurationSection                cfg_friends;
         ConfigurationSection                cfg_invites;
         ConfigurationSection                cfg_players;
         ConfigurationSection                cfg_walocs;
         ConfigurationSection                cfg_zapin;
         ConfigurationSection                cfg_zapout;
         List<String>                        cfg_slist;
         Map<String, Object>                 m;
         Faction                             f;
         FactionChunk                        fc;
         LinkedList<ConfigurationSection>    zaps;  
         HashMap<String, Faction>            allfactions;
         
         zaps = new LinkedList<ConfigurationSection>();
         cfg = new YamlConfiguration();
         allfactions = new HashMap<String, Faction>();
 
         try {
             cfg.load(_fdata);
         } catch (FileNotFoundException ex) {
             return null;
         } catch (IOException ex) {
             return null;
         }
         
         getLogger().info(Language.get("DATALOAD_DATANOWINMEMORY"));
         
         m = cfg.getValues(false);
         
         for (Entry<String, Object> e : m.entrySet()) {
             cfg_root = (ConfigurationSection)e.getValue();
             
             f = new Faction();
             f.chunks = new HashMap<String, Map<Long, FactionChunk>>();
             
             f.lpud = System.currentTimeMillis();
             
             // access all of the list/array/map type stuff
             cfg_chunks = cfg_root.getConfigurationSection("chunks");
             if (cfg_chunks != null) {
                 //getLogger().info("CHUNKS NOT NULL");
                 for (String key : cfg_chunks.getKeys(false)) {
                     ConfigurationSection        ccs;
                     ConfigurationSection        _ccs;
                     int                         m1, m2, m3;
                     
                     fc = new FactionChunk();
 
                     m1 = key.indexOf('*');
                     m2 = key.indexOf('*', m1 + 1);
                     
                     //getLogger().info(String.format("key:%s", key));
 
                     fc.worldName = key.substring(0, m1);
                     fc.x = Integer.parseInt(key.substring(m1 + 1, m2));
                     fc.z = Integer.parseInt(key.substring(m2 + 1));
                     
                     fc.builders = null;
                     fc.users = null;
                     fc.faction = f;
 
                     ccs = cfg_chunks.getConfigurationSection(key);
                     fc.mru = ccs.getInt("mru");
                     fc.mrb = ccs.getInt("mrb");
                     
                     _ccs = ccs.getConfigurationSection("tid");
                     fc.tid = new HashMap<Integer, Integer>();
                     if (_ccs != null) {
                         for (Entry<String, Object> en : _ccs.getValues(false).entrySet()) {
                             fc.tid.put(Integer.parseInt(en.getKey()), (Integer)en.getValue());
                         }
                     }
                     
                     _ccs = ccs.getConfigurationSection("tidu");
                     fc.tidu = new HashMap<Integer, Integer>();
 
                     if (_ccs != null) {
                         for (Entry<String, Object> en : _ccs.getValues(false).entrySet()) {
                             fc.tid.put(Integer.parseInt(en.getKey()), (Integer)en.getValue());
                         }
                     }
                     
                     if (f.chunks.get(fc.worldName) == null) {
                         f.chunks.put(fc.worldName, new HashMap<Long, FactionChunk>());
                     }
                     
                     f.chunks.get(fc.worldName).put(LongHash.toLong(fc.x, fc.z), fc);
                     //
                 }
             }
             
             
             
             cfg_friends = cfg_root.getConfigurationSection("friends");
             
             f.friends = new HashMap<String, Integer>();
             
             if (cfg_friends != null) {
                 for (Entry<String, Object> en : cfg_friends.getValues(false).entrySet()) {
                     f.friends.put(en.getKey(), (Integer)en.getValue());
                 }
             }
             
             
             cfg_slist = cfg_root.getStringList("allies");
             
             f.allies = new HashSet<String>();
             
             if (cfg_slist != null) {
                 for (String name : cfg_slist) {
                     f.allies.add(name);
                 }
             }
             
             cfg_slist = cfg_root.getStringList("enemies");
             
             f.enemies = new HashSet<String>();
             
             if (cfg_slist != null) {
                 for (String name : cfg_slist) {
                     f.enemies.add(name);
                 }
             }
             
             cfg_invites = cfg_root.getConfigurationSection("invites");
             f.invites = new HashSet<String>();
             
             cfg_players = cfg_root.getConfigurationSection("players");
             f.players = new HashMap<String, FactionPlayer>();
             
             if (cfg_players != null) {
                 for (Entry<String, Object> en : cfg_players.getValues(false).entrySet()) {
                     FactionPlayer               fp;
 
                     fp = new FactionPlayer();
                     fp.faction = f;
                     fp.name = en.getKey();
                     fp.rank = (Integer)en.getValue();
                     f.players.put(en.getKey(), fp);
                 }
             }
             
             cfg_walocs = cfg_root.getConfigurationSection("walocs");
             f.walocs = new HashSet<WorldAnchorLocation>();
             
             if (cfg_walocs != null) {
                 for (String key : cfg_walocs.getKeys(false)) {
                     ConfigurationSection        _cs;
                     WorldAnchorLocation         waloc;
 
                     _cs = cfg_walocs.getConfigurationSection(key);
                     waloc = new WorldAnchorLocation();
                     waloc.x = _cs.getInt("x");
                     waloc.y = _cs.getInt("y");
                     waloc.z = _cs.getInt("z");
                     waloc.w = _cs.getString("world");
                     waloc.byWho = _cs.getString("byWho");
                     waloc.timePlaced = _cs.getLong("timePlaced");
                     f.walocs.add(waloc);
                 }
             }
             
             cfg_zapin = cfg_root.getConfigurationSection("zappersIncoming");
             if (cfg_zapin != null) {
                 for (String key : cfg_zapin.getKeys(false)) {
                     ConfigurationSection        _cs;
 
                     _cs = cfg_zapin.getConfigurationSection(key);
                     zaps.add(_cs);
                 }
             }
             
             cfg_zapout = cfg_root.getConfigurationSection("zappersOutgoing");
             if (cfg_zapout != null) {
                 for (String key : cfg_zapout.getKeys(false)) {
                     ConfigurationSection        _cs;
 
                     _cs = cfg_zapout.getConfigurationSection(key);
                     // these have to be done last once we have all the faction
                     // objects loaded into memory so we can lookup the faction
                     // specified by the zap entry structure
                     zaps.add(_cs);
                 }            
             }
             
             // access all the primitive value fields
             f.desc = cfg_root.getString("desc");
             f.flags = cfg_root.getInt("flags");
             f.hw = cfg_root.getString("hw");
             f.hx = cfg_root.getDouble("hx");
             f.hy = cfg_root.getDouble("hy");
             f.hz = cfg_root.getDouble("hz");
             f.lpud = cfg_root.getLong("lpud");
             f.mrc = cfg_root.getInt("mrc");
             f.mri = cfg_root.getInt("mri");
             f.mrsh = cfg_root.getInt("mrsh");
             f.mrtp = cfg_root.getInt("mrtp");
             f.mrz = cfg_root.getInt("mrz");
             f.name = e.getKey();
             f.peaceful = false;
             f.power = cfg_root.getDouble("power");
             f.worthEMC = cfg_root.getLong("worthEMC");
             
             allfactions.put(f.name.toLowerCase(), f);
         }
         
         // iterate through the zaps
         for (ConfigurationSection c_zap : zaps) {
             ZapEntry            ze;
             Faction             f_from, f_to;
             
             ze = new ZapEntry();
             ze.amount = c_zap.getDouble("amount");
             f_from = allfactions.get(c_zap.getString("from"));
             f_to = allfactions.get(c_zap.getString("to"));
             ze.from = f_from;
             ze.to = f_to;
             ze.isFake = c_zap.getBoolean("isFake");
             ze.perTick = c_zap.getDouble("perTick");
             ze.timeStart = c_zap.getLong("timeStart");
             ze.timeTick = c_zap.getLong("timeTick");
             
             f_from.zappersOutgoing.add(ze);
             f_to.zappersIncoming.add(ze);
         }
         
         //try {
         //    _DumpHumanReadableData(allfactions, new File("test.yml"));
         //} catch (Exception ex) {
         //    ex.printStackTrace();
         //}
         
         return allfactions;
     }
     
     /** This is mainly used to make faction name safe to use in other stuff
      *  such as the YAML data format.
      *
      */
     public String sanitizeString(String in) {
         char[]                          cb;
         String                          ac;
         int                             y;
         int                             z;
         
         ac = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_";
         z = 0;
         
         cb = new char[in.length()];
         
         for (int x = 0; x < in.length(); ++x) {
             for (y = 0; y < ac.length(); ++y) {
                 if (in.charAt(x) == ac.charAt(y)) {
                     break;
                 }
             }
             if (y < ac.length()) {
                 cb[z++] = in.charAt(x);
             }
         }
         
         return new String(cb, 0, z);
     }
     
     /** This is a comfort function used to automatically specify the correct
      *  variables during the call.
      * 
      */
     public void DumpHumanReadableData() throws FileNotFoundException, IOException {
         _DumpHumanReadableData(factions, fdata);
     }
     
     /** This is a comfort function used to specify to actual output file.
      * 
      */
     public void DumpHumanReadableData(File file) throws FileNotFoundException, IOException {
         _DumpHumanReadableData(factions, file);
     }
     
     /** This is used by _DumpHumanReadableData to dump the data about a chunk
      *  into a file using the YAML format. This is a utility function.
      *  
      */
     private void hrfWriteChunk(RandomAccessFile raf, FactionChunk chk) throws IOException {
         // mru (done)
         // mrb (done)
         // tid (loop)
         // tidu (loop)
         raf.writeBytes(String.format("  %s*%d*%d:\n", chk.worldName, chk.x, chk.z));
         raf.writeBytes(String.format("   mru: %d\n", chk.mru));
         raf.writeBytes(String.format("   mrb: %d\n", chk.mrb));
         raf.writeBytes("   tid:\n");
         if (chk.tid != null) {
             for (Entry<Integer, Integer> e : chk.tid.entrySet()) {
                 raf.writeBytes(String.format("    %d: %d\n", e.getKey(), e.getValue()));
             }
         }
         raf.writeBytes("   tidu:\n");
         if (chk.tidu != null) {
             for (Entry<Integer, Integer> e : chk.tidu.entrySet()) {
                 raf.writeBytes(String.format("    %d: %d\n", e.getKey(), e.getValue()));
             }
         }
     }
     
     /** This will save all persistent data to disk into a file specified in the
      *  YAML format.
      * 
      * @param allfactions               This is the faction's main data structure.
      * @param file                      The file to save the data in the YAML format into.
      * @throws FileNotFoundException    
      * @throws IOException 
      */
     public void _DumpHumanReadableData(Map<String, Faction> allfactions, File file) throws FileNotFoundException, IOException {
         RandomAccessFile                raf;
         Faction                         f;
         String                          fname;
         int                             j;
         
         
         raf = new RandomAccessFile(file, "rw");
         raf.setLength(0);
             
         for (Entry<String, Faction> ef : allfactions.entrySet()) {
             // TestFaction:
             f = ef.getValue();
             fname = sanitizeString(f.name);
             if (fname.length() == 0) {
                 continue;
             }
             //getLogger().info(String.format("dumping faction %s", fname));
             raf.writeBytes(String.format("%s:\n", fname));
             // members/players
             raf.writeBytes(" players:\n");
             for (Entry<String, FactionPlayer> p : f.players.entrySet()) {
                 raf.writeBytes(String.format("  %s: %d\n", p.getKey(), p.getValue().rank));                
             }
             raf.writeBytes(" friends:\n");
             if (f.friends != null) {
                 for (Entry<String, Integer> fr : f.friends.entrySet()) {
                     raf.writeBytes(String.format("  %s: %d\n", fr.getKey(), fr.getValue()));
                 }
             }
             
             raf.writeBytes(" chunks:\n");
             // if it is a String then it is the newer version
             if (f.chunks != null && f.chunks.size() > 0) {
                 Map     map;
                 
                 map = f.chunks;
                 
                 if (map.keySet().iterator().next().getClass().getName().equals("java.lang.String")) {
                     // this shall be the new execution path for upgraded data thus
                     // after an upgrade this should be the only path ever used again
                     for (Map<Long, FactionChunk> fcg : f.chunks.values()) {
                         for (Entry<Long, FactionChunk> fc : fcg.entrySet()) {
                             hrfWriteChunk(raf, fc.getValue());
                         }
                     }
                 } else {
                     // this is the old format and I had to do some casting to get it there
                     // because the Java deserialization puts it back as the original Map
                     // type so here it is to provide a valid upgrade path for older
                     // versions
                     Map<Long, FactionChunk>     m;
 
                     m = (Map<Long, FactionChunk>)(Object)f.chunks;
 
                     for (Entry<Long, FactionChunk> fc : m.entrySet()) {
                         FactionChunk        chk;
 
                         chk = fc.getValue();
 
                         hrfWriteChunk(raf, chk);
                     }
                 }
             }
             // desc
             raf.writeBytes(String.format(" desc: %s\n", f.desc));
             // flags
             raf.writeBytes(String.format(" flags: %d\n", f.flags));
             // hw, hx, hy, hz
             raf.writeBytes(String.format(" hw: %s\n", f.hw));
             raf.writeBytes(String.format(Locale.US, " hx: %f\n", f.hx));
             raf.writeBytes(String.format(Locale.US, " hy: %f\n", f.hy));
             raf.writeBytes(String.format(Locale.US, " hz: %f\n", f.hz));
             // invitations
             raf.writeBytes(String.format(" invites:\n"));
             if (f.invites != null) {
                 for (String inv : f.invites) {
                     inv = sanitizeString(inv);
                     if (inv.length() == 0) {
                         continue;
                     }
                     raf.writeBytes(String.format("  - %s\n", inv));
                 }
             }
             // lpud
             raf.writeBytes(String.format(" lpud: %d\n", f.lpud));
             // mrc
             raf.writeBytes(String.format(" mrc: %d\n", f.mrc));
             // mri
             raf.writeBytes(String.format(" mri: %d\n", f.mri));
             // mrsh
             raf.writeBytes(String.format(" mrsh: %d\n", f.mrsh));
             // mrtp
             raf.writeBytes(String.format(" mrtp: %d\n", (int)f.mrtp));
             // mrz
             raf.writeBytes(String.format(" mrz: %d\n", (int)f.mrz));
             // name (already used for root key name)
             // power
             raf.writeBytes(String.format(Locale.US, " power: %f\n", f.power));
             
             raf.writeBytes(" walocs:\n");
             j = 0;
             for (WorldAnchorLocation wal : f.walocs) {
                 raf.writeBytes(String.format("  %d:\n", j++));
                 raf.writeBytes(String.format("   byWho: %s\n", wal.byWho));
                 raf.writeBytes(String.format("   timePlaced: %d\n", wal.timePlaced));
                 raf.writeBytes(String.format("   world: %s\n", wal.w));
                 raf.writeBytes(String.format("   x: %d\n", wal.x));
                 raf.writeBytes(String.format("   y: %d\n", wal.y));
                 raf.writeBytes(String.format("   z: %d\n", wal.z));
             }
             // worthEMC
             raf.writeBytes(String.format(" worthEMC: %d\n", f.worthEMC));
             
             raf.writeBytes(" allies:\n");
             for (String name : f.allies) {
                 raf.writeBytes(String.format("  - %s", name));
             }
             
             raf.writeBytes(" enemies:\n");
             for (String name : f.enemies) {
                 raf.writeBytes(String.format("  - %s", name));
             }            
             
             
             HashSet<ZapEntry>[]             zez;
             String                          f_from;
             String                          f_to;
             
             zez = new HashSet[2];
             
             zez[0] = f.zappersIncoming;
             zez[1] = f.zappersOutgoing;
             
             for (HashSet<ZapEntry> hsze : zez) {
                 if (hsze == zez[0]) {
                     raf.writeBytes(" zappersIncoming:\n");
                 } else {
                     raf.writeBytes(" zappersOutgoing:\n");
                 }
                 
                 j = 0;
                 for (ZapEntry ze : f.zappersIncoming) {
                     raf.writeBytes(String.format("  %d:\n", j++));
                     // amount double
                     raf.writeBytes(String.format(Locale.US, "   amount: %f\n", ze.amount));
                     // from
                     raf.writeBytes(String.format("   from: %s\n", ze.from.name));
                     // isFake boolean
                     raf.writeBytes(String.format("   isFake: %b\n", ze.isFake));
                     // perTick boolean
                     raf.writeBytes(String.format(Locale.US, "   perTick: %f\n", ze.perTick));
                     // timeStart long 
                     raf.writeBytes(String.format("   timeStart: %d\n", ze.timeStart));
                     // timeTick long
                     raf.writeBytes(String.format("   timeTick: %d\n", ze.timeTick));
                     // to Faction
                     raf.writeBytes(String.format("   to: %s\n", ze.to.name));
                 }
             }
             // <end of loop>
         }
         raf.close();
         return;
     }
     
     /**
      * This will return the amount of EMC an item/block is worth.
      * 
      * @param tid               The TypeID of the item/block.
      * @param did               The DataID of the item/block or ZERO.
      * @return                  The amount of EMC the item/block is worth.
      */
     public int getEMC(int tid, int did) {
         if (!emcMap.containsKey(LongHash.toLong(tid, did)))
             return 0;
         return emcMap.get(LongHash.toLong(tid, did));
     }
     
     /** 
      *  ===UGLY HACK WARNING===
      *  This will hook the Herochat plugin and stand between it so that it can
      *  replace the token {faction} in any of the format strings used. This allows
      *  you to go inside the Herochat config file and insert {faction} where you
      *  would like for the faction name to appear at thus providing a faction tag.
      * 
      *  I mainly use a single reflection hack to access a private field, then I
      *  use an super class to do the work between the original class and the calling
      *  Herochat plugin method.
      */
     public void setupForHeroChat() {
         class ProxyExecutor implements EventExecutor {
             public P            p;
             
             @Override
             public void execute(Listener ll, Event __event) throws EventException {
                 StandardChannel     stdc;
                 String              format;
                 Class               clazz;
                 Field               field;
                 ChannelChatEvent    event;
                 
                 class Proxy implements MessageFormatSupplier {
                   MessageFormatSupplier         mfs;
                   P                             p;
                   Player                        player;
 
                   public Proxy(MessageFormatSupplier _mfs, P _p, Player _player) {
                       mfs = _mfs;
                       p = _p;
                       player = _player;
                   }
                   
                   @Override
                   public String getStandardFormat() {
                       String            fmt;
                       FactionPlayer     fp;
                       
                       fmt = mfs.getStandardFormat();
                       
                       p.getLogger().info(fmt);
                       
                       fp = p.getFactionPlayer(player.getName());
                       
                       if (fp != null) {
                         fmt = fmt.replace("{faction}", fp.faction.name);
                       } else {
                         fmt = fmt.replace("{faction}", "");
                       }
                       
                       return fmt;
                   }
                   @Override
                   public String getConversationFormat() {
                       return mfs.getConversationFormat();
                   }
                   @Override
                   public String getAnnounceFormat() {
                       return mfs.getAnnounceFormat();
                   }
                   @Override
                   public String getEmoteFormat() {
                       return mfs.getEmoteFormat();
                   }
                 }
                 
                 event = (ChannelChatEvent)__event;
 
                 Proxy g;
 
                 stdc = (StandardChannel)event.getChannel();
 
                 clazz = stdc.getClass();
 
                 format = stdc.getFormat();
 
                 try {
                     field = clazz.getDeclaredField("formatSupplier");
                     field.setAccessible(true);
                     g = new Proxy((MessageFormatSupplier)field.get(stdc), p, event.getSender().getPlayer());
                     field.set(stdc, g);
                 } catch (NoSuchFieldException ex) {
                     ex.printStackTrace();
                 } catch (IllegalAccessException ex) {
                     ex.printStackTrace();
                 }
                 //stdc.setFormat(null);
             }
         }
         
         ProxyExecutor           pe;
         
         pe = new ProxyExecutor();
         pe.p = this;
         
         Bukkit.getPluginManager().registerEvent(ChannelChatEvent.class, this, EventPriority.LOW, pe, this);
     }
     
     /*
      * This provides support for EssentialsChat, or really anything else. It
      * basically does this using a hacky kind of method. But, the good news
      * is that it of course works!
      */
     private void setupForChatFormat() {
         class ____ecl implements Listener {
             /*
              * I wrote this and I used LOWEST by accident but.. oddly.. it
              * works. I am not going to mess with it, but if anyone reads
              * this you can at least not be so confused.. we can instead be
              * confused together!
              * 
              * Like I said I think it should be HIGHEST, but LOWEST seems to
              * work just fine??
              */
             @EventHandler(priority = EventPriority.LOWEST)
             public void onPlayerChat(PlayerChatEvent event) {
                 FactionPlayer           fp;
                 Player                  player;
                 
                 player = event.getPlayer();
                 
                 if (player == null) {
                     return;
                 }
                 
                 fp = getFactionPlayer(player.getName());
                 
                 if (fp == null) {
                     event.setFormat(event.getFormat().replace("{FACTION}", ""));
                     event.setFormat(event.getFormat().replace("{faction}", ""));
                     event.setFormat(event.getFormat().replace("[FACTION]", ""));
                     event.setFormat(event.getFormat().replace("[faction]", ""));
                 } else {
                     event.setFormat(event.getFormat().replace("{FACTION}", fp.faction.name));
                     event.setFormat(event.getFormat().replace("{faction}", fp.faction.name));
                     event.setFormat(event.getFormat().replace("[FACTION]", fp.faction.name));
                     event.setFormat(event.getFormat().replace("[faction]", fp.faction.name));
                 }
                 
                 return;
             }
         }
         
         getServer().getPluginManager().registerEvents(new ____ecl(), this);
         return;
     }
     
     @Override
     public void onEnable() {
         File                            file;
         File                            femcvals;
         RandomAccessFile                raf;
         Iterator<Entry<Long, Integer>>  i;
         Entry<Long, Integer>            e;
         FileConfiguration               cfg;
         File                            fcfg;
         List<String>                    we;
         Object                          essentialsChat;
         Thread                          collectorThread;
         
         seeChunkLast = new HashMap<String, Long>();
         scannerWait = new HashMap<String, Long>();
         autoClaim = new HashSet<String>();
         mapView = new HashSet<String>();
         
         /*
          * This will setup support for language translations provided there
          * exists a translation file for the specified language.
          */
         getLogger().info(String.format("LOCALE [%s]", Locale.getDefault().getLanguage()));
         if (!Language.loadFrom(String.format("locale.%s", Locale.getDefault().getLanguage()))) {
             getLogger().info(String.format("LOCALE [LOAD FAILED] - MAYBE NO LOCALE FOR YOUR DEFAULT LANGUAGE!"));
             getLogger().info(String.format("   MAYBE YOU CAN CREATE ONE? SEND REQUEST TO kmcg3413@gmail.com"));
         }
         
         /*
          * This will setup the update checker which is used
          * to check if a newer version can be downloaded whenever
          * anyone with the OP privledge or the bypass permission
          * logs into the server. Then it notifies them.
          */
         updateChecker = new UpdateChecker();
         updateChecker.setProjectName("kfactions");
         updateChecker.start();
         
         /*
          * This is used to provide a link between this plugin and a central
          * place. Using this we are able to get an idea of how many servers
          * are using the plugin, their version, and also exchange information
          * with them such as giving them the latest version and any important
          * messages.
          * 
          * At the moment all I am doing is tracker what servers are using the
          * plugin. This information is stored in:
          * 
          * http://fuelbomb.net16.net/kfactiontracker.log
          * 
          */        
         final String        serverIp;
         final int           serverPort;
         final String        serverBukkitVersion;
         final String        serverMotd;
         final String        serverName;
         final String        serverId;
         final String        serverNets;
         
         serverIp = getServer().getIp();
         serverBukkitVersion = getServer().getBukkitVersion();
         //serverMotd = getServer().getMotd();
         serverMotd = "disabled";
         serverName = getServer().getServerName();
         serverId = getServer().getServerId();
         serverPort = getServer().getPort();
         
         /*
          * I was trying to find a way to be able to get the interface the
          * server is listening on and if not get all interfaces, but really
          * that is going to be really messy and honestly it might send out
          * information about the server that a hacker could use.
          * 
          * So I will just settle with getServer().getIp() and see how that works
          * out.
          */
         /*
         Enumeration<NetworkInterface>       nets;
         StringBuffer                        _serverNets;
         
         _serverNets = new StringBuffer();
         try {
             nets = NetworkInterface.getNetworkInterfaces();
             for (NetworkInterface net : Collections.list(nets)) {
                 StringBuffer    addrs;
                 
                 addrs = new StringBuffer();
                 for (InetAddress ia : Collections.list(net.getInetAddresses())) {
                     addrs.append(String.format("%s=%s", ia.getHostAddress(), ia.getHostName()));
                 }
                 
                 _serverNets.append(String.format(
                         "%s-%s-%s",
                         net.getDisplayName(),
                         net.getName(),
                         addrs.toString()
                 ));
             }
         } catch (SocketException ex) {
             _serverNets.append("error");
         }
         serverNets = _serverNets.toString();
         */
         
         collectorThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 URLConnection       conn;
                 InputStream         istrm;
                 String              url;
                 
                 url = String.format(
                         "http://fuelbomb.net16.net/kfactiontracker.php?ip=%s&port=%s&bver=%s&motd=%s&name=%s&id=%s&nets=disabled",
                         URLEncoder.encode(serverIp), 
                         URLEncoder.encode(String.format("%d", serverPort)), 
                         URLEncoder.encode(serverBukkitVersion), 
                         URLEncoder.encode(serverMotd), 
                         URLEncoder.encode(serverName), 
                         URLEncoder.encode(serverId)
                 );
                 
                 getLogger().info(Language.get("ONENABLE_WEBEXCHANDSHAKE"));
                 getLogger().info(url);
                 try {
                     conn = new URL(url).openConnection();
                     getLogger().info(Language.get("ONENABLE_WEBEXCREADING"));
                     istrm = conn.getInputStream();
                     istrm.read();
                     getLogger().info(Language.get("ONENABLE_WEBEXCSUCCESS"));
                 } catch (IOException ex) {
                     getLogger().info(Language.get("ONENABLE_WEBEXCFAILED"));
                 }
             }
         });
         collectorThread.setDaemon(true);
         collectorThread.start();
         /* ---------------------------------------------------------------*/
         
         /*
          * This is done to support EssentialsChat, which does not have a very
          * good API. It is also done to support hopefully any other plugin that
          * properly uses the format attribute in chat events.
          */
         setupForChatFormat();
         
         /*
          * HeroChat does not provide a public API this is one big hack!
          * 
          * This was done for a guy who wanted to be able to include something such
          * as {faction} in the Herochat plugin and have it replaced with the player's
          * faction. To facililate this I had to do a lot of dirty trickery. The good
          * news is that all that ugly stuff is contained in a single method called
          * 'setupForHeroChat'. The code below simply detects if Herochat has been
          * loaded.
          * 
          * If I am not mistaken the setupForChatFormat should properly handle Herochat
          * but they made their chat handler run under the MONITOR event priority so there
          * is not way to get an event call after it changes the format/message parameters.
          * 
          * So this is basically the only way unless we do hackery on Bukkit to create
          * a new priority.
          */
         try {
             Class.forName("com.dthielke.herochat.MessageFormatSupplier");
             setupForHeroChat();
             getLogger().info(Language.get("ONENABLE_HEROCHATFOUND"));
         } catch (ClassNotFoundException ex) {
             getLogger().info(Language.get("ONENABLE_NOHEROCHAT"));
         } catch (Exception ex) {
             getLogger().info(Language.get("ONENABLE_HEROCHATERROR"));
             ex.printStackTrace();
         }
         
         fcfg = new File("kfactions.config.yml");
         
         /*
          * This section loads the configuration from the disk into memory, then
          * it immediantly saves it back to disk to populate any missing options
          * with their default values. This erases any comments in the file but
          * this was the best option to get the desired results when I wrote this.
          */
         
         cfg = new YamlConfiguration();
         
         try {
             if (fcfg.exists()) {
                 cfg.load(fcfg);
             }
             if (cfg.contains("opRank")) 
                 opRank = cfg.getInt("opRank");
             else
                 opRank = -1;
             if (cfg.contains("usagePermission"))
                 usagePermission = cfg.getString("usagePermission");
             else
                 usagePermission = "kfactions.usage";
             
             if (cfg.contains("requirePermission"))
                 requirePermission = cfg.getBoolean("requirePermission");
             else
                 requirePermission = false;
             
             if (cfg.contains("bypassPermission"))
                 bypassPermission = cfg.getString("bypassPermission");
             else
                 bypassPermission = "kfactions.bypass";
             
             if (cfg.contains("repawnAtFactionHome"))
                 repawnAtFactionHome = cfg.getBoolean("repawnAtFactionHome");
             else
                 repawnAtFactionHome = false;
             
             if (cfg.contains("percentageofPowerOnTeleport"))
                 percentageofPowerOnTeleport = cfg.getDouble("percentageofPowerOnTeleport");
             else
                 percentageofPowerOnTeleport = 0.1;
     
             if (cfg.contains("powerUsedEachClaim"))
                 powerUsedEachClaim = cfg.getDouble("powerUsedEachClaim");
             else
                 powerUsedEachClaim = 0;
     
             if (cfg.contains("numberOfFreeClaims"))
                 numberOfFreeClaims = cfg.getInt("numberOfFreeClaims");
             else
                 numberOfFreeClaims = 2;
             
             if (cfg.contains("randomModifierOnProtectBlock"))
                 randomModifierOnProtectBlock = cfg.getBoolean("randomModifierOnProtectBlock");
             else
                 randomModifierOnProtectBlock = true;
                 
             if (cfg.contains("enabledScanner"))
                 enabledScanner = cfg.getBoolean("enabledScanner");
             else
                 enabledScanner = true;
             
             noGriefPerWorld = new HashSet<String>();
             if (cfg.contains("noGriefPerWorld")) {
                  for (String worldName : cfg.getStringList("noGriefPerWorld")) {
                      noGriefPerWorld.add(worldName);
                  }
             }
             
             if (cfg.contains("powerUsedToProtectBlock"))
                 powerUsedToProtectBlock = cfg.getDouble("powerUsedToProtectBlock");
             else
                 powerUsedToProtectBlock = 7.485380116959064;
             
             if (cfg.contains("scannerChance")) 
                 scannerChance = cfg.getDouble("scannerChance");
             else
                 scannerChance = 0.01;
     
             if (cfg.contains("scannerWaitTime"))
                 scannerWaitTime = cfg.getLong("scannerWaitTime");
             else
                 scannerWaitTime = 60 * 60;
             
             if (cfg.contains("landPowerCostPerHour"))
                 landPowerCostPerHour = cfg.getInt("landPowerCostPerHour");
             else
                 landPowerCostPerHour = 85.33;
             if (cfg.contains("worldsEnabled"))
                 we = cfg.getStringList("worldsEnabled");
             else {
                 we = new ArrayList();
                 we.add("world");
                 we.add("world_nether");
                 we.add("world_the_end");
             }
             
             if (cfg.contains("friendlyFire"))
                 friendlyFire = cfg.getBoolean("friendlyFire");
             else
                 friendlyFire = false;
             
             worldsEnabled = new HashSet<String>();
             for (String wes : we) {
                 worldsEnabled.add(wes);
             }
             
             ArrayList<String>       tmp;
             
             tmp = new ArrayList<String>();
             for (String worldName : noGriefPerWorld) {
                 tmp.add(worldName);
             }
             
             cfg.set("opRank", opRank);
             cfg.set("usagePermission", usagePermission);
             cfg.set("requirePermission", requirePermission);
             cfg.set("bypassPermission", bypassPermission);
             cfg.set("repawnAtFactionHome", repawnAtFactionHome);
             cfg.set("percentageofPowerOnTeleport", percentageofPowerOnTeleport);
             cfg.set("powerUsedEachClaim", powerUsedEachClaim);
             cfg.set("numberOfFreeClaims", numberOfFreeClaims);
             cfg.set("noGriefPerWorld", tmp);
             cfg.set("friendlyFire", friendlyFire);
             cfg.set("scannerChance", scannerChance);
             cfg.set("landPowerCostPerHour", landPowerCostPerHour);
             cfg.set("worldsEnabled", we);
             cfg.set("enabledScanner", enabledScanner);
             cfg.set("scannerWaitTime", scannerWaitTime);
             cfg.set("powerUsedToProtectBlock", powerUsedToProtectBlock);
             cfg.set("randomModifierOnProtectBlock", randomModifierOnProtectBlock);
             
             cfg.save(fcfg);
         } catch (InvalidConfigurationException ex) {
             ex.printStackTrace();
             return;
         } catch (FileNotFoundException ex) {
             ex.printStackTrace();
             return;
         } catch (IOException ex) {
             ex.printStackTrace();
             return;
         }
         
         
         // ensure that emcvals.txt exists
         femcvals = new File("kfactions.emcvals.txt");
         if (!femcvals.exists()) {
             getLogger().info(Language.get("ONENABLE_WRITTINGEMCVALS"));
             try {
                 raf = new RandomAccessFile(femcvals, "rw");
                 i = EMCMap.emcMap.entrySet().iterator();
                 while (i.hasNext()) {
                     e = i.next();
                     raf.writeBytes(String.format("%d:%d=%d\n", LongHash.msw(e.getKey()), LongHash.lsw(e.getKey()), e.getValue()));
                 }
                 raf.close();
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
         }
         
         // load from emcvals.txt
         emcMap = new HashMap<Long, Integer>();
         getLogger().info(Language.get("ONENABLE_READINGEMCVALS"));
         try {
             String      line;
             int         epos;
             int         cpos;
             int         tid;
             int         did;
             int         emc;
             
             raf = new RandomAccessFile(femcvals, "rw");
             while ((line = raf.readLine()) != null) {
                 epos = line.indexOf("=");
                 if (epos > -1) {
                     cpos = line.indexOf(":");
                     tid = Integer.parseInt(line.substring(0, cpos));
                     did = Integer.parseInt(line.substring(cpos + 1, epos));
                     emc = Integer.parseInt(line.substring(epos + 1));
                     emcMap.put(LongHash.toLong(tid, did), emc);
                 }
             }
             raf.close();
         } catch (FileNotFoundException ex) {
             ex.printStackTrace();
         } catch (IOException ex) {
             ex.printStackTrace();
         }
         
         getLogger().info(Language.get("ONENABLE_READINGGSPAWN"));
         file = new File("plugin.gspawn.factions");
         gspawn = null;
         if (file.exists()) {
             RandomAccessFile     fis;
             double               x, y, z;
             String               wname;
             
             try {
                 fis = new RandomAccessFile(file, "rw");
                 wname = fis.readUTF();
                 x = fis.readDouble();
                 y = fis.readDouble();
                 z = fis.readDouble();
                 fis.close();
             } catch (FileNotFoundException ex) {
                 wname = "world";
                 x = 0.0d;
                 y = 0.0d;
                 z = 0.0d;
             } catch (IOException ex) {
                 wname = "world";
                 x = 0.0d;
                 y = 0.0d;
                 z = 0.0d;
             }
             
             gspawn = new Location(getServer().getWorld(wname), x, y, z);
             getServer().getWorld(wname).setSpawnLocation((int)x, (int)y, (int)z);
         }
         // IF DATA ON DISK LOAD FROM THAT OTHERWISE CREATE
         // A NEW DATA STRUCTURE FOR STORAGE 
         getLogger().info(Language.get("ONENABLE_LOADINGDATA"));
         saveToDisk = true;
         file = new File("plugin.data.factions");
         
         // load from the original file but immediantly create a new
         // YAML data format file which will then be loaded
         if (file.exists() && !fdata.exists()) {
             try {
                 getLogger().info(Language.get("ONENABLE_UPGRADESTART"));
                 factions = (HashMap<String, Faction>)SLAPI.load("plugin.data.factions");
                 DumpHumanReadableData();
                 getLogger().info(Language.get("ONENABLE_UPGRADECOMPLETE"));
             } catch (Exception ex) {
                 ex.printStackTrace();
                 smsg(Language.get("ONENABLE_LOADERROR"));
             }            
         }
         
         // the old data format should have been upgraded and created a new
         // YAML file for us to load from
         if (fdata.exists()) {
             try {
                 factions = LoadHumanReadableData();
             } catch (Exception ex) {
                 factions = new HashMap<String, Faction>();
                 saveToDisk = false;
                 ex.printStackTrace();
                 smsg(Language.get("ONENABLE_ERRORINYAML"));
             }
         }
         
         // if both data sources do not exist
         if (!fdata.exists() && !file.exists()) {
             factions = new HashMap<String, Faction>();
             smsg(Language.get("ONENABLE_NODATAMAKENEW"));
         }
         
         // EVERYTHING WENT OKAY WE PREP THE DISK COMMIT THREAD WHICH WILL RUN LATER ON
         //getServer().getScheduler().scheduleAsyncDelayedTask(this, new DataDumper(this), 20 * 60 * 10);
         this.getServer().getPluginManager().registerEvents(new BlockHook(this), this);
         this.getServer().getPluginManager().registerEvents(new EntityHook(this), this);
         this.getServer().getPluginManager().registerEvents(new PlayerHook(this), this);
         this.getServer().getPluginManager().registerEvents(this, this);
         
         //this.getServer().getPluginManager().registerEvents(this, this);
         
         // let faction objects initialize anything <new> .. LOL like new fields
         Iterator<Entry<String, Faction>>            fe;
         Faction                                     f;
         
         getLogger().info(Language.get("ONENABLE_ENSUREDATA"));
         fe = factions.entrySet().iterator();
         while (fe.hasNext()) {
             f = fe.next().getValue();
             //getServer().getLogger().info(String.format("§7[f] initFromStorage(%s)", f.name));
             f.initFromStorage();
             // remove world anchors (temp)
             //getServer().getLogger().info("removing world anchors");
             //for (WorldAnchorLocation wal : f.walocs) {
             //    getServer().getWorld(wal.w).getBlockAt(wal.x, wal.y, wal.z).setTypeId(0);
             //    getServer().getWorld(wal.w).getBlockAt(wal.x, wal.y, wal.z).setData((byte)0);
             //}
         }
         
         final P       ___p;
         
         getLogger().info(Language.get("ONENABLE_SYNCTASKCREATED"));
         ___p = this;
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             public P            p;
             public boolean      isAsyncDone = true;
             
             @Override
             public void run() {
                 // copy factions array then schedule async task to work them
                 final Entry<String, Faction>[]      flist;
                 
                 if (!isAsyncDone)
                     return;
                 
                 isAsyncDone = false;
                 
                 p = ___p;
                 
                 synchronized (p.factions) {
                     flist = new Entry[factions.size()];
                     factions.entrySet().toArray(flist);
                 }
                 
                 getServer().getScheduler().scheduleAsyncDelayedTask(p, new Runnable() {
                     @Override
                     public void run() {
                         for (Entry<String, Faction> e : flist) {
                             calcZappers(e.getValue());
                         }
                         
                         isAsyncDone = true;
                     }
                 });
             }
         }
         , 20 * 10 * 60, 20 * 10 * 60); // every 10 minutes seems decent
         // make job to go through and calculate zappers for factions
     }
     
     /**
      * This will display the faction map to the player of their surrounding area. The
      * useArgs should be set to true only if the location for the player will return
      * invalid coordinates which will happen if you call this from a player move event
      * because the move has not actually happened yet so in that case the useArgs is
      * set to true and the correct px and py are provided (py is really pz). Any other
      * time useArgs is set to false and px and py can be set to anything.
      * 
      * @param player                the player to send the map for
      * @param useArgs               if true use work around from being called from move event handler
      * @param px                    move event handler work around
      * @param py                    move event handler work around
      */
     public void showPlayerMap(Player player, boolean useArgs, int px, int py) {
         final int colcnt =           41;
         final int rowcnt =           8;
         
         int                          bx, ex, x;
         int                          by, ey, y;
         FactionChunk                 fc;
         Map<String, Character>       sym;
         char                         bsym;
         char                         _sym;
         StringBuffer                 sb;
         char                         c;
         
         bsym = 'A';
         
         sym = new HashMap<String, Character>();
         
         if (!useArgs) {
             px = player.getLocation().getBlockX() >> 4;
             py = player.getLocation().getBlockZ() >> 4;
         }
         
         bx = px - (colcnt >> 1);
         by = py - (rowcnt >> 1);
         ex = bx + colcnt;
         ey = by + rowcnt;
         
         for (y = by; y < ey; ++y) {
             sb = new StringBuffer();
             sb.append("§7");
             for (x = bx; x < ex; ++x) {
                 fc = getFactionChunk(player.getWorld(), x, y);
                 if (fc == null) {
                     if (px == x && py == y) {
                         sb.append("§c+§7");
                     } else {
                         sb.append("+");
                     }
                 } else {
                     if (sym.containsKey(fc.faction.name)) {
                         _sym = sym.get(fc.faction.name);
                     } else {
                         ++bsym;
                         sym.put(fc.faction.name, bsym);
                         _sym = bsym;
                     }
                     if (px == x && py == y) {
                         sb.append(String.format("§c%c§7", _sym));
                     } else {
                         sb.append(String.format("§a%c§7", _sym));
                     }
                 }
             }
             player.sendMessage(String.format("§7%s", sb.toString()));
         }
         
         sb = new StringBuffer();
         
         for (Entry<String, Character> e : sym.entrySet()) {
             sb.append(String.format("§7%s:%c ", e.getKey(), e.getValue()));
         }
         
         player.sendMessage(sb.toString());
     }
     
     public void teleportPlayer(Player p, Location l) {
         Chunk           chunk;
         Location        nl;
         int             x, z, y;
         Block           block;
         
         chunk = l.getWorld().getChunkAt(l);
         
         x = l.getBlockX() & 0xf;
         z = l.getBlockZ() & 0xf;
         y = l.getBlockY();
         
         for (y = l.getBlockY(); y > -1; --y) {
             block = chunk.getBlock(x, y, z);
             if (!block.isEmpty()) {
                 nl = new Location(l.getWorld(), l.getBlockX(), y, l.getBlockZ());
                 p.sendBlockChange(nl, block.getTypeId(), block.getData());
                 break;
             }
         }
         
         p.teleport(l);
     }
     
     /**
      * This runs on interval and calculates any zaps on the faction's power.
      * 
      * @param f                 The faction to calculate zaps on.
      */
     public void calcZappers(Faction f) {
         long        ct, tdelta;
         double      toTake;
         
         ct = System.currentTimeMillis();
         // stops modification of power field and of zappers 
         synchronized (f) {
             Iterator<ZapEntry>          i;
             ZapEntry                    z;
             
             i = f.zappersIncoming.iterator();
             
             while (i.hasNext()) {
                 z = i.next();
                 
                 tdelta = ct - z.timeTick;
                 z.timeTick = ct;
                 toTake = z.perTick * (double)tdelta;
                 z.amount -= toTake;
                 if (!z.isFake)
                     f.power -= toTake;
                 // do not make them go negative that is rather
                 // too harsh and could essentially lock out a
                 // faction from ever playing again cause they
                 // would be unable to ever claim any land with
                 // a large negative power
                 if (f.power < 0)
                     f.power = 0;
                 // if zapper is zero-ed then remove it
                 if (z.amount < 1) {
                     i.remove();
                     synchronized (z.from) {
                         z.from.zappersOutgoing.remove(z);
                     }
                 }
             }
         }
     }
     
     @Override
     public void onDisable() {
         try {
             if (saveToDisk) {
                 DumpHumanReadableData();
                 getServer().getLogger().info(Language.get("ONDISABLE_SAVED"));
             } else {
                 getServer().getLogger().info(Language.get("ONDISABLE_SAVEDISABLED"));
             }
         } catch (Exception e) {
             getServer().getLogger().info(Language.get("ONDISABLE_EXSAVE"));
             e.printStackTrace();
             return;
         }
     }
     
     /**
      * This will return the faction object for the specified case-insensitive name.
      * @param factionName               A case-insensitive name.
      * @return                          The faction object.
      */
     public Faction getFactionByName(String factionName) {
         for (Faction f : factions.values()) {
             if (f.name.toLowerCase().equals(factionName.toLowerCase())) {
                 return f;
             }
         }
         return null;
     }
     
     /**
      * This will send a message to all online members of the faction.
      * @param f                     The faction object for the target faction.
      * @param m                     The raw chat message to send.
      */
     public void sendFactionMessage(Faction f, String m) {
         Iterator<Entry<String, FactionPlayer>>      i;
         Player                                      p;
         
         i = f.players.entrySet().iterator();
         while (i.hasNext()) {
             p = getServer().getPlayer(i.next().getValue().name);
             if (p != null) {
                 p.sendMessage(m);
             }
         }
     }
     
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         Player          p;
         Version         thisVersion;
         Version         latestVersion;
         FactionPlayer   fp;
         Faction         f;
         
         p = event.getPlayer();
         
         if (p == null) {
             return;
         }
         
         fp = getFactionPlayer(p.getName());
         if (fp != null) {        
             f = fp.faction;
             p.sendMessage(String.format(Language.get("PLAYERJOIN_PWRUNTILDEPLETED"),
                 f.name,
                 (int)(getFactionPower(f) / ((8192.0 / 24.0) * f.chunks.size()))
             ));
         }
         
         if (!p.isOp() && !p.hasPermission(bypassPermission)) {
             return;
         }
         
         if (updateChecker.canUpdate()) {
             thisVersion = updateChecker.getThisVersion();
             latestVersion = updateChecker.getLatestVersion();
             
             if (thisVersion.getMaj() == 0 &&
                 thisVersion.getMin() == 0 &&
                 thisVersion.getRev() == 0) {
                 p.sendMessage(Language.get("BADJARFILENAMEFORMAT"));
             } else {
                 p.sendMessage(String.format(
                         Language.get("NEWVERSION"),
                         latestVersion, thisVersion, latestVersion.getMin() - thisVersion.getMin(), latestVersion.getRev() - thisVersion.getRev()
                 ));
             }
         }
     }
     
     public void handlePlayerLogin(PlayerLoginEvent event) {
     }
     
     public void handlePlayerLogout(Player p) {
         FactionPlayer           fp;
         
         //fp = getFactionPlayer(p.getName());     
     }
     
     public void handlePlayerInteract(PlayerInteractEvent event) {
         int             x, z;
         World           w;
         FactionChunk    fchunk;
         FactionPlayer   fplayer;
         Player          player;
         int             rank;
         
         player = event.getPlayer();
         
         //x = player.getLocation().getBlockX() >> 4;
         //z = player.getLocation().getBlockZ() >> 4;
         w = player.getWorld();
         //getServer().getLogger().info(String.format("x:%d z:%d", x, z));
         
         // they are for sure not interacting with anything
         if (event.getClickedBlock() == null)
             return;
         
         x = event.getClickedBlock().getX() >> 4;
         z = event.getClickedBlock().getZ() >> 4;
         //getServer().getLogger().info(String.format("x:%d z:%d", x, z));
         
         fchunk = getFactionChunk(w, x, z);
         if (fchunk == null)
             return;
         
         fplayer = getFactionPlayer(player.getName());        
         
         rank = getPlayerRankOnLand(player, fchunk, fplayer);
 
         if (fchunk.tidu != null) {
             Block       block;
             
             if (!fchunk.tidudefreject)
                 event.setCancelled(false);
             else 
                 event.setCancelled(true);
             
             block = event.getClickedBlock();
 
             if (fchunk.tidu.containsKey(block.getTypeId())) {
                 if (rank < fchunk.tidu.get(block.getTypeId())) {
                     player.sendMessage(String.format(Language.get("NEEDHIGHERRANKTHAN"), rank, fchunk.tid.get(block.getTypeId()), TypeIdToNameMap.getNameFromTypeId(block.getTypeId())));
                     if (!fchunk.tidudefreject)
                         event.setCancelled(true);
                     else
                         event.setCancelled(false);
                     return;
                 }
                 return;
             }
             //
         }
 
         if (rank < fchunk.mru) {
             event.setCancelled(true);
             player.sendMessage(String.format(Language.get("RANKTOOLOW"), fchunk.faction.name));
             return;
         }
         
         return;
     }
     
     public boolean isWorldAnchor(int typeId) {
         if ((typeId == 214) || (typeId == 179) || (typeId == 4095))
             return true;
         return false;
     }
     
     public int getPlayerRankOnLand(Player player, FactionChunk fchunk, FactionPlayer fplayer) {
         int         rank;
         
         rank = -1;
         
         if (fchunk == null)
             return -1;
         
         if (fchunk.faction.friends != null) {
             if (fchunk.faction.friends.containsKey(player.getName())) {
                 rank = fchunk.faction.friends.get(player.getName());
             }
             if (fplayer != null) {
                 if (fchunk.faction.friends.containsKey(fplayer.faction.name)) {
                     rank = fchunk.faction.friends.get(fplayer.faction.name);
                 }
             }
         }
 
         if (rank == -1) {
             if (fplayer != null)
                 if (fplayer.faction == fchunk.faction)
                     rank = fplayer.rank;
         }
        
                 /*
          * If this player is an OP then assign them the OP rank specified from
          * the plugin configuration file. This only happens if they get more
          * rank from their OP rank.
          */
         if (player.isOp()) {
             if (opRank > rank) {
                 rank = opRank;
             }
         }
         
         return rank;
     }
     
     public void handleBlockPlace(BlockPlaceEvent event) {
         int             x, z;
         World           w;
         FactionChunk    fchunk;
         FactionPlayer   fplayer;
         Player          player;
         int             rank;
         Block           block;
         
         if (event.isCancelled())
             return;
         
         player = event.getPlayer();
         
         block = event.getBlockPlaced();
         
         x = event.getBlock().getX() >> 4;
         z = event.getBlock().getZ() >> 4;
         w = player.getWorld();
         
         fchunk = getFactionChunk(w, x, z);
         if (fchunk == null) {
             if (isWorldAnchor(block.getTypeId())) {
                 player.sendMessage(Language.get("WORLDANCHORONLYONCLAIM"));
                 event.setCancelled(true);
                 return;
             }
             return;
         }
         
         fplayer = getFactionPlayer(player.getName());
 
         rank = getPlayerRankOnLand(player, fchunk, fplayer);
         
         if (fchunk.tid != null) {
             
             if (!fchunk.tiddefreject)
                 event.setCancelled(false);
             else 
                 event.setCancelled(true);
             if (fchunk.tid.containsKey(block.getTypeId())) {
                 if (rank < fchunk.tid.get(block.getTypeId())) {
                     player.sendMessage(String.format(Language.get("NEEDHIGHERRANKTHAN"), rank, fchunk.tid.get(block.getTypeId()), TypeIdToNameMap.getNameFromTypeId(block.getTypeId())));
                     if (!fchunk.tiddefreject)
                         event.setCancelled(true);
                     else
                         event.setCancelled(false);
                     return;
                 }
                 return;
             }
         }        
         
         if (rank < fchunk.mrb) {
             player.sendMessage(String.format(Language.get("RANKTOOLOW"), fchunk.faction.name));
             event.setCancelled(true);
             return;
         }
 
         if (isWorldAnchor(block.getTypeId())) {
             if (fchunk.faction.walocs.size() > 1) {
                 player.sendMessage(String.format(Language.get("MAXWORLDANCHORS"), fchunk.faction.walocs.size()));
                 event.setCancelled(true);
                 return;
             }
             fchunk.faction.walocs.add(new WorldAnchorLocation(
                     block.getX(), block.getY(), block.getZ(),
                     block.getWorld().getName(),
                     player.getName()
             ));
         }
         
         return;       
     }
     
     @EventHandler
     public void handleBlockBreak(BlockBreakEvent event) {
         int             x, z;
         World           w;
         FactionChunk    fchunk;
         FactionPlayer   fplayer;
         Player          player;
         int             rank;
         Block           block;
         
         if (event.isCancelled())
             return;
         
         player = event.getPlayer();
         
         block = event.getBlock();
         x = event.getBlock().getX() >> 4;
         z = event.getBlock().getZ() >> 4;
         w = player.getWorld();
         
         fchunk = getFactionChunk(w, x, z);
         if (fchunk == null)
             return;
         
         fplayer = getFactionPlayer(player.getName());
         
         rank = getPlayerRankOnLand(player, fchunk, fplayer);
         
         if (fchunk.tid != null) {            
             if (!fchunk.tiddefreject)
                 event.setCancelled(false);
             else 
                 event.setCancelled(true);
             
             if (fchunk.tid.containsKey(block.getTypeId())) {
                 if (rank < fchunk.tid.get(block.getTypeId())) {
                     player.sendMessage(String.format(Language.get("NEEDHIGHERRANKTHAN"), rank, fchunk.tid.get(block.getTypeId()), TypeIdToNameMap.getNameFromTypeId(block.getTypeId())));
                     event.setCancelled(true);
                     return;
                 }
                 return;
             }
         }
                 
         // FINAL RANK CHECK DO OR DIE TIME
         if (rank < fchunk.mrb) {
             player.sendMessage(String.format(Language.get("RANKTOOLOW"), fchunk.faction.name));
             event.setCancelled(true);
             return;
         }
         
         // WORLD ANCHOR CONTROL
         if (isWorldAnchor(block.getTypeId())) {
             Iterator<WorldAnchorLocation>       i;
             WorldAnchorLocation                 wal;
             
             i = fchunk.faction.walocs.iterator();
             while (i.hasNext()) {
                 wal = i.next();
                 if ((wal.x == block.getX()) && 
                     (wal.y == block.getY()) && 
                     (wal.z == block.getZ()) && 
                     (wal.w.equals(block.getWorld().getName()))) {
                     player.sendMessage(Language.get("WORLDANCHORREMOVED"));
                     i.remove();
                 }
             }            
         }
         
         return;
     }
     
     @EventHandler
     public void handleEntityExplodeEvent(EntityExplodeEvent event) {
         List<Block>     blocks;
         Iterator<Block> iter;
         Block           b;
         int             x, z;
         World           w;
         FactionChunk    fchunk;
         double          pcost;
         
         w = event.getLocation().getWorld();
        
         blocks = event.blockList();
         iter = blocks.iterator();
         while (iter.hasNext()) {
             b = iter.next();
             
             x = b.getX() >> 4;
             z = b.getZ() >> 4;
             
             fchunk = getFactionChunk(w, x, z);
             if (fchunk != null) {
                 synchronized (fchunk.faction) {
                     if (noGriefPerWorld.contains(w.getName())) {
                         iter.remove();
                         continue;
                     }
                     if ((fchunk.faction.flags & NOBOOM) == NOBOOM) {
                         // remove explosion effecting 
                         // this block since it is protected
                         // by the NOBOOM flag
                         iter.remove();
                         continue;
                     }
                     // check if faction can pay for protection
                     if (randomModifierOnProtectBlock)
                         pcost = Math.random() * powerUsedToProtectBlock;
                     else
                         pcost = powerUsedToProtectBlock;
                     if (getFactionPower(fchunk.faction) >= pcost) {
                         fchunk.faction.power -= pcost;
                         iter.remove();
                         continue;
                     }
                 }
             }
         }
     }
     
     public boolean canPlayerBeDamaged(Player p) {
         Location                pl;
         FactionChunk            fc;
         int                     cx, cz;
         
         pl = p.getLocation();
         
         cx = pl.getChunk().getX();
         cz = pl.getChunk().getZ();
         
         fc = getFactionChunk(p.getWorld(), cx, cz);
         if (fc == null)
             return true;
         
         if ((fc.faction.flags & NOPVP) == NOPVP) {
             return false;
         }        
         
         return true;
     }
     
     /**
      * This methods catches event when one player damaged another. It is used
      * to enforce the friendlyfire configuration option. This method is called
      * directly by the Bukkit event system.
      * 
      * @param event                 bukkit supplied parameter
      */
     public void handleEntityDamageEntity(EntityDamageByEntityEvent event) {
         Entity          e, ed;
         Player          p, pd;
         Location        pl;
         int             cx, cz;
         FactionChunk    fc;
         FactionPlayer   fp, fpd;
         
         e = event.getEntity();
         
         if (!(e instanceof Player))
             return;
         
         p = (Player)e;
         
         // respect the worlds that we are enabled on
         if (!worldsEnabled.contains(p.getWorld().getName())) {
             return;
         }        
         
         ed = event.getDamager();
         
         // check for same team combat
         if (!friendlyFire && (ed instanceof Player)) {
             pd = (Player)ed;
             
             fp = getFactionPlayer(p.getName());
             fpd = getFactionPlayer(pd.getName());
             
             if (fp != null && fpd != null) {
                 // check if same faction
                 if (fp.faction == fpd.faction) {
                     event.setCancelled(true);
                     return;
                 }
                 // check if allied faction
                 // TODO
             }
         }
         
         pl = p.getLocation();
         
         cx = pl.getChunk().getX();
         cz = pl.getChunk().getZ();
         
         fc = getFactionChunk(p.getWorld(), cx, cz);
         if (fc == null)
             return;
         
         if ((fc.faction.flags & NOPVP) == NOPVP) {
             e = event.getDamager();
             if (e instanceof Player) {
                 p = (Player)e;                
                 p.sendMessage(Language.get("CANNOTATTACKINNOPVPZONE"));
             }
             event.setCancelled(true);
             return;
         }
         return;
     }
     
     public void handlePlayerRespawnEvent(PlayerRespawnEvent event) {
         if (repawnAtFactionHome) {
             FactionPlayer           fp;
             
             fp = getFactionPlayer(event.getPlayer().getName());
             if (fp != null) {
                 if (fp.faction.hw != null) {
                     event.setRespawnLocation(new Location(
                             getServer().getWorld(fp.faction.hw),
                             fp.faction.hx,
                             fp.faction.hy,
                             fp.faction.hz
                     ));
                     return;
                 }
             }
         }
         
         if (gspawn == null) {
             return;
         }
         event.setRespawnLocation(gspawn);
     }
     
     public void handlePlayerMove(PlayerMoveEvent event) {
         int             fx, fz;
         int             tx, tz;
         Faction         fc, tc;
         FactionChunk    _fc, _tc;
         World           world;
         final Player    player;
         
         fx = event.getFrom().getBlockX() >> 4;
         fz = event.getFrom().getBlockZ() >> 4;
         tx = event.getTo().getBlockX() >> 4;
         tz = event.getTo().getBlockZ() >> 4;
         
         // KEEPS US FROM EATING TONS OF CPU CYCLES WHEN ALL WE NEED TO DO
         // IS CHECK ON CHUNK TRANSITIONS
         if ((fx != tx) || (fz != tz)) {
             player = event.getPlayer();
             
             fc = null;
             tc = null;
             
             world = player.getWorld();
             _fc = getFactionChunk(world, fx, fz);
             if (_fc != null)
                 fc = _fc.faction;
             _tc = getFactionChunk(world, tx, tz);
             if (_tc != null)
                 tc = _tc.faction;
             
             if (mapView.contains(player.getName())) {
                 // EXECUTION WILL BE HERE WHEN THERE HAS BEEN A TRANSISTION
                 // FROM ONE CHUNK ONTO ANOTHER _AND_ THE PLAYER IS REGISTERED
                 // TO RECIEVE MAP VIEWS
                 showPlayerMap(player, true, tx, tz);
             }            
             
             // IF WALKING FROM SAME TO SAME SAY NOTHING
             if (fc == tc) {
                 return;
             }
             
             if (autoClaim.contains(player.getName()) && (tc == null)) {
                 player.sendMessage(Language.get("AUTOCLAIMTURNOFF"));
                 
                 getServer().getScheduler().runTask(this, new Runnable() {
                     @Override
                     public void run() {
                         String[]        args;
                         
                         args = new String[1];
                         args[0] = "claim";
                         onCommand(player, new __LocalCommandObject("f"), null, args);
                     }
                 });
             }
             
             // HANDLES walking from one faction chunk to another or walking from wilderness (fc can be null or not)
             if (tc != null) {
                 player.sendMessage(String.format(Language.get("ENTEREDFACTIONLAND"), tc.name));
                 return;
             }
             // HANDLES walking into wilderness
             if (fc != null) {
                 player.sendMessage(Language.get("ENTEREDWILDERNESS"));
                 return;
             }            
         }
         
     }
     
     public double getFactionPower(Faction f) {
         FactionPlayer                                   fp;
         Iterator<Entry<String, FactionPlayer>>          i;
         float                                           pow;
         long                                            ctime;
         double                                          delta;
         double                                          powcon;                
         int                                             landcnt;
         
         ctime =  System.currentTimeMillis();
         
         delta = (double)(ctime - f.lpud) / 1000.0d / 60.0d / 60.0d;
         
         landcnt = 0;
         for (Map m : f.chunks.values()) {
             landcnt += m.size();
         }
         
         powcon = delta * (double)landcnt * landPowerCostPerHour;
         
         synchronized (f) {
             f.power = f.power - powcon;
             if (f.power < 0.0)
                 f.power = 0.0;
             
             f.lpud = ctime;
 
             if ((f.flags & NODECAY) == NODECAY) {
                 return (double)(landcnt + 1) * 8192.0;
             }
         }
         return f.power;
     }
     
     public FactionChunk getFactionChunk(World world, int x, int z) {
         Iterator<Entry<String, Faction>>               i;
         Entry<String, Faction>                         e;
         Faction                                        f;
         FactionChunk                                   fc;
         
         i = factions.entrySet().iterator();
         while (i.hasNext()) {
             e = i.next();
             f = e.getValue();
             
             if (f.chunks.containsKey(world.getName())) {
                 fc = f.chunks.get(world.getName()).get(LongHash.toLong(x, z));
                 if (fc != null) {
                     return fc;
                 }
             }
         }
         return null;
     }
     
     public FactionPlayer getFactionPlayer(String playerName) {
         Iterator<Entry<String, Faction>>               i;
         Entry<String, Faction>                         e;
         Faction                                        f;
         
         playerName = playerName.toLowerCase();
         
         i = factions.entrySet().iterator();
         while (i.hasNext()) {
             e = i.next();
             f = e.getValue();
             for (Entry<String, FactionPlayer> e2 : f.players.entrySet()) {
                 if (e2.getKey().toLowerCase().equals(playerName)) {
                     return e2.getValue();
                 }
             }
         }
         return null;
     }
     
     // this is not used anymore and I guess I leave it just
     // for history; i used to use this but it can create
     // collisions and the X and Z are limited to 16-bit
     public Long getChunkLong(World world, int x, int z) {
         return new Long((world.getUID().getMostSignificantBits() << 32) | (z & 0xffff) | ((x & 0xffff) << 16));
         
     }
     
     public static void sendPlayerBlockChange(Player p, int x, int y, int z, int typeId, byte data) {
         Location            loc;
         
         loc = new Location(p.getWorld(), (double)x, (double)y, (double)z);
         
         p.sendBlockChange(loc, typeId, data);
     }
     
     public void displayHelp(Player player, String[] args) {
         String              helpSection;
         List<String>        lines;
         
         if ((args.length > 1) && args[0].equals("help")) {
             // help rank
             helpSection = String.format("HELP_%s", args[1].toUpperCase());
             lines = Language.getList(helpSection);
             
             if (lines != null) {
                 for (String l : lines) {
                     player.sendMessage(l);
                 }
                 return;
             }            
 
             for (String l : Language.getList("HELP_DEFAULT")) {
                 player.sendMessage(l);
             }
             player.sendMessage(String.format(Language.get("HELP_UNKNOWNCMD"), args[1]));
             return;
         }
         
         // no arguments / unknown command / help
        for (String l : Language.getList("HELP_DEFAULT")) {
             player.sendMessage(l);
         }
     }
     
     public void showPlayerChunk(Player player, boolean undo) {
         final int           cx, cz;
         int                 tid;
         byte                did;
         World               world;
         int                 ly, hy;
 
         cx = player.getLocation().getBlockX() >> 4;
         cz = player.getLocation().getBlockZ() >> 4;
 
         ly = player.getLocation().getBlockY();
         ly = ly - 5;
         hy = ly + 10;
 
         ly = ly < 0 ? 0 : ly;
         hy = hy > 255 ? 255 : hy;
 
         if (!undo) {
             // replace air with glass
             tid = 20;
             did = 0;
         } else {
             // replace glass back with air
             tid = 0;
             did = 0;
         }
 
         world = player.getWorld();
 
         for (int i = -1; i < 17; ++i) {
             for (int y = ly; y < hy; ++y) {
                 if (world.getBlockAt(cx * 16 + i, y, cz * 16 + 16).getTypeId() == 0)
                     sendPlayerBlockChange(player, cx * 16 + i, y, cz * 16 + 16, tid, (byte)did);
                 if (world.getBlockAt(cx * 16 + i, y, cz * 16 + -1).getTypeId() == 0)
                     sendPlayerBlockChange(player, cx * 16 + i, y, cz * 16 + -1, tid, (byte)did);
             }
         }
 
         for (int i = -1; i < 17; ++i) {
             for (int y = ly; y < hy; ++y) {
                 if (world.getBlockAt(cx * 16 + 16, y, cz * 16 + i).getTypeId() == 0)
                     sendPlayerBlockChange(player, cx * 16 + 16, y, cz * 16 + i, tid, (byte)did);
                 if (world.getBlockAt(cx * 16 + -1, y, cz * 16 + i).getTypeId() == 0)
                     sendPlayerBlockChange(player, cx * 16 + -1, y, cz * 16 + i, tid, (byte)did);
             }
         }        
     } 
     
     private void msg(Player p, String msg) {
         if (p == null) {
             getLogger().info(msg);
             return;
         }
         
         p.sendMessage(msg);
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         String              cmd;
         Player              player;
         
         player = null;
         
         // MUST BE TYPED INTO THE CONSOLE
         if (!(sender instanceof Player) || (((Player)sender).hasPermission(bypassPermission))) {
             if (args.length < 1) {
                 return true;
             }
             
             if (sender instanceof Player) {
                 player = (Player)sender;
             }
             
             cmd = args[0];
             
             if (cmd.equals("glitch1")) {
                 Faction             f;
                 
                 f = getFactionByName(args[1]);
                 
                 f.mrc = 700;
                 f.mri = 600;
                 return true;
             }
             
             if (command.getName().equals("resetwa")) {
                 int                                 x, y, z;
                 Iterator<WorldAnchorLocation>       i;
                 WorldAnchorLocation                 wal;
                 Faction                             f;
                 String                              fn;
                 String                              w;
                 
                 fn = args[0];
                 w = args[1];
                 x = Integer.parseInt(args[2]);
                 y = Integer.parseInt(args[3]);
                 z = Integer.parseInt(args[4]);
 
                 f = getFactionByName(fn);
                 if (f == null) {
                     msg(player, "[f] Could not find faction");
                     return true;
                 }
                 
                 i = f.walocs.iterator();
                 while (i.hasNext()) {
                     wal = i.next();
                     if ((wal.x == x) && 
                         (wal.y == y) && 
                         (wal.z == z) && 
                         (wal.w.equals(w))) {
                         msg(player, "§7[f] World anchor removed from faction control. You may now place one more.");
                         i.remove();
                         return true;
                     }
                 }
                 msg(player, "[f] Could not find world anchor.");
                 return true;
             }
             
             if (cmd.equals("setgspawn")) {
                 if (args.length < 1) {
                     msg(player, "§7[f] setgspawn needs one argument /f setgspawn <playername>");
                     return true;
                 }
                 Location            l;
                 File                file;
                 RandomAccessFile    raf;
                 
                 if (getServer().getPlayer(args[1]) == null) {
                     msg(player, "§7[f] player does not exist");
                     return true;
                 }
                 
                 l = getServer().getPlayer(args[1]).getLocation();
                 gspawn = l;
                 msg(player, "§7[f] set global respawn to location of player");
                 // ALSO WRITE OUT TO DISK FILE
                 file = new File("plugin.gspawn.factions");
                 try {
                     raf = new RandomAccessFile(file, "rw");
                     raf.writeUTF(l.getWorld().getName());
                     raf.writeDouble(l.getX());
                     raf.writeDouble(l.getY());
                     raf.writeDouble(l.getZ());
                     raf.close();
                 } catch (FileNotFoundException e) {
                     return true;
                 } catch (IOException e) {
                     msg(player, "§7[f] could not write gspawn to disk!");
                     return true;
                 }
                 return true;
             }
             if (cmd.equals("noboom")) {
                 // f noboom <faction-name>
                 Faction                 f;
                 
                 if (args.length < 1) {
                     msg(player, "§7[f] noboom needs one argument /f noboom <faction>");
                     return true;
                 }                              
                 
                 f = getFactionByName(args[1]);
                 if (f == null) {
                     msg(player, String.format("§7[f] faction %s can not be found", args[1]));
                     return true;
                 }
                 
                 if ((f.flags & NOBOOM) == NOBOOM) { 
                     f.flags = f.flags & ~NOBOOM;
                     msg(player, String.format("§7[f] NOBOOM toggled OFF on %s", args[1]));
                     return true;
                 }
                 
                 f.flags = f.flags | NOBOOM;
                 msg(player, String.format("§7[f] NOBOOM toggled ON on %s", args[1]));
                 return true;
             }
             
             if (cmd.equals("nopvp")) {
                 // f nopvp <faction-name>
                 Faction                 f;
 
                 if (args.length < 1) {
                     msg(player, "§7[f] nopvp needs one argument /f nopvp <faction>");
                     return true;
                 }                              
                 
                 f = getFactionByName(args[1]);
                 if (f == null) {
                     msg(player, String.format("§7[f] faction %s can not be found", args[1]));
                     return true;
                 }
                 
                 if ((f.flags & NOPVP) == NOPVP) { 
                     f.flags = f.flags & ~NOPVP;
                     msg(player, String.format("§7[f] NOPVP toggled OFF on %s", args[1]));
                     return true;
                 }
                 
                 f.flags = f.flags | NOPVP;
                 getServer().getLogger().info(String.format("§7[f] NOPVP toggled ON on %s", args[1]));
                 return true;
             }
 
             if (cmd.equals("nodecay")) {
                 // f nopvp <faction-name>
                 Faction                 f;
                 
                 if (args.length < 1) {
                     msg(player, "§7[f] nodecay needs one argument /f nodecay <faction>");
                     return true;
                 }                
                 
                 f = getFactionByName(args[1]);
                 if (f == null) {
                     msg(player, String.format("§7[f] faction %s can not be found", args[1]));
                     return true;
                 }
                 
                 if ((f.flags & NODECAY) == NODECAY) { 
                     f.flags = f.flags & ~NODECAY;
                     msg(player, String.format("§7[f] NODECAY toggled OFF on %s", args[1]));
                     return true;
                 }
                 
                 f.flags = f.flags | NODECAY;
                 msg(player, String.format("§7[f] NODECAY toggled ON on %s", args[1]));
                 return true;
             }
                         
             if (cmd.equals("yank")) {
                 // f yank <faction> <player>
                 Faction                 f;
                 
                 if (args.length < 3) {
                     msg(player, "§7[f] yank needs two arguments /f yank <faction> <player>");
                     return true;
                 }
                 
                 f = getFactionByName(args[1]);
                 
                 if (f == null) {
                     msg(player, String.format("§7[f] faction %s can not be found", args[1]));
                     return true;
                 }
                 
                 if (!f.players.containsKey(args[2])) {
                     msg(player, String.format("§7[f] faction %s has no player %s", args[1], args[2]));
                     return true;
                 }
                 
                 f.players.remove(args[2]);
                 msg(player, String.format("§7[f] player %s was yanked from faction %s", args[2], args[1]));
                 return true;
             }
             
             if (cmd.equals("stick")) {
                 // f stick <faction> <player>
                 FactionPlayer           fp;
                 Faction                 f;
 
                 if (args.length < 3) {
                     msg(player, "§7[f] stick needs two arguments /stick <faction> <player>");
                     return true;
                 }                
                 
                 fp = getFactionPlayer(args[2]);
                 if (fp != null) {
                     fp.faction.players.remove(fp.name);
                 }
                 
                 f = getFactionByName(args[1]);
                 if (f == null) {
                     msg(player, String.format("§7[f] faction %s can not be found", args[1]));
                     return true;
                 }
                 
                 fp = new FactionPlayer();
                 fp.rank = 1000;
                 fp.name = args[2];
                 fp.faction = f;
                 
                 f.players.put(args[2], fp);
                 
                 msg(player, String.format("§7[f] player %s was stick-ed in faction %s", args[2], args[1]));
                 return true;
             }
         }
         
         if (!(sender instanceof Player))
             return false;
         
         player = (Player)sender;
         
         if (requirePermission) {
             if (!player.hasPermission(usagePermission)) {
                 player.sendMessage(String.format("§7You need the %s permission.", usagePermission));
                 return true;
             }
         }
         
         // enforce world restrictions
         if (!worldsEnabled.contains(player.getWorld().getName())) {
             player.sendMessage("§7[f] This world has not been enabled for KFactions!");
             return true;
         }
         
         if (command.getName().equals("home")) {
             player.sendMessage("§7[f] Use /f home (requires a faction)");
             return true;
         }
         
         if (command.getName().equals("spawn")) {
             player.sendMessage("§7[f] Use /f spawn (requires a faction)");
             return true;
         }
         
         if (args.length < 1) {
             if (sender instanceof Player)
                 displayHelp((Player)sender, args);
             return false;
         }
         
         cmd = args[0];
         
         //this.getServer().getPluginManager().getPlugins()[0].
         //setmri, setmrc, setmrsp, setmrtp, 
         //sethome, tptp
         
         if (cmd.equals("c")) {
             StringBuffer        sb;
             FactionPlayer       fp;
 
             fp = getFactionPlayer(player.getName());
 
             if (fp == null) {
                 player.sendMessage("§7[f] §7You are not in a faction.");
                 return true;
             }
 
             sb = new StringBuffer();
 
             for (int x = 1; x < args.length; ++x) {
                 sb.append(args[x]);
                 sb.append(" ");
             }
 
             sendFactionMessage(fp.faction, String.format("§d[Faction]§r§e %s: %s", player.getDisplayName(), sb.toString()));
             return true;
         }
 
         if (cmd.equals("automap")) {
             if (mapView.contains(player.getName())) {
                 player.sendMessage("§7[f] The auto map is now §aOFF§r.");
                 mapView.remove(player.getName());
             }  else {
                 showPlayerMap(player, false, 0, 0);
                 mapView.add(player.getName());
                 player.sendMessage("§7[f] The auto map is now §aON§r.");
             }
             return true;
         }
         
         if (cmd.equals("map")) {
             showPlayerMap(player, false, 0, 0);
             return true;
         }
         
         if (cmd.equals("showanchors")) {
             Iterator<WorldAnchorLocation>       i;
             WorldAnchorLocation                 wal;
             FactionPlayer                       fp;
 
             fp = getFactionPlayer(player.getName());
             
             if (fp == null) {
                 player.sendMessage("§7[f] §7You are not in a faction.");
                 return true;
             }
             
             player.sendMessage("§7[f] Showing Placed World Anchors");
             i = fp.faction.walocs.iterator();
             while (i.hasNext()) {
                 wal = i.next();
                 player.sendMessage(String.format(
                     "  x:%d y:%d z:%d w:%s by:%s age:%d/days", 
                     wal.x, wal.y, wal.z, wal.w, wal.byWho,
                     (System.currentTimeMillis() - wal.timePlaced) / 1000 / 60 / 60 / 24
                 ));
             }
             return true;
         }
         
         if (cmd.equals("scan")) {
             double          chance;
             double          Xmax, Xmin, Zmax, Zmin;
             int             wndx, cndx, fndx;
             int             realX, realZ;
             FactionPlayer   fp;
             long            ct;
             long            sr;
 
             fp = getFactionPlayer(player.getName());
             
             if (fp == null) {
                 player.sendMessage("§7[f] §7You are not in a faction.");
                 return true;
             }
             
             if (!enabledScanner) {
                 player.sendMessage("§7[f] The scanner feature is not enabled on this server!");
                 return true;
             }
             
             ct = System.currentTimeMillis();
             
             if (scannerWait.containsKey(fp.faction.name)) {
                 sr = ct - scannerWait.get(fp.faction.name);
                 if (sr < (1000 * scannerWaitTime)) {
                     player.sendMessage(String.format(
                             "§7[f] You need to wait %d more seconds!",
                             ((1000 * scannerWaitTime) - sr) / 1000
                     ));
                     return true;
                 }
             }
             
             // scan all factions and all land claims to build minimum and maximum bounds
             fndx = (int)(Math.random() * factions.size());
             realX = 0;
             realZ = 0;
             Xmax = 0;
             Zmax = 0;
             Xmin = 0;
             Zmin = 0;
             for (Faction f : factions.values()) {
                 // pick random claim if any claim exists
                 wndx = (int)(Math.random() * f.chunks.size());
                 for (String wn : f.chunks.keySet()) {
                     cndx = (int)(Math.random() * f.chunks.get(wn).size());
                     for (FactionChunk fc : f.chunks.get(wn).values()) {
                         if (cndx == 0 && wndx == 0 && fndx == 0) {
                             realX = fc.x * 16;
                             realZ = fc.z * 16;
                         }
 
                         Xmax = Xmax == 0 || fc.x > Xmax ? fc.x : Xmax;
                         Zmax = Zmax == 0 || fc.z > Zmax ? fc.z : Zmax;
                         Xmin = Xmin == 0 || fc.x < Xmin ? fc.x : Xmin;
                         Zmin = Zmin == 0 || fc.z < Zmin ? fc.z : Zmin;                        
                         --cndx;
                     }
                     --wndx;
                 }
                 --fndx;
             }
             
             if (Math.random() > scannerChance) {
                 Xmax = Xmax * 16;
                 Xmin = Xmin * 16;
                 Zmax = Zmax * 16;
                 Zmin = Zmin * 16;
                 realX = (int)(Math.random() * (double)(Xmax - Xmin) + (double)Xmin);
                 realZ = (int)(Math.random() * (double)(Zmax - Zmin) + (double)Zmin);
                 realX = (realX >> 4) << 4;
                 realZ = (realZ >> 4) << 4;
             }
             
             scannerWait.put(fp.faction.name, ct);
             sendFactionMessage(fp.faction, String.format(
                     "§7 The world anomally scanner result is §a%d:%d§r.",
                     realX, realZ
             ));
             
             return true;
         }
         
         if (cmd.equals("curank") || cmd.equals("cbrank")) {
             FactionPlayer           fp;
             FactionChunk            fc;
             Location                loc;
             int                     bx, bz;
             int                     irank;
             
             loc = player.getLocation();
             fp = getFactionPlayer(player.getName());
             
             if (fp == null) {
                 player.sendMessage("§7[f] §7You are not in a faction.");
                 return true;
             }
             
             bx = loc.getBlockX() >> 4;
             bz = loc.getBlockZ() >> 4;
             
             fc = getFactionChunk(player.getWorld(), bx, bz);
             
             if (fc == null) {
                 player.sendMessage("§7[f] §7Land not claimed by anyone.");
                 return true;
             }
             
             if ((fc.faction != fp.faction) && (!player.isOp())) {
                 player.sendMessage("§7[f] §7Land not claimed by your faction.");
                 return true;
             }
 
             if ((fc.mrb >= fp.rank) && (!player.isOp())) {
                 player.sendMessage("§7[f] §7Land rank is equal or greater than yours.");
                 return true;
             }
             
             if (args.length < 2) {
                 player.sendMessage("§7[f] §7The syntax is /f curank <value> OR /f cbrank <value>");
                 return true;
             }
             
             irank = Integer.valueOf(args[1]);
             
             if ((irank >= fp.rank) && (!player.isOp())) {
                 player.sendMessage("§7[f] §7Your rank is too low.");
                 return true;
             }
             
             if (cmd.equals("curank")) {
                 fc.mru = irank;
             } else {
                 fc.mrb = irank;
             }
             player.sendMessage("§7[f] §7The rank was set. Use /f inspect to check.");
             return true;
         }
         
         if (cmd.equals("sethome")) {
             FactionPlayer           fp;
             FactionChunk            fc;
             
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage("§7[f] You must be in a faction!");
                 return true;                
             }
             
             fc = getFactionChunk(player.getWorld(), player.getLocation().getBlockX() >> 4, player.getLocation().getBlockZ() >> 4);
             
             if (fc == null) {
                 player.sendMessage("§7[f] Must be on faction land.");
                 return true;
             }
             
             if (fc.faction != fp.faction) {
                player.sendMessage("§7[f] Must be on your faction land.");
                 return true;
             }
             
             if (fp.rank < fp.faction.mrsh) {
                 player.sendMessage("§7[f] Your rank is too low.");
                 return true;
             }
             
             fp.faction.hx = player.getLocation().getX();
             fp.faction.hy = player.getLocation().getY();
             fp.faction.hz = player.getLocation().getZ();
             fp.faction.hw = player.getLocation().getWorld().getName();
             
             player.sendMessage("§7[f] Faction home set.");
             return true;
         }
         if (cmd.equals("tptp") || cmd.equals("home") || cmd.equals("spawn")) {
             FactionPlayer           fp;
             
             if (cmd.equals("spawn")) {
                 args = new String[3];
                 args[1] = player.getName();
                 args[2] = "spawn";
             }
             
             if (cmd.equals("home")) {
                 args = new String[3];
                 args[1] = player.getName();
                 args[2] = "home";                
             }
             
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage("§7[f] You must be in a faction!");
                 return true;                
             }
             
             if (fp.rank < fp.faction.mrtp) {
                 player.sendMessage("§7[f] Your rank is too low.");
                 return true;
             }
             
             if (args.length < 3) {
                 player.sendMessage("§7[f] Use syntax /f tptp <player> <player/home>");
                 return true;
             }
             
             FactionPlayer       src;
             FactionPlayer       dst;
             
             src = getFactionPlayer(args[1]);
             dst = getFactionPlayer(args[2]);
             
             if (src == null) {
                 player.sendMessage("§7[f] Source player does not exist.");
                 return true;
             }
             
             if (src.rank > fp.rank) {
                 player.sendMessage(String.format("§7[f] The player %s is higher in rank than you. Ask him.", args[2]));
                 return true;
             }
 
             if (dst == null) {
                 if (!args[2].equals("home") && !args[2].equals("spawn")) {
                     player.sendMessage("§7[f] Source player does not exist, or use word $home.");
                     return true;
                 }
             } else {
                 if (dst.rank > fp.rank) {
                     player.sendMessage(String.format("§7[f] The player %s is higher in rank than you. Ask him.", args[3]));
                     return true;
                 }
             }
             
             Location        loc;
             
             if (dst != null) {
                 if ((src.faction != dst.faction) || (src.faction != fp.faction)) {
                     player.sendMessage("§7[f] Everyone has to be in the same faction.");
                     return true;
                 }
                 
                 loc = getServer().getPlayer(args[2]).getLocation();
                 if (getServer().getPlayer(args[1]) != null) {
                     getServer().getPlayer(args[1]).teleport(loc);
                     synchronized (fp.faction) {
                         fp.faction.power = fp.faction.power - (fp.faction.power * 0.1);
                     }
                 } else {
                     player.sendMessage(String.format("§7[f] The player %s was not found.", args[1]));
                 }
                 return true;
             }
             
             if ((src.faction != fp.faction)) {
                 player.sendMessage("§7[f] Everyone has to be in the same faction.");
                 return true;
             }
             
             // are we going home or to spawn?
             if (args[2].equals("spawn")) {
                 loc = gspawn;
                 if (gspawn == null) {
                     player.sendMessage("§7[f] The spawn has not been set for factions!");
                     return true;
                 }
             } else {
                 // teleport them to home
                 if (fp.faction.hw == null) {
                     player.sendMessage("§7[f] The faction home is not set! Use /f sethome");
                     return true;
                 }
                 loc = new Location(getServer().getWorld(fp.faction.hw), fp.faction.hx, fp.faction.hy + 0.3, fp.faction.hz);                
             }
             teleportPlayer(getServer().getPlayer(args[1]), loc);
             synchronized (fp.faction) {
                 fp.faction.power = fp.faction.power - (fp.faction.power * percentageofPowerOnTeleport);
             }
             return true;
             
         }
         if (cmd.equals("setmrsh")) {
             FactionPlayer           fp;
             int                     rank;
             
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage("§7[f] You must be in a faction!");
                 return true;                
             }
             
             if (fp.rank < fp.faction.mrsh) {
                 player.sendMessage("§7[f] Your rank is too low.");
                 return true;
             }
             
             if (args.length < 2) {
                 player.sendMessage("§7[f] Use /f setmrsh <rank>");
                 return true;
             }
             
             rank = Integer.parseInt(args[1]);
             
             if (rank > fp.rank) {
                 player.sendMessage("§7[f] You can not set the rank higher than your own.");
                 return true;
             }
             
             fp.faction.mrsh = rank;
             player.sendMessage("§7[f] The rank was changed.");
             return true;
         }
         if (cmd.equals("setmrtp")) {
             FactionPlayer           fp;
             int                     rank;
             
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage("§7[f] You must be in a faction!");
                 return true;                
             }
             
             if (fp.rank < fp.faction.mrtp) {
                 player.sendMessage("§7[f] Your rank is too low.");
                 return true;
             }
             
             if (args.length < 2) {
                 player.sendMessage("§7[f] Use /f setmrtp <rank>");
                 return true;
             }
             
             rank = Integer.parseInt(args[1]);
             
             if (rank > fp.rank) {
                 player.sendMessage("§7[f] You can not set the rank higher than your own.");
                 return true;
             }
             
             fp.faction.mrtp = rank;
             player.sendMessage("§7[f] The rank was changed.");
             return true;
         }
         if (cmd.equals("setmrz")) {
             FactionPlayer           fp;
             int                     rank;
             
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage("§7[f] You must be in a faction!");
                 return true;                
             }
             
             if (fp.rank < fp.faction.mrz) {
                 player.sendMessage("§7[f] Your rank is too low.");
                 return true;
             }
             
             if (args.length < 2) {
                 player.sendMessage("§7[f] Use /f setmrz <rank>");
                 return true;
             }
             
             rank = Integer.parseInt(args[1]);
             
             if (rank > fp.rank) {
                 player.sendMessage("§7[f] You can not set the rank higher than your own.");
                 return true;
             }
             
             fp.faction.mrz = rank;
             player.sendMessage("§7[f] The rank was changed.");
             return true;
         }
         if (cmd.equals("setmrc")) {
             FactionPlayer           fp;
             int                     rank;
             
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage("§7[f] You must be in a faction!");
                 return true;                
             }
             
             if (fp.rank < fp.faction.mrc) {
                 player.sendMessage("§7[f] Your rank is too low.");
                 return true;
             }
             
             if (args.length < 2) {
                 player.sendMessage("§7[f] Use /f setmrc <rank>");
                 return true;
             }
             
             rank = Integer.parseInt(args[1]);
             
             if (rank > fp.rank) {
                 player.sendMessage("§7[f] You can not set the rank higher than your own.");
                 return true;
             }
             
             fp.faction.mrc = rank;
             player.sendMessage("§7[f] The rank was changed.");
             return true;
         }
         if (cmd.equals("setmri")) {
             FactionPlayer           fp;
             int                     rank;
             
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage("§7[f] You must be in a faction!");
                 return true;                
             }
             
             if (fp.rank < fp.faction.mri) {
                 player.sendMessage("§7[f] Your rank is too low.");
                 return true;
             }
             
             if (args.length < 2) {
                 player.sendMessage("§7[f] Use /f setmri <rank>");
                 return true;
             }
             
             rank = Integer.parseInt(args[1]);
             
             if (rank > fp.rank) {
                 player.sendMessage("§7[f] You can not set the rank higher than your own.");
                 return true;
             }
             
             fp.faction.mri = rank;
             player.sendMessage("§7[f] The rank was changed.");
             return true;
         }
         
         if (cmd.equals("setzaprank")) {
             FactionPlayer           fp;
             
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage("§7[f] You must be in a faction!");
                 return true;
             }
             
             if (args.length < 2) {
                 player.sendMessage("§7[f] Too few arguments. Use /f setzaprank <rank>");
                 return true;
             }
             
             if (fp.rank < fp.faction.mrz) {
                 player.sendMessage("§7[f] You are lower than the MRZ rank, so you can not change it.");
                 return true;
             }
             
             fp.faction.mrz = Integer.parseInt(args[1]);
             return true;
         }
         
         if (cmd.equals("showzaps")) {
             FactionPlayer           fp;
             Faction                 f;
 
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage("§7[f] You must be in a faction!");
                 return true;
             }
             
             f = fp.faction;
             
             synchronized (f) {
                 double     grandTotal;
 
                 player.sendMessage("[§6f§r] [§6DIR§r ] [§6AMOUNT§r    ] [§6FACTION§r]");
                 for (ZapEntry e : f.zappersOutgoing) {
                     player.sendMessage(String.format("    §6OUT§r (%11f) §6%s", e.amount, e.to.name));
                 }
                 
                 grandTotal = 0;
                 for (ZapEntry e : f.zappersIncoming) {
                     player.sendMessage(String.format("    §6IN§r   (%11f) §6%s", e.amount, e.from.name));
                     if (!e.isFake)
                         grandTotal += e.amount;
                 }
                 player.sendMessage(String.format("TOTAL ZAP INCOMING: %f", grandTotal));
             }
             
             return true;
         }
         
         if (cmd.equals("zap")) {
             FactionPlayer           fp;
             Faction                 tf;
             Faction                 f;
             int                     amount;
             String                  target;
             
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage("§7[f] You must be in a faction!");
                 return true;
             }
             
             if (fp.rank < fp.faction.mrz) {
                 player.sendMessage("§7[f] You do not have enough rank to ZAP.");
                 return true;
             }
             // <faction> <amount>
             if (args.length < 3) {
                 player.sendMessage("§7[f] The syntax is /f zap <faction> <amount>");
                 return true;
             }
             
             target = args[1];
             amount = Integer.parseInt(args[2]);
             
             if (amount <= 0) {
                 player.sendMessage("The amount of zap must be greater then zero.");
                 return true;
             }
             
             if (amount > fp.faction.power) {
                 player.sendMessage(String.format("§7[f] You do not have %d in power to ZAP with.", amount));
                 return true;
             }
             
             tf = getFactionByName(target);
             
             if (tf == null) {
                 player.sendMessage(String.format("§7[f] Faction could not be found by the name '%s'", target));
                 return true;
             }
             
             f = fp.faction;
             
             // make sure not more than 20 zaps are going this
             // is to prevent a DOS memory attack by flooding us with
             // thousands of entries
             Iterator<ZapEntry>      i;
             int                     zapCnt;
             
             i = tf.zappersIncoming.iterator();
             
             zapCnt = 0;
             while (i.hasNext()) {
                 if (i.next().from == f)
                     zapCnt++;
             }
             
             if (zapCnt > 20) {
                 player.sendMessage("§7[f] You reached maximum number of active zaps [20]");
                 return true;
             }
 
             ZapEntry            ze;
             
             ze = new ZapEntry();
             
             // no longer will it create fake zaps
             // instead it will warn the player of
             // the problem and not allow them to
             // zap the target faction
             if (tf.power < f.power) {
                 player.sendMessage("§7[f] That faction has less power than you! You can zap only if they have more than you.");
                 return true;
             }
             
             ze.amount = (double)amount;
             ze.timeStart = System.currentTimeMillis();
             ze.timeTick = ze.timeStart;
             ze.perTick = (double)amount / (1000.0d * 60.0d * 60.0d * 48.0d);
             
             if (ze.perTick <= 0.0d) {
                 // ensure it will at least drain amount which
                 // will result in the ZapEntry's removal
                 ze.perTick = 1.0d;
             }
             
             ze.from = f;
             ze.to = tf;
             
             synchronized (f) {
                 f.zappersOutgoing.add(ze);
                 tf.zappersIncoming.add(ze);
             
             // go ahead and subtract what they spent
                 f.power -= amount;
             }
             
             player.sendMessage("§7[f] Zap has commenced.");
             return true;
         }
         
         if (cmd.equals("create")) {
             FactionPlayer         fp;
             Faction               f;
             
             fp = getFactionPlayer(player.getName());
             
             if (args.length < 2) {
                 player.sendMessage("§7[f] The correct syntax is §/f create <name>§r.");
                 return true;
             }
             
             args[1] = sanitizeString(args[1]);
             
             if (fp != null) {
                 player.sendMessage("§7[f] You must leave your current faction to create a new faction.");
                 return true;
             }
             
             if (args.length < 2) {
                 player.sendMessage("§7[f] You must specify the new faction name. /f create <faction-name>");
                 return true;
             }
             
             if (factions.containsKey(args[1].toLowerCase())) {
                 player.sendMessage(String.format("§7[f] The faction name %s is already taken.", args[1]));
                 return true;
             }
             
             f = new Faction();
             f.name = args[1];
             f.desc = "default description";
             f.mrc = 700;
             f.mri = 600;
             
             fp = new FactionPlayer();
             fp.faction = f;
             fp.name = player.getName();
             fp.rank = 1000;
             f.players.put(player.getName(), fp);
             
             getServer().broadcastMessage(String.format("§7[f] %s created new faction %s!", player.getName(), args[1]));
             
             synchronized (factions) {
                 factions.put(args[1].toLowerCase(), f);
             }
             return true;
         }
         
         if (cmd.startsWith("cbr")) {
             FactionPlayer       fp;
             FactionChunk        fc;
             int                 x, z;
             Map<Integer, Integer>     m;
             
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage("§7[f] You must be in a faction!");
                 return true;
             }
             
             x = player.getLocation().getBlockX();
             z = player.getLocation().getBlockZ();
             
             fc = getFactionChunk(player.getWorld(), x >> 4, z >> 4);
             
             if (fc == null) {
                 player.sendMessage("§7[f] This land is not owned");
                 return true;
             }
             
             if (fc.faction != fp.faction) {
                 player.sendMessage(String.format("§7[f] This land is owned by §c%s§7!", fp.faction.name));
                 return true;
             }
             
             if (cmd.equals("cbru")) {
                 m = fc.tidu;
             } else {
                 m = fc.tid;
             }
             
             player.sendMessage("§7[f] Clearing Block Ranks");
             if (m != null) {
                 Iterator<Entry<Integer, Integer>>         i;
                 Entry<Integer, Integer>                   e;
                 
                 i = m.entrySet().iterator();
                 
                 while (i.hasNext()) {
                     e = i.next();
                     if (e.getValue() >= fp.rank) {
                         player.sendMessage(String.format("§7 Rank too low for to clear %s[%d] at rank %d!", TypeIdToNameMap.getNameFromTypeId(e.getKey()), e.getKey(), e.getValue()));
                     } else {
                         i.remove();
                     }
                 }
             }
             
             return true;
         }
 
         if (cmd.startsWith("lbr")) {
             FactionPlayer       fp;
             FactionChunk        fc;
             int                 x, z;
             Map<Integer, Integer>     m;
             
             fp = getFactionPlayer(player.getName());
              if (fp == null) {
                 player.sendMessage("§7[f] You must be in a faction!");
                 return true;
             }
             
             x = player.getLocation().getBlockX();
             z = player.getLocation().getBlockZ();
             
             fc = getFactionChunk(player.getWorld(), x >> 4, z >> 4);
             
             if (fc == null) {
                 player.sendMessage("§7[f] This land is not owned");
                 return true;
             }
             
             if (cmd.equals("lbru")) {
                 player.sendMessage("§7[f] List §cInteraction§7 Block Rank For Claim");
                 m = fc.tidu;
             } else {
                 player.sendMessage("§7[f] List §cPlace/Break§7 Block Rank For Claim");
                 m = fc.tid;
             }
             
             if (m != null) {
                 Iterator<Entry<Integer, Integer>>         i;
                 Entry<Integer, Integer>                   e;
                 
                 i = m.entrySet().iterator();
                 
                 while (i.hasNext()) {
                     e = i.next();
                     player.sendMessage(String.format("§7For §c%s§7(§d%d§7) you need rank §c%d§7 or better.", TypeIdToNameMap.getNameFromTypeId(e.getKey()), e.getKey(), e.getValue()));
                 }
                 
                 if (cmd.equals("lbru")) {
                     player.sendMessage("§7Use §d/f cbru§7 (to clear) and §d/f bru§7 to add to the list.");
                 } else {
                     player.sendMessage("§7Use §d/f cbr§7 (to clear) and §d/f br§7 to add to the list.");
                 }
             }
             return true;
         }
         
         if (cmd.equals("seechunk")) {
             long                ct, dt;
             final Player        _player;
             
             ct = System.currentTimeMillis();
             
             if (seeChunkLast.get(player.getName()) != null) {
                 dt = ct - seeChunkLast.get(player.getName());
                 if (dt < 10000) {
                     player.sendMessage(String.format("§7You need to wait %d more seconds before you can use this command again!", 10 - (dt / 1000)));
                     return true;
                 }
             }
             
             seeChunkLast.put(player.getName(), ct);
             
             showPlayerChunk(player, false);
             
             _player = player;
             getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                 @Override
                 public void run() {
                     showPlayerChunk(_player, true);
                 }
             }, 20 * 10);
             
             player.sendMessage("§7[f] The current chunk now surrounded with glass where there was air!");
             return true;
         }
         
         if (cmd.startsWith("br")) {
             FactionPlayer       fp;
             FactionChunk        fc;
             int                 typeId;
             byte                typeData;
             int                 rank;
             int                 x, z;
             Map<Integer, Integer>     m;
             
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage("§7[f] You must be in a faction!");
                 return true;
             }
             
             x = player.getLocation().getBlockX();
             z = player.getLocation().getBlockZ();
             
             fc = getFactionChunk(player.getWorld(), x >> 4, z >> 4);
                        
             if (fc == null) {
                 player.sendMessage("This land is not claimed.");
                 return true;
             }
             
             if (fc.faction != fp.faction) {
                 player.sendMessage(String.format("§7[f] This land is owned by %s.", fc.faction.name));
                 return true;
             }
             
             if (args.length == 1) {
                 player.sendMessage("§7[f] Either hold item in hand, or use /f blockrank <rank> <typeId> <dataId(optinal)>");
                 return true;
             }
             
             rank = Integer.parseInt(args[1]);
             
             if (rank >= fp.rank) {
                 player.sendMessage("§7[f] You can not set a block rank equal or greater than your rank.");
                 return true;
             }
 
             if (args.length > 2) {
                 typeId = Integer.parseInt(args[2]);
                 if (args.length < 4) {
                     typeData = (byte)0;
                 } else {
                     typeData = (byte)Integer.parseInt(args[3]);
                 }
             } else {
                 if (player.getItemInHand().getTypeId() == 0) {
                     if (cmd.equals("bru")) {
                         player.sendMessage("§7[f] Either hold item in hand and use §c/f bru <rank>§7, or use §c/f br <rank> <typeId>§7");
                     } else {
                         player.sendMessage("§7[f] Either hold item in hand and use §c/f br <rank>§7, or use §c/f br <rank> <typeId>§7");
                     }
                     return true;
                 }
                 typeId = player.getItemInHand().getTypeId();
                 typeData = player.getItemInHand().getData().getData();
             }
             
             
             if (cmd.equals("bru")) {
                 if (fc.tidu == null)
                     fc.tidu = new HashMap<Integer, Integer>();
                 m = fc.tidu;
             } else {
                 if (fc.tid == null)
                     fc.tid = new HashMap<Integer, Integer>();
                 m = fc.tid;
             }
             
             /// UPGRADE CODE BLOCK
             if (m.containsKey(typeId)) {
                 if (m.get(typeId) >= fp.rank) {
                     player.sendMessage(String.format("§7[f] Block rank exists for §a%s[%d]§r and is equal or higher than your rank §b%d§r.", TypeIdToNameMap.getNameFromTypeId(typeId), fc.tid.get(typeId), fp.rank));
                     return true;
                 }
             }
             
             m.put(typeId, rank);
             player.sendMessage(String.format("§7[f] Block §a%s§r[%d] at rank §a%d§r added to current claim.", TypeIdToNameMap.getNameFromTypeId(typeId), typeId, rank));
             return true;
         }
             
         if (cmd.equals("invite")) {
             FactionPlayer       fp;
             Player              p;
             
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage("§7[f] You must be in a faction!");
                 return true;
             }
             
             if (args.length < 2) {
                 player.sendMessage("§7[f] You must specify the name of whom to invite! /f invite <player-name>");
                 return true;
             }
             
             if (fp.rank < fp.faction.mri) {
                 player.sendMessage(String.format("§7[f] Your rank is %d but needs to be %d.", fp.rank, fp.faction.mri));
                 return true;
             }
             
             p = getServer().getPlayer(args[1]);
             
             if (p == null) {
                 player.sendMessage("§7[f] The player must be online to invite them.");
                 return true;
             }
             
             fp.faction.invites.add(args[1].toLowerCase());
             sendFactionMessage(fp.faction, String.format("§7[f] %s has been invited to your faction", args[1]));
             if (p != null) {
                 p.sendMessage(String.format("§7[f] You have been invited to %s by %s. Use /f join %s to join!", fp.faction.name, fp.name, fp.faction.name));
             } 
             return true;
         }
         
         if (cmd.equals("kick")) {
             FactionPlayer       fp;
             FactionPlayer       _fp;
             
             fp = getFactionPlayer(player.getName());
             
             if (fp.rank < fp.faction.mri) {
                 player.sendMessage(Language.get("RANKTOOLOWTOKICKORINVITE"));
                 return true;
             }
             
             if (args.length < 2) {
                 player.sendMessage(Language.get("KICKSYNTAX"));
                 return true;
             }
             
             _fp = getFactionPlayer(args[1]);
             
             if (_fp == null) {
                 player.sendMessage(Language.get("PLAYERSPECIFIEDNOTINFACTION"));
                 return true;
             }
             
             if (_fp.faction != fp.faction) {
                 player.sendMessage(String.format(Language.get("PLAYERINXYOUINZ"), _fp.faction.name, fp.faction.name));
                 return true;
             }
             
             if (_fp.rank >= fp.rank) {
                 player.sendMessage(String.format(Language.get("FORKICKPLAYERTOOHIGHRANK"), _fp.name, _fp.rank, fp.rank));
                 return true;
             }
             
             fp.faction.players.remove(_fp.name);
             getServer().broadcastMessage(String.format(Language.get("PLAYERKICKED"), _fp.name, fp.faction.name, fp.name));
             
             return true;
         }
         
         if (cmd.equals("join")) {
             FactionPlayer       fp;
             Faction             f;
             
             fp = getFactionPlayer(player.getName());
             if (fp != null) {
                 player.sendMessage(Language.get("LEAVECURRENTTOJOINNEW"));
                 return true;
             }
             
             if (args.length < 2) {
                 player.sendMessage(Language.get("JOINSYNTAX"));
                 return true;
             }
             
             f = getFactionByName(args[1]);
             if (f == null) {
                 player.sendMessage(Language.get("NOFACTIONBYTHATNAME"));
                 return true;
             }
             
             // FIX FOR OLDER VER CLASS
             if (f.invites == null)
                 f.invites = new HashSet<String>();
             
             Iterator<String>            i;
             
             i = f.invites.iterator();
             
             while (i.hasNext()) {
                 if (i.next().toLowerCase().equals(player.getName().toLowerCase())) {
                     f.invites.remove(player.getName());
 
                     fp = new FactionPlayer();
                     fp.faction = f;
                     fp.name = player.getName();
                     fp.rank = 0;
                     f.players.put(player.getName(), fp);
                     getServer().broadcastMessage(String.format(Language.get("JOINEDFACTION"), fp.name, f.name));
                     return true;
                 }
             }
             
             player.sendMessage(Language.get("NOINVITETOJOIN"));
             return true;
             
         }        
         
         if (cmd.equals("leave")) {
             FactionPlayer           fp;
             FactionChunk            fchunk;
             
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage(Language.get("NOTINFACTION"));
                 return true;
             }
             
             /// MAKE SURE WE ARE NOT STANDING ON FACTION CLAIMED LAND
             fchunk = getFactionChunk(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockZ());
             if (fchunk != null) {
                 if (fchunk.faction == fp.faction) {
                     player.sendMessage(Language.get("NOTBEONFACTIONLAND"));
                     return true;
                 }
             }
             /// MAKE SURE NOT EMPTY IF SO NEED TO USE DISBAND
             /// IF WE ARE OWNER WE NEED TO TRANSFER OWNERSHIP BEFORE WE LEAVE
             if (fp.faction.players.size() == 1) {
                 // ENSURE THEY ARE HIGH ENOUGH FOR OWNER (CATCH BUGS KINDA)
                 fp.rank = 1000;
                 player.sendMessage(Language.get("LASTPLAYERUSEDISBAND"));
                 return true;
             }
             
             // IF THEY ARE THE OWNER HAND OWNERSHIP TO SOMEBODY ELSE KINDA AT RANDOM
             if (fp.rank == 1000) {
                 Iterator<Entry<String, FactionPlayer>>      i;
                 FactionPlayer                               _fp;
                 
                 i = fp.faction.players.entrySet().iterator();
                 _fp = null;
                 while (i.hasNext()) {
                     _fp = i.next().getValue();
                     if (!fp.name.equals(_fp.name))
                         break;
                 }
                 _fp.rank = 1000;
                 getServer().broadcastMessage(String.format(Language.get("OWNERSHIPHANDEDTO"), fp.faction.name, fp.name));
             }
             
             getServer().broadcastMessage(String.format(Language.get("LEFTFACTION"), fp.name, fp.faction.name));
             fp.faction.players.remove(fp.name);            
             return true;
         }
         
         if (cmd.equals("disband")) {
             FactionPlayer               fp;
             
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage(Language.get("NOTINFACTION"));
                 return true;
             }
             
             // MUST BE OWNER OF FACTION
             if (fp.rank < 1000) {
                 player.sendMessage(Language.get("NOTOWNER"));
                 return true;
             }
             
             // why the hell am i doing this? --kmcguire
             fp.faction.chunks = new HashMap<String, Map<Long, FactionChunk>>(); 
             
             getServer().broadcastMessage(String.format(Language.get("FACTIONDISBANDED"), fp.name, fp.faction.name));
             
             factions.remove(fp.faction.name.toLowerCase());
             return true;
         }
         
         if (cmd.equals("listfriends")) {
             String                              friendName;
             Faction                             f;
             FactionPlayer                       fp;
             int                                 frank;
             
             Iterator<Entry<String, Integer>>    i;
             Entry<String, Integer>              e;
             
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage(Language.get("NOTINFACTION"));
                 return true;
             }
             
             if (fp.faction.friends == null) {
                 player.sendMessage(Language.get("FACTIONHASNOFRIENDS"));
                 return true;
             }
             
             i = fp.faction.friends.entrySet().iterator();
             while (i.hasNext()) {
                 e = i.next();
                 player.sendMessage(String.format(Language.get("FACTIONFRIENDLISTING"), e.getKey(), e.getValue()));
             }
             player.sendMessage(Language.get("FACTIONFRIENDLISTINGDONE"));
             return true;
         }
         
         if (cmd.equals("addfriend")) {
             String          friendName;
             Faction         f;
             FactionPlayer   fp;
             int             frank;
 
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage(Language.get("NOTINFACTION"));
                 return true;
             }
             
             if (args.length < 3) {
                 player.sendMessage(Language.get("ADDFRIENDSYNTAX"));
                 return true;
             }
             
             friendName = args[1];
             frank = Integer.parseInt(args[2]);
 
             if (frank >= fp.rank) {
                 player.sendMessage(String.format(Language.get("CANNOTSETFRIENDRANKHIGHER"), frank, fp.rank));
                 return true;
             }
             
             if (fp.faction.friends == null)
                 fp.faction.friends = new HashMap<String, Integer>();
 
             fp.faction.friends.put(friendName, frank);
             sendFactionMessage(fp.faction, String.format(Language.get("ADDEDFRIEND"), friendName, frank));
             return true;
         }
         
         if (cmd.equals("remfriend")) {
             String          friendName;
             Faction         f;
             FactionPlayer   fp;
             int             frank;
 
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage(Language.get("NOTINFACTION"));
                 return true;
             }
             
             if (args.length < 2) {
                 player.sendMessage(Language.get("REMFRIENDSYNTAX"));
                 return true;
             }
             
             friendName = args[1];
             
             if (fp.faction.friends == null)
                 fp.faction.friends = new HashMap<String, Integer>();
             
             if (fp.faction.friends.containsKey(friendName)) {
                 frank = fp.faction.friends.get(friendName);
                 if (frank >= fp.rank) {
                     player.sendMessage(String.format(Language.get("CANNOTREMOVEFRIENDHIGHERRANK"), frank, fp.rank));
                     return true;
                 }
             }
             
             fp.faction.friends.remove(friendName);
             sendFactionMessage(fp.faction, String.format(Language.get("REMOVEDFRIEND"), friendName));
             return true;            
         }
         
         if (cmd.equals("rename")) {
             FactionPlayer               fp;
             String                      newName;
             
             fp = getFactionPlayer(player.getName());
             
             if (fp == null) {
                 player.sendMessage(Language.get("NOTINFACTION"));
                 return true;
             }
             
             if (fp.rank < 1000) {
                 player.sendMessage(Language.get("NOTOWNER"));
                 return true;
             }
             
             if (args.length < 2) {
                 player.sendMessage(Language.get("RENAMESYNTAX"));
                 return true;                
             }
             
             newName = args[1];
             
             newName = sanitizeString(newName);
             
             if (newName.length() < 1) {
                 player.sendMessage(Language.get("NAMEMUSTHAVELENGTH"));
                 return true;
             }
             
             getServer().broadcastMessage(String.format(Language.get("FACTIONRENAME"), fp.faction.name, newName));
             
             synchronized (factions) {
                 factions.remove(fp.faction.name.toLowerCase());
                 fp.faction.name = newName;
                 factions.put(fp.faction.name.toLowerCase(), fp.faction);
             }
             return true;
         }
         
         if (cmd.equals("claim")) {
             FactionPlayer               fp;
             int                         x, z, c;
             FactionChunk                fchunk;
             double                      pow;
             
             fp = getFactionPlayer(player.getName());
             
             if (fp == null) {
                 player.sendMessage(Language.get("NOTINFACTION"));
                 return true;
             }
             
             // IS OUR RANK GOOD ENOUGH?
             if (fp.rank < fp.faction.mrc) {
                 player.sendMessage(String.format(Language.get("RANKTOOLOWTOCLAIM"), fp.rank, fp.faction.mrc));
                 return true;
             }
             
             // count the number of claims they current have
             c = 0;
             for (Map m : fp.faction.chunks.values()) {
                 c += m.size();
             }
             
             // have they exceeded the free claims count?
             if (c >= numberOfFreeClaims) {
                 pow = getFactionPower(fp.faction);
                 if (pow < powerUsedEachClaim) {
                     player.sendMessage(String.format(Language.get("NOMOREFREECLAIMS"), powerUsedEachClaim));
                     return true;
                 }
                 synchronized(fp.faction) {
                     fp.faction.power -= powerUsedEachClaim;
                 }
             }
             
             x = player.getLocation().getBlockX();
             z = player.getLocation().getBlockZ();
             
             fchunk = getFactionChunk(player.getWorld(), x >> 4, z >> 4);
             if (fchunk != null) {
                 if (fchunk.faction == fp.faction) {
                     player.sendMessage(String.format(Language.get("CHUNKALREADYOWNEDBYYOURFACTION"), fchunk.faction.name));
                     return true;
                 }
                 
                 if (noGriefPerWorld.contains(player.getWorld().getName())) {
                     player.sendMessage(Language.get("WORLDPROHIBITOVERCLAIM"));
                     return true;
                 }
 
                 if (fchunk.faction != fp.faction) {
                     if (getFactionPower(fchunk.faction) >= fchunk.faction.chunks.size()) {
                         player.sendMessage(String.format(Language.get("FACTIONHASENOUGHPOWERTOHOLD"), fchunk.faction.name));
                         return true;
                     }
                     getServer().broadcastMessage(String.format(Language.get("LOSTCLAIMTO"), fchunk.faction.name, fp.faction.name, fp.name));
                     fchunk.faction.chunks.remove(getChunkLong(player.getWorld(), x >> 4, z >> 4));
                 }
             }
             
             fchunk = new FactionChunk();
             fchunk.x = x >> 4;
             fchunk.z = z >> 4;
             fchunk.worldName = player.getWorld().getName();
             fchunk.faction = fp.faction;
             fchunk.mrb = 500;               // DEFAULT VALUES
             fchunk.mru = 250;
             
             fchunk.faction = fp.faction;
             
             if (fp.faction.chunks.get(player.getWorld().getName()) == null) {
                 fp.faction.chunks.put(player.getWorld().getName(), new HashMap<Long, FactionChunk>());
             }
             
             fp.faction.chunks.get(player.getWorld().getName()).put(LongHash.toLong(x >> 4, z >> 4), fchunk);
             //fp.faction.chunks.put(getChunkLong(player.getWorld(), x >> 4, z >> 4), fchunk);
             getServer().broadcastMessage(String.format(Language.get("FACTIONCLAIMEDLAND"), fp.name, fp.faction.name));
             return true;
         }
         
         if (cmd.equals("setrank")) {
             FactionPlayer           fp;
             FactionPlayer           mp;
             int                     nrank;
             
             if (args.length < 3) {
                 player.sendMessage(Language.get("SETRANKSYNTAX"));
                 return true;
             }
             
             fp = getFactionPlayer(args[1]);
             mp = getFactionPlayer(player.getName());
             
             if (fp == null) {
                 player.sendMessage(Language.get("PLAYERNOTINFACTION"));
                 return true;
             }
             
             if (mp == null) {
                 player.sendMessage(Language.get("NOTINFACTION"));
                 return true;
             }
             
             if (fp.faction != mp.faction) {
                 player.sendMessage(Language.get("YOUNOTINSAMEFACTIONAS"));
             }
             
             if ((fp.rank >= mp.rank) && (!player.isOp())) {
                 player.sendMessage(Language.get("PLAYERATORGREATERRANK"));
                 return true;
             }
             
             nrank = Integer.valueOf(args[2]);
             
             if ((nrank >= mp.rank) && (!player.isOp())) {
                 player.sendMessage(Language.get("TARGETPLAYERGREATERRANK"));
                 return true;
             }
             
             fp.rank = nrank;
             player.sendMessage(String.format(Language.get("RANKFORXIS"), args[1], nrank));
             return true;
         }
         
         if (cmd.equals("autoclaim")) {
             FactionChunk            fc;
             int                     bx, bz;
             
             bx = player.getLocation().getBlockX();
             bz = player.getLocation().getBlockZ();
             
             fc = getFactionChunk(player.getWorld(), bx >> 4, bz >> 4);
             
             if (fc == null) {
                 args = new String[1];
                 args[0] = "claim";
                 onCommand(player, new __LocalCommandObject("f"), null, args);
             }
             
             if (autoClaim.contains(player.getName())) {
                 autoClaim.remove(player.getName());
                 player.sendMessage(String.format(Language.get("AUTOCLAIMOFF")));
             } else {
                 autoClaim.add(player.getName());
                 player.sendMessage(String.format(Language.get("AUTOCLAIMON")));
             }
             return true;
         }
         
         if (cmd.equals("unclaim")) {
             FactionPlayer               fp;
             FactionChunk                fchunk;
             int                         x, z;
             
             fp = getFactionPlayer(player.getName());
 
             if (fp == null) {
                 player.sendMessage(Language.get("NOTINFACTION"));
                 return true;
             }            
 
             // IS OUR RANK GOOD ENOUGH?
             if (fp.rank < fp.faction.mrc) {
                 player.sendMessage(String.format(Language.get("RANKTOOLOWCLAIMUNCLAIM"), fp.rank, fp.faction.mrc));
                 return true;
             }
             
             x = player.getLocation().getBlockX() >> 4;
             z = player.getLocation().getBlockZ() >> 4;
             fchunk = getFactionChunk(player.getWorld(), x, z);
             
             if (fchunk == null) {
                 player.sendMessage(Language.get("LANDCHUNKNOTONED"));
                 return true;
             }
             
             if (fchunk.faction != fp.faction) {
                 player.sendMessage(String.format(Language.get("NOTYOURLAND"), fp.faction.name, fchunk.faction.name));
                 return true;
             }
             
             fp.faction.chunks.get(player.getWorld().getName()).remove(LongHash.toLong(x, z));
             
             // UPDATE FACTION POWER
             getFactionPower(fp.faction);
             
             getServer().broadcastMessage(String.format(Language.get("FACTIONUNCLAIMED"), fp.name, fp.faction.name));
             return true;
         }
         
         if (cmd.equals("unclaimall")) {
             FactionPlayer                       fp;
             Iterator<Entry<Long, FactionChunk>> i;
             
             fp = getFactionPlayer(player.getName());
             
             if (fp == null) {
                 player.sendMessage(Language.get("NOTINFACTION"));
                 return true;
             }
             
             // IS OUR RANK GOOD ENOUGH?
             if (fp.rank < fp.faction.mrc) {
                 player.sendMessage(String.format(Language.get("RANKTOOLOWCLAIMUNCLAIM"), fp.rank, fp.faction.mrc));
                 return true;
             }
             
             fp.faction.chunks = new HashMap<String, Map<Long, FactionChunk>>();
             
             getServer().broadcastMessage(String.format(Language.get("UNCLAIMEDALL"), fp.name, fp.faction.name));
             return true;
         }
         
         if (cmd.equals("charge")) {
             int                         icnt;
             int                         mid;
             byte                        dat;
             double                      pts;
             
             icnt = player.getItemInHand().getAmount();
             mid = player.getItemInHand().getTypeId();
             dat = player.getItemInHand().getData().getData();
             
             pts = (double)getEMC(mid, dat);
             
             player.sendMessage(String.format(Language.get("CHECKCHARGE"), pts, pts * icnt));
             return true;
         } 
         
         if (cmd.equals("docharge")) {
             FactionPlayer               fp;
             int                         icnt;
             double                      pts;
             int                         mid;
             byte                        dat;
                     
             fp = getFactionPlayer(player.getName());
             if (fp == null) {
                 player.sendMessage(Language.get("NOTINFACTION"));
                 return true;
             }
             
             icnt = player.getItemInHand().getAmount();
             mid = player.getItemInHand().getTypeId();
             dat = player.getItemInHand().getData().getData();
             
             //pts = (double)EEMaps.getEMC(mid, dat);
             pts = (double)getEMC(mid, dat);       
             
             player.sendMessage(String.format(Language.get("ITEMVALIS"), pts));
             
             pts = pts * icnt;
             
             if (pts == 0.0d) {
                 player.sendMessage(Language.get("ITEMNOCHARGE"));
                 return true;
             }
             
             player.setItemInHand(null);
             
             synchronized (fp.faction) {
                 fp.faction.power += pts;
             }
             
             //getServer().broadcastMessage(String.format("§7[f] %s in %s charged faction power!", player.getDisplayName(), fp.faction.name));
             sendFactionMessage(fp.faction, String.format(Language.get("ITEMCHARGED"), player.getDisplayName(), pts, getFactionPower(fp.faction)));
             return true;
         }
         
         // SHOW FACTION INFORMATION ABOUT OUR FACTION
         // OR ANOTHER FACTION SPECIFIED
         if (cmd.equalsIgnoreCase("who")) {
             FactionPlayer                           fp;
             Faction                                 f;
             Iterator<Entry<String, FactionPlayer>>  iter;
             Entry<String, FactionPlayer>            e;
             String                                  o;
             
             fp = getFactionPlayer(player.getName());
             
             if (args.length < 2 && fp == null) {
                 player.sendMessage(Language.get("NEEDTOBEINFACTION"));
                 return true;
             }
             
             if (args.length == 2) {
                 f = getFactionByName(args[1]);
                 if (f == null) {
                     fp = getFactionPlayer(args[1]);
                     if (fp == null) {
                         player.sendMessage(String.format(Language.get("COULDNOTFINDFACTIONORPLAYER"), args[1]));
                         return true;
                     }
                     f = fp.faction;
                 }
             } else {
                 f = fp.faction;
             }
             
             if (f == null) {
                 player.sendMessage(String.format(Language.get("FACTIONNOTFOUND"), args[1]));
                 return true;
             }
             
             // display name and description
             player.sendMessage(String.format(Language.get("FACTION_WHO_HEADER"), f.name));
             //player.sendMessage(String.format("Description: %s", f.desc));
             // display land/power/maxpower
             if (fp != null) {
                 player.sendMessage(String.format(Language.get("FACTION_INFO"), 
                     f.chunks.size(), 
                     (int)(landPowerCostPerHour * f.chunks.size()), 
                     (int)getFactionPower(f)
                 ));
                 player.sendMessage(String.format(Language.get("FACTION_FOOTER"), 
                      (int)(getFactionPower(f) / (landPowerCostPerHour * f.chunks.size()))
                 ));
             } else {
                 player.sendMessage(String.format(Language.get("FACTION_OTHER"),
                         f.chunks.size()
                 ));                
             }    
             
             // mri, mrc, flags, mrz, mrtp, mrsh
             player.sendMessage(String.format(
                 Language.get("FACTION_RANKS"), 
                 f.mri, f.mrc, f.flags, (int)f.mrz, (int)f.mrtp, f.mrsh
             ));
             
             iter = f.players.entrySet().iterator();
             
             o = Language.get("FACTION_MEMBERS_HEADER"); 
             while (iter.hasNext()) {
                 e = iter.next();
                 if (getServer().getPlayer(e.getKey()) != null) {
                     o = String.format(Language.get("FACTION_MEMBERS_ONLINE"), o, e.getKey(), e.getValue().rank);
                 } else {
                     o = String.format(Language.get("FACTION_MEMBERS_OFFLINE"), o, e.getKey(), e.getValue().rank);
                 }
             }
             player.sendMessage(o);
             return true;
         }
         
         if (cmd.equals("inspect")) {
             Location            loc;
             FactionChunk        fc;
             int                 bx, bz;
             
             loc = player.getLocation();
             
             bx = loc.getBlockX() >> 4;
             bz = loc.getBlockZ() >> 4;
             
             fc = getFactionChunk(player.getWorld(), bx, bz);
             
             if (fc == null) {
                 player.sendMessage(Language.get("LANDNOTCLAIMED"));
                 return true;
             }
             
             player.sendMessage(String.format(Language.get("INSPECT_INFO"), fc.faction.name, fc.mrb, fc.mru));
             return true;
         }
         displayHelp(player, args);
         return false;
     }
     
     protected void smsg(String msg) {
         this.getLogger().info(msg);
     }
 }
