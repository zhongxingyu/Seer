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
     //private static double velocity;
     //private static double friction/*Koeffisient*/; //Friction koeffisient? for the current underlag
     public static final int SPRITE_SIZE = 32;
     public static final int WIDTH = SPRITE_SIZE;
     public static final int HEIGHT = SPRITE_SIZE;
     private static final SpriteSheet sheet = new SpriteSheet("/irgame/res/textures/moon_block_sprite_sheet.png");
     //public static Image sprite;
     public static Image sprite[] = new Image[5];
     public int spriteXPos;
     public int spriteYPos;
     public int xPos;
     public int yPos;
     public int HORIZ_VEL = 2;
     public Rectangle hitBox;
     public static final int yCoordinates[] = new int[Game.ground.length];
     
     public Ground(int spriteXPos, int spriteYPos, int startXPos, int startYPos){
         this.spriteXPos = spriteXPos * WIDTH;
         this.spriteYPos = spriteYPos * HEIGHT;
         for (int i = 0; i < sprite.length; i++){
             sprite[i] = sheet.img.getSubimage(i*WIDTH, this.spriteYPos, WIDTH, HEIGHT);
         }
         
         xPos = startXPos * WIDTH;
         yPos = Game.HEIGHT - startYPos * HEIGHT;
         hitBox = new Rectangle(xPos, yPos, WIDTH, HEIGHT);
     }
     
     public static void render(Graphics g, Ground[] grnd, Ground[] grndF){    //Renders the ground
         for (int i = 0; i < grnd.length; i++){    //Draws every block at their given position
             g.drawImage(grnd[i].sprite[grnd[i].spriteXPos/32], grnd[i].xPos, grnd[i].yPos, null);
             g.drawImage(grndF[i].sprite[4], grndF[i].xPos, grndF[i].yPos, null);
             g.drawImage(grndF[21+i].sprite[4], grndF[21+i].xPos, grndF[21+i].yPos, null);
         }      
     }
     
     public static void setSprite(Ground[] grnd){
         for (int i = 0; i < grnd.length; i++){
             if (i == 0){
                 if (yCoordinates[i] <= yCoordinates[20]){
                      if (yCoordinates[i] <= yCoordinates[i+1]){
                         grnd[i].spriteXPos = 0 * grnd[i].WIDTH;
                     }else {
                         grnd[i].spriteXPos = 2 * grnd[i].WIDTH;
                     }  
                 }else {
                     if (yCoordinates[i] > yCoordinates[i+1]){
                         grnd[i].spriteXPos = 3 * grnd[i].WIDTH;
                     }else {
                         grnd[i].spriteXPos = 1 * grnd[i].WIDTH;
                     }
                 } 
             }else if(i == 20){
                 if (yCoordinates[i] <= yCoordinates[i-1]){
                     if (yCoordinates[i] <= yCoordinates[0]){
                         grnd[i].spriteXPos = 0 * grnd[i].WIDTH;
                     }else {
                         grnd[i].spriteXPos = 2 * grnd[i].WIDTH;
                     } 
                 }else {
                     if (yCoordinates[i] > yCoordinates[0]){
                         grnd[i].spriteXPos = 3 * grnd[i].WIDTH;
                     }else {
                         grnd[i].spriteXPos = 1 * grnd[i].WIDTH;
                     }
                 }
             }else{
                 if (yCoordinates[i] <= yCoordinates[i-1]){
                     if (yCoordinates[i] <= yCoordinates[i+1]){
                         grnd[i].spriteXPos = 0 * grnd[i].WIDTH;
                     }else {
                         grnd[i].spriteXPos = 2 * grnd[i].WIDTH;
                     }  
                 }else {
                     if (yCoordinates[i] > yCoordinates[i+1]){
                         grnd[i].spriteXPos = 3 * grnd[i].WIDTH;
                     }else {
                         grnd[i].spriteXPos = 1 * grnd[i].WIDTH;
                     }
                 }
             }
         }
     }
     
     public static void rePos(Ground[] grnd, Ground[] grndF, int i){
         if (i == 0){
             setYPos(19, 20, i);
         }else if (i == 1){
             setYPos(20, i-1, i);
         }else{
             setYPos(i-2, i-1, i);
         }
     } 
     
     private static void setYPos(int prevG2, int prevG, int curG){
         switch(yCoordinates[prevG]){
             case 1:
                 if(yCoordinates[prevG2] == 2){
                     yCoordinates[curG] = 1; 
                 }else {
                     yCoordinates[curG] = (int)(Math.random() * 2 + 1);
                 }
                 
                 break;
                 
             case 2:
                 if(yCoordinates[prevG2] != 2){
                     yCoordinates[curG] = 2; 
                 }else {
                     yCoordinates[curG] = (int)(Math.random() * 3 + 1);
                 }
                 
                 break;
                 
             case 3:
                 if(yCoordinates[prevG2] != 3){
                     yCoordinates[curG] = 3;   
                 }else {
                     yCoordinates[curG] = (int)(Math.random() * 2 + 2);
                 }
                 
                 break;
         }
     }
     
     public static boolean newLvl(int time, int level){
         boolean nL = false;
        System.out.println(time);
        if(time == level*30 || level != 1 && time == 0){
             nL = true;    
         }
         return nL;
     }
     
     public static boolean outOfArea(Ground[] grnd, int i){
         boolean oOA = false;
         if(grnd[i].xPos <= -grnd[i].WIDTH){
             oOA = true;    
         }
         return oOA;
     }
     
     
     
 }
