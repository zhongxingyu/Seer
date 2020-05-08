 package de.yogularm;
 
 import java.awt.Font;
 import java.awt.event.KeyListener;
 import java.util.Random;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.glu.GLU;
 
 import com.jogamp.opengl.util.FPSAnimator;
 import com.jogamp.opengl.util.awt.TextRenderer;
 
 public class Game implements GLEventListener {
 	private World world;
 	private Input input = new Input();
 	private long lastFrameTime;
 	private long frameCount = 0;
 	private float frameTime = 0;
 	private Vector viewSize = Vector.getZero();
 	private int width;
 	private int height;
 	private boolean isGameover = false;
 	private float gameoverTime = 0;
 	private FPSAnimator animator;
 	private ExceptionHandler exceptionHandler;
 
	public static final String VERSION = "0.1.5";
 
 	public void init(GLAutoDrawable drawable) {
 		try {
 			GL2 gl = drawable.getGL().getGL2();
 	
 			gl.glDisable(GL.GL_DEPTH_TEST);
 			gl.glDisable(GL.GL_CULL_FACE);
 			gl.glEnable(GL.GL_TEXTURE_2D);
 			gl.glClearColor(0.8f, 0.8f, 1, 1);
 			gl.glEnable(GL.GL_BLEND);
 			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
 			OpenGLHelper.checkErrors(gl);
 	
 			Res.init();
 
 			restart();
 		} catch (Exception e) {
 			if (exceptionHandler != null)
 				exceptionHandler.handleException(e);
 		}
 	}
 
 	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
 			int height) {
 		try {
 			GL2 gl = drawable.getGL().getGL2();
 			GLU glu = new GLU();
 	
 			// limit to maximum block counts in each direction
 			float resolution = Math.max((float) width / Config.MAX_VIEW_WIDTH,
 					(float) height / Config.MAX_VIEW_HEIGHT);
 			//resolution = Math.max(resolution, Config.MIN_RESOLUTION);
 			float w = width / resolution;
 			float h = height / resolution;
 	
 			gl.glMatrixMode(GL2.GL_PROJECTION);
 			gl.glLoadIdentity();
 	
 			// coordinate system origin at lower left with width and height same as the
 			// window
 			glu.gluOrtho2D(0.0f, w, 0.0f, h);
 	
 			gl.glMatrixMode(GL2.GL_MODELVIEW);
 			gl.glLoadIdentity();
 	
 			gl.glViewport(0, 0, width, height);
 	
 			viewSize = new Vector(w, h);
 			world.getCamera().setBounds(
 					world.getCamera().getBounds().changeSize(viewSize));
 	
 			OpenGLHelper.checkErrors(gl);
 	
 			this.width = width;
 			this.height = height;
 		} catch (Exception e) {
 			if (exceptionHandler != null)
 				exceptionHandler.handleException(e);
 		}
 	}
 
 	public void dispose(GLAutoDrawable drawable) {
 
 	}
 
 	public void display(GLAutoDrawable drawable) {
 		try {
 			GL2 gl = drawable.getGL().getGL2();
 	
 			captureFrameTime();
 	
 			if (!isGameover)
 				update();
 			else {
 				gameoverTime -= frameTime;
 				if (gameoverTime < 0)
 					restart();
 			}
 	
 			render(gl);
 			renderGUI(gl);
 			renderText(gl);
 	
 			// run gc every minute
 			if (frameCount % (60 * 60) == 0)
 				System.gc();
 			frameCount++;
 		} catch (Exception e) {
 			if (exceptionHandler != null)
 				exceptionHandler.handleException(e);
 		}
 	}
 
 	public void start(GLAutoDrawable drawable) {
 		try {
 			animator = new FPSAnimator(drawable, 60);
 			animator.add(drawable);
 			animator.start();
 		} catch (Exception e) {
 			if (exceptionHandler != null)
 				exceptionHandler.handleException(e);
 		}
 	}
 
 	public void stop() {
 		try {
 			animator.stop();
 		} catch (Exception e) {
 			if (exceptionHandler != null)
 				exceptionHandler.handleException(e);
 		}
 	}
 
 	public KeyListener getKeyListener() {
 		return input.getKeyListener();
 	}
 
 	private void update() {
 		world.update(frameTime);
 		input.affect(world, frameTime);
 		if (world.getPlayer().isDead()) {
 			gameoverTime = Config.GAMEOVER_LENGTH;
 			isGameover = true;
 		}
 	}
 
 	private void render(GL2 gl) {
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
 		OpenGLHelper.checkErrors(gl);
 
 		world.render(gl);
 	}
 
 	private void renderGUI(GL2 gl) {
 		gl.glMatrixMode(GL2.GL_PROJECTION);
 		gl.glPushMatrix();
 		gl.glLoadIdentity();
 
 		gl.glMatrixMode(GL2.GL_MODELVIEW);
 		gl.glPushMatrix();
 		gl.glLoadIdentity();
 
 		GLU glu = new GLU();
 		glu.gluOrtho2D(0.0f, width, 0.0f, height);
 
 		if (isGameover) {
 			gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
 			gl.glColor4f(0, 0, 0, 0.5f);
 			OpenGLHelper.renderRect(gl, width, height);
 		}
 
 		RenderTransformation.draw(gl, Res.images.coin, 20, height - 70, 50, 50);
 		RenderTransformation.draw(gl, Res.images.heart, 20, height - 140, 50, 50);
 
 		gl.glMatrixMode(GL2.GL_PROJECTION);
 		gl.glPopMatrix();
 		gl.glMatrixMode(GL2.GL_MODELVIEW);
 		gl.glPopMatrix();
 	}
 
 	private void renderText(GL2 gl) {
 		TextRenderer renderer = new TextRenderer(new Font("Verdana", Font.BOLD
 				| Font.ITALIC, 40));
 		try {
 			renderer.beginRendering(width, height);
 			renderer.setColor(0, 0, 0, 0.8f);
 			renderer
 					.draw("" + world.getPlayer().getCollectedCoins(), 80, height - 60);
 
 			int life = Math.max(0, Math.round(world.getPlayer().getLife() - 1));
 			renderer.draw("" + life, 80, height - 130);
 
 			if (isGameover)
 				renderer.draw("GAME OVER", width / 2 - 130, height / 2 - 20);
 		} finally {
 			renderer.endRendering();
 			renderer.dispose();
 		}
 
 		renderer = new TextRenderer(new Font("Verdana", Font.BOLD, 12));
 		try {
 			renderer.beginRendering(width, height);
 			renderer.setColor(0, 0, 0, 1);
 			renderer.draw("Yogularm Infinite " + VERSION, width - 180, height - 20);
 		} finally {
 			renderer.endRendering();
 			renderer.dispose();
 		}
 	}
 
 	private void captureFrameTime() {
 		long newTime = System.nanoTime();
 		if (lastFrameTime != 0)
 			frameTime = (newTime - lastFrameTime) / 1000000000.0f; // ns to s
 		lastFrameTime = newTime;
 		System.out.println((1 / frameTime) + " FPS");
 		frameTime = Math.min(frameTime, Config.MAX_FRAMETIME);
 	}
 
 	private void restart() {
 		world = new World(new Random().nextInt());
 		world.getCamera().setBounds(
 				world.getCamera().getBounds().changeSize(viewSize));
 		gameoverTime = 0;
 		isGameover = false;
 	}
 
 	public void setExceptionHandler(ExceptionHandler handler) {
 		this.exceptionHandler = handler;
 	}
 }
