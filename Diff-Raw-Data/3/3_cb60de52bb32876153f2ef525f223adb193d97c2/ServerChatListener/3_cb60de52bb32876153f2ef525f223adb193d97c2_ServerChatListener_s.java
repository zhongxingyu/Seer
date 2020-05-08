 package me.jamiemac262.ServerAIReWrite;
 
 import me.jamiemac262.ServerAIReWrite.function.WarnPlayer;
 import me.jamiemac262.ServerAIReWrite.function.Home;
 import me.jamiemac262.ServerAIReWrite.function.IsMuted;
 import me.jamiemac262.ServerAIReWrite.function.SendAIMessage;
 import me.jamiemac262.ServerAIReWrite.function.SendPrivateAIMessage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.logging.Level;
 import me.jamiemac262.ServerAIReWrite.function.GameTime;
 import me.jamiemac262.ServerAIReWrite.function.Gamemode;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 public class ServerChatListener implements Listener {
 
     public static ArrayList<String> FilterList = new ArrayList<String>();
     public static Array filterList;
     public static ServerAI plugin;
     static int sc = 0;
     public static boolean Smute;
     String tempMessage;
     String[] Playermessage;
     public FileConfiguration playerStat = null;
     public File playerStatFile = null;
     public AsyncPlayerChatEvent chat;
     public ServerChatListener(ServerAI instance) {
         System.out.println("[SAI] Loading the filter list");
         loadFilterList();
         plugin = instance;
     }
 
     @EventHandler
     public void onPlayerChat(AsyncPlayerChatEvent chat) throws FileNotFoundException, IOException {
         chat = chat;
         Player p = chat.getPlayer();
         boolean muted = IsMuted.isMuted(p);
         ChatColor RED = ChatColor.RED;
         ChatColor WHITE = ChatColor.WHITE;
         if (muted == true) {
             sendMuteMsg(p, RED + "[SAI] " + WHITE + "Nice try " + p.getName() + ", But you need to speak to a moderator about your language before I can let you speak again");
             chat.setCancelled(true);
         } else {
             PluginDescriptionFile pdffile = plugin.getDescription();
 
             tempMessage = chat.getMessage().toLowerCase();
             Playermessage = tempMessage.split(" ");
 
             if (ServerAI.filtering == true && containsSwear(Playermessage)) {
                 new WarnPlayer(p);
                 chat.setCancelled(true);
             }
             //start of SAI's responses
             //start of casual conversation
             if ((containsString(Playermessage, "hello") || containsString(Playermessage, "yo") || containsString(Playermessage, "hi") && ((containsString(Playermessage, "SAI"))))) {
                 new SendAIMessage(0.5, "Hello " + p.getName(), "Hi, How are you?", "Hello, nice to see someone cares about me...");
             } else if (containsString(Playermessage, "sai?")) {
                 new SendAIMessage(0.5, "yes? " + p.getDisplayName(), "can I help you?", "I do not compute, what do you want?");
             } else if ((containsString(Playermessage, "sai") && containsString(Playermessage, "how are you"))) {
                 new SendAIMessage(0.5, "My scans do not indicate any critical errors, Thank you for asking " + p.getName(), "Glad to see someone cares...", "I do not see any noticable errors in my system...");
 
             } else if ((containsString(Playermessage, "sai")) && containsString(Playermessage, "tell") && containsString(Playermessage, "about") && containsString(Playermessage, "yourself")) {
                 new SendPrivateAIMessage(p, 0.5, "My designation is Server Artificial Intelegence, however most players just call me SAI", "My designation is Server Artificial Intelegence, however most players just call me SAI", "My designation is Server Artificial Intelegence, however most players just call me SAI");
                 new SendPrivateAIMessage(p, 0.5, "i am operating on Version " + pdffile.getVersion(), "i am operating on Version " + pdffile.getVersion(), "i am operating on Version " + pdffile.getVersion());
                 new SendPrivateAIMessage(p, 0.5, "My Main creator is jamiemac262 however my coding consists of contributions from external sources - mainly bukkit.org", "My Main creator is jamiemac262 however my coding consists of contributions from external sources - mainly bukkit.org", "My Main creator is jamiemac262 however my coding consists of contributions from external sources - mainly bukkit.org");
                 new SendPrivateAIMessage(p, 0.5, "my memory functions and other features were developed by dmkiller11 and later updated and maintained by random8861 & jamiemac262.", "my memory functions and other features were developed by dmkiller11 and later updated and maintained by random8861 & jamiemac262.", "my memory functions and other features were developed by dmkiller11 and later updated and maintained by random8861 & jamiemac262.");
             }
             if ((containsString(Playermessage, "sai") && containsString(Playermessage, "cake"))) {
                 new SendAIMessage(0.5, "The cake is a lie", "The cake is a lie", "The cake is a lie");
             }
             if ((containsString(Playermessage, "sai") && containsString(Playermessage, "sun"))) {
 
                 new SendAIMessage(0.5, "Sunshine :)", "Don't ya just love the sun", "Sun baby");
 
             } //end of casual conversation
             //start of standard commands
             /*teleport
 		
              This code is a work in progress for the next release, i hope
 		
              if anybody reads this anc can see the problem with this section of code, please contace me on skype
 		
              jamiemac262
              or by e-mail jamiemac262@gmail.com
              thank you :)
              */ /*
              //could not pass event AsyncPlayerChatEvent <== this is the error you are getting?? yeh and a stack trace with it === Can you skype me te stacktrace pls? i will need to re-make the error lol..... on a server gimme 3 mins
              else if((containsString(Playermessage, "sai") && (containsString(Playermessage, "teleport")) || containsString(Playermessage, "tp"))){
              if(p.hasPermission("sai.tp")){
              if (containsString(Playermessage, "me")){
              Player victim = findPlayerInArray(Playermessage);
              //teleport the player to another player
              if(chat.getMessage().contains("me to")){
              Location victimL = victim.getLocation();
              p.teleport(victimL);
              new SendAIMessage(0.5, "Ok, sending you to" + victim.getDisplayName(), "Well if you're sure that's what you want to do. sending you to" + victim.getDisplayName(),"Here goes...");
              }
              else if(chat.getMessage().contains("to me")){
              Location victimT = p.getLocation();
              victim.teleport(victimT); // SHOULD WORK NOW, ONE PROBLEM!!!! " to me" will not be in an array that is split by spaces!!!!!!!!!ah....
              new SendAIMessage(0.5, "Ok, let me just....... done", "Well if you're sure that's what you want to do. sending " + victim.getDisplayName() + "to you","Here goes...");
              }
 				
              }
              else{
              Player victim = null;
              Player target = null;
              for(Player player : Bukkit.getOnlinePlayers()){//this part
              if(Arrays.asList(Playermessage).contains(player)){
              if(victim == null){
              victim = Bukkit.getPlayer(player.getName());
              }	
              else{	
              target = Bukkit.getPlayer(player.getName());//ohhhhhh....... player is already gotten is it not? in the getOnlinePlayer part
              }
              }
              }
              Location targetL = target.getLocation();
              victim.teleport(targetL);
              new SendAIMessage(0.5, "Ok, sending " + victim.getDisplayName() + "to" + target.getDisplayName(), "Well if you're sure that's what you want to do. sending " + victim.getDisplayName() + "to " + target.getDisplayName(),"Here goes...");
              }
              }
              }*/ // set day
             else if ((containsString(Playermessage, "sai") && containsString(Playermessage, "day"))) {
                 if (p.hasPermission("sai.time")) {
                     GameTime time = new GameTime(p);
                     time.day();
                 } else if (!p.hasPermission("sai.time")) {
 
                     noPerms();
                 }
             } else if ((containsString(Playermessage, "sai") && containsString(Playermessage, "home") && containsString(Playermessage, "set") == false)) {
                 if (p.hasPermission("sai.home")) {
                     Location home = Home.getHome(p);
                     p.teleport(home);
                     new SendAIMessage(0.5, "Sure thing " + p.getName() + "! welcome home", "ahh, home sweet home", "3... 2... uhhh, 1... WARP!");
                 } else if (!p.hasPermission("sai.home")) {
 
                     noPerms();
                 }
             } else if ((containsString(Playermessage, "sai") && containsString(Playermessage, "set") && containsString(Playermessage, "home"))) {
                 if (p.hasPermission("sai.home")) {
 
                     Home.setHome(p);
 
                     new SendAIMessage(0.5, "Sure thing " + p.getName() + "! welcome home", "ahh, home sweet home", "Done! should i arrange a house warming?");
                 } else if (!p.hasPermission("sai.home")) {
 
                     noPerms();
                 }
             }
             /*else if((containsString(Playermessage, "sai") && containsString(Playermessage, "set") && containsString(Playermessage, "warp"))){
              if(p.hasPermission("sai.warp.set")){
 			
              Warp.setWarp(Playermessage[3], p);
 				
              new SendAIMessage(0.5,"Sure thing " + p.getName() + "! welcome to " + Playermessage[3],"ahh, the sweet smell of Warping","Done! I'm just not sure why.");
              }
              else if(!p.hasPermission("sai.warp.set")){
 				
              noPerms();
              }
              }
              else if((containsString(Playermessage, "sai") && containsString(Playermessage, "warp") && containsString(Playermessage, "set") == false)){
              if(p.hasPermission("sai.warp")){
              Location home = Home.getHome(p);
              p.teleport(home);
              new SendAIMessage(0.5,"Sure thing " + p.getName() + "! welcome home","ahh, home sweet home","3... 2... uhhh, 1... WARP!");
              }
              else if(!p.hasPermission("sai.warp")){
 				
              noPerms();
              }
              }*/
             //set night
             if ((containsString(Playermessage, "sai") && containsString(Playermessage, "night"))) {
                 if (p.hasPermission("sai.time")) {
                     GameTime time = new GameTime(p);
                     time.night();
                 } else if (!p.hasPermission("sai.time")) {
                     noPerms();
                 }
             }
             if ((containsString(Playermessage, "sai") && containsString(Playermessage, "gamemode") && !containsString(Playermessage, "check"))) {
                 if (p.hasPermission("sai.gamemode")) {
                     Gamemode gm = new Gamemode(p);
                     gm.change();
                 } else {
                     noPerms();
                 }
             }
             if (containsString(Playermessage, "sai") && containsString(Playermessage, "me") && containsString(Playermessage, "spawn")) {
                 Location spawn = p.getWorld().getSpawnLocation();
                 p.teleport(spawn);
                 new SendAIMessage(0.5, "hmm, where did i put those co-ordiantes?", "tadaaa, welcome to the spawn area " + p.getDisplayName(), p.getDisplayName() + " is now at spawnn :D");
 
             }//end of SAI's standard commands
             //start of SAI's moderator commands
             if (containsString(Playermessage, "sai") && containsString(Playermessage, "check") && containsString(Playermessage, "gamemode")) {
                 if (p.hasPermission("sai.check")) {
                     chat.setCancelled(true);
 
                     new SendPrivateAIMessage(p, 0.5, "Warning: My AI has not finished learning this function","Warning: My AI has not finished learning this function","Warning: My AI has not finished learning this function");
                     Player target = findPlayerInArray(Playermessage);
                     new SendPrivateAIMessage(p, 0.5, "checking gamemode for" + target.getDisplayName(), "ok this will take a second", "ok let me check my memory circuits for " + target.getDisplayName());
                     Gamemode mode = new Gamemode(target);
                     String gamemode = mode.check();
                     new SendPrivateAIMessage(p, 0.5, "it seems that " + target.getDisplayName() + "is in" + gamemode, "it seems that " + target.getDisplayName() + "is in" + gamemode, "it seems that " + target.getDisplayName() + "is in" + gamemode);
                 } else {
                     noPerms();
                 }
             }
             if ((containsString(Playermessage, "sai") && containsString(Playermessage, "op"))) {
                 Player target = Bukkit.getPlayer(Playermessage[2]);
                 Player[] Players = Bukkit.getOnlinePlayers();
                 if (p.isOp()) {
                     if (Arrays.asList(Players).contains(target)) {
                         Bukkit.getPlayer(Playermessage[2]).setOp(true);
                         new SendAIMessage(0.5, "" + Bukkit.getPlayer(Playermessage[2]).getDisplayName() + " is now an op", "ok opping " + Bukkit.getPlayer(Playermessage[2]).getDisplayName(), Bukkit.getPlayer(Playermessage[2]).getDisplayName() + " you are now an op. Don't abuse this privalege");
                     }
                 } else if (Arrays.asList(Players).contains(target)) {
                     new SendAIMessage(0.5, "Player is not online, and has never been online", "are you sure that player exists?", "I have infinite knowledge and cannot find that player in my database!");
                 } else {
                     noPerms();
                 }
             }
 
             if ((containsString(Playermessage, "sai") && containsString(Playermessage, "update"))) {
                 if (p.hasPermission("sai.admin")) {
                     ServerAI.doUpdate();
                 }
             }
 
             if (containsString(Playermessage, "sai") && containsString(Playermessage, "kill")) {
                 if (p.hasPermission("sai.kill")) {
                     Player target = findPlayerInArray(Playermessage);
                     target.setHealth(0);
                     new SendAIMessage(0.5, "Oh dear " + target.getDisplayName() + " appears to be a bit on the dead side", "Oh dear " + target.getDisplayName() + " appears to be a bit on the dead side", "Oh dear " + target.getDisplayName() + " appears to be a bit on the dead side");
                 } else {
                     noPerms();
                 }
             }
 
             if (containsString(Playermessage, "sai") && containsString(Playermessage, "smite")) {
                 if (p.hasPermission("sai.kill")) {
                     Player target = findPlayerInArray(Playermessage);
                     for (World world : Bukkit.getWorlds()) {
                         world.strikeLightning(target.getLocation());
 
                     }
                     new SendAIMessage(0.5, "Oh dear " + target.getDisplayName() + " just got struck by lightning", "Oh dear " + target.getDisplayName() + " just got struck by lightning", "Oh dear " + target.getDisplayName() + " just got struck by lightning");
                 } else {
                     noPerms();
                 }
             }
 
             if (containsString(Playermessage, "sai") && containsString(Playermessage, "ban")) {
                 if (p.hasPermission("sai.ban")) {
                     Player target = findPlayerInArray(Playermessage);
                     target.setBanned(true);
                     target.kickPlayer("You have been banned!");
                 } else {
                     noPerms();
                 }
             }
             if (containsString(Playermessage, "sai") && containsString(Playermessage, "kick")) {
                 if (p.hasPermission("sai.kick")) {
                     Player target = findPlayerInArray(Playermessage);
                     target.kickPlayer("You have been kicked!");
                 } else {
                     noPerms();
                 }
             }
             if (containsString(Playermessage, "sai") && containsString(Playermessage, "set") && containsString(Playermessage, "spawn")) {
                 if (p.hasPermission("sai.set")) {
                     p.getWorld().setSpawnLocation(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
                     new SendAIMessage(0.5, "A new spawn has been set.", "A new spawn has been set.", "A new spawn has been set.");
                 } else {
                     noPerms();
                 }
             }
 
             if (containsString(Playermessage, "sai") && containsString(Playermessage, "stop") && containsString(Playermessage, "server")) {
                 if (p.isOp()) {
                     new SendAIMessage(0.2, "Attention all players! shutdown imminent", "WARNING: Immediate Shutdown", "Confirmed. All players prepare for immediate shutdown");
                     try {
                         Thread.sleep(1000L);
                     } catch (InterruptedException e) { //if thread sleep is interrupted for ANY reason, catch this error
                         e.printStackTrace();
                     } //gives 100 ms before telling server to shut itself down
                     new SendAIMessage(0.5, "BYE BYE", "NOOO THERE SHUTTING ME DOWN :(", "GOODBYE MY FRIENDS, TELL MY WIFE I LOVE HER, WAIT IM A ROBOT, DAMN");
                     try {
                         Thread.sleep(1000L);
                     } catch (InterruptedException e) { //if thread sleep is interrupted for ANY reason, catch this error
                         e.printStackTrace();
                     }
                     Bukkit.getServer().shutdown();
                 }
             }
             if (containsString(Playermessage, "sai") && containsString(Playermessage, "suck") && containsString(Playermessage, "you")) {
                 new SendAIMessage(0.5, "Well I <3 you too", "You suck more", "I don't care about your petit human insults");
             }
             /*if(containsString(Playermessage, "sai") && containsString(Playermessage, "rank")){
              boolean nstaff = false;
              if(StaffPermission.isModerator(p)){
              nstaff = true;
              new SendAIMessage(0.5, p.getDisplayName() + " is a Moderator", p.getDisplayName() + " is a Moderator", p.getDisplayName() + " is a Moderator");
              }
              if(StaffPermission.isAdminstrator(p)){
              nstaff = true;
              new SendAIMessage(0.5, p.getDisplayName() + " is an Administrator", p.getDisplayName() + " is an Administrator", p.getDisplayName() + " is an Administrator");
              }
              if(StaffPermission.isOwner(p)){
              nstaff = true;
              new SendAIMessage(0.5, p.getDisplayName() + " is an Owner", p.getDisplayName() + " is an Owner", p.getDisplayName() + " is an Owner");
              }
              if (nstaff = false){
              new SendAIMessage(0.5, p.getDisplayName() + " is Not Staff", p.getDisplayName() + " is Not Staff", p.getDisplayName() + " is Not Staff");
              }
              }*/
         }
         //end of SAI's moderator commands
 
         // THIS LINE BREAKS SAI DO NOT UNCOMMENT!!!
         //////else{new SendAIMessage(0.5, "Sorry, i do not know how to do this yet", "I do not understand", "I have not been taught this yet");}//end of SAI's responses
     }
 
     public static void loadFilterList() {
         try {
             File file = new File("plugins" + File.separator + "ServerAI" + File.separator + "Filter.txt");
             if (!file.exists()) {
                 file.createNewFile();
             } else if (file.exists()) {
                 BufferedReader bufferedreader = new BufferedReader(new FileReader(file));
                 String s;
                 System.out.println("[SAI] Filter list loaded");
                 for (int i = 1; (s = bufferedreader.readLine().toLowerCase()) != null; i++) {
                     FilterList.add(s);
                     System.out.println("[SAI] Loaded word number " + (i) + ": " + s);
                 }
                 bufferedreader.close(); //sort of important unless you want to leak data XD kk
             }
         } catch (Exception exception) {
         }
     }
 
     public static void sendMuteMsg(Player player, String message) {
         player.sendMessage(message);
     }
 
     //now when someone doesnt have perm just write noPerms(); instead of the lengthy AI statement :)
     public static SendAIMessage noPerms() {
         SendAIMessage message = new SendAIMessage(0.5, "I cant let you do that", "If i let you do that they will disable me", "no way. you cant do that!!");
         return message;
     }
 
     public boolean containsString(String[] read, String contains) {
         if (Arrays.asList(read).contains(contains)) {
             return true;
         } else {
             return false;
         }
     }
 
     public boolean containsSwear(String[] read) {
         boolean result = false;
         for (int i = 0; i <= FilterList.size(); i++) {
             if (Arrays.asList(read).contains(FilterList.toArray()[i])) {
                 result = true;
             } else {
                 result = false;
             }
         }
         return result;
     }
 
     public static Player findPlayerInArray(String[] playernames) {
         ServerAI.logger.log(Level.INFO, "Running PlayerName check");
         for (int i = 0; i < playernames.length; i++) {
             String string = playernames[i];
             ServerAI.logger.log(Level.INFO, string);
         }
         Player foundPlayer = null;
         Player[] player = Bukkit.getOnlinePlayers();
         
         for (int i = 0; i < playernames.length; i++) {
             ServerAI.logger.log(Level.INFO, playernames[i]);
             for (int j = 0; j < player.length; j++) {
                 String play = player[j].getDisplayName();
                 ServerAI.logger.log(Level.INFO, play);
                 if(play.equals(playernames[i])){
                 foundPlayer = player[j];
                 }
                 
             }          
         }
         
         return foundPlayer;
     }
 }
