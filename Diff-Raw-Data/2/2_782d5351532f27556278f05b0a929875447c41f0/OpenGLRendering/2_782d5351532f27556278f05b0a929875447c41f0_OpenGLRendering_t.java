 package rendering;
 
 import game.Game;
 import input.CanvasListener;
 
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import javax.media.nativewindow.util.Point;
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.GLProfile;
 
 import settings.Settings;
 import util.Log;
 
 import com.jogamp.newt.event.WindowAdapter;
 import com.jogamp.newt.event.WindowEvent;
 import com.jogamp.newt.opengl.GLWindow;
 
 public class OpenGLRendering {
 	// private GLCanvas canvas;
 	private static GLWindow window;
 	private static Robot robot;
 	private static Point screenLocation = new Point();
 	private static boolean fullscreen;
 
 	static {
 		try {
 			robot = new Robot();
 		} catch (AWTException e) {
 			e.printStackTrace();
 		}
 		GLProfile.initSingleton();
 	}
 
 	private boolean disposed;
 
 	public OpenGLRendering(final GLEventListener r) {
 
 		GLProfile glp = GLProfile.getDefault();
 		logAvailableImplementations(glp);
 		GLCapabilities caps = new GLCapabilities(glp);
 		caps.setDoubleBuffered(true);
 		/*
 		 * frame = Util.createFrame(); canvas = new GLCanvas(caps);
 		 * canvas.setAutoSwapBufferMode(true); canvas.addGLEventListener(r);
 		 * Container pane = frame.getContentPane(); pane.setLayout(new
 		 * BoxLayout(pane, BoxLayout.X_AXIS)); canvas.setMaximumSize(new
 		 * Dimension( Settings.STEREO ? Settings.WIDTH * 2 : Settings.WIDTH,
 		 * Settings.HEIGHT)); canvas.setMinimumSize(new Dimension(
 		 * Settings.STEREO ? Settings.WIDTH * 2 : Settings.WIDTH,
 		 * Settings.HEIGHT)); // canvas.setPreferredSize(new Dimension(0, 0));
 		 * pane.add(canvas); CanvasListener l = new CanvasListener();
 		 * canvas.addMouseMotionListener(l); canvas.addMouseListener(l);
 		 * canvas.addMouseWheelListener(l); canvas.addKeyListener(l); Util.c =
 		 * canvas; frame.setVisible(true);
 		 */
 		window = GLWindow.create(caps);
 		window.setSize(Settings.STEREO ? Settings.WIDTH * 2 : Settings.WIDTH,
 				Settings.HEIGHT);
 		window.addGLEventListener(r);
 		setFullscreen(Settings.USE_FULL_SCREEN);
 		window.setAlwaysOnTop(true);
 		window.setAutoSwapBufferMode(true);
 		window.setVisible(true);
		window.setPosition(window.getScreen().getWidth() - window.getWidth(), 50);
 		window.setTitle("fungine");
 		CanvasListener c = new CanvasListener();
 		window.addMouseListener(c);
 		window.addKeyListener(c);
 		window.addWindowListener(new WindowAdapter() {
 			public void windowDestroyNotify(WindowEvent arg0) {
 				Game.INSTANCE.exitFlag = true;
 			};
 		});
 	}
 
 	private void logAvailableImplementations(GLProfile glp) {
 		StringBuilder sb = new StringBuilder(
 				"available opengl implementations: ");
 		for (Method m : glp.getClass().getMethods()) {
 			if (m.getName().startsWith("isGL")) {
 				try {
 					if ((Boolean) m.invoke(glp)) {
 						sb.append(m.getName().substring(2));
 						sb.append(", ");
 					}
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (InvocationTargetException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		Log.log(this, sb.toString());
 	}
 
 	public void dispose() {
 		Log.log(this, "disposing");
 		disposed = true;
 		GLProfile.shutdown();
 		Log.log(this, "disposed");
 		window.destroy();
 	}
 
 	public void display() {
 		if (!disposed)
 			window.display();
 	}
 
 	public static void hideMouse(boolean b) {
 		window.setPointerVisible(!b);
 	}
 
 	public static void centerMouse() {
 		screenLocation.setX(0);
 		screenLocation.setY(0);
 		screenLocation = window.getLocationOnScreen(screenLocation);
 		robot.mouseMove(screenLocation.getX() + window.getWidth()
 				/ (Settings.STEREO ? 4 : 2),
 				screenLocation.getY() + window.getHeight() / 2);
 	}
 
 	public static boolean isFullscreen() {
 		return fullscreen;
 	}
 
 	public static void setFullscreen(boolean fullscreenFlag) {
 		fullscreen = fullscreenFlag;
 		window.setFullscreen(fullscreen);
 	}
 
 }
