 import java.util.TimerTask;
 import java.awt.*;
 import java.awt.geom.AffineTransform;
 import java.awt.event.*;
 
 public class FunkyPainter extends TimerTask  {
 
 	private Graphics2D graphics;
 	private Graphics gfx;
 	private FunkyAnimator Animator;
 	private FunkyScene Scene;
 	private FunkyApplet Applet;
 	private Image offscreenImage;
 	private AffineTransform identity = new AffineTransform();
 
 	public FunkyPainter(Graphics gfx, FunkyAnimator animator, FunkyScene scene, Image img, FunkyApplet applet) {
 		this.Animator = animator;
 		this.Scene = scene;
 		this.graphics = (Graphics2D) gfx;
 		this.offscreenImage = img;
 		this.gfx = this.offscreenImage.getGraphics();
 		this.Applet = applet;
 		initialDraw();
 	}
 	private void initialDraw() {
 		graphics.setColor(Color.black);
 		graphics.fillRect(0, 0, Scene.width, Scene.height);
 	}
 
 	public void run() { Animator.tick();
 		this.paint();
 	}
 
 	private void paint() {
 		int i;
		gfx.clearRect(0, 0, Scene.width, Scene.height);
 
 		if (Scene.objectsNumber > 0)
 			for (i = 0; i < Scene.objectsNumber; i++)
 				if (Scene.objects[i] != null)
 					Scene.objects[i].draw(gfx);
 
 		AffineTransform trans = new AffineTransform();
 		trans.setTransform(identity);
 		// trans.rotate(Math.atan((Scene.mouseOffsets.y + 1) / (Scene.mouseOffsets.x + 1)), Scene.width / 2, Scene.height / 2);
 
		graphics.clearRect(0, 0, Scene.width, Scene.height);
 		graphics.setTransform(trans);
 		graphics.drawImage(offscreenImage, 0, 0, Applet);
 	}
 }
