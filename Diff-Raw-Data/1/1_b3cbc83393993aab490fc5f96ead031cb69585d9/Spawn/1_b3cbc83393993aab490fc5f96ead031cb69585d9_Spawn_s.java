 package org.gdc.gdcalaga;
 import java.util.ArrayList;
 import org.newdawn.slick.geom.Vector2f;
 /*
  * I want this class to learn about the player constantly. It needs to observe the player and from there, decide how to suit the level
  * for the player. We don't have many enemies yet, but we should later have enemies with unique characteristics and features. The class
  * should know how often the player shoots, accuracy, how much the player moves or jitters. Place tanky enemies, quick enemies, anything
  * to make the player adapt, even if it's only a little. The class should also make sure that the player gets a good variety of enemies,
  * not just one type of enemy all the time. I, subsage will handle it, unless anyone else wants to, going to bed.
  */
 
 
 public class Spawn 
 {
     private static int wave=0;
     
     public static void spawnWave(PathRegistry paths, EntityManager mng) {
         
         int countWaves = 0;
         ArrayList<Entity> ents = mng.getEntities();
         
         for(Entity e : ents)
         {
             if(e instanceof Wave){
                 countWaves++;
             }
         }
         
         
         if(countWaves==0)
         {
         	EnemyGroup group = new EnemyGroup(mng, 100, 800, 0);
 
             int pathNum = (int)Math.floor(Math.random() * paths.getNumOfPaths());
            pathNum=0;
             
         	float x, y;
         	x = (float)(Math.random()*400);
         	y = (float)(Math.random()*400);
         	
         	Vector2f offsetPos = new Vector2f(x, y);
         	
             /*
             Enemy newEnemy = new Enemy(mng,x,y);
             
             Path enemyPath = new Path(group.xPos + x, group.yPos + y);
             enemyPath.addNode(true, 400, 0, 1);
             enemyPath.addNode(false, 700, 300, 1);
             enemyPath.addNode(true, 0, 0, 1f);
             */
            
             Path enemyPath = paths.getPath(pathNum 
                                          , new Vector2f(group.pos.x + offsetPos.x, 
                                                         group.pos.y + offsetPos.y));
             
             int rows = (int)Math.ceil(Math.random() * 3 + 4);
             int cols = (int)Math.ceil(Math.random() * 3 + 2);
             
             new Wave(mng, "block", rows, cols, 10, 200, enemyPath);
             
             wave++;
         }
         
     }
     
     
     public static void updateStatistics(){
     	//TODO: Updates statistics on player. 
     }
     
     public static int getWave(){
     	return wave;
     }
 }
