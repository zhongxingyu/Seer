 package dungeon.models;
 
 import dungeon.models.messages.Transform;
 import dungeon.util.Vector;
 
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class Player implements Spatial, Identifiable {
   public static final int SIZE = 900;
 
   private final int id;
 
   private final String name;
 
   private final int lives;
 
   private final int hitPoints;
 
   private final int maxHitPoints;
 
   private final int mana;
 
   private final int maxMana;
 
   /**
    * The amount of money the player has.
    */
   private final int money;
 
   /**
    * The items in the player's bag.
    */
   private final List<Item> items;
 
   /**
    * Which level is the player currently in?
    */
   private final String levelId;
 
   /**
    * Which room is the player currently in?
    */
   private final String roomId;
 
   /**
    * His position in the room.
    */
   private final Position position;
 
   /**
    * In which direction is the player looking?
    */
   private final Direction viewingDirection;
 
   /**
    * In which room was the player, when he activated the current save point?
    *
    * Whenever the player enters a new level, this should be reset to the starting room's id.
    */
   private final String savePointRoomId;
 
   /**
    * At which position was the player, when he activated the current save point?
    *
    * Whenever the player enters a new level, this should be reset to the player's position in the starting room.
    */
   private final Position savePointPosition;
 
   public Player (int id, String name, int lives, int hitPoints, int maxHitPoints, int money, int mana, int maxMana, List<Item> items, String levelId, String roomId, Position position, Direction viewingDirection, String savePointRoomId, Position savePointPosition) {
     this.id = id;
     this.name = name;
     this.lives = lives;
     this.hitPoints = hitPoints;
     this.maxHitPoints = maxHitPoints;
     this.money = money;
     this.mana = mana;
     this.maxMana = maxMana;
     this.items = Collections.unmodifiableList(new ArrayList<>(items));
     this.levelId = levelId;
     this.roomId = roomId;
     this.position = position;
     this.viewingDirection = viewingDirection;
     this.savePointRoomId = savePointRoomId;
     this.savePointPosition = savePointPosition;
   }
 
   public int getId () {
     return this.id;
   }
 
   public String getName () {
     return this.name;
   }
 
   public int getLives () {
     return this.lives;
   }
 
   public int getHitPoints () {
     return this.hitPoints;
   }
 
   public int getMaxHitPoints () {
     return this.maxHitPoints;
   }
 
   public int getMoney () {
     return this.money;
   }
 
   public int getMana () {
     return this.mana;
   }
 
   public int getMaxMana () {
     return this.maxMana;
   }
 
   public List<Item> getItems () {
     return this.items;
   }
 
   public String getLevelId () {
     return this.levelId;
   }
 
   public String getRoomId () {
     return this.roomId;
   }
 
   public Position getPosition () {
     return this.position;
   }
 
   public Direction getViewingDirection () {
     return this.viewingDirection;
   }
 
   public String getSavePointRoomId () {
     return this.savePointRoomId;
   }
 
   public Position getSavePointPosition () {
     return this.savePointPosition;
   }
 
   public Rectangle2D space () {
     return new Rectangle2D.Float(this.position.getX(), this.position.getY(), SIZE, SIZE);
   }
 
   /**
    * @return a list of all health potions in the player's bag.
    */
   public List<Item> getHealthPotions () {
     List<Item> healthPotions = new ArrayList<>();
 
     for (Item item : this.items) {
       if (item.getType() == ItemType.HEALTH_POTION) {
         healthPotions.add(item);
       }
     }
 
     return healthPotions;
   }
 
   /**
    * Returns a projectile that the player shoots.
    *
    * This means that the projectile is moving in the viewing direction and shot from the "hip".
    */
   private Projectile createProjectile (int id, int speed, int damage, DamageType type) {
     Position position = new Position(
       this.position.getVector()
         .plus(new Vector(SIZE / 2, SIZE / 2))
         .plus(new Vector(-Projectile.SIZE / 2, -Projectile.SIZE / 2))
         .plus(
           this.viewingDirection.getVector().times(SIZE / 2)
         )
     );
 
     return new Projectile(id, this, position, this.viewingDirection.getVector().times(speed), damage, type);
   }
 
   public Projectile attack (int id) {
     return createProjectile(id, 5000, 1, DamageType.NORMAL);
   }
 
   public Projectile iceBoltAttack (int id) {
     return createProjectile(id, 7000, 2, DamageType.ICE);
   }
 
   public Player apply (Transform transform) {
     int id = this.id;
     String name = this.name;
     int lives = this.lives;
     int hitPoints = this.hitPoints;
     int maxHitPoints = this.maxHitPoints;
     int money = this.money;
     int mana = this.mana;
     int maxMana = this.maxMana;
     List<Item> items = this.items;
     String levelId = this.levelId;
     String roomId = this.roomId;
     Position position = this.position;
     Direction viewingDirection = this.viewingDirection;
     String savePointRoomId = this.savePointRoomId;
     Position savePointPosition = this.savePointPosition;
 
     if (transform instanceof MoveTransform) {
       MoveTransform move = (MoveTransform)transform;
 
       position = new Position(this.position.getX() + move.xDelta, this.position.getY() + move.yDelta);
     } else if (transform instanceof ViewingDirectionTransform) {
       viewingDirection = ((ViewingDirectionTransform)transform).direction;
     } else if (transform instanceof HitpointTransform) {
       HitpointTransform hpTransform = (HitpointTransform)transform;
 
       hitPoints = Math.max(Math.min(hitPoints + hpTransform.delta, this.maxHitPoints), 0);
     } else if (transform instanceof LivesTransform) {
       LivesTransform livesTransform = (LivesTransform)transform;
 
       lives += livesTransform.delta;
     } else if (transform instanceof TeleportTransform) {
       TeleportTransform teleportTransform = (TeleportTransform)transform;
 
       roomId = teleportTransform.roomId;
       position = teleportTransform.position;
     } else if (transform instanceof SavePointTransform) {
       SavePointTransform savePointTransform = (Player.SavePointTransform)transform;
 
       savePointRoomId = savePointTransform.roomId;
       savePointPosition = savePointTransform.position;
     } else if (transform instanceof MoneyTransform) {
       money += ((MoneyTransform)transform).delta;
     } else if (transform instanceof AddItemTransform) {
       items = new ArrayList<>(items);
       items.add(((AddItemTransform)transform).item);
     } else if (transform instanceof ManaTransform) {
       ManaTransform manaTransform = (Player.ManaTransform)transform;
 
      mana += manaTransform.delta;
     } else if (transform instanceof RemoveItemTransform) {
       items = new ArrayList<>();
 
       for (Item item : this.items) {
         if (!item.equals(((RemoveItemTransform)transform).item)) {
           items.add(item);
         }
       }
     }
 
     return new Player(id, name, lives, hitPoints, maxHitPoints, money, mana, maxMana, items, levelId, roomId, position, viewingDirection, savePointRoomId, savePointPosition);
   }
 
   public static class MoveTransform implements Transform {
     private final int xDelta;
 
     private final int yDelta;
 
     public MoveTransform (int xDelta, int yDelta) {
       this.xDelta = xDelta;
       this.yDelta = yDelta;
     }
   }
 
   public static class HitpointTransform implements Transform {
     private final int delta;
 
     public HitpointTransform (int delta) {
       this.delta = delta;
     }
   }
 
   public static class LivesTransform implements Transform {
     private final int delta;
 
     public LivesTransform (int delta) {
       this.delta = delta;
     }
   }
 
   public static class TeleportTransform implements Transform {
     private final String roomId;
 
     private final Position position;
 
     public TeleportTransform (String roomId, Position position) {
       this.roomId = roomId;
       this.position = position;
     }
   }
 
   public static class SavePointTransform implements Transform {
     private final String roomId;
 
     private final Position position;
 
     public SavePointTransform (String roomId, Position position) {
       this.roomId = roomId;
       this.position = position;
     }
   }
 
   public static class MoneyTransform implements Transform {
     private final int delta;
 
     public MoneyTransform (int delta) {
       this.delta = delta;
     }
   }
 
   public static class AddItemTransform implements Transform {
     private final Item item;
 
     public AddItemTransform (Item item) {
       this.item = item;
     }
   }
 
   public static class ManaTransform implements Transform {
     private final int delta;
 
     public ManaTransform (int delta) {
       this.delta = delta;
     }
   }
 
   public static class RemoveItemTransform implements Transform {
     private final Item item;
 
     public RemoveItemTransform (Item item) {
       this.item = item;
     }
   }
 
   public static class ViewingDirectionTransform implements Transform {
     private final Direction direction;
 
     public ViewingDirectionTransform (Direction direction) {
       this.direction = direction;
     }
   }
 
   @Override
   public boolean equals (Object o) {
     if (this == o) {
       return true;
     }
 
     if (o == null || getClass() != o.getClass()) {
       return false;
     }
 
     Player player = (Player)o;
 
     if (this.id != player.id) {
       return false;
     }
 
     return true;
   }
 
   @Override
   public int hashCode () {
     return this.id;
   }
 }
