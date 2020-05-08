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
 	private NewHighscore newHighscore;
 	private int score;
 	
 	public NewHighscoreView(GameContainer gc, int score) {
 		this.font = new TrueTypeFont(new Font(Font.MONOSPACED, Font.BOLD, 50), false);
 		this.textField = new TextField(gc, font, Game.WINDOW_WIDTH/4, Game.WINDOW_HEIGHT/2, 300, 50);
 		this.textField.setMaxLength(8);
 	}
 
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {		
 		g.setFont(font);
 		g.setColor(Color.green);
 		g.drawString("New highscore: " + this.score + "!", Game.WINDOW_WIDTH/4, Game.WINDOW_HEIGHT/4);
 		g.drawString("Enter name:", this.textField.getX(), this.textField.getY()-this.textField.getHeight());
 		
 		this.textField.setFocus(true);
 		this.textField.render(gc, g);
 	}
 
 	public TextField getTextField() {
 		return textField;
 	}
 }
