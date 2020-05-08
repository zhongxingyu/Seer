 package ogo.spec.game.model;
 
 import java.util.Set;
 import java.util.HashSet;
 
 public class AirCreature extends Creature {
 
     public final static int MAX_ENERGY = 100;
     public final static int ENERGY_INC = 1;
     public final static int ENERGY_DEC = 2;
     public final static int ENERGY_TICKS = 1000 / Game.TICK_TIME_IN_MS;//loose 1 energy per second
 
     private int energy;
     private int energyCooldown = 0;
 
     public AirCreature(Tile currentTile, GameMap map, int id) {
         super(currentTile,map, id);
         this.energy = MAX_ENERGY;
     }
 
     @Override
     public void tick(long tick) {
         energyTick();
         super.tick(tick);
     }
 
    public void setEnergyNoChange(int e)
    {
        energy = e;
    }

     private void setEnergy(int e)
     {
        setEnergyNoChange(e);
         Change c = super.getChange();
         c.type = Change.ChangeType.ENERGY;
         c.newValue = e;
         Game.globalGameObject.addChange(c);
     }
 
     private void energyTick() {
         if (this.energyCooldown == 0) {
             TileType currentTileType = super.currentTile.getType();
             if (super.moveCooldown == -1 && currentTileType == TileType.LAND) {
                 //not moving and on land recharge that focking bird
                 this.setEnergy(Math.min(AirCreature.MAX_ENERGY, this.energy + AirCreature.ENERGY_INC));
             } else {
                 //moving or not on land, decrease energy
                 this.setEnergy(Math.max(0, this.energy - AirCreature.ENERGY_DEC));
             }
 
             if (this.energy <= 0 &&
                     (currentTileType == TileType.DEEP_WATER || currentTileType == TileType.SHALLOW_WATER)
                     ) {
                 this.dealDamage(Creature.MAX_LIFE);
             }
 
             this.energyCooldown = AirCreature.ENERGY_TICKS;
         }
         if (--this.energyCooldown < 0) {
             this.energyCooldown = 0;
         }
     }
 
     @Override
     protected int getMoveSpeed(TileType tileType) {
         return Creature.TICKS_PER_TILE_AVG;
     }
 
     @Override
     protected int getEatValue(Creature creature) {
         if (creature instanceof AirCreature) {
             return 5;
         }
         if (creature instanceof LandCreature) {
             return 5;
         }
         if (creature instanceof SeaCreature) {
             return 5;
         }
         return 0;
     }
 
     @Override
     protected Set<TileType> getAllowedTypes() {
         HashSet<TileType> types = new HashSet<TileType>();
         types.add(TileType.DEEP_WATER);
         types.add(TileType.SHALLOW_WATER);
         types.add(TileType.LAND);
         return types;
     }
 
     @Override
     protected boolean canMove(int ticks) {
         int requiredEnergy = ticks * AirCreature.ENERGY_DEC / AirCreature.ENERGY_TICKS;
         return (this.energy > requiredEnergy);
     }
     /*
      * // when re-enabling this, make sure to check if creature is alive
      * // using this.isAlive().
      */
 
     public int getEnergy() {
         return energy;
     }
 
     public String toString()
     {
         return super.toString() + "\nEnergy: " + this.energy;
     }
 }
