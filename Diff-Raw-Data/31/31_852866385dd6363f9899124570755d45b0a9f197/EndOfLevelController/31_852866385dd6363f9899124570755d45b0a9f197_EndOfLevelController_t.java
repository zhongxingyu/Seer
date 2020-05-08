 package controller;
 
 
 
 import model.EndOfLevel;
 import model.HighScore;
 import model.InGame;
 import model.NewHighscore;
 import model.StartMenu;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import utils.LevelUtils;
 import utils.SaveUtils;
 import view.EndOfLevelView;
 
 public class EndOfLevelController extends BasicGameState{
 	private EndOfLevelView endOflevelView;
 	private final GameController gameController;
 	private StateBasedGame sbg;
 	private GameContainer gc;
 	private boolean gameOver;
 	private boolean victory;
 	private boolean newHighScore;
 	
 	public EndOfLevelController (final GameController gameController) {
 		super();
 		this.gameController = gameController;
 		this.gameOver = false;
 		this.victory = false;
 		this.newHighScore = false;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		this.sbg = sbg;
 		this.gc = gc;
 	}
 	
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		super.enter(gc, game);
 		
 		//Checks if there's any more levels
 		this.victory = LevelUtils.getNbrOfFiles(this.gameController.getGame().getInGame().getLevel() + 1) == 0;
 		//Checks if the game is lost
 		this.gameOver = (this.gameController.getGame().getInGame().isGameOver());
 		//Checks if there's a new higscore
 		this.newHighScore = (this.gameController.getGame().getInGame().getPlayer().getScore() > SaveUtils.getScoreList()[9]);
 		
 		this.endOflevelView = new EndOfLevelView(this.gameController.getGame().getInGame().getPlayer().getScore(), gameOver, victory);
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		this.endOflevelView.render(gc, sbg, g);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 	}
 	
 	@Override
 	public void keyPressed (int key, char c) {
 		if(key == Input.KEY_TAB) {
 			this.gameController.changeFullscreen(this.gc);
 		} else if (key == Input.KEY_ESCAPE) {
 			sbg.enterState(StartMenu.STATE_ID);
 		} else if (key == Input.KEY_ENTER) {
 			 if (this.newHighScore && (this.gameOver || this.victory)) {
 				sbg.enterState(NewHighscore.STATE_ID);
 			} else if (this.gameOver || this.victory) {
 				sbg.enterState(HighScore.STATE_ID);
 			} else {
 				sbg.enterState(InGame.STATE_ID);
 			}
 		}
 	}
 
 	@Override
 	public int getID() {
 		return EndOfLevel.STATE_ID;
 	}
 
 }
