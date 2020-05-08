 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.awt.*; 
 /**
  * Schrijft levens in de menubar
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Lifes extends Actor
 {
     public Lifes() {  
         GreenfootImage img = new GreenfootImage(200, 200);     
         img.setColor(new Color(255,255,255));    
         Font font = img.getFont();      
  
         img.setFont(font);      
        img.drawString("Health: ", 5, 195);  
         setImage(img);      
     }   
 }
