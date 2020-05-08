 package com.blockTwo.game;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 import javax.media.opengl.GL;
 
 /**
  *
  * @author r.anderson8
  */
 public class Restitution implements ApplicationListener {
 
     private final int pixels_per_m = 30;
     private boolean muted = false;
     private BitmapFont infoText;
     
     private ContactListener contactListener;
     
     private World world;
     private float timeStep = 1.0f / 60.0f;
     private int positionIters = 8;
     private int velocityIters = 3;
     
     private OrthographicCamera camera;
     
     private SpriteBatch batch;
     
     private Texture bgImage;
     private Texture ballImage;
     private Texture audioPlaying;
     private Texture audioMuted;
     
     private Sound bounceSound;
     private Sound buttonTurnOnSound;
     private Sound buttonTurnOffSound;
     
     @Override
     public void create() {
         /**
          * Info Text.
          */
         infoText = new BitmapFont();
         infoText.setScale(3.0f);
         infoText.setColor(1.0f, 1.0f, 1.0f, 1.0f);
         
         /**
          * Camera.
         */
         camera = new OrthographicCamera();
         camera.setToOrtho(false, 800, 480);
         
         /**
          * Contact Listener.
          */
         contactListener = new ContactListener() {
 
             @Override
             public void beginContact(Contact cntct) {
                 Body a = cntct.getFixtureA().getBody();
                 Body b = cntct.getFixtureB().getBody();
                 if (a.getUserData().equals(ballImage)) {
                     if (!muted) {
                         bounceSound.play();
                     }
                 }
                 if (b.getUserData().equals(ballImage)) {
                     if (!muted) {
                         bounceSound.play();
                     }
                 }
             }
 
             @Override
             public void endContact(Contact cntct) {
 
             }
 
             @Override
             public void preSolve(Contact cntct, Manifold mnfld) {
 
             }
 
             @Override
             public void postSolve(Contact cntct, ContactImpulse ci) {
 
             }
         };
         
         /**
          * Sprite Batch.
          */
         batch = new SpriteBatch();
         
         /**
          * Textures.
          */
        bgImage = new Texture(Gdx.files.internal("assets/background.jpg"));
        ballImage = new Texture(Gdx.files.internal("assets/ball_orange.png"));
        audioPlaying = new Texture(Gdx.files.internal("assets/audio.png"));
        audioMuted = new Texture(Gdx.files.internal("assets/audio_muted.png"));
         
         /**
          * Audio.
          */
         // Sounds
        bounceSound = Gdx.audio.newSound(Gdx.files.internal("assets/ball_bounce.wav"));
        buttonTurnOnSound = Gdx.audio.newSound(Gdx.files.internal("assets/click_on.wav"));
        buttonTurnOffSound = Gdx.audio.newSound(Gdx.files.internal("assets/click_off.wav"));
         // Music
         
         
         /**
          * World.
          */
         world = new World(new Vector2(0.0f, -15.81f), true);
         world.setContactListener(contactListener);
         
         
         /**
          * Body Definitions.
          */
         // Ground
         BodyDef groundDef = new BodyDef();
         groundDef.allowSleep = false;
         groundDef.position.set(0.0f, 0.0f);
         groundDef.type = BodyType.StaticBody;
         
         // Ball
         BodyDef ballDef = new BodyDef();
         ballDef.allowSleep = true;
         ballDef.position.set(new Vector2(camera.viewportWidth / pixels_per_m * 1 / 4, 480 / pixels_per_m - 1.0f));
         ballDef.type = BodyType.DynamicBody;
         
         /**
          * Fixture Definitions.
          */
         // Ground
         FixtureDef groundFixDef = new FixtureDef();
         groundFixDef.density = 5.0f;
         EdgeShape groundShape = new EdgeShape();
         groundShape.set(new Vector2(-50.0f, 0.0f), new Vector2(50.0f, 0.0f));
         groundFixDef.shape = groundShape;
         
         // Ball
         FixtureDef ballFixDef = new FixtureDef();
         ballFixDef.density = 5.0f;
         ballFixDef.restitution = 0.8f;
         ballFixDef.shape = new CircleShape();
         ballFixDef.shape.setRadius(1.0f);
         
         /**
          * Body Creation.
          */
         Body ground = world.createBody(groundDef);
         Texture groundImage = new Texture(ballImage.getTextureData());
         ground.setUserData(groundImage);
         Body ball = world.createBody(ballDef);
         ball.setUserData(ballImage);
         
         /**
          * Fixture Creation.
          */
         ground.createFixture(groundFixDef);
         ball.createFixture(ballFixDef);
         
         
         
     }
 
     @Override
     public void resize(int width, int height) {
         
     }
 
     @Override
     public void render() {
         /**
          * World Update.
          */
         world.step(timeStep, velocityIters, positionIters);
         
         /**
          * Camera Update.
          */
         camera.update();
         
         /**
          * User Input.
          */
         if (Gdx.input.justTouched()) {
             
             // Swap the "muted" flag
             if (muted) {
                 buttonTurnOnSound.play();
                 muted = false;
             } else {
                 buttonTurnOffSound.play();
                 muted = true;
             }
             // Swap the ball's restitution value
             Iterator<Body> iter = world.getBodies();
             while (iter.hasNext()) {
                 Body b = iter.next();
                 if (b.getUserData().equals(ballImage)) {
                     ArrayList<Fixture> fixtures = b.getFixtureList();
                     for (Fixture fixture : fixtures) {
                         if (fixture.getRestitution() == 1.0f) {
                             fixture.setRestitution(0.8f);
                         } else {
                             fixture.setRestitution(1.0f);
                         }
                     }
                 }
             }
         } else if (Gdx.input.isKeyPressed(Keys.R)) {
             /**
              * Reset.
              */
             // Reset the ball's position
             Iterator<Body> iter = world.getBodies();
             while (iter.hasNext()) {
                 Body b = iter.next();
                 if (b.getUserData().equals(ballImage)) {
                     b.setTransform(new Vector2(b.getPosition().x, camera.viewportHeight / pixels_per_m - 1.0f), b.getAngle());
                     b.setLinearVelocity(Vector2.Zero);
 //                    b.setActive(true);
                     b.setAwake(true);
                 }
             }
         }
         
         /**
          * Clear Screen.
          */
         Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f); // White
         Gdx.gl.glClear(GL.GL_COLOR_BUFFER_BIT); // Clear the Screen
         
         
         
         /*********************************
          * Drawing.
          *********************************/
         batch.setProjectionMatrix(camera.combined);
         batch.begin();
         
         /**
          * Background Drawing.
          */
         batch.draw(bgImage, 0, 0, camera.viewportWidth, camera.viewportHeight);
         
         /**
          * Body Drawing.
          */
         Iterator<Body> iter = world.getBodies();
         while (iter.hasNext()) {
             Body body = iter.next();
             ArrayList<Fixture> fixtures = body.getFixtureList();
             for (int i=0; i < fixtures.size(); i++) {
                 Body curBody = fixtures.get(i).getBody();
                 Fixture curFix = fixtures.get(i);
                 Vector2 worldPoint = curBody.getWorldPoint(Vector2.Zero);
                 Shape curShape = curFix.getShape();
                 float width = 0.0f;
                 float height = 0.0f;
                 width = curShape.getRadius() * 2;
                 height = width;
                 Vector2 screenPoint = new Vector2(worldPoint.x * pixels_per_m - (width * pixels_per_m / 2), worldPoint.y * pixels_per_m - (height * pixels_per_m / 2));
                 batch.draw((Texture) curBody.getUserData(), screenPoint.x, screenPoint.y,
                         width * pixels_per_m, height * pixels_per_m);
             }
         }
         
         /**
          * Audio State Drawing.
          */
         float x = 400;
         float y = 400;
         float width = 64;
         float height = 64;
         if (muted) {
             batch.draw(audioMuted, x, y, width, height);
         } else {
             batch.draw(audioPlaying, x, y, width, height);
         }
         
         /**
          * Info Text Drawing.
          */
         infoText.draw(batch, "Press 'r' to reset", x, y - 10); // Using coords from the audio state image
         
         batch.end();
         // End Drawing
         
         
     }
     
 
     @Override
     public void pause() {
 
     }
 
     @Override
     public void resume() {
 
     }
 
     @Override
     public void dispose() {
         world.dispose();
         batch.dispose();
         ballImage.dispose();
         bounceSound.dispose();
     }
     
 }
