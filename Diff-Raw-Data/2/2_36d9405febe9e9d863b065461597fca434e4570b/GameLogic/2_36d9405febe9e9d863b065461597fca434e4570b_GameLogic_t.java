 package yotris.logic;
 import java.util.Observable;
 import yotris.ui.UserInterface;
 import yotris.util.Settings;
 
 public class GameLogic extends Observable {
 	private UserInterface ui;
 	private Grid grid;
 	private Settings settings;
 	private Piece fallingPiece;
 	private int score;
 	
 	public GameLogic(UserInterface ui, Settings settings)	 {
 		this.ui = ui;
 		this.settings = settings;
 		this.reset(settings);

		addObserver(ui);
 	}
 
 	private void reset(Settings settings) {
 		this.grid = new Grid(settings.getGridWidth(), settings.getGridHeight());	
 		fallingPiece = null;
 		score = 0;
 	}
 
 	public GameState update() {
 		GameState state = new GameState(true);	
 		state.running = false;
 
 		Grid renderGrid = getRenderGrid();
 		state.renderGrid = renderGrid;
 
 		notifyObservers(state);
 
 		return state;
 	}
 
 	public Grid getRenderGrid() {
 		Grid renderGrid = new Grid(this.grid.getWidth(), this.grid.getHeight());
 		renderGrid.plotPiece(fallingPiece);
 		return renderGrid;
 	}
 
 }
