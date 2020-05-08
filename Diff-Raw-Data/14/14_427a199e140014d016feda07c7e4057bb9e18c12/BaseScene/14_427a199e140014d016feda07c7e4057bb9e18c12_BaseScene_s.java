 package ua.nevis.rainyday.scenes;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.scene.Scene;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import ua.nevis.rainyday.managers.ResourceManager;
 
 import android.app.Activity;
 
 public abstract class BaseScene extends Scene {
 	protected ResourceManager resourceManager;
 	protected Engine engine;
 	protected Activity activity;
 	protected Camera camera;
 	protected VertexBufferObjectManager vboManager;
 
 	public BaseScene() {
 		this.resourceManager = ResourceManager.getInstance();
 		this.engine = resourceManager.engine;
 		this.activity = resourceManager.activity;
 		this.camera = resourceManager.camera;
 		this.vboManager = resourceManager.vboManager;
 	}
 
 	public abstract void createScene();
 
 	public abstract void disposeScene();
 
 	public abstract SceneType getSceneType();
 
 	public abstract void onBackKeyPressed();
 
 	public abstract void onMenuKeyPressed();
 
 }
