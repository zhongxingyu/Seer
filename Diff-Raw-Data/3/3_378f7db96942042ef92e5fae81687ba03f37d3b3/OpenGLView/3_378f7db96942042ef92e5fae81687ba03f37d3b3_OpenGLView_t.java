 package riskyspace.view.opengl.impl;
 
 import java.awt.Cursor;
 import java.awt.GraphicsEnvironment;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.List;
 import java.util.Map;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.GLProfile;
 import javax.media.opengl.awt.GLCanvas;
 import javax.swing.JFrame;
 
 import riskyspace.Main;
 import riskyspace.logic.SpriteMapData;
 import riskyspace.model.BuildAble;
 import riskyspace.model.Colony;
 import riskyspace.model.Fleet;
 import riskyspace.model.Player;
 import riskyspace.model.PlayerStats;
 import riskyspace.model.Territory;
 import riskyspace.services.Event;
 import riskyspace.services.EventBus;
 import riskyspace.view.View;
 
 import com.jogamp.opengl.util.FPSAnimator;
 
 public class OpenGLView implements View, GLEventListener {
 	
 	private GLRenderArea renderArea = null;
 	private JFrame frame = null;
 	
 	public OpenGLView (int rows, int cols) {
 		System.setProperty("sun.java2d.noddraw", "true");
 		System.setProperty("sun.awt.noerasebackground", "true");
 		
 		GLProfile glProfile = GLProfile.getDefault();
 		GLCapabilities glCapabilities = new GLCapabilities(glProfile);
 		GLCanvas canvas = new GLCanvas(glCapabilities);
 		
 		int width = Toolkit.getDefaultToolkit().getScreenSize().width;
 		int height = Toolkit.getDefaultToolkit().getScreenSize().height;
 		
 		renderArea = new GLRenderArea(width, height, rows, cols);
 		frame = Main.getFrame();
 		if (Toolkit.getDefaultToolkit().getMaximumCursorColors() > 0) {
 			Image cursor = Toolkit.getDefaultToolkit().getImage("res/blue_cursor.png");
 			Cursor c = Toolkit.getDefaultToolkit().createCustomCursor(cursor, new Point(0,0), "main");
 			frame.setCursor(c);
 		}
 		canvas.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent event) {
 				if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
 					System.exit(0);
 				} else if (event.getKeyCode() == KeyEvent.VK_SPACE) {
 					Event evt = new Event(Event.EventTag.MOVE, null);
 					EventBus.CLIENT.publish(evt);
 				} else if (event.getKeyCode() == KeyEvent.VK_ENTER) {
 					Event evt = new Event(Event.EventTag.NEXT_TURN, null);
 					EventBus.CLIENT.publish(evt);
 				}
 			}
 			@Override public void keyReleased(KeyEvent arg0) {}
 			@Override public void keyTyped(KeyEvent arg0) {}
 		});
 		canvas.addKeyListener(renderArea.getCameraKeyListener());
 		canvas.addMouseListener(renderArea.getClickHandler());
 		canvas.addGLEventListener(this);
 		
 		frame.add(canvas);
 		frame.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
 		frame.setIgnoreRepaint(true);
 		if (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported()) {
 			if (!System.getProperty("os.name").contains("Windows")) {
 				GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
 			} else {
 				GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
 				frame.setAlwaysOnTop(true);
 				frame.setResizable(false);
 				frame.pack();
 				frame.setVisible(true);
 			}
 		} else {
 			System.err.println("Fullscreen not supported");
 		}
 		
 		canvas.requestFocusInWindow();
 		
 		FPSAnimator anim = new FPSAnimator(canvas, 60);
 		anim.start();
 	}
 	
 	//*****GLEventListener Methods*****
 	@Override
 	public void display(GLAutoDrawable drawable) {
 //		long b = System.currentTimeMillis();
 //		if (sec == 0 || b -sec > 1000) {
 //			System.out.println("FPS: " + times);
 //			times = 0;
 //			sec = b;
 //		}
 //		times++;
 		
 		GL2 gl = drawable.getGL().getGL2();
 		drawable.getGL().glClearColor(0, 0, 0, 1f);
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
 		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
 		gl.glClear(GL2.GL_ALPHA_BITS);
 		
 		renderArea.draw(drawable, renderArea.getBounds(), renderArea.getBounds(), 0);
 	}
 
 	long sec;
 	int times;
 	
 	@Override
 	public void init(GLAutoDrawable drawable) {
 		GL2 gl = drawable.getGL().getGL2();
 		gl.setSwapInterval(1); // Enable VSync
 		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
 		gl.glEnable(GL.GL_TEXTURE_2D);
 		gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
 		gl.glEnable(GL.GL_DEPTH_TEST);
 		gl.glDepthFunc(GL.GL_LESS);
 		gl.glEnable(GL2.GL_ALPHA_TEST);
 		gl.glEnable (GL2.GL_BLEND);
 		gl.glBlendFunc (GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
 		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
 		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
 		gl.glAlphaFunc(GL2.GL_GREATER, 0.10f);
 	}
 
 	@Override
 	public void dispose(GLAutoDrawable drawable) {
 	}
 
 	@Override
 	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
 		renderArea.updateSize(width, height);
 	}
 	
 	//*****View Methods*****
 	@Override
 	public void draw() {
 		
 	}
 
 	@Override
 	public void setViewer(Player player) {
 		renderArea.setViewer(player);
 	}
 
 	@Override
 	public void setActivePlayer(Player player) {
 		renderArea.setActivePlayer(player);
 	}
 
 	@Override
 	public void setVisible(boolean visible) {
 		frame.setVisible(visible);
 	}
 
 	@Override
 	public boolean isVisible() {
 		return frame.isVisible();
 	}
 
 	@Override
 	public void updateData(SpriteMapData data) {
 		renderArea.updateData(data);
 	}
 
 	@Override
 	public void setPlayerStats(PlayerStats stats) {
 		renderArea.setStats(stats);
 	}
 
 	@Override
 	public void setQueue(Map<Colony, List<BuildAble>> colonyQueues) {
 		renderArea.setQueue(colonyQueues);
 	}
 
 	@Override
 	public void showPlanet(Territory selection) {
 		renderArea.showTerritory(selection);
 	}
 
 	@Override
 	public void showColony(Colony selection) {
 		renderArea.showColony(selection);
 	}
 
 	@Override
 	public void showFleet(Fleet selection) {
 		renderArea.showFleet(selection);
 	}
 
 	@Override
 	public void hideMenus() {
 		renderArea.hideSideMenus();
 	}
 
 	@Override
 	public void showGameOver(Player loser) {
 		renderArea.showGameOver(loser);
 	}
 	
 	@Override
 	public void showWinnerScreen() {
 		renderArea.showWinnerScreen();
 	}
}
