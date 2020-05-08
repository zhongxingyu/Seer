 package cs4620.material;
 
 import javax.media.opengl.GL2;
 import javax.vecmath.Matrix4f;
 import javax.vecmath.Vector3f;
 
 import cs4620.framework.GlslException;
 import cs4620.framework.Program;
 import cs4620.scene.ProgramInfo;
 import cs4620.scene.SceneProgram;
 import cs4620.shape.Mesh;
 
 
 public class FlowerMaterial extends PhongMaterial {
 	
 	private SceneProgram flowerProgram;
 	
 	// When light is close enough to directly above flower, the math
 	// for bending the flower becomes unstable -- use the Blinn-Phong
 	// shader material in this case instead
 	private PhongMaterial unbentMaterial;
 	private boolean useFlowerShader = true;
 	
 	// the height of the flower in object space
 	private float flowerHeight = 3.0f;
 	
 	// matrices passed to the vertex shader as uniforms
 	private Matrix4f frameToObj;
 	private Matrix4f objToFrame;
 	
 	// TODO: (Shaders 1 Problem 2) Declare any other variables you want to pass as
 	// uniforms to your vertex shader here
 	private float un_maxPhi;
 	private float un_maxRadius;
 	
 	public FlowerMaterial()
 	{
 		super();
 		unbentMaterial = new PhongMaterial();
 	}
 	
 	/**
 	 * Update the values that will be assigned to the flower shader's
 	 * uniforms based on the new position of the light source (expressed
 	 * in the coordinate frame of the flower mesh vertices)
 	 * @param lightx the light x coordinate in the object frame
 	 * @param lighty the light y coordinate in the object frame
 	 * @param lightz the light z coordinate in the object frame
 	 */
 	
 	public void setUniforms(float lightx, float lighty, float lightz)
 	{
 		// the height of the flower mesh in object-space
 		float height = flowerHeight;
 		
 		// find the values L_x and L_y from the figure in pa2a.pdf
 		float L_x = (float) Math.sqrt(lightx * lightx + lightz * lightz);
 		float L_y = lighty;
 		
 		if(L_x < 0.00001)
 		{
 			// if the light is too close to lying directly above the flower, the math
 			// for the bending of the flower becomes unstable, so just render
 			// the unbent flower
 			useFlowerShader = false;
 		}
 		else
 		{
 			useFlowerShader = true;
 			
 			// These matrices map between the frame of the flower mesh's vertices and a frame in which
 			// the light lies on the z>0 part of the x-y plane
 			frameToObj = new Matrix4f(lightx / L_x, 0, -lightz / L_x, 0,
 					                  0,            1,  0,            0,
 					                  lightz / L_x, 0,  lightx / L_x, 0,
 					                  0,            0,  0,            1);
 			
 			// find inverse of frameToObj
 			objToFrame = new Matrix4f();
 			objToFrame.transpose(frameToObj);
 			
 			// the angle theta from the diagram in pa2a.pdf
 			float theta = (float) Math.atan(L_y / L_x);
 			
 			// TODO: (Shaders 1 Problem 2) use L_x and L_y to calculate any other uniforms you
 			// want to send to the vertex shader
 			un_maxPhi = (float)Math.PI - (float)(Math.PI/2.0) - theta;
 			un_maxRadius = height/un_maxPhi;
 					
 		}
 	}
 
 	@Override
 	public void draw(GL2 gl, ProgramInfo info, Mesh mesh, boolean wireframe) {
 		
 		if(flowerProgram == null)
 		{
 			try
 			{
 				flowerProgram = new SceneProgram(gl, "flower.vs", "flower.fs");
 			}
 			catch(GlslException e)
 			{
 				System.err.println(e.getMessage());
 				System.exit(1);
 			}
 		}
 		
 		if(useFlowerShader)
 		{
 			Program p = Program.swap(gl, flowerProgram);
 			
 			flowerProgram.setAllInfo(gl,  info);
 			
 			flowerProgram.setAmbientColor(gl, new Vector3f( ambient[0],  ambient[1],  ambient[2]));
 			flowerProgram.setDiffuseColor(gl, new Vector3f( diffuse[0],  diffuse[1],  diffuse[2]));
 			flowerProgram.setSpecularColor(gl, new Vector3f(specular[0], specular[1], specular[2]));
 			flowerProgram.setShininess(gl, shininess);
 			
 			// set frame transformation matrices
 			if(flowerProgram.getUniform("un_FrameToObj") != null)
 				flowerProgram.getUniform("un_FrameToObj").setMatrix4(gl, frameToObj);
 			
 			if(flowerProgram.getUniform("un_ObjToFrame") != null)
 				flowerProgram.getUniform("un_ObjToFrame").setMatrix4(gl, objToFrame);
 			
 			// TODO: (Shaders 1 Problem 2) Send any custom uniforms here
			if(flowerProgram.getUniform("un_maxPhi") != null)
				flowerProgram.getUniform("un_maxPhi").set1Float(gl, un_maxPhi);
 			
			if(flowerProgram.getUniform("un_maxRadius") != null)
				flowerProgram.getUniform("un_maxRadius").set1Float(gl, un_maxRadius);
			
			if(flowerProgram.getUniform("flowerHeight") != null)
				flowerProgram.getUniform("flowerHeight").set1Float(gl, flowerHeight);
				
 			if(wireframe) {
 				mesh.drawWireframe(gl);
 			} else {
 				mesh.draw(gl);
 			}
 			
 			// Use the previous program
 			Program.use(gl, p);
 		}
 		else
 		{
 			// render the unbent flower using per-pixel Blinn-Phong
 			unbentMaterial.draw(gl, info, mesh, wireframe);
 		}
 	}
 	
 	@Override
 	public void drawUsingProgram(GL2 gl, SceneProgram program, Mesh mesh, boolean wireframe) {
 		program.setAmbientColor(gl, new Vector3f( ambient[0],  ambient[1],  ambient[2]));
 		program.setDiffuseColor(gl, new Vector3f( diffuse[0],  diffuse[1],  diffuse[2]));
 		program.setSpecularColor(gl, new Vector3f(specular[0], specular[1], specular[2]));
 		program.setShininess(gl, shininess);
 		
 		if(wireframe) {
 			mesh.drawWireframe(gl);
 		} else {
 			mesh.draw(gl);
 		}
 	}
 	
 	public void setAmbient(float r, float g, float b)
 	{
 		super.setAmbient(r, g, b);
 		unbentMaterial.setAmbient(r, g, b);
 	}
 
 	public void setDiffuse(float r, float g, float b)
 	{
 		super.setDiffuse(r, g, b);
 		unbentMaterial.setDiffuse(r, g, b);
 	}
 
 	public void setSpecular(float r, float g, float b)
 	{
 		super.setSpecular(r, g, b);
 		unbentMaterial.setSpecular(r, g, b);
 	}
 
 	public void setShininess(float shininess)
 	{
 		super.setShininess(shininess);
 		unbentMaterial.setShininess(shininess);
 	}
 
 	@Override
 	public Object getYamlObjectRepresentation() {
 		// irrelevant, since flower is not used with editable scene graph
 		return null;
 	}
 
 }
