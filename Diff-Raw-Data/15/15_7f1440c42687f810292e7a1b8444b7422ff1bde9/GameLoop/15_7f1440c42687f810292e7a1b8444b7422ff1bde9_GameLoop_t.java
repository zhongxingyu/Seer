 package game;
 
 import manager.SoundManager;
 import rendering.OpenGLRendering;
 import rendering.RenderUpdater;
 import util.Log;
 import util.RepeatedRunnable;
 import util.TickCounter;
 
 public class GameLoop extends RepeatedRunnable {
 	public static int TICKS_PER_SECOND = 60;
 	private int SKIP_TICKS;
 	private final int MAX_FRAMESKIP = 5;
 	private final int MAX_FPS = 200;
 	public RenderUpdater renderer;
 	public GameMechanics mechanics;
 	public SoundManager sound;
 	private long nextTick;
 	private int loops;
 	private float interpolation;
 	public TickCounter currentFPS;
 	public TickCounter currentTick;
 	public long renderTick = 0;
 	public int tick = 0;
 	private boolean pauseLogic;
 	public long timePerTick;
 	public long timePerRender;
 
 	public GameLoop() {
 		super("GameLoopThread");
 		setFPS(TICKS_PER_SECOND);
 		currentFPS = new TickCounter();
 		currentTick = new TickCounter();
 	}
 
 	public void setFPS(int fps) {
 		fps = Math.max(fps, 1);
 		TICKS_PER_SECOND = fps;
 		SKIP_TICKS = 1000000000 / TICKS_PER_SECOND;
 	}
 
 	public void start() {
 		nextTick = System.nanoTime();
 		loops = 0;
 		interpolation = 0;
 		super.start();
 	}
 
 	@Override
 	public void startPause() {
 		super.startPause();
 		if (mechanics != null)
 			mechanics.restart();
 	}
 
 	@Override
 	protected void executeRepeatedly() {
 		loops = 0;
 		if (Game.INSTANCE.exitFlag) {
 			Game.INSTANCE.exit();
 			return;
 		}
		if (Game.INSTANCE.fullscreenFlag != OpenGLRendering.isFullscreen()) {
			OpenGLRendering.setFullscreen(Game.INSTANCE.fullscreenFlag);
 		}
 		while (!pauseLogic && System.nanoTime() > nextTick
 				&& loops < MAX_FRAMESKIP) {
 			// Log.log(this, currentFPS.fps);
 			long timePerTickStart = System.currentTimeMillis();
 			if (mechanics != null)
 				mechanics.update(0);
 			if (sound != null)
 				sound.update(0);
 			timePerTick = System.currentTimeMillis() - timePerTickStart;
 			nextTick += SKIP_TICKS;
 			tick++;
 			currentTick.tick();
 			loops++;
 			// renderTick = 0;
 		}
 		if (renderer != null) {
 			/*
 			 * Log.log(this, Threading.isSingleThreaded(),
 			 * Threading.isOpenGLThread(), ThreadingImpl.getMode(),
 			 * ThreadingImpl.isOpenGLThread(), ThreadingImpl.isSingleThreaded(),
 			 * ThreadingImpl.isToolkitThread(), ThreadingImpl.isX11());
 			 */
 			// if (renderloops < (float) MAX_FPS / TICKS_PER_SECOND) {
 			currentFPS.tick();
 			if (pauseLogic)
 				interpolation = 0;
 			else
 				interpolation = (float) (System.nanoTime() + SKIP_TICKS - nextTick)
 						/ (float) (SKIP_TICKS);
 			long timePerRenderStart = System.currentTimeMillis();
 			renderer.update(interpolation);
 			timePerRender = System.currentTimeMillis() - timePerRenderStart;
 			renderTick++;
 			/*
 			 * } else { Thread.yield(); Util.sleep(1); }
 			 */
 		}
 	}
 
 	public void exit() {
 		Log.log(this, "exit");
 		if (mechanics != null)
 			mechanics.dispose();
 		mechanics = null;
 		if (renderer != null)
 			renderer.dispose();
 		renderer = null;
 		if (sound != null)
 			sound.dispose();
 		sound = null;
 	}
 
 	public void pauseLogic() {
 		pauseLogic = true;
 	}
 
 	public boolean isPaused() {
 		return pauseLogic;
 	}
 
 	public void continueLogic() {
 		pauseLogic = false;
 		nextTick = System.nanoTime();
 	}
 
 }
