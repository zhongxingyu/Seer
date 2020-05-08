 package me.limebyte.endercraftessentials;
 
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.gui.InGameHUD;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.gui.WidgetAnim;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class SplashScreen {
 	
 	private SpoutPlayer player;
 	private GenericTexture image;
 	private static final int WIDTH = 328;
 	private static final int HEIGHT = 125;
 	private static final short DURATION = 20;
 	
 	
 	public SplashScreen(final SpoutPlayer player) {
 		this.player = player;
 	}
 	
 	/**
 	 * Attaches the splash screen to the player.
 	 */
 	public final void attach(final String splashURL) {
 		InGameHUD screen = player.getMainScreen();
 		
 		image = new GenericTexture();
 		image.setAnchor(WidgetAnchor.CENTER_LEFT);
 		image.setWidth(WIDTH).setHeight(HEIGHT);
 		image.setX(-WIDTH).setY(-HEIGHT / 2);
 		image.setUrl(splashURL).setDrawAlphaChannel(true);
 		
 		screen.attachWidget(EndercraftEssentials.getInstance(), image);
		screen.animate(WidgetAnim.POS_X, screen.getWidth() / 2 - WIDTH / 2, DURATION, (short) 1, false, false);
 		screen.animateStart();
 	}
 	
 }
