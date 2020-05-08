 package co.mcme.jobs;
 
 import co.mcme.jobs.commands.JobAdminCommand;
 import co.mcme.jobs.util.Util;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Sound;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.ChatPaginator;
 import org.bukkit.util.ChatPaginator.ChatPage;
 
 public final class Jobs extends JavaPlugin implements Listener {
 
     private final Logger log = Logger.getLogger("Minecraft");
     static Configuration conf;
     public static HashMap<String, Job> runningJobs = new HashMap();
     public static TreeMap<String, Job> notRunningJobs = new TreeMap();
     public static ArrayList<World> protected_worlds = new ArrayList();
     public static HashMap<Job, World> opened_worlds = new HashMap();
     public static HashMap<Job, Long> timedout_waiting = new HashMap();
     public static boolean debug = false;
     static File inactiveDir;
     static File activeDir;
 
     @Override
     public void onEnable() {
         inactiveDir = new File(Bukkit.getPluginManager().getPlugin("TheGaffer").getDataFolder().getPath() + System.getProperty("file.separator") + "jobs" + System.getProperty("file.separator") + "inactive");
         activeDir = new File(Bukkit.getPluginManager().getPlugin("TheGaffer").getDataFolder().getPath() + System.getProperty("file.separator") + "jobs" + System.getProperty("file.separator") + "active");
         if (!activeDir.exists()) {
             activeDir.mkdirs();
             Util.info("Did not find the active jobs directory");
         }
         if (!inactiveDir.exists()) {
             inactiveDir.mkdirs();
             Util.info("Did not find the inactive jobs directory");
         }
         getServer().getPluginManager().registerEvents(this, this);
         setupConfig();
         debug = getConfig().getBoolean("general.debug");
         getCommand("jobadmin").setExecutor(new JobAdminCommand());
         getServer().getPluginManager().registerEvents(this, this);
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             @Override
             public void run() {
                 Util.debug("Starting cleanup");
                 Cleanup.scheduledCleanup();
             }
         }, 0, (5 * 60) * 20);
     }
 
     @Override
     public void onDisable() {
         for (Job job : runningJobs.values()) {
             if (job.isDirty()) {
                 try {
                     job.writeToFile();
                 } catch (IOException ex) {
                     Logger.getLogger(Jobs.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
 
     public void setupConfig() {
         conf = getConfig();
         getConfig().options().copyDefaults(true);
         saveConfig();
         ArrayList<String> worlds_config = (ArrayList) getConfig().getStringList("protect_worlds");
         for (String name : worlds_config) {
             protected_worlds.add(Bukkit.getWorld(name));
         }
         loadJobs();
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if ((args.length > 0) && ((sender instanceof Player))) {
             Player player = (Player) sender;
             if (label.equalsIgnoreCase("job")) {
                 if (args.length > 0) {
                     if (args[0].equalsIgnoreCase("reload") && player.hasPermission("jobs.reload")) {
                         for (Job job : runningJobs.values()) {
                             try {
                                 job.writeToFile();
                             } catch (IOException ex) {
                                 Logger.getLogger(Jobs.class.getName()).log(Level.SEVERE, null, ex);
                             }
                         }
                         runningJobs = new HashMap();
                         player.sendMessage(ChatColor.GRAY + "Loaded " + co.mcme.jobs.files.Loader.loadActiveJobs() + " old job(s) from file.");
                         Util.info(runningJobs.size() + " of which are active.");
                     }
                     if (args[0].equalsIgnoreCase("write") && player.hasPermission("jobs.write")) {
                         if (runningJobs.containsKey(args[1])) {
                             Job writing = runningJobs.get(args[1]);
                             try {
                                 writing.writeToFile();
                             } catch (IOException ex) {
                                 Logger.getLogger(Jobs.class.getName()).log(Level.SEVERE, null, ex);
                             }
                         }
                     }
 
                     if (args.length == 2 && player.hasPermission("jobs.run")) {
                         if (args[0].equalsIgnoreCase("start")) {
                             if (args[1] != null) {
                                 String jobname = args[1];
                                 if (notRunningJobs.containsKey(jobname)) {
                                     storeJob(jobname, player.getName(), "reopen");
                                     player.sendMessage(ChatColor.GRAY + "Successfully reopend the " + ChatColor.AQUA + jobname + ChatColor.GRAY + " job.");
                                 } else {
                                     storeJob(jobname, player.getName(), "new");
                                     player.sendMessage(ChatColor.GRAY + "Successfully created the " + ChatColor.AQUA + jobname + ChatColor.GRAY + " job.");
                                 }
                             } else {
                                 player.sendMessage(ChatColor.RED + "You must provide a job name.");
                             }
                         }
                         if (args[0].equalsIgnoreCase("stop")) {
                             if (args[1] != null) {
                                 String jobname = args[1];
                                 if (runningJobs.containsKey(jobname)) {
                                     storeJob(jobname, player.getName(), "remove");
                                     player.sendMessage(ChatColor.GRAY + "Successfully closed the " + ChatColor.AQUA + jobname + ChatColor.GRAY + " job.");
                                 } else {
                                     player.sendMessage(ChatColor.RED + "No job found by that name.");
                                 }
                             } else {
                                 player.sendMessage(ChatColor.RED + "You must provide a job name.");
                             }
                         }
                     }
                     if (args[0].equalsIgnoreCase("check")) {
                         if (player.hasPermission("jobs.check")) {
                             if (runningJobs.size() > 0) {
                                 StringBuilder out = new StringBuilder();
                                 out.append(ChatColor.GRAY).append("Running Jobs:");
                                 for (String jobName : runningJobs.keySet()) {
                                     Job job = runningJobs.get(jobName);
                                     out.append("\n").append(ChatColor.AQUA).append(jobName).append(ChatColor.GRAY).append(" with ").append(job.getAdmin().getName()).append(" (").append(job.getWorkers().size()).append(")");
                                 }
                                 player.sendMessage(out.toString());
                             } else {
                                 player.sendMessage(ChatColor.GRAY + "No jobs currently running.");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "You don't have permission.");
                         }
                     }
                     if (args[0].equalsIgnoreCase("join")) {
                         if (player.hasPermission("jobs.join")) {
                             if (runningJobs.size() > 0) {
                                 if (args.length > 1) {
                                     if (runningJobs.containsKey(args[1])) {
                                         Job jobToJoin = runningJobs.get(args[1]);
                                         if (jobToJoin.addWorker(player)) {
                                             player.sendMessage(ChatColor.GRAY + "You have joined the job " + ChatColor.AQUA + jobToJoin.getName());
                                         } else {
                                             player.sendMessage(ChatColor.RED + "You cannot be added to that job.");
                                         }
                                     } else {
                                         player.sendMessage(ChatColor.RED + "No job ruuning by the name of `" + args[1] + "`");
                                     }
                                 } else {
                                     player.sendMessage(ChatColor.RED + "You must provide the name of the job you would like to join.");
                                 }
                             } else {
                                 player.sendMessage(ChatColor.RED + "No jobs currently running.");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "You do not have permission.");
                         }
                     }
                     if (args[0].equalsIgnoreCase("warpto")) {
                         if (runningJobs.size() > 0) {
                             if (args.length > 1) {
                                 if (runningJobs.containsKey(args[1])) {
                                     Job jobToJoin = runningJobs.get(args[1]);
                                     player.teleport(jobToJoin.getWarp());
                                     player.sendMessage(ChatColor.GRAY + "Warped to " + ChatColor.AQUA + jobToJoin.getName());
                                 } else {
                                     player.sendMessage(ChatColor.RED + "No job ruuning by the name of `" + args[1] + "`");
                                 }
                             } else {
                                 player.sendMessage(ChatColor.RED + "You must provide the name of the job you would like to warp to.");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "No jobs currently running.");
                         }
                     }
                     if (args[0].equalsIgnoreCase("info")) {
                         if (args.length > 1) {
                             if (runningJobs.containsKey(args[1])) {
                                 Job jobToJoin = runningJobs.get(args[1]);
                                 player.sendMessage(getJobInfo(jobToJoin));
                             } else if (notRunningJobs.containsKey(args[1])) {
                                 Job jobToJoin = notRunningJobs.get(args[1]);
                                 player.sendMessage(getJobInfo(jobToJoin));
                             } else {
                                 player.sendMessage(ChatColor.RED + "No job found by the name of `" + args[1] + "`");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "What job would you like to get info on?");
                         }
                     }
                     if (args[0].equalsIgnoreCase("archive")) {
                         if (player.hasPermission("jobs.check")) {
                             if (notRunningJobs.size() > 0) {
                                 StringBuilder out = new StringBuilder();
                                 int pageNum = 1;
                                 boolean first = true;
                                 for (String jobName : notRunningJobs.keySet()) {
                                     Job job = notRunningJobs.get(jobName);
                                     if (!first) {
                                         out.append("\n");
                                     }
                                     out.append(ChatColor.AQUA).append(jobName).append(ChatColor.GRAY).append(" with ").append(job.getAdmin().getName()).append(" (").append(job.getWorkers().size()).append(")");
                                     if (first) {
                                         first = false;
                                     }
                                 }
                                 if (args.length > 1) {
                                     pageNum = Integer.valueOf(args[1]);
                                 }
                                 ChatPage page = ChatPaginator.paginate(out.toString(), pageNum, ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH, 8);
                                 player.sendMessage(ChatColor.AQUA + "Job Archive Page: " + page.getPageNumber() + " of " + page.getTotalPages());
                                 player.sendMessage(page.getLines());
                             } else {
                                player.sendMessage(ChatColor.GRAY + "No jobs currently running.");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "You don't have permission.");
                         }
                     }
                 } else {
                     return false;
                 }
             }
         }
         return true;
     }
 
     public void storeJob(String jobname, String admin, String status) {
         Player p = Bukkit.getPlayer(admin);
         if (status.equalsIgnoreCase("new")) {
             Location adminloc = Bukkit.getPlayer(admin).getLocation();
             int newx = (int) adminloc.getX();
             int newy = (int) adminloc.getY();
             int newz = (int) adminloc.getZ();
             adminloc.setX(newx);
             adminloc.setY(newy);
             adminloc.setZ(newz);
             Job newjob = new Job(jobname, admin, true, adminloc, adminloc.getWorld().getName());
             runningJobs.put(jobname, newjob);
             try {
                 newjob.writeToFile();
             } catch (IOException ex) {
                 Logger.getLogger(Jobs.class.getName()).log(Level.SEVERE, null, ex);
             }
             opened_worlds.put(newjob, adminloc.getWorld());
             for (Player targetP : Bukkit.getOnlinePlayers()) {
                 targetP.sendMessage(ChatColor.GRAY + admin + " has started a new job called '" + jobname + "'");
                 targetP.playSound(targetP.getLocation(), Sound.WITHER_DEATH, 1, 100);
             }
         }
         if (status.equalsIgnoreCase("reopen")) {
             if (notRunningJobs.containsKey(jobname)) {
                 Job oldjob = notRunningJobs.get(jobname);
                 oldjob.setAdmin(Bukkit.getOfflinePlayer(admin));
                 oldjob.setStatus(true);
                 enableJob(oldjob);
             }
         }
         if (status.equalsIgnoreCase("remove")) {
             if (runningJobs.containsKey(jobname)) {
                 Job oldjob = runningJobs.get(jobname);
                 oldjob.setStatus(false);
                 notRunningJobs.put(oldjob.getName(), oldjob);
                 runningJobs.remove(jobname);
                 opened_worlds.remove(oldjob);
                 try {
                     oldjob.writeToFile();
                 } catch (IOException ex) {
                     Logger.getLogger(Jobs.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
 
     private void loadJobs() {
         Util.debug("Loaded " + co.mcme.jobs.files.Loader.loadActiveJobs() + " active jobs from file.");
         for (Job job : runningJobs.values()) {
             getServer().getPluginManager().registerEvents(job, this);
         }
         Util.debug("Loaded " + co.mcme.jobs.files.Loader.loadInactiveJobs() + " inactive jobs from file.");
     }
 
     @EventHandler
     public void onPlace(BlockPlaceEvent event) {
         World happenedin = event.getBlock().getWorld();
         Util.debug("Place event fired in " + happenedin.getName());
         for (World w : protected_worlds) {
             Util.debug(w.getName() + " is protected!");
         }
         if (protected_worlds.contains(happenedin)) {
             Util.debug("Place event is in protected world!");
             if (opened_worlds.containsValue(happenedin)) {
                 Util.debug("Place event is is in opened world");
                 OfflinePlayer toCheck = Bukkit.getOfflinePlayer(event.getPlayer().getName());
                 for (Job job : opened_worlds.keySet()) {
                     if (job.getWorld().equals(happenedin)) {
                         if (toCheck.getPlayer().hasPermission("jobs.ignorestatus")) {
                             event.setCancelled(false);
                         } else {
                             event.setCancelled(!job.isWorking(toCheck));
                         }
                     }
                 }
             } else {
                 Util.debug("Protected world is not open!");
                 event.setCancelled(!event.getPlayer().hasPermission("jobs.ignorestatus"));
             }
         }
     }
 
     @EventHandler
     public void onBreak(BlockBreakEvent event) {
         World happenedin = event.getBlock().getWorld();
         Util.debug("Break event fired in " + happenedin.getName());
         for (World w : protected_worlds) {
             Util.debug(w.getName() + " is protected!");
         }
         if (protected_worlds.contains(happenedin)) {
             Util.debug("Break event is in protected world!");
             if (opened_worlds.containsValue(happenedin)) {
                 Util.debug("Break event is in opened world");
                 OfflinePlayer toCheck = Bukkit.getOfflinePlayer(event.getPlayer().getName());
                 for (Job job : opened_worlds.keySet()) {
                     if (job.getWorld().equals(happenedin)) {
                         if (toCheck.getPlayer().hasPermission("jobs.ignorestatus")) {
                             event.setCancelled(false);
                         } else {
                             event.setCancelled(!job.isWorking(toCheck));
                         }
                     }
                 }
             } else {
                 Util.debug("Protected world is not open!");
                 event.setCancelled(!event.getPlayer().hasPermission("jobs.ignorestatus"));
             }
         }
     }
 
     @EventHandler
     public void onJoin(PlayerJoinEvent event) {
         if (runningJobs.size() > 0) {
             event.getPlayer().sendMessage(ChatColor.GRAY + "There is a job running! Use /job check to find out what it is!");
         }
     }
 
     private String getJobInfo(Job job) {
         StringBuilder out = new StringBuilder();
         out.append(ChatColor.GRAY).append(job.getName()).append("\n");
         out.append("Started by: ").append(ChatColor.AQUA).append(job.getAdmin().getName()).append("\n").append(ChatColor.GRAY);
         out.append("Started on: ").append(ChatColor.AQUA).append(new Date(job.getRunningSince()).toGMTString()).append("\n").append(ChatColor.GRAY);
         out.append("Location: ").append(ChatColor.AQUA).append(job.getWorld().getName()).append(" (x: ").append(job.getWarp().getX()).append(", y: ").append(job.getWarp().getY()).append(", z: ").append(job.getWarp().getZ()).append(")").append("\n").append(ChatColor.GRAY);
         out.append("Stored in: ").append(ChatColor.AQUA).append(job.getFileName()).append("\n").append(ChatColor.GRAY);
         String status = (job.getStatus()) ? ChatColor.GREEN + "OPEN" : ChatColor.RED + "CLOSED";
         out.append("Status: ").append(status);
         return out.toString();
     }
 
     public String jobExists(String name) {
         String out = "never";
         if (runningJobs.containsKey(name)) {
             out = "active";
         }
         if (notRunningJobs.containsKey(name)) {
             out = "dormant";
         }
         if (!(notRunningJobs.containsKey(name) || runningJobs.containsKey(name))) {
             out = "never";
         }
         return out;
     }
 
     static void scheduleAdminTimeout(Job job) {
         Long time = System.currentTimeMillis();
         timedout_waiting.put(job, time);
     }
 
     public static void disableJob(Job job) {
         if (runningJobs.containsValue(job)) {
             job.setStatus(false);
             notRunningJobs.put(job.getName(), job);
             runningJobs.remove(job.getName());
             opened_worlds.remove(job);
             try {
                 job.writeToFile();
             } catch (IOException ex) {
                 Logger.getLogger(Jobs.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     public static void enableJob(Job job) {
         if (notRunningJobs.containsValue(job)) {
             job.setStatus(true);
             runningJobs.put(job.getName(), job);
             notRunningJobs.remove(job.getName());
             opened_worlds.put(job, job.getWorld());
             try {
                 job.writeToFile();
             } catch (IOException ex) {
                 Logger.getLogger(Jobs.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     public static File getInactiveDir() {
         return inactiveDir;
     }
 
     public static File getActiveDir() {
         return activeDir;
     }
 }
