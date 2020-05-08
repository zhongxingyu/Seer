 package atl.space.entities;
 
import static org.lwjgl.opengl.GL11.GL_LINE_STRIP;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex3f;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector3f;
//import java.util.ArrayList;
 
 public class PointTrailRenderComponent extends RenderableComponent {
 	public LinkedList<Vector3f> trail;
 	public int trailsize = 0;
 	public float trailfade = 0f;
 	
 	public PointTrailRenderComponent(){
 		trail = new LinkedList<Vector3f>();
 	}
 	public PointTrailRenderComponent(int ts, float tf){
 		trail = new LinkedList<Vector3f>();
 		trailsize = ts;
 		trailfade = tf;
 	}
 	
 	public void update(int delta, List<Entity> entities) {
 		trail.addFirst(new Vector3f(owner.position));
 		//System.out.println(trail.size());
 	}
 
 	@Override
 	public Component getStepped(int delta, List<Entity> entities) {
 		PointTrailRenderComponent prc = new PointTrailRenderComponent();
 		prc.update(delta, entities);
 		return prc;
 	}
 
 	@Override
 	public void render() {
 		glBegin(GL_LINE_STRIP);
 			float alpha = 1f;
 			glVertex3f(owner.position.x, owner.position.y, owner.position.z);
 			//System.out.println("Pos: " + owner.position.x+ " " + owner.position.y+ " " + owner.position.z);
 			cleanUpList();
 			Iterator<Vector3f> it = trail.iterator();
 			while(it.hasNext()){
 				Vector3f current = it.next();
 				glColor4f(1, 1, 1, alpha);
 				glVertex3f((float)current.x, (float)current.y, (float)current.z);
 				//System.out.println(trail.get(i).x+" "+trail.get(i).y+" "+trail.get(i).z);
 				alpha -= trailfade;
 				//System.out.println(alpha);
 			}
 			glColor4f(1, 1, 1, 1);
 		glEnd();
 		
 		//doesn't work for some reason
 	}
 	
 	public void cleanUpList(){
 		while (trail.size() > trailsize) {
 			 trail.removeLast();
 		}
 	}
 }
