 package dungeon.game;
 
 import dungeon.models.*;
 import dungeon.models.messages.IdentityTransform;
 import dungeon.models.messages.Transform;
 import dungeon.ui.messages.MoveCommand;
 import dungeon.util.Vector;
 
 import java.awt.geom.Rectangle2D;
 import java.util.*;
 import java.util.logging.Logger;
 
 /**
  * The game logic.
  *
  * It works a bit like a state machine. Every time you call {@link GameLogic#pulse(double)}, it evaluates
  * what happened in the amount of time, that you pass to {@link GameLogic#pulse(double)}, with respect to the flags,
  * that are set.
  *
  * Think of commands, that the player can send (like walking up), as flags. Let's say for example the UP and LEFT flag
  * are set and you call {@code pulse(150)}. This will return transforms that correspond to moving in up-left direction
  * for 150ms.
  */
 public class GameLogic {
   private static final Logger LOGGER = Logger.getLogger(GameLogic.class.getName());
 
   private static final int SPEED = 2000;
 
   /**
    * The next available ID.
    *
    * IDs in maps may only be between 0 and 999999.
    *
    * @see #nextId()
    */
   private int nextId = 1000000;
 
   private long lastDamageTime;
 
   private final Set<Direction> activeMoveDirections = EnumSet.noneOf(Direction.class);
 
   private Direction viewingDirection = Direction.RIGHT;
 
   private boolean attacking;
 
   private boolean useIceBolt;
 
   private boolean useHealthPotion;
 
   private boolean useManaPotion;
 
   private boolean interact;
 
   private final List<Item> useItems = new ArrayList<>();
 
   /**
    * Which weapon to equip on next pulse.
    */
   private Item equipWeapon;
 
   /**
    * Which items to sell to which merchant on next pulse.
    */
   private final Map<Merchant, List<Item>> sellItems = new LinkedHashMap<>();
 
   /**
    * Which items to buy from which merchant on next pulse.
    */
   private final Map<Merchant, List<Item>> buyItems = new LinkedHashMap<>();
 
   private long lastAttackTime;
 
   private long lastManaUsedTime;
 
   private long lastManaRestoreTime;
 
 
   private GameState gameState = GameState.PLAYING;
 
   private World world;
 
   public GameLogic (World world) {
     this.world = world;
   }
 
   /**
    * Set a move flag.
    */
   public void activateMoveDirection (MoveCommand command) {
     this.activeMoveDirections.add(command.getDirection());
 
     this.viewingDirection = command.getDirection();
   }
 
   /**
    * Reset a move flag.
    */
   public void deactivateMoveDirection (MoveCommand command) {
     this.activeMoveDirections.remove(command.getDirection());
   }
 
   /**
    * Set the attacking flag.
    */
   public void activateAttack () {
     this.attacking = true;
   }
 
   /**
    * Reset the attacking flag.
    */
   public void deactivateAttack () {
     this.attacking = false;
   }
 
   /**
    * Use a health potion during the next pulse.
    */
   public void useHealthPotion () {
     this.useHealthPotion = true;
   }
 
   /**
    * Use a mana potion during the next pulse.
    */
   public void useManaPotion () {
     this.useManaPotion = true;
   }
 
   /**
    * Use {@code item} during the next pulse.
    */
   public void useItem (Item item) {
     this.useItems.add(item);
   }
 
   /**
    * Equip {@code item} during the next pulse.
    */
   public void equipWeapon (Item item) {
     this.equipWeapon = item;
   }
 
   /**
    * Set the ice bolt attacking flag.
    */
   public void activateIceBolt () {
     this.useIceBolt = true;
   }
 
   /**
    * Reset the ice bolt attacking flag.
    */
   public void deactivateIceBoltAttack () {
     this.useIceBolt = false;
   }
 
   /**
    * Sell item on next pulse.
    */
   public void sellItem (Merchant merchant, Item item) {
     List<Item> items = this.sellItems.get(merchant);
 
     if (items == null) {
       items = new ArrayList<>();
     }
 
     items.add(item);
 
     this.sellItems.put(merchant, items);
   }
 
   /**
    * Buy item on next pulse.
    */
   public void buyItem (Merchant merchant, Item item) {
     List<Item> items = this.buyItems.get(merchant);
 
     if (items == null) {
       items = new ArrayList<>();
     }
 
     items.add(item);
 
     this.buyItems.put(merchant, items);
   }
 
   /**
    * Interact with a nearby NPC on next pulse.
    */
   public void interact () {
     this.interact = true;
   }
 
   /**
    * Returns the current game state.
    *
    * You can use this to check, if the player has died, won, etc.
    */
   public GameState getGameState () {
     return this.gameState;
   }
 
   /**
    * Compute all changes, that have happened in the last #delta seconds.
    *
    * @return A transaction of all changes that have happened
    */
   public Transaction pulse (double delta) {
     Transaction transaction = new Transaction(this.world);
 
     this.handleHealthPotion(transaction);
     this.handleManaPotion(transaction);
     this.useItems(transaction);
     this.equipWeapon(transaction);
     this.sellItems(transaction);
     this.buyItems(transaction);
     this.handleMovement(transaction, delta);
     this.handleProjectiles(transaction, delta);
     this.updateViewingDirection(transaction);
     this.handleDrops(transaction);
     this.moveEnemies(transaction, delta);
     this.handleEnemies(transaction);
     this.handleEnemyLives(transaction);
     this.handleTeleporters(transaction);
     this.handleCheckpoint(transaction);
     this.handleRespawn(transaction);
     this.handleMana(transaction);
     this.handleAttack(transaction);
     this.handleIceBolt(transaction);
     this.handleInteractionWithNpcs(transaction);
 
     this.world = transaction.getWorld();
 
     this.handleDefeat();
 
     return transaction;
   }
 
   /**
    * Use a health potion if the player has one.
    */
   private void handleHealthPotion (Transaction transaction) {
     if (this.useHealthPotion) {
       this.useHealthPotion = false;
 
       List<Item> healthPotions = transaction.getWorld().getPlayer().getHealthPotions();
 
       if (healthPotions.size() > 0) {
         Item healthPotion = healthPotions.get(0);
 
         healthPotion.use(transaction);
         transaction.pushAndCommit(new Player.RemoveItemTransform(healthPotion));
       }
     }
   }
 
   /**
    * Use a mana potion if the player has one.
    */
   private void handleManaPotion (Transaction transaction) {
     if (this.useManaPotion) {
       this.useManaPotion = false;
 
       List<Item> manaPotions = transaction.getWorld().getPlayer().getManaPotions();
 
       if (manaPotions.size() > 0) {
         Item manaPotion = manaPotions.get(0);
 
         manaPotion.use(transaction);
         transaction.pushAndCommit(new Player.RemoveItemTransform(manaPotion));
       }
     }
   }
 
   /**
    * Use the items, that have been requested.
    */
   private void useItems (Transaction transaction) {
     for (Item item : this.useItems) {
       LOGGER.info("Use item " + item);
 
       item.use(transaction);
 
       transaction.pushAndCommit(new Player.RemoveItemTransform(item));
     }
 
     this.useItems.clear();
   }
 
   private void equipWeapon (Transaction transaction) {
     if (this.equipWeapon != null && this.equipWeapon.getType().isEquipable()) {
       LOGGER.info("Equip weapon " + this.equipWeapon);
 
       transaction.pushAndCommit(new Player.EquipWeaponTransform(this.equipWeapon.getId()));
 
       this.equipWeapon = null;
     }
   }
 
   private void sellItems (Transaction transaction) {
     for (Map.Entry<Merchant, List<Item>> entry : this.sellItems.entrySet()) {
       for (Item item : entry.getValue()) {
         Merchant merchant = transaction.getWorld().getCurrentRoom().findMerchant(entry.getKey());
 
         if (merchant.getMoney() >= item.getValue()) {
           transaction.pushAndCommit(new Merchant.BuyItemTransform(merchant, item));
           transaction.pushAndCommit(new Player.MoneyTransform(item.getValue()));
           transaction.pushAndCommit(new Player.RemoveItemTransform(item));
         }
       }
     }
 
     this.sellItems.clear();
   }
 
   private void buyItems (Transaction transaction) {
     for (Map.Entry<Merchant, List<Item>> entry : this.buyItems.entrySet()) {
       for (Item item : entry.getValue()) {
         Merchant merchant = transaction.getWorld().getCurrentRoom().findMerchant(entry.getKey());
 
         if (transaction.getWorld().getPlayer().getMoney() >= item.getValue()) {
           transaction.pushAndCommit(new Merchant.SellItemTransform(merchant, item));
           transaction.pushAndCommit(new Player.MoneyTransform(-item.getValue()));
           transaction.pushAndCommit(new Player.AddItemTransform(item));
         }
       }
     }
 
     this.buyItems.clear();
   }
 
   private void handleMovement (Transaction transaction, double delta) {
     Transform movementTransform = moveTransform(delta);
     movementTransform = filterWalls(movementTransform);
     Player movedPlayer = transaction.getWorld().getPlayer().apply(movementTransform);
 
     if (!this.outOfBorders(movedPlayer, transaction.getWorld().getCurrentRoom())) {
       transaction.pushAndCommit(movementTransform);
     }
   }
 
   /**
    * Create the appropriate MoveTransform with respect to the currently active directions.
    */
   private Transform moveTransform (double delta) {
     Vector finalDirection = new Vector(0, 0);
 
     for (Direction direction : this.activeMoveDirections) {
       finalDirection = finalDirection.plus(direction.getVector());
     }
 
     if (finalDirection.isZero()) {
       return new IdentityTransform();
     } else {
       finalDirection = finalDirection.normalize();
       finalDirection = finalDirection.times(SPEED * delta);
 
       return new Player.MoveTransform((int)finalDirection.getX(), (int)finalDirection.getY());
     }
   }
 
   /**
    * Prevent movement if the player would walk on a wall.
    */
   private Transform filterWalls (Transform transform) {
     Player movedPlayer = this.world.getPlayer().apply(transform);
 
     for (Tile wall : this.world.getCurrentRoom().getWalls()) {
       if (this.touch(movedPlayer, wall)) {
         return new IdentityTransform();
       }
     }
 
     return transform;
   }
 
   /**
    * Move the projectiles and collide it with walls and borders.
    */
   private void handleProjectiles (Transaction transaction, double delta) {
     Room room = transaction.getWorld().getCurrentRoom();
 
     for (Projectile projectile : room.getProjectiles()) {
       transaction.pushAndCommit(new Projectile.MoveTransform(projectile.getId(), new Position(projectile.getPosition().getVector().plus(projectile.getVelocity().times(delta)))));
 
       for (Tile wall : room.getWalls()) {
         if (this.touch(wall, projectile)) {
           transaction.pushAndCommit(new Room.RemoveProjectileTransform(room.getId(), projectile));
           break;
         }
       }
 
       if (this.outOfBorders(projectile, room)) {
         transaction.pushAndCommit(new Room.RemoveProjectileTransform(room.getId(), projectile));
         break;
       }
 
       Player player = transaction.getWorld().getPlayer();
 
       if (this.touch(player, projectile) && !player.equals(projectile.getSource())) {
         this.damagePlayer(transaction, projectile.getDamage());
         transaction.pushAndCommit(new Room.RemoveProjectileTransform(room.getId(), projectile));
         break;
       }
 
       for (Enemy enemy : room.getEnemies()) {
         if (this.touch(enemy, projectile) && !enemy.equals(projectile.getSource())) {
           this.damageEnemy(transaction, enemy, projectile.getDamage());
           transaction.pushAndCommit(new Enemy.MoveTransform(enemy, projectile.getVelocity().normalize().times(100)));
           transaction.pushAndCommit(new Room.RemoveProjectileTransform(room.getId(), projectile));
           break;
         }
       }
     }
   }
 
   private void moveEnemies (Transaction transaction, double delta) {
     for (Enemy enemy : transaction.getWorld().getCurrentRoom().getEnemies()) {
       enemy.getMoveStrategy().move(transaction, enemy, delta);
     }
   }
 
   /**
    * Update the direction the player is currently facing.
    */
   private void updateViewingDirection (Transaction transaction) {
     if (transaction.getWorld().getPlayer().getViewingDirection() != this.viewingDirection) {
       transaction.pushAndCommit(new Player.ViewingDirectionTransform(this.viewingDirection));
     }
   }
 
   /**
    * Pickup drops that the player is touching.
    */
   private void handleDrops (Transaction transaction) {
     for (Drop drop : transaction.getWorld().getCurrentRoom().getDrops()) {
       if (this.touch(transaction.getWorld().getPlayer(), drop)) {
         LOGGER.info("Pick up " + drop);
 
         transaction.push(new Room.RemoveDropTransform(drop.getId()));
 
         if (drop.isMoney()) {
           transaction.push(new Player.MoneyTransform(drop.getMoney()));
         } else {
           transaction.push(new Player.AddItemTransform(drop.getItem()));
         }
 
         transaction.commit();
       }
     }
   }
 
   /**
    * Damage player on enemy contact.
    */
   private void handleEnemies (Transaction transaction) {
     for (Enemy enemy : transaction.getWorld().getCurrentRoom().getEnemies()) {
       if (this.touch(transaction.getWorld().getPlayer(), enemy)) {
         this.damagePlayer(transaction, enemy.getStrength());
       }
     }
   }
 
   /**
    * Destroy enemies when their hit points drop below 0.
    *
    * Enemies leave a random item.
    */
   private void handleEnemyLives (Transaction transaction) {
     Room room = transaction.getWorld().getCurrentRoom();
 
     for (Enemy enemy : room.getEnemies()) {
       if (enemy.getHitPoints() <= 0) {
         transaction.pushAndCommit(new Room.RemoveEnemyTransform(enemy));
 
         ItemType[] itemTypes = ItemType.values();
         ItemType itemType = itemTypes[(new Random()).nextInt(itemTypes.length)];
 
         transaction.pushAndCommit(
           new Room.AddDropTransform(
             room.getId(),
             new Drop(
               this.nextId(),
               enemy.getPosition(),
               new Item(this.nextId(), itemType),
               0
             )
           )
         );
 
         if (enemy.getOnDeath() != null) {
           if ("VICTORY".equals(enemy.getOnDeath())) {
             this.gameState = GameState.VICTORY;
           } else {
             transaction.pushAndCommit(new Player.AdvanceLevelTransform(enemy.getOnDeath()));
           }
         }
       }
     }
   }
 
   /**
    * Create a teleport transform if the player touches a teleporter.
    */
   private void handleTeleporters (Transaction transaction) {
     for (TeleporterTile teleporter : transaction.getWorld().getCurrentRoom().getTeleporters()) {
       if (this.touch(transaction.getWorld().getPlayer(), teleporter)) {
         TeleporterTile.Target target = teleporter.getTarget();
 
         transaction.pushAndCommit(new Player.TeleportTransform(target.getRoomId(), target.getPosition()));
 
         return;
       }
     }
   }
 
   /**
    * Create a savepoint transform if the player touches a savepoint
    */
   private void handleCheckpoint (Transaction transaction) {
     Player player = transaction.getWorld().getPlayer();
 
     for (SavePoint savePoint : transaction.getWorld().getCurrentRoom().getSavePoints()) {
       if (this.touch(player, savePoint)) {
         transaction.pushAndCommit(
           new Player.SavePointTransform(player.getRoomId(), player.getPosition())
         );
 
         return;
       }
     }
   }
 
   /**
    * Reset HP when players loses a life and respawn the player if he dies
    */
   private void handleRespawn (Transaction transaction) {
     Player player = transaction.getWorld().getPlayer();
 
     if (player.getHitPoints() == 0) {
       transaction.pushAndCommit(new Player.LivesTransform(-1));
       transaction.pushAndCommit(new Player.HitpointTransform(player.getMaxHitPoints()));
       transaction.pushAndCommit(new Player.TeleportTransform(player.getSavePointRoomId(), player.getSavePointPosition()));
     }
   }
 
   /**
    * Restore mana every 5 seconds by 1
    */
   private void handleMana (Transaction transaction) {
     if (transaction.getWorld().getPlayer().getMana() < transaction.getWorld().getPlayer().getMaxMana()
       && System.currentTimeMillis() - this.lastManaRestoreTime > 5000
       && System.currentTimeMillis() - this.lastManaUsedTime > 5000) {
       this.lastManaRestoreTime = System.currentTimeMillis();
 
       transaction.pushAndCommit(new Player.ManaTransform(1));
     }
   }
 
   /**
    * Set the game state to DEFEAT when the player's hit points drop to 0.
    */
   private void handleDefeat () {
     if (this.world.getPlayer().getLives() == 0) {
       this.gameState = GameState.DEFEAT;
     }
   }
 
   /**
    * Create a new projectile if the player is attacking.
    */
   private void handleAttack (Transaction transaction) {
     if (this.attacking && System.currentTimeMillis() - this.lastAttackTime > 200) {
       this.lastAttackTime = System.currentTimeMillis();
 
       Player player = transaction.getWorld().getPlayer();
 
       Projectile projectile = player.attack(this.nextId());
 
       transaction.pushAndCommit(new Room.AddProjectileTransform(transaction.getWorld().getCurrentRoom().getId(), projectile));
     }
   }
 
   /**
    * Create a new projectile if the player is attacking with ice bolts.
    */
   private void handleIceBolt (Transaction transaction) {
     Player player = transaction.getWorld().getPlayer();
 
     if (this.useIceBolt && player.getMana() > 0) {
       this.lastManaUsedTime = System.currentTimeMillis();
       this.useIceBolt = false;
 
       Projectile projectile = player.iceBoltAttack(this.nextId());
 
       transaction.pushAndCommit(new Player.ManaTransform(-1));
       transaction.pushAndCommit(new Room.AddProjectileTransform(transaction.getWorld().getCurrentRoom().getId(), projectile));
     }
   }
 
   /**
    * Interact with a nearby NPC and merchants.
    */
   private void handleInteractionWithNpcs (Transaction transaction) {
     if (!this.interact) {
       return;
     }
 
     this.interact = false;
 
     for (NPC npc : transaction.getWorld().getCurrentRoom().getNpcs()) {
       Vector playerPosition = transaction.getWorld().getPlayer().getCenter().getVector();
       Vector npcPosition = npc.getCenter().getVector();
 
       double distance = npcPosition.minus(playerPosition).length();
 
       if (distance < (NPC.SIZE + Player.SIZE) * Math.sqrt(2) / 2) {
         transaction.pushAndCommit(new NPC.InteractTransform(npc));
         return;
       }
     }
 
     for (Merchant merchant : transaction.getWorld().getCurrentRoom().getMerchants()) {
       Vector playerPosition = transaction.getWorld().getPlayer().getCenter().getVector();
       Vector merchantPosition = merchant.getCenter().getVector();
 
       double distance = merchantPosition.minus(playerPosition).length();
 
       if (distance < (Merchant.SIZE + Player.SIZE) * Math.sqrt(2) / 2) {
         transaction.pushAndCommit(new Merchant.InteractTransform(merchant));
         return;
       }
     }
 
   }
 
   /**
    * Returns a free ID.
    */
   private int nextId () {
     return this.nextId++;
   }
 
   /**
    * Inflict {@code amount} damage on player if he has not suffered any damage lately.
    */
   private void damagePlayer (Transaction transaction, int amount) {
     if (System.currentTimeMillis() - this.lastDamageTime > 1000) {
       this.lastDamageTime = System.currentTimeMillis();
 
       transaction.pushAndCommit(new Player.HitpointTransform(-amount));
     }
   }
 
   /**
    * Inflict {@code amount} damage on {@code enemy}.
    */
   private void damageEnemy (Transaction transaction, Enemy enemy, int amount) {
     transaction.pushAndCommit(new Enemy.HitPointTransform(enemy, -amount));
   }
 
   /**
    * Helper to check if two spatial objects touch.
    */
   private boolean touch (Spatial a, Spatial b) {
     return a.space().intersects(b.space());
   }
 
   /**
    * Helper to check if a spatial object is out of the playing field.
    */
   private boolean outOfBorders (Spatial object, Room room) {
     Rectangle2D space = object.space();
 
    return space.getMinX() < 0 || space.getMinY() < 0 || space.getMaxX() > room.getXSize() || space.getMinY() > room.getYSize();
   }
 }
