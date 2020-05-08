 package objects;
 
 /**
  * Created with IntelliJ IDEA.
  * User: brad
  * Date: 4/4/13
  * Time: 6:48 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Player extends Entity {
 
     public Player() {
         super(Thing.playerImg);
         x = 1;
         y = 1;
     }
 
     public void move(int direction) {
         switch (direction) {
             // Left
             case 1:
                 x--;
                 return;
             // Up
             case 2:
                 y--;
                 return;
             // Right
             case 3:
                 x++;
                 return;
             // Down
             case 4:
                 y++;
         }
     }
 }
