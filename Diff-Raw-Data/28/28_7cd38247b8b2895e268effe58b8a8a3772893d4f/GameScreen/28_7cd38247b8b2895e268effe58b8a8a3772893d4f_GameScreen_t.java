 package com.vgdc.merge.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.vgdc.merge.MainGame;
 import com.vgdc.merge.entities.Entity;
 import com.vgdc.merge.entities.Item;
 import com.vgdc.merge.entities.Platform;
 import com.vgdc.merge.entities.physics.PlatformType;
 import com.vgdc.merge.entities.rendering.HealthBarRenderer;
 import com.vgdc.merge.entities.rendering.HitboxRenderer;
 import com.vgdc.merge.entities.rendering.PlatformRenderer;
 import com.vgdc.merge.world.World;
 
 public class GameScreen extends AbstractScreen {
 	public static final boolean SHOWHITBOXES = false;
 
 	private SpriteBatch batch;
 
 	private World myWorld;
 
 	public GameScreen(MainGame game) {
 		super(game);
 
 	}
 
 	@Override
 	public void dispose() {
 		batch.dispose();
 		myWorld.dispose();
 	}
 
 	float max;
 
 	HealthBarRenderer hpr;
 
 	@Override
 	public void render(float delta) {
 		if (delta < 0.05f) {
 			Gdx.gl.glClearColor(1, 1, 1, 1);
 			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 			batch.begin();
 			myWorld.onUpdate();
 			myWorld.onRender(batch);
 
 			hpr.onRender(batch, delta);
 			batch.end();
 			if (SHOWHITBOXES)
 				hrenderer.onRender(myWorld.getCamera());
 
 		}
 	}
 
 	@Override
 	public void resize(int width, int height) {
 
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	HitboxRenderer hrenderer;
 
 	@Override
 	public void show() {
 		float w = Gdx.graphics.getWidth();
 		float h = Gdx.graphics.getHeight();
 
 		myWorld = new World();
 		myWorld.setCamera(new OrthographicCamera(800, 600));
 		myWorld.getCamera().position.x = 400;
 		myWorld.getCamera().position.y = 300;
 		myWorld.setHandler(game.getHandler());
 		myWorld.setDimensions(800, 600);
 
 		if (SHOWHITBOXES) {
 			hrenderer = new HitboxRenderer(myWorld);
 		}
 
 		hpr = new HealthBarRenderer(myWorld);
 
 		batch = new SpriteBatch();
 
 		Entity testEntity = null;
 		testEntity = new Entity(
				game.getHandler().getEntityData("test_enemy"), myWorld);
 		testEntity.setPosition(new Vector2(500, 0));
 		myWorld.getEntityManager().addEntity(testEntity);
 		testEntity = new Entity(
				game.getHandler().getEntityData("test_player"), myWorld);
 		testEntity.setPosition(new Vector2(58, 58));
 		testEntity.getMovingBody().setElasticity(0);
 		myWorld.getEntityManager().addEntity(testEntity);
 
		Item item = new Item(game.getHandler().getEntityData("test_item"),
 				myWorld);
 		item.setPosition(new Vector2(450, 275));
 		item.getMovingBody().setElasticity(0);
 		item.getController().onCreate();
 		myWorld.getEntityManager().addEntity(item);
 
		item = new Item(game.getHandler().getEntityData("spear_i_e"),
 				myWorld);
 		item.setPosition(new Vector2(650, 275));
 		item.getMovingBody().setElasticity(0);
 		item.getController().onCreate();
 		myWorld.getEntityManager().addEntity(item);
 
 		for (Vector2 pos : new Vector2[] { new Vector2(250, 50),
 				new Vector2(650, 50), new Vector2(750, 500),
 				new Vector2(-50/* 650 */, 300) }) {
 			Platform platform = new Platform(myWorld);
 			platform.getPlatformBody().setPlatformType(PlatformType.Rectangle);
			platform.setRenderer(new PlatformRenderer(game.getHandler().getTexture(
					"PlatformTest"), 13, 13, 13, 13));
 			platform.getPhysicsBody().setPosition(pos);
 			platform.getPhysicsBody().setSize(new Vector2(200, 50));
 			myWorld.getEntityManager().addEntity(platform);
 		}
 		// Tall platform
 		Platform platform = new Platform(myWorld);
 		platform.getPlatformBody().setPlatformType(PlatformType.Rectangle);
 		platform.setRenderer(new PlatformRenderer(game.getHandler().getTexture("data/test/PlatformTest.png"),
 				13, 13, 13, 13));
 		platform.getPhysicsBody().setPosition(new Vector2(400, 125));
 		platform.getPhysicsBody().setSize(new Vector2(50, 200));
 		myWorld.getEntityManager().addEntity(platform);
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 
 	}
 }
