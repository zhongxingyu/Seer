 package ua.nevis.rainyday.scenes;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.util.GLState;
 
 public class SplashScene extends BaseScene {
 	private Sprite splash;
 
 	@Override
 	public void createScene() {
 		splash = new Sprite(0, 0, resourceManager.splashRegion, vboManager) {
 			@Override
 			protected void preDraw(GLState pGLState, Camera pCamera) {
 				super.preDraw(pGLState, pCamera);
 				pGLState.enableDither();
 			}
 		};
		splash.setPosition(800 - splash.getWidth(), 480 - splash.getHeight());
 		attachChild(splash);
 	}
 
 	@Override
 	public void disposeScene() {
 		splash.detachSelf();
 		splash.dispose();
 		this.detachSelf();
 		this.dispose();
 	}
 
 	@Override
 	public SceneType getSceneType() {
 		return SceneType.SCENE_SPLASH;
 	}
 
 	@Override
 	public void onBackKeyPressed() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onMenuKeyPressed() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
