 package javachallenge.graphics.util;
 
 import javachallenge.graphics.components.Sprite;
 
 import javax.swing.ImageIcon;
 
 @SuppressWarnings("serial")
 public class AnimatedImage extends Sprite {
 	protected ImageIcon[] icons;
 	protected int delay;
	private boolean stop = false;
 	
 	public AnimatedImage(ImageIcon[] icons, int delay, Position position) {
 		super(icons[0], position);
 		this.icons = icons;
 		this.delay = delay;
 
		Thread showThread = new Thread("AnimatedImage") {
 			public void run() {
 				int index = 0;
				while (!stop) {
 					AnimatedImage.this.setIcon(AnimatedImage.this.icons[index]);
 					index = (index  + 1) % AnimatedImage.this.icons.length;
 					try { sleep(AnimatedImage.this.delay); } catch (InterruptedException e) {}
 				}
 			}
		};
		showThread.start();
	}
	
	public void destroy() {
		stop = true;
 	}
 }
