 package controller;
 
 import model.Game;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import view.EndOfLevelView;
 
 public class EndOfLevelController extends BasicGameState{
 	private EndOfLevelView endOflevelView;
 	private GameController gameController;
 	private StateBasedGame sbg;
 	private GameContainer gc;
 	private boolean gameOver;
 	private boolean victory;
 	private boolean newHighScore;
 	
 	public EndOfLevelController (GameController gameController) {
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
 	
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		super.enter(container, game);
 		
 		//kolla om det finns fler banor?
 		this.victory = this.gameController.getInGameController().getNbrOfFiles(this.gameController.getInGameController().getLevel() + 1) == 0;
 		//kolla om spelaren förlorat
 		this.gameOver = (this.gameController.getInGameController().isGameOver());
 		//kolla om newHighScore
 		this.newHighScore = (this.gameController.getInGameController().getPlayerController().getPlayer().getScore() > this.gameController.getScoreList()[9]);
 			
 		this.endOflevelView = new EndOfLevelView(this.gameController.getInGameController().getPlayerController().getPlayer().getScore(),
 				gameOver, victory);
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
 	
 	public void keyPressed (int key, char c) {
 		if (key == Input.KEY_ESCAPE) {
 			sbg.enterState(Game.START_MENU);
 		} else if (key == Input.KEY_ENTER) {
			 if (this.newHighScore) {
 				sbg.enterState(Game.NEW_HIGHSCORE);
 			} else if (this.gameOver || this.victory) {
 				sbg.enterState(Game.HIGHSCORE);
 			} else {
 				sbg.enterState(Game.IN_GAME);
 			}
 			
 			
 		}
 	}
 
 	@Override
 	public int getID() {
 		return Game.END_OF_LEVEL;
 	}
 
 }
