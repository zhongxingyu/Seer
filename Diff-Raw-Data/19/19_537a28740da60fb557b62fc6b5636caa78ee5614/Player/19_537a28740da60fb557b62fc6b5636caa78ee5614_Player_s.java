 package no.hist.gruppe5.pvu.mainroom.objects;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.*;
 import no.hist.gruppe5.pvu.Assets;
 import no.hist.gruppe5.pvu.mainroom.MainScreen;
 
 /**
  * Created with IntelliJ IDEA.
  * User: karl
  * Date: 8/30/13
  * Time: 9:34 AM
  *
  */
 
 public class Player {
 
     private final float PLAYER_SIZE;
 
     private final Animation PLAYER_BACK;
     private final Animation PLAYER_LEFT;
     private final Animation PLAYER_RIGHT;
     private final Animation PLAYER_FRONT;
     private final Animation PLAYER_SITTING;
 
     private Animation mCurrentAnimaion;
 
     private Animation[] animations;
 
     private boolean mSitting = false;
 
     private Body mPlayerBody;
     private Sprite mPlayerSprite;
 
     public Player(World world) {
 
         PLAYER_BACK = new Animation(Assets.MAIN_AVATAR_BACK, 3);
         PLAYER_LEFT = new Animation(Assets.MAIN_AVATAR_SIDE_LEFT, 3);
         PLAYER_RIGHT = new Animation(Assets.MAIN_AVATAR_SIDE_RIGHT, 3);
         PLAYER_FRONT = new Animation(Assets.MAIN_AVATAR_FRONT, 3);
         PLAYER_SITTING = new Animation(Assets.MAIN_AVATAR_SITTING, 1);
 
         mCurrentAnimaion = PLAYER_FRONT;
 
         animations = new Animation[]{PLAYER_BACK, PLAYER_LEFT, PLAYER_RIGHT, PLAYER_FRONT, PLAYER_SITTING};
 
         mPlayerSprite = new Sprite(PLAYER_FRONT.getFrame());
         PLAYER_SIZE = mPlayerSprite.getWidth();
 
         //Dynamic Body
         BodyDef bodyDef = new BodyDef();
         bodyDef.type = BodyDef.BodyType.DynamicBody;
         bodyDef.position.set(30f, 30f);
         mPlayerBody = world.createBody(bodyDef);
         PolygonShape boxShape = new PolygonShape();
         boxShape.setAsBox(PLAYER_SIZE / 3, PLAYER_SIZE / 3);
         FixtureDef fixtureDef = new FixtureDef();
         fixtureDef.shape = boxShape;
         fixtureDef.density = 1.0f;
         fixtureDef.friction = 0.0f;
         fixtureDef.restitution = 1;
 
         mPlayerBody.createFixture(fixtureDef);
         mPlayerBody.setFixedRotation(true);
         mPlayerBody.setUserData(MainScreen.OBJECT_PLAYER);
 
         //mPlayerSprite.setSize(PLAYER_SIZE, ( mPlayerSprite.getHeight() / mPlayerSprite.getWidth() ) * PLAYER_SIZE) ;
     }
 
     public void draw(SpriteBatch batch) {
         mPlayerSprite.draw(batch);
     }
 
     public void update() {
         updatePlayerMovement();
 
         if(!mSitting) {
             Vector2 pos = mPlayerBody.getPosition();
             mPlayerSprite.setPosition(pos.x - (mPlayerSprite.getWidth() / 2), pos.y - mPlayerSprite.getHeight() / 4.6f);
         }
 
         float dt = Gdx.graphics.getDeltaTime();
 
         for (Animation a : animations)
             a.animate(dt);
     }
 
     private void updatePlayerMovement() {
         float PLAYER_SPEED = 40f;
         boolean keyPressed = false;
 
         Vector2 newSpeed = new Vector2();
 
         if(Gdx.input.isKeyPressed(Input.Keys.W)) {
             newSpeed.y = PLAYER_SPEED;
             mCurrentAnimaion = PLAYER_BACK;
             keyPressed = true;
         } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
             newSpeed.y = -PLAYER_SPEED;
             mCurrentAnimaion = PLAYER_FRONT;
             keyPressed = true;
         }
 
         if (Gdx.input.isKeyPressed(Input.Keys.D)) {
             newSpeed.x = PLAYER_SPEED;
             mCurrentAnimaion = PLAYER_RIGHT;
             keyPressed = true;
         } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
             newSpeed.x = -PLAYER_SPEED;
             mCurrentAnimaion = PLAYER_LEFT;
             keyPressed = true;
         }
 
        if(keyPressed) {
             mPlayerSprite.setRegion(mCurrentAnimaion.getFrame());
        }
        if(!keyPressed) {
             mPlayerSprite.setRegion(mCurrentAnimaion.frames[0]);
         }
 

         mPlayerBody.setLinearVelocity(newSpeed);
 
         if(newSpeed.x != 0 || newSpeed.y != 0)
             mSitting = false;
 
     }
 
     public float getWidth() {
         return mPlayerSprite.getWidth();
     }
 
     public Vector2 getPosition() {
         return mPlayerBody.getPosition();
     }
 
     public void sitDown() {
         mSitting = true;
         mPlayerBody.setTransform(75f, 24f, 0f);
         mPlayerSprite.setPosition(71f, 23f);
         mPlayerSprite.setRegion(PLAYER_SITTING.getFrame());
     }
 
     public boolean isSitting() {
         return mSitting;
     }
 }
