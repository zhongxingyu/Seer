 package com.bukkit.gemo.FalseBook.IC.ICs.worldedit;
 
 import com.bukkit.gemo.FalseBook.IC.ICs.BaseChip;
 import com.bukkit.gemo.FalseBook.IC.ICs.BaseIC;
 import com.bukkit.gemo.FalseBook.IC.ICs.ICGroup;
 import com.bukkit.gemo.FalseBook.IC.ICs.InputState;
 import com.bukkit.gemo.FalseBook.IC.ICs.Lever;
 import com.bukkit.gemo.utils.BlockUtils;
 import com.bukkit.gemo.utils.Parser;
 import com.bukkit.gemo.utils.SignUtils;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Blaze;
 import org.bukkit.entity.Chicken;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.MagmaCube;
 import org.bukkit.entity.MushroomCow;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.block.SignChangeEvent;
 
 public class ICMobSpawner extends BaseIC {
 
     public ICMobSpawner() {
         this.ICName = "MOB SPAWNER";
         this.ICNumber = "ic.mobspawner";
         setICGroup(ICGroup.WORLDEDIT);
         this.chipState = new BaseChip(true, false, false, "Clock", "", "");
         this.chipState.setOutputs("Output = Input", "", "");
         this.chipState.setLines("<b>Mobname to spawn:</b><ul><li>AngryWolf</li><li>Blaze</li><li>Chicken</li><li>Cow</li><li>Creeper</li><li>Dog</li><li>EnderDragon</li><li>Ghast</li><li>Giant</li><li>Magmacube</li><li>MushroomCow</li><li>Pig</li><li>PigZombie</li><li>Sheep</li><li>Skeleton</li><li>Slime</li><li>Spider</li><li>Villager</li><li>Zombie</li><li>Wolf</li></ul>If you place \"C:\" in front of the name, it will spawn the mob as a child (if possible).", "The amount of mobs to spawn (default: 1)");
         this.ICDescription = "The MC1200 spawns a mob in the first free space above the block behind the IC sign when the input (the \"clock\") goes from low to high.";
     }
 
     public void checkCreation(SignChangeEvent event) {
         EntityType[] entityTypes = EntityType.values();
         if (event.getLine(1) == null) {
             SignUtils.cancelSignCreation(event, "Mob '" + event.getLine(1) + "' not found.");
             return;
         }
         boolean found = false;
         String mobLine = event.getLine(1).toLowerCase();
         mobLine = mobLine.replace("monster", "");
         mobLine = mobLine.replace("c:", "");
         for (int i = 0; i < entityTypes.length; i++) {
             if ((!entityTypes[i].isAlive()) && (!entityTypes[i].isSpawnable())) {
                 continue;
             }
             if ((mobLine.equalsIgnoreCase(entityTypes[i].name())) || (mobLine.equalsIgnoreCase("pig_zombie")) || (mobLine.equalsIgnoreCase("DOG")) || (mobLine.equalsIgnoreCase("ANGRYWOLF")) || (mobLine.equalsIgnoreCase("MAGMACUBE")) || (mobLine.equalsIgnoreCase("BLAZE"))) {
                event.setLine(1, event.getLine(2).toUpperCase());
                 found = true;
                 break;
             }
         }
         if (!found) {
             String name = "";
             for (int i = 0; i < entityTypes.length; i++) {
                 if ((!entityTypes[i].isAlive()) && (!entityTypes[i].isSpawnable())) {
                     continue;
                 }
                 name = entityTypes[i].name().replace(" ", "").replace("_", "").replace("-", "");
                 if (mobLine.equalsIgnoreCase(name)) {
                     event.setLine(1, event.getLine(1).toUpperCase());
                     found = true;
                     break;
                 }
             }
         }
 
         if (!found) {
             SignUtils.cancelSignCreation(event, "Mob '" + event.getLine(1) + "' not found.");
             return;
         }
 
         if (!Parser.isIntegerOrEmpty(event.getLine(2))) {
             SignUtils.cancelSignCreation(event, "Line 4 must be an integer.");
             return;
         }
         if ((Parser.isInteger(event.getLine(2)))
                 && (Parser.getInteger(event.getLine(2), 1) < 1)) {
             SignUtils.cancelSignCreation(event, "Line 4 must be > 0.");
             return;
         }
     }
 
     public void Execute(Sign signBlock, InputState currentInputs, InputState previousInputs) {
         if ((currentInputs.isInputOneHigh()) && (previousInputs.isInputOneLow())) {
             World w = signBlock.getWorld();
 
             int mobCount = 0;
             if (!Parser.isIntegerOrEmpty(signBlock.getLine(2))) {
                 return;
             }
             mobCount = Parser.getInteger(signBlock.getLine(2), 1);
             if (mobCount < 1) {
                 return;
             }
 
             boolean isChild = false;
             boolean isAngryWolf = false;
             boolean isDog = false;
             boolean isSpecial = false;
             Class clazz = null;
             String mobLine = signBlock.getLine(1).toLowerCase();
             mobLine = mobLine.replace("monster", "");
             if (mobLine.startsWith("c:")) {
                 isChild = true;
                 mobLine = mobLine.replace("c:", "");
             }
             EntityType typeOfMob = null;
             for (int i = 0; i < EntityType.values().length; i++) {
                 if ((!EntityType.values()[i].isAlive()) && (!EntityType.values()[i].isSpawnable())) {
                     continue;
                 }
                 if (EntityType.values()[i].name().equalsIgnoreCase(mobLine)) {
                     typeOfMob = EntityType.values()[i];
                     break;
                 }
             }
             if (typeOfMob == null) {
                 String name = "";
                 for (int i = 0; i < EntityType.values().length; i++) {
                     if ((!EntityType.values()[i].isAlive()) && (!EntityType.values()[i].isSpawnable())) {
                         continue;
                     }
                     name = EntityType.values()[i].name().replace(" ", "").replace("_", "").replace("-", "");
                     if (mobLine.equalsIgnoreCase(name)) {
                         typeOfMob = EntityType.values()[i];
                         break;
                     }
                 }
                 if (mobLine.equalsIgnoreCase("pigzombie")) {
                     typeOfMob = EntityType.PIG_ZOMBIE;
                 } else if (mobLine.equalsIgnoreCase("dog")) {
                     isDog = true;
                     typeOfMob = EntityType.PIG_ZOMBIE;
                 } else if (mobLine.equalsIgnoreCase("angrywolf")) {
                     isAngryWolf = true;
                     typeOfMob = EntityType.PIG_ZOMBIE;
                 } else if (mobLine.equalsIgnoreCase("magmacube")) {
                     isSpecial = true;
                     isChild = false;
                     typeOfMob = null;
                     clazz = MagmaCube.class;
                 } else if (mobLine.equalsIgnoreCase("blaze")) {
                     isSpecial = true;
                     isChild = false;
                     typeOfMob = null;
                     clazz = Blaze.class;
                 }
             }
 
             if ((typeOfMob == null) && (clazz == null)) {
                 return;
             }
 
             if ((isChild) && (clazz == null)
                     && (typeOfMob != EntityType.WOLF) && (typeOfMob != EntityType.SHEEP) && (typeOfMob != EntityType.COW) && (typeOfMob != EntityType.MUSHROOM_COW) && (typeOfMob != EntityType.PIG) && (typeOfMob != EntityType.CHICKEN)) {
                 isChild = false;
             }
 
             Location loc = getICBlock(signBlock);
             int maxY = Math.min(w.getMaxHeight() - 1, loc.getBlockY() + 10);
             for (int y = loc.getBlockY() + 1; y <= maxY; y++) {
                 if ((BlockUtils.canPassThrough(w.getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getTypeId())) && (BlockUtils.canPassThrough(w.getBlockAt(loc.getBlockX(), y + 1, loc.getBlockZ()).getTypeId()))) {
                     Location sPos = loc;
                     sPos.setX(sPos.getX() + 0.5D);
                     sPos.setZ(sPos.getZ() + 0.5D);
                     sPos.setY(y);
                     switchLever(Lever.BACK, signBlock, true);
                     for (int c = 0; c < mobCount; c++) {
                         if (!isSpecial) {
                             LivingEntity entity = w.spawnCreature(loc, typeOfMob);
                             if (isAngryWolf) {
                                 ((Wolf) entity).setAngry(true);
                             } else if (isDog) {
                                 ((Wolf) entity).setSitting(true);
                             }
                             if (isChild) {
                                 if (typeOfMob == EntityType.WOLF) {
                                     ((Wolf) entity).setBaby();
                                 }
                                 if (typeOfMob == EntityType.COW) {
                                     ((Cow) entity).setBaby();
                                 }
                                 if (typeOfMob == EntityType.MUSHROOM_COW) {
                                     ((MushroomCow) entity).setBaby();
                                 }
                                 if (typeOfMob == EntityType.PIG) {
                                     ((Pig) entity).setBaby();
                                 }
                                 if (typeOfMob == EntityType.CHICKEN) {
                                     ((Chicken) entity).setBaby();
                                 }
                                 if (typeOfMob == EntityType.SHEEP) {
                                     ((Sheep) entity).setBaby();
                                 }
                             }
                         } else {
                             w.spawn(loc, clazz);
                         }
                     }
                     return;
                 }
             }
         } else {
             switchLever(Lever.BACK, signBlock, false);
         }
     }
 }
