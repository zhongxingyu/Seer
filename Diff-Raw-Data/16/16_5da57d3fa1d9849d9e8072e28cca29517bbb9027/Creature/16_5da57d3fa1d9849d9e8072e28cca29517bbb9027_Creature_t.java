 package ogo.spec.game.model;
 
 import java.util.Set;
 
 /**
  * Creature class
  * @author sjef
  */
 public abstract class Creature extends Inhabitant {
 
     //Time between strikes
     public static final int ATTACK_COOLDOWN_TICKS = 20;
     //Time it takes to move to next tile at fastest speed.
     public static final int TICKS_PER_TILE_FAST = 10;
     //Avg speed is 2 * fast
     public static final int TICKS_PER_TILE_AVG = TICKS_PER_TILE_FAST * 2;
     //Slow speed is 3 * fast
     public static final int TICKS_PER_TILE_SLOW = TICKS_PER_TILE_FAST * 3;
     public static final int MAX_LIFE = 20;
     protected Creature attackingCreature;
     private int life;
     private CreaturePath path;
     protected int moveCooldown;
     protected int attackCooldown;
     protected int lifeCooldown;

     protected long tick;
 
     /**
      * Big ass constructor
      */
     public Creature(Tile currentTile, GameMap map, int id) {
         super(id);
         this.moveCooldown = -1;
         this.attackCooldown = 0;
         this.lifeCooldown = 0;
         this.life = 15;
         this.id = id;
 
         currentTile.setInhabitant(this);
 
         path = new CreaturePath(map, super.currentTile, getAllowedTypes());
     }
 
     /**
      * Return the life of the creature :O
      * This might seem easy but in fact it requires 12 characters to accomplish.
      * EXCLUDING whitespace!
      * Life is a variable in the class Creature (this class)
      * Life is of the type int, since this function returns life it also returns an int
      *
      * Life is an important property of creature since life is precious and short.
      *
      * @return
      */
     public int getLife() {
         return life;
     }
 
     /**
      * Tick methods gets called from game every x ms.
      * tick() take care of life, attacking and moving.
      * For each of these actions there is a private function
      */
     public void tick(long tick) {
         this.tick = tick;
         //call life tick
         this.lifeTick();
         //call attack tick
         this.attackTick();
         //call move tick
         this.moveTick();
 
         //end of the function
         //some more comments
     }
 
     /**
      * Handle movement
      * decrease moveCooldown every iteration, if 0 do some stuff like moving, attacking or eating.
      *
      */
     private void moveTick() {
         if (this.moveCooldown <= 0) {
             //LETS DO STUFF
             Tile nextTile = this.path.getNextTile();
             // if attackingCreature == null the creature is not attacking
             // this.canMove() will check if the creature has enough energy to move (only for aircreatures)
             // only do stuff if there is a next tile
             if (nextTile != null
                     && this.canMove(this.calculateMoveSpeed(super.currentTile, nextTile))) {
                 Inhabitant inhabitant = nextTile.getInhabitant();
                 if (inhabitant == null) {
                     //no inhabitant on next tile, lets move there <:)
                     this.doMove(nextTile);
                 } else if (inhabitant instanceof Food) {
                     //food on the next tile, lets eat it
                     this.doEat((Food) inhabitant);
                     //now the creature is fat so it has to do some movement.
                     this.doMove(nextTile);
                 } else {
                     //start attacking
                     this.doAttack((Creature) inhabitant);
                 }
             } else {
                 //at this point the creature is not moving. To indicate that we set moveCooldown to -1
                 this.moveCooldown = -1;
             }
         }
 
         if (this.moveCooldown != -1) {
             //decrease moveCooldown with a min of 0
             if ((--this.moveCooldown) < 0) {
                 this.moveCooldown = 0;
             }
         }
     }
 
     /**
      * Handle attacking
      * Decrease attackCooldown every iteration and strike the enemy when 0
      */
     private void attackTick() {
         //some nice boolean statement going on here
         if (this.attackCooldown == 0 && this.attackingCreature != null) {
             Tile attackCreatureTile = this.attackingCreature.getCurrentTile();
 
             //if attackingCreature is not on an adjacent tile anymore attacking should stop
             if (super.currentTile.isAdjacent(attackCreatureTile)) {
                 //attackingCreature is still in range, time to attack
                 //smack that ho!
                 this.strike();
                 this.attackCooldown = Creature.ATTACK_COOLDOWN_TICKS;
             } else {
                 //creature out of range set attackingCreature to null indicating this creature is not attacking anymore
                 this.attackingCreature = null;
             }
         }
 
         //decrease attackCooldown with a min of 0
         if ((--this.attackCooldown) < 0) {
             this.attackCooldown = 0;
         }
     }
 
     /**
      * Strike the enemy, dealing some damage and shit
      */
     private void strike() {
         int soundLevel = Game.globalGameObject.getSoundLevel();
         System.out.println(soundLevel);
         int damage = soundLevel;
         //TODO: listen to mic for damage
         if (this.attackingCreature.dealDamage(damage)) {
             //he dead, lets eat it
             this.eatCreature(this.attackingCreature);
             this.attackingCreature = null;
         }
     }
 
     /**
      * handles life reduction over time
      * every 5 seconds the creature should loose 1 life point.
      */
     private void lifeTick() {
         if (this.lifeCooldown == 0) {
             this.dealDamage(1);
             //informal specs say life should decrease with 1 every 5seconds. fuck hun, 10 seconden
             this.lifeCooldown = 10000 / Game.TICK_TIME_IN_MS;
         }
         if ((--this.lifeCooldown) < 0) {
             this.lifeCooldown = 0;
         }
     }
 
     protected void die() {
         this.getCurrentTile().setInhabitant(null);
     }
 
     public void select(Tile tile) {
         path.calculatePath(tile);
     }
 
     public int getMoveCooldown() {
         return moveCooldown;
     }
 
     /**
      * move to a tile, immediately moving to the next tile
      * and setting moveCooldown to some shitty number indicating how long the creature
      * should wait before he can move again. Animation is not handled in the model
      * @param tile
      */
     private void doMove(Tile tile) {
         Tile oldTile = super.currentTile;
         super.currentTile.setInhabitant(null);
         tile.setInhabitant(this);
         this.path.step();
 
         //creature is moved, calculate moveCooldown
         this.moveCooldown = this.calculateMoveSpeed(oldTile, tile);

         Change c = this.getChange();
         c.type = Change.ChangeType.MOVE_CREATURE;
         c.x = tile.x;
         c.y = tile.y;
         Game.globalGameObject.addChange(c);
     }

    protected Change getChange()
     {

         Change c = new Change();
         c.tick = this.tick;
         c.player = Game.globalGameObject.getPlayer(this);
         c.playerId = c.player.getId();
         c.creature = this;
         c.creatureId = super.getId();
         return c;
     }
 
     private int calculateMoveSpeed(Tile oldTile, Tile tile) {
 
         double moveCooldown = this.getMoveSpeed(tile.getType());
 
         if (oldTile.isDiagonal(tile)) {
             //if the tile is diagonal to the currenttile the movement should take sqrt(2) times longer
             moveCooldown *= Math.sqrt(2);
         }
 
         return (int) moveCooldown;
     }
 
     /**
      * eat some pie
      * @param food
      */
     private void doEat(Food food) {
         this.addLife(Food.HEALTH_REWARDED);
         food.eat();
     }
 
     /**
      * start attacking
      * @param creature
      */
     private void doAttack(Creature creature) {
         if (Game.globalGameObject.getPlayer(this).equals(Game.globalGameObject.getPlayer(creature))) {
             this.moveCooldown = -1;
         } else {
             this.attackingCreature = creature;
         }
     }
 
     /**
      * dealDamage is called from another creature, damage is done to this creature.
      * Returns true if this creature dies.
      */
     public boolean dealDamage(int damage) {
         this.life -= damage;
         if (this.life <= 0) {
             this.die();
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * eat dead creature.
      * @param creature
      */
     private void eatCreature(Creature creature) {
         this.addLife(this.getEatValue(creature));
     }
 
     /**
      * add life, max life at 20
      * @param life
      */
     protected void addLife(int inc) {
         int life = this.life + inc;
         if (life > MAX_LIFE) {
             life = MAX_LIFE;
         }
         this.setLife(life);
     }

     protected void setLife(int life)
     {
         this.life = life;
         Change c = this.getChange();
         c.type = Change.ChangeType.HEALTH;
         c.newValue = life;
         Game.globalGameObject.addChange(c);
     }
 
     protected abstract int getMoveSpeed(TileType tileType);
 
     protected abstract int getEatValue(Creature creature);
 
     protected abstract Set<TileType> getAllowedTypes();
 
     protected boolean canMove(int ticks) {
         return this.isAlive();
     }
 
     /**
      * Get the path.
      */
     public CreaturePath getPath() {
         return path;
     }
 
     @Override
     public String toString() {
         return super.toString() + "\nLife: " + this.life + "\nPosition.x: " + super.currentTile.x + "\nPosition.y: " + super.currentTile.y;
     }
 
     public boolean isAlive() {
         return life > 0;
     }
 }
