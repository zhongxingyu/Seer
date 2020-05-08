 package net.kingdomsofarden.andrew2060.invasion.monsters.nms.goals;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.kingdomsofarden.andrew2060.invasion.api.MobMinionManager;
 import net.kingdomsofarden.andrew2060.invasion.api.mobskills.MobAction;
 import net.kingdomsofarden.andrew2060.invasion.api.mobskills.MobTargetSelectorAction;
 import net.minecraft.server.v1_6_R2.EntityCreature;
 import net.minecraft.server.v1_6_R2.EntityInsentient;
 import net.minecraft.server.v1_6_R2.PathfinderGoal;
 import net.minecraft.server.v1_6_R2.PathfinderGoalSelector;
 
 import org.bukkit.craftbukkit.v1_6_R2.entity.CraftCreature;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.EntityType;
 
 public class MobGoalManager {
 
     private HashMap<EntityType,ArrayList<MobAction>> entityActionsMap; 
     private HashMap<EntityType,ArrayList<MobTargetSelectorAction>> entityTargettingMap;
     private Field pathfinderGoalField;
     public static MobMinionManager minionManager;
     public MobGoalManager() {
         minionManager = new MobMinionManager();
         this.entityActionsMap = new HashMap<EntityType,ArrayList<MobAction>>();
         this.pathfinderGoalField = null;
         try {
             Class<?>[] mobActions = getClasses("net.kingdomsofarden.andrew2060.invasion.api.mobskills.bundledActions");
             for(Class<?> actionClass: mobActions) {
                 try {
                     MobAction toAdd = (MobAction) actionClass.getConstructor(new Class<?>[] {}).newInstance(new Object[] {});
                     for(EntityType type : toAdd.getMobTypes()) {
                         addMobAction(type, toAdd);
                     }
                 } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                     e.printStackTrace();
                 }
             }
             Class<?>[] mobTargettingActions = getClasses("net.kingdomsofarden.andrew2060.invasion.api.mobskills.bundledtargettingactions");
             for(Class<?> actionClass: mobTargettingActions) {
                 try {
                     MobTargetSelectorAction toAdd = (MobTargetSelectorAction) actionClass.getConstructor(new Class<?>[] {}).newInstance(new Object[] {});
                     for(EntityType type : toAdd.getMobTypes()) {
                         addMobTargettingSelectorAction(type, toAdd);
                     }
                 } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                     e.printStackTrace();
                 }
             }
             Class<?> goalSelectorItemClass = Class.forName("net.minecraft.server.v1_6_R2.PathfinderGoalSelectorItem");
             pathfinderGoalField = goalSelectorItemClass.getField("a");
             pathfinderGoalField.setAccessible(true);
            
         } catch (ClassNotFoundException | IOException | NoSuchFieldException | SecurityException e) {
             e.printStackTrace();
             return;
         }
     }
     public boolean addMobAction(EntityType type, MobAction action) {
         if(this.entityActionsMap.containsKey(type)) {
             this.entityActionsMap.get(type).add(action);
         } else {
             this.entityActionsMap.put(type, new ArrayList<MobAction>());
             this.entityActionsMap.get(type).add(action);
         }
         return true;
     }
     public boolean addMobTargettingSelectorAction(EntityType type, MobTargetSelectorAction toAdd) {
         if(this.entityTargettingMap.containsKey(type)) {
             this.entityTargettingMap.get(type).add(toAdd);
         } else {
             this.entityTargettingMap.put(type, new ArrayList<MobTargetSelectorAction>());
             this.entityTargettingMap.get(type).add(toAdd);
         }
         return true;
     }
     @SuppressWarnings("rawtypes")
     public boolean registerGoals(Creature mob) {
         EntityType mobType = mob.getType();
         ArrayList<MobAction> actions = this.entityActionsMap.get(mobType);
         ArrayList<MobTargetSelectorAction> targettingActions = this.entityTargettingMap.get(mobType);
         boolean flag = false;
         if(actions != null && actions.size() > 0) {
             flag = true;
             try {
                 EntityCreature nmsEntity = ((CraftCreature)mob).getHandle();
                 Field goalSelectorField = EntityInsentient.class.getField("goalSelector");
                 goalSelectorField.setAccessible(true);
                 PathfinderGoalSelector goalSelector = (PathfinderGoalSelector) goalSelectorField.get(nmsEntity);
                 goalSelector.a(1, new PathfinderGoalMobSkillSelector(mob, actions));
                 goalSelectorField.set(nmsEntity, goalSelector);
             } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | ClassCastException e) {
                 e.printStackTrace();
                 return false;
             }            
         }
         
         if(targettingActions != null && targettingActions.size() > 0) {
             flag = true;
             try {
                 EntityCreature nmsEntity = ((CraftCreature)mob).getHandle();
                 Field targetSelectorField = EntityInsentient.class.getField("targetSelector");
                 targetSelectorField.setAccessible(true);
                 PathfinderGoalSelector targetSelector = (PathfinderGoalSelector) targetSelectorField.get(nmsEntity);
                 Field goalListField = PathfinderGoalSelector.class.getField("a");
                 goalListField.setAccessible(true);
                 List goalList = (List) goalListField.get(targetSelector);
                 Set<Object> toRemove = new HashSet<Object>();
                 for(MobTargetSelectorAction targettingAction : targettingActions) {
                     for(Class<? extends PathfinderGoal> clazz : targettingAction.getReplaces()) {
                         for(Object selectorItem : goalList) {
                             PathfinderGoal goal = (PathfinderGoal) pathfinderGoalField.get(selectorItem);
                             if(clazz.isInstance(goal)) {
                                 toRemove.add(selectorItem);
                                 break;
                             }
                         }
                     }
                 }
                 for(Object obj : toRemove) {
                     goalList.remove(obj);
                 }
                 goalListField.set(targetSelector, goalList);
                 targetSelector.a(1,new PathfinderGoalMobSkillTargetting(mob,targettingActions));
                 targetSelectorField.set(nmsEntity, targetSelector);
             } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                 e.printStackTrace();
                 return false;
             }
         }
         return flag;
     }
     
     public ArrayList<MobAction> getMobActions(Creature creature) {
         try {
             EntityCreature nmsEntity = ((CraftCreature)creature).getHandle();
             Field goalSelectorField = EntityInsentient.class.getField("goalSelector");
             goalSelectorField.setAccessible(true);
             PathfinderGoalSelector goalSelector = (PathfinderGoalSelector) goalSelectorField.get(nmsEntity);
             Field goalListField = PathfinderGoalSelector.class.getField("a");
             goalListField.setAccessible(true);
             List<?> goalList = (List<?>) goalListField.get(goalSelector);
             for(int i = 0; i < goalList.size(); i++) {
                 Object selectorItem = goalList.get(i);
                 PathfinderGoal goal = (PathfinderGoal) pathfinderGoalField.get(selectorItem);
                 if(goal instanceof PathfinderGoalMobSkillSelector) {
                     return ((PathfinderGoalMobSkillSelector)goal).getActions();
                 }
             }
         } catch(NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
             e.printStackTrace();
         }
         return null;
         
     }
     
     /**
      * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
      *
      * @param packageName The base package
      * @return The classes
      * @throws ClassNotFoundException
      * @throws IOException
      */
     private static Class<?>[] getClasses(String packageName)
             throws ClassNotFoundException, IOException {
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         assert classLoader != null;
         String path = packageName.replace('.', '/');
         Enumeration<URL> resources = classLoader.getResources(path);
         List<File> dirs = new ArrayList<File>();
         while (resources.hasMoreElements()) {
             URL resource = resources.nextElement();
             dirs.add(new File(resource.getFile()));
         }
         ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
         for (File directory : dirs) {
             classes.addAll(findClasses(directory, packageName));
         }
         return classes.toArray(new Class[classes.size()]);
     }
 
     /**
      * Recursive method used to find all classes in a given directory and subdirs.
      *
      * @param directory   The base directory
      * @param packageName The package name for classes found inside the base directory
      * @return The classes
      * @throws ClassNotFoundException
      */
     private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
         List<Class<?>> classes = new ArrayList<Class<?>>();
         if (!directory.exists()) {
             return classes;
         }
         File[] files = directory.listFiles();
         for (File file : files) {
             if (file.isDirectory()) {
                 assert !file.getName().contains(".");
                 classes.addAll(findClasses(file, packageName + "." + file.getName()));
             } else if (file.getName().endsWith(".class")) {
                 classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
             }
         }
         return classes;
     }
 }
