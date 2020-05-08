 package paulenka.aleh.pm.appletree;
 
 import java.util.Random;
 
 public class AppleTree {
 
     private final static int LIMIT = 5000;
 
     private Random random = new Random();
 
     private int numberOfApples = 0;
 
     protected Random getRandom() {
         return random;
     }
 
     public int getNumberOfApples() {
         return numberOfApples;
     }
 
     protected void setNumberOfApples(int numberOfApples) {
         this.numberOfApples = numberOfApples;
     }
 
     public int grow() {
        int applesGrown = getRandom().nextInt(LIMIT - getNumberOfApples());
        setNumberOfApples(getNumberOfApples() + applesGrown);
 
        return applesGrown;
     }
 
     public int grow(int days) {
         int applesGrown = 0;
         for (int i = 0; i < days; ++i) {
             applesGrown += grow();
         }
         return applesGrown;
     }
 
     public int shake() {
        int applesFallen = getRandom().nextInt(getNumberOfApples());
        setNumberOfApples(getNumberOfApples() - applesFallen);
 
        return applesFallen;
     }
 
     public int shake(int seconds) {
         int applesFallen = 0;
         for (int i = 0; i < seconds; ++i) {
             applesFallen += shake();
         }
         return applesFallen;
     }
 }
