 package topplintowers.crates;
 
 import java.util.ArrayList;
 
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.shape.IShape;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import android.util.Log;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Shape.Type;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.Joint;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.Shape;
 import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
 
 import topplintowers.MainActivity;
 import topplintowers.resources.PoolManager;
 import topplintowers.resources.ResourceManager;
 import topplintowers.scenes.gamescene.GameScene;
 
 public class WoodCrate extends Crate {
 	// FROM DERRICK: Weight: 2, Friction: 5, Elasticity: 1, breaks under weight 15
 	// TODO: customize this crate's density, elasticity, and friction
 	private static final FixtureDef FIXTURE_DEF_WOOD = PhysicsFactory.createFixtureDef(1, 0.05f, 0.3f);  
 	
 	private PolygonShape topPlank, bottomPlank, leftPlank, rightPlank, diagonalPlank;
 	Vector2[] diagonalVertices;
 	private Fixture topFixture, bottomFixture, leftFixture, rightFixture, diagonalFixture;
 	ArrayList<Fixture> fixtures = new ArrayList<Fixture>();
 	Vector2 velocity;
 	float angularVelocity;
 	
 	float totalSize 		 	= getSize() / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 	float topAndBottomHeight 	= 12f / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 	float topAndBottomWidth  	= 65f / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 	float leftAndRightHeight 	= 41f / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 	float leftAndRightWidth  	= 10f / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 	float insideHeight 			= 8f  / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 	float insideWidth 			= 45f / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 	
 	private boolean toBeBroken;
 	public boolean toBeBroken() { return toBeBroken; }
 	public void setToBeBroken(boolean toBeBroken) { this.toBeBroken = toBeBroken; }
 	
 	private boolean broken;
 	public boolean isBroken() { return broken; }
 	
 
 	public WoodCrate() {
 		super(CrateType.WOOD, PoolManager.getInstance().mWoodPool);
 		
 		toBeBroken = false;
 		broken = false;
 		
 		sprite = PoolManager.getInstance().mWoodPool.obtainPoolItem();
 		createCrate();
 		
 		GameScene.getScene().getContainer().attachChild(sprite);
 		GameScene.activeCrates.get(type).add(this);
 	}
 	
 	private void createCrate() { 
 		BodyDef bd = new BodyDef();
 		bd.type = BodyType.DynamicBody;
 		bd.position.set(20, 0);
 		box = GameScene.mPhysicsWorld.createBody(bd);
		box.setUserData("wood");
 		
 		createTopAndBottomFixtures(box);
 		createLeftAndRightFixtures(box);
 		//createInsideFixtures(box);
 		createDiagonalFixture(box);
 		
 		GameScene.getScene().mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(sprite, box));
 	}
 	
 	private void createTopAndBottomFixtures(Body crateBody) {
 		float topCenterX = 0;
 		float topCenterY = -totalSize/2 + topAndBottomHeight/2;
 		Vector2 topCenter = new Vector2(topCenterX, topCenterY);
 		
 		topPlank = new PolygonShape();
 		topPlank.setAsBox(topAndBottomWidth/2, topAndBottomHeight/2, topCenter, 0);	
 		topFixture = crateBody.createFixture(topPlank, 1);
 		
 		fixtures.add(topFixture);
 		
 		float bottomCenterX = 0;
 		float bottomCenterY = totalSize/2 - topAndBottomHeight/2;
 		Vector2 bottomCenter = new Vector2(bottomCenterX, bottomCenterY);
 		
 		bottomPlank = new PolygonShape();
 		bottomPlank.setAsBox(topAndBottomWidth/2, topAndBottomHeight/2, bottomCenter, 0);
 		bottomFixture = crateBody.createFixture(bottomPlank, 1); 
 		fixtures.add(bottomFixture);
 	}
 	
 	private void createLeftAndRightFixtures(Body crateBody) {		
 		float leftCenterX = -totalSize/2 + leftAndRightWidth/2;
 		float leftCenterY = 0;
 		Vector2 leftCenter = new Vector2(leftCenterX, leftCenterY);
 		
 		leftPlank = new PolygonShape();
 		leftPlank.setAsBox(leftAndRightWidth/2, leftAndRightHeight/2, leftCenter, 0);
 		leftFixture = crateBody.createFixture(leftPlank, 1);
 		fixtures.add(leftFixture);
 		
 		float rightCenterX = totalSize/2 - leftAndRightWidth/2;
 		float rightCenterY = 0;
 		Vector2 rightCenter = new Vector2(rightCenterX, rightCenterY);
 		
 		rightPlank = new PolygonShape();
 		rightPlank.setAsBox(leftAndRightWidth/2, leftAndRightHeight/2, rightCenter, 0);
 		rightFixture = crateBody.createFixture(rightPlank, 1);
 		fixtures.add(rightFixture);
 	}
 	
 	private void createInsideFixtures(Body crateBody) {
 		float centerX1 = 0;
 		float centerY1 = -totalSize/2 + topAndBottomHeight + insideHeight/2;
 		Vector2 center1 = new Vector2(centerX1, centerY1);
 		
 		PolygonShape plank1 = new PolygonShape();
 		plank1.setAsBox(insideWidth/2, insideHeight/2, center1, 0);		
 		fixtures.add(crateBody.createFixture(plank1, 1));
 		plank1.dispose();
 		
 		float centerX2 = 0;
 		float centerY2 = centerY1 + insideHeight;
 		Vector2 center2 = new Vector2(centerX2, centerY2);
 		
 		PolygonShape plank2 = new PolygonShape();
 		plank2.setAsBox(insideWidth/2, insideHeight/2, center2, 0);		
 		fixtures.add(crateBody.createFixture(plank2, 1));
 		plank2.dispose();
 		
 		float centerX3 = 0;
 		float centerY3 = centerY2 + insideHeight;
 		Vector2 center3 = new Vector2(centerX3, centerY3);
 		
 		PolygonShape plank3 = new PolygonShape();
 		plank3.setAsBox(insideWidth/2, insideHeight/2, center3, 0);		
 		fixtures.add(crateBody.createFixture(plank1, 1));
 		plank3.dispose();
 		
 		float centerX4 = 0;
 		float centerY4 = centerY3 + insideHeight;
 		Vector2 center4 = new Vector2(centerX4, centerY4);
 		
 		PolygonShape plank4 = new PolygonShape();
 		plank4.setAsBox(insideWidth/2, insideHeight/2, center4, 0);		
 		fixtures.add(crateBody.createFixture(plank1, 1));
 		plank4.dispose();
 		
 		float centerX5 = 0;
 		float centerY5 = centerY4 + insideHeight;
 		Vector2 center5 = new Vector2(centerX5, centerY5);
 		
 		PolygonShape plank5 = new PolygonShape();
 		plank5.setAsBox(insideWidth/2, insideHeight/2, center5, 0);		
 		fixtures.add(crateBody.createFixture(plank1, 1));
 		plank5.dispose();
 	}
 	
 	private void createDiagonalFixture(Body crateBody) {
 		float offset = 6f / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 		
 		diagonalVertices = new Vector2[6];
 		diagonalVertices[0] = new Vector2(totalSize/2 - leftAndRightWidth - offset, -totalSize/2 + topAndBottomHeight);
 		diagonalVertices[1] = new Vector2(totalSize/2 - leftAndRightWidth, -totalSize/2 + topAndBottomHeight);
 		diagonalVertices[2] = new Vector2(totalSize/2 - leftAndRightWidth, -totalSize/2 + topAndBottomHeight + offset);
 		diagonalVertices[3] = new Vector2(-totalSize/2 + leftAndRightWidth + offset, totalSize/2 - topAndBottomHeight);
 		diagonalVertices[4] = new Vector2(-totalSize/2 + leftAndRightWidth, totalSize/2 - topAndBottomHeight);
 		diagonalVertices[5] = new Vector2(-totalSize/2 + leftAndRightWidth, totalSize/2 - topAndBottomHeight - offset);
 		
 		diagonalPlank = new PolygonShape();
 		diagonalPlank.set(diagonalVertices);
 		diagonalFixture = crateBody.createFixture(diagonalPlank, 1);
 		fixtures.add(diagonalFixture);
 	}
 	
 	public void breakApart() {
 		toBeBroken = false;
 		broken = true;
 		
         Vector2 center = box.getWorldCenter();
         Vector2 position = box.getPosition();
         float angle = box.getAngle();
         
         VertexBufferObjectManager vbom = GameScene.getScene().getVBOM();
         Sprite topSprite 	  = new Sprite(0, 0, ResourceManager.mWoodCrateTopTextureRegion, vbom);
         Sprite bottomSprite	  = new Sprite(0, 0, ResourceManager.mWoodCrateBottomTextureRegion, vbom);
         Sprite leftSprite 	  = new Sprite(0, 0, ResourceManager.mWoodCrateLeftTextureRegion, vbom);
         Sprite rightSprite 	  = new Sprite(0, 0, ResourceManager.mWoodCrateRightTextureRegion, vbom);
         Sprite diagonalSprite = new Sprite(0, 0, ResourceManager.mWoodCrateDiagonalTextureRegion, vbom);
         
         box.destroyFixture(topFixture);			topFixture = null;
         box.destroyFixture(bottomFixture);		bottomFixture = null;
         box.destroyFixture(leftFixture);		leftFixture = null;
         box.destroyFixture(rightFixture);		leftFixture = null;
         box.destroyFixture(diagonalFixture);	diagonalFixture = null;
         
         dispose();
        
         GameScene.activeCrates.get(type).remove(this);        
 
         
         Vector2 topPosition = new Vector2 (position.x, position.y - totalSize/2);
         createBoxBody(topSprite, topPosition, angle);
         
         Vector2 bottomPosition = new Vector2 (position.x, position.y + totalSize/2);
         createBoxBody(bottomSprite, bottomPosition, angle);
         
         Vector2 leftPosition = new Vector2 (position.x - totalSize/2 + leftAndRightWidth/2, position.y);
         createBoxBody(leftSprite, leftPosition, angle);
         
         Vector2 rightPosition = new Vector2 (position.x + totalSize/2 - leftAndRightWidth/2, position.y);
         createBoxBody(rightSprite, rightPosition, angle);
         
         createDiagonalBody(diagonalSprite, position, angle);
         
         //Vector2 center1 = box.getWorldCenter();
         //Vector2 center2 = body1.getWorldCenter();
         
         //Vector2 velocity1 = velocity.add(new Vector2(-angularVelocity * center1.sub(center).y, angularVelocity * center1.sub(center).x));
         //Vector2 velocity2 = velocity.add(new Vector2(-angularVelocity * center2.sub(center).y, angularVelocity * center2.sub(center).x));
         
         //body1.setAngularVelocity(angularVelocity);
         //body1.setLinearVelocity(velocity1);
         
         //body2.setAngularVelocity(angularVelocity);
         //body2.setLinearVelocity(velocity2);
 	}
 	
 	private void createBoxBody(Sprite sprite, Vector2 position, float angle) {
 		Body body = PhysicsFactory.createBoxBody(GameScene.mPhysicsWorld, sprite, BodyType.DynamicBody, FIXTURE_DEF_WOOD);
         GameScene.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(sprite, body, true, true));
         body.setTransform(position, angle);
         GameScene.getScene().attachChild(sprite);
 	}
 	
 	private void createDiagonalBody(Sprite sprite, Vector2 position, float angle) {
 		Body body = PhysicsFactory.createPolygonBody(GameScene.mPhysicsWorld, sprite, diagonalVertices, BodyType.DynamicBody, FIXTURE_DEF_WOOD);
         GameScene.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(sprite, body, true, true));
         body.setTransform(position, angle);
         GameScene.getScene().attachChild(sprite);
 	}
 	
 }
