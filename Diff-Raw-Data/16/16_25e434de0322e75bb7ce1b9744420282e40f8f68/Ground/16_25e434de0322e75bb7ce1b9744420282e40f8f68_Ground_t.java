 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package irgame.object;
 
 import irgame.Game;
 import irgame.graphics.SpriteSheet;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 
 /**
  *
  * @author lkpit11dljo
  */
 public class Ground{
     private static final int SPRITE_SIZE = 32;
     public static final int WIDTH = SPRITE_SIZE;
     public static final int HEIGHT = SPRITE_SIZE;
     
     private final SpriteSheet SHEET = new SpriteSheet("/irgame/res/textures/moon_block_sprite_sheet.png"); //Loads the ground sprite sheet.
     private final Image SPRITE[] = new Image[5];
     public int spriteXPos;
     public int spriteYPos;
     
     public int xPos;
     public int yPos;
     public static final int[] Y_COORDINATES = new int[Game.GROUND.length];
     public Rectangle hitBox;
     
     public int HORIZ_VEL = 2;
     
     public Ground(int spriteXPos, int spriteYPos, int startXPos, int startYPos){    //The constructor that sets the starting position and sprite for the ground.   
         this.spriteXPos = spriteXPos * WIDTH;
         this.spriteYPos = spriteYPos * HEIGHT;
         for (int i = 0; i < SPRITE.length; i++){
             SPRITE[i] = SHEET.img.getSubimage(i*WIDTH, this.spriteYPos, WIDTH, HEIGHT); 
         }
         
         xPos = startXPos * WIDTH;
         yPos = Game.HEIGHT - startYPos * HEIGHT;
         hitBox = new Rectangle(xPos, yPos, WIDTH, HEIGHT);
     }
     
     public static void render(Graphics g, Ground[] grnd, Ground[] grndFill){    //Takes care of the rendering of the ground.
         for (int i = 0; i < grnd.length; i++){
             g.drawImage(grnd[i].SPRITE[grnd[i].spriteXPos/32], grnd[i].xPos, grnd[i].yPos, null);
             g.drawImage(grndFill[i].SPRITE[4], grndFill[i].xPos, grndFill[i].yPos, null);
             g.drawImage(grndFill[21+i].SPRITE[4], grndFill[21+i].xPos, grndFill[21+i].yPos, null);
         }      
     }
     
     public static void setSprite(Ground[] grnd){    //Sets the sprite for the object.
         for (int i = 0; i < grnd.length; i++){
             if (i == 0){
                 if (Y_COORDINATES[i] <= Y_COORDINATES[20]){
                      if (Y_COORDINATES[i] <= Y_COORDINATES[i+1]){
                         grnd[i].spriteXPos = 0 * grnd[i].WIDTH;
                     }else {
                         grnd[i].spriteXPos = 2 * grnd[i].WIDTH;
                     }  
                 }else {
                     if (Y_COORDINATES[i] > Y_COORDINATES[i+1]){
                         grnd[i].spriteXPos = 3 * grnd[i].WIDTH;
                     }else {
                         grnd[i].spriteXPos = 1 * grnd[i].WIDTH;
                     }
                 } 
             }else if(i == 20){
                 if (Y_COORDINATES[i] <= Y_COORDINATES[i-1]){
                     if (Y_COORDINATES[i] <= Y_COORDINATES[0]){
                         grnd[i].spriteXPos = 0 * grnd[i].WIDTH;
                     }else {
                         grnd[i].spriteXPos = 2 * grnd[i].WIDTH;
                     } 
                 }else {
                     if (Y_COORDINATES[i] > Y_COORDINATES[0]){
                         grnd[i].spriteXPos = 3 * grnd[i].WIDTH;
                     }else {
                         grnd[i].spriteXPos = 1 * grnd[i].WIDTH;
                     }
                 }
             }else{
                 if (Y_COORDINATES[i] <= Y_COORDINATES[i-1]){
                     if (Y_COORDINATES[i] <= Y_COORDINATES[i+1]){
                         grnd[i].spriteXPos = 0 * grnd[i].WIDTH;
                     }else {
                         grnd[i].spriteXPos = 2 * grnd[i].WIDTH;
                     }  
                 }else {
                     if (Y_COORDINATES[i] > Y_COORDINATES[i+1]){
                         grnd[i].spriteXPos = 3 * grnd[i].WIDTH;
                     }else {
                         grnd[i].spriteXPos = 1 * grnd[i].WIDTH;
                     }
                 }
             }
         }
     }
     
     public static void rePosY(int i){    //Repositions the y-coordinate of the object.
         if (i == 0){
             setYPos(19, 20, i);
         }else if (i == 1){
             setYPos(20, i-1, i);
         }else{
             setYPos(i-2, i-1, i);
         }
     } 
     
     private static void setYPos(int prevG2, int prevG, int curG){   //Sets the new y-coordinate of the object.
         switch(Y_COORDINATES[prevG]){
             case 1:
                if (Y_COORDINATES[prevG2] == 2){
                     Y_COORDINATES[curG] = 1; 
                 }else {
                     Y_COORDINATES[curG] = (int)(Math.random() * 2 + 1);
                 }
                 
                 break;
                 
             case 2:
                if (Y_COORDINATES[prevG2] != 2){
                     Y_COORDINATES[curG] = 2; 
                 }else {
                     Y_COORDINATES[curG] = (int)(Math.random() * 3 + 1);
                 }
                 
                 break;
                 
             case 3:
                if (Y_COORDINATES[prevG2] != 3){
                     Y_COORDINATES[curG] = 3;   
                 }else {
                     Y_COORDINATES[curG] = (int)(Math.random() * 2 + 2);
                 }
                 
                 break;
         }
     }
     
     public static boolean newLvl(int time, int level){  //Checks if it is possible to increase the level and returns true if it is.
         boolean nL = false;
        if (time == level*30 || time == level*29.5){
            nL = true;
         }
         return nL;
     }
     
     public static boolean outOfArea(Ground[] grnd, int i){  //Checks if the ground is out of the windowarea and retruns true if it is.
         boolean oOA = false;
        if (grnd[i].xPos <= -grnd[i].WIDTH){
             oOA = true;    
         }
         return oOA;
     }
     
     
     
 }
