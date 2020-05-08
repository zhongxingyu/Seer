 	package states;
 
 import input.Controls;
 import input.Controls.Action;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.font.effects.Effect;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.util.FontUtils;
 
 import time.Timer;
 import ui.Transitions;
 import config.Config;
 import config.Section;
 
 public class ResultsState extends BasicGameState {
 	private enum Grade {
 		S("assets/images/ui/grades/S.png", new Color(255, 215, 0)), 
 		A("assets/images/ui/grades/A.png", new Color(0, 198, 100)), 
 		B("assets/images/ui/grades/B.png", new Color(150, 75, 0)), 
 		C("assets/images/ui/grades/C.png", new Color(229, 0, 0));
 	
 		private Image img;
 		
 		private Grade(String imgPath, Color color) {
 			try {
 				this.img = new Image(imgPath);
 				img.setImageColor(color.r, color.g, color.b);
 			} catch (SlickException e) {
 				e.printStackTrace();
 			}
 		}
 		
		@SuppressWarnings("unused")
 		public Image getImage() {
			return getImage((float) Math.PI / 2);
 		}
 		
		public Image getImage(float jiggle) {
			img.setRotation(25f *  (float) Math.cos(jiggle));
			return img;
 		}
 	};
 
 	public static final int ID = 2;
 	private static final int SPACING = 65;
 	private static final float S_THRESHOLD = 1.0f;
 	private static final float A_THRESHOLD = 1.25f;
 	private static final float B_THRESHOLD = 2.0f;
 	private static final List<Section> completedSections = new LinkedList<Section>();
 	private List<Grade> grades;
 	private UnicodeFont smallFont;
 	private Image background;
 	private int counter;
 	private static final int PULSE_RATE = 500;
 	private static float jigglin = 0;
 	
 	@SuppressWarnings("unchecked")
 	public ResultsState() throws SlickException {
 		smallFont = Config.MENU_FONT;
         ((List<Effect>) smallFont.getEffects()).add(new ColorEffect(java.awt.Color.WHITE));
         counter = 0;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame game) throws SlickException {
 		grades = new LinkedList<Grade>();
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics graphics) throws SlickException {
 		graphics.setColor(Color.white);
 		int sectionX = 350;
 		int goalX = 500;
 		int timeX = 650;
 		int gradeX = 750;
 		int bestX = 916;
 		int bestGradeX = 1016;
 		int y = (Config.RESOLUTION_HEIGHT / 2) - (SPACING * (grades.size() + 2) / 2);
 		
 		drawBackground(graphics);
 		graphics.setColor(new Color(0, 0, 0, 128));
 		graphics.fillRect(0, 0, Config.RESOLUTION_WIDTH, Config.RESOLUTION_HEIGHT);
 		
 		FontUtils.drawCenter(smallFont, "Stage", sectionX, y, 0);
 		FontUtils.drawCenter(smallFont, "Goal",  goalX,    y, 0);
 		FontUtils.drawCenter(smallFont, "Time",  timeX,    y, 0);
 		FontUtils.drawCenter(smallFont, "Rank",  gradeX,   y, 0);
 		FontUtils.drawCenter(smallFont, "Best",  bestX,    y, 0);
 		FontUtils.drawCenter(smallFont, "Rank",  bestGradeX,   y, 0);
 		y += SPACING;
 		
 		for (int i = 0; i < grades.size(); i++) {
 			Section s = completedSections.get(i);
 			Timer t = Config.times.get(s);
 			String goal = String.format("%.2fs", s.getGoalTime() / 1000f);
 			String time = String.format("%.2fs", t.getLastTime().getMillis() / 1000f);
 			Grade grade = grades.get(i);
 			String best = String.format("%.2fs", t.getBestTime().getMillis() / 1000f);
 			Grade bestGrade = getGrade(t.getBestTime().getMillis(), t.getGoal().getMillis());
 			
 			FontUtils.drawCenter(smallFont, s.getDisplayName(), sectionX, y, 0);
 			FontUtils.drawCenter(smallFont, goal, goalX, y, 0);
 			FontUtils.drawCenter(smallFont, time, timeX, y, 0);
 			FontUtils.drawCenter(smallFont, best, bestX, y, 0);
			grade.getImage(jigglin).drawCentered(gradeX, y+20);
			bestGrade.getImage(jigglin).drawCentered(bestGradeX, y+20);
 			y += SPACING;
 		}
 		
 		String helpText = "Press Left Click to Continue" + ".....".substring(0, counter / PULSE_RATE + 1);
 		FontUtils.drawCenter(smallFont, helpText, Config.RESOLUTION_WIDTH / 2, y, 0);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta) throws SlickException {
 		Controls.update(gc);
 		
		jigglin += delta/100f;
 		counter = (counter + delta) % (5 * PULSE_RATE);
 		if (Controls.isKeyPressed(Action.FIRE)) {
 			game.enterState(IntroState.ID, Transitions.fadeOut(), Transitions.fadeIn());
 		}
 	}
 
 	@Override
 	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
 		super.enter(container, game);
 		grades.clear();
 		for (Section s : completedSections) {
 			int time = Config.times.get(s).getLastTime().getMillis();
 			grades.add(getGrade(time, s.getGoalTime()));
 		}
 		Section lastSection = completedSections.get(completedSections.size() - 1);
 		background = new Image(lastSection.getBackgroundPath());
 		background = background.getScaledCopy((float) Config.RESOLUTION_HEIGHT / background.getHeight());
 	}
 
 	@Override
 	public void leave(GameContainer gc, StateBasedGame game) throws SlickException {
 		super.leave(gc, game);
 		completedSections.clear();
 	}
 	
 	public static void recordResult(Section section) {
 		completedSections.add(section);
 	}
 	
 	public static void clearResults() {
 		completedSections.clear();
 	}
 	
 	private Grade getGrade(int time, int goal) {
 		if (time <= S_THRESHOLD * goal) return Grade.S;
 		else if (time <= A_THRESHOLD * goal) return Grade.A;
 		else if (time <= B_THRESHOLD * goal) return Grade.B;
 		else return Grade.C;
 	}
 	
 	private void drawBackground(Graphics graphics) {
 		int backgroundX = 0;
 		while (backgroundX < Config.RESOLUTION_WIDTH){
 			graphics.drawImage(background,  backgroundX, 0);
 			backgroundX += background.getWidth();
 		}
 	}
 
 	@Override
 	public int getID() {
 		return ID;
 	}
 
 }
