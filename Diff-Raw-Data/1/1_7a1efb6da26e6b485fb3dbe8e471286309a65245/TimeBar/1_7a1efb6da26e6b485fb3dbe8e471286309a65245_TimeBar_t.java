 package ui;
 
 import org.jbox2d.common.Vec2;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.fills.GradientFill;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 
 import time.Time;
 import time.Timer;
 import config.Config;
 
 public class TimeBar {
 	private GradientFill timeFill;
 	private GradientFill salmonTimeFill;
 	private GradientFill currentFill;
 	private float salmonTimer = 0;
 	private Rectangle timeShape;
 	private final Vec2 timePos = new Vec2(20,20);
 	private final float timeHeight = 30;
 	private float timeWidth;
 	private float timeDivide;
 	private static UnicodeFont goalFont;
 	private static UnicodeFont currentFont;
 	
 	
 	static {
 		
 	}
 
 	public TimeBar(GameContainer gc, UnicodeFont goalFont, UnicodeFont currentFont) {
 		timeWidth = Config.RESOLUTION_WIDTH - 2*timePos.x;
 		timeShape = new Rectangle(timePos.x, timePos.y, 0, timeHeight);
 		timeFill = new GradientFill(timePos.x, timePos.y, new Color(32, 131, 153, 100), (Config.RESOLUTION_WIDTH - timePos.x)/4, timePos.y,
                 new Color(255, 217, 235, 100), true);
 		TimeBar.goalFont = goalFont;
 		TimeBar.currentFont = currentFont;
 		salmonTimeFill = new GradientFill(timePos.x, timePos.y, new Color(255, 189, 180, 100), (Config.RESOLUTION_WIDTH - timePos.x)/4, timePos.y,
                 new Color(205, 134, 134, 100), true);
 		currentFill = timeFill;
 	}
 
 	public void render(GameContainer gc, Graphics graphics, Timer timer, boolean timerGo) {
 		graphics.setColor(Color.white);
 		
 		String goalStr = getTimeString(timer.getGoal());
 		graphics.fillRect(Config.RESOLUTION_WIDTH/2 - 1, timePos.y+timeHeight, 1, 20);
 		goalFont.drawString(Config.RESOLUTION_WIDTH/2 - goalFont.getWidth("Goal")/2, timeHeight + timePos.y + 20, "Goal");
 		goalFont.drawString(Config.RESOLUTION_WIDTH/2 - goalFont.getWidth(goalStr)/2, goalFont.getHeight("Goal")+timeHeight + timePos.y + 20, goalStr);
 		
 		if (timer.getBestTime().getMillis() < Integer.MAX_VALUE) {
 			String bestStr = getTimeString(timer.getBestTime());
 			float bestOffset = Math.max(Math.min(timer.getBestTime().getMillis()/timeDivide, timeWidth),0) + timePos.x +1;
 			graphics.fillRect(bestOffset, timePos.y+timeHeight, 1, 20);
 			goalFont.drawString(bestOffset - goalFont.getWidth("Best")/2, timeHeight + timePos.y + 20, "Best");
 			goalFont.drawString(bestOffset - goalFont.getWidth(bestStr)/2, goalFont.getHeight("Best")+timeHeight + timePos.y + 20, bestStr);
 		}
 		
 		graphics.setColor(new Color(0,0,0,100));
 		graphics.fillRect(timePos.x, timePos.y, timeWidth, timeHeight);
 		timeShape.setWidth(Math.max(Math.min(timer.getCurrentTime().getMillis()/timeDivide, timeWidth),0));
 		graphics.fill(timeShape, currentFill);
 		
 		String currentStr = "Time: "+getTimeString(timer.getCurrentTime());
 
 		if (!timerGo) {
 			currentStr = " Ready!";
 		}
 		currentFont.drawString(timePos.x + 10, (timeHeight - currentFont.getHeight("Current"))/2+timePos.y, currentStr);
 		
 	}
 	
 	public void enter(GameContainer gc, StateBasedGame game, Timer timer) {
 		timeDivide = 2*timer.getGoal().getMillis()/timeWidth;
 	}
 	
 	public void update(GameContainer gc, StateBasedGame game, int delta) {
 		salmonTimer -= delta;
 		if (salmonTimer <= 0) currentFill = timeFill;
 	}
 	
 	public void gotSalmon() {
 		salmonTimer = 250;
 		System.out.println("Hi");
 		currentFill = salmonTimeFill;
 	}
 	
 	private String getTimeString(Time t) {
 		if (t == null || t.getMillis() == Integer.MAX_VALUE) return "N/A";
 		return String.format("%.2f", t.getMillis() / 1000f);
 	}
 
 }
