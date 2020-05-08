 package com.gmail.ellendar.scrollsai;
 
 import java.awt.AWTException;
 import java.awt.GraphicsEnvironment;
 import java.awt.Robot;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.gmail.ellendar.ImpossibleException;
 import com.gmail.ellendar.scrollsai.sacrifice.Sacrifice;
 import com.gmail.ellendar.scrollsai.scroll.Scroll;
 import com.gmail.ellendar.scrollsai.ui.PausePoint;
 
 public class Control {
 	
 	public static final Robot robot;
 	
 	private static final int CARD_CLICK_Y = 760;
 	private static final int SACRIFICE_Y = 640;
 	private static final int RESOURCE_OFFSET_X = -30;
 	private static final int DRAW_OFFSET_X = 30;
 	
 	private static final Point CLEAR_SCROLL_SELECTION = new Point(1015, 250);
 	
 	private static final int UNTARGETED_SCROLL_CAST_Y = 563;
 	
 	private static final Point[][] ourGridControlPoints = {
 		{new Point(425,250), new Point(356,322),new Point(400,395),new Point(328,475),new Point(380,560)},
 		{new Point(540, 250), new Point(470, 320), new Point(525, 392), new Point(450, 472), new Point(509, 562)},
 		{new Point(654, 250), new Point(590, 319), new Point(650, 390), new Point(580, 475), new Point(640, 559)}
 	};
 	
 	@SuppressWarnings("serial")
 	public static final Map<Integer, List<Integer>> handXCoordinatesByHandSize = new HashMap<Integer, List<Integer>>() {{
 		put(1, Arrays.asList(725));
 		put(2, Arrays.asList(703, 808));
 		put(3, Arrays.asList(557, 724, 892));
 		put(4, Arrays.asList(473, 640, 807, 975));
 		put(5, Arrays.asList(391, 558, 723, 891, 1057));
 		put(6, Arrays.asList(391, 524, 658, 791, 924, 1057));
 		put(7, Arrays.asList(391, 501, 613, 724, 835, 945, 1057));	
 		put(8, Arrays.asList(391, 486, 581, 677, 772, 867, 962, 1057));	
 	}};
 	
 	static {
 		try {
 			//this is the magic number to get the correct monitor
 			robot = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0]);
 		} catch (AWTException e) {
 			throw new RuntimeException();
 		}
 	}
 	
 	private static final Logger logger = LoggerFactory.getLogger(Control.class);
 	
 	public static void doSacrifice(GameState state, Sacrifice sacrifice) {
 		logger.info("Sacrificing scroll: " + sacrifice.getScroll() + "(" + sacrifice.getType() + ")");
 		
 		clickScroll(state, sacrifice.getScroll());
		wait(1000);
 		
 		int x = handXCoordinatesByHandSize.get(state.getHand().size()).get(state.getHand().indexOf(sacrifice.getScroll()));
 		
 		switch(sacrifice.getType()) {
 			case DRAW:
 				click(state, x + DRAW_OFFSET_X, SACRIFICE_Y);
 				break;
 			case RAMP:
 				click(state, x + RESOURCE_OFFSET_X, SACRIFICE_Y);
 				logger.info("Sacrificed for resourcees. Mana is now at " + state.getMaxMana());
 				break;
 			case SKIP:
 				return;
 			default:
 				throw new ImpossibleException("Invalid sacrifice type.");
 				
 		}
		wait(200);
 	}
 	
 	public static void clickOurGrid(GameState state, int x, int y) {
 		logger.info("Clicking our grid: (" + x + "," + y + ")" );
 		Point clickPoint = ourGridControlPoints[x][y];
 		
 		click(state, clickPoint.x, clickPoint.y);
 	}
 	
 	public static void clearScrollSelection(GameState state) {
 		click(state, CLEAR_SCROLL_SELECTION);
 	}
 	
 	public static void clickScroll(GameState state, Scroll scroll) {
 		if(!state.getHand().contains(scroll)) {
 			throw new GameStateViolationException("Hand does not contain scroll" + scroll);
 		}
 		logger.info("Clicking scroll " + scroll + " at index " + state.getHand().indexOf(scroll));
 		int x = handXCoordinatesByHandSize.get(state.getHand().size()).get(state.getHand().indexOf(scroll));
 		click(state, x, CARD_CLICK_Y);
 	}
 	
 	public static void castUntargetedScroll(GameState state, Scroll scroll) {
 		if(!state.getHand().contains(scroll)) {
 			throw new GameStateViolationException("Hand does not contain scroll" + scroll);
 		}
 		int x = handXCoordinatesByHandSize.get(state.getHand().size()).get(state.getHand().indexOf(scroll));
 		click(state, x, UNTARGETED_SCROLL_CAST_Y);
 	}
 	
 	public static void click(GameState state, int x, int y) {
 		if(state.getScreenOffset() == null) {
 			state.update();
 		}
 		click(state.getScreenOffset().x + x, state.getScreenOffset().y + y);
 	}
 	
 	public static void click(Point point) {
 		click(point.x, point.y);
 	}
 	
 	public static void click(GameState state, Point point) {
 		click(state.getScreenOffset().x + point.x, state.getScreenOffset().y + point.y);
 	}
 	
 	// all debug waits
 	public static void click(int x, int y) {
 		try {
 			PausePoint.check();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		logger.info("Clicking absolute coordinate: (" + x + "," + y + ")" );
 		robot.mouseMove(x, y);
		//debugging wait, so we can see where the mouse is moving
		wait(100);
 		robot.mousePress(InputEvent.BUTTON1_MASK);
 		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		//some clicks won't read properly without a small delay before moving the mouse off.
		//removing this wait WILL break the bot
		wait(50);
 		robot.mouseMove(0, 0);
 	}
 	
 	public static void wait(int duration) {
 		try {
 			Thread.sleep(duration);
 		} catch (InterruptedException e) {
 			logger.error(e.getMessage(), e);
 		}
 	}
 	
 	public static void endTurn(GameState state) {
 		click(state, ScreenParser.ourTurnCriticalPixel);
 	}
 	
 	public static void toggleHealthDisplay() {
 		robot.keyPress(KeyEvent.VK_CONTROL);
 		robot.keyRelease(KeyEvent.VK_CONTROL);
 	}
 	
 	// load bearing waits
 	public static void startGame(GameState state) {
 		click(state, 540, 370);
 		wait(200);
 		click(state, 725, 400);
 		wait(500);
 		click(state, 525, 360);
 		wait(10000); //could be replaced with detection of main menu
 	}
 	
 	public static void leaveMatch(GameState state) {
 		click(state, 700, 830);
 		wait(5000);
 	}
 	
 	// load bearing waits
 	public static void surrender(GameState state) {
 		click(state, 5, 5);
 		wait(500);
 		click(state, 720, 508);
 		wait(500);
 		click(state, 718, 315);
 		wait(5000);
 		leaveMatch(state);
 		wait(10000); //could be replaced with detection of main menu
 	}
 }
