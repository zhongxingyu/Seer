 package me.limebyte.endercraftessentials;
 
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class SplashScreen {
 	
 	private SpoutPlayer player;
 	private GenericTexture image;
 	private static final int WIDTH = 328;
 	private static final int HEIGHT = 125;
 	
 	
 	public SplashScreen(final SpoutPlayer player) {
 		this.player = player;
 	}
 	
 	/**
 	 * Attaches the splash screen to the player.
 	 */
 	public void attach(final String splashURL) {
		image = new GenericTexture();
 		image.setAnchor(WidgetAnchor.CENTER_CENTER);
 		image.setWidth(WIDTH).setHeight(HEIGHT);
 		image.setX(-WIDTH / 2).setY(-HEIGHT / 2);
 		image.setUrl(splashURL).setDrawAlphaChannel(true);
 		player.getMainScreen().attachWidget(EndercraftEssentials.getInstance(), image);
 	}
 	
 }
