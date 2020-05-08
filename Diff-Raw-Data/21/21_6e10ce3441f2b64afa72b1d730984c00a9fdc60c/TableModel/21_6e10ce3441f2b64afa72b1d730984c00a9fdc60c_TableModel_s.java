 package scenegraph;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLException;
 
 import com.sun.opengl.cg.CgGL;
 import com.sun.opengl.util.texture.Texture;
 import com.sun.opengl.util.texture.TextureIO;
 
 public class TableModel extends SceneGraphNode {
 	
 	private GlassModel glass = null;
 	private EiffelModel eiffel = null;
 	
 	private Texture tex = null;
 	
 	
 	public TableModel(GLAutoDrawable drawable, float scale) {
 		super(drawable, "models/table", scale);	
 		
 		glass = new GlassModel(drawable, scale);
 //		glass.setTranslation(0.8f, 0.832f, 0);
 		this.addChild(glass);
 		
 		eiffel = new EiffelModel(drawable, scale);
 		this.addChild(eiffel);
 		this.init(drawable);
 		this.setVertexShaderEnabled(false);
 		this.setFragShaderEnabled(false);
 	}
 
 	@Override
	public void init(GLAutoDrawable drawable) {
		try {
			tex = TextureIO.newTexture(new File("models/baked-baked_blinn1SG_pCube1SG-pCube1.jpg"),false);
		} catch (GLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}

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
		GL gl = drawable.getGL();
		gl.glEnable(GL.GL_TEXTURE_2D);
		tex.bind();
 		drawable.getGL().glCallList(this.getObjectList());
		tex.disable();
		gl.glDisable(GL.GL_TEXTURE_2D);
		
 	}
 }
