 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.awt.Color;
 
 public class SnowWorld extends World
 {
     private SpeakerButton sButton = new SpeakerButton();
     private GreenfootSound bkgMusic, bkgMusic2;
     private boolean musicChange = false;
     private Lives theLives;
     public Counter score = new Counter("Score: ");
     public int scoreMult = 1;
     public int multCounter = 0;
     public int coins = 0;
     public static boolean jumpU = false;
     public static boolean rampCoins = false;
     public static boolean rocketsSpawn = false;
     public boolean chase = false;
     private int storeTimer = 0;
     private int c;
     private SnowWorld sw;
     public void act() {
         volumeAdjust();
         spawnPowerup();
         multCounter--;
         showPowerup();
         addSnowMobile();
         speedUp();
         storeTimer++;
         
         if (Greenfoot.isKeyDown("s") && storeTimer >= 600){
             Greenfoot.setWorld(new StoreWorld(sw,coins));
             storeTimer = 0;
         }    
     }
 
     public SnowWorld(int Coins) {    
         super(900, 700, 1, false); 
         coins = Coins;
         
         theLives = new Lives();
         addObject(theLives, 820, 670);
         //start music in the snow world
         bkgMusic = new GreenfootSound("Animals - Martin Garrix.mp3");
         //Credit: Martin Garrix
         bkgMusic2 = new GreenfootSound("chase.mp3");
         //Credit: ????
         bkgMusic.playLoop();
         Object.speed = 3;
         prepare();
     }
 
     public Lives getLives() {
         return theLives;
     }
 
     public void stopMusic() {
         bkgMusic.stop();
         bkgMusic2.stop();
     }
 
     private void prepare() {
         setPaintOrder(Counter.class, Coin2.class, SnowMobile.class, Boarder.class, Image.class, Lives.class, Buttons.class, Obstacles.class, Weapon.class, Pickup.class, SnowMobileCrashed.class ,SnowTrail.class);
         Boarder boarder = new Boarder();
         addObject(boarder, getWidth()/2, 400);
         Obstacles obstacles = new Obstacles();
         addObject(obstacles, 1000, 1000);
         Back back = new Back();
         addObject(back, 30, 680);
         Back back2 = new Back();
         addObject(back2, 100, 680);
         for(int y = getHeight() + 50; y > -700; y -= 3) { //spawn in trees
             obstacles.addTree(y);
         }      
         addObject(score, getWidth()/2, 17);
         Highscore hs = new Highscore();
         addObject(hs, getWidth() - hs.getImage().getWidth()/2 - 5, 17);
         addObject(sButton, 30, 60);
     }
 
     public void spawnPowerup() {
         Boarder b = getBoarder();
         if (Greenfoot.getRandomNumber(2000) == 0 && getScore() >= 15000) { 
             addObject2(new Gun(), randX(-100), -100);
         }
         if (rocketsSpawn && Greenfoot.getRandomNumber(2000) == 0 && getScore() >= 75000) { 
             addObject2(new RocketLauncher(), randX(-100), -100);
         }
         if (Greenfoot.getRandomNumber(2000) == 0 && getScore() >= 25000  && getObjects(Shield.class).size() == 0 && (b != null && !b.shield || b == null)) { 
             addObject2(new Shield(), randX(-100), -100);
         }
         if (Greenfoot.getRandomNumber(3000) == 0 && getObjects(Invincible.class).size() == 0 && (b != null && b.invincible > 100 || b == null) && getScore() >= 15000) { 
             addObject2(new Invincible(), randX(-100), -100);
         }
         if (Greenfoot.getRandomNumber(100) == 0) {
             addObject2(new Coin(), randX(-100), -100);
         }
         if (Greenfoot.getRandomNumber(300) == 0) {
             addObject2(new Coin2(), randX(-100), -100);
         }
         if (Greenfoot.getRandomNumber(400) == 0 && getScore() >= 20000) {
             Ramp r = new Ramp();
             int x = randX(-100, 30);
             if (getObjectsAt(x, -100, Object.class).size() == 0) {
                 addObject2(r, x, -100);
                 int rx = r.getX();
                 int ry = r.getY();            
                 int rh = r.getImage().getHeight();
                 addObject2(new Coin2(), rx, ry - rh/2 - 8*Object.speed);
                 addObject2(new Coin2(), rx, ry - rh/2 - 24*Object.speed);
                 addObject2(new Coin2(), rx, ry - rh/2 - 40*Object.speed);
                 addObject2(new Coin2(), rx, ry - rh/2 - 56*Object.speed);  
                 if (rampCoins) {
                     addObject2(new Coin2(), rx, ry - rh/2 - 16*Object.speed);
                     addObject2(new Coin2(), rx, ry - rh/2 - 32*Object.speed);
                     addObject2(new Coin2(), rx, ry - rh/2 - 48*Object.speed);
                     addObject2(new Coin2(), rx, ry - rh/2 - 64*Object.speed);  
                 }    
             }
         }
         if (Greenfoot.getRandomNumber(1500) == 0 && getObjects(ScoreX2.class).size() == 0 && scoreMult == 1) {
             addObject2(new ScoreX2(), randX(-100), -100);
         }
         if (Greenfoot.getRandomNumber(800) == 0) {
             spawnLineOfCoins();
         }
         if (Greenfoot.getRandomNumber(1500) == 0 && getObjects(Magnet.class).size() == 0 && (b != null && b.magnetTimer <= 0 || b == null) && getScore() >= 35000) {
             addObject2(new Magnet(), randX(-100), -100);
         }
     }
 
     public void spawnLineOfCoins() {
         int x1 = randX(-20);
         int r = Greenfoot.getRandomNumber(5) + 5;
         int x2 = randX(-20 - 60*r);
         for (int i = 0; i < r; i++) {
             addObject2( new Coin(), (i * x2 + (r - i) * x1) / r, -20 - 60*i);
         }
     }
 
     public int randX(int y) {
         return randX(y, 10);
     }
 
     public int randX(int y, int precision) { //Returns random x-value between the trees at a given y-value
         int min = 0;                         //x-value will be at least precision units away from the trees
         int max = 0;
         Tree tree = new Tree();
         int w = tree.getImage().getWidth();
         for(int i = w/2; getObjectsAt(i, y, Tree.class).size() != 0; i += w) {
             min = i + w;
         }
         for(int i = min + w; getObjectsAt(i, y, Tree.class).size() == 0 && i < getWidth(); i += w) {
             max = i;
         }
         return Greenfoot.getRandomNumber(max - min - 2*precision) + min + precision;
     }
 
     public void scoreMult(int multiplier, int frames) {
         multCounter = frames;
         scoreMult = multiplier;
     }
 
     public void incScore(int pts) {
         scoreMult = (multCounter <= 0) ? 1 : scoreMult;
         score.add((Object.speed - 2) * scoreMult * pts);    
     }
 
     public void showPowerup() {
         removeObjects(getObjects(Image.class));
         Image coinImage = new Image("coin.png");
         coinImage.getImage().scale(20, 20);
         Image coinNumImage = new Image("" + coins, 30, Color.BLACK, Color.WHITE);
         addObject(coinImage, coinImage.getWidth()/2 + 5, coinImage.getHeight()/2 + 5);
         addObject(coinNumImage, coinImage.getWidth() + 8 + coinNumImage.getWidth()/2, coinImage.getHeight()/2 + 5);
         coinNumImage.getImage().setTransparency(220);
         if (multCounter > 0 && scoreMult == 2) {
             Image multImage = new Image("x2.png");
             addObject(multImage, score.getX() + score.getImage().getWidth()/2 + multImage.getWidth()/2 + 4, 15);
         }
         if(multCounter > 0 && ScoreX2.Multiplier == 3){
             Image multImage2 = new Image("x3.png");
             addObject(multImage2, score.getX() + score.getImage().getWidth()/2 + multImage2.getWidth()/2 + 4, 15);
         }    
         if (getObjects(Boarder.class).size() != 0) {
             Boarder b = getBoarder();
             if (b.gun > 0) {
                 Image gunImage = new Image("GunPowerup.png");
                 addObject(gunImage, score.getX() - score.getImage().getWidth()/2 - gunImage.getWidth()/2 - 4, 20);
                 Image gunNumImage = new Image("" + b.gun, 30, Color.BLACK, null);
                 addObject(gunNumImage, gunImage.getX() - 40, 15);
             }
         }
         if (getObjects(Boarder.class).size() != 0) {
             Boarder b = getBoarder();
             if (b.magnetTimer > 0) {
                 Image magImage = new Image("magnet.png");
                 addObject(magImage, score.getX() + score.getImage().getWidth()/2 + (new Image("x2.png")).getWidth() + magImage.getWidth()/2 + 8, 15);
             }
         }
         if (getObjects(Boarder.class).size() != 0) {
             Boarder b = getBoarder();
             if (b.shield) {
                 Image shieldImage = new Image("shield.png");
                 addObject(shieldImage, score.getX() - score.getImage().getWidth()/2 - shieldImage.getWidth()/2 - 100, 20);
             }
         }
         if (getObjects(Boarder.class).size() != 0) {
             Boarder b = getBoarder();
             if (b.rocket > 0) {
                 Image rocketImage = new Image("RocketAndrew.png");
                 addObject(rocketImage, score.getX() - score.getImage().getWidth()/2 - rocketImage.getWidth()/2 - 160, 20);
                 Image rNumImage = new Image("" + b.rocket, 30, Color.BLACK, null);
                 addObject(rNumImage, rocketImage.getX() - 30, 15);
             }
         }
     }
 
     public void speedUp() { 
         int speed1 = Object.speed;
         Object.speed = (int) (2.5 + .5*Math.sqrt(8 * getScore() / 10000 + 1));
         if (speed1 < Object.speed) {
             (new GreenfootSound("Speedboost.mp3")).play();
         }
     }
     
     public void addCoin(int quant) {
         coins += scoreMult * quant;
     }
 
     public int getCoins() {
         return coins;
     }
 
     public void setCoins(int Coins) {
         coins = Coins;
     }
 
     public Boarder getBoarder() {
         return (getObjects(Boarder.class).size() > 0) ? (Boarder) getObjects(Boarder.class).get(0) : null;
     }
 
     public void addObject2(Actor a, int x, int y) { //adds an object at (x,y) if there is no object there already
         if (getObjectsAt(x, y, Object.class).size() == 0) addObject(a, x ,y);        
     }
 
     public int getScore() {
         return score.getValue();
     }
 
     public void addSnowMobile() {
         Boarder boarder = getBoarder();
         if (Greenfoot.getRandomNumber(2000) <= Object.speed && getScore()>=50000) {
             //addObject(new SnowMobile(), getBoarder().getX(), getHeight() + 50);
             addObject(new SnowMobile(), randX(getHeight() + 50, 80), getHeight() + 50);
             (new GreenfootSound("policeSound.mp3")).play();
             if(chase == false){
                 bkgMusic.stop();
                 bkgMusic2.playLoop();
                 chase = true;
             }
         }
     }
 
     public SpeakerButton getSpeaker() {
         return sButton;
     }
 
     public void volumeAdjust() {
         if (getSpeaker().getOff()==true) {
             bkgMusic.setVolume(0);
             bkgMusic2.setVolume(0);
         }
         else if (getSpeaker().getOff()==false) {
             bkgMusic.setVolume(80);
            bkgMusic.setVolume(80);
         }
     }
     // upgrades
     public void JumpU() {
         jumpU = true;
     }
 
     public boolean getJumpU() {
         return jumpU;
     }
 }
