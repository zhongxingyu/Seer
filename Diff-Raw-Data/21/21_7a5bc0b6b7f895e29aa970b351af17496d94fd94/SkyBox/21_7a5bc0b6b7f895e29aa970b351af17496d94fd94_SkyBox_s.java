 package scenegraph;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GLAutoDrawable;
 
 public class SkyBox extends SceneGraphNode {
 
 	public SkyBox(GLAutoDrawable drawable, String modelPath, float scale) {
 		super(drawable, modelPath, scale);
		// TODO Auto-generated constructor stub
		this.setFragShaderEnabled(true);
		this.setVertexShaderEnabled(true);
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
//		drawable.getGL().glDisable(GL.GL_LIGHTING); // disable lights, because otherwise the sky would be shaded by the light sources
 		drawable.getGL().glCallList(this.getObjectList());
//		drawable.getGL().glEnable(GL.GL_LIGHTING);
 //		drawable.getGL().glDisable(GL.GL_TEXTURE_2D);
 	}
 	
 	@Override
 	public void postDraw(GLAutoDrawable drawable) {
 		drawable.getGL().glCallList(this.getObjectList());
 	}
 }
