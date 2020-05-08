 import greenfoot.*;
 import java.lang.Object;
 import java.util.List;
 
 public class Player extends Actor {
     private int speed;
     private int initialSpeed;
     private int vSpeed = 0;
     private int acceleration = 1;
     private int jumpStrength = 4;
     private boolean jumping = false;
     private boolean falling = false;
     private int reduceCollisionDetection = 3;
     private int currCollDetection = 0;
 
     protected GreenfootImage image1;
     protected GreenfootImage image2;
     protected GreenfootImage image3;
     protected GreenfootImage image4;
     protected GreenfootImage image5;
     protected GreenfootImage image6;
     protected GreenfootImage image7;
     protected GreenfootImage image8;
 
 
    public Player() {
         this.initialSpeed = speed;
        this.speed = 5;
         
         //super(6);
         getImage().scale(40, 40);
         image1 = new GreenfootImage("POLITIElinks2.png");
         image2 = new GreenfootImage("POLITIElinks1.png");
         image3 = new GreenfootImage("POLITIErechts2.png");
         image4 = new GreenfootImage("POLITIErechts1.png");
         image5 = new GreenfootImage("POLITIEachter2.png");
         image6 = new GreenfootImage("POLITIEachter1.png");
         image7 = new GreenfootImage("POLITIEvoor2.png");
         image8 = new GreenfootImage("POLITIEvoor1.png");
     }
 
     @Override
     public void act() {
         getImage().scale(40, 40);
         checkFall();
         onTouchMovingBrick();
         if (speed / 4 <= 0) speed = 4;
 
         if (Greenfoot.isKeyDown("a")) {
             move(-speed / 4, 0);
             switchImageLeft();
         }
 
         if (Greenfoot.isKeyDown("d")) {
             move(+speed / 4, 0);
             switchImageRight();
         }
         
         if (Greenfoot.isKeyDown("space") && jumping == false) {
             jump();
         }
         
         int waterOffset = 70 - ((SkyscraperWorld)getWorld()).getWaterLevel() / 2 / 10;
         if (waterOffset <= getY()) {
              setRotation(90);
             ((SkyscraperWorld)getWorld()).gameOver();
             return;
         }
 
         
         
         Actor coin = getOneIntersectingObject(Coin.class);
         if (coin != null) {
             Greenfoot.playSound("Coin.wav");
             getWorld().removeObject(coin);
             ((SkyscraperWorld)getWorld()).getScoreCounter().add(10);
         } 
     }
 
     private void move(int dx, int dy) {
      setLocation(getX() + dx, getY() + dy);
     }
     
     public void jump() {
         if (vSpeed <= 0 && !jumping) {
             jumping = true;
             vSpeed = -jumpStrength;
         }
     }
     
     public void fall() {
         setLocation(getX(), getY() + vSpeed);
         if (vSpeed > 4) {
             vSpeed = 4;
         } else {
             vSpeed = vSpeed + acceleration;
         }
         
         boolean moveUp = false;
         Actor obj = null;
         for (int i = 1; i <= 4; ++i) {
             obj = getOneObjectAtOffset(0, i, Surface.class);
             if (obj != null) {
                 moveUp = true;
                 break;
             }
         }
         if (moveUp) {
             setLocation(getX(), obj.getY() - 4);
         }
     }
 
     private void checkFall() {
         if (jumping || !onGround()) {
             fall();
         }
         
         if (onGround()) {
             vSpeed = 0;
             jumping = false;
         }
     }
     
     public boolean onGround()
     {
          return getOneObjectAtOffset(-1, 4, Surface.class) != null
              || getOneObjectAtOffset(0, 4, Surface.class) != null
              || getOneObjectAtOffset(1, 4, Surface.class) != null;
     }
     
     public void onTouchMovingBrick(){
         Actor actor = null;
         boolean unused = (actor = getOneObjectAtOffset(-1, 4, MovingBrickLeft.class))!= null 
         || (actor = getOneObjectAtOffset(0, 4, MovingBrickLeft.class))!= null
         || (actor = getOneObjectAtOffset(1, 4, MovingBrickLeft.class))!= null;
         if(actor != null) {
             setLocation(getX() + ((MovingBrickLeft)actor).getMovingBrickLeftSpeed(), getY()); 
         }
         
         Actor actor2 = null;
         boolean unused2 = (actor2 = getOneObjectAtOffset(-1, 4, MovingBrickRight.class))!= null 
         || (actor2 = getOneObjectAtOffset(0, 4, MovingBrickRight.class))!= null
         || (actor2 = getOneObjectAtOffset(1, 4, MovingBrickRight.class))!= null;
         if(actor2 != null) {
             setLocation(getX() + ((MovingBrickRight)actor2).getMovingBrickRightSpeed(), getY()); 
         }
         
         Actor actor3 = null;
         boolean unused3 = (actor3 = getOneObjectAtOffset(-1, 4, MovingBrickUp.class))!= null 
         || (actor3 = getOneObjectAtOffset(0, 4, MovingBrickUp.class))!= null
         || (actor3 = getOneObjectAtOffset(1, 4, MovingBrickUp.class))!= null;
         if(actor3 != null) {
             setLocation(getX(), getY() + ((MovingBrickUp)actor3).getMovingBrickUpSpeed()); 
         }
     }
     
     protected void switchImageLeft()
     {
         if (getImage() == image1)
         {
             setImage(image2);
         }
         else
         {
             setImage(image1);
         }
     }
 
     protected void switchImageRight()
     {
         if (getImage() == image3)
         {
             setImage(image4);
         }
         else
         {
             setImage(image3);
         }
     }
 
     protected void switchImageStraight()
     {
         if (getImage() == image5)
         {
             setImage(image6);
         }
         else
         {
             setImage(image5);
         }
     }
 
     protected void switchImageBack()
     {
         if (getImage() == image7)
         {
             setImage(image8);
         }
         else
         {
             setImage(image7);
         }
     }
 }
