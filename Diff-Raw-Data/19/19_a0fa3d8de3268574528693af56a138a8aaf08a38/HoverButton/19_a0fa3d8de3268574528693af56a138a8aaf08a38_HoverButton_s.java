 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lamprey.seprphase3.GUI.Images;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.EventListener;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.Array;
 
 /**
  *
  * @author Simeon
  */
 public class HoverButton extends Image {
     private final static float OVER     = 1f;
     private final static float NOT_OVER = 0.8f;
     private final static float PRESSED  = 0.4f;
     private final static float STEP     = 0.025f;
     
     private Array<EventListener> listeners;
     private ClickListener listener;
     private HoverButtonType type;
     private float current;
 
     
     public HoverButton(Texture texture, HoverButtonType type) {
         super(texture);
         this.type = type;
        current = 0.8f;
     }
     
     public HoverButton(HoverButtonType type) {
         super();
         this.type = type;
         if(type == HoverButtonType.Component) {
             current = 1f;
         }
         else {
             current = 0.8f;
         }
     }
     
     @Override
     public void draw (SpriteBatch batch, float parentAlpha) {
         listeners = this.getListeners();
         
         if (listeners.size > 0 && (listeners.first() instanceof ClickListener)) {
             listener = (ClickListener) listeners.first();
             
             //Increase and decrease current based on mouse hover
             if      (listener.isPressed()) {
                 current = PRESSED;
             }
            else if (type == HoverButtonType.Component && listener.isOver() && current > NOT_OVER ) {
                 current -= STEP;
             }
            else if (type == HoverButtonType.Component && !listener.isOver() && current < OVER) {
                 current += STEP;
             }
            else if (listener.isOver() && current < OVER) {
                 current += STEP; }
            else if (!listener.isOver() && current > NOT_OVER) {
                 current -= STEP; }
             
             //Put current to min or max if it underflows or overflows
             if      (current > OVER) {
                 current = OVER; }
             else if (!listener.isPressed() && current < NOT_OVER) {
                 current = NOT_OVER; }
             
             //Set the proper color and draw the button
             if (listener.isPressed() || type == HoverButtonType.NotTransparent || type == HoverButtonType.Component) {
                 this.setColor(current, current, current, 1f); }
             else {
                 this.setColor(current, current, current, current); }
             super.draw(batch, parentAlpha);
         }
         else {
             super.draw(batch, parentAlpha);
         }
     }
 }
