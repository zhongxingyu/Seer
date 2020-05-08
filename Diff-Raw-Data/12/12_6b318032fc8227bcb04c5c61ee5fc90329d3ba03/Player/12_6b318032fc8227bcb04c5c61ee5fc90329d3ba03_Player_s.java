 package dungeon.models;
 
 import dungeon.models.messages.Transform;
 import dungeon.util.Vector;
 
 import java.awt.geom.Rectangle2D;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class Player implements Spatial, Identifiable, Serializable {
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
    * Which weapon has the player equipped?
    */
   private final int weaponId;
 
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
 
   public Player (int id, String name, int lives, int hitPoints, int maxHitPoints, int money, int mana, int maxMana, List<Item> items, String levelId, String roomId, int weaponId, Position position, Direction viewingDirection, String savePointRoomId, Position savePointPosition) {
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
     this.weaponId = weaponId;
     this.position = position;
     this.viewingDirection = viewingDirection;
     this.savePointRoomId = savePointRoomId;
     this.savePointPosition = savePointPosition;
   }
 
   /**
    * Creates a new level 1 player with default values.
    */
   public Player (int id, String name) {
     this(id, name, 3, 5, 5, 0, 10, 10, new ArrayList<Item>(), "level-1", "room-1", 0, new Position(0, 0), Direction.RIGHT, "room-1", new Position(0, 0));
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
 
   public int getWeaponId () {
     return this.weaponId;
   }
 
   public Item getWeapon () {
     for (Item item : this.getItems()) {
       if (item.getId() == this.getWeaponId()) {
         return item;
       }
     }
 
     return null;
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
 
   @Override
   public Position getCenter () {
     return new Position(this.position.getVector().plus(new Vector(SIZE / 2, SIZE / 2)));
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
    * @return a list of all mana potions in the player's bag.
    */
   public List<Item> getManaPotions () {
     List<Item> manaPotions = new ArrayList<>();
 
     for (Item item : this.items) {
       if (item.getType() == ItemType.MANA_POTION) {
         manaPotions.add(item);
       }
     }
 
     return manaPotions;
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
     int damageBonus = 0;
     int speedBonus = 0;
 
     if (this.getWeapon() != null) {
       damageBonus = this.getWeapon().getType().getDamageDelta();
 
       if (this.getWeapon().getType() == ItemType.WEAK_BOW) {
         speedBonus = 1000;
       } else if (this.getWeapon().getType() == ItemType.STRONG_BOW) {
         speedBonus = 2000;
       }
     }
 
     return this.createProjectile(id, 5000 + speedBonus, 1 + damageBonus, DamageType.NORMAL);
   }
 
   public Projectile iceBoltAttack (int id) {
     return this.createProjectile(id, 7000, 2, DamageType.ICE);
   }
 
   public Player apply (Transform transform) {
     if (transform instanceof PlayerTransform) {
       return ((PlayerTransform)transform).apply(this);
     } else {
       return this;
     }
   }
 
   /**
    * Describes a general transformation of a player.
    */
   private static class PlayerTransform implements Transform {
     private final int playerId;
 
     private final boolean appliesToAll;
 
     /**
      * @param player To which player should this apply? It applies to all, if you pass null.
      */
     public PlayerTransform (Player player) {
       if (player == null) {
         this.playerId = 0;
         this.appliesToAll = true;
       } else {
         this.playerId = player.id;
         this.appliesToAll = false;
       }
     }
 
     public Player apply (Player player) {
       if (this.appliesToAll || player.id == this.playerId) {
         return new Player(
           this.id(player),
           this.name(player),
           this.lives(player),
           this.hitPoints(player),
           this.maxHitPoints(player),
           this.money(player),
           this.mana(player),
           this.maxMana(player),
           this.items(player),
           this.levelId(player),
           this.roomId(player),
           this.weaponId(player),
           this.position(player),
           this.viewingDirection(player),
           this.savePointRoomId(player),
           this.savePointPosition(player)
         );
       } else {
         return player;
       }
     }
 
     protected int id (Player player) {
       return player.id;
     }
 
     protected String name (Player player) {
       return player.name;
     }
 
     protected int lives (Player player) {
       return player.lives;
     }
 
     protected int hitPoints (Player player) {
       return player.hitPoints;
     }
 
     protected int maxHitPoints (Player player) {
       return player.maxHitPoints;
     }
 
     protected int money (Player player) {
       return player.money;
     }
 
     protected int mana (Player player) {
       return player.mana;
     }
 
     protected int maxMana (Player player) {
       return player.maxMana;
     }
 
     protected List<Item> items (Player player) {
       return player.items;
     }
 
     protected String levelId (Player player) {
       return player.levelId;
     }
 
     protected String roomId (Player player) {
       return player.roomId;
     }
 
     protected int weaponId (Player player) {
       return player.weaponId;
     }
 
     protected Position position (Player player) {
       return player.position;
     }
 
     protected Direction viewingDirection (Player player) {
       return player.viewingDirection;
     }
 
     protected String savePointRoomId (Player player) {
       return player.savePointRoomId;
     }
 
     protected Position savePointPosition (Player player) {
       return player.savePointPosition;
     }
   }
 
   public static class MoveTransform extends PlayerTransform {
     private final Vector delta;
 
     public MoveTransform (Player player, Vector delta) {
       super(player);
 
       this.delta = delta;
     }
 
     @Override
     protected Position position (Player player) {
       return new Position(player.position.getVector().plus(this.delta));
     }
   }
 
   public static class HitPointTransform extends PlayerTransform {
     private final int delta;
 
     public HitPointTransform (Player player, int delta) {
       super(player);
 
       this.delta = delta;
     }
 
     @Override
     protected int hitPoints (Player player) {
       return Math.max(Math.min(player.hitPoints + this.delta, player.maxHitPoints), 0);
     }
   }
 
   public static class LivesTransform extends PlayerTransform {
     private final int delta;
 
     public LivesTransform (Player player, int delta) {
       super(player);
 
       this.delta = delta;
     }
 
     @Override
     protected int lives (Player player) {
      return Math.max(0, player.lives + delta);
     }
   }
 
   public static class TeleportTransform extends PlayerTransform {
     private final String roomId;
 
     private final Position position;
 
     public TeleportTransform (Player player, String roomId, Position position) {
       super(player);
 
       this.roomId = roomId;
       this.position = position;
     }
 
     @Override
     protected String roomId (Player player) {
       return this.roomId;
     }
 
     @Override
     protected Position position (Player player) {
       return this.position;
     }
   }
 
   /**
    * This has no playerId, because all players have the same save point.
    */
   public static class SavePointTransform extends PlayerTransform {
     private final String roomId;
 
     private final Position position;
 
     public SavePointTransform (String roomId, Position position) {
       super(null);
 
       this.roomId = roomId;
       this.position = position;
     }
 
     @Override
     protected String savePointRoomId (Player player) {
       return this.roomId;
     }
 
     @Override
     protected Position savePointPosition (Player player) {
       return this.position;
     }
   }
 
   public static class MoneyTransform extends PlayerTransform {
     private final int delta;
 
     public MoneyTransform (Player player, int delta) {
       super(player);
 
       this.delta = delta;
     }
 
     @Override
     protected int money (Player player) {
      return Math.max(0, player.money + delta);
     }
   }
 
   public static class AddItemTransform extends PlayerTransform {
     private final Item item;
 
     public AddItemTransform (Player player, Item item) {
       super(player);
 
       this.item = item;
     }
 
     @Override
     protected List<Item> items (Player player) {
       List<Item> items = new ArrayList<>(player.items);
       items.add(this.item);
       return items;
     }
   }
 
   public static class ManaTransform extends PlayerTransform {
     private final int delta;
 
     public ManaTransform (Player player, int delta) {
       super(player);
 
       this.delta = delta;
     }
 
     @Override
     protected int mana (Player player) {
       return Math.max(Math.min(player.mana + this.delta, player.maxMana), 0);
     }
   }
 
   public static class RemoveItemTransform extends PlayerTransform {
     private final Item item;
 
     public RemoveItemTransform (Player player, Item item) {
       super(player);
 
       this.item = item;
     }
 
     @Override
     protected List<Item> items (Player player) {
       List<Item> items = new ArrayList<>();
 
       for (Item item : player.items) {
         if (!item.equals(this.item)) {
           items.add(item);
         }
       }
 
       return items;
     }
   }
 
   public static class EquipWeaponTransform extends PlayerTransform {
     private final int weaponId;
 
     public EquipWeaponTransform (Player player, int weaponId) {
       super(player);
 
       this.weaponId = weaponId;
     }
 
     @Override
     protected int weaponId (Player player) {
       return this.weaponId;
     }
   }
 
   public static class ViewingDirectionTransform extends PlayerTransform {
     private final Direction direction;
 
     public ViewingDirectionTransform (Player player, Direction direction) {
       super(player);
 
       this.direction = direction;
     }
 
     @Override
     protected Direction viewingDirection (Player player) {
       return this.direction;
     }
   }
 
   /**
    * This has no playerId, because when one player advances, all do.
    */
   public static class AdvanceLevelTransform extends PlayerTransform {
     private final String levelId;
 
     private final String roomId;
 
     public AdvanceLevelTransform (String levelId, String roomId) {
       super(null);
 
       this.levelId = levelId;
       this.roomId = roomId;
     }
 
     @Override
     protected String levelId (Player player) {
       return this.levelId;
     }
 
     @Override
     protected String roomId (Player player) {
       return this.roomId;
     }
 
     @Override
     protected Position position (Player player) {
       return Position.ZERO;
     }
 
     @Override
     protected String savePointRoomId (Player player) {
       return this.roomId;
     }
 
     @Override
     protected Position savePointPosition (Player player) {
       return Position.ZERO;
     }
   }
 
   public static class RespawnTransform extends PlayerTransform {
     public RespawnTransform (Player player) {
       super(player);
     }
 
     @Override
     protected int lives (Player player) {
       return player.lives - 1;
     }
 
     @Override
     protected int hitPoints (Player player) {
       return player.maxHitPoints;
     }
 
     @Override
     protected String roomId (Player player) {
       return player.savePointRoomId;
     }
 
     @Override
     protected Position position (Player player) {
       return player.savePointPosition;
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
