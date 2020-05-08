 package is.ru.tgra;
 
 import java.nio.FloatBuffer;
 
 import org.lwjgl.opengl.GL11;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 
 public class ScreenText extends GraphicObject{
   private String text;
 
 public ScreenText(int x,int y,String text,FloatBuffer vb){
 	  super(x,y,vb);
 	  this.text = text;
   }
 
   @Override
   public void display(){
 	  SpriteBatch spriteBatch = new SpriteBatch();
 	  BitmapFont font = new BitmapFont();
 	  spriteBatch.begin(); 
 	  font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
	  font.draw(spriteBatch, this.text, this.getX(), this.getY()); 
 	  spriteBatch.end();
 	  Gdx.gl11.glVertexPointer(2, GL11.GL_FLOAT, 0, this.getVertexBuffer());
   }
 
   public String getText() {
 	return text;
 }
 
   public void setText(String text) {
 	this.text = text;
 }
   
 }
