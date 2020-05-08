 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.awt.Color;
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
  * Write a description of class Reset here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Reset extends Buttons
 {
     public Reset() 
     {
         setImage(new GreenfootImage("Reset Highscore", 30, Color.BLUE, null));
     }
 
     public void act() 
     {
         setImage(new GreenfootImage("Reset Highscore", 30, Color.BLUE, null));
 
         if (Greenfoot.getMouseInfo() != null){
             if (Greenfoot.getMouseInfo().getX() >= 360 && Greenfoot.getMouseInfo().getX() <= 540 &&
             Greenfoot.getMouseInfo().getY() >= 665 && Greenfoot.getMouseInfo().getY() <= 685) {
                 setImage(new GreenfootImage("Reset Highscore", 30, Color.RED, null));
             }
         }
         if (Greenfoot.mouseClicked(this)) {
             File scores = new File(".scores");
             try{
                 if(scores.exists()==false){
                     scores.createNewFile();
                     PrintWriter out = new PrintWriter(new FileWriter(scores, false));
                    out.println(0);
                     out.close();
                 }
                 else{
                     PrintWriter out = new PrintWriter(new FileWriter(scores, false));
                    out.println(0);
                     out.close();
                 }
             }
             catch(IOException e){
                 System.out.println("COULD NOT LOG!!");
             }
             Greenfoot.setWorld(new Intro());
         }
     }
 }
