 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Lewis
  * Date: 22/09/13
  * Time: 15:50
  * To change this template use File | Settings | File Templates.
  */
 
 
 public class GUI {
 
     private static int direction;
     static Image[] arrow;
     int size;
 
     public GUI (Map map) {
         size = map.GetSize();
     }
 
     static {
         try {
         direction = 0;
         arrow = new Image[5];
        arrow[0] = new Image("/src/Resources/D0_Centre.png");
        arrow[1] = new Image("/src/Resources/D1_Up.png");
        arrow[2] = new Image("/src/Resources/D2_Right.png");
        arrow[3] = new Image("/src/Resources/D3_Down.png");
        arrow[4] = new Image("/src/Resources/D4_Left.png");
         } catch(SlickException e) {
             // do nothing
         }
     }
     //set to 0 after changing tile, set when press direction
     public void setDirection(int d){
         direction = d;
     }
 
     public void render(GameContainer container, Graphics g) throws SlickException {
         arrow[direction].draw(size*100, (size-1)*100);
     }
 
 }
