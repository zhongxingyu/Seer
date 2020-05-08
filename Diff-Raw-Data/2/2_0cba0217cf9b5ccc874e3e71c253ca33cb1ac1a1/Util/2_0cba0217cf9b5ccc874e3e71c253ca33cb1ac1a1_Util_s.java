 package se.DMarby.Pets;
 
 import java.lang.reflect.Field;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import net.minecraft.server.Entity;
 import net.minecraft.server.EntityLiving;
 import net.minecraft.server.EntityTypes;
 import net.minecraft.server.EntityVillager;
 import net.minecraft.server.EntityZombie;
 import net.minecraft.server.PathfinderGoalSelector;
 import net.minecraft.server.World;
 
 import org.bukkit.DyeColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.Chicken;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.MagmaCube;
 import org.bukkit.entity.MushroomCow;
 import org.bukkit.entity.Ocelot;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Villager;
 import org.bukkit.entity.Villager.Profession;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 
 @SuppressWarnings("unchecked")
 public class Util {
 
     private static Map<Class<? extends Entity>, Integer> ENTITY_CLASS_TO_INT;
     private static Map<Integer, Class<? extends Entity>> ENTITY_INT_TO_CLASS;
     public static double MAX_DISTANCE = 10 * 10;
     public static int MAX_LEVEL = -1;
     private static Field GOAL_FIELD;
     private static DyeColor[] colors = DyeColor.values();
     private static Profession[] professions = Profession.values();
 
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
         } else if (pet.equalsIgnoreCase("ocelot")) {
             entity = new EntityOcelotPet(world, player);
             ((Ocelot) entity.getBukkitEntity()).setBaby();
             ((Ocelot) entity.getBukkitEntity()).setAgeLock(true);
         } else if (pet.equalsIgnoreCase("cat")) {
             entity = new EntityOcelotPet(world, player);
             ((Ocelot) entity.getBukkitEntity()).setBaby();
             ((Ocelot) entity.getBukkitEntity()).setTamed(true);
             ((Ocelot) entity.getBukkitEntity()).setAgeLock(true);
         } else if (pet.equalsIgnoreCase("pig")) {
             entity = new EntityPigPet(world, player);
             ((Pig) entity.getBukkitEntity()).setBaby();
             ((Pig) entity.getBukkitEntity()).setAgeLock(true);
         } else if (pet.equalsIgnoreCase("sheep")) {
             entity = new EntitySheepPet(world, player);
             ((Sheep) entity.getBukkitEntity()).setBaby();
             Random rand = new Random();
             ((Sheep) entity.getBukkitEntity()).setColor(colors[rand.nextInt(colors.length)]);
             ((Sheep) entity.getBukkitEntity()).setAgeLock(true);
         } else if (pet.equalsIgnoreCase("silverfish")) {
             entity = new EntitySilverfishPet(world, player);
         } else if (pet.equalsIgnoreCase("villager")) {
             entity = new EntityVillagerPet(world, player);
             ((Villager) entity.getBukkitEntity()).setBaby();
             Random rand = new Random();
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
         } else if (pet.equalsIgnoreCase("zombie") || pet.equalsIgnoreCase("babyzombie")) {
             entity = new EntityZombiePet(world, player);
         } else if (pet.equalsIgnoreCase("zombievillager") || pet.equalsIgnoreCase("babyzombievillager")) {
             entity = new EntityZombiePet(world, player);
             ((EntityZombie) entity).setVillager(true);
        } else if (pet.equalsIgnoreCase("golem")) {
             entity = new EntityIronGolemPet(world, player);
         }
         if (entity != null) {
             world.addEntity(entity, SpawnReason.CUSTOM);
             entity.getBukkitEntity().teleport(player);
             return (LivingEntity) entity.getBukkitEntity();
         }
         return null;
     }
 
     static {
         GOAL_FIELD = getField(PathfinderGoalSelector.class, "a");
         try {
             Field field = getField(EntityTypes.class, "d");
             ENTITY_INT_TO_CLASS = (Map<Integer, Class<? extends Entity>>) field.get(null);
             field = getField(EntityTypes.class, "e");
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
         registerEntityClass(EntityIronGolemPet.class);
     }
 }
