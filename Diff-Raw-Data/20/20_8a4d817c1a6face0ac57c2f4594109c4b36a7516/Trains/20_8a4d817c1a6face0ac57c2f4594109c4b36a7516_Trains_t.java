 import org.newdawn.slick.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Rikki
  * Date: 22/09/13
  * Time: 01:06
  */
 public class Trains extends BasicGame {
 
     public static void main(String[] args) {
         // make a new game thing!
         Trains trains = new Trains("Trains"); // Trains!
    try {
        AppGameContainer agc = new AppGameContainer(trains);
        agc.setDisplayMode(800, 600, false);
        agc.start();
    } catch(SlickException e) {}
 
     }
 
     Map map = new Map(5);
     GUI gui = new GUI(map);
 
     public Trains(String title) {
         super(title);
     }
 
     @Override
     public void init(GameContainer container) throws SlickException {
 
     }
 
     @Override
     public void update(GameContainer container, int delta) throws SlickException {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void render(GameContainer container, Graphics g) throws SlickException {
         map.render(container, g);
         gui.render(container, g);
     }
 }
