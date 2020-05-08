 package draynorChopper.main;
 
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import org.powerbuddy.api.input.Mouse;
 import org.powerbuddy.api.methods.Calculations;
 import org.powerbuddy.api.methods.Camera;
 import org.powerbuddy.api.methods.GameObjects;
 import org.powerbuddy.api.methods.Players;
 import org.powerbuddy.api.methods.Skills;
 import org.powerbuddy.api.methods.Widgets;
 import org.powerbuddy.api.util.Random;
 import org.powerbuddy.api.util.Timer;
 import org.powerbuddy.api.wrapper.Area;
 import org.powerbuddy.api.wrapper.Component;
 import org.powerbuddy.api.wrapper.GameObject;
 import org.powerbuddy.api.wrapper.Player;
 import org.powerbuddy.api.wrapper.Tile;
 import org.powerbuddy.bot.event.Paintable;
 import org.powerbuddy.script.Manifest;
 import org.powerbuddy.script.Script;
 
 import draynorChopper.enums.State;
 import draynorChopper.strategies.AntiBan;
//import draynorChopper.strategies.ChangeState;
 import draynorChopper.strategies.CheckInv;
 import draynorChopper.strategies.ChoppTree;
 import draynorChopper.strategies.DepositAll;
 import draynorChopper.strategies.WalkToBank;
 import draynorChopper.strategies.WalkToTrees;
 import draynorChopper.util.Condition;
 import draynorChopper.util.Strategy;
 
 /*
  * Description: Chops Willows near Draynor and then Banks them!
  * Author: Zeejfps.
  * Version: 1.0
  * 
  * Need to add:
  *   Need Fail-safes, in case you die or something.
  *   Make new Closest Object Method untill fixed. ----> 	DONE
  *   More Antiban...
  *   Better Walking!!!!! For sure!	
  *   Update Paint to support XP/H -----> 					DONE
  *   Remove the debug Log ----->						 	DONE	
  */
 @Manifest(author = "Zeejfps", name = "Draynor Willow Chopper")
 public class DraynorChopper extends Script implements Paintable{
 	
 	private final int startExp = Skills.WOODCUTTING.getExperience();
 	private final Timer t = new Timer(0); //Using Not a Girl's method here...
 	
 	private BufferedImage img;
	private final File input = new File("C:/Users/Zeejfps/powerBuddy/eclipsWorkspace/Draynor Willow Chopper/images/paint.png");
 	
 	public static State state  = State.NOT_CHOPPING;
 	public static GameObject tree;	
 	public static LinkedList<Strategy> strats = new LinkedList<Strategy>();
 	public static int logsChopped = 0;
 	public static final int[] treeIds = {5551, 1308, 5552, 5553};
 	
 	public static final Area BANK_AREA = new Area(
 			new Tile(3092, 3247),
 			new Tile(3092, 3240),
 			new Tile(3097, 3240),
 			new Tile(3097, 3246),
 			new Tile(3093, 3246),
 			new Tile(3093, 3247)
 			);
 	
 	public static final Area TREE_AREA = new Area(
 			new Tile(3092, 3225),
 			new Tile(3092, 3231),
 			new Tile(3091, 3236),
 			new Tile(3088, 3239),
 			new Tile(3084, 3240),
 			new Tile(3080, 3239),
 			new Tile(3084, 3235),
 			new Tile(3086, 3231),
 			new Tile(3087, 3227),
 			new Tile(3088, 3225)
 			);
 
 	public static final Tile[] PATH_TO_BANK = { 
 			//new Tile(3087, 3232),  Not sure why this tile is here....
 			new Tile(3085, 3241), 
 			new Tile(3085, 3248), 
 			new Tile(3089, 3248),
 			new Tile(3093, 3242)
 			};
 
 	public static final Tile[] PATH_TO_TREES = { 
 			new Tile(3091, 3248), 
 			new Tile(3085, 3241),
 			new Tile(3087, 3233)
 			};	
 	//Above needs to be replaced with better Walking system!
 	
 //---------------------------------------Main Stuff Below---------------------------------------------
 	
 	@Override
 	public int loop() {
 		
 		for (Strategy strat : strats) {
 			//log("Executing... State: " + state);
 			if (strat.isValid()) {
 				strat.execute();
 			}
 			continue;
 		}
 
 		return 100 + Random.nextInt(100, 400); //I am not sure if Random is necessary...
 	}
 
 	@Override
 	public void onEnd() {
 		log("Thank you for using my script :D");
 	}
 
 	@Override
 	public void onStart() {
 		try {
			img = ImageIO.read(input);
 		} catch (IOException e) {
 			log("No Paint found...");
 		}
 		
 		Camera.setPitch(true);
 		
		
		//createStrat(new ChangeState());
 		createStrat(new CheckInv());
 		createStrat(new ChoppTree());
 		createStrat(new WalkToBank());
 		createStrat(new DepositAll());
 		createStrat(new WalkToTrees());
 		createStrat(new AntiBan());
 		
		
 		log("Thank you for using Draynor Willow Chopper, by: Zeejfps!");
 	}
 	
 	@Override
 	public void paint(Graphics g) {
 		Graphics2D g2d = (Graphics2D)g;
 		
 		//-------------------Paint---------------------
 		
 		if(!isContinueValid()) {
 			g2d.drawImage(img, 7, 344, null); //Paint Image
 			
 			final Font timeFont = new Font(Font.SANS_SERIF, Font.BOLD, 17);
 			g2d.setFont(timeFont);
 			g2d.drawString("" + t.toElapsedString(), 438, 377); //Once again thanks to Not a Girl xD
 			g2d.drawString(calcExp(), 374, 412);
 			g2d.drawString(Integer.toString(logsChopped), 374, 449); //410 is good for xp/h
 		}
 		//------------------End Paint-------------------
 		
 		// ----------------Mouse Drawing ----------------
 		int x1 = (int)(Mouse.getLocation().getX() - 5);
 		int x2 = (int)(Mouse.getLocation().getX() + 5);
 		
 		int y1 = (int)(Mouse.getLocation().getY());
 		int y2 = (int)(Mouse.getLocation().getY());
 
 		g2d.drawLine(x1, y1, x2, y2);
 		
 		x1 = (int)(Mouse.getLocation().getX());
 		x2 = (int)(Mouse.getLocation().getX());
 		
 		y1 = (int)(Mouse.getLocation().getY() - 5);
 		y2 = (int)(Mouse.getLocation().getY() + 5);
 		
 		g2d.drawLine(x1, y1, x2, y2);
 		//---------------End Mouse Drawing----------------		
 	}
 	
 //---------------------------------------Main Stuff Above-----------------------------------------------
 
 //-----------------------------------Extra Static Methods Below-----------------------------------------
 	
 	/*
 	 * Checks if player is in area.
 	 */
 	public static boolean inArea(final Area area) {
 		Player p = Players.getLocal();
 		return area.contains(p.getLocation().getX(), p.getLocation().getY());
 	}
 	
 	/*
 	 * This Method checks if the GameObject passed in still exists.
 	 */
 	public static boolean treeExists() {
 		if (tree != null) {
 			List<GameObject> tempObjs = GameObjects.getLoaded();
 			
 			for (GameObject tempObj : tempObjs) {
 				if ((tempObj.getId() == tree.getId()) && (Calculations.distanceBetween(tempObj.getLocation(), tree.getLocation()) == 0)) {
 					return true;
 				}
 			}
 		} 
 		return false;
 	}
 	
 	/*
 	 * Better version of findClosest
 	 */
 	public static GameObject findClosest(int... ids) {
 		final List<GameObject> allObjs = GameObjects.getLoaded();
 		GameObject closestObj = null;
 		int dist = 99999999;
 		
 		for (GameObject obj : allObjs) {
 			for (int id : ids) {
 				if (id == obj.getId()) {
 					int objDist = Calculations.distanceTo(obj.getLocation());
 					if (objDist < dist) {
 						dist = objDist;
 						closestObj = obj;
 					}
 					break;
 				}
 			}
 		}
 		return closestObj;
 	}
 	
 	/*
 	 * Overides sleep as a Condtional sleep
 	 */
 	public static boolean sleep(Condition c, int timeout) {
         for (int i = 0; i < timeout / 100 && !c.isValid(); i++) {
             sleep(100);
         }
         return c.isValid();
     }
 	
 	private String calcExp() {
 		double expGained = Skills.WOODCUTTING.getExperience() - startExp;
 		double secondsPassed = t.getElapsed() / 1000.0;
 		double xpPerSec = expGained / secondsPassed;
 		double xpPerHour = xpPerSec * 3600;
 		
 		return "" + String.format("%,.0f", xpPerHour);
 	}
 	
 	private boolean isContinueValid() {
 		
 		Component temp = Widgets.getContinue();
 		
         return temp != null && temp.isOnScreen();
     }
 	
 	/*
 	 * This method adds Strategies to the strats List
 	 */
 	private void createStrat(final Strategy strat) {
 		strats.add(strat);
 	}	
 }//END of DraynorChopper CLASS
