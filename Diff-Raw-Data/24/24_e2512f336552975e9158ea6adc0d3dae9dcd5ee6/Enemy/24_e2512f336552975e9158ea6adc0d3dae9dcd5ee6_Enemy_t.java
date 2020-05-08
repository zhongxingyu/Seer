 package dungeon.models;
 
 import dungeon.game.Transaction;
 import dungeon.models.messages.Transform;
 import dungeon.util.Vector;
 
 import java.awt.geom.Rectangle2D;
 import java.util.List;
 import java.util.Random;
 
 public class Enemy implements Spatial, Identifiable {
   public static final int SIZE = 1000;
 
   private final int id;
 
   private final int hitPoints;
 
   /**
    * How much damage the enemy inflicts on the player.
    */
   private final int strength = 1;
 
   private final int speed;
 
   /**
    * The enemy's position in the room.
    */
   private final Position position;
 
   private final MoveStrategy moveStrategy;
 
   private final String onDeath;
 
   public Enemy (int id, int hitPoints, int speed, Position position, MoveStrategy moveStrategy, String onDeath) {
     this.id = id;
     this.hitPoints = hitPoints;
     this.speed = speed;
     this.position = position;
     this.moveStrategy = moveStrategy;
     this.onDeath = onDeath;
   }
 
   public int getId () {
     return this.id;
   }
 
   public int getHitPoints () {
     return this.hitPoints;
   }
 
   public int getStrength () {
     return this.strength;
   }
 
   public int getSpeed () {
    return this.speed;
   }
 
   public Position getPosition () {
     return this.position;
   }
 
   public MoveStrategy getMoveStrategy () {
     return this.moveStrategy;
   }
 
   /**
    * What should happen when the enemy dies?
    *
    * null => nothing
    * "VICTORY" => victory
    * "level-id#room-id" => advance to level "level-id" and room "room-id"
    */
   public String getOnDeath () {
     return this.onDeath;
   }
 
   public Enemy apply (Transform transform) {
     int id = this.id;
     int hitPoints = this.hitPoints;
    int speed = this.speed;
     Position position = this.position;
     MoveStrategy moveStrategy = this.moveStrategy;
     String onDeath = this.onDeath;
 
     if (transform instanceof HitPointTransform && this.equals(((HitPointTransform)transform).enemy)) {
       hitPoints += ((HitPointTransform)transform).delta;
     } else if (transform instanceof MoveTransform && this.equals(((MoveTransform)transform).enemy)) {
       position = new Position(position.getVector().plus(((MoveTransform)transform).delta));
     } else if (transform instanceof TeleportTransform && this.equals(((TeleportTransform)transform).enemy)) {
       position = ((TeleportTransform)transform).position;
     }
 
     return new Enemy(id, hitPoints, speed, position, moveStrategy, onDeath);
   }
 
   @Override
   public Rectangle2D space () {
     return new Rectangle2D.Float(this.position.getX(), this.position.getY(), SIZE, SIZE);
   }
 
   @Override
   public Position getCenter () {
     return new Position(this.position.getVector().plus(new Vector(SIZE / 2, SIZE / 2)));
   }
 
   @Override
   public boolean equals (Object o) {
     if (this == o) {
       return true;
     }
 
     if (o == null || getClass() != o.getClass()) {
       return false;
     }
 
     Enemy enemy = (Enemy)o;
 
     if (this.id != enemy.id) {
       return false;
     }
 
     return true;
   }
 
   @Override
   public int hashCode () {
     return this.id;
   }
 
   public static class HitPointTransform implements Transform {
     private final Enemy enemy;
 
     private final int delta;
 
     public HitPointTransform (Enemy enemy, int delta) {
       this.enemy = enemy;
       this.delta = delta;
     }
   }
 
   public static class MoveTransform implements Transform {
     private final Enemy enemy;
 
     private final Vector delta;
 
     public MoveTransform (Enemy enemy, Vector delta) {
       this.enemy = enemy;
       this.delta = delta;
     }
   }
 
   public static class TeleportTransform implements Transform {
     private final Enemy enemy;
 
     private final Position position;
 
     public TeleportTransform (Enemy enemy, Position position) {
       this.enemy = enemy;
       this.position = position;
     }
   }
 
   public static enum MoveStrategy {
     NORMAL {
       @Override
       public void move (Transaction transaction, Enemy enemy, double delta) {
         Vector enemyToPlayer = this.getShortestDistance(enemy, transaction.getWorld().getPlayers());
 
         if (enemyToPlayer.length() < 5000 && !enemyToPlayer.isZero()) {
           transaction.pushAndCommit(new Enemy.MoveTransform(enemy, enemyToPlayer.normalize().times(enemy.getSpeed() * delta)));
         }
       }
     },
     TELEPORT {
       @Override
       public void move (Transaction transaction, Enemy enemy, double delta) {
         Player targetPlayer = this.getClosestPlayer(enemy, transaction.getWorld().getPlayers());
         Vector enemyToPlayer = targetPlayer.getPosition().getVector().minus(enemy.getPosition().getVector());
 
         if (enemyToPlayer.length() < 500 || RANDOM.nextInt(300) == 0) {
           Room room = transaction.getWorld().getCurrentRoom(targetPlayer);
           Vector position = new Vector(RANDOM.nextFloat() * room.getXSize(), RANDOM.nextFloat() * room.getYSize());
           position = position.times(0.5 + RANDOM.nextFloat() / 2);
 
           transaction.pushAndCommit(new TeleportTransform(enemy, new Position(position)));
         } else {
           transaction.pushAndCommit(new Enemy.MoveTransform(enemy, enemyToPlayer.normalize().times(enemy.getSpeed() * delta)));
         }
       }
     };
 
     private static final Random RANDOM = new Random();
 
     public abstract void move (Transaction transaction, Enemy enemy, double delta);
 
     /**
      * Finds the {@link Player}, that is closest to {@code enemy}.
      */
     Player getClosestPlayer (Enemy enemy, List<Player> players) {
       Player closest = players.get(0);
       double shortestDistance = closest.getPosition().getVector().minus(enemy.getPosition().getVector()).length();
 
       for (Player player : players) {
         double distance = player.getPosition().getVector().minus(enemy.getPosition().getVector()).length();
 
         if (distance < shortestDistance) {
           shortestDistance = distance;
           closest = player;
         }
       }
 
       return closest;
     }
 
     /**
      * Finds the shortest distance between {@code enemy} and any of {@code players}.
      *
      * @return A vector pointing from {@code enemy} to the {@link Player}, that is closest to it
      */
     Vector getShortestDistance (Enemy enemy, List<Player> players) {
       Player closest = this.getClosestPlayer(enemy, players);
 
       return closest.getPosition().getVector().minus(enemy.getPosition().getVector());
     }
   }
 }
