 package org.prova;
 
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.extra.Enviroment;
 import org.anddev.andengine.extra.ExtraScene;
 import org.anddev.andengine.extra.ExtraSound;
 import org.anddev.andengine.extra.ExtraVibration;
 import org.anddev.andengine.extra.Resource;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 
 public class Scene2 extends ExtraScene {
 
 	private ExtraSound s;
 
 	@Override
 	public void createScene() {
 		Rectangle rec = new Rectangle(0, 0, Enviroment.getInstance().getScreenWidth(), Enviroment.getInstance().getScreenHeight());
 		rec.setColor(0.6f, 0.6f, 1f);
 		getChild(ExtraScene.BACKGROUND_LAYER).attachChild(rec);
 		
 		TextureRegion box = Resource.getTexture(128, 128, "box");
 		Sprite sprite = new Sprite(100, 100, box);
 		getChild(ExtraScene.GAME_LAYER).attachChild(sprite);
 		
 		this.s = Resource.getSound("done");
 	}
 
 	@Override
 	public void endScene() {
 		
 	}
 
 	@Override
 	public void manageAreaTouch(ITouchArea pTouchArea) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void manageSceneTouch(TouchEvent pSceneTouchEvent) {
 		ExtraVibration.duration(200);
 		this.s.play();
 	}
 
 	@Override
 	public void startScene() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
