 
 
 
 import org.lwjgl.opengl.GL11;
 
 import com.badlogic.gdx.Gdx;
 
 public class EnemyBox extends Box
 {
 	public EnemyBox(float xpos, float ypos, float size, Boxesmustdie application, int hitpoints)
 	{
 		super(xpos, ypos, size, application, hitpoints, true);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void update() 
 	{
		if (this.hitpoints == 0 || this.xpos < -(this.size))
 		{
 			application.destroyed.add(this);
 		}
 		this.xpos -= application.level;
 	}
 	
 	@Override
 	public void display()
 	{
 		Gdx.gl11.glLoadIdentity();
         Gdx.gl11.glColor4f(1f, 0f, 0f, 0f);
         
         Gdx.gl11.glPushMatrix();
         Gdx.gl11.glTranslatef(xpos, ypos, 0);
         Gdx.gl11.glScalef(size,size,0);
         Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
         Gdx.gl11.glPopMatrix();
 	}
 }
