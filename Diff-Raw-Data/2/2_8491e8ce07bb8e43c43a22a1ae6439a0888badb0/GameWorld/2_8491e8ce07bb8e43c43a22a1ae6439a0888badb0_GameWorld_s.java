 package org.ludumdare24.world;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 import org.gameflow.entity.Entity;
 import org.gameflow.utils.MathTools;
 import org.ludumdare24.MainGame;
 import org.ludumdare24.Mutator;
 import org.ludumdare24.entities.*;
 import org.ludumdare24.entities.creature.Creature;
 import org.ludumdare24.screens.GameScreen;
 
 import java.util.Random;
 
 /**
  *
  */
 public class GameWorld {
 
     private static final int MAX_CREATURES_COUNT = 300;
     private static final int FOOD_SPREAD = 15;
     private static final int MAX_FOOD_ENTITIES_COUNT = 100;
     private final int initialPlayerCreatureCount = 5;
     private final int initialUngodlyCreatureCount = 7;
     private final int initialTreeCount = 16;
 
     private PlayerGod player;
 
     private Array<Creature> creatures = new Array<Creature>();
     private Array<AppleTree> appleTrees = new Array<AppleTree>();
     private Array<FoodEntity> foodEntities = new Array<FoodEntity>();
 
     private Array<Entity> entitiesToRemove = new Array<Entity>();
     private Array<Entity> entitiesToAdd = new Array<Entity>();
 
     private Array<WorldListener> worldListeners = new Array<WorldListener>();
     private Random random = new Random();
 
     private Mutator mutator = new Mutator(random);
 
     public void create(MainGame game) {
         // Create player
         player = new PlayerGod(game);
 
         createTribe(game, 400, 200, player, initialPlayerCreatureCount);
 
         createEnemyTribes(game);
 
 
         // Create some trees
         for (int i = 0; i < initialTreeCount; i++) {
             AppleTree tree = new AppleTree(this, random);
             tree.setWorldPos(random.nextFloat() * 1100, random.nextFloat() * 800);
             appleTrees.add(tree);
         }
     }
 
     public void createEnemyTribes(MainGame game) {
         float cx = 400;
         float cy = 220;
 
         float d = 2.8f;
 
         createTribe(game, cx, cy + cy*d, null, initialUngodlyCreatureCount);
         createTribe(game, cx, cy - cy*d, null, initialUngodlyCreatureCount);
         createTribe(game, cx + cx*d, cy, null, initialUngodlyCreatureCount);
         createTribe(game, cx - cx*d, cy, null, initialUngodlyCreatureCount);
     }
 
     private void createTribe(MainGame game, float x, float y, God god, int tribeSize) {
 
         // Place move target
         if (god != null) god.placeMoveTarget(x, y);
 
         // Tribe mother
        Creature tribeMother = createCreature(null, god, x, y, null);
 
         // Spawn members based on mother
         for (int i = 0; i < tribeSize; i++) {
             createCreature(game, god, x, y, tribeMother);
         }
     }
 
     private Creature createCreature(MainGame game, God god, float x, float y, Creature mother) {
 
         Creature creature;
         if (mother != null) {
             creature = new Creature(game, this, mutator, mother);
         }
         else {
             creature = new Creature(game, this, god, mutator);
         }
 
         float x2 = x + (float)(random.nextGaussian() * 50);
         float y2 = y + (float )(random.nextGaussian() * 35);
         creature.setWorldPos(x2, y2);
 
         addEntity(creature);
 
         // Count creature for god
         if (god != null) {
             god.addFollower(creature);
         }
 
         return creature;
     }
 
     /**
      * Adds food to the game world at some pos.
      */
     public void spawnFood(FoodType foodType, float x, float y, double totalEnergy) {
         if (totalEnergy > 0) {
             // Calculate how many food items to spawn
             int num = Math.max(1, (int) (totalEnergy / foodType.getEnergyInOne())); // At least one
 
             // Calculate how much energy in each food item
             double energyPerFood = totalEnergy / num;
             double energyPart = energyPerFood / foodType.getEnergyInOne();
 
             for (int i = 0; i < num && foodEntities.size < MAX_FOOD_ENTITIES_COUNT; i++) {
                 // Create a food item
                 FoodEntity foodEntity = new FoodEntity(this, random, foodType, energyPart);
 
                 // Put it close to the target point
                 foodEntity.setWorldPos(
                         x + (float) random.nextGaussian() * FOOD_SPREAD,
                         y + (float) random.nextGaussian() * FOOD_SPREAD);
 
                 addEntity(foodEntity);
             }
         }
     }
 
     public void showOnScreen(GameScreen gameScreen) {
         // Show hud
         gameScreen.addEntity(player);
 
         // Add creatures
         for (Creature creature : creatures) {
             gameScreen.addEntity(creature);
         }
 
         // Add trees
         for (AppleTree appleTree : appleTrees) {
             gameScreen.addEntity(appleTree);
         }
     }
 
     public void update(float durationSeconds) {
         // Remove entities to remove
         for (Entity entity : entitiesToRemove) {
             // Remove from correct list
             if (Creature.class.isInstance(entity)) creatures.removeValue((Creature) entity, true);
             else if (AppleTree.class.isInstance(entity)) appleTrees.removeValue((AppleTree) entity, true);
             else if (FoodEntity.class.isInstance(entity)) foodEntities.removeValue((FoodEntity) entity, true);
             else throw new IllegalArgumentException("Unknown entity type " + entity.getClass());
 
             notifyEntityRemoved(entity);
         }
         entitiesToRemove.clear();
 
         // Add entities to add
         for (Entity entity : entitiesToAdd) {
             // Add to correct list
             if (Creature.class.isInstance(entity)) creatures.add((Creature) entity);
             else if (AppleTree.class.isInstance(entity)) appleTrees.add((AppleTree) entity);
             else if (FoodEntity.class.isInstance(entity)) foodEntities.add((FoodEntity) entity);
             else throw new IllegalArgumentException("Unknown entity type " + entity.getClass());
 
             notifyEntityAdded(entity);
         }
         entitiesToAdd.clear();
     }
 
     public Creature getClosestCreature(float x, float y, Creature exceptThis) {
         return (Creature) findClosestEntity(x, y, null, creatures);
     }
 
     public Creature getClosestCreatureOfGod(float x, float y, God god) {
         float closestDistance = Float.POSITIVE_INFINITY;
         Creature closestEntity = null;
         for (Creature entity : creatures) {
             if (entity.getGod() == god) {
                 Vector2 worldPos = entity.getWorldPos();
                 float distance = MathTools.distanceSquared(worldPos.x, worldPos.y, x, y);
                 if (distance < closestDistance) {
                     closestDistance = distance;
                     closestEntity = entity;
                 }
             }
         }
         return closestEntity;
     }
 
     public Creature getClosestCreature(float x, float y, Creature exceptThis, float withinDistance) {
         return (Creature) findClosestEntity(x, y, exceptThis, creatures, withinDistance);
     }
 
     public FoodEntity getClosestFood(float x, float y) {
         return (FoodEntity) findClosestEntity(x, y, null, foodEntities);
     }
 
     public FoodEntity getClosestFood(float x, float y, float withinDistance) {
         return (FoodEntity) findClosestEntity(x, y, null, foodEntities, withinDistance);
     }
 
     public AppleTree getClosestAppleTree(float x, float y) {
         return (AppleTree) findClosestEntity(x, y, null, appleTrees);
     }
 
     public AppleTree getClosestAppleTree(float x, float y, float withinDistance) {
         return (AppleTree) findClosestEntity(x, y, null, appleTrees, withinDistance);
     }
 
     private WorldEntity findClosestEntity(float x, float y, WorldEntity exceptThis, Array<? extends WorldEntity> entities, float withinDistance) {
         WorldEntity closestEntity = findClosestEntity(x, y, exceptThis, entities);
         if (closestEntity != null) {
             float distance = MathTools.distance(closestEntity.getX(), closestEntity.getY(), x, y);
             if (distance > withinDistance) return null; // Too far away
         }
         return closestEntity;
     }
 
     private WorldEntity findClosestEntity(float x, float y, WorldEntity exceptThis, Array<? extends WorldEntity> entities) {
         float closestDistance = Float.POSITIVE_INFINITY;
         WorldEntity closestEntity = null;
         for (WorldEntity entity : entities) {
             if (entity != exceptThis) {
                 Vector2 worldPos = entity.getWorldPos();
                 float distance = MathTools.distanceSquared(worldPos.x, worldPos.y, x, y);
                 if (distance < closestDistance) {
                     closestDistance = distance;
                     closestEntity = entity;
                 }
             }
         }
         return closestEntity;
     }
 
     public void removeEntity(Entity entity) {
         if (!entitiesToRemove.contains(entity, true)) {
             entitiesToRemove.add(entity);
         }
     }
 
     public void addEntity(Entity entity) {
         if (!entitiesToAdd.contains(entity, true)) entitiesToAdd.add(entity);
     }
 
     public void addListener(WorldListener value) {
         worldListeners.add(value);
     }
 
     public void removeListener(WorldListener value) {
         worldListeners.removeValue(value, true);
     }
 
     public int getNumberOfCreatures() {
         return creatures.size;
     }
 
     private void notifyEntityAdded(Entity entity) {
         for (WorldListener worldListener : worldListeners) {
             worldListener.onEntityCreated(entity);
         }
     }
 
     private void notifyEntityRemoved(Entity entity) {
         for (WorldListener worldListener : worldListeners) {
             worldListener.onEntityRemoved(entity);
         }
     }
 
     public Random getRandom() {
         return random;
     }
 
     public Mutator getMutator() {
         return mutator;
     }
 
     public boolean canAddCreatures() {
         return creatures.size < MAX_CREATURES_COUNT;
     }
 }
