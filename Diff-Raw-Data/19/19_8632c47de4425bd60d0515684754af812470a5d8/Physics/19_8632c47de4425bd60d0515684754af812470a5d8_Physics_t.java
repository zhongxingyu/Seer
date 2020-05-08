 package game;
 
 import gui.MenuGUI;
 import java.awt.Polygon;
 import java.util.ArrayList;
 import org.apache.log4j.Logger;
 
 /**
  * The purpose of the
  * <code>Physics</code> class is to provide the game physics. It calculates
  * velocities and displacements and is able to detect collisions.
  * 
 * @author Nikolaos Bukas
  */
 public class Physics implements Runnable {
 
     private GameState gameState;
     private static int height, width;
     private final static int WRAP_AROUND_BUFFER = 60;
     private final static int ANGULAR_SPEED = 3;
     private final static Logger log = Logger.getLogger(Physics.class.getName());
 
     /**
      * Creates Physics using given game state.
      * @param gameState current game state
      */
     public Physics(GameState gameState) {
         this.gameState = gameState;
         log.setLevel(Logic.LOG_LEVEL);
     }
 
     /**
      * Updates the physical properties (velocity, heading, x & y coordinates,
      * and acceleration) of every in-game object.
      */
     public void update() {
         log.info("Physics update start");
         height = MenuGUI.HEIGHT;
         width = MenuGUI.WIDTH;
         PlayerShip player = gameState.getPlayerShip();
         if (player != null) {
             log.debug("updating player ship");
             updateObject(player);
         }
 
         AlienShip alien = gameState.getAlienShip();
         if (alien != null) {
             log.debug("updating alien");
             updateObject(alien);
         }
 
         if (!gameState.getAsteroids().isEmpty()) {
             for (Asteroid asteroid : gameState.getAsteroids()) {
                 log.debug("updating asteroid " + asteroid.toString());
                 updateObject(asteroid);
             }
         }
 
 
         if (!gameState.getProjectiles().isEmpty()) {
             for (Projectile projectile : gameState.getProjectiles()) {
                 log.debug("updating projectile " + projectile.toString());
                 updateObject(projectile);
                 log.debug("projectile updated");
             }
         }
 
         if (!gameState.getBonusDrops().isEmpty()) {
             for (BonusDrop bonusDrop : gameState.getBonusDrops()) {
                 log.debug("updating bonus " + bonusDrop.toString());
                 updateObject(bonusDrop);
             }
         }
 
         if (!gameState.getExplosions().isEmpty()) {
             for (MapObjectTTL explosion : gameState.getExplosions()) {
                 log.debug("updating explosion " + explosion.toString());
                 updateObject(explosion);
             }
         }
         log.debug("update run complete");
     }
 
     private static void updateObject(MapObject gameObject) {
         if (gameObject instanceof PlayerShip) {
             PlayerShip ship = (PlayerShip) gameObject;
             ship.setHeading(ship.getHeading() + calculateHeadingDisplacement(ship.isTurningRight(), ship.isTurningLeft()));
         }
 
         float[] velocity = gameObject.getVelocity();
         float[] acceleration = calculate2DAcceleration(gameObject.getHeading(), gameObject.getAcceleration());
        
         int[] displacement = calculateDisplacement(velocity, acceleration, 1);
         //int[] displacement = calculateDisplacement(velocity, gameObject.getVelocity(), 1);
         gameObject.setX(gameObject.getX() + displacement[0]);
         gameObject.setY(gameObject.getY() + displacement[1]);
 
       
         gameObject.setVelocity(calculateNewVelocity(gameObject, velocity, acceleration, 1));
         wrapAround(gameObject);
     }
 
     private static void updateObject(PlayerShip gameObject) {
         log.info("updating object");
         PlayerShip ship = (PlayerShip) gameObject;
         ship.setHeading(ship.getHeading() + calculateHeadingDisplacement(ship.isTurningRight(), ship.isTurningLeft()));
 
         float[] velocity = gameObject.getVelocity();
         float[] acceleration = calculate2DAcceleration(gameObject.getHeading(), gameObject.getAcceleration());
 
         int[] displacement = calculateDisplacement(velocity, acceleration, 1);
         //int[] displacement = calculateDisplacement(velocity, gameObject.getVelocity(), 1);
         gameObject.setX(gameObject.getX() + displacement[0]);
         gameObject.setY(gameObject.getY() + displacement[1]);
 
         gameObject.setVelocity(calculateNewVelocity(gameObject, velocity, acceleration, 1));
         wrapAround(gameObject);
         log.info("update object complete");
     }
 
     private static void updateObject(Projectile gameObject) {
         float[] velocity = gameObject.getVelocity();
         float[] acceleration = calculate2DAcceleration(gameObject.getHeading(), gameObject.getAcceleration());
 
         int[] displacement = calculateDisplacement(velocity, acceleration, 1);
         //int[] displacement = calculateDisplacement(velocity, gameObject.getVelocity(), 1);
         gameObject.setX(gameObject.getX() + displacement[0]);
         gameObject.setY(gameObject.getY() + displacement[1]);
 
         gameObject.setVelocity(calculateNewVelocity(gameObject, velocity, acceleration, 1));
 
         wrapAround(gameObject);
     }
 
     /**
      * Returns list of MapObjects that have been detected in collision.
      * The list will always have an even number of entries. Each sequential pair
      * of MapObjects indicates that the two of them collided.
      *
      * @return list of MapObject detected in collision
      */
     public ArrayList<MapObject> getCollisions() {
         PlayerShip playerShip = gameState.getPlayerShip();
         AlienShip alienShip = gameState.getAlienShip();
         ArrayList<Asteroid> asteroidList = gameState.getAsteroids();
         ArrayList<Projectile> projectileList = gameState.getProjectiles();
         ArrayList<BonusDrop> bonusList = gameState.getBonusDrops();
 
         ArrayList<MapObject> listOfCollisions = new ArrayList<MapObject>();
         Polygon shipShape;
 
         log.debug("checking for playership collisions");
         if (playerShip != null) {
             shipShape = playerShip.getShape();
 
             //Checking for collisions between PlayerShip and Asteroids
             for (Asteroid asteroid : asteroidList) {
                 if (detectCollision(shipShape, asteroid.getShape())) {
                     listOfCollisions.add(playerShip);
                     listOfCollisions.add(asteroid);
                 }
             }
 
             //Checking collisions between PlayerShip and Projectiles
             for (Projectile projectile : projectileList) {
                 if (projectile.getOwner() == Projectile.ALIEN_OWNER && detectCollision(shipShape, projectile.getShape())) {
                     listOfCollisions.add(playerShip);
                     listOfCollisions.add(projectile);
                 }
             }
 
             //Checking collisions between PlayerShip and BonusDrops
             for (BonusDrop bonusDrop : bonusList) {
                 if (detectCollision(shipShape, bonusDrop.getShape())) {
                     listOfCollisions.add(playerShip);
                     listOfCollisions.add(bonusDrop);
                 }
             }
         }
 
         log.debug("checking for alien collision");
         if (alienShip != null) {
             shipShape = alienShip.getShape();
 
             log.debug("checking for collision b/w player & alien");
             if ((playerShip != null) && detectCollision(playerShip.getShape(), shipShape)) {
                 log.debug("collision b/w player and alien detected, adding");
                 listOfCollisions.add(playerShip);
                 listOfCollisions.add(alienShip);
             }
 
             //Checking collisions between AlienShip and Asteroids
             log.debug("checking for alien and asteroid collision");
             for (Asteroid asteroid : asteroidList) {
                 if (detectCollision(shipShape, asteroid.getShape())) {
                     listOfCollisions.add(alienShip);
                     listOfCollisions.add(asteroid);
                 }
             }
 
             //Checkin collisions between AlienShip and Projectiles
             log.debug("checking for alien and projectile collision");
             for (Projectile projectile : projectileList) {
                 if (projectile.getOwner() != Projectile.ALIEN_OWNER && detectCollision(shipShape, projectile.getShape())) {
                     listOfCollisions.add(alienShip);
                     listOfCollisions.add(projectile);
                 }
             }
         }
 
         log.debug("checking for projectile & asteroid collision");
         for (Projectile projectile : projectileList) {
             for (Asteroid asteroid : asteroidList) {
                 if (detectCollision(projectile.getShape(), asteroid.getShape())) {
                     listOfCollisions.add(projectile);
                     listOfCollisions.add(asteroid);
                 }
             }
         }
 
         return listOfCollisions;
     }
 
     private static boolean detectCollision(Polygon shapeOne, Polygon shapeTwo) {
         for (int i = 0; i < shapeTwo.npoints; i++) {
             if (shapeOne.contains(shapeTwo.xpoints[i], shapeTwo.ypoints[i])) {
                 return true;
             }
         }
         
         for (int i = 0; i < shapeOne.npoints; i++) {
             if (shapeTwo.contains(shapeOne.xpoints[i], shapeOne.ypoints[i])) {
                 return true;
             }
         }
 
         return false;
     }
 
     private static float[] calculate2DAcceleration(float heading, float acceleration) {
         float[] acceleration2D = {0, 0};
 
         acceleration2D[0] = (float) (acceleration * Math.cos(Math.toRadians(heading - 90)));
         acceleration2D[1] = (float) (acceleration * Math.sin(Math.toRadians(heading - 90)));
        
         return acceleration2D;
     }
 
     private static float[] calculateNewVelocity(MapObject gameObject, float[] velocity, float[] acceleration, float time) {
         velocity[0] = velocity[0] + acceleration[0] * time;
 
         if (gameObject instanceof PlayerShip) {
             if (velocity[0] > PlayerShip.MAX_VELOCITY) {
                 velocity[0] = PlayerShip.MAX_VELOCITY;
                 gameObject.setAcceleration(0);
             }
             if (velocity[0] < (-1) * PlayerShip.MAX_VELOCITY) {
                 velocity[0] = (-1) * PlayerShip.MAX_VELOCITY;
                 gameObject.setAcceleration(0);
             }
         }
 
         velocity[1] = velocity[1] + acceleration[1] * time;
 
         if (gameObject instanceof PlayerShip) {
             if (velocity[1] > PlayerShip.MAX_VELOCITY) {
                 velocity[1] = PlayerShip.MAX_VELOCITY;
             }
             if (velocity[1] < (-1) * PlayerShip.MAX_VELOCITY) {
                 velocity[1] = (-1) * PlayerShip.MAX_VELOCITY;
             }
         }
         return velocity;
     }
 
     private static int[] calculateDisplacement(float[] velocity, float[] acceleration, float time) {
         int[] displacement = {0, 0};
 
 //        displacement[0] = Math.round((velocity[0] * time + 0.5f * acceleration[0] * (float) Math.pow(time, 2)));
 //        displacement[1] = Math.round((float) velocity[1] * time + 0.5f * acceleration[1] * (float) Math.pow(time, 2));
        displacement[0] = Math.round(velocity[0] * time + 0.5f * acceleration[0]);
        displacement[1] = Math.round(velocity[1] * time + 0.5f * acceleration[1]);
         
         return displacement;
     }
 
     private static float calculateHeadingDisplacement(boolean turningRight, boolean turningLeft) {
         if (turningRight && !turningLeft) {
             return ANGULAR_SPEED;
         } else if (!turningRight && turningLeft) {
             return -1 * ANGULAR_SPEED;
         } else {
             return 0;
         }
     }
 
     private static void wrapAround(MapObject gameObject) {
         if (gameObject.getX() > width + WRAP_AROUND_BUFFER) {
             gameObject.setX(0);
         } else if (gameObject.getX() < -1 * WRAP_AROUND_BUFFER) {
             gameObject.setX(width);
         }
 
         if (gameObject.getY() > height + WRAP_AROUND_BUFFER) {
             gameObject.setY(0);
         } else if (gameObject.getY() < -1 * WRAP_AROUND_BUFFER) {
             gameObject.setY(height);
         }
         //gameObject.setX(gameObject.getX() % (width + 100));
         //gameObject.setY(gameObject.getY() % (height + 100));
     }
 
     /**
      * Calls the update method.
      */
     @Override
     public void run() {
         update();
     }
 }
