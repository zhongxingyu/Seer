 package game;
 
 /**
  *
  * @author Nikolaos Bukas
  */
 
 import gui.MenuGUI;
 import java.awt.Polygon;
 import java.util.ArrayList;
 
 public class Physics implements Runnable{
     
     private GameState gameState;
     private static int height, width;
     
     Physics(GameState gameState)
     {
         this.gameState = gameState;
     }
     
     public void update()
     {
         height = MenuGUI.HEIGHT;
         width = MenuGUI.WIDTH;
         if (gameState.getPlayerShip() != null)
         {
             updateObject(gameState.getPlayerShip());
         }
         
         if(gameState.getAlienShip() != null)
         {
             updateObject(gameState.getAlienShip());
         }
         
         if (!gameState.getAsteroids().isEmpty())
         {
             for(Asteroid asteroid : gameState.getAsteroids())
             {
                 updateObject(asteroid);
             }
         }
         
         
         if (!gameState.getProjectiles().isEmpty())
         {
             for(Projectile projectile : gameState.getProjectiles())
             {
                 updateObject(projectile);
             }
         }
         
         if (!gameState.getBonusDrops().isEmpty())
         {
         for(BonusDrop bonusDrop : gameState.getBonusDrops())
             {
                 updateObject(bonusDrop);
             }
         }
         
         if (!gameState.getExplosions().isEmpty())
         {
             for(MapObject explosion : gameState.getExplosions())
             {
                 updateObject(explosion);
             }
         }
     }
     
     private static void updateObject(MapObject gameObject)
     {
         float[] velocity = gameObject.getVelocity();
         float[] acceleration = calculate2DAcceleration(gameObject.getHeading(), gameObject.getAcceleration());
         
         gameObject.setVelocity(calculateNewVelocity(gameObject, velocity, acceleration, 1));
         acceleration = calculate2DAcceleration(gameObject.getHeading(), gameObject.getAcceleration());
         
         int[] displacement = calculateDisplacement(velocity, acceleration, 1);
         //int[] displacement = calculateDisplacement(velocity, gameObject.getVelocity(), 1);
         gameObject.setX(gameObject.getX() + displacement[0]);
         gameObject.setY(gameObject.getY() + displacement[1]);
         
         wrapAround(gameObject);
     }
     
     public ArrayList<MapObject> getCollisions()
     {
         PlayerShip playerShip = gameState.getPlayerShip();
         AlienShip alienShip = gameState.getAlienShip();
         ArrayList<Asteroid> asteroidList = gameState.getAsteroids();
         ArrayList<Projectile> projectileList = gameState.getProjectiles();
         ArrayList<BonusDrop> bonusList = gameState.getBonusDrops();
         
         ArrayList<MapObject> listOfCollisions = new ArrayList<MapObject>();
         Polygon shipShape;
         
         shipShape = playerShip.getShape();
         
         //Checking for collisions between PlayerShip and Asteroids
         for(Asteroid asteroid : asteroidList)
         {
             if(detectCollision(shipShape, asteroid.getShape()))
             {
                 listOfCollisions.add(playerShip);
                 listOfCollisions.add(asteroid);
             }
         }
         
         //Checking collisions between PlayerShip and Projectiles
         for(Projectile projectile : projectileList)
         {
             if(projectile.getOwner() != Projectile.ALIEN_OWNER && detectCollision(shipShape, projectile.getShape()))
             {
                     listOfCollisions.add(playerShip);
                     listOfCollisions.add(projectile);
             }
         }
         
         //Checking collisions between PlayerShip and BonusDrops
         for(BonusDrop bonusDrop : bonusList)
         {
             if(detectCollision(shipShape, bonusDrop.getShape()))
             {
                     listOfCollisions.add(playerShip);
                     listOfCollisions.add(bonusDrop);
             }
         }
         
         if(alienShip != null)
         {
             shipShape = alienShip.getShape();
             
             if(detectCollision(playerShip.getShape(), shipShape))
             {
                 listOfCollisions.add(playerShip);
                 listOfCollisions.add(alienShip);
             }
             
             //Checking collisions between AlienShip and Asteroids
             for(Asteroid asteroid : asteroidList)
             {
                 if(detectCollision(shipShape, asteroid.getShape()))
                 {
                     listOfCollisions.add(alienShip);
                     listOfCollisions.add(asteroid);
                 }
             }
             
             //Checkin collisions between AlienShip and Projectiles
             for(Projectile projectile : projectileList)
             {
                 if(projectile.getOwner() != Projectile.ALIEN_OWNER && detectCollision(shipShape, projectile.getShape()))
                 {
                     listOfCollisions.add(alienShip);
                     listOfCollisions.add(projectile);
                 }
             }
         }
         
         return listOfCollisions;
     }
     
     private static boolean detectCollision(Polygon shapeOne, Polygon shapeTwo)
     {
         for(int i = 0; i < shapeTwo.npoints; i++)
         {
             if (shapeOne.contains(shapeTwo.xpoints[i], shapeTwo.ypoints[i]))
                 return true;    
         }
         for(int i = 0; i < shapeOne.npoints; i++)
         {
             if (shapeTwo.contains(shapeOne.xpoints[i], shapeOne.ypoints[i]))
                 return true; 
         }
         
         return false;
     }
     
     private static float[] calculate2DAcceleration(float heading, float acceleration)
     {
         float[] acceleration2D = {0, 0};
         
         acceleration2D[0] =  (float) (acceleration * Math.cos(Math.toRadians(heading)));
         acceleration2D[1] =  (float) (acceleration * Math.sin(Math.toRadians(heading)));
        
         return acceleration2D;
     }
     
     private static float[] calculateNewVelocity(MapObject gameObject, float[] velocity, float[] acceleration, float time)
     {
         velocity[0] = velocity[0] + acceleration[0] * time;
         
         if(gameObject instanceof PlayerShip)
         {
             if(velocity[0] > PlayerShip.MAX_VELOCITY)
             {
                 velocity[0] = PlayerShip.MAX_VELOCITY;
                 gameObject.setAcceleration(0);
             }
             if(velocity[0] < (-1) * PlayerShip.MAX_VELOCITY)
             {
                 velocity[0] = (-1) * PlayerShip.MAX_VELOCITY;
                 gameObject.setAcceleration(0);
             }
         }
         
         velocity[1] = velocity[1] + acceleration[1] * time;
         
         if(gameObject instanceof PlayerShip)
         {
             if(velocity[1] > PlayerShip.MAX_VELOCITY)
             {
                 velocity[1] = PlayerShip.MAX_VELOCITY;
             }
             if(velocity[1] < (-1) * PlayerShip.MAX_VELOCITY)
             {
                 velocity[1] = (-1) * PlayerShip.MAX_VELOCITY;
             }
         }
         
         return velocity;
     }
    
     private static int[] calculateDisplacement(float[] velocity, float[] acceleration, float time)
     {
         int[] displacement = {0, 0};
         
        displacement[0] = (int) (velocity[0] * time + 0.5 * acceleration[0] * Math.pow(time, 2));
        displacement[1] = (int) (velocity[1] * time + 0.5 * acceleration[1] * Math.pow(time, 2));
        
        return displacement;
    }
    
    private static int[] calculateDisplacement2(int[] originalVelocity, int[] newVelocity, int time)
    {
        int[] displacement = {0, 0};
         
        displacement[0] = (int) ((originalVelocity[0] + newVelocity[0]) * time);
        displacement[1] = (int) ((originalVelocity[1] + newVelocity[1]) * time);
         
         return displacement;
     }
     
     private static void wrapAround(MapObject gameObject)
     {
         if(gameObject.getX() > width)
         {
             gameObject.setX(0);
         }
         else if(gameObject.getX() < 0)
         {
             gameObject.setX(width);
         }
         
         if(gameObject.getY() > height)
         {
             gameObject.setY(0);
         }
         else if(gameObject.getY() < 0)
         {
             gameObject.setY(height);
         }
     }
 
     @Override
     public void run() {
         update();
     }
 }
