 package states;
 
 import java.awt.Font;
 import java.util.ArrayList;
 
 import game.Game;
 import game.MenuButton;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.fills.GradientFill;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 
 public class HelpState extends BasicGameState{
 	public static final int ID = 6;
 	private static final int TRANSITION_DELAY = 0;
 	
 	private MenuButton backButton;
 	private ArrayList<MenuButton> buttons;
 	private Image player, house, enemy;
 	private TrueTypeFont ttfBold, ttfPlain;
 	private int playerX, playerY, houseX, houseY, enemyX, enemyY;
 	private String helpText1, helpText2, helpText3;
 
 	
 	
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sb)
 			throws SlickException {
 		
 		ttfBold = new TrueTypeFont(new Font("Verdana", Font.BOLD, 30), true);
 		ttfPlain = new TrueTypeFont(new Font("Verdana", Font.PLAIN, 22	), true);
 		player = new Image("res/sprites/hero/hero1.png");
 		house = new Image("res/sprites/house.png");
 		enemy = new Image("res/sprites/enemies/enemy.png");
 		
 		playerX = Game.centerWidth - player.getWidth()*7;
 		playerY = Game.centerHeight/2;
 		houseX = Game.centerWidth - house.getWidth()/2;
 		houseY = Game.centerHeight/2;
 		enemyX = Game.centerWidth + enemy.getWidth()*10;
 		enemyY = Game.centerHeight/2;
 		
		helpText1 = "1. The goal of this game is to destroy the incoming waves of enemies and to keep you and your house safe.";
		helpText2 = "2. You will lose if the enemies kill you or destroy your house.";
		helpText3 = "3. Control your player with 'W,A,S,D' or 'ARROW KEYS'. Shoot bullets with 'LEFT MOUSEBUTTON'.";
		
 		
 		buttons = new ArrayList<MenuButton>();
 		backButton = new MenuButton("backButton", new Vector2f(Game.centerWidth - 100,
 				Game.centerHeight + 375), new Image("res/buttons/back.png"));
 		buttons.add(backButton);
 		
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sb, Graphics g)
 			throws SlickException {
 		
 		sb.getState(InGameState.ID).render(gc, sb, g);
 		Rectangle r = new Rectangle(0, 0, Game.app.getWidth(), Game.app.getHeight());
 		GradientFill rFill = new GradientFill(0,0,new Color(0, 0, 0, 255),Game.app.getWidth(), Game.app.getHeight(),new Color(0, 0, 0, 255));
 		g.fill(r, rFill);
 		
 		for(MenuButton button: buttons){
 			button.render(gc, sb, g);
 		}
 		
 		drawHelp(g);
 		
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta)
 			throws SlickException {
 		for(MenuButton button: buttons){
 			button.update(gc, sb, delta);
 		}
 		
 		if (backButton.isMousePressed()) {
 			sb.enterState(MenuState.ID, new FadeOutTransition(Color.black, TRANSITION_DELAY), new FadeInTransition(Color.black, TRANSITION_DELAY));
 		}
 	}
 
 	@Override
 	public int getID() {
 		return ID;
 	}
 	
 	private void drawHelp(Graphics g) {
 		
 		
 		//images
 		g.drawImage(player, playerX, playerY);
 		g.drawImage(house, houseX, houseY);
 		g.drawImage(enemy, enemyX, enemyY);
 		
 		//headlines
		ttfBold.drawString(playerX, playerY - 50, "You");
 		ttfBold.drawString(houseX + house.getWidth()/4, houseY - 50, "House");
 		ttfBold.drawString(enemyX - enemy.getWidth()/4, enemyY - 50, "Enemy");
 		
 		//text
		ttfPlain.drawString(playerX, playerY + house.getHeight()*1.5f, helpText1, Color.red);
		ttfPlain.drawString(playerX, playerY + house.getHeight()*1.5f + 50, helpText2, Color.red);
		ttfPlain.drawString(playerX, playerY + house.getHeight()*1.5f + 100, helpText3, Color.red);
 	}
 
 }
