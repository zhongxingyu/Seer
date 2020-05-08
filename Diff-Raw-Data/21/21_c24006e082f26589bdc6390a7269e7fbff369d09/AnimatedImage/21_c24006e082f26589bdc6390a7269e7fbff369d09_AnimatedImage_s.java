 package javachallenge.graphics.util;
 
 import javachallenge.graphics.components.Sprite;
 
 import javax.swing.ImageIcon;
 
 @SuppressWarnings("serial")
 public class AnimatedImage extends Sprite {
 	protected ImageIcon[] icons;
 	protected int delay;
 	
 	public AnimatedImage(ImageIcon[] icons, int delay, Position position) {
 		super(icons[0], position);
 		this.icons = icons;
 		this.delay = delay;
 
		new Thread() {
 			public void run() {
 				int index = 0;
				while (true) {
 					AnimatedImage.this.setIcon(AnimatedImage.this.icons[index]);
 					index = (index  + 1) % AnimatedImage.this.icons.length;
 					try { sleep(AnimatedImage.this.delay); } catch (InterruptedException e) {}
 				}
 			}
		}.start();
 	}
 }
