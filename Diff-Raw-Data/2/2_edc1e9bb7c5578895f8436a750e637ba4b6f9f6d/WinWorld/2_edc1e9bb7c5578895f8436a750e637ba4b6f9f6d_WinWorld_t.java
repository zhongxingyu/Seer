 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.awt.Color;
 /**
  * Write a description of class WinWorld here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class WinWorld extends World
 {
     Dialogue score;
     Dialogue notCrazy;
     /**
      * Constructor for objects of class WinWorld.
      * 
      */
     public WinWorld(int scor)
     {    
         // Create a new world with 600x400 cells with a cell size of 1x1 pixels.
         super(1024, 600, 1);
        //Greenfoot.playSound("victoryMusic.mp3");
         AddScoreDialogue("Your Score:" + scor, 512, 400);
         if (scor > 800) {
             AddNotCrazyDialogue();
         }
     }
     
     private void AddNotCrazyDialogue()
     {
         notCrazy = new Dialogue();
     
         GreenfootImage textImage = new GreenfootImage("You have overcome PTSD!", 48, Color.BLACK, Color.WHITE);
         //textImage.drawString(message, x, y);
         
         GreenfootImage image = new GreenfootImage(textImage.getWidth()+12, textImage.getHeight()+12);  
         image.setColor(Color.BLACK);  
         image.fill();  
         image.setColor(Color.WHITE);  
         image.fillRect(3, 3, image.getWidth()-6, image.getHeight()-6);  
         image.drawImage(textImage, 6, 6);
         
         notCrazy.setImage(image);
         addObject(notCrazy, 512, 300);
     }
     
     private void AddScoreDialogue(String message, int x, int y)
     {
 
         score = new Dialogue();
     
         GreenfootImage textImage = new GreenfootImage(message, 56, Color.BLACK, Color.WHITE);
         textImage.drawString(message, x, y);
         
         GreenfootImage image = new GreenfootImage(textImage.getWidth()+12, textImage.getHeight()+12);  
         image.setColor(Color.BLACK);  
         image.fill();  
         image.setColor(Color.WHITE);  
         image.fillRect(3, 3, image.getWidth()-6, image.getHeight()-6);  
         image.drawImage(textImage, 6, 6);
         
         score.setImage(image);
         addObject(score, x, y);
         
     }
 }
