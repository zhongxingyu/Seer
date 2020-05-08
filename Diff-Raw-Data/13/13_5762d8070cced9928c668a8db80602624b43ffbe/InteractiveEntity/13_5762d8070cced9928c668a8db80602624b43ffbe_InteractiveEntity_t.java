 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ianvernon
  * Date: 11/4/13
  * Time: 10:36 PM
  * To change this template use File | Settings | File Templates.
  */
 public class InteractiveEntity implements Entity {
     protected String name;
     protected Image image;
     protected Vector2f position;
 
    public InteractiveEntity(String name, Image image, Vector2f position)
     {
         this.name = name;
         this.image = image;
         this.position = position;
     }
 
     public String getName()
     {
         return name;
     }
     public Vector2f getPosition()
     {
         return position;
     }
     public void setPosition(Vector2f position)
     {
         this.position = position;
     }
     public void render(Graphics graphics)
     {
         image.draw(position.x, position.y);
     }
     public void update(GameContainer gc, StateBasedGame sbg, int delta)
     {
         // implement later
         return;
     }
 }
