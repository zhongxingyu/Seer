 package tetris;
 
 import java.awt.Color;
 import java.awt.Font;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class GameOverState extends BasicGameState {
 	private int stateID = 2;
 	private Image background;
 	private UnicodeFont font;
 	private FontButton[] buttons;
 
 	public GameOverState(int stateID) throws SlickException {
 		this.stateID = stateID;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void init(GameContainer container, final StateBasedGame game)
 			throws SlickException {
 		background = new Image("images/gameover.png");
 		font = new UnicodeFont(new java.awt.Font("Verdana", Font.BOLD, 40));
 		font.getEffects().add(new ColorEffect(new Color(211, 211, 211)));
 		font.addNeheGlyphs();
 		font.loadGlyphs();
 
 		buttons = new FontButton[2];
 		buttons[0] = new FontButton(container, font, "STATISTICS", 260, 310, game,
 				stateID) {
 			@Override
 			public void perform() {
				ChartDrawer test = new ChartDrawer(1);
 				test.reveal();
 			}
 		};
 		
 		buttons[1] = new FontButton(container, font, "BACK", 330, 370,
 				game, stateID) {
 			@Override
 			public void perform() {
 				game.enterState(Game.MAINMENUSTATE);
 			}
 		};
 
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 		background.draw(0, 0);
 		
 		for (FontButton button : buttons) {
 			button.render(container, g);
 		}
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta)
 			throws SlickException {
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 }
