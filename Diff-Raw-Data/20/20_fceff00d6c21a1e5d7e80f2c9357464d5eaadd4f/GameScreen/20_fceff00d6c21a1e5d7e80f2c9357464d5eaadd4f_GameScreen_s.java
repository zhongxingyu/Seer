 package com.github.slamm.morrigna.core;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.ScreenAdapter;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.World;
 import com.droidinteractive.box2dlight.RayHandler;
 import com.github.slamm.morrigna.core.hud.HudRenderSystem;
 import com.github.slamm.morrigna.core.map.MapRenderSystem;
 import com.reed.birdseye.ArrayListsz;
 import com.reed.birdseye.CollisionDetection;
 import com.reed.birdseye.CurrentTool;
 import com.reed.birdseye.Fishing;
 import com.reed.birdseye.Food;
 import com.reed.birdseye.House;
 import com.reed.birdseye.Particles;
 import com.reed.birdseye.Player;
 import com.reed.birdseye.SaveAndLoad;
 import com.reed.birdseye.Time;
 
 public class GameScreen extends ScreenAdapter {
 
     /**
      * static for getting and setting position during save / load
      */
     public static OrthographicCamera mapCamera;
 
     /**
      * static to modify when entering different map areas.
      */
     public static OrthogonalTiledMapRenderer mapRenderer;
 
     public static RayHandler rayHandler;
 
     public static float xRate = 0;
 
     public static float yRate = 0;
 
     private final ArrayListsz arrays;
 
     private final SpriteBatch batch;
 
     private final OrthographicCamera camera;
 
     private final CollisionDetection collision;
 
     private final BitmapFont currentFont;
 
     private final CurrentTool currentTool;
 
     private final Fishing fishing;
 
     private final Food food;
 
     private final House house;
 
     private HudRenderSystem hudSystem;
 
     private MapRenderSystem mapSystem;
 
     private final Player player;
 
     private final ShapeRenderer shapeRenderer;
 
     private final Particles smoke;
 
     private final World world;
 
     public GameScreen() {
         float w = Gdx.graphics.getWidth();
         float h = Gdx.graphics.getHeight();
         batch = new SpriteBatch();
         camera = new OrthographicCamera(w, h);
         mapCamera = new OrthographicCamera(w, h);
         camera.update();
         mapCamera.update();
         mapRenderer = new OrthogonalTiledMapRenderer(Assets.mainTiledMap, batch);
         // translate HUD camera to make bottom left cordinate 0,0
         camera.translate(w / 2, h / 2);
         // translate camera to spawn point
         mapCamera.translate(1422 + 16, 3562 + 24);
         shapeRenderer = new ShapeRenderer();
         player = new Player();
         arrays = new ArrayListsz();
         collision = new CollisionDetection();
         fishing = new Fishing();
         arrays.treeArrayEstablisher();
         house = new House();
         currentFont = new BitmapFont();
         world = new World(new Vector2(0, 0), true);
         rayHandler = new RayHandler(world);
         smoke = new Particles();
         food = new Food();
         currentTool = new CurrentTool();
     }
 
     @Override
     public void hide() {
         SaveAndLoad.save();
     }
 
     @Override
     public void render(float delta) {
         update();
         draw(delta);
         handleInput();
     }
 
     @Override
     public void show() {
         SaveAndLoad.load();
         hudSystem = new HudRenderSystem(batch, camera, currentFont);
         mapSystem = new MapRenderSystem(batch, mapCamera, currentFont);
     }
 
     private void draw(float deltaTime) {
         Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
         Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
         batch.setProjectionMatrix(mapCamera.combined);
         mapCamera.translate(xRate, yRate);
         mapCamera.update();
         mapRenderer.setView(mapCamera);
         mapRenderer.render();
         batch.begin();
         // set camera for drawing moving items.
         batch.setProjectionMatrix(mapCamera.combined);
         arrays.drawTreeTrunk(batch);
         arrays.drawCoal(batch);
         arrays.mobDraw(batch);
         arrays.pigUpdateAndDraw(batch);
         batch.end();
         shapeRenderer.setProjectionMatrix(mapCamera.combined);
         arrays.mobHealthBars(shapeRenderer);
         arrays.pigHealthBars(shapeRenderer);
         batch.begin();
         player.draw(batch);
         // set static for tool drawing (so it is affected by lights)
         batch.setProjectionMatrix(camera.combined);
         currentTool.render(batch);
         currentTool.update();
         // set camera for drawing moving items.
         batch.setProjectionMatrix(mapCamera.combined);
         arrays.drawBrush(batch, currentFont);
         smoke.smokeUpdateAndDraw(batch, deltaTime);
         batch.end();
         mapSystem.render(deltaTime);
         hudSystem.render(deltaTime);
         rayHandler.setCombinedMatrix(mapCamera.combined);
         rayHandler.updateAndRender();
     }
 
     /**
      * Handle input for zooming in and out
      */
     private void handleInput() {
         if (Gdx.input.isKeyPressed(Input.Keys.O)) {
             camera.zoom += 0.02;
             mapCamera.zoom += 0.02;
         }
         if (Gdx.input.isKeyPressed(Input.Keys.I)) {
             camera.zoom -= 0.02;
             mapCamera.zoom -= 0.02;
         }
         if (Gdx.input.isKeyPressed(Input.Keys.U)) {
             camera.zoom = 1;
             mapCamera.zoom = 1;
         }
     }
 
     private void update() {
         mapSystem.update();
         // can be changed anytime in time class
         player.setSprites();
         player.move();
         player.input();
         hudSystem.update();
         collision.doCollision();
         fishing.update();
         fishing.fishCaught();
         house.update();
         Time.update(rayHandler);
         camera.update();
         arrays.mobUpdate();
         food.affectHealth();
         food.looseHunger();
         arrays.updateCoal();
     }
 }
