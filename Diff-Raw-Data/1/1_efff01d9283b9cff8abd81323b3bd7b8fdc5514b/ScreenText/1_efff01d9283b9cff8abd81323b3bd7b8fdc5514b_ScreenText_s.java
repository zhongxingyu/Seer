 package is.ru.tgra;
 
 import java.nio.FloatBuffer;
 
 import org.lwjgl.opengl.GL11;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 
 /**
  * This class creates all the text you can see in the game as {@link GraphicObject}. It is
  * used for the score which the player has earned through the gameplay, as well display what
  * level he is on.
  */
 
 public class ScreenText extends GraphicObject
 {
     private String text;
     
     /**
      * A constructor which creates a graphical object along with a string for the text to
      * be displayed.
      * 
      * @param x		Position of the text on the x-grid.
      * @param y		Position of the text on the y-grid.
      * @param text	The text which is displayed.
      * @param vb	The vertex buffer which the objected is inserted.
      */
     public ScreenText(int x,int y,String text,FloatBuffer vb)
     {
         super(x,y,vb);
         this.text = text;
     }
     
     /**
      * A constructor similar to the one before, but includes no text.
      * 
      * @param x		Position of the text on the x-grid.
      * @param y		Position of the text on the y-grid.
      * @param text	The text which is displayed.
      * @param vb	The vertex buffer which the objected is inserted.
      */
     public ScreenText(int x,int y,FloatBuffer vb)
     {
         super(x,y,vb);
         this.text = "";
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void display()
     {
         SpriteBatch spriteBatch = new SpriteBatch();
         BitmapFont font = new BitmapFont();
         spriteBatch.begin();
         font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
         font.draw(spriteBatch, this.text, this.getX(), this.getY());
         spriteBatch.end();
         Gdx.gl11.glVertexPointer(2, GL11.GL_FLOAT, 0, this.getVertexBuffer());
     }
     
     /**
      * Getter function for the text message.
      * @return	The text to be displayed.
      */
     public String getText()
     {
         return text;
     }
     
     /**
      * Setter function for the text message.
      * @param text	The text to be displayed.
      */
     public void setText(String text)
     {
         this.text = text;
     }
 
 }
