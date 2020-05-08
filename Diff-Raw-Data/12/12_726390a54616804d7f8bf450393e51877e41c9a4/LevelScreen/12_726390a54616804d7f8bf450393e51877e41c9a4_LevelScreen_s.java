 package com.icbat.game.tradesong.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.maps.tiled.TmxMapLoader;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
 import com.badlogic.gdx.maps.tiled.renderers.*;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.scenes.scene2d.*;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.Timer;
 import com.icbat.game.tradesong.OrthoCamera;
 import com.icbat.game.tradesong.Tradesong;
 import com.icbat.game.tradesong.stages.GameWorldStage;
 import com.icbat.game.tradesong.stages.HUDStage;
 
 /**
  * Generic level screen. The way maps are shown.
  * */
 public class LevelScreen extends AbstractScreen {
 
 	public String mapName = "";
 
     GameWorldStage worldStage;
     HUDStage hud;
 
     Timer itemSpawnTimer;
 
 
     private TiledMap map;
 	private TiledMapRenderer renderer;
 
 	private OrthoCamera rendererCamera;
     private OrthoCamera gameWorldCamera;
 
 	public LevelScreen(String level, Tradesong gameInstance) {
         super(gameInstance);
 
 
         // Load the map
         gameInstance.assets.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
         this.mapName = "maps/" + level + ".tmx";  // "Internal" relative address. What the asset loader wants. Is there a better way to do this?
 
         gameInstance.assets.load(mapName, TiledMap.class);
         gameInstance.assets.finishLoading();
 
         this.map = gameInstance.assets.get(mapName);
         this.renderer = new OrthogonalTiledMapRenderer(this.map, 1f / 64f);
 
 
         int width = Gdx.graphics.getWidth();
         int height = Gdx.graphics.getHeight();
 
         // Load the stages
         worldStage = new GameWorldStage(this.gameInstance, map.getProperties());
         hud = new HUDStage(this.gameInstance);
 
         // Set up cameras
         rendererCamera = new OrthoCamera(width, height);
         gameWorldCamera = new OrthoCamera(width, height);
 
 
         worldStage.setCamera(gameWorldCamera);
 
 
         // DualCamController
         worldStage.getBackgroundActor().addListener(new DualCamController(rendererCamera, gameWorldCamera));
 
 
         // Setup an input Multiplexer
         InputMultiplexer plexer = new InputMultiplexer();
         plexer.addProcessor(hud);
         plexer.addProcessor(worldStage);
 
         Gdx.input.setInputProcessor(plexer);
 
 
         // Set up timers
         itemSpawnTimer = new Timer();
 
 
         int spawnInitialDelay = 5;
         int spawnIntervalSeconds = 6;
         itemSpawnTimer.scheduleTask(
             new Timer.Task() {
                 public void run() {
                     worldStage.spawnItem();
                 }
 
             }, spawnInitialDelay, spawnIntervalSeconds
         );
     }
 
     @Override
 	public void render(float delta) {
 		super.render(delta);
         rendererCamera.update();
         renderer.setView(rendererCamera);
 		renderer.render();
         worldStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
         hud.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
 		worldStage.draw();
         hud.draw();
 	}
 
     @Override
     public void resize(int width, int height) {
         super.resize(width, height);
         worldStage.setViewport(width, height, false);
         hud.setViewport(width,height, false);
         worldStage.getBackgroundActor().setBounds(0, 0, width, height);
     }
 
 	@Override
 	public void dispose() {
 		// TODO dispose ALL the things
         itemSpawnTimer.clear(); // Cancels all tasks
 
 		this.map.dispose();
         this.worldStage.dispose();
 
         super.dispose(); // Likely needs to be called last
 
 	}
 
     @Override
     public void pause() {
         super.pause();
         itemSpawnTimer.stop();
     }
 
     @Override
     public void resume() {
         super.resume();
         itemSpawnTimer.start();
     }
 
     /**
      * Input handling for moving rendererCamera on maps. Handles:
      *  - touch-dragging
      * */
     class DualCamController extends ClickListener {
         final OrthographicCamera camera1;
         final OrthographicCamera camera2;
         final Vector3 curr = new Vector3();
         final Vector3 last = new Vector3(-1, -1, -1);
         final Vector3 delta = new Vector3();
 
         public DualCamController(OrthographicCamera camera1, OrthographicCamera camera2) {
             this.camera1 = camera1;
             this.camera2 = camera2;
         }
 
         @Override
         public void touchDragged(InputEvent event, float x, float y, int pointer) {
             super.touchDragged(event, x, y, pointer);
 
             // Use Camera1 as the last point
             camera1.unproject(curr.set(x, y, 0));
 
             // If this isn't the first drag called
             if (!(last.x == -1 && last.y == -1 && last.z == -1)) {
                 // Still use camera 1 as the latest; this time as change
                 camera1.unproject(delta.set(last.x, last.y, 0));
                 delta.sub(curr);
                 camera1.position.add(delta.x, delta.y * -1, 0);
                 camera2.position.add(delta.x * 32, delta.y * -32, 0);
             }
 
             last.set(x, y, 0);
         }
 
         @Override
         public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
             super.touchUp(event, x, y, pointer, button);
             last.set(-1, -1, -1);
         }
     }
 
 }
