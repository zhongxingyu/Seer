 package controller;
 
 
 import model.Game;
 import model.HighScore;
 import model.NewHighscore;
 
 import view.NewHighscoreView;
 import model.NewHighscore;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.gui.AbstractComponent;
 import org.newdawn.slick.gui.ComponentListener;
 import org.newdawn.slick.gui.TextField;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 
 public class NewHighscoreController extends BasicGameState implements ComponentListener {
 	private GameController gameController;
 	private NewHighscoreView newHighscoreView;
 	private StateBasedGame sbg;
 	private GameContainer gc;
 	
 	public NewHighscoreController(GameController gameController) {
 		this.gameController = gameController;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {						
 		this.sbg = sbg;	
 	}
 	
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		super.enter(container, game);
		this.newHighscoreView = new NewHighscoreView(container, this.gameController.getGame().getInGame().getPlayer().getScore());
 		this.newHighscoreView.getTextField().addListener(this);
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		this.newHighscoreView.render(gc, sbg, g);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		this.gc = gc;
 	}
 	
 	public void keyPressed (int key, char c) {
 		if(key == Input.KEY_F) {
 			this.gameController.changeFullscreen(this.gc);
 		}
 	}
 
 	@Override
 	public int getID() {
 		return NewHighscore.STATE_ID;
 	}
 
 	@Override
 	public void componentActivated(AbstractComponent textField) {
 		this.gameController.getGame().saveScore(this.gameController.getGame().getInGame().getPlayer().getScore(),
 				this.newHighscoreView.getTextField().getText());
 		textField.setFocus(false);
 		sbg.enterState(HighScore.STATE_ID);
 		
 	}
 
 }
