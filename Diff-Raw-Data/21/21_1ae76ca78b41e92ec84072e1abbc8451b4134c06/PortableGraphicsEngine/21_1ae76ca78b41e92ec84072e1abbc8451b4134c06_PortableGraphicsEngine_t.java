 package com.avona.games.towerdefence.gfx;
 
 import com.avona.games.towerdefence.Game;
 import com.avona.games.towerdefence.Layer;
 import com.avona.games.towerdefence.LayerHerder;
 import com.avona.games.towerdefence.Mouse;
 import com.avona.games.towerdefence.PortableMainLoop;
 import com.avona.games.towerdefence.RGB;
 import com.avona.games.towerdefence.TickRater;
 import com.avona.games.towerdefence.TimeTrack;
 import com.avona.games.towerdefence.V2;
 import com.avona.games.towerdefence.enemy.Enemy;
 import com.avona.games.towerdefence.particle.Particle;
 import com.avona.games.towerdefence.tower.Tower;
 
 public abstract class PortableGraphicsEngine {
 
 	public static final int DEFAULT_HEIGHT = 480;
 	public static final int DEFAULT_WIDTH = 675;
 
 	public V2 size = new V2();
 
 	public Layer menuLayer;
 	public Layer gameLayer;
 
 	protected TimeTrack graphicsTime;
 	protected TickRater graphicsTickRater;
 	protected Game game;
 	protected Mouse mouse;
 
 	protected VertexArray[] worldVertices;
 	protected VertexArray[] menuVertices;
 
 	public PortableGraphicsEngine(Game game, Mouse mouse,
 			LayerHerder layerHerder, TimeTrack graphicsTime) {
 		this.game = game;
 		this.mouse = mouse;
 		this.graphicsTime = graphicsTime;
 		this.graphicsTickRater = new TickRater(graphicsTime);
 
 		gameLayer = layerHerder
 				.findLayerByName(PortableMainLoop.GAME_LAYER_NAME);
 		menuLayer = layerHerder
 				.findLayerByName(PortableMainLoop.MENU_LAYER_NAME);
 	}
 
	public void onNewScreenContext() {
		// Make sure that the VertexArrays are cleared on a screen context
		// reset.  Otherwise, any preloaded texture wouldn't be reloaded again.
		worldVertices = null;
		menuVertices = null;
	}

 	public abstract Texture allocateTexture();
 
 	public abstract void prepareTransformationForLayer(Layer layer);
 
 	public abstract void resetTransformation();
 
 	protected abstract void prepareScreen();
 
 	protected abstract void drawVertexArray(final VertexArray array);
 
 	public abstract void drawText(final String text, final double x,
 			final double y, final float colR, final float colG,
 			final float colB, final float colA);
 
 	public abstract V2 getTextBounds(final String text);
 
 	public void render(final float gameDelta, final float graphicsDelta) {
 		graphicsTickRater.updateTickRate();
 
 		prepareScreen();
 
 		prepareTransformationForLayer(gameLayer);
 		renderWorld();
 
 		for (Enemy e : game.enemies) {
 			renderEnemy(e);
 		}
 		for (Tower t : game.towers) {
 			renderTower(t);
 		}
 		for (Particle p : game.particles) {
 			renderParticle(p);
 		}
 		if (game.selectedObject != null) {
 			if (game.selectedObject instanceof Tower) {
 				final Tower t = (Tower) game.selectedObject;
 				drawCircle(t.location.x, t.location.y, t.range, 1.0f, 1.0f,
 						1.0f, 1.0f);
 			}
 		}
 		if (game.draggingTower && game.selectedBuildTower != null) {
 			final V2 l = gameLayer.convertToVirtual(mouse.location);
 			final Tower t = game.selectedBuildTower;
 			drawCircle(l.x, l.y, t.range, 1.0f, 1.0f, 1.0f, 1.0f);
 		}
 		resetTransformation();
 
 		prepareTransformationForLayer(menuLayer);
 		renderMenu();
 		resetTransformation();
 
 		if(!game.gameTime.isRunning()) {
 			renderPauseOverlay(gameLayer, true);
 			renderPauseOverlay(menuLayer, false);
 		}
 
 		renderStats();
 		renderMouse();
 	}
 	
 	private void renderPauseOverlay(Layer layer, boolean renderText) {
 		prepareTransformationForLayer(layer);
 		final VertexArray va = new VertexArray();
 		va.hasColour = true;
 		va.numCoords = 4;
 		va.mode = VertexArray.Mode.TRIANGLE_STRIP;
 
 		va.reserveBuffers();
 
 		GeometryHelper.boxVerticesAsTriangleStrip(0, 0, layer.virtualRegion.x, layer.virtualRegion.y, va);
 		
 		for(int i = 0; i < 4; ++i)
 			va.addColour(0.0f, 0.0f, 0.0f, 0.4f);
 		drawVertexArray(va);
 
 		va.freeBuffers();
 		
 		if(renderText)
 			// XXX: Maybe this is working by chance? :P
 			drawText("Game paused", layer.virtualRegion.x / 2 - 10, layer.virtualRegion.y + 40f, 1.0f, 1.0f, 1.0f, 1.0f);
 		
 		resetTransformation();
 	}
 
 	protected void createWorld() {
 		if (worldVertices != null) {
 			// In case we're recreating the world, allow re-using of the
 			// buffers.
 			for (VertexArray va : worldVertices) {
 				va.freeBuffers();
 			}
 		}
 		worldVertices = new VertexArray[1];
 
 		VertexArray va = new VertexArray();
 		va.hasTexture = true;
 		va.numCoords = 4;
 		va.mode = VertexArray.Mode.TRIANGLE_STRIP;
 
 		va.reserveBuffers();
 		GeometryHelper.boxVerticesAsTriangleStrip(0.0f, 0.0f,
 				gameLayer.virtualRegion.x, gameLayer.virtualRegion.y, va);
 
 		va.texture = allocateTexture();
 		va.texture.loadImage(game.world.gameBackgroundName);
 
 		GeometryHelper.boxTextureAsTriangleStrip(va);
 
 		worldVertices[0] = va;
 	}
 
 	protected void renderWorld() {
 		if (worldVertices == null) {
 			createWorld();
 		}
 
 		for (VertexArray va : worldVertices) {
 			drawVertexArray(va);
 		}
 	}
 
 	protected void buildMenu() {
 		menuVertices = new VertexArray[1];
 
 		VertexArray va = new VertexArray();
 		va.hasTexture = true;
 		va.numCoords = 4;
 		va.mode = VertexArray.Mode.TRIANGLE_STRIP;
 
 		va.reserveBuffers();
 
 		GeometryHelper.boxVerticesAsTriangleStrip(0.0f, 0.0f,
 				menuLayer.virtualRegion.x, menuLayer.virtualRegion.y, va);
 
 		va.texture = allocateTexture();
 		va.texture.loadImage(game.world.menuBackgroundName);
 
 		GeometryHelper.boxTextureAsTriangleStrip(va);
 
 		drawVertexArray(va);
 		menuVertices[0] = va;
 	}
 
 	protected void renderMenu() {
 		if (menuVertices == null) {
 			buildMenu();
 		}
 
 		for (VertexArray va : menuVertices) {
 			drawVertexArray(va);
 		}
 	}
 
 	public void renderEnemy(final Enemy e) {
 		if (e.isDead())
 			return;
 
 		RGB gfxcol = e.life.normalized();
 
 		final float width = 12;
 		final V2 location = e.location;
 
 		final VertexArray va = new VertexArray();
 		va.hasColour = true;
 		va.numCoords = 4;
 		va.mode = VertexArray.Mode.TRIANGLE_STRIP;
 
 		va.reserveBuffers();
 
 		GeometryHelper.boxVerticesAsTriangleStrip(location.x - width / 2,
 				location.y - width / 2, width, width, va);
 
 		// Top right
 		va.addColour(gfxcol.R * 1.0f, gfxcol.G * 0.9f, gfxcol.B * 0.9f, 1.0f);
 		// Bottom right
 		va.addColour(gfxcol.R * 0.8f, gfxcol.G * 0.6f, gfxcol.B * 0.6f, 1.0f);
 		// Top left
 		va.addColour(gfxcol.R * 0.6f, gfxcol.G * 0.8f, gfxcol.B * 0.8f, 1.0f);
 		// Bottom left
 		va.addColour(gfxcol.R * 0.9f, gfxcol.G * 1.0f, gfxcol.B * 1.0f, 1.0f);
 
 		drawVertexArray(va);
 
 		va.freeBuffers();
 	}
 
 	public void renderStats() {
 		String towerString = "";
 		if (game.selectedObject != null) {
 			if (game.selectedObject instanceof Tower) {
 				final Tower t = (Tower) game.selectedObject;
 				towerString = String.format("tower lvl %d | ", t.level);
 			} else if (game.selectedObject instanceof Enemy) {
 				final Enemy e = (Enemy) game.selectedObject;
 				towerString = String
 						.format(
 								"enemy lvl %d, health R%.0f G%.0f B%.0f  /  R%.0f G%.0f B%.0f | ",
 								e.level, e.life.R, e.life.G, e.life.B,
 								e.maxLife.R, e.maxLife.G, e.maxLife.B);
 			}
 		}
 		final String fpsString = String.format(
 				"%swave %d | %d killed | %d lifes | $%d | fps %.2f",
 				towerString, game.currentWave != null ? game.currentWave
 						.getLevel() : -1, game.killed, game.lifes, game.money,
 				graphicsTickRater.tickRate);
 		final V2 bounds = getTextBounds(fpsString);
 		final float width = bounds.x + 4;
 		final float height = bounds.y + 2;
 
 		VertexArray va = new VertexArray();
 		va.hasColour = true;
 		va.numCoords = 4;
 		va.mode = VertexArray.Mode.TRIANGLE_STRIP;
 
 		va.reserveBuffers();
 
 		GeometryHelper
 				.boxVerticesAsTriangleStrip(0.0f, 0.0f, width, height, va);
 		GeometryHelper.boxColoursAsTriangleStrip(0.0f, 0.0f, 0.0f, 0.2f, va);
 
 		drawVertexArray(va);
 
 		va.freeBuffers();
 
 		drawText(fpsString, 2, 4, 1.0f, 1.0f, 1.0f, 1.0f);
 	}
 
 	public void renderTower(final Tower t) {
 		final float width = t.radius;
 		final V2 location = t.location;
 
 		RGB gfxcol = t.strength.normalized();
 
 		VertexArray va = new VertexArray();
 		va.hasColour = true;
 		va.numCoords = 4;
 		va.mode = VertexArray.Mode.TRIANGLE_STRIP;
 
 		va.reserveBuffers();
 
 		GeometryHelper.boxVerticesAsTriangleStrip(location.x - width / 2,
 				location.y - width / 2, width, width, va);
 
 		// Top right
 		va.addColour(gfxcol.R * 1.0f, gfxcol.G * 0.9f, gfxcol.B * 0.9f, 1.0f);
 		// Bottom right
 		va.addColour(gfxcol.R * 0.8f, gfxcol.G * 0.6f, gfxcol.B * 0.6f, 1.0f);
 		// Top left
 		va.addColour(gfxcol.R * 0.6f, gfxcol.G * 0.8f, gfxcol.B * 0.8f, 1.0f);
 		// Bottom left
 		va.addColour(gfxcol.R * 0.9f, gfxcol.G * 1.0f, gfxcol.B * 1.0f, 1.0f);
 
 		drawVertexArray(va);
 
 		va.freeBuffers();
 	}
 
 	public void renderParticle(final Particle p) {
 		if (p.isDead())
 			return;
 
 		RGB gfxcol = p.strength.normalized();
 
 		final float width = 10;
 		final V2 location = p.location;
 
 		VertexArray va = new VertexArray();
 
 		va.hasColour = true;
 		va.numCoords = 4;
 		va.mode = VertexArray.Mode.TRIANGLE_STRIP;
 
 		va.reserveBuffers();
 
 		GeometryHelper.boxVerticesAsTriangleStrip(location.x - width / 2,
 				location.y - width / 2, width, width, va);
 
 		// Top right
 		va.addColour(gfxcol.R * 1.0f, gfxcol.G * 0.9f, gfxcol.B * 0.9f, 1.0f);
 		// Bottom right
 		va.addColour(gfxcol.R * 0.8f, gfxcol.G * 0.6f, gfxcol.B * 0.6f, 1.0f);
 		// Top left
 		va.addColour(gfxcol.R * 0.6f, gfxcol.G * 0.8f, gfxcol.B * 0.8f, 1.0f);
 		// Bottom left
 		va.addColour(gfxcol.R * 0.9f, gfxcol.G * 1.0f, gfxcol.B * 1.0f, 1.0f);
 
 		drawVertexArray(va);
 
 		va.freeBuffers();
 	}
 
 	public void renderMouse() {
 		if (!mouse.onScreen)
 			return;
 		final V2 p = mouse.location;
 		final float col = 0.5f + 0.3f * (float) Math.abs(Math
 				.sin(4 * graphicsTime.clock));
 		drawFilledCircle(p.x, p.y, mouse.radius, 1.0f, 1.0f, 1.0f, col);
 	}
 
 	protected void onReshapeScreen() {
 		final float gameFieldPercentage = gameLayer.virtualRegion.x
 				/ (gameLayer.virtualRegion.x + menuLayer.virtualRegion.x);
 
 		final float gameRatio = gameLayer.virtualRegion.x
 				/ gameLayer.virtualRegion.y;
 
 		gameLayer.region.y = size.y;
 		gameLayer.region.x = gameRatio * gameLayer.region.y;
 		if (gameLayer.region.x / size.x > gameFieldPercentage) {
 			// Too wide, screen width is the limit.
 			gameLayer.region.x = size.x * gameFieldPercentage;
 			gameLayer.region.y = gameLayer.region.x / gameRatio;
 		}
 		gameLayer.offset.x = 0;
 		gameLayer.offset.y = (size.y - gameLayer.region.y) * 0.5f;
 
 		final float menuRatio = menuLayer.virtualRegion.x
 				/ menuLayer.virtualRegion.y;
 
 		final V2 remainingSize = new V2(size.x - gameLayer.offset.x
 				- gameLayer.region.x, size.y);
 
 		menuLayer.region.y = remainingSize.y;
 		menuLayer.region.x = menuRatio * menuLayer.region.y;
 		if (menuLayer.region.x > remainingSize.x) {
 			// Too wide, screen width is the limit.
 			menuLayer.region.x = remainingSize.x;
 			menuLayer.region.y = menuLayer.region.x / menuRatio;
 		}
 
 		gameLayer.offset.x += (remainingSize.x - menuLayer.region.x) * .5f;
 
 		menuLayer.offset.y = gameLayer.offset.y;
 		menuLayer.offset.x = gameLayer.offset.x + gameLayer.region.x;
 	}
 
 	public void drawCircle(final float x, final float y, final float radius,
 			final float colR, final float colG, final float colB,
 			final float colA, final int segments, VertexArray va) {
 
 		final double angleStep = 2 * Math.PI / segments;
 		final float[] cols = new float[] { colR, colG, colB, colA };
 
 		for (int i = 0; i < segments; ++i) {
 			final double angle = i * angleStep;
 			va.addCoord(x + (Math.cos(angle) * radius), y
 					+ (Math.sin(angle) * radius));
 			va.addColour(cols);
 		}
 		// Close the circle.
 		va.addCoord(x + (Math.cos(angleStep) * radius), y
 				+ (Math.sin(angleStep) * radius));
 		va.addColour(cols);
 	}
 
 	public void drawCircle(final float x, final float y, final float radius,
 			final float colR, final float colG, final float colB,
 			final float colA) {
 
 		final int segments = 100;
 
 		VertexArray va = new VertexArray();
 		va.hasColour = true;
 		va.numCoords = 101;
 		va.mode = VertexArray.Mode.LINE_STRIP;
 
 		va.reserveBuffers();
 
 		drawCircle(x, y, radius, colR, colG, colB, colA, segments, va);
 
 		drawVertexArray(va);
 		va.freeBuffers();
 	}
 
 	public void drawFilledCircle(final float x, final float y,
 			final float radius, final float colR, final float colG,
 			final float colB, final float colA) {
 
 		final int segments = 100;
 
 		VertexArray va = new VertexArray();
 		va.hasColour = true;
 		va.numCoords = 101;
 		va.mode = VertexArray.Mode.TRIANGLE_FAN;
 
 		va.reserveBuffers();
 		va.addCoord(x, y);
 		va.addColour(colR, colG, colB, colA);
 
 		drawCircle(x, y, radius, colR, colG, colB, colA, segments, va);
 
 		drawVertexArray(va);
 	}
 }
