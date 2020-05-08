 package mx.x10.divinescripts.scalegrabber;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 
 import mx.x10.divinescripts.scalegrabber.strategies.Antiban;
 import mx.x10.divinescripts.scalegrabber.strategies.Banking;
 import mx.x10.divinescripts.scalegrabber.strategies.GoInTunnel;
 import mx.x10.divinescripts.scalegrabber.strategies.NavigateShortcut;
 import mx.x10.divinescripts.scalegrabber.strategies.OpenActionBar;
 import mx.x10.divinescripts.scalegrabber.strategies.SafeSpot;
 import mx.x10.divinescripts.scalegrabber.strategies.TakeScales;
 import mx.x10.divinescripts.scalegrabber.strategies.TeleToTaverly;
 import mx.x10.divinescripts.scalegrabber.strategies.WalkToBank;
 import mx.x10.divinescripts.scalegrabber.strategies.WalkToTunnel;
 import mx.x10.divinescripts.scalegrabber.utils.Strategy;
 
 import org.powerbot.core.event.listeners.PaintListener;
 import org.powerbot.core.script.ActiveScript;
 import org.powerbot.game.api.Manifest;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.util.Timer;
 
 
 @Manifest(authors = { "DivineScripts" }, description = "Grabs scales", name = "Scale Grabber")
 public class ScaleGrabber extends ActiveScript implements PaintListener{
 	
 	private static final int SCALE_PRICE = 1100;
 	public static int scalesTotal;
 	private final Timer timer = new Timer(0);
 	private static final NumberFormat NUM_FORMAT = new DecimalFormat("###,###,###");
 	
 	private final Strategy[] strategies = {new OpenActionBar(), new TeleToTaverly(),new WalkToBank(),
 			new Banking(), new WalkToTunnel(), 
 			new GoInTunnel(), new NavigateShortcut(),new SafeSpot(), new Antiban(), 
 			new TakeScales()};
 	private String currentState = "Starting up...";
 	
 	@Override
 	public int loop() {
 			for(Strategy strategy : strategies) {
 				if(strategy.isValid()) {
 					currentState = strategy.getState();
 					strategy.execute();
 				}
 			}
 		return Random.nextInt(300, 500);
 	}
 
 	@Override 
 	public void onRepaint(Graphics g) {
 		final int currentScales = Inventory.getCount(Strategy.SCALE_ID) + scalesTotal;
 		final int profit = currentScales * SCALE_PRICE;
 		final int profitPerHour = (int) (profit * (3600000d / timer.getElapsed()));
		final int scalePerHour = (int) ((scalesTotal + currentScales) * (3600000d / timer.getElapsed()));
 
 		g.setColor(Color.BLACK);
 		g.fillRect(0, 390, 517, 140);
 		g.setColor(Color.LIGHT_GRAY);
 		g.setFont(new Font("Arial", 0, 20));
 		g.drawString("Time elapsed: " + timer.toElapsedString(), 30, 430);
 		g.drawString("$ P/H: " + NUM_FORMAT.format(profitPerHour), 30, 460);
 		g.drawString("State: " + currentState, 30, 490);
 		g.drawString("Scales Collected: " + NUM_FORMAT.format(currentScales), 285, 430);
 		g.drawString("Scales P/H: " + NUM_FORMAT.format(scalePerHour), 285, 460);
 		g.drawString("Profit: " + NUM_FORMAT.format(profit), 285, 490);
 	}
 }
