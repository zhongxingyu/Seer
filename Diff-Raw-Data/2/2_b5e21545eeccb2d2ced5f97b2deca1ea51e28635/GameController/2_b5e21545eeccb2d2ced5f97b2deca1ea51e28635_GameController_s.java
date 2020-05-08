 package twelve.team;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 
 import twelve.team.Board.moveType;
 import twelve.team.Piece.Team;
 import twelve.team.Settings.GameType;
 
 
 interface GameControllerListener {
 	public void onNextTurn();
 	public void onTimeUp();
 	public void onGameWin(Team winner);
 	public void newGame();
 }
 
 //class for managing gameplay and game logic
 //receives move info from vBoard and processes
 //tells vBoard what to change visually
 //this will be the interface for client-server model. messages will be decoded and processed here
 //before the visual changes are sent to vBoard.
 public class GameController implements GameTimerListener {
 	
 	private Board board;
 	private VisualBoard vBoard;
 	private GameTimer gameTimer;
 	private int turnsPlayed = 0;
 	//private boolean player1Turn; //player1 is user
 
 	private int whiteWins = 0;
     private int blackWins = 0;
     private Settings settings;
     private NetworkGame network;
     
     private Team currentTurn = Team.WHITE;
     
     //For network games and listeners
     private ArrayList<GameControllerListener> listeners = new ArrayList<GameControllerListener>();
     private ArrayList<Move> oldMoves = new ArrayList<Move>();
     private ArrayList<Move> moves = new ArrayList<Move>();
     
    //Disable for no debuging
     private boolean debug = true;
 	
 	
 	public GameController() {
 		settings = new Settings();
 		board = new Board();
 		vBoard = new VisualBoard(this);
 		gameTimer = new GameTimer(15000);
 		gameTimer.setActionListener(this);
 	}
 	
 	public void reset(){
 		board.resetBoard();
 		vBoard.updateBoard();
 		gameTimer.reset();
 		currentTurn = Team.WHITE;
 		
 		for(GameControllerListener list : listeners){
 			list.newGame();
 		}
 	}
 	
 	public void startGame() {
 		showBoard();
 		gameTimer.run();
 	}
 	
 	public void setupGame(){
 		showOptions(false);
 		if(settings.gameType == GameType.MULT_SERVER){
 			network = new NetworkGame(this, true, true);
 		} else if(settings.gameType == GameType.MULT_CLIENT){
 			network = new NetworkGame(this, false, true);
 		} else {
 			network = new NetworkGame(this, false, false);
 		}
 		network.showConnectionSettings();
 		network.start();
 	}
 	
 	public void showSplash() {
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
     	SplashPanel splashscreen = new SplashPanel();
     	int x = (dim.width - splashscreen.getWidth())/2;
     	int y = (dim.height - splashscreen.getHeight())/2;
     	splashscreen.setLocation(x,y);
     	splashscreen.setVisible(true);
     	long StartTime = System.currentTimeMillis();
     	long Timer = 5000;
     	long ElapsedTime = System.currentTimeMillis()- StartTime;
     	setupGame();
     	while(ElapsedTime <= Timer)
     	{
     		ElapsedTime = System.currentTimeMillis() - StartTime;
     	}
     	splashscreen.setVisible(false);
 	}
 	
 	public void showBoard(){
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		int x = (dim.width - vBoard.getWidth())/2;
     	int y = (dim.height - vBoard.getHeight())/2;
     	vBoard.setLocation(x,y);
     	
     	java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
             	vBoard.setVisible(true);
             }
         });
 	}
 	
 	public void resetScore() {
 		whiteWins = 0;
 		blackWins = 0;
 	}
 	
 	public void showOptions(boolean cancelEnabled) {
 		SettingsDialog dialog = new SettingsDialog(new javax.swing.JFrame(), true, this, cancelEnabled);
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
     	int x = (dim.width - dialog.getWidth())/2;
     	int y = (dim.height - dialog.getHeight())/2;
     	dialog.setLocation(x,y);
         dialog.addWindowListener(new java.awt.event.WindowAdapter() {
             @Override
             public void windowClosing(java.awt.event.WindowEvent e) {
                 //Settings may have changed
             }
         });
         dialog.setVisible(true);
 	}
 	
 	public void updateSettings(Settings set) {
 		this.settings = set;
 		board = new Board(settings.boardHeight, settings.boardWidth);
 		vBoard.dispose();
 		vBoard = new VisualBoard(this);
 	}
 	
 	public Settings getSettings() {
 		return settings;
 	}
 	
 	public static void main(String args[]) {
     	
 		final GameController controller = new GameController();      
 		controller.showSplash();
 		controller.startGame();
         
         /*java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
             	controller.startGame();
             }
         });*/
         
         
         
     }
 	
 	public Team getTurn(){
 		return currentTurn;
 	}
 	
 	public int getWins(Team team){
 		if(team == Team.BLACK)
 			return blackWins;
 		else
 			return whiteWins;
 	}
 	
 	public void incrementWins(Team team){
 		if(team == Team.BLACK)
 			blackWins++;
 		else
 			whiteWins++;
 	}
 	
 	public boolean move(Point start, Point end) throws MoveException, Exception{
 		return move(start, end, moveType.NONE);
 	}
 	
 	public boolean move(Point start, Point end, moveType type) throws MoveException, Exception{
 		boolean move = false;
 		if(type == moveType.NONE && board.isPaika(start, end)){
 			move = board.move(start, end, type);
 			moves.add(new Move(start, end, moveType.PAIKA));
 		} else {
 			move = board.move(start, end, type);
 			moves.add(new Move(start, end, type));
 		}
 		
 		if(!move){ //turn is over
 			turnsPlayed++;
 			oldMoves = new ArrayList<Move>(moves);
 			//oldMoves = moves;
 			moves.clear();
 			changeTurn();
 			
 			gameTimer.reset();
 			for(GameControllerListener listener : listeners){
 				listener.onNextTurn();
 			}
 			
 			System.out.println("About to check gameover");
 			
 			Team winner = board.isGameOver();
 			if(winner != null){
 				incrementWins(winner);
 				for(GameControllerListener listener : listeners){
 					listener.onGameWin(winner);
 				}
 				PlayAgain dial = new PlayAgain(new JFrame(), true, winner);
 				dial.setVisible(true);
 				if(dial.playAgain()){
 					board.resetBoard();
 					for(GameControllerListener listener : listeners){
 						listener.newGame();
 					}
 				} else {
 					System.exit(0);
 				}
 			}
 			if(turnsPlayed > settings.boardHeight*10){
 				for(GameControllerListener listener : listeners){
 					listener.onGameWin(null);
 				}
 				PlayAgain dial = new PlayAgain(new JFrame(), true, winner);
 				dial.setVisible(true);
 				if(dial.playAgain()){
 					board.resetBoard();
 					for(GameControllerListener listener : listeners){
 						listener.newGame();
 					}
 				} else {
 					System.exit(0);
 				}
 			}
 		}
 		vBoard.updateBoard();
 		return move;
 	}
 	
 	public ArrayList<Move> getMoves(){
 		return oldMoves;
 	}
 	
 	/*
 	 * Returns the Board
 	 */
 	public Board getBoard(){
 		return board;
 	}
 	
 	public void addGameControllerListener(GameControllerListener listener){
 		listeners.add(listener);
 	}
 
 	private void changeTurn() {
 		currentTurn = (currentTurn == Team.WHITE)	? currentTurn = Team.BLACK : Team.WHITE;
 		String text = currentTurn == Team.WHITE ? "White" : "Black";
 		vBoard.setTurn(text);
 	}
 	
 	/*
 	 * Event called when Game Timer Expires
 	 * @see twelve.team.GameTimerListener#TimesUp()
 	 */
 	@Override
 	public void TimesUp() {
 		final TimesUp panel = new TimesUp(new JFrame(), true);				
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		int x = (dim.width - panel.getWidth())/2;
     	int y = (dim.height - panel.getHeight())/2;
     	panel.setLocation(x,y);
     	panel.setVisible(true);
 		gameTimer.reset();
 		changeTurn();
 	}
 
 	/*
 	 * Event called when the seconds on the GameTimer is decreased
 	 * @see twelve.team.GameTimerListener#secondDecrease(int)
 	 */
 	@Override
 	public void secondDecrease(int timeLeft) {
 		vBoard.setTimer(timeLeft);
 	}
 	
 	//For Debugging purposes
 	public void debug(String message){
 		if(debug){
 			System.out.println(message);
 		}
 	}
 	
 };
