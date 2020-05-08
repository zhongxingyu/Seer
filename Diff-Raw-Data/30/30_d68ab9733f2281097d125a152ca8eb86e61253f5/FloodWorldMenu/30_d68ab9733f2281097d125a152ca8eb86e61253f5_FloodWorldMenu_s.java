 import greenfoot.*;
 
 public class FloodWorldMenu extends World {
     private boolean nextMenu = false;
     public FloodWorldMenu() {
         super(80, 80, 10);
         setBackground("sandbaggame_menu.png");
     }
 
     @Override
     public void act() {
<<<<<<< HEAD
         if (Greenfoot.isKeyDown("1")) {
             Greenfoot.setWorld(new FloodWorld(SandbagPlayer.PlayerType.POLICE));
             return;
         }
 
         if (Greenfoot.isKeyDown("2")) {
             Greenfoot.setWorld(new FloodWorld(SandbagPlayer.PlayerType.GENIUS));
             return;
         }
 
         if (Greenfoot.isKeyDown("3")) {
             Greenfoot.setWorld(new FloodWorld(SandbagPlayer.PlayerType.CITIZEN));
             return;
=======
        if (Greenfoot.isKeyDown("enter")) {
            setBackground("achtergrondSelectie.png");
            nextMenu = true;
        }
        
        if (nextMenu == true) {
            if (Greenfoot.isKeyDown("1")) {
                Greenfoot.setWorld(new FloodWorld(Player.PlayerType.POLICE));
                return;
            }
    
            if (Greenfoot.isKeyDown("2")) {
                Greenfoot.setWorld(new FloodWorld(Player.PlayerType.GENIUS));
                return;
            }
    
            if (Greenfoot.isKeyDown("3")) {
                Greenfoot.setWorld(new FloodWorld(Player.PlayerType.CITIZEN));
                return;
            }
>>>>>>> 380c07292c09508898ec8fd506c88756e594a30c
         }
     }
 }
