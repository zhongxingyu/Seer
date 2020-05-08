 package me.hopps.ld27.screens;
 
 import com.artemis.Entity;
 import com.artemis.World;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.utils.TimeUtils;
 import me.hopps.ld27.game.EntityCreator;
 import me.hopps.ld27.game.systems.BlockRenderer;
 import me.hopps.ld27.game.systems.DisappearUpdater;
 import me.hopps.ld27.game.systems.PhysicsUpdater;
 import me.hopps.ld27.utils.ResourceManager;
 
 public class GameScreen implements Screen {
 
     private static final int MAXLVL = 5;
     int lvl = 5;
 
     ResourceManager resManager;
     OrthographicCamera cam;
     PhysicsUpdater physics;
     boolean started;
     World world;
     Entity player;
     float x, y, r;
     long startTime;
 
     public GameScreen(ResourceManager resManager) {
         cam = new OrthographicCamera();
         cam.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
         cam.position.set(400f, 300f, 0f);
         cam.update();
         this.resManager = resManager;
 
         createLevel(lvl);
     }
 
     private void createLevel(int lvl) {
 
         if(lvl <= MAXLVL) {
             world = new World();
             world.setSystem(new BlockRenderer(resManager.shapeRenderer));
             world.setSystem(new DisappearUpdater(resManager.assets.get("res/sounds/ouch.wav", Sound.class)));
             physics = new PhysicsUpdater(resManager.assets.get("res/sounds/jump.wav", Sound.class));
             world.setSystem(physics);
             world.initialize();
 
             Pixmap level = new Pixmap(Gdx.files.internal("res/levels/" + lvl + ".png"));
             /*
              * -1           = Block
              * -16776961    = Start
              * -2139062017  = DeathTrap
              * 2555903      = End
              * -2621185     = Disappearing Block
              */
             for(int i = 0; i < level.getWidth(); i++) {
                 for(int k = 0; k < level.getHeight(); k++) {
                     if(level.getPixel(i, k) == -1) {
                         EntityCreator.createBlock(world, i*32, k*32);
                     }
                     if(level.getPixel(i, k) == -2621185) {
                         EntityCreator.createDisappeaingBlock(world, i * 32, k * 32);
                     }
                     if(level.getPixel(i, k) == -16776961) {
                         x = i * 32f + 4;
                         y = k * 32f + 4;
                         player = EntityCreator.createPlayer(world, x, y);
                     }
                     if(level.getPixel(i, k) == -2139062017) {
                         EntityCreator.createTrap(world, i * 32, k * 32);
                     }
                     if(level.getPixel(i, k) == 2555903) {
                         EntityCreator.createEnd(world, i * 32, k * 32);
                     }
                 }
             }
         }
     }
 
     @Override
     public void render(float delta) {
         Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
         r += delta;
 
         if(physics.lose) {
             started = false;
             resManager.assets.get("res/sounds/boom.wav", Sound.class).play();
            createLevel(lvl);
         }
         if(physics.won) {
             player.disable();
             player = EntityCreator.createPlayer(world, x, y);
             started = false;
             resManager.assets.get("res/sounds/jay.wav", Sound.class).play();
             physics.won = false;
             createLevel(++lvl);
         }
         if(!started) {
             startTime = TimeUtils.millis();
             r = 0;
         }
         if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
             if(!started) {
                 started = true;
                 resManager.assets.get("res/sounds/select.wav", Sound.class).play();
             }
         }
         if(started) {
             if(Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
                 physics.jump = true;
             }
             if(Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                 physics.right = true;
             }
             if(Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                 physics.left = true;
             }
         }
 
         if(r > 1f) {
             resManager.assets.get("res/sounds/beep.wav", Sound.class).play();
             r -= 1f;
         }
 
         if(10000 + startTime - TimeUtils.millis() < 0) {
             physics.lose = true;
         }
 
         resManager.spriteBatch.begin();
         resManager.bigFont.draw(resManager.spriteBatch,"" + ((10000 + startTime - TimeUtils.millis())/1000), 400 - resManager.bigFont.getBounds("" + ((10000 + startTime - TimeUtils.millis())/1000)).width/2, 325);
         if(!started) {
             resManager.smallFont.draw(resManager.spriteBatch,"Press Space to start, then use W/A/D to move", Gdx.graphics.getWidth()/2 - resManager.smallFont.getBounds("Press Space to start, then use W/A/D to move").width/2, 265);
         }
         resManager.spriteBatch.end();
 
         cam.update();
         resManager.shapeRenderer.setProjectionMatrix(cam.combined);
         resManager.shapeRenderer.begin(ShapeRenderer.ShapeType.FilledRectangle);
         world.setDelta(delta);
         world.process();
         resManager.shapeRenderer.end();
     }
 
     @Override
     public void resize(int x, int y) {
     }
 
     @Override
     public void show() {
     }
 
     @Override
     public void hide() {
     }
 
     @Override
     public void pause() {
     }
 
     @Override
     public void resume() {
     }
 
     @Override
     public void dispose() {
     }
 }
