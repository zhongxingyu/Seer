 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Vector2f;
 
 import java.util.ArrayList;
 
 
 public class GameLogic {
 
     private int time;
     private int STARTING_LIVES = 3;
     private ArrayList<Image> livesList;
     private ArrayList<Enemy> enemyList;
     private ArrayList<Powerup> powerups;
     private static int NUM_OF_ENEMIES = 200;
     private static int NUM_OF_TOPENEMIES = 50;
     private static int NUM_OF_TOPHOLES = 50;
     private static int NUM_OF_BOTTOMENEMIES = 50;
     private static int NUM_OF_BOTTOMHOLES = 50;
     private int enemyTypeInterval = 0;
     private int topE;
     private int topH;
     private int bottomE;
     private int bottomH;
     private static int NUM_OF_POWERUPS = 5;
     private Image timerBox;
     private Scoreboard scoreboard;
     public boolean isColliding;
 
 
 
     public GameLogic() {
         init();
     }
 
     public void init() {
         livesList = new ArrayList<Image>();
         enemyList = new ArrayList<Enemy>();
         powerups = new ArrayList<Powerup>();
         scoreboard = new Scoreboard();
         isColliding = false;
 
         try {
             timerBox = new Image("res/misc/TimerBackground.png");
         } catch (SlickException e) {
             e.printStackTrace();
         }
 
         //load the enemies
         int LOCATION = 550;
 
         //Initialize enemies
         try {
             //Top road bounded enemies
             topE = 0;
             while(topE < NUM_OF_TOPENEMIES) {
 
                 Image freshmanImageTop = new Image("res/character/Freshman.png");
 
                 int randomX;
                 randomX = (int) (67500 * Math.random());
 
                 int randomY = 0;
                 while(randomY < 225 || randomY > 325) {
                     randomY = (int) (600*Math.random());
                 }
 
 
                 Vector2f freshmanPosTop = new Vector2f(randomX, randomY);
                 Shape freshmanShapeTop = new Rectangle(0, 0, freshmanImageTop.getWidth(), freshmanImageTop.getHeight());
                 Freshman fresh = new Freshman("freshman" + enemyTypeInterval, freshmanImageTop, freshmanPosTop, freshmanShapeTop, 3);
                 enemyList.add(enemyTypeInterval, fresh);
                 enemyTypeInterval++;
                 topE++;
             }
 
             //Top road bounded holes
             topH = enemyTypeInterval;
             while(topH < (NUM_OF_TOPENEMIES + NUM_OF_TOPHOLES)) {
                 Image freshmanImageTop = new Image("res/character/hole.png");
 
                 int randomX;
                 randomX = (int) (67500 * Math.random());
 
                 int randomY = 0;
                 while(randomY < 225 || randomY > 325) {
                     randomY = (int) (600*Math.random());
                 }
 
 
                 Vector2f freshmanPosTop = new Vector2f(randomX, randomY);
                 Shape freshmanShapeTop = new Rectangle(0, 0, freshmanImageTop.getWidth(), freshmanImageTop.getHeight());
                 Freshman fresh = new Freshman("freshman" + enemyTypeInterval, freshmanImageTop, freshmanPosTop, freshmanShapeTop, 3);
                 enemyList.add(enemyTypeInterval, fresh);
                 enemyTypeInterval++;
                 topH++;
             }
 
             //Bottom road bounded enemies
            bottomE = enemyTypeInterval;
             while(bottomE < (NUM_OF_TOPENEMIES + NUM_OF_TOPHOLES + NUM_OF_BOTTOMENEMIES)) {
                 Image freshmanImage = new Image("res/character/bike.png");
 
                 int randomXOdd;
                 randomXOdd = (int) (67500 * Math.random());
 
                 int randomYOdd = 0;
                 while(randomYOdd < 350 || randomYOdd > 450) {
                     randomYOdd = (int) (600*Math.random());
                 }
 
 
                 Vector2f freshmanPos = new Vector2f(randomXOdd, randomYOdd);
                 Shape freshmanShape = new Rectangle(0, 0, freshmanImage.getWidth(), freshmanImage.getHeight());
                 enemyList.add(new Freshman("freshman" + enemyTypeInterval, freshmanImage, freshmanPos, freshmanShape, 3));
                 enemyTypeInterval++;
                 bottomE++;
             }
 
             //Bottom road bounded holes
             bottomH = enemyTypeInterval;
             while(bottomH < (NUM_OF_TOPENEMIES + NUM_OF_TOPHOLES + NUM_OF_BOTTOMENEMIES + NUM_OF_BOTTOMHOLES)) {
                 Image freshmanImage = new Image("res/character/hole.png");
 
                 int randomXOdd;
                 randomXOdd = (int) (67500 * Math.random());
 
                 int randomYOdd = 0;
                 while(randomYOdd < 350 || randomYOdd > 450) {
                     randomYOdd = (int) (600*Math.random());
                 }
 
 
                 Vector2f freshmanPos = new Vector2f(randomXOdd, randomYOdd);
                 Shape freshmanShape = new Rectangle(0, 0, freshmanImage.getWidth(), freshmanImage.getHeight());
                 enemyList.add(new Freshman("freshman" + enemyTypeInterval, freshmanImage, freshmanPos, freshmanShape, 3));
                 enemyTypeInterval++;
                 bottomH++;
             }
 
         } catch (SlickException e) {
             e.printStackTrace();
         }
 
         //Bottom road bounded enemies
 
 //        try {
 //            for (int i = 1; i < NUM_OF_ENEMIES; i+=2) {
 //                Image freshmanImage = new Image("res/character/bike.png");
 //
 //                int randomXOdd;
 //                randomXOdd = (int) (67500 * Math.random());
 //
 //                int randomYOdd = 0;
 //                while(randomYOdd < 350 || randomYOdd > 450) {
 //                    randomYOdd = (int) (600*Math.random());
 //                }
 //
 //
 //                Vector2f freshmanPos = new Vector2f(randomXOdd, randomYOdd);
 //                Shape freshmanShape = new Rectangle(0, 0, freshmanImage.getWidth(), freshmanImage.getHeight());
 //                enemyList.add(new Freshman("freshman" + i, freshmanImage, freshmanPos, freshmanShape, 3));
 //                //LOCATION = LOCATION + 300;
 //            }
 //        } catch (SlickException e) {
 //            e.printStackTrace();
 //        }
 
         // load the powerups
 
         LOCATION = 600;
 
         try
         {
             for(int i = 0; i < NUM_OF_POWERUPS; i++)
             {
                 Image powerUpImage = new Image("res/character/ExtraLife.png");
                 Vector2f powerUpPos = new Vector2f(LOCATION, 370);
                 Shape powerUpShape = new Rectangle(0, 0, powerUpImage.getWidth(), powerUpImage.getHeight());
                 powerups.add(new ExtraLife("extraLife" + i, powerUpImage, powerUpPos, powerUpShape, 0, false));
                 //LOCATION = LOCATION + 300;
             }
         }
         catch(SlickException ex)
         {
             ex.printStackTrace();
         }
 
 
         // initialize lives
         try {
             //scoreboard stuff, loads the image
             for (int i = 0; i < STARTING_LIVES; i++) {
                 Image liveImage = new Image("res/misc/heart.png");
                 livesList.add(liveImage);
             }
         } catch (SlickException e) {
             e.printStackTrace();
         }
     }
 
     public void update(int speed, boolean isMoving, Player player, int delta) {
         this.time += delta;
 
         //Translates enemy
         if (isMoving) {
             for (int i = 0; i < enemyList.size(); i++) {
                 enemyList.get(i).setPosition(new Vector2f(enemyList.get(i).getPosition().getX() - speed, enemyList.get(i).getPosition().getY()));
             }
             for (int i = 0; i < powerups.size(); i++) {
                 powerups.get(i).setPosition(new Vector2f(powerups.get(i).getPosition().getX() - speed, 370));
             }
         }
         else {
             //TODO fix this ian. please. :)
             for (int i = 0; i < enemyList.size(); i++) {
                 enemyList.get(i).setPosition(new Vector2f(enemyList.get(i).getPosition().getX() - speed, enemyList.get(i).getPosition().getY()));
             }
             for (int i = 0; i < powerups.size(); i++) {
                 powerups.get(i).setPosition(new Vector2f(powerups.get(i).getPosition().getX() - speed, 370));
             }
         }
         for (int i = 0; i < enemyList.size(); i++){
             if(i < topE) {
                 enemyList.get(i).setPosition(new Vector2f(enemyList.get(i).getPosition().getX() - 5, enemyList.get(i).getPosition().getY()));
             }
            else if(i>=topH && i<bottomE) {
             enemyList.get(i).setPosition(new Vector2f(enemyList.get(i).getPosition().getX() + 2, enemyList.get(i).getPosition().getY()));
             }
         }
 
         isColliding = false;
         //checks for lives and collisions
         for (int i = 0; i < enemyList.size(); i++) {
             //if (player.isCollidingWith(enemyList.get(i)) && !livesList.isEmpty()) {
             if (player.isCollidingWith(enemyList.get(i)) && !livesList.isEmpty()) {
                 if(i < topE) {
                     topE--;
                    topH--;
                    bottomE--;
                    bottomH--;
                 }
                 else if(i < topH) {
                     topH--;
                    bottomE--;
                    bottomH--;
                 }
                 else if(i < bottomE) {
                     bottomE--;
                    bottomH--;
                 }
                 else {
                     bottomH--;
                 }
                 System.out.println("Collision with" + enemyList.get(i).getName());
                 livesList.remove(livesList.size() - 1);
                 enemyList.remove(i);
                 isColliding = true;
             } else {
                 //go to end screen
             }
 
         }
         // check if interacting with powerup
         for(int i = 0; i < powerups.size(); i++)
         {
             if(player.isCollidingWith(powerups.get(i)) && !powerups.isEmpty())
             {
                 System.out.println("Collision with" + powerups.get(i).getName());
                 try
                 {
                     livesList.add(new Image("res/misc/heart.png"));
                 }
                 catch(Exception ex)
                 {
                     ex.printStackTrace();
                 }
                 powerups.get(i).utilize(player);
                 powerups.remove(i);
             }
         }
 
         scoreboard.update(livesList.size(), time, powerups.size());
     }
 
     public void render(Graphics g) {
         scoreboard.render(g);
 
 
         //render enemies
         for (int i = 0; i < enemyList.size(); i++) {
             enemyList.get(i).render(g);
             g.draw(enemyList.get(i).getCollisionShape());
         }
 
         // render powerups
 
         for(int i = 0; i < powerups.size(); i++)
         {
             powerups.get(i).render(g);
             g.draw(powerups.get(i).getCollisionShape());
         }
 
         //draws the lives
 
         g.drawString("Lives:", 10, 10);
         int location = 70;
         for (int i = 0; i < livesList.size(); i++) {
             livesList.get(i).draw(location, 10);
             location += livesList.get(i).getWidth() + 7; //the integer is the spacing between the images
         }
 
 
         if (livesList.isEmpty()) {
             g.drawString("No more lives!", 300, 50);
         }
 
         //TODO replace this with the new image for the scoreboard background
         //timerBox.draw(2, 98);
         g.drawString("Time: " + time / 1000 + "s", 10, 47);
 
 
         if (time / 1000 >= 15) {
             g.drawString("No more time!", 500, 50);
         }
     }
     boolean getIsColliding(){
         return isColliding;
     }
 
 }
