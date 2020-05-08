 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package raycaster;
 
 import Hardware_Accelerated.AGame;
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
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.logging.Logger;
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
 
     @Override
     public void InitializeAndLoad() {
         inst=this;
         hasWon=false;
         isDead=false;
         menu=new Menu();
         isStart=true;
         begin=new Animation(71,0,new Vector2(400,300),250);
         startTimer=System.currentTimeMillis();
         startSound=new SoundFile("Resources/Sound/long_sound_1.wav",0);
         new Thread(new makeIntro()).start();
         
         isRebellionHappening=false;
         player=new Player(500,500,0);
         LevelMaster.makeExists();
         sprites=new ArrayList<Sprite>();
         try {
             level=new Level("Resources/Dungeons/train.png",player,sprites);
         } catch (IOException ex) {
             Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         camera=new Camera(Math.PI/4,320,640,480);//Field of View, Number of Rays, Width, Height
         //camera=new Camera(Math.PI/4,1279,640,480); //MAX RAY COUNT
         camera.setLevel(level.getWalls()); //
         particles=new ParticleManager(Color.BLUE,-0.1,0.5,0,false,300);//color,gravity,bounciness,air resistance,stickiness,lifetime
         this.setBackgroundColor(Color.BLACK);
         LevelMaster.makeWalls();
         LevelMaster.makeExists();
         LevelMaster.makeItemsAndNPC();
         
         isStart=true;
         begin=new Animation(71,0,new Vector2(400,300),250);
         makeIntro();
         startTimer=System.currentTimeMillis();
         startSound=new SoundFile("Resources/Sound/long_sound_1.wav",0);
     }
 
     @Override
     public void UnloadContent() {
         
     }
 
     @Override
     public void Update() {
         if ((!isStart && menu.exitMenu) && (!isDead || !hasWon)) {
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
             if(player.getHelath() < -10){
                 this.isDead=true;
             }
             
         }else if(!menu.exitMenu){
             menu.Update(keyboard,player);
         }
         else{
             if(keyboard.isKeyDown(KeyEvent.VK_SPACE)){
                 isStart=false;
                 if(startSound.isAlive()){
                     startSound.kill();
                 }
             }
            if(!startSound.isAlive()){
                startSound.start();
            }
         }
     }
 
     @Override
     public void Draw(Graphics2D g, ImageCollection batch) {
         if (!isStart && (!isDead || !hasWon)) {
             if(!menu.exitMenu){
                 menu.Draw(batch);
             }
             player.Draw(batch);
             camera.castRays(batch, player.getX(), player.getY(), player.getDir());
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
                 batch.DrawString(new Vector2(20, 500), "Current Location: "+this.currentLocation, Color.white, 1000000000, FontType.MONOSPACED, FontStyle.PLAIN, 12);
                 batch.DrawString(new Vector2(20, 515), "Health: "+this.player.getHelath(), Color.white, 1000000000, FontType.MONOSPACED, FontStyle.PLAIN, 12);
             }
         }else if(isDead){
             batch.drawRect(new Vector2(0,0), 800, 600, Color.black, 1000);
             batch.DrawString(new Vector2(200, 200), "YOU'RE DEAD", Color.red, 10000, FontType.MONOSPACED, FontStyle.PLAIN, 12);
         }else if(hasWon){
             batch.drawRect(new Vector2(0,0), 800, 600, Color.black, 1000);
             batch.DrawString(new Vector2(200, 200), "SUCCESS", Color.white, 10000, FontType.MONOSPACED, FontStyle.PLAIN, 12);
             batch.DrawString(new Vector2(200, 300), "After gaining all the data, I'm going to share this with the world.", Color.white, 10000, FontType.MONOSPACED, FontStyle.PLAIN, 12);
             batch.DrawString(new Vector2(200, 315), "The World Needs hope afterall", Color.white, 10000, FontType.MONOSPACED, FontStyle.PLAIN, 12);
         }else{
             if(System.currentTimeMillis()-startTimer >= 15500){
                 isStart=false;
             }
             Rect r=new Rect(0,0,(int)begin.getWidth(),(int)begin.getHeight(),0);
             begin.Draw(batch,0f,r,3f,3f);
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
     
     private class makeIntro implements Runnable{
         public void run(){
             makeIntro();
         }
     }
     
     public void cycleIncrease(){
         this.cycleNum+=1;
         this.cycleNum%=60;
     }
     
 }
