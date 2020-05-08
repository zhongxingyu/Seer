 import com.google.gson.JsonArray;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 /**
  * The help menu of the game.
  * @since 5/26/13
  * @version 1
  * @author alex
  */
 public class HelpMenuState extends BasicGameState {
 	private JsonArray help;
 	private Image backImg;
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		help = new GameConfig("loc/help_en.json").getArray();
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
 		g.drawImage(backImg, 0, 0);
 		g.setColor(Color.white);
 		g.setFont(MainMenuState.font);
 		int y = 50;
 		for (int i = 0; i < help.size(); i++) {
 			g.drawString(help.get(i).getAsString(), gc.getWidth()/2 - MainMenuState.font.getWidth(help.get(i).getAsString())/2, y);
 			y += MainMenuState.font.getHeight(help.get(i).getAsString());
 		}
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame s, int i) throws SlickException {
 		if(gc.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
 			s.enterState(1); // Jump to main menu!
 		}
 	}
	
	public void setBackImg(Image img) {
		this.backImg = img;
	}
 
 	@Override
 	public int getID() {
 		return 3;
 	}
 }
