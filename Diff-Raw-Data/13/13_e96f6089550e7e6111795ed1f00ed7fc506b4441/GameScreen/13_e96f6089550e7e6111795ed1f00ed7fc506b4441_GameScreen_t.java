 
 package it.chalmers.tendu.screens;
 
 import it.chalmers.tendu.Tendu;
 import it.chalmers.tendu.controllers.InputController;
 import it.chalmers.tendu.defaults.Constants;
 import it.chalmers.tendu.gamemodel.MiniGame;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 
 /**Abstract screen class that can be extended by all minigame and menu screens*/
 public abstract class GameScreen {
 	public Tendu game; //reference to the main Tendu object
 	public MiniGame model; //model of current minigame
 	private ShapeRenderer shapeRenderer; // used to render vector graphics
 	private int count; // used to count renders for events that should be
 	// displayed a short time.
 	
 	/**
 	 * @param game Tendu object that creates the screen
 	 * @param model MiniGame model, set to null if not used (menu)
 	 * @return a new GameScreen
 	 */
 	public GameScreen(Tendu game, MiniGame model) {
 		this.game = game;
 		this.model = model;
 		if(model != null) {
 			model.startGame();
 		}
 		shapeRenderer = new ShapeRenderer();
 	}
 
 	/**
 	 * Call to change current screen to a new screen
 	 * Only use when the current screen is finished
 	 * @param screen the new screen */
 	protected void setScreen(GameScreen screen) {
 		game.setScreen(screen);
 	}
 
 	/** all rendering goes here **/
 	public void render(){
 		//model.checkGame();
 		
 		shapeRenderer.setProjectionMatrix(game.getCamera().combined);
 		shapeRenderer.begin(ShapeType.FilledRectangle);
 		if (count == 0) {
 			shapeRenderer.setColor(Color.YELLOW);
 		}else{
 			shapeRenderer.setColor(Color.RED);
 			count --;
 		}
 		//Gdx.app.log("Quota", calculateTimerWidth() + "");
		//shapeRenderer.filledRect(50, 50, calculateTimerWidth(), 6);
 		shapeRenderer.end();
 	}
 
 	/** All game logic goes here */
 	public abstract void tick(InputController input);
 
 	/** clean up goes here
 	 * 	make sure to call super() if overriden
 	 */
 	public void removed() {
 	}
 	
 	private int calculateTimerWidth() {
 		double quota = (double) model.getTimeLeft()
 				/ (double) model.getGameTime();
 		double timerWitdth = Math.abs(quota * (Constants.SCREEN_WIDTH-100));
 		return (int) timerWitdth;
 
 	}
 	
 	//TODO maybe the model could remove time without involving the screen?
 	/**
 	 * Removes time for the user and shows this 
 	 * buy changing color on the timer.
 	 * @param time
 	 */
 	public void loseTime(int time){
 		count=60;
 		model.changeTimeWith(-time);
 	}
 	
 }
