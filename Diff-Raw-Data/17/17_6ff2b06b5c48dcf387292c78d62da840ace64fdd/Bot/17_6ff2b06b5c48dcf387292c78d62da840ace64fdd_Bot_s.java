import junit.runner.LoadingTestCollector;

 /**
  * Provides basic game state handling.
  */
 public abstract class Bot extends AbstractSystemInputParser {
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setup(int loadTime, int turnTime, int rows, int cols, int turns, int viewRadius2,
             int attackRadius2, int spawnRadius2) {
         Ants.initialize(loadTime, turnTime, rows, cols, turns, viewRadius2, attackRadius2, spawnRadius2);
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void beforeUpdate() {
         Ants.Instance.setTurnStartTime(System.currentTimeMillis());
         Ants.Instance.clearMyAnts();
         Ants.Instance.clearEnemyAnts();
         Ants.Instance.clearMyHills();
         Ants.Instance.clearEnemyHills();
         Ants.Instance.clearFood();
         Ants.Instance.clearDeadAnts();
         Ants.Instance.getOrders().clear();
         Ants.Instance.clearVision();
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void addWater(int row, int col) {
         Ants.Instance.update(Ilk.WATER, new Tile(row, col));
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void addAnt(int row, int col, int owner) {
         Ants.Instance.update(owner > 0 ? Ilk.ENEMY_ANT : Ilk.MY_ANT, new Tile(row, col));
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void addFood(int row, int col) {
         Ants.Instance.update(Ilk.FOOD, new Tile(row, col));
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void removeAnt(int row, int col, int owner) {
         Ants.Instance.update(Ilk.DEAD, new Tile(row, col));
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void addHill(int row, int col, int owner) {
         Ants.Instance.updateHills(owner, new Tile(row, col));
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void afterUpdate() {
         Ants.Instance.setVision();
     }
 }
