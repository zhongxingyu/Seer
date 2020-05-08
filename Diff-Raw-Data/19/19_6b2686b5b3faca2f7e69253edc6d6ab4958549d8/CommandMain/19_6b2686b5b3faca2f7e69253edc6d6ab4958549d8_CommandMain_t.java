 package littlegruz.autoruncommands;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Map.Entry;
 import java.util.UUID;
 import java.util.logging.Logger;
 
 import littlegruz.autoruncommands.listeners.CommandBlockListener;
 import littlegruz.autoruncommands.listeners.CommandPlayerListener;
 
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CommandMain extends JavaPlugin{
    Logger log = Logger.getLogger("This is MINECRAFT!");
    private final CommandPlayerListener playerListener = new CommandPlayerListener(this);
    private final CommandBlockListener blockListener = new CommandBlockListener(this);
    private HashMap<String, String> playerCommandMap;
    private HashMap<Location, String> blockCommandMap;
    private HashMap<String, String> commandMap;
    private File playerFile;
    private File commandFile;
    private File blockFile;
    private boolean placeBlock;
    private String blockCommand;
 
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
       log.info("Autorun Commands v2.0 is melting! MELTING!");
    }
 
    public void onEnable(){
       //Create the directory and files if needed
       new File(getDataFolder().toString()).mkdir();
       playerFile = new File(getDataFolder().toString() + "/playerList.txt");
       commandFile = new File(getDataFolder().toString() + "/commands.txt");
       blockFile = new File(getDataFolder().toString() + "/blockList.txt");
       
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
       
       placeBlock = false;
       blockCommand = "";
       
       //Set up the listeners
       PluginManager pm = this.getServer().getPluginManager();
       pm.registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, playerListener, Event.Priority.Normal, this);
       pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
       pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
       
       log.info("Autorun Commands v2.0 is enabled");
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
       if(commandLabel.compareToIgnoreCase("setclickcommand") == 0){
          if(args.length >= 1){
             String command;
             command = args[0];
             for(int i = 1; i < args.length; i++){
                command += " " + args[i];
             }
             if(playerCommandMap.get(sender.getName()) != null){
                playerCommandMap.remove(sender.getName());
             }
             playerCommandMap.put(sender.getName(), command);
             sender.sendMessage("Command association successful");
          }
          else
             sender.sendMessage("Not enough arguments");
       }
       else if(commandLabel.compareToIgnoreCase("removeclickcommand") == 0){
          if(playerCommandMap.get(sender.getName()) != null){
             playerCommandMap.remove(sender.getName());
             sender.sendMessage("Command removed");
          }
          else
             sender.sendMessage("You have no associated command");
       }
       else if(commandLabel.compareToIgnoreCase("displayclickcommand") == 0){
          if(playerCommandMap.get("GLOBAL") != null)
             sender.sendMessage("Your command in use is: /" + playerCommandMap.get("GLOBAL"));
          else if(playerCommandMap.get(sender.getName()) != null)
             sender.sendMessage("Your command in use is: /" + playerCommandMap.get(sender.getName()));
          else
             sender.sendMessage("You have no associated command");
       }
       else if(commandLabel.compareToIgnoreCase("blockcommand") == 0){
          if(args.length != 0){
             if(commandMap.get(args[0]) != null){
                blockCommand = args[0];
                placeBlock = true;
             }
             else{
                sender.sendMessage("No command found with that identifier");
                sender.sendMessage("Try \'/addacommand <identifier> <command> [args]\' first");
             }
          }
          else{
             sender.sendMessage("No autorun command given");
          }
       }else if(commandLabel.compareToIgnoreCase("addacommand") == 0){
          if(args.length > 1){
             String id;
             String command;
             id = args[0];
             command = args[1];
             for(int i = 2; i < args.length; i++){
                command += " " + args[i];
             }
             if(commandMap.put(id, command) != null){
                sender.sendMessage("Overwrote old command");
             }
             else
                sender.sendMessage("Command added");
          }
          else{
             sender.sendMessage("An identifier and command must be given");
          }
       }
       
       return true;
    }
 
    public HashMap<String, String> getPlayerClickMap(){
       return playerCommandMap;
    }
 
    public HashMap<Location, String> getBlockCommandMap(){
       return blockCommandMap;
    }
 
    public HashMap<String, String> getcommandMap(){
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
 
 }
