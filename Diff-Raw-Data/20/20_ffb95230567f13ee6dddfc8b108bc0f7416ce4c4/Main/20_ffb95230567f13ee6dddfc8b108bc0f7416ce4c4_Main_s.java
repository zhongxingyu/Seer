 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package raycaster;
 
 import Hardware_Accelerated.AGame;
 import Hardware_Accelerated.AccelGame;
 import MenuAndStory.Menu;
 import Utilities.Animation;
 import Utilities.FontStyle;
 import Utilities.FontType;
 import Utilities.ImageCollection;
 import Utilities.KeyBoard;
 import Utilities.Rect;
 import Utilities.SoundFile;
 import Utilities.Vector2;
 import bsodhackathon.BSoDHackathon;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import javax.swing.JComponent;
 import level.Level;
 import level.LevelMaster;
 import objects.ParticleManager;
 import objects.Player;
 import objects.Sprite;
 
 /**
  *
  * @author pcowal15
  */
 public class Main extends AGame{
     
     public static Main inst;
     
     Camera camera;
     Level level;
     Player player;
     ArrayList<Sprite> sprites;
     ParticleManager particles;
     public boolean isRebellionHappening;
     
     Animation begin;
     boolean isStart;
     long startTimer;
     SoundFile startSound;
     
     Menu menu;
     String currentLocation="train";
     
     boolean hasWon;
     boolean isDead;
     
     public int cycleNum;
 
     int fps;
     long st=System.currentTimeMillis();
     
     long updateTimer=System.currentTimeMillis();
     
     boolean first=true;
     
     
     @Override
     public void InitializeAndLoad() {
         inst = this;
         hasWon = false;
         isDead = false;
         menu = new Menu();
         isStart = true;
         startTimer = System.currentTimeMillis();
         startSound = new SoundFile("Resources/Sound/long_sound_1.wav", 0);
 //        begin=new Animation(71,0,new Vector2(400,300),250);
 //        new Thread(new makeIntro()).start();
         
         isRebellionHappening = false;
         player = new Player(500, 500, 0);
 //        LevelMaster.makeExists();
         sprites = new ArrayList<Sprite>();
         particles = new ParticleManager(Color.BLUE, -0.1, 0.5, 0, false, 300);//color,gravity,bounciness,air resistance,stickiness,lifetime
         this.setBackgroundColor(Color.BLACK);
         
         isStart = true;
         begin = new Animation(70, 0, new Vector2(400, 300), 250);
         makeIntro();
         startSound = new SoundFile("Resources/Sound/long_sound_1.wav", 0);
 //        startSound.start();
 //        startTimer = System.currentTimeMillis();
         
     }
 
     @Override
     public void UnloadContent() {
         
     }
 
     @Override
     public void Update() {
         if(first){
             startSound.start();
             startTimer= System.currentTimeMillis();
 //            mouse.captureMouse(true, new Vector2(400,300), new Vector2(800,600), AccelGame.gui);
             first=false;
         }
         
         if ((!isStart && menu.exitMenu) && (!isDead || !hasWon) ){//&& System.currentTimeMillis()-this.updateTimer >=16) {
             player.update(level);
             //player movement
             if (keyboard.isKeyDown('w') || keyboard.isKeyDown(KeyEvent.VK_UP) || keyboard.isKeyDown(KeyBoard.Eight)) {
                 player.moveForwards(level);
             }
             if (keyboard.isKeyDown(KeyEvent.VK_LEFT) || keyboard.isKeyDown(KeyBoard.Four)) {
                 player.turnLeft();
                 camera.screwFloor(0.5);
             }
             if (keyboard.isKeyDown('s') || keyboard.isKeyDown(KeyEvent.VK_DOWN) || keyboard.isKeyDown(KeyBoard.Two)) {
                 player.moveBackwards(level);
             }
 //            if(mouse.dx() != 0){
 //                player.turn(mouse.dx());
 //                camera.screwFloor(0.5);
 //            }
             
             if (keyboard.isKeyDown(KeyEvent.VK_RIGHT) || keyboard.isKeyDown(KeyBoard.Six)) {
                 player.turnRight();
                 camera.screwFloor(0.5);
             }
             if (keyboard.isKeyDown('a')) {
                 player.moveLeft(level);
             }
             if (keyboard.isKeyDown('d')) {
                 player.moveRight(level);
             }
             if (keyboard.isKeyDown('e')) {
                 player.swingSword();
             }else{
                 player.stopSwing();
             }
             camera.screwFloor(player.speed() / 10);
 
             //scope
             if (keyboard.isKeyDown(KeyEvent.VK_SPACE)) {
                 camera.setFOV(camera.fov + (Math.PI / 12 - camera.fov) * 0.1);
                 player.setDirSpeed(Math.PI / 360);
             } else {
                 camera.setFOV(camera.fov + (Math.PI / 4 - camera.fov) * 0.1);
                 player.setDirSpeed(Math.PI / 90);
             }
             
             if(keyboard.isKeyDown('m')){
                 menu.start();
             }
             
             camera.setZ(player.getZ(), 0);
             level.update(player, this);
             
             if(player.hasWon()){
                 this.hasWon=true;
             }
             if(player.getHelath() <= 0){
                 this.isDead=true;
             }
             this.updateTimer=System.currentTimeMillis();
         }else if(!menu.exitMenu){
             menu.Update(keyboard,player);
         }
         else{//isStart==true
             if(keyboard.isKeyDown(KeyEvent.VK_SPACE)){
                 isStart=false;
                 if(startSound.isAlive()){
                     startSound.kill();
                 }
             }
         }
     }
 
     @Override
     public void Draw(Graphics2D g, ImageCollection batch) {
         if (isStart) {
             if (System.currentTimeMillis() - startTimer >= 15250) {
                 isStart = false;
                 return;
             }else{
                 Rect r = new Rect(0, 0, (int) begin.getWidth(), (int) begin.getHeight(), 0);
                 begin.Draw(batch, 0f, r, 3f, 3f);
             }
         } else if (!isStart && (!isDead && !hasWon)) {
             if (!menu.exitMenu) {
                 menu.Draw(batch);
             }
             player.Draw(batch);
            camera.castRays(batch, player.getX(), player.getY(), player.getDirection());
            //camera.castRays(batch, player);
             if (sprites != null) {
                 for (Sprite o : sprites) {
                     camera.drawImage(batch, player, o.sprite(), o.x(), o.y(), true);
                 }
             }
             level.Draw(camera, player, batch);
             if (menu.exitMenu) {
                 batch.DrawString(new Vector2(680, 20), "M for Menu", Color.white, 1000000000, FontType.MONOSPACED, FontStyle.PLAIN, 12);
                 batch.DrawString(new Vector2(680, 35), "WASD to Move", Color.white, 1000000000, FontType.MONOSPACED, FontStyle.PLAIN, 12);
                 batch.DrawString(new Vector2(680, 50), "Arrows to Turn", Color.white, 1000000000, FontType.MONOSPACED, FontStyle.PLAIN, 12);
                 batch.DrawString(new Vector2(680, 65), "E to Swing Sword", Color.white, 1000000000, FontType.MONOSPACED, FontStyle.PLAIN, 12);
                 batch.DrawString(new Vector2(20, 500), "Current Location: " + this.currentLocation, Color.white, 1000000000, FontType.MONOSPACED, FontStyle.PLAIN, 12);
                 batch.DrawString(new Vector2(20, 515), "Health: " + this.player.getHelath(), Color.white, 1000000000, FontType.MONOSPACED, FontStyle.PLAIN, 12);
             }
         } else if (isDead) {
             batch.drawRect(new Vector2(0, 0), 800, 600, Color.black, 1000);
             batch.DrawString(new Vector2(200, 200), "YOU'RE DEAD", Color.red, 10000, FontType.MONOSPACED, FontStyle.PLAIN, 12);
         } else if (hasWon) {
             batch.drawRect(new Vector2(0, 0), 800, 600, Color.black, 1000);
             batch.DrawString(new Vector2(200, 200), "SUCCESS", Color.white, 10000, FontType.MONOSPACED, FontStyle.PLAIN, 12);
             batch.DrawString(new Vector2(200, 300), "After gaining all the data, I'm going to share this with the world.", Color.white, 10000, FontType.MONOSPACED, FontStyle.PLAIN, 12);
             batch.DrawString(new Vector2(200, 315), "The World Needs hope afterall", Color.white, 10000, FontType.MONOSPACED, FontStyle.PLAIN, 12);
         }
         
         if(System.currentTimeMillis()-st >= 1000){
             System.out.println("FPS: "+fps);
             st=System.currentTimeMillis();
             fps=0;
         }else{
             fps+=1;
         }
         
     }
 
     
     public static void main(String[] args){
         BSoDHackathon.main(args);
     }
     
     public void giveNextLevel(Level l){
         this.level=l;
         camera.setLevel(l.getWalls());
     }
     
     public boolean isRebellionHappening(){
         return isRebellionHappening;
     }
     
     public void makeIntro(){
         String path="Resources/Sprites/Introanimation";
         for(int i=1; i<72; i++){
             try{
                 if(i>=50 && i<60){
                     continue;
                 }
                 begin.addCell(path+"/"+i+".png");
                 
             }catch(Exception e){
                 System.out.println(path+"/"+i+".png");
             }
         }
     }
     
     public void setLocation(String loc){
         this.currentLocation=new String(loc);
     }
     public String getLocation(){
         return this.currentLocation;
     }
     
     public void cycleIncrease(){
         this.cycleNum+=1;
         this.cycleNum%=60;
     }
     
     public void giveResolutionAndQuality(int x, int y, String qual){
         //camera = new Camera(Math.PI / 4, 640, 640, 480);
         switch(qual){
             case "Low":
                 camera=new Camera(Math.PI/4, 320, x,y);
                 break;
             case "Med":
                 camera=new Camera(Math.PI/4, 640, x,y);
                 break;
             case "High":
                 camera=new Camera(Math.PI/4, 960, x,y);
                 break;
             case "Ultra":
                 camera=new Camera(Math.PI/4, 1270, x,y);
                 break;
             default:
                 camera=new Camera(Math.PI/4, 320, x,y);
         }
         LevelMaster.makeItemsAndNPC();
         LevelMaster.makeLevels();
         LevelMaster.makeWalls();
         LevelMaster.makeExists();
         level = LevelMaster.levels.get(LevelMaster.startLevel);
         level.make();
         level.setVisited(true);
         //camera=new Camera(Math.PI/4,1279,640,480); //MAX RAY COUNT
         camera.setLevel(level.getWalls()); //
     }
 }
