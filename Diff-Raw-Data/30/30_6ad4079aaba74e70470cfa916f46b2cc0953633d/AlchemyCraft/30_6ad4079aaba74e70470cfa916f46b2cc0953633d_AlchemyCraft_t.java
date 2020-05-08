 package HanzVu;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /*TODO:
  *[ ]View recipes already discovered
  *[ ]View levels of distillation/transmutation
  *[ ]View exp needed for level up
  *[x]Add separate class for FileIO
  */
 public class AlchemyCraft extends JavaPlugin{
     public static final Logger log = Logger.getLogger("Minecraft");
     
     private final acPlayerListener playerListener = new acPlayerListener(this);
     private final acBlockListener blockListener = new acBlockListener(this);
     public final acRecipes recipes = new acRecipes(this);
     public final acLeveling leveling = new acLeveling(this);
     public final acFileIO fileio = new acFileIO(this);
     
     public HashMap<Location, String> stills = new HashMap<Location, String>();
     public HashMap<Location, String> furnaces = new HashMap<Location, String>();
     public HashMap<String, String> alchemicObject = new HashMap<String, String>();
     
     @Override
     public void onEnable(){
         log.info("AlchemyCraft version " + this.getDescription().getVersion() + " started.");
         
         
        
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Low, this);
         pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Low, this);
         pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Low, this);
         
         pm.registerEvent(Type.BLOCK_DISPENSE, blockListener, Priority.Low, this);
         pm.registerEvent(Type.BLOCK_PLACE, blockListener, Priority.Low, this);
         pm.registerEvent(Type.BLOCK_DAMAGE, blockListener, Priority.Low, this);
         
         
         //Adds recipes to obtain fire and water (unplaceable by default)
         ShapelessRecipe[] recipe = new ShapelessRecipe[2];
         recipe[0] = new ShapelessRecipe(new ItemStack(Material.FIRE, 1));
         recipe[0].addIngredient(Material.LAVA_BUCKET);
         recipe[1] = new ShapelessRecipe(new ItemStack(Material.WATER, 1));
         recipe[1].addIngredient(Material.WATER_BUCKET);
         getServer().addRecipe(recipe[0]);
         getServer().addRecipe(recipe[1]);
         
         ReadAlchemicObjects();
         
     }
     
     @Override
     public void onDisable(){
        StoreAlchemicObjects();
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
         Player player=null;
         if(sender instanceof Player) player = (Player)sender;
         if(player!=null){
             String name = player.getName();
             if(label.equalsIgnoreCase("still")){
                 if(alchemicObject.containsKey(name)) player.sendMessage("You are already creating a " + alchemicObject.get(name));
                 else{
                     alchemicObject.put(player.getName(), "still");
                     player.sendMessage(ChatColor.GOLD + "Still ready to be created");
                 }
             }
                 
 
             if(label.equalsIgnoreCase("furnace")){
                 if(alchemicObject.containsKey(name))
                     player.sendMessage("You are already creating a " + alchemicObject.get(name));
                 else{
                     alchemicObject.put(player.getName(), "furnace");
                     player.sendMessage(ChatColor.GOLD + "Furnace ready to be created");
                 }
             }
         }
         return true;
     }
     
     public void ReadAlchemicObjects(){
         File f = fileio.OpenFile("plugins\\Alchemy\\objects.txt");
         if(f != null){
            //the Reading function takes a String[] and returns a String[]
            //I want to send a String and return an int. This is my little work around
             String[] string = new String[1];
             string[0] = "number";
             String[] num = fileio.ReadFromFile(f, string);
             int number = 0;
             try{number = Integer.parseInt(num[0]);}
             catch(NumberFormatException ex){ 
                 log.info("Could not load alchemic objects!");
                 return;
             }
            //number = number of objects in the file
             
            //Each object has 5 parts:
            //t = type (furnace or still)
            //p = player (owner
            //x,y,z = location
             String[] Keys = new String[number*5];
            for(int i = 0; i<(number*5); i+=5){
                 Keys[i] = String.valueOf(i/5) + "t";
                 Keys[i+1] = String.valueOf(i/5) + "p";
                 Keys[i+2] = String.valueOf(i/5) + "x";
                 Keys[i+3] = String.valueOf(i/5) + "y";
                 Keys[i+4] = String.valueOf(i/5) + "z";
                 
                        
             }
             
             String[] data = fileio.ReadFromFile(f, Keys);
            for(int i=0; i<(number*5); i+=5){
                 int x,y,z = 0;
                     try{
                         x = Integer.parseInt(data[i+2]);
                         y = Integer.parseInt(data[i+3]);
                         z = Integer.parseInt(data[i+4]);
                     }
                    
                    catch(NumberFormatException ex){ 
                         continue;
                     }
                     
                 if(data[i].equals("furnace")){
                     furnaces.put(new Location(getServer().getWorlds().get(0),x,y,z), data[i+1]);
                 }
                 else{
                     stills.put(new Location(getServer().getWorlds().get(0),x,y,z), data[i+1]);
                 }
             }
         }
     }
     
     public void StoreAlchemicObjects(){
         File f = fileio.OpenFile("plugins\\Alchemy\\objects.txt");
         if(f != null){
             String[] Keys = new String[(stills.size() + furnaces.size())*5 +1];
             String[] Data = new String[Keys.length];
             
            //Makes a list of keys for each object.
            //Each object has 5 parts as described in the loading func above 
             for(int i =0; i<Keys.length-1; i+=5){
                 Keys[i] = String.valueOf(i/5) + "t";
                 Keys[i+1] = String.valueOf(i/5) + "p";
                 Keys[i+2] = String.valueOf(i/5) + "x";
                 Keys[i+3] = String.valueOf(i/5) + "y";
                 Keys[i+4] = String.valueOf(i/5) + "z";
             }
             
            
             int i =0;
             Set<Location> locs = stills.keySet();
             Iterator it = locs.iterator();
             while(it.hasNext()){
                 Location temp = (Location)it.next();
                 Data[i] = "still";
                 Data[i+1] = stills.get(temp);
                 Data[i+2] = String.valueOf(temp.getBlockX());
                 Data[i+3] = String.valueOf(temp.getBlockY());
                 Data[i+4] = String.valueOf(temp.getBlockZ());
                 i+=5;
             }
             
             locs = furnaces.keySet();
             it = locs.iterator();
             while(it.hasNext()){
                 Location temp = (Location)it.next();
                 Data[i] = "furnace";
                 Data[i+1] = furnaces.get(temp);
                 Data[i+2] = String.valueOf(temp.getBlockX());
                 Data[i+3] = String.valueOf(temp.getBlockY());
                 Data[i+4] = String.valueOf(temp.getBlockZ());
                 i+=5;
             }
         
            //The last key/data is the number of objects that are being stored (so the loading procedure knows how much to load)
             Keys[Keys.length -1] = "number";
             Data[Keys.length -1] = String.valueOf((Keys.length -1)/5);
             fileio.SaveToFile(f, Data, Keys, "Saving Alchemic Objects");
             
         }
     }
     
 }
