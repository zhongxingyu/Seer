 package graphics;
 
 import static org.lwjgl.opengl.GL20.glUniformMatrix4;
 import static org.lwjgl.opengl.GL20.glUniform3f;
 
 import java.nio.FloatBuffer;
 
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 
 public class Camera {
 
 	private Vector3f pos;
 	private Vector3f target;
 	
 	private Matrix4f view;
 	private Matrix4f perspective;
 	
 	private FloatBuffer viewPerspective;
 	private boolean dirty;
 	
 	private int posUniform;
 	private int matUniform;
 	
 	private float fov = 45.f;
 	
 	
 	public Camera(Shader shader)
 	{
 		this.pos = new Vector3f(0.0f, 0.0f, 50.0f);
 		this.target = new Vector3f(0.0f, 0.0f, 0.0f);
 		
 		
 		this.dirty = true;
 		
 		this.view = GLOperations.buildViewMatrix(pos, target);
 		this.perspective = GLOperations.buildPerspectiveMatrix(fov, 1.33f, 0.1f, 1000f);
 		
 		//view, perspective apparently means perspective * view. AK does this too, dunno why
 		this.viewPerspective = GLOperations.generateFloatBuffer(Matrix4f.mul(view, perspective, null)); 
 	
 		this.posUniform = shader.getUniforms().get("cameraPosition");
 		this.matUniform = shader.getUniforms().get("viewPerspective");
 		
 	}
 	
 	public void setActive()
 	{
 		update();
 	}
 	
 	public void setPosition(Vector3f newPos)
 	{
 		pos = newPos;
 		dirty = true;
 	}
 	
 	public void addPosition(Vector3f delta)
 	{
 		dirty = true;
 		Vector3f.add(pos, delta, pos);
 	}
 	
 	
 	
 	public void update()
 	{
 		if(dirty)
 		{
 			dirty = false;
 			
//			System.out.println("position " + pos + "\ttarget: " + target);
			
 			//update everything for the time being
 			this.view = GLOperations.buildViewMatrix(pos, target);
 			this.perspective = GLOperations.buildPerspectiveMatrix(fov, 1.33f, 0.1f, 1000f);
			
 			
 			this.viewPerspective = GLOperations.generateFloatBuffer(Matrix4f.mul(perspective, view, null));
 			
 			
 			glUniformMatrix4(matUniform, false, viewPerspective);
 			glUniform3f(posUniform, pos.x, pos.y, pos.z);
 		}
 		
 		
 	}
 	
 	
 }
