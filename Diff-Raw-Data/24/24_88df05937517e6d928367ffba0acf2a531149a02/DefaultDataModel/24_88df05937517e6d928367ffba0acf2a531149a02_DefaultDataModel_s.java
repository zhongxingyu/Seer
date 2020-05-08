 package backgammon.model;
 
 import backgammon.app.GameSettings;
 import backgammon.listener.IModelEventListener;
 import backgammon.model.board.DefaultBackgammonBoard;
 import backgammon.model.interfaces.IBackgammonBoard;
 import backgammon.model.interfaces.IDataController;
 import backgammon.model.interfaces.IPlayer;
 import backgammon.model.player.ComputerPlayer;
 import backgammon.model.player.HumanPlayer;
 import backgammon.model.player.Move;
 
 public class DefaultDataModel implements IDataController {
 	
 	private IModelEventListener listener;
 	private GameSettings settings;
 	
 	private IPlayer player1;
 	private IPlayer player2;
 	private IBackgammonBoard gameBoard;
 	
 	public DefaultDataModel(GameSettings currentSettings) {
 		this.settings = currentSettings;
 		this.gameBoard = new DefaultBackgammonBoard();
		initPlayer(player1, this.settings.getNamePlayer1(), this.settings.getSelectedKIModePlayer1());
		initPlayer(player2, this.settings.getNamePlayer2(), this.settings.getSelectedKIModePlayer2());
	}
	
	private void initPlayer(IPlayer player, String name, GameSettings.KIMode mode) {
		if (mode.equals(GameSettings.KIMode.HUMAN)) {
			player = new HumanPlayer(name, this);
 		} else {
			player = new ComputerPlayer(name, this, mode);
 		}
 	}
 		
 	public void addDataModelListener(IModelEventListener listener) {
 		
 		this.listener = listener;
 	}
 	
 	public void startGame() {
 		
 		player1.init();
 		player2.init();
 		
 		// EVENT 
 		
 /*		int beginnerResult;
 		do {
 			beginnerResult = player1.rollDice(1, 6) - player2.rollDice(1, 6);
 			
 			// EVENT RESULT EVEN
 			
 		} while (beginnerResult == 0);
 		
 		IPlayer firstPlayer = (beginnerResult > 0) ? (player1) : (player2);
 		
 		// EVENT RESULT BEGINNER FIRSTPLAYER(int playerID)
 		
 		
 		while (firstPlayer.move() < 0) {
 			
 			// EVENT MOVE ILLEGAL(int playerID)
 			
 		}*/
 		
 		
 	}
 
 	public IBackgammonBoard getBackgammonBoard() {
 		return this.gameBoard;
 	}
 	
 	public int registerMove(Move newMove) {
 		return 0;
 	}
 
 }
