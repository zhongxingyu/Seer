 package com.avona.games.towerdefence;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 public abstract class PortableGraphicsEngine {
 
 	public static final int DEFAULT_HEIGHT = 480;
 	public static final int DEFAULT_WIDTH = 675;
 
 	public V2 size;
 
 	public LayerHerder layerHerder = new LayerHerder();
 	public Layer menuLayer;
 	public Layer gameLayer;
 
 	protected TimeTrack graphicsTime = new TimeTrack();
 	protected TickRater graphicsTickRater = new TickRater(graphicsTime);
 	protected Game game;
 	protected Mouse mouse;
 
 	private FloatBuffer vertexBuffer;
 	private FloatBuffer colorBuffer;
 
 	public PortableGraphicsEngine(Game game, Mouse mouse) {
 		this.game = game;
 		this.mouse = mouse;
 
 		gameLayer = new Layer();
 		gameLayer.virtualRegion.x = World.WIDTH;
 		gameLayer.virtualRegion.y = World.HEIGHT;
 		gameLayer.name = "game";
 		layerHerder.layers.add(gameLayer);
 
 		menuLayer = new Layer();
 		menuLayer.virtualRegion.x = 125;
 		menuLayer.virtualRegion.y = 480;
 		menuLayer.name = "menu";
 		layerHerder.layers.add(menuLayer);
 
 		vertexBuffer = allocateFloatBuffer(102 * 2);
 		colorBuffer = allocateFloatBuffer(102 * 4);
 	}
 
 	public abstract void prepareTransformationForLayer(Layer layer);
 
 	public abstract void resetTransformation();
 
 	protected abstract void prepareScreen();
 
 	protected abstract void drawTriangleStrip(final int vertices,
 			final FloatBuffer vertexBuffer, final FloatBuffer colorBuffer);
 
 	protected abstract void drawLine(final int vertices,
 			final FloatBuffer vertexBuffer, final FloatBuffer colorBuffer);
 
 	protected abstract void drawTriangleFan(final int vertices,
 			final FloatBuffer vertexBuffer, final FloatBuffer colorBuffer);
 
 	public abstract void drawText(final String text, final double x,
 			final double y, final float colR, final float colG,
 			final float colB, final float colA);
 
 	public abstract V2 getTextBounds(final String text);
 
 	public void render(float gameDelta, float graphicsDelta) {
 		graphicsTime.updateTick(graphicsDelta);
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
 		resetTransformation();
 
 		prepareTransformationForLayer(menuLayer);
 		renderMenu();
 		resetTransformation();
 
 		renderStats();
 		renderMouse();
 	}
 
 	protected void renderWorld() {
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		vertexBuffer.put((float) (gameLayer.virtualRegion.x));
 		vertexBuffer.put((float) (gameLayer.virtualRegion.y));
 		colorBuffer.put(new float[] { 0.35f, 0.82f, 0.90f, 1.0f });
 
 		vertexBuffer.put((float) (gameLayer.virtualRegion.x));
 		vertexBuffer.put(0.0f);
 		colorBuffer.put(new float[] { 0.60f, 0.83f, 0.91f, 1.0f });
 
 		vertexBuffer.put(0.0f);
 		vertexBuffer.put((float) (gameLayer.virtualRegion.y));
 		colorBuffer.put(new float[] { 0.34f, 0.81f, 0.89f, 1.0f });
 
 		vertexBuffer.put(0.0f);
 		vertexBuffer.put(0.0f);
 		colorBuffer.put(new float[] { 0.37f, 0.84f, 0.92f, 1.0f });
 
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		drawTriangleStrip(4, vertexBuffer, colorBuffer);
 
 		// Draw the waypoints... top first...
 		assert (vertexBuffer.capacity() >= game.world.waypoints.size() * 2);
 		assert (colorBuffer.capacity() >= game.world.waypoints.size() * 4);
 		for (V2 p : game.world.waypoints) {
 			vertexBuffer.put(p.x + 4.0f);
 			vertexBuffer.put(p.y + 4.0f);
 			colorBuffer.put(new float[] { 0.0f, 0.0f, 0.0f, 1.0f });
 		}
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		drawLine(game.world.waypoints.size(), vertexBuffer, colorBuffer);
 
 		// ...then bottom...
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 		for (V2 p : game.world.waypoints) {
 			vertexBuffer.put(p.x - 4.0f);
 			vertexBuffer.put(p.y - 4.0f);
 			colorBuffer.put(new float[] { 0.0f, 0.0f, 0.0f, 1.0f });
 		}
 
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		drawLine(game.world.waypoints.size(), vertexBuffer, colorBuffer);
 	}
 
 	protected void renderMenu() {
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		vertexBuffer.put((float) (menuLayer.virtualRegion.x));
 		vertexBuffer.put((float) (menuLayer.virtualRegion.y));
 		colorBuffer.put(new float[] { 0.9f, 1.0f, 0.0f, 1.0f });
 
 		vertexBuffer.put((float) (menuLayer.virtualRegion.x));
 		vertexBuffer.put((float) (0));
 		colorBuffer.put(new float[] { 0.9f, 1.0f, 0.0f, 1.0f });
 
 		vertexBuffer.put((float) (0));
 		vertexBuffer.put((float) (menuLayer.virtualRegion.y));
 		colorBuffer.put(new float[] { 0.9f, 1.0f, 0.0f, 1.0f });
 
 		vertexBuffer.put((float) (0));
 		vertexBuffer.put((float) (0));
 		colorBuffer.put(new float[] { 0.9f, 1.0f, 0.0f, 1.0f });
 
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		drawTriangleStrip(4, vertexBuffer, colorBuffer);
 	}
 
 	protected FloatBuffer allocateFloatBuffer(final int entries) {
 		final ByteBuffer byteBuf = ByteBuffer.allocateDirect(entries * 4);
 		byteBuf.order(ByteOrder.nativeOrder());
 		return byteBuf.asFloatBuffer();
 	}
 
 	public void renderEnemy(final Enemy e) {
 		if (e.isDead())
 			return;
 
 		final float pg = 0.01f * (float) e.health;
 		final float pr = 1.0f - pg;
 
 		final double width = 12;
 		final V2 location = e.location;
 
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		vertexBuffer.put((float) (location.x + width / 2));
 		vertexBuffer.put((float) (location.y + width / 2));
 		colorBuffer.put(new float[] { pr * 1.0f, pg * 0.9f, 0.0f, 1.0f });
 
 		vertexBuffer.put((float) (location.x + width / 2));
 		vertexBuffer.put((float) (location.y - width / 2));
 		colorBuffer.put(new float[] { pr * 0.8f, pg * 0.6f, 0.0f, 1.0f });
 
 		vertexBuffer.put((float) (location.x - width / 2));
 		vertexBuffer.put((float) (location.y + width / 2));
 		colorBuffer.put(new float[] { pr * 0.6f, pg * 0.8f, 0.0f, 1.0f });
 
 		vertexBuffer.put((float) (location.x - width / 2));
 		vertexBuffer.put((float) (location.y - width / 2));
 		colorBuffer.put(new float[] { pr * 0.9f, pg * 1.0f, 0.0f, 1.0f });
 
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		drawTriangleStrip(4, vertexBuffer, colorBuffer);
 	}
 
 	public void renderStats() {
 		final String fpsString = String.format("fps %.2f",
 				graphicsTickRater.tickRate);
 		final V2 bounds = getTextBounds(fpsString);
 		final double width = bounds.x + 4;
 		final double height = bounds.y + 2;
 
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		vertexBuffer.put((float) (width));
 		vertexBuffer.put((float) (height));
 		colorBuffer.put(new float[] { 0.1f, 0.1f, 0.1f, 1.0f });
 
 		vertexBuffer.put((float) (width));
 		vertexBuffer.put((float) (0));
 		colorBuffer.put(new float[] { 0.1f, 0.1f, 0.1f, 1.0f });
 
 		vertexBuffer.put((float) (0));
 		vertexBuffer.put((float) (height));
 		colorBuffer.put(new float[] { 0.1f, 0.1f, 0.1f, 1.0f });
 
 		vertexBuffer.put((float) (0));
 		vertexBuffer.put((float) (0));
 		colorBuffer.put(new float[] { 0.1f, 0.1f, 0.1f, 1.0f });
 
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		drawTriangleStrip(4, vertexBuffer, colorBuffer);
 
 		drawText(fpsString, 2, 4, 1.0f, 1.0f, 1.0f, 1.0f);
 	}
 
 	public void renderTower(final Tower t) {
 		final double width = t.radius;
 		final V2 location = t.location;
 
 		if (t.showRange) {
 			drawCircle(t.location.x, t.location.y, t.range, 1.0f, 1.0f, 1.0f,
 					1.0f);
 		}
 
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		vertexBuffer.put((float) (location.x + width / 2));
 		vertexBuffer.put((float) (location.y + width / 2));
 		colorBuffer.put(new float[] { 0.0f, 0.0f, 0.9f, 1.0f });
 
 		vertexBuffer.put((float) (location.x + width / 2));
 		vertexBuffer.put((float) (location.y - width / 2));
 		colorBuffer.put(new float[] { 0.0f, 0.0f, 0.6f, 1.0f });
 
 		vertexBuffer.put((float) (location.x - width / 2));
 		vertexBuffer.put((float) (location.y + width / 2));
 		colorBuffer.put(new float[] { 0.0f, 0.0f, 0.8f, 1.0f });
 
 		vertexBuffer.put((float) (location.x - width / 2));
 		vertexBuffer.put((float) (location.y - width / 2));
 		colorBuffer.put(new float[] { 0.0f, 0.0f, 1.0f, 1.0f });
 
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		drawTriangleStrip(4, vertexBuffer, colorBuffer);
 	}
 
 	public void renderParticle(final Particle p) {
 		if (p.isDead())
 			return;
 
 		final double width = 10;
 		final V2 location = p.location;
 
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		vertexBuffer.put((float) (location.x + width / 2));
 		vertexBuffer.put((float) (location.y + width / 2));
 		colorBuffer.put(new float[] { 0.9f, 0.6f, 0.2f, 1.0f });
 
 		vertexBuffer.put((float) (location.x + width / 2));
 		vertexBuffer.put((float) (location.y - width / 2));
 		colorBuffer.put(new float[] { 0.6f, 0.9f, 0.2f, 1.0f });
 
 		vertexBuffer.put((float) (location.x - width / 2));
 		vertexBuffer.put((float) (location.y + width / 2));
 		colorBuffer.put(new float[] { 0.8f, 1.0f, 0.2f, 1.0f });
 
 		vertexBuffer.put((float) (location.x - width / 2));
 		vertexBuffer.put((float) (location.y - width / 2));
 		colorBuffer.put(new float[] { 1.0f, 0.8f, 0.2f, 1.0f });
 
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		drawTriangleStrip(4, vertexBuffer, colorBuffer);
 	}
 
 	public void renderMouse() {
 		if (!mouse.onScreen)
 			return;
 		final V2 p = mouse.location;
 		final float col = 0.4f + 0.6f * (float) Math.abs(Math
 				.sin(2 * graphicsTime.clock));
		drawFilledCircle(p.x, p.y, col, col, col, 1.0f, mouse.radius);
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
 
 		menuLayer.offset.y = (remainingSize.y - menuLayer.region.y) * .5f;
 		menuLayer.offset.x = gameLayer.offset.x + gameLayer.region.x
 				+ remainingSize.x - menuLayer.region.x;
 	}
 
	public void drawCircle(final float x, final float y, final float colR,
			final float colG, final float colB, final float colA,
			final float radius, final int segments) {
 
 		final double angleStep = 2 * Math.PI / segments;
 		final float[] colors = new float[] { colR, colG, colB, colA };
 
 		assert (vertexBuffer.capacity() >= (segments + 2) * 2);
 		assert (colorBuffer.capacity() >= (segments + 2) * 4);
 
 		for (int i = 0; i < segments; ++i) {
 			final double angle = i * angleStep;
 			vertexBuffer.put((float) (x + (Math.cos(angle) * radius)));
 			vertexBuffer.put((float) (y + (Math.sin(angle) * radius)));
 			colorBuffer.put(colors);
 		}
 		// Close the circle.
 		vertexBuffer.put((float) (x + (Math.cos(angleStep) * radius)));
 		vertexBuffer.put((float) (y + (Math.sin(angleStep) * radius)));
 		colorBuffer.put(colors);
 	}
 
	public void drawCircle(final float x, final float y, final float colR,
			final float colG, final float colB, final float colA,
			final float radius) {
 
 		final int segments = 100;
 
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
		drawCircle(x, y, colR, colG, colB, colA, radius, segments);
 
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		drawLine(101, vertexBuffer, colorBuffer);
 	}
 
 	public void drawFilledCircle(final float x, final float y,
			final float colR, final float colG, final float colB,
			final float colA, final float radius) {
 
 		final int segments = 100;
 
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		assert (vertexBuffer.capacity() >= (segments + 2) * 2);
 		assert (colorBuffer.capacity() >= (segments + 2) * 4);
 
 		vertexBuffer.put((float) x);
 		vertexBuffer.put((float) y);
 		colorBuffer.put(new float[] { colR, colG, colB, colA });
 
		drawCircle(x, y, colR, colG, colB, colA, radius, segments);
 
 		vertexBuffer.position(0);
 		colorBuffer.position(0);
 
 		drawTriangleFan(101, vertexBuffer, colorBuffer);
 	}
 }
