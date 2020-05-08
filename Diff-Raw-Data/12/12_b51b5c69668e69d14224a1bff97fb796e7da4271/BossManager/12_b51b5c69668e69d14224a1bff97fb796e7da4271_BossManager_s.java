 package com.ne0nx3r0.rareitemhunter.boss;
 
 import com.ne0nx3r0.rareitemhunter.RareItemHunter;
 import com.ne0nx3r0.rareitemhunter.boss.skill.Blink;
 import com.ne0nx3r0.rareitemhunter.boss.skill.Burst;
 import com.ne0nx3r0.rareitemhunter.boss.skill.Disarm;
 import com.ne0nx3r0.rareitemhunter.boss.skill.Disorient;
 import com.ne0nx3r0.rareitemhunter.boss.skill.GreaterBurst;
 import com.ne0nx3r0.rareitemhunter.boss.skill.JumpAttack;
 import com.ne0nx3r0.rareitemhunter.boss.skill.LightningBolt;
 import com.ne0nx3r0.rareitemhunter.boss.skill.LightningStorm;
 import com.ne0nx3r0.rareitemhunter.boss.skill.PoisonDart;
 import com.ne0nx3r0.rareitemhunter.boss.skill.Pull;
 import com.ne0nx3r0.rareitemhunter.boss.skill.ShootArrow;
 import com.ne0nx3r0.rareitemhunter.boss.skill.ShootFireball;
 import com.ne0nx3r0.rareitemhunter.boss.skill.SpawnCaveSpider;
 import com.ne0nx3r0.rareitemhunter.boss.skill.SpawnCreeper;
 import com.ne0nx3r0.rareitemhunter.boss.skill.SpawnSilverfish;
 import com.ne0nx3r0.rareitemhunter.boss.skill.SpawnSkeleton;
 import com.ne0nx3r0.rareitemhunter.boss.skill.SpawnSpider;
 import com.ne0nx3r0.rareitemhunter.boss.skill.SpawnZombie;
 import com.ne0nx3r0.rareitemhunter.boss.skill.SpawnZombiePig;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.UUID;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Level;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.inventory.EntityEquipment;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class BossManager
 {
     private final RareItemHunter plugin;
     
     Map<String,BossTemplate> bossTemplates;
     
     Map<String,BossEggSpawnPoint> spawnPoints;
     public CopyOnWriteArrayList<BossEgg> bossEggs;
     List<Boss> activeBosses;
     
     private saveFileManager saveManager;
     
     public BossManager(RareItemHunter plugin)
     {
         this.plugin = plugin;
         
         bossEggs = new CopyOnWriteArrayList<>();
         
         activeBosses = new ArrayList<>();
         
         spawnPoints = new HashMap<>();
         
         List<BossSkill> availableBossSkills = new ArrayList<>();
         
         availableBossSkills.add(new Burst());
         availableBossSkills.add(new GreaterBurst());
         availableBossSkills.add(new Disarm());
         availableBossSkills.add(new ShootArrow());
         availableBossSkills.add(new ShootFireball());
         //availableBossSkills.add(new FakeWeb());-> Seems to be sort of... crashing the server. Orefuscator conflict, possibly...
         availableBossSkills.add(new Blink());
         availableBossSkills.add(new JumpAttack());
         availableBossSkills.add(new SpawnZombiePig());
         availableBossSkills.add(new SpawnSkeleton());
         availableBossSkills.add(new SpawnZombie());
         availableBossSkills.add(new SpawnCreeper());
         availableBossSkills.add(new SpawnSpider());
         availableBossSkills.add(new SpawnCaveSpider());
         availableBossSkills.add(new Pull());
         availableBossSkills.add(new SpawnSilverfish());
         availableBossSkills.add(new PoisonDart());
         availableBossSkills.add(new Disorient());
         availableBossSkills.add(new LightningStorm());
         availableBossSkills.add(new LightningBolt());
 
         bossTemplates = new HashMap<>();
 
         File bossesFile = new File(plugin.getDataFolder(),"bosses.yml");
 
         if(!bossesFile.exists())
         {
             plugin.copy(plugin.getResource("bosses.yml"),bossesFile);
         }
         
         FileConfiguration bossesYml = YamlConfiguration.loadConfiguration(bossesFile);
         
         for(Iterator<String> it = bossesYml.getKeys(false).iterator(); it.hasNext();)
         {
             String sBossName = it.next();
             
             String sType = bossesYml.getString(sBossName+"."+"type");
 
             EntityType entityType = EntityType.fromName(sType);
             
             if(entityType == null)
             {
                 plugin.getLogger().log(Level.WARNING,
                     "{0} has an invalid entity type ''{1}'', skipping this boss.",
                     new Object[]{sBossName, bossesYml.getString(sBossName+"."+"type")});
                 
                 plugin.getLogger().log(Level.WARNING,"Hint: PigZombie vs pig_zombie");
                 
                 continue;
             }
             
             int hp = bossesYml.getInt(sBossName+".hp");
 
             int attackPower = bossesYml.getInt(sBossName+".attackPower");
 
             int essencesDropped = bossesYml.getInt(sBossName+".essencesDropped");
 
 // Add equipment if it has any
             List<ItemStack> equipment = new ArrayList<>();
             
             if(bossesYml.isSet(sBossName+".armor"))
             {
                 List<String> bossEquipmentStrings = (List<String>) bossesYml.getList(sBossName+".armor");
                 
                 for(String sItem : bossEquipmentStrings)
                 {
                     if(equipment.size() < 4)
                     {
                         ItemStack is = this.getItemStackFromEquipmentString(sBossName,sItem);
                         
                         if(is != null)
                         {
                             equipment.add(is);
                         }
                     }
                     else
                     {
                         plugin.getLogger().log(Level.WARNING, "{0} has too many armor items, skipping ''{1}''",
                                 new Object[]{sBossName, sItem});
                     }
                 }
             }
             
 // Add weapon if boss has one
             ItemStack weapon = null; 
             
             if(bossesYml.isSet(sBossName+".weapon"))
             {
                 // Method will return null if invalid, and handle notification of error
                 weapon = this.getItemStackFromEquipmentString(sBossName,bossesYml.getString(sBossName+".weapon"));
             }
 
 // Create the template
             BossTemplate bossTemplate = new BossTemplate(sBossName,entityType,hp,attackPower,essencesDropped,equipment,weapon);
             
 // Add any skills
             if(bossesYml.isSet(sBossName+".skills"))
             {
                 List<String> skillStrings = (List<String>) bossesYml.getList(sBossName+".skills");
 
                 for(String skillString : skillStrings)
                 {           
                     String[] skillValues = skillString.split(" ");
 
                     String skillName = skillValues[2];
                     int chance = Integer.parseInt(skillValues[0].substring(0,skillString.indexOf("%")));
                     int level = Integer.parseInt(skillValues[4]);
 
                     for(BossSkill bossSkill : availableBossSkills)
                     {
                         if(bossSkill.getYmlName().equalsIgnoreCase(skillName))
                         {
                             bossTemplate.addSkill(bossSkill, level, chance);
                         }
                     }
                 }  
             }
             
 // Add any events
             if(bossesYml.isSet(sBossName+".events"))
             {
                 List<String> eventStrings = (List<String>) bossesYml.getList(sBossName+".events");
 
                 for(String eventString : eventStrings)
                 {           
                     String[] eventValues = eventString.split(" ");
 
                     String sEventType = eventValues[0];
 
                     BossEventType eventType = null;
                     
                     for(BossEventType bet : BossEventType.values())
                     {
                         if(bet.name().equalsIgnoreCase(sEventType))
                         {
                             eventType = BossEventType.valueOf(sEventType);
                         }
                     }
 
                     if(eventType == null)
                     {
                         plugin.getLogger().log(Level.WARNING, 
                             "''{0}'' is not a valid event type on boss ''{1}''. Skipping.", 
                             new Object[]{sEventType, sBossName});
 
                         continue;
                     }
 
                     int iEventValue = -1;
 
                     try
                     {
                         iEventValue = Integer.parseInt(eventValues[1]);
                     }
                     catch(Exception e)
                     {
                         plugin.getLogger().log(Level.WARNING, 
                             "''{0}'' is not a valid event value on boss ''{1}''. Skipping.", 
                             new Object[]{eventValues[1], sBossName});
 
                         continue;
                     }                
 
                     String skillName = eventValues[2].replace("_", " ");
                     int level = Integer.parseInt(eventValues[4]);
 
                     for(BossSkill bossSkill : availableBossSkills)
                     {
                         if(bossSkill.getName().equalsIgnoreCase(skillName))
                         {
                             bossTemplate.addEvent(new BossEvent(eventType,iEventValue,bossSkill,level));
                         }
                     }
                 }  
             }
             
 // Save the template
             this.bossTemplates.put(bossTemplate.name.toLowerCase(),bossTemplate);
         }
        
         this.saveManager = new saveFileManager(plugin,this);
         
 // Schedule random boss spawns
 
         int iTimer = 60 * 20 * plugin.getConfig().getInt("timeBetweenChancesToGenerateBossEgg",60 * 60 * 20);
         int iMaxChance = plugin.getConfig().getInt("maxChanceToGenerateBossEgg",20);
         int iExpiration = 60 * 20 * plugin.getConfig().getInt("bossEggExpiration",15 * 60 * 20);
         
         if(iTimer > 0)
         {
             plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
                 plugin,
                 new RandomlyGenerateBossTask(plugin,iMaxChance,iTimer,iExpiration), 
                 iTimer, 
                 iTimer);
         }
         
 // Setup autospawner
         int autospawnTicks = plugin.getConfig().getInt("autospawnTicks");
         
         plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
             plugin,
             new BossAutoSpawner(
                     plugin,
                     this,
                     plugin.getConfig().getDouble("autospawnDistance")
             ), 
             autospawnTicks, 
             autospawnTicks);
     }
     
     public boolean isBoss(UUID id)
     {        
         for(Boss boss : this.activeBosses) {
             if(boss.getUniqueId().equals(id)) {
                 return true;
             }
         }
         
         return false;
     }
 
     public Boss getBoss(Entity entity)
     {
         for(Boss boss : this.activeBosses) {
             if(boss.getUniqueId().equals(entity.getUniqueId())) {
                 return boss;
             }
         }
         return null;
     }
     
     public boolean isBossEgg(Block b)
     {
         if(b.getType() == Material.DRAGON_EGG)
         {
             for(MetadataValue mdv : b.getMetadata("isBossEgg"))
             {
                 if(mdv.getOwningPlugin().equals(plugin))
                 {
                     if(mdv.asBoolean())
                     {
                         return true;
                     }
                 }
             }
         }
 
         return false;
     }
 
     public Boss hatchBoss(BossEgg egg)
     {        
         this.removeBossEgg(egg);
         
         Boss boss = this.spawnBoss(egg.getName(), egg.getLocation());
         
         return boss;
     }
 
     public Boss hatchBossAtLocation(Location lClicked) {
         for(BossEgg egg : this.bossEggs) {
             if(egg.getLocation().equals(lClicked)) {
                 return  this.hatchBoss(egg);
             }
         }
         return null;
     }
 
     public Boss spawnBoss(String sBossName, Location eggLocation)
     {
         Boss boss = new Boss(this.bossTemplates.get(sBossName.toLowerCase()));
         
         Entity ent = eggLocation.getWorld().spawnEntity(eggLocation, boss.getEntityType());
       
         boss.setEntity(ent);
         
         LivingEntity lent = (LivingEntity) ent;
         
         lent.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,9999999,5));
 
         EntityEquipment lequips = lent.getEquipment();
             
         if(boss.template.equipment != null)
         {
             lequips.setArmorContents(boss.template.equipment.toArray(new ItemStack[4]));
 
             lequips.setBootsDropChance(0f);
             lequips.setLeggingsDropChance(0f);
             lequips.setChestplateDropChance(0f);
             lequips.setHelmetDropChance(0f);
         }
             
         if(boss.template.weapon != null)
         {
             lequips.setItemInHand(boss.template.weapon);
 
             lequips.setItemInHandDropChance(0f);
         }
         
         activeBosses.add(boss);
         
         this.saveManager.save();
         
         return boss;
     }
 
     public boolean hasSpawnPoints()
     {
         return !this.spawnPoints.isEmpty();
     }
     
     public BossEgg spawnBossEgg(String bossName,String sSpawnPointName, boolean autoSpawn)
     {
         return spawnBossEgg(this.bossTemplates.get(bossName.toLowerCase()), sSpawnPointName, autoSpawn);
     }  
     
     public BossEgg spawnBossEgg(String sBossName,Block block, boolean autoSpawn)
     { 
         return spawnBossEgg(this.bossTemplates.get(sBossName.toLowerCase()), block, autoSpawn);
     }
 
     public BossEgg spawnBossEgg(BossTemplate bossTemplate,String sSpawnPointName, boolean autoSpawn)
     {
         Random random = new Random();
         
         BossEggSpawnPoint spawnPoint = this.spawnPoints.get(sSpawnPointName.toLowerCase());
         
         for(int i=0;i<15;i++)
         {
             Location lSpawnPoint = spawnPoint.location.clone().add(
                 random.nextInt(spawnPoint.radius*2)-spawnPoint.radius,
                 0,
                 random.nextInt(spawnPoint.radius*2)-spawnPoint.radius);
 
             Block block = lSpawnPoint.getBlock();
 
             World world = lSpawnPoint.getWorld();
             int mapHeight = world.getMaxHeight();
             int x = lSpawnPoint.getBlockX();
             int y = lSpawnPoint.getBlockY();
             int z = lSpawnPoint.getBlockZ();
 
             for(int j=0;j<spawnPoint.radius && j<mapHeight;j++)
             {
                 if(y+j+1 < mapHeight)
                 {
                     Block up1 = world.getBlockAt(x,y+j,z);
                     Block up2 = world.getBlockAt(x,y+j+1,z);
                     
                     if(up1 != null && up1.getType() != Material.AIR
                     && up2 != null && up2.getType() == Material.AIR)
                     {
                         boolean eggExistsThereAlready = false;
                         for(BossEgg egg : this.bossEggs)
                         {
                             if(egg.getLocation().equals(up2.getLocation())) {
                                 eggExistsThereAlready = true;
                                 break;
                             }
                         }
                         
                         if(!eggExistsThereAlready)
                         {
                             BossEgg egg = this.spawnBossEgg(bossTemplate, up2, autoSpawn);
                             
                             if(egg != null) {
                                 return egg;
                             }
                         }
                     }
                 }
 
                 if(y-j-1 > 0)
                 {
                     Block down1 = world.getBlockAt(x,y-j,z);
                     Block down2 = world.getBlockAt(x,y-j-1,z);
                     if(down1 != null && down1.getType() == Material.AIR
                     && down2 != null && down2.getType() != Material.AIR)
                     {
                         boolean eggExistsThereAlready = false;
                         
                         for(BossEgg egg : this.bossEggs)
                         {
                             if(egg.getLocation().equals(down1.getLocation())) {
                                 eggExistsThereAlready = true;
                                 break;
                             }
                         }
                         
                         if(!eggExistsThereAlready)
                         {
                             BossEgg egg = this.spawnBossEgg(bossTemplate, down1, autoSpawn);
                             
                             if(egg != null) {
                                 return egg;
                             }
                         }
                     }
                 }
             }
         }
         
         return null;
     }
     
     public BossEgg spawnBossEgg(BossTemplate bossTemplate,Block block,boolean autoHatch)
     { 
         for(BossEgg egg : this.bossEggs) {
             if(egg.getLocation().equals(block.getLocation()))
             {
                 return null;
             }
         }
         
         block.getRelative(BlockFace.DOWN).setType(Material.BEDROCK);
 
         block.setType(Material.DRAGON_EGG);
 
         block.setMetadata("isBossEgg", new FixedMetadataValue(plugin,true));
 
         BossEgg newEgg = new BossEgg(
             bossTemplate.name,
             block.getLocation(),
             autoHatch
         );
 
         this.bossEggs.add(newEgg);
 
         this.saveManager.save();
 
         return newEgg;
     }
     
     public BossEgg spawnRandomBossEgg()
     {
         Random r = new Random();
 
         Object[] values = this.spawnPoints.keySet().toArray();
         String sRandomSpawnPointName = (String) values[r.nextInt(values.length)];
 
         Object[] bosses = this.bossTemplates.values().toArray();
         BossTemplate bossTemplate = (BossTemplate) bosses[r.nextInt(bosses.length)];
 
         BossEgg egg = spawnBossEgg(bossTemplate, sRandomSpawnPointName, true);
 
         if(egg.getLocation() == null)
         {
             plugin.getServer().broadcast("-------------- RareItemHunter ----------------","rareitemhunter.admin");
             plugin.getServer().broadcast("Was unable to find a place to spawn a "+bossTemplate.name+" boss egg at spawn point "+sRandomSpawnPointName,"rareitemhunter.admin");
         }
         
         return egg;
     }
 
     public void addSpawnPoint(String name, Location location, int radius)
     {
         this.spawnPoints.put(name.toLowerCase(), new BossEggSpawnPoint(name.toLowerCase(),location,radius));
         
         this.saveManager.save();
     }
 
     public boolean isSpawnPoint(String name)
     {
         return this.spawnPoints.containsKey(name.toLowerCase());
     }
 
     public void delSpawnPoint(String name)
     {
         this.spawnPoints.remove(name.toLowerCase());
         
         this.saveManager.save();
     }
 
     public Iterable<BossEggSpawnPoint> getSpawnPoints()
     {
         return this.spawnPoints.values();
     }
 
     public boolean isValidLocation(String sPointName)
     {
         return this.spawnPoints.containsKey(sPointName.toLowerCase());
     }
 
     public boolean isValidBossName(String sBossName)
     {
         return this.bossTemplates.containsKey(sBossName.toLowerCase());
     }
 
     public void removeBossEgg(BossEgg egg)
     {
         if(this.bossEggs.contains(egg))
         {
             Block block = egg.getLocation().getBlock();
             Block blockNeneath = block.getRelative(BlockFace.DOWN);
 
             if(block.getType() == Material.DRAGON_EGG)
             {
                 block.setType(Material.AIR);
             }
             if(blockNeneath.getType() == Material.BEDROCK)
             { 
                 blockNeneath.setType(Material.AIR);
             }
 
             this.bossEggs.remove(egg);
             
             block.removeMetadata("isBossEgg", plugin);
             
             this.saveManager.save();
         }
     }
 
     // Used primarily by entityexplode event
     public void removeBossEggAtLocation(Location lEgg) {
         for(BossEgg egg : this.bossEggs) {
             if(egg.getLocation().equals(lEgg)) {
                 this.removeBossEgg(egg);
             }
         }
     }
 
     public Location getNearestBossEggLocation(Location l)
     {
         Location lClosest = null;
         
         World lWorld = l.getWorld();
         
         double dClosest = -1;
         
         double dCurrent;
         
         for(BossEgg egg : bossEggs)
         {
             Location lEgg = egg.getLocation();
             
             if(lEgg.getWorld().equals(lWorld))
             {
                 dCurrent = l.distanceSquared(lEgg);
                 
                 if(dClosest == -1)
                 {
                     dClosest = dCurrent;
                     
                     lClosest = lEgg;
                 }
                 else if(dCurrent < dClosest)
                 {
                     dClosest = dCurrent;
                     
                     lClosest = lEgg;
                 }
             }
         }
         
         return lClosest;
     }
     
 // Misc helper methods
     private ItemStack getItemStackFromEquipmentString(String sBossName,String sItem)
     {
         String[] equipValues = sItem.split(" ");
                     
         Material equipMaterial = Material.matchMaterial(equipValues[0]);
 
         if(equipMaterial != null)
         {
             ItemStack is = new ItemStack(equipMaterial);
 
             if(equipValues.length > 1)
             {
                 for(String sEnchantment : equipValues[1].split(","))
                 {
                     String[] enchantmentPair = sEnchantment.split(":");
 
                     Enchantment en = Enchantment.getByName(enchantmentPair[0]);
                     int level = 0;
 
                     try
                     {
                         level = Integer.parseInt(enchantmentPair[1]);
                     }
                     catch(Exception e)
                     {
                         plugin.getLogger().log(Level.WARNING,"'"+enchantmentPair[1]+"' is not a valid enchantment level on boss '"+sBossName+"'. Skipping.");
 
                         return null;
                     }
 
                     if(en == null)
                     {
                         plugin.getLogger().log(Level.WARNING,"'"+enchantmentPair[0]+"' is not a valid enchantment name on boss '"+sBossName+"'. Skipping.");
 
                         return null;
                     }
 
                     is.addEnchantment(en, level);
                 }
             }
 
             return is;
         }
         else
         {
             plugin.getLogger().log(Level.WARNING,"'"+equipValues[0]+"' is not a valid material on boss '"+sBossName+"'. Skipping.");
         }
         
         return null;
     }
 
     public void destroyBoss(Entity eBoss,Boss boss)
     {
         this.activeBosses.remove(boss);
         eBoss.remove();
     }
 
     public BossEgg spawnBoss(String sBossName, String sPointName)
     {
         BossEggSpawnPoint sp = this.spawnPoints.get(sPointName.toLowerCase());
 
         BossEgg egg = this.spawnBossEgg(sBossName, sPointName, false);
         
         this.hatchBoss(egg);
         
         return egg;
     }
 
     public String getBossesString()
     {
         //TODO: Clean this up
         if(this.bossTemplates.isEmpty())
         {
             return "";
         }
         
         String sBosses = "";
         
         for(String sBossName : this.bossTemplates.keySet())
         {
             sBosses += ", "+sBossName;
         }
         
         return sBosses.substring(2);
     }
 
     public String getSpawnPointsString()
     {
         //TODO: Clean this up
         if(this.spawnPoints.isEmpty())
         {
             return "";
         }
         
         String sSpawnPoints = "";
         
         for(String sSpawnPoint : this.spawnPoints.keySet())
         {
             sSpawnPoints += ", "+sSpawnPoint;
         }
         
         return sSpawnPoints.substring(2);
     }
 }
