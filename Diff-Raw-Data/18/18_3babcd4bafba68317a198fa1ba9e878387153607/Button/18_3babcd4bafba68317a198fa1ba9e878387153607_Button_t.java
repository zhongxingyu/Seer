 package fi.hbp.angr.screens;
 
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector3;
 
 /* TODO It could be nice and clean to have one static touch handler here
  * to remove small overhead caused by current design. */
 
 /**
  *
  */
 public class Button {
     /**
      * Screen camera.
      *
      * Camera reference is needed for project between screen and openGL world.
      */
     private final Camera camera;
     /**
      * Font for drawing this button.
      */
     private BitmapFont font;
     /**
      * Action class that will be called if this button is touched.
      */
     private final ButtonAction action;
     /**
      * ID of this button.
      */
     private final int id;
 
     /**
      * Test vector used for bounds calculation.
      */
     private final Vector3 t1 = new Vector3();
     /**
      * Test vector used for bounds calculation.
      */
     private final Vector3 t2 = new Vector3();
 
     /**
      * UI Button Action Interface.
      */
     public interface ButtonAction {
         /**
          * Button action method that is called once if any
          * button is pressed.
          * @param id button id.
          */
         void buttonAction(int id);
     }
 
     /**
      * Constructor for UI button.
      * @param camera camera object used for projection.
      * @param font font that is used to draw this button.
      * @param action action that is called if button is touched.
      * @param id button identifier that is passed to the button action handler.
      */
     public Button(Camera camera, BitmapFont font, ButtonAction action, int id) {
         this.camera = camera;
         this.font = font;
         this.action = action;
         this.id = id;
     }
 
     /**
      *
      * @param batch sprite batch to be used.
      * @param text current button text.
      * @param x coordinate.
      * @param y coordinate.
      */
     public void draw(SpriteBatch batch, String text, float x, float y) {
         TextBounds bnd = font.draw(batch, text, x, y);
 
         t1.set(x, y, 0f);
         float y2 = (y >= 0 ) ? y + font.getLineHeight() : y - font.getLineHeight();
         t2.set(x + bnd.width, y2, 0f);
 
         camera.project(t1);
         camera.project(t2);
 
         t1.y = (float)(Gdx.graphics.getHeight()) - t1.y;
         t2.y = (float)(Gdx.graphics.getHeight()) - t2.y;
 
         if(justTouched((int)t1.x, (int)t1.y, (int)t2.x, (int)t2.y)) {
             action.buttonAction(id);
         }
     }
 
     /**
      * Check if an area was just touched.
      * @param x1 border coordinate.
      * @param y1 border coordinate.
      * @param d2 border coordinate.
      * @param d2 border coordinate.
      * @return true if given area was just touched; otherwise false.
      */
     protected static boolean justTouched(int x1, int y1, int x2, int y2) {
         int tmp;
 
         if (x1 > x2) {
             tmp = x1;
             x1 = x2;
             x2 = tmp;
         }
 
         if (y1 > y2) {
             tmp = y1;
             y1 = y2;
             y2 = tmp;
         }
 
         if (Gdx.input.justTouched()) {
             int px = Gdx.input.getX();
             int py = Gdx.input.getY();
 
             if ((px >= x1 && px <= x2) && (py >= y1 && py <= y2)) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Get ID of this button.
     * @return id of this button.
      */
     public int getID() {
         return id;
     }
 }
