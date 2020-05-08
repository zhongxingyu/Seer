 import java.util.Timer;
 import java.util.TimerTask;
 import java.applet.Applet;
 import java.awt.*;
 import java.awt.event.*;
 
 @SuppressWarnings("serial") public class FunkyApplet extends Applet {
 	private static final int FRAME_TIME = 1000 / 60;
 	private Timer timer;
 	private FunkyScene Scene;
 	private FunkyPainter Painter;
 	private FunkyAnimator Animator;
 	private FunkyHandler Handler;
 
 	public void start() {
 		final Graphics graphics = getGraphics();
		Image img = createImage(getWidth(), getHeight());
 		Scene = new FunkyScene();
 		Animator = new FunkyAnimator(Scene);
 		Handler = new FunkyHandler(Scene);
 		Painter = new FunkyPainter(graphics, Animator, Scene, img, this);
 		timer = new Timer(true);
 		setup();
 		timer.scheduleAtFixedRate(Painter, 0, FRAME_TIME);
 		addMouseListener(Handler);
 	}
 
 	public void stop() {
 		timer.cancel();
 		addMouseListener(Handler);
 	}
 
 	public void setup() {
 		Scene.width = getWidth();
 		Scene.height = getHeight();
 		Scene.addObject();
 	}
 
 }
