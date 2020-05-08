 package gameObjects;
 
 
 public class PowerUp extends GameObject{
 	
     public PowerUp(double x, double y, String imgPath){
         myX = x;
         myY = y;
         myImgPath = imgPath;
         myType = "PowerUp";
         setLocation(myX,myY);
     }
     
     public String getImgPath()
     {
     	return myImgPath;
     }
     
     @Override
     public GameObject makeGameObject(GameObjectData god) {
         Double x = god.getX();
         Double y = god.getY();
         String imgPath = god.getImgPath();
        return new Barrier(x, y, imgPath);
     }
     
     /**
      * Barrier() and getFactory() must be implemented by each game object; 
      * they are used for the factory system.
      */
     private PowerUp() {
         myType = "PowerUp";
     }
     
     public static GameObjectFactory getFactory() {
         return new GameObjectFactory(new PowerUp());
     }
     
 }
