 package atl.space.components.render;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.gluProject;
 
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.List;
 
 import atl.space.components.Component;
 import atl.space.entities.Entity;
 
 import utility.BufferTools;
 
 public class Overlay2DRenderComponent extends RenderableComponent {
 	
 	//private FloatBuffer perspectiveProjectionMatrix = BufferTools
 			//.reserveDataf(16);
 	private FloatBuffer orthographicProjectionMatrix = BufferTools
 			.reserveDataf(16);
 	
 	public Overlay2DRenderComponent(){
 		//do camera setup before this
 		this(createOrthoMatrix(-1, 1, -1, 1, 1, -1));
 	}
 	public Overlay2DRenderComponent(String id){
 		super(id);
 		setOrthoProjMatrix(createOrthoMatrix(-1, 1, -1, 1, 1, -1));
 	}
 	public Overlay2DRenderComponent(FloatBuffer orthoProjMatrix){
 		super("overlay2drendercomponent");
 		setOrthoProjMatrix(orthoProjMatrix);
 	}
 	
 	public void setOrthoProjMatrix(FloatBuffer orthoProjMatrix){
 		orthographicProjectionMatrix = orthoProjMatrix;
 	}
 	public FloatBuffer getOrthoProjMatrix(){
 		return orthographicProjectionMatrix;
 	}
 	
 	public Overlay2DRenderComponent(Overlay2DRenderComponent orc) {
 		super(orc.getId());
 		this.orthographicProjectionMatrix = orc.orthographicProjectionMatrix;
 	}
 	public Component clone() {
 		return new Overlay2DRenderComponent(this);
 	}
 	public static FloatBuffer createOrthoMatrix(float left, float right, float bottom, float top, 
 			float near, float far){
 		FloatBuffer orthoProjMatrix = BufferTools
 				.reserveDataf(16);
 		glPushMatrix();
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glOrtho(left, right, bottom, top, near, far);
 		glGetFloat(GL_PROJECTION_MATRIX, orthoProjMatrix);
 		glPopMatrix();
 		glMatrixMode(GL_MODELVIEW);
 		return orthoProjMatrix;
 	}
 	
 	@Override
 	public void render() {
 		//implementations of render will use setUp2d and backTo3d
 	}
 	
 	public FloatBuffer getWinPos(){
 		FloatBuffer winpos = BufferTools.reserveDataf(3);
 
 		IntBuffer viewport = BufferTools.reserveDatai(16);
 		FloatBuffer modelview = BufferTools.reserveDataf(16);
 		FloatBuffer projection = BufferTools.reserveDataf(16);
 
 		glGetFloat(GL_MODELVIEW_MATRIX, modelview);
 		glGetFloat(GL_PROJECTION_MATRIX, projection);
 		glGetInteger(GL_VIEWPORT, viewport);
 
 		gluProject(owner.position.x, owner.position.y, owner.position.z,
 				modelview, projection, viewport, winpos);
 		return winpos;
 	}
 	
 	public void setUp2D(){ //args for the orthographic matrix?
 		//glUseProgram(0);
 		glMatrixMode(GL_PROJECTION);
 		glPushMatrix();
 		//glLoadIdentity();
 		glLoadMatrix(orthographicProjectionMatrix);
 		glMatrixMode(GL_MODELVIEW);
 		glPushMatrix();
 		glLoadIdentity();
 		//draw
 	}
 	public void backTo3D(){ //args for the projection matrix?
 		//assumes in modelview matrix mode
		glColor4f(1,1,1,1);
 		glPopMatrix();
 		glMatrixMode(GL_PROJECTION);
 		glPopMatrix();
 		//glLoadMatrix(perspectiveProjectionMatrix);
 		glMatrixMode(GL_MODELVIEW);
 		
 	}
 
 	@Override
 	public void update(int delta, List<Entity> entities) {
 		//do nothing? may need to do stuff in subclasses
 	}
 }
