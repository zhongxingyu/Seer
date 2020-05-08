 public class Robot{
   enum Emotions{DestroyAllHumans, Sober, Normal};
   public enum Move {
     Left, Right, Up, Down, Wait, Abort, Shave
   }
 
   public Point position;
   public int lambdaCount;
   public int score;
   public int waterTime;
   public int waterThreshold;
   public Robot(Point startingPosition) {
     position = startingPosition;
     lambdaCount = 0;
     score = 0;
     waterTime = 0;
   }
 
   public Robot(final Robot oldRobot) {
     position = new Point(oldRobot.position.r, oldRobot.position.c);
     lambdaCount = oldRobot.lambdaCount;
     score = oldRobot.score;
     waterTime = oldRobot.waterTime;
   }
 
   public void moveUp() {
     position.r++;
     score--;
   }
 
   public void moveDown() {
     position.r--;
     score--;
   }
 
   public void moveLeft() {
       position.c--;
       score--;
   }
 
   public void moveRight() {
       position.c++;
       score--;
   }
   public void setPosition(final int r, final int c)
   {
     position.c = c;
     position.r = r;
   }
   public void stay() {
       score--;
   }
 
   public void gainLambda() {
       lambdaCount++;
       score += 25;
   }
 
   public void liftLambda() {
    score += 50*lambdaCount;
   }
 
   public void abort() {
     score += 25*lambdaCount;
   }
 
   public int getScore() {
       return score;
   }
 
   public int getLambda() {
       return lambdaCount;
   }
 
   public void cleanWaterTime() {
       waterTime = 0;
   }
 
   public void stayInWater() {
       waterTime++;
   }
 
   public int getWaterTime() {
       return waterTime;
   }
   public Point getPosition()
   {
     return position;
   }
 
 }
