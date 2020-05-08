 package topplintowers.scenes.gamescene.hud;
 
 import org.andengine.entity.modifier.MoveModifier;
 import org.andengine.entity.modifier.ScaleModifier;
 import org.andengine.entity.scene.IOnAreaTouchListener;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.ITouchArea;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.input.touch.detector.ClickDetector;
 import org.andengine.input.touch.detector.ClickDetector.IClickDetectorListener;
 import org.andengine.opengl.texture.region.TextureRegion;
 
 import android.util.Log;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
 import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
 
 import topplintowers.MainActivity;
 import topplintowers.crates.*;
 import topplintowers.resources.ResourceManager;
 import topplintowers.scenes.gamescene.GameScene;
 
 public class CrateThumbnail implements IOnSceneTouchListener, IClickDetectorListener, IOnAreaTouchListener {
 	private static MainActivity instance = MainActivity.getSharedInstance();
 	
 	private boolean hidden = false;
 	private Sprite sprite;
 	private String side;
 	private CrateContainer parent;
 	private CrateType type;
 	private Text countText;
 	
 	final ScaleModifier zoomOut;
 	final ScaleModifier zoomIn;
 	MoveModifier move;
 	
 	private Body groundBody; // used for Mouse Joint
 	
 	private ClickDetector mClickDetector;
 	
 	public CrateThumbnail(MyHUD HUD, CrateContainer parent, CrateType type) {
 		this.mClickDetector = new ClickDetector(this);	
 		this.side = parent.getSide();
 		this.parent = parent;
 		this.type = type;
 		
 	    zoomOut = new ScaleModifier(0.3f, 1f, 0f);
 	    zoomOut.setAutoUnregisterWhenFinished(true);
 	    zoomIn = new ScaleModifier(0.3f, 0f, 1f);
 	    zoomIn.setAutoUnregisterWhenFinished(true);
 	    	
 		TextureRegion textureRegion = CrateType.getTextureRegion(type);
 		sprite = new Sprite(0, 0, textureRegion, instance.getVertexBufferObjectManager());
 		sprite.setSize(45, 45);
 		
 		sprite.setScale(1f);
 		
 		parent.thumbs.add(this);
 		setThumbPosition(parent, sprite);
 		
 		Integer count = MyHUD.mAvailableCrateCounts.get(type);
 		if (count < 100)
 			createCounter(sprite, side, count.toString());
 	
 		HUD.registerTouchArea(sprite);
 		HUD.setOnSceneTouchListenerBindingOnActionDownEnabled(true);
 		HUD.attachChild(sprite);
 	}
 	
 	public String getSide() { return side; }
 	public Sprite getSprite() { return sprite; }
 	public CrateType getType() { return type; }
 	public Text getCounterText() { return countText; }
 	public CrateContainer getParent() { return parent; }
 	public void setHidden(boolean b) { hidden = b; }
 	public boolean isHidden() {
 		if (hidden == true) return true;
 		return false;
 	}
 	
 	private void setThumbPosition(CrateContainer parent, Sprite thumb) {
 		Sprite parentSprite = parent.getSprite();
 		float thumbPosX = parentSprite.getX() + 7.5f;
 		if (parent.getSide() == "right")
 			thumbPosX = instance.mCamera.getWidth() - thumb.getWidth() - 7.5f;
 		float thumbPosY = 0;
 		float offsetY = 10;
 		Sprite lastThumb = null;
 		
 		if (parent.thumbs.size() > 0) {
 			if (parent.thumbs.size() != 1) {
 				lastThumb = parent.thumbs.get(parent.thumbs.size() - 2).sprite;
 				thumbPosY = lastThumb.getY() + lastThumb.getHeight() + offsetY;
 			}
 		}
     	thumb.setPosition(thumbPosX, thumbPosY);
 	}
 	
 	private void createCounter(Sprite parent, String side, String crateCount) {
 		float posX = 0;
 		float crateEdge = 0, spaceToFill = 0, textWidth = 0;
 		
 		countText = new Text(0, 0, ResourceManager.mFont32, crateCount, instance.getVertexBufferObjectManager());
 		if (side == "left") {
 			crateEdge = parent.getX() + parent.getWidthScaled();
 			spaceToFill = this.parent.getSprite().getWidthScaled() - crateEdge;
 			textWidth = countText.getWidthScaled();
 			posX = crateEdge + (spaceToFill - textWidth)/2 - 5; //5 is just an offset
 		} else {
 			crateEdge = parent.getX();
 			spaceToFill = crateEdge - this.parent.getSprite().getX();
 			textWidth = countText.getWidth();
 			posX = -(spaceToFill/2) - (textWidth/2);
 		}
 		float textPosY = parent.getHeight()/2 - countText.getHeight()/2;
 		countText.setPosition(posX, textPosY);
 		parent.attachChild(countText);
 	}
 
 	@Override
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 		this.mClickDetector.onTouchEvent(pSceneTouchEvent);
 		return true;
 	}
 	
 	@Override
 	public void onClick(ClickDetector pClickDetector, int pPointerID, float pSceneX, float pSceneY) {
 		return;
 	}
 	
 	public void shrinkThumbnail() {
 		sprite.registerEntityModifier(zoomOut.deepCopy());
 		hidden = true;	
 		countText.setVisible(false);
 	}
 	
 	public void expandThumbnail() {
 		// TODO: there is a bug where if two hit at exactly the same time they aren't positioned correctly, 
 		//may need to look into how to make sure this method is only executed once at a time
 		//low priority
 		sprite.registerEntityModifier(zoomIn.deepCopy());	
 		hidden = false;
 		countText.setVisible(true);
 	}
 
 	
 	@Override
 	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, ITouchArea pTouchArea, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 		float curX = pSceneTouchEvent.getX() - 5;
		float curY = pSceneTouchEvent.getY() - 5;
		float offset = instance.mCamera.getCenterY() - 240;
		curY += offset;
       
 		if (pSceneTouchEvent.isActionDown()) {
 			
 			int crateCount = MyHUD.mAvailableCrateCounts.get(type);
 			if (crateCount > 0) {
 				
 				Crate newCrate = createCrate(type);
 				newCrate.setPosition(curX - newCrate.getSize()/2, curY - newCrate.getSize()/2);
 				
 				if (newCrate != null) {
 					int newCrateCount = crateCount - 1;
 					if (newCrateCount == 0) {
 						shrinkThumbnail();
 						parent.resizeContainer(parent.getSprite().getHeight());
 						parent.repositionCrates();
 					}
 					MyHUD.mAvailableCrateCounts.put(type, newCrateCount);
 					
 					GameScene gs = GameScene.getScene();
 					
 					float jointX = pSceneTouchEvent.getX() / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 					float jointY = pSceneTouchEvent.getY() / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
					jointY += offset / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
					
 					Vector2 vector = new Vector2(jointX, jointY);
 					
 					BodyDef groundBodyDef = new BodyDef();
 	                groundBodyDef.position.set(vector);
 	                GameScene.getScene().setGroundBody(GameScene.mPhysicsWorld.createBody(groundBodyDef));
 					
 					Log.e("", "Creating mouse joint at " + vector.toString() + "...");
 					gs.setMJActive(gs.createMouseJoint(newCrate.getBox(), jointX, jointY));
 						
 					return true;
 				}
 				//GameScene.getScene().onSceneTouchEvent(GameScene.getScene(), pSceneTouchEvent);
 			}
 			
 			//newCrate.getBox().setType(BodyType.KinematicBody);
 //			dX = curX;
 //			dY = curY;
 //			newCrate.getBox().setTransform(curX/32,  curY/32, 0);
 		
 //		} else if (pSceneTouchEvent.isActionMove()) {
 //			dX = curX - lastX;
 //			dY = curY - lastY;
 //			newCrate.getBox().setTransform(curX/32, curY/32, 0);
 //			lastX = curX;
 //			lastX = curY;
 //			
 //		} else if (pSceneTouchEvent.isActionUp()) { 
 //			newCrate.getBox().setType(BodyType.DynamicBody);
 //			newCrate.getBox().setLinearVelocity(dX, dY);
 		}
 			
 		return false;
 	}
 	
 	private Crate createCrate(CrateType type) {
 		Crate crate = null;
 		
 		int crateCount = MyHUD.mAvailableCrateCounts.get(type);
 		if (crateCount > 0) {
 			switch (type) {
 				case WOOD:			crate = new WoodCrate();			break;
 				case STONE:			crate = new StoneCrate();			break;
 				case METAL:			crate = new MetalCrate();			break;
 				case MAGNET:		crate = new MagnetCrate();			break;
 				case ELECTROMAGNET:	crate = new ElectromagnetCrate(); 	break;
 				case STICKY:		crate = new StickyCrate();			break;
 				case TRANSFORMER: 	crate = new TransformerCrate(); 	break;
 				
 				default: crate = new WoodCrate(); break;
 			}
 			
 			int newCrateCount = crateCount - 1;
 			if (newCrateCount == 0) {
 				shrinkThumbnail();
 				parent.resizeContainer(parent.getSprite().getHeight());
 				parent.repositionCrates();
 			}
 			MyHUD.mAvailableCrateCounts.put(type, newCrateCount);
 		}
 			
 		return crate;
 	}
 }
