 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 
 /**
  * Write a description of class Tut here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Tut extends World
 {
 
     /**
      * Constructor for objects of class Tut.
      * 
      */
     public Tut()
     {    
         // Create a new world with 600x400 cells with a cell size of 1x1 pixels.
         super(900, 700, 1, false); 
         
         prepare();
     }
     
     public void prepare() {
         setPaintOrder(Bot.class, Tutorial.class, Back.class, Instructions.class, Obstacles.class);
         Bot bot = new Bot();
         addObject(bot, 450, 588);
         Instructions instructions = new Instructions();
         addObject(instructions, 450, 170);
         Back back = new Back();
         addObject(back, 450, 655);
         Tutorial tutorial = new Tutorial();
         addObject(tutorial, 450, 350);
         //Background background = new Background();
         //addObject(background, 882, 17);
         Obstacles obstacles = new Obstacles();
         addObject(obstacles, 0, 0);
         /*for(int y = -50; y < getHeight() + 50; y += 6) { //spawn in snow
         Snow snow = new Snow();
         snow.setRotation(Greenfoot.getRandomNumber(360));
         addObject(snow, Greenfoot.getRandomNumber(getWidth()), y);
         }*/
         for(int y = getHeight() + 50; y > -700; y -= 3) { //spawn in trees
             obstacles.addTree(y);
         }   
         instructions.setLocation(451, 120);
         instructions.setLocation(446, 119);
         tutorial.setLocation(450, 339);
     }
    
    public Bot getBot() {
        return (Bot) getObjects(Bot.class).get(0);      
    }
 }
