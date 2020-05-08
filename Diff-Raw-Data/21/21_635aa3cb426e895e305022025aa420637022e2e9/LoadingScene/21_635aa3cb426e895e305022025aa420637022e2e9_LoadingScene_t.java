 package topplintowers.scenes;
 
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.opengl.texture.region.TextureRegion;
 
 import topplintowers.resources.ResourceManager;
 import topplintowers.scenes.SceneManager.SceneType;
 
 public class LoadingScene extends BaseScene {
 
 	private Text mText;
 	private TextureRegion mTexture;
 	private Sprite mSprite;
 	
 	
 	@Override
 	public void createScene() {
 		Sprite newSprite = new Sprite(0, 0, ResourceManager.mBackgroundTextureRegion, vbom);
 		attachChild(newSprite);
 		
 		Rectangle rect = new Rectangle(0, 0, 800, 480, vbom);
 		rect.setColor(0,0,0,0.75f);
 		attachChild(rect);
 		
 		mText = new Text(0, 0, resourceManager.mFont48, "Loading...", vbom);
 		float textX = 800 - mText.getWidth() - 10;
 		float textY = 480 - mText.getHeight() - 10;
 		mText.setPosition(textX, textY);
 		
 		attachChild(mText);
 	}
 	
 	public void setCrateTexture() {
 		Integer random = (int)(Math.random() * 6);
 		mTexture = resourceManager.getInstance().mLoadingCrateTextureRegions.get(random);
 		
		mSprite = new Sprite(575, 396, mTexture, vbom);

 		attachChild(mSprite);
 	}
 	
 	@Override
 	public void onBackKeyPressed() { return; }
 	
 	@Override
 	public void onMenuKeyPressed() { return; }
 
 	@Override
 	public SceneType getSceneType() { return SceneType.LOADING; }
 
 	@Override
 	public void disposeScene() {
		mSprite.detachSelf();
		mSprite.dispose();
 	}
 }
