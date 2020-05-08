 package atl.space.entities;
 
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex3f;
 
 import java.util.List;
//import org.lwjgl.util.vector.Vector3f;
 
 public class PointRenderComponent extends RenderableComponent {
 	
 	public void update(int delta, List<Entity> entities) {
 		//do nothing
 	}
 
 	@Override
 	public Component getStepped(int delta, List<Entity> entities) {
 		PointRenderComponent prc = new PointRenderComponent();
 		prc.update(delta, entities);
 		return prc;
 	}
 
 	@Override
 	public void render() {
 		//openGL stuffs, render system?
 		//float[] point = new float[]{owner.position.x, owner.position.y, owner.position.z};
 		
 		//request a point somewhere, when all renders are called compiles requests into a VBO
 		//System.out.println("render!");
 		glBegin(GL_POINTS);
 			glVertex3f(owner.position.x, owner.position.y, owner.position.z);
 		glEnd();
 	}
 }
