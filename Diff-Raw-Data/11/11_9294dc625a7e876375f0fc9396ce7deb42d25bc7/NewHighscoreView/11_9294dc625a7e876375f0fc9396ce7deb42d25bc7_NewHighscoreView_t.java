 package view;
 
 import java.awt.Font;
 
 import model.Game;
 import model.NewHighscore;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.gui.TextField;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class NewHighscoreView {
 	
 	private TextField textField;
 	private TrueTypeFont font;
 	private int score;
	private final int POSX = Game.WINDOW_WIDTH/5;
 	
 	public NewHighscoreView(GameContainer gc, int score) {
 		this.font = new TrueTypeFont(new Font(Font.MONOSPACED, Font.BOLD, 50), false);
		this.textField = new TextField(gc, font, POSX + 330, Game.WINDOW_HEIGHT/2, 300, font.getHeight() + 5);
 		this.textField.setMaxLength(8);
		textField.setBorderColor(Color.transparent);
 		this.score = score;
 	}
 
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {		
 		g.setFont(font);
 		g.setColor(Color.green);
		g.drawString("New highscore: " + this.score + "!", POSX, Game.WINDOW_HEIGHT/4);
		g.drawString("Enter name:", POSX, Game.WINDOW_HEIGHT/2);
 		
 		this.textField.setFocus(true);
 		this.textField.render(gc, g);
 	}
 
 	public TextField getTextField() {
 		return textField;
 	}
 }
