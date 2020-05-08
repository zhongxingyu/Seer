 package scenegraph;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GLAutoDrawable;
 
import com.sun.opengl.cg.CgGL;

 public class SkyBox extends SceneGraphNode {
 
 	public SkyBox(GLAutoDrawable drawable, String modelPath, float scale) {
 		super(drawable, modelPath, scale);
		this.setFragShaderEnabled(false);
		this.setVertexShaderEnabled(false);
 	}
 
 	@Override
 	public void init(GLAutoDrawable drawable) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void bindParameters() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void animate(GLAutoDrawable drawable) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void draw(GLAutoDrawable drawable) {
 //		drawable.getGL().glEnable(GL.GL_TEXTURE_2D); // enable texturing
 		drawable.getGL().glCallList(this.getObjectList());
 //		drawable.getGL().glDisable(GL.GL_TEXTURE_2D);
 	}
 	
 	@Override
 	public void postDraw(GLAutoDrawable drawable) {
 		drawable.getGL().glCallList(this.getObjectList());
 	}
 }
