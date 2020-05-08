 package cs4620.material;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.media.opengl.GL2;
 import javax.vecmath.Vector3f;
 
 import cs4620.framework.GlslException;
 import cs4620.framework.Program;
 import cs4620.scene.ProgramInfo;
 import cs4620.scene.SceneProgram;
 import cs4620.shape.Mesh;
 
 /*
  * A material that draws an object with toon shading by quantizing
  * the angle used in the diffuse reflectance calculation. It also
  * draws a black outline around the object.
  */
 public class ToonMaterial extends PhongMaterial {
 	private static SceneProgram displaceProgram = null;
 	private static SceneProgram quantizeProgram = null;
 	
 	public float displaceScale = 0.05f;
 
 	public ToonMaterial()
 	{
 		super();
 		setAmbient(0.0f, 0.0f, 0.0f);
 		setSpecular(0.0f, 0.0f, 0.0f);
 		setShininess(0.0f);
 	}
 	
 	@Override
 	public void draw(GL2 gl, ProgramInfo info, Mesh mesh, boolean wireframe) {
 		if(quantizeProgram == null)
 		{
 			try
 			{
 				quantizeProgram = new SceneProgram(gl, "toonQuantize.vs", "toonQuantize.fs");
 			}
 			catch(GlslException e)
 			{
 				e.printStackTrace();
 				System.exit(1);
 			}
 		}
 		
 		if(displaceProgram == null)
 		{
 			try
 			{
 				displaceProgram = new SceneProgram(gl, "toonDisplace.vs", "toonDisplace.fs");
 			}
 			catch(GlslException e)
 			{
 				e.printStackTrace();
 				System.exit(1);
 			}
 		}
 		
 		// Toon uses two passes. Use the displace shader first
 		// Store the previously used program
 		Program p = Program.swap(gl,  displaceProgram);
 		
 		displaceProgram.setAllInfo(gl, info);
 		
 		displaceProgram.setAmbientColor(gl, new Vector3f( ambient[0],  ambient[1],  ambient[2]));
 		displaceProgram.setDiffuseColor(gl, new Vector3f( diffuse[0],  diffuse[1],  diffuse[2]));
 		displaceProgram.setSpecularColor(gl, new Vector3f(specular[0], specular[1], specular[2]));
 		displaceProgram.setShininess(gl, shininess);
 		
 		if(displaceProgram.getUniform("displaceScale") != null)
 			displaceProgram.getUniform("displaceScale").set1Float(gl, displaceScale);
 		else
 			System.out.println("Toon displace shader is ignoring uniform \"displaceScale\"");
 		
 		// TODO: (Shaders 1 Problem 1) Add code here to draw using the toon displacement shader
 		gl.glCullFace(GL2.GL_FRONT);
 		
 		if(wireframe) {
 			mesh.drawWireframe(gl);
 		} else {
 			mesh.draw(gl);
 		}
 		
 		// Now use the quantize program
 		Program.use(gl, quantizeProgram);
 		
 		quantizeProgram.setAllInfo(gl, info);
 		
 		quantizeProgram.setAmbientColor(gl, new Vector3f( ambient[0],  ambient[1],  ambient[2]));
 		quantizeProgram.setDiffuseColor(gl, new Vector3f( diffuse[0],  diffuse[1],  diffuse[2]));
 		quantizeProgram.setSpecularColor(gl, new Vector3f(specular[0], specular[1], specular[2]));
 		quantizeProgram.setShininess(gl, shininess);
 		
 		if(wireframe) {
 			mesh.drawWireframe(gl);
 		} else {
 			mesh.draw(gl);
 		}
 		
 		// Use the previous program
 		Program.use(gl, p);
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
 
 	@Override
 	public Object getYamlObjectRepresentation() {
 		Map<Object, Object> result = new HashMap<Object,Object>();
 		result.put("type", "ToonMaterial");
 
 		result.put("ambient", convertFloatArrayToList(ambient));
 		result.put("diffuse", convertFloatArrayToList(diffuse));
 		result.put("specular", convertFloatArrayToList(specular));
 		result.put("shininess", shininess);
 		result.put("displaceScale", displaceScale);
 
 		return result;
 	}
 
 }
