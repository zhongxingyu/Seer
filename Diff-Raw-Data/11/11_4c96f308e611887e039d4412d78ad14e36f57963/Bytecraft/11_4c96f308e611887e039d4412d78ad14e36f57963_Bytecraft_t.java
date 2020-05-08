 package info.bytecraft;
 
 import info.bytecraft.api.*;
 import info.bytecraft.api.BytecraftPlayer.Flag;
 import info.bytecraft.commands.*;
 import info.bytecraft.database.*;
 import info.bytecraft.database.db.DBContextFactory;
 import info.bytecraft.listener.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.maxmind.geoip.LookupService;
 
 public class Bytecraft extends JavaPlugin
 {
     private HashMap<String, BytecraftPlayer> players;
     private static IContextFactory contextFactory;
     
     private LookupService lookup = null;
     
     private final SimpleDateFormat format = new SimpleDateFormat(
             "MM/dd/YY hh:mm a");
     
     public void onLoad()
     {
         File folder = getDataFolder();
         reloadConfig();
         
         FileConfiguration config = getConfig();
         contextFactory = new DBContextFactory(config, this);
         
         try{
             lookup = new LookupService(new File(folder, "GeoIPCity.dat"), 
                     LookupService.GEOIP_MEMORY_CACHE);
         }catch(IOException e){
             getLogger().warning("Could not find GeoIPCity.dat, is it in the correct folder?");
         }
     }
 
     public void onEnable()
     {
         players = Maps.newHashMap();
         for (Player delegate : Bukkit.getOnlinePlayers()) {
             try {
                 addPlayer(delegate, delegate.getAddress().getAddress());
             } catch (PlayerBannedException e) {}
         }
 
         registerEvents();
 
         getCommand("ban").setExecutor(new BanCommand(this));
         getCommand("bless").setExecutor(new BlessCommand(this));
         getCommand("clear").setExecutor(new ClearCommand(this));
         getCommand("cmob").setExecutor(new CreateMobCommand(this));
         getCommand("creative").setExecutor(new GameModeCommand(this, "creative"));
         getCommand("channel").setExecutor(new ChannelCommand(this));
         getCommand("fill").setExecutor(new FillCommand(this));
         getCommand("force").setExecutor(new ForceCommand(this));
         getCommand("gamemode").setExecutor(new GameModeCommand(this, "gamemode"));
         getCommand("give").setExecutor(new GiveCommand(this));
         getCommand("god").setExecutor(new SayCommand(this, "god"));
         getCommand("hardwarn").setExecutor(new WarnCommand(this, "hardwarn"));
         getCommand("home").setExecutor(new HomeCommand(this));
         getCommand("inv").setExecutor(new InventoryCommand(this));
         getCommand("item").setExecutor(new ItemCommand(this));
         getCommand("kick").setExecutor(new KickCommand(this));
         getCommand("kill").setExecutor(new KillCommand(this));
         getCommand("makewarp").setExecutor(new WarpCreateCommand(this));
         getCommand("me").setExecutor(new ActionCommand(this));
         getCommand("message").setExecutor(new MessageCommand(this));
         getCommand("pos").setExecutor(new PositionCommand(this));
         getCommand("say").setExecutor(new SayCommand(this, "say"));
         getCommand("summon").setExecutor(new SummonCommand(this));
         getCommand("support").setExecutor(new SupportCommand(this));
         getCommand("spawn").setExecutor(new SpawnCommand(this));
         getCommand("survival").setExecutor(new GameModeCommand(this, "survival"));
         getCommand("time").setExecutor(new TimeCommand(this));
         getCommand("tpblock").setExecutor(new TeleportBlockCommand(this));
         getCommand("teleport").setExecutor(new TeleportCommand(this));
         getCommand("tppos").setExecutor(new TeleportPosCommand(this));
         getCommand("user").setExecutor(new UserCommand(this));
         getCommand("vanish").setExecutor(new VanishCommand(this));
         getCommand("wallet").setExecutor(new WalletCommand(this));
         getCommand("warn").setExecutor(new WarnCommand(this, "warn"));
         getCommand("warp").setExecutor(new WarpCommand(this));
         getCommand("who").setExecutor(new WhoCommand(this));
         //getCommand("zone").setExecutor(new ZoneCommand(this));
     }
     
     public IContextFactory getContextFactory()
     {
         return contextFactory;
     }
     
     public IContext createContext()
     throws DAOException
     {
         return contextFactory.createContext();
     }
 
     private void registerEvents()
     {
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(new ChatListener(this), this);
         pm.registerEvents(new BytecraftPlayerListener(this), this);
         pm.registerEvents(new BlessListener(this), this);
         //pm.registerEvents(new RareDropListener(this), this);
         pm.registerEvents(new BytecraftBlockListener(this), this);
         //pm.registerEvents(new DamageListener(this), this);
         pm.registerEvents(new FillListener(this), this);
         pm.registerEvents(new PlayerPromotionListener(this), this);
         //pm.registerEvents(new SelectListener(this), this);
         //pm.registerEvents(new ZoneListener(this), this);
     }
     
     // ========================================================
     // ================== Player Methods ======================
     // ========================================================
     
     public void reloadPlayer(BytecraftPlayer player)
     {
         try{
             addPlayer(player.getDelegate(), player.getAddress().getAddress());
         }catch(PlayerBannedException e){
             player.kickPlayer(e.getMessage());
         }
     }
 
     public BytecraftPlayer getPlayer(Player player)
     {
         return getPlayer(player.getName());
     }
 
     public BytecraftPlayer getPlayer(String name)
     {
         return players.get(name);
     }
     
     public BytecraftPlayer getPlayerOffline(String name)
     {
         if(this.players.containsKey(name)){
             return players.get(name);
         }
         
         return null;
     }
 
     public BytecraftPlayer addPlayer(Player srcPlayer,  InetAddress addr)
             throws PlayerBannedException
     {
         if (players.containsKey(srcPlayer.getName())) {
             return players.get(srcPlayer.getName());
         }
         
         try (IContext ctx = createContext()){
             IPlayerDAO dao = ctx.getPlayerDAO();
             BytecraftPlayer player = dao.getPlayer(srcPlayer);
             
             if(player == null){
                player = dao.createPlayer(srcPlayer);
             }
             
             player.setFlag(Flag.HARDWARNED, false);
             player.setFlag(Flag.SOFTWARNED, false);
             player.setFlag(Flag.MUTE, false);
             
             IReportDAO reportDao = ctx.getReportDAO();
             List<PlayerReport> reports = reportDao.getReports(player);
             for(PlayerReport report: reports){
                 Date validUntil = report.getValidUntil();
                 if (validUntil == null) {
                     continue;
                 }
                 if (validUntil.getTime() < System.currentTimeMillis()) {
                     continue;
                 }
                 
                 if (report.getAction() == PlayerReport.Action.SOFTWARN) {
                     player.setFlag(Flag.SOFTWARNED, true);
                 }
                 else if (report.getAction() == PlayerReport.Action.HARDWARN) {
                     player.setFlag(Flag.HARDWARNED, true);
                 }else if(report.getAction() == PlayerReport.Action.MUTE){
                     player.setFlag(Flag.MUTE, true);
                 }
                 else if (report.getAction() == PlayerReport.Action.BAN) {
                     throw new PlayerBannedException(ChatColor.RED + "You are banned from this server until "
                            + ChatColor.GOLD + format.format(validUntil));
                 }
             }
             
             ChatColor color = player.getRank().getColor();
             if(player.hasFlag(Flag.HARDWARNED) || player.hasFlag(Flag.SOFTWARNED)){
                 color = ChatColor.GRAY;
             }
             if(player.hasFlag(Flag.NOBLE)){
                 if(player.getRank() == Rank.MEMBER || player.getRank() == Rank.SETTLER){
                     color = ChatColor.GOLD;
                 }
             }
             
             player.setDisplayName(color + player.getName() + ChatColor.WHITE);
             String name = color + player.getName();
             
             if(name.length() > 16){
                 player.setPlayerListName(name.substring(0, 15));
             }else{
                 player.setPlayerListName(name);
             }
             
             player.setIp(addr.getHostAddress());
             player.setHost(addr.getCanonicalHostName());
             
             if(lookup != null){
                 com.maxmind.geoip.Location loc = lookup.getLocation(player.getIp());
                 if(loc != null){
                     player.setCountry(loc.countryName);
                     player.setCity(loc.city);
                 }
             }
             
             players.put(player.getName(), player);
             return player;
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
     }
 
     public void removePlayer(BytecraftPlayer player)
     {
         try(IContext ctx = createContext()){
             ctx.getPlayerDAO().updatePlayTime(player);
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
         this.players.remove(player.getName());
     }
 
     public List<BytecraftPlayer> getOnlinePlayers()
     {
         List<BytecraftPlayer> playersList = new ArrayList<BytecraftPlayer>();
         for (BytecraftPlayer player : players.values()) {
             playersList.add(player);
         }
         return playersList;
     }
     
     public List<BytecraftPlayer> matchPlayer(String name)
     {
         List<BytecraftPlayer> players = Lists.newArrayList();
         for(BytecraftPlayer player: getOnlinePlayers()){
             String playerName = player.getName().toLowerCase();
             if(playerName.startsWith(name.toLowerCase())){
                 players.add(player);
             }
         }
         return players;
     }
     
     public BytecraftPlayer getBytecraftPlayerOffline(String name)
     {
         if(players.containsKey(name)){
             return players.get(name);
         }
         try(IContext ctx = createContext()){
             return ctx.getPlayerDAO().getPlayer(name);
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
     }
 
     // ========================================================
     // ======================= Zones ==========================
     // ========================================================
     
     public List<Zone> getZones(String world)
     {
         try(IContext ctx = createContext()){
             return ctx.getZoneDAO().getZones(world);
         } catch (DAOException e) {
             throw new RuntimeException(e);
         }
     }
     
     public Zone getZone(String name)
     {
         try(IContext ctx = createContext()){
             return ctx.getZoneDAO().getZone(name);
         }catch(DAOException e){
             throw new RuntimeException(e);
         }
     }
     
     // ========================================================
     // ===================== Other ============================
     // ========================================================
     public long getValue(Block block)
     {
         switch (block.getType()) {
         case STONE:
             return 1;
         case DIRT:
             return 1;
         case GRASS:
             return 1;
         case SAND:
             return 2;
         case IRON_ORE:
             return 30;
         case COAL_ORE:
             return 5;
         case LAPIS_ORE:
             return 5;
         case GOLD_ORE:
             return 50;
         case EMERALD_ORE:
             return 100;
         case DIAMOND_ORE:
             return 200;
         default:
             return 1;
         }
     }
     
     public Location getWorldSpawn(String name)
     {
         String loc = this.getConfig().getString("spawn." + name);
         
         if(loc == null || loc.equalsIgnoreCase("")){
             return Bukkit.getWorld(name).getSpawnLocation();
         }
         
         String[] args = loc.split(", ");
         
         Location location = new Location(Bukkit.getWorld(name), parseDouble(args[0]), parseDouble(args[1]), 
                 parseDouble(args[2]), parseFloat(args[3]), parseFloat(args[4]));
         
         return location;
     }
     
     private double parseDouble(String s)
     {
         try{
             return Double.parseDouble(s);
         }catch(NumberFormatException e){
             return 0.0;
         }
     }
     
     private float parseFloat(String s)
     {
         try{
             return Float.parseFloat(s);
         }catch(NumberFormatException e){
             return 0.0F;
         }
     }
     
 }
