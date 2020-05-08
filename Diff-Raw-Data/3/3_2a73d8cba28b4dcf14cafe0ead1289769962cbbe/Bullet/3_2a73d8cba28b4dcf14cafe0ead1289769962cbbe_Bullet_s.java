 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dandm_marpg_client;
 
 /**
  *
  * @author Dajne Win
  */
 public class Bullet extends Entity {
     
     public javax.swing.JButton button;
     private int x;
     private int y;
     public boolean addedButton;
     public boolean finishedMoving;
     private int finalX;
     private int finalY;
     private double gradient;
     private Player bulletOwner;
     
     public Bullet(String name, javax.swing.JButton bulletButton, Player owner, int initX, int initY, int destX, int destY)
     {
         super(name);
         button = bulletButton;
         x = initX;
         y = initY;
         finalX = destX;
         finalY = destY;
         gradient = 0.1;
         bulletOwner = owner;
         finishedMoving = false;
     }
     
     public void move()
     {
         if(x != finalX && y != finalY)
         {
             x = (int)(x + (gradient * (finalX - x)));
            y = (int)(y + (gradient * (finalX - y)));
             button.setBounds(x, y, 10, 10);
             gradient += 0.1;
         }
         else
         {
             finishedMoving = true;
         }
     }
     
 }
