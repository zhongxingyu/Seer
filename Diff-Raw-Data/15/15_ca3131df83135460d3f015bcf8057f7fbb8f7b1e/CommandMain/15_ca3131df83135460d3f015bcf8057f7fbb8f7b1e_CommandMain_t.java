 package littlegruz.autoruncommands;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Map.Entry;
 import java.util.UUID;
 import java.util.logging.Logger;
 
 import littlegruz.autoruncommands.listeners.CommandBlockListener;
 import littlegruz.autoruncommands.listeners.CommandEntityListener;
 import littlegruz.autoruncommands.listeners.CommandPlayerListener;
 import littlegruz.autoruncommands.listeners.CommandServerListener;
 
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CommandMain extends JavaPlugin{
    Logger log = Logger.getLogger("This is MINECRAFT!");
    private HashMap<String, String> playerCommandMap;
    private HashMap<Location, String> blockCommandMap;
    private HashMap<String, String> commandMap;
    private HashMap<String, Location> playerPosMap;
    private HashMap<String, String> deathCommandMap;
    private HashMap<String, String> respawnCommandMap;
    private HashMap<String, Integer> repeatCommandMap;
    private HashMap<String, Integer> runningRepeatCommandMap;
    private File playerFile;
    private File commandFile;
    private File blockFile;
    private File deathFile;
    private File respawnFile;
    private File startupFile;
    private File repeatFile;
    private boolean placeBlock;
    private boolean startupDone;
    private String blockCommand;
    private String startupCommands;
 
    public void onDisable(){
       //Save player data
       try{
          BufferedWriter bw = new BufferedWriter(new FileWriter(playerFile));
          Iterator<Map.Entry<String, String>> it = playerCommandMap.entrySet().iterator();
          
          //Save the players and corresponding commands
          bw.write("<Player> <Command>\n");
          while(it.hasNext()){
             Entry<String, String> mp = it.next();
             bw.write(mp.getKey() + " " + mp.getValue() + "\n");
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving player command file");
       }
       
       //Save block data
       try{
           BufferedWriter bw = new BufferedWriter(new FileWriter(blockFile));
           Iterator<Map.Entry<Location, String>> it = blockCommandMap.entrySet().iterator();
           
           //Save the blocks and corresponding commands
           bw.write("<Block Location> <Command>\n");
           while(it.hasNext()){
              Entry<Location, String> mp = it.next();
              bw.write(mp.getKey().getWorld().getUID().toString() + " "
                      + Double.toString(mp.getKey().getX()) + " "
                      + Double.toString(mp.getKey().getY()) + " "
                      + Double.toString(mp.getKey().getZ()) + " "
                      + mp.getValue() + "\n");
           }
           bw.close();
        }catch(IOException e){
           log.info("Error saving block command file");
        }
       
       //Save player death data
       try{
          BufferedWriter bw = new BufferedWriter(new FileWriter(deathFile));
          Iterator<Map.Entry<String, String>> it = deathCommandMap.entrySet().iterator();
          
          //Save the players and corresponding commands
          bw.write("<Player> <Command>\n");
          while(it.hasNext()){
             Entry<String, String> mp = it.next();
             bw.write(mp.getKey() + " " + mp.getValue() + "\n");
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving player death command file");
       }
       
       //Save player respawn data
       try{
          BufferedWriter bw = new BufferedWriter(new FileWriter(respawnFile));
          Iterator<Map.Entry<String, String>> it = respawnCommandMap.entrySet().iterator();
          
          //Save the players and corresponding commands
          bw.write("<Player> <Command>\n");
          while(it.hasNext()){
             Entry<String, String> mp = it.next();
             bw.write(mp.getKey() + " " + mp.getValue() + "\n");
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving player respawn command file");
       }
       
       //Save server start up data
       try{
          BufferedWriter bw = new BufferedWriter(new FileWriter(startupFile));
          StringTokenizer st = new StringTokenizer(startupCommands, ":");
          
          while(st.countTokens() > 0){
             bw.write(st.nextToken() + "\n");
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving server start up command file");
       }
       
       //Save repeat command data
       try{
          BufferedWriter bw = new BufferedWriter(new FileWriter(repeatFile));
          Iterator<Map.Entry<String, Integer>> it = repeatCommandMap.entrySet().iterator();
          
          //Save the players and corresponding commands
          bw.write("<Command> <Interval>\n");
          while(it.hasNext()){
             Entry<String, Integer> mp = it.next();
             bw.write(mp.getKey() + " " + mp.getValue().toString() + "\n");
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving player repeat command file");
       }
       
       //Save command data
       try{
           BufferedWriter bw = new BufferedWriter(new FileWriter(commandFile));
           Iterator<Map.Entry<String, String>> it = commandMap.entrySet().iterator();
           
           //Save the blocks and corresponding commands
           bw.write("<Identifing name> <Command>\n");
           while(it.hasNext()){
              Entry<String, String> mp = it.next();
              bw.write(mp.getKey() + " " + mp.getValue() + "\n");
           }
           bw.close();
        }catch(IOException e){
           log.info("Error saving command file");
        }
       log.info("Autorun Commands v2.7 disabled");
    }
 
    public void onEnable(){
       //Create the directory and files if needed
       new File(getDataFolder().toString()).mkdir();
       playerFile = new File(getDataFolder().toString() + "/playerList.txt");
       commandFile = new File(getDataFolder().toString() + "/commands.txt");
       blockFile = new File(getDataFolder().toString() + "/blockList.txt");
       deathFile = new File(getDataFolder().toString() + "/deathList.txt");
       respawnFile = new File(getDataFolder().toString() + "/respawnList.txt");
       startupFile = new File(getDataFolder().toString() + "/startupCommands.txt");
       repeatFile = new File(getDataFolder().toString() + "/repeatList.txt");
       
       //Load the player file data
       playerCommandMap = new HashMap<String, String>();
       try{
          BufferedReader br = new BufferedReader(new FileReader(playerFile));
          StringTokenizer st;
          String input;
          String name;
          String command;
          while((input = br.readLine()) != null){
             if(input.compareToIgnoreCase("<Player> <Command>") == 0){
                continue;
             }
             st = new StringTokenizer(input, " ");
             name = st.nextToken();
             command = st.nextToken();
             playerCommandMap.put(name, command);
          }
          
       }catch(FileNotFoundException e){
          log.info("No original player command file, creating new one.");
       }catch(IOException e){
          log.info("Error reading player command file");
       }catch(Exception e){
          log.info("Incorrectly formatted player command file");
       }
       
       //Load the block file data
       blockCommandMap = new HashMap<Location, String>();
       try{
          BufferedReader br = new BufferedReader(new FileReader(blockFile));
          StringTokenizer st;
          String input;
          String command;
          Location loc = null;
          
          while((input = br.readLine()) != null){
             if(input.compareToIgnoreCase("<Block Location> <Command>") == 0){
                continue;
             }
             st = new StringTokenizer(input, " ");
             loc = new Location(getServer().getWorld(UUID.fromString(st.nextToken())), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()));
             command = st.nextToken();
             blockCommandMap.put(loc, command);
          }
          
       }catch(FileNotFoundException e){
          log.info("No original block command file, creating new one.");
       }catch(IOException e){
          log.info("Error reading block command file");
       }catch(Exception e){
          log.info("Incorrectly formatted block command file");
       }
       
       //Load the player death file data
       deathCommandMap = new HashMap<String, String>();
       try{
          BufferedReader br = new BufferedReader(new FileReader(deathFile));
          StringTokenizer st;
          String input;
          String name;
          String command;
          while((input = br.readLine()) != null){
             if(input.compareToIgnoreCase("<Player> <Command>") == 0){
                continue;
             }
             st = new StringTokenizer(input, " ");
             name = st.nextToken();
             command = st.nextToken();
             deathCommandMap.put(name, command);
          }
          
       }catch(FileNotFoundException e){
          log.info("No original player death command file, creating new one.");
       }catch(IOException e){
          log.info("Error reading player death command file");
       }catch(Exception e){
          log.info("Incorrectly formatted player death command file");
       }
       
       //Load the player respawn file data
       respawnCommandMap = new HashMap<String, String>();
       try{
          BufferedReader br = new BufferedReader(new FileReader(respawnFile));
          StringTokenizer st;
          String input;
          String name;
          String command;
          while((input = br.readLine()) != null){
             if(input.compareToIgnoreCase("<Player> <Command>") == 0){
                continue;
             }
             st = new StringTokenizer(input, " ");
             name = st.nextToken();
             command = st.nextToken();
             respawnCommandMap.put(name, command);
          }
          
       }catch(FileNotFoundException e){
          log.info("No original player respawn command file, creating new one.");
       }catch(IOException e){
          log.info("Error reading player respawn command file");
       }catch(Exception e){
          log.info("Incorrectly formatted player respawn command file");
       }
       
       //Load the start up data
       startupCommands = "";
       try{
          BufferedReader br = new BufferedReader(new FileReader(startupFile));
          String input;
          while((input = br.readLine()) != null){
             if(input.compareToIgnoreCase("<Command>") == 0){
                continue;
             }
             startupCommands += ":" + input;
          }
          
       }catch(FileNotFoundException e){
          log.info("No original start up command file, creating new one.");
       }catch(IOException e){
          log.info("Error reading start up command file");
       }catch(Exception e){
          log.info("Incorrectly formatted start up command file");
       }
       
       //Load the repeat command file data
       repeatCommandMap = new HashMap<String, Integer>();
       try{
          BufferedReader br = new BufferedReader(new FileReader(repeatFile));
          StringTokenizer st;
          String input;
          
          //Assumes that the name is only one token long
          while((input = br.readLine()) != null){
             if(input.compareToIgnoreCase("<Command> <Interval>") == 0){
                continue;
             }
             st = new StringTokenizer(input, " ");
             repeatCommandMap.put(st.nextToken(), Integer.parseInt(st.nextToken()));
          }
          
       }catch(FileNotFoundException e){
          log.info("No original repeat command file, creating new one.");
       }catch(IOException e){
          log.info("Error reading repeat command file");
       }catch(Exception e){
          log.info("Incorrectly formatted repeat command file");
       }
       
       //Load the command file data
       commandMap = new HashMap<String, String>();
       try{
          BufferedReader br = new BufferedReader(new FileReader(commandFile));
          StringTokenizer st;
          String input;
          String args;
          String name;
          
          //Assumes that the name is only one token long
          while((input = br.readLine()) != null){
             if(input.compareToIgnoreCase("<Identifing name> <Command>") == 0){
                continue;
             }
             st = new StringTokenizer(input, " ");
             name = st.nextToken();
             args = st.nextToken();
             while(st.hasMoreTokens()){
                args += " " + st.nextToken();
             }
             commandMap.put(name, args);
          }
          
       }catch(FileNotFoundException e){
          log.info("No original command file, creating new one.");
       }catch(IOException e){
          log.info("Error reading command file");
       }catch(Exception e){
          log.info("Incorrectly formatted command file");
       }
       
       //Start running the repeating tasks
       
       placeBlock = false;
       startupDone = false;
       blockCommand = "";
       playerPosMap = new HashMap<String, Location>();
       runningRepeatCommandMap = new HashMap<String, Integer>();
       
       //Set up the listeners
       getServer().getPluginManager().registerEvents(new CommandPlayerListener(this), this);
       getServer().getPluginManager().registerEvents(new CommandBlockListener(this), this);
       getServer().getPluginManager().registerEvents(new CommandEntityListener(this), this);
       getServer().getPluginManager().registerEvents(new CommandServerListener(this), this);
       
       log.info("Autorun Commands v2.7 is enabled");
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
       if(commandLabel.compareToIgnoreCase("setclickcommand") == 0){
          if(sender.hasPermission("autoruncommands.setclick")){
             if(args.length != 0){
                String command = args[0];
                String associate;
                
                if(args.length == 2)
                   associate = args[1];
                else
                   associate = sender.getName();
                
                if(commandMap.get(command) != null){
                   if(playerCommandMap.get(associate) != null){
                      playerCommandMap.remove(associate);
                   }
                   playerCommandMap.put(associate, command);
                   sender.sendMessage("Command association successful");
                }
                else if(commandMap.get(command + "[op]") != null){
                   if(playerCommandMap.get(associate) != null){
                      playerCommandMap.remove(associate);
                   }
                   playerCommandMap.put(associate, command + "[op]");
                   sender.sendMessage("OP command association successful");
                }
                else{
                   sender.sendMessage("No command found with that identifier");
                   sender.sendMessage("Try \'/addacommand <identifier> <command> [args]\' first");
                }
             }
             else
                sender.sendMessage("Not enough arguments");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       else if(commandLabel.compareToIgnoreCase("removeclickcommand") == 0){
          if(sender.hasPermission("autoruncommands.removeclick")){
             String associate;
             
             if(args.length == 1)
                associate = args[0];
             else
                associate = sender.getName();
             
             if(playerCommandMap.get(associate) != null){
                playerCommandMap.remove(associate);
                sender.sendMessage("Command removed");
             }
             else
                sender.sendMessage(associate + " has no associated command");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       else if(commandLabel.compareToIgnoreCase("removedeathcommand") == 0){
          if(sender.hasPermission("autoruncommands.removedeath")){
             String associate;
             
             if(args.length == 1)
                associate = args[0];
             else
                associate = sender.getName();
             
             if(deathCommandMap.get(associate) != null){
                deathCommandMap.remove(associate);
                sender.sendMessage("Command removed");
             }
             else
                sender.sendMessage(associate + " has no associated death command");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       else if(commandLabel.compareToIgnoreCase("removerespawncommand") == 0){
          if(sender.hasPermission("autoruncommands.removerespawn")){
             String associate;
             
             if(args.length == 1)
                associate = args[0];
             else
                associate = sender.getName();
             
             if(respawnCommandMap.get(associate) != null){
                respawnCommandMap.remove(associate);
                sender.sendMessage("Command removed");
             }
             else
                sender.sendMessage(associate + " has no associated respawn command");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       else if(commandLabel.compareToIgnoreCase("displayclickcommand") == 0){
          if(sender.hasPermission("autoruncommands.displayclick")){
             if(playerCommandMap.get("GLOBAL") != null)
                sender.sendMessage("Your command in use is: /" + commandMap.get(playerCommandMap.get("GLOBAL")));
             else if(playerCommandMap.get(sender.getName()) != null)
                sender.sendMessage("Your command in use is: /" + commandMap.get(playerCommandMap.get(sender.getName())));
             else
                sender.sendMessage("You have no associated command");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       else if(commandLabel.compareToIgnoreCase("displaydeathcommand") == 0){
          if(sender.hasPermission("autoruncommands.displaydeath")){
             if(deathCommandMap.get("GLOBAL") != null)
                sender.sendMessage("Your death command in use is: /" + commandMap.get(deathCommandMap.get("GLOBAL")));
             else if(deathCommandMap.get(sender.getName()) != null)
                sender.sendMessage("Your death command in use is: /" + commandMap.get(deathCommandMap.get(sender.getName())));
             else
                sender.sendMessage("You have no associated death command");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       else if(commandLabel.compareToIgnoreCase("displayrespawncommand") == 0){
          if(sender.hasPermission("autoruncommands.displayrespawn")){
             if(respawnCommandMap.get("GLOBAL") != null)
                sender.sendMessage("Your respawn command in use is: /" + commandMap.get(respawnCommandMap.get("GLOBAL")));
             else if(respawnCommandMap.get(sender.getName()) != null)
                sender.sendMessage("Your respawn command in use is: /" + commandMap.get(respawnCommandMap.get(sender.getName())));
             else
                sender.sendMessage("You have no associated respawn command");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       else if(commandLabel.compareToIgnoreCase("displaystartupcommands") == 0){
          if(sender.hasPermission("autoruncommands.displaystartup")){
             if(!startupCommands.isEmpty()){
                sender.sendMessage("The commands that run on start up are:");
                StringTokenizer st = new StringTokenizer(startupCommands, ":");
                while(st.countTokens() > 0)
                   sender.sendMessage(st.nextToken().replace("[op]", ""));
             }
             else
                sender.sendMessage("You have no commands set to run at start up");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       else if(commandLabel.compareToIgnoreCase("setcommandblock") == 0){
          if(sender.hasPermission("autoruncommands.setblock")){
             if(args.length != 0){
                if(commandMap.get(args[0]) != null){
                   blockCommand = args[0];
                   placeBlock = true;
                   sender.sendMessage("Right click with your fist to apply \'"
                   + commandMap.get(args[0]) + "\'");
                }
                else if(commandMap.get(args[0] + "[op]") != null){
                   blockCommand = args[0] + "[op]";
                   placeBlock = true;
                   sender.sendMessage("Right click with your fist to apply the OP command \'"
                   + commandMap.get(args[0] + "[op]").replace("[op]", "") + "\'");
                }
                else{
                   sender.sendMessage("No command found with that identifier");
                   sender.sendMessage("Try \'/addacommand <identifier> <command> [args]\' first");
                }
             }
             else
                sender.sendMessage("No autorun command given");
          }
       }
       else if(commandLabel.compareToIgnoreCase("addacommand") == 0){
          if(sender.hasPermission("autoruncommands.addcommand")){
             if(args.length > 1){
                String id;
                String command;
                if(commandMap.get(args[0] + "[op]") != null){
                   sender.sendMessage("An op command with that name already exists");
                   return true;
                }
                id = args[0];
                command = args[1];
                for(int i = 2; i < args.length; i++){
                   command += " " + args[i];
                }
                if(commandMap.put(id, command) != null)
                   sender.sendMessage("Overwrote old command");
                else
                   sender.sendMessage("Command added");
             }
             else
                sender.sendMessage("An identifier and command must be given");
          }
       }
       else if(commandLabel.compareToIgnoreCase("addopcommand") == 0){
          if(sender.hasPermission("autoruncommands.addopcommand")){
             if(args.length > 1){
                String id;
                String command;
                if(commandMap.get(args[0]) != null){
                   sender.sendMessage("A non-op command with that name already exists");
                   return true;
                }
                id = args[0] + "[op]";
                command = args[1];
                for(int i = 2; i < args.length; i++){
                   command += " " + args[i];
                }
                if(commandMap.put(id, command) != null)
                   sender.sendMessage("Overwrote old op command");
                else
                   sender.sendMessage("Op command added");
             }
             else
                sender.sendMessage("An identifier and command must be given");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       else if(commandLabel.compareToIgnoreCase("removeacommand") == 0){
          if(sender.hasPermission("autoruncommands.removecommand")){
             if(args.length == 1){
                boolean remove = false;
                String rmCommand = args[0];
                
                //Check if the command to be removed exists or is op
                if(commandMap.remove(rmCommand) != null)
                   remove = true;
                else if(commandMap.remove(rmCommand + "[op]") != null){
                   rmCommand += "[op]";
                   remove = true;
                }
                   
                if(remove){
                   ArrayList<String> names = new ArrayList<String>();
                   // Remove the command where it is associated with players
                   Iterator<Map.Entry<String, String>> it1 = playerCommandMap.entrySet().iterator();
                   while(it1.hasNext()){
                      Entry<String, String> mp1 = it1.next();
                      if(mp1.getValue().compareTo(rmCommand) == 0)
                         names.add(mp1.getKey());
                   }
                   for(int i = 0; i < names.size(); i++)
                      playerCommandMap.remove(names.get(i));
                   names.clear();
 
                   // Remove the command where it is associated with blocks
                   ArrayList<Location> places = new ArrayList<Location>();
                   Iterator<Map.Entry<Location, String>> it2 = blockCommandMap.entrySet().iterator();
                   while(it2.hasNext()){
                      Entry<Location, String> mp2 = it2.next();
                      if(mp2.getValue().compareTo(rmCommand) == 0)
                         places.add(mp2.getKey());
                   }
                   for(int i = 0; i < places.size(); i++)
                      blockCommandMap.remove(places.get(i));
                   places.clear();
                   
                   // Remove the command where it is associated with the death of players
                   it1 = deathCommandMap.entrySet().iterator();
                   while(it1.hasNext()){
                      Entry<String, String> mp1 = it1.next();
                      if(mp1.getValue().compareTo(rmCommand) == 0)
                         names.add(mp1.getKey());
                   }
                   for(int i = 0; i < names.size(); i++)
                      deathCommandMap.remove(names.get(i));
                   names.clear();
                   names.trimToSize();
                   
                   startupCommands = startupCommands.replace(":" + rmCommand, "");
                   sender.sendMessage("Command removed");
                }
                else
                   sender.sendMessage("No command was found with that identifer");
             }
             else
                sender.sendMessage("No command identifier given");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }else if(commandLabel.compareToIgnoreCase("setdeathcommand") == 0){
          if(sender.hasPermission("autoruncommands.setdeath")){
             if(args.length != 0){
                String command = args[0];
                String associate;
                
                if(args.length == 2){
                   if(getServer().getPlayer(args[1]) == null){
                      sender.sendMessage("No player found with that name");
                      return true;
                   }
                   associate = getServer().getPlayer(args[1]).getName();
                }
                else
                   associate = sender.getName();
                
                if(commandMap.get(command) != null){
                   if(deathCommandMap.get(associate) != null){
                      deathCommandMap.remove(associate);
                   }
                   deathCommandMap.put(associate, command);
                   sender.sendMessage("Command association successful");
                }
                else if(commandMap.get(command + "[op]") != null){
                   if(deathCommandMap.get(associate) != null){
                      deathCommandMap.remove(associate);
                   }
                   deathCommandMap.put(associate, command + "[op]");
                   sender.sendMessage("OP command association successful");
                }
                else{
                   sender.sendMessage("No command found with that identifier");
                   sender.sendMessage("Try \'/addacommand <identifier> <command> [args]\' first");
                }
             }
             else
                sender.sendMessage("Not enough arguments");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }else if(commandLabel.compareToIgnoreCase("setrespawncommand") == 0){
          if(sender.hasPermission("autoruncommands.setrespawn")){
             if(args.length != 0){
                String command = args[0];
                String associate;
                
                if(args.length == 2){
                   if(getServer().getPlayer(args[1]) == null){
                      sender.sendMessage("No player found with that name");
                      return true;
                   }
                   associate = getServer().getPlayer(args[1]).getName();
                }
                else
                   associate = sender.getName();
                
                if(commandMap.get(command) != null){
                   if(respawnCommandMap.get(associate) != null){
                      respawnCommandMap.remove(associate);
                   }
                   respawnCommandMap.put(associate, command);
                   sender.sendMessage("Command association successful");
                }
                else if(commandMap.get(command + "[op]") != null){
                   if(respawnCommandMap.get(associate) != null){
                      respawnCommandMap.remove(associate);
                   }
                   respawnCommandMap.put(associate, command + "[op]");
                   sender.sendMessage("OP command association successful");
                }
                else{
                   sender.sendMessage("No command found with that identifier");
                   sender.sendMessage("Try \'/addacommand <identifier> <command> [args]\' first");
                }
             }
             else
                sender.sendMessage("Not enough arguments");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       else if(commandLabel.compareToIgnoreCase("addstartupcommand") == 0){
          if(sender.hasPermission("autoruncommands.addstartup")){
             if(args.length != 0){
                String command = args[0];
                
                if(commandMap.get(command) != null)
                   addStartupCommand(sender, command);
                else if(commandMap.get(command + "[op]") != null)
                   addStartupCommand(sender, command);
                else{
                   sender.sendMessage("No command found with that identifier");
                   sender.sendMessage("Try \'/addacommand <identifier> <command> [args]\' first");
                }
             }
             else
                sender.sendMessage("Not enough arguments");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       else if(commandLabel.compareToIgnoreCase("removestartupcommand") == 0){
          if(sender.hasPermission("autoruncommands.removestartup")){
             if(args.length != 0){
                String command = args[0];
                
                if(commandMap.get(command) != null)
                   removeStartupCommand(sender, command);
                else if(commandMap.get(command + "[op]") != null)
                   removeStartupCommand(sender, command + "[op]");
                else{
                   sender.sendMessage("No command found with that identifier");
                   sender.sendMessage("Try \'/addacommand <identifier> <command> [args]\' first");
                }
             }
             else
                sender.sendMessage("Not enough arguments");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       else if(commandLabel.compareToIgnoreCase("addrepeatcommand") == 0){
          if(sender.hasPermission("autoruncommands.addrepeat")){
             if(args.length == 2){
                try{
                   final String command;
                   int interval, id;
                   
                   // Get the commands or stop if it does not exist
                   if(commandMap.get(args[0]) != null)
                     command = args[0];
                   else if(commandMap.get(args[0] + "[op]") != null)
                     command = args[0] + "[op]";
                   else{
                      sender.sendMessage("No command found with that identifier");
                      sender.sendMessage("Try \'/addacommand <identifier> <command> [args]\' first");
                      return true;
                   }
                   
                   interval = Integer.parseInt(args[1]);
                   
                   // If the command is found and is not already repeating, add it
                   // NOTE: All commands will be run by the console
                   if(runningRepeatCommandMap.get(args[0]) == null){
                      id = getServer().getScheduler().scheduleAsyncRepeatingTask(this,  new Runnable() {
 
                         public void run() {
                           getServer().dispatchCommand(getServer().getConsoleSender(), commandMap.get(command));
                         }
                      }, interval * 20, interval * 20);
 
                      repeatCommandMap.put(command, interval);
                      runningRepeatCommandMap.put(command, id);
                      sender.sendMessage("That command will repeat every "
                            + interval + " seconds from now");
                   }
                   else
                      sender.sendMessage("That command is already repeating");
                }catch(NumberFormatException e){
                   sender.sendMessage("Please enter an integer for the repeating inverval");
                }
             }
             else
                sender.sendMessage("Wrong number of arguments");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       else if(commandLabel.compareToIgnoreCase("removerepeatcommand") == 0){
          if(sender.hasPermission("autoruncommands.removerepeat")){
             if(args.length == 1){
                String command = args[0];
                
                if(commandMap.get(command) != null || commandMap.get(command + "[op]") != null){
                   getServer().getScheduler().cancelTask(runningRepeatCommandMap.get(command));
                   runningRepeatCommandMap.remove(command);
                   repeatCommandMap.remove(command);
                   sender.sendMessage("That command has now stopped repeating");
                }
                else{
                   sender.sendMessage("No command found with that identifier");
                   sender.sendMessage("Try \'/addacommand <identifier> <command> [args]\' first");
                }
             }
             else
                sender.sendMessage("Wrong number of arguments");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       
       return true;
    }
    
    public void removeStartupCommand(CommandSender sender, String command){
       if(startupCommands.contains(":" + command)){
          startupCommands = startupCommands.replace(":" + command, "");
          sender.sendMessage("Command removal successful");
       }
       else{
          sender.sendMessage("No command set with that identifier");
          sender.sendMessage("Check with \'/displaystartupcommands' first");
       }
    }
    
    public void addStartupCommand(CommandSender sender, String command){
       if(startupCommands.contains(command))
          sender.sendMessage("That command is already present");
       else{
          startupCommands += ":" + command;
          sender.sendMessage("Command association successful");
       }
    }
 
    public HashMap<String, String> getPlayerClickMap(){
       return playerCommandMap;
    }
 
    public HashMap<String, String> getPlayerDeathMap(){
       return deathCommandMap;
    }
 
    public HashMap<String, String> getPlayerRespawnMap(){
       return respawnCommandMap;
    }
 
    public HashMap<Location, String> getBlockCommandMap(){
       return blockCommandMap;
    }
 
    public HashMap<String, Integer> getRepeatMap(){
       return repeatCommandMap;
    }
 
    public HashMap<String, Integer> getRunningRepeatMap(){
       return runningRepeatCommandMap;
    }
 
    public HashMap<String, String> getCommandMap(){
       return commandMap;
    }
    
    public boolean isPlaceBlock(){
       return placeBlock;
    }
    
    public void setPlaceBlock(boolean placeBlock){
       this.placeBlock = placeBlock;
    }
    
    public String getBlockCommand(){
       return blockCommand;
    }
 
    public HashMap<String, Location> getPlayerPosMap() {
       return playerPosMap;
    }
 
    public String getStartupCommands() {
       return startupCommands;
    }
 
    public boolean isStartupDone() {
       return startupDone;
    }
 
    public void setStartupDone(boolean startupDone) {
       this.startupDone = startupDone;
    }
 }
