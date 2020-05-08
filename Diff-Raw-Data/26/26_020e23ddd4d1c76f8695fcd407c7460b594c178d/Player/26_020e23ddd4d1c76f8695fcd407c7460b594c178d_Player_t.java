 package player;
 
 import enemies.Enemy;
 import util.EnemyDeletable;
 import java.awt.*;
 import util.EnemyPredicate;
 
 public abstract class Player {
 
     private static final int SENBONSAKURA_RADIUS = 100, 
             SENBONSAKURA_SQUARE = SENBONSAKURA_RADIUS*SENBONSAKURA_RADIUS;
     protected int lives;
     protected boolean isActive;
     protected int x;
     protected int y;
     protected EnemyDeletable parent;
     private int senbonSakuraN;
    private int senbonSakuraC;
 
     public Player(int _x, int _y, int numberOfLives, boolean startActive, int numberOfSenbonsakura, EnemyDeletable _parent) {
         lives = numberOfLives;
         isActive = startActive;
         x = _x;
         y = _y;
         parent = _parent;
         senbonSakuraN = numberOfSenbonsakura;
     }
 
     public int getX() {
         return x;
     }
 
     public int getY() {
         return y;
     }
 
     public int getLives() {
         return lives;
     }
 
     public void resetLives(int _lives) {
         lives = _lives;
     }
 
     public void decLives(int _x, int _y) {
         lives--;
         x = _x;
         y = _y;
     }
 
     public boolean isActive() {
         return isActive;
     }
 
     public void setActive(boolean isActive) {
         this.isActive = isActive;
     }
 
     public abstract void move();
 
     protected void senbonSakura() {
         if (senbonSakuraN-- > 0) {
            senbonSakuraC = 40;
         }
     }
 
     public void paint(Graphics g) {
         g.setColor(Color.WHITE);
         g.fillOval(x - 5, y - 5, 10, 10);
        if (senbonSakuraC-->0) {
             g.fillOval(x, y, SENBONSAKURA_RADIUS, SENBONSAKURA_RADIUS);
             parent.deleteIf(new EnemyPredicate() {
                 public boolean satisfiedBy(Enemy e) {
                     float p1 = (x + 5) - e.getX();
                     float p2 = (y + 5) - e.getY();
                     return (p1*p1 + p2*p2) < SENBONSAKURA_SQUARE;
                 }
             });
         }
     }
 }
