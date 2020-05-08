 package epsilon.map.entity;
 
import epsilon.game.Collision;
 import epsilon.game.Input;
 import epsilon.game.SoundPlayer;
 import epsilon.map.Background;
 import epsilon.map.Map;
 import epsilon.map.WorldStore;
 import java.awt.Graphics;
 import java.util.ArrayList;
 
 /**
  * Map made for testing purposes, now used as a single player map
  *
  * @author Marius
  */
 public class TestMap implements Map {
 
     /*
      * The different lists used for storing entities
      * There are a number of them to keep the iterating over lists to a minimum
      */
     ArrayList<Entity> renderableEntities;
     ArrayList<MoveableEntity> moveableEntities;
     ArrayList<Entity> entities;
     ArrayList<Shot> shots;
     WorldStore worldstore;
 
     // the soundtrack that is played continuously while playing the map
     SoundPlayer soundtrack;
 
     // the entity of the player played on this computer. Used for calculating rendering positions
     TestPlayerEntity playerEntity;
 
     // the background object used on this map
     Background bg;
 
     private int shotCooldown = 0;
 
     /**
      * Initialises all entities on the map, and all fields in the object
      */
     public TestMap() {
 
         renderableEntities = new ArrayList<Entity>();
         moveableEntities = new ArrayList<MoveableEntity>();
         entities = new ArrayList<Entity>();
         shots = new ArrayList<Shot>();
 
         worldstore = new WorldStore(50);
 
         bg = new Background("/pics/bg3.png", 1.25);
 
         TestPlayerEntity test = new TestPlayerEntity(-70, 400, "");
         playerEntity = test;
 
         worldstore.add(new Floor_1(-500, 525));
         worldstore.add(new Floor_1(-500, 565));
         worldstore.add(new Floor_1(-450, 565));
         worldstore.add(new Floor_1(-400, 565));
         worldstore.add(new Floor_1(-350, 565));
         worldstore.add(new Floor_1(-300, 565));
         worldstore.add(new Floor_1(-250, 565));
         worldstore.add(new Floor_1(-200, 565));
         worldstore.add(new Floor_1(-150, 565));
         worldstore.add(new Floor_1(-100, 565));
         worldstore.add(new Floor_1(-50, 565));
         worldstore.add(new Floor_1(-50, 525));
 
 
         worldstore.add(new Floor_1(1000, 565));
         worldstore.add(new Floor_1(1050, 565));
         worldstore.add(new Floor_1(1100, 565));
         worldstore.add(new Floor_1(1150, 565));
         worldstore.add(new Floor_1(1200, 565));
         worldstore.add(new Floor_1(1250, 565));
         worldstore.add(new Floor_1(1300, 565));
         worldstore.add(new Floor_1(1350, 565));
         worldstore.add(new Floor_1(1400, 565));
         worldstore.add(new Floor_1(1450, 565));
 
         worldstore.add(new Floor_1(80, 505));
 
         worldstore.add(new Floor_1(250, 415));
         worldstore.add(new Floor_1(300, 415));
 
         worldstore.add(new Floor_1(500, 385));
         worldstore.add(new Floor_1(550, 385));
 
 
         worldstore.add(new Floor_1(350, 455));
         worldstore.add(new Floor_1(500, 495));
 
         Enemy enemy = new EnemyPatrol(-300,100);
         renderableEntities.add(enemy);
         moveableEntities.add(enemy);
         entities.add(enemy);
 
 
         renderableEntities.add(test);
         moveableEntities.add(test);
         entities.add(test);
 
         // Test MP3 playing
         String filename = "/sound/zabutom.lets.shooting.mp3";
         soundtrack = new SoundPlayer(filename);
         soundtrack.play();
 
     }
 
     public void render(Graphics g, int delta) {
 
         bg.render(g, playerEntity.getXPosition(), playerEntity.getYPosition());
 
         worldstore.renderAll(g, delta, playerEntity.getXRenderPosition(), playerEntity.getYRenderPosition());
 
         Entity[] temp = new Entity[renderableEntities.size()];
         renderableEntities.toArray(temp);
 
         for (int i = 0; i < temp.length; i++) {
             temp[i].render(g, delta, playerEntity.getXRenderPosition(), playerEntity.getYRenderPosition());
             temp[i].renderHitBox(g, playerEntity.getXRenderPosition(), playerEntity.getYRenderPosition());
         }
 
     }
 
     public void update() {
 
         if (shotCooldown>0) {
             shotCooldown--;
         }
         if (Input.get().attack() && shotCooldown == 0) {
             //sound.close();
             Shot shot = new Shot(playerEntity.getXPosition(),playerEntity.getYPosition(),playerEntity.facingRight());
             moveableEntities.add(shot);
             renderableEntities.add(shot);
             shots.add(shot);
             shotCooldown += 30;
         }
 
         // creating temp array to avoid unexpected behaviour when removing elements
         Shot[] tempshot = new Shot[shots.size()];
         shots.toArray(tempshot);
 
         // checking each shot if it has traveled its distance
         for (int i = 0; i < tempshot.length; i++) {
             Shot shot = tempshot[i];
             if (shot.distanceDone()) {
                 // removing the shot from the lists
                 shots.remove(shot);
                 renderableEntities.remove(shot);
                 moveableEntities.remove(shot);
             }
         }
 
         MoveableEntity[] temp = new MoveableEntity[moveableEntities.size()];
         moveableEntities.toArray(temp);
 
         for (int i = 0; i < temp.length; i++) {
             temp[i].calculateMovement();
         }
 
         worldstore.checkCollision(playerEntity);
 
         for (int i=0;i<temp.length;i++) {
             temp[i].move();
         }
     }
 
     public double[] getPlayerPosition() {
         return new double[]{playerEntity.getXPosition(), playerEntity.getYPosition()};
     }
 
     public void resetPlayerPosition() {
         playerEntity.resetPosition();
     }
 
 }
