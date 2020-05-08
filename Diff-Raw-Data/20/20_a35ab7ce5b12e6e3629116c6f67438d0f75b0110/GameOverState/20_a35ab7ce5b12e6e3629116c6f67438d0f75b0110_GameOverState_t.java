 package spaceshooters.states;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import spaceshooters.fx.Star;
 import spaceshooters.gui.font.Font;
 import spaceshooters.main.Spaceshooters;
 import spaceshooters.util.save.SaveData;
 
 /**
  * The game comes here after player dies in singleplayer.
  * 
  * @author Mat
  * 
  */
 public class GameOverState extends BasicGameState {
 	
 	public static int score = 0;
	public static int highscore = 0;
 	private Font font;
 	private Image background;
 	private ArrayList<Star> stars = new ArrayList<>();
 	private Random random = new Random();
 	
 	@Override
 	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
 		for (int i = 0; i < 12; i++) {
 			Star s = new Star(random.nextInt(container.getWidth()), random.nextInt(container.getHeight()));
 			stars.add(s);
 		}
		highscore = SaveData.getSaveData().getInt("player.highscore");
 	}
 	
 	@Override
 	public void init(GameContainer container, StateBasedGame game) throws SlickException {
 		background = new Image(Spaceshooters.getGraphicsFolder() + "menu/over.png");
 		font = new Font(Spaceshooters.getFont(), 24);
 	}
 	
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
 		g.setFont(font);
 		
 		for (Star s : stars) {
 			s.getTexture().draw(s.getX(), s.getY());
 		}
 		background.draw();
 		
 		g.drawString("Highscore: " + highscore, 650 - (font.getWidth("Highscore: " + highscore) / 2), 150);
 		g.drawString("Score: " + score, 150 - (font.getWidth("Score: " + score) / 2), 150);
 		g.drawString("Press Enter to restart!", 400 - (font.getWidth("Press Enter to restart!") / 2), 250);
 		g.drawString("Press Escape to go to main menu!", 400 - (font.getWidth("Press Escape to go to main menu!") / 2), 300);
 	}
 	
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
 		Input i = container.getInput();
 		
 		if (i.isKeyPressed(Input.KEY_ESCAPE)) {
 			game.enterState(States.MAIN_MENU.ordinal());
 		}
 		if (i.isKeyPressed(Input.KEY_ENTER)) {
 			game.enterState(States.GAMEPLAY.ordinal());
 		}
 	}
 	
 	@Override
 	public void leave(GameContainer container, StateBasedGame game) throws SlickException {
 		stars.clear();
 	}
 	
 	@Override
 	public int getID() {
 		return States.GAME_OVER.ordinal();
 	}
 }
