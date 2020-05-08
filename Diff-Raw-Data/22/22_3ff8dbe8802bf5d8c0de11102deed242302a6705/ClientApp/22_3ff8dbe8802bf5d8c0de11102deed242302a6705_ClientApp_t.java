 package propra2012.gruppe33.networktest;
 
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.SceneProcessor;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.SceneProcessor.NetworkMode;
 
 public class ClientApp {
 
 	public static void main(String[] args) throws Exception {
 
 		// Create a new scene
 		SceneProcessor sceneProcessor = new SceneProcessor(NetworkMode.Client,
 				"Bomberman", 1024, 768);

		// Stop rendering if hidden...
 		sceneProcessor.onlyRenderWithFocus(false);
 
 		// Connect the scene
 		sceneProcessor.openClient("localhost", 1337).linkClient("Kr0e");
 
 		// Start the processor (In this thread!)
 		sceneProcessor.start(60);
 	}
 }
