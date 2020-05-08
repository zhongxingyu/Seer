 package levelLoadSave;
 
 import java.awt.image.BufferedImage;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import weapons.DamagingProjectile;
 import weapons.Projectile;
 import weapons.ScatterPattern;
 import weapons.ShotPattern;
 import weapons.SidePattern;
 import weapons.SinglePattern;
 import weapons.UnlimitedGun;
 import weapons.Weapon;
 
 import game.TopDownDemo;
 import gameObjects.Enemy;
 import gameObjects.GameObject;
 import gameObjects.Player;
 
 import com.golden.gamedev.object.SpriteGroup;
 
 /**
  * 
  * @author antaresyee
  *
  */
 
 public class PlayerLoadObserver extends LoadObserver {
     SpriteGroup myPlayerGroup;
     SpriteGroup myProjectileGroup;
     
     TopDownDemo myGame;
 
     public PlayerLoadObserver(SpriteGroup playerGroup, SpriteGroup projectileGroup, TopDownDemo game) {
         super.myType = "Player";
         myPlayerGroup = playerGroup;
         myProjectileGroup = projectileGroup;
         myGame = game;
     }
 
     @Override
     public void objectLoaded(GameObject go) {
         System.out.println("entered player object loaded");
        myPlayerGroup.add(go);
         myGame.setPlayer((Player) go);
         
         Projectile p = new DamagingProjectile("resources/fire.png",myProjectileGroup,1);
         
         ShotPattern s1 = new SinglePattern(-1);
         ShotPattern s2 = new ScatterPattern(5, -1, 5);
         ShotPattern s3 = new SidePattern(-1, 1);
         Weapon w1 = new UnlimitedGun(300,p,s1);
         Weapon w2 = new UnlimitedGun(300, p ,s2);
         Weapon w3 = new UnlimitedGun(300, p ,s3);
         Player player = (Player) go;
         player.addWeapon(w1);
         player.addWeapon(w2);
         player.addWeapon(w3);
 
         //set sprite image
         try {
             File imgSrc = new File(go.getImgPath());
             BufferedImage sourceImage = ImageIO.read(imgSrc);
             go.setImage(sourceImage);
         } catch (IOException e1) {
             e1.printStackTrace();
         }
         
 
     }
 }
