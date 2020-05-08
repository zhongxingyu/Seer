 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Nikolaos
  */
 public class Physics {
    public void update(MapObject mapobject);
    
    public void update(Ship ship);
     
    public List<Trajectory> getTrajectory(AlienShip alienship);
     
    public void checkCollision(MapObject PlayerShip, List <MapObject> AlienShip, List<MapObject> Asteroids, List<MapObject> Projectiles, List<MapObject> BonusDrops);
}
