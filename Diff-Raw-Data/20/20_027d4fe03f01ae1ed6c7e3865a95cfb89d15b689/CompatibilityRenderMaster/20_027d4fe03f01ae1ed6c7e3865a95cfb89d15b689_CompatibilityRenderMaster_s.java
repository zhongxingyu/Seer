 
 package graphics.compatibility;
 
 
 import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
 import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
 import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
 import static org.lwjgl.opengl.GL11.glClear;
 import static org.lwjgl.opengl.GL11.glEnable;
 import graphics.Camera;
 import graphics.Light;
 import graphics.Model;
 import graphics.RenderMaster;
 import graphics.Shader;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.Vector3f;
 
 public class CompatibilityRenderMaster implements RenderMaster{
 	
 	CompatibilityModelFactory modelFactory;
 	
 	List<CompatibilityModel> models;
 	Camera camera;
 	Shader shader;
 	
 	static float rotation = 0.0f;
 	static float height = 0.0f;
 	
 	public CompatibilityRenderMaster()
 	{
         glEnable(GL_DEPTH_TEST);
 		
 		String fragmentShader = "temp/fragShader.txt";
 		String vertexShader = "temp/vertShader.txt";
 		
 		shader = new CompatibilityShader(fragmentShader, vertexShader);
 		
 		
 		modelFactory = new CompatibilityModelFactory(shader);
 		
         models = new ArrayList<CompatibilityModel>();
         
         for(int i = 0; i < 80; i++)
         {
         	this.addModel("filename goes here");
         }
 		
 		
 		camera = new Camera(shader);
 		
 	}
 
 	
 	public void render() {
 		
 		if(Keyboard.isKeyDown(Keyboard.KEY_LEFT))
 		{
			rotation += 0.02f;	
 		}else if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
 		{
			rotation -= 0.02f;
 		}
 		
 		if(Keyboard.isKeyDown(Keyboard.KEY_UP))
 		{
 			height += 0.05f;	
 		}else if(Keyboard.isKeyDown(Keyboard.KEY_DOWN))
 		{
 			height -= 0.05f;
 		}
 		
 		
		float lent = 50.0f;
 		camera.setPosition(new Vector3f(lent * (float)Math.sin(rotation), height, lent * (float)Math.cos(rotation)));
 		
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		camera.setActive();
 		for(CompatibilityModel mesh : models)
 		{
 			mesh.draw();
 		}
         
         Display.update();
     }
 
 	@Override
 	public Light addLight() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Light addMasterLight() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void loadMeshes(String[] filenames) {
 		for(String file : filenames){
 			modelFactory.loadModel(file);
 		}
 	}
 
 	public void unloadMeshes(String[] filenames) {
 		for(String file : filenames){
 			modelFactory.unloadModel(file);
 		}
 	}
 
 	public Model addModel(String filename) {
 		CompatibilityModel m = modelFactory.getModel(filename);
 		models.add(m);
 		
 		return (Model)m;
 	}
 
 }
