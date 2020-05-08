 package fi.hbp.angr.models.actors;
 
 import aurelienribon.bodyeditor.BodyEditorLoader;
 
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.Joint;
 import com.badlogic.gdx.physics.box2d.QueryCallback;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 
 import fi.hbp.angr.AssetContainer;
 import fi.hbp.angr.G;
 import fi.hbp.angr.models.CollisionFilterMasks;
 import fi.hbp.angr.models.SlingshotActor;
 import fi.hbp.angr.stage.GameStage;
 
 /**
  * Hans the player model.
  */
 public class Hans extends Actor implements InputProcessor {
     /**
      * Specific asset container for Hans.
      */
     public static class HansAssetContainer {
         /**
          * Body assets.
          */
         public AssetContainer acBody = new AssetContainer();
         /**
          * Upper part of hand.
          */
         public AssetContainer acHand_u = new AssetContainer();
         /**
          * Lower part of hand.
          */
         public AssetContainer acHand_l = new AssetContainer();
     }
 
     /**
      * Model data for Hans.
      */
     protected class _ModelData {
         /**
          * Body.
          */
         public Body body;
         /**
          * Origin coordinates of the body.
          */
         public Vector2 bodyOrigin;
         /**
          * Sprite.
          */
         public Sprite sprite;
     }
 
     /**
      * Name of this model.
      */
     private static final String MODEL_NAME = "hans";
     /**
      * File path to the body texture.
      */
     private static final String HANS_BODY_TEXTURE = "data/" + MODEL_NAME + "_body.png";
     /**
      * File path to the upper part of hand.
      */
     private static final String HANS_HAND_U_TEXTURE = "data/" + MODEL_NAME + "_hand_u.png";
     /**
      * File path to the lower part of hand.
      */
     private static final String HANS_HAND_L_TEXTURE = "data/" + MODEL_NAME + "_hand_l.png";
 
     /**
      * Model data for body of Hans.
      */
     private final _ModelData hbody;
     /**
      * Model data for upper part of hand.
      */
     private final _ModelData hand_u;
     /**
      * Model data for lower part of hand.
      */
     private final _ModelData hand_l;
     /**
      * Model data array. This is used for rendering.
      */
     private final _ModelData[] modelArray;
 
     /* Palm joint related */
     /**
      * Object jointed into hand.
      */
     private Body objPalm;
     /**
      * Joint used between hand and hand of Hans.
      */
     private Joint palmJoint;
 
     /* Input processing/Controls */
     /**
      * Game stage.
      */
     private final GameStage stage;
     /**
      * Test point for testing contact with mouse.
      */
     private final Vector3 testPoint = new Vector3();
     /**
      * Ground body.
      */
     private final Body groundBody;
     /**
      * Hit body.
      */
     private Body hitBody = null;
     /**
      * true if currently dragging; otherwise false.
      */
     private boolean dragging;
     /**
      * When this is true the palm joint will break if
      * break force is exceeded.
      */
     private boolean enableJointBreaking = false;
 
     /**
      * Preload static data
      */
     public static void preload() {
         G.getAssetManager().load(HANS_BODY_TEXTURE, Texture.class);
         G.getAssetManager().load(HANS_HAND_U_TEXTURE, Texture.class);
         G.getAssetManager().load(HANS_HAND_L_TEXTURE, Texture.class);
     }
 
     /**
      * Initialize assets of this object
      * @param hac storage location for assets of this model.
      * @param bel Body editor loader.
      */
     public static void initAssets(HansAssetContainer hac, BodyEditorLoader bel) {
         /* Textures */
         hac.acBody.texture = G.getAssetManager().get(
                 bel.getImagePath(MODEL_NAME + "_body"),
                 Texture.class);
         hac.acHand_u.texture = G.getAssetManager().get(
                 bel.getImagePath(MODEL_NAME + "_hand_u"),
                 Texture.class);
         hac.acHand_l.texture = G.getAssetManager().get(
                 bel.getImagePath(MODEL_NAME + "_hand_l"),
                 Texture.class);
 
         /* Hans body BodyDef */
         hac.acBody.bd = new BodyDef();
         hac.acBody.bd.type = BodyType.StaticBody;
         hac.acBody.bd.position.set(0, 0);
         hac.acBody.bd.active = true;
 
         /* Hans body FixtureDef */
         hac.acBody.fd = new FixtureDef();
         hac.acBody.fd.density = 4.5f;
         hac.acBody.fd.friction = 1.0f;
         hac.acBody.fd.restitution = 0.3f;
         hac.acBody.fd.filter.categoryBits = CollisionFilterMasks.HANS_BODY;
         hac.acBody.fd.filter.maskBits = ~CollisionFilterMasks.ALL;
 
         /* Hand_u BodyDef */
         hac.acHand_u.bd = new BodyDef();
         hac.acHand_u.bd.type = BodyType.DynamicBody;
         hac.acHand_u.bd.active = true;
 
         /* Hand_u FixtureDef */
         hac.acHand_u.fd = new FixtureDef();
         hac.acHand_u.fd.density = 4.5f;
         hac.acHand_u.fd.friction = 1.0f;
         hac.acHand_u.fd.restitution = 0.3f;
         hac.acHand_u.fd.filter.categoryBits = CollisionFilterMasks.HANS_HAND;
         hac.acHand_u.fd.filter.maskBits = ~CollisionFilterMasks.ALL;
 
         /* Hand_l Defs */
         hac.acHand_l.bd = hac.acHand_u.bd;
         hac.acHand_l.fd = hac.acHand_u.fd;
     }
 
     /**
      * Class constructor.
      * @param stage the game stage.
      * @param bel a Body editor loader object.
      * @param hac preloaded assets for this class.
      * @param x spawn coordinate.
      * @param y spawn coordinate.
      * @param angle spawn angle.
      */
     public Hans(GameStage stage, BodyEditorLoader bel, HansAssetContainer hac, float x, float y, float angle) {
         this.stage = stage;
         World world = stage.getWorld();
 
         /* Attach body */
         hac.acBody.bd.position.set(new Vector2(x * G.WORLD_TO_BOX,
                                                y * G.WORLD_TO_BOX));
         hbody = createModelData(world, bel, hac.acBody, "hans_body");
 
         /* Attach hand_u */
         hac.acHand_u.bd.position.set(new Vector2((x + 80) * G.WORLD_TO_BOX,
                                                y * G.WORLD_TO_BOX));
         hac.acHand_u.bd.angle = -45.0f * MathUtils.degRad;
         hand_u = createModelData(world, bel, hac.acHand_u, "hans_hand_u");
 
         /* Attach hand_l */
         hac.acHand_l.bd.position.set(new Vector2((x + 50) * G.WORLD_TO_BOX,
                                                y * G.WORLD_TO_BOX));
         hand_l = createModelData(world, bel, hac.acHand_l, "hans_hand_l");
 
         /* Create array of models for easier rendering */
         modelArray = new _ModelData[]{ hbody, hand_l, hand_u };
 
         /* Create joints */
         createHandJoint(world, hbody,  new Vector2(0f, 0f),
                                hand_u, new Vector2(0f, 0f),
                                -45.0f * MathUtils.degRad);
         createHandJoint(world, hand_u, new Vector2(0.4f, 0f),
                                hand_l, new Vector2(0f, 0f),
                                90.0f * MathUtils.degRad);
 
         /* We also need an invisible zero size ground body
          * to which we can connect the mouse joint */
         BodyDef bodyDef = new BodyDef();
         groundBody = world.createBody(bodyDef);
     }
 
     private _ModelData createModelData(World world, BodyEditorLoader bel, AssetContainer ac, String modelName) {
         _ModelData md = new _ModelData();
 
         md.body = world.createBody(ac.bd);
         md.body.setUserData(this);
         md.sprite = new Sprite(ac.texture);
         bel.attachFixture(md.body,
                           modelName,
                           ac.fd,
                           md.sprite.getWidth() * G.WORLD_TO_BOX);
         md.bodyOrigin = bel.getOrigin(modelName, md.sprite.getWidth()).cpy();
         Vector2 origin = bel.getOrigin(modelName, md.sprite.getWidth()).cpy();
         md.sprite.setOrigin(origin.x, origin.y);
         md.sprite.setPosition(md.body.getPosition().x * G.BOX_TO_WORLD,
                               md.body.getPosition().y * G.BOX_TO_WORLD);
         md.sprite.setRotation((float)Math.toDegrees(md.body.getAngle()));
 
         return md;
     }
 
     private void createHandJoint(World world, _ModelData a, Vector2 anchorA, _ModelData b, Vector2 anchorB, float refAngle) {
         RevoluteJointDef jointDef = new RevoluteJointDef();
         jointDef.initialize(a.body, b.body, hbody.body.getWorldCenter());
         jointDef.lowerAngle = -0.5f * MathUtils.PI; // -90 degrees
         jointDef.upperAngle = 0.25f * MathUtils.PI; // 45 degrees
         jointDef.referenceAngle = refAngle;
         jointDef.enableLimit = true;
         jointDef.maxMotorTorque = 0.5f;
         jointDef.motorSpeed = 0.0f;
         jointDef.enableMotor = true;
         jointDef.localAnchorA.set(anchorA);
         jointDef.localAnchorB.set(anchorB);
 
         world.createJoint(jointDef);
     }
 
     @Override
     public void draw(SpriteBatch batch, float parentAlpha) {
         for (_ModelData model : modelArray) {
             Vector2 pos = model.body.getPosition();
             model.sprite.setPosition(pos.x * G.BOX_TO_WORLD - model.bodyOrigin.x,
                                      pos.y * G.BOX_TO_WORLD - model.bodyOrigin.y);
             model.sprite.setOrigin(model.bodyOrigin.x, model.bodyOrigin.y);
             model.sprite.setRotation((float)(model.body.getAngle() * MathUtils.radiansToDegrees));
             model.sprite.draw(batch, parentAlpha);
         }
 
         evalJointBreak();
     }
 
     /**
      * Check if palm joint break force is exceed
      */
     private void evalJointBreak() {
         if (palmJoint != null) {
             Vector2 v = palmJoint.getReactionForce(1.0f);
             float len = v.len();
             if (len >= 0.45f && enableJointBreaking) {
                 hand_l.body.getWorld().destroyJoint(palmJoint);
                 palmJoint = null;
                 enableJointBreaking = false;
             } else if (len <= 0.4f) {
                 enableJointBreaking = true;
             }
         }
     }
 
     @Override
     public float getX() {
         Vector2 pos = hbody.body.getPosition();
         return pos.x * G.BOX_TO_WORLD - hbody.bodyOrigin.x;
     }
 
     @Override
     public float getY() {
         Vector2 pos = hbody.body.getPosition();
         return pos.y * G.BOX_TO_WORLD - hbody.bodyOrigin.y;
     }
 
     public void setPalmJoint(Body obj) {
         this.objPalm = obj;
 
         RevoluteJointDef jointDef = new RevoluteJointDef();
         jointDef.initialize(hand_l.body, obj, hand_l.bodyOrigin);
         jointDef.enableLimit = false;
         jointDef.maxMotorTorque = 0.5f;
         jointDef.motorSpeed = 0.0f;
         jointDef.enableMotor = true;
         jointDef.localAnchorA.set(new Vector2(0.8f, 0.0f));
         jointDef.localAnchorB.set(new Vector2(0.0f, 0.0f));
 
         palmJoint = obj.getWorld().createJoint(jointDef);
     }
 
     public boolean isPalmJointActive() {
         return (objPalm != null);
     }
 
     @Override
     public boolean keyDown(int arg0) {
         return false;
     }
 
     @Override
     public boolean keyTyped(char arg0) {
         return false;
     }
 
     @Override
     public boolean keyUp(int arg0) {
         return false;
     }
 
     @Override
     public boolean mouseMoved(int arg0, int arg1) {
         return false;
     }
 
     @Override
     public boolean scrolled(int arg0) {
         return false;
     }
 
     QueryCallback callback = new QueryCallback() {
         @Override
         public boolean reportFixture (Fixture fixture) {
             /* If the hit point is inside the fixture of the body
              * we report it
              */
             Vector2 v = fixture.getBody().getPosition();
             if (fixture.testPoint(testPoint.x, testPoint.y) ||
                     v.dst(testPoint.x, testPoint.y) < SlingshotActor.touchDst) {
                 hitBody = fixture.getBody();
                 if (hitBody.equals(objPalm)) {
                     return false;
                 }
             }
             return true; /* Keep going until all bodies in the are are checked. */
         }
     };
 
     @Override
     public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (palmJoint == null)
             return false;
 
         /* Translate the mouse coordinates to world coordinates */
         stage.getCamera().unproject(testPoint.set(screenX, screenY, 0));
         testPoint.x *= G.WORLD_TO_BOX;
         testPoint.y *= G.WORLD_TO_BOX;
 
         hitBody = null;
         objPalm.getWorld().QueryAABB(callback,
                         testPoint.x - 0.0001f,
                         testPoint.y - 0.0001f,
                         testPoint.x + 0.0001f,
                         testPoint.y + 0.0001f);
         if (hitBody == groundBody) hitBody = null;
 
         if (hitBody == null)
             return false;
 
         /* Ignore kinematic bodies, they don't work with the mouse joint */
         if (hitBody.getType() == BodyType.KinematicBody)
             return false;
 
         if (hitBody.equals(this.objPalm)) {
             dragging = true;
         }
         return false;
     }
 
     @Override
     public boolean touchDragged(int x, int y, int pointer) {
         return false;
     }
 
     @Override
     public boolean touchUp(int arg0, int arg1, int arg2, int arg3) {
         /* Object will be released soon. */
         if (dragging == true) {
             objPalm = null;
             dragging = false;
         }
         return false;
     }
 }
