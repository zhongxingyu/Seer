 package io.github.dector.lightmap.visualiser.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import io.github.dector.lightmap.core.Light;
 import io.github.dector.lightmap.core.LightMap;
 import io.github.dector.lightmap.core.Position;
 import io.github.dector.lightmap.visualiser.assets.AssetsLoader;
 
 import java.util.Random;
 
 import static com.badlogic.gdx.Input.Keys;
 
 /**
  * @author dector
  */
 public class VisualiserScreen extends AbstractScreen {
 
 	private static final int TILE_W = 32;
 	private static final int TILE_H = 32;
 
 	private static final float MIN_ZOOM = 0.5f;
 	private static final float MAX_ZOOM = Math.min(TILE_W, TILE_H);
 
 	private LightMap map;
 
 	private SpriteBatch batch;
 	private SpriteBatch hudBatch;
 	private OrthographicCamera cam;
 	private TextureRegion lightSourceOnTex;
 	private TextureRegion lightSourceOffTex;
 	private TextureRegion tileTex;
 	private TextureRegion playerTex;
 	private TextureRegion darkTex;
 
 	private BitmapFont font;
 
 	private boolean affectLights;
 
 	private int dynamicLightId;
 	private int playerX;
 	private int playerY;
 
 	public VisualiserScreen() {
 		batch = new SpriteBatch();
 		hudBatch = new SpriteBatch();
 		cam = new OrthographicCamera();
 
 		lightSourceOnTex = AssetsLoader.loadImageFileAsRegion("lightSource_on.png", 32, 32);
 		lightSourceOffTex = AssetsLoader.loadImageFileAsRegion("lightSource_off.png", 32, 32);
 		tileTex = AssetsLoader.loadImageFileAsRegion("tile.png", 32, 32);
 		playerTex = AssetsLoader.loadImageFileAsRegion("player.png", 32, 32);
 		darkTex = AssetsLoader.loadImageFileAsRegion("dark.png", 32, 32);
 
 		font = AssetsLoader.loadFont("visitor.ttf", 18);
 
 		affectLights = true;
 
 		// TODO mockup
 		{
			/*int w = 25;
 			int h = 25;
 
 			map = new LightMap(w, h);
 			map.addStaticLight(new Light(3), 5, 5);
 			map.addStaticLight(new Light(4), 9, 5);
 			map.addStaticLight(new Light(6), 16, 8);
 			map.addStaticLight(new Light(5), 6, 15);
 
 			map.addStaticLight(new Light(2, 5), 20, 18);
 			map.addStaticLight(Light.lightCircle(3), 5, 20);
 			map.addStaticLight(Light.lightSquare(3), 13, 20);
 
 			playerX = 10;
 			playerY = 10;
			dynamicLightId = map.addDynamicLight(new Light(3), Position.from(playerX, playerY));*/
 		}
 
 		// Update time test
		{
 			int w = 1000;
 			int h = 1000;
 
 			map = new LightMap(w, h);
 
 			Random rnd = new Random();
 
 			for (int i = 0; i < 10000; i++) {
 				map.addStaticLight(new Light(rnd.nextInt(10)), rnd.nextInt(w), rnd.nextInt(h));
 			}
 
 			playerX = 10;
 			playerY = 10;
 			dynamicLightId = map.addDynamicLight(new Light(3), Position.from(playerX, playerY));
		}
 
 		centerMap();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 
 		cam.viewportWidth = width;
 		cam.viewportHeight = height;
 	}
 
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		map.step();
 
 		cam.update();
 		batch.setProjectionMatrix(cam.combined);
 		batch.begin();
 		batch.setColor(1, 1, 1, 1);
 
 		// Draw tiles
 		for (int i = 0; i < map.getWidth(); i++) {
 			for (int j = 0; j < map.getHeight(); j++) {
 				draw(tileTex, i, j);
 			}
 		}
 
 		// Draw lights
 		for (Position p : map.getStaticLightsPositions()) {
 			if (map.getStaticLightAt(p).isOn()) {
 				draw(lightSourceOnTex, p.x, p.y);
 			} else {
 				draw(lightSourceOffTex, p.x, p.y);
 			}
 		}
 
 		// Draw player
 		draw(playerTex, playerX, playerY);
 
 		// Draw darkness
 		if (affectLights) {
 			for (int i = 0; i < map.getWidth(); i++) {
 				for (int j = 0; j < map.getHeight(); j++) {
 					batch.setColor(1, 1, 1, 1 - map.getLightValueAt(i, j));
 					draw(darkTex, i, j);
 				}
 			}
 		}
 
 		batch.end();
 
 		hudBatch.begin();
 		font.drawMultiLine(hudBatch, getInfoString(), 10, getHeight() - 10);
 		hudBatch.end();
 	}
 
 	private void draw(TextureRegion reg, int x, int y) {
 		batch.draw(reg, x * TILE_W, y * TILE_H, TILE_W, TILE_H);
 	}
 
 	private void centerMap() {
 		cam.position.set(map.getWidth() * TILE_W / 2, map.getHeight() * TILE_H / 2, 0);
 	}
 
 	private String getInfoString() {
 		StringBuilder sbuilder = new StringBuilder();
 
 		sbuilder.append("Map size: ")
 				.append(map.getWidth())
 				.append("x")
 				.append(map.getHeight())
 				.append("\n");
 
 		sbuilder.append("Static lights: ")
 				.append(map.getStaticLightsCount())
 				.append("\n");
 
 		sbuilder.append("\n");
 		sbuilder.append("Drag map with mouse\n");
 		sbuilder.append("Doubleclick to add/remove light\n");
 		sbuilder.append("Scroll on light to change radius\n");
 		sbuilder.append("Scroll elsewhere to zoom\n");
 		sbuilder.append("[F2] to toggle darkness\n");
 		sbuilder.append("[F3] to center map\n");
 		sbuilder.append("[Arrows] to move player\n");
 		sbuilder.append("[R] to put player in random position\n");
 
 		return sbuilder.toString();
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		switch (keycode) {
 			case Keys.ESCAPE:
 				Gdx.app.exit();
 				break;
 			case Keys.F2:
 				affectLights = !affectLights;
 				break;
 			case Keys.F3:
 				centerMap();
 				break;
 			case Keys.RIGHT:
 				playerX += 1;
 				movePlayer();
 				// map.moveDynamicLight(dynamicLightId, 1, 0);
 				break;
 			case Keys.LEFT:
 				playerX -= 1;
 				movePlayer();
 				// map.moveDynamicLight(dynamicLightId, -1, 0);
 				break;
 			case Keys.UP:
 				playerY += 1;
 				movePlayer();
 				// map.moveDynamicLight(dynamicLightId, 0, 1);
 				break;
 			case Keys.DOWN:
 				playerY -= 1;
 				movePlayer();
 				// map.moveDynamicLight(dynamicLightId, 0, -1);
 				break;
 			case Keys.R:
 				Random rnd = new Random();
 
 				playerX = rnd.nextInt(map.getWidth());
 				playerY = rnd.nextInt(map.getHeight());
 
 				movePlayer();
 				break;
 		}
 
 		return true;
 	}
 
 	private void movePlayer() {
 		if (playerX < 0)
 			playerX = 0;
 		else if (playerX >= map.getWidth())
 			playerX = map.getWidth() - 1;
 
 		if (playerY < 0)
 			playerY = 0;
 		else if (playerY >= map.getHeight())
 			playerY = map.getHeight() - 1;
 
 		map.setDynamicLightTo(dynamicLightId, playerX, playerY);
 	}
 
 	private long lastClickTime;
 	private Vector2 dragPointStart = new Vector2();
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		long time = System.currentTimeMillis();
 		long diffTime = time - lastClickTime;
 
 		tmpVec3.set(screenX, screenY, 0);
 		cam.unproject(tmpVec3);
 
 		if (diffTime < 200) {
 			Position tilePos = getTilePositionAt(screenX, screenY);
 
 			if (tilePos != null) {
 				if (map.hasStaticLightAt(tilePos)) {
 					map.removeStaticLightAt(tilePos);
 				} else {
 					map.addStaticLight(new Light(3), tilePos);
 				}
 			}
 		}
 
 		lastClickTime = time;
 		dragPointStart.set(screenX, screenY);
 
 		return true;
 	}
 
 	private final Vector3 tmpVec3 = new Vector3();
 
 	private Position getTilePositionAt(int screenX, int screenY) {
 		tmpVec3.set(screenX, screenY, 0);
 		cam.unproject(tmpVec3);
 
 		int tileX = (int) tmpVec3.x / TILE_W;
 		int tileY = (int) tmpVec3.y / TILE_H;
 
 		if (0 <= tileX && tileX < map.getWidth()
 				&& 0 <= tileY && tileY < map.getHeight()) {
 			return new Position(tileX, tileY);
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		cam.position.x -= (screenX - dragPointStart.x) * cam.zoom;
 		cam.position.y += (screenY - dragPointStart.y) * cam.zoom;
 
 		dragPointStart.set(screenX, screenY);
 
 		return true;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		int mouseX = Gdx.input.getX();
 		int mouseY = Gdx.input.getY();
 
 		Position tilePos = getTilePositionAt(mouseX, mouseY);
 
 		boolean lightChanged = false;
 
 		if (tilePos != null) {
 			Light l = map.getStaticLightAt(tilePos);
 
 			if (l != null) {
 				boolean changeInner = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)
 						|| Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
 
 				if (changeInner) {
 					map.changeStaticLightAt(tilePos, l.innerRadius - amount, l.outerRadius);
 				} else {
 					map.changeStaticLightAt(tilePos, l.innerRadius, l.outerRadius - amount);
 				}
 
 				lightChanged = true;
 			}
 		}
 
 		if (! lightChanged) {
 			if (amount < 0) {
 				cam.zoom /= 2;
 			} else {
 				cam.zoom *= 2;
 			}
 
 			if (cam.zoom < MIN_ZOOM)
 				cam.zoom = MIN_ZOOM;
 			else if (cam.zoom > MAX_ZOOM)
 				cam.zoom = MAX_ZOOM;
 		}
 
 		return true;
 	}
 }
