 package sirilog;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class Main extends JavaPlugin {
     
     @Override
     public void onEnable(){ 
         
         File sirilogdir =   new File("plugins" + File.separator + "SiriLog");
         File sirilog =      new File("plugins" + File.separator + "SiriLog" + File.separator + "sirilog.log");
         File sirilogconf =  new File("plugins" + File.separator + "SiriLog" + File.separator + "config.yml");
         
         if (!(sirilog.exists())){
             try {
                 sirilogdir.mkdir();
                 sirilog.createNewFile();
             } catch (IOException ex) {
                 System.out.println("Error creating the log file");
             }
         }
         
         if (!(sirilogconf.exists())){
             try {
                 sirilogconf.createNewFile();
             } catch (IOException ex) {
                 System.out.println("Error creating the config file");
             }
         }
         
         rConfig();
         
         if (Configs.config.getBoolean("log.breakblock")){
             getServer().getPluginManager().registerEvents(new Listenbreak(), this);
         }
         if (Configs.config.getBoolean("log.placeblock")){
             getServer().getPluginManager().registerEvents(new Listenput(), this);
         }
         if (Configs.config.getBoolean("log.playerinteractions")){
             getServer().getPluginManager().registerEvents(new Listenuse(), this);
         }
         if (Configs.config.getBoolean("log.openchest")){
             getServer().getPluginManager().registerEvents(new Listenchest(), this);
         }
         if (Configs.config.getBoolean("log.explosions")){
             getServer().getPluginManager().registerEvents(new Listenexplode(), this);
         }
         
         getServer().getPluginManager().registerEvents(new Listenwand(), this);
         
         getLogger().info("SiriLog started fine");
     }
  
     @Override
     public void onDisable() {
         sConfig();
         try {
             new Logsave().finalsave();
             getLogger().info("Queue saved");
         } catch (IOException ex) {
             getLogger().info("Error saving Queue");
         }
         getLogger().info("SiriLog clossed");
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 	if(cmd.getName().equalsIgnoreCase("sl")) {
             if (args.length >= 1){
                 switch (args[0]) {
                     case "save":
                         try {
                             new Logsave().finalsave();
                         } catch (IOException ex) {
                             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                         }
                         sender.sendMessage("Queue saved");
                         break;
                     case "queue":
                         sender.sendMessage(Logsave.logs.size() + " in queue");
                         break;
                     case "reload":
                         rConfig();
                         sender.sendMessage("Configurations reloaded");
                         break;
                     case "look":
                     {
                         if (!"database".equals(Configs.config.getString("recordings.mode"))){
                             sender.sendMessage("This command is only available on database mode");
                             break;
                         }
                         if (Configs.loc1 != null && Configs.loc2 != null){
                             ArrayList<String[]> list;
                             
                             String[] d1 = Configs.loc1.split(" ");
                             String[] d2 = Configs.loc2.split(" ");
                             
                             int x1 = Integer.parseInt(d1[0]);
                             int y1 = Integer.parseInt(d1[1]);
                             int z1 = Integer.parseInt(d1[2]);
                             String w1 = d1[3];
                             
                             int x2 = Integer.parseInt(d2[0]);
                             int y2 = Integer.parseInt(d2[1]);
                             int z2 = Integer.parseInt(d2[2]);
                             String w2 = d2[3];
                             
                             if (!w1.equals(w2)){
                                 sender.sendMessage("The worlds of the selections are different");
                                 break;
                             }
                             
                             if (x1 > x2){
                                 int xx1 = x1;
                                 x1 = x2;
                                 x2 = xx1;
                             }
                             if (y1 > y2){
                                 int yy1 = y1;
                                 y1 = y2;
                                 y2 = yy1;
                             }
                             if (z1 > z2){
                                 int zz1 = z1;
                                 z1 = z2;
                                 z2 = zz1;
                             }
                             
                             String q = "SELECT `id`,`date`,`user`,`action`,`block`,`x`,`y`,`z` FROM logs WHERE"
                                     + " `x` BETWEEN " + x1 + " AND " + x2 + " AND"
                                     + " `y` BETWEEN " + y1 + " AND " + y2 + " AND"
                                     + " `z` BETWEEN " + z1 + " AND " + z2 + " AND"
                                     + " `world` = " + w1;
                             try {
                                 list = new MySQL().getData(q);
                             } catch (SQLException ex) {
                                 Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                                 break;
                             }
                             Iterator l = list.iterator();
                             while(l.hasNext()){
                                 String[] da = (String[]) l.next();
                                sender.sendMessage(da[0] + " " + da[2] + " " + da[3] + " " + da[4] + " " + da[5] + "-" + da[6] + "-" + da[7]);
                             }
                         }else{
                             sender.sendMessage("You need to select two blocks with the Wand (wooden pickaxe)");
                         }
                         break;
                     }
                     case "lookid":
                     {
                         if (!"database".equals(Configs.config.getString("recordings.mode"))){
                             sender.sendMessage("This command is only available on database mode");
                             break;
                         }
                         if (args.length == 2){
                             ArrayList<String[]> list;
                             
                             String id = args[1];
                             
                             String q = "SELECT `id`,`date`,`user`,`action`,`block`,`x`,`y`,`z` FROM logs WHERE `id` = " + id;
                             try {
                                 list = new MySQL().getData(q);
                             } catch (SQLException ex) {
                                 Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                                 break;
                             }
                             Iterator l = list.iterator();
                             while(l.hasNext()){
                                 String[] da = (String[]) l.next();
                                sender.sendMessage(da[0] + " " + da[1] + " " + da[2] + " " + da[3] + " " + da[4] + " " + da[5] + "-" + da[6] + "-" + da[7]);
                             }
                         }else{
                             sender.sendMessage("Usage: /sl lookid [log's id]");
                         }
                         break;
                     }
                     case "newchest":
                     {
                         int x1 = Integer.parseInt(Configs.loc1.split(" ")[0]);
                         int y1 = Integer.parseInt(Configs.loc1.split(" ")[1]);
                         int z1 = Integer.parseInt(Configs.loc1.split(" ")[2]);
                         String w1 = Configs.loc1.split(" ")[3];
                         
                         int x2 = Integer.parseInt(Configs.loc2.split(" ")[0]);
                         int y2 = Integer.parseInt(Configs.loc2.split(" ")[1]);
                         int z2 = Integer.parseInt(Configs.loc2.split(" ")[2]);
                         String w2 = Configs.loc2.split(" ")[3];
                         
                         if(w1.equals(w2)){
                             ArrayList<Integer> list = new ArrayList<>();
                             int x;
                             int y;
                             int z;
                             if (x1 > x2){
                                 int xx1 = x1;
                                 x1 = x2;
                                 x2 = xx1;
                             }
                             if (y1 > y2){
                                 int yy1 = y1;
                                 y1 = y2;
                                 y2 = yy1;
                             }
                             if (z1 > z2){
                                 int zz1 = z1;
                                 z1 = z2;
                                 z2 = zz1;
                             }
                             for (x = x1; x <= x2; x++){
                                 for (y = y1; y <= y2; y++){
                                     for (z = z1; z <= z2; z++){
                                         int block = Bukkit.getWorld(w1).getBlockAt(x, y, z).getTypeId();
                                         if (!Configs.customchest.contains(block) && block != 0){
                                             Configs.customchest.add(block);
                                             list.add(block);
                                         }
                                     }
                                 }
                             }
                             String added = "";
                             int i;
                             for (i = 0;i < list.size();i++){
                                 added += list.get(i).toString() + " ";
                             }
                             if ("".equals(added)){
                                 sender.sendMessage("No added new block IDs");
                             }else{
                                 sender.sendMessage("Block IDs " + added + "added to custom chests");
                             }
                         }
                         sConfig();
                         break;
                     }
                     case "newinteraction":
                     {
                         int x1 = Integer.parseInt(Configs.loc1.split(" ")[0]);
                         int y1 = Integer.parseInt(Configs.loc1.split(" ")[1]);
                         int z1 = Integer.parseInt(Configs.loc1.split(" ")[2]);
                         String w1 = Configs.loc1.split(" ")[3];
                         
                         int x2 = Integer.parseInt(Configs.loc2.split(" ")[0]);
                         int y2 = Integer.parseInt(Configs.loc2.split(" ")[1]);
                         int z2 = Integer.parseInt(Configs.loc2.split(" ")[2]);
                         String w2 = Configs.loc2.split(" ")[3];
                         
                         if(w1.equals(w2)){
                             ArrayList<Integer> list = new ArrayList<>();
                             int x;
                             int y;
                             int z;
                             if (x1 > x2){
                                 int xx1 = x1;
                                 x1 = x2;
                                 x2 = xx1;
                             }
                             if (y1 > y2){
                                 int yy1 = y1;
                                 y1 = y2;
                                 y2 = yy1;
                             }
                             if (z1 > z2){
                                 int zz1 = z1;
                                 z1 = z2;
                                 z2 = zz1;
                             }
                             for (x = x1; x <= x2; x++){
                                 for (y = y1; y <= y2; y++){
                                     for (z = z1; z <= z2; z++){
                                         int block = Bukkit.getWorld(w1).getBlockAt(x, y, z).getTypeId();
                                         if (!Configs.customint.contains(block) && block != 0){
                                             Configs.customint.add(block);
                                             list.add(block);
                                         }
                                     }
                                 }
                             }
                             String added = "";
                             int i;
                             for (i = 0;i < list.size();i++){
                                 added += list.get(i).toString() + " ";
                             }
                             if ("".equals(added)){
                                 sender.sendMessage("No added new block IDs");
                             }else{
                                 sender.sendMessage("Block IDs " + added + "added to custom interactions");
                             }
                         }
                         sConfig();
                         break;
                     }
                     default:
                         sender.sendMessage("Unknown SiriLog command");
                         break;
                 }
             }else {
                 sender.sendMessage("SiriLog 0.0.4 running on this server!");
             }
             return true;
         }
         return false;
     }
     
     public void sConfig(){
         FileConfiguration conf = getConfig();
         conf.set("custom.chests", Configs.customchest);
         conf.set("custom.interactions", Configs.customint);
         saveConfig();
     }
     
     public void rConfig(){
         reloadConfig();
         FileConfiguration conf = getConfig();
         
         if(!conf.contains("recordings.showlogsaved")) {
             conf.set("recordings.showlogsaved", false);
         }
         if(!conf.contains("recordings.savelimit")) {
             conf.set("recordings.savelimit", 30);
         }
         if(!conf.contains("log.breakblock")) {
             conf.set("log.breakblock", true);
         }
         if(!conf.contains("log.placeblock")) {
             conf.set("log.placeblock", true);
         }
         if(!conf.contains("log.openchest")) {
             conf.set("log.openchest", true);
         }
         if(!conf.contains("log.explosions")) {
             conf.set("log.explosions", true);
         }
         if(!conf.contains("log.playerinteractions")) {
             conf.set("log.playerinteractions", true);
         }
         if(!conf.contains("recordings.mode")) {
             conf.set("recordings.mode", "file");
         }
         if(!conf.contains("recordings.mysql.server")) {
             conf.set("recordings.mysql.server", "localhost");
         }
         if(!conf.contains("recordings.mysql.database")) {
             conf.set("recordings.mysql.database", "sirilog");
         }
         if(!conf.contains("recordings.mysql.user")) {
             conf.set("recordings.mysql.user", "root");
         }
         if(!conf.contains("recordings.mysql.pass")) {
             conf.set("recordings.mysql.pass", "pass");
         }
         
         if(!conf.contains("custom.chests")) {
             List<Integer> c = null;
             conf.set("custom.chests", c);
             Configs.customchest = conf.getIntegerList("custom.chests");
         }else{
             Configs.customchest = conf.getIntegerList("custom.chests");
         }
         
         if(!conf.contains("custom.interactions")) {
             List<Integer> i = null;
             conf.set("custom.interactions", i);
             Configs.customint = conf.getIntegerList("custom.interactions");
         }else{
             Configs.customint = conf.getIntegerList("custom.interactions");
         }
         
         saveConfig();
         Configs.config = conf;
         
         rMySQL();
     }
     
     public void rMySQL(){
         if ("database".equals(Configs.config.getString("recordings.mode"))){
             MySQL mysql = new MySQL();
             if (mysql.connect()){
                 mysql.defaultDb();
             }
         }
     }
 }
