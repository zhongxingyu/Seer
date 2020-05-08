 package littlegruz.arpeegee;
 
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
 import java.util.Random;
 import java.util.StringTokenizer;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import littlegruz.arpeegee.commands.Begin;
 import littlegruz.arpeegee.commands.Display;
 import littlegruz.arpeegee.entities.RPGMagicPlayer;
 import littlegruz.arpeegee.entities.RPGMeleePlayer;
 import littlegruz.arpeegee.entities.RPGRangedPlayer;
 import littlegruz.arpeegee.entities.RPGSubClass;
 import littlegruz.arpeegee.listeners.EnemyDeaths;
 import littlegruz.arpeegee.listeners.EntityDamageEntity;
 import littlegruz.arpeegee.listeners.PlayerInteract;
 import littlegruz.arpeegee.listeners.PlayerJoin;
 import littlegruz.arpeegee.listeners.PlayerLevel;
 import littlegruz.arpeegee.listeners.PlayerProjectile;
 import littlegruz.arpeegee.listeners.PlayerRespawn;
 import littlegruz.arpeegee.listeners.PlayerSpeed;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Animals;
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
/* Create a custom RPG where the admin creates classes with the desired power
 * levels along with wizz-bang spells and attributes
 * 
 * If blade/archery/egg is high enough, chance (or increase chance) of crit DONE
  * Add spells which get opened up as the player levels DONE
  * 
  * Spells (so far): DONE
  * Heal
  * Adv. Heal
  * Lightning (single)
  * Lightning (area)
  * Fireball
  * Teleport
  * 
  * Diamond sword gives chance for critical hits DONE
  * Iron sword gives chance for dodge DONE
  * Rage mechanic gives increased sword bonuses, extra damage and speed DONE
  * Right click with sword will activate rage if rage meter is full DONE
  * 
  * Can fire bow quicker than egg but with less damage than egg DONE
  * Egg can occasionally explode DONE
  * A ranged armour equip can make user run faster DONE
  * Cooldowns for spells DONE
  * Levels and stat increase DONE (but no tested balancing)
  * Reduced damage taken by Warriors DONE
  * Weapon assignment DONE */
 
 /* And then for the sub-classes
  * Name [name]
  * Archery [modifier]
  * Blade [modifier]
  * Block [modifier]
  * Egg [modifier]
  * Spells [modifier]
  * 
  * e.g. For a Eggman sub-class it may look like this
  * Name Eggman
  * Archery 0.5
  * Blade 0.5
  * Block 0.5
  * Egg 5
  * Spells 0
  * 
  * The modifiers change the normal damage done by those weapons or activates
  * certain perks*/
 
 public class ArpeegeeMain extends JavaPlugin {
    private Logger log = Logger.getLogger("This is MINECRAFT!");
    private File meleePlayerFile;
    private File rangedPlayerFile;
    private File magicPlayerFile;
    private File subClassFile;
    private HashMap<String, RPGMeleePlayer> meleePlayerMap;
    private HashMap<String, RPGRangedPlayer> rangedPlayerMap;
    private HashMap<String, RPGMagicPlayer> magicPlayerMap;
    private HashMap<String, RPGSubClass> subClassMap;
    private HashMap<String, String> berserkMap;
    private HashMap<Entity, String> projMap;
    
    public void onEnable(){
       BufferedReader br;
       String input;
       StringTokenizer st;
       
       // Create the directory if needed
       new File(getDataFolder().toString()).mkdir();
       meleePlayerFile = new File(getDataFolder().toString() + "/meleePlayer.txt");
       rangedPlayerFile = new File(getDataFolder().toString() + "/rangedPlayer.txt");
       magicPlayerFile = new File(getDataFolder().toString() + "/magicPlayer.txt");
       subClassFile = new File(getDataFolder().toString() + "/subclasses.txt");
 
       subClassMap = new HashMap<String, RPGSubClass>();
       // Load up the sub-classes from file
       try{
          br = new BufferedReader(new FileReader(subClassFile));
          
          // Load sub-class file data into the sub-class HashMap
          while((input = br.readLine()) != null){
             String name;
             st = new StringTokenizer(input, " ");
             name = st.nextToken();
             subClassMap.put(name, new RPGSubClass(name,
                   Double.parseDouble(st.nextToken()),
                   Double.parseDouble(st.nextToken()),
                   Double.parseDouble(st.nextToken()),
                   Double.parseDouble(st.nextToken()),
                   Double.parseDouble(st.nextToken())));
          }
          br.close();
 
       }catch(FileNotFoundException e){
          log.info("No original Arpeegy sub-class file found. One will be created for you");
       }catch(IOException e){
          log.info("Error reading Arpeegy sub-class file");
       }catch(Exception e){
          log.info("Incorrectly formatted Arpeegy sub-class file");
       }
 
       meleePlayerMap = new HashMap<String, RPGMeleePlayer>();
       // Load up the melee players from file
       try{
          br = new BufferedReader(new FileReader(meleePlayerFile));
          
          // Load player file data into the player HashMap
          while((input = br.readLine()) != null){
             String name;
             RPGSubClass rpgSubClass;
             
             st = new StringTokenizer(input, " ");
             name = st.nextToken();
             rpgSubClass = new RPGSubClass(st.nextToken(),
                   Double.parseDouble(st.nextToken()),
                   Double.parseDouble(st.nextToken()),
                   Double.parseDouble(st.nextToken()),
                   Double.parseDouble(st.nextToken()),
                   Double.parseDouble(st.nextToken()));
             
             if(subClassMap.get(rpgSubClass.getName()) == null)
                log.warning("Player " + name + " has an unfound sub-class name. Please fix this before they login.");
             
             meleePlayerMap.put(name, new RPGMeleePlayer(name, rpgSubClass,
                   Integer.parseInt(st.nextToken()),
                   Integer.parseInt(st.nextToken())));
          }
          br.close();
          
       }catch(FileNotFoundException e){
          log.info("No original Arpeegy melee player file found. One will be created for you");
       }catch(IOException e){
          log.info("Error reading Arpeegy melee player file");
       }catch(Exception e){
          log.info("Incorrectly formatted Arpeegy melee player file");
       }
 
       rangedPlayerMap = new HashMap<String, RPGRangedPlayer>();
       // Load up the ranged players from file
       try{
          br = new BufferedReader(new FileReader(rangedPlayerFile));
          
          // Load ranged player file data into the ranged player HashMap
          while((input = br.readLine()) != null){
             String name;
             RPGSubClass rpgSubClass;
             
             st = new StringTokenizer(input, " ");
             name = st.nextToken();
             rpgSubClass = new RPGSubClass(st.nextToken(),
                   Double.parseDouble(st.nextToken()),
                   Double.parseDouble(st.nextToken()),
                   Double.parseDouble(st.nextToken()),
                   Double.parseDouble(st.nextToken()),
                   Double.parseDouble(st.nextToken()));
             
             if(subClassMap.get(rpgSubClass.getName()) == null)
                log.warning("Player " + name + " has an unfound sub-class name. Please fix this before they login.");
             
             rangedPlayerMap.put(name, new RPGRangedPlayer(name, rpgSubClass,
                   Integer.parseInt(st.nextToken())));
          }
          br.close();
          
       }catch(FileNotFoundException e){
          log.info("No original Arpeegy ranged player file found. One will be created for you");
       }catch(IOException e){
          log.info("Error reading Arpeegy ranged player file");
       }catch(Exception e){
          log.info("Incorrectly formatted Arpeegy ranged player file");
       }
 
       magicPlayerMap = new HashMap<String, RPGMagicPlayer>();
       // Load up the magic players from file
       try{
          br = new BufferedReader(new FileReader(magicPlayerFile));
          
          // Load magic player file data into the magic player HashMap
          while((input = br.readLine()) != null){
             String name;
             RPGSubClass rpgSubClass;
             
             st = new StringTokenizer(input, " ");
             name = st.nextToken();
             rpgSubClass = new RPGSubClass(st.nextToken(),
                   Double.parseDouble(st.nextToken()),
                   Double.parseDouble(st.nextToken()),
                   Double.parseDouble(st.nextToken()),
                   Double.parseDouble(st.nextToken()),
                   Double.parseDouble(st.nextToken()));
             
             if(subClassMap.get(rpgSubClass.getName()) == null)
                log.warning("Player " + name + " has an unfound sub-class name. Please fix this before they login.");
             
             magicPlayerMap.put(name, new RPGMagicPlayer(name, rpgSubClass,
                   Integer.parseInt(st.nextToken())));
          }
          br.close();
          
       }catch(FileNotFoundException e){
          log.info("No original Arpeegy magic player file found. One will be created for you");
       }catch(IOException e){
          log.info("Error reading Arpeegy magic player file");
       }catch(Exception e){
          log.info("Incorrectly formatted Arpeegy magic player file");
       }
 
       //Set up listeners
       getServer().getPluginManager().registerEvents(new EnemyDeaths(), this);
       getServer().getPluginManager().registerEvents(new EntityDamageEntity(this), this);
       getServer().getPluginManager().registerEvents(new PlayerInteract(this), this);
       getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
       getServer().getPluginManager().registerEvents(new PlayerLevel(this), this);
       getServer().getPluginManager().registerEvents(new PlayerProjectile(this), this);
       getServer().getPluginManager().registerEvents(new PlayerRespawn(this), this);
       getServer().getPluginManager().registerEvents(new PlayerSpeed(), this);
 
       getCommand("displaysubclass").setExecutor(new Display(this));
      getCommand("iammelee").setExecutor(new Begin(this));
      getCommand("iamranged").setExecutor(new Begin(this));
      getCommand("iammagic").setExecutor(new Begin(this));
 
       berserkMap = new HashMap<String, String>();
       projMap = new HashMap<Entity, String>();
 
       log.info("LittleRPG v0.5 beta enabled");
    }
    
    public void onDisable(){
 
       // Save ALL the data!
       BufferedWriter bw;
       try{
          bw = new BufferedWriter(new FileWriter(meleePlayerFile));
          
          // Save all melee players to file
          Iterator<Map.Entry<String, RPGMeleePlayer>> it = meleePlayerMap.entrySet().iterator();
          while(it.hasNext()){
             Entry<String, RPGMeleePlayer> player = it.next();
             bw.write(player.getKey() + " "
                   + player.getValue().getSubClassObject().getName() + " "
                   + Double.toString(player.getValue().getSubClassObject().getArch()) + " "
                   + Double.toString(player.getValue().getSubClassObject().getBlade()) + " "
                   + Double.toString(player.getValue().getSubClassObject().getBlock()) + " "
                   + Double.toString(player.getValue().getSubClassObject().getEgg()) + " "
                   + Double.toString(player.getValue().getSubClassObject().getSpell()) + " "
                   + Integer.toString(player.getValue().getLevel()) + " "
                   + Integer.toString(player.getValue().getRage()) + "\n");
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving Arpeegy melee players");
       }
       
       try{
          bw = new BufferedWriter(new FileWriter(rangedPlayerFile));
          
          // Save all ranged players to file
          Iterator<Map.Entry<String, RPGRangedPlayer>> it = rangedPlayerMap.entrySet().iterator();
          while(it.hasNext()){
             Entry<String, RPGRangedPlayer> player = it.next();
             bw.write(player.getKey() + " "
                   + player.getValue().getSubClassObject().getName() + " "
                   + Double.toString(player.getValue().getSubClassObject().getArch()) + " "
                   + Double.toString(player.getValue().getSubClassObject().getBlade()) + " "
                   + Double.toString(player.getValue().getSubClassObject().getBlock()) + " "
                   + Double.toString(player.getValue().getSubClassObject().getEgg()) + " "
                   + Double.toString(player.getValue().getSubClassObject().getSpell()) + " "
                   + Integer.toString(player.getValue().getLevel()) + "\n");
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving Arpeegy ranged players");
       }
       
       try{
          bw = new BufferedWriter(new FileWriter(magicPlayerFile));
          
          // Save all magic players to file
          Iterator<Map.Entry<String, RPGMagicPlayer>> it = magicPlayerMap.entrySet().iterator();
          while(it.hasNext()){
             Entry<String, RPGMagicPlayer> player = it.next();
             bw.write(player.getKey() + " "
                   + player.getValue().getSubClassObject().getName() + " "
                   + Double.toString(player.getValue().getSubClassObject().getArch()) + " "
                   + Double.toString(player.getValue().getSubClassObject().getBlade()) + " "
                   + Double.toString(player.getValue().getSubClassObject().getBlock()) + " "
                   + Double.toString(player.getValue().getSubClassObject().getEgg()) + " "
                   + Double.toString(player.getValue().getSubClassObject().getSpell()) + " "
                   + Integer.toString(player.getValue().getLevel()) + "\n");
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving Arpeegy magic players");
       }
 
       try{
          bw = new BufferedWriter(new FileWriter(subClassFile));
          
          // Save classes to file
          Iterator<Map.Entry<String, RPGSubClass>> it = subClassMap.entrySet().iterator();
          while(it.hasNext()){
             Entry<String, RPGSubClass> subClassIter = it.next();
             bw.write(subClassIter.getKey() + " "
                   + Double.toString(subClassIter.getValue().getArch()) + " "
                   + Double.toString(subClassIter.getValue().getBlade()) + " "
                   + Double.toString(subClassIter.getValue().getBlock()) + " "
                   + Double.toString(subClassIter.getValue().getEgg()) + " "
                   + Double.toString(subClassIter.getValue().getSpell()) + "\n");
          }
          bw.close();
       }catch(IOException e){
          log.info("Error saving Arpeegy sub-classes");
       }
       
       log.info("LittleRPG v0.5 beta disabled");
    }
 
    public HashMap<String, RPGMeleePlayer> getMeleePlayerMap() {
       return meleePlayerMap;
    }
 
    public HashMap<String, RPGRangedPlayer> getRangedPlayerMap() {
       return rangedPlayerMap;
    }
 
    public HashMap<String, RPGMagicPlayer> getMagicPlayerMap() {
       return magicPlayerMap;
    }
    
    public HashMap<String, RPGSubClass> getSubClassMap() {
       return subClassMap;
    }
    
    public HashMap<String, String> getBerserkMap() {
       return berserkMap;
    }
    
    public HashMap<Entity, String> getProjMap() {
       return projMap;
    }
    
    /* Returns true if the RNG smiles upon the user*/
    public boolean probabilityRoll(int percent){
       Random rand = new Random();
       
       if(rand.nextInt(100) <= percent)
          return true;
       else
          return false;
    }
    
    /* Checks if the given entity is an enemy*/
    public boolean isEnemy(Entity ent){
       if(ent instanceof Animals)
          return true;
       else if(ent instanceof Monster)
          return true;
       else if(ent instanceof EnderDragon)
          return true;
       return false;
    }
    
    /* Sets a task to turn off a ranged ability's cooldown*/
    public void giveCooldown(final Player playa, int delay){
       getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
          public void run() {
             rangedPlayerMap.get(playa.getName()).setEggReadiness(true);
             playa.getInventory().setItem(1, new ItemStack(Material.EGG,1));
          }
      }, delay * 20); // Multiplied by 20 to turn the delay time into seconds
    }
    
    /* Sets a task to turn off a melee ability to attack*/
    public void giveCooldown(final RPGMeleePlayer playa, int delay){
       getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
          public void run() {
             meleePlayerMap.get(playa.getName()).setAttackReadiness(true);
          }
      }, (delay * 20) - playa.getLevel());
    }
    
    /* Sets a task to turn off a magical ability's cooldown*/
    public void giveCooldown(final Player playa, final String type, double delay){
       getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
          public void run() {
             RPGMagicPlayer rpgPlaya = magicPlayerMap.get(playa.getName());
             ItemStack is = new ItemStack(351,1);
             
             if(type.compareTo("heal") == 0){
                rpgPlaya.setHealReadiness(true);
                is.setDurability((short)15);
                playa.getInventory().setItem(1, is);
             }
             else if(type.compareTo("advHeal") == 0){
                rpgPlaya.setAdvHealReadiness(true);
                is.setType(Material.BONE);
                playa.getInventory().setItem(5, is);
             }
             else if(type.compareTo("light") == 0){
                rpgPlaya.setLightningReadiness(true);
                is.setDurability((short)11);
                playa.getInventory().setItem(0, is);
             }
             else if(type.compareTo("advLight") == 0){
                rpgPlaya.setAdvLightningReadiness(true);
                is.setType(Material.BLAZE_ROD);
                playa.getInventory().setItem(6, is);
             }
             else if(type.compareTo("fire") == 0){
                rpgPlaya.setFireballReadiness(true);
                is.setDurability((short)1);
                playa.getInventory().setItem(2, is);
             }
             else if(type.compareTo("tele") == 0){
                rpgPlaya.setTeleportReadiness(true);
                is.setDurability((short)13);
                playa.getInventory().setItem(3, is);
             }
             else if(type.compareTo("baaa") == 0){
                rpgPlaya.setSheepReadiness(true);
                is.setType(Material.WHEAT);
                playa.getInventory().setItem(4, is);
             }
          }
      }, (long) (delay * 20));
    }
 }
