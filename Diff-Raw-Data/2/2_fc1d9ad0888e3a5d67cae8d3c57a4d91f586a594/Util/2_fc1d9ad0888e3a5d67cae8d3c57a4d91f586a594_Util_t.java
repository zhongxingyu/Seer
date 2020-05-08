 package se.DMarby.Pets;
 
 import net.minecraft.server.v1_7_R1.Entity;
 import net.minecraft.server.v1_7_R1.*;
 import org.bukkit.DyeColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
 import org.bukkit.entity.*;
 import org.bukkit.entity.Ocelot.Type;
 import org.bukkit.entity.Villager.Profession;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.inventory.ItemStack;
 
 import java.lang.reflect.Field;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 @SuppressWarnings("unchecked")
 public class Util {
 
     private static Map<Class<? extends Entity>, Integer> ENTITY_CLASS_TO_INT;
     private static Map<Integer, Class<? extends Entity>> ENTITY_INT_TO_CLASS;
     public static double MAX_DISTANCE = 10 * 10;
     public static boolean removeInFight = false;
     public static int MAX_LEVEL = -1;
     private static Field GOAL_FIELD;
     private static DyeColor[] colors = DyeColor.values();
     private static Profession[] professions = Profession.values();
     private static Random rand = new Random();
     private static List<Integer> allvariants = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 256, 257, 258, 259, 260, 261, 262, 512, 513, 514, 515, 516, 517, 518, 768, 769, 770, 771, 772, 773, 774, 1024, 1025, 1026, 1027, 1028, 1029, 1030);
     private static List<ItemStack> armor = Arrays.asList(new ItemStack(417), new ItemStack(418), new ItemStack(419));
 
     public static void clearGoals(PathfinderGoalSelector... goalSelectors) {
         if (GOAL_FIELD == null || goalSelectors == null) {
             return;
         }
         for (PathfinderGoalSelector selector : goalSelectors) {
             try {
                 List<?> list = (List<?>) GOAL_FIELD.get(selector);
                 list.clear();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     private static Field getField(Class<?> clazz, String field) {
         Field f = null;
         try {
             f = clazz.getDeclaredField(field);
             f.setAccessible(true);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return f;
     }
 
     public static void load(FileConfiguration config) {
         /* if (!config.isSet("pet.max-distance-squared")) {
         config.set("pet.max-distance-squared", MAX_DISTANCE);
         }
         MAX_DISTANCE = config.getDouble("pet.max-distance-squared");
         if (!config.isSet("pet.max-level")) {
         config.set("pet.max-level", MAX_LEVEL);
         }
         MAX_LEVEL = config.getInt("pet.max-level");*/
         if(!config.isSet("remove-in-fight")){
             config.set("remove-in-fight", false);
         }
         removeInFight = config.getBoolean("remove-in-fight");
     }
 
     public static void registerEntityClass(Class<? extends Entity> clazz) {
         if (ENTITY_CLASS_TO_INT.containsKey(clazz)) {
             return;
         }
         Class<?> search = clazz;
         while ((search = search.getSuperclass()) != null && Entity.class.isAssignableFrom(search)) {
             if (!ENTITY_CLASS_TO_INT.containsKey(search)) {
                 continue;
             }
             int code = ENTITY_CLASS_TO_INT.get(search);
             ENTITY_INT_TO_CLASS.put(code, clazz);
             ENTITY_CLASS_TO_INT.put(clazz, code);
             return;
         }
         throw new IllegalArgumentException("unable to find valid entity superclass");
     }
 
     public static LivingEntity spawnPet(Player player, String pet) {
         World world = ((CraftWorld) player.getWorld()).getHandle();
         EntityLiving entity = null;
         if (pet.equalsIgnoreCase("slime")) {
             entity = new EntitySlimePet(world, player);
             ((Slime) entity.getBukkitEntity()).setSize(1);
         } else if (pet.equalsIgnoreCase("blaze")) {
             entity = new EntityBlazePet(world, player);
         } else if (pet.equalsIgnoreCase("cavespider")) {
             entity = new EntityCaveSpiderPet(world, player);
         } else if (pet.equalsIgnoreCase("chicken")) {
             entity = new EntityChickenPet(world, player);
             ((Chicken) entity.getBukkitEntity()).setBaby();
             ((Chicken) entity.getBukkitEntity()).setAgeLock(true);
         } else if (pet.equalsIgnoreCase("cow")) {
             entity = new EntityCowPet(world, player);
             ((Cow) entity.getBukkitEntity()).setBaby();
             ((Cow) entity.getBukkitEntity()).setAgeLock(true);
         } else if (pet.equalsIgnoreCase("magmacube")) {
             entity = new EntityMagmaCubePet(world, player);
             ((MagmaCube) entity.getBukkitEntity()).setSize(1);
         } else if (pet.equalsIgnoreCase("mooshroom")) {
             entity = new EntityMushroomCowPet(world, player);
             ((MushroomCow) entity.getBukkitEntity()).setBaby();
             ((MushroomCow) entity.getBukkitEntity()).setAgeLock(true);
         } else if (pet.equalsIgnoreCase("ocelot") || pet.equalsIgnoreCase("wildcat")){
             entity = new EntityOcelotPet(world, player);
             ((Ocelot) entity.getBukkitEntity()).setBaby();
             ((Ocelot) entity.getBukkitEntity()).setCatType(Type.WILD_OCELOT);
             ((Ocelot) entity.getBukkitEntity()).setAgeLock(true);
         } else if (pet.equalsIgnoreCase("cat") || pet.equalsIgnoreCase("blackcat")) {
             entity = new EntityOcelotPet(world, player);
             ((Ocelot) entity.getBukkitEntity()).setBaby();
             ((Ocelot) entity.getBukkitEntity()).setCatType(Type.BLACK_CAT);
             ((Ocelot) entity.getBukkitEntity()).setAgeLock(true);
         } else if (pet.equalsIgnoreCase("redcat")) {
             entity = new EntityOcelotPet(world, player);
             ((Ocelot) entity.getBukkitEntity()).setBaby();
             ((Ocelot) entity.getBukkitEntity()).setCatType(Type.RED_CAT);
             ((Ocelot) entity.getBukkitEntity()).setAgeLock(true);
         } else if (pet.equalsIgnoreCase("siamesecat")) {
             entity = new EntityOcelotPet(world, player);
             ((Ocelot) entity.getBukkitEntity()).setBaby();
             ((Ocelot) entity.getBukkitEntity()).setCatType(Type.SIAMESE_CAT);
             ((Ocelot) entity.getBukkitEntity()).setAgeLock(true);
         } else if (pet.equalsIgnoreCase("pig")) {
             entity = new EntityPigPet(world, player);
             ((Pig) entity.getBukkitEntity()).setBaby();
             ((Pig) entity.getBukkitEntity()).setAgeLock(true);
         }else if (pet.equalsIgnoreCase("blacksheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             ((Sheep) entity.getBukkitEntity()).setColor(DyeColor.BLACK);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         }else if (pet.equalsIgnoreCase("bluesheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             ((Sheep) entity.getBukkitEntity()).setColor(DyeColor.BLUE);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         }else if (pet.equalsIgnoreCase("brownsheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             ((Sheep) entity.getBukkitEntity()).setColor(DyeColor.BROWN);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         }else if (pet.equalsIgnoreCase("cyansheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             ((Sheep) entity.getBukkitEntity()).setColor(DyeColor.CYAN);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         }else if (pet.equalsIgnoreCase("graysheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             ((Sheep) entity.getBukkitEntity()).setColor(DyeColor.GRAY);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         }else if (pet.equalsIgnoreCase("greensheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             ((Sheep) entity.getBukkitEntity()).setColor(DyeColor.GREEN);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         }else if (pet.equalsIgnoreCase("lightbluesheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             ((Sheep) entity.getBukkitEntity()).setColor(DyeColor.LIGHT_BLUE);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         }else if (pet.equalsIgnoreCase("limesheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             ((Sheep) entity.getBukkitEntity()).setColor(DyeColor.LIME);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         }else if (pet.equalsIgnoreCase("magentasheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             ((Sheep) entity.getBukkitEntity()).setColor(DyeColor.MAGENTA);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         }else if (pet.equalsIgnoreCase("orangesheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             ((Sheep) entity.getBukkitEntity()).setColor(DyeColor.ORANGE);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         }else if (pet.equalsIgnoreCase("pinksheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             ((Sheep) entity.getBukkitEntity()).setColor(DyeColor.PINK);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         }else if (pet.equalsIgnoreCase("purplesheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             ((Sheep) entity.getBukkitEntity()).setColor(DyeColor.PURPLE);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         }else if (pet.equalsIgnoreCase("redsheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             ((Sheep) entity.getBukkitEntity()).setColor(DyeColor.RED);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         }else if (pet.equalsIgnoreCase("silversheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             ((Sheep) entity.getBukkitEntity()).setColor(DyeColor.SILVER);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         }else if (pet.equalsIgnoreCase("whitesheep") || pet.equalsIgnoreCase("sheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             ((Sheep) entity.getBukkitEntity()).setColor(DyeColor.WHITE);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         }else if (pet.equalsIgnoreCase("yellowsheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             ((Sheep) entity.getBukkitEntity()).setColor(DyeColor.YELLOW);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         } else if (pet.equalsIgnoreCase("silverfish")) {
             entity = new EntitySilverfishPet(world, player);
         } else if (pet.equalsIgnoreCase("villager")) {
             entity = new EntityVillagerPet(world, player);
             ((Villager) entity.getBukkitEntity()).setBaby();
             ((Villager) entity.getBukkitEntity()).setProfession(professions[rand.nextInt(professions.length)]);
             ((Villager) entity.getBukkitEntity()).setAgeLock(true);
         } else if (pet.equalsIgnoreCase("greenvillager")) {
             entity = new EntityVillagerPet(world, player);
             ((Villager) entity.getBukkitEntity()).setBaby();
             ((Villager) entity.getBukkitEntity()).setAgeLock(true);
             ((EntityVillager) entity).setProfession(5);
         } else if (pet.equalsIgnoreCase("wolf")) {
             entity = new EntityWolfPet(world, player);
             ((Wolf) entity.getBukkitEntity()).setBaby();
             ((Wolf) entity.getBukkitEntity()).setAgeLock(true);
         } else if (pet.equalsIgnoreCase("dog")) {
             entity = new EntityWolfPet(world, player);
             ((Wolf) entity.getBukkitEntity()).setBaby();
             ((Wolf) entity.getBukkitEntity()).setTamed(true);
             ((Wolf) entity.getBukkitEntity()).setCollarColor(colors[rand.nextInt(colors.length)]);
             ((Wolf) entity.getBukkitEntity()).setAgeLock(true);
         } else if (pet.equalsIgnoreCase("snowman")) {
             entity = new EntitySnowmanPet(world, player);
         } else if (pet.equalsIgnoreCase("creeper")) {
             entity = new EntityCreeperPet(world, player);
         } else if (pet.equalsIgnoreCase("electrocreeper")) {
             entity = new EntityCreeperPet(world, player);
             ((Creeper) entity.getBukkitEntity()).setPowered(true);
         } else if (pet.equalsIgnoreCase("bat")) {
             entity = new EntityBatPet(world, player);
         } else if (pet.equalsIgnoreCase("squid")) {
             entity = new EntitySquidPet(world, player);
         } else if (pet.equalsIgnoreCase("zombiepigman") || pet.equalsIgnoreCase("babyzombiepigman")) {
             entity = new EntityPigZombiePet(world, player);
         } else if (pet.equalsIgnoreCase("zombie") || pet.equalsIgnoreCase("babyzombie")) {
             entity = new EntityZombiePet(world, player);
         } else if (pet.equalsIgnoreCase("zombievillager") || pet.equalsIgnoreCase("babyzombievillager")) {
             entity = new EntityZombiePet(world, player);
             ((EntityZombie) entity).setVillager(true);
         } else if (pet.equalsIgnoreCase("irongolem")) {
             entity = new EntityIronGolemPet(world, player);
         } else if (pet.equalsIgnoreCase("horse")) {
             entity = new EntityHorsePet(world, player);
             ((Horse) entity.getBukkitEntity()).setBaby();
             ((Horse) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
             ((Horse) entity.getBukkitEntity()).setAgeLock(true);
             ((EntityHorse) entity).setType(0);
             ((EntityHorse) entity).setVariant(allvariants.get(rand.nextInt(allvariants.size())));
         } else if (pet.equalsIgnoreCase("donkey")) {
             entity = new EntityHorsePet(world, player);
             ((Horse) entity.getBukkitEntity()).setBaby();
             ((Horse) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
             ((Horse) entity.getBukkitEntity()).setAgeLock(true);
             ((EntityHorse) entity).setType(1);
         } else if (pet.equalsIgnoreCase("mule")) {
             entity = new EntityHorsePet(world, player);
             ((Horse) entity.getBukkitEntity()).setBaby();
             ((Horse) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
             ((Horse) entity.getBukkitEntity()).setAgeLock(true);
             ((EntityHorse) entity).setType(2);
         } else if (pet.equalsIgnoreCase("undeadhorse")) {
             entity = new EntityHorsePet(world, player);
             ((Horse) entity.getBukkitEntity()).setBaby();
             ((Horse) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
             ((Horse) entity.getBukkitEntity()).setAgeLock(true);
             ((EntityHorse) entity).setType(3);
         } else if (pet.equalsIgnoreCase("skeletonhorse")) {
             entity = new EntityHorsePet(world, player);
             ((Horse) entity.getBukkitEntity()).setBaby();
             ((Horse) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
             ((Horse) entity.getBukkitEntity()).setAgeLock(true);
             ((EntityHorse) entity).setType(4);
         } else if (pet.equalsIgnoreCase("ridablehorse")) {
             entity = new EntityHorsePet(world, player, true);
             ((Horse) entity.getBukkitEntity()).setAdult();
             ((Horse) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
             ((Horse) entity.getBukkitEntity()).setAgeLock(true);
             ((EntityHorse) entity).setTame(true);
             ((EntityHorse) entity).setType(0);
             ((EntityHorse) entity).setVariant(allvariants.get(rand.nextInt(allvariants.size())));
             ((EntityHorsePet) entity).giveShit();
         }
         if (entity != null) {
             entity.setPosition(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
             world.addEntity(entity, SpawnReason.CUSTOM);
             entity.getBukkitEntity().teleport(player);
             return (LivingEntity) entity.getBukkitEntity();
         }
         System.err.println("Pet is null!");
         return null;
     }
     
     public static boolean isInt(String str) {
         try {
             Integer.parseInt(str);
                     return true;
         }
         catch(NumberFormatException nfe) {
                 return false;
         }
     }
 
     static {
         GOAL_FIELD = getField(PathfinderGoalSelector.class, "a");
         try {
             Field field = getField(EntityTypes.class, "d");
             ENTITY_INT_TO_CLASS = (Map<Integer, Class<? extends Entity>>) field.get(null);
            field = getField(EntityTypes.class, "f");
             ENTITY_CLASS_TO_INT = (Map<Class<? extends Entity>, Integer>) field.get(null);
         } catch (Exception e) {
             e.printStackTrace();
         }
         registerEntityClass(EntityBlazePet.class);
         registerEntityClass(EntityCaveSpiderPet.class);
         registerEntityClass(EntityChickenPet.class);
         registerEntityClass(EntityCowPet.class);
         registerEntityClass(EntityMagmaCubePet.class);
         registerEntityClass(EntityMushroomCowPet.class);
         registerEntityClass(EntityOcelotPet.class);
         registerEntityClass(EntityPigPet.class);
         registerEntityClass(EntitySheepPet.class);
         registerEntityClass(EntitySilverfishPet.class);
         registerEntityClass(EntitySlimePet.class);
         registerEntityClass(EntityVillagerPet.class);
         registerEntityClass(EntityWolfPet.class);
         registerEntityClass(EntitySnowmanPet.class);
         registerEntityClass(EntityCreeperPet.class);
         registerEntityClass(EntitySquidPet.class);
         registerEntityClass(EntityBatPet.class);
         registerEntityClass(EntityZombiePet.class);
         registerEntityClass(EntityPigZombiePet.class);
         registerEntityClass(EntityIronGolemPet.class);
         registerEntityClass(EntityHorsePet.class);
 
     }
 }
