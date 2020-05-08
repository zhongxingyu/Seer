 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gamestate;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.math.Vector3f;
 import java.util.LinkedList;
 import java.util.Random;
 import variables.P;
 
 /**
  *
  * @author dagen
  */
 public class PlatformController {
     private AssetManager assetManager;
     public LinkedList<Platform> platforms;
     
     
     public PlatformController(PlatformFactory platformFactory){
         platforms = new LinkedList<Platform>();
         Platform platform;
         Random random;
         int randomNumber;
         //Adding the first platform
         platform = platformFactory.createPlatform();
        platform.getRigidBodyControl().setPhysicsLocation(new Vector3f(10f,10f,0));
         platforms.add(platform);
         //Adding all the following platforms
         for(int i = 1; i < P.platformsPerLevel; i++){
             random=new Random();
             randomNumber=(random.nextInt(8)-4);
             platform = platformFactory.createPlatform();
             platform.getRigidBodyControl().setPhysicsLocation(new Vector3f((P.platformDistance+2*P.platformLength)*i,(float)(platforms.getLast().getRigidBodyControl().getPhysicsLocation().y+randomNumber),0));
             this.platforms.addLast(platform);
         }
     }
     
     public void addPlatform(Platform platform){
         this.platforms.add(platform);
         
     }
     
     public void deletePlatform() {
         platforms.remove(0);
     }
 }
